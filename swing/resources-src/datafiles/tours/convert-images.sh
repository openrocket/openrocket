#!/bin/bash

#
# A script to batch-convert all source image files into suitable
# slideset image files.  It converts all *.png, *.jpg and *.xcf.gz
# files into the suitably sized jpg images in the datafiles directory.
#

DEST=../../../resources/datafiles/tours

CONVERSION="-background #ececec -flatten -geometry 600x400 -quality 85"


# Convert all xcf files
find -iname "*.xcf.gz" | grep -v MANUAL | while read FILE; do

    echo Converting $FILE
    BASE="$(echo $FILE | sed 's/\.xcf\.gz$//')"
    xcf2png "$FILE" | convert $CONVERSION - $DEST/$BASE.jpg

done

# Convert all png and jpg files
find -iname "*.png" -o -iname "*.jpg" | grep -v MANUAL | while read FILE; do 

    echo Converting $FILE
    BASE="$(echo $FILE | sed 's/\.png$//' | sed 's/\.jpg$//')"
    convert $CONVERSION $FILE $DEST/$BASE.jpg

done
