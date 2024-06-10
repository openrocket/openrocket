***********
Preferences
***********

This page describes how you can customize OpenRocket using the Preferences dialog.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Accessing the Preferences Dialog
================================

The **Preferences dialog** can be opened by selecting :guilabel:`Preferences` from the :menuselection:`Edit` menu, or by
pressing :kbd:`Ctrl` + :kbd:`,` (comma) on your keyboard for Windows and Linux, or :kbd:`Cmd` + :kbd:`,` (comma) on your keyboard for macOS.

.. figure:: /img/setup/preferences/Access-Preferences.png
   :alt: Accessing the Preferences dialog from the Edit menu
   :figclass: or-figclass, or-image-border
   :figwidth: 45 %
   :align: center

   Accessing the **Preferences dialog** from the :menuselection:`Edit` menu

Explanation of the Available Preferences
========================================

.. _general_tab:

General
-------

The **General** tab contains general settings for the OpenRocket application.

.. figure:: /img/setup/preferences/Prefs-General.png
   :alt: General tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`General` tab in the Preferences dialog

- :guilabel:`Interface language`: Select the language you want the OpenRocket GUI to be displayed in.

  Currently supported languages:
   - ``System default`` - OpenRocket will use the language set in your operating system.
   - ``English/English`` - This is the main language of OpenRocket. All other languages are translations of the English version.
   - ``čeština/Czech``
   - ``Deutsch/German``
   - ``español/Spanish``
   - ``français/French``
   - ``italiano/Italian``
   - ``Nederlands/Dutch``
   - ``polski/Polish``
   - ``português/Portuguese``
   - ``Türkçe/Turkish``
   - ``русский/Russian``
   - ``українська/Ukrainian``
   - ``Arabic/العربية``
   - ``中文/Chinese``
   - ``日本語/Japanese``

   .. note::
      You must restart OpenRocket for the language change to take effect.

   .. warning::
      Many translations are not complete or up-to-date. Missing translations texts are displayed in English.

      If you find any errors or missing translations, please let us know by creating an issue on the
      `OpenRocket GitHub page <https://github.com/openrocket/openrocket/issues>`__
      or by :doc:`Contributing to the OpenRocket translations </dev_guide/contributing_to_translations>`.

- :guilabel:`UI Theme`: Select the theme you want the OpenRocket GUI to be displayed in.

  Currently supported themes:
   - ``Auto (detect)`` - OpenRocket will use the theme set in your operating system.
   - ``Light (default)`` - A light theme (the "original" OpenRocket theme).

     .. figure:: /img/setup/preferences/Theme-Light.png
        :alt: Light OpenRocket UI theme.
        :figclass: or-figclass, or-image-border
        :figwidth: 55 %
        :align: center

        Light OpenRocket UI theme.

   - ``Dark`` - A dark theme.

     .. figure:: /img/setup/preferences/Theme-Dark.png
        :alt: Dark OpenRocket UI theme.
        :figclass: or-figclass, or-image-border
        :figwidth: 55 %
        :align: center

        Dark OpenRocket UI theme.

   - ``Dark, high-contrast`` - A dark theme, but with more contrast than the standard dark theme.

     .. figure:: /img/setup/preferences/Theme-DarkContrast.png
        :alt: Dark Contrast OpenRocket UI theme.
        :figclass: or-figclass, or-image-border
        :figwidth: 55 %
        :align: center

        Dark Contrast OpenRocket UI theme.

  .. note::
     You must restart OpenRocket for the UI theme change to take effect.

- :guilabel:`UI Font Size`: Select the font size you want the OpenRocket GUI to be displayed in. The default is 13.
  The smaller the number, the smaller the font size.

  .. figure:: /img/setup/preferences/FontSize13.png
    :alt: Font size 13 (default).
    :figclass: or-figclass, or-image-border
    :figwidth: 55 %
    :align: center

    Font size 13 (default).

  .. figure:: /img/setup/preferences/FontSize9.png
    :alt: Font size 9 (default).
    :figclass: or-figclass, or-image-border
    :figwidth: 55 %
    :align: center

    Font size 9.

  .. figure:: /img/setup/preferences/FontSize17.png
    :alt: Font size 17 (default).
    :figclass: or-figclass, or-image-border
    :figwidth: 55 %
    :align: center

    Font size 17.

  .. note::
     You must restart OpenRocket for the font size change to take effect.

- :guilabel:`User-defined thrust curves`: You can specify a directory where you have stored your own thrust curves.
  OpenRocket will scan this directory for thrust curves when you open the program. OpenRocket comes pre-installed with
  thrust curves from the `ThrustCurve.org <https://www.thrustcurve.org/>`__ database, but you can add your own using the
  user-defined thrust curves. For more information, see the :doc:`Thrust Curves </user_guide/thrust_curves>` section.

  You can add multiple directories if you separate the paths with a semicolon (;).

  - :guilabel:`Add`: Add a directory where you have stored your own thrust curves.
  - :guilabel:`Reset`: Reset the directories to the default directories.

- :guilabel:`Always check for software updates at startup`: If this option is enabled, OpenRocket will check for software updates
  every time you start the program. If an update is available, you will be notified.

  - :guilabel:`Check now`: Check for software updates now.
  - :guilabel:`Also check for pre-releases`: If this option is enabled, OpenRocket will also check for pre-releases (so you
    can test new features before they are officially released and help us fix last-minute mistakes).

- :guilabel:`Open last design file on startup`: If this option is enabled, OpenRocket will open the last design file you were working on
  when you start the program.

- :guilabel:`Show warning when saving in RASAero format`: If this option is enabled, OpenRocket will show a warning when you export a design
  in RASAero format. This is because RASAero does not support all the features of OpenRocket, so some information may be lost when you save
  in RASAero format.

- :guilabel:`Show warning when saving in RockSim format`: If this option is enabled, OpenRocket will show a warning when you export a design
  in RockSim format. This is because RockSim does not support all the features of OpenRocket, so some information may be lost when you save
  in RockSim format.

- :guilabel:`Show confirmation dialog when discarding preferences`: If this option is enabled, OpenRocket will show a confirmation dialog
  when you discard changes to the preferences.

:guilabel:`Import preferences`, :guilabel:`Export preferences`, and :guilabel:`Reset all preferences` are explained in
:ref:`Importing and Exporting Preferences <importing_exporting_prefs>` and :ref:`Resetting Preferences <resetting_prefs>`.

Design
------

The **Design** tab contains settings for the design of the rocket.

.. figure:: /img/setup/preferences/Prefs-Design.png
   :alt: Design tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Design` tab in the Preferences dialog

- :guilabel:`Position to insert new body components`: Where to insert new body components in the rocket design view.

  - :guilabel:`Always ask`: OpenRocket will ask you where you want to insert new body components every time you add a new component.
  - :guilabel:`Insert in middle`: Insert new body components after the currently selected component.
  - :guilabel:`Add to end`: Insert new body components at the end of the parent component.

- :guilabel:`Position to insert new stages`: Where to insert new stages in the rocket design view.

  - :guilabel:`Always ask`: OpenRocket will ask you where you want to insert new stages every time you add a new stage.
  - :guilabel:`Insert in middle`: Insert new stages after the currently selected stage.
  - :guilabel:`Add to end`: Insert new stages at the end of the rocket.

- :guilabel:`Size of text in rocket design panel`: The size of the text in the rocket design view.
- :guilabel:`Default Mach Number for C.P. Estimate`: Mach value that is used for the Center of Pressure (CP) calculations
  in the rocket design view.
- :guilabel:`Always open leftmost tab when opening a component edit dialog`: If enabled, if you edit a rocket component and
  open the component configuration, it will always open in the leftmost tab. If disabled, the last tab you used will be opened.
- :guilabel:`Show confirmation dialog for discarding component changes`: If enabled, OpenRocket will show a confirmation
  dialog when you discard changes to a component (if you click the :guilabel:`Cancel` button in the component configuration window).
- :guilabel:`Show confirmation dialog for discarding simulation changes`: If enabled, OpenRocket will show a confirmation
  dialog when you discard changes to a simulation (if you click the :guilabel:`Cancel` button in the simulation configuration window).
- :guilabel:`Update estimated flight parameters in design window`: If enabled, OpenRocket will calculate and update the estimated
  flight parameters in the rocket design view (in the top-left) when you make changes to the rocket design.
- :guilabel:`Only show pod set/booster markers when the pod set/booster is selected`: If enabled, OpenRocket will only show the
  pod set/booster instance markers in the rocket design view when the pod set/booster is selected. The instance markers show
  where pod/booster instances are located in the rocket design.

  .. figure:: /img/setup/preferences/PodAndBoosterMarker.png
     :alt: Booster and Pod instance markers on a body tube in the rocket design view.
     :figclass: or-figclass, or-image-border
     :figwidth: 55 %
     :align: center

     Booster and Pod instance markers on a body tube in the rocket design view.

Simulation
----------

.. figure:: /img/setup/preferences/Prefs-Simulation.png
   :alt: Simulation tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Simulation` tab in the Preferences dialog

- :guilabel:`Confirm deletion of simulations`: If enabled, OpenRocket will show a confirmation dialog when you delete a simulation.
- :guilabel:`Run out-dated simulations when you open the simulation tab`: If enabled, OpenRocket will run simulations that are out-dated
  when you switch the simulation tab (in the task tabs).
- :guilabel:`Geodetic calculations`: Which calculation method to use for coordinates on the Earth.
- :guilabel:`Time step`: The smallest time step to use in the simulations. A smaller time step will give more accurate results but
  will take longer to compute.

  .. note::
     OpenRocket uses optimized time steps. It will use a larger value than the set time step for parts in the simulation
     that do not require a smaller time step.

     In other words, the simulation time step is not fixed, but will vary throughout the simulation.

- :guilabel:`Reset to default`: Reset the simulator options to the default values.

.. attention::
   The settings in the Launch tab have **no effect on existing simulations in your design**.

   Only simulations that you create after changing these settings will be affected.

Launch
------

.. figure:: /img/setup/preferences/Prefs-Launch.png
   :alt: Launch tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Launch` tab in the Preferences dialog

.. hlist::
   :columns: 2

   - Wind
      - :guilabel:`Average windspeed`: The average wind speed relative to the ground.
      - :guilabel:`Standard deviation`: Standard deviation of the wind speed (= a measure of the dispersion of the wind speed values).
        The actual wind speed is within twice the standard deviation 95% of the time.
      - :guilabel:`Turbulence intensity`: The standard deviation of the wind speed divided by the average wind speed. Typical
        values range from 5% to 20%.
      - :guilabel:`Wind direction`: The direction the wind is coming from. 0° is north, 90° is east, 180° is south, and 270° is west.
   - Atmospheric conditions
      - :guilabel:`Use International Standard Atmosphere`: If enabled, the atmospheric conditions will be set to the
        International Standard Atmosphere (ISA). This model has a temperature of 15 °C and a pressure of 1013.25 mbar at
        sea level.

        If disabled, you can set the temperature and pressure manually.
      - :guilabel:`Temperature`: The temperature at the launch site.
      - :guilabel:`Pressure`: The pressure at the launch site.
   - Launch site
      - :guilabel:`Latitude`: The latitude coordinate of the launch site.
      - :guilabel:`Longitude`: The longitude coordinate of the launch site.
      - :guilabel:`Altitude`: The altitude of the launch site.
   - Launch rod
      - :guilabel:`Length`: The length of the launch rod.
      - :guilabel:`Always launch directly up-wind or down-wind`: If enabled, the launch rod will always point into the wind.
      - :guilabel:`Angle`: The angle of the launch rod relative to the ground. At 0°, the launch rod points straight up (vertical).
        If the checkbox to "Always launch directly up-wind or down-wind" is enabled, positive angles point up-wind, and negative angles
        point down-wind. If the checkbox is disabled, positive angles towards the direction axis. E.g. if direction is set
        to 90° (East of the wind), positive angles will point the launch rod East. Negative angles will point the rod West.
      - :guilabel:`Direction`: Direction of the launch rod relative to the wind. 0° is pointing in the wind direction.
        90° is pointing East of the wind.

.. attention::
   The settings in the Launch tab have **no effect on existing simulations in your design**.

   Only simulations that you create after changing these settings will be affected.

Units
-----

The **Units** tab allows you to set the units that OpenRocket uses throughout the program.

.. figure:: /img/setup/preferences/Prefs-Units.png
   :alt: Units tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Units` tab in the Preferences dialog

.. hlist::
   :columns: 2

   - :guilabel:`Rocket dimensions`: Unit for dimensions of rocket components (e.g. diameter, length).
   - :guilabel:`Motor dimensions`: Unit for dimensions of rocket motors (diameter, length).
   - :guilabel:`Distance`: Unit for distances (e.g. altitude).
   - :guilabel:`Velocity`: Unit for velocities.
   - :guilabel:`Acceleration`: Unit for accelerations.
   - :guilabel:`Mass`: Unit for masses.
   - :guilabel:`Force`: Unit for forces.
   - :guilabel:`Total impulse`: Unit for total impulse.
   - :guilabel:`Moment of inertia`: Unit for moments of inertia.
   - :guilabel:`Stability`: Primary unit for stability margin/static margin.

     Possible values:

     - ``mm``, ``cm``, ``m``, ``in``: Distance between the center of gravity (CG) and the center of pressure (CP).
     - ``cal``: Caliber. 1 caliber = 1 diameter of the rocket.
     - ``%``: Percentage of the rocket length.

   - :guilabel:`Secondary stability`: Secondary unit for stability margin/static margin.

     Same values as the primary stability unit.

   - :guilabel:`Display secondary stability unit`: If enabled, display both the primary and secondary stability units in the rocket design view.
   - :guilabel:`Line density`: Unit for line density (= one-dimensional density).
   - :guilabel:`Surface density`: Unit for surface density (= two-dimensional density).
   - :guilabel:`Bulk density`: Unit for bulk density (= three-dimensional density).
   - :guilabel:`Surface roughness`: Unit for surface roughness.
   - :guilabel:`Area`: Unit for areas.
   - :guilabel:`Angle`: Unit for angles.
   - :guilabel:`Roll rate`: Unit for roll rates.
   - :guilabel:`Temperature`: Unit for temperatures.
   - :guilabel:`Pressure`: Unit for pressures.
   - :guilabel:`Wind speed`: Unit for wind speeds.
   - :guilabel:`Latitude`: Unit for latitudes.
   - :guilabel:`Longitude`: Unit for longitudes.

- :guilabel:`Default metric`: Set the default unit system to metric units.
- :guilabel:`Default imperial`: Set the default unit system to imperial units.

Materials
---------

The **Materials** tab shows a list of materials that are pre-installed in OpenRocket, plus custom user-defined materials that
you have added.

.. figure:: /img/setup/preferences/Prefs-Materials.png
   :alt: Materials tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Materials` tab in the Preferences dialog

On the left are a list of all the materials in OpenRocket. Pre-installed OpenRocket materials are displayed in greyed-out text.
User-defined materials are displayed in normal text.

- :guilabel:`New`: Add a new custom material.
- :guilabel:`Edit`: Edit the selected material. You can edit both custom materials and pre-installed OpenRocket materials.
  However, editing a pre-installed material will create a new custom material with the same name, instead of modifying the
  original material.
- :guilabel:`Delete`: Delete the selected material. You can only delete custom materials.
- :guilabel:`Revert all`: Delete all user-defined materials.

.. note::
   Editing materials will not affect existing rocket designs.


Graphics
--------

The **Graphics** tab allows you to set the graphics settings for OpenRocket.

.. figure:: /img/setup/preferences/Prefs-Graphics.png
   :alt: Graphics tab in the Preferences dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 65 %
   :align: center

   :guilabel:`Graphics` tab in the Preferences dialog

- **Graphics Editor**: Select which graphics editor you want to use to edit textures in OpenRocket (if you click the
  :guilabel:`Edit` button next to the Texture dropdown in the :guilabel:`Appearance` tab of the component configuration window).

  - :guilabel:`Show Prompt`: OpenRocket will ask you which graphics editor you want to use every time you edit a texture.
  - :guilabel:`Use Default Editor`: OpenRocket will use the default graphics editor you have set in your operating system.
  - :guilabel:`Command Line`: You can set the command line for the graphics editor you want to use. This is useful if you have
    multiple graphics editors installed and want to use a specific one. Enter the file path of the graphics editor executable
    and any command line arguments you want to use in the text input field, or click the :guilabel:`Select Graphics Editor Program`
    button to select the executable file.
- **3D Graphics**

  - :guilabel:`Enable 3D Graphics`: If enabled, 3D rendering is supported in OpenRocket. If disabled, you can not use any
    3D features inside OpenRocket.
  - :guilabel:`Enable Anti-aliasing`: If enabled, OpenRocket will use anti-aliasing to smooth the edges of 3D graphics.
  - :guilabel:`Use Off-screen Rendering`: If enabled, OpenRocket will render 3D graphics off-screen. This can improve performance
    on some systems, but may cause issues on others.

    .. tip::
       If you experience issues with 3D graphics, try toggling this option (enable it if is was disabled, or vice versa).

  .. note::
     The effects will take place the next time you open a window.

.. _resetting_prefs:

Resetting Preferences
=====================

To reset all preferences to their default values, click the :guilabel:`Reset all preferences` button at the bottom of the
:ref:`General tab <general_tab>`.

.. _importing_exporting_prefs:

Importing and Exporting Preferences
===================================

You can export the current preferences to an XML file, or import preferences from an XML file. This can be useful for
instance if you need to teach other people how to use OpenRocket and want them to have the same settings as you.

Export Preferences
------------------

To export preferences, click the :guilabel:`Export preferences` button at the bottom of the :ref:`General tab <general_tab>`.
This will open a file dialog where you can select where to save the preferences file:

.. figure:: /img/setup/preferences/ExportPreferences.png
   :alt: Exporting preferences to an XML file
   :figclass: or-figclass, or-image-border
   :figwidth: 55 %
   :align: center

   Exporting preferences to an XML file

There are two options in the file dialog:

- :guilabel:`Export user directories` If enabled, any user directories that are set in the preferences will be exported as well.
  If you import the preferences on another computer, the user directories will be set to the same directories as on the original computer.

  You can disable this option if the target computer has different directories, or if you don't want to share the information
  about your directories.
- :guilabel:`Export window information (position, size...)`: If enabled, cached window information (position, size, etc.) will be exported.
  If you import the preferences on another computer, the windows will be opened in the same position and size as on the original computer.

  You can disable this option if you want the windows to be opened in the default position and size.

Here is an example of the exported preferences XML file:

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8" standalone="no"?><!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
   <preferences EXTERNAL_XML_VERSION="1.0">
     <root type="user">
       <map/>
       <node name="OpenRocket">
         <map>
           <entry key="Tube Fin SetAlwaysOpenPreset" value="false"/>
           <entry key="LaunchRodDirection" value="1.5707963267948966"/>
           <entry key="ExportDecimalPlaces" value="3"/>
           <entry key="LaunchRodAngle" value="0.0"/>
           <entry key="WindTurbulence" value="0.06366197723675814"/>
           <entry key="UIFontSize" value="13"/>
           ...

Import Preferences
------------------

To import preferences, click the :guilabel:`Import preferences` button at the bottom of the :ref:`General tab <general_tab>`
and select the preferences XML file you want to import.
