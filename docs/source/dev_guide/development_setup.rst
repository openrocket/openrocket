*****************************
Development Environment Setup
*****************************

This guide will walk you through setting up the development environment to build OpenRocket from the source code.

.. contents:: Table of Contents
   :depth: 2
   :local:

----

Prerequisites
=============

- `JDK 17 <https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html>`__. OpenRocket is developed using Java 17,
  so you will need to install it to build and run OpenRocket. If you have multiple versions of Java installed, ensure that
  Java |java_vers| is the default version.

- `Git <https://git-scm.com/downloads>`__. Git is a version control system that is used to manage the source code for OpenRocket.
  You will need to install Git to clone the OpenRocket repository.

- `GitHub Account <https://github.com>`__. GitHub is a platform for hosting Git repositories. You will need a GitHub account to
  fork the OpenRocket repository and submit pull requests.

- `Gradle <https://gradle.org/install/>`__. OpenRocket uses Gradle as its build system. You will need to install Gradle to build OpenRocket.

Obtaining the Source Code
=========================

The source code for OpenRocket is hosted on `GitHub <https://github.com/openrocket/openrocket>`__. However, you cannot change
this code directly. This is because the OpenRocket repository is the official repository for the project, and only the project
maintainers can make changes to it. This is to ensure that the codebase remains stable and consistent.
Instead, you must fork the OpenRocket repository, which creates a personal copy of the repository that you can make changes to.
You can then submit a pull request to the OpenRocket repository to propose your changes.

Forking the Repository
----------------------

The first step is to fork the OpenRocket repository. As mentioned earlier, the OpenRocket repository is the official repository
for the project, and only the project maintainers can make changes to it.

Go to the OpenRocket repository on GitHub (`link <https://github.com/openrocket/openrocket>`__) and click the :guilabel:`Fork` button:

.. figure:: /img/dev_guide/development_setup/fork_repo.png
   :align: center
   :width: 90%
   :alt: Forking the official OpenRocket repository.

   Forking the official OpenRocket repository on `github.com/openrocket/openrocket <https://github.com/openrocket/openrocket>`__.

You can leave the default settings and click ``Create fork``. This will create a copy of the OpenRocket repository in your GitHub account:

.. figure:: /img/dev_guide/development_setup/forked_repo.png
   :align: center
   :width: 80%
   :alt: Your forked repo.

   Your forked repo.

You can always retrieve your forked repository under your GitHub account, under ``Your repositories``, or by visiting the URL
``https://github.com/<your_username>/openrocket`` (replace ``<your_username>``
with your actual username).

Cloning the Repository
----------------------

Now that you have forked the OpenRocket repository, you can clone it to your local machine. To do this, open a terminal
and run the following command (replace ``[YOUR USERNAME]`` with your GitHub username):

.. code-block:: bash

   # Use the following command if you have set up SSH keys with GitHub
   git clone git@github.com:[YOUR USERNAME]/openrocket.git

   # Otherwise, clone the repository using HTTPS
   git clone https://github.com/[YOUR USERNAME]/openrocket.git

This will clone the OpenRocket repository to your local machine. You can now make changes to the code and push them to your forked repository.

One final step you need to do is to initialize the submodules. OpenRocket uses submodules for some of its dependencies.
To initialize the submodules, run the following commands:

.. code-block:: bash

   git submodule init
   git submodule update


Keeping your Fork in Sync
-------------------------

Once you have forked the OpenRocket repository, you will need to keep your fork in sync with the official repository. This is because
the official repository may have changes that are not in your fork, and you will want to keep your fork up-to-date with the latest changes.
For example, in the following image you can see that your fork is 10 commits behind the official repository:

.. figure:: /img/dev_guide/development_setup/forked_repo_outdated.png
   :align: center
   :width: 80%
   :alt: An outdated forked repo.

   An outdated forked repo.

Luckily, GitHub makes it easy to keep your fork in sync with the official repository. You can do this by clicking the
``Sync fork`` button on your forked repository page and then clicking the :guilabel:`Update branch` button:

.. figure:: /img/dev_guide/development_setup/sync_fork.png
   :align: center
   :width: 80%
   :alt: Syncing your forked repo on GitHub.

   Syncing your forked repo on GitHub.

If all went well, your fork should now be up-to-date with the official repository:

.. figure:: /img/dev_guide/development_setup/forked_repo_up_to_date.png
   :align: center
   :width: 80%
   :alt: An up-to-date forked repo.

   An up-to-date forked repo.

.. warning::
      It is important to keep your fork in sync with the official repository. If you don't, you may encounter conflicts
      when you try to submit a pull request.

      **Regularly check your forked repository to see if it is behind the official repository**. If it is, sync your fork!

Now you have updated your fork, but you still need to update your local repository (your clone).
To do this, you need to fetch the changes from the official repository and pull them into your local repository.
You can do this by running the following commands:

.. code-block:: bash

   git fetch && git pull

Setting Up the Development Environment
======================================

This section will guide you through setting up the development environment to build OpenRocket from the source code.

.. _setup_intellij:

IntelliJ IDEA
-------------

`IntelliJ IDEA <https://www.jetbrains.com/idea/>`__ is a popular Java IDE that is used by many developers. It has a lot of
features that make it easier to develop Java applications. We **highly** recommend using IntelliJ IDEA for developing
OpenRocket. You can download the Community Edition for free from the `JetBrains website <https://www.jetbrains.com/idea/download>`__
(scroll down to “IntelliJ IDEA Community Edition” and click the download button).

Once you have downloaded and installed IntelliJ IDEA, you can open the OpenRocket project:

1. **Start IntelliJ IDEA**

2. **Import the OpenRocket project**

   In IntelliJ, select :menuselection:`File --> New --> Project from Existing Sources...`. This will open a file dialog.
   Navigate to the directory where you cloned OpenRocket and select the :file:`build.gradle` file in the root :file:`openrocket`
   directory and click :guilabel:`Open`.

3. **Import Project as Gradle Project**

   IntelliJ should automatically detect that this is a Gradle project. If prompted, select ``Load Gradle Project``.

   .. figure:: /img/dev_guide/development_setup/load_gradle_project.png
      :align: center
      :width: 80%
      :alt: Load Gradle Project.

      IntelliJ IDEA will automatically detect that this is a Gradle project and prompt you to load it. Click ``Load Gradle Project``.

   If you do not have this pop-up or if you have dismissed it, you can still import the project as a Gradle project.
   Open the :file:`build.gradle` file in the root :file:`openrocket` directory in IntelliJ (double-click the file in
   IntelliJ's project view). Then right-click anywhere in the file and select :menuselection:`Link Gradle Project`.

   .. figure:: /img/dev_guide/development_setup/IntelliJ-GradleLink.png
         :align: center
         :width: 80%
         :alt: Linking the Gradle project from the :file:`build.gradle` file.

         Linking the Gradle project from the :file:`build.gradle` file.

4. **Configure JDK for the Project**

   - Go to :menuselection:`File --> Project Structure --> (Project Settings -->) Project`.
   - Set the Project SDK to JDK |java_vers|.

     .. figure:: /img/dev_guide/development_setup/project_sdk.png
        :align: center
        :width: 80%
        :alt: Set the project SDK.

        Set the project SDK to JDK |java_vers|.

     If JDK |java_vers| is not listed, you can download it from the Project Structure dialog by \
     going to :menuselection:`(Platform Settings -->) SDKs`, clicking the :guilabel:`+` button, and selecting ``Download JDK...``. Then select \
     version |java_vers| and any vendor (e.g. OpenJDK, Amazon Corretto, ...).

   - Confirm in the Project Structure dialog under :menuselection:`(Project Settings -->) Modules` that the SDK in each module is set to JDK |java_vers|. \
     If not, you can change it by selecting the module and setting the SDK in the right pane. Ensure that the list view on the bottom-right \
     does not show ``<No SDK>``. If it does, click the *Module SDK* dropdown and click (again) on the JDK |java_vers| SDK.

   .. figure:: /img/dev_guide/development_setup/modules_sdk.png
      :align: center
      :width: 80%
      :alt: Set the module SDK.

      Set the module SDK to JDK |java_vers|.

5. **Run the Application**
   By default, IntelliJ should be set up with 3 run configurations:

   - ``SwingStartup``: Run the application directly from within IntelliJ. You will user this configuration most of the time. \
     You can also run IntelliJ in debug mode by clicking the green bug icon next to the play button.

   - ``openrocket-jar``: Run all the unit tests and build the application as a JAR file.

   - ``openrocket-test``: Only run the unit tests.

   .. figure:: /img/dev_guide/development_setup/run_configurations.png
      :align: center
      :width: 80%
      :alt: Default installed run configurations.

      The default installed run configurations.

   You can run the application by selecting the ``SwingStartup`` configuration and clicking the green play button.
   This will instantiate the OpenRocket application from within IntelliJ IDEA. If you want to stop the running application,
   click the red square button on the top-right in IntelliJ.

   .. figure:: /img/dev_guide/development_setup/swingstartup.png
         :align: center
         :width: 80%
         :alt: Running OpenRocket from IntelliJ IDEA.

         Running OpenRocket directly from IntelliJ IDEA.

6. **That's it!** You can now start developing OpenRocket. 🚀

Command Line Interface
----------------------

It is also possible to develop in a text editor and build OpenRocket from the command line using Gradle. Please refer to the :doc:`Building and Releasing </dev_guide/building_releasing>`
section for all the possible Gradle tasks. To run OpenRocket, you can use:

.. code-block:: bash

   ./gradlew run

Troubleshooting
===============

1. **JDK Not Recognized**

   Ensure that the JDK path is correctly configured in :menuselection:`File --> Project Structure --> SDKs`.

2. **Gradle Sync Issues**

   - If IntelliJ fails to import Gradle projects correctly, try refreshing the Gradle project by clicking on the "Reload All Gradle Projects" icon in the Gradle tool window.
   - Ensure the `gradle-wrapper.properties` file points to the correct Gradle version which supports Java |java_vers|.

3. **Error: Could not find or load main class info.openrocket.swing.startup.SwingStartup
   Caused by: java.lang.ClassNotFoundException: info.openrocket.swing.startup.SwingStartup** Error when running the SwingStartup
   configuration in IntelliJ.

   - Ensure that you have loaded the project from Gradle when you first opened the project in IntelliJ (step 3 in the
     :ref:`IntelliJ setup <setup_intellij>`).