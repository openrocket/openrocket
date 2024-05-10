## Building the docs
Install Sphinx using pip, Python's package manager. Open your command line interface and run:
```bash
pip install sphinx
```

You'll also need to install some additional sphinx dependencies. Run
```bash
pip install sphinx-rtd-theme
pip install sphinx_new_tab_link
```

Build the docs by running in the `docs` directory:
```bash
make html
```

To clean your build (necessary when you change the theme or make other changes to the build configuration), run:
```bash
make clean
```