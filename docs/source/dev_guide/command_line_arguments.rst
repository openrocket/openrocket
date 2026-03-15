**********************
Command Line Arguments
**********************

You can run the JAR file with the following command line arguments:

.. list-table::
   :widths: 40 60
   :header-rows: 1

   * - Argument
     - Definition
   * - ``-Dopenrocket.debug``
     - Start OpenRocket with extra debugging tools
   * - ``-Dopenrocket.debug.prefs``
     - Use debugging preferences
   * - ``-Dopenrocket.debug.bugurl={url: String}``
     - Provide a URL that redirects you to bug reporting
   * - ``-Dopenrocket.debug.updateurl={url: String}``
     - Set the URL used in the software updater
   * - ``-Duser.home={dir: String}``
     - Set the home directory used by OpenRocket
   * - ``-Dos.name``
     - Set the current operating system
   * - ``-Dopenrocket.airstart.altitude``
     - Set the airstart altitude
   * - ``-Dopenrocket.debug.safetycheck``
     - Whether to use additional safety checks
   * - ``-Dopenrocket.debug.coordinatecount={count: int}``
     - For debugging, will print a line after coordinate instantiations
   * - ``-Dopenrocket.debug.quaternioncount={count: int}``
     - For debugging, will print a line after quaternion instantiations
   * - ``-Dopenrocket.3d.disable``
     - Allows you to disabled the 3D view if the program won't start because of it
   * - ``-Dopenrocket.debug.motordigest``
     - ?
   * - ``-Dopenrocket.preseteditor.fileMenu``
     - Activate an experimental preset editor window
   * - ``-Dopenrocket.debug.fileMenu``
     - Shows a debugging file menu, with special debugging tools
   * - ``-Duser.country``
     - Set the user country
   * - ``-Djava.version``
     - Set the Java version
   * - ``-Dopenrocket.ignore-jre``
     - Whether to ignore checking the JRE version at startup
   * - ``-Dopenrocket.laf={theme}``
     - Set the UI theme (choices are 'LIGHT' or 'DARK')
   * - ``-Dopenrocket.bypass.presets``
     - Bypass loading component presets
   * - ``-Dopenrocket.bypass.motors``
     - Bypass loading motors
   * - ``-Dopenrocket.debug.checkAllVersionUpdates``
     - Check for all software updates, even if you selected "Ignore this update" in the update dialog

Usage
=====

Run as:

.. code-block:: bash

   java -D{argument}={value} -jar OpenRocket.jar

or simply:

.. code-block:: bash

   java -D{argument} -jar OpenRocket.jar

when no value is required.

For instance to set the UI Theme to light:

.. code-block:: bash

   java -Dopenrocket.laf=LIGHT -jar OpenRocket.jar

.. note::
   In the codebase, search for ``System.getProperty(...)`` to search for uses of the different arguments, e.g. ``System.getProperty("openrocket.debug")``.
