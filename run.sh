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

LIBS="bin/"
LIBS="$LIBS:resources/"
for i in lib/*.jar ; do
    LIBS="$LIBS:$i"
done
LIBS="$LIBS:lib/jogl/gluegen-rt.jar"
LIBS="$LIBS:lib/jogl/jogl.all.jar"

java -cp $LIBS $JAVAOPTS net.sf.openrocket.startup.Startup "$@"

