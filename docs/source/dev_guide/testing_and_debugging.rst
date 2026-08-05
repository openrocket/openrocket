*********************
Testing and Debugging
*********************

This guide provides information on how to test and debug OpenRocket. It covers unit testing, debugging techniques, and best practices for ensuring code quality.

Unit Testing
============

OpenRocket uses JUnit 5 (Jupiter) as its testing framework. The test code is organized in separate directories from the main code:

* :file:`core/src/test/java` - Tests for the core functionality
* :file:`swing/src/test/java` - Tests for the swing UI components

Running Tests
-------------

You can run tests using Gradle:

.. code-block:: bash

   # Run all tests
   ./gradlew test

   # Run tests for a specific module
   ./gradlew core:test
   ./gradlew swing:test

   # Run a specific test class
   ./gradlew core:test --tests "info.openrocket.core.util.MathUtilTest"

Test Structure
--------------

Tests in OpenRocket follow standard JUnit conventions:

* Test classes are named with a ``Test`` suffix (e.g., ``MathUtilTest``)
* Test methods are annotated with ``@Test``
* Many tests extend ``BaseTestCase`` which provides common functionality

Example Test
------------

Here's a simple example of a test class:

.. code-block:: java

   package info.openrocket.core.util;

   import org.junit.jupiter.api.Test;
   import static org.junit.jupiter.api.Assertions.*;

   public class ExampleTest extends BaseTestCase {

       @Test
       public void testSomeFeature() {
           // Arrange
           SomeClass instance = new SomeClass();

           // Act
           int result = instance.someMethod();

           // Assert
           assertEquals(42, result);
       }
   }

Writing Good Tests
------------------

When writing tests for OpenRocket, follow these guidelines:

1. Test one thing per test method
2. Use descriptive test method names that explain what is being tested
3. Structure tests with Arrange-Act-Assert pattern
4. Mock external dependencies when appropriate
5. Test edge cases and error conditions
6. Keep tests independent of each other

3D Golden-Image Tests
---------------------

The 3D engine has live-OpenGL regression tests for the design render modes, Photo Studio, and weighted
blended transparency:

* ``Figure3DRenderModesGoldenTest`` covers Figure, Unfinished, and Finished modes with MSAA enabled and
  disabled.
* ``PhotoStudioGoldenTest`` covers the normal Photo Studio view, alternate lighting, flame, smoke,
  sparks, and motion blur.
* ``WeightedBlendedTransparencyGoldenTest`` covers intersecting translucent geometry and verifies that
  its result does not depend on scene-object order.

Run all three classes with:

.. code-block:: bash

   ./gradlew :swing:test \
     --tests "info.openrocket.swing.gui.figure3d.rendering.Figure3DRenderModesGoldenTest" \
     --tests "info.openrocket.swing.gui.figure3d.photo.PhotoStudioGoldenTest" \
     --tests "info.openrocket.swing.gui.figure3d.rendering.WeightedBlendedTransparencyGoldenTest"

These tests require a live graphical environment and are skipped in headless environments. Each test
writes its newly rendered candidate to :file:`swing/build/visual-regression` before comparing it with the
approved baseline under :file:`swing/src/test/resources/figure3d`.

When a baseline is not present, the test is aborted with an ``awaiting visual approval`` message after
writing the candidate. Inspect that image, copy it to the matching filename in the test resources only if
it is correct, and rerun the test. Do not update a baseline merely to make a changed image pass; first
confirm that the visual change is intended.

Code Coverage
=============

OpenRocket uses the `JaCoCo <https://www.jacoco.org/>`_ plugin to track and enforce
test coverage. Coverage verification is integrated into the build process to ensure
code quality.

- **Core module:** minimum 60% coverage threshold  
- **Swing module:** threshold currently disabled (0%) due to limited test coverage  
- **Aggregate reports:** generated via the ``jacocoRootReport`` task

If coverage thresholds are not met, JAR packaging will fail. This prevents deployment
of code that does not meet the required quality standards.

Usage
-----

To generate coverage reports:

.. code-block:: bash

   ./gradlew test jacocoRootReport

To build with coverage verification:

.. code-block:: bash

   ./gradlew build

Coverage verification runs automatically during JAR packaging. Detailed HTML
reports are available under ``build/reports/jacoco/`` (aggregate report), ``core/build/reports/jacoco/``, and
``swing/build/reports/jacoco/`` for review. Additionally,
a GitHub Action publishes coverage reports for easier tracking in CI.



Debugging
=========

The most powerful debugging tool is your IDE's debugger. Both IntelliJ IDEA and Eclipse provide excellent debugging capabilities:

1. Set breakpoints in your code
2. Run OpenRocket in debug mode
3. Inspect variables and step through code execution

3D Diagnostics
--------------

The following JVM properties are intended for diagnosing the 3D engine rather than as persistent user
preferences:

``-Dopenrocket.gl.debug=true``
   Install the OpenGL debug-message callback for each context when the driver supports it. The environment
   variable ``OPENROCKET_GL_DEBUG=true`` enables the same behavior.

``-Dopenrocket.figure3d.glDebug=true``
   Poll ``glGetError`` at the engine's debug checkpoints. This can stall the GPU and is considerably more
   expensive than normal rendering.

``-Dopenrocket.figure3d.debug=true``
   Enable extra Photo Studio panel and canvas lifecycle logging.

``-Dopenrocket.figure3d.forceConstrainedGpu=true``
   Force the reduced-memory GPU profile. Use ``false`` to force the full profile instead of automatic
   detection.

``-Dopenrocket.figure3d.msaaSamples=N``
   Override the sample count of the renderer's off-screen scene target. This is the target controlled by
   the MSAA preference during normal use.

``-Dopenrocket.figure3d.samples=N``
   Override samples requested for the AWT default framebuffer. The normal value is zero because the scene
   resolves its own MSAA before presentation; nonzero values can prevent context creation on some Windows
   drivers and virtual machines.

``-Dopenrocket.figure3d.disableRobustContext=true``
   Do not request the Windows robust-context attributes used to detect graphics resets.

``-Dopenrocket.3d.disable``
   Disable the 3D engine entirely when a context failure otherwise prevents OpenRocket from starting.

The bug-report dialog includes the most recently detected GPU memory profile and both the requested and
effective lwjgl3-awt ``GLData`` values, so open a 3D view before collecting a report when possible.
