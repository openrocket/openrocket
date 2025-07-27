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
where ``xx`` is the language code (e.g. ``messages_nl.properties`` for Dutch). The l10n files are a simple key-value pair
where the key is the text to be translated and the value is the translated text. For example, this is a snippet from the
``messages.properties`` file:

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

----

Modifying an Existing Translation
=================================

Open the l10n file for the language you want to modify in the :file:`core/src/main/resources/l10n` directory. For example, to modify
the French translation, open the :file:`messages_fr.properties` file, since ``fr`` corresponds to the language code of French.
Find the key for the text you want to modify and change the value.

When you are done, create a pull request with your changes. The maintainers will review your changes and merge them if they are
appropriate.

----

Creating a New Translation
==========================

If you want to create a new translation for a language that is not yet supported, you can create a new language file in the
:file:`core/src/main/resources/l10n` directory. The file should be named ``messages_xx.properties``, where ``xx`` is the language code
of the language you want to translate to. For example, to create a translation for Finnish, you would create a file named
:file:`messages_fi.properties`.

Copy the contents of the :file:`messages.properties` file into the new file. Translate the English text into the new language and
save the file.

Edit the :file:`swing/src/main/java/info/openrocket/swing/gui/util/SwingPreferences.java` file and add the new language to the
``SUPPORTED_LOCALES`` array. For example, to add Finnish, you would change this line:

.. code-block:: java

   for (String lang : new String[] { "en", "ar", "de", "es", "fr", "it", "nl", "ru", "cs", "pl", "ja", "pt", "tr" }) {

To this (notice the addition of ``"fi"`` at the end)

.. code-block:: java

   for (String lang : new String[] { "en", "ar", "de", "es", "fr", "it", "nl", "ru", "cs", "pl", "ja", "pt", "tr", "fi" }) {

Finally, add yourself to the list of translation contributors (you deserve some fame! 🙂). This is done in the
:file:`swing/src/main/java/info/openrocket/swing/gui/dialogs/AboutDialog.java` file.
In this file, edit the String 'CREDITS' and add your details to the list after the 'Translations by:'-tag.

When you are done, create a pull request with your changes. The maintainers will review your changes and merge them if they are.
If you are not at all familiar with git, you can also `create an issue <https://github.com/openrocket/openrocket/issues/new/choose>`__
with your changes and the maintainers will create the pull request for you.
