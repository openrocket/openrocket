********************
Development Overview
********************

Welcome to the OpenRocket Development Guide! This documentation is designed for developers interested in contributing to OpenRocket.

This guide covers the architecture, codebase, and development workflows in detail. To dive deeper into specific topics,
use the links below to navigate to different sections of this development guide. To learn more about the technical aspects
of OpenRocket, such as the aerodynamic calculations, refer to the `Technical documentation <https://openrocket.info/documentation.html>`__.

Code structure
==============

OpenRocket is a Java application organized using the Java Platform Module System (JPMS) and built with `Gradle <https://gradle.org/>`__.
The code is organized in the following two packages:

- `info.openrocket.core <https://github.com/openrocket/openrocket/tree/unstable/core>`__ - The backend of OpenRocket. \
  This package contains the classes that represent the rocket and its components, as well as the simulation engine. \
  The classes in this package are not dependent on any GUI libraries and can be used in other applications.

- `info.openrocket.swing <https://github.com/openrocket/openrocket/tree/unstable/swing>`__ - The GUI of OpenRocket. \
  This package contains the classes that create the user interface. OpenRocket uses the Java Swing library for the GUI.

Further Reading
===============

Explore the following sections to learn more about OpenRocket's development:

- :doc:`Development Environment Setup </dev_guide/development_setup>`
  |br_no_pad|
  *How to set up your development environment to build and run OpenRocket.*

- :doc:`OpenRocket Architecture </dev_guide/architecture>`
  |br_no_pad|
  *An overview of the high-level code architecture of OpenRocket.*

- :doc:`Codebase Walkthrough </dev_guide/codebase_walkthrough>`
  |br_no_pad|
  *A detailed guide to the codebase of OpenRocket.*

- :doc:`Development Guidelines </dev_guide/development_guidelines>`
  |br_no_pad|
  *Guidelines for contributing to OpenRocket.*

- :doc:`Testing and Debugging </dev_guide/testing_and_debugging>`
  |br_no_pad|
  *How to test and debug the OpenRocket code.*

- :doc:`API Documentation </dev_guide/api_documentation>`
  |br_no_pad|
  *Documentation for the OpenRocket API.*

- :doc:`Building and Releasing </dev_guide/building_releasing>`
  |br_no_pad|
  *How to build and release new OpenRocket versions.*

- :doc:`Contributing to the Website </dev_guide/contributing_to_the_website>`
  |br_no_pad|
  *How to contribute to the* `openrocket.info <https://openrocket.info/>`__ *website.*

- :doc:`Contributing to Translations </dev_guide/contributing_to_translations>`
  |br_no_pad|
  *How to contribute to translating the OpenRocket UI into different languages.*

- :doc:`Contributing to the Documentation </dev_guide/contributing_to_the_docs>`
  |br_no_pad|
  *How to contribute to this OpenRocket documentation.*

- :doc:`FAQ and Troubleshooting </dev_guide/faq_troubleshooting>`
  |br_no_pad|
  *Frequently asked questions and troubleshooting tips for developers.*

**We encourage contributions from everyone and hope this guide helps you get started with developing OpenRocket. ❤️**

