**********************************
Exporting Fins and Centering Rings
**********************************

Fins and centering rings are usually cut from flat sheet material, for example with a laser cutter or a CNC router, or
printed on a 3D printer. OpenRocket can export these parts at their real size, so you do not have to redraw them in a CAD
program. After exporting, OpenRocket reads the file back and compares its dimensions with your design, so you can
see whether the file is correct before you cut or print anything.

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

Exporting a Part
================

You can export **fin sets** and **centering rings**. Other components, such as body tubes and motor tubes, cannot be
exported with these formats, and selecting one does not export the fins and rings that sit inside it. Select the fin set
or the centering ring itself. To export the fins and the rings of your rocket to one file, select all of them in the
component tree (hold :kbd:`Ctrl` or :kbd:`Shift` while clicking).

There are two ways to export:

* Open the configuration dialog of the fin set or centering ring (double-click it in the component tree) and click the
  **Export** button at the top right of the dialog.
* Right-click the components in the component tree and choose **Export as**.

.. figure:: /img/user_guide/exporting_parts/export_button.png
   :align: center
   :width: 65%
   :figclass: or-image-border
   :alt: The Export button in the configuration dialog of a centering ring.

   The Export button in the configuration dialog of a centering ring.

Both ways show the same list of formats. The formats that do not apply to the selected component are greyed out.

.. figure:: /img/user_guide/exporting_parts/export_menu.png
   :align: center
   :width: 35%
   :figclass: or-image-border
   :alt: The export formats.

   The export formats. DXF, STEP, STL and 3MF are for fins and centering rings. SVG and OBJ are the older exports.

Choose a format, pick a file name, and OpenRocket writes the file. A window with the dimension check
(see :ref:`dimension_check`) then opens.

What is exported
----------------

* **Fin sets**: one fin of the set, including its tab. The fin is exported as a flat plate with the thickness of the fin
  set. The cross section (rounded, airfoil), the root fillets and the fin cant are not part of the export.
* **Centering rings**: the ring with its inner diameter. If a motor mount (inner tube) passes through the ring, a hole is
  cut for it, one for every motor of a cluster. Holes that lie inside another hole are left out, and overlapping holes are
  merged into one outline. The thickness of the part is the *Thickness* of the ring.

When you export several parts, they are laid out next to each other, in rows, with 10 mm between them.

All lengths in the files are in **millimeters**, at 1:1 scale, whatever units you use in OpenRocket.

Formats
=======

DXF
---

A 2D drawing for laser cutters, CNC routers, nesting software and 2D CAD programs. The file is a DXF R12 file, an old
version that is widely supported. The drawing contains three layers, so cutting software can treat them separately:

* ``CUT``: the outlines to cut
* ``MARK``: a small cross at the center of every circle, to help with alignment
* ``LABEL``: the name of each part

Nesting several designs on one sheet is done in your nesting software. OpenRocket does not add a kerf (the width of the
cut) to the outlines.

STEP
----

Exact 3D solids for CAD programs such as Fusion, Onshape, SolidWorks and FreeCAD. Every part is a separate named product
in the file, extruded along the Z axis from 0 to the thickness of the part. Straight edges and circles are exact, not
approximated by small triangles, so you can edit the parts in your CAD program.

STL
---

A 3D triangle mesh for 3D printing. All parts are written to one binary STL file. Circles are approximated by short
straight segments, with a maximum deviation of 0.005 mm. STL files do not have a unit; the mesh is in millimeters, which is
what slicers assume.

Slicers read an STL file as a single object. To print several parts in one go, use 3MF instead.

3MF
---

A 3D triangle mesh for 3D printing, in the format that slicers such as Bambu Studio, PrusaSlicer, OrcaSlicer and Cura use
natively. Every part is a separate named object, so the slicer can arrange the parts on the build plate. The parts start
at the origin of the plate. Circles are approximated in the same way as in STL.

.. _dimension_check:

Dimension Check
===============

After the file is written, OpenRocket reads the file back and measures the parts from the geometry in the file, not
from the numbers that were used to write it. It then shows the design values next to the values found in the file.

.. figure:: /img/user_guide/exporting_parts/dimension_check.png
   :align: center
   :width: 65%
   :figclass: or-image-border
   :alt: The dimension check window after exporting a DXF file.

   The dimension check after exporting a DXF file.

Each row has one of these results:

* **OK**: the value in the file matches the design. Lengths must match within 0.001 mm. The volume of a mesh (STL and
  3MF) must match within 0.1%, because circles in a mesh are made of straight segments.
* **Mismatch**: the value in the file is different from the design. Do not use the file before you know why.
* **Not found**: the geometry for this dimension is not in the file.
* **Reference**: this dimension cannot be measured from the file and is listed for information only. For example, a
  2D drawing has no thickness, and the holes of a mesh are not measured. Check these in your CAD program.

OpenRocket also checks that the file says that it is in millimeters. If it is not, the window warns you.

Measured dimensions
-------------------

======== =============================================================== ===========
Part     Dimensions that are measured                                    Formats
======== =============================================================== ===========
Fin      Overall length and height (including the tab)                   all
Fin      Material thickness                                              STEP, STL, 3MF
Ring     Outer diameter                                                  all
Ring     Inner diameter, diameter of every hole, hole distance to center DXF, STEP
Any      Volume                                                          STL, 3MF
======== =============================================================== ===========

Troubleshooting
===============

**The export is greyed out.**
   Select a fin set or a centering ring. The 3D formats and DXF are not available for other components.

**"A hole in 'Centering ring' reaches the outer edge of the part."**
   A hole, such as the hole for a motor mount, is larger than the ring or sticks out of it. A 3D solid cannot be made of
   this ring. Change the dimensions of the ring or of the motor mount. A DXF file can still be exported.

**"Part 'Fins' has no thickness."**
   The thickness of the fin set or the ring is zero. Enter the thickness of your material in the configuration dialog.

**The part has the wrong size in my CAD program.**
   Check that the program reads the file as millimeters. DXF and STEP files tell the program the unit, STL files do not.
