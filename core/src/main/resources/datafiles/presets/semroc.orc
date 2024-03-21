<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
SEMROC parts file for OpenRocket

Copyright 2017-2019 by Dave Cook NAR 21953  caveduck17@gmail.com

See the file LICENSE in this distribution for license information.

This file provides parts definitions for SEMROC products.  It has been rewritten from the original
semroc.orc distributed with OpenRocket 15.03 with various improvements:

    * Descriptions normalized to comma-separated list of attributes in increasing specificity
    * Material types all matched to generic_materials.orc
    * Only lists materials that are referenced in this file
    * Dimension units are those specified in reference materials such as catalogs
    * Excess significant digits removed from dimensions; generally kept 3-4 significant figures
    * Unspecified or missing dimensions estimated by photogrammetry of drawings and photos
    * Numerous dimension/mass/material/part number errors fixed (sorry, WAY too many to list)
    * Mass overrides have been eliminated wherever feasible

Semroc carries a vast line of model rocketry parts including a comprehensive set of equivalent
parts for the Estes and Centuri lines, as well as some MPC/Quest metric tube sizes, and even some
Aerotech tubes.  Thus, there is a lot of overlap between this file and the separate Estes and (future)
Centuri files.  Semroc only makes balsa nose cones,
so they have made shape-identical balsa nose cones for many Estes and Centuri plastic nose cones.

Using this file:
    Drop this file in the OS-dependent location where OpenRocket looks for component databases:
        Windows:  $APPDATA/OpenRocket/Components/ (you need to set $APPDATA)
        OSX:      $HOME/Library/Application Support/OpenRocket/Components/
        Linux:    $HOME/.openrocket/Components/

    You need to restart OpenRocket after adding these files before the parts will be
    available.

LINKS
=====
Classic parts cross reference: http://www.semroc.com/Store/scripts/xref.asp
   This chart lists cross-references for Semroc PNs matching Centuri PNs.
   It's notable for tabulatingg the years of production for Centuri and FSI parts.
   It appears that certain Semroc parts listed there may have never actually existed because the
   Semroc PNs don't appear anywhere else on either the leggacy or new site.
   These "unimplemented" Semroc PNs are:
      BC-1057  = Centuri BC-109
      BC-27567 = Centuri BC-275B
      BC-27563 = Centuri BC-275C
      BNC-5A   = Estes PNC-5A
      BNC-50S  = Estes PNC-50S
      BNC-55AZ = Estes BNC-55AZ
    In a twist, there are also 3 "old Semroc" nose cones that were never assigned "new Semroc" PNs,
    possibly because they were obsolete tube sizes.  No "new Semroc" nose cones are listed in
    the 600 and 900 series.  All of the "old Semroco" nose cones were listed as having been
    produced in 1968-1971.
      NB-607
      NB-611
      NB-908

A fun oddity:  On the legacy site at http://www.semroc.com/Store/scripts/prodView.asp?idproduct=1575
you find a "Balsa Nose Cone #8 Boid Assortment" with PN BC-BOID, and a drawing for a BC-834C.
The My Boid was an ST-8 kit introduced in 2005 that had different nose cone and fins in every kit.
There is now a 13mm (ST-5) version of the My Boid.  John Lee's 2009 review of the original My Boid
on RocketReviews said "As I understand it, the parts in this kit are highly variable and consist of
whatever Semroc has an excess of".  The original KA-6 My Boid kit card says "Over 100 different species!"
The instructions (see on https://www.rocketryforum.com/threads/my-boid-semroc-kit-ka-6.24373/) give the PN for the
nose cone as "BC-BOID".  My conclusion is that if you ordered a BC-BOID, you would get a random
ST-8 nose cone!


DONE
====
Body tubes
Tube couplers
Centering rings
Baffle rings, fiber and plywood
Engine blocks
Bulkheads (fiber)
Bulkheads / nose blocks (balsa)
Balsa nose cones
Balsa reducers

TODO
====
Finish rectifying nose cone shoulder lengths against drawings from legacy site

-->
<OpenRocketComponent>
  <Version>0.1</Version>
  <Materials>

    <!-- fiber for centering rings from built-in semroc file -->
    <Material UnitsOfMeasure="g/cm3">
      <Name>Fiber, bulk</Name>
      <Density>657.0</Density>
      <Type>BULK</Type>
    </Material>
        
    <Material UnitsOfMeasure="g/cm3">
      <Name>Plywood, light, bulk</Name>
      <Density>352.4</Density>
      <Type>BULK</Type>
    </Material>
    
    <Material UnitsOfMeasure="g/cm3">
      <Name>Balsa, bulk, 6lb/ft3</Name>
      <Density>96.0</Density>
      <Type>BULK</Type>
    </Material>
    
    <Material UnitsOfMeasure="g/cm3">
      <Name>Balsa, bulk, 7lb/ft3</Name>
      <Density>112.0</Density>
      <Type>BULK</Type>
    </Material>

    <Material UnitsOfMeasure="g/cm3">
      <Name>Balsa, bulk, 8lb/ft3</Name>
      <Density>128.1</Density>
      <Type>BULK</Type>
    </Material>

    <!-- Use Estes body tube density until we know better -->    
    <Material UnitsOfMeasure="kg/m3">
      <Name>Paper, spiral kraft glassine, Estes avg, bulk</Name>
      <Density>894.4</Density>
      <Type>BULK</Type>
    </Material>
 

    <!-- SURFACE (sheet) materials, only needed for parachute and streamer components -->

        
    <!-- Estes/Centuri HDPE poly chute material (100% printed) is about 1 mil thick -->
    <!-- This value is just the bare poly value for 1.0 mil thick, printed material may be a bit different -->
    <Material UnitsOfMeasure="g/m2">
      <Name>Polyethylene film, HDPE, 1.0 mil, bare</Name>
      <Density>0.0235</Density>
      <Type>SURFACE</Type>
    </Material>

    <!-- streamer material taken to be 2 mil HDPE or equivalent -->
    <Material UnitsOfMeasure="kg/m2">
      <Name>Polyethylene film, HDPE, 2.0 mil, bare</Name>
      <Density>0.0470</Density>
      <Type>SURFACE</Type>
    </Material>

    <!-- Mylar (polyester) density quoted by DuPont as 1.39 g/cc -->
    <Material UnitsOfMeasure="g/cm3">
      <Name>Mylar, bulk</Name>
      <Density>1390.0</Density>
      <Type>BULK</Type>
    </Material>


    <!-- LINE materials for parachute shroud lines -->


    <Material UnitsOfMeasure="kg/m">
      <Name>Carpet Thread</Name>
      <Density>3.3E-4</Density>
      <Type>LINE</Type>
    </Material>

  </Materials>

  <Components>
    <!-- body tubes -->
    <!-- We adopt the statistical Estes average tube density, and PNs
         from the current eRockets/SEMROC website -->
    <!-- T-1+ -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-1+-34</PartNumber>
      <Description>Body tube, BT-1+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.220</InsideDiameter>
      <OutsideDiameter Unit="in">0.246</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-1+-8</PartNumber>
      <Description>Body tube, BT-1+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.220</InsideDiameter>
      <OutsideDiameter Unit="in">0.246</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- BT-2 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-2-18</PartNumber>
      <Description>Body tube, BT-2, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.255</InsideDiameter>
      <OutsideDiameter Unit="in">0.281</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- ST-2 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T2-34</PartNumber>
      <Description>Body tube, ST-2, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.234</InsideDiameter>
      <OutsideDiameter Unit="in">0.260</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2180</PartNumber>
      <Description>Body tube, ST-2, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.234</InsideDiameter>
      <OutsideDiameter Unit="in">0.260</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-220</PartNumber>
      <Description>Body tube, ST-2, 2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.234</InsideDiameter>
      <OutsideDiameter Unit="in">0.260</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- BT-2+ -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-2+-34</PartNumber>
      <Description>Body tube, BT-2+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.292</InsideDiameter>
      <OutsideDiameter Unit="in">0.318</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-2+-8</PartNumber>
      <Description>Body tube, BT-2+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.292</InsideDiameter>
      <OutsideDiameter Unit="in">0.318</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <!-- BT-3 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-3-34</PartNumber>
      <Description>Body tube, BT-3, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.349</InsideDiameter>
      <OutsideDiameter Unit="in">0.375</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-3-8</PartNumber>
      <Description>Body tube, BT-3, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.349</InsideDiameter>
      <OutsideDiameter Unit="in">0.375</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-3H</PartNumber>
      <Description>Body tube, BT-3, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.349</InsideDiameter>
      <OutsideDiameter Unit="in">0.375</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-3XW</PartNumber>
      <Description>Body tube, BT-3, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.349</InsideDiameter>
      <OutsideDiameter Unit="in">0.375</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>

    <!-- BT-4 -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-4-34</PartNumber>
      <Description>Body tube, BT-4, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4</PartNumber>
      <Description>Body tube, BT-4, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4W</PartNumber>
      <Description>Body tube, BT-4, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4IJ</PartNumber>
      <Description>Body tube, BT-4, 9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4FD</PartNumber>
      <Description>Body tube, BT-4, 6.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">6.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4HW</PartNumber>
      <Description>Body tube, BT-4, 6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4LJ</PartNumber>
      <Description>Body tube, BT-4, 2.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.422</InsideDiameter>
      <OutsideDiameter Unit="in">0.448</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>

    <!-- BT-4+ -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-4+-34</PartNumber>
      <Description>Body tube, BT-4+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.452</InsideDiameter>
      <OutsideDiameter Unit="in">0.478</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-4+-8</PartNumber>
      <Description>Body tube, BT-4+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.452</InsideDiameter>
      <OutsideDiameter Unit="in">0.478</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- BT-5 / #5 / ST-5 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-5-34</PartNumber>
      <Description>Body tube, BT-5/ST-5, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-5-22</PartNumber>
      <Description>Body tube, BT-5/ST-5, 22"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5</PartNumber>
      <Description>Body tube, BT-5/ST-5, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-5180</PartNumber>
      <Description>Body tube, BT-5/ST-5, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-5120</PartNumber>
      <Description>Body tube, BT-5/ST-5, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5-110</PartNumber>
      <Description>Body tube, BT-5/ST-5, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <!-- slotting in BT-5SE is 2 fairly wide slots -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5SE</PartNumber>
      <Description>Body tube, BT-5/ST-5, 10.0", slotted</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-590</PartNumber>
      <Description>Body tube, BT-5/ST-5, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <!-- slot in ST-590S1 is a single rectangular cutout -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-590S1</PartNumber>
      <Description>Body tube, BT-5/ST-5, 9.0", slotted</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-580</PartNumber>
      <Description>Body tube, BT-5/ST-5, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-560</PartNumber>
      <Description>Body tube, BT-5/ST-5, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <!-- Slotting on ST-560S is actually a 50% cutaway -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-560S</PartNumber>
      <Description>Body tube, BT-5/ST-5, 6.0", slotted</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5P</PartNumber>
      <Description>Body tube, BT-5/ST-5, 5.1"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">5.1</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-545</PartNumber>
      <Description>Body tube, BT-5/ST-5, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-540</PartNumber>
      <Description>Body tube, BT-5/ST-5, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5CJ</PartNumber>
      <Description>Body tube, BT-5/ST-5, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-530</PartNumber>
      <Description>Body tube, BT-5/ST-5, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5-2.75</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <!-- 2.5" tube has aberrant PN ST-5250 on eRockets 2017 site -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-5250</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>
    <!-- eRockets description is inconsistent, doesn't say punched -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-525E</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.5", punched</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5-0225</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5BJ</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-520</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-520E</PartNumber>
      <Description>Body tube, BT-5/ST-5, 2.0", punched</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5MJ</PartNumber>
      <Description>Body tube, BT-5/ST-5, 1.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-515</PartNumber>
      <Description>Body tube, BT-5/ST-5, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5T</PartNumber>
      <Description>Body tube, BT-5/ST-5, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5XW</PartNumber>
      <Description>Body tube, BT-5/ST-5, 1.375"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">1.375</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5C</PartNumber>
      <Description>Body tube, BT-5/ST-5, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.515</InsideDiameter>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BodyTube>
    
    <!-- BT-5+ -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-5+-34</PartNumber>
      <Description>Body tube, BT-5+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.559</InsideDiameter>
      <OutsideDiameter Unit="in">0.585</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-5+-8</PartNumber>
      <Description>Body tube, BT-5+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.559</InsideDiameter>
      <OutsideDiameter Unit="in">0.585</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- ST-6 / #6 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-6180</PartNumber>
      <Description>Body tube, ST-6/#6, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.610</InsideDiameter>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-6120</PartNumber>
      <Description>Body tube, ST-6/#6, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.610</InsideDiameter>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-690</PartNumber>
      <Description>Body tube, ST-6/#6, 9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.610</InsideDiameter>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-660</PartNumber>
      <Description>Body tube, ST-6/#6, 6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.610</InsideDiameter>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-620</PartNumber>
      <Description>Body tube, ST-6/#6, 2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.610</InsideDiameter>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- BT-19 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-19-34</PartNumber>
      <Description>Body tube, BT-19, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.700</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-19180</PartNumber>
      <Description>Body tube, BT-19, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.700</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-19M</PartNumber>
      <Description>Body tube, BT-19, 6.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.700</OutsideDiameter>
      <Length Unit="in">6.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-19-60</PartNumber>
      <Description>Body tube, BT-19, 6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.700</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>

    <!-- BT-20 -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-20-34</PartNumber>
      <Description>Body tube, BT-20, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20-220</PartNumber>
      <Description>Body tube, BT-20, 22"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20</PartNumber>
      <Description>Body tube, BT-20, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20P</PartNumber>
      <Description>Body tube, BT-20, 13.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">13.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20-120</PartNumber>
      <Description>Body tube, BT-20, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20L</PartNumber>
      <Description>Body tube, BT-20, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20N</PartNumber>
      <Description>Body tube, BT-20, 9.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">9.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20B</PartNumber>
      <Description>Body tube, BT-20, 8.65"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">8.65</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20XW</PartNumber>
      <Description>Body tube, BT-20, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20E</PartNumber>
      <Description>Body tube, BT-20, 7.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">7.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20D</PartNumber>
      <Description>Body tube, BT-20, 6.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">6.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20HW</PartNumber>
      <Description>Body tube, BT-20, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20DJ</PartNumber>
      <Description>Body tube, BT-20, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20G</PartNumber>
      <Description>Body tube, BT-20, 3.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20J</PartNumber>
      <Description>Body tube, BT-20, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20M</PartNumber>
      <Description>Body tube, BT-20, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20AE</PartNumber>
      <Description>Body tube, BT-20, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.710</InsideDiameter>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>

    <!-- ST-7 / #7 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7-34</PartNumber>
      <Description>Body tube, ST-7, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7180</PartNumber>
      <Description>Body tube, ST-7, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7120</PartNumber>
      <Description>Body tube, ST-7, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7100</PartNumber>
      <Description>Body tube, ST-7, 10"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-790</PartNumber>
      <Description>Body tube, ST-7, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-786P</PartNumber>
      <Description>Body tube, ST-7, 8.625", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">8.625</Length>
    </BodyTube>
    <!-- ***MISSING ITEM*** Centuri original 1960s Black Widow had 8" upper tube, Semroc has no equivalent -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-770</PartNumber>
      <Description>Body tube, ST-7, 7.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">7.0</Length>
    </BodyTube>
    <!-- ST-765 has aberrant PN ST-7650  -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7650</PartNumber>
      <Description>Body tube, ST-7, 6.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">6.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-760</PartNumber>
      <Description>Body tube, ST-7, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-758</PartNumber>
      <Description>Body tube, ST-7, 5.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">5.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-755</PartNumber>
      <Description>Body tube, ST-7, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-753</PartNumber>
      <Description>Body tube, ST-7, 5.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">5.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-750</PartNumber>
      <Description>Body tube, ST-7, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-750P</PartNumber>
      <Description>Body tube, ST-7, 5.0", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-749</PartNumber>
      <Description>Body tube, ST-7, 4.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">4.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-745</PartNumber>
      <Description>Body tube, ST-7, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <!-- SOURCE ERROR: ST-7400 Semroc has aberrant PN ST-7400 for ST-7 4" length, should be ST-740 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7400</PartNumber>
      <Description>Body tube, ST-7, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-735P</PartNumber>
      <Description>Body tube, ST-7, 3.5", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-7325</PartNumber>
      <Description>Body tube, ST-7, 3.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">3.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-730</PartNumber>
      <Description>Body tube, ST-7, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-730P</PartNumber>
      <Description>Body tube, ST-7, 3.0", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-728P</PartNumber>
      <Description>Body tube, ST-7, 2.75", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-728</PartNumber>
      <Description>Body tube, ST-7, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-725</PartNumber>
      <Description>Body tube, ST-7, 2.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-723</PartNumber>
      <Description>Body tube, ST-7, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-722</PartNumber>
      <Description>Body tube, ST-7, 2.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-720</PartNumber>
      <Description>Body tube, ST-7, 2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <!-- ST-720H has a single hole punched in the middle -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-720H</PartNumber>
      <Description>Body tube, ST-7, 2", punched</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <!-- ST-720X has a single hole punched near the end for a motor hook -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-720X</PartNumber>
      <Description>Body tube, ST-7, 2", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-703</PartNumber>
      <Description>Body tube, ST-7, 0.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </BodyTube>

    <!-- BT-30" -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30-300</PartNumber>
      <Description>Body tube, BT-30, 30"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30-180</PartNumber>
      <Description>Body tube, BT-30, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- Plain BT-30 designation matches Estes original BT-30 9" long -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30</PartNumber>
      <Description>Body tube, BT-30, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30F</PartNumber>
      <Description>Body tube, BT-30, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30SH</PartNumber>
      <Description>Body tube, BT-30, 7.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">7.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30B</PartNumber>
      <Description>Body tube, BT-30, 6.13"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">6.13</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30C</PartNumber>
      <Description>Body tube, BT-30, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30A</PartNumber>
      <Description>Body tube, BT-30, 3.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30AP</PartNumber>
      <Description>Body tube, BT-30, 3.5", punched at end for vents</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30XW</PartNumber>
      <Description>Body tube, BT-30, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30J</PartNumber>
      <Description>Body tube, BT-30, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-30K</PartNumber>
      <Description>Body tube, BT-30, 2.75", punched center</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.725</InsideDiameter>
      <OutsideDiameter Unit="in">0.767</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>

    <!-- BT-20+ -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-20+-34</PartNumber>
      <Description>Body tube, BT-20+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.744</InsideDiameter>
      <OutsideDiameter Unit="in">0.770</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-20+-8</PartNumber>
      <Description>Body tube, BT-20+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.744</InsideDiameter>
      <OutsideDiameter Unit="in">0.770</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- BT-40 -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-40-185</PartNumber>
      <Description>Body tube, BT-40, 18.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.765</InsideDiameter>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <Length Unit="in">18.5</Length>
    </BodyTube>

    <!-- Plain BT-40 designation matches ancient Estes part -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-40</PartNumber>
      <Description>Body tube, BT-40, 13.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.765</InsideDiameter>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <Length Unit="in">13.25</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-40W</PartNumber>
      <Description>Body tube, BT-40, 9.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.765</InsideDiameter>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <Length Unit="in">9.25</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-40SP</PartNumber>
      <Description>Body tube, BT-40, 7.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.765</InsideDiameter>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <Length Unit="in">7.5</Length>
    </BodyTube>

    <!-- ST-8 -->
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8-34</PartNumber>
      <Description>Body tube, ST-8, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8180</PartNumber>
      <Description>Body tube, ST-8, 18"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8157</PartNumber>
      <Description>Body tube, ST-8, 15.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">15.75</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8150</PartNumber>
      <Description>Body tube, ST-8, 15"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8141</PartNumber>
      <Description>Body tube, ST-8, 14.1"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">14.1</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8140</PartNumber>
      <Description>Body tube, ST-8, 14"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8130</PartNumber>
      <Description>Body tube, ST-8, 13"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">13.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8120</PartNumber>
      <Description>Body tube, ST-8, 12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8113</PartNumber>
      <Description>Body tube, ST-8, 11.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">11.3</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8105</PartNumber>
      <Description>Body tube, ST-8, 10.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8103</PartNumber>
      <Description>Body tube, ST-8, 10.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">10.3</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8100</PartNumber>
      <Description>Body tube, ST-8, 10"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-890</PartNumber>
      <Description>Body tube, ST-8, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-890S3</PartNumber>
      <Description>Body tube, ST-8, 9.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8800</PartNumber>
      <Description>Body tube, ST-8, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-873</PartNumber>
      <Description>Body tube, ST-8, 7.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">7.3</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-860</PartNumber>
      <Description>Body tube, ST-8, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-856</PartNumber>
      <Description>Body tube, ST-8, 5.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">5.6</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-855</PartNumber>
      <Description>Body tube, ST-8, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8450</PartNumber>
      <Description>Body tube, ST-8, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-840</PartNumber>
      <Description>Body tube, ST-8, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-830</PartNumber>
      <Description>Body tube, ST-8, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-825</PartNumber>
      <Description>Body tube, ST-8, 2.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-823</PartNumber>
      <Description>Body tube, ST-8, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8200</PartNumber>
      <Description>Body tube, ST-8, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- ST-8F (same OD as old FSI RT-8 / HRT-8 tube) -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F180</PartNumber>
      <Description>Body tube, ST-8F, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F120</PartNumber>
      <Description>Body tube, ST-8F, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F90M</PartNumber>
      <Description>Body tube, ST-8F, 9.0", marked for 3 fins</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F90</PartNumber>
      <Description>Body tube, ST-8F, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F80</PartNumber>
      <Description>Body tube, ST-8F, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F60</PartNumber>
      <Description>Body tube, ST-8F, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F40</PartNumber>
      <Description>Body tube, ST-8F, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F33</PartNumber>
      <Description>Body tube, ST-8F, 3.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">3.25</Length>
    </BodyTube>
    
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-8F28</PartNumber>
      <Description>Body tube, ST-8F, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.885</InsideDiameter>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>

    <!-- LT-085 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-085300</PartNumber>
      <Description>Body tube, LT-085, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.945</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-085220</PartNumber>
      <Description>Body tube, LT-085, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.945</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-085160</PartNumber>
      <Description>Body tube, LT-085, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.945</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-08580</PartNumber>
      <Description>Body tube, LT-085, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.945</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>


    <!-- BT-50 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50-30</PartNumber>
      <Description>Body tube, BT-50, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50-18</PartNumber>
      <Description>Body tube, BT-50, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50V</PartNumber>
      <Description>Body tube, BT-50, 16.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">16.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50SV</PartNumber>
      <Description>Body tube, BT-50, 16.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">16.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50TF</PartNumber>
      <Description>Body tube, BT-50, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50XW</PartNumber>
      <Description>Body tube, BT-50, 15.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">15.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50KE</PartNumber>
      <Description>Body tube, BT-50, 15.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50N</PartNumber>
      <Description>Body tube, BT-50, 14.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50L</PartNumber>
      <Description>Body tube, BT-50, 12.7"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">12.7</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50-113</PartNumber>
      <Description>Body tube, BT-50, 11.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">11.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50P</PartNumber>
      <Description>Body tube, BT-50, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50B</PartNumber>
      <Description>Body tube, BT-50, 10.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">10.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50A</PartNumber>
      <Description>Body tube, BT-50, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50W</PartNumber>
      <Description>Body tube, BT-50, 9.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50IJ</PartNumber>
      <Description>Body tube, BT-50, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50BH</PartNumber>
      <Description>Body tube, BT-50, 8.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">8.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50H</PartNumber>
      <Description>Body tube, BT-50, 7.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">7.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50FE</PartNumber>
      <Description>Body tube, BT-50, 6.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">6.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50EE</PartNumber>
      <Description>Body tube, BT-50, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50F</PartNumber>
      <Description>Body tube, BT-50, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50S</PartNumber>
      <Description>Body tube, BT-50, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50J</PartNumber>
      <Description>Body tube, BT-50, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50AH</PartNumber>
      <Description>Body tube, BT-50, 1.88"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">1.88</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50AE</PartNumber>
      <Description>Body tube, BT-50, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>
    
    <!-- BTH-50 / BT-50MF foil lined heavy wall 24mm motor mount tube-->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-50MF</PartNumber>
      <Description>Body tube, BT-50MF/BTH-50, 34.0", foil lined</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.992</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50MF-8</PartNumber>
      <Description>Body tube, BT-50MF/BTH-50, 8.0", foil lined</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.992</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- ST-9 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-9180</PartNumber>
      <Description>Body tube, ST-9, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-995</PartNumber>
      <Description>Body tube, ST-9, 9.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-995S3</PartNumber>
      <Description>Body tube, ST-9, 9.5", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-990</PartNumber>
      <Description>Body tube, ST-9, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-980</PartNumber>
      <Description>Body tube, ST-9, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-978P</PartNumber>
      <Description>Body tube, ST-9, 7.75", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">7.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-950</PartNumber>
      <Description>Body tube, ST-9, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-950E</PartNumber>
      <Description>Body tube, ST-9, 5.0", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <!-- not specified how ST-950SC differs from ST-950, except it costs more -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-950SC</PartNumber>
      <Description>Body tube, ST-9, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-945</PartNumber>
      <Description>Body tube, ST-9, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-9400</PartNumber>
      <Description>Body tube, ST-9, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-940E</PartNumber>
      <Description>Body tube, ST-9, 4.0", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-940S3</PartNumber>
      <Description>Body tube, ST-9, 4.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-930</PartNumber>
      <Description>Body tube, ST-9, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-930E</PartNumber>
      <Description>Body tube, ST-9, 3.0", punched for engine hook</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>

    <!-- BT-50+ -->
    <!-- BT-50+ and BT-51 are for nearly all purposes interchangeable, only .001
    difference -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-50+-34</PartNumber>
      <Description>Body tube, BT-50+, 34"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.984</InsideDiameter>
      <OutsideDiameter Unit="in">1.010</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-50+-8</PartNumber>
      <Description>Body tube, BT-50+, 8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.984</InsideDiameter>
      <OutsideDiameter Unit="in">1.010</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    
    <!-- BT-51 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-51N</PartNumber>
      <Description>Body tube, BT-51, 12.42"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.985</InsideDiameter>
      <OutsideDiameter Unit="in">1.011</OutsideDiameter>
      <Length Unit="in">12.42</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-51CI</PartNumber>
      <Description>Body tube, BT-51, 3.88"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.985</InsideDiameter>
      <OutsideDiameter Unit="in">1.011</OutsideDiameter>
      <Length Unit="in">3.88</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-51SE</PartNumber>
      <Description>Body tube, BT-51, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.985</InsideDiameter>
      <OutsideDiameter Unit="in">1.011</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    
    <!-- BT-52 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-52180</PartNumber>
      <Description>Body tube, BT-52, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.988</InsideDiameter>
      <OutsideDiameter Unit="in">1.014</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-52S</PartNumber>
      <Description>Body tube, BT-52, 3.938"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.988</InsideDiameter>
      <OutsideDiameter Unit="in">1.014</OutsideDiameter>
      <Length Unit="in">3.938</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-52AG</PartNumber>
      <Description>Body tube, BT-52, 2.1"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.988</InsideDiameter>
      <OutsideDiameter Unit="in">1.014</OutsideDiameter>
      <Length Unit="in">2.1</Length>
    </BodyTube>

    <!-- ST-10 -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10-34</PartNumber>
      <Description>Body tube, ST-10, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10180</PartNumber>
      <Description>Body tube, ST-10, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10130</PartNumber>
      <Description>Body tube, ST-10, 13.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">13.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10126</PartNumber>
      <Description>Body tube, ST-10, 12.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">12.6</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10125S4</PartNumber>
      <Description>Body tube, ST-10, 12.5", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">12.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10120</PartNumber>
      <Description>Body tube, ST-10, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10120S3</PartNumber>
      <Description>Body tube, ST-10, 12.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10113S4</PartNumber>
      <Description>Body tube, ST-10, 11.25", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">11.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10108S8</PartNumber>
      <Description>Body tube, ST-10, 10.75", 8 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">10.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10105</PartNumber>
      <Description>Body tube, ST-10, 10.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10105S6</PartNumber>
      <Description>Body tube, ST-10, 10.5", 6 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10100</PartNumber>
      <Description>Body tube, ST-10, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10100S3</PartNumber>
      <Description>Body tube, ST-10, 10.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1095</PartNumber>
      <Description>Body tube, ST-10, 9.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1095S3</PartNumber>
      <Description>Body tube, ST-10, 9.5", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1094</PartNumber>
      <Description>Body tube, ST-10, 9.4"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.4</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1090</PartNumber>
      <Description>Body tube, ST-10, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1090S3</PartNumber>
      <Description>Body tube, ST-10, 9.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1090S4</PartNumber>
      <Description>Body tube, ST-10, 9.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1083S3</PartNumber>
      <Description>Body tube, ST-10, 8.25", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">8.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1080</PartNumber>
      <Description>Body tube, ST-10, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1063</PartNumber>
      <Description>Body tube, ST-10, 6.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">6.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1060</PartNumber>
      <Description>Body tube, ST-10, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1053</PartNumber>
      <Description>Body tube, ST-10, 5.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">5.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1050</PartNumber>
      <Description>Body tube, ST-10, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1043</PartNumber>
      <Description>Body tube, ST-10, 4.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">4.3</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1040</PartNumber>
      <Description>Body tube, ST-10, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1039</PartNumber>
      <Description>Body tube, ST-10, 3.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">3.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1035</PartNumber>
      <Description>Body tube, ST-10, 3.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-10340</PartNumber>
      <Description>Body tube, ST-10, 3.4"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">3.4</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1033S4</PartNumber>
      <Description>Body tube, ST-10, 3.25", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">3.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1030</PartNumber>
      <Description>Body tube, ST-10, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1020</PartNumber>
      <Description>Body tube, ST-10, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.040</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- ST-11 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11180</PartNumber>
      <Description>Body tube, ST-11, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11158</PartNumber>
      <Description>Body tube, ST-11, 15.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">15.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11145</PartNumber>
      <Description>Body tube, ST-11, 14.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">14.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11133</PartNumber>
      <Description>Body tube, ST-11, 13.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">13.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11125</PartNumber>
      <Description>Body tube, ST-11, 12.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">12.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-11120</PartNumber>
      <Description>Body tube, ST-11, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1190</PartNumber>
      <Description>Body tube, ST-11, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1180</PartNumber>
      <Description>Body tube, ST-11, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1160</PartNumber>
      <Description>Body tube, ST-11, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1128</PartNumber>
      <Description>Body tube, ST-11, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.130</InsideDiameter>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>

    <!-- LT-115 / BTH-52 29mm motor tube -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115340</PartNumber>
      <Description>Body tube, LT-115, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115300</PartNumber>
      <Description>Body tube, LT-115, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115220</PartNumber>
      <Description>Body tube, LT-115, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115180</PartNumber>
      <Description>Body tube, LT-115, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <!-- This is listed as an Aerotech part but is identical to LT-115 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115178, AER-12918</PartNumber>
      <Description>Body tube, LT-115, 17.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">17.750</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115170</PartNumber>
      <Description>Body tube, LT-115, 17.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">17.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115160</PartNumber>
      <Description>Body tube, LT-115, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115160S4</PartNumber>
      <Description>Body tube, LT-115, 16.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <!-- This is listed as an Aerotech part but is identical to LT-115 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115120, AER-12912</PartNumber>
      <Description>Body tube, LT-115, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-115109</PartNumber>
      <Description>Body tube, LT-115, 10.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">10.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11580</PartNumber>
      <Description>Body tube, LT-115, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11575</PartNumber>
      <Description>Body tube, LT-115, 7.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">7.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11560</PartNumber>
      <Description>Body tube, LT-115, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11555</PartNumber>
      <Description>Body tube, LT-115, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11550</PartNumber>
      <Description>Body tube, LT-115, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11545</PartNumber>
      <Description>Body tube, LT-115, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11535</PartNumber>
      <Description>Body tube, LT-115, 3.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11530</PartNumber>
      <Description>Body tube, LT-115, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-11520</PartNumber>
      <Description>Body tube, LT-115, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.140</InsideDiameter>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- BT-55 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55-180</PartNumber>
      <Description>Body tube, BT-55, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55KG</PartNumber>
      <Description>Body tube, BT-55, 16.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">16.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55V</PartNumber>
      <Description>Body tube, BT-55, 16.4"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">16.4</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55C</PartNumber>
      <Description>Body tube, BT-55, 14.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55W</PartNumber>
      <Description>Body tube, BT-55, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55-120</PartNumber>
      <Description>Body tube, BT-55, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55-113</PartNumber>
      <Description>Body tube, BT-55, 11.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">11.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55-110</PartNumber>
      <Description>Body tube, BT-55, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55-109</PartNumber>
      <Description>Body tube, BT-55, 10.92"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">11.92</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55KA</PartNumber>
      <Description>Body tube, BT-55, 10.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55IJ</PartNumber>
      <Description>Body tube, BT-55, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55K</PartNumber>
      <Description>Body tube, BT-55, 7.1"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">7.1</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55RJ</PartNumber>
      <Description>Body tube, BT-55, 5.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">5.3</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55S</PartNumber>
      <Description>Body tube, BT-55, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55E</PartNumber>
      <Description>Body tube, BT-55, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-55J</PartNumber>
      <Description>Body tube, BT-55, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>

    <!-- LT-125 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125300</PartNumber>
      <Description>Body tube, LT-125, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125220</PartNumber>
      <Description>Body tube, LT-125, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125220S4</PartNumber>
      <Description>Body tube, LT-125, 22.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125157S4</PartNumber>
      <Description>Body tube, LT-125, 15.7", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">15.7</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125150</PartNumber>
      <Description>Body tube, LT-125, 15.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125150S4</PartNumber>
      <Description>Body tube, LT-125, 15.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-1251225</PartNumber>
      <Description>Body tube, LT-125, 12.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">12.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-125122S3</PartNumber>
      <Description>Body tube, LT-125, 12.2", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">12.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-12580</PartNumber>
      <Description>Body tube, LT-125, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.250</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- ST-13 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13300</PartNumber>
      <Description>Body tube, ST-13, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13180</PartNumber>
      <Description>Body tube, ST-13, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13150</PartNumber>
      <Description>Body tube, ST-13, 15.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13135</PartNumber>
      <Description>Body tube, ST-13, 13.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">13.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13130</PartNumber>
      <Description>Body tube, ST-13, 13.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">13.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13120</PartNumber>
      <Description>Body tube, ST-13, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13105</PartNumber>
      <Description>Body tube, ST-13, 10.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-13100</PartNumber>
      <Description>Body tube, ST-13, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1383</PartNumber>
      <Description>Body tube, ST-13, 8.26"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">8.26</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1380</PartNumber>
      <Description>Body tube, ST-13, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1363</PartNumber>
      <Description>Body tube, ST-13, 6.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">6.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1360</PartNumber>
      <Description>Body tube, ST-13, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1346</PartNumber>
      <Description>Body tube, ST-13, 4.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">4.6</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1340</PartNumber>
      <Description>Body tube, ST-13, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1338</PartNumber>
      <Description>Body tube, ST-13, 3.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">3.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1330</PartNumber>
      <Description>Body tube, ST-13, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1323</PartNumber>
      <Description>Body tube, ST-13, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1320</PartNumber>
      <Description>Body tube, ST-13, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- BT-56 dimensions given not same as Estes (1.304,1.346) but Semroc says it's
         "same as Estes BT-56" so I use the correct Estes dimensions.  BT-56 is often said
         to be equivalent to ST-13 but the latter is actually .004 smaller with .001
         thinner wall -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-56-10</PartNumber>
      <Description>Body tube, BT-56, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.304</InsideDiameter>
      <OutsideDiameter Unit="in">1.346</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>

    <!-- BT-58 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58-180</PartNumber>
      <Description>Body tube, BT-58, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58SA</PartNumber>
      <Description>Body tube, BT-58, 13.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">13.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58-1275</PartNumber>
      <Description>Body tube, BT-58, 12.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">12.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58WP</PartNumber>
      <Description>Body tube, BT-58, 12.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">12.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58W</PartNumber>
      <Description>Body tube, BT-58, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58AC</PartNumber>
      <Description>Body tube, BT-58, 11.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">11.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58AJ</PartNumber>
      <Description>Body tube, BT-58, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58IJ</PartNumber>
      <Description>Body tube, BT-58, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58MX</PartNumber>
      <Description>Body tube, BT-58, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58-65</PartNumber>
      <Description>Body tube, BT-58, 6.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">6.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58SB</PartNumber>
      <Description>Body tube, BT-58, 6.375"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">6.375</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58SV</PartNumber>
      <Description>Body tube, BT-58, 6.125"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">6.125</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58AR</PartNumber>
      <Description>Body tube, BT-58, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58LJ</PartNumber>
      <Description>Body tube, BT-58, 5.375"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">5.375</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58F</PartNumber>
      <Description>Body tube, BT-58, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-58MY</PartNumber>
      <Description>Body tube, BT-58, 1.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.498</InsideDiameter>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BodyTube>
    
    <!-- LT-150 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-150300</PartNumber>
      <Description>Body tube, LT-150, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.500</InsideDiameter>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-150220</PartNumber>
      <Description>Body tube, LT-150, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.500</InsideDiameter>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-15080</PartNumber>
      <Description>Body tube, LT-150, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.500</InsideDiameter>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    
    <!-- BT-60 -->
    <!-- SOURCE ERROR: INVESTIGATE Modern eRockets/SEMROC site has separate BT-60 and ST-16 listings
         but gives ST-16 dimensions for everything.  Legacy Semroc site has Centuri ST-16 dimensions of
         ID = 1.600, OD = 1.640 and Estes compatible BT-60 dimensions of ID = 1.595, OD = 1.637  ***
         Using Estes compatible dimensions for BT-60, i.e. assuming legacy site is correct. -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T60-34</PartNumber>
      <Description>Body tube, BT-60, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60180</PartNumber>
      <Description>Body tube, BT-60, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60KF</PartNumber>
      <Description>Body tube, BT-60, 16.1"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">16.1</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60P</PartNumber>
      <Description>Body tube, BT-60, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60AE</PartNumber>
      <Description>Body tube, BT-60, 14.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">14.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60AD</PartNumber>
      <Description>Body tube, BT-60, 14.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60ADS4</PartNumber>
      <Description>Body tube, BT-60, 14.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60KC</PartNumber>
      <Description>Body tube, BT-60, 12.84"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">12.84</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60DS</PartNumber>
      <Description>Body tube, BT-60, 12.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">12.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60D</PartNumber>
      <Description>Body tube, BT-60, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60AJ</PartNumber>
      <Description>Body tube, BT-60, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60HE</PartNumber>
      <Description>Body tube, BT-60, 8.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">8.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60BB</PartNumber>
      <Description>Body tube, BT-60, 7.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">7.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60K</PartNumber>
      <Description>Body tube, BT-60, 7.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">7.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60FG</PartNumber>
      <Description>Body tube, BT-60, 6.7"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">6.7</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60-63</PartNumber>
      <Description>Body tube, BT-60, 6.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">6.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60R</PartNumber>
      <Description>Body tube, BT-60, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60RS4</PartNumber>
      <Description>Body tube, BT-60, 5.0", 4 slots Omega</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60V</PartNumber>
      <Description>Body tube, BT-60, 4.31"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">4.31</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60S</PartNumber>
      <Description>Body tube, BT-60, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60RB</PartNumber>
      <Description>Body tube, BT-60, 3.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">3.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60XW</PartNumber>
      <Description>Body tube, BT-60, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60J</PartNumber>
      <Description>Body tube, BT-60, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-60C</PartNumber>
      <Description>Body tube, BT-60, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.595</InsideDiameter>
      <OutsideDiameter Unit="in">1.637</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BodyTube>

    <!-- ST-16 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16180</PartNumber>
      <Description>Body tube, ST-16, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16163</PartNumber>
      <Description>Body tube, ST-16, 16.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">16.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16161</PartNumber>
      <Description>Body tube, ST-16, 16.125"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">16.125</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16160</PartNumber>
      <Description>Body tube, ST-16, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16151</PartNumber>
      <Description>Body tube, ST-16, 15.12"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">15.12</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16132</PartNumber>
      <Description>Body tube, ST-16, 13.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">13.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16130</PartNumber>
      <Description>Body tube, ST-16, 13.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">13.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16128</PartNumber>
      <Description>Body tube, ST-16, 12.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">12.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16120</PartNumber>
      <Description>Body tube, ST-16, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16110</PartNumber>
      <Description>Body tube, ST-16, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16109</PartNumber>
      <Description>Body tube, ST-16, 10.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">10.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-16100</PartNumber>
      <Description>Body tube, ST-16, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1690</PartNumber>
      <Description>Body tube, ST-16, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1685</PartNumber>
      <Description>Body tube, ST-16, 8.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">8.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1675</PartNumber>
      <Description>Body tube, ST-16, 7.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">7.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1674</PartNumber>
      <Description>Body tube, ST-16, 7.4"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">7.4</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1667</PartNumber>
      <Description>Body tube, ST-16, 6.7"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">6.7</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1660</PartNumber>
      <Description>Body tube, ST-16, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1652</PartNumber>
      <Description>Body tube, ST-16, 5.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">5.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1650</PartNumber>
      <Description>Body tube, ST-16, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1645</PartNumber>
      <Description>Body tube, ST-16, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1640</PartNumber>
      <Description>Body tube, ST-16, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1630</PartNumber>
      <Description>Body tube, ST-16, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1622</PartNumber>
      <Description>Body tube, ST-16, 2.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1620</PartNumber>
      <Description>Body tube, ST-16, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.600</InsideDiameter>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- LT-175 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175300</PartNumber>
      <Description>Body tube, LT-175, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175220</PartNumber>
      <Description>Body tube, LT-175, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175220S3</PartNumber>
      <Description>Body tube, LT-175, 22.0", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <!-- LT-175220S4 discontinued 2017 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175220S4</PartNumber>
      <Description>Body tube, LT-175, 22.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175200</PartNumber>
      <Description>Body tube, LT-175, 20.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">20.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175200S4</PartNumber>
      <Description>Body tube, LT-175, 20.0", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">20.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175165</PartNumber>
      <Description>Body tube, LT-175, 16.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">16.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175165S3</PartNumber>
      <Description>Body tube, LT-175, 16.5", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">16.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-175120</PartNumber>
      <Description>Body tube, LT-175, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-17580</PartNumber>
      <Description>Body tube, LT-175, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-17560</PartNumber>
      <Description>Body tube, LT-175, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-17555</PartNumber>
      <Description>Body tube, LT-175, 5.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">5.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-17530</PartNumber>
      <Description>Body tube, LT-175, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-17520</PartNumber>
      <Description>Body tube, LT-175, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>

    <!-- ST-18 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-18180</PartNumber>
      <Description>Body tube, ST-18, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-18160</PartNumber>
      <Description>Body tube, ST-18, 16.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">16.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-18150</PartNumber>
      <Description>Body tube, ST-18, 15.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-18120</PartNumber>
      <Description>Body tube, ST-18, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1890</PartNumber>
      <Description>Body tube, ST-18, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1860</PartNumber>
      <Description>Body tube, ST-18, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1849</PartNumber>
      <Description>Body tube, ST-18, 4.9"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">4.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1844</PartNumber>
      <Description>Body tube, ST-18, 4.375"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">4.375</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1830</PartNumber>
      <Description>Body tube, ST-18, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1817</PartNumber>
      <Description>Body tube, ST-18, 1.7"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">1.7</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-1815</PartNumber>
      <Description>Body tube, ST-18, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BodyTube>

    <!-- T-1.88 -->
    <!-- This is an Aerotech compatible tube made by Tube Dimensional and listed in 2017 by
    eRockets under the SEMROC parts line.  It is not a legacy SEMROC product though. -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11926</PartNumber>
      <Description>Body tube, T-1.88, Aerotech type, 22.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.880</OutsideDiameter>
      <Length Unit="in">22.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11924</PartNumber>
      <Description>Body tube, T-1.88, Aerotech type, 22.75", 3 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.880</OutsideDiameter>
      <Length Unit="in">22.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11923</PartNumber>
      <Description>Body tube, T-1.88, Aerotech type, 22.75", 4 slots</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.880</OutsideDiameter>
      <Length Unit="in">22.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11912</PartNumber>
      <Description>Body tube, T-1.88, Aerotech type, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.880</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11909</PartNumber>
      <Description>Body tube, T-1.88, Aerotech type, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.800</InsideDiameter>
      <OutsideDiameter Unit="in">1.880</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>

    <!-- ST-20 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-20-34</PartNumber>
      <Description>Body tube, ST-20, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20180</PartNumber>
      <Description>Body tube, ST-20, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20145</PartNumber>
      <Description>Body tube, ST-20, 14.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">14.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20140</PartNumber>
      <Description>Body tube, ST-20, 14.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20128</PartNumber>
      <Description>Body tube, ST-20, 12.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">12.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20120</PartNumber>
      <Description>Body tube, ST-20, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-20100</PartNumber>
      <Description>Body tube, ST-20, 10.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">10.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2098</PartNumber>
      <Description>Body tube, ST-20, 9.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">9.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2090</PartNumber>
      <Description>Body tube, ST-20, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2060</PartNumber>
      <Description>Body tube, ST-20, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2050</PartNumber>
      <Description>Body tube, ST-20, 5.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-2030</PartNumber>
      <Description>Body tube, ST-20, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BodyTube>

    <!-- LT-200 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-200300</PartNumber>
      <Description>Body tube, LT-200, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.088</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-200220</PartNumber>
      <Description>Body tube, LT-200, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.088</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-200215</PartNumber>
      <Description>Body tube, LT-200, 21.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.088</OutsideDiameter>
      <Length Unit="in">21.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-20080</PartNumber>
      <Description>Body tube, LT-200, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.088</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- BT-70 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-70-34</PartNumber>
      <Description>Body tube, BT-70, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-70KD</PartNumber>
      <Description>Body tube, BT-70, 17.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">17.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-70BD</PartNumber>
      <Description>Body tube, BT-70, 15.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">15.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-70V</PartNumber>
      <Description>Body tube, BT-70, 10.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">10.6</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-70H</PartNumber>
      <Description>Body tube, BT-70, 7.15"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">7.15</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RT-70</PartNumber>
      <Description>Body tube, BT-70, 0.68", ring</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.217</OutsideDiameter>
      <Length Unit="in">0.68</Length>
    </BodyTube>

    <!-- BTH-70 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-300</PartNumber>
      <Description>Body tube, BTH-70, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <!-- BTH-70-286 discontinued 2017 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-286</PartNumber>
      <Description>Body tube, BTH-70, 28.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">28.6</Length>
    </BodyTube>
    <!-- BTH-70-280 discontinued 2017 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-280</PartNumber>
      <Description>Body tube, BTH-70, 28.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">28.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-180</PartNumber>
      <Description>Body tube, BTH-70, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-172</PartNumber>
      <Description>Body tube, BTH-70, 17.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">17.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-11275</PartNumber>
      <Description>Body tube, BTH-70, 12.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">12.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-120</PartNumber>
      <Description>Body tube, BTH-70, 12.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-88</PartNumber>
      <Description>Body tube, BTH-70, 8.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">8.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-58</PartNumber>
      <Description>Body tube, BTH-70, 5.8"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">5.8</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-40</PartNumber>
      <Description>Body tube, BTH-70, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-28</PartNumber>
      <Description>Body tube, BTH-70, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-70-05</PartNumber>
      <Description>Body tube, BTH-70, 0.5", ring</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.175</InsideDiameter>
      <OutsideDiameter Unit="in">2.427</OutsideDiameter>
      <Length Unit="in">0.5</Length>
    </BodyTube>

    <!-- LT-225 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-225300</PartNumber>
      <Description>Body tube, LT-225, 30.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-225220</PartNumber>
      <Description>Body tube, LT-225, 22.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-225140</PartNumber>
      <Description>Body tube, LT-225, 14.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-225140S4</PartNumber>
      <Description>Body tube, LT-225, 14.0", 4 slots, SLS Laser-X</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">14.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-22580</PartNumber>
      <Description>Body tube, LT-225, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-22570</PartNumber>
      <Description>Body tube, LT-225, 7.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">7.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-22560</PartNumber>
      <Description>Body tube, LT-225, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.250</InsideDiameter>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>

    <!-- BT-80 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80180</PartNumber>
      <Description>Body tube, BT-80, 18.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80170</PartNumber>
      <Description>Body tube, BT-80, 17.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">17.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80KD</PartNumber>
      <Description>Body tube, BT-80, 14.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">14.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80DA</PartNumber>
      <Description>Body tube, BT-80, 11.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">11.6</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80OP (Oscar Papa)</PartNumber>
      <Description>Body tube, BT-80, 11.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">11.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-8093</PartNumber>
      <Description>Body tube, BT-80, 9.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">9.3</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80A</PartNumber>
      <Description>Body tube, BT-80, 9.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">9.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80SV</PartNumber>
      <Description>Body tube, BT-80, 8.81"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">8.81</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80WH</PartNumber>
      <Description>Body tube, BT-80, 8.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80V</PartNumber>
      <Description>Body tube, BT-80, 7.6"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">7.6</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80S</PartNumber>
      <Description>Body tube, BT-80, 4.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">4.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80MA</PartNumber>
      <Description>Body tube, BT-80, 3.22"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">3.22</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80R</PartNumber>
      <Description>Body tube, BT-80, 2.2"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">2.2</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80BJ</PartNumber>
      <Description>Body tube, BT-80, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-80C</PartNumber>
      <Description>Body tube, BT-80, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BodyTube>

    <!-- BTH-80 -->
    <!-- BTH-80 and Aerotech 2.6 dimensions are effectively identical.  The sizes given on
         the eRockets 2017 tube size list are slighly discrepant, and the BTH-80 wall thickness
         is incorrectly given as .050 when all other sources indicate .041
         I am adopting the dimensions ID = 2.558, OD = 2.640 (wall .041) per the original
         SEMROC chart.  This makes the most sense since it has ID identical to original
         BT-80.  Aerotech indicated in a Nov 2014 post to TRF that their specs were
         ID = 2.56, OD = 2.64 +/- .005
    -->
    <!-- PN AER-12628 27" with 4 fin slots and 1 lug slot is Astrobee lower tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_27in_4slot_1lug AER-12628</PartNumber>
      <Description>Body tube, BTH-80, 27.0", 4 fin slots, 1 lug slot, PN AER-12628</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">27.0</Length>
    </BodyTube>
    <!-- PN AER-12629 27" with 1 lug slot (only) is Astrobee upper tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_27in_1lug AER-12629</PartNumber>
      <Description>Body tube, BTH-80, 27.0", 1 lug slot, PN AER-12629</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">27.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_24in AER-12626</PartNumber>
      <Description>Body tube, BTH-80, 24.0", PN AER-12626</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">24.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_24in_4slot AER-12623</PartNumber>
      <Description>Body tube, BTH-80, 24.0", 4 fin slots, PN AER-12623</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">24.0</Length>
    </BodyTube>
    <!-- PN AER-12615 with 3 fin slots + 1 lug slot is Mirage lower tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_24in_3slot_1lug AER-12625</PartNumber>
      <Description>Body tube, BTH-80, 24.0", 3 fin slots, 1 lug slot, PN AER-12625</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">24.0</Length>
    </BodyTube>
    <!-- PN AER-12627 with 1 lut slot (only) is Mirage center tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_24in_1lugslot AER-12627</PartNumber>
      <Description>Body tube, BTH-80, 24.0", 1 lug slot, PN AER-12627</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">24.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_24in_3slot_2lug AER-12624</PartNumber>
      <Description>Body tube, BTH-80, 24.0", 3 fin slots, 2 lug slots, PN AER-12624</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">24.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_19in AER-12619</PartNumber>
      <Description>Body tube, BTH-80, 19.0", PN AER-12619</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">19.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTH-80_15in AER-12615</PartNumber>
      <Description>Body tube, BTH-80, 15.0", PN AER-12615</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.558</InsideDiameter>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <Length Unit="in">15.0</Length>
    </BodyTube>

    <!-- ST-27 Centuri standard tube -->
    <!-- This is an odd duck since for most ST-xx sizes, the xx indicates the ID, but for
    ST-27 it's the OD, and the wall thickness is only .013, not the normal .020 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-27180</PartNumber>
      <Description>Body tube, ST-27, 18.0", PN ST-27180</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.674</InsideDiameter>
      <OutsideDiameter Unit="in">2.700</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-27014</PartNumber>
      <Description>Body tube, ST-27, 1.38", PN ST-27014</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.674</InsideDiameter>
      <OutsideDiameter Unit="in">2.700</OutsideDiameter>
      <Length Unit="in">1.38</Length>
    </BodyTube>

    <!-- LT-275 (Centuri heavy wall tube) -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-275300</PartNumber>
      <Description>Body tube, LT-275, 30.0", PN LT-275300</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.750</InsideDiameter>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-275220</PartNumber>
      <Description>Body tube, LT-275, 22.0", PN LT-275220</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.750</InsideDiameter>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <Length Unit="in">22.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LT-27580</PartNumber>
      <Description>Body tube, LT-275, 8.0", PN LT-27580</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.750</InsideDiameter>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </BodyTube>

    <!-- T-3.0 (supposedly) Aerotech compatible tube by Totally Tubular -->
    <!-- SOURCE ERROR: T-3.0 inconsistent sizes for T-3.0 said to be "same as the Aerotech and LOC 3" tubes"
         but lists ID=2.950, OD=3.000, wall 0.050, which are internally inconsistent
         LOC tubes are in fact ID 3.000, OD 3.100, wall=0.050, so I use that.
         SOURCE ERROR: T-3.0  incorrect mfr attribution for T-3.0: Aerotech has never offered a 3" tube
         or kit; in early 2017 I personally confirmed this with Charlie Savoie, GM of Aerotech.
    -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-3.0</PartNumber>
      <Description>Body tube, T-3.0"H, 34.0", PN T-3.0</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.000</InsideDiameter>
      <OutsideDiameter Unit="in">3.100</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>

    <!-- ST-36 Centuri compatible tube.  Only available in 3" length. -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>ST-3630</PartNumber>
      <Description>Body tube, ST-36, 3.0", PN ST-3630</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.600</InsideDiameter>
      <OutsideDiameter Unit="in">3.690</OutsideDiameter>
      <Length Unit="in">30.0</Length>
    </BodyTube>

    <!-- RT-99 ring tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RT-99D</PartNumber>
      <Description>Body tube, RT-99, 0.39", ring tube</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.668</InsideDiameter>
      <OutsideDiameter Unit="in">3.700</OutsideDiameter>
      <Length Unit="in">0.39</Length>
    </BodyTube>

    <!-- BT-100 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-100-34</PartNumber>
      <Description>Body tube, BT-100, 34.0", PN T-100-34</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.702</InsideDiameter>
      <OutsideDiameter Unit="in">3.744</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-100Z</PartNumber>
      <Description>Body tube, BT-100, 10.9", PN BT-100Z</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.702</InsideDiameter>
      <OutsideDiameter Unit="in">3.744</OutsideDiameter>
      <Length Unit="in">10.9</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-100D</PartNumber>
      <Description>Body tube, BT-100, 4.09", PN BT-100D</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.702</InsideDiameter>
      <OutsideDiameter Unit="in">3.744</OutsideDiameter>
      <Length Unit="in">4.09</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-100CE</PartNumber>
      <Description>Body tube, BT-100, 3.5", PN BT-100CE</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.702</InsideDiameter>
      <OutsideDiameter Unit="in">3.744</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>

    <!-- BT-101 -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>T-101-34</PartNumber>
      <Description>Body tube, BT-101, 34.0", PN T-101-34</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101SV</PartNumber>
      <Description>Body tube, BT-101, 24.625", PN BT-101SV</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">24.625</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101LA</PartNumber>
      <Description>Body tube, BT-101, 21.4", PN BT-101LA</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">21.4</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101-2075</PartNumber>
      <Description>Body tube, BT-101, 20.75", PN BT-101-2075</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">20.75</Length>
    </BodyTube>
    <!-- plain "BT-101" was original Estes PN for a 16.5" tube -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101</PartNumber>
      <Description>Body tube, BT-101, 16.5", PN BT-101</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">16.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101KJ</PartNumber>
      <Description>Body tube, BT-101, 10.5", PN BT-101KJ</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">10.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101K</PartNumber>
      <Description>Body tube, BT-101, 7.59", PN BT-101K</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">7.59</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101T</PartNumber>
      <Description>Body tube, BT-101, 2.78", PN BT-101T</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">2.78</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BT-101A</PartNumber>
      <Description>Body tube, BT-101, 1.0", PN BT-101A</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.896</InsideDiameter>
      <OutsideDiameter Unit="in">3.938</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BodyTube>


    <!-- =================== -->
    <!-- Clear Plastic Tubes -->
    <!-- =================== -->

    <!-- SOURCE ERROR: PST-65 dimension discrepancies ***
         Semroc clear tube PST-65 (also called "BT-65 Clear" on the legacy site) and the corresponding BNC-65xx nose
         cone dimensions show significant discrepancies.  Here is a table (all dimensions in inches)

         SOURCE                        OD           ID         WALL    REMARKS

         Estes 1974 CPC                1.641        1.595      (.023)  This is known to be an error in the CPC
         Estes 1974 cat, PST-65R       1.796        1.750      (.023)  Same values appeared in all 1970s print catalogs
         Semroc 2017 site, PST-65      (1.799)      1.737      .031    Specifies "diameter" (presumed ID) and wall only
         Semroc 2017 site, BNC-65xx    NA           NA         NA      Modern site does not give nose cone diameters
         Semroc legacy site, PST-65    1.774        1.750      (.012)  Likely to be an error
         Semroc legacy site, BNC-65xx  1.796        1.750      (.023)

         DISCUSSION

         The Estes 1974 Custom Parts Catalog values are completely wrong and can be ignored.
         
         The Semroc legacy site values for the BNC-65xx nose cones BNC-65L and BNC-65AF agree exactly with the
         Estes values.
         
         The modern Semroc site has PST-65 values that work out to a compatible OD but a discrepant ID.
         
         The legacy Semroc site gives an OD in the tube listings that is likely to be discrepant as it implies a very
         thin wall.  it is worth noting that the digits "96" are exactly offset 2 keyboard positions from the
         "74" seen in the questionable 1.774 value. 

         At NARAM-58 I had a conversation with Randy Boadway (proprietor of eRockets and modern Semroc) about the
         plastic tube sizes.  He mentioned that the sizing of the clear tubes was inconsistent from one lot to the next.
         This could possibly account for the smaller ID value given on the 2017 site.

         For now I am adopting the Estes print catalog values as the most likely to be correct, and so that the PST-65
         related parts definitions in this database will all interchange correctly.
    -->

    <!-- Series 7 (Centuri compatible) clear tubes -->

    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-719</PartNumber>
      <Description>Body tube, CPT-7, 1.87", PN CPT-719</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">1.87</Length>
    </BodyTube>
    <!-- SOURCE ERROR: CPT-720 legacy semroc site lists 1.87" length.  Should be 2.0"; has been corrected on 2017 site  -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-720</PartNumber>
      <Description>Body tube, CPT-7, 2.0", PN CPT-720</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-722</PartNumber>
      <Description>Body tube, CPT-7, 2.25", PN CPT-722</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-7180</PartNumber>
      <Description>Body tube, CPT-7, 18.0", PN CPT-7180</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.715</InsideDiameter>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- Series 8 (Centuri compatible) clear tubes -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-825</PartNumber>
      <Description>Body tube, CPT-8, 2.5", PN CPT-825</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-835</PartNumber>
      <Description>Body tube, CPT-8, 3.5", PN CPT-835</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-8180</PartNumber>
      <Description>Body tube, CPT-8, 18.0", PN CPT-8180</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.865</InsideDiameter>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- Series 10 (Centuri compatible) clear tubes -->
    <!-- SOURCE ERROR:  CPT-10 ALL Semroc legacy site erroneously replicates the CPT-8 dimensions for the CPT-10.
         Semroc/eRockets dimensions from the 2017 site are inconsistent with ID 1.039", OD 1.060", wall .021" ***
         Here I have set the dimensions to match up with actual ST-10 (ID of 1.00) and use the likely correct
         .021 wall to give an OD of 1.042  -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-10180</PartNumber>
      <Description>Body tube, CPT-10, 18.0", PN CPT-10180</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.00</InsideDiameter>
      <OutsideDiameter Unit="in">1.042</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- Series 13 (Centuri compatible) clear tubes.  These are newer and only appear on the modern Semroc/eRockets site.
         The quoted dimensions are identical to paper #13 tubes, and CPT-13 is only available in a 3.5" length. -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPT-1335</PartNumber>
      <Description>Body tube, CPT-13, 3.5", PN CPT-1335</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.300</InsideDiameter>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <Length Unit="in">3.5</Length>
    </BodyTube>

    <!-- PST-50 (Estes compatible) clear tubes -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-50S</PartNumber>
      <Description>Body tube, PST-50, 4.0", PN PST-50S</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-50FJ</PartNumber>
      <Description>Body tube, PST-50, 6.0", PN PST-50FJ</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-50</PartNumber>
      <Description>Body tube, PST-50, 18.0", PN PST-50</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">0.950</InsideDiameter>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- PST-55 (Estes compatible) clear tubes.  Only the PST-55-6 is listed on the modern 2017 site. -->
    <!-- SOURCE ERROR:  on the legacy Semroc site, PST-55 and PST-55-6 are both listed with 12" length and identical weight *** -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-55-6</PartNumber>
      <Description>Body tube, PST-55, 6.0", PN PST-55-6</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-55</PartNumber>
      <Description>Body tube, PST-55, 12.0", PN PST-55</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-55-180</PartNumber>
      <Description>Body tube, PST-55, 18.0", PN PST-55-180</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.283</InsideDiameter>
      <OutsideDiameter Unit="in">1.325</OutsideDiameter>
      <Length Unit="in">18.0</Length>
    </BodyTube>

    <!-- PST-65 (Estes compatible) clear tubes -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-65R</PartNumber>
      <Description>Body tube, PST-65, 5.0", PN PST-65R</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.796</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </BodyTube>
    <!-- SOURCE ERROR: Semroc legacy site gives length of PST-65L as 5.0", same as PST-65R.  Should be 12". *** -->
    <BodyTube>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PST-65L</PartNumber>
      <Description>Body tube, PST-65, 12.0", PN PST-65L</Description>
      <Material Type="BULK">Mylar, bulk</Material>
      <InsideDiameter Unit="in">1.750</InsideDiameter>
      <OutsideDiameter Unit="in">1.796</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </BodyTube>


    <!-- Tube couplers -->
    <!-- ============= -->
    <!-- Estes, Centuri and Semroc couplers are made of spiral wound "fish paper", which
    is a vulcanized kraft material, dark bluish-grey in color.  The spiral gap is very
    pronounced, and the material is much harder and stiffer than regular kraft body tube.

    Obtaining authoritative wall thicknesses of these couplers is difficult; none of these
    manufacturers routinely published the wall thickness of their tube couplers.

    The JT-80C is a special case with two different versions having been made, one of
    which was thinner .021" glassine/kraft, used in some legacy Estes models such as the
    early Saturn V and #1321 Maxi-Alpha III, and as a ring tail in Hyperion and Manta Bomber.
    -->

    <!-- BT-3 couplers -->

    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-3-5-34</PartNumber>
      <Description>Tube coupler, paper, BT-3, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.250</InsideDiameter>
      <OutsideDiameter Unit="in">0.346</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-3-1</PartNumber>
      <Description>Tube coupler, paper, BT-3, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.250</InsideDiameter>
      <OutsideDiameter Unit="in">0.346</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- BT-4 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-4-34</PartNumber>
      <Description>Tube coupler, paper, BT-4, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.422</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-4-2</PartNumber>
      <Description>Tube coupler, paper, BT-4, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.422</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-4-1.25</PartNumber>
      <Description>Tube coupler, paper, BT-4, 1.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.422</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-4-1</PartNumber>
      <Description>Tube coupler, paper, BT-4, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.422</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- BT-5 / #5 couplers -->
    
    <!-- SOURCE ERROR: Semroc quotes CPL-5-34 OD as 0.516 which is greater than BT-5 ID of .515" -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-5-34</PartNumber>
      <Description>Tube coupler, paper, BT-5, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-5-2</PartNumber>
      <Description>Tube coupler, paper, BT-5, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </TubeCoupler>
    <!-- SOURCE ERROR: Semroc site has PN SEM-CPL-5-1.5 but other text giving CPL-5-1.75 and
         description says 1.75" so I went with 1.75" -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-5-1.75</PartNumber>
      <Description>Tube coupler, paper, BT-5, 1.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </TubeCoupler>
    <!-- Semroc lists both Estes JTC-5C and Centuri HTC-5, not known if IDs differ -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JTC-5C</PartNumber>
      <Description>Tube coupler, paper, BT-5, 0.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-5</PartNumber>
      <Description>Tube coupler, paper, ST-5, 0.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-5B</PartNumber>
      <Description>Tube coupler, paper, ST-5, 0.75", punched vent</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.474</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </TubeCoupler>

    <!-- #7 / ST-7 couplers -->
    <!-- ***MISSING PART*** - ST-7 coupler 1.5" long as used in 1970+ Black Widow, not on Semroc site -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-7</PartNumber>
      <Description>Tube coupler, paper, ST-7, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    <!-- HTC-7B is Black Widow external coupler (Centuri HTC-70), goes over outside of #7 tube
         SOURCE ERROR: Semroc legacy site gives OD of HTC-7B as 0.761"; this is actually the ID
         -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-7B</PartNumber>
      <Description>Tube coupler, paper, ST-7 external, 1.0", large punched hole</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.800</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-7P</PartNumber>
      <Description>Tube coupler, paper, ST-7, 1.0", small punched hole</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.674</InsideDiameter>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- BT-20 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-20-34</PartNumber>
      <Description>Tube coupler, paper, BT-20, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.667</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-20-3</PartNumber>
      <Description>Tube coupler, paper, BT-20, 3.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.667</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-20</PartNumber>
      <Description>Tube coupler, paper, BT-20, 2.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.667</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-20C</PartNumber>
      <Description>Tube coupler, paper, BT-20, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.667</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- BT-30 couplers: NONE -->

    <!-- BT-40 couplers: NONE -->
    
    <!-- ST-8 / #8 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-8</PartNumber>
      <Description>Tube coupler, paper, ST-8, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.821</InsideDiameter>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- ST-8F couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-8F</PartNumber>
      <Description>Tube coupler, paper, ST-8F, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.841</InsideDiameter>
      <OutsideDiameter Unit="in">0.883</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    
    <!-- ST-9 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-9</PartNumber>
      <Description>Tube coupler, paper, ST-9, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.906</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- ST-10 couplers -->
    
    <!-- SOURCE ERROR: Semroc lists a CPL-ST10-3" but dimensions are for an ST-20 tube -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-10</PartNumber>
      <Description>Tube coupler, paper, ST-10, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.956</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- BT-50 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JTC-50C</PartNumber>
      <Description>Tube coupler, paper, BT-50, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.906</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JTC-50B</PartNumber>
      <Description>Tube coupler, paper, BT-50, .437"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.906</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.437</Length>
    </TubeCoupler>
    <!-- CPL-20-50-34" is AR-2050 centering ring stock in bulk -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-20-50-34</PartNumber>
      <Description>Tube coupler, paper, BT-50, 34.0", AR-2050 stock</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-50-34</PartNumber>
      <Description>Tube coupler, paper, BT-50, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.880</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-50-4</PartNumber>
      <Description>Tube coupler, paper, BT-50, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.880</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-50-1</PartNumber>
      <Description>Tube coupler, paper, BT-50, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.880</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>

    <!-- ST-11 couplers -->
    
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-11</PartNumber>
      <Description>Tube coupler, paper, ST-11, 1.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.968</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>

    <!-- LT-115 / BTH-52 / BT-52H couplers -->
    
    <!-- SOURCE ERROR: eRockets Semroc 2017 listings for CPL-52H-34 give OD=1.140, ID=0.980.  The OD is
         exactly the same as the ID of the BTH-52 tube, which is virtually impossible. I
         offset the ID and OD down by .002 to be more realistic.
    -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-52H-34</PartNumber>
      <Description>Tube coupler, paper, BTH-52/LT-115, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-52H-4</PartNumber>
      <Description>Tube coupler, paper, BTH-52/LT-115, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>

    <!-- BT-55 couplers -->
    
    <!-- SOURCE ERROR: CPL-55-34 here again the quoted OD of the coupler is exactly the ID of the tube. -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-55-34</PartNumber>
      <Description>Tube coupler, paper, BTH-55, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.213</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <!-- CPL-50-55 is thick centering ring AR-5055 stock -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-50-55-34</PartNumber>
      <Description>Tube coupler, paper, BT-55, AR-5055 type, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <!-- JT-55C wall thickness taken to be .030 ***verify***
         SOURCE ERROR: JT-55C Estes 1980 catalog gives JT-55C length 1.3", Legacy semroc site has length = 1.50", new
         eRockets/Semroc site gives 1.25". -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-55C</PartNumber>
      <Description>Tube coupler, paper, BT-55, 1.3"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.221</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">1.3</Length>
    </TubeCoupler>
    <!-- note on new eRockets/Semroc 2017 site says JT-55CP is "pin punched for Blue Bird Zero" -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-55CP</PartNumber>
      <Description>Tube coupler, paper, BT-55, 1.25", punched vent</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.221</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>
    
    <!-- LT-125 couplers: NONE -->

    <!-- ST-13 couplers -->
    <!-- wall thickness taken to be .030 ***verify*** -->
    <!-- SOURCE ERROR: HTC-13 1975 Centuri catalog has  HTC-13 length = 1.5", legacy Semroc
         site erroneously gives 1.75".  It's correct again in eRockets/Semroc 2017 site -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-13</PartNumber>
      <Description>Tube coupler, paper, ST-13, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.238</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">1.50</Length>
    </TubeCoupler>

    <!-- BT-58 couplers -->
    
    <!-- wall thickness taken to be .030 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-58C</PartNumber>
      <Description>Tube coupler, paper, BT-58, 1.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.436</InsideDiameter>
      <OutsideDiameter Unit="in">1.496</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </TubeCoupler>

    <!-- LT-150 couplers:  NONE -->
    
    <!-- BT-60 couplers -->
    
    <!-- Semroc coupler has specified wall .034" -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-60-4</PartNumber>
      <Description>Tube coupler, paper, BT-60, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.525</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>
    <!-- wall thickness taken to be .036 ***verify*** -->
    <!-- SOURCE ERROR: legacy Semroc site has JTC-60C length = 1.75", new eRockets/Semroc site has 1.5"
         Estes 1980 catalog gives 1.5".
    -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-60C</PartNumber>
      <Description>Tube coupler, paper, BT-60, 1.50"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.521</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">1.50</Length>
    </TubeCoupler>
    <!-- wall thickness taken to be .036 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-16</PartNumber>
      <Description>Tube coupler, paper, ST-16, 1.75"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.521</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </TubeCoupler>
    <!-- wall thickness taken to be .036 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-16S</PartNumber>
      <Description>Tube coupler, paper, ST-16, 0.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.521</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.5</Length>
    </TubeCoupler>

    <!-- LT-175 couplers:  NONE -->

    <!-- ST-18 couplers -->
    
    <!-- wall thickness taken to be .036 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-18</PartNumber>
      <Description>Tube coupler, paper, ST-18, 1.5"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.726</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </TubeCoupler>
    <!-- wall thickness taken to be .036 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-18S</PartNumber>
      <Description>Tube coupler, paper, ST-18, 0.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.726</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </TubeCoupler>

    <!-- Aerotech 1.9" tube couplers -->

    <!-- Direct Aerotech coupler (eRockets might be reselling the actual Aerotech part here) -->
    <!-- SOURCE ERROR: AER-11804 eRockets/Semroc 2017 web listing for AER-11804 gives internally inconsistent
         values of OD 1.804", ID 1.610", wall 0.083".  The OD is too big as the primary
         tube ID is only 1.80", and the ID doesn't agree with the given wall thickness.  I
         set the OD to a plausible 1.795", took the wall thickness at face value, and set
         the ID to 1.629 accordingly. -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-11804</PartNumber>
      <Description>Tube coupler, paper, AT 1.9", 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.629</InsideDiameter>
      <OutsideDiameter Unit="in">1.795</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>
    <!-- Semroc coupler for Aerotech 1.9" tube.  Has consistent dimensions on eRockets
         2017 site -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-1.88-4</PartNumber>
      <Description>Tube coupler, paper, AT 1.9", 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.66</InsideDiameter>
      <OutsideDiameter Unit="in">1.79</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>
    
    <!-- LT-200 couplers: NONE -->
    
    <!-- ST-20 couplers -->
    
    <!-- wall thickness taken to be .036 ***verify*** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>HTC-20</PartNumber>
      <Description>Tube coupler, paper, ST-20, 2.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.926</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </TubeCoupler>
    <!-- SOURCE ERROR: CPL-ST20-4 eRockets/Semroc 2017 listing for CPL-ST20-4 gives internallly inconsistent values
         of ID 1.952, OD 1.981, .036 wall.  I took the wall thickness as likely to be
         correct and used an OD with offset of .005 from the ID of an ST-20.
    -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-ST20-4</PartNumber>
      <Description>Tube coupler, paper, ST-20, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">1.923</InsideDiameter>
      <OutsideDiameter Unit="in">1.995</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>

    <!-- BT-70 couplers -->
    
    <!-- ID adopted as Estes spec of 2.115 (yielding .029 wall) -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-70E</PartNumber>
      <Description>Tube coupler, paper, BT-70, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.115</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">4.0</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-70A</PartNumber>
      <Description>Tube coupler, paper, BT-70, 1.25"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.115</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-70D</PartNumber>
      <Description>Tube coupler, paper, BT-70, 0.625"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.115</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.625</Length>
    </TubeCoupler>

    <!-- LT-225 couplers: NONE -->

    <!-- LT-275 couplers: NONE -->

    <!-- BT-80 couplers -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-80-4</PartNumber>
      <Description>Tube coupler, paper, BT-80, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.486</InsideDiameter>
      <OutsideDiameter Unit="in">2.554</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>
    <!-- JT-80C complications!  See http://www.rocketryforum.com/archive/index.php/t-128230.html
         There are two different versions:

         "Old" JT-80C was a .021" wall glassine finish tube, not fish paper.
         Thus it has OD 2.554, ID 2.512

         "New" JT-80 is fish paper and has a .040 wall, and thus OD 2.554, ID 2.474  (per BMS specs)

         *** Determine which versions Semroc sold/sells ***
         meanwhile I have listed both variants
    -->
    <!-- The legacy Semroc site only lists JT-80E, and has no JT-80C -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-80E</PartNumber>
      <Description>Tube coupler, paper, BT-80, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.474</InsideDiameter>
      <OutsideDiameter Unit="in">2.554</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </TubeCoupler>
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-80C legacy</PartNumber>
      <Description>Tube coupler, paper, BT-80, glassine, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.512</InsideDiameter>
      <OutsideDiameter Unit="in">2.554</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    <!-- *** oddly, the new eRockets/Semroc site describes their JT-80C as "rice paper", a
    unique designation among all their couplers. *** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>JT-80C new</PartNumber>
      <Description>Tube coupler, paper, BT-80, 1.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.474</InsideDiameter>
      <OutsideDiameter Unit="in">2.554</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </TubeCoupler>
    <!-- Aerotech coupler for AT 2.6" tube having same ID as BT-80.  Adopted the .083 wall
         quoted by eRockets for the 1.9" tube -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-12606</PartNumber>
      <Description>Tube coupler, paper, BT-80, 4.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.390</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">4.00</Length>
    </TubeCoupler>

    <!-- T-3.0" tube couplers -->
    
    <!-- SOURCE ERROR: (?) CPL-3.0=34 OD quoted of 2.92" is a very sloppy fit if the
         intended mating tube is LOC 3.0" tube with its 3.00" ID. LOC couplers are OD
         2.99, ID 2.88, wall 0.055; I adopted that for now *** Investigate *** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-3.0-34</PartNumber>
      <Description>Tube coupler, paper, T-3.0, 34.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.88</InsideDiameter>
      <OutsideDiameter Unit="in">2.99</OutsideDiameter>
      <Length Unit="in">34.0</Length>
    </TubeCoupler>
    <!-- SOURCE ERROR: CPL-3.0"-6" inconsistent length.  PN given as CPL-3.0"-6", but in description
    it is CPL-3.0"-4" and describes 4" length.  I adopted 6" *** investigate *** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-3.0-6</PartNumber>
      <Description>Tube coupler, paper, T-3.0, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">2.88</InsideDiameter>
      <OutsideDiameter Unit="in">2.99</OutsideDiameter>
      <Length Unit="in">6.0</Length>
    </TubeCoupler>
    
    <!-- BT-100 couplers -->

    <!-- SOURCE ERROR: CPL-100-6 dimensions on eRocokets/Semroc 2017 site are completely wrong, with
         OD 2.920", ID 2.900", wall .034".  The OD would be appropriate for the
         CPL-3.0". I adopted a correct OD to mate with BT-100, and took the wall thickness
         as correct -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-100-6</PartNumber>
      <Description>Tube coupler, paper, BT-100, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.629</InsideDiameter>
      <OutsideDiameter Unit="in">3.697</OutsideDiameter>
      <Length Unit="in">6.00</Length>
    </TubeCoupler>

    <!-- BT-101 couplers -->

    <!-- SOURCE ERROR: CPL-101-6 Data missing for BT-101 couplers.  I adopted proper mating OD for
         BT-101 and took wall .034 as used in other Semroc couplers *** investigate *** -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-101-6</PartNumber>
      <Description>Tube coupler, paper, BT-101, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.826</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">6.00</Length>
    </TubeCoupler>
    
    <!-- Aerotech 4.0" tube couplers (mating tube ID same as BT-101).  Dimensions not
         given; using 3.89 OD and .083 wall as used for other AT tubes -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AER-14008</PartNumber>
      <Description>Tube coupler, paper, T-4.0, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">3.724</InsideDiameter>
      <OutsideDiameter Unit="in">3.89</OutsideDiameter>
      <Length Unit="in">6.00</Length>
    </TubeCoupler>

    <!-- T-4.5" couplers -->
    <!-- This is an odd product as Semroc offers no 4.5" tube -->
    <TubeCoupler>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CPL-4.5-6</PartNumber>
      <Description>Tube coupler, paper, T-4.5, 6.0"</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">4.31</InsideDiameter>
      <OutsideDiameter Unit="in">4.47</OutsideDiameter>
      <Length Unit="in">6.00</Length>
    </TubeCoupler>
    
    
    <!-- centering rings -->
    <!-- =============== -->
    
    <!-- Semroc has some unique centering rings with punch-outs that allow one ring to fit
         multiple tube sizes.  To handle this, I have made a separate listing for each
         size supported by the PN, and appended a suffix to the PN to indicate which size
         has been selected.  However I have only expanded these out for some dual-size
         rings.  I did not expand out the RAU series "universal" rings, which have ~15
         combinations. -->

    <!-- AR series thick centering rings for Estes compatible tube sizes -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AR-520</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-20, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.543</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AR-2050</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-50, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>AR-2050S</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-50, .25", split</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>

    <!-- CRs (thin fiber or ply) for Centuri compatible tube sizes -->

    <!-- BT-1+ inner diam -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-1+-5</PartNumber>
      <Description>Centering ring, fiber, BT-1+ to BT-5, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.248</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-1+-20</PartNumber>
      <Description>Centering ring, fiber, BT-1+ to BT-20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.248</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <!-- BT-2 inner diam -->
    
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2-3</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-3, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.347</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-2-3</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-3, 1/8"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.347</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2-4</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-4, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.420</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-2-4</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-4, 1/8"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.420</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2-5</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-5, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2-20</PartNumber>
      <Description>Centering ring, fiber, BT-2 to BT-20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.283</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <!-- BT-3 inner diam -->

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-3-4</PartNumber>
      <Description>Centering ring, fiber, BT-3 to BT-4, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.420</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </EngineBlock>

    <!-- CR-3-5 is identical to EB-5 -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-3-5</PartNumber>
      <Description>Centering ring, fiber, BT-3 to BT-5, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-3-20</PartNumber>
      <Description>Centering ring, fiber, BT-3 to BT-20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <!-- BT-4 inner diam -->

    <!-- CR-4-5 has slightly incorrect OD on eRockets/SEMROC site -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-4-5</PartNumber>
      <Description>Centering ring, fiber, BT-4 to BT-5, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.450</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-4-20</PartNumber>
      <Description>Centering ring, fiber, BT-4 to BT-20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.450</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-4-50</PartNumber>
      <Description>Centering ring, fiber, BT-4 to BT-50, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.450</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </EngineBlock>

    <!-- #5 / BT-5 inner diam -->

    <!-- CR-57 was 0.25" long in original OpenRocket file, keeping for now -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-57</PartNumber>
      <Description>Centering ring, fiber, #5 to #7, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-57EH</PartNumber>
      <Description>Centering ring, fiber, #5 to #7, .05", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- CR-5-20-1/8 is same as EB-20A -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-5-20-1/8</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-20, 0.125"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-5-20</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-20, .06"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.06</Length>
    </CenteringRing>
    <!-- thickness .25 in original OpenRocket file, confirmed by eRockets site -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-58</PartNumber>
      <Description>Centering ring, fiber, #5 to #8, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-5-50</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-50, .06"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.06</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-510</PartNumber>
      <Description>Centering ring, fiber, #5 to #10, .025"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">0.025</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-510EH</PartNumber>
      <Description>Centering ring, fiber, #5 to #10, .025", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">0.025</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-510SC</PartNumber>
      <Description>Centering ring, fiber, #5 to #10, .025", shock cord holes</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">0.025</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-511</PartNumber>
      <Description>Centering ring, fiber, #5 to #11, .025"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.025</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-5-55</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-55, .06"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.06</Length>
    </CenteringRing>
    <!-- CR-513 thickness not specified on eRockets site -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-513</PartNumber>
      <Description>Centering ring, fiber, #5 to #13, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-5-60</PartNumber>
      <Description>Centering ring, fiber, BT-5 to BT-60, .06"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.545</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.06</Length>
    </CenteringRing>

    <!-- #7 inner diam -->
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-8T</PartNumber>
      <Description>Centering ring, fiber, #7 to #8, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- L means long but len was 0.08" in original file - changed to .25" since eRockets
         site says its spiral wound -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-8L</PartNumber>
      <Description>Centering ring, fiber, #7 to #8, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <!-- len was 0.07" in original file, changed to .05" -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-8F</PartNumber>
      <Description>Centering ring, fiber, #7 to #8F, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.883</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- CR-7-9 same as #9 thrust ring TR-9 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-9</PartNumber>
      <Description>Centering ring, fiber, #7 to #9, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-9EH</PartNumber>
      <Description>Centering ring, fiber, #7 to #9, .090", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.090</Length>
    </CenteringRing>
    <!-- CR-7-9EH2 has 2 different styles of hook slots adjacent to each other -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-9EH2</PartNumber>
      <Description>Centering ring, fiber, #7 to #9, .090", 2x engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.090</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-10</PartNumber>
      <Description>Centering ring, fiber, #7 to #10, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-11</PartNumber>
      <Description>Centering ring, fiber, #7 to #11, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-115</PartNumber>
      <Description>Centering ring, fiber, #7 to #115, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-55EH</PartNumber>
      <Description>Centering ring, fiber, #7 to BT-55, .05", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-55</PartNumber>
      <Description>Centering ring, fiber, #7 to BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-13</PartNumber>
      <Description>Centering ring, fiber, #7 to #13, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-60, CR-KD-6</PartNumber>
      <Description>Centering ring, fiber, #7 to BT-58, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.496</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-60</PartNumber>
      <Description>Centering ring, fiber, #7 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-16</PartNumber>
      <Description>Centering ring, fiber, #7 to #16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-16EH</PartNumber>
      <Description>Centering ring, fiber, #7 to #16, .05", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- *** There is a separate listing of CR-7-18 as CR-KA-11 in the "for kits" section, not
    known if different *** -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-18</PartNumber>
      <Description>Centering ring, fiber, #7 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-18</PartNumber>
      <Description>Centering ring, fiber, #7 to #18, .05", engine hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7-20</PartNumber>
      <Description>Centering ring, fiber, #7 to #20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-7x3-20</PartNumber>
      <Description>Centering ring, fiber, #7 to #20, .05", triple cluster</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- BT-20 inner diam -->
    <!-- CR-20-T35 centers BT-20 in Quest metric 35mm (tube size not sold by Semroc) -->

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-50</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-50, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- CR-20-50 is same as EB-50 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-50</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-50, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </CenteringRing>
    <!-- Description mentions BT-52H but conflicts with PN and other description elements -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-52</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-52, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.986</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-11</PartNumber>
      <Description>Centering ring, fiber, BT-20 to #11, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-11EH</PartNumber>
      <Description>Centering ring, fiber, BT-20 to #11, .05", hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-55</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-58</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-58, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.496</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-60</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2x20-60</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-60, .05", 2 motor cluster</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-3x20-60</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-60, .05", 3 motor cluster</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <!-- CR-20-1.9P plywood ring for Aerotech 1.9", thickness not specified -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-1.9P</PartNumber>
      <Description>Centering ring, plywood, BT-20 to 1.9, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-20</PartNumber>
      <Description>Centering ring, fiber, BT-20 to #20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20-20P</PartNumber>
      <Description>Centering ring, plywood, BT-20 to #20, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-70</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-20-80</PartNumber>
      <Description>Centering ring, fiber, BT-20 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    
    <!-- #8 inner diam: complete, there is only one! -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-8-16</PartNumber>
      <Description>Centering ring, fiber, #8 to #16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.910</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- ST-8F: none exist -->

    <!-- LT-085 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-085-125</PartNumber>
      <Description>Centering ring, plywood, LT-085 to LT-125, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.947</InsideDiameter>
      <OutsideDiameter Unit="in">1.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- BT-50 inner diam, fiber -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-101, .375"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.375</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50-18</PartNumber>
      <Description>Centering ring, fiber, BT-50 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50-20P</PartNumber>
      <Description>Centering ring, plywood, BT-50 to #20, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-60</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-60P</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-60, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101(BTH-52)</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2x50-70/80(BT-70)</PartNumber>
      <Description>Centering ring, fiber, 2xBT-50 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2x50-70/80(BT-80)</PartNumber>
      <Description>Centering ring, fiber, 2xBT-50 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-2x50-70S</PartNumber>
      <Description>Centering ring, fiber, 2xBT-50 to BT-70, hook slots, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-1.9(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to T-1.88, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-60(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-60, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-70(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-80(BT-50)</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-100</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-100, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">3.700</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-101</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50-52H</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BTH-52, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50-55</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-55, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-55</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-55S</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-55, hook slot, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-58</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-58, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.496</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-60S</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-60, hook slot, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-70</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-80</PartNumber>
      <Description>Centering ring, fiber, BT-50 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50-T40</PartNumber>
      <Description>Centering ring, fiber, BT-50 to T-40, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="mm">39.95</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- BT-50 inner diam, plywood -->

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-3x50-80P</PartNumber>
      <Description>Centering ring, plywood, 3xBT-50 cluster to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-4x50-80P</PartNumber>
      <Description>Centering ring, plywood, 4xBT-50 cluster to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-60P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-60, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-20P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to ST-20, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-1,9P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to 1.9", .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50-70P</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-70, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <!-- In addition to the RA-50-70P there is also a dual-inner-diam RA-50/52H-70P -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-70P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-70, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-80P(BT-50)</PartNumber>
      <Description>Centering ring, plywood, BT-50 to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.978</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- BTH-50 (BT-50H) inner diam -->

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50H-55</PartNumber>
      <Description>Centering ring, fiber, BTH-50 to BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.994</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50H-60</PartNumber>
      <Description>Centering ring, fiber, BTH-50 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.994</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- #9 / ST-9 inner diam -->
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225X2</PartNumber>
      <Description>Centering ring, fiber, 2x#9 cluster to #225, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225X2P</PartNumber>
      <Description>Centering ring, plywood, 2x#9 cluster to #225, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-11</PartNumber>
      <Description>Centering ring, fiber, #9 to #11, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-115</PartNumber>
      <Description>Centering ring, fiber, #9 to #115, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-115S</PartNumber>
      <Description>Centering ring, fiber, #9 to #115, .25", split</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-125</PartNumber>
      <Description>Centering ring, fiber, #9 to #125, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.248</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-55</PartNumber>
      <Description>Centering ring, fiber, #9 to BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-55E</PartNumber>
      <Description>Centering ring, fiber, #9 to BT-55, .05", hook cutout</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-13</PartNumber>
      <Description>Centering ring, fiber, #9 to #13, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-60</PartNumber>
      <Description>Centering ring, fiber, #9 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-150P</PartNumber>
      <Description>Centering ring, plywood, #9 to #150, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.498</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-16</PartNumber>
      <Description>Centering ring, fiber, #9 to #16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-16P</PartNumber>
      <Description>Centering ring, plywood, #9 to #16, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-175P</PartNumber>
      <Description>Centering ring, plywood, #9 to #175, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-175P</PartNumber>
      <Description>Centering ring, plywood, #9 to #175, .125", 4 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-18</PartNumber>
      <Description>Centering ring, fiber, #9 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-18P</PartNumber>
      <Description>Centering ring, plywood, #9 to #18, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-20</PartNumber>
      <Description>Centering ring, fiber, #9 to #20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-70</PartNumber>
      <Description>Centering ring, fiber, #9 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-70P</PartNumber>
      <Description>Centering ring, plywood, #9 to BT-70, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225</PartNumber>
      <Description>Centering ring, fiber, #9 to #225, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225P</PartNumber>
      <Description>Centering ring, plywood, #9 to #225, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225X2</PartNumber>
      <Description>Centering ring, fiber, 2x#9 cluster to #225, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225X2P</PartNumber>
      <Description>Centering ring, plywood, 2x#9 cluster to #225, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225X3</PartNumber>
      <Description>Centering ring, plywood, 3x#9 cluster to #225, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225-3F</PartNumber>
      <Description>Centering ring, plywood, #9 to #225, .125", 3 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-225-4F</PartNumber>
      <Description>Centering ring, plywood, #9 to #225, .125", 4 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-80</PartNumber>
      <Description>Centering ring, fiber, #9 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-9-80P</PartNumber>
      <Description>Centering ring, plywood, #9 to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- BT-51 and BT-52: none listed on Semroc site 2017 -->

    <!-- #10 inner diam -->
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-13</PartNumber>
      <Description>Centering ring, fiber, #10 to #13, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-13P</PartNumber>
      <Description>Centering ring, plywood, #10 to #13, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-16</PartNumber>
      <Description>Centering ring, fiber, #10 to #16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-16P</PartNumber>
      <Description>Centering ring, plywood, #10 to #16, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-175P</PartNumber>
      <Description>Centering ring, plywood, #10 to #175, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-18</PartNumber>
      <Description>Centering ring, fiber, #10 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10-20</PartNumber>
      <Description>Centering ring, fiber, #10 to #20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- #11 inside diam -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-11-18</PartNumber>
      <Description>Centering ring, fiber, #11 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.172</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    

    <!-- BTH-52 inner diam (same as LT-115), fiber -->
    <!-- *** Semroc gives wrong ID of 1.215, smaller than BTH-50 OD of 1.220 *** -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-52H-55</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-55, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-52H-60</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-60, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-52H-60</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-60, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-1.9(BTH-52)</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to T-1.88, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-60(BTH-52)</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-60, .25"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-70(BTH-52)</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-80(BTH-52)</PartNumber>
      <Description>Centering ring, fiber, BTH-52 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101(BT-52H)</PartNumber>
      <Description>Centering ring, fiber, BT-52H to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.138</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    
    <!-- BTH-52 inner size, plywood -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-60P(BTH-52)</PartNumber>
      <Description>Centering ring, plywood, BTH-52 to BT-60, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-20P(BTH-52)</PartNumber>
      <Description>Centering ring, plywood, BTH-50 to ST-20, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-50/52H-1.9P(BTH-52)</PartNumber>
      <Description>Centering ring, plywood, BTH-52 to 1.9", .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-70P(BTH-52)</PartNumber>
      <Description>Centering ring, plywood, BTH-52 to BT-70, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <!-- RA-50/52H-80P is incorrectly listed with the P suffix -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-80P(BTH-52)</PartNumber>
      <Description>Centering ring, plywood, BTH-52 to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50/52H-101P(BT-52H)</PartNumber>
      <Description>Centering ring, plywood, BT-52H to BT-101, .375"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.138</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.375</Length>
    </CenteringRing>
    <!-- dual-size part with secondary "38mm" size - unusual since no 38mm tubes carried.
         Thickness shown as 0.25" is different than RA-50/52H-101P at .375" -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-52H/38mm-101P(BT-52H)</PartNumber>
      <Description>Centering ring, plywood, BT-52H to BT-101, .250"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.138</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </CenteringRing>
    
    <!-- #115 inner diam -->
    <!-- Same (exactly) as BTH-52 alias BT-52H -->
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-150P</PartNumber>
      <Description>Centering ring, plywood, #115 to #150, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.498</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- PN was originally CR-11516 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-16</PartNumber>
      <Description>Centering ring, fiber, #115 to #16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-16EH</PartNumber>
      <Description>Centering ring, fiber, #115 to #16, .05", hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-16P</PartNumber>
      <Description>Centering ring, plywood, #115 to #16, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-175P</PartNumber>
      <Description>Centering ring, plywood, #115 to #175, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-175-4F</PartNumber>
      <Description>Centering ring, plywood, #115 to #175, .125", 4 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-18</PartNumber>
      <Description>Centering ring, fiber, #115 to #18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-18EH</PartNumber>
      <Description>Centering ring, fiber, #115 to #18, .05", hook slot</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- PN originally CR-11520 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-20</PartNumber>
      <Description>Centering ring, fiber, #115 to #20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-70P</PartNumber>
      <Description>Centering ring, plywood, #115 to BT-70, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-225P</PartNumber>
      <Description>Centering ring, plywood, #115 to #225, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-225-3F</PartNumber>
      <Description>Centering ring, plywood, #115 to #225, .125", 3 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-225-4F</PartNumber>
      <Description>Centering ring, plywood, #115 to #225, .125", 4 fin locks</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-80P</PartNumber>
      <Description>Centering ring, plywood, #115 to BT-80, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-115-275P</PartNumber>
      <Description>Centering ring, plywood, #115 to #275, .125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.748</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- BT-55 inner diam -->

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-55-58</PartNumber>
      <Description>Centering ring, fiber, BT-55 to BT-58, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.327</InsideDiameter>
      <OutsideDiameter Unit="in">1.496</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-55-60</PartNumber>
      <Description>Centering ring, fiber, BT-55 to BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.327</InsideDiameter>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-55-70</PartNumber>
      <Description>Centering ring, fiber, BT-55 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.327</InsideDiameter>
      <OutsideDiameter Unit="in">1.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-55-80</PartNumber>
      <Description>Centering ring, fiber, BT-55 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.327</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>


    <!-- BT-58 inner diam -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-58-80</PartNumber>
      <Description>Centering ring, fiber, BT-58 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.542</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- BT-60 inner diam -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-60-70</PartNumber>
      <Description>Centering ring, fiber, BT-60 to BT-70, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.639</InsideDiameter>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-60-80</PartNumber>
      <Description>Centering ring, fiber, BT-60 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.639</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>


    <!-- T-38 inner diam (ring ID = 1.64") -->
    
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-52H/38mm-101P(38mm)</PartNumber>
      <Description>Centering ring, plywood, 38mm to BT-101, .250"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.138</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </CenteringRing>
    

    <!-- BT-70 inner diam -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-70-80</PartNumber>
      <Description>Centering ring, fiber, BT-70 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">2.219</InsideDiameter>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-70-101</PartNumber>
      <Description>Centering ring, fiber, BT-70 to BT-80, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">2.219</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>
    
    <!-- BT-80 inner diam -->

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-80-101</PartNumber>
      <Description>Centering ring, fiber, BT-80 to BT-101, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">2.602</InsideDiameter>
      <OutsideDiameter Unit="in">3.894</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- ===================== -->
    <!-- Ejection Baffle Rings -->
    <!-- ===================== -->
    <!--
        Semroc has two different ejection baffle designs:
            Laser-cut fiber system using a 7-hole ring, a 1-hole ring, with a vented laser-cut fiber conical
            section between.

            Plywood system using two offset laser-cut 1-hole bulkheads and two pieces of smaller tubing

        Data availability:
             There is almost no dimension data and no weight data published for these products.  

             Thickness of the fiber rings is likely the same as for other Semroc centering rings, i.e. about 0.05"
             Thickness of the plywood rings is unspecified.

             Based on CAD drawings on the 2018 site, I've adopted 0.010" clearance for the OD the
             rings vs the tube ID into which they fit.

             Length of the baffle assemblies is unspecified.  On the 2018 site there are CAD renderings of
             the fiber units suggesting the overall length is 150-175% of the diameter.  There are low-fidelity
             line drawings of the plywood/tube units that I do not trust for dimensions.  From the legacy
             site we know that the EB-80T system used 2.0" long LT11520 tubes, making the overall length of
             that unit about 2.5".

             Diameters of the holes in the 1-hole rings in fiber rings are not specified.

             Diameters of the holes for the 7-hole rings EBR-58, EBR-60, EBR-16 and EBR-18 are known due
             to publication of high fidelity CAD drawings for these items on the 2018 eRockets website.

             Thickness of the fiber cone is unspecified and not currently known.

        Modeling a fiber baffle assembly in OpenRocket:

             You need 3 separate components:
                * A 7-hole ring.  Use one of the EBR-xx components I've put in this file
                * A 1-hole ring.  For now, just use anaother copy of the EBR-xx, mass will be pretty close
                * The fiber cone.  I recommend creating a tube that will have the right mass and
                  approximately the right moments of inertia.

        Modeling a plywood baffle assembly in OpenRocket:

             You need four components for the plywood/tube assembly:
                * Two plywood rings with holes the diameter of the baffle tubes.
                * Two baffle tubes offset from the centerline by half their diameter and in
                  longitudinala position by about 0.5"

        The multi-hole baffle rings for the fiber cone system have 7 holes.
        You can model them as OpenRocket bulkheads with a central hole to make the
        mass correct, computed as follows using some quick photogrammetry on the #16
        baffle.  About 35% of the area has been removed, so

           dhole^2 = .35 * diam^2
           dhole = .59 * diam

        The moments of inertia will be slightly too large, but not enough to matter.

        Parts tabulation:

        Ejection baffles and rings appear in the following sizes on the legacy and new websites.  The new
        eRockets/Semroc site has been considerably improved with matching drawings for all baffle related
        products.  A BT-50 / #9 size has been added, while plywood rings are no longer listed separately.
        In addition, the #20 size baffle has changed from being listed as fiber to plywood.

        PN suffixes: for the legacy ring PNs, the T suffix indicates a 2-pack.  Meaning of the W suffix is unknown.
        Significance of the T suffix on the baffle PNs is also unknown.

        Size      Type    Baffle Assy          Rings (legacy)        Rings     Notes
                          Legacy   2018                              2018
        ===========================================================================================================
        BT-50/#9  Fiber            EB-9                                        Fits ST-9 and BT-50
        BT-55     Fiber   EB-55    EB-55                  
        BT-58     Fiber                        EBR-58                EBR-58
        BT-60     Fiber   EB-60    EB-60       EBR-60  EBR-60W       EBR-60    EBR-60 vs EBR-60W diff unknown
        BT-70     Plywood EB-70    EB-70T      EBR-70T               
        BT-80     Plywood EB-80T   EB-80T      EBR-80T               
        #9        Fiber            EB-9                                        same as BT-50/#9 row above
        #13       Fiber   EB-13    EB-13                  
        #16       Fiber   EB-16    EB-16       EBR-16 EBR-16W        EBR-16    EBR-16 vs EBR-16W diff unknown
        #18       Fiber   EB-18    EB-18       EBR-18W               EBR-18
        #20       Fiber   EB-20                                                legacy site is fiber
        #20       Plywood          EB-ST-20                                    2018 site is plywood
        #125      Fiber   EB-125   EB-125                                      legacy site describes as fiber system
        #175      Plywood EB-175T  EB-175T     EBR-175T                  
        #225      Plywood EB-225T  EB-225T     EBR-225 EBR-225T                EBR-225 was single ring, EBR-225T is 2

        For purposes of this file, I created synthetic part numbers for the baffle rings that were
        never sold separately.
    -->

    <!-- FIBER 7-HOLE BAFFLE RINGS -->

    <!-- EBR-50 and EBR-9 are the same product -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-50_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, BT-50, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.558</InsideDiameter>
      <OutsideDiameter Unit="in">0.94</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-9_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, ST-9, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.558</InsideDiameter>
      <OutsideDiameter Unit="in">0.94</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-55_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, BT-55, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.755</InsideDiameter>
      <OutsideDiameter Unit="in">1.273</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-58</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, BT-58, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.882</InsideDiameter>
      <OutsideDiameter Unit="in">1.49</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-60, EBR-60W</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, BT-60, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.939</InsideDiameter>
      <OutsideDiameter Unit="in">1.59</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-13_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, ST-13, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.765</InsideDiameter>
      <OutsideDiameter Unit="in">1.29</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-16, EBR-16W</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, ST-16, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.942</InsideDiameter>
      <OutsideDiameter Unit="in">1.59</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-18, EBR-18W</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, ST-18, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.060</InsideDiameter>
      <OutsideDiameter Unit="in">1.79</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-20_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, ST-20, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.178</InsideDiameter>
      <OutsideDiameter Unit="in">1.99</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-125_syn</PartNumber>
      <Description>Baffle ring, fiber, 7 holes, LT-125, .05"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.736</InsideDiameter>
      <OutsideDiameter Unit="in">1.24</OutsideDiameter>
      <Length Unit="in">0.05</Length>
    </CenteringRing>

    <!-- PLYWOOD 1-HOLE BAFFLE RINGS.  Thickness and hole size are estimated. -->

    <!-- EBR-70T.  Assumed baffle tube is BTH-50 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-70T</PartNumber>
      <Description>Baffle ring, plywood, 1 hole, BT-70, 0.125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.994</InsideDiameter>
      <OutsideDiameter Unit="in">2.165</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- EBR-80T.  Baffle tube is known to be LT-115 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-80T</PartNumber>
      <Description>Baffle ring, plywood, 1 hole, BT-80, 0.125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.222</InsideDiameter>
      <OutsideDiameter Unit="in">2.55</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- EBR-20_ply_syn.  Assuming baffle tube is ST-8 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-20_ply_syn</PartNumber>
      <Description>Baffle ring, plywood, 1 hole, ST-20, 0.125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.910</InsideDiameter>
      <OutsideDiameter Unit="in">1.99</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- EBR-175T.  Assuming baffle tube is ST-7 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-175, EBR-175T</PartNumber>
      <Description>Baffle ring, plywood, 1 hole, LT-175, 0.125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">0.761</InsideDiameter>
      <OutsideDiameter Unit="in">1.74</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- EBR-225T.  Assuming baffle tube is ST-10 -->
    <CenteringRing>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EBR-225, EBR-225T</PartNumber>
      <Description>Baffle ring, plywood, 1 hole, LT-225, 0.125"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <InsideDiameter Unit="in">1.042</InsideDiameter>
      <OutsideDiameter Unit="in">2.24</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </CenteringRing>

    <!-- ================= -->
    <!-- Balsa Transitions -->
    <!-- ================= -->
    <!-- Two versions are given for each part.  The first is an increasing transition with fore diameter
         is smaller than the aft.  The reducing version is tagged with [R] in the PN, and has the fore
         diameter as the larger. -->
    <!-- SOURCE ERROR: The 2018 Semroc/e-rockets site does not list masses for any BR-xxx balsa reducers.
         Many have masses given on the Semroc legacy site, but the new ones do not.  -->



    <!-- ============================= -->
    <!-- BR-5xx - ST-5 to larger sizes -->
    <!-- ============================= -->

    <!-- There is no BR-56 -->

    <!-- BR-57 has exposed length 0.5 in, both shoulder lengths 0.69 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-57</PartNumber>
        <Description>Transition, balsa, #5 to #7, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-57 [R]</PartNumber>
        <Description>Transition, balsa, #7 to #5, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.50</Length>
    </Transition>

    <!-- BR-58 has exposed length 0.6 in, both shoulders 0.69 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-58</PartNumber>
        <Description>Transition, balsa, #5 to #8, increasing, 0.6 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.60</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-58 [R]</PartNumber>
        <Description>Transition, balsa, #8 to #5, reducing, 0.6 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.60</Length>
    </Transition>

    <!-- BR-58F has exposed length 0.6 in, both shoulders 0.69 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-58F</PartNumber>
        <Description>Transition, balsa, #5 to #8F, increasing, 0.6 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.921</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.885</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.60</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-58F [R]</PartNumber>
        <Description>Transition, balsa, #8F to #5, reducing, 0.6 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.921</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.885</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.60</Length>
    </Transition>

    <!-- BR-59 has both shoulders 0.69" per drawings on legacy site
         SOURCE ERROR:  BR-59 on legacy site table has exposed length of 1.0 in, but drawing scales to 0.82 in
         (13/16 inch).  Which is right?
    -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-59</PartNumber>
        <Description>Transition, balsa, #5 to #9, increasing, 0.82 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.82</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-59 [R]</PartNumber>
        <Description>Transition, balsa, #9 to #5, reducing, 0.82 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.82</Length>
    </Transition>

    <!-- BR-510 has shoulders 0.69 in, exposed length 0.75 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-510</PartNumber>
        <Description>Transition, balsa, #5 to #10, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-510 [R]</PartNumber>
        <Description>Transition, balsa, #10 to #5, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-511 has shoulders 0.69 in, exposed length 0.75 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-511</PartNumber>
        <Description>Transition, balsa, #5 to #11, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-511 [R]</PartNumber>
        <Description>Transition, balsa, #11 to #5, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-513 has shoulders 0.69 in, exposed length 1.5 in -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-513</PartNumber>
        <Description>Transition, balsa, #5 to #13, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.134</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-513 [R]</PartNumber>
        <Description>Transition, balsa, #13 to #5, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.134</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-5-T20 (metric 20mm) has length 0.75" (e-rockets site), shoulders 0.5" (estimated from oblique photo) -->
    <!-- SOURCE ERROR: BR-5-T20 price listed at $9.99 on new site though all others of similar size are $3 to $3.50.
         No drawing shown. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-5-T20</PartNumber>
        <Description>Transition, balsa, #5 to metric T20, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">19.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-5-T20 [R]</PartNumber>
        <Description>Transition, balsa, metric T20 to #5, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">19.0</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-5-T20A (metric 20mm) has length 1.0" (e-rockets site), shoulders 0.5" (estimated from photo) -->
    <!-- SOURCE ERROR: BR-5-T20A price listed at $9.99 on new site though all others of similar size are $3 to $3.50.
         No drawing shown. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-5-T20A</PartNumber>
        <Description>Transition, balsa, #5 to metric T20, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">19.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-5-T20 [R]</PartNumber>
        <Description>Transition, balsa, metric T20 to #5, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">19.0</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- ============================= -->
    <!-- BR-6xx - ST-6 to larger sizes -->
    <!-- ============================= -->

    <!-- BR-616NC is flared Nike Cajun 1/10 scale; front shoulder is 0.625 in, aft shoulder 0.69 in, exposed length 1.7 in.
         Mass discrepancy due to flare not considered significant enough for mass override.
         SOURCE ERROR:  Semroc BR-616NC has PN suggesting smaller diam is ST-6 (.650 OD/ .610ID), however description
         on legacy site says "will connect a #8 body tube to a #16 body tube".  The drawing scales to a forward shoulder
         diameter of .605 which confirms it's really for #6 tube and the description is wrong.
    -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-610NC</PartNumber>
        <Description>Transition, balsa, #6 to #16, increasing, flared, Nike-Cajun 1/10</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.650</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.610</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.625</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.70</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-610NC [R]</PartNumber>
        <Description>Transition, balsa, #16 to #6, reducing, flared, Nike-Cajun 1/10</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.650</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.610</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.625</AftShoulderLength>
        <Length Unit="in">1.70</Length>
    </Transition>

    <!-- ============================= -->
    <!-- BR-7xx - ST-7 to larger sizes -->
    <!-- ============================= -->

    <!-- BR-78 and BR-78S have 0.69 shoulders; BR-78 exposed length is nominally 0.875" while BR-78S is 0.5"
         Actual exposed length from the legacy site drawings is 0.90" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-78</PartNumber>
        <Description>Transition, balsa, #7 to #8, increasing, 0.90", PN BR-78</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.90</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-78 [R]</PartNumber>
        <Description>Transition, balsa, #8 to #7, reducing, 0.90", PN BR-78</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.90</Length>
    </Transition>

    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-78S</PartNumber>
        <Description>Transition, balsa, #7 to #8, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.500</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-78 [R]</PartNumber>
        <Description>Transition, balsa, #8 to #7, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.500</Length>
    </Transition>

    <!-- BR-79 and BR-79L have 0.69 shoulders; BR-79 exposed length is 1.00 while BR-79L is 2.0" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-79</PartNumber>
        <Description>Transition, balsa, #7 to #9, increasing, 1.00 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-79 [R]</PartNumber>
        <Description>Transition, balsa, #9 to #7, reducing, 1.00 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.00</Length>
    </Transition>

    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-79L</PartNumber>
        <Description>Transition, balsa, #7 to #9, increasing, 2.00 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-79L [R]</PartNumber>
        <Description>Transition, balsa, #9 to #7, reducing, 2.00 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>

    <!-- BR-710 has shoulders 0.69", length 0.75" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-710</PartNumber>
        <Description>Transition, balsa, #7 to #10, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-710 [R]</PartNumber>
        <Description>Transition, balsa, #10 to #7, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-711 has shoulders 0.69", length 0.75" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-711</PartNumber>
        <Description>Transition, balsa, #7 to #11, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-711 [R]</PartNumber>
        <Description>Transition, balsa, #11 to #7, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-713 has shoulders 0.69", length 1.5" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-713</PartNumber>
        <Description>Transition, balsa, #7 to #13, increasing, 1.50 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-713 [R]</PartNumber>
        <Description>Transition, balsa, #13 to #7, reducing, 1.50 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-716 has shoulders 0.69", length 2.0" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-716</PartNumber>
        <Description>Transition, balsa, #7 to #16, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-716 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #7, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- BR-718 has 0.69" shoulders, 2.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-718</PartNumber>
        <Description>Transition, balsa, #7 to #18, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-718 [R]</PartNumber>
        <Description>Transition, balsa, #18 to #7, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>


    <!-- ============================= -->
    <!-- BR-8xx - ST-8 to larger sizes -->
    <!-- ============================= -->

    <!-- BR-810 has 0.69" shoulders and 0.5" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-810</PartNumber>
        <Description>Transition, balsa, #8 to #10, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-810 [R]</PartNumber>
        <Description>Transition, balsa, #10 to #8, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>

    <!-- BR-813 has a 3/8" cylindrical section and total exposed length of 1.87" (see below) -->
    <!-- SOURCE ERROR: BR-813 correct overall exposed length is unclear.
            Semroc legacy site:         1.70"  Disagrees significantly with drawing
            Scaled drawing (legacy):    1.99"  (cyl section 0.34" plus 1.65" conical taper - pixel scale 224/in)
            Semroc/e-rockets new site:  1.75"  (1 3/8" plus 3/8" straight section)
            Scaled drawing (new):       1.87"  (cyl section 0.32" plus 1.56" conical taper - different pixel scale 324/in)
         The stated length disagrees with the drawing on both the legacy and new sites.  There was obviously an attempt
         to improve the description on the new site, and the drawing was changed, but they still don't agree.
         I'm breaking the tie by using the new drawing value of 1.87 -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-813</PartNumber>
        <Description>Transition, balsa, #8 to #13, increasing, 1.87 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.87</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-813 [R]</PartNumber>
        <Description>Transition, balsa, #13 to #8, reducing, 1.87 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.87</Length>
    </Transition>

    <!-- BR-813P is a true cylindrical taper transition, shoulders 0.69" and length supposed to be 1.5" -->
    <!--- SOURCE ERROR: BR-813P inconsistency strikes again.  Legacy site gives length as 1.5", but drawing
          scales to about 1.85" and still shows the cylindrical section.  New site shows a photo of a
          transition that clearly does not have the cylinder, but gives no length and no drawing.
          Here I'm going to go with the legacy table.  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-813P</PartNumber>
        <Description>Transition, balsa, #8 to #13, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-813P [R]</PartNumber>
        <Description>Transition, balsa, #13 to #8, reducing, 1.50 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-816 has 0.69" shoulders and length 1.50" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-816</PartNumber>
        <Description>Transition, balsa, #8 to #16, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-816 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #8, reducing, 1.50 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-816NT is a flared bi-conical transition for 1/10 Nike-Tomahawk
         Fore shoulder is 0.5", aft is 0.62", length is 1.7" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-816NT</PartNumber>
        <Description>Transition, balsa, #8 to #16, increasing, 1.7 in len, Nike-Tomahawk 1/10</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.70</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-816 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #8, reducing, 1.7 in len, Nike-Tomahawk 1/10</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.70</Length>
    </Transition>

    
    <!-- =============================== -->
    <!-- BR-8Fxx - ST-8F to larger sizes -->
    <!-- =============================== -->

    <!-- BR-8F11 has 0.69" shoulders, 1.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-8F11</PartNumber>
        <Description>Transition, balsa, #8F to #11, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.921</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.885</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-8F11 [R]</PartNumber>
        <Description>Transition, balsa, #11 to #8F, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.921</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.885</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- BR-8F11L has 0.69" shoulders, 1.5" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-8F11L</PartNumber>
        <Description>Transition, balsa, #8F to #11, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.921</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.885</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-8F11L [R]</PartNumber>
        <Description>Transition, balsa, #11 to #8F, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.921</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.885</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- ============================= -->
    <!-- BR-9xx - ST-9 to larger sizes -->
    <!-- ============================= -->

    <!-- BR-916 has 0.69" shoulders and 2.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-916</PartNumber>
        <Description>Transition, balsa, #9 to #16, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-916 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #9, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- BR-918 has 0.69" shoulders and 2.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-918</PartNumber>
        <Description>Transition, balsa, #9 to #18, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-918 [R]</PartNumber>
        <Description>Transition, balsa, #18 to #9, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- BR-920 has 0.875 shoulders and 2.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-920</PartNumber>
        <Description>Transition, balsa, #9 to #20, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.998</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.875</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.875</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-920 [R]</PartNumber>
        <Description>Transition, balsa, #20 to #9, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.875</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.998</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.875</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- =============================== -->
    <!-- BR-10xx - ST-10 to larger sizes -->
    <!-- =============================== -->

    <!-- BR-1013 has 0.69" shoulders and 0.75" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1013</PartNumber>
        <Description>Transition, balsa, #10 to #13, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1013 [R]</PartNumber>
        <Description>Transition, balsa, #13 to #10, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-1013B has 0.69" shoulders and 1.20" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1013B</PartNumber>
        <Description>Transition, balsa, #10 to #13, increasing, 1.20 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.20</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1013B [R]</PartNumber>
        <Description>Transition, balsa, #13 to #10, reducing, 1.20 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.20</Length>
    </Transition>

    <!-- BR-1016 has 0.69" shoulders and 1.5" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1016</PartNumber>
        <Description>Transition, balsa, #10 to #16, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1016 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #10, reducing, 1.50 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-1016S has 0.69" shoulders and 1.2" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1016S</PartNumber>
        <Description>Transition, balsa, #10 to #16, increasing, 1.2 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.20</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1016S [R]</PartNumber>
        <Description>Transition, balsa, #16 to #10, reducing, 1.20 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.20</Length>
    </Transition>

    <!-- =============================== -->
    <!-- BR-11xx - ST-11 to larger sizes -->
    <!-- =============================== -->

    <!-- BR-1116 has 0.69" shoulders and length 1.50" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1116</PartNumber>
        <Description>Transition, balsa, #11 to #16, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1116 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #11, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- BR-1118 has 0.69" shoulders and 2.0" length -->
    <!-- SOURCE ERROR:  BR-1118 Legacy Semroc table gives 1.50" length, but drawing scales to 2.0"
         New site does not list length, and has same drawing.  Adopting the drawing value. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1118</PartNumber>
        <Description>Transition, balsa, #11 to #18, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1118 [R]</PartNumber>
        <Description>Transition, balsa, #18 to #11, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- BR-1120NT has fore shoulder 0.67", aft shoulder 0.71", length 2.08", flared Nike-Tomahawk -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1120NT</PartNumber>
        <Description>Transition, balsa, #11 to #20, increasing, 2.08 in len, Nike-Tomahawk</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.71</AftShoulderLength>
        <Length Unit="in">2.08</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1120NT [R]</PartNumber>
        <Description>Transition, balsa, #20 to #11, reducing, 2.08 in len, Nike-Tomahawk</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.71</AftShoulderLength>
        <Length Unit="in">2.08</Length>
    </Transition>

    <!-- =============================== -->
    <!-- BR-13xx - ST-13 to larger sizes -->
    <!-- =============================== -->

    <!-- BR-1316 has 0.69" shoulders and 0.75" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316</PartNumber>
        <Description>Transition, balsa, #13 to #16, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #13, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-1316F has fore shoulder 0.69", aft shoulder 0.75", length 1.75" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316F</PartNumber>
        <Description>Transition, balsa, #13 to #16, increasing, 1.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.75</AftShoulderLength>
        <Length Unit="in">1.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316F [R]</PartNumber>
        <Description>Transition, balsa, #16 to #13, reducing, 1.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.75</Length>
    </Transition>

    <!-- BR-1316 has fore shoulder 0.69", aft shoulder 0.75", length 1.5" -->
     <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316L</PartNumber>
        <Description>Transition, balsa, #13 to #16, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.75</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316L [R]</PartNumber>
        <Description>Transition, balsa, #16 to #13, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- BR-1316M has fore shoulder 0.69", aft shoulder 0.75", length 1.0" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316M</PartNumber>
        <Description>Transition, balsa, #13 to #16, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.75</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1316M [R]</PartNumber>
        <Description>Transition, balsa, #16 to #13, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- BR-1320 has both shoulders 0.85" (possibly wrong drawing), length 0.75" per spec -->
    <!-- SOURCE ERROR: BR-1320 Legacy Semroc site gives length as 0.75".  Drawing scales to 2.0".
         Mass listed is 0.28 oz which seems low for a 2" long adapter; it's the same as for the
         1.0" long BR-1316M.  The drawing also shows a cylinder section.  Overall I think the drawing is
         more likely to be wrong, so using the spec value of length 0.75", and the usual #13 shoulder of 0.69"  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1320</PartNumber>
        <Description>Transition, balsa, #13 to #20, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1320 [R]</PartNumber>
        <Description>Transition, balsa, #20 to #13, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BR-1320L has shoulders 0.85" and length 4.5" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1320L</PartNumber>
        <Description>Transition, balsa, #13 to #20, increasing, 4.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.300</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">4.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1320L [R]</PartNumber>
        <Description>Transition, balsa, #20 to #13, reducing, 4.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.300</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">4.5</Length>
    </Transition>


    <!-- =============================== -->
    <!-- BR-16xx - ST-16 to larger sizes -->
    <!-- =============================== -->

    <!-- BR-1618 has 0.85" shoulders and 1.5" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1618</PartNumber>
        <Description>Transition, balsa, #16 to #18, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1618 [R]</PartNumber>
        <Description>Transition, balsa, #18 to #16, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- BR-1618F is an odd shape with a slightly flared aft diameter, and a 0.30" cylindrical section at the #18 end.
         shoulders are 0.85" and overall exposed length is 1.8" --> 
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1618F</PartNumber>
        <Description>Transition, balsa, #16 to #18, increasing, flared + cylinder, 1.8 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.8</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1618F [R]</PartNumber>
        <Description>Transition, balsa, #18 to #16, reducing, flared + cylinder, 1.8 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.8</Length>
    </Transition>

    <!-- BR-1620 has 0.85" shoulders and a 1.5" exposed length (see below) -->
    <!-- SOURCE ERROR:  BR-1620 Semroc legacy site gives 1.5" exposed length, but the drawing scales to 1.75".
         Adopted the spec length because this drawing again looks like a special one with cylindrical section
         at the larger end -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1620</PartNumber>
        <Description>Transition, balsa, #16 to #20, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1620 [R]</PartNumber>
        <Description>Transition, balsa, #20 to #16, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- BR-1620F has 0.85" shoulders and 1.20" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1620F</PartNumber>
        <Description>Transition, balsa, #16 to #20, increasing, 1.2 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.2</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1620F [R]</PartNumber>
        <Description>Transition, balsa, #20 to #16, reducing, 1.2 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.2</Length>
    </Transition>

    <!-- BR-16225F has fore shoulder 0.90", aft shoulder 1.08", length 1.5" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-16225F</PartNumber>
        <Description>Transition, balsa, #16 to #225, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.90</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.08</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-16225F [R]</PartNumber>
        <Description>Transition, balsa, #225 to #16, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.225</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.08</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.90</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- =============================== -->
    <!-- BR-18xx - ST-18 to larger sizes -->
    <!-- =============================== -->

    <!-- SOURCE ERROR: BR-1820 and BR-1820L are seemingly conflated on the new Semroc/e-rockets site.
         For the unit on this page: http://www.erockets.biz/semroc-balsa-reducer-18-to-20-1-7-8-long-sem-br-1820/
            Title:              SEMROC BALSA REDUCER #18 TO #20, 1 7/8" LONG SEM-BR-1820
            Description text:   BR-1820L
            Drawing caption:    BR-1820
            Length, specified:  1 7/8"
            Length, scaled dwg: 1.78"
         The L suffix usually means "long", and this is confirmed by the URL.
         The drawing says "BR-1820", but the photo of the product shows a long reducer that generally
         matches the drawing.
         For the unit on this page: http://www.erockets.biz/semroc-balsa-reducer-18-to-20-sem-br-1820l/
            Title:              SEMROC BALSA REDUCER #18 TO #20 SEM-BR-1820L
            Description text:   Semroc Balsa Reducer #18 to #20   SEM-BR-1820L
            Drawing caption:    BR-1820L
            Length, specified:  - not given -
            Length, scaled dwg: 0.42"
         Here the nomenclature is consistent, but the drawing shows a very short exposed length inconsistent
         with the L designation.
    -->
    <!-- To resolve the BR-1820 / BR-1820L issue, I'm taking the long one to be BR-1820L. -->

    <!-- BR-60-18 is oddball cross-series adaapter, length 1.0", fore shoulder 1.07", aft shoulder 0.67".
         It has a BR prefix so I've grouped with the Centuri compatible set. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-60-18</PartNumber>
        <Description>Transition, balsa, BT-60 to ST-18, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.07</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-60-18 [R]</PartNumber>
        <Description>Transition, balsa, ST-18 to BT-60, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.07</AftShoulderLength>
        <Length Unit="in">2.4</Length>
    </Transition>

    <!-- BR-1820 scaled drawing dims are shoulder 0.67", length 0.375".  See discussion above. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1820</PartNumber>
        <Description>Transition, balsa, #18 to #20, increasing, 0.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">0.375</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1820 [R]</PartNumber>
        <Description>Transition, balsa, #20 to #18, reducing, 0.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">0.375</Length>
    </Transition>


    <!-- BR-1820L has 0.87" shoulders, length 1.875".  See discussion above.  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1820L</PartNumber>
        <Description>Transition, balsa, #18 to #20, increasing, 1.875 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.040</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">1.875</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-1820L [R]</PartNumber>
        <Description>Transition, balsa, #20 to #18, reducing, 1.875 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">1.875</Length>
    </Transition>


    <!-- BR-18225 from new site has 0.81" fore shoulder, 1.0" aft shoulder, and 2.0" length -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-18225</PartNumber>
        <Description>Transition, balsa, #18 to #225, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.81</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.0</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-18225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #18, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.81</AftShoulderLength>
        <Length Unit="in">2.00</Length>
    </Transition>


    <!-- =================================== -->
    <!-- BR-085-xxx - LT-085 to larger sizes -->
    <!-- =================================== -->

    <!-- BR-085-175 (new site only) scaled dwg fore shoulder 0.88, aft shoulder 1.06, length 1.42" -->
    <!-- SOURCE ERROR: BR-085-175 length not specified on new Semroc/e-rockets site -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-085-175</PartNumber>
        <Description>Transition, balsa, #085 to #175, increasing, 1.42 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.945</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.88</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.06</AftShoulderLength>
        <Length Unit="in">1.42</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-085-175 [R]</PartNumber>
        <Description>Transition, balsa, #175 to #085, reducing, 1.42 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.06</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.945</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.88</AftShoulderLength>
        <Length Unit="in">1.42</Length>
    </Transition>

    <!-- BR-085-225 (new site only except for length) spec length 2.5", scaled photo len 2.37", fore shoulder 0.96",
         aft shoulder 1.18".  There is no drawing so the scaled dimensions are not precise. Using spec length from
         old site with scaled photo dimensions from new site.  -->
    <!-- SOURCE ERROR: BR-085-225 length not specified on new Semroc/e-rockets site -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-085-225</PartNumber>
        <Description>Transition, balsa, #085 to #225, increasing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.945</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.865</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.96</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.18</AftShoulderLength>
        <Length Unit="in">2.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-085-225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #085, reducing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.18</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.945</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.865</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.96</AftShoulderLength>
        <Length Unit="in">2.50</Length>
    </Transition>

    <!-- ================================= -->
    <!-- BR-115xx - LT-115 to larger sizes -->
    <!-- ================================= -->
    <!-- e-rockets has introduced some new BRx-115xx reducers that do not appear on the legacy Semroc site -->

    <!-- BRS-84-115 is an oddball.  The fore diameter is 0.84" but it has no shoulder, so I've listed it under
         the LT-115 series since there's nothing else it can mate to. Photogrammetry is hard since there is only
         an oblique photo; I get an exposed length of 3.5" and a 115 shoulder length of 0.62" -->
    <!-- SOURCE ERROR: BRS-84-115 (only found on 2018 Semroc/e-rockets site) has no length given. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-84-115</PartNumber>
        <Description>Transition, balsa, 0.84" to #115, increasing, 3.5 in len, no fore shoulder</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.84</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.84</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.140</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.62</AftShoulderLength>
        <Length Unit="in">3.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-84-115 [R]</PartNumber>
        <Description>Transition, balsa, #115 to 0.84", reducing, 3.5 in len, no aft shoulder</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.140</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.62</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.84</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.84</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.50</Length>
    </Transition>


    <!-- BR-11516 has spec 3.375" length, scaled dwg shoulders 0.67" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-11516</PartNumber>
        <Description>Transition, balsa, #115 to #16, increasing, 3.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.140</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.375</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-11516 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #115, reducing, 3.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.140</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.375</Length>
    </Transition>


    <!-- BRS-115-16 "special" (on new site only) has no forward shoulder. All dims from scaled dwg on
         new Semroc/e-rockets site:
         length 4.25", #16 shoulder len 0.87" -->
    <!-- SOURCE ERROR: BRS-115-16 drawing is captioned "BRS-116-16" on new site -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BRS-115-16</PartNumber>
        <Description>Transition, balsa, #115 to #16, increasing, 4.25 in len, no fwd shoulder</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.220</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.600</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">4.25</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BRS-115-16 [R]</PartNumber>
        <Description>Transition, balsa, #16 to #115, reducing, 4.25 in len, no aft shoulder</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.600</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.220</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">4.25</Length>
    </Transition>


    <!-- BR-115-168 "special" is an oddball for a mysterious "#168" tube size.  Dimensions scaled from
         drawing on new site:  shoulders 1.0", exposed len 1.19", ID of rear tube 1.68", OD of rear tube 1.75" -->
    <!-- SOURCE ERROR: BR-115-168 "special" has no given dimensions. Description on 2018 site says only
         "Semroc Balsa Reducer #115 to #168   SEM-BR-115-168  Special".  All dims scaled from drawing:
         both shoulders 1.0", len 1.19", ID of rear tube 1.68", OD of rear tube 1.75"
         BR-115-168 description doesn't say what a #168 tube is.  I have
         no record or memory of a "#168" tube anywhere and a Google search turns up nothing useful. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-115-168</PartNumber>
        <Description>Transition, balsa, #115 to #168, increasing, 1.19 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.140</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.750</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.680</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">1.19</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-115-168 [R]</PartNumber>
        <Description>Transition, balsa, #168 to #115, reducing, 1.19 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.75</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.680</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.140</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.0</AftShoulderLength>
        <Length Unit="in">1.19</Length>
    </Transition>

    <!-- BR-11518 spec len on legacy site 3.375", shoulders (scaled dwg) 0.67" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-11518</PartNumber>
        <Description>Transition, balsa, #115 to #18, increasing, 3.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.140</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.800</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.375</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-11518 [R]</PartNumber>
        <Description>Transition, balsa, #18 to #115, reducing, 3.375 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.800</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.140</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.375</Length>
    </Transition>


    <!-- BR-115-225 (exists on new site only) no data given.  Scaled dwg length 1.75", both shoulders 0.92" -->
    <!-- SOURCE ERROR: BR-115-225 no exposed length given on 2018 Semroc/e-rockets site. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-115-225</PartNumber>
        <Description>Transition, balsa, #115 to #225, increasing, 1.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.220</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.140</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.92</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.92</AftShoulderLength>
        <Length Unit="in">1.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-115-225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #115, reducing, 1.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.92</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.220</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.140</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.92</AftShoulderLength>
        <Length Unit="in">1.75</Length>
    </Transition>

    <!-- ================================== -->
    <!-- BR-125-xx - LT-125 to larger sizes -->
    <!-- ================================== -->

    <!-- BR-125-175 no data given, no drawing, and a very oblique photo.  Taking length 1.50", shoulders
         0.87 same as BR-125-175L for which we at least have a drawing -->
    <!-- SOURCE ERROR: BR-125-175 has no exposed length given, no drawing, and only a very oblique photo.  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-175</PartNumber>
        <Description>Transition, balsa, #125 to #175, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-175 [R]</PartNumber>
        <Description>Transition, balsa, #175 to #125, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">1.50</Length>
    </Transition>

    <!-- BR-125-175L len 2.125", shoulders 0.87" from scaled drawing -->
    <!-- SOURCE ERROR: BR-125-175L has no exposed length given.  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-175L</PartNumber>
        <Description>Transition, balsa, #125 to #175, increasing, 2.125 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.125</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-175 [R]</PartNumber>
        <Description>Transition, balsa, #175 to #125, reducing, 2.125 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.125</Length>
    </Transition>

    <!-- BR-125-225 has length 2.8" (legacy site), shoulders 0.87" from scaled drawing -->
    <!-- SOURCE ERROR: BR-125-225 has no exposed length given on new site.  -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-225</PartNumber>
        <Description>Transition, balsa, #125 to #225, increasing, 2.82 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.80</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-125-225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #125, reducing, 2.82 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.80</Length>
    </Transition>

    <!-- ================================== -->
    <!-- BR-150-xx - LT-150 to larger sizes -->
    <!-- ================================== -->

    <!-- BR-150-200 has len 2.125" (legacy site), shoulders 0.87 (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-200</PartNumber>
        <Description>Transition, balsa, #150 to #200, increasing, 2.125 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.590</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.500</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.080</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.125</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-200 [R]</PartNumber>
        <Description>Transition, balsa, #200 to #150, reducing, 2.125 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.080</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.000</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.590</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.500</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.125</Length>
    </Transition>

    <!-- BR-150-225 has len 2.25" (legacy site), shoulders 0.87 (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-225</PartNumber>
        <Description>Transition, balsa, #150 to #225, increasing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.590</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.500</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #150, reducing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.590</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.500</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>

    <!-- BR-150-275 has len 2.9" (legacy site), shoulders 1.00 (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-275</PartNumber>
        <Description>Transition, balsa, #150 to #275, increasing, 2.9 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.590</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.500</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.9</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-150-275 [R]</PartNumber>
        <Description>Transition, balsa, #275 to #150, reducing, 2.9 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.590</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.500</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.9</Length>
    </Transition>

    <!-- ================================== -->
    <!-- BR-175-xx - LT-175 to larger sizes -->
    <!-- ================================== -->

    <!-- BR-175-225 has len 2.1" (legacy site), shoulders 0.87" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-175-225</PartNumber>
        <Description>Transition, balsa, #175 to #225, increasing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-175-225 [R]</PartNumber>
        <Description>Transition, balsa, #225 to #175, reducing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.87</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.87</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>

    <!-- BR-175-275 (new site only) length 2.0", shoulders 1.0" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-175-275</PartNumber>
        <Description>Transition, balsa, #175 to #275, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-175-275 [R]</PartNumber>
        <Description>Transition, balsa, #275 to #175, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.840</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- ================================== -->
    <!-- BR-225-xx - LT-225 to larger sizes -->
    <!-- ================================== -->

    <!-- BR-225-80H len 2.1" (legacy site), shoulders 1.0" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-225-80H</PartNumber>
        <Description>Transition, balsa, #225 to BTH-80, increasing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.340</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.250</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.558</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>BR-225-80H [R]</PartNumber>
        <Description>Transition, balsa, BT-80H to #225, reducing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.558</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.00</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.340</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.250</AftShoulderDiameter>
        <AftShoulderLength Unit="in">1.00</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>

    <!-- ============================= -->
    <!-- TA-3xx - BT-3 to larger sizes --> 
    <!-- ============================= -->
    <!-- TA-3xx do not appear on Semroc legacy site -->

    <!-- TA-35 has len 0.5" (new e-rockets site), shoulders 0.42" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-35</PartNumber>
        <Description>Transition, balsa, #3 to BT-5, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.375</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.349</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.42</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.42</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-35 [R]</PartNumber>
        <Description>Transition, balsa, #3 to BT-5, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.42</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.375</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.349</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.42</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>

    <!-- TA-35A has len 1.0" (new e-rockets site), shoulders 0.45" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-35A</PartNumber>
        <Description>Transition, balsa, #3 to BT-5, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.375</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.349</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.42</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.42</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-35A [R]</PartNumber>
        <Description>Transition, balsa, #3 to BT-5, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.42</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.375</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.349</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.42</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- ============================= -->
    <!-- TA-5xx - BT-5 to larger sizes -->
    <!-- ============================= -->

    <!-- TA-520 has length 0.75" (Estes spec and legacy site), shoulders 0.57" (scaled dwg from new site) -->
    <!-- SOURCE ERROR: TA-520 drawing on legacy site has incorrect length, scales to 0.6" (should be 0.75").
         Drawing on new e-rockets site is correct. -->

    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-520</PartNumber>
        <Description>Transition, balsa, BT-5 to BT-20, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-520 [R]</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-5, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- TA-550 has length 1.0" (legacy site), shoulders 0.5" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-550</PartNumber>
        <Description>Transition, balsa, BT-5 to BT-50, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">1.00</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-550 [R]</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-5, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.50</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- TA-580 (not in Estes catalog) has length 2.1" (legacy site) and shoulders 0.69" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-580</PartNumber>
        <Description>Transition, balsa, BT-5 to BT-80, increasing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.543</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.600</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.558</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-580 [R]</PartNumber>
        <Description>Transition, balsa, BT-80 to BT-5, reducing, 2.1 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.600</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.558</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.69</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.543</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.69</AftShoulderLength>
        <Length Unit="in">2.1</Length>
    </Transition>


    <!-- =============================== -->
    <!-- TA-20xx - BT-20 to larger sizes -->
    <!-- =============================== -->

    <!-- TA-2050 has length 2.0" (e-rockets site), shoulders 0.57" (9/16") -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-50, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050 [R]</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-20, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- TA-2050A has length 1.0" (e-rockets site), shoulders 0.57" (9/16")-->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050A</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-50, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050A [R]</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-20, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- TA-2050B has length 2.5" (e-rockets site), shoulders 0.57" (9/16") -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050B</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-50, increasing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2050B [R]</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-20, reducing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.5</Length>
    </Transition>


    <!-- TA-2055 has length 1.5" (legacy site), shoulders 0.57" (9/16") -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2055</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-55, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2055 [R]</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-20, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- TA-2058 has length 2.0" (legacy site), shoulders 0.57" (9/16") -->
    <!-- SOURCE ERROR: TA-2058 legacy site scaled drawing gives length 1.8", whereas
         specified length is 2.0" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2058</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-58, increasing, 1.8 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.540</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.498</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.8</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2058 [R]</PartNumber>
        <Description>Transition, balsa, BT-58 to BT-20, reducing, 1.8 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.540</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.498</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">1.8</Length>
    </Transition>

    <!-- TA-2060 has length 2.0" (legacy site), shoulders 0.57" (9/16") -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2060</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-60, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2060 [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-20, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- TA-2070 (new 2017 site only, not on legacy) has length 2.5" (scaled dwg), fore shoulder 0.57", 
         aft shoulder 0.67" (scaled dwg)-->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2070</PartNumber>
        <Description>Transition, balsa, BT-20 to BT-70, increasing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.217</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.175</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-2070 [R]</PartNumber>
        <Description>Transition, balsa, BT-70 to BT-20, reducing, 2.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.217</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">2.5</Length>
    </Transition>

    <!-- =============================== -->
    <!-- TA-50xx - BT-50 to larger sizes -->
    <!-- =============================== -->

    <!-- TA-5055 has length 1.0" (legacy site), shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-55, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055 [R]</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-50, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>


    <!-- TA-5055D same as TA-5055 but through drilled 0.75" dia.  Mass override from Semroc legacy site -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055D</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-55, increasing, 1.0 in len, drilled 0.75 in</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <Mass Unit="oz">0.23</Mass>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055D [R]</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-50, reducing, 1.0 in len, drilled 0.75 in</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <Mass Unit="oz">0.23</Mass>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- TA-5055L has length 1.5", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055L</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-55, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055 [R]</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-50, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- TA-5055LD same as TA-5055L but through drilled 0.75" dia. Mass override from legacy site. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055LD</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-55, increasing, 1.5 in len, drilled 0.75 in</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <Mass Unit="oz">0.31</Mass>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5055LD [R]</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-50, reducing, 1.5 in len, drilled 0.75 in</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <Mass Unit="oz">0.31</Mass>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- TA-5060C has actual exposed length 0.75", shoulders 0.57 (scaled dwg)
         This has a conical taper of o.5" plus a cylindrical section 0.25" long at the rear -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060C</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-60, increasing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060C [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-50, reducing, 0.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.57</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>


    <!-- TA-5060 has length 2.0", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-60, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060 [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-50, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- TA-5060E is an ellipsoid shape, length 2.0, shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060E</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-60, ellipsoid, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>ELLIPSOID</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060 [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-50, ellipsoid, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>ELLIPSOID</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- TA-5060M is conical, length 1.5", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060M</PartNumber>
        <Description>Transition, balsa, BT-50 to BT-60, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5060M [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-50, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>


    <!-- TA-5065 has length 2.0", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5065</PartNumber>
        <Description>Transition, balsa, BT-50 to PST-65, increasing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.774</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5065 [R]</PartNumber>
        <Description>Transition, balsa, PST-65 to BT-50, reducing, 2.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.774</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.0</Length>
    </Transition>

    <!-- =============================== -->
    <!-- TA-52xx - BT-52 to larger sizes -->
    <!-- =============================== -->

    <!-- TA-5260A has length 1.0", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5260A</PartNumber>
        <Description>Transition, balsa, BT-52 to BT-60, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.014</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.988</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5260A [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-52, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.014</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.998</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- TA-5260C has total exposed length 3.75", shoulders 0.67" (scaled dwg)
         This is a slight bi-conic shape with diameter reaching 1.32" at 0.75" back from the front -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5260C</PartNumber>
        <Description>Transition, balsa, BT-52 to BT-60, bi-conic, increasing, 3.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.014</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.988</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.75</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5260C [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-52, bi-conic, reducing, 3.75 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.014</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.998</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">3.75</Length>
    </Transition>

    <!-- =============================== -->
    <!-- TA-55xx - BT-55 to larger sizes -->
    <!-- =============================== -->

    <!-- TA-5560 has length 1.0", shoulders 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-60, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560 [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-55, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    <!-- TA-5560A had length of 1.25".  On legacy site "view" leads to "Invalid or Inactive Product".
         Doesn't exist on modern e-rockets site.  Shoulders presumed to be 0.67" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560A</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-60, increasing, 1.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.25</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560A [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-55, reducing, 1.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.25</Length>
    </Transition>

    <!-- TA-5560L is 2.4" long, shoulders 0.72" long (scaled dwg).  Only exists on e-rockets site. -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560L</PartNumber>
        <Description>Transition, balsa, BT-55 to BT-60, increasing, 2.4 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.4</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-5560L [R]</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-55, reducing, 2.4 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.4</Length>
    </Transition>

    <!-- TA-55-T35 is Estes-to-metric cross coupler.  Len 0.5", both shoulders 0.62" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-55-T35</PartNumber>
        <Description>Transition, balsa, BT-55 to T-35, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.62</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">35.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">34.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.62</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-55-T35 [R]</PartNumber>
        <Description>Transition, balsa, T-35 to BT-55, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">35.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">34.0</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.62</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.62</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>

    <!-- ====================================== -->
    <!-- TA-T40xx - Metric T-40 to larger sizes -->
    <!-- ====================================== -->
    <!-- TA-T40-65 is Estes-to-metric cross coupler.  Len 0.5", aft shoulder 0.62",
         fore shoulder 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-T40-65</PartNumber>
        <Description>Transition, balsa, T-40 to PST-65, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">40.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">39.0</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.774</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.62</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-T40-65 [R]</PartNumber>
        <Description>Transition, balsa, PST-65 to T-40, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.774</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.62</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">40.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">39.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>



    <!-- =============================== -->
    <!-- TA-60xx - BT-60 to larger sizes -->
    <!-- =============================== -->

    <!-- TA-6065 is 0.5" long, fore shoulder 0.72", aft shoulder 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6065</PartNumber>
        <Description>Transition, balsa, BT-60 to PST-65, increasing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.72</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.774</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6065 [R]</PartNumber>
        <Description>Transition, balsa, PST-65 to BT-60, reducing, 0.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.774</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.72</AftShoulderLength>
        <Length Unit="in">0.5</Length>
    </Transition>

    <!-- TA-6070 has length 1.5", fore shoulder 0.72", aft shoulder 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6070</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-70, increasing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.72</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.217</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.175</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6070 [R]</PartNumber>
        <Description>Transition, balsa, BT-70 to BT-60, reducing, 1.5 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.217</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.72</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- TA-6080 has length 2.25, fore shoulder 0.72", aft shoulder 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6080</PartNumber>
        <Description>Transition, balsa, BT-60 to BT-80, increasing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.72</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.600</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.558</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-6080 [R]</PartNumber>
        <Description>Transition, balsa, BT-80 to BT-60, reducing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.600</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.558</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.72</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>

    <!-- =============================== -->
    <!-- TA-70xx - BT-70 to larger sizes -->
    <!-- =============================== -->

    <!-- SOURCE ERROR: TA-7080 has length 1.5" in table on legacy site, but View page gives length 2.25".
         Scaled drawing gives 2.25", so the table is wrong. -->

    <!-- TA-7080 has length 2.25", fore shoulder 0.72", aft shoulder 0.67" (scaled dwg) -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-7080</PartNumber>
        <Description>Transition, balsa, BT-70 to BT-80, increasing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.217</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.72</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.600</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.558</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-7080 [R]</PartNumber>
        <Description>Transition, balsa, BT-80 to BT-70, reducing, 2.25 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.600</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.558</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.217</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.175</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.72</AftShoulderLength>
        <Length Unit="in">2.25</Length>
    </Transition>

    <!-- ================================= -->
    <!-- TA-70Hxx - BT-70H to larger sizes -->
    <!-- ================================= -->

    <!-- SOURCE ERROR: TA-7080H has length 1.5" in table on legacy site, but View page gives length 1.0".
         Scaled drawing also gives 1.0", so legacy site table is wrong.  -->
    <!-- TA-7080H has length 1.0", fore shoulder 0.85", aft shoulder 0.67" -->
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-7080H</PartNumber>
        <Description>Transition, balsa, BTH-70 to BTH-80, increasing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.247</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.85</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.640</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.558</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.67</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>
    <Transition>
        <Manufacturer>SEMROC</Manufacturer>
        <PartNumber>TA-7080H [R]</PartNumber>
        <Description>Transition, balsa, BTH-80 to BTH-70, reducing, 1.0 in len</Description>
        <Material Type="BULK">Balsa, bulk, 7 kg/m3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.558</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.67</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.247</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.175</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.85</AftShoulderLength>
        <Length Unit="in">1.0</Length>
    </Transition>

    
    <!-- ================= -->
    <!-- Engine Blocks     -->
    <!-- ================= -->

    
    <!-- SEMROC EB-5 is also CR-3-5.  OD corrected as e-rocket site value is too big -->
    <!-- SOURCE ERROR: EB-5 OD on 2017 eRocket site of .516 is wrong, must be less than 0.515 -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EB-5</PartNumber>
      <Description>Engine block, fiber, BT-5, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>TR-5</PartNumber>
      <Description>Engine block, fiber, BT-5, 0.5" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.376</InsideDiameter>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.5</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EB-20A</PartNumber>
      <Description>Engine block, fiber, BT-20, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.65</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.25</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EB-20B</PartNumber>
      <Description>Engine block, fiber, BT-20, 0.125" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.65</InsideDiameter>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.125</Length>
    </EngineBlock>

    <!-- On legacy and eRockets site, SEMROC EB-30 is balsa and 0.75" long
         *** there is no information about center hole size; I took it as 0.50" *** -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EB-30</PartNumber>
      <Description>Engine block, balsa, BT-30, 0.75" len</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <InsideDiameter Unit="in">0.50</InsideDiameter>
      <OutsideDiameter Unit="in">0.724</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>EB-50</PartNumber>
      <Description>Engine block, fiber, BT-50, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.738</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </EngineBlock>

    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>TR-9</PartNumber>
      <Description>Thrust ring, fiber, #9, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">0.713</InsideDiameter>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </EngineBlock>
    
    <!-- TR-115 0.25" is same as CR-9-115 but Semroc 2017 website incorrectly says the
         1.0" long version is the same.  -->
    <!--- SOURCE ERROR: TR-115 is same as CR-9-115 but Semroc 2017 website
          incorrectly says the 1.0" long version is the same. -->
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>TR-115</PartNumber>
      <Description>Thrust ring, fiber, #11, 1.0" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </EngineBlock>
    <EngineBlock>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>TR-115T</PartNumber>
      <Description>Thrust ring, fiber, #11, 0.25" len</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <InsideDiameter Unit="in">1.000</InsideDiameter>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">0.250</Length>
    </EngineBlock>

    <!-- Bulkheads -->

    <!-- The fits and part numbering need explanation:
         PB:  direct fit inside Estes tube sizes with two exceptions:
              PB-75 fits no known standard tube (check Quest?)
              PB-T20 fits a Quest T20 20mm ID tube
         RA:  fits inside the tube *coupler*, for Estes tube sizes
         CR:  direct fit inside Centuri tube sizes
         FB:  fiber nose block, .060
         NB:  balsa nose block (and one erroneous fiber block listing), Estes sizes
         BNB: balsa nose block, Centuri tube sizes
         BTC: Direct fit into Centuri tube sizes, e.g. BTC-6
              Exception: HTC-7B is an external coupler as used in the Black Widow (Centuri HTC-70)
              BTC is also used for balsa tail cones, adding to the confusion
    -->

    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PB-5P</PartNumber>
      <Description>Bulkhead, plywood, BT-5/#5, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.10</Length>
    </BulkHead>
    <!-- PB-75 is a weird part, does not exactly fit any standard tube -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PB-75</PartNumber>
      <Description>Bulkhead, plywood, 0.75" diam, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.75</OutsideDiameter>
      <Length Unit="in">0.10</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>PB-T20</PartNumber>
      <Description>Bulkhead, plywood, inside 20mm ID, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
      <Length Unit="in">0.10</Length>
    </BulkHead>
    <!-- FB-20 has an erroneous duplicate listing as NB-20P -->
    <!-- SOURCE ERROR: FB-20 has an erroneous duplicate listing as NB-20P -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>FB-20</PartNumber>
      <Description>Bulkhead, fiber, inside BT-20, .06"</Description>
      <Material Type="BULK">Fiber, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.724</OutsideDiameter>
      <Length Unit="in">0.06</Length>
    </BulkHead>
    <!-- RA-50P goes inside a JTC-50 coupler -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-50P</PartNumber>
      <Description>Bulkhead, plywood, inside JTC-50, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.910</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-10P</PartNumber>
      <Description>Bulkhead, plywood, inside #10, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <!-- RA-55P goes inside a JTC-55 coupler -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-55P</PartNumber>
      <Description>Bulkhead, plywood, inside JTC-55, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.235</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-13P</PartNumber>
      <Description>Bulkhead, plywood, inside #13, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <!-- RA-60P goes inside a JTC-60 coupler -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-60P</PartNumber>
      <Description>Bulkhead, plywood, inside JTC-60, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.537</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-16P</PartNumber>
      <Description>Bulkhead, plywood, inside #16, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-18P</PartNumber>
      <Description>Bulkhead, plywood, inside #18, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.798</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CR-20P</PartNumber>
      <Description>Bulkhead, plywood, inside #20, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <!-- RA-70P goes inside a JTC-70 coupler -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-70P</PartNumber>
      <Description>Bulkhead, plywood, inside JTC-70, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.117</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>
    <!-- RA-80P goes inside a JTC-80 coupler -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>RA-80P</PartNumber>
      <Description>Bulkhead, plywood, inside JTC-80, 3/32"</Description>
      <Material Type="BULK">Plywood, light, bulk</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.500</OutsideDiameter>
      <Length Unit="in">0.11</Length>
    </BulkHead>

    <!-- Bulkheads: balsa cylinders

         Dimensions from legacy Semroc site: www.semroc.com/Store/Products/BalsaConnectors.asp

         6 pound per ft3 balsa gives a reasonable fit to quoted masses from legacy Semroc
         site.  Balsa density from vendors is highly variable so it's not worthwhile to try for a
         perfect match to the quoted masses.
    -->

    <!-- Balsa nose blocks for Estes tube sizes -->
    
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-3</PartNumber>
      <Description>Nose block, balsa, BT-3</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.347</OutsideDiameter>
      <Length Unit="in">0.50</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-5</PartNumber>
      <Description>Nose block, balsa, BT-5</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-20</PartNumber>
      <Description>Nose block, balsa, BT-20</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.708</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-30</PartNumber>
      <Description>Nose block, balsa, BT-30</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.723</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-40</PartNumber>
      <Description>Nose block, balsa, BT-40</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.763</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-50</PartNumber>
      <Description>Nose block, balsa, BT-50</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-50L</PartNumber>
      <Description>Nose block, balsa, BT-50, long</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.19</Length>
    </BulkHead>
    <!-- NB-52 Length is given as 1.18" on new eRockets/Semroc site -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-52</PartNumber>
      <Description>Nose block, balsa, BT-52</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.986</OutsideDiameter>
      <Length Unit="in">1.18</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-55</PartNumber>
      <Description>Nose block, balsa, BT-55</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.281</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-60</PartNumber>
      <Description>Nose block, balsa, BT-60</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.593</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-65</PartNumber>
      <Description>Nose block, balsa, PST-65</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-70</PartNumber>
      <Description>Nose block, balsa, BT-70</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.173</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>NB-80</PartNumber>
      <Description>Nose block, balsa, BT-80</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.556</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BulkHead>

    <!-- Nose blocks for Centuri tube sizes -->

    <!-- BNB-7 is on new eRockets/Semroc site but not on legacy Semroc site -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNB-7</PartNumber>
      <Description>Nose block, balsa, #7</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-5</PartNumber>
      <Description>Nose block, balsa, #5</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.513</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    <!-- SOURCE ERROR: BTC-6 Semroc legacy site gives 0.615 for OD of BTC-6.  It can't be that
         big; they list ID of ST-6 tube as 0.610  -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-6</PartNumber>
      <Description>Nose block, balsa, #6</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.608</OutsideDiameter>
      <Length Unit="in">1.00</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-7</PartNumber>
      <Description>Nose block, balsa, #7</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">1.00</Length>
    </BulkHead>
    <!-- BTC-7S only appears in the new eRockets/Semroc 2017 site -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-7S</PartNumber>
      <Description>Nose block, balsa, #7, short</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.713</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-8</PartNumber>
      <Description>Nose block, balsa, #7</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">1.00</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-8F</PartNumber>
      <Description>Nose block, balsa, #8F</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.883</OutsideDiameter>
      <Length Unit="in">1.00</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-9</PartNumber>
      <Description>Nose block, balsa, #9</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.948</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-10</PartNumber>
      <Description>Nose block, balsa, #10</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-11</PartNumber>
      <Description>Nose block, balsa, #11</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.128</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-13</PartNumber>
      <Description>Nose block, balsa, #13</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BulkHead>
    <!-- BTC-13S ribbed only appears in the new eRockets/Semroc 2017 site.  It looks to be
    a part for a specific kit and is about 2 body diameters long in the illustration.
    *** Get specific dimensions and kit compatibility *** -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-13S</PartNumber>
      <Description>Nose block, balsa, #13, ribbed</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.298</OutsideDiameter>
      <Length Unit="in">2.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-16</PartNumber>
      <Description>Nose block, balsa, #16</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.598</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-20</PartNumber>
      <Description>Nose block, balsa, #20</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.998</OutsideDiameter>
      <Length Unit="in">2.1</Length>
    </BulkHead>

    <!-- Nose blocks for Centuri large tube series (LT-xxx) -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-085</PartNumber>
      <Description>Nose block, balsa, #085</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">0.863</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </BulkHead>
    <!-- SOURCE ERROR: BTC-115 Semroc legacy site gives OD of BTC-115 as 1.15".  ID of Series 115
    tube is given as 1.14" by multiple sources -->
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-115</PartNumber>
      <Description>Nose block, balsa, #115</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.138</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-125</PartNumber>
      <Description>Nose block, balsa, #125</Description>
      <Material Type="BULK">Balsa, bulk, 6lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.248</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-150</PartNumber>
      <Description>Nose block, balsa, #150</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.498</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-175</PartNumber>
      <Description>Nose block, balsa, #175</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">1.748</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-225</PartNumber>
      <Description>Nose block, balsa, #225</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.248</OutsideDiameter>
      <Length Unit="in">2.5</Length>
    </BulkHead>
    <BulkHead>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BTC-275</PartNumber>
      <Description>Nose block, balsa, #275</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <OutsideDiameter Unit="in">2.748</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </BulkHead>

    <!-- Nose Cones -->
    <!-- ========== -->
    <!-- 
         General notes on Semroc nose cones:

         All Semroc nose cones are balsa.  Carl McLawhorn built his own nose cone making
         machines and produced them in-house.

         BNC-xxx designations and Semroc-specific nose cones for Estes tube sizes:

            There are some nose cones produced by SEMROC with Estes style designations that were not identified as
            such in any existing Estes literature.  These fall into a few different situations:

            1) Specialty parts that Estes actually made for use in certain kits.  In the era after Estes stopped
            assigning "BNC-xxx" codes, they would have a numeric PN only, and might never appear in a catalog.
            Examples:
                BNC-5RA PN 70217 for #0893 Red Alert (PN given in instructions, no known Estes use of "BNC-5RA")

            2) Semroc-specific parts that Semroc made for their own unique kits.  It appears that if they were made to
            mate with an Estes tube size, Semroc would assign a made-up Estes style BNC-xxx designation. Examples:
                BNC-20MG (1.9" odd shape for Semroc Moon Glo)

            3) Semroc unique parts that are upscales/downscales of other well known Estes nose cones.  Examples:
                BNC-20LS (2.0" elliptical, downscale of BNC-60L)

            4) Semroc parts that are balsa versions of Estes plastic nose cones.  Examples:
                BNC-20ED (4.2" "capsule", version of PNC-20ED from Saros, Nomad)
            
                
         Semroc published data:
         
         On the legacy Semroc site, shoulder diameter, maximum diameter, exposed length,
         and weight in ounces are given for all listed Estes, Centuri and Quest (metric) compatible
         nose cones.  On the new eRockets/Semroc 2017 site, only the exposed length is
         given.

         In a huge win, the Semroc legacy site has manufacturing drawing outlines for nearly
         every balsa part.  I've confirmed with eRockets owner Randy Boadway that these
         actually are the drawings used by the balsa turning equipment, and thus are
         completely accurate.  He also confirmed that Semroc did not observe the original
         manufacturer's shoulder lengths.

         Nose cone mass and balsa density:

         The nose cone weights shown on the legacy Semroc site generally correspond to
         balsa density of a little under 7 lb/ft3, so I have used that.

         Shoulder lengths:

         Semroc does not list shoulder lengths of nose cones on either the legacy site or
         the new eRockets/Semroc 2017 site.  For this file we have obtained all shoulder
         lengths from the manufacturing drawing outlines published on the Semroc legacy and
         2018 websites.

         Shoulder diameter:
         
         Here Centuri published nose cone shoulder diameter, while Estes did not.
         Fortunately, Semroc lists this for all of their nose cones on the legacy
         site. For nose cones added in the "new" Semroc site, if there is no data we use a
         small offset down from the ID of the body tube, which will be extremely close to
         truth.
         
         Nose cone shapes:

         For nose cone shaapes not understood by OpenRocket, an approximation is used and
         noted in the XML comments.  If the mass ends up far off, a CG override is used to
         match the specified mass/weight.
    -->

    <!-- BC-2 Balsa Nose Cones -->
    
    <!-- BC-2ET is a 2-piece nose cone / nozzle set that you cut apart in the middle.
         Despite the BC-xx nomenclature, it's really for BT-2, not ST-2 tube.
         It makes an Apollo 1/70 escape tower.  Shoulder diam of both pieces is 0.25".
         On Semroc legacy site drawing, the main diameter is 0.28", combined overall length 1.37"

         We have to split this up into two pieces with partially synthetic part numbers.
         The nose cone part is a cone+cylinder hybrid with exposed length 0.62", where the
         aft cylindrical part is 0.25" long and the conical section is 0.37".  Shoulder len is 0.25".

         The nozzle section is a conical flare 0.24" exposed length to a max diam of 0.56",
         with a shoulder diam 0.25" and shoulder length 0.25".
         There is also a nozzle protrusion 0.31" dia x 0.04" long, which is disregarded here.
         For OpenRocket purposes we model this as a rear facing transition with no aft shoulder.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2ET_nc, BC-2ET</PartNumber>
      <Description>Nose cone, balsa, BT-2, 0.67", Apollo escape tower 1/72, PN BC-2ET</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.281</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.25</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">0.62</Length>
    </NoseCone>

    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-2ET_nozzle, BC-2ET</PartNumber>
        <Description>Nozzle tail cone, balsa, BT-2, 0.24", Apollo escape tower 1/72, PN BC-2ET</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.281</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.25</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.25</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.56</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.56</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">0.24</Length>
    </Transition>

    <!-- BNC-2 Balsa Nose Cones -->
    <!-- BNC-2PY is 1.25" ogive (described as 'elliptical') for Estes BT-2, shoulder 0.375" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-2PY</PartNumber>
      <Description>Nose cone, balsa, BT-2, 1.25", ogive, PN BNC-2PY</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.281</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.253</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">1.25</Length>
    </NoseCone>


    <!-- BNC-5xx Balsa Nose Cones -->

    <!-- BNC-5AL from Semroc legacy site, weight 0.02 oz.  Estes kit/catalog usage and numeric PN unknown.  Not
         mentioned in the Brohm Nose Cone Reference.  Semroc site does not mention any kit compatibility.
         This looks like a Semroc-only cone until proven otherwise.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5AL</PartNumber>
      <Description>Nose cone, balsa, BT-5, 0.4", rounded cone, PN BNC-5AL</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">0.4</Length>
    </NoseCone>
    <!-- BNC-5AW (Star Dart) ref 1974 custom parts catalog.  Semroc weight 0.02 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5AW</PartNumber>
      <Description>Nose cone, balsa, BT-5, 2.25", elliptical, PN BNC-5AW</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">2.25</Length>
    </NoseCone>
    <!-- BNC-5AX, PN 070208 (Screamer, Javelin) Estes ref 1974 custom parts catalog.
         Semroc weight .02 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5AX</PartNumber>
      <Description>Nose cone, balsa, BNC-5AX, 2.25", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">2.25</Length>
    </NoseCone>
    <!-- BNC-5BA (Mini-BOMARC and #1220 Mars Snooper) Estes ref 1974 custom parts catalog.
         Semroc weight 0.01 oz, Estes .013 oz
         Shape is shown as a "ram jet" style nacelle.  Approximated as an ogive. -->
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5BA</PartNumber>
      <Description>Nose cone, balsa, BNC-5BA, 0.625", ramjet nacelle</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.18</ShoulderLength>
      <Length Unit="in">0.625</Length>
    </NoseCone>
    <!-- BNC-5E, Estes ref 1974 parts catalog, Semroc weight 0.020 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5E</PartNumber>
      <Description>Nose cone, balsa, BNC-5E, 1.375", fat ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">1.375</Length>
    </NoseCone>
    <!-- BNC-5NAS, Nike Ajax, Estes use unknown. K-79/#1279 Nike-Ajax used BNC-20CB
         This is probably a Semroc-only part.  ***Actual shoulder length unconfirmed.***
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5NAS</PartNumber>
      <Description>Nose cone, balsa, BNC-5NAS, 2.5", long cone/ogive</Description>
      <Material Type="BULK">Balsa, bulk, Estes typical</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>
    <!-- BNC-5RA from Estes #0893 Red Alert (1991-1992). Max diamter of flared part is
         0.81" and weight is 0.10 oz per Semroc legacy NC table ***shoulder length
         unconfirmed*** -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5RA</PartNumber>
      <Description>Nose cone, balsa, BNC-5RA, 2.0", flared ogive, Red Alert</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.10</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>
    <!-- BNC-5S, ref 1974 custom parts catalog, Semroc weight 0.02 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5S</PartNumber>
      <Description>Nose cone, balsa, BNC-5S, 1.5", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>
    <!-- BNC-5V ref 1974 parts catalog,  Semroc weight 0.01 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5V</PartNumber>
      <Description>Nose cone, balsa, BNC-5V, 0.75", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">0.75</Length>
    </NoseCone>
    <!-- BNC-5TT, no Estes usage known.  Semroc weight 0.39 oz.  This is a very large
    flare, though the legacy Semroc table doesn't show the correct OD. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5TT</PartNumber>
      <Description>Nose cone, balsa, BNC-5TT, 2.0", large flared ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.39</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>
    <!-- BNC-5W ref 1974 parts catalog, Semroc weight 0.04 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-5W</PartNumber>
      <Description>Nose cone, balsa, BNC-5W, 2.875", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.541</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">2.875</Length>
    </NoseCone>


    <!-- ====================================== -->
    <!-- BC-5xx Balsa Nose Cones for ST-5 tubes -->
    <!-- ====================================== -->

    <!-- BC-505 is spherical, 0.5" long, shoulder 0.41", Centuri PNC-55 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-505</PartNumber>
      <Description>Nose cone, balsa, ST-5, 0.5", spherical, Centuri PNC-55 shape, PN BC-505</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">0.5</Length>
    </NoseCone>

    <!-- BC-508 is ellipsoid, 0.75" long (called 0.8"), shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-508</PartNumber>
      <Description>Nose cone, balsa, ST-5, 0.75", ellipsoid, PN BC-508</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">0.75</Length>
    </NoseCone>

    <!-- BC-510 is pure conical, 1.0" long, shoulder 0.41", Centuri BC-51 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-510</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.0", conical, Centuri BC-51 shape, PN BC-510</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.0</Length>
    </NoseCone>

    <!-- BC-510P is ellipsoid, 1.0" long, shoulder 0.41", Centuri PNC-51 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-510P</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.0", ellipsoid, Centuri PNC-51 shape, PN BC-510P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.0</Length>
    </NoseCone>

    <!-- BC-510S is ogive, 1.0" long, shoulder 0.41", downscale of old Semroc NB-204 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-510S</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.0", ogive, old Semroc NB-204 shape, PN BC-510S</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.0</Length>
    </NoseCone>

    <!-- BC-512 is ogive, 1.2" long, shoulder 0.41", old Semroc NB-103 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-512</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.2", ogive, old Semroc NB-103 shape, PN BC-512</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.2</Length>
    </NoseCone>

    <!-- BC-514 is ogive, 1.4" long, shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-514</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.4", ogive, PN BC-514</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.4</Length>
    </NoseCone>

    <!-- BC-515 is conical, 1.5" long, shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-515</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.5", conical, PN BC-515</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-516 is conical, 1.6" long, shoulder 0.41", Centuri BC-52 shape
         Has a 0.35" long cylindrical section at rear, conical part is actually 1.25" long. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-516</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.6", conical, Centuri BC-52 shape, PN BC-516</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.6</Length>
    </NoseCone>

    <!-- BC-517 is ogive, 1.7" long, shoulder 0.41", Centuri BC-50 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-517</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.7", ogive, Centuri BC-50 shape, PN BC-517</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.7</Length>
    </NoseCone>

    <!-- BC-518 is ogive, 1.8" long, shoulder 0.41", old Semroc NB-104 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-518</PartNumber>
      <Description>Nose cone, balsa, ST-5, 1.8", ogive, old Semroc NB-104 shape, PN BC-518</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-520S is an oversize bulbous shape (max diam 0.834"), 2.0" long, shoulder 0.41"
         mass override from legacy site -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-520S</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.0", bulbous, PN BC-520S</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <Mass Unit="oz">0.07</Mass>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-522 is ellipsoid, 2.2" long, shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-522</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.2", ellipsoid, PN BC-522</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BC-522P is ogive, 2.25" long (stated 2.2), shoulder 0.41", Centuri PNC-54 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-522P</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.25", ogive, Centuri PNC-54 shape, PN BC-522P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.25</Length>
    </NoseCone>

    <!-- BC-523 is a cone-cylinder-cone payload fairing shape, Centuri BC-56 equivalent.
         Overall length 2.3", shoulder 0.41".
         forward conical section is 1.0" long with a rounded tip of radius approx 0.125"
         center cylinder is 1.05" long, diameter 0.83"
         aft conical taper is 0.25" long
         Mass override from legacy site data table -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-523</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.3", payload fairing, Centuri BC-56 shape, PN BC-523</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <Mass Unit="oz">0.06</Mass>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-524 is ogive, 2.4" long, shoulder 0.41", Centuri BC-54 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-524</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.4", ogive, Centuri BC-54 shape, PN BC-524</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.4</Length>
    </NoseCone>

    <!-- BC-526 is conical, 2.6" long, shoulder 0.41", old Semroc NB-106 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-526</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.6", conical, PN BC-526</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-527 is 5:1 ogive, 2.7" long, shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-527</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.7", 5:1 ogive, PN BC-527</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BC-528 is conical, 2.85" long (stated 2.8"), shoulder 0.41" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-528</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.8", conical, PN BC-528</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BC-529 is ogive, 2.9" long, shoulder 0.41", old Semroc NB-107 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-529</PartNumber>
      <Description>Nose cone, balsa, ST-5, 2.9", ogive, old Semroc NB-107 shape, PN BC-529</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.543</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
      <ShoulderLength Unit="in">0.41</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>


    <!-- ====================================
         BC-6 Balsa Nose Cones for ST-6 tubes
         ====================================
    -->
    <!-- BC-613 is 1.25" fat ogive (called 1.3"), shoulder 0.42", downscale of Estes BNC-30D -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-613</PartNumber>
      <Description>Nose cone, balsa, BC-613, 1.25", fat ogive, Estes BNC-30D downscale, PN BC-613</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">1.25</Length>
    </NoseCone>

    <!-- BC-615 is 1.5" ellipsoid, shoulder 0.42", downscale of Estes BNC-20B -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-615</PartNumber>
      <Description>Nose cone, balsa, BC-615, 1.5", elliptical, Estes BNC-20B downscale, PN BC-615</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-620 is 2.0" 3:1 ogive, shoulder 0.42" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-620</PartNumber>
      <Description>Nose cone, balsa, BC-620, 2.0", 3:1 ogive, PN BC-620</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-624 is 2.4" ogive, shoulder 0.42", downscale of Estes BNC-20N -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-624</PartNumber>
      <Description>Nose cone, balsa, BC-624, 2.4", ogive, Estes BNC-20N downscale, PN BC-624</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">2.4</Length>
    </NoseCone>

    <!-- BC-626 is 4:1 ogive, shoulder 0.42" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-626</PartNumber>
      <Description>Nose cone, balsa, BC-626, 2.6", 4:1 ogive, PN BC-626</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-628 is 2.9" conical (called 2.8"), shoulder 0.42", 1/10 scale Nike-Asp -->
    <!-- SOURCE ERROR: BC-628 is called 2.8" long, but drawing shows it to be 2.9" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-628</PartNumber>
      <Description>Nose cone, balsa, BC-628, 2.9", conical, 1/10 Nike-Asp, PN BC-628</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-631 is 3.1" ogive, shoulder 0.42", 1/10 scale Asp -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-631</PartNumber>
      <Description>Nose cone, balsa, BC-631, 3.0", ogive, 1/10 Asp, PN BC-631</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-633 is 3.3" 5:1 ogive, shoulder 0.42" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-633</PartNumber>
      <Description>Nose cone, balsa, BC-633, 3.3", 5:1 ogive, PN BC-633</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">3.3</Length>
    </NoseCone>

    <!-- BC-634 is 3.5" conical (called 3.4"), shoulder 0.42", 1/10 Nike-Apache -->
    <!-- SOURCE ERROR: BC-634 is called 3.4" long, but drawing shows it to be 3.5" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-634</PartNumber>
      <Description>Nose cone, balsa, BC-634, 3.5", conical, 1/10 Nike-Apache, PN BC-634</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-646 is 4.6" flared conical for 1/10 Nike-Cajun, shoulder 0.42". 
         Max diam 0.780", length of forward conic section 3.6", aft section 1.0"
         mass override from legacy site data table -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-646</PartNumber>
      <Description>Nose cone, balsa, BC-646, 4.6", flared conical, 1/10 Nike-Cajun, PN BC-646</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Mass Unit="oz">0.07</Mass>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.650</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.608</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>


    <!-- ===================
         BNC-10xx Nose Cones
         ===================
         Unlike Estes, Semroc shows the OD of BNC-10 nose cones to be 0.720, which matches the OD of a BT-10.  -->

    <!-- BNC-10A, length 0.812", shoulder 0.25".  Semroc weight 0.02 oz.  Used in K-4/#1204 Streak -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-10A</PartNumber>
      <Description>Nose cone, balsa, BT-10, 0.8", elliptical, PN BNC-10A</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.720</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">0.812</Length>
    </NoseCone>

    <!-- BNC-10B not known to have ever been used in any Estes kit, Semroc weight 0.04 oz 
         Length 1.687", shoulder 0.35" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-10B</PartNumber>
      <Description>Nose cone, balsa, BNC-10B, 1.69", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.720</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.35</ShoulderLength>
      <Length Unit="in">1.687</Length>
    </NoseCone>

    <!-- BNC-19 nose cones -->

    <!-- BNC-19MC is an odd one - a 2.6" Mercury capsule for BT-19, weight 0.02 oz, shoulder 0.43"
         Estes had no equivalent, and this is the only thing Semroc ever made for a BT-19.
         Was this an OEM part for a Dr. Zooch peanut scale Mercury-Redstone perhaps? -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-19MC</PartNumber>
      <Description>Nose cone, balsa, BT-19, 2.6", Mercury capsule, PN BNC-19MC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.02</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.700</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.672</ShoulderDiameter>
      <ShoulderLength Unit="in">0.43</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>


    <!-- BNC-20xx nose cones -->
    <!--
        Semroc BNC-20xx nose cones never made by Estes:

        BNC-20AH (3.0" slightly rounded ogive, downscale of BHC-60AH)
        BNC-20BA (0.9" ramjet nacelle)
        BNC-20ED (4.2" long capsule shape)
        BNC-20FB (2.29" elliptical)
        BNC-20G3 (3:1 ogive)
        BNC-20G4 (4:1 ogive)
        BNC-20G5 (5:1 ogive)
        BNC-20H  (0.82" elliptical)
        BNC-20LS (2.0" elliptical, downscale of BNC-60L)
        BNC-20MG (1.9" capsule) Semroc Moon Glo shape
        BNC-20SP (very short .25" rounded pod cap)
        BNC-20SU (2.3" straight cone)
        BNC-20WC (3.0" straight cone)
        BNC-20X  (2.5" elliptical)

        Semroc BNC-20xx with designators that do not match Estes:
        
        BNC-20MH (mercury capsule base, this is Estes PSM-1 71032)
        BNC-20MC (mercury capsule nose, this is Estes PSM-1 71030)
    -->
    <!-- BNC-20A is 0.82" ellipsoid (K-7 Phantom, K-13 Falcon) ref 1975 Estes catalog, shoulder 0.27" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20A</PartNumber>
      <Description>Nose cone, balsa, BT-20, 0.8", rounded ogive, BNC-20A</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.27</ShoulderLength>
      <Length Unit="in">0.82</Length>
    </NoseCone>

    <!-- BNC-20AH (non Estes, downscale of BNC-60AH) ref 2017 eRockets/Semroc website, shoulder .42" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20AH</PartNumber>
      <Description>Nose cone, balsa, BT-20, 3.0", elliptical, BNC-20AH</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BNC-20AM (K-53 Stinger etc) ref 1988 Estes catalog, shoulder 0.54" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20AM</PartNumber>
      <Description>Nose cone, balsa, BT-20, 2.0", rounded cone, BNC-20AM</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.54</ShoulderLength>
      <Length Unit="in">2.00</Length>
    </NoseCone>

    <!-- BNC-20AZ (#2033 Trident II etc.), ref 1974 Estes custom parts catalog.  shoulder 0.54" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20AZ</PartNumber>
      <Description>Nose cone, balsa, BT-20, 2.5", ogive, BNC-20AZ</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>PARABOLIC</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.54</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- *** CORRECTING SHOULDER LENGTHS ETC. HERE *** -->

    <!-- BNC-20B (K-5 Apogee II etc.) ref 1988 Estes catalog -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20B</PartNumber>
      <Description>Nose cone, balsa, BNC-20B, 1.7", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.312</ShoulderLength>
      <Length Unit="in">1.687</Length>
    </NoseCone>

    <!-- BNC-20BA is 0.9" (non Estes) ramjet nacelle.  Shoulder len 0.3" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20BA</PartNumber>
      <Description>Nose cone, balsa, BT-20, 0.9", ramjet nacelle, PN BNC-20BA</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.30</ShoulderLength>
      <Length Unit="in">0.9</Length>
    </NoseCone>

    <!-- BNC-20CB 70231 (#1279 Nike-Ajax), dimensions from Semroc legacy site, weight 0.04 oz
         Not in the 1974 custom parts catalog; not on 2017 eRocket/Semroc website
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20CB</PartNumber>
      <Description>Nose cone, balsa, BNC-20CB, 1.75", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.75</Length>
    </NoseCone>
    <!-- BNC-20ED (non Estes) ref legacy Semroc website, shape approximate -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20ED</PartNumber>
      <Description>Nose cone, balsa, BNC-20ED, 4.2", long capsule shape, PNC-20ED</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.07</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>
    <!-- BNC-20FB (non Estes) ref 2017 eRockets/Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20FB</PartNumber>
      <Description>Nose cone, balsa, BNC-20FB, 2.29", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">2.29</Length>
    </NoseCone>
    <!-- BNC-20G3 (non Estes) 3:1 ogive ref legacy Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20G3</PartNumber>
      <Description>Nose cone, balsa, BNC-20G3, 2.2", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>
    <!-- BNC-20G4 (non Estes) 4:1 ogive ref legacy Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20G4</PartNumber>
      <Description>Nose cone, balsa, BNC-20G4, 3.0", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <!-- BNC-20G5 (non Estes) 5:1 ogive ref 2017 eRockets/Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20G5</PartNumber>
      <Description>Nose cone, balsa, BNC-20G5, 3.7", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BNC-20H (non Estes) is an 0.87" (spec 0.82") ellipsoid, shoulder 0.27" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20H</PartNumber>
      <Description>Nose cone, balsa, BT-20, 0.87", ellipsoid, PN BNC-20H</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.27</ShoulderLength>
      <Length Unit="in">0.87</Length>
    </NoseCone>

    <!-- BNC-20L (Mini-Bertha #0803 only) ref 1974 Estes custom parts catalog
         SOURCE ERROR: BNC-20L Semroc legacy gives incorrect length 2.0", weight .04 oz
         Estes official length is 1 3/8" but:
         Semroc new gives length 1.4"
         balsamachining.com also gives length 1.4"
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20L</PartNumber>
      <Description>Nose cone, balsa, BNC-20L, 1.375", parabolic</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.375</Length>
    </NoseCone>

    <!-- BNC-20LS is 1.43" ellipsoid, shoulder 0.53", downscale of BNC-60L ref 2017 eRockets/Semroc website
         SOURCE ERROR:  BNC-20LS length is given as 2.0" on legacy site, but drawing shows 1.43", which
         is also the correct scale-down from the BNC-60L. This error persists into the 2018 eRockets website.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20LS</PartNumber>
      <Description>Nose cone, balsa, BT-20, 1.43", ellipsoid, Estes BNC-60L downscale, PN BNC-20LS</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.53</ShoulderLength>
      <Length Unit="in">1.43</Length>
    </NoseCone>

    <!-- BNC-20MC matches Estes PN 71030 PSM-1 and is the nose of the Mercury capsule
         that contained the parachutes.  This part actually resembles a nose cone, though it was shoulderless. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20MC</PartNumber>
      <Description>Nose cone, balsa, BNC-20MC, Estes PSM-1 MR capsule nose</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.787</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.780</ShoulderDiameter>
      <ShoulderLength Unit="in">0.0</ShoulderLength>
      <Length Unit="in">1.228</Length>
    </NoseCone>
    <!-- BNC-20MH matches Estes PN 71032 (also referred to as PSM-1) and is the aft heat
         shield end of the Mercury capsule.  To actually use this as a tail cone, you'd
         need to create a transition definition for it.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20MH</PartNumber>
      <Description>Nose cone, balsa, BNC-20MH, Estes PSM-1 MR capsule base</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.820</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.800</ShoulderDiameter>
      <ShoulderLength Unit="in">0.0</ShoulderLength>
      <Length Unit="in">0.712</Length>
    </NoseCone>
    <!-- BNC-20MG (non Estes, Semroc KV-82 Moon Glo capsule shape) ref eRockets/Semroc 2017 site.
         shape is approximation.  Longer shoulder estimated per outline drawing.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20MG</PartNumber>
      <Description>Nose cone, balsa, BNC-20MG, 1.9", Semroc Moon Glo capsule</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.625</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>
    <!-- BNC-20N ref 1975 catalog, 0.08 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20N</PartNumber>
      <Description>Nose cone, balsa, BNC-20N, 2.75", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- BNC-20P is 1.35" Spaceman head, shoulder 0.47", bulbous shape, 0.06 oz -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20P</PartNumber>
      <Description>Nose cone, balsa, BT-20, 1.35", Astron Spaceman, PN BNC-20P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.06</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.90</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.47</ShoulderLength>
      <Length Unit="in">1.35</Length>
    </NoseCone>

    <!-- BNC-20R ref 1975 Estes catalog -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20R</PartNumber>
      <Description>Nose cone, balsa, BNC-20R, 2.75", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.711</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- BNC-20SP (non Estes), listed in legacy product search results but not table. Shoulder 0.25" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20SP</PartNumber>
      <Description>Nose cone, balsa, BT-20, 0.25", elliptical pod cap, PN BNC-20SP</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.25</ShoulderLength>
      <Length Unit="in">0.25</Length>
    </NoseCone>

    <!-- BNC-20SU (non Estes) ref 2017 eRoockets/Semroc web site -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20SU</PartNumber>
      <Description>Nose cone, balsa, BNC-20SU, 2.3", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>
    <!-- BNC-20WC (non Estes) ref 2017 eRockets/Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20WC</PartNumber>
      <Description>Nose cone, balsa, BNC-20WC, 3.0", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <!-- BNC-20X (non Estes) ref 2017 eRockets/Semroc website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20X</PartNumber>
      <Description>Nose cone, balsa, BT-20, 2.5", elliptical, PN BNC-20X</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BNC-20Y is a 1.1" (spec 1.0") conical, shoulder 0.4" (Yankee #1381 only) ref 1988 Estes catalog -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-20Y</PartNumber>
      <Description>Nose cone, balsa, BT-20, 1.0", conical, PN BNC-20Y</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.736</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
      <ShoulderLength Unit="in">0.40</ShoulderLength>
      <Length Unit="in">1.1</Length>
    </NoseCone>

    <!-- ===================================== -->
    <!-- BC-7xx Balsa Nose Cones for ST-7 tube -->
    <!-- ===================================== -->
    <!--
         *** BC-721CN nacelle cone/nozzle set not included, need rear taper diameter ***
         *** BC-726CN cone/nozzle set not included, need individal dimensions ***
    -->

    <!-- BC-708 is 0.95" cone (called 0.8"), shoulder 0.42" -->
    <!-- SOURCE ERROR: BC-708 is called 0.8" long, but drawing shows it to be 0.95" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-708</PartNumber>
      <Description>Nose cone, balsa, ST-7, 0.95", conical, PN BC-708</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">0.95</Length>
    </NoseCone>

    <!-- BC-710 is 1.0" conical, shoulder 0.47", Centuri PNC-71 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-710</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.0", conical, Centuri PNC-71 shape, PN BC-710</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.47</ShoulderLength>
      <Length Unit="in">1.0</Length>
    </NoseCone>
    
    <!-- BC-711 is 1.1" conical, shoulder 0.57", Centuri BC-71 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-711</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.1", conical, Centuri BC-71, PN BC-711</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">1.1</Length>
    </NoseCone>

    <!-- BC-714 is 1.4" ogive for Semroc Triton, Hydra VII, Mini Bat, 0.69" shoulder -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-714</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.4", ogive, old Semroc NB-204, PN BC-714</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">1.4</Length>
    </NoseCone>

    <!-- BC-715 is 1.5" near ellipsoid (called rounded ogive), shoulder 0.68", Centuri PNC-70 equiv -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-715</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.5", rounded ogive, Centuri PNC-70 shape, PN BC-715</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-715D is 1.5" ogive, 0.4" shoulder, Estes BNC-30 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-715D</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.5", ogive, Estes BNC-30D shape, PN BC-715D</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.40</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-715CN is a 2-part nacelle cone / nozzle set, so we have to split into an NC + tailcone transition -->

    <!-- BC-715CN_nacelle has exposed len 0.96", shoulder 0.375", cone at front of nacelle 0.4" long.
         Ogive is not a bad approximation to the shape.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-715CN_nacelle</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.5", nacelle, half of PN BC-715CN</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">0.96</Length>
    </NoseCone>

    <!-- BC-715CN_nozzle has exposed len 0.75", shoulder 0.375", pure cone, aft diam of cone 0.45" -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-715CN_nozzle [R]</PartNumber>
        <Description>Transition, balsa, ST-7, 0.75", conical nozzle, half of PN BTC-715CN</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.713</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.375</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.45</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">0.75</Length>
    </Transition>

    <!-- BC-716 is 1.6" ellipsoid, shoulder 0.68", Centuri BC-70 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-716</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.6", elliptical, Centuri BC-70, PN BC-716</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.6</Length>
    </NoseCone>

    <!-- BC-718 is 1.8" fat ogive, shoulder 0.55", early Centuri BC-711 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-718</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.8", ogive, early Centuri BC-711, PN BC-718</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.55</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-719 is 1.9" ogive, shoulder 0.68", Centuri BC-72 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-719</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.9", ogive, Centuri BC-72, PN BC-719</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.9</Length>
    </NoseCone>

    <!-- BC-720 is 2.0" ellipsoid, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-720</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.0", elliptical, PN BC-720</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-721 is 2.1" ogive, shoulder len 0.68", equiv to old Semroc NB-206 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-721</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.1", ogive, old Semroc NB-206, PN BC-721</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.1</Length>
    </NoseCone>

    <!-- BC-721CN is a nacelle + nozzle set, so we split into two components like BC-715CN -->

    <!-- BC-721CN_nacelle has exposed len 0.62", shoulder 0.50", ogive exposed shape has aft
         diam of 0.41". -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-721CN_nacelle</PartNumber>
      <Description>Nose cone, balsa, ST-7, 0.62", nacelle, half of PN BC-721CN</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">0.62</Length>
    </NoseCone>

    <!-- BC-721CN_nozzle has exposed len 0.75", shoulder 0.50", compound shape with an 0.5" long
         conical section tapering down to 0.55" diameter, then an 0.25" long cylinder of 0.375"
         diameter with a chamfer at the rear edge. Turns out we can model this by making the
         aft shoulder be the cylindrical section.  Mass and moments of inertia will be correct,
         aerodynamics not so much since OpenRocket doesn't model exposed shoulders. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-721CN_nozzle [R]</PartNumber>
        <Description>Transition, balsa, ST-7, 0.75", compound nozzle, half of PN BTC-721CN</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.713</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.55</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.375</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.25</AftShoulderLength>
        <Length Unit="in">0.50</Length>
    </Transition>

    <!-- BC-722, 2.2" ogive, shoulder len 0.52", no longer listed on eRockets/Semroc 2017 website.
         Described on legacy site as "close to BNC-30E" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-722</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.2", rounded ogive, Estes BNC-30E, PN BC-722</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.52</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BC-723 is 3:1 ogive 2.3" long, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-723</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.3", ogive, PN BC-723</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-723P is 2.3" rounded ogive, shoulder 0.68", Centuri PNC-73 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-723P</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.3", rounded ogive, Centuri PNC-73 shape, PN BC-723P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-726CN is a cone + nozzle set, split here into two components -->

    <!-- BC-726CN_cone has exposed len 1.00", shoulder 0.33", and is a pure cone with slightly rounded tip. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-726CN_cone</PartNumber>
      <Description>Nose cone, balsa, ST-7, 1.0", conical, half of PN BC-726CN</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.33</ShoulderLength>
      <Length Unit="in">1.00</Length>
    </NoseCone>

    <!-- BC-726CN_nozzle is a classic deLaval nozzle with exposed len 1.25", shoulder 0.33".
         The converging section is 0.40" long with a minimum diam of 0.38", the diverging section
         is 0.85" long with an aft diam of 0.71" -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-726CN_nozzle [R]</PartNumber>
        <Description>Transition, balsa, ST-7, 1.25", deLaval nozzle, half of PN BC-726CN</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.713</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.375</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.71</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">1.25</Length>
    </Transition>

    <!-- BC-727 described on Semroc legacy site as "close to BNC-20N", 2.75" len, shoulder len 0.69" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-727</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.75", ogive, Estes BNC-20N shape, PN BC-727</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- BC-728 is pure conical, 2.85" long (listed as 2.8"), shoulder len 0.33".  Described as
         "Point nose cone", possibly referring to Centuri "the Point" kit. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-728</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.85", conical, PN BC-728</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.33</ShoulderLength>
      <Length Unit="in">2.85</Length>
    </NoseCone>

    <!-- BC-728F is 2.8" ellipsoid, 0.68" shoulder, FSI NC-71 shape.
         BC-728F no longer appears in eRockets/Semroc 2017 website -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-728F</PartNumber>
      <Description>Nose cone, balsa, ST-7, 2.8", rounded ogive, FSI NC-71 shape, PN BC-728F</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BC-730 is 3.0" rounded tip ogive, 0.68" shoulder, Centuri PNC-76 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-730</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.0", ogive, Centuri PNC-76 shape, PN BC-730</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-730G is a 3.0" pure 4:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-730G</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.0", 4:1 ogive, PN BC-730G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-730P is a 3.0" rounded tip ogive, shoulder 0.68", Centuri PNC-76 blowmolded shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-730P</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.0", rounded ogive, Centuri PNC-76 blowmold shape, PN BC-730P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-731 is a 3.1" ogive (called rounded ogive), shoulder 0.68" on legacy site, Centuri BC-76 equivalent -->
    <!-- SOURCE ERROR:  BC-731 On the Semroc / e-rockets site in 2018, BC-731 is described as
         "rounded ogive short shoulder" and a photo (but no drawing) is given showing a part with an apparently
         shorter shoulder than the BC-731 drawing on the legacy Semroc site.  It looks possible that the
         design of BC-731 has changed. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-731</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.1", ogive, Centuri BC-76 shape, PN BC-731</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-731B is a 3.1" ogive, shoulder 0.68", Centuri BC-125B downscale -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-731B</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.1", ogive, Centuri BC-125B downscale, PN BC-731B</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-731F is a 3.1" ogive, shoulder 0.57", FSI NC-72 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-731F</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.1", ogive, FSI NC-72 shape, PN BC-731F</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-733 is a 3.3" ogive, shoulder 0.68", old Semroc NB-208 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-733</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.3", ogive, old Semroc NB-208, PN BC-733</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.3</Length>
    </NoseCone>

    <!-- BC-734 is a 3.4" compound shape, shoulder 0.68", equivalent to Centuri BC-78.
         Shape is ogive at the tip for length 1.25" and diam of 0.40"
         then a cylindrical section 1.125" long, diam 0.40"
         finally a straight conical taper of length 1.025" out to ST-7 diam -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-734</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.4", ogive-cyl-conical, Centuri BC-78, PN BC-734</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.05</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-734P is a 3.4" ogive (called "pointed-ogive"), shoulder 0.68", Centuri PNC-74 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-734P</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.4", pointed-ogive, Centuri PNC-74 shape, PN BC-734P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-735 is a 3.5" ogive, shoulder 0.68", Centuri BC-74 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-735</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.5", ogive, Centuri BC-74 shape, PN BC-735</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-736 is a 3.6" pure conical, shoulder 0.68", Centuri BC-79 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-736</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.6", conical, Centuri BC-79, PN BC-736</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.6</Length>
    </NoseCone>

    <!-- BC-737 is a 3.7" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-737</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.65", ogive, PN BC-737</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.65</Length>
    </NoseCone>

    <!-- BC-738 is a 3.8" 5:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-738</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.8", 5:1 ogive, PN BC-738</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.8</Length>
    </NoseCone>

    <!-- BC-739 is a 3.9" conical, shoulder 0.68", old Semroc NB-210 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-739</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.9", conical, old Semroc NB-210, PN BC-739</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- unclear what difference is between BC-739G and BC-739O.
         BC-739O does not exist on Semroc legacy site.  They both appear on the e-rockets / Semroc
         site, where the G has a photo and the O has a drawing.
    -->

    <!-- BC-739G is a 3.9" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-739G</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.9", ogive, PN BC-739G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-739O (O as in Oscar) is a 3.9" ogive, shoulder 0.68".  Appears only on new e-rockets.biz
         Semroc parts listing.  Not on Semroc legacy site -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-739O</PartNumber>
      <Description>Nose cone, balsa, ST-7, 3.9", ogive, PN BC-739O</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-744 is a 4.4" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-744</PartNumber>
      <Description>Nose cone, balsa, ST-7, 4.4", ogive, PN BC-744</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.4</Length>
    </NoseCone>

    <!-- BC-760 is a 6.0" flared ogive, shoulder 0.68", similar to Honest John. 
         Actual shape is an ogive section 5.0" long to a max diam of 1.05", with a 1.0"
         conical taper back down to the ST-7 OD.  Mass override is required. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-760</PartNumber>
      <Description>Nose cone, balsa, ST-7, 6.0", flared ogive, BC-760</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.20</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.759</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.713</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>

    <!-- =================================== -->
    <!-- BNC-30 nose cones for BT-30 tube    -->
    <!-- =================================== -->

    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30C</PartNumber>
      <Description>Nose cone, balsa, BNC-30C, 0.75", spherical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">0.75</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30D</PartNumber>
      <Description>Nose cone, balsa, BNC-30D, 1.5", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>PARABOLIC</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30DE</PartNumber>
      <Description>Nose cone, balsa, BNC-30DE, 1.375", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">1.375</Length>
    </NoseCone>
    <!-- BNC-30DOB has no shoulder; for Orange Bullet -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30DOB</PartNumber>
      <Description>Nose cone, balsa, BNC-30DOB, 1.375", ogive, no shoulder</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.0</ShoulderLength>
      <Length Unit="in">1.375</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30M</PartNumber>
      <Description>Nose cone, balsa, BNC-30M, 1.5", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30E</PartNumber>
      <Description>Nose cone, balsa, BNC-30E, 2.3", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>PARABOLIC</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-30N</PartNumber>
      <Description>Nose cone, balsa, BNC-30N, 2.75", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.0.767</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.723</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- =================================== -->
    <!-- BNC-40 nose cones for BT-40 tube    -->
    <!-- =================================== -->

    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-40D</PartNumber>
      <Description>Nose cone, balsa, BNC-40D, 1.5", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.763</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-40F</PartNumber>
      <Description>Nose cone, balsa, BNC-40F, 1.9", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.763</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.9</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-40SP</PartNumber>
      <Description>Nose cone, balsa, BNC-40SP, 2.4", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.763</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">2.4</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BNC-40G</PartNumber>
      <Description>Nose cone, balsa, BNC-40G, 4.5", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.825</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.763</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- ===================================== -->
    <!-- BC-8xx Balsa Nose Cones for ST-8 tube -->
    <!-- ===================================== -->

    <!-- BC-812 is a 1.2" rounded tip conical, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-812</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.2", conical, PN BC-812</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.2</Length>
    </NoseCone>

    <!-- BC-813 is a 1.3" ellipsoid, shoulder len 0.68" -->    
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-813</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.25", ellipsoid, PN BC-813</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.25</Length>
    </NoseCone>

    <!-- BC-814 is 1.4" ellipsoid, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-814</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.4", elliptical, PN BC-814</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.4</Length>
    </NoseCone>

    <!-- BC-815 is 1.5" ogive, shoulder 0.68", old Semroc NB-304 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-815</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.5", ogive, old Semroc NB-304, PN BC-815</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-817 is 1.7" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-817</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.7", ogive, PN BC-817</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.7</Length>
    </NoseCone>

    <!-- BC-818 is a 1.8" ellipsoid (called Bezier), shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-818</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.8", bezier, PN BC-818</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-818L is 1.8" ellipsoid (called ogive), shoulder 0.68", Estes BNC-60L downscale -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-818L</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.8", ogive, Estes BNC-60L downscale, PN BC-818L</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-819 is a 1.9" fat ogive, shoulder 0.68", Centuri BC-82 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-819</PartNumber>
      <Description>Nose cone, balsa, ST-8, 1.9", ogive, Centuri BC-82, PN BC-819</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.9</Length>
    </NoseCone>

    <!-- BC-820 is 2.0" ellipsoid (called Bezier), shoulder 0.68", Centuri PNC-80 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-820</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.0", ellipsoid, Centuri PNC-80 shape, PN BC-820</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-821 is a 2.1" ellipsoid, shoulder 0.68", Centuri BC-80 equivalent.  This is the
         original Centuri Snipe Hunter nose cone. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-821</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.1", ellipsoid, Centuri BC-80 shape, PN BC-821</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.1</Length>
    </NoseCone>

    <!-- BC-821SH is the modern Semroc Snipe Hunter nose/transition combo piece.  Nose is shape of BC-821 but with
         shorter shoulder.  Split into two parts here.  -->

    <!-- BC-821SH_nose from drawing is a 2.1" ellipsoid, shoulder 0.40"
         from actual Semroc Snipe Hunter ca. 2014: len 2 1/4", shoulder 3/8" 
         On the legacy site you can find a BC-821SH "Snipe Hunter set" but the only drawing is for a
         regular BC-821 long-shoulder cone; it may not have been a combined part then. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-821SH_nose</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.1", ellipsoid, Centuri Snipe Hunter/BC-80, PN BC-821SH</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.40</ShoulderLength>
      <Length Unit="in">2.1</Length>
    </NoseCone>

    <!-- BC-821SH_transition is reducing transition for Centuri/Semroc Snipe Hunter.
         Does not exist on legacy Semroc site.
         From drawing: Exposed length 0.82", fore shoulder 0.40", aft shoulder 0.65".
         From an actual Semroc Snipe Hunter: length 1.0", fore shoulder 3/8", aft shoulder 9/16"
         It is a little shorter than a regular BR-78 transition, and has shorter shoulders. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-821SH_transition</PartNumber>
        <Description>Transition, balsa, ST-8, 0.82", increasing, half of PN BC-821SH</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.759</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.715</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.65</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.908</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.863</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.40</AftShoulderLength>
        <Length Unit="in">0.82</Length>
    </Transition>
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-821SH_transition [R]</PartNumber>
        <Description>Transition, balsa, ST-8, 0.82", reducing, half of PN BC-821SH</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">0.908</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">0.863</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.40</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.759</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.715</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.65</AftShoulderLength>
        <Length Unit="in">0.82</Length>
    </Transition>

    <!-- BC-823 is 2.3" ogive, shoulder 0.68", old Semroc NB-306 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-823</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.3", ogive, old Semroc NB-306, PN BC-823</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-823E is 2.3" ogive, shoulder 0.68", upscale of Estes BNC-5E -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-823E</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.3", ogive, Estes BNC-5E upscale, PN BC-823E</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-RW825 is described on new 2017-2018 eRockets/Semroc site as upscale of BNC-5E, but the drawing
         does not resemble a BNC-5E at all.  Despite the 825 designation, the stated length is 2.3".
         BC-RW825 Does not appear on Semroc legacy site.
         Given that the drawing is impossible (tapered shoulder), there is not much I can do with
         this except presume that the BNC-5E upscale shape and length are correct, so I use a 2.3" ogive
         with a 0.68" shoulder like most other BC-8xx nose cones.

         SOURCE ERROR: BC-RW825 eRockets/Semroc website has outline drawing showing pure cone, should be
         fat ogive to be a BNC-5E upscale as stated.  The drawing also looks problematic as the shoulder
         is not even cylindrical.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-RW825</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.3", ogive, Estes BNC-5E upscale, PN BC-RW825</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-826 is a 2.6" ellipsoid, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-826</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.6", rounded ogive, PN BC-826</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-827 is 2.7" 3:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-827</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.7", ogive, PN BC-827</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BC-828 is 2.8" ogive, shoulder 0.68", 1/10 scale Sandia Tomahawk -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-828</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.8", ogive, 1/10 scale Sandia Tomahawk, PN BC-828</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BC-829 is 2.9" ogive, shoulder 0.68", 1/10 scale D-Region Tomahawk -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-829</PartNumber>
      <Description>Nose cone, balsa, ST-8, 2.9", ogive, 1/10 scale D-Region Tomahawk, PN BC-829</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-830 is a 3.0" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-830</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.0", ogive, PN BC-830</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-832 is a 3.2" ellipsoid (called Bezier), shoulder 0.68", Centuri BC-83 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-832</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.2", bezier, Centuri BC-83, PN BC-832</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.2</Length>
    </NoseCone>

    <!-- BC-832C is a 3.25" conical (called 3.2"), shoulder 0.68", Centuri BC-85 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-832C</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.2", conical, Centuri BC-85, PN BC-832C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>

    <!-- BC-833 is a 3.3" ellipsoid, shoulder 0.68", Centuri PNC-83 shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-833</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.3", ellipsoid, Centuri PNC-83 shape, PN BC-833</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.3</Length>
    </NoseCone>

    <!-- BC-834 is a 3.35" (nominal 3.4") compound ogive-cylinder-conical-cylinder shape, shoulder 0.68"
         Centuri BC-87 equivalent
         Ogive tip section is 1.35" long, to diam of 0.48"
         cylinder is 0.48" diam, 1.0" long
         conical section is 0.85" long
         aft cylinder section is 0.15" long x 0.908 diam
         Modeled as overall conical, mass and inertia moments are about right -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-834</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.4", compound ogive-cyl-conical-cyl, Centuri BC-87, PN BC-834</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.35</Length>
    </NoseCone>

    <!-- BC-834C is a 3.4" ogive, shoulder 0.68", Centuri BC-81 equivalent -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-834C</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.4", rounded ogive, Centuri BC-81, PN BC-834C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-836 is 3.6" ogive, shoulder 0.68", downscale of Estes BNC-55AC -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-836</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.6", secant ogive, Estes BNC-55AC downscale, PN BC-836</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.6</Length>
    </NoseCone>

    <!-- BC-837 is a 3.7" ogive, shoulder 0.68", old Semroc NB-309 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-837</PartNumber>
      <Description>Nose cone, balsa, ST-8, 3.7", ogive, old Semroc NB-309, PN BC-837</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BC-838 has a 5-segment compound shape, equiv to Centuri BC-88.
         Length per drawing is 4.17", shoulder 0.68".
         tip segment is a very short conic, length 0.08", to a diam of 0.22"
         segment 2 is a shallower conic, length 1.84", diam 0.22" to 0.50"
         segment 3 is a cylinder, diam 0.50", length 0.75"
         segment 4 is an enlarging conic, length 1.15", diam 0.50" to 0.908"
         segment 5 is a short cylinder, length 0.35", diam 0.908"
         SOURCE ERROR: BC-838 has listed length of 3.8" on both old and new Semroc sites, but drawing scales to 4.17"
         -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-838</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.17", compound, Centuri BC-88, PN BC-838</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.17</Length>
    </NoseCone>


    <!-- BC-840 is a 4.0" ellipsoid, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-840</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.0", ellipsoid, PN BC-840</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-845 is a 4.5" 5:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-845</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.5", 5:1 ogive, PN BC-845</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-845P is a 4.5" ellipsoid (called rounded ogive), shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-845P</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.5", rounded ogive, PN BC-845P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-846G is a 4.6" ogive, shoulder 0.68", Centuri PNC-89 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-846G</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.6", rounded ogive, Centuri PNC-89 shape, PN BC-846G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BC-846 is a 4.6" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-846</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.6", rounded ogive, PN BC-846</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BC-847 is 4.75" conical (quoted 4.7"), shoulder 0.68", old Semroc NB-312 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-847</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.75", conical, old Semroc NB-312, PN BC-847</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.75</Length>
    </NoseCone>

    <!-- BC-847W is a 4.7" modified ogive, shoulder 0.68", Estes BNC-5W upscale -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-847W</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.7", ogive, Estes BNC-5W upscale, PN BC-847W</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.7</Length>
    </NoseCone>

    <!-- BC-848 is a 4.8" ogive, shoulder 0.68", Centuri BC-89 clone -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-848</PartNumber>
      <Description>Nose cone, balsa, ST-8, 4.8", ogive, Centuri BC-89, PN BC-848</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.8</Length>
    </NoseCone>

    <!-- BC-853 is a 5.3" ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-853</PartNumber>
      <Description>Nose cone, balsa, ST-8, 5.3", ogive, PN BC-853</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BC-857 is a 5.7" flared ogive, shoulder 0.68", equiv to Centuri BC-86 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-857</PartNumber>
      <Description>Nose cone, balsa, ST-8, 5.7", flared ogive, Centuri BC-86, PN BC-857</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.28</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.908</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.863</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">5.7</Length>
    </NoseCone>
    
    <!-- ==================================================== -->
    <!-- BC-8Fxx Balsa Nose Cones (same OD as FSI HRT-8 tube) -->
    <!-- ==================================================== -->

    <!-- BC-8F20 is a 2.0" rounded ogive, shoulder 0.68" -->    
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-8F20</PartNumber>
      <Description>Nose cone, balsa, ST-8F, 2.0", rounded ogive, PN BC-8F20</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.883</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-8F28 is a 2.8" very rounded ogive, shoulder 0.68", FSI NHC-81 clone -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-8F28</PartNumber>
      <Description>Nose cone, balsa, ST-8F, 2.8", rounded ogive, FSI NHC-81, PN BC-8F28</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.921</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.883</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- ================================================ -->
    <!-- Series 085 nose cones for Centuri LT-085 tubes   -->
    <!-- ================================================ -->

    <!-- BC-08542 is 4.2" ogive, upscale of Centuri BC-524.  Length 4.2", shoulder len 0.69" (scaled dwg)  -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-08542</PartNumber>
      <Description>Nose cone, balsa, LT-085, 4.2", ogive, Centuri BC-524 upscale, PN BC-08542</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.945</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.862</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- =================================== -->
    <!-- BC-9xx Balsa Nose Cones for #9 tube -->
    <!-- =================================== -->

    <!-- BC-912 is a "Ram Jet" nacelle 1.2" long, shoulder 0.57", "BNC-50BA for ST-9",
         weight given as .05 oz on legacy site -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-912</PartNumber>
      <Description>Nose cone, balsa, ST-9, 1.2", BOMARC nacelle, Estes BNC-50BA shape, PN BC-912</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">1.2</Length>
    </NoseCone>

    <!-- BC-913 is 1.3" long conical with rounded tip, shoulder 0.57", Centuri PNC-71 upscale
         SOURCE ERROR: BC-913 described as "ogive" but drawing shows conical
         page description says "upscale of Centuri PNC-71" which is also shown as conical
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-913</PartNumber>
      <Description>Nose cone, balsa, ST-9, 1.3", conical, Centuri PNC-71 upscale, PN BC-912</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">1.3</Length>
    </NoseCone>

    <!-- BC-913 is 1.4" long blunt ogive, shoulder 0.57", Estes BNC-50J shape
         page description says "BNC-50J for ST-9"
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-914</PartNumber>
      <Description>Nose cone, balsa, ST-9, 1.4", blunt ogive, Estes BNC-50J shape, PN BC-914</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">1.4</Length>
    </NoseCone>

    <!-- BC-918 is Estes Astron Spaceman type, upscale of BNC-20P, 1.8" long, 0.50" shoulder.
         Uses mass override due to bulbous shape -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-918</PartNumber>
      <Description>Nose cone, balsa, ST-9, 1.8", Astron Spaceman, Estes BNC-20P shape, PN BC-918</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.13</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-922 is a 2.2" capsule shape, shoulder 0.57".  Using mass override from legacy site list -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-922</PartNumber>
      <Description>Nose cone, balsa, ST-9, 2.2", capsule, PN BC-922</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.11</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BC-926 is a 2.6" rounded ogive, shoulder 0.57", Estes BNC-50KP shape for ST-9 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-926</PartNumber>
      <Description>Nose cone, balsa, ST-9, 2.6", rounded ogive, Estes BNC-50KP shape, PN BC-926</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-926BC is a 2.6" nacelle, shoulder 0.57", Estes BNC-50BC (Ram Jet) shape for ST-9 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-926BC</PartNumber>
      <Description>Nose cone, balsa, ST-9, 2.6", nacelle, Estes BNC-50BC shape, PN BC-926BC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-928 is 2.8" ogive, shoulder 0.68", Estes BNC-50K shape for ST-9 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-926</PartNumber>
      <Description>Nose cone, balsa, ST-9, 2.8", ogive, Estes BNC-50K shape, PN BC-926</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BC-930 is 3.0" 3:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-930</PartNumber>
      <Description>Nose cone, balsa, ST-9, 3.0", 3:1 ogive, PN BC-930</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-932 is/was a 3.25" ellipsoid, shoulder 0.68", Estes BNC-50X shape for ST-9
         BC-932 is not found in the legacy product list search, however it does appear with a
         "new" flag in the legacy nose cone table, but the "View" link gets an error.
         It has identical specs to the BC-933.  I am keeping a listing for it because it's
         possible that old kit instructions might reference it.
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-932</PartNumber>
      <Description>Nose cone, balsa, ST-9, 3.25", elliptical, Estes BNC-50X shape, PN BC-932</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>

    <!-- BC-933 is the surviving Estes BNC-50X shape for ST-9.  It's a 3.25" ellipsoid, shoulder 0.68" -->
    <!-- SOURCE ERROR: BC-932 description is identical to BC-933, they both say BNC-50X for ST-9.  It
         looks like BC-932 is actually dead. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-933</PartNumber>
      <Description>Nose cone, balsa, ST-9, 3.25", elliptical, Estes BNC-50X shape, PN BC-933</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>

    <!-- BC-937 is a 3.7" ellipsoid, shoulder 0.68", downscale of Estes BNC-55AO.
         BC-937 does not appear in the legacy site product search results, but does appear in the table
         with a "New" flag. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-937</PartNumber>
      <Description>Nose cone, balsa, ST-9, 3.7", elliptical, Estes BNC-55AO downscale, PN BC-937</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BC-940 is 4.0" ellipsoid, shoulder 0.68", upscale of Centuri PNC-76 -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-940</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.0", rounded ogive, Centuri PNC-76 upscale, PN BC-940</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-940G is 4.0" 4:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-940G</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.0", 4:1 ogive, PN BC-940G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BNC-941 is Estes BNC-50AD Honest John shape; flared so we use mass override
         Exposed length 4.1", shoulder 1.07" (much longer shoulder than Estes version) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-941</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.1", flared ogive, Honest John, Estes BNC-50AD, PN BC-941</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.25</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">1.07</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- SOURCE ERROR: BC-943 and BC-944  Semroc legacy and new sites both show length of 4.3" for BC-943 and
         4.4" for BC-944.  They are supposed to be modeled after the Estes PNC-50Y and
         BNC-50Y, respectively.  However the official Estes specs for those items have
         identical lengths of 4.375".  UPDATE: The Semroc drawings show that they do in fact
         have slightly different shapes - the BNC-50Y is more pointed, and the Semroc shoulder
         lengths are different.  The BC-944 drawing shows the correct Estes-compatible length
         of 4.375", but the BC-943 drawing is too short at 4.30".
    -->
    <!-- BC-943 is 4.3" Estes PNC-50Y shape (slightly too short?) for ST-9, shoulder 0.57" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-943</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.3", ogive, Estes PNC-50Y shape, PN BC-943</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">4.30</Length>
    </NoseCone>

    <!-- BC-944 is 4.375" Estes BNC-50Y shape for ST-9, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-944</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.375", ogive, Estes BNC-50Y shape, PN BC-944</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.375</Length>
    </NoseCone>

    <!-- BC-945 is 4.5" Estes Firecat flared ogive shape BNC-50BD, shoulder 0.57".  Mass override needed here. -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-945</PartNumber>
      <Description>Nose cone, balsa, ST-9, 4.5", Firecat flared ogive, Estes BNC-50BD shape, PN BC-945</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.22</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-950 is a 5.0" 5:1 ogive, shoulder 0.68" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-950</PartNumber>
      <Description>Nose cone, balsa, ST-9, 5.0", 5:1 ogive, PN BC-950</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!--
        BC-958 is a 5.8" conic with cylinder extension, shoulder 0.57"
        BC-958 hits a complication in Estes part nomenclature.
        BC-958 is "BNC-50V" shape, which is a Semroc equivalent to the Estes
        PNC-50V/PNC-50BB (where the 50BB includes a tail cone).  Estes never made this in balsa,
        but apparently used the nose cone alone as PNC-50V, and also as PNC-50BB that
        included the tailcone.

        There is a photo on siriusrocketry.biz showing the Estes nose cone in plastic with a
        conjoined tailcone.  It's described as "Estes PNC-50V/50BB Nose Cone".

        Conical but with cylindrical section at rear 1.0" long
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-958</PartNumber>
      <Description>Nose cone, balsa, ST-9, 5.8", conical, Estes PNC-50V/PNC-50BB nose, PN BC-958</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">5.8</Length>
    </NoseCone>

    <!-- BC-960 is 6.0" flared ogive, shoulder 0.82".  site says "BNC-50HJ for ST-9", which is obviously an
         Honest John shape.  Used a 0.33 oz mass override -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-960</PartNumber>
      <Description>Nose cone, balsa, ST-9, 6.0", flared ogive, Honest John, Estes BNC-50HJ, PN BC-960</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.33</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>

    <!-- BC-961 is Estes BNC-50NA shape (Nike Ajax), 6.1" conical ogive, shoulder 0.97" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-961</PartNumber>
      <Description>Nose cone, balsa, ST-9, 6.1", conical ogive, Nike Ajax, Estes BNC-50NA, PN BC-961</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.998</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.97</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- =================================== -->
    <!-- BNC-50 nose cones for BT-50 tube    -->
    <!-- =================================== -->

    <!-- BNC-2 was the Apollo capsule nose cone for the NCK-29 Apollo Capsule
         Bizarrely, BNC-2 was a fit for a BT-50 tube and should have been called BNC-50xxx.
         Estes dimensions and PN from 1974 Custom Parts Catalog
         Mass override used because it is significantly flared, OD = 1.360 (Estes) 1.356 (Semroc)
         The Semroc mass spec is different than the Estes one, and Semroc has it as 1.5" long
    -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-2</PartNumber>
      <Description>Nose cone, balsa, BNC-2, 1.5", Apollo capsule</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.09</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>
    <!-- Semroc BNC-50AO is repro of Estes BNC-50AO, Honest John upper part, requiring a
         shroud for the lower section -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50AD</PartNumber>
      <Description>Nose cone, balsa, BNC-50AD, 4.1", Honest John top shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.25</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50AH</PartNumber>
      <Description>Nose cone, balsa, BNC-50AH, 4", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>
    <!-- BNC-50AO is downscale of Estes BNC-55AO -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50AO</PartNumber>
      <Description>Nose cone, balsa, BNC-50AO, 3.7", elliptical, BNC-55AO downscale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.7</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50AR</PartNumber>
      <Description>Nose cone, balsa, BNC-50AR, 5.5", rounded conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">5.5</Length>
    </NoseCone>
    <!-- BNC-50BA (old BOMARC #0657) ref Semroc legacy website.
         conical shape looks like best match.  Semroc weight given as 0.05 oz -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50BA</PartNumber>
      <Description>Nose cone, balsa, BNC-50BA, 1.3", ramjet nacelle, BOMARC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.05</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.3</Length>
    </NoseCone>
    <!-- BNC-50BC (Wolverine #0816 etc.), ref 1974 parts catalog. Ramjet style cone a la BOMARC pods.
         Estes gives len 2 3/4" and weight .156 oz but Semroc gives length 2.6" and weight 0.11 oz.
         Scaling from scans on spacemodeling.com, length of the ramjet cone is 0.5".
    -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50BC</PartNumber>
      <Description>Nose cone, balsa, BNC-50BC, 2.6", ramjet nacelle</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.11</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>
    <!-- BNC-50BD (old Firecat #0821 only), ref 1974 custom parts catalog.
         Flared ogive so shape is an approximation.  Mass override needed due to heavier flared shape.
         Estes 1974 parts catalog gives len 5", Semroc legacy has 4.5".
         NC max diameter is 1.174" on Semroc legacy.
         If you want to model this cone more accurately for aerodynamics you should mate a 1.174" ogive nose
         of about 3" long to a 1.174 to .976 transition of about 1.5" long.
         For this and similar cases we really need OR to support predefined parts that are compositions
         of other parts.
    -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50BD</PartNumber>
      <Description>Nose cone, balsa, BNC-50BD, 4.5", flared ogive, Firecat</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.22</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.30</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>
    <!-- Semroc BNC-50C is upscale of Centuri PNC-71 -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50C</PartNumber>
      <Description>Nose cone, balsa, BNC-50C, 1.3", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">1.3</Length>
    </NoseCone>
    <!-- Semroc BNC-50CBB is rescale of Canaroc BN-200B.  Long conical shape makes it look
         like the "BB" suffix may mean Black Brant -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50CBB</PartNumber>
      <Description>Nose cone, balsa, BNC-50CBB, 4.7", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.7</Length>
    </NoseCone>
    <!-- Semroc BNC-50CPB is rescale of Canaroc PN-200B.  -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50CPB</PartNumber>
      <Description>Nose cone, balsa, BNC-50CPB, 2.1", bezier</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.1</Length>
    </NoseCone>
    <!-- Semroc BNC-50G is a 5.85:1 ogive  -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50G</PartNumber>
      <Description>Nose cone, balsa, BNC-50G, 5.7", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">5.7</Length>
    </NoseCone>
    <!-- Semroc BNC-50G3 is a 3:1 ogive -->
    <!-- SOURCE ERROR: BNC-50G3 Semroc legacy and new sites both describe BNC-50G3 as a #9 cone -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50G3</PartNumber>
      <Description>Nose cone, balsa, BNC-50G3, 2.93", 3:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.93</Length>
    </NoseCone>
    <!-- Semroc BNC-50G4 is a 4:1 ogive -->
    <!-- SOURCE ERROR: BNC-50G4 Semroc legacy and new sites both describe BNC-50G4 as a #9 cone -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50G4</PartNumber>
      <Description>Nose cone, balsa, BNC-50G4, 3.9", 4:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>
    <!-- Semroc BNC-50G5 is a 5:1 ogive
         SOURCE ERROR: BNC-50G5 Semroc legacy and new sites both describe this as a #9 cone -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50G5</PartNumber>
      <Description>Nose cone, balsa, BNC-50G5, 4.9", 5:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>
    <!-- Semroc BNC-50HJ is Honest John flared ogive, no Estes counterpart
         Unlike BNC-50AO, this part has the full shape and does not need a shroud -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50HJ</PartNumber>
      <Description>Nose cone, balsa, BNC-50HJ, 6.0", flared ogive, Honest John</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.32</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>
    <!-- BNC-50JE is a slightly flared "capsule" shape (not Mercury/Gemini).  Not a repro -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50JE</PartNumber>
      <Description>Nose cone, balsa, BNC-50JE, 4.1", capsule shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.07</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>
    <!-- BNC-50J is an Estes repro -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50J</PartNumber>
      <Description>Nose cone, balsa, BNC-50J, 1.37", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">1.37</Length>
    </NoseCone>
    <!-- Semroc BNC-50K is an Estes repro -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50K</PartNumber>
      <Description>Nose cone, balsa, BNC-50K, 2.75", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>
    <!-- Semroc BNC-50KS is a BNC-50K but with "short shoulder".  Not a repro.
         *** actual shoulder length unknown but less than 0.5" *** -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50KS</PartNumber>
      <Description>Nose cone, balsa, BNC-50KS, 2.75", ogive, short shoulder</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.375</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>
    <!-- Semroc BNC-50KP is a balsa version of Estes PNC-50K, which has a very slight
         shape difference from the BNC-50. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50KP</PartNumber>
      <Description>Nose cone, balsa, BNC-50KP, 2.75", ogive, PNC-50K shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>
    <!-- Semroc BNC-50MA is a Mercury/Atlas capsule.  Not a repro.  Mass from Semroc
         legacy site -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50MA</PartNumber>
      <Description>Nose cone, balsa, BNC-50MA, 2.2", Mercury Atlas capsule</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.10</Mass>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>
    <!-- Semroc BNC-50NA is for Nike Ajax -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50NA</PartNumber>
      <Description>Nose cone, balsa, BNC-50NA, 6.0", conical, Nike Ajax</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>
    <!-- Semroc BNC-50P is an upscale of the Estes Spaceman nose cone.  Mass from Semroc legacy site -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50P</PartNumber>
      <Description>Nose cone, balsa, BNC-50P, 1.8", bulbous, Spaceman upscale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.13</Mass>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50SF1</PartNumber>
      <Description>Nose cone, balsa, BNC-50SF1, 3.0", conical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <!-- SEMROC BNC-50SF2 is a hybrid shape, approx 1" cone with 2" cylinder behind it.  Mass from Semroc legacy site -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50SF1</PartNumber>
      <Description>Nose cone, balsa, BNC-50SF1, 3.0", cylinder-conical combo</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.13</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50SF3</PartNumber>
      <Description>Nose cone, balsa, BNC-50SF3, 3.0", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50SF4</PartNumber>
      <Description>Nose cone, balsa, BNC-50SF4, 3.0", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <!-- SEMROC BNC-50SF5 is a "straight" nose cone with a 90 degree flat face.  Mass from Semroc legacy site.
         To model drag better, you should create a very short cone with a 2.9" cylinder behind it. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50SF5</PartNumber>
      <Description>Nose cone, balsa, BNC-50SF5, 3.0", cylinder with flat face</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.18</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>
    <!-- Semroc BNC-50V: legacy site says "same as BNC-50BB without the tail cone". (confirmed by Brohm for Estes PNC-50V/BB)
         The actual shape (based on an actual Estes PNC-50V) is a 4.75" cone with a 1.0" cylinder section behind it.
         Mass for the balsa version from Semroc legacy site. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50V</PartNumber>
      <Description>Nose cone, balsa, BNC-50V, 5.75", conical-cylinder hybrid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.16</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">5.75</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50X</PartNumber>
      <Description>Nose cone, balsa, BNC-50X, 3.25", elliptical</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50Y</PartNumber>
      <Description>Nose cone, balsa, BNC-50Y, 4.4", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.4</Length>
    </NoseCone>
    <!-- SEMROC BNC-50YP is an exact match for the Estes PNC-50Y, which is 0.1" shorter than BNC-50Y.  -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-50YP</PartNumber>
      <Description>Nose cone, balsa, BNC-50YP, 4.3", ogive, PNC-50Y shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">0.976</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.948</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">4.3</Length>
    </NoseCone>


    <!-- ==================================== -->
    <!-- Series 10 nose cones for ST-10 tube  -->
    <!-- ==================================== -->
    <!-- shoulder lengths all estimaetd from Semroc drawings unless a Centuri part exists -->

    <!-- BC-1016 is old Semroc NB-404, 1.6" ogive, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1016</PartNumber>
        <Description>Nose cone, balsa, ST-10, 1.6", ogive, old Semroc NB-404</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">1.6</Length>
    </NoseCone>

    <!-- BC-1016C is Centuri Orion downscale, actually a rounded cone (Semroc erroneously says "rounded ogive")
         Ellipsoid shape makes mass come out better.
         There is also listed a BC-1016CO with identical specs, I just show as a 2nd PN on this one
         Shoulder len 0.75" (scaled dwg)
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1016C, BC-1016CO</PartNumber>
        <Description>Nose cone, balsa, ST-10, 1.6", round tip cone, Centuri Orion downscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">1.6</Length>
    </NoseCone>

    <!-- BC-1017 is downscale of Centuri PNC-231, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1017</PartNumber>
        <Description>Nose cone, balsa, ST-10, 1.7", round tip cone, Centuri PNC-231 downscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">1.7</Length>
    </NoseCone>

    <!-- BC-1019 is upscale of old Semroc NB-204, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1019</PartNumber>
        <Description>Nose cone, balsa, ST-10, 1.9", ogive, upscale of old Semroc NB-204</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">1.9</Length>
    </NoseCone>

    <!-- BC-1020 is upscale of Estes Mark II nose cone, shoulder 0.69", spherical tip
         radius 0.4" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1020</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.0", spherical tip, upscale of Estes Mark II</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-1020E is ellipsoid, equivalent to Centuri BC-101, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1020E</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.0", ellipsoid, Centuri BC-101</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.0</Length>
    </NoseCone>

    <!-- BC-1022 is upscale of Centuri PNC-70, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1022</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.2", ellipsoid, upscale of Centuri PNC-70</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BC-1023 is downscale of Coaster Centauri.  Shape is a fat ogive, approaching ellipsoid.
         Length 2.25", shoulder len 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1023 given length on legacy site is 2.3", but scaled dwg is 2.25".
         Looks like part number length rounding propagated into the description.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1023</PartNumber>
        <Description>Nose cone, balsa, BC-1023, 2.25", ellipsoid, downscale of Coaster Centauri</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.25</Length>
    </NoseCone>

    <!-- BC-1023C is downscale of Centuri BC-1636.  Length 2.25", shoulder len 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1023C given length on legacy site is 2.3", but scaled dwg is 2.25".
         Looks like part number length rounding propagated into the description.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1023C</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.25", ogive, downscale of Centuri BC-1636</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.25</Length>
    </NoseCone>

    <!-- BC-1024 is old Semroc NB-406, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1024</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.4", ogive, old Semroc NB-406</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.4</Length>
    </NoseCone>

    <!-- BC-1028 is upscale of Estes BNC-5E.  Len 2.75", shoulder len 0.60" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1028 length on legacy site is 2.8", but scaled dwg is 2.75".
         Looks like part number length rounding propagated into the description.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1028</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.75", ogive, upscale of Estes BNC-5E</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.60</ShoulderLength>
        <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- BNC-1029 is upscale of Estes BNC-50K.  Len 2.9", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1029</PartNumber>
        <Description>Nose cone, balsa, ST-10, 2.9", ogive, upscale of Estes BNC-50K</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-1031 is upscale of Centuri PNC-73.  Length 3.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1031</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.1", ellipsoid, upscale of Centuri PNC-73</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-1032 is generic 3:1 ogive.  Length 3.2", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1032</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.2", 3:1 ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.2</Length>
    </NoseCone>

    <!-- BC-1033 is downscale of Estes PNC-80K.  Len 3.3", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1033</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.3", ogive, downscale of Estes PNC-80K</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.3</Length>
    </NoseCone>

    <!-- BC-1037 listed on old Semroc site as "Centuri BC-10x".  Len 3.7", shoulder len 0.69" (scaled dwg).
         It no longer appears on the 2018 Semroc/e-rockets site. *** what was this? *** -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1037</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.7", ogive, Centuri BC-10x</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BC-1037N is upscale of Estes BNC-30N.  Length 3.7", shoulder len 0.69" (scaed dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1037N</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.7", ogive, upscale of Estes BNC-30N</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BC-1038 is a capsule shape, Centuri BC-108.  Len 4.13", shoulder 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR:  BC-1038 given length on legacy site is 3.8", but scaled drawing gives 4.13". -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1038</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.13", caapsule, Centuri BC-108</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.13</Length>
    </NoseCone>


    <!-- BC-1038B is a fat ogive, downscale of Centuri BC-175B, len 3.8", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1038B</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.8", fat ogive, downscale of Centuri BC-175B</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.8</Length>
    </NoseCone>

    <!-- BC-1039 is Centuri BC-103, old Semroc NB-410, len 3.9", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1039</PartNumber>
        <Description>Nose cone, balsa, ST-10, 3.9", ogive, Centuri BC-103, old Semroc NB-410</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-1040 is "special zig-zag" from new Semroc 2017 site, not on legacy site.  Looks like witch hat
         with 2 brims.  No mass given so I give it a 20% bump from a regular conical.
         Length 4.0", shoulder len 0.65", each "hat" section is 0.75" long (scaled dwg 130ppi)
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1040</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.0", special zig-zag</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.65</ShoulderLength>
        <Length Unit="in">4.0</Length>
        <Mass Unit="oz">0.18</Mass>
    </NoseCone>

    <!-- BC-1041 is Centuri BC-105, cone with 0.63" cylinder aft. Len 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1041</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.1", conical+cylinder, Centuri BC-105</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-1041G is upscale of Centuri PNC-76 blowmolded cone.  Len 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1041G</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.1", ellipsoid, Centuri PNC-76 upscale, PN BC-1041GG</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-1041P is Centuri PNC-103.  Len 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1041P</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.1", ellipsoid, Centuri PNC-103 shape, PN BC-1041P</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-1042 is generic 4:1 ogive.  Len 4.2", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1042</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.2", 4:1 ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BC-1042AC is downscale of Estes BNC-55AC.  Len 4.2", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1042AC</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.2", secant ogive, Estes BNC-55AC downscale, PN BC-1042AC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BC-1043 is flared double cone, Centuri PNC-102. Len 4.46", shoulder len 0.69" (scaled dwg).
         Used ellipsoid shape to get best moments of inertia. -->
    <!-- SOURCE ERROR: BC-1043 length given on legacy site as 4.3", but drawing scales to 4.46" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1043</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.46", flared double cone, Centuri PNC-102 shape, PN BC-1043</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.46</Length>
        <Mass Unit="oz">0.32</Mass>
    </NoseCone>

    <!-- BC-1045 is generic 4.5" ogive, no reference given.  Len 4.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1045</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.5", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-1045RR (2018 erockets site only) is a nose cone with a #10 balsa tube coupler turned
         in the same assembly. Len 4.65", shoulder len .50" (scaled dwg 61ppi)
         This is not the exact same nose cone shape as BC-1045.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1045RR</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.65", ogive, nose plus tube coupler</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.50</ShoulderLength>
        <Length Unit="in">4.65</Length>
    </NoseCone>

    <!-- BC-1045P is Centuri PNC-106, ogive, len 4.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1045P</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.5", rounded ogive, Centuri PNC-106 shape, PN BC-1045P</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-1048 is generic rounded tip ogive.  Semroc legacy says "Centuri BC-10x".
         Length is 4.98", shoulder len 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR:  BC-1048 length given on legacy site as 4.8", but drawing scales to 4.98" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1048</PartNumber>
        <Description>Nose cone, balsa, ST-10, 4.98", rounded tip ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">4.98</Length>
    </NoseCone>

    <!-- BC-1050 is a generic slightly rounded tip ogive.  Semroc legacy says "Centuri PNC-10x"
         Length 5.0", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1050</PartNumber>
        <Description>Nose cone, balsa, ST-10, 5.0", rounded-tip ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BC-1051 is 5.1" cone, old Semroc NB-413.  Len 5.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1051</PartNumber>
        <Description>Nose cone, balsa, ST-10, 5.1", conical, old Semroc NB-413</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">5.1</Length>
    </NoseCone>

    <!-- BC-1052 is 5.2" ogive, Centuri BC-107.  Len 5.2", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1052</PartNumber>
        <Description>Nose cone, balsa, ST-10, 5.2", ogive, Centuri BC-107</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">5.2</Length>
    </NoseCone>

    <!-- BC-1056 is 5.6" ogive, upscale of Estes BNC-5W.  Len 5.6", shoulder len 0.62" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1056</PartNumber>
        <Description>Nose cone, balsa, ST-10, 5.6", ogive, upscale of Estes BNC-5W</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.62</ShoulderLength>
        <Length Unit="in">5.6</Length>
    </NoseCone>

    <!-- BC-1063 is 1/16 Nike Smoke, slightly flared conical.
         The deviation from simple conical doesn't justify a mass override here. 
         Len 6.3", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BC-1063</PartNumber>
        <Description>Nose cone, balsa, BC-1063, 6.3", flared conical, 1/16 Nike Smoke</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.040</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.000</ShoulderDiameter>
        <ShoulderLength Unit="in">0.69</ShoulderLength>
        <Length Unit="in">6.3</Length>
    </NoseCone>


    <!-- =================================== -->
    <!-- BNC-51 nose cones for BT-51 tube    -->
    <!-- =================================== -->

    <!-- BNC-51MR is a 1/70 scale Mercury-Redstone capsule for BT-51.
         Conical shape is approximate.  Mass is from Semroc legacy site. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-51MR</PartNumber>
      <Description>Nose cone, balsa, BNC-50MR, 1.79", Mercury capsule</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.07</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.119</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.985</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">1.79</Length>
    </NoseCone>


    <!-- =================================== -->
    <!-- BNC-52 nose cones for BT-52 tube    -->
    <!-- =================================== -->

    <!-- SEMROC BNC-52AG is a 1/242 scale Apollo CM/SM/LM section.  Shape is complex so I approximate
         as conical and apply the mass from theSemroc legacy site.  Shoulder len from Estes BNC-52AG. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-52AG</PartNumber>
      <Description>Nose cone, balsa, BNC-52AG, 3.25", 1/242 Apollo CM/SM/LM section</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.1</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.014</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.988</ShoulderDiameter>
      <ShoulderLength Unit="in">0.5</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>

    <!-- SEMROC BNC-52G matches the Estes Thor Agena B capsule.  Mass from Semroc legacy site. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-52G</PartNumber>
      <Description>Nose cone, balsa, BNC-52G, 1.25", Thor Agena B capsule</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Mass Unit="oz">0.07</Mass>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.014</OutsideDiameter>
      <ShoulderDiameter Unit="in">0.986</ShoulderDiameter>
      <ShoulderLength Unit="in">0.50</ShoulderLength>
      <Length Unit="in">1.25</Length>
    </NoseCone>

    <!-- =================================== -->
    <!-- Series 11 nose cones for ST-11 tube -->
    <!-- =================================== -->

    <!-- BC-1121 is rounded tip cone, Cineroc shape downscale.  Len 2.15", shoulder len 0.69" (scaled dwg)  -->
    <!-- SOURCE ERROR: BC-1121 len is given as 2.1" but drawing scales to 2.15" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1121</PartNumber>
      <Description>Nose cone, balsa, ST-11, 2.15", rounded tip cone, Cineroc shape downscale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.15</Length>
    </NoseCone>

    <!-- BC-1122 is downscale of old Semroc NB-808.  Len 2.2", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1122</PartNumber>
      <Description>Nose cone, balsa, ST-11, 2.2", ogive, downscale of old Semroc NB-808</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BC-1135 is 3.55" ellipsoid, FSI HNC-101 shape.  Len 3.55", shoulder len 0.83" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1135 len is given as 3.5" but drawing scales to 3.55" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1135</PartNumber>
      <Description>Nose cone, balsa, ST-11, 3.55", ellipsoid, FSI HNC-101 shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.83</ShoulderLength>
      <Length Unit="in">3.55</Length>
    </NoseCone>

    <!-- BC-1135G is generic 3:1 ogive.  Len 3.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1135G</PartNumber>
      <Description>Nose cone, balsa, ST-11, 3.5", 3:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-1147 is generic 4:1 ogive.  Len 4.7", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1147</PartNumber>
      <Description>Nose cone, balsa, ST-11, 4.7", 4:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.7</Length>
    </NoseCone>

    <!-- BC-1149 is downscale of Estes PNC-55AC.  Len 4.9", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1149</PartNumber>
      <Description>Nose cone, balsa, ST-11, 4.9", secant ogive, downscale of Estes PNC-55AC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BC-1150 is a 5.0" curved capsule.  Conical is decent approximation for mass.
         Len 5.0", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1150</PartNumber>
      <Description>Nose cone, balsa, ST-11, 5.0", capsule shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BC-1155 is a conic with short cylindrical section aft, 1/10 WAC Corporal
         Len 5.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1155</PartNumber>
      <Description>Nose cone, balsa, ST-11, 5.5", 1/10 scale WAC Corporal</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.5</Length>
    </NoseCone>

    <!-- BC-1159 is generic 5:1 ogive.  Len 5.81", shoulder len 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1159 len is given as 5.9" but drawing scales to 5.81" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1159</PartNumber>
      <Description>Nose cone, balsa, ST-11, 5.81", 5:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.81</Length>
    </NoseCone>

    <!-- BC-1180 is generic 8.0" ogive.  Len 8.0", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1180</PartNumber>
      <Description>Nose cone, balsa, ST-11, 8.0", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.170</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.130</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BTC-11SC is a bulbous flared tailcone drilled for 18mm motor tube
         SOURCE ERROR: BTC-115C Semroc legacy site gives length and weight as zero.
         Mass is estimated based on specified weights of other #11 nose cones
         Length and shoulder length scaled from drawing on legacy site.
    -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-11SC [R]</PartNumber>
        <Description>Transition, balsa, BTC-11SC, tailcone, drilled 18mm, PN BTC-11SC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.32</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.170</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.130</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.170</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.130</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">2.875</Length>
    </Transition>


    <!-- ============================================ -->
    <!-- Series 115 nose cones for Series 115 tube    -->
    <!-- ============================================ -->

    <!-- BC-11518 is 1.8" ellipsoid.  Len 1.8", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11518</PartNumber>
      <Description>Nose cone, balsa, LT-115, 1.8", ellipsoid, PN BC-11518</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-11524 is 2.4" ellipsoid (called Bezier).  Len 2.4", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11524</PartNumber>
      <Description>Nose cone, balsa, LT-115, 2.4", ellipsoid, PN BC-11524</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">2.4</Length>
    </NoseCone>

    <!-- BC-11534 is a 3.4" ogive. Len 3.4", shoulder len 0.58" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11534</PartNumber>
      <Description>Nose cone, balsa, LT-115, 3.4", ogive, PN BC-11534</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.58</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-11535 is 3.5" round tip ogive (called Bezier), clone of Centuri BC-115A
         Len 3.5", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11535</PartNumber>
      <Description>Nose cone, balsa, LT-115, 3.5", round tip ogive, Centuri BC-115A, PN BC-11535</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-11544 is a 4.4" ogive.  Len 4.4", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11544</PartNumber>
      <Description>Nose cone, balsa, LT-115, 4.4", ogive, PN BC-11544</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.4</Length>
    </NoseCone>

    <!-- BC-11546 is a 4.6" ogive.  Len 4.6", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11546</PartNumber>
      <Description>Nose cone, balsa, LT-115, 4.6", ogive, PN BC-11546</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BC-11549 is 4.9" round tip ogive, clone of Centuri BC-115B.  Len 4.9", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11549</PartNumber>
      <Description>Nose cone, balsa, LT-115, 4.9", round tip ogive, Centuri BC-115B, PN BC-11549</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BC-11554 is a 5.4" ogive, designated "Nike Herc 13mm".  Unclear how this differs from BC-11554G. 
         Len 5.4", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11554</PartNumber>
      <Description>Nose cone, balsa, LT-115, 5.4", ogive, Nike Herc 13mm, PN BC-11554</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-11554G is a 5.4" ogive.  Len 5.4", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11554G</PartNumber>
      <Description>Nose cone, balsa, LT-115, 5.4", ogive, PN BC-11554G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-11560 is a 6.0" ogive.  Len 6.0", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-11560</PartNumber>
      <Description>Nose cone, balsa, LT-115, 6.0", ogive, PN BC-11560</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.220</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.137</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>


    <!-- =================================== -->
    <!-- BNC-55 nose cones for BT-55 tube    -->
    <!-- =================================== -->

    <!-- SEMROC BNC-55AA is clone of Estes BNC-55A -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55AA</PartNumber>
        <Description>Nose cone, balsa, BNC-55AA, 3.125", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5</ShoulderLength>
        <Length Unit="in">3.125</Length>
    </NoseCone>

    <!-- SEMROC BNC-55AC is clone of Estes BNC-55AC -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55AC</PartNumber>
        <Description>Nose cone, balsa, BNC-55AC, 5.375", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.375</ShoulderLength>
        <Length Unit="in">5.375</Length>
    </NoseCone>

    <!-- SEMROC BNC-55ACP is modified clone of Estes BNC-55AC with pointed nose tip -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55ACP</PartNumber>
        <Description>Nose cone, balsa, BNC-55ACP, 5.375", ogive, pointed tip BNC-55AC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.375</ShoulderLength>
        <Length Unit="in">5.375</Length>
    </NoseCone>

    <!-- SEMROC BNC-55AM is clone of Estes BNC-55AM
         BNC-55AM PN 70280 (#1258 Demon) from Brohm list and Semroc legacy site.  Semroc also carries an upscale as
         BC-27589.
         The actual shape is a rounded-tip cone with a ~0.5" cylindrical section at the aft end.  It's far enough
         away from any OpenRocket shape that we use a mass override.  Shoulder len estimated from Semroc drawing.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55AM</PartNumber>
        <Description>Nose cone, balsa, BNC-55AM, 4.2", rounded cone</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.27</Mass>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">4.2</Length>
    </NoseCone>


    <!-- SEMROC BNC-55AO is clone of Estes BNC-55AO (K-48 Bandit etc.).  OR built in file has typo as "BNC-55AD" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55AO</PartNumber>
        <Description>Nose cone, balsa, BNC-55AO, 5.0", elliptical</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- SEMROC BNC-55B is upscale of Estes BNC-20B.  Shoulder length scaled from BNC-20B 5/16" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55AB</PartNumber>
        <Description>Nose cone, balsa, BNC-55B, 3.0", elliptical</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5625</ShoulderLength>
        <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- SEMROC BNC-55BE is clone of Estes BNC-55BE for #1272 Vostok.  No data for shoulder length, estimated from
         Semroc drawing
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55BE</PartNumber>
        <Description>Nose cone, balsa, BNC-55BE, 1.75", dual conic (Vostok)</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">1.75</Length>
    </NoseCone>

    <!-- SEMROC BNC-55CO is downscale of Centuri PNC-231.  Shoulder len estimated from Semroc drawing -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55CO</PartNumber>
        <Description>Nose cone, balsa, BNC-55CO, 2.2", rounded conical, Centuri PNC-231 downscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- SEMROC BNC-55CT is clone of Centuri PNC-132.  Shape is an ellipsoid from section with a straight conic taper aft.
         Ellipsoid is pretty good shape approximation for mass/CG
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55CT</PartNumber>
        <Description>Nose cone, balsa, BNC-55CT, 2.7", ellipsoid/conic, Centuri PNC-132 shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- SEMROC BNC-55D is clone of Estes PNC-55D ramjet nacelle (Sea Dart).  Shape is approximation, mass override used. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55D</PartNumber>
        <Description>Nose cone, balsa, BNC-55D, 3.7", ramjet nacelle, Sea Dart PNC-55D shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.29</Mass>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.750</ShoulderLength>
        <Length Unit="in">3.75</Length>
    </NoseCone>

    <!-- SEMROC BNC-55D2 is a scale Delta II fairing/cone (ref a 2008 oldrocketforum.com thread) with OD 1.573.
         Shape is a rounded cone, followed by 1.573 OD cylinder, followed by a very short (0.25" or so) decreasing conic
         back down to BT-55 size.  Neither old nor new Semroc web sites identify it as Delta II scale.  Estes has
         never to date mada a Delta II scale kit. You could model the aerodynamics of this correctly with 3 separate
         components.  Here I make it to the actual size, but OR will complain about size discontinuity. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55D2</PartNumber>
        <Description>Nose cone, balsa, BNC-55D2, 3.0", Delta II payload fairing shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.33</Mass>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.573</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.750</ShoulderLength>
        <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- SEMROC BNC-55EX is a near clone of the Estes PNC-55EX (#1955 Ranger, #1925 Exocet).  In the Semroc outline
         drawing the shoulder is considerably shorter than the Estes one so I estimated it at 5/8" long.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55EX</PartNumber>
        <Description>Nose cone, balsa, BNC-55EX, 3.375", ogive, Estes PNC-55EX shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">3.375</Length>
    </NoseCone>

    <!-- SEMROC BNC-55F is a clone of Estes BNC-55F (V-2).  Dimensions from the Estes version. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55F</PartNumber>
        <Description>Nose cone, balsa, BNC-55F, 3.875", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5</ShoulderLength>
        <Length Unit="in">3.875</Length>
    </NoseCone>

    <!-- SEMROC BNC-55FD is a clone of Estes BNC-55F, but drilled from the base for nose weight with a
         0.75" hole (BT-20 size perhaps?) to a depth of 2".  See BC-1340D for similar NC where dill dimensions are specified.
         Other dimensions from the Estes BNC-55F. 
         OpenRocket cannot model the drilled geometry for mass/inertia so I have to use a mass override.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55FD</PartNumber>
        <Description>Nose cone, balsa, BNC-55FD, 3.875", ogive, drilled</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.18</Mass>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5</ShoulderLength>
        <Length Unit="in">3.875</Length>
    </NoseCone>

    <!-- SEMROC BNC-55G3 is a pure 3:1 ogive.  No Estes equivalent. Shoulder length estimated. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55G3</PartNumber>
        <Description>Nose cone, balsa, BNC-55G3, 4.0", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- SEMROC BNC-55G4 is a pure 4:1 ogive.  No Estes equivalent. Shoulder length estimated. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55G4</PartNumber>
        <Description>Nose cone, balsa, BNC-55G4, 5.3", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- SEMROC BNC-55G5 is a pure 5:1 ogive.  No Estes equivalent. Shoulder length estimated. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55G5</PartNumber>
        <Description>Nose cone, balsa, BNC-55G5, 6.6", ogive</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">6.6</Length>
    </NoseCone>

    <!-- SEMROC BNC-55GT is a 1/90 scale Gemini capsule. No Estes equivalent. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55GT</PartNumber>
        <Description>Nose cone, balsa, BNC-55GT, 2.3", Gemini-Titan capsule</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.16</Mass>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.50</ShoulderLength>
        <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- SEMROC BNC-55HJ (Honest John) is equivalent to Estes PNC-55HJ (for which we have no info) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55HJ</PartNumber>
        <Description>Nose cone, balsa, BNC-55HJ, 8.4", flared ogive, Honest John shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.89</Mass>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- SEMROC BNC-55KP is used in the Semroc Der V-1.5, which is a downscale of Estes Der V-3.
         ref Der V-1.5 instructions on Semroc site -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55KP</PartNumber>
        <Description>Nose cone, balsa, BNC-55KP, 4.2", ogive, Estes Der V-3 downscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- SEMROC BNC-55HV is a 2.25:1 Haack/von Karman nose.  No Estes equivalent.
         The legacy Semroc site erroneously lists this as "Haack-Van Karden"
     -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55HV</PartNumber>
        <Description>Nose cone, balsa, BNC-55HV, 3.8", Haack/von Karman</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>HAACK</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">3.8</Length>
    </NoseCone>

    <!-- SEMROC BNC-55LP is a downscale of the Semroc BNC-60LP / Estes PNC-60L -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55LP</PartNumber>
        <Description>Nose cone, balsa, BNC-55LP, 3.0", ellipsoid, Estes PNC-60L downscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.750</ShoulderLength>
        <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- SEMROC BNC-55PT is a complex tri-conic shape.  No Estes equivalent.  Semroc doesn't say what it is,
         there are no clues in Brohm, and there are no other online references.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55PT</PartNumber>
        <Description>Nose cone, balsa, BNC-55PT, 6.2", tri-conic</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.30</Mass>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.2</Length>
    </NoseCone>

    <!-- SEMROC BNC-55SC is Estes #1287 LTV Scout payload fairing shape. This is a composite shape of plastic and tube parts
         used in the kit including 2 sections of PNC-1287 (nose cone and dual transition) and a 1.39" diam tube SBT-139BJ.
         Actual diam is 1.39" though Semroc legacy site doesn't reflect that.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55SC</PartNumber>
        <Description>Nose cone, balsa, BNC-55SC, 7.4", LTV Scout payload fairing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.94</Mass>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">7.4</Length>
    </NoseCone>

    <!-- SEMROC BNC-55SH is ogive for scale Sandhawk (per Semroc legacy site).  May be equivalent to Estes PNC-55EK. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55SH</PartNumber>
        <Description>Nose cone, balsa, BNC-55SH, 3.5", ogive, Sandhawk scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- SEMROC BNC-55V is "V2 scale" ogive 4.3" long. Compare to BNC-55F at 3.8" long.  No Estes equivalent known.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55V</PartNumber>
        <Description>Nose cone, balsa, BNC-55V, 4.3", ogive, V-2 scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">4.3</Length>
    </NoseCone>

    <!-- SEMROC BNC-55VD is "V2 scale" ogive 4.3" long, drilled to about .75" diam x 2" long. No Estes equivalent known.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55VD</PartNumber>
        <Description>Nose cone, balsa, BNC-55V, 4.3", ogive, drilled, V-2 scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.22</Mass>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.625</ShoulderLength>
        <Length Unit="in">4.3</Length>
    </NoseCone>

    <!-- SEMROC BNC-55WC is WAC Corporal scale shape.  It is conical with a short (3/4" or so) cylindrical section at rear. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55WC</PartNumber>
        <Description>Nose cone, balsa, BNC-55WC, 6.2", conical+cylinder, WAC Corporal scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.2</Length>
    </NoseCone>

    <!-- SEMROC BNC-55X is upscale of Estes BNC-50X -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55X</PartNumber>
        <Description>Nose cone, balsa, BNC-55X, 4.1", ellipsoid, BNC-50X upscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- SEMROC BNC-55Y is upscale of Estes BNC-50Y -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-55X</PartNumber>
        <Description>Nose cone, balsa, BNC-55Y, 5.9", ogive, BNC-50Y upscale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">5.9</Length>
    </NoseCone>

    <!-- SEMROC BTC-55VZ is tailcone drilled through for 18mm motor tube.  Estes BTC-55Z had 0.5" shoulder.
         Has smoother taper than BTC-55Z and (per Semroc legacy site) is 0.01 oz lighter than BTC-55Z. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-55VZ [R]</PartNumber>
        <Description>Transition, balsa, BTC-55VZ, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.24</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.0</Length>
    </Transition>

    <!-- SEMROC BTC-55Z is tailcone drilled through for 18mm motor tube.  Equivalent to Estes BTC-55Z.
         Differs from Semroc BTC-55VZ in having "heavy taper" (per new Semroc site) and is slightly heavier than BTC-55VZ.
    -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-55Z [R]</PartNumber>
        <Description>Transition, balsa, BTC-55Z, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.25</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.0</Length>
    </Transition>




    <!-- =================================== -->
    <!-- Series 13 nose cones for ST-13 tube -->
    <!-- =================================== -->

    <!-- BC-1315 is Centuri Orion shape.  Len 1.5", shoulder len 0.58" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1315</PartNumber>
      <Description>Nose cone, balsa, ST-13, 1.5", rounded tip cone, Centuri Orion shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.58</ShoulderLength>
      <Length Unit="in">1.5</Length>
    </NoseCone>

    <!-- BC-1319 is Centuri BC-130.  Len 1.9", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1319</PartNumber>
      <Description>Nose cone, balsa, ST-13, 1.9", ellipsoid, Centuri BC-130</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">1.9</Length>
    </NoseCone>

    <!-- BC-1321 is 2.1" ellipsoid, no cross-compatibility specified.  
         Len 2.1", shoulder len 0.58" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1321</PartNumber>
      <Description>Nose cone, balsa, ST-13, 2.1", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.58</ShoulderLength>
      <Length Unit="in">2.1</Length>
    </NoseCone>

    <!-- BC-1327 is Centuri PNC-132 shape, 2.7" length, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1327</PartNumber>
      <Description>Nose cone, balsa, ST-13, 2.7", ellipsoid, Centuri PNC-132 shape, PN BC-1327</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BC-1327S is same as BC-1327, 2.7" len with shorter 0.48" shoulder (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1327S</PartNumber>
      <Description>Nose cone, balsa, ST-13, 2.7", ellipsoid, short shoulder, Centuri PNC-132 shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.48</ShoulderLength>
      <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BC-1328 is upscale of Centuri BC-101.  Len 2.75", shoulder len 0.69" (scaled dwg) -->
    <!--- SOURCE ERROR: BC-1328 length given as 2.8", but drawing scales to 2.75" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1328</PartNumber>
      <Description>Nose cone, balsa, ST-13, 2.75", ellipsoid, upscale of Centuri BC-101</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.75</Length>
    </NoseCone>

    <!-- BC-1329 is Centuri BC-132.  Len 2.9", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1329</PartNumber>
      <Description>Nose cone, balsa, ST-13, 2.9", ogive, Centuri BC-132</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-1330 is 3.0" generic ogive, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1330</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.0", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-1331 is generic 3.1" elliptical, shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1331</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.1", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-1336 is FSI NC-123 shape, len 3.6", shoulder 0.72" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1336</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.6", ogive, FSI NC-123 shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.72</ShoulderLength>
      <Length Unit="in">3.6</Length>
    </NoseCone>

    <!-- BC-1338 is upscale of Estes BNC-50K, len 3.8", shoulder 0.57" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1338</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.8", ogive, upscale of Estes BNC-50K</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.57</ShoulderLength>
      <Length Unit="in">3.8</Length>
    </NoseCone>

    <!-- BC-1338B is downscale of Centuri BC-225B, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1338B</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.8", ogive, downscale of Centuri BC-225B</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.8</Length>
    </NoseCone>

    <!-- BC-1339 is Centuri BC-135 clone, len 3.9", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1339</PartNumber>
      <Description>Nose cone, balsa, ST-13, 3.9", rounded ogive, Centuri BC-135, PN BC-1339</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-1340 is a 4.0" slightly flared ogive (1.5" diam), shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1340</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.0", flared ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.0</Length>
      <Mass Unit="oz">0.35</Mass>
    </NoseCone>

    <!-- BC-1340D is a BC-1340 drilled with .75" x 2.0" deep for weight (ref Semroc legacy site scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1340D</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.0", flared ogive, drilled 0.75" x 2.0" deep</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.0</Length>
      <Mass Unit="oz">0.32</Mass>
    </NoseCone>

    <!-- BC-1340G is a generic 3:1 ogive, len 4.0", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1340G</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.0", 3:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-1344 is a generic 4.4" long ogive, no cross-compatibility specified.
         Len 4.4", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1344</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.4", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.4</Length>
    </NoseCone>

    <!-- BC-1345 is upscale of Estes BNC-50X elliptical, len 4.5", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1345</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.5", ellipsoid, upscale of Estes BNC-50X</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-1349 is upscale of Centuri PNC-83, len 4.9", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1349</PartNumber>
      <Description>Nose cone, balsa, ST-13, 4.9", ellipsoid, upscale of Centuri PNC-83</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BC-1350 is generic 5.0" ellipsoid, len 5.0", shoulder 0.87" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-1350 appears in the legacy site table http://www.semroc.com/Store/Products/NoseCones.asp
         but does not appear in the All > nose cones > balsa parts product query -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1350</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.0", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.87</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BC-1352 is generic 5.25" ellipsoid, shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1352</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.25", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.25</Length>
    </NoseCone>

    <!-- BC-1353 is 5.3" secant ogive, shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1353</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.3", secant ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BC-1353F is 5.3" elliptical, FSI NC-121 shape, shoulder 0.97" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1353F</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.3", ellipsoid, FSI NC-121 shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.97</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BC-1354 is generic 4:1 ogive, len 5.4", shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1354</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.4", 4:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-1354C 5.4" ellipsoid is upscale of Centuri BC-76, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1354C</PartNumber>
      <Description>Nose cone, balsa, ST-13, 5.4", ellipsoid, upscale of Centuri BC-76</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-1364 6.4" rounded ogive is Centuri PNC-136 clone, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1364</PartNumber>
      <Description>Nose cone, balsa, ST-13, 6.4", rounded tip ogive, Centuri PNC-136 shape, PN BC-1364</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.4</Length>
    </NoseCone>

    <!-- BC-1364A rounded tip ogive is Estes PNC-56A clone (see extensive notes in Estes file about PNC-56's)
         Len 6.4", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1364A</PartNumber>
      <Description>Nose cone, balsa, ST-13, 6.4", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.4</Length>
    </NoseCone>

    <!-- BC-1367 is generic 5:1 ogive, len 6.7", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1367</PartNumber>
      <Description>Nose cone, balsa, ST-13, 6.7", 5:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BC-1371 7.1" ogive is upscale of Centuri BC-89, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1371</PartNumber>
      <Description>Nose cone, balsa, ST-13, 7.1", ogive, upscale of Centuri BC-89</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.300</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">7.1</Length>
    </NoseCone>

    <!-- ================================================ -->
    <!-- Series 125 nose cones for Centuri LT-125 tube    -->
    <!-- ================================================ -->

    <!-- BC-12525 is 2.5" ogive.  Len 2.5", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12525</PartNumber>
      <Description>Nose cone, balsa, LT-125, 2.5", ogive, PN BC-12525</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BC-12536 is 3.6" slightly rounded tip ogive, clone of Centuri BC-125A.
         Len 3.6", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12536</PartNumber>
      <Description>Nose cone, balsa, LT-125, 3.6", ogive, Centuri BC-125A, PN BC-12536</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">3.6</Length>
    </NoseCone>

    <!-- BC-12545 is 4.5" ellipsoid, upscale of Estes BNC-50X.  Len 4.5", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12545</PartNumber>
      <Description>Nose cone, balsa, LT-125, 4.5", ellipsoid, upscale of Estes BNC-50X, PN BC-12545</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-12548 is 4.8" ogive, upscale of Centuri BC-727.  Len 4.8", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12548</PartNumber>
      <Description>Nose cone, balsa, LT-125, 4.8", ogive, upscale of Centuri BC-727, PN BC-12548</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">4.8</Length>
    </NoseCone>

    <!-- BC-12553 is 5.3" rounded ogive (closer to ellipsoid).  Len 5.3", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12553</PartNumber>
      <Description>Nose cone, balsa, LT-125, 5.3", ellipsoid, PN BC-12553</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BC-12555 is 5.5" ogive, clone of Centuri BC-125B.  Len 5.5", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12555</PartNumber>
      <Description>Nose cone, balsa, LT-125, 5.5", ogive, Centuri BC-125B, PN BC-12555</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.5</Length>
    </NoseCone>

    <!-- BC-12558 is 5.8" ogive, upscale of Centuri BC-733. Len 5.8", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12558</PartNumber>
      <Description>Nose cone, balsa, LT-125, 5.8", ogive, Centuri BC-733 upscale, PN BC-12558</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.8</Length>
    </NoseCone>

    <!-- BC-12561 is 6.1" ogive, upscale of Centuri BC-735.  Len 6.1", shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-12561</PartNumber>
      <Description>Nose cone, balsa, LT-125, 6.1", ogive, Centuri BC-735 upscale, PN BC-12561</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BC-125105 is 10.5" flared ogive, upscale of Centuri BC-760.  Len 10.5", shoulder 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-125105</PartNumber>
      <Description>Nose cone, balsa, LT-125, 10.5", flared ogive, Centur BC-760 upscale, PN BC-125105</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.250</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">10.5</Length>
      <Mass Unit="oz">1.08</Mass>
    </NoseCone>


    <!-- =================================== -->
    <!-- BNC-58 nose cones for BT-58 tube    -->
    <!-- =================================== -->

    <!-- BNC-58A8 8.0" ogive described as "Arcas 1/3 scale #8".  See also BNC-58AC for another Arcas variant. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58A8</PartNumber>
      <Description>Nose cone, balsa, BT-58, 8.0", ogive, PN BNC-58A8, Arcas 1/3 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BNC-58AR 8.8" ogive described as "1/4 scale Arcon" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58AR</PartNumber>
      <Description>Nose cone, balsa, BT-58, 8.8", ogive, PN BNC-58AR, Arcon 1/4 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">8.8</Length>
    </NoseCone>

    <!-- BNC-58AB 8.8" ogive described as "Deci-Scale Aerobee Standard" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58AB</PartNumber>
      <Description>Nose cone, balsa, BT-58, 8.8", ogive, PN BNC-58AB, Aerobee standard deci-scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">8.8</Length>
    </NoseCone>

    <!-- BNC-58AC 6.1" secant ogive described as "secant conical...For 1/3 Scale Arcas" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58AC</PartNumber>
      <Description>Nose cone, balsa, BT-58, 6.1", secant ogive, PN BNC-58AC, Arcas 1/3 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BNC-58AF 4.5" conical described as "For 1/10 Scale Astrobee F" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58AF</PartNumber>
      <Description>Nose cone, balsa, BT-58, 4.5", conical, PN BNC-58AF, Astrobee F 1/10 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BNC-58AP 1.2" conical capsule described as "1.2 inch capsule", PN implies Apollo capsule -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58AP</PartNumber>
      <Description>Nose cone, balsa, BT-58, 1.2", conical, PN BNC-58AP, Apollo capsule shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">1.2</Length>
    </NoseCone>

    <!-- BNC-58B 3.5" ellipsoid -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58B</PartNumber>
      <Description>Nose cone, balsa, BT-58, 3.5", ellipsoid, PN BNC-58B</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BNC-58G 2.2" rounded conical capsule described as "upscale of BNC-52G" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58G</PartNumber>
      <Description>Nose cone, balsa, BT-58, 2.2", capsule, PN BNC-58G, upscale of BNC-52G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">2.2</Length>
    </NoseCone>

    <!-- BNC-58G3 is 3:1 5.3" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58G3</PartNumber>
      <Description>Nose cone, balsa, BT-58, 5.3", ogive, PN BNC-58G3, 3:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BNC-58G4 is 4:1 6.9" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58G4</PartNumber>
      <Description>Nose cone, balsa, BT-58, 6.9", ogive, PN BNC-58G4, 4:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BNC-58G5 is 5:1 8.4" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58G4</PartNumber>
      <Description>Nose cone, balsa, BT-58, 8.4", ogive, PN BNC-58G5, 5:1 ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- BNC-58HD is 4.6" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58HD</PartNumber>
      <Description>Nose cone, balsa, BT-58, 4.6", ogive, PN BNC-58HD</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BNC-58MA is 4.0" Mercury Atlas capsule -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58MA</PartNumber>
      <Description>Nose cone, balsa, BT-58, 4.0", Mercury Atlas Capsule, PN BNC-58MA</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">4.0</Length>
      <Mass Unit="oz">0.33</Mass>
    </NoseCone>

    <!-- BNC-58MX is 6.4" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58MX</PartNumber>
      <Description>Nose cone, balsa, BT-58, 6.4", ogive, PN BNC-58MX</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">6.4</Length>
    </NoseCone>

    <!-- BNC-58P is 3.0" secant ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58P</PartNumber>
      <Description>Nose cone, balsa, BT-58, 3.0", secant ogive, PN BNC-58P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BNC-58PD is 3.0" secant ogive, drilled 0.75" x approx 2.0" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58PD</PartNumber>
      <Description>Nose cone, balsa, BT-58, 3.0", secant ogive, drilled, PN BNC-58P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">3.0</Length>
      <Mass Unit="oz">0.24</Mass>
    </NoseCone>

    <!-- BNC-58SS is 3.1" modified ellipsoid, called "Bezier" -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-58SS</PartNumber>
      <Description>Nose cone, balsa, BT-58, 3.1", modified ellipsoid, PN BNC-58SS</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.540</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.495</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BTC-58AY is a 3.9" ogive section 24mm drilled tailcone described as "1/4 scale Arcon"
         Aft OD and shoulder lengths estimated
    -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-58AY [R]</PartNumber>
        <Description>Transition, balsa, BT-58, 3.9", ogive, reducing, PN BTC-58AY, Arcon 1/4 scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.540</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.495</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.400</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.9</Length>
        <Mass Unit="oz">0.33</Mass>
    </Transition>

    <!-- BTC-58MX is a 3.9" ogive section tailcone, drilled for 18mm MMT.  Aft OD and shoulder length estimated from drawing. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-58MX [R]</PartNumber>
        <Description>Transition, balsa, BT-58, 3.9", ogive, reducing, PN BTC-58MX</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.540</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.495</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.300</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.736</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.9</Length>
        <Mass Unit="oz">0.33</Mass>
    </Transition>

    <!-- =============================================== -->
    <!-- Series 150 nose cones for Centuri LT-150 tubes  -->
    <!-- =============================================== -->

    <!-- BC-15044 is 4.4" conical, upscale of Estes BNC-5S, shoulder len 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-15044</PartNumber>
      <Description>Nose cone, balsa, LT-150, 4.4", conical, Estes BNC-5S upscale, PN BC-15044</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.497</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">4.4</Length>
    </NoseCone>

    <!-- BC-15066 is 6.6" ogive, upscale of Estes BNC-5AX, shoulder len 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-15066</PartNumber>
      <Description>Nose cone, balsa, LT-150, 6.6", ogive, Estes BNC-5AX upscale, PN BC-15066</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.497</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">6.6</Length>
    </NoseCone>

    <!-- BC-15070 is 7.0" ogive, upscale of Estes BNC-50Y, shoulder 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-15070</PartNumber>
      <Description>Nose cone, balsa, LT-150, 7.0", ogive, Estes BNC-50Y upscale, PN BC-15070</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.497</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">7.0</Length>
    </NoseCone>

    <!-- BC-15080 is 8.0" ogive, upscale of Centuri BC-845, shoulder len 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-15080</PartNumber>
      <Description>Nose cone, balsa, LT-150, 8.0", round tip ogive, Centuri BC-845 upscale, PN BC-15080</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.497</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BC-15081 is 8.1" ogive, upscale of Centuri PNC-89, shoulder len 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-15081</PartNumber>
      <Description>Nose cone, balsa, LT-150, 8.1", round tip ogive, Centuri PNC-89 upscale, PN BC-15081</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.590</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.497</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">8.1</Length>
    </NoseCone>


    <!-- =============================== -->
    <!-- BNC-60xx nose cones for BT-60   -->
    <!-- =============================== -->
    <!-- shoulder lengths scaled from Semroc drawings when not given by Estes -->

    <!-- BNC-60AB is Gemini capsule, dimensions from Estes, mass from Semroc -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AB</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.625", Gemini Capsule, PN BNC-60AB</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.21</Mass>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.375</ShoulderLength>
        <Length Unit="in">2.625</Length>
    </NoseCone>

    <!-- BNC-60AC is 6.7" secant ogive, determined to be upscale of BNC-55AC. Shoulder length scaled from Semroc drawing
         on legacy site -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AC</PartNumber>
        <Description>Nose cone, balsa, BT-60, 7.25", secant ogive, Estes BNC-55AC upscale, PN BNC-60AC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BNC-60AH is 6.6" elliptical, clone of Estes PNC-60AH (note that length differs from previous Estes BNC-60AH) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AH</PartNumber>
        <Description>Nose cone, balsa, BT-60, 6.6", ellipsoid, Estes PNC-60AH clone, PN BNC-60AH</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.6</Length>
    </NoseCone>

    <!-- BNC-60AHD is 6.6" elliptical, clone of Estes PNC-60AH, drilled 0.75 x 4.0" deep -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AHD</PartNumber>
        <Description>Nose cone, balsa, BT-60, 6.6", ellipsoid, drilled 0.75 x 4.0", Estes PNC-60AH clone, PN BNC-60AHD</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.6</Length>
        <Mass Unit="oz">0.62</Mass>
    </NoseCone>

    <!-- BNC-60AK (Estes K-41/#1241 Mercury Redstone) is Mercury capsule, conical shape is approximate
         Ref 1974 Estes custom parts catalog and K-41 instructions found on JimZ site
         BNC-60AK also appears in Brohm and on the Semroc parts list page for K-41/#1241.
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AK</PartNumber>
        <Description>Nose cone, balsa, BT-60, 3.0", Mercury Capsule, PN BNC-60AK</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.21</Mass>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.375</ShoulderLength>
        <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BNC-60AL (K-43/#1243 Mars Lander).  Dimensions, PN and weight from 1974 parts catalog -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AL</PartNumber>
        <Description>Nose cone, balsa, BT-60, 1.25", Mars Lander, PN BNC-60AL</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.17</Mass>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5</ShoulderLength>
        <Length Unit="in">1.25</Length>
    </NoseCone>

    <!-- BNC-60AM is 5.1" round tip cone+cylinder, upscale of Estes BNC-20AM for Stinger etc. 
         shoulder length scaled from Semroc legacy drawing -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AM</PartNumber>
        <Description>Nose cone, balsa, BT-60, 5.1", round tip cone + cylinder, PN BNC-60AM</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">5.1</Length>
    </NoseCone>

    <!-- BNC-60AO is 7.0" ellipsoid.  Inexact upscale of BNC/PNC-55AO, exact would only be 6.1" long -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AO</PartNumber>
        <Description>Nose cone, balsa, BT-60, 7.0", ellipsoid, PN BNC-60AO</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.875</ShoulderLength>
        <Length Unit="in">7.0</Length>
    </NoseCone>

    <!-- BNC-60AS is 8.3" ogive, upscale of Centuri BC-1052 -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60AS</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.3", ogive, Centuri BC-1052 upscale, PN BNC-60AS</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">8.3</Length>
    </NoseCone>

    <!-- BNC-60BC is a 4.2" ramjet nacelle, upscale of BNC-50BC -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60BC</PartNumber>
        <Description>Nose cone, balsa, BT-60, 4.2", ramjet nacelle, PN BNC-60BC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.6</ShoulderLength>
        <Length Unit="in">4.2</Length>
        <Mass Unit="oz">0.45</Mass>
    </NoseCone>

    <!-- BNC-60C is 0.9" clone of the plastic Camroc nose cone shape (called flat spherical) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60C</PartNumber>
        <Description>Nose cone, balsa, BT-60, 0.9", flat spherical, Estes Camroc NC shape, PN BNC-60C</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.375</ShoulderLength>
        <Length Unit="in">0.9</Length>
    </NoseCone>

    <!-- BNC-60CO is a 2.6" round tip cone, downscale of Centuri PNC-231 -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60CO</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.6", round tip cone, Centuri PNC-231 downscale, PN BNC-60CO</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.68</ShoulderLength>
        <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BNC-60G3 is a generic 4.9" 3:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60G3</PartNumber>
        <Description>Nose cone, balsa, BT-60, 4.9", 3:1 ogive, PN BNC-60G3</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BNC-60G36 is a generic 5.9" 3.6:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60G36</PartNumber>
        <Description>Nose cone, balsa, BT-60, 5.9", 3.6:1 ogive, PN BNC-60G36</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">5.9</Length>
    </NoseCone>

    <!-- BNC-60G4 is a generic 6.6" 4:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60G4</PartNumber>
        <Description>Nose cone, balsa, BT-60, 6.6", 4:1 ogive, PN BNC-60G4</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">6.6</Length>
    </NoseCone>

    <!-- BNC-60G5 is a generic 8.2" 5:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60G5</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.2", 5:1 ogive, PN BNC-60G5</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BNC-60GMR is 3.8" capsule, called "Goony Mercury Redstone" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60GMR</PartNumber>
        <Description>Nose cone, balsa, BT-60, 3.8", goony Mercury Redstone capsule, PN BNC-60GMR</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.57</ShoulderLength>
        <Length Unit="in">3.8</Length>
        <Mass Unit="oz">0.31</Mass>
    </NoseCone>

    <!-- BNC-60GT is 2.8" 1/70 scale Gemini capsule -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60GT</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.8", Gemini capsule 1/70, PN BNC-60GT</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BNC-60K is a 4.6" ogive, upscale of Estes BNC-50K -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60K</PartNumber>
        <Description>Nose cone, balsa, BT-60, 4.6", ogive, Estes BNC-50K upscale, PN BNC-60K</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BNC-60KP is a 4.4" ellipsoid, upscale of Estes PNC-50K (which differs from BNC-50) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60KP</PartNumber>
        <Description>Nose cone, balsa, BT-60, 4.4", ellipsoid, Estes PNC-50K upscale, PN BNC-60KP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">4.4</Length>
    </NoseCone>

    <!-- BNC-60L is a 3.1" ellipsoid (called rounded ogive), clone of Estes BNC-60L -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60L</PartNumber>
        <Description>Nose cone, balsa, BT-60, 3.1", ellipsoid, Estes BNC-60L clone, PN BNC-60L</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BNC-60LP is a 2.7" ellipsoid, clone of Estes PNC-60L (which differs from BNC-60L) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60LP</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.7", ellipsoid, Estes PNC-60L clone, PN BNC-60LP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BNC-60LV is a 3.1" ellipsoid (called Bezier), clone of Vern Estes's original Big Bertha nose cone
         It's a little different shape than BNC-60L, the aft portion is fatter. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60LV</PartNumber>
        <Description>Nose cone, balsa, BT-60, 3.1", ellipsoid, Vern Estes original Big Bertha clone, PN BNC-60LV</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BNC-60MS is a 2.6" ellipsoid.  It is not the same shape as Estes PNC-60MS, which is 3.125" long -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60MS</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.6", ellipsoid, PN BNC-60MS</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">2.7</Length>
    </NoseCone>

    <!-- BNC-60NA is a slightly rounded tip 4.75" ogive, clone of Estes PNC-60NA for D-Region Tomahawk etc, though Semroc
         fails to note this.  Using the 4.75" length derived for the Estes part. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60NA</PartNumber>
        <Description>Nose cone, balsa, BT-60, 4.75", ogive, Estes PNC-60NA clone, PN BNC-60NA</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">4.75</Length>
    </NoseCone>

    <!-- BNC-60NS is 10.0" flared conical, Nike Smoke shape (though Semroc fails to note this) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60NS</PartNumber>
        <Description>Nose cone, balsa, BT-60, 10.0", flared conical, Nike Smoke shape, PN BNC-60NS</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">10.0</Length>
    </NoseCone>

    <!-- BT-60OR IS A 1.6" very round tip conical shape.  *** what is this for? No Brohm listing.  *** -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60OR</PartNumber>
        <Description>Nose cone, balsa, BT-60, 1.6", very round tip conical, PN BNC-60OR</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">1.6</Length>
    </NoseCone>

    <!-- BT-60PE is 8.0" tri-conic Pershing missile shape (though Semroc fails to note that) -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60PE</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.0", tri-conic, Pershing missile shape, PN BNC-60PE</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BT-60PED is 8.0" tri-conic Pershing missile shape (though Semroc fails to note that), drilled 0.75" x 4.0" -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60PED</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.0", tri-conic, drilled, Pershing missile shape, PN BNC-60PED</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.0</Length>
        <Mass Unit="oz">0.50</Mass>
    </NoseCone>

    <!-- BNC-60RL is 8.4" ogive, clone of Estes PNC-60RL shape -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60RL</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.4", ogive, Estes PNC-60RL clone, PN BNC-60RL</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- BNC-60R is a 6.4" conical, upscale of Estes BNC-20R -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60R</PartNumber>
        <Description>Nose cone, balsa, BT-60, 6.4", conical, Estes BNC-20R upscale, PN BNC-60R</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">6.4</Length>
    </NoseCone>

    <!-- BNC-60SM is an 8.0" conical -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60SM</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.0", conical, PN BNC-60SM</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.25</ShoulderLength>
        <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BNC-60SU is a complex capsule shape, incorrectly called "Ogive".  Nothing like this in Brohm. 
         SOURCE ERROR: BNC-60SU is called an ogive on Semroc legacy and 2017 sites ***
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60SU</PartNumber>
        <Description>Nose cone, balsa, BT-60, 6.4", capsule/payload, PN BNC-60SU</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.25</ShoulderLength>
        <Length Unit="in">6.4</Length>
        <Mass Unit="oz">1.03</Mass>
    </NoseCone>

    <!-- BNC-60SV is an 8.1" round tip ogive.  No known Estes equivalent. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60SV</PartNumber>
        <Description>Nose cone, balsa, BT-60, 8.1", round tip ogive, PN BNC-60SV</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">8.1</Length>
    </NoseCone>

    <!-- BNC-60T is a 2.8" capsule shape, clone of Estes BNC-60T which is 2.875" long with 0.5" shoulder per 1974 CPC.
         The Semroc drawing scales to 2.83", which totally fails to resolve the length question.  I adopt the Estes value. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60T</PartNumber>
        <Description>Nose cone, balsa, BT-60, 2.8", Mercury capsule, Estes BNC-60T clone, PN BNC-60T</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.5</ShoulderLength>
        <Length Unit="in">2.875</Length>
        <Mass Unit="oz">0.24</Mass>
    </NoseCone>

    <!-- BNC-60V is 5.25" ogive, V-2 scale shape.  Not a clone; Estes has never had a BT-60 V-2.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60V</PartNumber>
        <Description>Nose cone, balsa, BT-60, 5.2", ogive, V-2 scale, PN BNC-60V</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">5.25</Length>
    </NoseCone>

    <!-- BNC-60V is 5.25" ogive, V-2 scale shape, drilled 0.75" x 3.0" deep.  -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60VD</PartNumber>
        <Description>Nose cone, balsa, BT-60, 5.2", ogive, V-2 scale, drilled 0.75 x 3.0", PN BNC-60VD</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">1.00</ShoulderLength>
        <Length Unit="in">5.25</Length>
        <Mass Unit="oz">0.45</Mass>
    </NoseCone>

    <!-- BNC-60YP is 7.25" ogive, upscale of Estes PNC-50Y -->    
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-60YP</PartNumber>
        <Description>Nose cone, balsa, BT-60, 7.25", ogive, Estes PNC-50Y upscale, PN BNC-60YP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
        <ShoulderLength Unit="in">0.82</ShoulderLength>
        <Length Unit="in">7.25</Length>
    </NoseCone>

    <!-- BTC-60CY is 1.5" conical tailcone, drilled for 24mm MMT.  Aft diameter estimated. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-60CY [R]</PartNumber>
        <Description>Transition, balsa, BT-60, 1.5", conical, reducing, drilled 24mm, PN BTC-60CY</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.23</Mass>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.593</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.100</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">1.5</Length>
    </Transition>

    <!-- BTC-60LZ is a BNC-60L drilled for 18mm motor tube -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-60LZ [R]</PartNumber>
        <Description>Transition, balsa, BT-60, 2.8", ellipsoid, reducing, drilled 18mm BNC-60L, PN BTC-60LZ</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.24</Mass>
        <Shape>ELLIPSOID</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.593</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">2.8</Length>
    </Transition>

    <!-- BTC-60VY is 3.7" ogive, drilled for 24mm motor mount tube -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-60VY [R]</PartNumber>
        <Description>Transition, balsa, BT-60, 3.7", ogive, reducing, drilled 24mm, PN BTC-60VY</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.45</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.593</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.7</Length>
    </Transition>

    <!-- BTC-60VZ is 3.7" ogive, drilled for 18mm motor mount tube -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-60VZ [R]</PartNumber>
        <Description>Transition, balsa, BT-60, 3.7", ogive, reducing, drilled 18mm, PN BTC-60VZ</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.47</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.593</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.7</Length>
    </Transition>



    <!-- ============================================== -->
    <!-- Series 16 nose cones for Centuri ST-16 tubes   -->
    <!-- ============================================== -->
    <!-- shoulder lengths scaled from Semroc legacy site drawings -->

    <!-- BC-1623J is 2.3" very blunt ogive, upscale of Estes BNC-50J, shoulder len 0.77" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1623J</PartNumber>
      <Description>Nose cone, balsa, ST-16, 2.3", blunt ogive, Estes BNC-50J upscale, PN BC-1623J</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.77</ShoulderLength>
      <Length Unit="in">2.3</Length>
    </NoseCone>

    <!-- BC-1625 is 2.5" ellipsoid, clone of Centuri BC-160, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1625</PartNumber>
      <Description>Nose cone, balsa, ST-16, 2.5", ellipsoid, Centuri BC-160, PN BC-1625</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BC-1625P is 2.5" ellipsoid, shape clone of Centuri PNC-160, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1625P</PartNumber>
      <Description>Nose cone, balsa, ST-16, 2.5", ellipsoid, Centuri PNC-160 shape, PN BC-1625P</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BC-1631 is 3.1" ogive, clone of old Semroc NB-808, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1631</PartNumber>
      <Description>Nose cone, balsa, ST-16, 3.1", ogive, old Semroc NB-808 clone, PN BC-1631</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.1</Length>
    </NoseCone>

    <!-- BC-1633 is 3.3" capsule shape, clone of Centuri BC-164, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1633</PartNumber>
      <Description>Nose cone, balsa, ST-16, 3.3", capsule, Centuri BC-164, PN BC-1633</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.3</Length>
      <Mass Unit="oz">0.24</Mass>
    </NoseCone>

    <!-- BC-1634 is 3.4" ogive, clone of Centuri BC-162, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1634</PartNumber>
      <Description>Nose cone, balsa, ST-16, 3.4", ogive, Centuri BC-162, PN BC-1634</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-1636 is 3.6" "rounded ogive", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1636</PartNumber>
      <Description>Nose cone, balsa, ST-16, 3.6", rounded ogive, PN BC-1636</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.6</Length>
    </NoseCone>

    <!-- BC-1646 is 4.6" ogive, upscale of Estes BNC-50K, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1646</PartNumber>
      <Description>Nose cone, balsa, ST-16, 4.6", ogive, Estes BNC-50K upscale, PN BC-1646</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.6</Length>
    </NoseCone>

    <!-- BC-1647 is 4.7" ogive, upscale of Estes BNC-30E, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1647</PartNumber>
      <Description>Nose cone, balsa, ST-16, 4.7", ogive, Estes BNC-30E upscale, PN BC-1647</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.7</Length>
    </NoseCone>

    <!-- BC-1648 is generic 4.8" ogive -->
    <!-- SOURCE ERROR: BC-1648 length is supposed to be 4.8" but legacy drawing scales to 4.6" and superposition
         shows it to be exactly the same drawing as for BC-1646 but with the label changed to BC-1648. This error
         has propagated to the new eRockets site.  Are manufactured BC-1648's actually only 4.6" long? -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1648</PartNumber>
      <Description>Nose cone, balsa, ST-16, 4.8", ogive, PN BC-1648</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.8</Length>
    </NoseCone>

    <!-- BC-1649 is generic 4.9" 3:1 ogive, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1649</PartNumber>
      <Description>Nose cone, balsa, ST-16, 4.9", 3:1 ogive, PN BC-1649</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BC-1650 is 5.0" ogive, clone of old Semroc NB-813, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1650</PartNumber>
      <Description>Nose cone, balsa, ST-16, 5.0", ogive, old Semroc NB-813 clone, PN BC-1650</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BC-1654 is 5.4" ellipsoid, upscale of Estes BNC-50X, shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1654</PartNumber>
      <Description>Nose cone, balsa, ST-16, 5.4", ellipsoid, Estes BNC-50X upscale, PN BC-1654</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-1655 is 5.5" ogive, clone of Centuri PNC-165, shoulder len 0.82" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1655</PartNumber>
      <Description>Nose cone, balsa, ST-16, 5.5", ogive, Centuri PNC-165 shape, PN BC-1655</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.82</ShoulderLength>
      <Length Unit="in">5.5</Length>
    </NoseCone>

    <!-- BC-1660 is 6.0" ogive, clone of FSI NC-152, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1660</PartNumber>
      <Description>Nose cone, balsa, ST-16, 6.0", ogive, FSI NC-152 clone, PN BC-1660</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>

    <!-- BC-1661D is a 6.1 ogive drilled 0.75" x 4.0", Centuri V-2 shape, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1661D</PartNumber>
      <Description>Nose cone, balsa, ST-16, 6.1", ogive, drilled 0.75" x 4.0", Centuri V-2 shape, PN BC-1661</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BC-1666 is generic 6.6" 4:1 ogive, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1666</PartNumber>
      <Description>Nose cone, balsa, ST-16, 6.6", 4:1 ogive, PN BC-1666</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.6</Length>
    </NoseCone>

    <!-- BC-1667 is a 6.75 ellipsoid (called parabolic), shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1667</PartNumber>
      <Description>Nose cone, balsa, ST-16, 6.75", ellipsoid, PN BC-1667</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">6.75</Length>
    </NoseCone>

    <!-- BC-1672 is 7.2" ogive, designated "Nike Herc 18mm" on legacy site only, shoulder len 0.86" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1672</PartNumber>
      <Description>Nose cone, balsa, ST-16, 7.2", ogive, Nike Hercules, PN BC-1672</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.86</ShoulderLength>
      <Length Unit="in">7.2</Length>
    </NoseCone>

    <!-- BC-1665 was described on legacy site as 6.5" ogive, also designated "Nike Herc 18mm" (with a "New!" tag).
         SOURCE ERROR:  BC-1665 Drawing on legacy Semroc site for BC-1665 is titled "BC-1672".  Semroc legacy site NC
         table also gives its length as 7.2".
         This entry has disappeared from the 2017 Semroc site; I think it was very likely bogus, so omitting it.
    -->

    <!-- BC-1674 is a 7.4" ogive, upscale of Estes BNC-50Y.  But legacy scaled dwg len is 7.25", shoulder 0.69" -->
    <!-- SOURCE ERROR: BC-1674 legacy site gives length 7.4", but drawing scales to 7.25" on both legacy and
         new eRockets sites -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1674</PartNumber>
      <Description>Nose cone, balsa, ST-16, 7.4" nominal, ogive, Estes BNC-50Y upscale, PN BC-1674</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">7.25</Length>
    </NoseCone>

    <!-- BC-1682 is generic 8.2" 5:1 ogive, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1682</PartNumber>
      <Description>Nose cone, balsa, ST-16, 8.2", 5:1 ogive, PN BC-1682</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BC-16100 is a 10.0" flared bi-conic, Nike Smoke 1/10 scale shape, shoulder 0.77" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-16100</PartNumber>
      <Description>Nose cone, balsa, ST-16, 10.0", flared conical, Nike Smoke 1/10 scale, PN BC-16100</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.77</ShoulderLength>
      <Length Unit="in">10.0</Length>
    </NoseCone>

    <!-- BC-16103 is a 10.3" flared conical (called ogive), Centuri BC-86 upscale, similar to Honest John shape
         Shoulder length 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-16103</PartNumber>
      <Description>Nose cone, balsa, ST-16, 10.3", flared ogive, Centuri BC-86 upscale, PN BC-16103</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.598</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">10.3</Length>
      <Mass Unit="oz">1.59</Mass>
    </NoseCone>

    <!-- BTC-16V2 is a 4" ogive tailcone, V-2 scale shape, drilled for 18mm motor tube. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-16V2 [R]</PartNumber>
        <Description>Transition, balsa, ST-16, 4.0", ogive, reducing, drilled 18mm, V-2 scale, PN BTC-16V2</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.30</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.640</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.598</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.14</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.736</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">4.0</Length>
    </Transition>


    <!-- ================================ -->
    <!-- BC-16836 "Special" size 1.75" OD -->
    <!-- ================================ -->
    <!-- BNC-16838 "Special" is ellipsoid with len 3.165", 1.75" OD, 1.688" shoulder dia, and appears only on the new
         eRockets/semroc website.  It matches no known tube size. Shoulder diameter is an estimate from an
         oblique photo, no mfg drawing is given. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-16836</PartNumber>
      <Description>Nose cone, balsa, special 1.75" OD, 3.165", ellipsoid, PN BC-16836</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.750</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.688</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.165</Length>
    </NoseCone>


    <!-- ====================================== -->
    <!-- BNC-65xx nose cones for Estes PST-65   -->
    <!-- ====================================== -->
    <!-- shoulder lengths scaled from Semroc legacy site drawings -->

    <!-- BNC-65AF is 4.0" ellipsoid -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-65AF</PartNumber>
      <Description>Nose cone, balsa, PST-65, 4.0", ellipsoid, PN BNC-65AF</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.796</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BNC-65L is 3.4" ellipsoid -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-65L</PartNumber>
      <Description>Nose cone, balsa, PST-65, 3.4", ellipsoid, PN BNC-65L</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.796</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">0.60</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- ================================ -->
    <!-- Series 18 nose cones for ST-18   -->
    <!-- ================================ -->
    <!-- shoulder lengths scaled from Semroc legacy site drawings -->

    <!-- BC-1828 is 2.8" ellipsoid, shoulder 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1828</PartNumber>
      <Description>Nose cone, balsa, ST-18, 2.8", ellipsoid, PN BC-1828</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.8</Length>
    </NoseCone>

    <!-- BC-1828D is 2.8" ellipsoid, drilled 0.75" x 2.0", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1828D</PartNumber>
      <Description>Nose cone, balsa, ST-18, 2.8", ellipsoid, drilled 0.75" x 2.0", PN BC-1828D</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.8</Length>
      <Mass Unit="oz">0.37</Mass>
    </NoseCone>

    <!-- BC-1829D is an unusual 2.9" bi-ellipsoid, drilled 0.75" x 2.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1829D</PartNumber>
      <Description>Nose cone, balsa, ST-18, 2.9", bi-ellipsoid, drilled 0.75" x 2.5", PN BC-1829D</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-1834 is a 3.4" rounded conical, Estes Cineroc nose shape, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1834</PartNumber>
      <Description>Nose cone, balsa, ST-18, 3.4", round tip cone, Estes Cineroc shape, PN BC-1834</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.4</Length>
      <Mass Unit="oz">0.41</Mass>
    </NoseCone>

    <!-- BC-1835 is generic 3.5" ellipsoid, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1835</PartNumber>
      <Description>Nose cone, balsa, ST-18, 3.5", ellipsoid, PN BC-1835</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-1837 is a 3.7" irregular ellipsoid, upscale of Centuri PNC-132, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1837</PartNumber>
      <Description>Nose cone, balsa, ST-18, 3.7", ellipsoid, Centuri PNC-132 upscale, PN BC-1837</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.7</Length>
    </NoseCone>

    <!-- BC-1840 is a 4.0" round tip ogive, shoulder len 0.76" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1840</PartNumber>
      <Description>Nose cone, balsa, ST-18, 4.0", round tip ogive, PN BC-1840</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.76</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-1842 is a 4.2" ellipsoid (called ogive), upscale of Estes BNC-20B, shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1842</PartNumber>
      <Description>Nose cone, balsa, ST-18, 4.2", ellipsoid, Estes BNC-20B upscale, PN BC-1842</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BC-1845 is 4.5" fat ogive, shoulder len 0.76" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1845</PartNumber>
      <Description>Nose cone, balsa, ST-18, 4.5", fat ogive, PN BC-1845</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.76</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-1848 is a 4.8" round tip fat ogive, shoulder len 0.75" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1848</PartNumber>
      <Description>Nose cone, balsa, ST-18, 4.8", round tip ogive, PN BC-1848</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">4.8</Length>
    </NoseCone>

    <!-- BC-1853 is a 5.3" secant ogive, upscale of Estes BNC-55F, shoulder len 0.67" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1853</PartNumber>
      <Description>Nose cone, balsa, ST-18, 5.3", secant ogive, Estes BNC-55F upscale, PN BC-1853</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.67</ShoulderLength>
      <Length Unit="in">5.3</Length>
    </NoseCone>

    <!-- BC-1856 is a generic 5.6" 3:1 ogive, shoulder len 0.67" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1856</PartNumber>
      <Description>Nose cone, balsa, ST-18, 5.6", 3:1 ogive, PN BC-1856</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.67</ShoulderLength>
      <Length Unit="in">5.6</Length>
    </NoseCone>

    <!-- BC-1859 is a 5.9" ogive, V-2 1/35 scale, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1859</PartNumber>
      <Description>Nose cone, balsa, ST-18, 5.9", ogive, V-2 1/35 scale, PN BC-1859</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">5.9</Length>
    </NoseCone>

    <!-- BC-1859D is 5.9" ogive, V-2 1/35 scale, drilled 0.74" x 4.0", shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1859D</PartNumber>
      <Description>Nose cone, balsa, ST-18, 5.9", ogive, V-2 1/35 scale, drilled 0.74" x 4.0", PN BC-1859D</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">5.9</Length>
      <Mass Unit="oz">0.61</Mass>
    </NoseCone>

    <!-- BC-1861 is 6.1" ellipsoid, upscale of Estes BNC-56X, shoulder len 0.97" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1861</PartNumber>
      <Description>Nose cone, balsa, ST-18, 6.1", ellipsoid, Estes BNC-50X upscale, PN BC-1861</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.97</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BC-1869 is a 6.9" ogive, upscale of Estes BNC-20N, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1869</PartNumber>
      <Description>Nose cone, balsa, ST-18, 6.9", ogive, Estes BNC-20N upscale, PN BC-1869</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BC-1869C is a 6.9" conical, upscale of Estes BNC-20R, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1869C</PartNumber>
      <Description>Nose cone, balsa, ST-18, 6.9", conical, Estes BNC-20R upscale, PN BC-1869C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BC-1874 is generic 7.4" 4:1 ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1874</PartNumber>
      <Description>Nose cone, balsa, ST-18, 7.4", 4:1 ogive, PN BC-1874</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">7.4</Length>
    </NoseCone>

    <!-- BC-1892 is generic 9.2" 5:1 ogive, shoulder len 0.67" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-1892</PartNumber>
      <Description>Nose cone, balsa, ST-18, 9.2", 5:1 ogive, PN BC-1892</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.798</ShoulderDiameter>
      <ShoulderLength Unit="in">0.67</ShoulderLength>
      <Length Unit="in">9.2</Length>
    </NoseCone>

    <!-- BTC-18VY is 4.1" ogive tailcone, V-2 1/35 scale, drilled 24mm, shoulder len 0.57" (scaled dwg) -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-18VY [R]</PartNumber>
        <Description>Transition, balsa, ST-18, 4.1", ogive, reducing, drilled 24mm, V-2 1/35 scale, PN BTC-18VY</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.21</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">1.840</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.798</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.11</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.976</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">4.1</Length>
    </Transition>


    <!-- =============================================== -->
    <!-- Series 175 nose cones for Centuri LT-175 tubes  -->
    <!-- =============================================== -->
    <!-- ALL BC-175 nose cones are shown as discontinued on the 2018 eRockets/Semroc website. -->


    <!-- BC-17535 is 3.5" ogive, upscale of Estes BNC-60L, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17535</PartNumber>
      <Description>Nose cone, balsa, LT-175, 3.5", ogive, Estes BNC-60L upscale, PN BC-17535</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BC-17541 is 4.1" ogive, clone of Centuri BC-175A, shoulder len 1.08" (scaled dwg). -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17541</PartNumber>
      <Description>Nose cone, balsa, LT-175, 4.1", ogive, Centuri BC-175A, PN BC-17541</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-17560 is 6.0" ogive, shoulder len 0.98" (scaled dwg).  -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17560</PartNumber>
      <Description>Nose cone, balsa, LT-175, 6.0", ogive, PN BC-17560</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.07</ShoulderLength>
      <Length Unit="in">6.0</Length>
    </NoseCone>

    <!-- BC-17561 is 6.1" ellipsoid, upscale of Estes BNC-50X, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17561</PartNumber>
      <Description>Nose cone, balsa, LT-175, 6.1", ellipsoid, Estes BNC-50X upscale, PN BC-17561</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BC-17567 is 6.7" ogive, clone of Centuri BC-175B, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17567</PartNumber>
      <Description>Nose cone, balsa, LT-175, 6.7", ogive, Centuri BC-175B, PN BC-17567</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BC-17581 is 8.1" ogive, upscale of Estes PNC-50Y, shoulder len 0.88" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17581</PartNumber>
      <Description>Nose cone, balsa, LT-175, 8.1", ogive, Estes PNC-50Y upscale, PN BC-17581</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">0.88</ShoulderLength>
      <Length Unit="in">8.1</Length>
    </NoseCone>

    <!-- BC-17582 is 8.2" ogive-conical, clone of Centuri BC-175C, shoulder len 1.08" (scaled dwg).
         The scaled dwg comes to about 8.1" long but I'm not flagging that as an error. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17582</PartNumber>
      <Description>Nose cone, balsa, LT-175, 8.2", conic+ogive, Centuri BC-175C, PN BC-17582</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BC-17585 is 8.5" ogive, shoulder len 0.98" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17585</PartNumber>
      <Description>Nose cone, balsa, LT-175, 8.5", ogive, PN BC-17585</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">0.98</ShoulderLength>
      <Length Unit="in">8.5</Length>
    </NoseCone>

    <!-- BC-17590 is 9.0" conical, upscale of old Semroc NB-413, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17590</PartNumber>
      <Description>Nose cone, balsa, LT-175, 9.0", conical, old Semroc NB-413 upscale, PN BC-17590</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.0</Length>
    </NoseCone>

    <!-- BC-17592 is 9.2" ogive, upscale of Centuri BC-107, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BC-17592</PartNumber>
      <Description>Nose cone, balsa, LT-175, 9.2", ogive, Centuri BC-107 upscale, PN BC-17592</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">1.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.747</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.2</Length>
    </NoseCone>


    <!-- ============================================== -->
    <!-- Series 20 nose cones for Centuri ST-20 tubes   -->
    <!-- ============================================== -->
    <!-- shoulder lengths scaled from drawaings on Semroc legacy site -->

    <!-- BC-2019 is a 1.9" capsule, Orion 1/100 scale.  The Semroc part has a stub shoulder
         on top 0.345" dia x 0.48" long that is much too long for scale, and is not included in the
         quoted length of the capsule. Shoulder length is 0.68" (scaled dwg).
         The shape of the Semroc part is not correct as the NASA Orion capsule is now a pure cone 
         like Apollo was.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2019</PartNumber>
      <Description>Nose cone, balsa, ST-20, 1.9", capsule, Orion 1/100 scale, PN BC-2019</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">1.9</Length>
      <Mass Unit="oz">0.30</Mass>
    </NoseCone>

    <!-- BC-2025 is a 2.5" ellipsoid, Centuri BC-200 clone, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2025</PartNumber>
      <Description>Nose cone, balsa, ST-20, 2.5", ellipsoid, Centuri BC-200, PN BC-2025</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BC-2026 is a 2.6" modified ellipsoid, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2026</PartNumber>
      <Description>Nose cone, balsa, ST-20, 2.6", modified ellipsoid, PN BC-2026</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">2.6</Length>
    </NoseCone>

    <!-- BC-2031 is a 3.2" round tip cone, Centuri Orion clone.  The PN makes it look like a source error, except
         that BC-2032 is another different round tip cone style.  Actual len 3.2", shoulder len 0.77" (scaled dwg)
    -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2031</PartNumber>
      <Description>Nose cone, balsa, ST-20, 3.2", round tip cone, Centuri Orion clone, PN BC-2031</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.77</ShoulderLength>
      <Length Unit="in">3.2</Length>
    </NoseCone>

    <!-- BC-2032 is a 3.2" nominal (very) round tip cone, clone of Centuri PNC-231.  Actual len 3.33", shoulder len
         0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2032</PartNumber>
      <Description>Nose cone, balsa, ST-20, 3.3", round tip cone, Centuri PNC-231 shape, PN BC-2032</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.33</Length>
    </NoseCone>

    <!-- BC-2033 is a 3.3" ellipsoid, upscale of Semroc (NOT Estes) BNC-60MS, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2033</PartNumber>
      <Description>Nose cone, balsa, ST-20, 3.3", ellipsoid, Semroc BNC-60MS upscale, PN BC-2033</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.3</Length>
    </NoseCone>

    <!-- BC-2034CR is a 3.4" Crayon nose (truncated cone with cylinder aft).  The cylindrical part is about 0.78" long.
         Modeled as ellipsoid to get the mass right.  Actual drag will be quite a bit higher. Shoulder len
         0.89" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2034CR</PartNumber>
      <Description>Nose cone, balsa, ST-20, 3.4", Crayon nose, PN BC-2034CR</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.89</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-2039 si a 3.9" round tip ogive (called Bezier), FSI NC-192 clone, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2039</PartNumber>
      <Description>Nose cone, balsa, ST-20, 3.9", round tip ogive, FSI NC-192 clone, PN BC-2039</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-2045 is a 4.5" ogive, clone of Centuri BC-204, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2045</PartNumber>
      <Description>Nose cone, balsa, ST-20, 4.5", ogive, Centuri BC-204, PN BC-2045</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-2047 is a 4.7" ellipsoid, upscale of Estes BNC-20B, shoulder len 0.78" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2047</PartNumber>
      <Description>Nose cone, balsa, ST-20, 4.7", ellipsoid, Estes BNC-20B upscale, PN BC-2047</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.78</ShoulderLength>
      <Length Unit="in">4.7</Length>
    </NoseCone>

    <!-- BC-2050 is a 5.0" conical, clone of FSI NC-193, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2050</PartNumber>
      <Description>Nose cone, balsa, ST-20, 5.0", conical, FSI NC-193 clone, PN BC-2050</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BC-2057 is a generic 5.7" ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2057</PartNumber>
      <Description>Nose cone, balsa, ST-20, 5.7", ogive, PN BC-2057</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">5.7</Length>
    </NoseCone>

    <!-- BC-2061 is a generic 6.1" 3:1 ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2061</PartNumber>
      <Description>Nose cone, balsa, ST-20, 6.1", 3:1 ogive, PN BC-2061</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">6.1</Length>
    </NoseCone>

    <!-- BC-2065 is a generic 6.5" ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2065</PartNumber>
      <Description>Nose cone, balsa, ST-20, 6.5", ogive, PN BC-2065</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">6.5</Length>
    </NoseCone>

    <!-- BC-2080 is a generic 8.0" ellipsoid, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2080</PartNumber>
      <Description>Nose cone, balsa, ST-20, 8.0", ellipsoid, PN BC-2080</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">8.0</Length>
    </NoseCone>

    <!-- BC-2082 is a generic 8.2" 4:1 ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2082</PartNumber>
      <Description>Nose cone, balsa, ST-20, 8.2", 4:1 ogive, PN BC-2082</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BC-2090 is a 9.0" fat ogive (called Bezier), Estes BNC-50Y upscale, shoulder len 0.84" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-2090</PartNumber>
      <Description>Nose cone, balsa, ST-20, 9.0", ogive, Estes BNC-50Y upscale, PN BC-2090</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.84</ShoulderLength>
      <Length Unit="in">9.0</Length>
    </NoseCone>

    <!-- BC-20102 is a generic 10.2" 5:1 ogive, shoulder len 0.68" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20102</PartNumber>
      <Description>Nose cone, balsa, ST-20, 10.2", 5:1 ogive, PN BC-20102</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.68</ShoulderLength>
      <Length Unit="in">10.2</Length>
    </NoseCone>

    <!-- BC-20104 is a 10.4" ogive, Arcon 1/3 scale, shoulder len 0.875" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20104</PartNumber>
      <Description>Nose cone, balsa, ST-20, 10.4", ogive, Arcon 1/3 scale, PN BC-20104</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.875</ShoulderLength>
      <Length Unit="in">10.4</Length>
    </NoseCone>

    <!-- BC-20107 is a 10.7" conical, FSI NC-191 clone, shoulder len 0.875" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20107</PartNumber>
      <Description>Nose cone, balsa, ST-20, 10.7", conical, FSI NC-191 clone, PN BC-20107</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.040</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.875</ShoulderLength>
      <Length Unit="in">10.7</Length>
    </NoseCone>

    <!-- BTC-20BB is a 3.0" complex nozzle section drilled for 29mm motor tube, shoulder len 1.08" (scaled dwg) -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-20BB [R]</PartNumber>
        <Description>Transition, balsa, ST-20, 3.0", complex nozzle, reducing, drilled 29mm, PN BTC-20BB</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.21</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.040</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.998</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">1.08</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.48</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.14</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">3.0</Length>
    </Transition>


    <!-- ============================================= -->
    <!-- Series 200 nose cones for Centuri LT-200 tube -->
    <!-- ============================================= -->

    <!-- BC-20040 is a 4.0" ellipsoid, shoulder 0.78" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20040</PartNumber>
      <Description>Nose cone, balsa, LT-200, 4.0", ellipsoid, PN BC-20040</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.080</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">0.78</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-20045 is 4.5" elliptical, Coaster Centauri shape, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20045</PartNumber>
      <Description>Nose cone, balsa, LT-200, 4.5", ellipsoid, Coaster Centauri shape, PN BC-20045</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.080</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-20090 is 9.0" rounded tip ogive (called bezier), upscale of Centuri PNC-106,
         shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20090</PartNumber>
      <Description>Nose cone, balsa, LT-200, 9.0", ogive, Centuri PNC-106 upscale, PN BC-20090</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.080</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.0</Length>
    </NoseCone>

    <!-- BC-20099 is 9.9" rounded tip ogive (called bezier), shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-20099</PartNumber>
      <Description>Nose cone, balsa, LT-200, 9.9", round tip ogive, PN BC-20099</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.080</OutsideDiameter>
      <ShoulderDiameter Unit="in">1.998</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.9</Length>
    </NoseCone>


    <!-- ============================== -->
    <!-- BNC-70xx (BT-70) nose cones    -->
    <!-- ============================== -->
    <!-- shoulder lengths estimated except when Estes dimension exists -->

    <!-- BNC-70AJ is 4.25" ogive, clone of Estes BNC-70AJ -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70AJ</PartNumber>
        <Description>Nose cone, balsa, BT-70, 4.25" ogive, Estes BNC-70AJ clone, PN BNC-70AJ</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">4.25</Length>
    </NoseCone>

    <!-- BNC-70AO is 8.4" ellipsoid, upscale of Estes BNC-55AO -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70AO</PartNumber>
        <Description>Nose cone, balsa, BT-70, 8.4" ellipsoid, upscale of Estes BNC-55AO, PN BNC-70AO</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- BNC-70AP is 1.7" conicala, Apollo capsule shape -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70AP</PartNumber>
        <Description>Nose cone, balsa, BT-70, 1.7" conical, Apollo capsule, PN BNC-70AP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">1.7</Length>
    </NoseCone>

    <!-- BNC-70B is 7.0" ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70B</PartNumber>
        <Description>Nose cone, balsa, BT-70, 7.0" ogive, PN BNC-70B</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.0</Length>
    </NoseCone>

    <!-- BNC-70C is 2.5" rounded tip cone -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70C</PartNumber>
        <Description>Nose cone, balsa, BT-70, 2.5" round tip cone, PN BNC-70C</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BNC-70CT is a dual shape with the front 2/3 being an ellipsoid and the last 1/3 being a shallow conic -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70CT</PartNumber>
        <Description>Nose cone, balsa, BT-70, 4.5" ellipsoid+conic, PN BNC-70CT</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BNC-70CP is 11.1" slightly round tip ogive, upscale of BC-845P -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70CP</PartNumber>
        <Description>Nose cone, balsa, BT-70, 11.1" round tip ogive, upscale of BC-845P, PN BNC-70CP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">11.1</Length>
    </NoseCone>

    <!-- BNC-70D is 5.0" ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70D</PartNumber>
        <Description>Nose cone, balsa, BT-70, 5.0" ogive, PN BNC-70D</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BNC-70G3 is 6.7" 3:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70G3</PartNumber>
        <Description>Nose cone, balsa, BT-70, 6.7" 3:1 ogive, PN BNC-70G3</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BNC-70G4 is 8.9" 4:1 ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70G4</PartNumber>
        <Description>Nose cone, balsa, BT-70, 8.9" 4:1 ogive, PN BNC-70G4</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.9</Length>
    </NoseCone>

    <!-- BNC-70MS is 3.5" ellipsoid, upscale of BNC-60MS -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70MS</PartNumber>
        <Description>Nose cone, balsa, BT-70, 3.5" ellipsoid, PN BNC-70MS</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">3.5</Length>
    </NoseCone>

    <!-- BNC-70NH is 9.8" ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70NH</PartNumber>
        <Description>Nose cone, balsa, BT-70, 9.8" ogive, PN BNC-70NH</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">9.8</Length>
    </NoseCone>

    <!-- BNC-70PE is 11.6 tri-conic Pershing missile shape (erroneously called ogive)
         Only on Semroc legacy site.
         SOURCE ERROR: BNC-70PE incorrectly called "ogive" on both old and new sites, not identified as Pershing shape ***
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70PE</PartNumber>
        <Description>Nose cone, balsa, BT-70, 11.6" tri-conic, Pershing missile shape, PN BNC-70PE</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">11.6</Length>
    </NoseCone>

    <!-- BNC-70PED is drilled version of BNC-70PE.  Drilling dimensions not given
         but drawing on legacy site makes it look about 0.75" x 6" deep.  Mass override used since it's 0.15 oz lighter. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70PED</PartNumber>
        <Description>Nose cone, balsa, BT-70, 11.6" tri-conic, Pershing missile shape, drilled, PN BNC-70PED</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">11.6</Length>
        <Mass Unit="oz">1.15</Mass>
    </NoseCone>

    <!-- BNC-70TT is 7.2" ogive, says "Tin-Tin" on legacy site. Drawing on legacy site shows a noticeably short shoulder. -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70TT</PartNumber>
        <Description>Nose cone, balsa, BT-70, 7.2" ogive, Tin-Tin shape, PN BNC-70TT</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">0.75</ShoulderLength>
        <Length Unit="in">7.2</Length>
    </NoseCone>

    <!-- BNC-70V is 7.1" ogive, V-2 scale shape -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70V</PartNumber>
        <Description>Nose cone, balsa, BT-70, 7.1" ogive, V-2 scale, PN BNC-70V</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.1</Length>
    </NoseCone>

    <!-- BNC-70VD is BNC-70V (7.1" ogive) drilled 0.75 x 4" for nose weight -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70VD</PartNumber>
        <Description>Nose cone, balsa, BT-70, 7.1" ogive, drilled, PN BNC-70VD, V-2 scale</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.1</Length>
        <Mass Unit="oz">1.05</Mass>
    </NoseCone>

    <!-- BNC-70X is 7.3" elliptical, probable upscale of BNC-50X, length checks out 
         SOURCE ERROR: BNC-70X neither new nor old Semroc sites mention it's a BNC-50X upscale ***
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70X</PartNumber>
        <Description>Nose cone, balsa, BT-70, 7.3" ellipsoid, PN BNC-70X, upscale of Estes BNC-50X</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.3</Length>
    </NoseCone>


    <!-- =========================================================== -->
    <!-- BNC-70Hxx (BT-70H) nose cones for heavy wall BTH-70 tubes   -->
    <!-- =========================================================== -->
    <!-- shoulder lengths estimated -->
    <!-- SOURCE ERROR: BNC-70 and BHC-70Hxx are not separated on new Semroc site, but BNC-80 and BNC-80Hxx are -->

    <!-- BNC-70HAC is 9.2" secant ogive, upscale of Estes BNC-55AC -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HAC</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 9.2" secant ogive, PN BNC-70HAC, upscale of Estes BNC-55AC</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">9.2</Length>
    </NoseCone>

    <!-- BNC-70HAJ is 4.25" ogive, Estes BNC-70AJ shape -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HAJ</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 4.25" ogive, PN BNC-70HAJ, Estes BNC-70AJ shape</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">4.25</Length>
    </NoseCone>

    <!-- BNC-70HAO is 8.4" ellipsoid, upscale of Estes BNC-55AO -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HAO</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 8.4" ellipsoid, PN BNC-70HAO, upscale of Estes BNC-55AO</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- BNC-70HB is 7.0" ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HB</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 7.0" ogive, PN BNC-70HB</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.0</Length>
    </NoseCone>

    <!-- BNC-70HCT is a dual shape with the front 2/3 being an ellipsoid and the last 1/3 being a shallow conic
    -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HCT</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 4.5" ellipsoid+conic, PN BNC-70HCT</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BNC-70HD is 5.0" ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HD</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 5.0" ogive, PN BNC-70HD</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BNC-70HP is a 7.5" rounded ogive -->
    <NoseCone>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BNC-70HP</PartNumber>
        <Description>Nose cone, balsa, BTH-70, 7.5" rounded ogive, PN BNC-70HP</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.247</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
        <ShoulderLength Unit="in">1.0</ShoulderLength>
        <Length Unit="in">7.5</Length>
    </NoseCone>

    <!-- BTC-70HY is a straight conic tailcone drilled for 24mm MMT, length 1.75", shoulder 0.57"
         mass from legacy site. -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-70HY [R]</PartNumber>
        <Description>Transition, balsa, BT-70, 1.75 in, conical, reducing, PN BTC-70HY</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.47</Mass>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.247</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.57</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.300</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">1.75</Length>
    </Transition>


    <!-- BTC-70VY is a 5.0" ogive tailcone drilled for 24mm MMT, mass from legacy site -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-70VY [R]</PartNumber>
        <Description>Transition, balsa, BT-70, 5.0", ogive, reducing, PN BTC-70VY</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">0.80</Mass>
        <Shape>OGIVE</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.247</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.300</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">5.0</Length>
    </Transition>


    <!-- ================================================ -->
    <!-- Series 225 nose cones for Centuri LT-225 tubes   -->
    <!-- ================================================ -->
    <!-- ALL BC-225 nose cones are shown as discontinued on the 2018 eRockets/Semroc website -->
    <!-- Shoulder lengths scaled from drawings on Semroc legacy site -->

    <!-- BC-22530 shape called "bezier", weight spec 0.74 oz, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22530</PartNumber>
      <Description>Nose cone, balsa, LT-225, 3.0", ellipsoid, FSI NC-22x, PN BC-22530</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">3.0</Length>
    </NoseCone>

    <!-- BC-22545 is 4.5" ellipsoid, clone of Centuri BC-225A, weight spec 1.2 oz, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22545</PartNumber>
      <Description>Nose cone, balsa, LT-225, 4.5", ellipsoid, Centuri BC-225A, PN BC-22545</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">4.5</Length>
    </NoseCone>

    <!-- BC-22548 shape called "bezier", weight spec 1.13 oz, len 4.75", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22548</PartNumber>
      <Description>Nose cone, balsa, LT-225, 4.75", ellipsoid, Centuri PNC-132 upscale, PN BC-22548</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">4.75</Length>
    </NoseCone>

    <!-- BC-22551 weight spec 1.16 oz, len 5.1", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22551</PartNumber>
      <Description>Nose cone, balsa, LT-225, 5.1", ogive, Centuri BC-132 upscale, PN BC-22551</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">5.1</Length>
    </NoseCone>

    <!-- BC-22563 shape called "bezier", rounded point ogive, weight spec 1.34 oz, len 6.25",
         shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22563</PartNumber>
      <Description>Nose cone, balsa, LT-225, 6.25", rounded tip ogive, FSI NC-225, PN BC-22563</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.25</Length>
    </NoseCone>

    <!-- BC-22567  is 6.7" ellipsoid, Centuri BC-225B clone, weight spec 1.41 oz, len 6.7", 
         shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22567</PartNumber>
      <Description>Nose cone, balsa, LT-225, 6.7", ellipsoid, Centuri BC-225B, PN BC-22567</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BC-22567E weight spec 1.41 oz, upscale of Estes BNC-30E, len 6.7", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22567E</PartNumber>
      <Description>Nose cone, balsa, LT-225, 6.7", ogive, BNC-30E upscale, PN BC-22567E</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.7</Length>
    </NoseCone>

    <!-- BC-22569 weight spec 1.53 oz, len 6.9", shoudler len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22569</PartNumber>
      <Description>Nose cone, balsa, LT-225, 6.9", ogive, PN BC-22569</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BC-22578 weight spec 1.66 oz, upscale of Estes BNC-50X, length 7.8", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22578</PartNumber>
      <Description>Nose cone, balsa, LT-225, 7.8", ogive, Estes BNC-50X upscale, PN BC-22578</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">7.8</Length>
    </NoseCone>

    <!-- BC-22579 weight spec 1.64 oz, upscale of Centuri PNC-165, len 7.9", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22579</PartNumber>
      <Description>Nose cone, balsa, LT-225, 7.9", ogive, Centuri PNC-165 upscale, PN BC-22579</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">7.9</Length>
    </NoseCone>

    <!-- BC-22588 shape (Estes BNC-55AO upscale) is closer to ogive, weight spec 1.82 oz.
         Length 8.94", shoulder len 1.08" (scaled dwg)  -->
    <!-- SOURCE ERROR: BC-22588 length given as 8.8", but mfg drawing length scales to 8.94" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22588</PartNumber>
      <Description>Nose cone, balsa, LT-225, 8.94", ellipsoid, Estes BNC-55AO upscale, PN BC-22588</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">8.94</Length>
    </NoseCone>


    <!-- BC-22595 is Estes BNC-55AC upscale, weight spec 1.52 oz, len 9.5", shoulder len 1.08" (scaled dwg).
         Shoulder is very much not to scale, original BNC-55AC only had 0.375" shoulder  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22595</PartNumber>
      <Description>Nose cone, balsa, LT-225, 9.5", ogive, Estes BNC-55AC upscale, PN BC-22595</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.5</Length>
    </NoseCone>

    <!-- BC-22597 is Estes BNC-5AX upscale, weight spec 1.73 oz, len 9.7", shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-22597</PartNumber>
      <Description>Nose cone, balsa, LT-225, 9.7", ogive, Estes BNC-5AX upscale, PN BC-22597</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">9.7</Length>
    </NoseCone>

    <!-- BC-225103 weight spec 2.16 oz, shoulder len 1.08" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-225103</PartNumber>
      <Description>Nose cone, balsa, LT-225, 10.3", ogive, BNC-50Y upscale, PN BC-225103</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.340</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.248</ShoulderDiameter>
      <ShoulderLength Unit="in">1.08</ShoulderLength>
      <Length Unit="in">10.3</Length>
    </NoseCone>
    
    <!-- ================================= -->
    <!-- BNC-80 nose cones for BT-80 tubes -->
    <!-- ================================= -->
    <!-- shoulder lengths all estimated -->

    <!-- BNC-80AC is 10.6" secant ogive, upscale of Estes BNC-55AC. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80AC</PartNumber>
      <Description>Nose cone, balsa, BT-80, 10.6", secant ogive, PN BNC-80AC, upscale of Estes BNC-55AC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.6</Length>
    </NoseCone>

    <!-- BNC-80AH is 10.5" ellipsoid (called ogive by Semroc) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80AH</PartNumber>
      <Description>Nose cone, balsa, BNC-80AH, 10.5", ellipsoid, PN BNC-80AH</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.5</Length>
    </NoseCone>

    <!-- BNC-80AO is 9.8" ellipsoid, upscale of Estes BNC-50AO -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80AO</PartNumber>
      <Description>Nose cone, balsa, BT-80, 9.8", ellipsoid, PN BNC-80AO</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">9.8</Length>
    </NoseCone>

    <!-- BNC-80BB is Estes PNC-80BB Super Big Bertha shape -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80BB</PartNumber>
      <Description>Nose cone, balsa, BT-80, 4.0", ellipsoid, PN BNC-80BB, Estes PNC-80BB shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BNC-80CO is 4.2" round tip cone -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80CO</PartNumber>
      <Description>Nose cone, balsa, BT-80, 4.2", round tip cone, PN BNC-80CO</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BNC-80MR is Mercury Redstone capsule shape -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80MR</PartNumber>
      <Description>Nose cone, balsa, BT-80, 4.2", Mercury capsule, PN BNC-80MR</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BNC-80D is 5.0" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80D</PartNumber>
      <Description>Nose cone, balsa, BT-80, 5.0", fat ogive, PN BNC-80D</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BNC-80F is 8.5" ogive, upscale of Estes BNC-55F -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80F</PartNumber>
      <Description>Nose cone, balsa, BT-80, 8.5", ogive, PN BNC-80F</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.5</Length>
    </NoseCone>

    <!-- BNC-80G3 is 8.8" 3:1 ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80G3</PartNumber>
      <Description>Nose cone, balsa, BT-80, 8.8", 3;1 ogive, PN BNC-80G3</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.8</Length>
    </NoseCone>

    <!-- BNC-80G4 is 10.4" 4:1 ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80G4</PartNumber>
      <Description>Nose cone, balsa, BT-80, 10.4", 4:1 ogive, PN BNC-80G4</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.4</Length>
    </NoseCone>

    <!-- BNC-80K is 8.2" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80K</PartNumber>
      <Description>Nose cone, balsa, BT-80, 8.2", ogive, PN BNC-80K</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BNC-80KA is 7.33" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80KA</PartNumber>
      <Description>Nose cone, balsa, BT-80, 7.3", ogive, PN BNC-80KA</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">7.3</Length>
    </NoseCone>

    <!-- BNC-80C is straight 6.5" conical -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80C</PartNumber>
      <Description>Nose cone, balsa, BT-80, 6.5", conical, PN BNC-80C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">6.5</Length>
    </NoseCone>

    <!-- BNC-80KP is 6.9" rounded ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80KP</PartNumber>
      <Description>Nose cone, balsa, BT-80, 6.9", rounded ogive, PN BNC-80KP</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BNC-80L is 5.1" ellipsoid (called bezier) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80L</PartNumber>
      <Description>Nose cone, balsa, BT-80, 5.1", ellipsoid, PN BNC-80HL</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">5.1</Length>
    </NoseCone>

    <!-- BNC-80V is 8.4" ogive, V-2 scale -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80V</PartNumber>
      <Description>Nose cone, balsa, BT-80, 5.1", ogive, PN BNC-80V, V-2 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.4</Length>
    </NoseCone>

    <!-- BNC-80VD is 8.4" ogive, V-2 scale, drilled 0.75" x about 5" deep (est. from legacy site drawing) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80VD</PartNumber>
      <Description>Nose cone, balsa, BT-80, 5.1", ogive, drilled, PN BNC-80V, V-2 scale</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.4</Length>
      <Mass Unit="oz">1.75</Mass>
    </NoseCone>

    <!-- BNC-80VE is 4.9" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80VE</PartNumber>
      <Description>Nose cone, balsa, BT-80, 4.9", ogive, PN BNC-80VE</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BNC-80X is 9.8" ellipsoid, upscale of Estes BNC-50X -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80X</PartNumber>
      <Description>Nose cone, balsa, BT-80, 9.8", ellipsoid, PN BNC-80X, upscale of Estes BNC-50X</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">9.8</Length>
    </NoseCone>

    <!-- BNC-80Y is 11.5" ogive, upscale of Estes BNC-50Y -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80Y</PartNumber>
      <Description>Nose cone, balsa, BT-80, 11.5", ogive, PN BNC-80Y, upscale of Estes BNC-50Y</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.600</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">11.5</Length>
    </NoseCone>

    <!-- BTC-80VY is 5.9" ogive tailcone drilled for 24mm MMT -->
    <Transition>
        <Manufacturer>Semroc</Manufacturer>
        <PartNumber>BTC-80VY [R]</PartNumber>
        <Description>Transition, balsa, BTC-80VY, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Mass Unit="oz">1.25</Mass>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="in">2.600</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">2.555</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">1.300</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">1.000</AftShoulderDiameter>
        <AftShoulderLength Unit="in">0.0</AftShoulderLength>
        <Length Unit="in">5.9</Length>
    </Transition>



    <!-- ==================================================== -->
    <!-- BNC-80H nose cones for BTH-80 heavy wall BT-80 tubes -->
    <!-- ==================================================== -->
    <!-- shoulder lengths all estimated -->

    <!-- BNC-80HAC is 10.6" secant ogive, upscale of Estes BNC-55AC. See alaso BNC-80HACS -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HACS</PartNumber>
      <Description>Nose cone, balsa, BTH-80, 10.6" secant ogive, PN BNC-80HACS, upscale of Estes BNC-55AC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.6</Length>
    </NoseCone>

    <!-- BNC-80HACS is 10.6" secant ogive, upscale of Estes BNC-55AC.  Semroc notes "uses JT-80E coupler",
         may actually have had different shoulder size than BNC-80HAC, though listed sizes are the same.
         Appears only on Semroc legacy site, no longer listed on website in Dec 2017. -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HACS</PartNumber>
      <Description>Nose cone, balsa, BNC-80HACS, 10.6", secant ogive, upscale of Estes BNC-55AC</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.6</Length>
    </NoseCone>

    <!-- BNC-80HAH is 10.5" ellipsoid (called ogive by Semroc) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HAH</PartNumber>
      <Description>Nose cone, balsa, BNC-80HAH, 10.5", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">10.5</Length>
    </NoseCone>

    <!-- BNC-80HAO is 9.8" ellipsoid, upscale of Estes BNC-50AO -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HAO</PartNumber>
      <Description>Nose cone, balsa, BNC-80HAO, 9.8", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">9.8</Length>
    </NoseCone>

    <!-- BNC-80HBB is Estes PNC-80BB Super Big Bertha shape -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HBB</PartNumber>
      <Description>Nose cone, balsa, BNC-80HBB, 4.0", ellipsoid, Estes PNC-80BB shape</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BNC-80HCO is 4.2" round tip cone -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HCO</PartNumber>
      <Description>Nose cone, balsa, BNC-80HCO, 4.2", round tip cone</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.2</Length>
    </NoseCone>

    <!-- BNC-80HD is 5.0" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HD</PartNumber>
      <Description>Nose cone, balsa, BNC-80HD, 5.0", fat ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">5.0</Length>
    </NoseCone>

    <!-- BNC-80HK is 8.2" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HK</PartNumber>
      <Description>Nose cone, balsa, BNC-80HK, 8.2", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">8.2</Length>
    </NoseCone>

    <!-- BNC-80HKA is 7.33" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HKA</PartNumber>
      <Description>Nose cone, balsa, BNC-80HKA, 7.3", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">7.3</Length>
    </NoseCone>
    
    <!-- BNC-80HKP is 6.9" rounded ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HKP</PartNumber>
      <Description>Nose cone, balsa, BNC-80HKP, 6.9", rounded ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">6.9</Length>
    </NoseCone>

    <!-- BNC-80HL is 5.1" ellipsoid (called bezier) -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HL</PartNumber>
      <Description>Nose cone, balsa, BNC-80HL, 5.1", ellipsoid</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">5.1</Length>
    </NoseCone>

    <!-- BNC-80HVE is 4.9" ogive -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HVE</PartNumber>
      <Description>Nose cone, balsa, BNC-80HVE, 4.9", ogive</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">4.9</Length>
    </NoseCone>

    <!-- BNC-80HX is 9.8" ellipsoid, upscale of Estes BNC-50X -->
    <NoseCone>
      <Manufacturer>Semroc</Manufacturer>
      <PartNumber>BNC-80HX</PartNumber>
      <Description>Nose cone, balsa, BNC-80HX, 9.8", ellipsoid, upscale of Estes BNC-50X</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.640</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.555</ShoulderDiameter>
      <ShoulderLength Unit="in">1.0</ShoulderLength>
      <Length Unit="in">9.8</Length>
    </NoseCone>


    <!-- ================================================================================ -->
    <!-- Series 275 (LT-275) nose cones.  All are upscales of Estes/Centuri parts.        -->
    <!-- ================================================================================ -->
    <!-- ALL BC-275 nose cones are shown as discontinued on the 2018 eRockets/Semroc website -->

    <!-- BC-27540 weight spec 1.51 oz, shoulder len 1.28" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-27540</PartNumber>
      <Description>Nose cone, balsa, LT-275, 4.0", blunt ogive, Estes BNC-50J upscale, PN BC-27540</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.748</ShoulderDiameter>
      <ShoulderLength Unit="in">1.28</ShoulderLength>
      <Length Unit="in">4.0</Length>
    </NoseCone>

    <!-- BC-27554 weight spec 1.7 oz, shoulder len 1.28" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-27554</PartNumber>
      <Description>Nose cone, balsa, LT-275, 5.4", ogive, Centuri BC-1631 upscale, PN BC-27554</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.748</ShoulderDiameter>
      <ShoulderLength Unit="in">1.28</ShoulderLength>
      <Length Unit="in">5.4</Length>
    </NoseCone>

    <!-- BC-27555 weight spec 1.99 oz, shoulder len 1.28" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-27555</PartNumber>
      <Description>Nose cone, balsa, LT-275, 5.5", elliptical, Estes BNC-60L upscale, PN BC-27555</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.748</ShoulderDiameter>
      <ShoulderLength Unit="in">1.28</ShoulderLength>
      <Length Unit="in">5.5</Length>
    </NoseCone>

    <!-- BC-27589 - upscale of BNC-55AM (***missing from Estes file***), no perfect shape
         match.  Calling it ogive which gives close mass.  Power series (param 0.45) is
         closer to shape, but power series param not specifiable in .orc files and
         defaults to 0.50.  Weight spec 2.42 oz, shoulder len 1.28" -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-27589</PartNumber>
      <Description>Nose cone, balsa, LT-275, 8.9", round tip cone, BNC-55AM upscale, PN BC-27589</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="in">2.840</OutsideDiameter>
      <ShoulderDiameter Unit="in">2.748</ShoulderDiameter>
      <ShoulderLength Unit="in">1.28</ShoulderLength>
      <Length Unit="in">8.9</Length>
    </NoseCone>


    <!-- =================================== -->
    <!-- Metric Quest / MRI / MPC Nose Cones -->
    <!-- =================================== -->
    <!-- Here we have mixed units.  The nose cone diameter and shoulder diameter are given in mm
         per their definitions, while the length and shoulder length are in inches to match the
         Semroc advertised specs.  -->

    <!-- BC-T1518 is ellipsoid with length 1.8", shoulder len 0.42" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T1518</PartNumber>
      <Description>Nose cone, balsa, T-15, 1.8", ellipsoid, PN BC-T1518</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">14.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">1.8</Length>
    </NoseCone>

    <!-- BC-T1529 is ogive with length 2.9", shoulder len 0.42" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T1529</PartNumber>
      <Description>Nose cone, balsa, T-15, 2.9", ogive, PN BC-T1529</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">14.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.42</ShoulderLength>
      <Length Unit="in">2.9</Length>
    </NoseCone>

    <!-- BC-T2014 is ogive with length 1.4", shoulder len 0.53" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T2014</PartNumber>
      <Description>Nose cone, balsa, T-20, 1.4", ogive, PN BC-T2014</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">19.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.53</ShoulderLength>
      <Length Unit="in">1.4</Length>
    </NoseCone>

    <!-- BC-T2032C is conical (with aft cyl section len 0.5") with length 3.25",
         shoulder length 0.53" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-T2032C legacy site gives length 3.2", but scaled drawing is 3.25".  Looks to be a 
         roundoff error where Carl changed a 3.25" length to a "32" part number suffix.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T2032C</PartNumber>
      <Description>Nose cone, balsa, T-20, 3.25", conical, PN BC-T2032C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">19.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.53</ShoulderLength>
      <Length Unit="in">3.25</Length>
    </NoseCone>

    <!-- BC-T2032 is ogive with length 3.2", shoulder length 0.53" (scaled dwg)  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T2032</PartNumber>
      <Description>Nose cone, balsa, T-20, 3.2", ogive, PN BC-T2032</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">19.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.53</ShoulderLength>
      <Length Unit="in">3.2</Length>
    </NoseCone>

    <!-- BC-T2534 is ogive with length 3.4", shoulder length 0.63" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T2534</PartNumber>
      <Description>Nose cone, balsa, T-25, 3.4", ogive, PN BC-T2534</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">24.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.63</ShoulderLength>
      <Length Unit="in">3.4</Length>
    </NoseCone>

    <!-- BC-T3033 is blunt ogive with length 3.35", shoulder length 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-T3033 legacy site gives length 3.3" but scaled drawing is 3.35".
         Apparently another part number length roundoff.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3033</PartNumber>
      <Description>Nose cone, balsa, T-30, 3.35", blunt ogive, PN BC-T3033</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.35</Length>
    </NoseCone>

    <!-- BC-T3525E is ellipsoid with length 2.5", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3525E</PartNumber>
      <Description>Nose cone, balsa, T-35, 2.5", ellipsoid, PN BC-T3525E</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">2.5</Length>
    </NoseCone>

    <!-- BC-T3532 is ellipsoid with length 3.2", shoulder len 0.75" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3532</PartNumber>
      <Description>Nose cone, balsa, T-35, 3.2", ellipsoid, PN BC-T3532</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.75</ShoulderLength>
      <Length Unit="in">3.2</Length>
    </NoseCone>

    <!-- BC-T3539 is ogive with length 3.9", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3529</PartNumber>
      <Description>Nose cone, balsa, T-35, 3.9", ogive, PN BC-T3529</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">3.9</Length>
    </NoseCone>

    <!-- BC-T3541C is conical with length 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3541C</PartNumber>
      <Description>Nose cone, balsa, T-35, 4.1", conical, PN BC-T3541C</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>CONICAL</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-T3541E is ellipsoid with length 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3541E</PartNumber>
      <Description>Nose cone, balsa, T-35, 4.1", ellipsoid, PN BC-T3541E</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>ELLIPSOID</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-T3541H is Haack with length 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3541H</PartNumber>
      <Description>Nose cone, balsa, T-35, 4.1", Haack, PN BC-T3541H</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>HAACK</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-T3541G is ogive with length 4.1", shoulder len 0.69" (scaled dwg) -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3541G</PartNumber>
      <Description>Nose cone, balsa, T-35, 4.1", ogive, PN BC-T3541G</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>

    <!-- BC-T3541B is rounded tip ogive with length 4.05", shoulder len 0.69" (scaled dwg) -->
    <!-- SOURCE ERROR: BC-T3541B legacy site gives length 4.1" but scaled drawing is 4.05".
         Apparently another part number length roundoff.  -->
    <NoseCone>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>BC-T3541B</PartNumber>
      <Description>Nose cone, balsa, T-35, 4.1", round tip ogive, PN BC-T3541B</Description>
      <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
      <Filled>true</Filled>
      <Shape>OGIVE</Shape>
      <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
      <ShoulderDiameter Unit="mm">34.0</ShoulderDiameter>
      <ShoulderLength Unit="in">0.69</ShoulderLength>
      <Length Unit="in">4.1</Length>
    </NoseCone>




    <!-- ========== -->
    <!-- Parachutes -->
    <!-- ========== -->

    <!-- SOURCE ERROR: Semroc legacy site lists inconsistent thicknesses for the various sizes of
         parachutes...I doubt that these are all correct.  Here is the list:

         12"   1 mil
         14"   1.5 mil
         16"   1.5 mil
         20"   1.1 mil
         24"   1.1 mil
         32"   1.5 mil
         36"   no thickness given

         For now I have used the stock 1 mil HDPE Estes type material for all of the Semroc
         sizes until we get better information.
    -->

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-12RY</PartNumber>
      <Description>Parachute kit, plastic, 12 in, red/yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">12.0</Diameter>
      <Sides>6</Sides>
      <LineCount>6</LineCount>
      <LineLength Unit="in">12.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-12RW</PartNumber>
      <Description>Parachute kit, plastic, 12 in, red/white</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">12.0</Diameter>
      <Sides>6</Sides>
      <LineCount>6</LineCount>
      <LineLength Unit="in">12.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-12BY</PartNumber>
      <Description>Parachute kit, plastic, 12 in, black/yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">12.0</Diameter>
      <Sides>6</Sides>
      <LineCount>6</LineCount>
      <LineLength Unit="in">12.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-12BW</PartNumber>
      <Description>Parachute kit, plastic, 12 in, blue/white</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">12.0</Diameter>
      <Sides>6</Sides>
      <LineCount>6</LineCount>
      <LineLength Unit="in">12.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-14R</PartNumber>
      <Description>Parachute kit, plastic, 14 in, red</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">14.0</Diameter>
      <Sides>6</Sides>
      <LineCount>6</LineCount>
      <LineLength Unit="in">14.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-16O</PartNumber>
      <Description>Parachute kit, plastic, 16 in, orange</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">16.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">16.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-16R</PartNumber>
      <Description>Parachute kit, plastic, 16 in, red</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">16.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">16.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-16Y</PartNumber>
      <Description>Parachute kit, plastic, 16 in, yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">16.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">16.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-20R</PartNumber>
      <Description>Parachute kit, plastic, 20 in, red</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">20.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">20.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-20Y</PartNumber>
      <Description>Parachute kit, plastic, 20 in, yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">20.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">20.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-24Y</PartNumber>
      <Description>Parachute kit, plastic, 24 in, yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">24.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">24.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <Parachute>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>CP-32Y</PartNumber>
      <Description>Parachute kit, plastic, 32 in, yellow</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
      <Diameter Unit="in">32.0</Diameter>
      <Sides>8</Sides>
      <LineCount>8</LineCount>
      <LineLength Unit="in">32.0</LineLength>
      <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
    </Parachute>

    <!-- ========= -->
    <!-- STREAMERS -->
    <!-- ========= -->

    <!-- On the legacy site there is a "Streamer Pak" SP-136O but the only description is 36 inch orange.
         Material, width and thickness not given.  On the new e-rockets site there are a number of
         streamers, and dimensions are given. Though the material is not specified, the photos are good
         enough to tell that the material is similar to or identical to the Estes type plastic streamers.
    -->

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-108</PartNumber>
      <Description>Streamer, 1 x 8 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">8.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-110</PartNumber>
      <Description>Streamer, 1 x 10 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">10.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-112</PartNumber>
      <Description>Streamer, 1 x 12 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">12.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-118</PartNumber>
      <Description>Streamer, 1 x 18 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">18.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-124</PartNumber>
      <Description>Streamer, 1 x 24 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">24.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-136</PartNumber>
      <Description>Streamer, 1 x 36 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">36.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-190</PartNumber>
      <Description>Streamer, 1 x 90 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.0</Width>
      <Length Unit="in">90.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-224</PartNumber>
      <Description>Streamer, 1.75 x 24 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.75</Width>
      <Length Unit="in">24.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-236</PartNumber>
      <Description>Streamer, 1.75 x 36 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.75</Width>
      <Length Unit="in">36.0</Length>
    </Streamer>

    <Streamer>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>SP-290</PartNumber>
      <Description>Streamer, 1.75 x 90 in., HDPE, 2.0 mil</Description>
      <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
      <Thickness Unit="in">0.002</Thickness>
      <Width Unit="in">1.75</Width>
      <Length Unit="in">90.0</Length>
    </Streamer>


    <!-- =========== -->
    <!-- LAUNCH LUGS -->
    <!-- =========== -->

    <!-- 1/8" Estes compatible lengths -->
    <!-- Estes specified the ID as 5/32 == 0.156".  Measured OD of some samples is 0.173" -->

    <!-- LL-2AM not known in Estes usage -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2AM</PartNumber>
      <Description>Launch lug, paper, 1/8 x 0.375 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">0.375</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2A</PartNumber>
      <Description>Launch lug, paper, 1/8 x 1.25 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">1.25</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2B</PartNumber>
      <Description>Launch lug, paper, 1/8 x 2.375 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">2.375</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2C</PartNumber>
      <Description>Launch lug, paper, 1/8 x 5 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">5.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2D</PartNumber>
      <Description>Launch lug, paper, 1/8 x 8 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </LaunchLug>

    <!-- LL-2E 9.5" long was used in the K-43 Mars Lander. -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-2E</PartNumber>
      <Description>Launch lug, paper, 1/8 x 9.5 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </LaunchLug>


    <!-- 1/8" Semroc numeric length PNs -->

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-104</PartNumber>
      <Description>Launch lug, paper, 1/8 x 0.4 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">0.4</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-108</PartNumber>
      <Description>Launch lug, paper, 1/8 x 0.75 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">0.75</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-110</PartNumber>
      <Description>Launch lug, paper, 1/8 x 1.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-115</PartNumber>
      <Description>Launch lug, paper, 1/8 x 1.5 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">1.5</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-117</PartNumber>
      <Description>Launch lug, paper, 1/8 x 1.75 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">1.75</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-120</PartNumber>
      <Description>Launch lug, paper, 1/8 x 2.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-122</PartNumber>
      <Description>Launch lug, paper, 1/8 x 2.25 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-130</PartNumber>
      <Description>Launch lug, paper, 1/8 x 3.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </LaunchLug>

    <!-- LL-180 is alias for Estes LL-2D -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-180</PartNumber>
      <Description>Launch lug, paper, 1/8 x 8.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">8.0</Length>
    </LaunchLug>

    <!-- LL-195 is alias for Estes LL-2E -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-195</PartNumber>
      <Description>Launch lug, paper, 1/8 x 9.5 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">9.5</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-1120</PartNumber>
      <Description>Launch lug, paper, 1/8 x 12.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.156</InsideDiameter>
      <OutsideDiameter Unit="in">0.173</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </LaunchLug>

    <!-- 3/16 Estes-compatible numbers -->
    <!-- 3/16 lug measured OD = 0.227, set ID to give same wall thickness as 1/8 lugs -->
    <!-- 3/16 x 2 possibly has LL designation; no known Estes ref but Semroc calls
         it LL-3B.  Carl McLawhorn was pretty careful about his PNs for exact Estes parts -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-3B</PartNumber>
      <Description>Launch lug, paper, 3/16 x 2.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.210</InsideDiameter>
      <OutsideDiameter Unit="in">0.227</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </LaunchLug>


    <!-- 3/16 Semroc numeric length PNs -->

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-310</PartNumber>
      <Description>Launch lug, paper, 3/16 x 1.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.210</InsideDiameter>
      <OutsideDiameter Unit="in">0.227</OutsideDiameter>
      <Length Unit="in">1.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-320</PartNumber>
      <Description>Launch lug, paper, 3/16 x 2.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.210</InsideDiameter>
      <OutsideDiameter Unit="in">0.227</OutsideDiameter>
      <Length Unit="in">2.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-330</PartNumber>
      <Description>Launch lug, paper, 3/16 x 3.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.210</InsideDiameter>
      <OutsideDiameter Unit="in">0.227</OutsideDiameter>
      <Length Unit="in">3.0</Length>
    </LaunchLug>

    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-3120</PartNumber>
      <Description>Launch lug, paper, 3/16 x 12.0 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.210</InsideDiameter>
      <OutsideDiameter Unit="in">0.227</OutsideDiameter>
      <Length Unit="in">12.0</Length>
    </LaunchLug>

    <!-- 1/4 Semroc numeric length PNs -->
    <!-- ID/OD estimated using 9/32 ID and giving it .021 wall -->
    <LaunchLug>
      <Manufacturer>SEMROC</Manufacturer>
      <PartNumber>LL-423</PartNumber>
      <Description>Launch lug, paper, 1/4 x 2.25 in</Description>
      <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
      <InsideDiameter Unit="in">0.281</InsideDiameter>
      <OutsideDiameter Unit="in">0.323</OutsideDiameter>
      <Length Unit="in">2.25</Length>
    </LaunchLug>

    
  </Components>
</OpenRocketComponent>
