""" Tube size info class.

This module defines a class capturing info about one particular rocket body tube series.

The tube information includes only the fundamental defining characteristics of the tube
series; primarily just its name and the inside and outside diameters.

When loading the files, the contents have to be run through jsmin to strip comments.

There are open issues about how to handle:
* color
* material or construction variations (e.g. plain outer wrap vs glassine layer,
  spiral winding angle)

  """

import json
import jsmin


class Tube_size_list:

    def __init__(self):
        self.ts_list = {}

    def load_file(self, file=None):
        with open(file, 'r') as f:
            s = jsmin(f.read())
            self.ts_list = json.loads(s)
        return self.ts_list


class Tube_size_info:

    def __init__(self,
                 name="unnamed",
                 id=0.0,
                 od=0.0,
                 units="in"):
        self.name = name
        self.id = id
        self.od = od
        self.units = units

    def wallThickness(self):
        """Computes wall thickness for the tube size class

        Returns:
            Wall thickness, in internal dimensions
        """
        return (self.od - self.id) / 2.0
