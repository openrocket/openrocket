#!/bin/bash

#
# This script runs the version of OpenRocket compiled by Eclipse from
# the bin/ directory.  You can provide Java arguments and OpenRocket
# arguments.
#

JAVAOPTS=""

while echo "$1" | grep -q "^-" ; do
    JAVAOPTS="$JAVAOPTS $1"
    shift
done


java -cp bin/:resources/:lib/miglayout15-swing.jar:lib/jcommon-1.0.16.jar:lib/jfreechart-1.0.13.jar:lib/iText-5.0.2.jar $JAVAOPTS net.sf.openrocket.startup.Startup "$@"

