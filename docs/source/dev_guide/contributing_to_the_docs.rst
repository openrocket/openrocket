*********************************
Contributing to the Documentation
*********************************

This document explains how to contribute to the OpenRocket documentation. It provides information on why we use Sphinx
for our documentation, how to edit and build the documentation, and the style guide for writing documentation.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Why Sphinx?
===========

This documentation is generated using `Sphinx <https://www.sphinx-doc.org/en/master/>`__. Sphinx is a tool that makes it
easy to create intelligent and beautiful documentation. It is used by many open-source projects, including
`Python <https://www.python.org/>`__, `NumPy <https://numpy.org/>`__, and `Matplotlib <https://matplotlib.org/>`__.
Sphinx uses `reStructuredText <https://docutils.sourceforge.io/rst.html>`__ as its markup language, which is a lightweight markup language that is easy to read and write. Sphinx also supports `Markdown <https://www.markdownguide.org/>`__ and `LaTeX <https://www.latex-project.org/>`__.

Previously, OpenRocket used MediaWiki for its documentation, but we decided to switch to Sphinx for several reasons:

- Sphinx is more powerful, modern, and flexible than MediaWiki. It allows us to create more complex and interactive documentation.

- Sphinx is easier to maintain than MediaWiki. It is easier to update and manage the documentation in the source files.

- Sphinx can give warnings and errors when building the documentation, which helps to catch mistakes and inconsistencies.

- The documentation is hosted on our GitHub repository, together with the source code. This ensures all resources are concentrated,
  makes it easier to keep the documentation in sync with the code, and allows for better version control.

- Lastly, for some users who wanted to contribute to the MediaWiki documentation, their access to the MediaWiki was blocked.
  We were never able to solve that issue.

Read the Docs
-------------

The OpenRocket documentation is hosted on `Read the Docs <https://readthedocs.org/>`__. Read the Docs is a popular
platform for hosting documentation for open-source projects. It automatically builds the documentation from the source
files in the OpenRocket repository and makes it available online. This makes it easy for users to access the documentation
and for contributors to update it. It also supports versioning, so you can view the documentation for different versions of
OpenRocket, and it has a search feature that allows you to quickly find what you are looking for. Additionally, you can also
add translations to the documentation, which makes it accessible to a wider audience.

----

Editing and Building the Documentation
======================================

If you would like to contribute to the documentation, you can do so by editing the reStructuredText files in the
:file:`docs/source` directory of the OpenRocket repo. You can then build the documentation by first
`installing Sphinx <https://www.sphinx-doc.org/en/master/usage/installation.html>`__ and some extra dependencies.
Open a new terminal window in the :file:`docs` directory and run the following command:

.. code-block:: bash

    pip install -r requirements.txt

To build the docs, run the following command from the ``docs`` directory:

.. code-block:: bash

    make html

This will generate the documentation in the :file:`docs/build/html` directory. You can then view the documentation by opening the
``index.html`` file in your web browser.

To clean your build (necessary when you change the theme or make other changes to the build configuration), run:

.. code-block:: bash

   make clean


If you would like to contribute to the documentation, please submit a pull request with your changes. If you are not sure how to
do this, please see the ``Obtaining the Source Code`` section in :doc:`Development Environment Setup </dev_guide/development_setup>`.
Also check out the `GitHub documentation <https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request>`__
on how to submit a pull request. If you don't want to go through the hassle of setting up a development environment, you can also
`submit an issue <https://github.com/openrocket/openrocket/issues/new/choose>`__ with your proposed changes and we will take care of the rest,
or you can `contact us <https://openrocket.info/contact.html>`__.

----

Style Guide
===========

This section provides a style guide for writing documentation for OpenRocket. It covers conventions that we use in the docs
and useful tips for writing reStructuredText/Sphinx docs.

.. _heading_levels:

Heading levels
--------------

Normally, in reStructuredText, there are no heading levels assigned to certain characters as the structure is determined
from the succession of headings. However, we have set the following heading level rules for the documentation:

- \# with overline, for parts *(not really used at the moment)*

- \* with overline, for chapters

- \= for sections

- \- for subsections

- \^ for subsubsections

- \" for paragraphs

Note that the overline and underline characters must be the same length as the text they are underlining.

For example:

.. code-block:: rst

    *****************************************
    H1: This is a chapter (title of the page)
    *****************************************

    H2: This is a section
    =====================

    H3: This is a subsection
    ------------------------

    H4: This is a subsubsection
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^

    H5: This is a paragraph
    """""""""""""""""""""""

Horizontal Rules
----------------

Horizontal rules are used to separate sections of the documentation. They are created using four or more hyphens (----).

For example:

.. code-block:: rst

    This is a section
    =================

    ----

    This is another section
    =======================

.. note::

   As seen in the example, it is recommended to **always add a horizontal rule before starting a new section**
   (H2, see :ref:`Heading levels <heading_levels>`).

Adding Images
-------------

Images are added to the documentation like this:

.. code-block:: rst

   .. figure:: /img/path/to/your/image.png
      :width: 50% (please always express this as a percentage, and don't go over 95% width)
      :align: "left", "center", or "right" ("center" should be used in general)
      :alt: Alternative text
      :figclass: or-image-border (optional, for custom styling)

       This is the caption of the image.

Images are stored in the :file:`img` directory in the :file:`docs/source` directory. When adding images, please make sure
they are in the correct format (PNG, JPEG, or SVG) and that you place them in the correct directory. Use the same directory
structure as the rst source file that you want to include the image in. For example, if you want to include an image in
:file:`docs/source/user_guide/quick_start.rst`, place the image in :file:`docs/source/img/user_guide/quick_start/`.

Just for fun, here is an image of my cat:

.. figure:: /img/dev_guide/contributing_to_the_docs/Oscar.jpeg
   :width: 50%
   :align: center
   :alt: A cute cat
   :figclass: or-image-border

   This is a picture of my cat, Oscar.

Hyperlinks
----------

Hyperlinks to external sites are created like this:

.. code-block:: rst

    `link text <www.your_url.com>`__

Replace ``link text`` with the text you want to display as the hyperlink, and ``www.your_url.com`` with the actual URL
of the hyperlink. For example: `Hey, I'm a link! <https://www.youtube.com/watch?v=dQw4w9WgXcQ>`__.

.. warning::

   Always use a double underscore at the end. Don't use a single underscore, as this can cause issues when you have
   multiple hyperlinks with the same text.

Admonitions: Tip, Note, Warning, Attention, See also
----------------------------------------------------

As you saw just above, you can add notes and warnings to draw attention to important information. The following are
all the possible admonition type: "**attention**", "**caution**", "**danger**", "**error**", "**hint**", "**important**",
"**note**", "**tip**", "**warning**". More information can be found
`here <https://docutils.sourceforge.io/docs/ref/rst/directives.html#specific-admonitions>`__.

The most uses admonitions in the OpenRocket docs are:

Tip
^^^

.. code-block:: rst

    .. tip::

       This is a tip.

.. tip::

   This is what the tip looks like.

Note
^^^^

.. code-block:: rst

    .. note::

       This is a note.

.. note::

   This is what the note looks like.

Warning
^^^^^^^

.. code-block:: rst

    .. warning::

       This is a warning.

.. warning::

   This is what the warning looks like.

Attention
^^^^^^^^^

.. code-block:: rst

    .. attention::

       This is an attention.

.. attention::

    This is what a point of attention looks like.

See Also
^^^^^^^^

.. code-block:: rst

    .. seealso::

       This is a seealso.

.. seealso::

   See also the following page :doc:`Development Overview </dev_guide/development_overview>`

Semantic Markup
---------------

Sphinx uses interpreted text roles to insert semantic markup into documents. They are written as \:rolename\:\`content\`.
More information can be found `here <https://www.sphinx-doc.org/en/master/usage/restructuredtext/roles.html>`__. What
this means is that you can add roles to pieces of text that have a specific meaning so that Sphinx renders that text
in an appropriate way. Below you find some of the most common roles used in the OpenRocket documentation:

\:menuselection\: Role
^^^^^^^^^^^^^^^^^^^^^^

The ``:menuselection:`` role is used to represent a sequence of menu selections in a user interface.

Example:
  :menuselection:`File --> Open example`

(Ensure you use the correct arrow character, which is ``-->``.)

\:command\: Role
^^^^^^^^^^^^^^^^

The ``:command:`` role is used to represent a command that a user can enter in a command-line interface.

Example:
  To list the contents of a directory, use the :command:`ls` command.

\:file\: Role
^^^^^^^^^^^^^

The ``:file:`` role is used to indicate a file or a file path.

Example:
  Open the configuration file :file:`conf.py` to modify the settings.

\:kbd\: Role
^^^^^^^^^^^^

The ``:kbd:`` role is used to indicate keyboard keys or shortcuts.

Example:
  Press :kbd:`Ctrl` + :kbd:`C` to copy the text.

\:guilabel\: Role
^^^^^^^^^^^^^^^^^

The ``:guilabel:`` role is used to indicate labels of GUI elements like buttons, labels, or fields.

Example:
  Click the :guilabel:`Submit` button to save your changes.

Substitutions
-------------

Sphinx allows you to define substitutions that can be used to replace text in the documentation. This is useful for
replacing frequently used text that is prone to update (e.g. versions of something, or dates). More information can be
found `here <https://www.sphinx-doc.org/en/master/usage/restructuredtext/roles.html#substitutions>`__.
Custom substitutions are defined in :file:`docs/source/conf.py` in the ``rst_prolog`` section. For example, there is a
substitution for ``|java_vers|`` that defines the version of Java that OpenRocket requires. You can then use this
substitution in the documentation like this: OpenRocket uses Java ``|java_vers|`` (Java |java_vers|).

Escaping Special Characters
---------------------------

If you need to include a special character in your text that is normally interpreted by Sphinx, you can escape it by
preceding it with a backslash. For example, to include a backslash in your text, you would write ``\\``. To include
a colon, you would write ``\:``.

----

.. note::

   The reStructuredText syntax and Sphinx' capabilities are **very rich**. This page barely scratches the surface of what you can do.
   Please take the time to read the `documentation on reStructuredText <https://www.sphinx-doc.org/en/master/usage/restructuredtext/index.html>`__
   and `Sphinx <https://www.sphinx-doc.org/en/master/usage/index.html>`__. If you find interesting features that you think would be
   useful for the OpenRocket documentation, please use them and document them here!


Line Wrapping
-------------

Please try to keep your lines in the .rst files under ± 120 characters. This makes it easier to read the documentation in
the source files and prevent horizontal scrolling for code blocks. You can break up normal text on a new line without issues,
if there is no blank line between two lines of text, the two lines will be rendered as one paragraph in the output.

Here is an example of correct and incorrect line wrapping inside the source code:

.. figure:: /img/dev_guide/contributing_to_the_docs/Line-Wrapping.png
   :width: 80%
   :align: center
   :alt: Correct and incorrect line wrapping.
   :figclass: or-image-border

   Correct and incorrect line wrapping of a .rst file.

For breaking up list items, you must ensure that the next line is indented by the same amounts of spaces as the first line
of the list item. For example:

.. code-block:: rst

    - This is a list item that is very long and needs to be broken up into multiple lines. This is a list item that is very long and needs to be broken up into multiple lines. This is a list item that is very long and needs to be broken up into multiple lines.

    - This is a list item that is broken up into multiple lines. This is a list item that is broken up into multiple
      lines. This is a list item that is broken up into multiple lines.

If you do not have the right indentation, you will get a compile warning when you build the documentation.

ToDo's
------

If you are working on a part of the documentation that is not yet finished, you can add a ToDo note to remind yourself to
finish it later. You can do this by adding a ``todo`` directive to the text. For example:

.. code-block:: rst

   .. todo::

      This section is not yet finished. Please come back later to complete it.

You can view the ToDo's in the documentation if you set the ``todo_include_todos`` option to ``True`` in the
:file:`docs/source/conf.py` file. After you've done this and rebuilt the docs, you should see a list of all the ToDo's here:

.. todolist::
