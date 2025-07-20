******************
File Specification
******************

This page describes the specification of OpenRocket's design file format (:file:`.ork`), and the OpenRocket component
file format (:file:`.orc`).

.. contents:: Table of Contents
   :depth: 2
   :local:

----

OpenRocket Design File (.ork)
=============================

The OpenRocket design file format is a human-readable XML format that describes a rocket design. It contains all the
information needed to recreate the design in OpenRocket, including the rocket's components, materials, and simulation
settings. The file format is designed to be extensible, allowing for the addition of new features and properties in a
backward-compatible way.

.. note::

   Not every parameter in the XML file is explained here. Please refer to the
   :file:`core/src/main/java/info/openrocket/core/file/openrocket` package in the
   `source code <https://github.com/openrocket/openrocket/tree/unstable/core/src/main/java/info/openrocket/core/file/openrocket>`__.

File Architecture
-----------------

The OpenRocket design file (:file:`.ork`) is a ZIP archive containing an XML format file and any associated graphics files.
While OpenRocket also accepts the :file:`.ork.gz` extension, the plain :file:`.ork` extension is recommended.

To examine the contents of an :file:`.ork` file:

1. Rename the file extension from :file:`.ork` to :file:`.zip`
2. Extract the contents using any ZIP extraction tool
3. The main rocket design is stored in the XML file named `rocket.ork`

Version Control
---------------

The file format uses semantic versioning, indicated by the ``version`` attribute in the root ``<openrocket>`` tag.
The version number follows these rules:

* Major version increases indicate backward-incompatible changes
* Minor version increases indicate backward-compatible additions
* The version is internally stored as an integer, divided by 100 (e.g., version "1.10" is stored as 110)
* For maximum compatibility, files should be saved using the oldest format version that supports all required features

The ``creator`` attribute optionally specifies the software and version used to create the file.

Version History
---------------

The following table lists the version history of the OpenRocket design file format (taken from
`fileformat.txt <https://github.com/openrocket/openrocket/blob/unstable/fileformat.txt>`_ file in the root directory
of our repository).

.. list-table::
   :header-rows: 1
   :widths: 10 20 70

   * - Version
     - Release
     - Changes
   * - 1.0
     - OpenRocket 0.9.0
     - Initial public release
   * - 1.1
     - OpenRocket 0.9.4
     - * Added fin tab support (``<tabheight>``, ``<tablength>``, ``<tabposition>`` elements)
       * Enabled subcomponent attachment to tube couplers
   * - 1.2
     - OpenRocket 1.1.1
     - Added ``<digest>`` tag for motor definitions to uniquely identify thrust curve characteristics
   * - 1.3
     - OpenRocket 1.1.9
     - Added ``<launchlongitude>`` and ``<geodeticmethod>`` parameters for simulation conditions
   * - 1.4
     - OpenRocket 12.03
     - * Added ``launchrodvelocity`` and ``deploymentvelocity`` attributes to ``<flightdata>``
       * Modified motor digesting algorithm
       * Added ``<separationevent>`` and ``<separationdelay>`` elements for stage components
   * - 1.5
     - OpenRocket 12.09
     - * Introduced ComponentPresets
       * Added lowerstageseparation as recovery device deployment event
       * Added ``<datatypes>`` section for custom expressions
   * - 1.6
     - OpenRocket 13.04
     - * Added component Appearances (decals & paint)
       * Added configurable parameters for recovery devices, motor ignition and separation
   * - 1.7
     - OpenRocket 15.03
     - * Added simulation extensions and configuration
       * Added support for TubeFins
   * - 1.8
     - OpenRocket 22.02
     - * Added new components (RailButton ``<railbutton>``, Pods ``<podset>``, Booster ``<parallelstage>``)
       * Added internal appearance options
       * Added PhotoStudio settings
       * Added override CD parameter
       * Added stage activeness remembrance
       * Added nose cone flip parameter
       * Separated override subcomponents
       * Renamed various parameters for clarity
   * - 1.9
     - OpenRocket 23.09
     - Added component IDs for flight event sources
   * - 1.10
     - OpenRocket 24.12
     - * Added simulation warning priority
       * Added document preferences
       * Added wind model settings
       * Added warning flight events
       * Added maximum time attribute for simulation conditions

----

Root Structure
--------------

The following shows the root XML structure of an OpenRocket design file:

.. code-block:: xml

   <?xml version='1.0' encoding='utf-8'?>
   <openrocket version="1.10" creator="OpenRocket 24.12.beta.01">
      <rocket>
         <!-- Rocket definition -->
      </rocket>
      <simulations>
         <!-- Simulation data -->
      </simulations>
      <photostudio>
         <!-- PhotoStudio settings -->
      </photostudio>
      <docprefs>
         <!-- Document preferences -->
      </docprefs>
   </openrocket>

The file must begin with the XML declaration:

.. code-block:: xml

    <?xml version='1.0' encoding='utf-8'?>

Then follows the actual content of the OpenRocket design file, starting with the ``<openrocket>`` tag with the following
content:

* ``rocket``: Rocket definition (see :ref:`Rocket<Rocket>`)
* ``simulations``: Simulation data (see :ref:`Simulation Data <SimulationData>`)
* ``photostudio``: PhotoStudio settings (see :ref:`PhotoStudio Settings <PhotoStudioSettings>`)
* ``docprefs``: Document preferences (see :ref:`Document Preferences <DocumentPreferences>`)

----

.. _Rocket:

Rocket
------

The ``<rocket>`` contains metadata of the rocket (name, designer...), motor configurations in the design, and all the
rocket component definitions:

.. code-block:: xml
   :emphasize-lines: 1

   <rocket>
      <!-- Rocket definition -->
      <name>[ROCKET NAME]</name>
      <id>[UUID]</id>
      <axialoffset method="absolute">0.0</axialoffset>
      <position type="absolute">0.0</position>
      <designer>[DESIGNER]</designer>
      <motorconfiguration configid="[CONFIG ID]">
         <!-- Motor configuration -->
      </motorconfiguration>

      <subcomponents>
         <!-- Rocket components -->
      </subcomponents>
   </rocket>

The ``<rocket>`` element contains the following primary attributes:

* ``name``: Name of the rocket design
* ``id``: Unique identifier (UUID) - automatically generated by OpenRocket
* ``axialoffset``: Absolute position offset
* ``designer``: Designer's name
* ``motorconfiguration``: One or more motor configurations
* ``subcomponents``: Container for all the rocket components in the design

----

Motor Configuration
-------------------

Each ``<motorconfiguration>`` element defines a specific motor setup:

.. code-block:: xml
   :emphasize-lines: 1

    <motorconfiguration configid="[UUID]" default="true|false">
        <stage number="0" active="true|false"/>
    </motorconfiguration>

----

Rocket Components
-----------------

Common Component Attributes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Most components share these common attributes:

* ``id``: Unique identifier (UUID) - automatically generated by OpenRocket
* ``name``: Component name
* ``finish``: Surface finish type
* ``material``: Material specifications including:

   * ``type``: (bulk, surface, line)
   * ``density``: Material density
   * ``group``: Material category

Position and Offset Attributes:

* ``axialoffset``: Position along the rocket's axis
* ``position``: Legacy position attribute
* ``radialdirection``: Angular direction for radial components
* ``radialposition``: Distance from center axis

----

Appearance Settings
^^^^^^^^^^^^^^^^^^^
Components can have appearance settings defined using the ``<appearance>`` tag:

.. code-block:: xml
   :emphasize-lines: 1

    <appearance>
        <paint red="51" green="51" blue="51" alpha="255"/>
        <shine>0.5</shine>
        <decal name="decals/BodyStripe.png" rotation="0.0" edgemode="CLAMP">
            <center x="0.0" y="0.0"/>
            <offset x="0.0" y="-0.2"/>
            <scale x="1.0" y="10.0"/>
        </decal>
    </appearance>

Inside Appearance
"""""""""""""""""

Components can have internal appearance defined separately from external (e.g. for tubes or fins):

.. code-block:: xml
   :emphasize-lines: 1

    <insideappearance>
        <edgessameasinside>false</edgessameasinside>
        <insidesameasoutside>false</insidesameasoutside>
        <paint red="187" green="187" blue="187" alpha="255"/>
        <shine>0.3</shine>
        <decal name="decals/mainBodyWrap2.png" rotation="0.0" edgemode="REPEAT">
            <center x="0.0" y="0.0"/>
            <offset x="0.0" y="0.0"/>
            <scale x="1.0" y="1.0"/>
        </decal>
    </insideappearance>

----

<subcomponents>
"""""""""""""""
Used in assembly components (rocket, stage, pod set, body tube, ...) as a container for child components.

<stage>
"""""""
Contains components for each defined stage.

.. code-block:: xml
   :emphasize-lines: 1

    <stage>
        <name>Booster</name>
        <id>[UUID]</id>
        <separationevent>ejection</separationevent>
        <separationaltitude>200.0</separationaltitude>
        <separationdelay>0.0</separationdelay>
        <separationconfiguration configid="[UUID]">
            <separationevent>burnout</separationevent>
            <separationaltitude>200.0</separationaltitude>
            <separationdelay>0.0</separationdelay>
        </separationconfiguration>
    </stage>

<nosecone>
""""""""""

The ``<nosecone>`` element defines the **Nose Cone** component:

.. code-block:: xml
   :emphasize-lines: 1

   <nosecone>
      <name>Nose cone</name>
      <id>[UUID]</id>
      <finish>normal</finish>
      <material type="bulk" density="500.0">Basswood</material>
      <length>0.22</length>
      <thickness>0.002</thickness>
      <shape>power</shape>
      <shapeclipped>false</shapeclipped>
      <shapeparameter>0.4</shapeparameter>
      <aftradius>0.075</aftradius>
      <aftshoulderradius>0.0</aftshoulderradius>
      <aftshoulderlength>0.0</aftshoulderlength>
      <aftshoulderthickness>0.0</aftshoulderthickness>
      <aftshouldercapped>false</aftshouldercapped>
   </nosecone>

Note: Shape values are typically same as editor fields, except:
* "power" for Power
* "parabolic" for Parabolic
* "haack" for Haack series

<bodytube>
""""""""""

The ``<bodytube>`` element defines the **Body Tube** component:

.. code-block:: xml
   :emphasize-lines: 1

   <bodytube>
      <name>Body tube</name>
      <id>[UUID]</id>
      <finish>normal</finish>
      <material type="bulk" density="680.0">Cardboard</material>
      <length>0.3</length>
      <thickness>0.002</thickness>
      <radius>auto</radius>
   </bodytube>

<transition>
""""""""""""

The ``<transition>`` element defines the **Transition** component:

.. code-block:: xml
   :emphasize-lines: 1

   <transition>
      <name>Transition</name>
      <id>[UUID]</id>
      <finish>normal</finish>
      <material type="bulk" density="500.0">Basswood</material>
      <length>0.11</length>
      <thickness>0.002</thickness>
      <shape>haack</shape>
      <shapeclipped>true</shapeclipped>
      <shapeparameter>0.3</shapeparameter>
      <foreradius>auto</foreradius>
      <aftradius>0.05</aftradius>
      <foreshoulderradius>0.0</foreshoulderradius>
      <foreshoulderlength>0.0</foreshoulderlength>
      <foreshoulderthickness>0.0</foreshoulderthickness>
      <foreshouldercapped>false</foreshouldercapped>
      <aftshoulderradius>0.0</aftshoulderradius>
      <aftshoulderlength>0.0</aftshoulderlength>
      <aftshoulderthickness>0.0</aftshoulderthickness>
      <aftshouldercapped>false</aftshouldercapped>
   </transition>

<railbutton>
""""""""""""

The ``<railbutton>`` element defines the **Rail Button** component:

.. code-block:: xml
   :emphasize-lines: 1

   <railbutton>
      <name>Rail Button</name>
      <id>[UUID]</id>
      <instancecount>1</instancecount>
      <instanceseparation>0.0582</instanceseparation>
      <angleoffset method="relative">180.0</angleoffset>
      <axialoffset method="middle">0.0</axialoffset>
      <position type="middle">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="1420.0" group="Plastics">Delrin</material>
      <outerdiameter>0.0097</outerdiameter>
      <innerdiameter>0.008</innerdiameter>
      <height>0.0097</height>
      <baseheight>0.002</baseheight>
      <flangeheight>0.002</flangeheight>
      <screwheight>0.0</screwheight>
   </railbutton>

<launchlug>
"""""""""""

The ``<launchlug>`` element defines the **Launch Lug** component:

.. code-block:: xml
   :emphasize-lines: 1

   <launchlug>
      <name>12. Launch lug</name>
      <id>[UUID]</id>
      <instancecount>1</instancecount>
      <instanceseparation>0.0</instanceseparation>
      <angleoffset method="relative">19.0</angleoffset>
      <radialdirection>19.0</radialdirection>
      <axialoffset method="middle">0.0</axialoffset>
      <position type="middle">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="680.0">Cardboard</material>
      <radius>0.0035</radius>
      <length>0.035</length>
      <thickness>0.001</thickness>
   </launchlug>

----

Motor Mount
^^^^^^^^^^^

Components such as body tubes and inner tubes can be used as motor mounts. If they are, a ``<motormount>`` element will
be present in the XML component block:

.. code-block:: xml
   :emphasize-lines: 3

   <bodytube>
      <!-- Other Body Tube definitions -->
      <motormount>
         <ignitionevent>automatic</ignitionevent>
         <ignitiondelay>0.0</ignitiondelay>
         <overhang>0.003</overhang>
         <motor configid="[UUID]">
            <type>single</type>
            <manufacturer>Estes</manufacturer>
            <digest>22aec01287ea1e3b8c6f66b26fe5fea6</digest>
            <designation>A8</designation>
            <diameter>0.018</diameter>
            <length>0.07</length>
            <delay>3.0</delay>
         </motor>
         <ignitionconfiguration configid="[UUID]">
            <ignitionevent>automatic</ignitionevent>
            <ignitiondelay>0.0</ignitiondelay>
         </ignitionconfiguration>
      </motormount>
   </bodytube>

----

Fin Sets
^^^^^^^^

<freeformfinset>
""""""""""""""""

The ``<freeformfinset>`` element defines the **Freeform Fin Set** component:

.. code-block:: xml
   :emphasize-lines: 1

   <freeformfinset>
      <name>Freeform fin set</name>
      <id>[UUID]</id>
      <position type="bottom">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="500.0">Basswood</material>
      <fincount>5</fincount>
      <rotation>0.0</rotation>
      <thickness>0.003</thickness>
      <crosssection>airfoil</crosssection>
      <cant>0.0</cant>
      <filletradius>0.0</filletradius>
      <filletmaterial type="bulk" density="680.0">Cardboard</filletmaterial>
      <finpoints>
         <point x="0.0" y="0.0"/>
         <point x="0.03882760416666667" y="0.056620833333333336"/>
         <point x="0.11000052083333334" y="0.05582708333333333"/>
         <point x="0.07951041666666667" y="0.0"/>
      </finpoints>
   </freeformfinset>

<trapezoidfinset>
"""""""""""""""""

The ``<trapezoidfinset>`` element defines the **Trapezoidal Fin Set** component:

.. code-block:: xml
   :emphasize-lines: 1

   <trapezoidfinset>
      <name>Trapezoidal fin set</name>
      <id>[UUID]</id>
      <position type="bottom">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="680.0">Cardboard</material>
      <fincount>3</fincount>
      <rotation>0.0</rotation>
      <thickness>0.003</thickness>
      <crosssection>square</crosssection>
      <cant>0.0</cant>
      <filletradius>0.0</filletradius>
      <filletmaterial type="bulk" density="680.0">Cardboard</filletmaterial>
      <rootchord>0.05</rootchord>
      <tipchord>0.05</tipchord>
      <sweeplength>0.025</sweeplength>
      <height>0.03</height>
   </trapezoidfinset>

<ellipticalfinset>
""""""""""""""""""

The ``<ellipticalfinset>`` element defines the **Elliptical Fin Set** component:

.. code-block:: xml
   :emphasize-lines: 1

   <ellipticalfinset>
      <name>Elliptical fin set</name>
      <id>[UUID]</id>
      <position type="bottom">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="680.0">Cardboard</material>
      <fincount>3</fincount>
      <rotation>0.0</rotation>
      <thickness>0.003</thickness>
      <crosssection>square</crosssection>
      <cant>0.0</cant>
      <filletradius>0.0</filletradius>
      <filletmaterial type="bulk" density="680.0">Cardboard</filletmaterial>
      <rootchord>0.05</rootchord>
      <height>0.05</height>
   </ellipticalfinset>

<tubefinset>
""""""""""""

The ``<tubefinset>`` element defines the **Tube Fin Set** component:

.. code-block:: xml
   :emphasize-lines: 1

   <tubefinset>
      <name>Tube Fin Set</name>
      <id>[UUID]</id>
      <instancecount>6</instancecount>
      <fincount>6</fincount>
      <radiusoffset method="coaxial">0.0</radiusoffset>
      <angleoffset method="fixed">0.0</angleoffset>
      <rotation>0.0</rotation>
      <axialoffset method="bottom">0.0</axialoffset>
      <position type="bottom">0.0</position>
      <finish>normal</finish>
      <material type="bulk" density="680.0" group="PaperProducts">Cardboard</material>
      <fincount>6</fincount>
      <rotation>0.0</rotation>
      <radius>auto</radius>
      <length>0.1</length>
      <thickness>0.002</thickness>
   </tubefinset>

----

Inner Components
^^^^^^^^^^^^^^^^

<innertube>
"""""""""""

The ``<innertube>`` element defines the **Inner Tube** component:

.. code-block:: xml
   :emphasize-lines: 1

   <innertube>
      <name>Inner Tube</name>
      <id>[UUID]</id>
      <axialoffset method="bottom">0.0</axialoffset>
      <position type="bottom">0.0</position>
      <material type="bulk" density="680.0" group="PaperProducts">Cardboard</material>
      <length>0.07</length>
      <radialposition>0.0</radialposition>
      <radialdirection>0.0</radialdirection>
      <outerradius>0.0095</outerradius>
      <thickness>5.000000000000004E-4</thickness>
      <clusterconfiguration>single</clusterconfiguration>
      <clusterscale>1.0</clusterscale>
      <clusterrotation>0.0</clusterrotation>
   </innertube>

<tubecoupler>
"""""""""""""

The ``<tubecoupler>`` element defines the **Tube Coupler** component:

.. code-block:: xml
   :emphasize-lines: 1

   <tubecoupler>
      <name>Tube Coupler</name>
      <id>[UUID]</id>
      <axialoffset method="bottom">0.03175</axialoffset>
      <material type="bulk" density="943.0">Phenolic</material>
      <length>0.0635</length>
      <outerradius>auto</outerradius>
      <thickness>5.0E-4</thickness>
   </tubecoupler>

<centeringring>
"""""""""""""""

The ``<centeringring>`` element defines the **Centering Ring** component:

.. code-block:: xml
   :emphasize-lines: 1

   <centeringring>
      <name>Centering Ring</name>
      <id>[UUID]</id>
      <instancecount>1</instancecount>
      <axialoffset method="bottom">-0.05715</axialoffset>
      <material type="bulk" density="657.0">Fiber</material>
      <length>0.00635</length>
      <outerradius>0.016269</outerradius>
      <innerradius>auto</innerradius>
   </centeringring>

<bulkhead>
""""""""""

The ``<bulkhead>`` element defines the **Bulkhead** component:

.. code-block:: xml
   :emphasize-lines: 1

   <bulkhead>
      <name>Bulkhead</name>
      <id>[UUID]</id>
      <instancecount>1</instancecount>
      <instanceseparation>0.0</instanceseparation>
      <axialoffset method="bottom">0.0</axialoffset>
      <position type="bottom">0.0</position>
      <material type="bulk" density="680.0" group="PaperProducts">Cardboard</material>
      <length>0.002</length>
      <radialposition>0.0</radialposition>
      <radialdirection>0.0</radialdirection>
      <outerradius>auto</outerradius>
   </bulkhead>

<engineblock>
"""""""""""""

The ``<engineblock>`` element defines the **Engine Block** component:

.. code-block:: xml
   :emphasize-lines: 1

   <engineblock>
      <name>Engine Block</name>
      <id>[UUID]</id>
      <axialoffset method="bottom">-0.0635</axialoffset>
      <material type="bulk" density="657.0">Fiber</material>
      <length>0.00635</length>
      <outerradius>0.008992</outerradius>
      <thickness>7.366E-4</thickness>
   </engineblock>

----

Mass Components
^^^^^^^^^^^^^^^

<parachute>
"""""""""""

The ``<parachute>`` element defines the **Parachute** component:

.. code-block:: xml
   :emphasize-lines: 1

    <parachute>
        <name>Parachute</name>
         <id>[UUID]</id>
        <axialoffset method="top">0.032</axialoffset>
        <packedlength>0.042</packedlength>
        <packedradius>0.009</packedradius>
        <cd>auto</cd>
        <material type="surface" density="0.067">Ripstop nylon</material>
        <deployevent>ejection</deployevent>
        <deployaltitude>200.0</deployaltitude>
        <deploydelay>0.0</deploydelay>
        <diameter>0.3</diameter>
        <linecount>6</linecount>
        <linelength>0.3</linelength>
        <linematerial type="line" density="0.0018">Elastic cord</linematerial>
    </parachute>


<streamer>
""""""""""

The ``<streamer>`` element defines the **Streamer** component:

.. code-block:: xml
   :emphasize-lines: 1

   <streamer>
      <name>Streamer</name>
      <id>[UUID]</id>
      <axialoffset method="top">0.0</axialoffset>
      <position type="top">0.0</position>
      <packedlength>0.025</packedlength>
      <packedradius>0.0125</packedradius>
      <radialposition>0.0</radialposition>
      <radialdirection>0.0</radialdirection>
      <cd>auto</cd>
      <material type="surface" density="0.067" group="Fabrics">Ripstop nylon</material>
      <deployevent>ejection</deployevent>
      <deployaltitude>200.0</deployaltitude>
      <deploydelay>0.0</deploydelay>
      <striplength>0.5</striplength>
      <stripwidth>0.05</stripwidth>
   </streamer>

<shockcord>
"""""""""""

The ``<shockcord>`` element defines the **Shock Cord** component:

.. code-block:: xml
   :emphasize-lines: 1

    <shockcord>
      <name>Shock Cord</name>
      <id>[UUID]</id>
      <axialoffset method="top">0.044449999999999996</axialoffset>
      <position type="top">0.044449999999999996</position>
      <packedlength>0.019049999999999997</packedlength>
      <packedradius>0.0065405</packedradius>
      <radialposition>0.0</radialposition>
      <radialdirection>0.0</radialdirection>
      <cordlength>0.3047999999999995</cordlength>
      <material type="line" density="0.00297638" group="ThreadsLines">Elastic rubber band (flat 3.2 mm, 1/8 in)</material>
   </shockcord>

<masscomponent>
"""""""""""""""

.. code-block:: xml
   :emphasize-lines: 1

    <masscomponent>
        <name>Unspecified</name>
         <id>[UUID]</id>
        <position type="top">0.11</position>
        <packedlength>0.05</packedlength>
        <packedradius>0.0225</packedradius>
        <radialposition>0.0</radialposition>
        <radialdirection>0.0</radialdirection>
        <mass>0.061</mass>
        <masscomponenttype>masscomponent</masscomponenttype>
    </masscomponent>

----

.. _SimulationData:

Simulation Data
---------------
The ``<simulations>`` section contains flight simulation data. Each simulation is enclosed in a ``<simulation>`` tag:

.. code-block:: xml
   :emphasize-lines: 1, 2, 35

   <simulations>
      <simulation status="loaded">
         <name>Simulation 1</name>
         <simulator>RK4Simulator</simulator>
         <calculator>BarrowmanCalculator</calculator>
         <conditions>
            <configid>[UUID]</configid>
            <launchrodlength>1.0</launchrodlength>
            <launchrodangle>0.0</launchrodangle>
            <launchroddirection>90.0</launchroddirection>
            <windaverage>2.0</windaverage>
            <windturbulence>0.1</windturbulence>
            <wind model="average">
               <speed>2.0</speed>
               <direction>1.5707963267948966</direction>
               <standarddeviation>0.2</standarddeviation>
            </wind>
            <windmodeltype>Average</windmodeltype>
            <launchaltitude>0.0</launchaltitude>
            <launchlatitude>45.0</launchlatitude>
            <launchlongitude>0.0</launchlongitude>
            <geodeticmethod>flat</geodeticmethod>
            <atmosphere model="isa"/>
            <timestep>0.05</timestep>
            <maxtime>1200.0</maxtime>
         </conditions>
         <flightdata maxaltitude="50.605" maxvelocity="29.249" maxacceleration="143.659"
                   maxmach="0.086" timetoapogee="3.443" flighttime="15.89"
                   groundhitvelocity="4.583" launchrodvelocity="15.366"
                   deploymentvelocity="2.634" optimumdelay="2.763">
         <!-- Flight data points -->
         </flightdata>
      </simulation>
      <!-- Other simulations go here -->
      <simulation>
         <!-- Simulation 2 content -->
      </simulation>
   <simulations>

----

.. _PhotoStudioSettings:

PhotoStudio Settings
--------------------

.. code-block:: xml
   :emphasize-lines: 1

   <photostudio>
      <!-- Orientation settings -->
      <roll>value</roll>         <!-- Roll angle -->
      <yaw>value</yaw>          <!-- Yaw angle -->
      <pitch>value</pitch>       <!-- Pitch angle -->
      <advance>value</advance>   <!-- Advance position -->

      <!-- View settings -->
      <viewAlt>value</viewAlt>       <!-- View altitude -->
      <viewAz>value</viewAz>         <!-- View azimuth -->
      <viewDistance>value</viewDistance>  <!-- Camera distance -->
      <fov>value</fov>               <!-- Field of view -->

      <!-- Lighting settings -->
      <lightAlt>value</lightAlt>     <!-- Light altitude -->
      <lightAz>value</lightAz>       <!-- Light azimuth -->
      <sunlight red="R" green="G" blue="B" alpha="A"/>  <!-- Sunlight color -->
      <ambiance>value</ambiance>     <!-- Ambient light intensity -->
      <skyColor red="R" green="G" blue="B" alpha="A"/>  <!-- Sky color -->

      <!-- Effects settings -->
      <motionBlurred>true/false</motionBlurred>     <!-- Motion blur enabled -->
      <flame>true/false</flame>                     <!-- Flame effect enabled -->
      <flameColor red="R" green="G" blue="B" alpha="A"/>  <!-- Flame color -->
      <smoke>true/false</smoke>                     <!-- Smoke effect enabled -->
      <smokeColor red="R" green="G" blue="B" alpha="A"/>  <!-- Smoke color -->
      <sparks>true/false</sparks>                   <!-- Spark effect enabled -->
      <exhaustScale>value</exhaustScale>            <!-- Exhaust size scale -->
      <flameAspectRatio>value</flameAspectRatio>   <!-- Flame shape ratio -->

      <!-- Spark properties -->
      <sparkConcentration>value</sparkConcentration>  <!-- Spark density -->
      <sparkWeight>value</sparkWeight>               <!-- Spark size -->

      <!-- Environment settings -->
      <sky>value</sky>  <!-- Sky rendering mode -->
   </photostudio>

Colors are specified using RGBA values, each in the range 0-255.

----

.. _DocumentPreferences:

Document Preferences
--------------------
The ``<docprefs>`` section contains document-wide settings, including material definitions:

.. code-block:: xml
   :emphasize-lines: 1, 2

    <docprefs>
        <docmaterials>
            <material>BULK|My Custom Material 1|680.0|Custom</material>
            <material>BULK|My Custom Metal|0.0018|Metals</material>
        </docmaterials>
    </docprefs>

----

OpenRocket Component File (.orc)
================================

The OpenRocket component file format is a human-readable XML format that defines standard rocket components and their
specifications. This file format allows rocket components to be stored in a database, making it easier for users to
select pre-defined components rather than entering specifications manually.

File Architecture
-----------------

The OpenRocket component file (:file:`.orc`) is a plain XML file that contains component definitions. When OpenRocket is
built, these files are serialized into a single binary file (:file:`system.ser`) that is included in the OpenRocket jar.

Root Structure
--------------

The following shows the root XML structure of an OpenRocket component file:

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <OpenRocketComponent>
       <Version>0.1</Version>
       <Materials>
           <!-- Material definitions -->
       </Materials>
       <Components>
           <!-- Component definitions -->
       </Components>
   </OpenRocketComponent>

Materials
---------

The ``<Materials>`` section defines materials used within the component database:

.. code-block:: xml

   <Materials>
       <Material UnitsOfMeasure="g/cm3">
           <Name>Material Name</Name>
           <Density>0.0</Density>
           <Type>BULK</Type>
       </Material>
   </Materials>

Material properties:
- ``UnitsOfMeasure``: Density units (supported formats):

   * Bulk density: `g/cm3`, `kg/m3`, `lb/ft3`
   * Areal density: `g/cm2`, `oz/in2`
   * Line density: `g/cm`, `oz/in`

- ``Type``: Material type (`BULK`, `SURFACE`, or `LINE`)

Components
----------

Common Component Attributes
^^^^^^^^^^^^^^^^^^^^^^^^^^^

All components share these common attributes:

.. code-block:: xml

   <Manufacturer>Text</Manufacturer>
   <PartNumber>Text</PartNumber>
   <Description>Text</Description>
   <Material Type="BULK">Material Name</Material>
   <Mass Unit="kg">0.035892</Mass>
   <Finish></Finish>
   <CG></CG>

Component Types
^^^^^^^^^^^^^^^

Body Tube
"""""""""

.. code-block:: xml

   <BodyTube>
       <InsideDiameter Unit="m">0.0657352</InsideDiameter>
       <OutsideDiameter Unit="m">0.06604</OutsideDiameter>
       <Length Unit="m">0.36195</Length>
   </BodyTube>

Nose Cone
"""""""""

.. code-block:: xml

   <NoseCone>
       <Shape>ELLIPSOID</Shape>
       <ShapeParameter>1.0</ShapeParameter>
       <Filled>true</Filled>
       <OutsideDiameter Unit="m">0.06604</OutsideDiameter>
       <BaseDiameter Unit="m">0.06604</BaseDiameter>
       <ShoulderDiameter Unit="m">0.064922</ShoulderDiameter>
       <ShoulderLength Unit="m">0.0381</ShoulderLength>
       <ShoulderThickness Unit="m">0.003175</ShoulderThickness>
       <Length Unit="m">0.1016</Length>
       <Thickness Unit="m">0.003175</Thickness>
   </NoseCone>

Supported nose cone shapes:

* `CONICAL`
* `ELLIPSOID`
* `HAACK`
* `OGIVE`
* `PARABOLIC`
* `POWER`

.. note::

   HAACK, OGIVE, PARABOLIC, and POWER types support a shape parameter that can be set in the UI but cannot be
   specified in the .orc file.

Transition
""""""""""

.. code-block:: xml

   <Transition>
       <Shape>OGIVE</Shape>
       <Filled>true</Filled>
       <ForeOutsideDiameter Unit="m">0.067056</ForeOutsideDiameter>
       <ForeShoulderDiameter Unit="m">0.064973</ForeShoulderDiameter>
       <ForeShoulderLength Unit="m">0.0254</ForeShoulderLength>
       <ForeShoulderThickness Unit="m">0.003175</ForeShoulderThickness>
       <AftOutsideDiameter Unit="m">0.03175</AftOutsideDiameter>
       <AftShoulderDiameter Unit="m">0.0</AftShoulderDiameter>
       <AftShoulderLength Unit="m">0.0</AftShoulderLength>
       <AftShoulderThickness Unit="m">0.003175</AftShoulderThickness>
       <Length Unit="m">0.1524</Length>
       <Thickness Unit="m">0.003175</Thickness>
   </Transition>

Centering Ring
""""""""""""""

.. code-block:: xml

   <CenteringRing>
       <InsideDiameter Unit="m">0.0187452</InsideDiameter>
       <OutsideDiameter Unit="m">0.0240792</OutsideDiameter>
       <Thickness Unit="m">0.003175</Thickness>
       <Length Unit="m">0.00635</Length>
   </CenteringRing>

Bulkhead
""""""""

.. code-block:: xml

   <BulkHead>
       <Filled>true</Filled>
       <OutsideDiameter Unit="m">0.028701999999999995</OutsideDiameter>
       <Length Unit="m">0.038099999999999995</Length>
   </BulkHead>

Engine Block
""""""""""""

.. code-block:: xml

   <EngineBlock>
       <InsideDiameter Unit="m">0.013131799999999999</InsideDiameter>
       <OutsideDiameter Unit="m">0.017983199999999998</OutsideDiameter>
       <Thickness Unit="m">0.003175</Thickness>
       <Length Unit="m">0.003175</Length>
   </EngineBlock>

Launch Lug
""""""""""

.. code-block:: xml

   <LaunchLug>
       <InsideDiameter Unit="m">0.0055626</InsideDiameter>
       <OutsideDiameter Unit="m">0.006096</OutsideDiameter>
       <Length Unit="m">0.0508</Length>
   </LaunchLug>

Parachute
"""""""""

.. code-block:: xml

   <Parachute>
       <Diameter Unit="m">0.6095999999999999</Diameter>
       <DragCoefficient>0.80</DragCoefficient>
       <Sides>8</Sides>
       <LineCount>8</LineCount>
       <LineLength Unit="m">0.7493</LineLength>
   </Parachute>

Tube Coupler
""""""""""""

.. code-block:: xml

   <TubeCoupler>
       <InsideDiameter Unit="m">0.0</InsideDiameter>
       <OutsideDiameter Unit="m">0.017907</OutsideDiameter>
       <Thickness Unit="m">0.003175</Thickness>
       <Length Unit="m">0.019049999999999997</Length>
   </TubeCoupler>

Units of Measure
----------------

OpenRocket supports various units of measure for component specifications:

* Length: `mm`, `cm`, `m`, `in`, `in/64`, `ft`
* Distance: `m`, `km`, `ft`, `yd`, `mi`, `nmi`
* Velocity: `m/s`, `km/h`, `ft/s`, `mph`
* Mass: `g`, `kg`, `oz`, `lb`
* Angle: `deg`, `rad`, `arcmin`
* Force: `N`, `lbf`, `kgf`
* Impulse: `Ns`, `lbf*s`

Important Notes
---------------

1. Material definitions only have scope within the current datafile.
2. When a component is first created in a .ork file, the material definition is copied from the .orc file. Subsequent changes to the material definition in the .orc file will not automatically update existing components in .ork files.
3. To update a component's material properties, you must manually reselect the component preset from the database.
4. The XML schema for .orc files is not formally defined in an XSD file.

