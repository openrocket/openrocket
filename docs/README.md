This directory contains the source files for the OpenRocket documentation, built using Sphinx. It includes both the User Guide and the Developer Guide.

The latest version of the documentation is available online at [ReadTheDocs](https://openrocket.readthedocs.io/en/latest/).

---

## Building the docs
Install Sphinx and additional dependencies using pip, Python's package manager. Open your command line interface 
inside the :file:`openrocket/docs` directory and run:
```bash
pip install -r requirements.txt
```

Build the docs by running in the `docs` directory:
```bash
make html
```

To clean your build (necessary when you change the theme or make other changes to the build configuration), run:
```bash
make clean
```