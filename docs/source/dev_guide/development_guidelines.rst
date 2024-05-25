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