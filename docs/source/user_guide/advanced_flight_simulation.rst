**************************
Advanced Flight Simulation
**************************

OpenRocket offers more advanced options for simulating flight. You can plot your rocket's predicted acceleration, climb,
eject and landing, make a prediction for how far downrange and in which direction your flight will land, and even
experiment with different models of Earth's geometry, as it affects your flight. Once you're satisfied with a sim, you
can export your data for analysis and charting in other packages.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Plotting your rocket's flight
=============================

To begin learning about OpenRocket's plotting features, first, click the :guilabel:`Plot / Export` button on the :guilabel:`Flight simulations` window.

.. figure:: /img/user_guide/advanced_flight_simulation/PlotExportButton.png
   :width: 400 px
   :align: center
   :figclass: or-image-border
   :alt: The Plot / export Button.

On the **Edit simulation** panel, you'll see tabs marked **Plot data** and **Export data**.

Plotting data
-------------

The :guilabel:`Plot data` tab opens first. Here you can define many parameters that will determine what values are plotted, and
what events are marked on the plot.

.. figure:: /img/user_guide/advanced_flight_simulation/PlotExportWindow.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Plot / export window.

Here you'll be able to quickly choose from a number of standard plots:

.. figure:: /img/user_guide/advanced_flight_simulation/PlotConfigs.png
   :width: 806 px
   :align: center
   :figclass: or-image-border
   :alt: Standard plots

You'll also be able to assign to the X and Y axes any one of over 50 parameters. If you click on the plot variable
dropdown, you'll see a search box and a list of variable categories. You can either scroll through the categories
to find the parameter you want, or type in the search box to filter the list:

.. figure:: /img/user_guide/advanced_flight_simulation/ChoosePlotVariable.png
   :width: 806 px
   :align: center
   :figclass: or-image-border
   :alt: Select plot variable

   Select a plot variable from the variable groups (left), or search for the desired variable (right).

The parameters are categorized in the
following groups:

- **Time**: Variables related to time
- **Position and Motion**: Variables related to the position and motion of the rocket (e.g. altitude, position, velocity, acceleration)
- **Orientation**: Variables related to the orientation of the rocket (e.g. pitch, yaw, roll)
- **Mass and Inertia**: Variables related to the mass and inertia
- **Stability**: Variables related to the stability of the rocket (e.g. CG, CP, stability margin)
- **Thrust and Drag**: Variables related to the thrust and drag (e.g. thrust, TWR, drag)
- **Coefficients**: Variables related to the calculation coefficients (e.g. normal force coefficient, roll moment coefficient)
- **Atmospheric Conditions**: Variables related to the atmospheric conditions (e.g. air pressure, wind velocity)
- **Characteristic Numbers**: Variables related to the characteristic numbers (e.g. Mach number, Reynolds number)
- **Reference Values**: Variables related to the reference values (e.g. reference area, reference length)
- **Simulation Information**: Variables related to the simulation information (e.g. simulation time step)
- **Custom**: *(User-defined parameters)*




You can assign multiple parameters to the Y-axis, and choose whether their scales appear on the left, or the right side
of the plot. You can add Y-axis parameters with the :guilabel:`New Y-axis plot type` button, or delete parameters from the plot
with the :guilabel:`X` buttons. (*The X-axis takes only a single plotted parameter, typically* **Time**).

Additionally, you can choose from several flight events, any or all of which can be called out on your plot, in reference
to the simulated time of occurrence.

.. figure:: /img/user_guide/advanced_flight_simulation/YaxisTypes.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: Setting Y-axes and Events for plotting

Plotted results
---------------

Below you can see a plot of *A simple model rocket*, simulation number 4, flying on a C6-5. Note that the five events
checked in the above screen have been marked on the plot (*some very close to each other, or to the edge*):
**Motor ignition**, **Motor burnout**, **Apogee**, **Recovery device deployment**, and **Ground hit**.

You can also see that the three Y-axis parameters described above: **Altitude**, **Vertical velocity**, and
**Vertical acceleration** appear as lines of three different colors.

.. figure:: /img/user_guide/advanced_flight_simulation/PlotOfSimulation.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: A Plot of the simulation.

As your rockets get more complex, with features like dual-deploy, air-start and multiple stages, your plots can grow in
complexity to simulate their expected behavior. Below is a plot (*from the example rockets*) of a "High Power Airstart"
rocket, modeled after a Patriot missile. The central motor starts on the launch pad, while the surrounding motors start
while the rocket is in the air (*hence, an "airstart"*). The plot records the separate motor start events, and the
deployment of both a drogue, and a main parachute.

.. figure:: /img/user_guide/advanced_flight_simulation/ComplexPlot.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: A Plot of Sim #5 of the "High Power Airstart" example rocket.

Notice what's happening in the plot above: The rocket is *losing velocity* - the blue line - before the airstart occurs.
This is probably not what we want.

However, simulation number 3 of the same rocket, below, has an earlier airstart, and looks like it should work as expected.
Looking at the slight wiggle in the velocity curve, we could also try another simulation to provide a little bit more
margin for error.

.. figure:: /img/user_guide/advanced_flight_simulation/ComplexPlot2.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: A Plot of Sim #3 of the "High Power Airstart" example rocket.

----

Launch Conditions and Simulation Options
========================================

From the :guilabel:`Plot data` window, you can click the :guilabel:`<< Edit:guilabel:` button to configure :guilabel:`Launch conditions`, and
:guilabel:`Simulation options` before you plot.

Launch conditions
-----------------

OpenRocket can simulate conditions at the launch site, so you can estimate how winds will direct your flight, and how
far downrange your rocket will drift.

In the screen shown below, you can set parameters (and units) for wind, and for your **Launch site**, you can set the
**Latitude**, **Longitude** and **Altitude**, as well as **Atmospheric conditions**. Note that Atmospheric conditions
affect your rocket's ascent velocity, as well as the local `Speed of Sound <https://en.wikipedia.org/wiki/Speed_of_sound>`__.

This is also the panel where you can set the length of your launch rod or rail. This length will affect whether your
simulation *passes or fails*, when it's evaluated for minimum speed off the rod.

.. figure:: /img/user_guide/advanced_flight_simulation/EditSimulationLaunchCond.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Edit simulation window: Launch conditions.

Simulation options
------------------

In the :guilabel:`Simulation options` tab, the :guilabel:`Simulator options` let you choose the shape of the simulated Earth in your
calculations (*doing so* **does not** *affect the Earth background in Photo Studio*), and you can choose the time-resolution
of the simulation. This is also the place where you add and set up **Simulation extensions**, which are beyond this
guide's purpose.

.. figure:: /img/user_guide/advanced_flight_simulation/EditSimulationSimOpts.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Edit simulation window: Simulation options.

   The Simulation options in the simulation configuration window

Aerodynamic lookup tables
--------------------------

OpenRocket normally uses the Extended Barrowman method to calculate aerodynamic forces (drag and stability) based on
your rocket's geometry. However, you can override these calculations by providing custom aerodynamic data from wind
tunnel tests, CFD simulations, or other sources using CSV lookup tables.

To configure lookup tables, edit a simulation and navigate to the :guilabel:`Simulation options`.
Then, under the :guilabel:`Aerodynamic data` section, click the :guilabel:`Configure...`.

Drag lookup tables
~~~~~~~~~~~~~~~~~~~

Drag lookup tables allow you to specify custom drag coefficients (Cd) as a function of Mach number and optionally angle
of attack (AoA). The CSV file must include:

- A **Mach** column (required) - Mach number values
- An **AoA** column (optional) - Angle of attack in degrees
- A **Cd** column (required) - Drag coefficient values

Example drag lookup table:

.. code-block:: text

   Mach,AoA,Cd
   0.0,0,0.25
   0.0,5,0.30
   0.0,10,0.35
   0.5,0,0.30
   0.5,5,0.36
   0.5,10,0.42
   1.0,0,0.35
   1.0,5,0.40
   1.0,10,0.45

If you don't include an AoA column, the table will only interpolate based on Mach number:

.. code-block:: text

   Mach,Cd
   0.0,0.25
   0.5,0.30
   1.0,0.35

Stability lookup tables
~~~~~~~~~~~~~~~~~~~~~~~

Stability lookup tables allow you to specify custom stability coefficients as a function of Mach number and optionally
angle of attack. The CSV file must include:

- A **Mach** column (required) - Mach number values
- An **AoA** column (optional) - Angle of attack in degrees
- A **Cn** column (required) - Normal force coefficient
- A **Cm** column (required) - Pitching moment coefficient
- A **Cp** column (required) - Center of pressure position (in meters from the nose)

Example stability lookup table:

.. code-block:: text

   Mach,AoA,Cn,Cm,Cp
   0.0,0,0.10,0.01,0.50
   0.0,5,0.15,0.02,0.52
   0.0,10,0.20,0.03,0.55
   0.5,0,0.12,0.015,0.51
   0.5,5,0.18,0.025,0.53
   0.5,10,0.25,0.035,0.56
   1.0,0,0.15,0.02,0.52
   1.0,5,0.22,0.03,0.54
   1.0,10,0.30,0.04,0.58

CSV file format
~~~~~~~~~~~~~~~

- **Header row**: The first non-empty, non-comment line must contain column names
- **Column names**: Case-insensitive, spaces and underscores are ignored. "Angle of Attack" or "AoA" both work
- **Comments**: Lines starting with ``#`` are preserved in the file but ignored during parsing
- **Blank lines**: Empty lines are preserved in the file but ignored during parsing
- **Separator**: Comma (``,``) is the default separator, but you can configure semicolon, tab, or space separators
- **Interpolation**: Values are linearly interpolated between table points in both Mach and AoA dimensions
- **Clamping**: Values outside the table range are clamped to the nearest edge value

Editing lookup table data
~~~~~~~~~~~~~~~~~~~~~~~~~

Once you've loaded a CSV file, you can edit the data directly in the configuration dialog. The text area shows the loaded data, which you can modify as needed. Your edits are automatically saved to the `.ork` file when you click :guilabel:`OK`.

**Important features:**

- **Data embedding**: The CSV data is embedded directly in the `.ork` file, so your rocket design is self-contained and portable
- **Edit preservation**: Any edits you make are preserved when you close and reopen the dialog or simulation window
- **Comment preservation**: Comment lines (starting with ``#``) are preserved in your edits
- **Refresh button**: Use the refresh button (↻) next to :guilabel:`Load from file...` to reload the original CSV file if it still exists on disk
- **Field separator**: You can change the CSV field separator (comma, semicolon, tab, or space) - the example format updates automatically

When lookup tables are used:

- Individual component forces are set to zero (only total forces are calculated from the table)
- The axial drag conversion uses the same polynomial as the Barrowman method
- Stall margin is calculated from the maximum AoA in the stability table (if AoA data is present)
- Damping moments are set to zero

When to use lookup tables
~~~~~~~~~~~~~~~~~~~~~~~~~~

Lookup tables are useful when:

- You have wind tunnel test data for your specific rocket design
- You have CFD simulation results that you want to use in OpenRocket
- You want to validate OpenRocket's Barrowman calculations against experimental data
- Your rocket has complex aerodynamic behavior not well-captured by the Barrowman method
- You need angle-of-attack dependent coefficients beyond what Barrowman provides

Note that when lookup tables are configured, they completely replace the Barrowman calculations for drag and/or stability.
You cannot mix lookup table data with Barrowman calculations.

----

Exporting Data
==============

Located on the :guilabel:`Plot / export panel`, the :guilabel:`Export Data tab` (shown below) helps you set up a
Comma-Separated Value (.csv) formatted file to export data from your simulations. You can export any or all of over
50 values (generally speaking, the list of parameters above, plus **Coriolis acceleration**). Optional **Comments**
sections list any flight events (**Apogee**, for example) you selected for your simulation, as well as description and
field descriptions.

You can choose separators other than comma, if you prefer semicolon, space, or TAB-delimited data. Once you have your
data choices set up, clicking the :guilabel:`Export` button brings up a file dialog to choose a filename and location
for your exported data.

.. figure:: /img/user_guide/advanced_flight_simulation/ExportData.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Export data window.

   The Export data window.

----

Exporting the 3D flight path
============================

In addition to the CSV data export, OpenRocket can export the flight's path as a
geographic track that can be opened in mapping tools such as Google Earth. This is done
from the :guilabel:`3D Path` tab of the simulation edit dialog.

OpenRocket does not need a GPS log for this. A simulation already records how far the rocket
has travelled from the pad at every time step, and the export turns that into coordinates
starting from the launch site. The exported track therefore reflects the wind drift and the
drift of separated stages.

.. note::

   The coordinates are rebuilt from the distance travelled rather than read from the
   simulation's own latitude and longitude columns. Those are stored in a saved file rounded
   to three decimal places, which in degrees is about 94 m: a whole flight collapses onto two
   or three positions and the track comes out as a staircase of right angles. The same
   rounding applied to a distance in meters leaves it accurate to a millimeter. This matters
   because a simulation loaded from a file counts as up to date and is never re-run, so its
   stored data is what gets exported.

Because the track is built from the launch coordinates, set a launch latitude and longitude
on the :guilabel:`Launch conditions` tab first. If both are left at zero -- which is
OpenRocket's "not set" rather than a real position in the Gulf of Guinea -- the exported file
falls back to the Kennedy Space Center (28.61, -80.6), and the tab warns you before writing it.
A single zero is a real coordinate, so a site on the equator or the prime meridian is exported
where you put it. This affects the exported file only: the simulation's own launch position is
never changed, and the shape of the flight is exported correctly either way.

It is worth setting the launch **altitude** on the same tab as well. OpenRocket leaves it at
zero by default, which is wrong for most launch sites and affects both the simulation itself
and where the track is drawn. See `Altitude reference`_.

Exporting a flight path
-----------------------

#. Run the simulation so that it has flight data.
#. On the :guilabel:`Launch conditions` tab, set the launch site latitude and longitude.
#. Open the simulation and select the :guilabel:`3D Path` tab.
#. Choose an output format, adjust the options described below, and click
   :guilabel:`Export`.
#. Choose where to save the file. For KML, open the resulting file in Google Earth
   (see `Viewing the track in Google Earth`_).

.. figure:: /img/user_guide/advanced_flight_simulation/ThreeDPathExport.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The 3D Path export tab.

   The 3D Path export tab.

Options
-------

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Option
     - Description
   * - Format
     - The output format. The built-in choices are **KML (Google Earth)**, **Waypoint
       CSV** and **GPX track**, followed by any custom templates you have installed.
   * - Altitude / Distance units
     - The units used for the altitude and distance values that appear in labels and in
       the waypoint CSV. Coordinate altitudes are always written in meters, as KML and GPX
       require.
   * - Presets
     - Three one-click placements -- **Drift cast**, **Flight path** and **Landing plots** --
       which set the controls below rather than acting behind them, so the panel always shows what
       the file will contain and a preset can be taken as a starting point.
   * - Track altitude from / Waypoint altitude from
     - What the exported altitudes are measured from, set separately for the path and the markers.
       See `Altitude reference`_.
   * - Draw shadow down to the ground
     - Adds a curtain under the track and a plumb line under each marker, so you can read where a
       point in the air sits on the map. Disabled once both are clamped, since there is nothing
       left to draw.
   * - Waypoints
     - Which points of interest to mark: pad, liftoff, burnout, apogee, ejection, landing,
       maximum velocity, and maximum acceleration. Every recovery device that deploys
       produces its own marker, all of them named *Ejection*; the deploying component's
       name is available to templates but is not shown on the map.
   * - Show waypoint names
     - Draw each marker's name next to it. A near-vertical flight packs its waypoints into
       a small patch of screen and the names then overlap each other, so clearing this
       exports bare markers that show their name when clicked.
   * - Color pins by stage
     - Tint each stage's markers to match its track, so a marker can be attributed at a
       glance. This uses a pin image fetched from Google's servers the first time the file
       is opened; clear it for a file that has to render without a network, and the markers
       fall back to the viewer's default.
   * - Flight path line
     - Include the airborne path.
   * - Ground track
     - Include the path projected straight down onto the ground. It is drawn in a darkened
       shade of its stage's color, so a bright line is always in the air and a dark one is
       always on the ground.
   * - Keep every Nth point
     - Thins the path line by keeping only every Nth simulation step. Use this to reduce
       the file size of long flights. The waypoint markers are not affected.
   * - Stage tracks
     - Where each stage's track begins on a staged flight. See `Staged flights`_. Disabled
       for a single-stage flight, which has nothing to divide up.

The selected format and options are remembered for the next export.

Altitude reference
------------------

The :guilabel:`Track altitude from` and :guilabel:`Waypoint altitude from` settings decide
whether the exported altitudes are heights above the terrain, heights above sea level, or
ignored entirely. This matters more than it sounds, because OpenRocket's launch altitude
defaults to zero:

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Setting
     - Description
   * - Automatic
     - The default. Uses sea level when the launch conditions carry a launch altitude, and
       heights above the terrain when it is left at zero.
   * - Above ground
     - Hangs the track off the terrain. Correct whatever the launch altitude says, but each
       point is measured from the ground directly beneath it, so a flight that drifts over
       broken terrain has its path bent to follow the ground profile.
   * - Above sea level
     - Places the track at its true elevation, which is the geometrically faithful
       trajectory. It needs a real launch altitude to be set.
   * - Clamped to the ground
     - Ignores the altitudes and lays the geometry flat on the terrain. This is the one to pick
       when the question is what the rocket drifts *over* rather than how high it went. A clamped
       track is tessellated, so it drapes over hills instead of cutting through them.

The track and the waypoints are set separately because they want different answers. A flight is
worth seeing suspended in the air, while the markers that label it are easier to read against the
ground they sit over -- so a common pairing is the track above sea level with the markers clamped.

.. warning::

   Exporting above sea level with the launch altitude left at zero draws the flight
   underground. A launch site 1200 m above sea level reports a 700 m flight as 700 m above
   *sea level*, which is 500 m below the terrain -- Google Earth then shows nothing at all.
   The **Automatic** setting avoids this, but the real fix is to set the launch altitude on
   the :guilabel:`Launch conditions` tab, since air density affects the simulated altitudes
   and velocities too.

Placements
----------

The three preset buttons set the placement controls in one click:

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Preset
     - What it sets
   * - Drift cast
     - Everything flat on the terrain, ground track only, every waypoint marked -- for reading the
       hazards under the drift. The airborne line is dropped because, clamped, it would only trace
       the ground track again.
   * - Flight path
     - The flight suspended in the air where it belongs, both tracks drawn, every waypoint marked,
       with shadows down to the ground so each point can still be placed on the map.
   * - Landing plots
     - The landing marker alone, on the ground, with no tracks at all.

Each preset states the whole set of controls rather than only some of them, so clicking one
always leaves the panel in a fully determined state and any preset can be reached from any
other. That includes the waypoint selection: a preset will replace one you picked by hand.

Staged flights
--------------

A staged flight produces one branch of flight data per stage, and the export gives each one
its own folder, its own track color, and its own set of waypoints.

Because every stage reaches its own apogee and its own landing, waypoint names are prefixed
with the stage they belong to -- *Sustainer Apogee*, *Booster Landing* -- so that the
markers can be told apart. A name that already begins with the stage name is left alone
rather than doubled up. Burnout is named for the stage whose motor burned out rather than
the branch it appears in: until separation the stages fly as one stack, so a booster's
burnout is recorded in the sustainer's data as well, and naming it after the branch would
give the sustainer two markers both called *Sustainer Burnout*.

Every stage's data also repeats the ascent the stages flew bolted together, because a branch
created at separation starts as a copy of its parent. The :guilabel:`Stage tracks` setting
decides what to do with that:

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Setting
     - Description
   * - Start at separation
     - The default. Each stage's track begins where it left the stack, so the shared ascent
       is drawn once and each stage's maximum velocity and acceleration are its own.
   * - Start on the pad
     - Every stage's track runs from the pad to its landing, so each reads as a complete
       flight. The shared ascent is then drawn once per stage, and a spent booster reports
       the whole stack's peak velocity and acceleration, reached while it was still attached.

.. note::

   Waypoint names are translated, but the stage prefix is not: it is the stage's name from
   your design, which OpenRocket only translates for its own default names. On a localized
   installation the two halves can therefore come from different languages, giving a marker
   named something like *Sustainer Apogee* with only one half translated. Translating the
   whole phrase would need a format string for every label; it has not been done because
   these strings are currently only supplied in English.

Output formats
--------------

OpenRocket includes three built-in formats. Each one is produced from a template, so you
can also add your own (see `Creating your own templates`_).

KML (Google Earth)
~~~~~~~~~~~~~~~~~~~

KML is the format used by Google Earth. The exported document contains one folder per
stage, and within each: the airborne flight path, the ground track projected onto the
terrain, and a placemark for every selected waypoint. Each stage gets its own line color,
taken from the same palette the plot window uses, so a stage keeps its color whether you
look at it in a graph or on a map. This is the best choice for viewing the flight in 3D.
See `Viewing the track in Google Earth`_ for how to open it.

The altitudes are written either as heights above the terrain or as heights above sea level,
depending on the `Altitude reference`_ setting.

Waypoint CSV
~~~~~~~~~~~~

The waypoint CSV lists only the selected points of interest, one per row, in the column
layout used by Google My Maps: altitude, latitude, longitude, label, symbol, color, label
color, and a descriptive name. Unlike KML it does not include the continuous path, which
makes it a compact way to plot just the pad, apogee, recovery, and landing points on a map.

To plot it in Google My Maps:

#. Open `mymaps.google.com <https://mymaps.google.com>`_ and click
   :guilabel:`Create a new map`.
#. Under the map's layer click :guilabel:`Import`, and select the exported ``.csv`` file.
#. When prompted, choose the ``latitude`` and ``longitude`` columns to position the
   markers, and the ``name`` column for their titles.

.. figure:: /img/user_guide/advanced_flight_simulation/ThreeDPathWaypointsCSV.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: Waypoints imported into Google My Maps.

   Waypoints imported into Google My Maps.

GPX track
~~~~~~~~~

GPX is a widely supported format for GPS tracks and waypoints. The exported file contains a
waypoint for each selected point of interest and a track for each stage. Use it to load the
flight into GPS software and other mapping tools that accept GPX; Google Earth can open GPX
files as well.

Viewing the track in Google Earth
---------------------------------

The KML file can be opened in either the web or the desktop version of Google Earth. Since
the Google Earth Pro desktop application is being retired after 25 June 2027, the web
version is described first.

Google Earth for web
~~~~~~~~~~~~~~~~~~~~~~

Google Earth for web runs in a browser at `earth.google.com <https://earth.google.com>`_
and needs no installation.

#. Open `earth.google.com <https://earth.google.com>`_ and, if prompted, choose to
   :guilabel:`Launch Earth`.
#. In the left toolbar, click :guilabel:`Projects` (the bookmark icon).
#. Click :guilabel:`New project` (or :guilabel:`Open`), then choose
   :guilabel:`Import KML file from computer`.
#. Select the ``.kml`` file you exported from OpenRocket.

Google Earth flies to the launch site and shows the flight path, ground track, and
waypoints. Click a waypoint to see its label, and use the project panel on the left to
turn individual parts of the track on and off.

.. figure:: /img/user_guide/advanced_flight_simulation/ThreeDPathGoogleEarthWeb.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: A flight path shown in Google Earth for web.

   A flight path shown in Google Earth for web.

Google Earth Pro (desktop)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you still have the Google Earth Pro desktop application installed, you can open the file
directly:

#. Double-click the exported ``.kml`` file, or in Google Earth Pro choose
   :menuselection:`File --> Open` and select it.
#. The track appears under :guilabel:`Places` in the left panel, where you can expand it to
   toggle the flight path, ground track, and individual waypoints.

Creating your own templates
===========================

Every output format is produced from a `Mustache <https://mustache.github.io/>`_
template. The three built-in formats are templates that ship with OpenRocket, and you can
add your own to produce a different layout, a different KML style, or an entirely
different file format.

Where templates go
------------------

Place template files in an ``ExportTemplates`` folder inside your OpenRocket user
directory. Create the folder if it does not exist:

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - Platform
     - Folder
   * - Windows
     - ``%APPDATA%\OpenRocket\ExportTemplates``
   * - macOS
     - ``~/Library/Application Support/OpenRocket/ExportTemplates``
   * - Linux
     - ``~/.openrocket/ExportTemplates``

Any file in that folder appears in the :guilabel:`Format` dropdown the next time you open
the :guilabel:`3D Path` tab.

Naming a template
-----------------

Name your file ``<name>.<ext>.mustache``. The middle part sets both the output file
extension and how the text is escaped:

- ``my-track.kml.mustache`` writes a ``.kml`` file with XML escaping.
- ``club-waypoints.csv.mustache`` writes a ``.csv`` file with CSV quoting.
- ``notes.txt.mustache`` writes a ``.txt`` file with no escaping.

The ``<name>`` part is what shows in the dropdown.

The data available to a template
--------------------------------

A template is filled from a model describing the flight. The top level holds summary
information and a list of ``branches`` (one per stage). Each branch holds a list of
``waypoints`` and a list of ``path`` points.

Top level:

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Token
     - Meaning
   * - ``{{title}}``
     - The simulation name.
   * - ``{{rocketName}}``
     - The rocket name.
   * - ``{{motor}}`` / ``{{configuration}}``
     - The flight configuration description.
   * - ``{{launchLatitude}}`` / ``{{launchLongitude}}``
     - Launch site coordinates, in degrees.
   * - ``{{launchAltitudeMeters}}``
     - Launch site altitude above sea level, in meters.
   * - ``{{altitudeUnit}}`` / ``{{distanceUnit}}``
     - The selected unit labels, for example ``ft`` or ``m``.
   * - ``{{maxAltitude}}`` / ``{{maxVelocity}}`` / ``{{maxAcceleration}}``
     - The flight's peak values, formatted for display.
   * - ``{{#includeFlightPath}}`` / ``{{#includeGroundTrack}}``
     - Section tags that are true when that option is selected.
   * - ``{{#showWaypointLabels}}`` / ``{{#colorWaypointPins}}``
     - Section tags for the two marker options. Use ``{{^showWaypointLabels}}`` to emit
       something only when names are switched off.
   * - ``{{kmlAltitudeMode}}`` / ``{{kmlWaypointAltitudeMode}}``
     - The KML ``<altitudeMode>`` matching the chosen altitude reference for the path and for the
       waypoints: ``relativeToGround``, ``absolute`` or ``clampToGround``. Pair each with the
       matching ``{{altitudeKmlMeters}}``.
   * - ``{{#extrudePath}}`` / ``{{#extrudeWaypoints}}``
     - Section tags that are true when the shadow should be drawn. They are sections rather than
       values so a template written before they existed renders nothing for them instead of an
       empty element.
   * - ``{{#tessellatePath}}``
     - Section tag that is true when the track is clamped and so needs tessellating.
   * - ``{{#branches}} ... {{/branches}}``
     - Repeats once per stage.

Inside ``{{#branches}}``:

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Token
     - Meaning
   * - ``{{name}}``
     - The stage name.
   * - ``{{index}}``
     - The stage's position in the list, counting from zero. Useful for building unique
       style ids, as the built-in KML template does.
   * - ``{{colorRgb}}``
     - The stage's color as ``rrggbb``.
   * - ``{{pathColorKml}}`` / ``{{groundColorKml}}``
     - The same color as KML ``aabbggrr`` literals, full strength for the flight path and
       darkened for the ground track.
   * - ``{{#hasPath}}`` / ``{{#hasWaypoints}}``
     - Section tags that are true when the stage has any path points or waypoints.
   * - ``{{#waypoints}} ... {{/waypoints}}``
     - Repeats once per selected point of interest.
   * - ``{{#path}} ... {{/path}}``
     - Repeats once per (thinned) flight-path point.

Inside ``{{#waypoints}}``:

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Token
     - Meaning
   * - ``{{type}}``
     - A short key: ``pad``, ``liftoff``, ``burnout``, ``apogee``, ``recovery``,
       ``landing``, ``maxvelocity`` or ``maxacceleration``.
   * - ``{{label}}``
     - The human-readable label on its own, for example ``Apogee``.
   * - ``{{qualifiedLabel}}``
     - The label prefixed with its stage on a staged flight, for example
       ``Booster Apogee``. Identical to ``{{label}}`` for a single-stage flight. This is
       what the built-in templates put on the map; see `Staged flights`_.
   * - ``{{branchName}}``
     - The stage this waypoint belongs to.
   * - ``{{device}}``
     - The recovery device name for an ejection, or empty for other waypoints. The built-in
       templates do not show it.
   * - ``{{latitude}}`` / ``{{longitude}}``
     - Coordinates in degrees, full precision.
   * - ``{{latitudeStr}}`` / ``{{longitudeStr}}``
     - The same coordinates rounded to six decimal places.
   * - ``{{altitudeMslMeters}}`` / ``{{altitudeAglMeters}}``
     - Altitude above sea level, and above the ground, in meters.
   * - ``{{altitudeKmlMeters}}``
     - Whichever of the two matches the chosen altitude reference. Use this with
       ``{{kmlAltitudeMode}}`` for KML coordinates.
   * - ``{{altitude}}`` / ``{{altitudeMsl}}``
     - Altitude above the pad, and above sea level, formatted in the selected unit.
   * - ``{{distance}}``
     - Horizontal distance from the pad, in the selected unit.
   * - ``{{bearing}}``
     - Compass bearing from the pad, in whole degrees.
   * - ``{{time}}`` / ``{{timeStr}}``
     - Time since launch, in seconds.

Inside ``{{#path}}``:

.. list-table::
   :header-rows: 1
   :widths: 35 65

   * - Token
     - Meaning
   * - ``{{latitude}}`` / ``{{longitude}}``
     - Coordinates in degrees.
   * - ``{{altitudeMslMeters}}`` / ``{{altitudeAglMeters}}``
     - Altitude above sea level, and above the ground, in meters.
   * - ``{{altitudeKmlMeters}}``
     - Whichever of the two matches the chosen altitude reference.
   * - ``{{altitude}}``
     - Altitude above the pad, formatted in the selected unit.
   * - ``{{time}}`` / ``{{timeStr}}``
     - Time since launch, in seconds.

Tokens from an outer level are still visible on an inner level, so inside
``{{#waypoints}}`` you can still use ``{{rocketName}}``, ``{{altitudeUnit}}`` or the
stage's ``{{index}}``.

.. note::

   KML and GPX expect coordinates in the order **longitude, latitude, altitude**. For KML,
   use ``{{altitudeKmlMeters}}`` together with ``{{kmlAltitudeMode}}`` so the file follows
   the `Altitude reference`_ setting. GPX elevations are defined as height above sea level
   with no relative-to-terrain equivalent, so use ``{{altitudeMslMeters}}`` there.

Example
-------

The built-in waypoint CSV template is a good starting point. It writes a header row and
then one row per waypoint:

.. code-block:: none

   "altitude({{altitudeUnit}})","latitude","longitude","label","symbol","color","label_color","name"
   {{#branches}}{{#waypoints}}"{{altitude}}","{{latitudeStr}}","{{longitudeStr}}","{{type}}","pushpin","yellow","white","{{rocketName}} {{motor}} {{qualifiedLabel}} - {{altitude}} {{altitudeUnit}} - {{distance}} {{distanceUnit}} @ {{bearing}} deg"
   {{/waypoints}}{{/branches}}

Saving that as ``club-waypoints.csv.mustache`` in the ``ExportTemplates`` folder makes it
available as a format called *club-waypoints*.

