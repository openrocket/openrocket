# 3D Flight Path Export

## Overview

OpenRocket can export a simulated flight as a geographic track: KML for Google Earth, a
waypoint CSV, a GPX track, or any format a user writes a template for. The feature lives on a
**3D Path** tab in the simulation configuration dialog.

No GPS log is involved. A simulation already records how far the rocket has traveled from the
pad at every time step, so the export turns that displacement into coordinates starting from
the launch site. The exported track therefore carries the simulated wind drift, including the
separate drift of spent stages.

The design separates three concerns that change for different reasons:

| Layer | Responsibility |
|---|---|
| `FlightPathExportOptions` | What the user asked for |
| `FlightPathModelBuilder` → `FlightPathModel` | What the flight was, in export terms, independent of file format |
| `FlightPathTemplate` + `FlightPathExporter` | How that becomes bytes in a particular format |

Adding a format means adding a template. It does not mean touching Java.

---

## Where the code is

Core, in `core/src/main/java/info/openrocket/core/file/flightpath/`:

| File | Role |
|---|---|
| `FlightPathExportOptions.java` | User choices, plus the `Waypoint`, `StageTrackStart` and `AltitudeReference` enums |
| `FlightPathModel.java` | The format-agnostic model a template renders against |
| `FlightPathModelBuilder.java` | Turns a simulation plus options into that model |
| `FlightPathExporter.java` | Renders a template against the model and writes it |
| `FlightPathTemplate.java` | An immutable template value object |
| `FlightPathTemplateRepository.java` | Finds built-in and user templates |

Templates, in `core/src/main/resources/templates/flightpath/`: `flightpath.kml.mustache`,
`waypoints.csv.mustache`, `flightpath.gpx.mustache`.

Swing, in `swing/src/main/java/info/openrocket/swing/gui/simulation/`:
`SimulationFlightPathExportPanel.java` (the tab) and `FlightPathColorDialog.java` (per stage
colors). `SimulationConfigDialog.java` hosts the tab.

Two one-line build changes: `com.samskivert:jmustache:1.16` in `core/build.gradle`, and in
`module-info.java` a `requires com.samskivert.jmustache` plus an
`exports info.openrocket.core.file.flightpath`. The core module is declared `open`, so
JMustache's reflective access into the model needs no extra `opens`. There is no
`requires transitive`, so Swing does not carry jmustache on its module path.

---

## The pipeline

```
Simulation + FlightData + FlightPathExportOptions
        |
        v
FlightPathModelBuilder.build()
        |
        v
FlightPathModel            (branches -> waypoints + path points)
        |
        v
FlightPathExporter.export(template, stream)
        |
        v
Mustache render -> UTF-8 bytes
```

The model is built fresh on every export call, and the Mustache compiler is rebuilt with it
because the escaper depends on the chosen template's extension.

---

## Coordinates

### Rebuilt from displacement, not read from the simulation

The builder prefers `TYPE_POSITION_X` and `TYPE_POSITION_Y`, meters east and north of the pad,
over the simulation's own `TYPE_LATITUDE` and `TYPE_LONGITUDE`.

This is not a stylistic choice. A saved `.ork` file stores latitude and longitude rounded to
three decimal places, which in degrees is about 94 m. A whole flight then collapses onto two or
three positions and the track comes out as a staircase of right angles. The same rounding
applied to a displacement in meters leaves it accurate to a millimeter. A simulation loaded from
a file counts as up to date and is never re-run, so the rounded data is the ordinary case rather
than a corner one.

When a branch carries no position data the builder falls back to the simulated coordinates, and
rescales them if the export origin differs from the simulation's launch point.

Degree lengths are WGS84 at the origin latitude, good to a few centimeters per kilometer:

```
metersPerDegreeLatitude  = 111132.92 - 559.82 cos 2φ + 1.175 cos 4φ
metersPerDegreeLongitude = 111412.84 cos φ - 93.5 cos 3φ
```

### An unset launch position

Both coordinates at exactly zero is OpenRocket's "not set", not a real position in the Gulf of
Guinea. The export substitutes the Kennedy Space Center, `EXPORT_FALLBACK_LATITUDE` 28.61 and
`EXPORT_FALLBACK_LONGITUDE` -80.6, and the panel warns before writing.

A single zero is a real coordinate, so a site on the equator or the prime meridian is exported
where the user put it. `FlightPathExporter.hasLaunchPosition()` encodes the same rule for the
panel's warning. Nothing here writes back to the simulation; only the exported coordinates
change, and the shape of the flight is correct either way.

### Precision and ordering

Coordinates that a reader sees are formatted to a fixed six decimal places, about a tenth of a
meter. The raw doubles print whatever digits they happen to need, so one file would say `-80.6`
where another says `-97.4966`, which reads as though the two were surveyed to different
accuracies.

Every coordinate pair in a balloon is marked `(lat, lon)`. KML's own `<coordinates>` are
longitude first, so a bare pair inside a KML file is read by people with a standing reason to
read it backwards, and at most launch sites both readings land somewhere plausible.

---

## Altitude reference

`AltitudeReference` has four values, three of which map to a KML `<altitudeMode>`:

| Value | KML mode | Meaning |
|---|---|---|
| `AUTOMATIC` | resolved | Decide from the simulation's launch altitude |
| `GROUND` | `relativeToGround` | Heights above the terrain |
| `SEA_LEVEL` | `absolute` | True elevations |
| `CLAMPED` | `clampToGround` | Geometry laid flat on the terrain |

The problem this exists to solve: OpenRocket's launch altitude defaults to zero. A site that is
really 1200 m up then reports its flight in meters above the pad, and placing that against sea
level draws the whole track 1200 m underground, where Google Earth shows nothing at all.
`AUTOMATIC` resolves to `SEA_LEVEL` only when the simulation carries a non-zero launch altitude,
and to `GROUND` otherwise.

The track and the waypoints carry **separate** references, because they want different answers.
A flight is worth seeing suspended in the air; the pins that label it are easier to read against
the ground they sit over.

Two consequences fall out of the KML spec:

- A clamped track is tessellated. KML honors `<tessellate>` only on a clamped line, and without
  it a clamped path cuts straight through hills instead of draping over them.
- The shadow, KML's `<extrude>`, is dropped for whichever half is already clamped, since there
  is nothing left to extrude to. The panel disables the checkbox when both halves are clamped.

---

## Staged flights

A branch created at stage separation begins life as a **verbatim copy of its parent's points**,
so the ascent the stages flew bolted together is repeated in every later branch. Exporting that
prefix again would draw the shared ascent once per stage and attribute the stack's flight to a
stage that was not yet flying on its own.

`StageTrackStart` chooses what to do about it. `SEPARATION`, the default, trims the prefix so
the shared ascent is drawn once and each stage's peaks are its own. `PAD` keeps it so every
stage reads as a complete flight, at the cost of drawing the ascent once per stage.

The same copying drives three more rules:

- **Pad and liftoff** are emitted on the primary branch only. The stack leaves the pad as one
  vehicle, and naming the event after a branch would claim the sustainer left the pad under its
  own power.
- **Peak velocity and acceleration** are scanned from the separation index, so a spent booster
  reports its own peaks rather than the whole stack's.
- **Maximum range** is scanned over the whole branch, shared ascent included. This is a
  deliberate difference from the peaks: a peak velocity is a claim about what that stage did,
  while a range is a claim about where that airframe went.
- **Burnout** is qualified with the stage that actually burned out rather than the branch it was
  recorded in, because a booster's burnout is also present in the sustainer's branch.

---

## Naming

Two mechanisms, applied in order.

`prefix(qualifier, label)` puts a qualifier in front of a label, unless there is no qualifier or
the label already begins with it. `qualify(...)` is the same thing gated on the flight having
more than one branch, used for stage names: without it every stage contributes an identically
named Apogee, Burnout and Landing.

The skip-if-already-prefixed guard is load-bearing. A booster chute actually named
`Booster Chute` yields `Booster Chute Ejection`, not `Booster Booster Chute Ejection`.

**Recovery pins** are named for the event and for the device: `Drogue Ejection`, `Main Ejection`.
A dual-deployment flight sets off two of these hundreds of meters apart, and named for the event
alone both markers read `Ejection` and cannot be told apart on the map, which is where the names
are drawn. The qualifier keeps the label's own capitalization rather than lowercasing it, which
would be wrong in languages that capitalize the noun.

**A mission name** is prefixed to the document title and to each branch name, so that several
files loaded into one Google Earth session do not all present a `Sustainer` and a
`Simulation 1`. It reaches the waypoints only if asked, because a near-vertical flight already
packs its markers into a small patch of screen. It is not remembered between exports: a stale
one would quietly mislabel the next file. It is skipped when the text already starts with it, so
a mission named after the rocket does not read `Sod Blaster Sod Blaster Sustainer`.

---

## Colors

Each stage carries three independent colors: flight path line, ground track, and waypoint pin.
Each is either the user's choice or that stage's palette entry, and none is computed from the
others.

`BRANCH_COLORS` is the same palette the plot window assigns to its series, so a stage keeps its
color whether you look at it in a graph or on a map. `GROUND_COLORS` is a separate palette
rather than a shade of the first: seen from directly overhead a ground track sits underneath its
own flight path, so entry *i* there is picked to contrast with entry *i* here, and the values are
saturated enough to hold up over aerial imagery, which is what a ground track is read against.
Pins default to the flight path color.

KML color literals are `aabbggrr`: alpha first and the channels in the opposite order to the
usual web notation.

A waypoint style is written only when it has something in it: a tinted pin, or a hidden label.
With plain pins and visible labels the stage gets no `<Style>` and its pins no `<styleUrl>`,
rather than an empty style that every pin points at.

---

## Balloons

Google Earth shows a feature's `description` in a balloon when it is clicked. Three levels:

**Document**: rocket, configuration, launch site, peak altitude, velocity and acceleration,
maximum range, time to apogee, flight time, and one line per stage saying where it came down.

**Stage folder**: that stage's own maximum range and landing.

**Waypoint**: time since liftoff, altitude above the pad and above sea level, distance and
bearing from the pad, coordinates, and for an ejection the device that deployed.

No `<Snippet>` is written. An empty `<Snippet maxLines="0"/>` was meant to keep Google Earth
from printing the opening lines of each description under the feature's name in the places tree,
but Google Earth Pro showed the description lines anyway, and Google Earth for web reports
`maxLines` as an unsupported element when loading the file. Google Earth for web does not show
`<description>` or `<Snippet>` text in its project panel or its placemark dialog at all, so the
balloons are only visible in Google Earth Pro. They are kept for that viewer.

Where the balloon opens differs by viewer, which matters when testing. Google Earth Pro opens a
waypoint balloon from a click in the 3D view, but opens the document and folder balloons only
from their names in the Places panel. Google Earth for web has no balloons; the only place it
exposes a description is the raw HTML in the placemark's Edit dialog.

`includeDescriptions` switches the whole lot off.

### Markup is pre-escaped, not wrapped in CDATA

A KML `description` holds HTML, and the two usual ways to write it are not equivalent here.

The template engine escapes every value it substitutes, because those values are user-supplied
names that would otherwise break the XML. Inside a `CDATA` block those escapes are never decoded,
so a rocket named `Bill & Ted` would reach the balloon as `Bill &amp; Ted`, visible to the reader
as literal entity text. A `CDATA` block is also breakable: a name containing `]]>` terminates it
early and produces an invalid document.

Writing the markup pre-escaped instead, as `&lt;b&gt;` rather than `<b>`, makes the escaping
consistent. Template markup and substituted values are each escaped exactly once, the XML parser
decodes them together, and the balloon receives the HTML the template intended and the name the
user typed. The degree sign is a literal UTF-8 character rather than `&deg;` for the same reason,
to avoid a second layer of entity decoding.

This trap is specific to text templating. An implementation that built the KML through a DOM
would get the behavior for free.

### Optional lines vanish rather than render empty

The Mustache compiler is configured with `emptyStringIsFalse(true)` and `zeroIsFalse(true)`, so
a value can be used as a section and the line around it disappears when there is nothing to say.
Five values use this: the configuration when the rocket has none, the sea level altitude when the
launch altitude was never set, the time to apogee and the flight time when there is none to
report, and the recovery device on a waypoint that is not an ejection. The landing lines for a
stage that never landed vanish the same way, but through the `hasLanding` flag rather than an
empty value.

Without it, a flight from a site with no recorded elevation would report every waypoint as being
some height "above sea level" that is really its height above the pad, which is exactly the class
of error the automatic altitude reference exists to prevent.

---

## Summary values

Flight level: peak altitude, velocity and acceleration, maximum range, time to apogee, flight
time. Per stage: maximum range in meters and in display units, a `hasLanding` flag, and the
landing's distance, bearing, time and coordinates.

Three points worth knowing:

**Maximum range is not the landing distance.** A rocket can drift downrange under the chute and
then partway back, so the farthest point from the pad is often not where it lands. The maximum is
the range safety figure; the landing is a separate fact. Both are exported.

**A flight that never landed must not report a landing.** This looks impossible and is not. In
`BasicEventSimulationEngine`, a run that reaches the maximum simulation time ends with a
`SIMULATION_END` event and no `GROUND_HIT`, and an aborted run, such as one where no motor fired,
produces neither. The landing values sit behind `hasLanding` rather than defaulting to the last
data point. The landing is scanned from the branch's events directly rather than read back out of
the generated waypoints, so it is still known when the user has switched the landing marker off.

**Time to apogee is effectively never empty.** `FlightData` computes it by finding the highest
recorded altitude, not by looking for an apogee event, so a flight cut short while still climbing
reports its last point. An assertion that a truncated flight has no time to apogee will fail.

---

## Templates and rendering

`FlightPathTemplate` is an immutable value object: a stable `id` used to remember the user's
selection, a `displayName` for the dropdown, an `extension`, the raw Mustache `source`, and a
`builtIn` flag. Its `toString()` returns the display name so it drops straight into a Swing
model. It has no `equals`, so identity comparisons go through `getId()`.

The `extension` does double duty: it picks the escaper and it forces the saved file's extension.

`FlightPathExporter.escaperFor(extension)`:

| Extension | Escaper |
|---|---|
| `kml`, `gpx`, `xml` | XML entities, ampersand replaced first |
| `csv` | Quotes doubled; templates quote each field themselves |
| anything else | None, raw output |

The ampersand must be replaced first because the replacements are applied in sequence, and a
later `&` rule would double-escape the entities the earlier rules produced.

The whole document is rendered into memory, then written UTF-8. The stream is deliberately not
closed, so ownership stays with the caller, which makes the explicit `flush()` the thing that
guarantees the bytes land.

### Template discovery

`FlightPathTemplateRepository.getTemplates()` returns the three built-ins in display order
followed by user templates. It builds a fresh list on every call and caches nothing, so edits to
the user directory appear the next time the dialog opens.

| id | Display name | Extension |
|---|---|---|
| `kml` | KML (Google Earth) | `kml` |
| `waypoints-csv` | Waypoint CSV | `csv` |
| `gpx` | GPX track | `gpx` |

A built-in whose classpath resource fails to load is logged at WARN and skipped, so a packaging
mistake drops one format rather than breaking the tab.

User templates live in an `ExportTemplates` folder inside the OpenRocket user directory
(`%APPDATA%\OpenRocket` on Windows, `~/Library/Application Support/OpenRocket` on macOS,
`~/.openrocket` on Linux). The repository does not create the folder. Any file ending
`.mustache`, matched case-insensitively, is picked up and sorted case-insensitively by filename.

The filename convention is `<name>.<ext>.mustache`. The id is the full filename, the display name
is the part before the last inner dot, and the extension is the part after it. With no inner dot
the extension defaults to `txt`, which means no escaping at all. An unreadable file is logged and
skipped while the rest still load.

### What the other two templates emit

The **waypoint CSV** is three lines of template. It emits one quoted row per waypoint in a
Google My Maps shape, with `symbol` and `label_color` hard-coded to `pushpin` and `white`, `color`
set to the stage's pin color as `#rrggbb` (or `yellow` when pin coloring is off), and a
composed `name` column carrying the rocket, configuration, label, altitude, distance and bearing. It uses the fixed six-decimal coordinate strings. It contains no continuous
path, which is what makes it small enough to hand round.

The **GPX track** emits a flat list of `<wpt>` elements across all branches, since GPX has no
waypoint grouping, then one `<trk>` per branch guarded by `hasPath`. Both use
`altitudeMslMeters`, because GPX `<ele>` is defined as meters above sea level. This is why the
model carries altitude above sea level separately from altitude above ground and from the
KML-mode altitude.

---

## The user interface

The tab is reached through Simulations, then editing a simulation, then the **3D Path** tab of
the configuration dialog, which is `THREED_PATH_IDX = 5`. It is enabled only when the simulation
has data and this is not a multi-simulation edit, reusing the export tab's disabled tooltips. On
this tab the dialog's OK button reads **Export** and Cancel reads **Close**, and pressing Export
runs the export without closing the dialog, matching the CSV export tab.

### Layout

An **Output format** box holding the template dropdown, then five boxes:

| Box | Contents |
|---|---|
| Units | Altitude and distance unit pickers |
| Mission | Name field, and whether it prefixes waypoints |
| Placements | The three preset toggles, the two altitude references, the shadow checkbox |
| Waypoints | Eight waypoint checkboxes in three columns, then show names, color pins, summary balloons |
| Flight path | Flight path line, ground track, keep every Nth point, stage track start, stage colors button |

Every row inside a box is a nested panel with its own grid rather than cells in one big grid,
because spanning controls across a single grid makes MigLayout under-report the width needed and
Swing then ellipsizes the labels. For the same reason a `textWidth()` constraint adds a few
pixels of slack to every label: the bundled Inter font measures narrower through `FontMetrics`,
which is what sizes a label, than the look and feel paints it, and Swing does not trim a
fraction of a pixel, it drops whole characters. A regression test asserts that no label in the
panel is truncated at the layout's own width.

### Placements

The three presets are `Drift cast`, `Flight path` and `Landing plots`. They set the controls
below them rather than acting behind them, so the panel always shows what the file will contain
and a preset can be taken as a starting point. Each states the **whole** set of controls it
covers rather than only narrowing, so any preset is reachable from any other; a preset that only
narrowed would be a one-way door.

They are toggle buttons in a group, and the group is kept honest: `syncPresetSelection()` selects
the preset whose every value matches the current controls, or clears the selection when none
does. It is wired to every control a preset covers, including each waypoint checkbox, and runs
after preferences load. A fresh panel matches `Flight path` exactly, so that is both the default
state and a visibly selected one, which also means the preset's values and the `load()` fallbacks
have to agree.

No preset turns on the shadow. Under a single pin the extrude is a plumb line and reads as a
position; under the whole length of an arcing flight path it is a solid wall that buries the
flight it is meant to explain. The checkbox remains for the case it is good at.

### Stage colors

A modal dialog with one row per stage and three swatches per row. All swatches share a single
`JColorChooser`, so the recent-colors list is common across stages, which is what you want when
picking a set of colors that have to work together. Reset puts every swatch back to its palette
default without closing the dialog. On OK, `collect()` omits any swatch still equal to its
default, so an untouched dialog changes nothing and a stage that was never customized keeps
following the palette.

### Preferences

Everything is remembered under `userRoot/OpenRocket/FlightPathExport`: the selected template id,
both units, all eight waypoint checkboxes, the geometry toggles, the stride, both altitude
references, the stage track start, the shadow, the marker options, the balloons, and the stage
colors as `index=rrggbb` pairs.

Two deliberate exceptions. The **mission name is not persisted**, because it describes one
particular flight rather than how the user likes the exporter set up. And **nothing is stored
until a file is actually written**, so canceling the save dialog leaves the remembered settings
alone. A corrupt color pair is skipped silently, costing one color rather than the whole tab.

### Export flow

1. Refuse with an explanation if the simulation has no data.
2. Ask for a filename, with a filter built from the template's display name and extension.
3. Force the template's extension onto the chosen name, then confirm an overwrite.
4. Build the options, store the preferences, remember the format.
5. If the simulation has no launch position, warn that the track will be placed at the Kennedy
   Space Center, that the simulation itself is not modified, and that the flight's shape is
   still correct. Anything but OK aborts.
6. Write on a `SwingWorker` through `SwingWorkerDialog`, mirroring the CSV export: a quick write
   finishes silently, a long one raises a modal progress dialog. Canceling deletes the partial
   file. An I/O failure is reported with the underlying message.

A ticked but disabled shadow checkbox exports `false`, so the geometry never disagrees with what
the panel shows.

---

## Testing

`FlightPathExportTest` holds 56 tests over the core pipeline, and
`SimulationFlightPathExportPanelTest` holds 5 over the panel. Between them they cover template
discovery, all three output shapes, both altitude references and their automatic resolution,
tessellation and the shadow, staged flights and the shared-ascent trim, path striding, the
waypoint selection, mission naming, all three per-stage colors, the coordinate rebuild under
saved-file rounding, the unset launch position, balloon contents and their switch, coordinate
precision and ordering, dual deployment naming, and XML and CSV escaping.

Two tests are worth knowing about specifically. One renders a rocket named with an ampersand, an
apostrophe and angle brackets, then parses the output with a DOM parser and checks the name comes
back intact inside the intended HTML, which proves both the escaping rule and that the document
stays well formed. Another counts the `(lat, lon)` markers against the number of coordinate pairs
in the file, so an unmarked pair cannot be added later without failing.

The panel tests run headless against a real panel instance and assert the starting placement, that
no placement draws the shadow, that the highlight clears on a manual edit and returns when the
edit is undone, and that no placement touches the balloons.

---

## Sharp edges

Things that are correct today and easy to break:

- **Combo order is load-bearing.** Both the altitude reference and stage track start combos map
  the selected index onto an enum ordinal. Reordering either the combo items or the enum
  constants silently changes what gets exported.
- **The preset definitions and the `load()` fallbacks must agree.** If they drift, the panel
  opens in a state that no preset button claims, and the highlight starts empty.
- **`getTemplates()` order matters.** `getDefault()` returns the first element, documented as
  KML. If the KML resource ever failed to load it would return the CSV template instead.
- **User template display names are not deduplicated.** Two user files with the same base name
  and different extensions both appear under one name, distinguishable only by the file filter in
  the save dialog. Selection is by index and the preference matches the unique id, so nothing
  breaks, but the dropdown is ambiguous.
- **A genuine zero is falsey inside a section**, a consequence of `zeroIsFalse`. That is what
  makes the optional lines vanish, but it means a template cannot use a section to test for the
  presence of a value that might legitimately be zero.
- `doExport()` carries a javadoc line claiming it shows a confirmation dialog on success. It does
  not; only the return value reports success.

---

## Extending it

A new output format is a Mustache file in `ExportTemplates` named `<name>.<ext>.mustache`. It
appears in the dropdown the next time the tab opens. The full token reference lives in the user
guide, under **Creating your own templates** in
`docs/source/user_guide/advanced_flight_simulation.rst`, which also documents the escaping rule
and the section behavior. The model exposes public fields and public no-arg methods, so
`{{altitude}}` and `{{#hasPath}}` both resolve reflectively.

Three things a new template should respect:

- KML wants coordinates in the order longitude, latitude, altitude. GPX names them in `lat` and
  `lon` attributes, so order does not matter there.
- Pair each `altitudeKmlMeters` with the matching `kmlAltitudeMode`, or the geometry ends up
  measured against the wrong datum.
- Use the fixed-precision coordinate strings for anything a person reads, and the raw doubles
  only for machine-read geometry.
