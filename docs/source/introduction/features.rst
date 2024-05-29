********
Features
********

This page lists the current features of OpenRocket and compares them to those of RockSim.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Current features (as of OpenRocket 23.09):
==========================================

General
-------

* Fully cross-platform, written in Java

* `Fully documented simulation methods <https://openrocket.info/documentation.html>`__

* Open Source (see our `GitHub repository <https://github.com/openrocket/openrocket>`), source code available under the `GNU GPL <https://www.gnu.org/licenses/gpl-3.0.txt>`__

* Export OpenRocket design file to other simulation programs (RockSim, RASAero II)

* Export component(s) to OBJ file for 3D printing

* Extensible with custom simulation listeners, expressions, and plugins

User Interface
--------------

* **Intuitive user interface**

* **Real-time view of CG and CP** position

* **Real-time flight altitude, velocity and acceleration** information from a continuous simulation performed in the background

* Zoomable schematic view of rocket from the side or rear, with rotation around the center axis

Design
------

* A multitude of available components to choose from

* **Trapezoidal, elliptical, free-form and tube fins** supported

* Support for **canted** fins (roll stabilization)

* **Staging** and **clustering** support

* **Pods** support

* Automatic calculation of component mass and CG based on shape and density

* Ability to **override mass, CG and CD** of components or stages separately

Simulation and Analysis
-----------------------

* Full **six degree of freedom** simulation

* Rocket stability computed using **extended Barrowman** method

* **Automatic design optimization** — *you can optimize any number of rocket parameters for flight altitude, maximum velocity or a number of other values*

* Realistic wind modeling

* Analysis of the **effect of separate components** on the stability, drag and roll characteristics of the rocket

* **Fully configurable plotting**, with various preset configurations

* Simulation data can be **exported to CSV** files for further analysis

* **Simulation listeners** allowing custom-made code to interact with the rocket during flight simulation

.. raw:: html

   <hr />

Planned Future Features
=======================

.. note::
   OpenRocket is under constant work in the free time of the developers. If you want to **help improve it**, please refer to the :doc:`Contribute page </introduction/contribute>`.

Below are a few major features that are under consideration:

* Better support for transonic and supersonic simulations (:doc:`help needed! </introduction/contribute>`)

* Monte Carlo simulation for dispersion analysis

* Simulate fin flutter

* Customized support for hybrid rocket motors and water rockets

* Importing and plotting actual flight data from altimeters

* Rocket flight animation

* A “wizard” for creating new rocket designs

* More advanced rocket optimization methods

* Import CD and CP data from other programs (e.g. RASAero)

For a full overview of the planned features, please refer to the `GitHub issue tracker <https://github.com/openrocket/openrocket/issues>`__.

.. raw:: html

   <hr />

Comparison to RockSim
=====================

`RockSim <https://www.apogeerockets.com/Rocket_Software/RockSim>`__ is a very powerful, commercial rocket design and simulation program.
It is more advanced than OpenRocket in some regards, but its price tag of $124 makes it inaccessible to many hobbyists.
OpenRocket is free, and the source code is available for modification by anyone.
To help you decide which program is right for you, we have compiled a comparison of the features of OpenRocket 23.09 and RockSim 10 below.

While hosted on the OpenRocket documentation, we have attempted to make this an objective comparison between the functionality
of the two software products. If you think something is wrong or omitted, please `contact us <https://openrocket.info/contact.html>`__.

General
-------

.. list-table:: OpenRocket vs. RockSim: General
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - License
     - .. cssclass:: or-table-cell, or-table-good

       | Open Source (GPLv3)

     - .. cssclass:: or-table-cell, or-table-poor

       | Proprietary

   * - Price
     - .. cssclass:: or-table-cell, or-table-good

       | Free

     - .. cssclass:: or-table-cell, or-table-poor

        | $124

   * - Supported platforms
     - .. cssclass:: or-table-cell, or-table-good

       | Windows, Mac, Linux

     - .. cssclass:: or-table-cell, or-table-okay

       | Windows, Mac

   * - Supported file formats
     - .. cssclass:: or-table-cell, or-table-good

       | ORK, RKT, CDX1

     - .. cssclass:: or-table-cell, or-table-poor

       | RKT

   * - Anti-piracy copy protection
     - .. cssclass:: or-table-cell, or-table-good

       | None

     - .. cssclass:: or-table-cell, or-table-okay

       | `PACE <http://www.paceap.com/>`__

UI Features
-----------

.. list-table:: OpenRocket vs. RockSim: UI Features
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - Side/back view
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - 3D view
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

        | Yes

   * - Photorealistic 3D rendering
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

   * - Export 3D design
     - .. cssclass:: or-table-cell, or-table-good

       | Yes (OBJ)

     - .. cssclass:: or-table-cell, or-table-good

       | Yes (3DS)

   * - Design view rotation
     - .. cssclass:: or-table-cell, or-table-good

       | Any angle

     - .. cssclass:: or-table-cell, or-table-poor

       | Side and top only

   * - Live CG/CP view
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Realtime simulation
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

Design Features
---------------

.. list-table:: OpenRocket vs. RockSim: Design Features
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - Basic components
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Freeform fins
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

        | Yes

   * - Asymmetric fin configurations
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Tube fins
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Ring-tail fins
     - .. cssclass:: or-table-cell, or-table-bad

       | No

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Fins attached to fins
     - .. cssclass:: or-table-cell, or-table-bad

       | No

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - External pods
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Canted fins / roll stabilization
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

   * - Standard component libraries
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Decals
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Clustering support
     - .. cssclass:: or-table-cell, or-table-good

       | Yes, modifiable

     - .. cssclass:: or-table-cell, or-table-okay

       | Yes, one-time wizard only

   * - Staging support
     - .. cssclass:: or-table-cell, or-table-good

       | Yes, unlimited

     - .. cssclass:: or-table-cell, or-table-okay

       | Yes, 3 stages

Simulation Features
-------------------

.. list-table:: OpenRocket vs. RockSim: Simulation Features
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - Degrees of freedom
     - .. cssclass:: or-table-cell, or-table-good

       | 6DOF

     - .. cssclass:: or-table-cell, or-table-okay

       | 3DOF

   * - Geodetic calculation (Earth coordinates, coriolis effect)
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

        | No

   * - Simulations extensible by own code
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

   * - Simulation animation
     - .. cssclass:: or-table-cell, or-table-bad

       | No

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Plotting any simulated variables
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Exporting simulated data
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Computing custom variables
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

Optimization and Analysis
-------------------------

.. list-table:: OpenRocket vs. RockSim: Optimization and Analysis
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - General design optimization
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

   * - Mass optimization
     - .. cssclass:: or-table-cell, or-table-okay

       | Yes (using general optimizer)

     - .. cssclass:: or-table-cell, or-table-good

        | Yes

   * - Stability analysis
     - .. cssclass:: or-table-cell, or-table-good

       | Yes, per component

     - .. cssclass:: or-table-cell, or-table-okay

       | Yes, per stage

   * - Drag analysis
     - .. cssclass:: or-table-cell, or-table-good

       | Yes, per component

     - .. cssclass:: or-table-cell, or-table-poor

       | Yes, per stage

   * - Roll analysis
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

Printing
--------

.. list-table:: OpenRocket vs. RockSim: Printing
   :widths: 20 20 20
   :header-rows: 1
   :class: or-table

   * -
     - OpenRocket
     - RockSim
   * - Schematic view
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Stability information
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

        | Yes

   * - Simulation results
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Parts list
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-good

       | Yes

   * - Component templates
     - .. cssclass:: or-table-cell, or-table-good

       | Nose cone, fin sets, centering rings

     - .. cssclass:: or-table-cell, or-table-good

       | Nose cone, fin sets, centering rings

   * - Fin placement guide
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No

   * - Export to PDF
     - .. cssclass:: or-table-cell, or-table-good

       | Yes

     - .. cssclass:: or-table-cell, or-table-bad

       | No
