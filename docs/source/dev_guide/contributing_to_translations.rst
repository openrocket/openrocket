****************************
Contributing to Translations
****************************

OpenRocket is translated into multiple languages. If you want to help with translations, this document will guide you through the process.

.. contents:: Table of Contents
   :depth: 2
   :local:

----

.. todo::
   Reference a doc in user_guide for changing the language

.. todo::
   Add current state of translations?

How Translations Work
=====================

OpenRocket's GUI elements do not (*should not*) display hard-coded text. Instead, they use a `Translator <https://github.com/openrocket/openrocket/blob/unstable/core/src/main/java/info/openrocket/core/l10n/Translator.java>`__
object with a certain key to look up the text to display. The Translator object is responsible for looking up the text
in the appropriate language file and returning the translated text.

The language files are located in the :file:`core/src/main/resources/l10n` directory. The base file for all translations is
``messages.properties``, which contains the English text. Each language has its own file, named ``messages_xx.properties``,
where ``xx`` is the two-letter ISO 639-1 language code (e.g. ``messages_nl.properties`` for Dutch, ``messages_zh.properties``
for Chinese). The l10n files are a simple key-value pair where the key is the text to be translated and the value is the
translated text. For example, this is a snippet from the ``messages.properties`` file:

.. code-block:: properties

   ! RocketPanel
   RocketPanel.lbl.ViewType = View Type:
   RocketPanel.lbl.Zoom = Zoom:
   RocketPanel.lbl.Stability = Stability:

Comments start with a ``!`` and are ignored. The key is the text to be translated, and the value is the translated text.
The key should be unique within the file and should start with the name of the class that uses the text, followed by the type
of widget that uses the text, followed by a representation of the text. For example, the key ``RocketPanel.lbl.ViewType``
is used by the ``RocketPanel`` class in a label widget to display the text "View Type:". The value for this key is "View Type:".

Other language files use the exact same keys as the ``messages.properties`` base file, but with the translated text as the value.
For example, this is a snippet from the ``messages_nl.properties`` file:

.. code-block:: properties

   ! RocketPanel
   RocketPanel.lbl.ViewType = Weergavetype:
   RocketPanel.lbl.Zoom = Zoom:
   RocketPanel.lbl.Stability = Stabiliteit:

When you now create a widget in the GUI, you should use the Translator object to get the translated text. For example, to
create a label widget with the text "View Type:", you would use the following code:

.. code-block:: java

   private static final Translator trans = Application.getTranslator();

   JLabel label = new JLabel(trans.get("RocketPanel.lbl.ViewType"));

When the GUI is displayed, the Translator object will look up the key ``RocketPanel.lbl.ViewType`` in the appropriate language
file and return the translated text. If the key is not found in the language file, the Translator object will return the English.
This way, the GUI can be easily translated into different languages by simply adding a new language file with the translated text.

.. note::
   Java's ``ResourceBundle`` uses a fallback chain when looking up translation files. For example, a user with locale
   ``zh_CN`` (Chinese Simplified) will automatically use ``messages_zh.properties`` if no ``messages_zh_CN.properties``
   exists. This means all languages, including regional ones, use a simple two-letter filename.

----

Crowdin Workflow
================

Translations are managed via `Crowdin <https://crowdin.com/project/openrocket>`__, which is integrated with the GitHub
repository. This automates the translation sync process:

**Source strings → Crowdin (automatic)**
   When changes to :file:`messages.properties` are pushed to the ``unstable`` branch, Crowdin automatically detects the new
   or modified strings and makes them available for translators on the Crowdin platform.

**Translations → GitHub (automatic)**
   Once translators have updated strings in Crowdin, Crowdin periodically opens a pull request to the repository with the
   updated ``.properties`` files. Maintainers review and merge this PR.

The configuration for this integration lives in :file:`crowdin.yml` at the root of the repository.

Translating via Crowdin
-----------------------

The recommended way to contribute translations is through the Crowdin web interface:

1. Go to `<https://crowdin.com/project/openrocket>`__ and sign in (or create a free account).
2. Select your language.
3. Translate the strings shown in the editor. Crowdin highlights untranslated and outdated strings.
4. Your changes are saved automatically and will be included in the next sync PR to GitHub.

You do not need to clone the repository or edit any files manually to contribute translations this way.

----

Modifying an Existing Translation
=================================

The preferred way to modify an existing translation is through Crowdin (see above). This ensures your changes go through
the review process and are kept in sync with Crowdin's translation memory.

If you need to edit a translation file directly (e.g. for a quick fix during development), open the ``.properties`` file
for the relevant language in :file:`core/src/main/resources/l10n`. For example, to modify the French translation, open
:file:`messages_fr.properties`. Find the key for the text you want to modify and change the value.

When you are done, create a pull request with your changes. The maintainers will review your changes and merge them if they are
appropriate.

----

Creating a New Translation
==========================

To add support for a new language, the following steps are required on the code side. The actual translation work is done
on Crowdin — once the language is added there, Crowdin will push the translated ``.properties`` file automatically.

**1. Add the language file**

Create a new file in :file:`core/src/main/resources/l10n` named ``messages_xx.properties``, where ``xx`` is the two-letter
ISO 639-1 language code. For example, for Finnish: :file:`messages_fi.properties`.

Add at minimum the ``debug.currentFile`` key so the locale can be identified at runtime:

.. code-block:: properties

   debug.currentFile = messages_fi.properties

**2. Register the locale in SwingPreferences**

Edit :file:`swing/src/main/java/info/openrocket/swing/gui/util/SwingPreferences.java` and add the new language code to the
``SUPPORTED_LOCALES`` array. For example, to add Finnish:

.. code-block:: java

   for (String lang : new String[] { "en", "ar", "de", "es", "fr", "it", "nl", "ru", "cs", "pl", "ja", "pt", "tr" }) {

becomes:

.. code-block:: java

   for (String lang : new String[] { "en", "ar", "de", "es", "fr", "it", "nl", "ru", "cs", "pl", "ja", "pt", "tr", "fi" }) {

**3. Add yourself to the credits**

Add your name to the list of translation contributors in
:file:`swing/src/main/java/info/openrocket/swing/gui/dialogs/AboutDialog.java` and :file:`README.md`,
after the ``Translations by:`` tag.

**4. Add the language on Crowdin**

Ask a maintainer to add the new language to the `Crowdin project <https://crowdin.com/project/openrocket>`__. Once added,
Crowdin will handle syncing translations into the repository automatically.

When you are done with the code changes, create a pull request. If you are not familiar with git, you can also
`create an issue <https://github.com/openrocket/openrocket/issues/new/choose>`__ and the maintainers will help.
