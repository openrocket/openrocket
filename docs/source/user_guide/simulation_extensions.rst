*********************
Simulation Extensions
*********************

By using OpenRocket's extension and listener mechanism, it's possible to modify the program itself to add features that 
are not supported by the program as distributed; some extensions that have been created already provide the ability to 
air-start a rocket, to add active roll control, and to calculate and save extra flight data.

This page will discuss extensions and simulations. We'll start by showing how a simulation is executed 
(so you can get a taste of what's possible), and then document the process of creating the extension. 

.. warning::

   Writing an extension inserts new code into the program. It is entirely possible to disrupt a simulation in a way that 
   invalidates simulation results, or can even crash the program. Be careful!
   
.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Adding an Existing Extension to a Simulation
============================================

Extensions are added to a simulation through a menu in the "Simulation Options" tab.

1. Open a .ork file and go to the **Flight Simulations** tab.
2. Click the **Edit simulation** button to open the **Edit simulation** dialog.
3. Go to the **Simulation options** tab.
4. Click the **Add extension** button.

This will open a menu similar to the one in the following screenshot:

.. figure:: /img/user_guide/simulation_extensions/Extension-menu.png
   :align: center
   :width: 35%
   :figclass: or-image-border
   :alt: Extension menu

   Extension menu.

Clicking on the name of an extension will add it to the simulation; if it has a configuration dialog the dialog will be opened:

.. figure:: /img/user_guide/simulation_extensions/Air-start-configuration.png
   :align: center
   :width: 45%
   :figclass: or-image-border
   :alt: Air-start configuration dialog

   Air-start configuration dialog.

In the case of the air-start extension, the configuration dialog allows you to set the altitude and velocity at which
your simulation will begin. After you close the configuration dialog (if any), a new panel will be added to the
**Simulation options** pane, showing the new extension with buttons to reconfigure it, obtain information about it, or
remove it from the simulation:

.. figure:: /img/user_guide/simulation_extensions/Air-start-pane.png
   :align: center
   :width: 35%
   :figclass: or-image-border
   :alt: Air-start extension pane

   Air-start extension pane.

----

Creating a New OpenRocket Extension
===================================

The remainder of this page will describe how a new simulation extension is created.

Preliminary Concepts
--------------------

Before we can discuss writing an extension, we need to briefly discuss some of the internals of OpenRocket. In particular,
we need to talk about the simulation status, flight data, and simulation listeners.

Simulation Status
~~~~~~~~~~~~~~~~~

As a simulation proceeds, it maintains its state in a
`SimulationStatus` object. This object contains
information about the rocket's current position, orientation,
velocity, simulation state, and the simulation's event queue. It also contains a
reference to a copy of the rocket design and its configuration. Any
simulation listener method (see below) may modify the state of
the rocket by changing the properties of the `SimulationStatus` object.

You can obtain current information regarding the state of the simulation by calling `get*()` methods. For instance, the
rocket's current position is returned by calling `getRocketPosition()`; the rocket's position can be changed by calling
`setRocketPosition(Coordinate position)`. All of the `get*()` and `set*()` methods can be found in
:file:`core/src/main/java/info/openrocket/core/simulation/SimulationStatus.java`. Note that while some information can be obtained in
this way, it is not as complete as that found in `FlightData` and `FlightDataBranch` objects.

Flight Data
~~~~~~~~~~~

OpenRocket refers to simulation variables as `FlightDataType`s, which are `List<Double>` objects with one list for each
simulation variable and one element in the list for each time step. To obtain a `FlightDataType`, for example the current
motor mass, from `flightData`, we call `flightData.get(FlightDataType.TYPE_MOTOR_MASS)`. The standard `FlightDataType`
lists are all created in `core/src/main/java/info/openrocket/core/simulation/FlightDataType.java`; the mechanism for creating a new
`FlightDataType` if needed for your extension will be described later.

Data from the current simulation step can be obtained with e.g. `flightData.getLast(FlightDataType.TYPE_MOTOR_MASS)`.

The simulation data for each stage of the rocket's flight is referred to as a `FlightDataBranch`. Every simulation has 
at least one `FlightDataBranch` for its sustainer, and will have additional branches for its boosters.

Finally, the collection of all of the `FlightDataBranch` es and some summary data for the simulation is stored in a 
`FlightData` object.

Flight Conditions
~~~~~~~~~~~~~~~~~

Current data regarding the aerodynamics of the flight itself are stored in a ``FlightConditions`` object. This includes 
things like the velocity, angle of attack, and roll and pitch angle and rates. It also contains a reference to the 
current ``AtmosphericConditions``.

Simulation Listeners
~~~~~~~~~~~~~~~~~~~~

Simulation listeners are methods that OpenRocket calls at specified points in the computation to either record 
information or modify the simulation state. These are divided into three interface classes, named ``SimulationListener``, 
``SimulationComputationListener``, and ``SimulationEventListener``.

All of these interfaces are implemented by the abstract class ``AbstractSimulationListener``. This class provides empty 
methods for all of the methods defined in the three interfaces, which are overridden as needed when writing a listener. 
A typical listener method (which is actually in the Air-start listener), would be:

.. code-block:: java

   public void startSimulation(SimulationStatus status) throws SimulationException {
       status.setRocketPosition(new Coordinate(0, 0, getLaunchAltitude()));
       status.setRocketVelocity(status.getRocketOrientationQuaternion().rotate(new Coordinate(0, 0, getLaunchVelocity())));
   }

This method is called when the simulation is first started. It obtains the desired launch altitude and velocity from its 
configuration, and inserts them into the simulation status to simulate an air-start.

The full set of listener methods, with documentation regarding when they are called, can be found in 
:file:`core/src/main/java/info/openrocket/core/simulation/listeners/AbstractSimulationListener.java`.

The listener methods can have three return value types:

* The ``startSimulation()``, ``endSimulation()``, and ``postStep()`` are called at a specific point of the simulation. They are 
  void methods and do not return any value.
* The ``preStep()`` and event-related hook methods return a boolean value indicating whether the associated action should 
  be taken or not. A return value of ``true`` indicates that the action should be taken as normally would be (default), 
  ``false`` will inhibit the action.
* The pre- and post-computation methods may return the computed value, either as an object or a double value. The 
  pre-computation methods allow pre-empting the entire computation, while the post-computation methods allow augmenting 
  the computed values. These methods may return ``null`` or ``Double.NaN`` to use the original values (default), or return 
  an overriding value.

Every listener receives a ``SimulationStatus`` (see above) object as the first argument, and may also have additional arguments.

Each listener method may also throw a ``SimulationException``. This is
considered an error during simulation (not a program bug),
and an error dialog is displayed to the user with the exception message. The simulation data produced thus far is not 
stored in the simulation. Throwing a ``RuntimeException`` is considered a bug in the software and will result in a bug report dialog.

If a simulation listener wants to stop a simulation prematurely without an error condition, it needs to add a flight 
event of type ``FlightEvent.SIMULATION_END`` to the simulation event queue:

.. code-block:: java

   status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime(), null));

This will cause the simulation to be terminated normally.

Creating a New Simulation Extension
-----------------------------------

Creating an extension for OpenRocket requires writing three classes:

* A listener, which extends ``AbstractSimulationListener``. This will be the bulk of your extension, and performs all the real work.
* An extension, which extends ``AbstractSimulationExtension``. This inserts your listener into the simulation. Your listener 
  can (and ordinarily will) be private to your extension.
* A provider, which extends ``AbstractSimulationExtensionProvider``. This puts your extension into the menu described above.

In addition, if your extension will have a configuration GUI, you will need to write:

* A configurator, which extends ``AbstractSwingSimulationExtensionConfigurator<E>``

You can either create your extension outside the source tree and make sure it is in a directory that is in your Java 
classpath when OpenRocket is executed, or you can insert it in the source tree and compile it along with OpenRocket. 
Since all of OpenRocket's code is freely available, and reading the code for the existing extensions will be very helpful 
in writing your own, the easiest approach is to simply insert it in the source tree. If you select this option, a very 
logical place to put your extension is in :file:`core/src/main/java/info/openrocket/core/simulation/extension/`

The extension examples provided with OpenRocket are located in a
subdirectory of this named :file:`example/`.

Your configurator, if any, will logically go in :file:`swing/src/main/java/info/openrocket/swing/simulation/extension/`

Configurators for the example extensions are located in a subdirectory
of this named :file:`example/`.

Extension Example
-----------------

To make things concrete, we'll start by creating a simple example extension, to air-start a rocket from a hard-coded altitude. 
Later, we'll add a configurator to the extension so we can set the launch altitude through a GUI at runtime. This is a 
simplified version of the ``AirStart`` extension included in the extension
``example`` directory in the OpenRocket source code tree (that extension also sets a 
start velocity).

.. code-block:: java
   :linenos:

    package info.openrocket.core.simulation.extension;
    
    import info.openrocket.core.simulation.SimulationConditions;
    import info.openrocket.core.simulation.SimulationStatus;
    import info.openrocket.core.simulation.exception.SimulationException;
    import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
    import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
    import info.openrocket.core.util.Coordinate;
    
    /**
     * Simulation extension that launches a rocket from a specific altitude.
     */
    public class AirStartExample extends AbstractSimulationExtension {
    
        public void initialize(SimulationConditions conditions) throws SimulationException {
            conditions.getSimulationListenerList().add(new AirStartListener());
        }
    
        @Override
        public String getName() {
            return "Air-Start Example";
        }
    
        @Override
        public String getDescription() {
            return "Simple extension example for air-start";
        }
    
        private class AirStartListener extends AbstractSimulationListener {
    
            @Override
            public void startSimulation(SimulationStatus status) throws SimulationException {
                status.setRocketPosition(new Coordinate(0, 0, 1000.0));
            }
        }
    }

There are several important features in this example:

* The ``initialize()`` method in lines 15-17, which adds the listener to the simulation. This is the 
  only method that is actually required to be defined in your
  extension, though any real extension (including this example) will
  almost certainly have more.
* The ``getName()`` method in lines 19-22, which provides the extension's name. A default ``getName()`` is provided by 
  ``AbstractSimulationExtension``, which simply uses the classname (so for this example, ``getName()`` would have returned 
  ``"AirStartExample"`` if this method hadn't overridden it).
* The ``getDescription()`` method in lines 24-27, which provides a brief description of the purpose of the extension. 
  This is the method that provides the text for the :guilabel:`Info` button dialog shown in the first section of this page.
* The listener itself in lines 29-35, which provides a single ``startSimulation()`` method. When the simulation starts 
  executing, this listener is called, and the rocket is set to an altitude of 1000 meters.

This will create the extension when it's compiled, but it won't put it in the simulation extension menu. To be able to 
actually use it, we need a provider, like this:

.. code-block:: java
   :linenos:

    import info.openrocket.core.plugin.Plugin;
    import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;
    
    @Plugin
    public class AirStartExampleProvider extends AbstractSimulationExtensionProvider {
        public AirStartExampleProvider() {
            super(AirStartExample.class, "Launch conditions", "Air-start example");
        }
    }

This class adds your extension to the extension menu with the
``super`` call in line 7. The first parameter (``"Launch Conditions"``) is the first menu level, 
while the second (``"Air-start example"``) is the actual menu entry. These strings can be anything you want; using a 
first level entry that didn't previously exist will add it to the first level menu.

Try it! Putting the extension in a file named :file:`core/src/main/java/info/openrocket/core/simulation/extension/AirStartExample.java`
and the provider in
:file:`core/src/main/java/info/openrocket/core/simulation/extension/AirStartExampleProvider.java`,
and compiling, and running OpenRocket will give you a new entry in the extensions menu; adding it to the simulation will cause your simulation to 
start at an altitude of 1000 meters.

Adding a Configurator
---------------------

To be able to configure the extension at runtime, we need to write a configurator and provide it with a way to 
communicate with the extension. First, we'll modify the extension as follows:

.. code-block:: java
   :linenos:

    package info.openrocket.core.simulation.extension;
    
    import info.openrocket.core.simulation.SimulationConditions;
    import info.openrocket.core.simulation.SimulationStatus;
    import info.openrocket.core.simulation.exception.SimulationException;
    import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
    import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
    import info.openrocket.core.util.Coordinate;
    
    /**
     * Simulation extension that launches a rocket from a specific altitude.
     */
    public class AirStartExample extends AbstractSimulationExtension {
    
        public void initialize(SimulationConditions conditions) throws SimulationException {
            conditions.getSimulationListenerList().add(new AirStartListener());
        }
    
        @Override
        public String getName() {
            return "Air-Start Example";
        }
    
        @Override
        public String getDescription() {
            return "Simple extension example for air-start";
        }

       public double getLaunchAltitude() {
           return config.getDouble("launchAltitude", 1000.0);
       }

       public void setLaunchAltitude(double launchAltitude) {
           config.put("launchAltitude", launchAltitude);
           fireChangeEvent();
       }
    
        private class AirStartListener extends AbstractSimulationListener {
    
            @Override
            public void startSimulation(SimulationStatus status) throws SimulationException {
                status.setRocketPosition(new Coordinate(0, 0, getLaunchAltitude()));
            }
        }
    }

This adds two methods to the extension (``getLaunchAltitude()`` and ``setLaunchAltitude()``), and calls ``getLaunchAltitude()`` 
from within the listener to obtain the configured launch altitude. ``config`` is a ``Config`` object, provided by 
``AbstractSimulationExtension`` (so it isn't necessary to call a constructor yourself). 
:file:`core/src/main/java/info/openrocket/core/util/Config.java` includes methods to interact with a configurator, allowing the 
extension to obtain ``double``, ``string``, and other configuration values.

In this case, we'll only be defining a single configuration field in our configurator, ``"launchAltitude"``.

The ``getLaunchAltitude()`` method obtains the air-start altitude for the simulation from the configuration, and sets a 
default air-start altitude of 1000 meters. Our ``startSimulation()`` method has been modified to make use of this to obtain 
the user's requested air-start altitude from the configurator, in place of the original hard-coded value.

The ``setLaunchAltitude()`` method places a new launch altitude in the configuration. While our extension doesn't make 
use of this method directly, it will be necessary for our configurator. The call to ``fireChangeEvent()`` in this method 
assures that the changes we make to the air-start altitude are propagated throughout the simulation.

The configurator itself looks like this:

.. code-block:: java
   :linenos:

   package info.openrocket.swing.simulation.extension;

   import javax.swing.JComponent;
   import javax.swing.JLabel;
   import javax.swing.JPanel;
   import javax.swing.JSpinner;

   import info.openrocket.core.document.Simulation;
   import info.openrocket.core.simulation.extension.AirStartExample;
   import info.openrocket.swing.gui.SpinnerEditor;
   import info.openrocket.swing.gui.adaptors.DoubleModel;
   import info.openrocket.swing.gui.components.BasicSlider;
   import info.openrocket.swing.gui.components.UnitSelector;
   import info.openrocket.core.plugin.Plugin;
   import info.openrocket.swing.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
   import info.openrocket.core.unit.UnitGroup;

   @Plugin
   public class AirStartExampleConfigurator extends AbstractSwingSimulationExtensionConfigurator<AirStartExample> {

       public AirStartExampleConfigurator() {
           super(AirStartExample.class);
       }

       @Override
       protected JComponent getConfigurationComponent(AirStartExample extension, Simulation simulation, JPanel panel) {
           panel.add(new JLabel("Launch altitude:"));

           DoubleModel m = new DoubleModel(extension, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0);

           JSpinner spin = new JSpinner(m.getSpinnerModel());
           spin.setEditor(new SpinnerEditor(spin));
           panel.add(spin, "w 65lp!");

           UnitSelector unit = new UnitSelector(m);
           panel.add(unit, "w 25");

           BasicSlider slider = new BasicSlider(m.getSliderModel(0, 5000));
           panel.add(slider, "w 75lp, wrap");

           return panel;
       }
   }

After some boilerplate, this class creates a new ``DoubleModel`` to
manage the air-start altitude (line 29). The most important things 
to notice about the ``DoubleModel`` constructor are the parameters ``"LaunchAltitude"`` and ``UnitGroup.UNITS_DISTANCE``.

* ``"LaunchAltitude"`` is used by the system to synthesize calls to the ``getLaunchAltitude()`` and ``setLaunchAltitude()`` 
  methods defined in ``AirStartExample`` above. The name of the ``DoubleModel``, ``"LaunchAltitude"``, **MUST** match the names of the corresponding 
  ``set`` and ``get`` methods exactly. If they don't, there will be an exception at runtime when the user attempts to change the value.
* ``UnitGroup.UNITS_DISTANCE`` specifies the unit group to be used by this ``DoubleModel``. OpenRocket uses SI (MKS) units internally,
  but allows users to select the units they wish to use for their interface. Specifying a ``UnitGroup`` provides the conversions
  and unit displays for the interface. The available ``UnitGroup`` options are defined in :file:`core/src/main/java/info/openrocket/core/unit/UnitGroup.java`

The remaining code in this method creates a ``JSpinner``, a ``UnitSelector``, and a ``BasicSlider`` all referring to this ``DoubleModel``. 
When the resulting configurator is displayed, it looks like this:

.. figure:: /img/user_guide/simulation_extensions/Example_Configurator.png
   :align: center
   :width: 45%
   :figclass: or-image-border
   :alt: Example configurator

   Example configurator.

The surrounding Dialog window and the **Close** button are provided by the system.

----

Example User Extensions Provided With OpenRocket
================================================

Several examples of user extensions are provided in the OpenRocket source tree. As mentioned previously, the extensions
are all located in :file:`core/src/main/java/info/openrocket/core/simulation/extension/example/` and their configurators are all located
in :file:`swing/src/main/java/info/openrocket/swing/simulation/extension/example/`. Also recall that every extension has a corresponding
provider.

.. list-table::
   :header-rows: 1

   * - Purpose
     - Extension
     - Configurator
   * - Set air-start altitude and velocity
     - `AirStart.java`
     - `AirStartConfigurator.java`
   * - Save some simulation values as a CSV file
     - `CSVSave.java`
     - *(none)*
   * - Calculate damping moment coefficient after every simulation step
     - `DampingMoment.java`
     - *(none)*
   * - Print summary of simulation progress after each step
     - `PrintSimulation.java`
     - *(none)*
   * - Active roll control
     - `RollControl.java`
     - `RollControlConfigurator.java`
   * - Stop simulation at specified time or number of steps
     - `StopSimulation`
     - `StopSimulationConfigurator`
