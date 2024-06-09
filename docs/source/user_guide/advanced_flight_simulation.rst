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

To begin learning about OpenRocket's plotting features, first, click the **Plot / Export** button on the **Flight simulations** window.

.. figure:: /img/user_guide/advanced_flight_simulation/PlotExportButton.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Plot / export Button.

On the **Edit simulation** panel, you'll see tabs marked **Plot data** and **Export data**.

Plotting data
-------------

The **Plot data** tab opens first. Here you can define many parameters that will determine what values are plotted, and
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

From the **Plot data** window, you can click the **<< Edit** button to configure **Launch conditions**, and
**Simulation options** before you plot.

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

In the **Simulation options** tab, the **Simulator options** let you choose the shape of the simulated Earth in your
calculations (*doing so* **does not** *affect the Earth background in Photo Studio*), and you can choose the time-resolution
of the simulation. This is also the place where you add and set up **Simulation extensions**, which are beyond this
guide's purpose.

.. figure:: /img/user_guide/advanced_flight_simulation/EditSimulationSimOpts.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Edit simulation window: Simulation options.

   The Simulation options in the simulation configuration window

----

Exporting Data
==============

Located on the **Plot / export panel**, the **Export Data tab** (shown below) helps you set up a Comma-Separated Value (.csv)
formatted file to export data from your simulations. You can export any or all of over 50 values (generally speaking,
the list of parameters above, plus **Coriolis acceleration**). Optional **Comments** sections list any flight events
(**Apogee**, for example) you selected for your simulation, as well as description and field descriptions.

You can choose separators other than comma, if you prefer semicolon, space, or TAB-delimited data. Once you have your
data choices set up, clicking the :guilabel:`Export` button brings up a file dialog to choose a filename and location for your
exported data.

.. figure:: /img/user_guide/advanced_flight_simulation/ExportData.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: The Export data window.

   The Export data window.

