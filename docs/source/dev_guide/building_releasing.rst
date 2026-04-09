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

- ``gradle.properties``: Contains project-wide Gradle settings such as warning and parallel build configuration.

- ``core/src/main/resources/build.properties``: Defines the OpenRocket version and other build metadata that are embedded in the application and library artifacts.

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
      - ``publishToMavenLocal``
      - Builds the Maven Central library artifacts for the ``info.openrocket:core`` module and installs them into the local Maven repository for validation.

   *  - core
      - ``publish``
      - Publishes the ``info.openrocket:core`` library artifacts to the configured Sonatype repository. For release versions this also signs the artifacts.

   *  - core
      - ``serializeEngines``
      - Fetch the latest thrust curves from ThrustCurve.org and store them in the OpenRocket SQLite motor database. The resulting ``.db`` file is saved in the ``src`` dir so it can be used for a build.

   *  - core
      - ``serializeEnginesDist``
      - Same as ``serializeEngines``, but loads the SQLite database file to the distribution directory (:file:`openrocket/build`) so it can be used in the final build.

   *  - core
      - ``submoduleUpdate``
      - Updates the submodule dependencies of the *core* module.

Thrust Curve Motor Database
===========================

The internal thrust curve motor database is stored as a SQLite file at
:file:`core/src/main/resources/datafiles/thrustcurves/thrustcurves.db`.
The ``serializeEngines`` task rebuilds this file by downloading motor data from
ThrustCurve.org. At runtime OpenRocket prefers the ``.db`` file and will fall
back to the legacy ``.ser`` file if no SQLite database is found. User-defined
motor directories can also include ``.db`` files; these are validated for the
expected schema before loading.

You can run these tasks from the command line using the Gradle Wrapper scripts. For example for the task ``run``, run the
following command in the root directory of the OpenRocket repository:

.. code-block:: bash

      # On macOS and Linux:
      ./gradlew run

      # On Windows:
      gradlew.bat run

.. _maven-central-publishing:

Maven Central Publishing
========================

OpenRocket now has two distinct release tracks:

- **Current and future releases**: publish proper Maven artifacts from the Gradle build.
- **Older releases**: backfill missing historical versions by checking out the exact tag, producing a Central-compliant bundle for that version, and uploading it once.

The published Maven Central library artifact is currently ``info.openrocket:core``. The root ``shadowJar`` output remains the desktop application distribution artifact and should not be treated as the main Maven Central library coordinate. The ``:swing`` module can be added later if there is a clear need to support it as a separate public library.

For development snapshots, use a Maven-style snapshot version ending in ``-SNAPSHOT`` in :file:`core/src/main/resources/build.properties`, for example ``26.xx-SNAPSHOT``. Do not use ``.SNAPSHOT`` because Sonatype's snapshot repository expects the Maven ``-SNAPSHOT`` suffix.

Central prerequisites
---------------------

Before publishing to Maven Central, make sure the following are available:

- a Sonatype Central account that is authorized for the ``info.openrocket`` namespace
- a Central Portal user token generated from ``https://central.sonatype.com/usertoken``
- ``SONATYPE_USERNAME`` and ``SONATYPE_PASSWORD`` environment variables, or matching Gradle properties ``sonatypeUsername`` and ``sonatypePassword``. These values must be the Portal token username and password, not your GitHub login, full name, or email address.
- an ASCII-armored PGP private key in ``SIGNING_KEY`` and its passphrase in ``SIGNING_PASSWORD``, or matching Gradle properties ``signingKey`` and ``signingPassword``
- Git submodules checked out, because ``:core:processResources`` copies files from the ``openrocket-database`` submodule

You can log in to ``https://central.sonatype.com`` using GitHub, but API publishing still uses the generated Portal token rather than the interactive login credentials. If the ``info.openrocket`` namespace was migrated from OSSRH and does not appear under a GitHub-backed login, sign in with the original OSSRH-linked account or contact Central Support to restore namespace access.

Signing key setup
-----------------

Sonatype requires every published file to be signed with OpenPGP, which means the build needs access to a private signing key and its passphrase.

The OpenRocket Gradle build uses Gradle's in-memory signing support, so it expects:

- ``SIGNING_KEY``: the ASCII-armored private key block
- ``SIGNING_PASSWORD``: the passphrase that protects that private key

A typical setup flow is:

1. Generate a new key pair if you do not already have one:

   .. code-block:: bash

      gpg --full-generate-key

   Use the OpenRocket release identity you want associated with published artifacts. Sonatype's GPG guidance notes that keys often default to an expiration date, so make sure the key remains valid for the expected release window.

2. List the available secret keys and note the key ID:

   .. code-block:: bash

      gpg --list-secret-keys --keyid-format LONG

3. Publish the public key to a public key server so consumers can verify the signatures:

   .. code-block:: bash

      gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>

4. Export the private key in ASCII-armored form for Gradle:

   .. code-block:: bash

      gpg --armor --export-secret-keys <KEY_ID> > openrocket-signing-key.asc

5. Store the exported private key content and its passphrase in your shell or CI secrets:

   .. code-block:: bash

      export SIGNING_KEY="$(cat openrocket-signing-key.asc)"
      export SIGNING_PASSWORD='<key-passphrase>'

In CI, store the full armored key block exactly as a multiline secret value. The passphrase is the one you entered when generating the GPG key.

If the signing key expires, extend it with ``gpg --edit-key <KEY_ID>`` and publish the updated public key to the key server again before the next release.

Current and future releases
---------------------------

The Gradle build publishes the ``:core`` module with:

- the main JAR
- ``-sources.jar``
- ``-javadoc.jar``
- a POM with the metadata required by Maven Central
- PGP signatures for release builds

Use the following process for new releases:

1. Build and validate the library artifacts locally:

   .. code-block:: bash

      export SONATYPE_USERNAME='<portal-token-username>'
      export SONATYPE_PASSWORD='<portal-token-password>'
      export SIGNING_KEY='<ascii-armored-private-key>'
      export SIGNING_PASSWORD='<pgp-passphrase>'

      ./gradlew :core:publishToMavenLocal

2. Publish the release artifacts to Sonatype's staging compatibility endpoint:

   .. code-block:: bash

      ./gradlew :core:publish

   Release versions publish to ``https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/``.
   Snapshot versions publish to ``https://central.sonatype.com/repository/maven-snapshots/``.

3. Transfer the staged repository into the Central Portal. Gradle's built-in ``maven-publish`` support uploads the artifacts, but Sonatype still requires a separate manual upload API call so the deployment appears in the Portal:

   .. code-block:: bash

      AUTH=$(printf "%s:%s" "$SONATYPE_USERNAME" "$SONATYPE_PASSWORD" | base64)

      curl -X POST \
        -H "Authorization: Bearer $AUTH" \
        "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/info.openrocket?publishing_type=automatic"

   Use ``publishing_type=user_managed`` instead of ``automatic`` if you want to inspect and release the deployment manually in the Portal UI.

4. Verify that the new version appears correctly in Central and that the published POM exposes the expected transitive dependencies for ``info.openrocket:core``.

Checking whether publishing succeeded
-------------------------------------

Use different checks for snapshots and official releases.

For ``-SNAPSHOT`` versions:

1. Check the published snapshot metadata in the Central snapshot repository:

   .. code-block:: text

      https://central.sonatype.com/repository/maven-snapshots/info/openrocket/core/<version>/maven-metadata.xml

   Example:

   .. code-block:: text

      https://central.sonatype.com/repository/maven-snapshots/info/openrocket/core/26.xx-SNAPSHOT/maven-metadata.xml

2. Confirm that the metadata lists a timestamped snapshot build such as ``26.xx-20260409.121931-1`` and that the expected files are present:

   - main JAR
   - ``-sources.jar``
   - ``-javadoc.jar``
   - ``.pom``
   - ``.module``

3. Optionally verify consumption from another Gradle build:

   .. code-block:: groovy

      repositories {
          maven {
              name = "Central Portal Snapshots"
              url = uri("https://central.sonatype.com/repository/maven-snapshots/")
              content {
                  includeModule("info.openrocket", "core")
              }
          }
          mavenCentral()
      }

      dependencies {
          implementation("info.openrocket:core:<version>")
      }

``-SNAPSHOT`` publishes do not appear in the Central Portal deployments page and are not the same as final Maven Central releases.

For official non-snapshot releases:

1. Run ``./gradlew :core:publish``.
2. Call the Sonatype manual upload endpoint described above.
3. Open ``https://central.sonatype.com/publishing/deployments`` and confirm that the deployment for ``info.openrocket:core:<version>`` appears and passes validation.
4. After release, verify that the final version is visible in the Central ecosystem and that the artifact can be resolved from ``mavenCentral()`` without the snapshot repository.

Historical backfill
-------------------

Central artifacts are immutable. If a version already exists on Maven Central, do not attempt to republish or replace it.

For historical versions that are missing from Central, work one release tag at a time:

1. Check whether the version already exists on Central. Stop if it does.
2. Check out the exact release tag that should be backfilled.
3. Build artifacts that match that tag exactly.
4. Publish that version once, without reusing files from newer releases.

For Gradle-era tags, prefer the same ``:core`` publication flow described above.

For older Ant-era tags, it is usually easier to create a Central bundle manually instead of retrofitting the modern Gradle publishing pipeline. Build the original binary JAR from that tag, generate a minimal compliant POM, create matching source and javadoc JARs from the checked-out source tree, sign every file, and upload the result as a bundle.

A typical bundle layout looks like this:

.. code-block:: text

   info/openrocket/core/23.09/
     core-23.09.jar
     core-23.09.pom
     core-23.09-sources.jar
     core-23.09-javadoc.jar
     core-23.09.jar.asc
     core-23.09.pom.asc
     core-23.09-sources.jar.asc
     core-23.09-javadoc.jar.asc
     core-23.09.jar.md5
     core-23.09.jar.sha1
     ...

Upload the bundle with the Central Portal API:

.. code-block:: bash

   AUTH=$(printf "%s:%s" "$SONATYPE_USERNAME" "$SONATYPE_PASSWORD" | base64)

   curl --request POST \
     --header "Authorization: Bearer $AUTH" \
     --form bundle=@central-bundle.zip \
     "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC"

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

macOS QuickLook Extension
-------------------------

The macOS installers include a QuickLook extension that allows users to preview ``.ork`` files directly in Finder
(via spacebar or the preview pane). This extension is built and signed separately from the main install4j build,
then merged into the final macOS DMG.

The source code and full instructions for the QuickLook extension are maintained in a separate repository:
`openrocket/macOS-QuickLook-extension <https://github.com/openrocket/macOS-QuickLook-extension>`__.

After building the macOS DMG with install4j, follow the steps in that repository's README to:

1. Build the QuickLook extension using Xcode.
2. Sign and notarize the extension with your Apple Developer ID.
3. Inject the signed extension into the install4j DMG using the ``runit`` script.

.. note::
   The QuickLook extension requires a **Developer ID Application** certificate and an **App-Specific Password**
   for notarization. See the `macOS-QuickLook-extension README <https://github.com/openrocket/macOS-QuickLook-extension#readme>`__
   for the full setup guide, including Apple Developer account configuration and Info.plist requirements.

.. warning::
   The install4j project must declare the ``info.openrocket.ork`` UTI (Uniform Type Identifier) in the macOS
   launcher's file association settings. Without this, macOS will not associate ``.ork`` files with OpenRocket,
   and the QuickLook extension will not activate. See the macOS-QuickLook-extension README for details.

Release Procedure
=================

The release procedure for OpenRocket is as follows:

1. Update the `ReleaseNotes.md <https://github.com/openrocket/openrocket/blob/unstable/ReleaseNotes.md>`__ with the changes that are part of the new release.
   This includes new features, bug fixes, and other changes that are part of the release. Make sure to include the version number and the release date.
   Take a look at the previous release notes to see how it should be formatted.

2. Update the component database and thrustcurves by running the gradle tasks ``subModuleUpdate`` and ``serializeEngines`` respectively.

3. Rerun all example design files (open the design and overwrite the files at :file:`core/src/main/resources/datafiles/examples`
with the new results) to ensure that they are up-to-date with the latest changes.

4. **Update the version number** in ``openrocket/core/src/main/resources/build.properties`` to the correct version number.

   For official releases, the version number should use the format ``YY.MM`` (*year.month*). For example, if the software is released in
   September 2023, the version number should be ``23.09``. If there are multiple releases in the same month, add an incremental number
   to the version number, e.g. ``23.09.01``.

   Development snapshot builds that are meant for Sonatype snapshot publishing should use the same base version with a Maven snapshot suffix, for example ``26.xx-SNAPSHOT``.

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

5. **Build the project JAR file** using Gradle (see above).

6. **Test the JAR file** to ensure that it works correctly and that the new version number is applied to the splash screen and under :menuselection:`Help --> About`.

7. **Publish the Maven Central library artifacts**.

   OpenRocket publishes ``info.openrocket:core`` from the Gradle ``:core`` module. Follow the process in :ref:`maven-central-publishing` to:

   - run ``./gradlew :core:publishToMavenLocal``
   - run ``./gradlew :core:publish``
   - call the Sonatype manual upload endpoint to make the staged deployment appear in the Central Portal

8. **Create the packaged installers** using install4j (see above).

   .. warning::
      Make sure to **enable code signing** for the installers.

      Make sure that `DS_Store <https://github.com/openrocket/openrocket/blob/unstable/install4j/23.09/macOS_resources/DS_Store>`__ for the macOS
      installer is updated. Instructions can be found `here <https://github.com/openrocket/openrocket/blob/unstable/install4j/README.md>`__.

9. **Add the macOS QuickLook extension** to the macOS DMG installers.

   Follow the instructions in the `macOS-QuickLook-extension repository <https://github.com/openrocket/macOS-QuickLook-extension>`__
   to build, sign, notarize, and inject the QuickLook preview extension into each macOS DMG (Apple Silicon and Intel).

10. **Test the installers** to ensure that they work correctly.

11. **Prepare the website** *(for official releases only, not for alpha, beta, or release candidate releases)*.

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

12. **Publish the release on GitHub**.

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

13. **Push the changes to the website**

    First, build the ``development`` branch locally to verify that the changes that you made in step 9 are correct.
    If everything is working (test the download links, the release notes, and the What's new page), create a new PR
    that merges the changes from the ``development`` branch to the ``master`` branch.

14. **Send out the release announcement**.

    Send out the release announcement to the OpenRocket mailing list, the TRF forum, and the OpenRocket social media channels
    (Discord, Facebook...).

    The announcement should include the new features, bug fixes, and other changes that are part of the new release.
    Make sure to include the download links to the new release. Here is an `example announcement <https://www.rocketryforum.com/threads/announcement-openrocket-23-09-is-now-available-for-download.183186/>`__.

15. **Merge the** ``unstable`` **branch to the** ``master`` **branch**.

    After the release is published, merge the changes from the `unstable <https://github.com/openrocket/openrocket>`__ branch
    to the `master <https://github.com/openrocket/openrocket/tree/master>`__ branch.

16. **Upload the new release to** `SourceForge <https://sourceforge.net/projects/openrocket/>`__.

    The downloads page on SourceForge is still very actively used, so be sure to upload the new release there as well.

17. **Update package managers** (e.g. snap, Chocolatey, Homebrew) with the new release.

Snap
====

The OpenRocket snap package is automatically updated using the `Snapcraft <https://snapcraft.io/>`__ build system.
However, the snap package's 'latest/stable' release needs to be manually updated after a new OpenRocket release is
published.

On a Linux system with snap installed, run the following command to build the new snap package:

.. code-block:: bash

      snapcraft remote-build

This will create a file named ``openrocket_<version>_<arch>.snap`` in the current directory.
Note that it may take up to 30 minutes for each architecture to be built.
To publish the new snap package, run the following command (separate commands for each ``<arch>``):

.. code-block:: bash

      snapcraft upload --release=stable openrocket_<version>_<arch>.snap

A similar procedure can be used to publish release candidates or beta versions by changing the ``--release`` option to
``candidate`` or ``beta``.

For developers with access to the OpenRocket Snapcraft account, more information can be found at the `snapcraft.io page <https://snapcraft.io/openrocket/releases>`__.
