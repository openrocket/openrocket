*********************** 
FAQ and Troubleshooting
***********************

This page contains frequently asked questions and troubleshooting tips for developers working with the OpenRocket codebase.

Frequently Asked Questions
==========================

.. rubric:: Q: Why does OpenRocket still use the "ancient" Java language? Python and web-based programs are the future.

A: Java is a very powerful language and is still widely used in the industry. While it is true that Java is not as popular
with (new) developers as Python or web-based languages, there are currently no plans to rewrite the codebase in another
language. The main reason for this is that it would take a lot of time to rewrite the codebase, more time than the current
OpenRocket developers can afford to miss out on their (limited) spare time. Besides, the current Java codebase is
still working fine and has been tested by many users over the years.

That being said, maintaining a Java application does have challenges, mainly in ensuring it runs on all platforms and
hardware configurations. Maintaining the GUI and 3D view has proven to be very challenging. Additionally, Java cannot be
run on mobile devices such as iOS devices. If you are a developer and would like to contribute to the codebase rewrite,
please contact the OpenRocket developers to see what you could do to help!

.. rubric:: Q: How do I set up my development environment?

A: Please refer to the :doc:`Development Setup </dev_guide/development_setup>` guide for detailed instructions on
setting up your development environment.

.. rubric:: Q: How do I run OpenRocket from the source code?

A: After setting up your development environment, you can run OpenRocket in your preferred IDE by running
the :command:`main` method in either :file:`swing/src/main/java/info/openrocket/swing/startup/SwingStartup.java` or
:file:`swing/src/main/java/info/openrocket/swing/startup/OpenRocket.java`, or run the following Gradle command:

.. code-block:: bash

   ./gradlew run

.. rubric:: Q: How do I contribute to OpenRocket?

A: Please refer to the :doc:`Development Guidelines </dev_guide/development_guidelines>` for information on how to
contribute to OpenRocket, including the pull request process.

.. rubric:: Q: Where can I find the API documentation?

A: Please refer to the :doc:`API Documentation </dev_guide/api_documentation>` for information on the OpenRocket API.

.. rubric:: Q: How do I run tests?

A: Please refer to the :doc:`Testing and Debugging </dev_guide/testing_and_debugging>` guide for information on how to run tests.

Troubleshooting
===============

Build Issues
------------

.. rubric:: Issue: Gradle build fails with "Could not find or load main class"

This can happen if your Java environment is not set up correctly. Make sure you have Java 17 or later installed and
that your JAVA_HOME environment variable is set correctly.

.. code-block:: bash

   # Check your Java version
   java -version

   # Check your JAVA_HOME environment variable
   echo $JAVA_HOME

.. rubric:: Issue: Gradle build fails with dependency resolution errors

Try cleaning your Gradle cache and rebuilding:

.. code-block:: bash

   ./gradlew clean
   ./gradlew build

Runtime Issues
--------------

.. rubric:: Issue: 3D view doesn't work or crashes

The 3D view uses JOGL (Java OpenGL), which can be problematic on some systems. You can disable the 3D engine by running
OpenRocket with the JVM argument ``-Dopenrocket.3d.disable``.

Getting Help
============

If you're still having issues, you can:

1. Check the GitHub Issues: https://github.com/openrocket/openrocket/issues
2. Join the OpenRocket Discord: https://discord.gg/qD2G5v2FAw
3. Post on the Rocketry Forum: https://www.rocketryforum.com/forums/rocketry-electronics-software.36/
4. Open a discussion item on GitHub: https://github.com/openrocket/openrocket/discussions
