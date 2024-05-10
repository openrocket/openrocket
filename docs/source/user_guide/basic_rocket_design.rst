Basic Rocket Design
===================

In this section we'll look at how to design a basic rocket by examining the A simple model rocket design example.
After reading this section you should have an understanding of how to start designing your own rockets.

We will start with a brief discussion on the selection of **available components**, and then the components used in
the ``A simple model rocket`` example design file (``File`` -> ``Open example`` -> ``A simple model rocket``).
Then we'll build a rocket from scratch to see how it’s done.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none


Component Configuration Window
------------------------------

There are several types of components available to you as a rocket designer, and you can either customize them to meet
your needs, or simply load component presets from the parts library.

To start, let's begin a new project: ``File`` -> ``New``. It doesn’t matter what we call it at the moment, but go ahead
and save the design straight away: ``File`` -> ``Save``. At this point you should be presented with a blank rocket design window:

.. figure:: /img/user_guide/basic_rocket_design/main_window.png
   :align: center
   :width: 70%
   :alt: The OpenRocket rocket design window.

   The OpenRocket rocket design window.

When you first start a new rocket design you will see that there are four categories of components available in the **Add new component** panel
on the top-right of the rocket design window:

* ``Assembly Components``: These are components that have **no physical meaning** of their own but are used to **group components together**.

* ``Body Components and Fin Sets``: These are components that are used to **build the rocket's airframe**.

* ``Inner Components``: These are components that are placed **inside the rocket's airframe**.

* ``Mass Components``: These are components that are used to **add mass** to the rocket or serve as **recovery devices**.

We would usually start building our rocket by selecting a nose cone but for the sake of this guide click the
``Body tube`` icon in the ``Body Components and Fin Sets`` section. This will then open up a configuration window
to edit the body tube parameters.

The Body Tube’ Configuration Window
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

At this point you should see a new window titled ``Body tube configuration``.

.. figure:: /img/user_guide/basic_rocket_design/body_tube_config.png
   :align: center
   :width: 60%
   :alt: The Body Tube configuration window.

   The Body Tube configuration window.

This window allows you to either select a preset for the component type you are choosing, or make your own. Let’s start by examining how to customize it ourselves. If you have a look at the Body tube configuration window you will see that at the top there is a Component name field. Here we can change the name of the current component to anything we choose. (Note that this name will then appear in the design tree seen in the Rocket Design panel of the main program window.)

Just below Component name there are several tabs:




