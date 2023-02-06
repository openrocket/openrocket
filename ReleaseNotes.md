<!-- 

!! NOTE: This HTML formatting is required for the welcome info dialog.
Always encapsulate each release with a <div> tag and the correct id.

-->

<body>
<div>

Release Notes
=============

</div>

<div id="22.02">

OpenRocket 22.02 (2023-02-08)
------------------------

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
