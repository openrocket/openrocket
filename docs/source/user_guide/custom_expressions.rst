******************
Custom Expressions
******************

Since OpenRocket 12.09, you are not limited to using just the built-in simulation variables in your plots and
analysis. With the custom expression feature, you can write expressions to calculate other values of interest during the
simulation. These can then be plotted or exported just like the built-in variables.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Overview
========

Custom expressions are added to your rocket document from the 'Analyze' menu under custom expressions. This will open a
window showing all your custom expressions.

.. figure:: /img/user_guide/custom_expressions/custom_expressions.png
   :align: center
   :width: 55%
   :figclass: or-image-border
   :alt: The Custom Expressions window.

   The Custom Expressions window.

Initially, there will be no custom expressions, so you will need to add one using the *New expression* button in the
lower right. This opens the expression builder window. You can also import expressions from another .ork file.

----

Building expressions
====================

.. figure:: /img/user_guide/custom_expressions/expression_builder.png
   :align: center
   :width: 45%
   :figclass: or-image-border
   :alt: The Expression Builder window.

   The Expression Builder window.

You must specify a name for the expression, a short symbol, the units of the resulting value, and of course the expression
itself. After you enter a valid value in each of the fields the adjacent indicator will change from red to green. Only
when all indicators are green will you be able to add or update the expression.

- The :guilabel:`Name` field can be arbitrary; it only must have not been used before.
- The :guilabel:`Symbol` field is intended for a short (locale-independent) symbol representing the value. It must not have been
  used before, contain no numbers, whitespaces, or special characters such as brackets or operators.
- There are no restrictions on the :guilabel:`Units`; it can even be empty for dimensionless quantities. However, if you enter a
  standard SI unit then you will be able to automatically convert the units when plotting or exporting. The available
  SI units are: m, m^2, m/s, kg, kg m^2, kg m^3, N, Ns, s, Pa, V, A, J, W, kg m/s, Hz, K. They must match exactly.
- The :guilabel:`Expression` must only contain valid symbols, operators, and numbers and must make sense mathematically.
  For convenience, the adjacent indicator updates on-the-fly.
- It is possible to nest custom expressions, i.e., you can use the symbol defined for a custom expression in another
  expression. However, you must ensure that expressions are calculated in the correct order if you do this. This can be
  done using the blue arrows in the custom expression pane to adjust the order of calculation.

To see a list of the available variables and their symbols, click the :guilabel:`Insert Variable` button. This will open a window
from which you can choose a variable and insert it (at the current cursor position) in the expression box. This is
particularly useful because you may not be able to type some of the symbols on your keyboard. The :guilabel:`Insert Operator`
window is similar and shows all the available mathematical operators and functions.

----

Index expressions
=================

The custom expressions are calculated at each time step of the simulation; however, there are some cases where it is
useful to have access to earlier values of a given variable. This is possible using **index expressions**. These use a
square bracket syntax to specify the time (in seconds) for the variable you want. For example, the expression:

.. code-block:: none

  m / m[0]

would give the ratio of the current mass to the launch mass at time 0. Similarly,

.. code-block:: none

  m - m[t-1]

would give you the change in mass over the last second.

You can specify any valid sub-expression inside the square brackets; the only restriction is that you can't nest another
index/range expression inside the square brackets.

When indexed expressions are calculated, interpolation is used to get the value exactly at the specified time, independent
of the time steps of the simulation.

If you specify a time smaller than 0 or greater than t then it will be clipped to 0 or t respectively. You can't access
data that has not been calculated yet.

----

Range expressions
=================

It is sometimes useful to have access to a range of values of a particular variable rather than just one point.
OpenRocket includes a number of useful operators for calculating statistics and other properties of ranges. These
operators can be identified in the operator selection box by the *\[\:\]* which will already have been filled out in place
of one or more of the parameters.

Range expressions are defined with a square bracket syntax similar to index expressions, but with a ':' used to separate
the lower and upper bounds of a range. For example, suppose we had an accelerometer on our rocket which (as many do)
includes some low-pass filtering on the output. This can be modeled as a moving average and defined with a custom
expression such as:

.. code-block:: none

   mean(At[t-0.5:t])

which will calculate a moving mean for the variable At (total acceleration) over the last 0.5 seconds of data.

As with index expressions, the upper and lower bounds can be any valid expression. If omitted, the upper bound will
default to t and the lower bound to 0, so the above expression can also be written

.. code-block:: none

   mean(At[t-0.5:])

In this particular case, we might want to make the expression more realistic by clipping accelerations above a given
threshold and perhaps returning the actual voltage from the sensor, for example:

.. code-block:: none

   0.2 * uclip( mean(At[t-.5:]), 10 )

Note that when range expressions are calculated the data is generated by interpolation over the specified range with a
fixed time step equal to the default time step set in your simulation options. This is independent of the current time step
*dt* used by the simulation engine. When generated, range expressions include the start time and step information.
This facilitates easy integration or optimization with functions such as *trapz([:])* for trapezoidal integration or
*tnear([:],x)* for finding the time value when a variable is nearest a specified value.

For a complete list of all the operators available see the operator selection list when making a new expression.

----

Troubleshooting
===============

While OpenRocket makes a reasonable attempt to check your expression as you enter it, it is still possible to enter
something invalid or that can't be calculated for some reason. In this case you will simply end up with no data available
to plot after running the simulation.

If you can't figure out why your expression is not generating any data or can't be accepted by the expression builder
then you might find some useful information in the error log. This can be accessed from the help -> debug log menu. Any
relevant messages are probably under the 'USER' category.

It should not be possible to cause a crash with an invalid expression. If you manage to, please report the bug and
include your expression.

Custom expressions are interpreted during the simulation and are necessarily much slower than "native" datatypes.
For a few simple expressions you probably won't notice much speed difference but it can become particularly significant
if you have range expressions. If speed is an issue for you then you might want to consider implementing your expression
as a `simulation listener <Simulation_Listeners>`_.
