****************************
Overrides and Surface Finish
****************************

This page explains how to use overrides and surface finish settings to create more accurate OpenRocket simulations.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Using Overrides and Surface Finish to Create More Accurate OpenRocket Simulations
=================================================================================

As you develop your design, OpenRocket estimates the rocket's stability and performance characteristics based on entered
and known information about the components you choose. As you accumulate parts, and build and test your rocket, you will
obtain more accurate information than what OpenRocket estimated. With this knowledge, you can override the initial
estimates and create more accurate flight simulations.

On this page, how OpenRocket looks at the different overrides will be explained in more detail, and practical examples
of their use will be given.

Overrides
---------

OpenRocket allows you to override three values that are determinative of a rocket’s flight characteristics: mass, center
of gravity (CG), and coefficient of drag (C\ :sub:`D`). All three of these values can be overridden at the stage level,
the component group level, or the single component level. In addition, while not being an override in its own right,
OpenRocket allows you to adjust the surface roughness of components so that drag estimates more accurately reflect
actual flight data.

Override Mass
~~~~~~~~~~~~~

The database of parts included with OpenRocket has estimates for component masses. These estimates come from a variety
of sources, ranging from data provided by the manufacturer and measured values to calculations based on the volume of
the part and the "standard" density of the material it is made from. The accuracy of these sources varies, but nothing
is more accurate than putting the actual part on a scale and weighing it. Once a part's measured mass is known, the mass
override can be used to change the mass value, more closely reflecting the part's mass, and thereby the rocket's actual
mass.

Override Center of Gravity
~~~~~~~~~~~~~~~~~~~~~~~~~~

OpenRocket also estimates each component's center of gravity based on its materials and geometry, and calculates the
overall center of gravity of each stage, and the entire rocket based on that. However, this really can't account for
the weight of things like glue and paint, which can be significant and can move the center of gravity forward or back.
Once you've got some components assembled, you can measure the CG (using any of the time-honored methods, like hanging
from a string or balancing on a knife edge) and override this, too.

Override Coefficient of Drag
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The drag on a rocket in flight is a function of the rocket's velocity, the density of the atmosphere, the frontal area
of the rocket, and a number called the coefficient of drag. This, in turn, is a result of the airflow over the rocket,
and is estimated based on the rocket's geometry and surface finish. Once you've actually flown the rocket and compared
your simulation to the actual flight, you can adjust the rocket's coefficient of drag to better match the flight data.

One caution, the coefficient of drag does not remain constant through the flight, especially if the rocket gets near the
speed of sound. At low speeds the friction on the rocket's body dominates the coefficient of drag, but as the speed
increases the effect of the pressure drag (the drag caused by the pressure of the front of the rocket hitting air) and
the base drag (caused by the low pressure area left behind the rocket) becomes progressively more significant. Adjusting
the coefficient of drag based on a flight with a small motor, and using this result to estimate behavior with a larger
motor can give misleading results. How to use drag coefficient overrides to "adjust" a calculated drag coefficient will
be discussed later.

Override for All Subcomponents
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

OpenRocket designs are structured as a tree: each component (except the rocket itself) has a parent component and may
have children (subcomponents). For instance, a single stage rocket will have that single stage as the child of the rocket,
and the stage will have (at least) a nose cone and a body tube as subcomponents. If fins are attached to the body tube,
the fin set is a subcomponent of the body tube. When applying overrides to a component, you can specify whether to apply
the override to all of its subcomponents as well. If you don't override the subcomponents, then the override applied to
the parent component is combined with that of all subcomponents. For instance, suppose a body tube has a calculated mass
of 100 grams and its fins have a calculated mass of 50 grams, the combination of body tube and fin set will have a mass
of 150 grams. If you override the body tube mass, set it to 110 grams, and don't override the subcomponents, the total
mass is now 160 grams.

If you do override the subcomponents, then the set mass of the parent replaces the mass of the entire subtree. So, using
the same body tube and fin set as before, if you set the mass override of the body tube to 170 grams, and override the
subcomponents, the total mass is 170 grams; the fins contribute no additional mass to the assembly.

But, why would you want to do this? Ultimately, the goal is to match the simulated mass to the measured mass after the
rocket is finished, taking into account such things as adhesives and any exterior finish (paint and decals). In the case
described above, the mass override is for the entire assembly -- the idea is that if you actually glue the fins to the
body tube and weigh the result at 170 grams, that's what you want the finished mass of the assembly to be.

Using Overrides to Produce a More Accurate Simulation
-----------------------------------------------------

Let's look at how you can apply overrides at different points in the design, construction, and simulation of the rocket
to improve the accuracy of your simulations.

When Designing Your Rocket
~~~~~~~~~~~~~~~~~~~~~~~~~~

You start designing your rocket by selecting different components, parts that have associated mass values and calculated
values for the center of gravity and coefficient of drag. As you add parts, OpenRocket continuously updates to combined
values, calculating an estimated mass, center of gravity, and coefficient of drag for the entire rocket. Then, based on
your mass and performance goals, you select a motor. When you have, it is time to make sure the center of gravity is
forward of the center of pressure by a comfortable stability margin; assuming it is, you can order your parts and start
putting the rocket together.

When You Have Your Parts
~~~~~~~~~~~~~~~~~~~~~~~~

Once you have your parts -- the nose cone, body tube, fin stock, and whatever recovery devices and other hardware you're
using -- you can weigh each component separately. You're likely to find that the parts weigh something close to the
calculated values, but are not exactly right. So, this (the individual part) is the lowest level on which you can
override a mass. After overriding the mass of each part, you can check again to be certain that the center of gravity
is still forward of the center of pressure by a comfortable stability margin, and modify your design as needed.

As You Build Your Rocket
~~~~~~~~~~~~~~~~~~~~~~~~

When assembling your rocket, as you complete subassemblies, you can weigh these subassemblies and measure their centers
of gravity, applying mass and center of gravity overrides at the second (subassembly) level. When applying an override
to a subassembly, you want to check the "Override for all Subcomponents" so that you don't count a subcomponent within
the assembly twice.

Finally, when your rocket is complete, you can again weigh and measure the center of gravity of the entire rocket, and
apply these overrides at the highest (stage) level. Once again, at this level you'll want to override subcomponents.

**Note: you should perform this override on every rocket you ever create. This gives the most accurate possible starting
point for your simulations.**

After Your First Test Flight
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Once you've flown your rocket for the first time, you can compare your simulations to your actual flight data. Since
each of your simulations was based on overridden measured mass and center of gravity values, the only thing you need to
adjust at this point is drag. You can take two approaches to this, you can adjust the friction drag by changing the
rocket's surface finish, or you can directly modify the drag coefficient. The process is much the same in both cases.

To change the surface finish, you select external components (parts of the rocket's actual airframe) and simply select
a different surface finish. Then, rerun the simulation again, and compare the new difference between the actual flight
results and your new simulation; you continue to adjust the surface finish until you are satisfied that the simulation
results are as close as possible to the actual flight data. Even though only the airframe surface matters, for the first
few adjustments, it is easier to select a new component finish on any part and then simply click the "Apply to All Components"
button. After you have bracketed your desired simulation result, you can fine-tune the simulation by adjusting the surface
finish for individual components.

To directly adjust the drag coefficient, you apply a drag coefficient override to the entire stage. In this case you
don't want to override the subcomponents; what you're doing is applying a constant offset to the drag coefficient
being calculated over the course of the flight.

After applying these modifications, you will have a more accurate simulation for future flights with different motors.

----

How and Why to Use Mass and Center of Gravity Overrides
=======================================================

Background
----------

The ‘’Stage’‘, ‘’Boosters’‘, and ‘’Pods’‘ assembly components feature mass and center of gravity override options that
may be used to adjust the rocket’s margin of stability. Prior to the release of OpenRocket 22.02, the use of these
options overrode the mass and center of gravity of **all** of the assembly subcomponents, limiting the use of these
options to matching the rocket’s finished mass and center of gravity.

Beginning with the release of OpenRocket 22.02, when using these options, you may choose not to override the mass
and center of gravity of the assembly subcomponents, choosing instead to add to or subtract from the values calculated
by OpenRocket. So, why is this an important change?

.. _margin_of_stability:

Margin of Stability
~~~~~~~~~~~~~~~~~~~

The recommended **stability margin for subsonic flights is not less than 1.0 caliber**, and **not less than 2.0 calibers
for transonic and supersonic flights**. However, the rocket’s stability margin changes during flight; the rocket can have
momentary marginal stability right off the launch rail with wind, or at high angles of attack at low velocity going
through apogee, but may otherwise be unstable during flight.

Effect of Rotational Inertia
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The cause of instability during flight is often rooted in the rocket’s rotational inertia (moment of inertia), how the
rocket’s mass is distribution about the axis (center of gravity), with larger moments requiring more torque to change
the rocket’s rate of rotation. When subcomponent mass and center of gravity values are overridden, the rocket’s rotational
inertia is changed, potentially causing the rocket to simulate as stable when it is not, or as unstable when it is, at
less than or near recommended stability margins.

Benefit of Not Overriding Subassembly Subcomponents
---------------------------------------------------

The rocket’s center of gravity can be viewed as the balancing point between the nose cone tip and the end of the motor
nozzle. If a large, heavy motor is used, then weight at the rocket’s tip is needed to balance that weight. And, this
creates a large rotational inertia, which may be significantly changed when the user overrides the subcomponent mass and
center of gravity values. Now, you can add mass at a specific location within the assembly component, without changing
the mass and center of gravity values of subcomponents.

How the Mass and Center of Gravity Overrides Work
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you select "override for all subcomponents", your value is set as an aggregate Mass or Center of Gravity value. If
you don't select it, your set value is added to the calculated value of the Mass or Center of Gravity. The Coefficient of
Drag override work in the same way.

Matching Measured Mass and Center of Gravity
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In other words, it is now possible to reduce the effect that matching measured mass and center of gravity has on the
rocket’s calculated rotational inertia. The user simply checks the assembly component’s mass and center of gravity
overrides, entering the difference between the measured and calculated values, leaving the override subcomponents box
unchecked. This method is less likely to have a material effect on the rocket’s rotational inertia than overriding all
the subcomponent values.

Adjustable Weight Systems
~~~~~~~~~~~~~~~~~~~~~~~~~

Using the assembly component’s mass and center of gravity overrides in this way, you can mimic adjustable weight systems
to compensate for significant changes in motor size and mass. The number of steps involved depends on the precision you
want achieve.

The approaches described below assume that you have a completed design that you will be making changes to; The steps
below describe how to make the changes to the "A simple model rocket" example include with OpenRocket.

Simplified Approach
~~~~~~~~~~~~~~~~~~~

The simplified approach has three basic steps.

The first step is to place a *Mass Component* inside the airframe, to match the rocket’s **flight ready** measured weight
and center of gravity (without a motor); it doesn't matter which component it is put inside of because you should position
it relative to the "Tip of the nose cone", entering your measured distance from the rocket's nose cone tip to the rocket's
actual center of gravity.

The second step, going to the *Stage configuration* pane, is to enter the distance from the tip of the nose cone to the
center of your adjustable weight as the *Override center of gravity* distance.

The third step is really two companion steps, you select your preferred motor configuration, and, on the
*Stage configuration* pane, you adjust the mass override value until you have a comfortable margin of stability between
the center of gravity and center of pressure (see, :ref:`Margin of Stability <margin_of_stability>`). Then, last of all, **don't forget to actually
add that amount of weight to your rocket's adjustable weight system**.

After that, before you head out to the range, actually install the motor (without the igniter) and verify the margin of
stability; it's always nice to put markers on the rocket, blue for the center of gravity and red for the center of pressure.

Precision Approach
~~~~~~~~~~~~~~~~~~

The highest level of precision requires that the mass overrides be used for **each component** and **each subassembly**,
and, finally, at the **stage** level. To achieve this precision, there are a few additional steps:

1. Lay out, weigh, and enter the mass override for each of the parts.
2. Using the component tree as your reference, identify each component that has one or more subcomponents under it.
   These components will be referred to as parents and the component and its subcomponents, together, will be called a
   subassembly; oft times there are nested subcomponents and each component with a subcomponent is a parent.
3. While building the rocket, each time a subassembly is completed, weigh that subassembly and enter the mass override
   value on the parent's configuration override tab, checking the "Override mass and CG of all subcomponents" box. This will
   take into account the additional mass of adhesives, fasteners, and the like.

From here, follow the `Simplified Approach`_ steps, above, which describes how to make the remaining changes.

As stated before, before you head out to the range, **actually install the motor (without the igniter) and *verify the
margin of stability***; again, it's always nice to put markers on the rocket, blue for the center of gravity and red for
the center of pressure.

----

How and Why to Use Surface Finish Settings and Coefficient of Drag (C\ :sub:`D`) Overrides
==========================================================================================

When you have finished your rocket, and adjusted the mass and center of gravity overrides as described above, there are
two remaining factors affecting flight performance that can be adjusted, the rocket's coefficient of drag and surface
finish (roughness). After you have flown your rocket and collected flight data (using an altimeter or other device),
you can compare the actual flight results to the OpenRocket simulation projections and make changes to the rocket's
(C\ :sub:`D`) or surface finish to bring the simulated results in line with the actual collected fight data.

The first step is usually bracketing the desire drag coefficient by changing the rocket's *Surface Finish*, and the
second is fine tuning the (C\ :sub:`D`) with the *Stage* level override. You can adjust the rocket's (C\ :sub:`D`) by
changing its surface roughness or by overriding its overall (C\ :sub:`D`), or a combination of both, depending upon the
precision you desire.

Using Surface Finish (Roughness) Settings
-----------------------------------------

The surface finish that you use for your rocket affects how air flows over the airframe (the smoother the surface, the
less the resistance; the rougher the surface, the greater the resistance). And, another word for this resistance is drag.
So, an adjustment to surface roughness changes the rocket's (C\ :sub:`D`).

As with other component specific characteristics, each component has its own *Surface Finish* setting, although you can
change the *Surface Finish* of every component by left-clicking the *Set for all* button on any *Appearance* tab. This
is similar to the override subcomponents feature, but only changes the components that exist at the time it us used;
parts added after using the *Set for all* feature will have the default roughness. OpenRocket allows you to select one
of five roughness settings, *Rough*, *Unfinished*, *Regular paint*, *Smooth paint*, and *Polished*, each with a
decreasing (C\ :sub:`D`) from rough to polished.

Using your actual collected flight data, by making the exterior of your rocket's surface rougher or smoother, you are
changing the rocket's overall (C\ :sub:`D`) without overriding the (C\ :sub:`D`) of any components. This method retains
all of the component (C\ :sub:`D`) values and does not affect the simulation's ability to calculate the natural
(C\ :sub:`D`) variations caused by changes in velocity and atmospheric density during flight.

When making changes using this method, a change is made to the *Surface Finish* setting, the simulation is rerun, and
the results compared. You then repeat this until you have bracketed the comparison between the actual collected flight
results and the simulation results. Then, out of the two bracketing simulations, set the *Surface Finish* to the setting
used in the simulation with the lowest results. If you want even more precision, adjust the Rocket's (C\ :sub:`D`) as
described below.

Using Coefficient of Drag (C\ :sub:`D`) Overrides
-------------------------------------------------

How the Stage Coefficient of Drag Override Works
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you select "override for all subcomponents", your value is set as an aggregate (C\ :sub:`D`) value. If you don't
select it, your set value is added to the calculated value. This is consistent with how the mass and CG overrides work.

Using the Stage Coefficient of Drag Override
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The coefficient of drag relates to a thing's resistance to airflow, and changes with velocity and the density of the
atmosphere, especially if the rocket gets near the speed of sound. Only the rocket's exterior components that comprise
the airframe of the rocket are exposed to the flow of air over the rocket and have a (C\ :sub:`D`) value that affects
flight performance. And adjusting the coefficient of drag for the entire rocket based on a flight with a small motor,
then using those results to estimate behavior with a larger motor can give misleading results. Because of this, and the
practical impossibility for most users to measure the drag of each component, or the finished rocket for that matter,
*overriding the (*C\ :sub:`D`*) at the component and subassembly levels is* **NOT recommended**.

However, armed with actual collected flight data, you can add or subtract from the rocket's overall (C\ :sub:`D`) at the
"Stage" level by NOT overriding the (C\ :sub:`D`) of subcomponents. Practically speaking, this method retains all of the
individual component (C\ :sub:`D`) values, but nudges the (C\ :sub:`D`) of the entire rocket just a bit more. And,
OpenRocket uses this nudge to more closely match actual flight and simulation results, *without affecting the simulation's
ability to calculate the natural (*C\ :sub:`D`*) variations caused by changes in velocity and atmospheric density during
flight*.

When making changes using this method, a change is made to the (C\ :sub:`D`), the simulation is rerun, and the results
compared. You then repeat this until you are satisfied with the comparison between the actual collected flight results
and the simulation results.
