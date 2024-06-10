# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))


# -- Project information -----------------------------------------------------

project = 'OpenRocket'
copyright = '2024, OpenRocket team'
author = 'OpenRocket team'
github_url = 'https://github.com/openrocket/openrocket'

# The full version, including alpha/beta/rc tags
release = '23.09'


# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx.ext.duration',
    'sphinx.ext.todo',
    'sphinx_new_tab_link',
    'sphinx_rtd_dark_mode',
]

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = []

# user starts in light mode
default_dark_mode = False

# -- Options for todo extension ----------------------------------------------

# Show TODOs in the output
todo_include_todos = False

# -- Options for ReadTheDocs -------------------------------------------------

html_theme_options = {
    #'analytics_id': 'G-XXXXXXXXXX',  #  Provided by Google in your dashboard
    #'analytics_anonymize_ip': False,
    'logo_only': False,
    'display_version': True,
    'prev_next_buttons_location': 'both',
    'style_external_links': True,
    'vcs_pageview_mode': '',
    # Toc options
    'collapse_navigation': True,
    'sticky_navigation': False,
    'navigation_depth': 3,
    'includehidden': True,
    'titles_only': False
}


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'sphinx_rtd_theme'

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']

html_logo = "_static/navbar-logo.png"

# Configure Sphinx to update the sidebar whenever the navigation data changes.
html_context = {
    'navigation_update': True
}

html_css_files = [
    'custom.css',
]

# -- Substitutions -----------------------------------------------------------
rst_prolog = """
.. |java_vers| replace:: 17
.. |br_no_pad| raw:: html

  <div style="line-height: 0; padding: 0; margin: 0"></div>
"""
