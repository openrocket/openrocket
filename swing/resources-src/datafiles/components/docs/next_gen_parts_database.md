# Next Generation Sport Rocket Parts Database

Looking past OpenRocket, it would be useful to have a more general database of sport rocket
parts, both current and historic.  The OpenRocket design of a collection of standalone flat
XML files will not support the more general needs.

## Desired Features

* Support for web applications (today this implies REST interface with json payload)
* Referential consistency for materials used in parts
* Composition (ability to define an assembly made up of other parts and assemblies)
* Store and index more attributes, including
  * Size series, e.g. BT-50
  * Color
  * 2D images
  * 3D models
  * PN aliases or multiple PN systems
  * Molding method for plastic parts: (injection | blow-mold)
  * Cross references [mfr, PN, (clone|upscale|downscale) ]
  * Kit usage references [mfr, kitName, kitPNs]
  * Catalog / webstore presence history
  * General documentation references
* Query support for
  * Parts in a designated size series, e.g. show all BT-50 compatible nose cones
  * Parametric generator for nose cones, transitions, bulkheads and CRs
  * Fin shape templates that auto-scale to any desired size
  * In-production parts (hard to maintain data though)
  * Materials, e.g. show all BT-55 plastic nose cones
  * Assigning a typical balsa density to different groups of parts (e.g. by mfr)
  * Scale prototype, e.g. show Nike Smoke nose cones of any sizs
  * Return computed mass, CG, moments of inertia
  
## General Future Solution

In order to get the desired query capabilities, there is going to have to be some kind
of SQL engine.  This could take the form of an embedded database like `sqlite` that could
be packaged with a standalone app like OpenRocket.  For a web service in the cloud, it would
probably just be a small Postgres RDS in AWS.

### Open Source Resources

Before going too far with any custom implementation, it would be good to have a look at
the best available parts libraries from the open source 3D modeling world.

### Objectives

This is a more concise distillation of the features discussed in the introduction.

* Nested composition
* 3D model support
* Broad query capability
* Complete physical attributes
* Central materials definitions
* Production history
* Documentation references
* Fin templates
* Parametric nose cone shapes
* Parametric density

## XML Generator Approach for OpenRocket

There is some embryonic code here for a Python based system to generate the XML
files for OpenRocket from a set of more fundamental json parameter files.
Although this can't implement everything envisioned for the general parts library,
it could still have benefits:

* Centralization of materials spec file
* Guarantees all parts in a given series (e.g. BT-50 tubes) have the same ID/OD
* Automatic generation of hierarchical engineering description field
* Description guaranteed to match dimensions
* Automatic notation of duplicate part numbers
* Compute whether mass override is needed based on mass ratio error threshold
* Instant regeneration of files with different balsa densities etc.

However I am not pursuing this for now, since it doesn't make progress towards
the general solution.
