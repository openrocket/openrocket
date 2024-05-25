********************
Codebase Walkthrough
********************

Important Modules and Packages
==============================

Root Directory Structure
========================



Module Folder Structure
=======================

OpenRocket uses the Gradle build system, where each modules (``info.openrocket.core`` and ``info.openrocket.swing``) adheres to the following folder structure:

.. code-block:: none

   ├── gradle                    # Gradle Wrapper
   ├── libs                      # (optional) Library JAR files that cannot be obtained from the gradle dependency system
   ├── resources-src             # Source files for the resources in the src dir (e.g. InkScape project file for the splash screen)
   ├── scripts                   # Utility scripts
   ├── src                       # Source code and resources
   │   ├── main                  # Application source code and resources
   │   │   ├── java              # Java source code
   │   │   ├── resources         # Resource files (e.g. images, configuration files, data files)
   │   ├── test                  # Test source code and resources
   │   │   ├── java              # Java test source code
   │   │   ├── resources         # Resource files for testing
   ├── src-extra                 # Extra source code, not part of the main application (e.g. template code for an OpenRocket plugin)

Core Module
-----------

The following is an overview of the packages in the ``info.openrocket.core`` module (*openrocket/core/src/main/java/info/openrocket/core*):

.. code-block:: none

   ├── aerodynamics        # Calculation of aerodynamic properties (e.g. drag)
   │   └── barrowman       # Barrowman method for calculating coefficient of drag (CD)
   ├── appearance          # Appearance of components (e.g. color, texture)
   │   └── defaults        # Default appearance settings
   ├── arch                # Get info on the system architecture (macOS, Windows, Linux)
   ├── communication       # Communication with external sites/programs (e.g. retrieve the latest version of OpenRocket from GitHub)
   ├── database            # Database handling (component database, motor database)
   │   └── motor           # Thrust curve database
   ├── document            # OpenRocket document and simulation handling
   │   ├── attachments     # Attachments to OpenRocket documents
   │   └── events          # OpenRocket events (e.g. document changed, simulation changed)
   ├── file                # File handling
   │   ├── configuration
   │   ├── iterator        # Iterate files in e.g. a directory or a zip file
   │   ├── motor           # Motor files handling
   │   ├── openrocket      # OpenRocket file handling
   │   │  ├── importt      # Import OpenRocket files
   │   │  └── savers       # Save OpenRocket files
   │   ├── rasaero         # RASAero II file handling
   │   │  ├── export       # Export OpenRocket files to RASAero II
   │   │  └── importt      # Import RASAero II files to OpenRocket
   │   ├── rocksim         # RockSim file handling
   │   │  ├── export       # Export OpenRocket files to RockSim
   │   │  └── importt      # Import RockSim files to OpenRocket
   │   ├── simplesax       # XML file handling
   │   ├── svg             # SVG file handling
   │   │  └── export       # SVG export
   │   └── wavefrontobj    # Wavefront OBJ file handling
   │      └── export       # Export OpenRocket components to Wavefront OBJ
   │         ├── components    # Export OpenRocket components
   │         └── shapes        # Export general geometry shapes
   ├── formatting          # Formatting of e.g. motor config names
   ├── gui
   │   └── util
   ├── l10n                # Translation of OpenRocket
   ├── logging             # Logging and message handling (e.g. error and warning messages)
   ├── masscalc            # Calculation of mass properties (weight and center of gravity)
   ├── material            # Material properties (physical properties of materials)
   ├── models              # Physical models (e.g. atmosphere, gravity, wind)
   │   ├── atmosphere      # Atmosphere models
   │   ├── gravity         # Gravity models
   │   └── wind            # Wind models
   ├── motor
   ├── optimization        # Optimization algorithms
   │   ├── general
   │   │   ├── multidim
   │   │   └── onedim
   │   ├── rocketoptimization
   │   │   ├── domains
   │   │   ├── goals
   │   │   ├── modifiers
   │   │   └── parameters
   │   └── services
   ├── plugin
   ├── preset
   │   ├── loader
   │   └── xml
   ├── rocketcomponent     # Rocket components (e.g. fins, nose cone, tube)
   │   └── position        # Position of rocket components
   ├── rocketvisitors
   ├── scripting
   ├── simulation
   │   ├── customexpression
   │   ├── exception
   │   ├── extension
   │   │   ├── example
   │   │   └── impl
   │   └── listeners
   │       ├── example
   │       └── system
   ├── startup
   ├── thrustcurve
   ├── unit
   ├── util
   │   └── enums
   └── utils


Swing Module
------------

The following is an overview of the packages in the ``info.openrocket.swing`` module (*openrocket/swing/src/main/java/info/openrocket/swing*):

.. code-block:: none

   ├── communication
   ├── file
   │   ├── motor
   │   ├── photo
   │   └── wavefrontobj
   ├── gui
   │   ├── adaptors
   │   ├── components
   │   │   └── compass
   │   ├── configdialog
   │   ├── customexpression
   │   ├── dialogs
   │   │   ├── flightconfiguration
   │   │   ├── motor
   │   │   │   └── thrustcurve
   │   │   ├── optimization
   │   │   ├── preferences
   │   │   └── preset
   │   ├── figure3d
   │   │   ├── geometry
   │   │   └── photo
   │   │      ├── exhaust
   │   │      └── sky
   │   │         └── builtin
   │   ├── figureelements
   │   ├── help
   │   │   └── tours
   │   ├── main
   │   │   ├── componenttree
   │   │   └── flightconfigpanel
   │   ├── plot
   │   ├── preset
   │   ├── print
   │   │   ├── components
   │   │   └── visitor
   │   ├── rocketfigure
   │   ├── scalefigure
   │   ├── simulation
   │   ├── theme
   │   ├── util
   │   ├── watcher
   │   └── widgets
   ├── logging
   ├── simulation
   │   └── extension
   │      ├── example
   │      └── impl
   ├── startup
   │   ├── jij
   │   └── providers
   └── utils

Units used in OpenRocket
========================

OpenRocket always uses internally pure SI units. For example all rocket dimensions and flight distances are in meters, all
masses are in kilograms, density is in kg/m³, temperature is in Kelvin etc. This convention is also used when storing the
design in the OpenRocket format.

The only exception to this rule is angles:

- Angles are represented as radians internally, but in the file format they are converted to degrees. This is to make
  the file format more human-readable and to avoid rounding errors.

- Latitude and longitude of the launch site are represented in degrees both internally and externally.

When displaying measures to the user, the values are converted into the preferred units of the user. This is performed
using classes in the package ``info.openrocket.core.unit``. The ``Unit`` class represents a single unit and it includes methods for
converting between that unit and SI units in addition to creating a string representation with a suitable amount of decimals.
A ``UnitGroup`` describes a measurable quantity such as temperature and contains the units available for that quantity,
such as Celsius, Fahrenheit and Kelvin.
