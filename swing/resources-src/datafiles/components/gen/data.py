""" global data class for OpenRocket XML generator.

This module holds the main in-memory data used for the XML generator.XML

"""

class Data:

    def __init__(self):
        self.tube_size_info_dict = {}
        """ dict: Tube_size_info objects keyed by the name, e.g. BT-50. """
