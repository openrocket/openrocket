**********************
Building and Releasing
**********************

This guide explains the build system of OpenRocket (Gradle), and how to release a new version of OpenRocket.

.. contents:: Table of Contents
   :depth: 2
   :local:

----

Gradle
======

`Gradle <http://www.gradle.org/>`__ is the build system for OpenRocket. It is used to compile the source code, run tests, and create the JAR file.
Key features of Gradle are:

- **Incremental builds**: Gradle only rebuilds what is necessary, which makes the build process faster.

- **Dependency management**: Gradle has a robust dependency management system capable of handling project and third-party libraries.

- **Performance**: Gradle uses techniques like build caching and parallel execution to improve performance of the build process.

The root directory of the OpenRocket repository contains several Gradle files:

- ``build.gradle``: This is the main build script file where you define your project configuration and tasks such as compile and run tasks, dependency management, plugins usage, and more.

- ``settings.gradle``: Used for multi-project build configurations to include which sub-projects should be part of the build.
  For OpenRocket, this file is used to identify the ``core`` and ``swing`` sub-projects.

- ``gradle.properties``: Contains project-wide properties that can be accessed from the build script. For example, the version number of OpenRocket can be defined here.

- ``gradlew`` and ``gradlew.bat``: These are Gradle Wrapper scripts for Unix-based and Windows systems respectively.
  It allows users to run Gradle builds without requiring Gradle to be installed on the system.

The ``core`` and ``swing`` sub-projects contain their own ``build.gradle`` and ``gradle.properties`` files that define the tasks specific to those sub-projects.

Gradle in IntelliJ
------------------

If you use IntelliJ IDEA, you can access the Gradle tasks within the IDE. First, open the Gradle tool window by going to
:menuselection:`View --> Tool Windows --> Gradle` or by clicking on the Gradle icon in the right-hand side of the window:

.. figure:: /img/dev_guide/building_releasing/gradle_in_intellij.png
   :align: center
   :width: 80%
   :alt: Opening the Gradle tool window in IntelliJ IDEA.

   Opening the Gradle tool window in IntelliJ IDEA.

This shows the following window:

.. figure:: /img/dev_guide/building_releasing/intellij_gradle_window.png
   :align: center
   :width: 30%
   :alt: The Gradle tool window in IntelliJ IDEA.

   The Gradle tool window in IntelliJ IDEA.

Here's a breakdown of the Gradle tasks:

- *info.openrocket*: the root project
   - *Tasks*: Gradle tasks specific to the root project.
      - *application*: Contains tasks related to running or debugging your application from within the IDE.
      - *build*: Includes tasks for building the entire project.
      - *build setup*: Tasks for initializing a new Gradle build, such as creating new Gradle files.
      - *distribution*: Tasks for assembling the application distribution, like creating zips or tarballs of the build outputs.
      - *documentation*: Tasks for generating documentation, typically using tools like Javadoc.
      - *help*: Provides tasks that list other tasks or project properties.
      - *info.openrocket*: Custom tasks specific to the 'info.openrocket' module.
      - *other*: Any other tasks that do not fit into the predefined categories.
      - *shadow*: Related to the Shadow plugin, which packages the project’s artifacts along with its dependencies into a single "fat" JAR.
      - *verification*: Tasks for testing and verifying the project, such as running unit tests.
   - *Dependencies*: Lists the dependencies of the project.
   - *Run Configurations*: Gradle run configurations that can be used in IntelliJ.
- *core*: the core module
   - *Tasks*: Gradle tasks specific to the 'core' module.
   - *Dependencies*: Lists the dependencies of the 'core' module.
- *swing*: the swing module
   - *Tasks*: Gradle tasks specific to the 'swing' module.
   - *Dependencies*: Lists the dependencies of the 'swing' module.

Most Important Gradle Tasks
---------------------------

Here are some of the most important Gradle tasks for OpenRocket:

.. list-table:: Most Important Gradle Tasks
   :widths: 25 25 50
   :header-rows: 1

   *  - Module
      - Task
      - Description

   *  - root (*info.openrocket*)
      - ``clean``
      - Deletes the build directory and all its contents (basically cleans up the project).

   *  - root (*info.openrocket*)
      - ``run``
      - Runs the OpenRocket application.

   *  - root (*info.openrocket*)
      - ``check``
      - Runs the unit tests and checks the code quality using the Checkstyle static analysis tool.

   *  - root (*info.openrocket*)
      - ``build``
      - Compiles the source code, runs the unit tests, and creates the JAR file for the *core* and *swing* module.

   *  - root (*info.openrocket*)
      - ``dist``
      - Creates a distributable JAR file of OpenRocket (a combination of the *core* and *swing* JAR) at :file:`openrocket/build/libs/OpenRocket-<build-version>.jar`.

   *  - core
      - ``serializeEngines``
      - Fetch the latest thrust curves from ThrustCurve.org and serialize them to the OpenRocket format. The resulting serialized file is saved in the ``src`` dir so it can be used for a build.

   *  - core
      - ``serializeEnginesDist``
      - Same as ``serializeEngines``, but loads the serialized file to the distribution directory (:file:`openrocket/build`) so it can be used in the final build.

   *  - core
      - ``submoduleUpdate``
      - Updates the submodule dependencies of the *core* module.

You can run these tasks from the command line using the Gradle Wrapper scripts. For example for the task ``run``, run the
following command in the root directory of the OpenRocket repository:

.. code-block:: bash

      # On macOS and Linux:
      ./gradlew run

      # On Windows:
      gradlew.bat run

install4j
=========

`install4j <http://www.ej-technologies.com/products/install4j/overview.html>`__ is used to create the packaged installers for OpenRocket from the JAR file.
install4j generously provides a free license for open source projects, including OpenRocket. Currently, only the OpenRocket administrators have access
to the install4j license.

Code Signing
------------

An important part of generating the installers is `code signing <https://en.wikipedia.org/wiki/Code_signing>`__.
This is done to ensure that the installer is not tampered with between the time it is created and the time it is run by the user.
Once the OpenRocket installer has been code signed, users will receive no more (or the minimum amount of) warnings from
their operating system that the installer is from an unknown source and may contain malware.
More information on how to do code signing in install4j can be found `here <https://www.ej-technologies.com/resources/install4j/help/doc/concepts/codeSigning.html>`__.

Only the OpenRocket administrators have access to the code signing certificates.

Code signing for Windows is done using a digital certificate from Sectigo. More information on the code signing procedure,
including whitelisting OpenRocket by Microsoft, see the `README file on GitHub <https://github.com/openrocket/openrocket/blob/unstable/install4j/README.md>`__.

For macOS, the code signing is done using an Apple Developer ID. Besides code signing, the OpenRocket app also needs to
be notarized. Luckily, install4j takes care of this. More information on the code signing procedure for macOS can be found in the
`README file on GitHub <https://github.com/openrocket/openrocket/blob/unstable/install4j/README.md>`__.

Linux does not require code signing.

Creating the Installers
-----------------------

First you need to build the project using Gradle (see above). This will create the JAR file that will be used to create the installers.

Then, open install4j (requires a license) and load the project file *openrocket/install4j/<build-version>/openrocket-<build-version>.install4j*
from the repository. Go to the :menuselection:`Build` tab and click on the :guilabel:`Start Build` button. This will create the installers in
the *openrocket/install4j/<build-version>/media/* directory.

.. figure:: /img/dev_guide/building_releasing/install4j_build.png
   :align: center
   :width: 80%
   :alt: Building the installers in install4j.

   Building the installers in install4j.

If you do not have access to the code signing certificates, you can create the installers without code signing by
enabling the checkboxes ``Disable code signing`` and ``Disable notarization`` in the ``Build`` tab.

Release Procedure
=================

The release procedure for OpenRocket is as follows:

1. Update the `ReleaseNotes.md <https://github.com/openrocket/openrocket/blob/unstable/ReleaseNotes.md>`__ with the changes that are part of the new release.
   This includes new features, bug fixes, and other changes that are part of the release. Make sure to include the version number and the release date.
   Take a look at the previous release notes to see how it should be formatted.

2. Update the component database and thrustcurves by running the gradle tasks ``subModuleUpdate`` and ``serializeEnginesDist`` respectively.

3. **Update the version number** in ``openrocket/core/src/main/resources/build.properties`` to the correct version number.

   For official releases, the version number should use the format ``YY.MM`` (*year.month*). For example, if the software is released in
   September 2023, the version number should be ``23.09``. If there are multiple releases in the same month, add an incremental number
   to the version number, e.g. ``23.09.01``.

   If a new release contains significant changes, it may be necessary to release alpha or beta versions first. In that case, the version
   number should be appended with ``.alpha.`` or ``.beta.`` plus an incremental number. For example, if the software is in beta stage
   in September 2023, the version number should be ``23.09.beta.01``. In general, alpha releases are not necessary. This is only for very rough releases.
   Beta releases are only necessary if there are significant changes that need to be tested by the community before the final release.

   One final option is to release a release candidate (RC) version. This is a version that is considered to be the final version,
   but needs to be tested by the community before the final release. The version number should be appended with ``.RC.`` plus an incremental number.
   For example, if the software is in RC stage in September 2023, the version number should be ``23.09.RC.01``.

   The official release that comes after the beta release should have the same version number as the beta release, but without the ``.beta.`` part.
   For instance, if the beta testing started in September 2023 with version number ``23.09.beta.01``, the final release should have version number ``23.09``,
   even if the final release is in November 2023. This is to ensure consistency in the version numbering and to link the beta release(s) to the final release.

4. **Build the project JAR file** using Gradle (see above).

5. **Test the JAR file** to ensure that it works correctly and that the new version number is applied to the splash screen and under :menuselection:`Help --> About`.

6. **Create the packaged installers** using install4j (see above).

   .. warning::
      Make sure to **enable code signing** for the installers.

      Make sure that `DS_Store <https://github.com/openrocket/openrocket/blob/unstable/install4j/23.09/macOS_resources/DS_Store>`__ for the macOS
      installer is updated. Instructions can be found `here <https://github.com/openrocket/openrocket/blob/unstable/install4j/README.md>`__.

7. **Test the installers** to ensure that they work correctly.

8. **Prepare the website** *(for official releases only, not for alpha, beta, or release candidate releases)*.

   The `source code for the website <https://github.com/openrocket/openrocket.github.io>`__ needs to be updated to point to the new release.
   Follow these steps:

   - Add the release to `downloads_config.json <https://github.com/openrocket/openrocket.github.io/blob/development/assets/downloads_config.json>`__.
   - Update the ``current_version`` in `_config <https://github.com/openrocket/openrocket.github.io/blob/development/_config.yml>`__.
   - Add a new entry to `_whats_new <https://github.com/openrocket/openrocket.github.io/tree/development/_whats-new>`__ for the new release.
     Create a ``wn-<version number>.md`` file with the changes that are part of the new release. Please take a close look to the previous entries to see how it should be formatted.
   - Update the `release notes <https://github.com/openrocket/openrocket.github.io/blob/development/_includes/ReleaseNotes.md>`__
     (which is a link to the What's new file that you just created). Again, take a close look at the previous entries to see how it should be formatted.

   .. warning::
      Make sure to **update the website on the** ``development`` **branch**. The ``master`` branch is the branch that is live
      on the website. First update the ``development`` branch and test the changes on the website. In a later step, the
      changes will be merged to the ``master`` branch.

9. **Publish the release on GitHub**.

   Go to the `releases page <https://github.com/openrocket/openrocket/releases>`__. Click *Draft a new release*.
   Select *Choose a tag* and enter a new tag name, following the format ``release-<version number>``, e.g. ``release-23.09``.
   The title should follow the format ``OpenRocket <version number> (<release date as YYYY-MM-DD>)``, e.g. ``OpenRocket 23.09 (2023-11-16)``.

   Fill in the release text, following the `ReleaseNotes.md <https://github.com/openrocket/openrocket/blob/unstable/ReleaseNotes.md>`__.
   If you want to credit the developers who contributed to the release, you can tag them anywhere in the release text using the `@username` syntax.
   They will then be automatically displayed in the contributors list on the release page.

   Finally, upload all the packaged installers and the JAR file to the release. The source code (zip and tar.gz) is
   automatically appended to each release, you do not need to upload it manually.

   If this is an alpha, beta, or release candidate release, tick the *Set as a pre-release* checkbox.

   Click *Publish release*.

10. **Push the changes to the website**

   First, build the ``development`` branch locally to verify that the changes that you made in step 8 are correct.
   If everything is working (test the download links, the release notes, and the What's new page), create a new PR
   that merges the changes from the ``development`` branch to the ``master`` branch.

11. **Send out the release announcement**.

    Send out the release announcement to the OpenRocket mailing list, the TRF forum, and the OpenRocket social media channels
    (Discord, Facebook...).

    The announcement should include the new features, bug fixes, and other changes that are part of the new release.
    Make sure to include the download links to the new release. Here is an `example announcement <https://www.rocketryforum.com/threads/announcement-openrocket-23-09-is-now-available-for-download.183186/>`__.

12. **Merge the** ``unstable``` **branch to the** ``master``` **branch**.

    After the release is published, merge the changes from the `unstable <https://github.com/openrocket/openrocket>`__ branch
    to the `master <https://github.com/openrocket/openrocket/tree/master>`__ branch.

13. **Upload the new release to** `SourceForge <https://sourceforge.net/projects/openrocket/>`__.

   The downloads page on SourceForge is still very actively used, so be sure to upload the new release there as well.

14. **Update package managers** (e.g. snap, Chocolatey, Homebrew) with the new release.
