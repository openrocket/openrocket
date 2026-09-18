*************
Thrust Curves
*************

A motor information file is referred to as a "thrust curve", as the
most important data in the thrust curve is function of thrust against
time. A thrust curve also contains additional information such as the
motor's mass, and metadata such as its manuacturer.

OpenRocket is able to use thrust curves in either the old RASP .eng file
format (see `https://www.thrustcurve.org/info/raspformat.html
<https://www.thrustcurve.org/info/raspformat.html>`_) or the newer
RockSim .rse format (see
`https://www.thrustcurve.org/thirdparty/RockSim Engine File Format.pdf
<https://www.thrustcurve.org/thirdparty/RockSim\ Engine\ File\ Format.pdf>`_).
The .rse file is to be preferred when it is available for a motor, as
it can contain more information than a .eng file such as motor center
of mass.

A database of thrust curves for commercial motors is maintained at
`https://www.thrustcurve.org/
<https://www.thrustcurve.org/>`_. OpenRocket uses this database as the
source for its own internal motor database. As of version 26.??, it is
possible to configure the program to check our database against a
cached copy of the thrustcurve.org database upon startup, and download
new additions as needed.

.. _importing_thrust_curves:

Import Custom Thrust Curves
===========================

To set up the thrust curves folder(s), please see the :ref:`Thrust
Curves Folder Settings <thrust_curves_setting>` section.


Thrust Calculation During Simulation
====================================

At each time step of a simulation, the current motor thrust is
calculated as follows:

#. The program determines which motors are currently firing.
#. The thrust from each motor is calculated by interpolating between
   the points from the motor's thrust curve, and the motor thrusts are
   added together.
#. If the motors' nozzle exit diameters are known, the motor thrust is
   corrected to account for air pressure.

The thrust correction is performed in accordance with the rocket
thrust equation, as presented in
`https://www1.grc.nasa.gov/beginners-guide-to-aeronautics/rocket-thrust/
<https://www1.grc.nasa.gov/beginners-guide-to-aeronautics/rocket-thrust/>`_.

F = ṁV\ :sub:`e` + (p\ :sub:`e` - p\ :sub:`0`)A\ :sub:`e`

The values in the thrust curve are simply the ṁV\ :sub:`e` term, while
the second term is the difference between standard pressure (p\
:sub:`e`) and the current atmospheric pressure (p\ :sub:`0`),
multiplied by the sum of the nozzle exit areas of all the active
motors. If a motor's nozzle exit diameter is unknown, entering a value
of 0 will result in no altitude correction (except in very high altitude
flights, this will not result in a significant error).
