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
