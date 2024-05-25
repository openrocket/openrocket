*********************************
Contributing to the Documentation
*********************************

This documentation is generated using `Sphinx <https://www.sphinx-doc.org/en/master/>`__. If you would like to contribute
to the documentation, you can do so by editing the reStructuredText files in the ``docs/source`` directory of the OpenRocket repo.
You can then build the documentation by first `installing Sphinx <https://www.sphinx-doc.org/en/master/usage/installation.html>`__:

.. code-block:: bash

    pip install sphinx

You'll also need to install some additional sphinx dependencies:

.. code-block:: bash

   pip install sphinx-rtd-theme
   pip install sphinx_new_tab_link

To build the docs, run the following command from the ``docs`` directory:

.. code-block:: bash

    make html

This will generate the documentation in the ``docs/build/html`` directory. You can then view the documentation by opening the
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

Heading levels
==============

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
