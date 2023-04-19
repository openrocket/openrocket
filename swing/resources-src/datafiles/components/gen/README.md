# XML File Generator

This directory contains an embryonic Python program with associated json data files that will generate the XML
files for OpenRocket component databases from a much more compact set of inputs.

## Motivation

The .orc XML files read by OpenRocket to define the "preset" parts have an enormous amount of duplicated data
that makes some kinds of systematic edits very difficult.  For example, if it's discovered that the specified
inside diameter of a body tube series is slightly different than what is already in the XML files, every single
body tube, tube coupler, nose cone, bulkhead, and centering ring for that tube size has to be changed.

Likewise, if you wish to systematically set the offset between the outside diameters of centering rings and the
inside diameters of the mating body tubes, each and every centering ring entry must be edited.

Another major benefit is to provide error checking and consistency guarantees that should greatly increase the
accuracy of the XML parts files.  In particiular, the following kinds of errors can be completely eliminated:

* Incorrect ID/OD becomes impossible for nearly all parts.  Everything will match the tube system.
* Can automatically add all the referenced materials to the .orc files and flag those that don't exist
  in the master materials list.  Eliminates zero-mass parts that occur when the specified material isn't at the
  top of the .orc file.
* Inconsistent organization of the description field in the XML will be eliminated, and can be easily changed.

Yet another benefit is to provide a place to capture research notes about various components and tube families.

Finally there is a desire to be able to automatically export large groups of parts into other formats such as
3D CAD and printing files.

## General Design

There will be a set of JSON files that define various systematic parameters, and the parts generated from them.
The JSON will be in at least first normal form so that fundamental data is not repeated.
The most important of these is the JSON file that defines the body tube size systems, since nearly everything
else is affected by tube diameters.

The description and part number fields in the XML file will be built from fields in the JSON.

Here is an example file defining the tube series dimensions:
```json
{ 
    "BT-20":
    {
        "name": "BT-20",
        "id" : "0.710 in",
        "od" : "0.736 in",
        "rmk": "standard 18mm motor tube"
    },
    "BT-50": 
    { 
        "name": "BT-50",
        "id" : "0.950 in",
        "od" : "0.976 in",
        "rmk": "standard 24mm motor tube"
    }
}

```

Here is a file for one manufacturer's implementation of a tube series. This factoring is necessary because
multiple manufacturers implement identical dimension tubes but in various materials, colors, lengths, and PNs.
```json
{ 
    "Estes BT-20 Classic":
    {
        "mfr_series_name" : "Estes BT-20 Classic",
        "tube_series_name" : "BT-20",
        "mfr_name" : "Estes",
        "material" : "paper, kraft/glassine, Estes",
        "description": "Body tube",
        "color_default" : "kraft",
        "data_sources" : ["Estes 1975 catalog"],
        "products" :
        {
            "BT-20":
            { 
                "len" : "18.0 in",
                "pns" : [ "BT-20", "30316" ]
            },
            "BT-20P":
            { 
                "len" : "13.75 in",
                "pns" : [ "BT-20P", "30333"]
            }
        }
   },
   "Estes BT-50 Classic" : 
    {
     "mfr_series_name": "Estes BT-50 Classic",
     "tube_series_name" : "BT-50",
     "mfr_name" : "Estes",
     "material" : "paper, kraft/glassine, Estes",
     "description": "Body tube",
     "color_default" : "kraft",
     "data_sources" : ["Estes 1975 catalog"],
     "products" : [
       { "len" : "18.0 in",
         "pns" : [ "BT-50", "30352" ]
       },
       { "len" : "18.0 in",
         "pns" : [ "BT-50", "30355"],
         "color" : "yellow",                            # color override
         "rmk" : "punched 1/4 inch hole at one end",    # optional remark
         "data_sources" : [ "Estes 1985 catalog", "http://foo.bar.baz" ]  # specific sources for this item
       }
    }
}
```

Here is a file for a nose cone series:
```
{ mfr_nose_cone_series: [
  { "mfr_series_name" : "Estes BNC-20",
    "tube_series_name" : "BT-20",
]}
```
