**********************
Advanced Rocket Design
**********************

In this section, advanced design principles and concepts are discussed, with step-by-step instructions describing how to
incorporate these techniques into designs created in OpenRocket. Implementing the techniques described in this section
may require specialized materials and electronic devices intended for use only by experienced rocketeers.

Advanced rocket design encompasses configurations for high power rockets generally, including:

- Recovery systems
- Through-the-wall fin construction
- Electronic and dual deployment
- Complex multi-staging (such as motor racking) and motor clustering
- Roll stabilization

Additionally, you may find that using mass and center of gravity (CG) overrides will improve OpenRocket flight and
recovery simulation accuracy.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Recovery Systems
================

Recovery Techniques
-------------------

Recovery systems are intended to return a rocket safely to the ground, without harm to people or damage to the rocket or
other objects. Though recovery mechanisms vary greatly, recovery systems generally include elements of one or more of
these techniques:

- Featherweight
- Break-apart
- Streamer
- Parachute
- Helicopter
- Gliding

Featherweight and Break-Apart Recovery
--------------------------------------

Featherweight and break-apart recovery work by creating enough drag to ensure that the terminal velocity of the rocket
is so low that it won't be damaged or do damage when it hits the ground. Featherweight designs are often minimum diameter
rockets that eject the burned-out motor casing altogether, or use the ejection charge to shift the casing position
rearward after motor burnout (within an extended motor hook), to induce instability and cause the rocket to tumble.
Break-apart recovery, aerodynamically, does the same thing, increasing drag and inducing instability by breaking the
rocket into two or more sections connected together by a shock cord. Typically, a featherweight rocket, and each section
of a break-apart rocket weighs less than one ounce.

.. figure:: /img/user_guide/advanced_rocket_design/Featherweight.png
   :width: 245 px
   :align: left
   :figclass: or-image-border
   :alt: The Estes Astron Streak, ca 1970 - a *featherweight recovery* rocket

Example Featherweight Design
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In earlier years, Estes sold a few rockets that featured *Featherweight Recovery*, meaning that a very light model could
spit out its used motor casing, and land directly on the ground with no damage, rather like a badminton shuttlecock.

Ejecting burned-out motor casings is not allowed in :abbr:`NAR (National Association of Rocketry)` contests unless a streamer or parachute is attached to the
ejected casing.

.. figure:: /img/user_guide/advanced_rocket_design/Unicon.png
   :width: 200 px
   :align: right
   :figclass: or-image-border
   :alt: The scratch Unicon, ca 1970 - a *break-apart recovery* rocket

Example Break-Apart Design
^^^^^^^^^^^^^^^^^^^^^^^^^^

Another approach to bringing a lightweight rocket safely to earth is to break the airframe into several aerodynamically
unstable pieces, and "flutter" them back to earth.

One such design: the `Unicon <https://archive.org/details/american-aircraft-modeler-02-1970/page/n41/mode/2up>`__
(for *"Unified/Consolidated"* - a stretch, I know) appeared as a plan in American Aircraft Modeler in February, 1970.
The body tube featured 3 pieces of launch lug, as did the nose cone. Into these lugs plugged one of 3 balsa sticks, each
of which held a fin, and a piece of body tube attached to the fin root and the stick, to provide a stable anchor against
the body. The entire assembly was connected with pieces of heavy-duty thread.

When the ejection charge fired, the nose cone popped off, releasing the sticks with fins, which, tied to the body tube
and nose cone, fluttered in the air, and slowed descent of the main airframe as it landed.

This author built one, and it worked pretty well.

Streamer and Parachute Recovery
-------------------------------

Streamers and parachutes add drag to slow the rocket descent rate. Generally, a larger streamer is always better. But,
streamer size is an example of the principle of diminishing marginal returns, eventually making a streamer larger will
only slightly increase drag (a rocket weighing more than 10 oz is beyond the effective use of a streamer). On the other
hand, because of their efficiency, parachutes create more drag with less cloth than any other method, and virtually all
high power rockets use parachutes.

Example Streamer Design
^^^^^^^^^^^^^^^^^^^^^^^

A method of recovery favored on windier days is to attach a flame-retardant streamer to the shock cord, in place of a chute.
Using OpenRocket you can simulate streamer recovery by equipping your rocket design with a streamer from the **Mass Objects** section.

A streamer flaps in the wind as the rocket falls, losing altitude faster than an equivalent volume of packed parachute.
Because it comes in faster, it won't drift as much in the wind. The rocket will also hit harder, potentially risking damage.
OpenRocket can help you estimate how fast your model will land.

By using "snap swivels" - small brass clips usually found in the fishing tackle aisle - you can prepare both a parachute
and a streamer for a rocket, choose your method of recovery at the field, and clip it onto the shock cord before you launch.

:abbr:`NAR (National Association of Rocketry)` requires 10 cm :sup:`2` of streamer area per gram of mass in contest models.

Example Parachute Design
^^^^^^^^^^^^^^^^^^^^^^^^

Parachute recovery is probably the most familiar model rocket recovery mechanism. Most of the beginner kits start with
parachutes, but even high-power, edge-of-space-kissing, multi-stage, electronic deployment rockets use parachutes to
slow descent. They're basic to the hobby.

OpenRocket gives you a number of simulation options for parachutes, including material, construction, size, number of
shroud lines, packed size and more. With OpenRocket, you can set your parachute's deployment to work just like your
real rocket's.

One thing that you're not able to directly simulate here is the *type* of 'chute you have. Parachutes come in different
types, from the semi-ellipsoid proper Parachute - an efficient shape (by drag to weight) which cannot be laid perfectly
flat, to the "parasheet" - a 'chute that can be formed from a flat piece of material (the typical model rocket kit
contains a parasheet), to X-shaped parachutes, to 'chutes with spill-holes, to parafoils and Rogallo wings. You'll have
to experiment with these chutes, and perhaps try and adjust the Drag coefficient to compensate for difference from
OpenRocket's ideal parachute.

:abbr:`NAR (National Association of Rocketry)` requires 5 cm :sup:`2` of parachute area per gram of mass in contest models.

In 2022, Apogee released a "Gliding Parachute", which could be steered by remote control back to the launch pad (given
enough altitude and favorable winds). OpenRocket cannot, at this writing, simulate the behavior of the Gliding Parachute.

Helicopter and Gliding Recovery
-------------------------------

Helicopter Recovery
^^^^^^^^^^^^^^^^^^^^

Helicopter recovery relies upon rigid lift-generating blades and auto-rotation to slow terminal velocity. This design
technique is the most complicated of all, and requires that the entire rocket be designed around the recovery device.
As important, the stresses generated by rapidly spinning blades hitting the ground effectively limits the use of this
technique to low mass (model) rockets.

Gliding Recovery
^^^^^^^^^^^^^^^^

A glider uses aerodynamic lift to control terminal velocity. However, because the aerodynamic requirements of vertical
flight are vastly different than gliding flight, to make this transition there must be a shift in the center of gravity
or the center of pressure. This transition can be made by reducing mass (ejecting the motor mount tube and weights) or
changing aerodynamic signature (ejection activated fin-elevators or swing-wings). Radio and other control systems are
currently being used to fly gliding recovery rockets, even high power.

Protecting Recovery Components
-------------------------------

Recovery components are made from lightweight materials which, while often flame retardant, aren't necessarily heat-proof,
or which may char and decay without bursting into flame.

To protect the recovery components and ensure they work properly for a safe landing, some method of shielding the
"Laundry" (slang for the parachute and associated cords) from the heat of ejection, or of cooling the eject gasses must
be used.

Heat Shields
------------

Protecting the components starts during rocket design: you can choose something durable and flame-resistant like Kevlar
cord for shock cord components that will be used in the eject area. A little later in this article, you'll see other
built-in options you can use.

Fire Resistant Wadding and Blankets
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The simplest way to protect the 'chute is to put something flameproof between the eject charge and the recovery hardware.
In small rockets, this can take the form of "Flameproof ejection wadding", as packaged with Estes motors, or flame-retardant
recycled cellulose insulation (nicknamed "Dog Barf" in the hobby community). Of the two, "Dog Barf" makes much less of a
mess on the field, and is recommended by some clubs. If you do use "Estes Flameproof ejection wadding", please try and
recover as much of the discarded paper as possible before leaving your launch site.

When using wadding, try and put in a minimum depth of 1.5 tube diameters of wadding. Wadding may be poked gently into a
rocket with a pencil or stick, but don't pack it down.

You can also buy a reusable 'chute protector, called a "Nomex blanket" or a "'Chute bag" (also made of Nomex). Nomex
blankets are typically square, often orange, and usually have a buttonhole sewn into the corner, to pass the shock cord
through. One wraps up the parachute and as much of the shock cord as is practical with the Nomex facing the eject charge.
This author was instructed to wrap the 'chute in Nomex "Like a burrito", but in reality there are several ways to pack a
chute protector that will work well. Ensure that the Nomex faces the eject charge, so it takes the heat of ejection, and
not your recovery device.

Nomex can be re-packed with the 'chute immediately after a flight. Nomex is machine-washable (and you'll probably want
to wash it at some point).

**A rule of thumb for sizing a Nomex blanket**: *The blanket should be a square, with a width that's 3 times the body diameter.*

Piston Ejection
^^^^^^^^^^^^^^^^

Another way of insulating the recovery material from the heat of ejection is with a Piston Ejection system.

In a typical piston ejection system, a piston is inserted into the body tube, and is free to slide up and down the tube's
length. The eject charge is on one side of the piston, and the recovery material on the other. The body tube, the piston
and the recovery material are all connected together, so as not to lose any parts.

At ejection charge firing, expanding gases push the piston (and the recovery material) up the tube and out of the rocket,
without exposing the recovery material to the heat of the eject charge. The piston should leave the rocket body, in order
to vent the ejection gases.

Pistons are often made of tube couplers, which have been sanded down a bit to smoothly slide in the body tube. One end of
the coupler is closed by a bulkhead. The closed end is called the "face" of the piston. The rounded wall of the coupler
is called the piston's "skirt".

The attachment shock cord runs from the eject charge end of the rocket, attaches to the piston at the face (or is threaded
through it and sealed) and more shock cord runs from the other side of the piston face and to the recovery material.
The attachment cord needs to be long enough for the piston to escape the body tube so exhaust gases are vented. The
piston *must* move smoothly and without sticking; if the piston sticks, the parachute may not be deployed.

Opinions differ on whether the "face" of the piston should face the 'chute or the ejection charge. According to one theory,
if the piston face is on the nose cone side of the piston, exhaust gases could make the piston skirt swell and cause the
piston to stick in the body tube, while if instead the piston faces the eject charge, eject gases that travel between the
piston skirt and the inside of the body tube form a "gaseous lubricant" which should prevent the piston from getting stuck.
Others beg to differ, and have had successful real world experience with the piston facing upward.

Ejection Gas Cooling
^^^^^^^^^^^^^^^^^^^^

Another approach to protecting the recovery material is to cool the ejection gases before they contact the 'chute.

Cooling Mesh
^^^^^^^^^^^^

Aerotech sells a metal cooling mesh for model rockets. The mesh looks like a tiny tangled slinky, or perhaps like twisted
tinsel from a Christmas tree. Installing a metal cooling mesh in the rocket body allows cooling of the exhaust gases,
which transfer much of their heat to the metal mesh as they pass. The configuration of the mesh also makes it something
of a particle filter, so chunks of burning material from the ejection charge get filtered out, instead of passing their
heat to your parachute.

Baffles
^^^^^^^

Still another approach is to install a baffle in the rocket, above the eject charge, and below the recovery system. A
baffle is often made from a coupler with two bulkheads, one in each end. Designs differ, but basically there's a hole
pattern in the top, and a hole pattern in the bottom, such that ejection gas will pass through, but because the holes
don't align, it will need to make a detour through the baffle. Meanwhile, heavier burning solid material from the eject
charge has much higher inertia, and won't be able to divert to the top set of holes. Much of it will be stopped by the
top bulkhead.

Servicing
^^^^^^^^^

Both baffles and cooling mesh will have limited lifespans, and need to be cleaned, serviced, or replaced. Cooling mesh in
particular can become clogged with particles from many flights, and may be placed in a difficult-to-reach position.
Baffles may burn, break, or get filled with particles. When this happens, the best service option may be to "poke out"
the cooling mesh or baffle, and go over to recovery wadding or a Nomex blanket.

One way to avert the "poke out" problem is to use screws to attach a baffle through the wall of the body tube. Nylon
screws may be used to avoid placing "ductile metal" in the airframe. Screw attachment allows the baffle to be removed
for servicing or replacement.

CO\ :sub:`2`\  Ejection Devices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Another approach to eject is pioneered by `Tinder Rocketry <https://www.tinderrocketry.com/>`__, who offer a CO\ :sub:`2`\
ejection system. Because a minimal pyro device is used to trigger the CO\ :sub:`2`\  ejection, there's not a lot of hot
material flying around inside the airframe, and no need for wadding or a Nomex blanket. The CO\ :sub:`2`\  is cold as it's released.

The CO\ :sub:`2`\  ejection system is claimed to operate more reliably than black powder at extreme altitude
(above 20,000 feet ASL), due to black powder's incomplete or non-existent burn at these altitudes.

----

Tube Fins and Ring Tails
========================

Tube Fins
---------

A tube fin is just that, a fin made using a shorter section of body tube, adhered to the main body tube, with or without
other flat fins. This type of rocket is easy enough to build, and OpenRocket helps you make an accurate simulation.

.. figure:: /img/user_guide/advanced_rocket_design/TubeFins.png
   :width: 250 px
   :align: right
   :figclass: or-image-border
   :alt: *A simple model rocket* - now with tube fins.

Let's convert *A Simple Model Rocket* from 3 flat fins, to 4 tube fins, just because we can.

1. From the :menuselection:`File` menu, choose :menuselection:`Open Example` and open **A simple model rocket**.
2. Left-click on the :guilabel:`Trapezoidal fin set` in the upper-left panel, and delete it.
3. Left-click on the :guilabel:`Body tube`.
4. Note that :guilabel:`Tube fins` is now enabled in :guilabel:`Body components and fin sets`. Click it.
5. The number of fins defaults to 6. These look a little long. Drag the :guilabel:`length` slider until the fins are about 7 cm long.
6. Drag the :guilabel:`plus` slider (under :guilabel:`Position relative to:``) to move the fins forward just a little.
7. Leave the **Tube fin set configuration** window open, and click on the :guilabel:`View Type` menu on the main window.
   Choose the :guilabel:`Back view`.
8. Notice that OpenRocket has defaulted to an exact solution for wrapping 6 tube fins around your rocket body tube.
   In the **Tube fin set configuration** window, drag the :guilabel:`Fin rotation` slider until they line up with the launch lug.
   (*Passing the launch rod through one of the tube fins to reach the launch lug is simpler than arranging the tube fins
   for the rod to pass between the tubes*)
9. Notice that you can adjust the Outer Diameter of the fins to create designs that are harder to build, or perhaps
   impossible to build due to overlaps between tube fins.
10. Click the :guilabel:`Automatic` check box beneath :guilabel:`Outer diameter`. The tubes conform once more to the body tube and touch
    each other. Because the contact surfaces are in the right place to adhere to each other, this is probably the easiest
    type of fin configuration to build.
11. Reduce the :guilabel:`Number of fins` to 5, then to 4. As long as :guilabel:`Automatic` is checked and the solution makes sense,
    the fins will wrap the body tube. At 4 fins, the tubes would have to dwarf the body tube to wrap it, and OpenRocket
    gives up on wrapping.
12. Re-adjust the :guilabel:`Fin rotation` slider to line the 4-fin set up with the launch lug, either within, or beside a tube fin.

Our fin conversion is complete, but before we leave the **Tube fin set configuration** window, note a few other details:

- You can select preset tube components by clicking the :guilabel:`Parts Library` button in the upper-right.
  *Note that these components represent what was available at the time this version of the OpenRocket component database was
  released - some may no longer be available*. Some details of the components will fill fields of the **Tube fin set
  configuration** window, but *Diameter* is not one of them - at least in OpenRocket 15.03.
- It's not possible to use the **Tube fins** component to create tube fins sliced at an angle, or to create semi-circular
  fin sections (tubes cut in half, lengthwise).
- Though you might imagine lots of cool tube fin scenarios, this Tube fin tool will require your tubes to be in side-to-side
  contact with a body tube.
- OpenRocket won't apply your specified color to the inside of the tube fins. They'll have the default color inside.

Ring Tails
----------

It's easy to visually add a ring tail fin to an OpenRocket model using a body tube, but there's one catch: the ring tail
must start at the exact aft-end of the body tube it surrounds, and the supporting fins must trail to support it. This is
because, unlike other non-external components in OpenRocket, external airframe parts follow a strict linear pattern:
Nose cone, then a body tube, then perhaps another, and perhaps another, etc. If you don't want the ring tail fin at the
end of the model, also visually simulate a ring tail virtually at any point up or down the airframe using an inner tube.
But, as with the body tube approach, there’s a catch, inner tubes are aerodynamically ignored during the simulation.

**If you like the "look" of ring tail models, *OpenRocket does that very, very well*. Just **BE AWARE** that
*OpenRocket WILL NOT accurately simulate ring tail fins* at this time.**

.. figure:: /img/user_guide/advanced_rocket_design/RingTail.png
   :width: 250 px
   :align: right
   :figclass: or-image-border
   :alt: *A simple model rocket* - with added Ring Tail.

So, if you want that ring tail "look," let's step through adding a ring tail to **A simple model rocket** to demonstrate
the body tube method.

1. From the :menuselection:`File` menu, choose :menuselection:`Open example...` and choose **A simple model rocket**.
2. Click the :guilabel:`Body tube` component to add a new body tube. *Note that it's added at the aft end of the main body tube,
   and is initially the same diameter as that tube*.
3. Increase the tube's :guilabel:`Outer diameter` to 8.6 cm, to let it just sit on the top fin. Yes, this will look strange for a moment or two.
4. Reduce the **Body tube length** to 2.5cm, so the back of the ring tail just touches the back of the fin points.
5. Set the appearance, if you'd like. Good choices for this model are White, and 50% :guilabel:`Shine`. *Note that since OpenRocket
   thinks this is a regular body tube, the inside of the ring tail won't receive your selected color.*
6. Close the Body tube configuration window, and switch the :guilabel:`View Type:` menu to :guilabel:`Back view`. You should see the ring
   tail surrounding and touching the fins.
7. You can look at the rocket in a **3D** View, or in **Photo studio** to see how it will look in the real world.

Your ring tail is complete.

In addition to not being able to accurately simulate this model, it's important to note that *the body tube ring tail
**will give you** a "Discontinuity in rocket body diameter" warning too*.

----

Through-the-Wall Fin Mounting
=============================

Model rocket fins are usually glued to the surface of an airframe. However, when higher thrust motors are used (E and
above) the increased thrust can literally rip fins off or shoot a motor up through the airframe. Instead,
"through-the-wall" (TTW) mounting refers to fins that protrude through a slot in the airframe and are glued to the motor
mount tube, one or more centering rings, and the airframe surrounding the slot. This construction technique significantly
strengthens fin joints and motor mounts.

There are three measurements necessary to create a fin tab: tab length, tab height, and tab position.

- **Tab length** is the distance from one side of the fin tab to the other. This is also the length of the slot that is
  cut through the airframe, the distance between the inside edges of the outermost centering rings.
- **Tab height** is the distance from outside of the airframe to the outside of the motor mount tube. This is calculated
  as follows: (BT OD - MMT OD) / 2, where BT is the airframe body tube and MMT is the motor mount tube diameters.
- **Tab position** is the distance from the root chord reference point to the fin tab reference point. OpenRocket features
  three choices:

  Relative to:

  - **the chord root leading edge** – the tab position is the distance from the fin chord root leading edge to the fin tab leading edge.
  - **the chord root midpoint** – the tab position is the distance from the fin chord root midpoint to the fin tab midpoint.
  - **the chord root trailing edge** – the tab position is the distance from the fin chord root trailing edge to the fin tab trailing edge.

OpenRocket will automatically calculate fin tab dimensions, within the following constraints:

- If there are no centering rings beneath a fin, the trailing edge of the fin tab is the fin chord trailing edge and the leading edge of the fin tab is the fin chord leading edge.
- If only one centering ring is beneath a fin, the trailing edge of the fin tab is the fin chord trailing edge and the leading edge of the fin tab is the trailing edge of the centering ring.
- If two centering rings are beneath a fin, the trailing edge of the fin tab is the leading edge of the trailing centering ring and the leading edge of the fin tab is the trailing edge of the leading centering ring.
- If more than two centering rings are beneath a fin, referring to the centering rings in order from the trailing edge to the leading edge of the fin chord, the trailing edge of the fin tab is the leading edge of the first centering ring and the fin tab leading edge is the trailing edge of the second centering ring. OpenRocket supports only one fin tab on each fin.

Converting a simple rocket to through-the-wall design:

1. At the OpenRocket **main window**, left-click the :menuselection:`File` menu, then left-click :menuselection:`Open example design`
   in the drop-down menu.
2. In the pop-up :menuselection:`Open example design` box, left-click the "*A simple model rocket*" selection, then left-click
   the :guilabel:`Open` button.
3. In the **Rocket design** view, double left-click the :guilabel:`Trapezoidal fin set` component.
4. Left-click the :guilabel:`Fin tabs` tab.
5. Left-click the :guilabel:`Calculate automatically` button.

And, a through-the-wall fin tab is automatically created between the two motor mount centering rings.

----

Electronic and Dual Deployment
==============================

.. todo::
      Add info on electronic and dual deployment.

----

Clustering and Multi-staging
============================

Complex rockets fall into two basic categories, a rocket that is propelled by a cluster of motors intended to be
simultaneously ignited or multi-staged (massively-staged), propelled by a series of motors that successively ignite the
next in line when the prior motor burns out.

.. figure:: /img/user_guide/advanced_rocket_design/xkcd_whatif_24_model_suborbital.png
   :width: 392 px
   :align: center
   :figclass: or-image-border

   From `xkcd 'what if' #24 <https://what-if.xkcd.com/24/>`__: *How many model rocket engines would it take to launch a
   real rocket to space?*, a 65,000 motor staged-and-clustered rocket. Recommended reading for all rocketeers.

Motor Clustering
----------------

Clustering refers to launching a rocket with more than one simultaneously-ignited rocket motor. Clustering is common in
"real" aerospace programs. Familiar American examples include: the `Gemini Titan <https://en.wikipedia.org/wiki/Titan_II_GLV>`__
\- a two-motor cluster, the `Saturn V <https://en.wikipedia.org/wiki/Saturn_V#S-IC_first_stage>`__ - a cluster of five
Rocketdyne F-1 motors driving the first stage, and the `Falcon 9 <https://en.wikipedia.org/wiki/Falcon_9#Launcher_versions>`__
\- a cluster of 9 Merlin motors driving the main stage.

In model and high-power rocketry, typical clusters seen are **2-motor**, always side-by-side, due to the geometry,
**3-motor**, in a triangle or straight line, **4-motor**, in a square, and **5-motor**, typically arranged with one
central motor surrounded by 4 in a square - though other arrangements are possible. There's nothing preventing much
larger ones, but 2, 3, 4 and 5 are most-often seen.

In three- and five-motor clusters, it's not uncommon to see a larger or higher-power central motor, surrounded by
smaller or weaker motors. This may be done for effect, or due to modeling constraints, or to more closely resemble its
full-scale inspiration, or possibly for reasons of cost. Clustered motors may be "canted" - that is, pointed to the
outside of the rocket fuselage's circumference, for effect, stability, or spin.

Designing a Rocket with Clustered Motors
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

OpenRocket makes it easy to design motor clusters. To begin with, add an **Inner Tube** to your aft-most **Body Tube**.
On the **Motor** tab, check the "This component is a motor mount" box. Set its inner diameter to one of the standard
motor sizes, unless you have a unique need: 13, 18, 24, 29, 38, 54, 75 or 98mm. Next, click on the **Cluster** tab.

.. figure:: /img/user_guide/advanced_rocket_design/ClusterTab.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: OpenRocket's **Cluster** tab

The **Cluster** tab lets you choose a common cluster configuration, and adjust it in your model. When you make an
**Inner Tube** a cluster, you treat every tube in the cluster identically with each addition. If you add an
**Engine block** or a **Mass component**, all of the tubes in the cluster will receive one.

First, pick a cluster configuration from the image tiles on the left side of the tab. Realize that depending upon the
sizes of your motor tube and body tube, not every cluster that you can make will fit.

Next, adjust the **Tube separation**. This value controls how close the clustered motors are to each other. A value of
1 places the tubes in contact with each other. You can enter decimals like "1.25" in the separation field. In addition
to potentially affecting your rocket's stability, the **Tube separation** you choose may influence the difficulty of
wiring your clustered motors for ignition, and your ability to place adhesive and parts around tightly-packed tubes
during construction.

.. figure:: /img/user_guide/advanced_rocket_design/ClusterAft.png
   :width: 800 px
   :align: center
   :figclass: or-image-border
   :alt: Clustered motor mounts, viewed from aft.

The **Rotation** setting rotates your cluster around the major axis of your rocket (the Up <--> Down one). It's used to
line up the motors with other decorative and structural components of your rocket. This alignment may be critical if
you're creating a design that ducts eject gasses from one part of the rocket to another.

The **Split cluster** button changes this component from a clustered motor component that can be handled as a unit, to
individual motor tubes, which may be positioned and edited independently of each other. Once you split the cluster,
items and settings you change for each tube will not automatically be added to the other tubes in the cluster. You may
want this option if you have motor mount tubes of different lengths or diameters in the cluster. *Once split, a cluster
cannot be recombined*. You must re-create the cluster as a unit if you'd like to revert to that approach.

Igniting a Cluster
^^^^^^^^^^^^^^^^^^

Important to the stability of the rocket's flight is that all the motors ignite more or less simultaneously. The initial
concerns here are that all the motors' igniters are wired to take a single application of voltage from the launch
controller, and that the controller be able to provide adequate voltage and current to ignite all the motors.

Estes Rockets used to advise that igniter wires be twisted together in either
`series or parallel configurations <https://en.wikipedia.org/wiki/Series_and_parallel_circuits>`__. Each has its advantages:
with a series connection, any burnt igniter will show an open circuit upon arming, while with a parallel connection, the
launch controller can use the same voltage as always, but supply more current to ignite multiple motors at once. Today,
most clusters are wired in parallel, and the rocketeer must ensure that ample current is available for launch.

Some cluster igniter wiring schemes use a **buss bar** - a short length of regular conductive wire, typically non-insulated,
for ease of connecting to it as needed - as a way of bridging what can be complex connections in a tight space, into an
easier connection plan. For example, you can twist one end of each igniter together in a bundle, and the other end of
each to the buss bar. The launch micro-clips then connect one to the bundle, and the other to the buss bar, for a
parallel connection.

A convenient tool for igniting a cluster is a **cluster whip** - a set of wires and micro-clips that allows the single
pair of clips at the launch pad to be easily broken into multiple sets of clips, to attach to multiple igniters, and
providing a parallel connection. The cluster whip connects to the igniters, and the launch controller's micro-clips
connect to conductors on the cluster whip.

Igniting Clustered APCP motors
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

APCP (*Ammonium Perchlorate Composite Propellant*) motors typical of Aerotech, Cesaroni, and Loki, are slower to ignite
than Black Powder motors (typical Estes motors). They may unpredictably "chuff", sit quiet for a moment and then ignite,
or even "spit" the igniter out. Because of this difference, and the unpredictability of APCP motor ignition, it's more
than a little likely that clustered APCP motors won't ignite simultaneously, if at all. When designing for an APCP
cluster (if you decide to roll these dice...), take into account what will happen to the rocket if not all motors
ignite before it pulls away from the pad. The safety of observers, and of your airframe hang in the balance.

Using Clustering for Body Tubes With, or Without Motors
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

It's possible to create imaginative designs, or mimic scale rockets of yesteryear by using OpenRocket's clustering
capability. One limitation of doing so is that regular **Body tube** components have no Cluster tab. To add Clustered
tubes (which are, by OpenRocket's definition always **Inner Tubes**) using OpenRocket's clustering features, you must
first have a regular **Body tube**.

You can use your regular **Body tube** as strapping around your clustered tubes, as a **Nose cone** mount, as an eject
gas manifold, or even create a "Vestigial" body tube. To do this, add a regular **Body tube** then set its length to
something like .0001 cm. The **Body tube** will be in the hierarchy and can have **Inner tubes**, but will barely be
seen in the renderings.

There are some limitations, as **Inner tubes** are not meant to be used in this way. They can't take a **Nose cone**
nor some other components. They won't affect aerodynamics, even if you're trying to make them into tube-fin-like things.
And whether as a unit or as a **Split cluster**, you can't convert **Inner tubes** to **Body tubes**.

Conventional Staging
--------------------

A "closed-hull" design with a separating airframe in which finned-stages holding motors are stacked up, and lower stages
holding burned-out casings separate under pressure as upper stages ignite. Conventional staging is inherently limited to
three stages because of the "Pisa Effect" which results in an increasing arcing trajectory with each stage.

In designing multi-staged rockets, it's important to realize that the center of mass will tend to start well toward the
rear of the rocket, based on the booster stage(s) weighted with the loaded motors. As booster motors are spent and the
spent stage(s) ejected, the center of mass will tend to move forward. Careful design ensures that the center of mass
remains forward of the center of pressure throughout the flight. Weighting and weight redistribution can move the
center of mass forward, while larger fin area tends to move the center of pressure aft. Ensure at least 1.0 airframe
caliber of separation between the (forward) center of mass and (aft) center of pressure. This is a rule of thumb, not a
hard-and-fast stability solution.

Rack Staging
------------

An "open-hull" design with a non-separating airframe in which motors are staked up, end-to-end, in a frame, and only the
burned-out casings are ejected under pressure as higher stages ignite, stage-after-stage. So long as high average impulse
lower stage motors are used to ensure adequate initial velocities, rack staging is not inherently limited because this
design overcomes the "Pisa Effect."

Here's a `2007 video demonstrating rack staging <https://sites.google.com/site/theskydartteam/projects/model-rocketry/rack-rocket-design>`__.

The BPS Aerospace thrust-vectoring design uses this approach to move a new motor into position for a landing burn.

Regulatory Concerns
====================

Rocketry is subject to regulation by federal, state, and local governments, and most of the regulations that rocketeers
must follow are promulgated by the National Fire Protection Association (NFPA) and the Federal Aviation Administration (FAA).
The NFPA divides rockets into two major classifications, model rockets (NFPA § 1122) and high power rockets (NFPA § 1127),
the difference primarily being weight and power, as follows:

- **Model Rocket**. A rocket vehicle that weighs no more than 1500 g (53 oz) with motors installed, is propelled by one
  or more model rocket motors having an installed total impulse of no more than 320 N-sec (71.9 lb-sec), and contains no
  more than a total of 125 g (4.4 oz) of propellant weight. (NFPA § 1122, subd. 3.3.7.2.)
- **High Power Rocket**. A rocket vehicle that weighs more than 1500 g (53 oz) with motors installed and is either
  propelled by one or more high power rocket motors or by a combination of model rocket motors having an installed total
  impulse of more than 320 N-sec (71.9 lb-sec). (NFPA §1127, subd. 3.3.13.1.)

Within the high power rocket classification, a subclassification for "complex" rockets is defined as a high power rocket
that is multi-staged or propelled by a cluster of two or more rocket motors. (NFPA §1127, subd. 3.3.13.1.1.) And, a
high-power rocket launched with an installed total impulse greater than 2,560 N-sec (576 lb-sec) must have an electronically
actuated recovery system. (NFPA §1127, subd. 4.10.2.)

----

National Association of Rocketry
================================

National Association of Rocketry pursuits the goal of safe, fun and educative sport rocketry. It is the oldest and largest
sport rocketry organization in the world. Visit dedicated `Wiki page <http://en.wikipedia.org/wiki/National_Association_of_Rocketry>`__
or `NAR official website <http://www.nar.org/>`__ for more information.

The major work of the :abbr:`NAR (National Association of Rocketry)` includes, but not limited to:

- Certification of Rocketry-Related products and establishment of safety codes

  The :abbr:`NAR (National Association of Rocketry)` is a recognized authority for safety certification of consumer
  rocket motors and user certification of high- power rocket fliers in the U.S. It plays a major role in establishment
  of safety codes for the hobby used and accepted by manufacturers and public safety officials nationwide.

- Certification of experienced rocketeers

  :abbr:`NAR (National Association of Rocketry)` issues three levels of High Power Rocketry (HPR) certificates,
  Level 1 (L1) through Level 3 (L3). Certificates are necessary to purchase powerful rocket motor components.

- Communication with public officials

  The :abbr:`NAR (National Association of Rocketry)` helps in communication with local public safety officials, and
  government regulatory agencies such as the Department of Transportation, Federal Aviation Administration, Bureau of
  Alcohol Tobacco Firearms and Explosives, and Consumer Product Safety Commission.

- Other work

  The :abbr:`NAR (National Association of Rocketry)` publishes the bimonthly color magazine Sport Rocketry (sent to
  each member and selected libraries and newsstands around the nation). The :abbr:`NAR (National Association of Rocketry)`
  provides a wide range of other services to its members, including: education programs; national and local competitions;
  grants to teachers and scholarships for student members; flight performance record recognition; liability insurance; and
  publication of technical literature.

----

Tripoli Rocketry Association
=============================

.. todo::

   Add information about Tripoli Rocketry Association.
