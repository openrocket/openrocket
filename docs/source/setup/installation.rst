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

Off-screen Rendering
^^^^^^^^^^^^^^^^^^^^

Some users have reported that the rocket shown in the 3D design view is not full-size. This can occur if the off-screen
rendering setting is not correct for your device. Using the Three-stage rocket example packaged with OpenRocket, this is
what the problem looks like:

.. figure:: /img/setup/installation/off_screen_rendering_wrong.png
   :alt: Wrong 3D View of Three-stage Rocket
   :figclass: or-figclass, or-image-border
   :figwidth: 75 %
   :align: center

   The 3D view of the Three-stage rocket example is too small.

To fix this, you need to change the off-screen rendering setting. To do this, open the ``Edit`` menu in the application
ribbon and select ``Preferences``. This will open the Preferences dialog box. This window has several tabs (``General``,
``Design``, ``Simulation``, ... You need to go to the ``Graphics`` tab (just click on that tab header) and on the bottom
of the page change the ``Use off-screen rendering`` setting:

.. figure:: /img/setup/installation/off_screen_rendering_setting.png
   :alt: Off-screen Rendering Setting
   :figclass: or-figclass, or-image-border
   :figwidth: 50 %
   :align: center

   The off-screen rendering setting is on the bottom of the ``Graphics`` tab.

Your 3D view should now look like this:

.. figure:: /img/setup/installation/off_screen_rendering_right.png
   :alt: Correct 3D View of Three-stage Rocket
   :figclass: or-figclass, or-image-border
   :figwidth: 75 %
   :align: center

   The 3D view of the Three-stage rocket example is now correct.

.. note::

   Whenever you face issues with 3D rendering in OpenRocket, it is a good idea to change the off-screen rendering setting
   to see if that fixes the problem.

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
