<!-- 

!! NOTE: This HTML formatting is required for the welcome info dialog.
Always encapsulate each release with a <div> tag and the correct id.

-->

<body>
<div>

Release Notes
=============

</div>

<div id="24.12.RC.01">

OpenRocket 24.12.RC.01 (2025-03-25)
-------------------------------------

Following are changes since 24.12.beta.01.  Notable features and fixes in **bold**.

### Multi-level Wind Feature
* **CSV Wind Data Import**: Import detailed wind profiles directly from CSV files. Thanks to @CSutter5!
* Reworked UI
* Allow zero wind speed input (fixes #2678)
* Add "Wind Direction" flight data type

### User Interface
* **Window Ghosting Eliminated on Windows OS**: We think @SiboVG has finally squashed this vexing problem (fixes #1667)
* **Improved UI Readability**: Some assorted changes to improve the readability of the UI after the 24.12.beta.01 change to the FlatLaf UI
* **Add UI Customization**: You can now tweak the UI to your liking. Added "UI Scale", "Font Size", and "Character Spacing".  See the "UI" tab in app preferences.
* **Improvements to Rotation Control in 2D Views**: You can now lock the angle so you don't accidentally rotate it with a click-drag; you can also enter the desired angle directly
* **Show Flight Config in Design View**: This should make it easier to get all necessary information when screenshotting just the rocket figure display (fixes #2735)
* Use light mode when exporting design info (fixes #2510)
* Support 5 decimal places in latitude and longitude settings (fixes #2590)
* Better positioning of modal dialogs (fixes #2652)
* Correctly track when files have changed (fixes #2679)
* Include number of assembly copies in motor count display (fixes #2725)
* Add File->Properties menu item (fixes #2662).  Thanks to @MiguelECL for their contribution!
* Focus Rocket Config Dialog when opening file (fixes #2146).  Thanks to @JonathanDeLaCruz for their contribution!
* Clear "Simulation abort" warning in design view when resetting flight config (fixes #2749)

### Warnings
* **Standardize Warning format** (fixes #2669)
* Clarify "Open airframe" warnings when due to separated booster stage
* Don't generate spurious warning when using a single tube fin (fixes #2663)
* Improve how warnings are saved to and reloaded from ORK file (fixes #2694)

### Calculations
* Correctly calculate CG on zero-length components (fixes #2626)
* Improve handling of very small fins (fixes #2633)
* Include enabled stages in calculations even if parent stage is disabled (fixes #2657)
* Correctly handle "auto" mass object size when parent component is filled (fixes #2660)
* Fix CP calculation error with tail cones (fixes #2751)

### Simulation
* Add "Altitude above Sea Level" as new flight data type
* Include side boosters in thrust calculation (fixes #2639)
* Improve handling of launch site altitude and limit to 11 km (fixes #2699)

### Export
* Add option to export only one instance of component to OBJ
* Fix export of Component Analysis data (fixes #2697)
* Correctly handle periods in file path when exporting (fixes #2701)
  
### Miscellaneous
* Add compatibility with plugins using the old `net.sf.openrocket` package instead of the new `info.openrocket.core` and `info.openrocket.swing`. The old plugins are copied and migrated to the new package structure with a `-migrated` suffix. (fixes #2676)
* Clean up the "CSV Save" example simulation extension (fixes #2696)
* Fix exception when changing the opacity setting in the appearance panel (fixes #2644)
* Improvements to the software updater (fixes #2648)
* Update "modified data" file metadata when saving
* Add additional file properties (fixes #2664).  Thanks to @MigelECL for their contribution!
* Honor Cd override when reading from a file (fixes #2745)
* Add Piotr Tendera Rocket Motors (TSP) and Raketenmodelbau Klima (Klima) motor manufacturers to motor database
* Enhancements and fixes to motor length and diameter filters

...and the usual additional assortment of small fixes and tweaks.

</div>

<div id="24.12.beta.01">
 
OpenRocket 24.12.beta.01 (2024-12-20)
-------------------------------------

Notable features and fixes in **bold**.

### RELEASE HIGHLIGHTS
* **Enhanced Simulation tab UI**: This is the beginning of a multi-release effort to make it easier to manage simulations and interpret their results. Please let us know what you think so far.
* **Multi-level wind input**: Configure different wind settings at different altitudes (in the simulation configuration dialog).
* **New Component Analysis Parameter Sweep Tool**: Plot and export Component Analysis parameter sweeps in the new Component Analysis Plot/Export tab. For instance, you can plot the rocket CD as a function of Mach number.
* **SVG Fin Export**: Export fin shapes directly to SVG for laser cutting or importing into CAD tools.
* **Expanded Platform Support**: We now offer installers for x86_64 and Arm64 on Windows, Mac, and Linux.
* **Project documentation moved to Sphinx**: See it at https://openrocket.readthedocs.io/.

### Simulation
As mentioned above, a lot of work went into the Simulation tab for this release, and there is a lot more to come.

#### Flight simulation
* **Overhauled Sim table GUI** (fixes #2456)
* **Multi-level wind input** (fixes #922, #2060, #2558)
* Sync wind speed, deviation, and turbulence widgets together in sim settings (fixes #2388)
* Add South/West units for latitude and longitude of launch site (fixes #2178)
* Improved accuracy of thrust calculation
* Abort sim when recovery deployment occurs under thrust
* Allow sustainer to tumble before apogee; if under thrust, abort sim
* Fire outdated sim on stage rename (fixes #2532)
* Added "aborted" status mark to simulations
* Don't show vertical acceleration as negative before liftoff
* Clamp compressibility correction factor to avoid singularity
* Allow for configurable maximum simulation time

#### Warnings
* **Add SIM_ABORT flight event type, instead of throwing exceptions**
* Don't warn about large angle of attack when we start to tumble
* Don't set open airframe warning on booster stages if either they're about to deploy a recovery device or they're unstable (and improve wording)

#### Plotting
* **Organize axis plot types selector into categories and add search function** (fixes #2338)
* Add air density as plottable variable (fixes #2462)
* Show stage name in sim plot tooltips (fixes #2521)
* Keep edit sim dialog open after plotting/exporting (fixes #2531)
* Add Flight Warning events to plots

### Component Analysis
* **Plot and export component analysis parameter sweeps** (fixes #2525): See the new Plot/Export tab.
* Highlight component selected in Component Analysis in the rocket figure display
* Add per-instance Cd column to Component Analysis Dialog (fixes #2019)
* Select components for plot/export

### Import/Export
* **Export fins to SVG file**: See button at bottom of config window
* Improve OBJ export using Delaunay triangulation (fixes #2444)
* Support booster export and import with Rocksim, other bug fixes (fixes #2437, #2377, #2435)
* Correct launch rod length when exporting to RASAero
* Correct conical nose cone/transition OBJ exporting (fixes #2609)

### Materials
* **Add material groups with search**: Check this out in any config window.
* **Add "document materials" that can be reused within a document (i.e. ORK file)**
* Easier to add custom materials
* Set balsa as default fin material

### Motor Configurations
* **Separate motor nominal vs. actual diameter** (fixes #2569): This allows Loki 76mm motors to pass the 75mm motor filter.
* Add "Save as default" option to motor config name (fixes #2537)

### Multi-stage Rockets
* **Add stage separation options for deployable payloads and a deployable payload example** (fixes #852, #2519): We had many requests for this from various competition participants.
* Don't add motor delay time to upper stage motor ignition time (fixes #2450)

### Preset Library
* **Fix parachute length resizing when using preset parachute**: Lots of folks reported this one.
* **Fix transition and nose cone component presets defaulting to a filled shape** (fixes #2480 and #2614)
* Correct diameter of Spherachutes to match Cd (fixes #2517)
* Fix sorting problems in preset library (fixes #2576)

### User Interface
* **Allow components to be hidden from view** (fixes #2485): Use the edit menu or contextual menu to show/hide selected components
* **Constrain angles in freeform fin editor** (fixes #427): Hold down shift or control-shift while dragging a point.
* **Add hex color input field in appearance panel** (fixes #2224)
* **Rotate 2D views by click-dragging** (fixes #2093)
* Allow 3 digits of precision in "shape" parameter (fixes #2409)
* Fix Mass Object radial rotation in 3D view (fixes #2550)
* Improve texture select combobox operation
* Change LAF engine to FlatLaf for all UI themes

### Command Line Options
* Choose user-defined component preset locations (fixes #1081)
* Suppress preset and motor loading at startup (fixes #1579)

### Project
* **Move project documentation to Sphinx**
* **Switched build system from Ant to Gradle**
* **Added Arm64 support for Windows and Linux**
* Adopted Java Platform Module System
* Renamed `net.sf.openrocket` package to `info.openrocket.core` and `info.openrocket.swing`. This breaks compatibility with plugins or other OR extensions that use the `net.sf.openrocket` package name.

### Misc
* **Re-introduced motor ignition delay optimization to Rocket Optimization** (fixes #2345)
* Support more and larger page sizes for printing (fixes #2483)
* Improve manufacturer search in Component preset library (fixes #2479)
* Fix shoulder scaling (fixes #2463)

*...and, as always, lots more minor fixes and improvements.*
</div>

<div id="23.09">

OpenRocket 23.09 (2023-11-16)
------------------------

You can find a visual overview of what's new for this release on [our website](https://openrocket.info//downloads.html?vers=23.09#whats-new).

### Major Updates

#### New Features:
* **3D Printing Support: Export any component or combination to OBJ file** (fixes #604)
* **RASAero compatibility: Import/Export CDX1 files** (fixes #875 and #1147)
* **Dark mode (normal and high-contrast) and custom UI font size support** (fixes #1089)
* **Export sim table to CSV** (fixes #2077)

#### Bug Fixes:
* **Fix Tube fin drag** (fixes #2065)
* **Fix Base drag when using Cd override** (fixes base drag hack sim error, fixes #2118)
* **Fix Atmospheric pressure when using ISA conditions** (fixes #2103)
* **Properly sanitize XML in ORK file** (eliminates corrupt ORK files, fixes #2051)


### Other New Features
* Bumped app to Java 17
* Export and import preferences to XML file
* Display secondary stability unit. This means you can display stability in both calibers *and* percentage of length (fixes #2079)
* Added "cases" and "manufacturers" substitution in motor config names (fixes #2055 and #2204)
* Selection of "common name" or "manufacturer's designation" in motor selection table is now reflected everywhere else in the program (fixes #2072)
* Added "plugged" option in charge delay combobox (fixes #2090)
* Added motor type to "show details" in motor selection (fixes #2069)
* Added instances settings in launch lug config (fixes #2035)
* Account for fin cant in fin root points, and support canted fins in fin marking guide (fixes #2231 and #2242)
* Set cluster tube separation in absolute or relative units (fixes #1970)
* Support transparent rendering and export of Photo Studio images (fixes #2076)
* Added "Select -> Components of same color" and "Select -> None" options (fixes #2129)
* Remember column width, order and visibility in component preset table (fixes #2357)

### Bug Fixes
* Fixed mass issues with fin sets (fixes #2217)
* Fixed CG issues for launch lugs and rail buttons (fixes #2040)
* Improved rail button drag calculations
* Added parts detail for pods and boosters (fixes #2084)
* Fixed parachute position when using auto radius (fixes #2036)
* Fixed pod set and booster marker position under certain circumstances (fixes #2047)
* Fix CG marker location in top view (fixes #2050)
* Handle zero-area fins (warn and don't crash with NaN error) (fixes #2032)
* Don't dispose config dialog when no components are selected in 3D view (fixes #2108)
* Display ISA values in temp and pressure fields (fixes #2104)
* Improved simulation of fins on transitions and nose cones (fixes #2113)
* Cleaned up multi-sim editing (fixes #2138 and #1826)
* Update ruler units immediately when preferences are changed (fixes #2151)
* Compute CG and CP based on currently active stages (fixes #2171)
* Improved mass/CG calculations for fillets (fixes 2209)
* Set auto radius correctly for mass objects (fixes #2267)
* Apply radial positioning to multi-engine clusters (fixes #2283)
* Fixed 3D rendering of fin tabs (fixes #2286)
* Update recent file list when opening via file association (fixes #2222)
* Corrected the columns displayed in the component preset table's popup menu, ensuring only relevant columns appear
* Ensured optimum delay is saved in flight summary and .ork files (fixes #2353)
* Corrected longitudinal moment of inertia calculations by excluding shoulders (fixes #2278)
* Fixed exception when setting wind speed to zero (fixes #2386)
* Fixed unexpected mass and CG override interaction (fixes #2394)

### Miscellaneous
* Updated example rockets (including brand-new two stage example)
* Show calculated values in override tab (fixes #1629)
* Decrease minimum FoV to 10 degrees in Photo Studio
* Increase resolution of launch temperature and pressure to 2 decimal places (fixes #2003)
* Display Cd override with 3 decimal places
* Added wiki button to help menu (fixes #2046)
* Eliminate option to save "some" sim data (fixes #2024)
* Added OK/Cancel buttons when editing simulations (fixes #2158)
* Added OK/Cancel buttons when editing preferences (fixes #2266)
* Added multi-sim edit indicators (fixes #2159)
* Show warning when motor file has illegal format (fixes #2150)
* Reset window position if off-screen (fixes #2141)
* Keep current field value when "auto" option is unchecked (fixes #2096)
* Open dialog to save design info when first saving file
* Added '3D Printable Nose Cone and Fins' to example rockets
* Use more sensible colors for thrust curve selection in motor selection dialog (fixes #2385)


...along with numerous other minor fixes and enhancements.
</div>

<div id="22.02">

OpenRocket 22.02 (2023-02-08)
------------------------

You can find a visual overview of what's new for this release on [our website](https://openrocket.info//downloads.html?vers=22.02#whats-new).

The 22.02 release includes hundreds of new features, bug fixes, and UI improvements, more than we could ever fit into one set of release notes.  These notes summarize the highlights of the entire release; for more detail consult the notes from the five previous public beta releases.
 
Please note that version 22.02 is required for Macs running macOS 13.0 or later.
 
## New Features
* Rocket design features
  * Pods and strap-on boosters
  * Tail cones
  * Freeform fins on nose cones and transitions
  * Highly configurable rail buttons
  * Massive update to parachute configuration and part library
  * Dave Cook's extensive component library now built-in
  * Coefficient of Drag Override for components and assemblies (and more flexible override settings in general)
  * Lots more detailed geometry warnings which identify the components involved
* Appearance updates
  * Settable opacity per-component
  * Separate left/right appearance for fins, and inner/outer appearance for tubes
* App Infrastructure
  * Packaged installers for Windows, Linux, and macOS (JAR file still available)
  * Updated to Java 11
  * Native ARM version for Apple Silicon Macs (snappy!)
  * File association for ORK files: double-click files to open in the app
  * On Mac, app stays open after last window is closed

## Simulation and Staging Improvements
  * Assorted CP calculation fixes
  * Improved nose cone drag calculations
  * Improved ground hit velocity calculations
  * Completely reworked tube fin simulation (no longer "experimental")
  * Improved time step selection for descent
  * Improved simulation plot appearance
  * Support for scientific notation and custom decimal places in simulation exports
  * Simulations now properly account for disabled stages
  * Update CG correctly when stages are toggled on or off
  * Display stage names in stage enable buttons
  * More reliable deployment of booster stage recovery device

## UI Improvements
* Updated icons everywhere
* Rocket figure display updates
  * Option to display stability margin as percentage of rocket length
  * New "Top View" option
  * Cleaned-up toolbar
  * Option to show/hide warnings
  * Option to show/hide CG and CP markings 
  * Improved click and double-click behavior on rocket
* Component Configuration Editor
  * Extensively reorganized for clarity and consistency
  * Massively improved keyboard navigation
  * Config panels remember which tab you were on previously
  * Cancel button to exit config panel without saving changes
* Contextual menus for managing the component tree
* Simultaneous multi-component edit (great for appearance editing)
* Completely updated operation of motor config and simulation tables
* Improved workflow when creating motor configurations
* Greatly improved component library browser
* Greatly improved freeform fin editor operation
* Photo Studio
  * sliders for settings
  * keyboard input for settings
  * settings remembered for each rocket
 
## Misc
* Extensively updated and reorganized example rocket collection
* Much improved RockSim import and export
* Updated motor database
 
_...plus about a billion bug fixes and so much more._
 
Huge thanks to all the users who gave us feedback throughout the public beta period and helped us get to our first release in eight years!
</div>

<div id="22.02.RC.01">

OpenRocket 22.02.RC.01 (2023-01-27)
------------------------
(through PR1996)

## New Features
* **Native ARM build for Apple Silicon Macs!** (fixes #1136 and #1533)
* Added "Cancel" button to component config window (fixes #960)
* Added component information box in config window (fixes #1564)
* Example designs have been updated and reorganized
* Add option to switch flight event markers to icons in sim plots
* Add support for tail cones (fixes #1889)
* Added additional warnings for inline pods, gaps and overlaps in airframe (fixes #1894)
* Added "plugged" to delay options for all motors
* Support rail button screws, updated rail button aerodynamics
* Updated surface roughness settings to be consistent with standard terminology
* Separate "override subcomponents" option for mass, CG, and drag (fixes #1750)
* Add motor designation to motor selection table
* Enable decal editing on Linux

## UI Improvements
* Reorganized Config dialogs for improved clarity and consistency
* Added "Top View" option to rocket display
* Add option to automatically open preset dialog when creating new component (fixes #1479)
* Tell user where body discontinuities are (fixes #241)
* Improved consistency of warning message text
* Hitting left arrow moves cursor to front of text fields (fixes #1942)
* Grey out texture edit button if using default material
* New app icons
* Zoom to active stages in rocket display
* Added welcome dialog (fixes #1768)
* Updated bug report form

## Notable Bug Fixes
* Fix various significant 3D view issues (fixes #939, #966, #1191, #1771)
* Don't mark all simulations out-of-date on flight config change (fixes #1973)
* Account for all recovery devices in pods when calculating descent rate (fixes #1910)
* Eliminated many occurrences of mystery discontinuity warnings (fixes #999)
* Rocksim import/export file updates
  * Support import and export of pods (fixes #1348)
  * Support import of trapezoidal fins on transitions (fixes #1553)
  * Support subassembly import
* Assorted extension and plug-in fixes
 
</div>
 

<div id="22.02.beta.05">

OpenRocket 22.02.beta.05 (2022-09-28)
------------------------
(through PR1688)

## New Features
* **C_D Override for subassemblies now implemented.  Please test!** [See this wiki help page](http://wiki.openrocket.info/Overrides_and_Surface_Finish#How_and_Why_to_Use_Surface_Finish_Settings_and_Coefficient_of_Drag_.28CD.29_Overrides) for important instructions (fixes #1171)
* Simulations now properly account for disabled stages (fixes #1477 and #1460)
* Rail Button improvements:
  * Additional parameters for rail buttons (fixes #1537)
  * Added rail buttons to preset parts database
  * Scaling now supported for rail buttons (fixes #1661)
* Don't include inactive stages in calculations. Disabling stages using the stage selector buttons now also affects the simulation results. (fixes #1477 and #1460)
* Mass objects maintain fixed volume when container diameter changes in auto mode (fixes #1315)
* Inner tubes can have custom inner/outer appearance (fixes #1509)

## UI Improvements
* Visually select child components of assembly components when selecting the parent (fixes #1489).  Try selecting a pod, stage, or booster and see.
* Significant improvements to preset selection window (fixes #1481)
* Added sliders to Photo Studio settings (fixes #1524)
* Show markers for pods and boosters in rocket side view (fixes #1146)
* Major improvements to focus behavior.  Keyboard navigation in the Component Config dialog is now _much_ more convenient.
  * When setting focus to a spinner field, select the entire field by default (fixes #1506)
  * After selecting a preset, automatically highlight most commonly edited parameter (fixes #1488). So, for example, after selecting a body tube preset the Length parameter will be selected by default.
  * Restore focus to motors, recovery, stage and simulation tables after table action and others (fixes #1558).  Simply put, you shouldn't need to click in the table areas just to enable keyboard shortcuts to work.
  * Use tab and arrow keys to traverse sim table (fixes #1552)
* Added and/or improved hover texts on a bunch of different buttons and controls
* Apply preset after double-clicking (fixes #1539)
* Pre-check diameter filters in preset chooser (fixes #1480)
* Preset selection window opens larger, and remembers column widths (fixes #1305).  Separate column widths are remembered for each component type.
* Ctrl/Cmd+A (select all) keyboard shortcut in component tree, simulation and motor configuration tables (fixes #1549)

## Notable Bug Fixes
* **Scripting now works again.  Please test!** (fixes #308, #826, #1108, and #1270)
* Don't re-run simulations when new sim is created with same motor (fixes #1510)
* Ensure simulations finish when running from scripts (fixes #1575)
* Fixed Cd reporting for fin sets (fixes #1440)
* Move component config window back to same monitor as main app window first time it is opened.  No more lost config windows! (fixes #1470)
* Rocket side view updates
  * Don't recenter rocket when zooming (fixes #1464)
  * Ensure full rocket is always visible and positioned correctly in rocket side view (fixes #1465)
* Fixed root edge display in 3D view and fin templates (fixes #1227).  This matters when you are attaching fins to a nose cone or transition.
* Numerous bug fixes in component scaling (fixes #1649, #1651, #1653, #1661, #1662, and #1663)
* Better register of double-clicks in 3D view (fixes #1054)
* Increased component analysis drag precision to three decimal digits (fixes #1476)
* Select recovery device/stage after config panel select (fixes #1490)
* Fixed an exception when scaling freeform fins (fixes #1520)
* Improved performance of freeform fin shape editor (fixes #1533)
* Better automatic calculation of fin tab sizes (fixes #1600)
* Fixed simulation errors when there were empty stages in the design (fixes #1617)
* Fixed stage activeness not updating when moving, deleting, or copying stages (fixes #1680)
* Fixed exception for zero-length transitions and nose cones (fixes #1677)

## Other
* Don't open motor selection dialog when duplicating motor configurations (fixes #1555)
* A bunch of updated component icons
* Added icons to most buttons
* Improvements to Undo behavior when adding new components (fixes #1513)
* Improvements to scale dialog (fixes #411)
* Component tree now set to reasonable minimum width (fixes #1648)
* Removed "show all compatible" option in preset chooser (fixes #1405)
* Cleaned up several of the example rockets
* Added warning to launch preferences that changes only affect new sims (fixes #1497)
* Fixed overlapping labels in motor selection diameter filter slider (fixes #1643)
* More Russian translation updates

</div>

<div id="22.02.beta.04">

OpenRocket 22.02.beta.04 (2022-06-17)
------------------------
(through PR1456)

**Please note:** For this new beta, the packaged installers are now back to using Java 11, due to multiple bug reports related to Java 17.
However, the JAR file will still allow Java 11 *or* 17, so if you want to keep experimenting with Java 17 then feel free.

### New Features
* **File association now works on all platforms** (fixes #1135)
* **Contextual menus now available via right-click throughout the program**
* **Edit multiple selected items at once (great for appearance editing!)**
* Show/hide CG/CP markings in rocket display
* Support for custom decimal places and exponential notation in simulation exports (fixes #1307 and #1354)
* Simulation warning when no recovery device is enabled (fixes #1436)
* Remember previously selected tab when opening component edit dialog (fixes #974)
* On Mac, app remains open when last window is closed (normal Mac behavior)

### Notable Bug Fixes
* **Tube fins are fixed!** Both CP and drag calculations should be good now. So good that we are no longer calling tube fin support "experimental".  Feedback please! (fixes #1258)
* Guarantee fit in rocket view (fixes #1231 and #1351) 
* Corrected mass display in some situations (fixes #1409)
* Show combined mass of multi-selected components when hovering (fixes #1411)
* Fix greyed-out buttons on Mac (fixes #1099)
* Improve scalability of Motors and Configurations tab (fixes #1285)

### Other
* Many component edit dialogs reorganized for consistency and clarity
* Loads more tweaks to parachute and streamer config dialog
* Modified file size estimation in Save dialog. Feedback please!
* Improved layout of rocket view ribbon
* Improved icons for zoom buttons, and launch lugs and tube fins
* Added sliders to Pod Set config
* Preset dialog now sorted by manufacturer by default
* Updated guided tours
* Improved Russian translation

</div>

<div id="22.02.beta.03">

OpenRocket 22.02.beta.03 (2022-05-18)
------------------------
(through PR1361)

### New Features
* Big improvements to parachutes:
  * Added manufacturers: Front Range, Fruity Chutes, Rocketman, b2 Rocketry, Spherachutes
  * Added additional fields to preset DB (e.g. spill holes)
  * Automatic packed size calculation (for select chute manufacturers only)
  * Parachute mass is automatically overridden with manufacturer data (when available)
  * Parachute component name is automatically populated with preset description

### Notable bug Fixes
* __Tube fin drag simulation has been extensively rewritten__.  This should fix flight simulations of tube fin models.  Please test your sims and let us know how it works for you (fixes issue 1207).  _Note that CP calculations for tube fins are still not fixed._
* Improved ground hit velocity estimation (partially fixes issue 1349)
* Rail buttons now included in Fin Marking Guide (fixes issue 1259)
* Tube fin sets now included in Fin Marking Guide (fixes issue... ah, there wasn't one)
* Override mass now divides when fin set is split (fixes issue 1292)
* Fixes exception when splitting fin sets (fixes issue 1302)
* Hovering over pod set reports total mass of pod set (fixes issue 1291)
* Fixes exception when putting fins on transitions (fixes issue 1247)
* Opacity slider now stays in sync with alpha changes in color selector (fixes issue 1326)

### Other
* Major cleanup of File menu.  Let us know how you like it.
* Improved time step selection for descent
* Java 17 is now included in the packaged installers and supported by the JAR file.

</div>

<div id="22.02.beta.02">

OpenRocket 22.02.beta.02 (2022-03-26)
------------------------
(through PR1261)

NOTE: Tube fin simulation is currently broken, and will be fixed in a future beta.

### New Features
* Check Java version at startup (requires Java 11)
* Opacity slider on appearance panel
* Export sim plots as PNG images

### Notable Bug Fixes
* Rocksim Import/Export
  * Corrected position offsets when importing and exporting (fixes issue 1164)
  * Corrected fin shape when importing (fixes issue 1220)
* Side boosters
  * Added stage selector for side boosters (fixes issue 1208)
  * Fixed simulation bug with side boosters (fixes issue 1210)
  * Eliminated warning when loading designs with boosters (fixes issue 1196)
  * Fixed Simulation Plot range with boosters (fixes issue 1228)
* More reliable creation of simulation when creating a new configuration (fixes issue 1163)
* No longer need to click on motor config to get flight data (fixes issue 1175)
* Motor database search more robust, especially for CTI (fixes issue 1174)
* More accurate and reliable simulation of fins on transitions (fixes issues 1173 and 1243)
* _plus other miscellaneous fixes_

### Other
* Set default color for all components to #BBBBBB, with Shine=30 (closes issue 1192)
* Update Mac installer style to more standard "drag app to Applications folder"

</div>

<div id="22.02.beta.01">

OpenRocket 22.02.beta.01 (2022-02-25)
------------------------
(through PR1155)

### Application
* Update to Java 11 
* Distributed as packaged installers

### New Rocket Design Capabilities
* **Pods**
* **Drop-off Boosters**
* **Rail buttons**
* **Attach freeform fins to nose cones and transitions**
* **Coefficient of Drag override**
* **Added Dave Cook's rocket component library**

### Staging Improvements
* Update CG correctly when stages are toggled
* Display stage names instead of numbers in Stage enable buttons
* Reported length reflects length of selected stages only
* More reliable deployment of booster stage recovery device
* Fixed booster tumbling behavior
* Eliminate exceptions during multi-stage simulation
* Improved accuracy of nose cone simulation

### Simulation Improvements
* Assorted CP calculation fixes
* Improved nose cone drag calculations
* Properly run all simulations when any design change is made
* Use actual burn time for determination of burnout event
* Improve mass calculation accuracy for motors
* Correct linear interpolation of motor CG
* Copy Simulation results to clipboard
* Report wind speed correctly
* Improved ground hit velocity calculations
* Run simulations all the way to the end
* No more warning if recovery device is deployed while motor is coasting

### Rocket Appearance
* Support rendering of transparent or translucent components
* Separate inside and outside color for tube components
* Separate left and right appearance for fin components

### User Interface
* Multi-select/copy/paste components in the tree
* Option to display rocket stability as percentage of length
* Improved layout on many windows and dialogs
* Now use "Export" to save to RKT format
* Improved File Dialog behavior
* Multi-select/delete motor configurations
* Improved UI appearance on Mac
* Motor Selection
  * Automatically open motor selection dialog when adding new motor configuation
  * Added checkbox to hide motors which are out of production
  * Highlight motor mount in rocket display when motor is selected
* Many improvements to freeform fin editor, including
  * More accurate insertion of new points
  * Highlight coordinates of selected point
  * Export fin profile to CSV file
  * Fixed scrolling and zooming
* Improvements to Photo Studio
  * Respond instantly to design changes
  * Settings saved per rocket
  * Settings layout improved
  * Settings values can now be entered via keyboard

### Misc
* Updated motor list to latest data from Thrustcurve.org
* Added additional fields to motors
* Disable fin-thickness warnings on phantom tubes
* Added warning message for phantom body tubes
* Updated print dialog to allow simulation control
* Improved Rocksim import
* Added or improved Spanish, Dutch, and simplified Chinese translations

..._plus many, many additional bug fixes and refinements_

</div>

<div id="15.03">

OpenRocket 15.03 (2015-03-28)
-----------------------------

OpenRocket now requires Java 1.7 for execution.

New Features

  * Experimental support for tube fins
  * Updated thrustcurves
  * Scriptable simulation extensions
  * Fin fillet mass
  * Better icons for different kinds of masses - altimeters, computers, etc.
  * Configurable default mach number
  * Improved preferences UI

Bug Fixes

  * Always use the correct filename extension when saving
    
</div>

<div id="14.11">

OpenRocket 14.11 (2014-11-02)
-----------------------------

New Features

  * Updated thrustcurves

Bug Fixes

  * Fixed a couple of bugs.

</div>

<div id="14.06">

OpenRocket 14.06 (2014-06-25)
-----------------------------

New Features

  * Klima motor textures
  * Added knots to windspeed and velocity units
  * Updated thrustcurves

Bug Fixes

  * Fixed annoying IndexOutOfBounds bug in tables.

</div>

<div id="14.05">

OpenRocket 14.05 (2014-05-21)
-----------------------------

New Features

  * Compute optimimum delay time when simulating
  * Display cg/mass overrides using icons in the component tree

Bug Fixes

  * Bug fixes in the motor selection dialog
  * Updated thrustcurves
  * Updated 3d libraries to 2.1.5

</div>

<div id="14.03">

OpenRocket 14.03 (2014-03-20)
-----------------------------

New Features

  * Photo Realistic 3d rocket renderer

Bug Fixes

  * Fixes to the flight configuration tab and motor selection dialog
  * Updated thrustcurves

</div>

<div id="13.11.2">

OpenRocket 13.11.2 (2014-01-01)
-------------------------------

Bug Fixes

  * Numerous bug fixes and usability improvements in the new
      flight configuration tab.
  * Fix couple of layout issues
  * Updated Spanish, French and Chinese translations

</div>

<div id="13.11.1">

OpenRocket 13.11.1 (2013-11-15)
-------------------------------

Bug Fixes

  * Added back the TubeConfiguration Configuration dialog
  * Seems the jogl update didn't happen in the 13.11 build.
  * Made the motor filter remember previous settings
  * Fixed various exceptions in flight configuration tables due to column reordering
  * Fixed NPE when deleting a configuration

</div>

<div id="13.11">

OpenRocket 13.11  (2013-11-08)
------------------------------

New Features

  * Chinese translations
  * Replaced flight configuration dialog with more efficient configuration tab
  * Improved filtering in motor chooser dialog

Bug Fixes

  * Updated jogl to correct 3d problems on various platforms
  * Fixed NPE introduced by changes in Java 1.7.0_45-b18

</div>

<div id="13.09.1">

OpenRocket 13.09.1 (2013-10-05)
-------------------------------

This release contains a number of bug fixes, updated 3D JOGL
libraries.  Added preliminary thrustcurves for Aerotech C3 and D2 18mm
reloads.

</div>

<div id="13.09">

OpenRocket 13.09 (2013-09-08)
-----------------------------

This release contains a number of bug fixes, updated 3D JOGL
libraries, and separates simulation edit and plot dialogs.

</div>

<div id="13.05">

OpenRocket 13.05 (2013-05-04)
-----------------------------

New Features

  * Added support for decals on rockets.  Added two new 3d views - 3d finished and 3d unfinshed.  Added support to export and
      apply decals (images) to rocket components.  Added ability to launch external graphics editor to edit decals and monitor
      file for writes to update the rocket view.
  * Added simulation of tumble recovery based on experimentation done by Sampo Niskanen.  This is particularly useful
      for low power staged flights.
  * Extended "motor configuration" concept to cover more properties.  The concept was renamed "Flight Configuration" and
      allows the user to override stage separation, recovery deployment, and motor ignition per flight configuration.
      These configurations are stored in the ork file with each component.  If no override is specified then the user specified
      default is used (set in the component edit dialog).  The flight configuration dialog was reworked to make it more
      usable.  The user selects the configuration in a drop down with buttons for "add", "delete" and "rename".  After selecting
      the configuration, the tabbed area below allows the user to change the configuration for this simulation.
  * Allow simulation of stages without a motor.  Users in the past have attempted to simulate separate recovery of payload
      and booster by tricking OpenRocket using a dummy motor with trivial thrust curve.  Now the user does not need to do this.
      There is an example of this provided in TARC Payloader.ork and Boosted Dart.ork
  * Simulate recovery of boosters.  The simulation engine will create new FlightDataBranches for each stage after
      separation.  The data for the boosters begins at t=separation.  The simulation plot allows the user to select
      which stage's data to show (or all).
  * Modified the zoom and pan controls in the simulation plot.  Added zoom in, out, and reset buttons.  Added
      scroll with mouse wheel.  If the alt key is used with either of these, only the domain is zoomed.  Richard
      contributed a more logical mouse controlled zoom - right click and drag will zoom (either domain, range or both).

</div>

<div id="12.09.1">

OpenRocket 12.09.1 (2012-06-28)
-------------------------------

Bug-fix release for 12.09, fixing numerous bugs.  Only new feature is
the possiblity to automatically open the latest design file on startup
(in Edit -> Preferences -> Options).

</div>

<div id="12.09">

OpenRocket 12.09  (2012-09-16)
------------------------------

Numerous new features by many contributors

- 3D rocket design view
- Component Presets
- Custom expressions in simulations
- Printing for centering ring and clustered centering ring components.
- Support simple arthmatic in dimension entry
- Support deploying recovery device at stage separation
- Support for fractional inches (1/64) for unit length
- Added preference for windspeed units separately
- Added "most recently used files" in File Menu.
- Improved printed accurracy in fin marking guide
- Calibration rulers added to printed templates
- Translations in Czech and Polish, numerous updates

</div>

<div id="12.03">

OpenRocket 12.03  (2012-03-17)
------------------------------

In this release the version numbering scheme has been changed to be
"YY.MM" indicating the year and month of the release.

Enhancements in the desktop version include saving designs in RKT
format thanks to Doug Pedrick, freeform fin set import from images by
Jason Blood, configurable stage separation events, guided help tours
and displaying the computed motor designation class.  The application
has also been translated to Italian by Mauro Biasutti and Russian by
the Sky Dart Team.

This also marks the first release for Android devices.  In this first
release you can open files and examine existing simulations, stability
data and motor files.  The Android port is thanks to work by Kevin
Ruland.

</div>

<div id="1.1.9">

OpenRocket 1.1.9  (2011-11-24)
------------------------------

This release calculates rocket flight in real-world coordinates and
takes into account geodetic effects (including coriolis effect) thanks
to work by Richard Graham.  Printing of transitions, nose cone
profiles and fin marking guides is available thanks to Doug Pedrick.
It also contains some usability features and bug fixes.

</div>

<div id="1.1.8">

OpenRocket 1.1.8  (2011-08-25)
------------------------------

This release contains bug fixes to the optimization methods.
It also contains a workaround to a JRE bug that prevents running
OpenRocket on Java 7.

</div>

<div id="1.1.7">

OpenRocket 1.1.7  (2011-08-12)
------------------------------

This release contains automatic rocket design optimization
functionality.  However, be cautious when using it and take the
results with a grain of salt.

</div>

<div id="1.1.6">

OpenRocket 1.1.6  (2011-07-22)
------------------------------

Internationalization support thanks to work by Boris du Reau and
translations by Tripoli Spain, Tripoli France and Stefan Lobas
(ERIG e.V.).  The release also contains rocket design scaling support
and numerous bug fixes.

</div>

<div id="1.1.5">

OpenRocket 1.1.5  (2011-06-10)
------------------------------

Removed native printing support.  Printing is now handled via PDF
viewer, which should make printing much more reliable and less
bug-prone.

</div>

<div id="1.1.4">

OpenRocket 1.1.4  (2011-03-05)
------------------------------

Initial printing support by Doug Pedrick, and various bug fixes.

</div>

<div id="1.1.3">

OpenRocket 1.1.3  (2010-10-06)
------------------------------

Support for drag-drop moving and copying of components.  Fixes a
severe bug in the undo system.

</div>

<div id="1.1.2">

OpenRocket 1.1.2  (2010-09-07)
------------------------------

Fixes a severe bug that prevented adding stages to rockets.

</div>

<div id="1.1.1">

OpenRocket 1.1.1  (2010-09-03)
------------------------------

Major rewrite of the simulation code, enhanced support for thrust
curve loading and selection, faster startup time and bug fixes.

Old simulation listeners are incompatible with this release.

</div>

<div id="1.1.0">

OpenRocket 1.1.0  (2010-03-21)
------------------------------

Support for loading RockSim rocket design files (.rkt) thanks to
Doug Pedrick.

</div>

<div id="1.0.0">

OpenRocket 1.0.0  (2010-03-10)
------------------------------

Added numerous new motor thrustcurves from thrustcurve.org, and fixed
a few more bugs.

</div>

<div id="0.9.6">

OpenRocket 0.9.6  (2010-02-17)
------------------------------

Updated aerodynamic calculation methods to be more in line with the
Barrowman method and enhanced simulation time step selection.  Fixed
numerous bugs.

</div>

<div id="0.9.5">

OpenRocket 0.9.5  (2009-11-28)
------------------------------

Fixed a serious defect which prevented adding a tube coupler and
centering ring on the same body tube.  Other minor improvements.

</div>

<div id="0.9.4">

OpenRocket 0.9.4  (2009-11-24)
------------------------------

Added through-the-wall fin tabs, attaching components to tube
couplers, material editing and automatic update checks, and fixed
numerous of the most commonly occurring bugs.

</div>

<div id="0.9.3">

OpenRocket 0.9.3  (2009-09-01)
------------------------------

Numerous bug fixes and enhancements including data exporting, showing
flight events in plots, example rocket designs, splitting clustered
inner tubes and automated bug reporting.

</div>

<div id="0.9.2">

OpenRocket 0.9.2  (2009-07-13)
------------------------------

Fixed imperial unit conversions.  Significant UI enhancements to the
motor configuration edit dialog, motor selection dialog and file
open/save.

</div>

<div id="0.9.1">

OpenRocket 0.9.1  (2009-06-09)
------------------------------

Bug fixes to file dialog and saving; initial support for cut/copy/paste
of simulations.

</div>

<div id="0.9.0">

OpenRocket 0.9.0  (2009-05-24)
------------------------------

Initial release.

</div>
</body>
