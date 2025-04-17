**********************
Development Guidelines
**********************

In order to maintain a high level of code quality and improve the efficiency for other developers to verify your code,
we have established the following guidelines for contributing to the project.

.. contents:: Table of Contents
   :depth: 2
   :local:

----

Coding Standards
================

Where possible, write unit tests for your code (see :doc:`Testing and Debugging </dev_guide/testing_and_debugging>`).
This will help to ensure that your code is correct, and will help to prevent regressions in the future.
Also include edge cases in your tests.

Use `atomic commits <https://en.wikipedia.org/wiki/Atomic_commit>`__. Each commit should be a single logical change.
Don't make several logical changes in one commit. For example, if a patch fixes a bug and optimizes the performance of a
feature, it should be split into two separate commits.

Commit messages should be clear, concise, and useful. The first line should be a short description of the commit, then a blank line,
then a more detailed explanation. The commit message should be in the present tense. For example, "Fix bug" and not "Fixed bug".

If applicable, include a reference to the issue that the commit addresses. For example, if you're working on a fix for GitHub
issue #123, then format your commit message like this: "[#123] Fix bug". This makes it easier to track which commits are
associated with which issues.

Pull Request Process
====================

When submitting a pull request to the OpenRocket repository, please follow these guidelines:

1. **Fork the Repository**: Start by forking the OpenRocket repository to your own GitHub account.

2. **Create a Branch**: Create a branch in your fork for your changes. Use a descriptive name that reflects the purpose of your changes.

3. **Make Your Changes**: Implement your changes following the coding standards described above.

4. **Test Your Changes**: Make sure your changes work as expected and don't break existing functionality. Run the relevant tests.

5. **Submit a Pull Request**: When you're ready, submit a pull request from your branch to the OpenRocket repository.

6. **Describe Your Changes**: In the pull request description, provide a clear explanation of what your changes do and why they are needed. Reference any related issues.

7. **Review Process**: Be prepared to respond to feedback and make changes if requested by the maintainers.

8. **Continuous Integration**: The pull request will be automatically tested by the CI system. Make sure all tests pass.

9. **Merge**: Once your pull request is approved, it will be merged into the main codebase.

Code Style
==========

OpenRocket follows these code style guidelines:

1. **Indentation**: Use 4 spaces for indentation, not tabs.

2. **Line Length**: Try to keep lines under 120 characters.

3. **Naming Conventions**:
   - Classes: Use PascalCase (e.g., `RocketComponent`)
   - Methods and variables: Use camelCase (e.g., `getAltitude()`)
   - Constants: Use UPPER_SNAKE_CASE (e.g., `MAX_ALTITUDE`)

4. **Comments**: Use Javadoc comments for classes, methods, and fields. Include descriptions, parameter explanations, return value descriptions, and exception information.

   .. code-block:: java

      /**
       * Calculates the apogee of the rocket.
       *
       * @param simulation The simulation to use for the calculation
       * @return The apogee altitude in meters
       * @throws SimulationException If the simulation fails
       */
      public double calculateApogee(Simulation simulation) throws SimulationException {
          // Implementation
      }

5. **Imports**: Organize imports and avoid wildcard imports.

6. **Exception Handling**: Always handle or propagate exceptions appropriately. Don't catch exceptions without proper handling.

7. **Resource Management**: Always close resources (files, streams, etc.) in a finally block or use try-with-resources.

Documentation Guidelines
========================

When adding or modifying code, please follow these documentation guidelines:

1. **Javadoc**: Add Javadoc comments to all public classes, methods, and fields.

2. **Code Comments**: Add inline comments to explain complex or non-obvious code.

3. **User Documentation**: Update the user documentation if your changes affect user-facing features.

4. **Developer Documentation**: Update the developer documentation (like this guide) if your changes affect the development process or API.

5. **Examples**: Provide examples for new features or APIs.

Checkstyle
==========

OpenRocket uses Checkstyle to enforce code style rules. You can run Checkstyle with:

.. code-block:: bash

   ./gradlew checkstyleMain checkstyleTest

Fix any Checkstyle violations before submitting your pull request.
