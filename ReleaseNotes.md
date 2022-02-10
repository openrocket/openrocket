Release Notes
=============

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
    
OpenRocket 14.11 (2014-11-02)
-----------------------------

New Features

  * Updated thrustcurves

Bug Fixes

  * Fixed a couple of bugs.

OpenRocket 14.06 (2014-06-25)
-----------------------------

New Features

  * Klima motor textures
  * Added knots to windspeed and velocity units
  * Updated thrustcurves

Bug Fixes

  * Fixed annoying IndexOutOfBounds bug in tables.

OpenRocket 14.05 (2014-05-21)
-----------------------------

New Features

  * Compute optimimum delay time when simulating
  * Display cg/mass overrides using icons in the component tree

Bug Fixes

  * Bug fixes in the motor selection dialog
  * Updated thrustcurves
  * Updated 3d libraries to 2.1.5

OpenRocket 14.03 (2014-03-20)
-----------------------------

New Features

  * Photo Realistic 3d rocket renderer

Bug Fixes

  * Fixes to the flight configuration tab and motor selection dialog
  * Updated thrustcurves

OpenRocket 13.11.2 (2014-01-01)
-------------------------------

Bug Fixes

  * Numerous bug fixes and usability improvements in the new
      flight configuration tab.
  * Fix couple of layout issues
  * Updated Spanish, French and Chinese translations

OpenRocket 13.11.1 (2013-11-15)
-------------------------------

Bug Fixes

  * Added back the TubeConfiguration Configuration dialog
  * Seems the jogl update didn't happen in the 13.11 build.
  * Made the motor filter remember previous settings
  * Fixed various exceptions in flight configuration tables due to column reordering
  * Fixed NPE when deleting a configuration

OpenRocket 13.11  (2013-11-08)
------------------------------

New Features

  * Chinese translations
  * Replaced flight configuration dialog with more efficient configuration tab
  * Improved filtering in motor chooser dialog

Bug Fixes

  * Updated jogl to correct 3d problems on various platforms
  * Fixed NPE introduced by changes in Java 1.7.0_45-b18

OpenRocket 13.09.1 (2013-10-05)
-------------------------------

This release contains a number of bug fixes, updated 3D JOGL
libraries.  Added preliminary thrustcurves for Aerotech C3 and D2 18mm
reloads.


OpenRocket 13.09 (2013-09-08)
-----------------------------

This release contains a number of bug fixes, updated 3D JOGL
libraries, and separates simulation edit and plot dialogs.


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


OpenRocket 12.09.1 (2012-06-28)
-------------------------------

Bug-fix release for 12.09, fixing numerous bugs.  Only new feature is
the possiblity to automatically open the latest design file on startup
(in Edit -> Preferences -> Options).


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


OpenRocket 1.1.9  (2011-11-24)
------------------------------

This release calculates rocket flight in real-world coordinates and
takes into account geodetic effects (including coriolis effect) thanks
to work by Richard Graham.  Printing of transitions, nose cone
profiles and fin marking guides is available thanks to Doug Pedrick.
It also contains some usability features and bug fixes.


OpenRocket 1.1.8  (2011-08-25)
------------------------------

This release contains bug fixes to the optimization methods.
It also contains a workaround to a JRE bug that prevents running
OpenRocket on Java 7.


OpenRocket 1.1.7  (2011-08-12)
------------------------------

This release contains automatic rocket design optimization
functionality.  However, be cautious when using it and take the
results with a grain of salt.


OpenRocket 1.1.6  (2011-07-22)
------------------------------

Internationalization support thanks to work by Boris du Reau and
translations by Tripoli Spain, Tripoli France and Stefan Lobas
(ERIG e.V.).  The release also contains rocket design scaling support
and numerous bug fixes.


OpenRocket 1.1.5  (2011-06-10)
------------------------------

Removed native printing support.  Printing is now handled via PDF
viewer, which should make printing much more reliable and less
bug-prone.


OpenRocket 1.1.4  (2011-03-05)
------------------------------

Initial printing support by Doug Pedrick, and various bug fixes.


OpenRocket 1.1.3  (2010-10-06)
------------------------------

Support for drag-drop moving and copying of components.  Fixes a
severe bug in the undo system.


OpenRocket 1.1.2  (2010-09-07)
------------------------------

Fixes a severe bug that prevented adding stages to rockets.


OpenRocket 1.1.1  (2010-09-03)
------------------------------

Major rewrite of the simulation code, enhanced support for thrust
curve loading and selection, faster startup time and bug fixes.

Old simulation listeners are incompatible with this release.


OpenRocket 1.1.0  (2010-03-21)
------------------------------

Support for loading RockSim rocket design files (.rkt) thanks to
Doug Pedrick.


OpenRocket 1.0.0  (2010-03-10)
------------------------------

Added numerous new motor thrustcurves from thrustcurve.org, and fixed
a few more bugs.


OpenRocket 0.9.6  (2010-02-17)
------------------------------

Updated aerodynamic calculation methods to be more in line with the
Barrowman method and enhanced simulation time step selection.  Fixed
numerous bugs.


OpenRocket 0.9.5  (2009-11-28)
------------------------------

Fixed a serious defect which prevented adding a tube coupler and
centering ring on the same body tube.  Other minor improvements.


OpenRocket 0.9.4  (2009-11-24)
------------------------------

Added through-the-wall fin tabs, attaching components to tube
couplers, material editing and automatic update checks, and fixed
numerous of the most commonly occurring bugs.


OpenRocket 0.9.3  (2009-09-01)
------------------------------

Numerous bug fixes and enhancements including data exporting, showing
flight events in plots, example rocket designs, splitting clustered
inner tubes and automated bug reporting.


OpenRocket 0.9.2  (2009-07-13)
------------------------------

Fixed imperial unit conversions.  Significant UI enhancements to the
motor configuration edit dialog, motor selection dialog and file
open/save.


OpenRocket 0.9.1  (2009-06-09)
------------------------------

Bug fixes to file dialog and saving; initial support for cut/copy/paste
of simulations.


OpenRocket 0.9.0  (2009-05-24)
------------------------------

Initial release.
