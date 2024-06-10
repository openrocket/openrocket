*******************
Basic Rocket Design
*******************

In this section we'll look at how to design a basic rocket by examining the A simple model rocket design example.
After reading this section you should have an understanding of how to start designing your own rockets.

We will start with a brief discussion on the selection of **available components**, and then the components used in
the ``A simple model rocket`` example design file (:menuselection:`File --> Open example --> A simple model rocket`).
Then we'll build a rocket from scratch to see how it’s done.

.. note::

   This section assumes you have already :doc:`installed OpenRocket </setup/installation>` and are familiar with the
   :doc:`basic layout of the program </setup/getting_started>`.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Component Configuration Window
==============================

There are several types of components available to you as a rocket designer, and you can either customize them to meet
your needs, or simply load component presets from the parts library.

To start, let's begin a new project: :menuselection:`File --> New`. It doesn’t matter what we call it at the moment, but go ahead
and save the design straight away: :menuselection:`File --> Save`. At this point you should be presented with a blank rocket design window:

.. figure:: /img/user_guide/basic_rocket_design/main_window.png
   :align: center
   :width: 95%
   :figclass: or-image-border
   :alt: The OpenRocket rocket design window.

   The OpenRocket rocket design window.

When you first start a new rocket design you will see that there are four categories of components available in the **Add new component** panel
on the top-right of the rocket design window:

* ``Assembly Components``: These are components that have **no physical meaning** of their own but are used to **group components together**.

* ``Body Components and Fin Sets``: These are components that are used to **build the rocket's airframe**.

* ``Inner Components``: These are components that are placed **inside the rocket's airframe**.

* ``Mass Components``: These are components that are used to **add mass** to the rocket or serve as **recovery devices**.

We would usually start building our rocket by selecting a nose cone but for the sake of this guide click the
:guilabel:`Body Tube` icon in the **Body Components and Fin Sets** section. This will then open up a configuration window
to edit the body tube parameters.

The Body Tube Configuration Window
----------------------------------

At this point you should see a new window titled **Body tube configuration**.

.. figure:: /img/user_guide/basic_rocket_design/body_tube_config.png
   :align: center
   :width: 60%
   :figclass: or-image-border
   :alt: The Body Tube configuration window.

   The Body Tube configuration window.

This window allows you to either select a preset for the component type you are choosing, or make your own. Let’s start
by examining how to customize it ourselves. If you have a look at the Body tube configuration window you will see that
at the top there is a :guilabel:`Component name` field. Here we can change the name of the current component to anything we choose.
*(Note that this name will then appear in the design tree seen in the Rocket Design panel of the main program window.)*

Just below Component name there are several tabs:

- :guilabel:`General`: allows us to alter the basic attributes of the component.
- :guilabel:`Motor`: allows us to make the body tube into a motor mount, and also edit the properties of the motor.
- :guilabel:`Override`: allows us to manually set the mass or centre of gravity for the component.
- :guilabel:`Appearance`: allows us to select colours, textures and other finishes.
- :guilabel:`Comment`: allows us to enter any comments or notes about the component.

.. note::

   These tabs are specific to the component being designed. For example, a *nose cone* will not have
   a :guilabel:`Motor` tab, but will instead have a :guilabel:`Shoulder` tab.

After you learn how to navigate around one component *configuration panel*, the others should be relatively self-explanatory.

General Tab
^^^^^^^^^^^

The rest of the *Body tube configuration* window lists the different parameters for the current component type. As you
can see, the :guilabel:`General` tab provides options to manually enter numbers, or you can also use the spin boxes (coarse control)
or sliders (fine control) provided to adjust the parameter values. The :guilabel:`Automatic` checkbox will adjust the dimensions
of the component automatically. Here you will also see a :guilabel:`Filled` checkbox. If this is checked you will notice that
the inner diameter goes to zero, i.e., a filled (solid) tube. Note how the Component mass changes when this box is checked.

On the right hand side of the window you will see the :guilabel:`Component material` and :guilabel:`Component finish` drop-down menus.
If you click on these you will be presented with a list of various materials and finishes, each with their own weight and
thickness. If you are using the same finish for the entire rocket you can click the :guilabel:`Set for all` button to make each
component use the same finish. The last notable feature in this window is in the bottom left. There you will see a live
display of the **Component mass**. This will update automatically as you change parameter values. Experiment with the
sliders to see how the component changes in the *design window*.

You may have noticed that, apart from the mass, there is no noticeable difference when either the :guilabel:`inner diameter` or
:guilabel:`wall thickness` parameters are changed. To see those changes reflected in the model you will have to switch to a
different :guilabel:`View Type`, by selecting the desired view from the drop-down list at the left of the main OpenRocket
program window.

Motor Tab
^^^^^^^^^

Next to the General tab is the :guilabel:`Motor` tab. If you click on it you will see that most parameters are grayed-out, with
the exception of one checkbox. As the label mentions, this is for when you want the body tube component to also be a
**motor mount**.

.. figure:: /img/user_guide/basic_rocket_design/03.Motor_tube_tab.png
   :width: 70%
   :align: center
   :figclass: or-image-border
   :alt: The Motor tab of the Configuration Window

   The *Motor tab* of the Configuration Window.

Have a quick look at this if you wish, but we'll discuss changing *flight configurations* for a later section of the guide.

Override
^^^^^^^^

We'll take a quick look at this tab, as it common to most components. For starting out though, you most likely will not need it.

.. figure:: /img/user_guide/basic_rocket_design/04.Body_tube_override_tab.png
   :width: 70%
   :align: center
   :figclass: or-image-border
   :alt: The Override tab of the Configuration Window

   The *Override tab* of the Configuration Window.

This tab would be used when you specifically wanted to override the mass and centre of gravity (CG) of the component.

Appearance
^^^^^^^^^^

Everyone likes something shiny, don’t they? This tab allows you edit the appearance of the component.

.. figure:: /img/user_guide/basic_rocket_design/05.Body_tube_appearance_tab.png
   :width: 70%
   :align: center
   :figclass: or-image-border
   :alt: The Appearance tab of the Configuration Window

   The *Appearance tab* of the Configuration Window.

There are two sections here, the **Figure style** section and the **Appearance** section. *Figure style* changes what
the 2D figure looks like, whereas *Appearance* will change what the 3D model will look like. If you wish to use any
custom textures or images in your rocket design, you can load those through the **Texture** drop-down menu.

Comment
^^^^^^^

This section does not really need much explanation. If you want to write any comments or notes about your component
(why you chose the values you did, etc.), then this is the place to do it.

.. figure:: /img/user_guide/basic_rocket_design/06.Body_tube_comment_tab.png
   :width: 70%
   :align: center
   :figclass: or-image-border
   :alt: The Comment tab of the Configuration Window

   The *Comment tab* of the Configuration Window.

Now that we have been through all of the tabs of the *Body Tube* component, click the :guilabel:`Close` button. You should now
notice that the rest of the components are now unlocked in the top right of the *Design window*. This is because all
component types can be added to a body tube. However, a **Nose cone** should be selected **first** if you are making
your own rocket.

Let’s have a look at the full list of components. If you cannot click on the component type, try selecting the
*Body tube* in the design window in the top left panel of OpenRocket.

----

Available Design Elements
=========================

As previously mentioned, there are 4 categories to choose components from within OpenRocket. These are split into
four sections:

- **Assembly Components**
- **Body Components and Fin Sets**
- **Inner Components**
- **Mass Components**

.. figure:: /img/user_guide/basic_rocket_design/07.Component_menu.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: The Component types menu

   The *Component types* menu.

Assembly Components
-------------------

Although called components, Assembly Components are NOT physical parts. Rather, an Assembly Component is an attachment
point on which the framework of the rocket is built (an Assembly Component is a container for physical parts), and each
type has features unique to that attachment type. These framework elements are intended to contain physical components,
and should NOT be left empty. As you will see in the image, the framework types are:

- **Stage**: Every rocket has at least one stage, which is the basic framework element to which the rocket's physical
  components are attached. A Stage may be renamed, and has override and comment tabs. A stage should NOT be used if empty.
- **Boosters**: A booster is a framework element to which physical components are attached, and may be used to build
  separate pieces of the rocket, such as a glider. Boosters may ONLY be attached to a body tube, and CAN separate during
  flight from the stage to which a booster is associated. Boosters may be renamed, and have separation, general, override,
  and comment tabs. A booster should NOT be used if empty.
- **Pods**: A pod is a framework element to which physical components are attached, and may be used to build connected
  pieces of the rocket that are adjacent to the main airframe, such as side motors. Pods may ONLY be attached to a body
  tube, and CANNOT separate from the stage to which a pod is associated. Pods may be renamed, and have general, override,
  and comment tabs. A pod should NOT be used if empty.

.. figure:: /img/user_guide/basic_rocket_design/08.Assembly_Components_Icons.png
   :width: 50%
   :align: center
   :figclass: or-image-border
   :alt: The Assembly Components submenu

   The *Assembly Components* submenu.

Body Components and Fin Sets
----------------------------

Within *Body Components and Fin Sets* we have 8 component types. These components make up the external and main body of
the rocket; everything else is inside the rocket. As you will see in the image the component types are:

- **Nose Cone**: The very tip of the rocket. Usually, you will select this component first.
- **Body Tube**: As we have seen, the body tube makes up the main section of a stage.
- **Transition**: A component that usually joins one body tube to another (normally larger) tube.
- **Trapezoidal** fin: A fin set that is in the shape of a trapezoid.
- **Elliptical** fin: A fin set that is in the shape of an ellipse.
- **Freeform** fin: This special kind of fin takes any shape you want. If you add it to your model it will open up a
  design window for you to alter the shape as desired.
- **Tube Fins** Special fins, typically made from body tubes attached to the outside of the rocket body.
- **Launch Lug**: This component usually goes on the outside of a body tube and is used while the rocket is on the launch pad.

.. figure:: /img/user_guide/basic_rocket_design/ComponentBodyAndFins.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: The Body Components and Fin Sets submenu

   The *Body Components and Fin Sets* submenu.

Inner Components
----------------

Within *Inner Components* we have 5 component types, and these components are all internal.  As with *Body components
and fin sets* we will now run through the list of components.

- **Inner tube**: This component lets you add tubes to the inside of the main body tube.
- **Coupler**: Used in multi-stage rockets, a coupler joins two sections together.
- **Centering ring**: These can be used to support other components (e.g., a motor), in the centre of a larger tube.
- **Bulkhead**: This is a block of material that forms a stop or barrier between two different areas.
- **Engine block**: An engine block prevents the motor from moving forward in the motor mount tube.

.. figure:: /img/user_guide/basic_rocket_design/ComponentInner.png
   :width: 65%
   :align: center
   :figclass: or-image-border
   :alt: The Inner Components submenu

   The *Inner Components* submenu.

Mass Components
---------------

Within *Mass Components* we have 4 component types. They are:

- **Parachute**: Like any good parachute, this component will stop your rocket from becoming scrap.
- **Streamer**: Another component for keeping your rocket safe, a streamer creates drag as your rocket falls down to earth.
- **Shock Cord**: A shock cord secures the nose cone to the body of the rocket so that it isn't lost when the nose is
  blown off to deploy the parachute/streamer.
- **Mass Component**: This is a block of mass used to adjust the rocket's Center of Gravity (CG). You can name it to
  whatever you want it to represent.

.. figure:: /img/user_guide/basic_rocket_design/ComponentMasses.png
   :width: 50%
   :align: center
   :figclass: or-image-border
   :alt: The Mass Components submenu

   The *Mass Components* submenu.

You have now had a brief run through the various components available for use in OpenRocket. The next section will deal with an example rocket.

----

*A Simple Model Rocket* Example
===============================

In this section we will look at the components used in the *A simple model rocket* example design. To get started, start
OpenRocket and navigate to the main window. As a reminder it looks like this:

.. figure:: /img/user_guide/basic_rocket_design/Main_window.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: The OpenRocket main window

   The OpenRocket *main window*.

Opening Example Designs
-----------------------

We'll begin by looking at how to find and load the example rockets within OpenRocket. Recall that this was also covered
in the *Getting Started* section of this guide, but as a refresher the steps for doing this are as follows:

- In the main window, click on the :menuselection:`File` menu at the top left.
- Scroll down to **Open Examples...** open, and expand that menu. Here you will see a list of the available rocket design examples.

Your screen should now look like this:

.. figure:: /img/user_guide/basic_rocket_design/OpenExample.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: Opening an example rocket

   Opening an *example rocket*

Click on first example, our **A simple model rocket**. A *Rocket configuration* window should appear, with the *Design Name*
(A simple model Rocket) and *Designer* (Sampo Niskanen) fields populated. The *Comments* and *Revision History* fields
will be blank. Click :guilabel:`Close`. You should now have successfully loaded the rocket and be able to see a 2D schematic
in the *Rocket Design* window.

.. figure:: /img/user_guide/basic_rocket_design/After_complete_design.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: Bottom half of the Rocket design window

   Bottom half of the *Rocket design* window.

Components used in *A simple model rocket*
------------------------------------------

Now we will have a quick look through the components used in the example rocket. If you look towards the top left of
the **Rocket Design** window you will see that there is a tree of components shown. By default they should all be fully
expanded. If not, do so now.

.. figure:: /img/user_guide/basic_rocket_design/Structure.png
   :width: 50%
   :align: center
   :figclass: or-image-border
   :alt: Top left-hand portion of the Rocket design window

   Top left-hand portion of the *Rocket design* window.

This image shows *A simple model rocket* at the top, followed by the **Sustainer**, which is *Stage 1* of the rocket.

If we look at the first component in the *Sustainer* stage we see that it is the nose cone. Double click on that now.
This will bring up the *Nose cone configuration* window.

.. figure:: /img/user_guide/basic_rocket_design/ConfigNose.png
   :width: 80%
   :align: center
   :figclass: or-image-border
   :alt: Nose Cone Configuration Window

   Nose Cone Configuration Window.

As we have already examined the *configuration window* we will not repeat ourselves here. However, you will see that in
this example we have used an **Ogive** nose cone with a *Shape parameter* of 1.0. If you read the description of the
component to the right in the configuration window, you will discover that a value of 1.0 produces a **tangent ogive**.

Moving down the window, you will see that the **Nose cone length** has a value of 10, the **Base diameter** has a value
of 2.5, and the **Wall thickness** has a value of 0.2. All of these parameters have been set, in this example, to use
centimeters (**cm**) as their unit. You should also see that this component is using **Polystyrene** as its material
with a **Regular paint** finish. As you can see, the material has a density of 1.05 grams per centimeter cubed, and the
paint is 60.0 micrometers thick.  In the bottom left, the component *weight* is currently 13.2 grams. Go ahead and play
around with the sliders to see how the component changes, and then change them back when you are finished.

If you move over to the *Shoulder* tab, you'll be able to see the size attributes of the shoulder. Again, you can play
around with the sliders if you want--as long as you change them back to the original settings when you are finished.
For reference these are **2.3, 2.0** and **0.2** centimeters, respectively. Also have a look through the rest of the
tabs to see what has been selected. The only other thing to note is that the appearance has been customized. If you
were to change to the **3D Finished** view type, you will see how the finished model looks.

Moving on to the *Body tube*, you can access its various attributes by double-clicking on it in the *Rocket design* window.
Note how its various attributes have been set to create the current size. This is all fairly straightforward and as we
have already looked into the various components earlier, we will not go into great detail here.

Note the eight other components that have been placed onto the *Body tube*. In descending order these are:

- **Trapezoidal fin set**
- **Inner Tube**
- **Centering Ring**
- **Centering Ring**
- **Shock Cord**
- **Parachute**
- **Wadding**
- **Launch lug**

The *Inner Tube* in this design is used as a motor mount. You will see this if you go to the **Motor** tab in the
*Inner Tube configuration* window. This means that a motor has been fitting into this piece. Upon inspection we can
see that the current motor name is displayed in the **Flight configuration** drop-down list, on the right side of the
main OpenRocket window. You can open this drop-down menu to select other motors that are available for use in this
simulation, as well as the other example simulations that you will see on the *Flight simulation* tab.

The ninth component is place inside the *inner tube*. This is the engine block. If you look at the schematic of the
rocket you will see that this is placed in front of the grey rectangle, which is the motor. To quickly address some of
the other components, you will note that the recovery method used in this rocket is a *Parachute*: the red dashed
rounded-rectangle near the nose cone of the rocket. Along with the parachute we have a *Shock cord*, which (as was
discussed earlier) prevents loss of the nose cone upon deployment of the recovery system. This is shown as a long, black,
dashed rounded-rectangle in the same general area as the parachute. The other black, dashed rounded-rectangle box is the
*Wadding*, which is a mass component and is used here to bring the centre of gravity forward towards the nose.

We have now looked through the components used within the simple model rocket example. If you would like to see other
available components, see **Appendix A**. Now that we are familiar with what makes up the rocket we will go ahead and
build one from scratch!

----

Building *A simple model rocket*
================================

In this section we will build up the example model from scratch. After you have completed this process, you should be
able to apply what you've learned in order to make any basic rocket you want. So let’s begin...

Open a new project window so that everything is blank. The following are the steps that you will follow to create the rocket.

1. **Select the nose cone**. Change its type to **Ogive**, if that isn't already selected. Make sure that *Shape parameter*,
   *Nose cone length*, *base diameter*, and *Wall thickness* values are set to **1.0**, **10.0**, **2.5** and **0.2**, respectively.
   The units are centimeters by default, so we should not need to change these. Finally, change the *Component material* to **Polystyrene**.

2. Next, while still in the *Nose cone configuration* window, move to the *Shoulder* tab. Change the diameter to **2.3**, the length to **2.0**, and the thickness to **0.2**. Also, check the **End capped** box.

3. The last thing we have to do with the nose cone is to change its *Appearance*, so switch to that tab now. Leave the *Figure style* section unchanged but under the *Appearance* section, change the colour to one lighter than black. (Or to whatever you want, go crazy!) Then adjust the shine to 50%. That is all we need to change for now.

You should now see this in the view area at the bottom of the main OpenRocket window:

.. figure:: /img/user_guide/basic_rocket_design/NoseComplete.png
   :width: 80%
   :align: center
   :figclass: or-image-border
   :alt: Nose Cone

   Nose Cone

4. Next, add the *Body tube*. The measurements for *length*, *outer diameter*, *inner diameter*, and *wall thickness*
are **30.0**, **2.5**, **2.3**, **0.1**, respectively. Note that it may be easier to enter the last two manually rather
than use the spin boxes or sliders. Leave the **Automatic** and **Filled** boxes unchecked. The material should remain
**Cardboard** and the finish should be **Regular Paint**. The only other thing you should change here is the appearance,
but we'll leave that to you as an exercise.

You should now have this:

.. figure:: /img/user_guide/basic_rocket_design/BodyComplete.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Nose Cone and Body

   Nose Cone and Body

5. Moving on, we next need to add a **Fin set**. Make sure you have clicked on the *Body tube* so that it’s highlighted
then click on the :guilabel:`Trapezoidal` component type. The *Trapezoidal fin set configuration* window will have appeared.
Leave the left side of the window alone: the settings there are fine as-is. However, we will need to change some things
on the right hand side. First, change the *Fin cross section* to **Rounded**. Also, reduce the *Thickness* to **0.2**.
That’s all we need to do in this tab. Now move to the *Appearance* tab and customize to your preference.

.. figure:: /img/user_guide/basic_rocket_design/FinsComplete.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Nose Cone, Body, and Fins

   Nose Cone, Body, and Fins

6. The next component we need to add is the **Inner tube**. Once again highlight the body tube and then click on the
:guilabel:`Inner tube` component button. There are a couple of attributes we should change here. First, increase the *Length* value
to **7.5**. Also, increase the *Plus* value to **0.5**. Leave everything else the same.

Now go to the *Motor* tab. There are a few things we need to add here. To start, check the *This component is a motor mount*
checkbox. This will allow us to add motors. The next few steps may seem complicated but do not be daunted by them. First,
we will deal with the easy part: Increase the *Motor overhang* to **0.3**. Now click the :guilabel:`Close` button to close the
*Inner Tube configuration* window.

.. _motorconfig:

Now click on the :guilabel:`Motors & Configuration` tab towards the left upper corner of the main OpenRocket window, just under
the menu bar. On the resulting page, note the *Motors* tab towards the left upper corner. Make sure the *Inner T...* box
is checked, and then click on the :guilabel:`New Configuration` button in the top center portion of the upper panel. You should see
that a new configuration has been added to the list of configuration.

.. figure:: /img/user_guide/basic_rocket_design/MotorConfigs.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: Motors & Configurations window

   Motors & Configurations window

Left-click in the *Inner Tube* column, and then click the :guilabel:`Select motor` button just below the open panel on the right
of the screen. The *Select a rocket motor* window now appears:

.. figure:: /img/user_guide/basic_rocket_design/MotorSelection.png
   :width: 95%
   :align: center
   :figclass: or-image-border
   :alt: Motor Selection window

   Motor Selection window

To make sure your window looks the same as the one above, follow these steps:

- Make sure the *Filter Motors* tab is selected in the top right corner of the window.
- Click :guilabel:`Clear All`, then select only the **Estes** option in the *Manufacturer* list.
- Make sure that the *Total Impulse* slider is positioned over **A** (all the way to the left).
- Check both the "Limit motor diameter to mount diameter" and "Limit motor length to mount length" boxes.
- Look for the **A8** motor in list on the left side of the window. Click on it.
- In the *Ejection charge delay* field at the top left, enter the value **3**.

Now click the :guilabel:`OK` button at the lower right corner of the window.

If everything went to plan, you have successfully added the **Estes A8-3** motor to your rocket. Now repeat these steps
for the following motors, using a *New Configuration* for each new motor you add:

- **Estes B6-4**
- **Estes C6-3**
- **Estes C6-5**
- **Estes C6-7**

After you have added the rest of the motors, click back on the :guilabel:`Rocket design` tab just under the main menu.

7. The next component we will add is the **Engine Block**. This time make sure that the *Inner tube* is highlighted and
then add an engine block component. Change the *Inner diameter* to **1.2** and the *Wall thickness* to **0.3**. Also,
change the *Position relative to:* **Top of the parent component**. The last change we will make is to increase *plus*
to **0.2**. That is all we have to do for this component.

Your rocket should now look like this:

.. figure:: /img/user_guide/basic_rocket_design/EngineBlockCompleted.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added the Engine Block

   Added the Engine Block

8. We will now add two **Centering Rings**. Make sure the *Body tube* is highlighted in the list of components under
   *Rocket design*, then click on the :guilabel:`Centering ring` component type. The first one is fine as-is, so we will not make
   any changes. However, we will add the following line under the :guilabel:`Comment` tab: **The centering ring automatically takes
   the outer diameter of the body tube and the inner diameter of the inner tube.**

Now click :guilabel:`Close`, select the body tube again and add another *Centering ring*. This time all we have to do is to change
*plus* to **-4.5**. Also, add the same comment as for the first ring.

.. figure:: /img/user_guide/basic_rocket_design/CenteringRings.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added Centering Rings

   Added Centering Rings

9. The next component we will add is the **Shock cord**. As usual, make sure the *Body tube* is selected before adding
the shock cord component. After it has been added change the *Plus* value to **2**, *Packed length* to **5.2**, and
*Packed diameter* to **1.2**. Again, we will add a comment to this component. Enter the following line to the *Comment*
section: **The shock cord does not need to be attached to anything in particular, as it functions only as a mass component.**.
Click the :guilabel:`Close` button to close the window, as the shock cord has now been completed.

.. figure:: /img/user_guide/basic_rocket_design/ShockCordComplete.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added Shock Cord

   Added Shock Cord

10. Now we need to add the **Parachute**. With the body tube highlighted, add a parachute component. Change *Plus* to
    **3.2**, *Packed length* to **4.2** and *Packed diameter* to **1.8**. That is everything we need to do to the parachute.
    Click :guilabel:`Close` to close the window. You can see what your rocket should now look like below.

.. figure:: /img/user_guide/basic_rocket_design/ParachuteAdded.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added Parachute

   Added Parachute

11. The second-to-last component to add is a **Mass Component**. Go ahead and add one to the body tube now. Adjust the
    *Mass* to **2** grams (g), the *Approximate density* to **0.16** g/cm^3, the *length* to **3.0**, the *diameter* to
    **2.3** and finally the *Plus* value to **8.0**. It may be easier to enter these manually. The last thing you have
    to do is to rename it from ‘Unspecified’ to **Wadding**. Leave everything else as it is and click the :guilabel:`Close` button.

.. figure:: /img/user_guide/basic_rocket_design/AddedMass.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added Mass

   Added Mass

12. Almost done! One final component left to add: the **Launch lug**. Add this to the body tube now. Change the *Length*
    to **3.5**, the *Outer diameter* to **0.7**, and the *inner diameter* to **0.5**. Leave the *Thickness* as it is.
    Also, change the *Radial position* to **19** degrees. Click :guilabel:`Close`.

.. figure:: /img/user_guide/basic_rocket_design/LaunchLugAdded.png
   :width: 90%
   :align: center
   :figclass: or-image-border
   :alt: Added Launch Lug

   Added Launch Lug

And that’s all there is to it. You have just completed building your first rocket within OpenRocket! From here you can
use what you know to create more rockets, or you can proceed to the next section of the User Guide:
:doc:`Basic Flight Simulation </user_guide/basic_flight_simulation>`. Have fun!

