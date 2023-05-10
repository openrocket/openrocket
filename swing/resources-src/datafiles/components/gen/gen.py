#
# system to generate OpenRocket XML data files from a set of much more normalized .json data files
#
# Copyright (c) 2017 by Dave Cook
#

import sys
import data

def main():
    """
    OpenRocket XML parts database generator
    """

    for arg in sys.argv:
        print "arg is {}".format(arg)

if __name__ == '__main__':
    main()
