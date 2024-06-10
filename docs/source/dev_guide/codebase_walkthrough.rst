********************
Codebase Walkthrough
********************

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

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

The following is an overview of the packages in the ``info.openrocket.core`` module (:file:`openrocket/core/src/main/java/info/openrocket/core`):

.. code-block:: none

   ├── aerodynamics            # Calculation of aerodynamic properties (e.g. aerodynamic forces, CD)
   │   └── barrowman           # Barrowman method for calculating aerodynamic properties
   ├── appearance              # Appearance of components (e.g. color, texture)
   │   └── defaults            # Default appearance settings
   ├── arch                    # Get info on the system architecture (macOS, Windows, Linux)
   ├── communication           # Communication with external sites/programs (e.g. retrieve the latest version of OpenRocket from GitHub)
   ├── database                # Database handling (component database, motor database)
   │   └── motor               # Thrust curve (i.e. motor) database
   ├── document                # OpenRocket document and simulation handling
   │   ├── attachments         # Attachments to OpenRocket documents
   │   └── events              # OpenRocket events (i.e. document changed, simulation changed)
   ├── file                    # File handling
   │   ├── iterator            # Iterate files in e.g. a directory or a zip file
   │   ├── motor               # Motor files handling
   │   ├── openrocket          # OpenRocket file handling
   │   │  ├── importt          # Import OpenRocket files
   │   │  └── savers           # Save OpenRocket files
   │   ├── rasaero             # RASAero II file handling
   │   │  ├── export           # Export OpenRocket files to RASAero II
   │   │  └── importt          # Import RASAero II files to OpenRocket
   │   ├── rocksim             # RockSim file handling
   │   │  ├── export           # Export OpenRocket files to RockSim
   │   │  └── importt          # Import RockSim files to OpenRocket
   │   ├── simplesax           # XML file handling
   │   ├── svg                 # SVG file handling
   │   │  └── export           # SVG export
   │   └── wavefrontobj        # Wavefront OBJ file handling
   │      └── export           # Export OpenRocket components to Wavefront OBJ
   │         ├── components    # Export OpenRocket components
   │         └── shapes        # Export general geometry shapes
   ├── formatting              # Formatting of e.g. motor config names
   ├── gui
   │   └── util                # Filename filter
   ├── l10n                    # Localization (translation of OpenRocket into languages other than English)
   ├── logging                 # Errors, warnings, and aborts
   ├── masscalc                # Mass property (e.g. mass, CG, moments of inertia) calculation drivers. Actual component mass calculations are in the components themselves
   ├── material                # Material properties (physical properties of materials)
   ├── models                  # Physical models (e.g. atmosphere, gravity, wind)
   │   ├── atmosphere          # Atmosphere models
   │   ├── gravity             # Gravity models
   │   └── wind                # Wind models
   ├── motor                   # Motor configuration, ID, and thrustcurves
   ├── optimization            # Optimization algorithms
   │   ├── general             # Parameter search space algorithms
   │   │   ├── multidim        # Multidimensional parallel search optimization
   │   │   └── onedim          # One dimensional golden-section search optimization
   │   ├── rocketoptimization  # Optimization of rocket parameters for specified goal functions
   │   │   ├── domains         # Limits on optimization parameters
   │   │   ├── goals           # Max, min, and specific value optimization goals
   │   │   ├── modifiers       # Modify rocket parameters
   │   │   └── parameters      # Simulation results that can be optimized
   │   └── services            # Provide parameters etc to optimizer
   ├── plugin                  # Plugin interface (more general but less developed than extension interface)
   ├── preset                  # Component presets
   │   ├── loader              # Component database file loader
   │   └── xml                 # Component database file writer
   ├── rocketcomponent         # Rocket components (e.g. fins, nose cone, tube)
   │   └── position            # Position of rocket components
   ├── rocketvisitors          # Create lists of components and motors
   ├── scripting               # Javascript scripting of OR functionality
   ├── simulation              # Flight simulation code
   │   ├── customexpression    # User defined custom expression handling
   │   ├── exception           # Exceptions occurring during simulation
   │   ├── extension           # User defined simulation extensions
   │   │   ├── example         # Examples of simulation extensions
   │   │   └── impl            # Helper methods for implementing extensions
   │   └── listeners           # Code "listening" to simulation to implement functionality
   │       ├── example         # Example user listeners
   │       └── system          # Listeners used by OpenRocket itself
   ├── startup                 # Root Application and related classes
   ├── thrustcurve             # Thrustcurve file and thrustcurve.org API
   ├── unit                    # Definitions of units and unit conversions
   ├── util                    # Miscellaneous utility methods
   │   └── enums               # Conversion of enums to names
   └── utils                   # More utility methods


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
