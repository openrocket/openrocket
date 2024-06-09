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