#!/bin/bash

#
# Rename translation keys in translation files.
#
# Usage:
#    renameTranslationKeys.sh <mapping files...>
#
# The mapping files contain "<original> <new>" key pairs.
# Empty lines and lines starting with "#" are ignored.
# All translation files are modified at once.
#

TRANSLATIONS=messages*.properties

cat "$@" | while read line; do

    if echo "$line" | grep -q "^\s*$\|^\s*#"; then
	continue
    fi

    if ! echo "$line" | egrep -q "^\s*[a-zA-Z0-9._-]+\s+[a-zA-Z0-9._-]+\s*$"; then
	echo "Invalid line:  $line"
    fi

    from="`echo $line | cut -d" " -f1`"
    to="`echo $line | cut -d" " -f2`"

    sed -i "s/^${from}\s*=\s*/${to} = /" $TRANSLATIONS

done
