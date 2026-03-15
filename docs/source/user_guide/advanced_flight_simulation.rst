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

