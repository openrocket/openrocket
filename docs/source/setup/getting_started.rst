***************
Getting Started
***************

In this section we have a look at how OpenRocket is organized, by analyzing in detail the structure of the **user interface**.
We will also briefly mention the **Example projects** that are accessible from the *File* menu. After reading this section
you will have a thorough understanding of how OpenRocket is structured, and will be ready to start designing a rocket of
your own. If you already know how this program is organized, feel free to jump to :doc:`Basic Rocket Design </user_guide/basic_rocket_design>`,
the next section.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

.. _the_user_interface:

The User interface
==================

The OpenRocket user interface is divided horizontally into four sections:

- :guilabel:`Main Menu` (green)
- :guilabel:`Task Tabs` (black)
- :guilabel:`Rocket Design`, :guilabel:`Motors & Configuration`, and :guilabel:`Flight Simulation Pane` (red)
- :guilabel:`Rocket Views Pane` (blue)

.. figure:: /img/setup/getting_started/2023.01.Guide.User_Interface.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   UI of OpenRocket divided into four

Main Menu
---------

The **Main Menu**, sometimes referred to as the **Menu Bar** or **Application Ribbon**, is located at the top of the OpenRocket
window. On macOS, the main menu is located at the top of the screen, embedded in the native macOS menu bar. We will run through
each of the menu options in the main menu.

File
^^^^

.. figure:: /img/setup/getting_started/02.04.01.File_Menu.png
   :width: 35%
   :align: center
   :figclass: or-image-border

   The OpenRocket File Menu

The **File Menu** is divided into five divisions by function:

1. **File opening options**:
      - :guilabel:`New`: Start a new project without closing the current one.
      - :guilabel:`Open...`: Open a previously saved `*.ork` file.
      - :guilabel:`Open Recent`: Open a recently opened file.
      - :guilabel:`Open Example`: Select and open an example project included with OpenRocket.
2. **File saving options**:
      - :guilabel:`Save`: Save changes to the current project.
      - :guilabel:`Save as...`: Save the project with a different filename or location.
3. **Import and export options**:
      - :guilabel:`Export as`: Export the project to a different file format, such as Rocksim 10 (`.rkt`).
      - :guilabel:`Save decal image`: Save a decal image file used in the project.
      - :guilabel:`Print design info...`: Print or export technical details of the rocket's components, fin set templates, or the rocket design to a `*.pdf`.
4. **Closing**:
      - :guilabel:`Close design`: Exit the current project (prompts to save unsaved changes).
5. **Quitting**:
      - :guilabel:`Quit`: Exit OpenRocket, saving each open project if necessary.

Edit
^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.02.File.png
   :width: 35%
   :align: center
   :figclass: or-image-border

   The OpenRocket Edit Menu

The **Edit Menu** is divided into three types of operations:

1. :guilabel:`Undo` and :guilabel:`Redo` an action
2. :guilabel:`Cut`, :guilabel:`Copy`, :guilabel:`Paste`, and :guilabel:`Delete` objects
3. :guilabel:`Scale`: scale components or the entire rocket.button
4. :guilabel:`Preferences`: access OpenRocket system preferences

Tools
^^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.03.File.png
   :width: 35%
   :align: center
   :figclass: or-image-border

   The OpenRocket Tools Menu

The **Tools Menu** provides the following design tools:

- :guilabel:`Component Analysis`: Analyze the (aerodynamic) effect of specific components
- :guilabel:`Rocket optimization`: Optimize particular rocket characteristics
- :guilabel:`Custom expressions`: Create custom expressions for specialized analysis
- :guilabel:`Photo Studio`: Display the rocket in 3D with a variety of backgrounds and effects in the photo studio

Help
^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.04.File.png
   :width: 25%
   :align: center
   :figclass: or-image-border

   The OpenRocket Help Menu

The **Help Menu** is divided into three sections:

1. :guilabel:`Guided tours`: demonstrating the use of OpenRocket
2. :guilabel:`Bug reporting` and :guilabel:`Debug log`: tools to assist users in providing feedback to the developers
3. :guilabel:`License` and :guilabel:`About`: other general information about OpenRocket

Task Tabs
---------

The windows shown below utilize the *A simple model rocket* example included with OpenRocket.

Rocket Design
^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/01.02.Rocket_Design.Tab.png
   :width: 95%
   :align: center
   :figclass: or-image-border

The **Rocket Design** tab is divided into three sections:

- *(Left)* The :guilabel:`Component tree`: A tree view of the components that make up the rocket.
- *(Middle)* :guilabel:`Component action buttons`: Buttons to for instance edit, move, or delete the currently selected components.
- *(Right)* :guilabel:`Component addition buttons`: Buttons to add new components to the rocket.

The components available in OpenRocket are divided into four classes based upon component function:

1. **Assembly Components**
2. **Body Components and Fin Sets** (external components)
3. **Internal Components**
4. **Mass Components** (which include electronics and recovery components)

Components are "greyed out" until it would be appropriate to add that component type to the currently selected component in
the component tree. For example, if you selected a fin set component in the component tree, then the nose cone component button
will be greyed out, because you can not add a nose cone to a fin set. As components are added, you will
see the component tree (on the left side of the window) grow with each component added.

.. todo::
   For a detailed description of each component, see \:ref\:\`Component Details <component_details>\`.

Motors & Configuration
^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/02.02.Motor-Configuration.Tab.png
   :width: 95%
   :align: center
   :figclass: or-image-border

The **Motors & Configurations** tab is where you select motors, recovery events, and stage timing. Motor configuration options include:

- :guilabel:`New Configuration`: Create a new flight configuration
- :guilabel:`Rename Configuration`: Rename the currently selected configuration
- :guilabel:`Remove Configuration`: Remove the currently selected configuration
- :guilabel:`Copy Configuration`: Copy the currently selected configuration

With a specific configuration selected, you may:

- :guilabel:`Select motor`: Choose a motor from the motor database for the currently selected motor mount.
- :guilabel:`Remove motor`: Remove the currently selected motor from the motor mount.
- :guilabel:`Select ignition`: Set the motor ignition timing for the current motor.
- :guilabel:`Reset ignition`: Reset the motor ignition timing for the current motor to the default values.

.. todo::
   For more motors and configuration utilization details, see \:ref\:\`Motors & Configuration Details <motors_configuration_details>\`.

Flight Simulations
^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/03.02.Flight_simulations.Tab.png
   :width: 95%
   :align: center
   :figclass: or-image-border

The **Flight Simulations** tab is where you manage and run flight simulations and flight simulation plots. From here,
you can add new simulations, or edit, run, or delete existing simulations. Select a single simulation, and you can
even plot and export the simulation results.

.. todo::
   For more details on how to use these functions, see \:ref\:\`Flight Simulations Details <flight_simulations_details>\`.

Rocket Views
------------

The windows shown below utilize the *A simple model rocket* example included with OpenRocket.

Top/Side/Back View
^^^^^^^^^^^^^^^^^^

The **Top View**, **Side View**, and **Back View** are line drawings, similar to a blueprint that shows all of the rocket
components and the placement of those components. Almost all of your design work will take place in the top, side, and back views.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.Top_View.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   Top view.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.Side_View.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   Side view.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.Back_View.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   Back view.

3D Figure/3D Unfinished
^^^^^^^^^^^^^^^^^^^^^^^

The **3D Figure** and **3D Unfinished** view allow you to look through the rocket's exterior to view many of the interior
components. These views can help you more clearly see the relationship between the placement of different components
inside the airframe.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Figure.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   3D Figure view.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Unfinished.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   3D Unfinished view.

3D Finished
^^^^^^^^^^^

The **3D Finished** view shows you what the rocket will look like when finished. OpenRocket allows you to select
component colors, inside and outside of outer tubes, right side or left side of fins, and even creating transparent
components, all with or without decals (transparent or opaque).

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Finished.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   3D Finished view.

Become Familiar with OpenRocket
-------------------------------

For new users of OpenRocket, before attempting to create your own custom rocket design, it is strongly recommended that
you become familiar with the OpenRocket user interface and generally accepted rocket design principles by opening and
looking at how an example is assembled, making changes to the example, and understanding how to simulate flights.

The example designs are found here:

.. figure:: /img/setup/getting_started/2023.01.Open_Example.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Where to find the example design files.

OpenRocket currently includes the following example designs:

- "Standard" designs:
   - **A simple model rocket**: A basic rocket design. This is a good starting point for new users. The design contains all
     the elements of a standard rocket design, including recovery and experimentation with different motors.
   - **Two-stage rocket**: A two-stage rocket design
   - **Three-stage rocket**: A three-stage rocket design
   - **TARC payload rocket**: Demonstrates payload and booster sections with individual recovery systems deployed by motor ejection.
     TARC = Team America Rocketry Challenge
   - **3D Printable Nose Cone and Fins**: A rocket design to test exporting the nose cone and fins to an OBJ file for 3D printing.
- "Advanced" designs
   - **Airstart timing**: Demonstrates the effect of different airstart timings on overall altitude.
   - **Base drag hack (short-wide)**: Demonstrates the application of the "base drag" hack to adjust the center of pressure
     for a short-wide rocket, one with a length to diameter ratio of less than 10:1.
   - **Chute release**: A simple model rocket example adapted to use an electronic chute release.
   - **Dual parachute deployment**: A standard fiberglass zipperless dual deploy rocket.
   - **Clustered motors**: A rocket design with clustered motors.
   - **Parallel booster staging**: Demonstrates parallel booster staging.
   - **Pods--airframes and winglets**: Demonstrates two uses of pods, both for the traditional "wing pods", and also
     using a phantom body tube to implement "fins on fins" for the horizontal stabilizer.
   - **Pods--powered with recovery deployment**: Demonstrates the use of pods for powered recovery deployment.
- Designs using advanced simulations, such as extensions and scripting
  - **Simulation extensions**: Demonstrates active roll control and air-start using simulation extensions. The main fins are slightly misaligned, which causes roll to occur.
  - **Simulation scripting**: Demonstrates active roll control and air-start using simulation extension written in JavaScript.

----

The Basics of Using OpenRocket
==============================

Rocket Configuration
--------------------

To build your first rocket, start OpenRocket, then double click the **Rocket** label at the top of the component tree to
open the **Rocket configuration** pop-up window. OpenRocket allows you to name your design, identify the designer, make
comments, and create a revision history.

The default design name is **Rocket**, but that name can be changed, and a design name change also changes the name of
the rocket shown on the component tree. So, rename your design and enter the designer, comments, and revision history
information you desire.

1. Double-Click **Rocket** in the component tree

   .. figure:: /img/setup/getting_started/04.01.02.Rocket_Configuration.png
      :width: 95%
      :align: center
      :figclass: or-image-border

      Open the rocket configuration window.

2. Rename **Rocket**

   .. figure:: /img/setup/getting_started/04.01.05.Rocket_Configuration.Rename.png
      :width: 95%
      :align: center
      :figclass: or-image-border

      Change the name of your rocket.

Adding External Components
--------------------------

Now it's time to start putting together components to build the rocket design. The generally accepted way of putting
together a rocket design is from top to bottom, from nose to tail. So, we'll add the nose cone first.

Adding a Nose Cone
^^^^^^^^^^^^^^^^^^^^^

With the **Stage** selected, click on the :guilabel:`Nose Cone` button and the **Nose Cone configuration** window will pop up.
Then, click the :guilabel:`Parts Library` button on the top-right of the configuration window. This will open a new window,
the **Component preset window**. From here, you can select a nose cone from a list of built-in nose cone presets from
various manufacturers. Select the nose cone shown below, and click the :guilabel:`Close` button to close the
**Nose Cone configuration** window.

.. figure:: /img/setup/getting_started/11.01.03.Rocket_Build.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Adding a nose cone to the rocket.

.. figure:: /img/setup/getting_started/Getting_Started.Components.Nose_Cone.Parts_Library.Highlighted.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   Click the :guilabel:`Parts Library` button (top-right in the nose cone configuration window) to select a nose cone from the parts database.

.. figure:: /img/setup/getting_started/Getting_Started.Components.Nose_Cone.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

   The nose cone has been added to the rocket.

Congratulations, you've just added your first component! 🎉

Adding a Payload Bay
^^^^^^^^^^^^^^^^^^^^

Next, we will add a payload bay after the nose cone. To do this, with either the **Stage** or **Nose Cone** selected,
click on the :guilabel:`Body Tube` button and the **Body Tube configuration** window will pop up.
Like with the nose cone, click :guilabel:`Parts Library` to open the **Component preset window**.
Select the body tube shown below, and click the :guilabel:`Close` button to close the **Body Tube configuration** window.

.. figure:: /img/setup/getting_started/11.02.01.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Adding a payload bay to the rocket.

.. figure:: /img/setup/getting_started/11.02.05.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Selecting a body tube from the parts database.

.. figure:: /img/setup/getting_started/11.02.06.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   The payload bay has been added to the rocket.

Adding a Transition
^^^^^^^^^^^^^^^^^^^

Transitions are most often used to connect body tubes with different diameters. But, a transition can also be used to
connect two body tubes of the same diameter, as will be done here.

To do this, with either the **Stage** or **Payload Bay** selected, click on the :guilabel:`Transition` button and the
**Transition configuration** window will pop up. The default **Transition Configuration** tab is the **General** tab.
On this tab, change your entries in the circled areas below to match the entries shown. Then, click the **Shoulder** tab,
and change your entries in the circled areas below to match the entries shown. Then, click the :guilabel:`Close` button.

.. figure:: /img/setup/getting_started/11.03.01.Rocket_Build.Transition.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Adding a transition behind the payload bay.

.. figure:: /img/setup/getting_started/11.03.06.Rocket_Build.Transition.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Setting the transition parameters in the :guilabel:`General` tab and :guilabel:`Shoulder` tab.

.. figure:: /img/setup/getting_started/11.03.07.Rocket_Build.Transition.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   The transition has been added to the rocket.

Adding a Body Tube
^^^^^^^^^^^^^^^^^^

Now, do what you did to add the **Payload Bay**, above, but select this body tube from the parts database:

.. figure:: /img/setup/getting_started/11.04.01.Rocket_Build.Body_Tube.png
   :width: 65%
   :align: center
   :figclass: or-image-border

   Add another body tube, behind the transition, and select it from the parts database.

.. figure:: /img/setup/getting_started/11.04.02.Rocket_Build.Body_Tube.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   The body tube has been added to the rocket.

Adding Fins
^^^^^^^^^^^

The bottom component are the fins. OpenRocket offers four types of fins, **Trapezoidal**, **Elliptical**, **Free Form**,
and **Tube Fins**. For this design, **Trapezoidal** fins will be used.

With the **Body Tube** selected, click on the :guilabel:`Trapezoidal` fins button and the **Trapezoidal Fin Set configuration**
window will pop up. On your default **General** tab, change your entries match the entries shown. Then, click the
:guilabel:`Close` button.

.. figure:: /img/setup/getting_started/11.05.01.Rocket_Build.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Add Trapezoidal fins inside the second body tube.

.. figure:: /img/setup/getting_started/11.05.02.Rocket_Build.Fins.png
   :width: 60%
   :align: center
   :figclass: or-image-border

   Fin set configuration window

Fins attach to another component, in this case the **Body Tube**. As circled below, the fins are shown underneath the
**Body Tube** on the component tree.

.. figure:: /img/setup/getting_started/11.05.03.Rocket_Build.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Trapezoidal fin set added to the rocket.

Adding a Launch Guide
^^^^^^^^^^^^^^^^^^^^^

OpenRocket includes two styles of launch guides, **Rail Buttons** and a **Launch Lug**. Because of the diameter of the
body tube, a **Launch Lug** will be used for this design. As with fins, launch guides attach to another component, in
this case the body tube.

You should now be able to open the **Launch Lug configuration** window without assistance. So, open your **Launch Lug configuration**
window, and change the specifications to match those shown below.

.. figure:: /img/setup/getting_started/11.06.02.Rocket_Build.Launch_Lug.png
   :width: 60%
   :align: center
   :figclass: or-image-border

   Launch lug configuration window.

.. figure:: /img/setup/getting_started/11.06.03.Rocket_Build.Launch_Lug.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Launch lug added to the body tube.

Adding a Parachute and Shock Cord
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Next we will add a **Parachute** and **Shock Cord** to the rocket for recovery.
Select the body tube and add a **Parachute** and **Shock Cord**. The parachute and shock cord attach to the body tube.

.. figure:: /img/setup/getting_started/11.07.03.Rocket_Build.Parachute.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Parachute parts library (left) and configuration window (right).

.. figure:: /img/setup/getting_started/11.08.01.Rocket_Build.Parachute.png
   :width: 60%
   :align: center
   :figclass: or-image-border

   Shock cord configuration window.

.. figure:: /img/setup/getting_started/11.08.02.Rocket_Build.Parachute.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Parachute and shock cord added to the body tube.

Adding an Engine Block
^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/11.09.01.Rocket_Build.Engine_Block.png
   :width: 60%
   :align: center
   :figclass: or-image-border

   Engine block configuration window.

.. figure:: /img/setup/getting_started/11.09.02.Rocket_Build.Engine_Block.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Engine block added to the body tube.

Viewing Your Design
===================

With the airframe complete, you can view your design in either 2D (as above) or three 3D views. The most commonly used
of which are **3D Unfinished** and **3D Finished**.

.. figure:: /img/setup/getting_started/11.06.03.Rocket_Build.3D_Unfinished.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   3D Unfinished view (body tubes are semi-transparent so that the internal components become visible).

.. figure:: /img/setup/getting_started/11.06.04.Rocket_Build.3D_Finished.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   3D Finished view (what your final build would look like).

----

Adding Appearance Settings
==========================

If you want your OpenRocket design to visually resemble what you want to build, you can change the appearance of the components.
When changing **Appearance** settings, it is best to be in the **3D Finished** pane so that you can see the changes that
you are making. So, let's start by changing the view to **3D Finished**.

Changing Color
--------------

The first change that will be made is to select the color for and change the color of the nose cone. Double-click on the
nose cone in the parts tree to open the **Nose Cone configuration** window, then select the :guilabel:`Appearance` tab.

.. figure:: /img/setup/getting_started/12.10.01.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Nose cone :guilabel:`Appearance` tab in the configuration window.

Now, uncheck the **Appearance** :guilabel:`Use default` box. Then, Click on the :guilabel:`Color` box to open the
**Choose color** window. Select the color of your choice (purple will be used here). Click :guilabel:`OK` to use your
selection, then :guilabel:`Close` the **Nose Cone configuration** window.

.. figure:: /img/setup/getting_started/12.10.05.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Changing the nose cone color.

.. figure:: /img/setup/getting_started/12.10.06.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Nose cone color changed.

Repeat those steps for the **Transition**, **Body Tube**, **Trapezoidal Fin Set**, and **Launch Lug**; body tubes, launch lugs, and fins also have a **Texture** that will need to be set to **none**.

.. figure:: /img/setup/getting_started/12.10.07.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   All external components, except for the payload bay have now been colored.

Now for a little magic. Open the **Payload Bay** appearance tab, uncheck the **Appearance** **Use default** box, and set
the :guilabel:`Texture` to ``<none>```. Then, click on the :guilabel:`Color` box to open the **Choose color** window.
Click on a light blue color (the box shown with the **X** below), then click :guilabel:`OK`. Now, set the :guilabel:`Opacity`
to **20%** and close the Payload Bay configuration window, and you have a transparent payload bay.

.. figure:: /img/setup/getting_started/12.10.11.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Change the color of the payload bay to light blue and lower the opacity to 20%.

.. figure:: /img/setup/getting_started/12.10.12.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   You now have a semi-transparent payload bay!

Adding Decals
-------------

One last bit of magic, let's apply a decal to the transparent Payload Bay.

But, before beginning, save the following image to your device.

.. figure:: /img/setup/getting_started/12.10.13.Rocket_Build_Appearance.Decal.png
   :width: 15%
   :align: center
   :figclass: or-image-border

   :download:`Save this decal image on your computer </img/setup/getting_started/12.10.13.Rocket_Build_Appearance.Decal.png>`.

With the decal saved to your device, you're ready to start.

Select Decal from File
^^^^^^^^^^^^^^^^^^^^^^

Open the Payload Bay configuration window and select the :guilabel:`Appearance` tab. Click on the :guilabel:`Texture`
type to activate the selection drop-down, and select :guilabel:`From file...`. Now, navigate to where you saved the decal,
and select it.

.. figure:: /img/setup/getting_started/12.10.16.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Decal Type, Size and Position
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To size and position the decal, first change the **Repeat** type to **Sticker** (you only want one symbol on the Payload Bay),
then change the :guilabel:`Scale` and :guilabel:`Offset` :guilabel:`x` and :guilabel:`y` values to those shown below.

.. figure:: /img/setup/getting_started/12.10.19.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Change the :guilabel:`Repeat` type and set the x and y :guilabel:`Scale` and :guilabel:`Offset` values.

.. figure:: /img/setup/getting_started/12.10.20.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   The decal has been added to the payload bay and sized and positioned correctly.

And, there you have it, a decal on a transparent payload bay!

So, let's see what you've learned, and extend your knowledge. See if you can follow the screens below without any instructions.
Save the following image to your device.

.. figure:: /img/setup/getting_started/12.10.21.Rocket_Build_Appearance.Fins.png
   :width: 15%
   :align: center
   :figclass: or-image-border

   :download:`Save this decal image on your computer </img/setup/getting_started/12.10.21.Rocket_Build_Appearance.Fins.png>`.

.. figure:: /img/setup/getting_started/12.11.03.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Split the Fins in the **Trapezoidal Fin Set configuration window** (left). After splitting, you'll see the separate fin
   instances in the component tree (right).

After splitting the fins, **SAVE AND REOPEN THE DESIGN FILE**, then view in **3D Finished**.

.. figure:: /img/setup/getting_started/12.11.11.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Change Appearance of **Fin #2** and **Fin #3**

.. figure:: /img/setup/getting_started/12.11.12.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

----

Viewing in Photo Studio
=======================

So, what will this bird look like in flight? For that, we can use the **Photo Studio** tool. However, to get a representative
representation, you need to add a motor first.

Selecting a Motor
-----------------

In the :guilabel:`Task tabs` in the UI, select the :guilabel:`Motors & Configuration` tab. Then, make sure that the correct
motor tube is selected on the left in the :guilabel:`Motor mounts` list before clicking :guilabel:`New Configuration`.

.. figure:: /img/setup/getting_started/12.12.02.Rocket_Build.Motor.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Adding a new flight configuration in the :guilabel:`Motors & Configuration` tab.

When you click :guilabel:`New Configuration`, the **Motor Selection** window opens. For this example, select the Estes D-12-7,
then click :guilabel:`OK`.

.. figure:: /img/setup/getting_started/12.12.03.Rocket_Build.Motor.png
   :width: 80%
   :align: center
   :figclass: or-image-border

   Selecting the Estes D-12-7 rocket motor in the **Motor Selection** window.

Now, select the D-12-7 as the **Flight Configuration**, and you're ready to go to the **Photo Studio**.

.. figure:: /img/setup/getting_started/12.12.05.Rocket_Build.Motor.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Select the D-12-7 as the **Flight Configuration**.

Flying in Photo Studio
----------------------

Are you ready to see your rocket fly? Then, open :guilabel:`Photo Studio` from the :menuselection:`Tools` menu.

.. figure:: /img/setup/getting_started/12.12.01.Rocket_Build.Photo_Studio.png
   :width: 95%
   :align: center
   :figclass: or-image-border

   Open **Photo Studio**.

You can now view your creation in 3D and interact with it. You can change the background, rocket orientation, camera settings,
and even add some cool effects.

.. figure:: /img/setup/getting_started/12.12.02.Rocket_Build.Photo_Studio.png
   :width: 85%
   :align: center
   :figclass: or-image-border

   The rocket inside **Photo Studio**.

So why did you to add a motor before going to Photo Studio? Because,
**you can't activate the flame effect in Photo Studio if your rocket does not have a motor**, and the flame effect is
arguably the coolest part of Photo Studio! Go to the :guilabel:`Effects` tab and enable the :guilabel:`Flame` effect.

.. figure:: /img/setup/getting_started/12.12.03.Rocket_Build.Photo_Studio.png
   :width: 85%
   :align: center
   :figclass: or-image-border

Now, play around with the settings to your heart's content!
