====================
Development Overview
====================

Welcome to the OpenRocket Development Guide! This documentation is designed for developers interested in contributing to OpenRocket.

This guide covers the architecture, codebase, and development workflows in detail. To dive deeper into specific topics,
use the links below to navigate to different sections of this development guide. To learn more about the technical aspects
of OpenRocket, such as the aerodynamic calculations, refer to the `Technical documentation <https://openrocket.info/documentation.html>`_.

Code structure
--------------

OpenRocket is a Java application organized using the Java Platform Module System (JPMS) and built with `Gradle <https://gradle.org/>`_.
The code is organized in the following two packages:

- `info.openrocket.core <https://github.com/openrocket/openrocket/tree/unstable/core>`_ - The backend of OpenRocket. \
  This package contains the classes that represent the rocket and its components, as well as the simulation engine. \
  The classes in this package are not dependent on any GUI libraries and can be used in other applications.

- `info.openrocket.swing <https://github.com/openrocket/openrocket/tree/unstable/swing>`_ - The GUI of OpenRocket. \
  This package contains the classes that create the user interface. OpenRocket uses the Java Swing library for the GUI.

Further Reading
---------------
Explore the following sections to learn more about OpenRocket's development:

- :doc:`Development Setup </dev_guide/development_setup>`
- :doc:`OpenRocket Architecture </dev_guide/architecture>`
- :doc:`Codebase Walkthrough </dev_guide/codebase_walkthrough>`
- :doc:`Development Guidelines </dev_guide/development_guidelines>`
- :doc:`Testing and Debugging </dev_guide/testing_and_debugging>`
- :doc:`API Documentation </dev_guide/api_documentation>`
- :doc:`Building and Releasing </dev_guide/building_releasing>`
- :doc:`Contributing to the Website </dev_guide/contributing_to_the_website>`
- :doc:`FAQ and Troubleshooting </dev_guide/faq_troubleshooting>`

We encourage contributions from everyone and hope this guide helps you get started with developing OpenRocket.

