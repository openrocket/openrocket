#!/bin/bash

# Usage:
# ./rename <file-containing-rename-keys> <files_to_modify ...>
#
# Rename keys file contains space-separated lines:
#    <old.key> <new.key>

RENAME="$1"
shift

cat "$RENAME" | while read line ; do

    FROM=`echo $line | awk '{print $1}'`
    TO=`echo $line | awk '{print $2}'`

    sed -i "s/$FROM/$TO/g" "$@"

done
