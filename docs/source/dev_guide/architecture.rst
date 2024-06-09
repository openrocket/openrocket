***********************
OpenRocket Architecture
***********************

This section describes the high-level architecture of OpenRocket, the important modules and their interactions, and the technology stack.
It is intended for developers who want to understand the structure of the code and how it works.

.. contents:: Table of Contents
   :depth: 2
   :local:

----

Introduction
============

OpenRocket is a Java application that runs on the desktop. It is built using the Swing GUI toolkit. The choice of Java
was originally made because it is a platform-independent language, making it possible to run OpenRocket on Windows, macOS, and Linux.
While the popularity of Java has waned in recent years, it is still a good choice for desktop applications because of its
mature libraries and tools. Additionally, rewriting OpenRocket in another language would be a massive undertaking that is
not currently feasible given the size of the developer team. Of course, any suggestions and help in this area are welcome.

OpenRocket is released under the GNU General Public License (GPL) version 3.0. This means that the source code is open and
available for anyone to use, modify, and distribute. The only major restriction is that any derivative works must also be
released under the GNU GPL. This ensures that the code remains open and free for everyone. So, no one can take the code and
sell it as a proprietary product.

Java Platform Module System (JPMS)
==================================

OpenRocket leverages the **Java Platform Module System** (**JPMS**) to enhance modularity, encapsulation, and maintainability.
JPMS allows OpenRocket to be organized into two distinct modules, the ``core`` module and the ``swing`` module,
each with its own well-defined boundaries and dependencies.

Each module in OpenRocket is described by a `module-info.java` file located at the root of the module's source directory
(:file:`<module>/src/main/java.module-info.java`). This file declares:

   * **Module Name:** A unique identifier for the module (e.g., `info.openrocket.core`, `info.openrocket.swing`).
   * **Dependencies:** The modules that this module depends on to function correctly. For example, the `info.openrocket.swing` module depends on the `info.openrocket.core` module.
   * **Exported Packages:** The packages within the module that are accessible to other modules.
   * **Services:** The services provided or consumed by the module (if applicable).

By embracing JPMS, OpenRocket gains several advantages:

   * **Strong Encapsulation:** Modules explicitly control what packages are exposed, preventing accidental access to internal implementation details.
   * **Reliable Configuration:** The module system verifies dependencies at compile time and runtime, reducing the risk of missing or incompatible components.
   * **Improved Maintainability:** Modules can be developed and tested independently, making it easier to understand, modify, and evolve the codebase.
   * **Scalability:** The modular structure facilitates the addition of new features or the replacement of existing components without impacting the entire application.


Core Module
===========

The ``core`` module contains the core functionality of OpenRocket, such as the rocket simulation engine, the file format
parsers and writers, and the rocket design classes. This module is intended to be reusable and can be used in other
applications that need rocket simulation capabilities.

Swing Module
============

The ``swing`` module contains the user interface of OpenRocket. It is built using the Swing GUI toolkit and provides a graphical
interface for designing rockets, running simulations, and viewing the results. This module depends on the core module
and uses its functionality to perform the simulations and display the results.

Rocket Components
=================



Aerodynamic Calculators and Simulators
======================================

Simulation Listeners
====================


Component Database
==================

Thrust Curves
=============

:abbr:`OR (OpenRocket)` uses Thrustcurves.org for its thrustcurves/motors.

Scripts
=======

Plugins
=======

File Format (.ork)
==================

The OpenRocket native format uses the file extension \*.ork. It is an XML format file combined with any needed graphics
files, contained in a ZIP archive. The extension \*.ork.gz is also accepted by OpenRocket, though plain .ork is recommended.
In other to view the contents of the file, you can simply rename the file extension to .zip and extract the contents.


The ``version`` attribute of the <openrocket> tag describes the file format version used, while the ``creator``
attribute *may* describe the software and version used to write the document. The file format version is increased
every time the format is changed. The minor number is increased when changes are made that are mostly backward-compatible,
meaning that older software versions should be able to read the design sans the new features. The major number is
increased when changes are made that render the design problematic or impossible to read for older software. For maximum
compatibility software should save a file in the oldest file format version that supports all the necessary design features.

For an overview of the changes between file format versions, see the `fileformat.txt <https://github.com/openrocket/openrocket/blob/unstable/fileformat.txt>`_
file in the root directory of the repository.



