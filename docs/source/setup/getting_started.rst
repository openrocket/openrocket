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

.. _the_user_interface:

The User interface
==================

The OpenRocket user interface is divided horizontally into four sections:
- **Main Menu** (green)
- **Task Tabs** (black)
- **Rocket Design, Motors & Configuration, and Flight Simulation Pane** (red)
- **Rocket Views Pane** (blue)

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
      - **New**: Start a new project without closing the current one.
      - **Open...**: Open a previously saved `*.ork` file.
      - **Open Recent**: Open a recently opened file.
      - **Open Example**: Select and open an example project included with OpenRocket.
2. **File saving options**:
      - **Save**: Save changes to the current project.
      - **Save as...**: Save the project with a different filename or location.
3. **Import and export options**:
      - **Export as**: Export the project to a different file format, such as Rocksim 10 (`.rkt`).
      - **Save decal image**: Save a decal image file used in the project.
      - **Print design info...**: Print or export technical details of the rocket's components, fin set templates, or the rocket design to a `*.pdf`.
4. **Closing**:
      - **Close design**: Exit the current project (prompts to save unsaved changes).
5. **Quitting**:
      - **Quit**: Exit OpenRocket, saving each open project if necessary.

Edit
^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.02.File.png
   :width: 35%
   :align: center
   :figclass: or-image-border

   The OpenRocket Edit Menu

The **Edit Menu** is divided into three types of operations:

1. Undoing and redoing an action
2. Cutting, copying, pasting, and deleting components and text
3. Scaling the rocket and system preferences

Tools
^^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.03.File.png
   :width: 35%
   :align: center
   :figclass: or-image-border

   The OpenRocket Tools Menu

The **Tools Menu** provides the following design tools:

- ``Component Analysis``: Analyze the (aerodynamic) effect of specific components
- ``Rocket optimization``: Optimize particular rocket characteristics
- ``Custom expressions``: Create custom expressions for specialized analysis
- ``Photo Studio``: Display the rocket in 3D with a variety of backgrounds and effects in the photo studio

Help
^^^^

.. figure:: /img/setup/getting_started/OR.Guide.User_Interface.04.04.File.png
   :width: 25%
   :align: center
   :figclass: or-image-border

   The OpenRocket Help Menu

The **Help Menu** is divided into three sections:

1. Guided tours demonstrating the use of OpenRocket
2. Bug reporting and debugging tools to assist users in providing feedback to the developers
3. Licensing, version, and other general information about OpenRocket

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
- The component tree
- Component arrangement buttons
- Component selection buttons

The components available in OpenRocket are divided into four classes based upon component function:
1. Assembly components
2. Body components and fin sets (external components)
3. Internal components
4. Mass components (which include electronics and recovery components)

Components are "greyed out" until it would be appropriate to add that component type. As components are added, you will
see the component tree (on the left side of the window) grow with each component added.

..
   TODO: For a detailed description of each component, see :ref:`Component Details <component_details>`.

Motors & Configuration
^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/02.02.Motor-Configuration.Tab.png
   :width: 95%
   :align: center
   :figclass: or-image-border

The **Motors & Configurations** tab is where you select motors, recovery events, and stage timing. Motor configuration options include:
- Creating new configurations
- Renaming existing configurations
- Removing (deleting) configurations
- Copying configurations

With a specific configuration selected, you may:
- Select (or select a different) motor
- Remove the motor
- Select and reset the motor ignition timing

.. TODO: For more motors and configuration utilization details, see :ref:`Motors & Configuration Details <motors_configuration_details>`.

Flight Simulations
^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/03.02.Flight_simulations.Tab.png
   :width: 95%
   :align: center
   :figclass: or-image-border

The **Flight Simulations** tab is where you manage and run flight simulations and flight simulation plots. From here,
you can add new simulations, or edit, run, or delete existing simulations. Select a single simulation, and you can
even plot and export the simulation results.

..
   TODO: For more details on how to use these functions, see :ref:`Flight Simulations Details <flight_simulations_details>`.

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

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.Side_View.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.Back_View.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

3D Figure/3D Unfinished
^^^^^^^^^^^^^^^^^^^^^^^

The **3D Figure** and **3D Unfinished** view allow you to look through the rocket's exterior to view many of the interior
components. These views can help you more clearly see the relationship between the placement of different components
inside the airframe.

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Figure.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Unfinished.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

3D Finished
^^^^^^^^^^^

The **3D Finished** view shows you what the rocket will look like when finished. OpenRocket allows you to select
component colors, inside and outside of outer tubes, right side or left side of fins, and even creating transparent
components, all with or without decals (transparent or opaque).

.. figure:: /img/setup/getting_started/Getting_Started.Rocket_Views.3D_Finished.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

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

Double-Click **Rocket**
^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/04.01.02.Rocket_Configuration.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Rename **Rocket**
^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/04.01.05.Rocket_Configuration.Rename.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Adding External Components
--------------------------

Now it's time to start putting together components to build the rocket design. The generally accepted way of putting
together a rocket design is from top to bottom, from nose to tail. So, we'll add the nose cone first.

Selecting a Nose Cone
^^^^^^^^^^^^^^^^^^^^^

With the ``Stage`` selected, click on the :guilabel:`Nose Cone` button and the **Nose Cone configuration** window will pop up.
Then, select :guilabel:`From database...` to open the Choose component present window. From here, you can select from the
pre-loaded parts database. Select the nose cone shown below, and click the :guilabel:`Close` button, then close the
**Nose Cone configuration** window.

.. figure:: /img/setup/getting_started/11.01.03.Rocket_Build.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/Getting_Started.Components.Nose_Cone.Parts_Library.Highlighted.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/Getting_Started.Components.Nose_Cone.jpg
   :width: 95%
   :align: center
   :figclass: or-image-border

Congratulations, you've just added your first component.

Adding a Payload Bay
^^^^^^^^^^^^^^^^^^^^

So that a few "appearance" can be demonstrated later, a payload bay will be added after the nose cone. To do this,
with either the **Stage** or **Nose Cone** selected, click on the :guilabel:`Body Tube` button and the **Body Tube configuration**
window will pop up. Then, select :guilabel:`From database...` to open the Choose component present window. From here, you can
select from the pre-loaded parts database. Select the body tube shown below, and click the :guilabel:`Close` button, then close
the **Body Tube configuration** window.

.. figure:: /img/setup/getting_started/11.02.01.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.02.05.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.02.06.Rocket_Build.Payload_Bay.png
   :width: 95%
   :align: center
   :figclass: or-image-border

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

.. figure:: /img/setup/getting_started/11.03.06.Rocket_Build.Transition.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.03.07.Rocket_Build.Transition.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Adding a Body Tube
^^^^^^^^^^^^^^^^^^

Now, do what you did to add the **Payload Bay**, above, but select this body tube from the parts database:

.. figure:: /img/setup/getting_started/11.04.01.Rocket_Build.Body_Tube.png
   :width: 65%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.04.02.Rocket_Build.Body_Tube.png
   :width: 95%
   :align: center
   :figclass: or-image-border

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

.. figure:: /img/setup/getting_started/11.05.02.Rocket_Build.Fins.png
   :width: 60%
   :align: center
   :figclass: or-image-border

Fins attach to another component, in this case the **Body Tube**. As circled below, the fins are shown underneath the
**Body Tube** on the component tree.

.. figure:: /img/setup/getting_started/11.05.03.Rocket_Build.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

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

.. figure:: /img/setup/getting_started/11.06.03.Rocket_Build.Launch_Lug.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Adding Internal Components
--------------------------

Selecting a Parachute and Shock Cord
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/11.07.03.Rocket_Build.Parachute.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.08.01.Rocket_Build.Parachute.png
   :width: 60%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.08.02.Rocket_Build.Parachute.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Selecting an Engine Block
^^^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/11.09.01.Rocket_Build.Engine_Block.png
   :width: 60%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.09.02.Rocket_Build.Engine_Block.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Viewing Your Design
===================

Basic Views
-----------

With the airframe complete, you can view your design in either 2D (as above) or three 3D views. The most commonly used of which are **3D Unfinished** and **3D Finished**.

.. figure:: /img/setup/getting_started/11.06.03.Rocket_Build.3D_Unfinished.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/11.06.04.Rocket_Build.3D_Finished.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Adding Appearance Settings
--------------------------

When changing **Appearance** settings, it is best to be in the **3D Finished** pane so that you can see the changes that you are making. So, let's start by changing the view to **3D Finished**.

.. figure:: /img/setup/getting_started/11.06.04.Rocket_Build.3D_Finished.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Changing Color
--------------

The first change that will be made is to select the color for and change the color of the nose cone. Double-click on the nose cone in the parts tree to open the **Nose Cone configuration** window, then select the **Appearance tab**.

Nose Cone **Appearance**
^^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.10.01.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Now, uncheck the **Appearance** **Use default** box. Then, Click on the **Color** box to open the **Choose color** window. Select the color of your choice (purple will be used here). Click **OK** to use your selection, then **Close** the **Nose Cone configuration** window.

Change Nose Cone Color
^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.10.05.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Nose Cone Color Changed
^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.10.06.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Repeat those steps for the **Transition**, **Body Tube**, **Trapezoidal Fin Set**, and **Launch Lug**; body tubes, launch lugs, and fins also have a **Texture** that will need to be set to **none**.

.. figure:: /img/setup/getting_started/12.10.07.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Now for a little magic. Open the **Payload Bay** appearance tab, uncheck the **Appearance** **Use default** box, and set the **Texture** to **none**. Then, click on the **Color** box to open the **Choose color** window. Click on a light blue color (the box shown with the **X** below), then click **OK**. Now, set the **Opacity** to 20% and close the Payload Bay configuration window, and you have a transparent payload bay.

.. figure:: /img/setup/getting_started/12.10.11.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/12.10.12.Rocket_Build_Appearance.Nose_Cone.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Adding Decals
-------------

One last bit of magic, let's apply a decal to the transparent Payload Bay.

But, before beginning, save this image to your device.

.. figure:: /img/setup/getting_started/12.10.13.Rocket_Build_Appearance.Decal.png
   :width: 15%
   :align: center
   :figclass: or-image-border

With the decal saved to your device, you're ready to start.

Select Decal from File
^^^^^^^^^^^^^^^^^^^^^^

Open the Payload Bay configuration window and select the **Appearance** tab. Click on the **Texture** type to activate the selection drop-down, and select **From file...**. Now, navigate to where you saved the decal, and select it.

.. figure:: /img/setup/getting_started/12.10.16.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

To size and position the decal, first change the **Repeat** type to **Sticker** (you only want one symbol on the Payload Bay), then change the "Scale** and **Offset** "x" and "y" values to those shown below.

Decal Type, Size and Position
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.10.19.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/12.10.20.Rocket_Build_Appearance.Payload_Bay.Decal.png
   :width: 95%
   :align: center
   :figclass: or-image-border

And, there you have it. A decal on a transparent payload bay.

So, let's see what you've learned, and extend your knowledge. See if you can follow the screens below without any instructions.

.. figure:: /img/setup/getting_started/12.10.21.Rocket_Build_Appearance.Fins.png
   :width: 15%
   :align: center
   :figclass: or-image-border

Save this image to your device

Split the Fins
^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.11.03.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

After splitting the fins, **SAVE AND REOPEN THE DESIGN FILE**, then view in **3D Finished**.

Change Appearance of **Fin #2** and **Fin #3**
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. figure:: /img/setup/getting_started/12.11.11.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

.. figure:: /img/setup/getting_started/12.11.12.Rocket_Build_Appearance.Fins.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Viewing in **Photo Studio**
---------------------------

So, what will this bird look like in Flight? To really find that out, you need to add a motor first.

Selecting a Motor
^^^^^^^^^^^^^^^^^

**Motors & Configuration** tab

Select the **Motors & Configuration** tab, then make sure that the correct motor tube is selected before clicking **New Configuration**.

.. figure:: /img/setup/getting_started/12.12.02.Rocket_Build.Motor.png
   :width: 95%
   :align: center
   :figclass: or-image-border

**Select a Rocket Motor**

When you select **New Configuration**, the **Select a rocket motor** window opens. For this example, select the Estes D-12-7, then click **OK**.

.. figure:: /img/setup/getting_started/12.12.03.Rocket_Build.Motor.png
   :width: 80%
   :align: center
   :figclass: or-image-border

Select **Flight Configuration**
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Now, select the D-12-7 as the **Flight Configuration**, and you're ready to go to the **Photo Studio**.

.. figure:: /img/setup/getting_started/12.12.05.Rocket_Build.Motor.png
   :width: 95%
   :align: center
   :figclass: or-image-border

Flying in **Photo Studio**
--------------------------

Open **Photo Studio**
^^^^^^^^^^^^^^^^^^^^^

Are you ready to see your rocket fly? Then, open **Photo Studio**.

.. figure:: /img/setup/getting_started/12.12.01.Rocket_Build.Photo_Studio.png
   :width: 95%
   :align: center
   :figclass: or-image-border

**Select a Rocket Motor**

Here it is:

.. figure:: /img/setup/getting_started/12.12.02.Rocket_Build.Photo_Studio.png
   :width: 85%
   :align: center
   :figclass: or-image-border

**Flame** Effect

So why did you need a motor? Because, **you can't create flames without it**.

.. figure:: /img/setup/getting_started/12.12.03.Rocket_Build.Photo_Studio.png
   :width: 85%
   :align: center
   :figclass: or-image-border

Now, experiment to your heart's content!
