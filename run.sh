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

java -cp $JAVAOPTS info.openrocket.swing.startup.Startup "$@"

