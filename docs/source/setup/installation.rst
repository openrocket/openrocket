*************************
Installation Instructions
*************************

.. contents:: Table of Contents
   :depth: 1
   :local:
   :backlinks: none

----

Introduction
============

OpenRocket is released in two forms: as a *packaged application* and as a *JAR file*.
**The easiest way to get OpenRocket up and running is to download and install one of the packaged installers.**
The packaged installers come with everything needed, including the correct version of Java;
*you will not need to install, update or downgrade Java on your device to run them.*

Download the latest version from `our downloads page <https://openrocket.info/downloads.html?vers=latest>`__.

.. raw:: html

   <hr>

Pre-Installation Procedures
===========================

Some users may experience problems with the OpenRocket installation if they don't follow the pre-installation procedures below.

Close All Instances of OpenRocket
---------------------------------

If you have OpenRocket running, close it before installing a new version. By default, new OpenRocket releases
install to the same location on your device; these releases even share preferences. Because of this, if OpenRocket is
open when you attempt to install an updated release, the installation may not update all of the files needed for
OpenRocket to function correctly.

Before installing an update, you *MUST* close all open instances of OpenRocket.

(Optional) Uninstall Prior OpenRocket Releases
----------------------------------------------

As described above, by default, new releases of OpenRocket install to the same location on your device.
Although **not expressly required**, it is suggested that all previously installed OpenRocket releases be uninstalled
before installing the updated release. This is *not required*, but is suggested to ensure the proper function of OpenRocket.

.. raw:: html

   <hr>

Installing OpenRocket 🚀
========================

**Download the latest version from** `our downloads page <https://openrocket.info/downloads.html?vers=latest>`__.
Scroll down to download the correct installer for your platform (Windows, macOS, or Linux).

Each platform has a different installation process. Click on the ``Show <your platform> installation instructions`` header under your
platform's download button to see the installation instructions.

.. raw:: html

   <hr>

After-Installation Checks
=========================

After installing OpenRocket, you need to verify that the installation was successful and potentially modify some settings
to ensure that OpenRocket functions correctly.

Verify the Installed Release Number
-----------------------------------

First, verify that you actually installed the current release of OpenRocket. You can see this in the splash screen, which
appears when you start OpenRocket. The splash screen will show the release number on the right:

.. figure:: /img/setup/installation/splash_screen.png
   :alt: OpenRocket Splash Screen
   :figclass: or-figclass
   :figwidth: 60 %
   :align: center

   During startup, the OpenRocket Splash Screen shows the release number on the right.

If the release number is not correct, you may have installed the wrong version of OpenRocket, or didn't follow the
`Pre-Installation Procedures`_ above.

Another way to check the release number is to open the ``Help`` menu in the application ribbon
and select ``About``. This will open a dialog box that shows the release number:

.. figure:: /img/setup/installation/about_dialog.png
   :alt: OpenRocket About Dialog
   :figclass: or-figclass, or-image-border
   :figwidth: 40 %
   :align: center

   The OpenRocket About Dialog (``Help`` → ``About``) shows the release number.

Check the Settings
------------------

For most users, OpenRocket's default settings will work fine. However, some users may need to change some settings.

3D Rendering
^^^^^^^^^^^^

If a 3D design view or Photo Studio does not open, first check :menuselection:`Edit --> Preferences --> Graphics` and
confirm that :guilabel:`Enable 3D Graphics` is selected. Close and reopen the affected 3D view after changing this
setting.

If rendering is slow or unreliable, try a lower :guilabel:`Level of detail` and disable MSAA, FXAA, shadows, ambient
occlusion, or surface roughness. See :ref:`graphics_preferences` for what each option controls. Also make sure that you
installed the OpenRocket package for your operating system and processor architecture and that your graphics drivers are
up to date.

The current 3D engine always renders the scene into an internal off-screen target before presenting it to the window.
There is no separate off-screen-rendering preference.

If a graphics-driver failure prevents OpenRocket from starting, the 3D engine can be disabled with the
``-Dopenrocket.3d.disable`` JVM argument. See :doc:`../dev_guide/command_line_arguments`.

.. _thrust_curves_setting:

Thrust Curves Folder
^^^^^^^^^^^^^^^^^^^^

OpenRocket ships with a bunch of built-in motor thrust curves. However, it is possible to import your own thrust curves,
see :ref:`Import Custom Thrust Curves <importing_thrust_curves>`. For this to work, you need to
**specify a folder where OpenRocket will look for thrust curves**. By default, OpenRocket will look in the ``ThrustCurves``
folder in your application data directory. The application data directory is different for each operating system, see the
table below for the default application data directories for each operating system.


.. list-table:: Default Application Data Directories by Operating System
   :widths: auto
   :header-rows: 1
   :class: or-table-line-blocks

   * - Operating System
     - Default Thrust Curves Directory
   * - Windows
     - | :file:`%APPDATA%\OpenRocket\ThrustCurves` (if ``APPDATA`` is available)\*
       | :file:`C:\Users\[YOUR USERNAME]\OpenRocket\ThrustCurves` (fallback if ``APPDATA`` is not available)
       |
       | \* ``APPDATA`` is usually :file:`C:\Users\[YOUR USERNAME]\AppData\Roaming`
   * - macOS
     - :file:`/Users/[YOUR USERNAME]/Library/Application Support/OpenRocket/ThrustCurves/`
   * - Linux
     - :file:`/home/[YOUR USERNAME]/.openrocket/ThrustCurves/` (hidden directory)

``[YOUR USERNAME]`` **is your user name on your device.**

To view or modify the user-defined thrust curves folder(s), open the :menuselection:`Edit` menu in the application ribbon and select
:menuselection:`Preferences`. This will open the Preferences dialog box. You need to go to the :menuselection:`General` tab
(should be open by default) and change the :guilabel:`User-defined thrust curves` setting:

.. figure:: /img/setup/installation/thrust_curves_setting.png
   :alt: Thrust Curves Setting
   :figclass: or-figclass, or-image-border
   :figwidth: 50 %
   :align: center

   The setting to change the user-defined thrust curves folder.

You can also add multiple thrust curve folders. To do this, click on the :guilabel:`Add` button and select the folder you want to add.
You can also manually enter a new folder path. This path must be separated from other paths by a semicolon (``;``).

Troubleshooting
===============

When you have issues with your installation, ensure that you have **read the installation instructions** for your platform.
When you download the installer from our `downloads page <https://openrocket.info/downloads.html?vers=latest>`__, you can
click on the :guilabel:`Show <your platform> installation instructions` header under your platform's download button to see the
installation instructions.

If you have further issues, please `contact us <https://openrocket.info/contact.html>`__.

Uninstalling
============

.. todo::
   Add uninstallation instructions.
