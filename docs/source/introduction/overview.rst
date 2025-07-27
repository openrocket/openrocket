********
Overview
********

What is OpenRocket?
===================

.. figure:: /img/openrocket_logo_256.png
   :figclass: or-figclass
   :figwidth: 25 %
   :align: right

   The OpenRocket Logo

Welcome! OpenRocket is an open-source model rocket simulation software application. It was originally developed by
Sampo Niskanen in 2009 as part of his master thesis at what was then `Helsinki University of Technology <https://www.aalto.fi/en/aalto-university/history>`__.
If you want to have a look at his thesis you can download it from `OpenRocket's technical documentation page <http://openrocket.info/documentation.html>`__.
Written entirely in Java, OpenRocket is fully cross-platform. To install the software, please refer to the :doc:`Installation Instructions </setup/installation>`

..  rst-class::  clear-both

OpenRocket is intended to be used by rocketeers who want to test the performance of a model rocket before actually
building and flying it. The software accurately computes the aerodynamic properties of rockets and simulates their flight,
returning a wide range of technical results.

The program can be roughly divided into two sections:

* **Rocket design**: In this phase, you can design the model rocket you intend to build, choosing from a wide range of \
  **body components**, **trapezoidal**, **elliptical**, and **free-form fins**, **inner components**, and **mass objects**. \
  During this phase, you will see a 2D representation of the rocket you are building and various technical information, \
  *such as size, mass, apogee, max. velocity, max. acceleration, stability, center of gravity (CG), and center of pressure \
  (CP)* about your rocket. This allows you to have a good idea of its performance even before running any simulation.

* **Flight simulation**: In this phase, you can run one or more simulations of your rocket's flight, choosing from one \
  or more **motor configurations**. Each simulation, calculated using the Runge-Kutta 4 simulator, returns a wide range \
  of data about the rocket's flight. Unfortunately, for the moment, a graphical visualization of the rocket's flight is \
  not available (`help needed <https://openrocket.info/contribute.html>`__).


For more information about OpenRocket's features and a few screenshots you can have a look `here <https://openrocket.info/features.html>`__.

How this Documentation is Organized
===================================

.. attention::

    This documentation is still a work in progress, so **some sections may be incomplete**. If you want to help us improve it, please refer to the :doc:`Contribute section </introduction/contribute>`.

This documentation is organized in 4 main sections:

#. **Introduction**: this section contains general information about OpenRocket, such as its history, its features, and how to contribute to the project.
#. **Setup**: this section contains information about how to install and run OpenRocket on your computer.
#. **User Guide**: this section contains information about how to use OpenRocket to design and simulate your model rockets.
#. **Developer Guide**: this section contains information about how to contribute to the development of OpenRocket.
