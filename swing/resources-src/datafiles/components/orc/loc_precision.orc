<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
loc_precision.orc - LOC Precision parts file for OpenRocket

Copyright 2014-2019 by Dave Cook  NAR 21953  caveduck17@gmail.com

See the file LICENSE in this distribution for license information.


Dimensions and masses are a severe challenge for LOC parts; in 34 years of existence through 3
different ownerships, LOC has never published good size/weight data for any of its
parts.  There are many missing dimensions on the LOC website, and several cases where
dimensions given are obviously approximate or even outright wrong.  After
the 2016 ownership change, the website as of fall 2018 is a bit better with regard to airframe
tube dimensions, but there remains little data about couplers, 75/98mm motor tubes, and many other items.

LOC is very inconsistent on whether decimal points are included in SKU/PNs.  I have adopted
the convention of using the decimal points since more of them seem to have that in the LOC
literature.

The best *official* LOC resource is the "2010-2012 Accessories Catalog".  However it is still
pretty incomplete.

Updates to the LOC website in early 2018 have added some dimensional data, but
the accuracy and precision are mostly poor.  Some of the new products like LBT-50 airframe
have good data though and there have been scattered other improvements.

The Apogee site has a very comprehensive listing of parts with measured
dimensions and masses for LOC and other lines of parts. In all cases except where I measured
different values from an actual part, I've adopted the Apogee values as more likely to be correct.

Apogee has not indexed some of the LOC line including plastic transitions and the
18mm and 24mm tubes.

NOTES:

* MMTHD-2.56 seems to be non-existent, though it was in the built-in OpenRocket file.
There formerly was an entry on the LOC site for 64mm MMT but had an icon for BT-3.00
As of Jan 2017 it is gone.  I am now pretty confident it never existed commercially.

* The builtin OpenRocket .orc has entries for PNC-2.56, PNC-3.00 and PNC-3.90 reversed and cut for
boattails. I kept this as potentially useful, but their authenticity as LOC parts is dubious.
I am not sure if they ever did officially exist; they do not appear in old catalogs up through 1989,
aren't listed on the 2017 website, and I cannot locate any reference to them in historical
materials.  They may have been incorporated in some older kits such as Marvin's Wild Ride as
on-the-fly modifications when kitting up those rockets.

* Shroud line specs for LOC parachutes were only described as "200# nylon cord".  This would be
about a 2.0 mm size, but (oddly) nobody seems to make that.  Closest fit would be a 233 lb 3/32"
size.  I have a couple of genuine 1980s LOC parachutes and the shrouds look much lighter than that.
So I've used 110# (1/16") for regular weight parachutes similar to what Top Flight uses, and 275#
(2.4mm) for the heavy duty chutes.  See extensive discussion of nylon paracord sizes in
generic_materials.orc.

* Slotted body tubes exist in the 2010-2012 accessories catalog, and again on the new 2017 web site:
  SBT-2.14-3S   (3 slots)
  SBT-2.14-4S   (4 slots)
  SBT-2.56-3S
  SBT-3.00-4S
  SBT-3.90-3S
  SBT-3.90-6S   (6 slots, used in Ultimate)

* Phenolic tubing (claimed flexible) has reappeared on the LOC website in fall 2018.  It is
said to be the saem size as the cardboard tubing.  Only the 1.52 and 5.38 sizes can be selected
in the shop, though the "additional information" tab shows all sizes from 1.52 through 7.51.

CENTERING RINGS AND LAUNCH LUGS:

Centering rings (plywood and one fiber type) are listed in the 2010 Accessories Catalog as
follows.  Thicknesses were mostly not given except for a general statement that they are made from "1/8",
3/16" and 1/4" specialty plywood".  The FCR-1.52-1.14 is the fiber centering ring.

UPDATE Apr 2018: On the 2018 website, thicknesses are given for more rings, enough to let us
infer thicknesses for all the ones not explicitly given somewhere.

FCR-1.52-1.14
CR-2.14-0.95        0.125 (inferred)
CR-2.14-1.14        0.125 (inferred)
CR-2.14-1.52        0.125 (inferred)
CR-2.56-0.95        0.125 (2018 site)
CR-2.56-1.14        0.125 (inferred)
CR-2.56-1.52        0.125 (inferred)
CR-3.00-0.71        0.250 (inferred)
CR-3.00-0.95        0.250 (2018 site)
CR-3.00-1.14        0.250 (inferred)
CR-3.00-1.52        0.250 (inferred)
CR-3.00-2.14        0.250 (inferred)
CR-3.90-1.14        0.250 (inferred)
CR-3.90-1.52        0.250 (2017 site)
CR-3.90-2.14        0.250 (inferred)
CR-3.90-3.00        0.250 (2018 site)
CR-5.38-1.52        0.250 (2017 site)
CR-5.38-2.14        0.250 (2017 site)
CR-5.38-3.00        0.250 (2017 site)
CR-5.38-3.90        0.250 (2017 site, 2018 site)
CR-7.51-1.52        0.250 (2018 site)
CR-7.51-2.14        0.250 (inferred)
CR-7.51-3.00        0.250 (inferred)
CR-7.51-3.90        0.250 (inferred)

* Launch lugs from 2010 Accessories Catalog:
LL-25   for 1/4" rod  6.0" long
LL-50   for 1/2" rod  6.0" long
LL-937  for 15/16" rod  5.0" long

REFERENCES:
    locprecision.com  - current parts listings as of March 2017, updated April 2018
    LOC 2009 to 2012 Accessories Catalog - retrieved from yumpu.com in 2017
    apogeerockets.com - carries LOC parts with many additional measurements and masses tabulated
    1986, 1987 and 1989 LOC catalogs - on ninfinger.org

*** TODO ***

1) Improve masses for most of the plastic parts.

-->
<!-- *** SOURCE ERROR: LOC 2018 website gives identical weight of .03125 lb for CR-2.14-1.52 and CR-2.14-1.14,
the former should be lighter because of the larger center hole. -->
<!-- *** SOURCE ERROR: The dimensions on the LOC website under "additional information" for nearly all LOC centering rings are wrong. -->

<OpenRocketComponent>
    <Version>1.0</Version>
    <Materials>

      <!-- LOC-specific materials for tube/couplers -->
        <!-- Average values for LOC tube, coupler and stiffener parts from spreadsheet density analysis of Apogee data
             These densities are a very good fit with everything but a few outliers -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, kraft, LOC coupler avg</Name>
            <Density>789.6</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, kraft, LOC coupler stiffener avg</Name>
            <Density>603.3</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, kraft glassine, LOC tube avg</Name>
            <Density>855.2</Density>
            <Type>BULK</Type>
        </Material>
        <!-- The LOC MMTHD-3.00 and MMTHD-3.90 have much higher density -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, kraft glassine, LOC MMTHD avg</Name>
            <Density>1220.6</Density>
            <Type>BULK</Type>
        </Material>

        <!-- standard materials copied from generic_materials.orc -->

        <!-- LOC phenolic tubes are assumed to be generic Kraft phenolic til proven otherwise -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, kraft, bulk</Name>
            <Density>943.0</Density>
            <Type>BULK</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m3">
            <Name>Polypropylene, bulk</Name>
            <Density>946.0</Density>
            <Type>BULK</Type>
        </Material>

        <Material UnitsOfMeasure="g/m">
            <Name>Nylon Paracord, 110 lb, 1/16 in. dia.</Name>
            <Density>0.00160</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="g/m">
            <Name>Nylon Paracord, 275 lb, 2.38 mm dia.</Name>
            <Density>0.00350</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 1.9 oz urethane coated ripstop nylon is LOC material per 2018 website -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.9 oz actual</Name>
            <Density>0.0589</Density>
            <Type>SURFACE</Type>
        </Material>

      </Materials>

      <Components>

        <!--
            LOC standard paper airframe tubes
            In sizes less than BT-3.00, LOC does not make *both* MMT heavy-wall and regular
            thickness tubes in the same size.
            LOC does not give masses for any of their tubes; most data is from the Apogee site
            Discount Rocketry formerly had info on LOC tubes but they now source their own tubes
            with generally compatible dimensions.
            Apogee does not list the LOC MMT-0.71 and MMT-0.95 tubes
            2018 website has changed some product lengths, and there are many errors in the dimensions/weights
        -->

        <!-- MMT-0.71 - dimensions from 2010 accessories catalog, no mass info available
             Working from density analysis, mass given avg density of 855.2 kg/m3 is estimated at 27.7 gm
             On 2018 website the SKU has changed to MMT-0.75 and is only shown as available in 2.75" length
             *** SOURCE ERROR: on 2018 website, 18mm motor mount tube is given as 2.75" in the menu dropdown, but the
             SKU is "MMT-0.75x6", and the listed dimension is "6 x .75 x .75 in".  The weight is also given as 0.0156 oz,
             suspiciously identical to the weight given for the MMT-0.95x6 tube. -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>MMT-0.71</PartNumber>
            <Description>Body tube, paper, MMT-0.71 18mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">0.715</InsideDiameter>
            <OutsideDiameter Unit="in">0.765</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <!-- MMT-0.95 - ID, OD and .025 wall per LOC 1980s catalogs and 2018 website, discount rocketry comparable weight 1.30 oz.
             In April 2018 website, there is now an airframe LBT-50, only sold in 12" lengths,
             and MMT-0.95x6 and MMT-0.95x12 -->
             <!-- *** SOURCE ERROR: April 2018 website lists MMT-0.95x6 with weight .0156 lb, and MMT-0.95x12 with weight 0.06875.
             The 12" tube should be twice the weight of the 6" tube.  Also the LBT-50 airframe tube, SKU BT-1.0 gives weight
             of 0.05 lb and size of 12 x 1 x 1 in, leaving no way to tell wall thickness.
             -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>MMT-0.95, LBT-50</PartNumber>
            <Description>Body tube, paper, LBT-50 / MMT-0.95 24mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">0.95</InsideDiameter>
            <OutsideDiameter Unit="in">1.0</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <!-- MMT-1.14 mass for 34" given as 2.40 oz / 68 g per Apogee;
             Discount Rocketry identical dimensions tube is quoted at 2.3 oz / 65.1 gm
             I measured a recent LOC Aura MMT (6" of MMT-1.14) at 11.1 gm implying 62.9 gm for 34"
            .035 wall, which I have adopted as it gives a density more in line with others
        -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-1.14, MMT-1.14</PartNumber>
            <Description>Body tube, paper, BT-1.14/MMT-1.14 29mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">1.14</InsideDiameter>
            <OutsideDiameter Unit="in">1.21</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>
        <!-- MMT-1.52 mass 118 g / 4.13 oz per apogee, 4.5 oz per discountrocketry, 3.25 oz 2010
             accessories catalog
             .055 wall per 2010 parts order form
             Apogee lists as "BT-1.52 / MMT-1.52"
             The Apogee mass value gives a somewhat low density (776) vs ~860 for others, but it
             looks real. I measured a vintage 1990 full length MMT-1.52 at 114.5 gm and a 15" length
             (2016 LOC Aura kit) at 51.5 gm implying 116gm for 34" length -->
        <!-- *** SOURCE ERROR: 2018 website gives wrong dimensions (and probably weight) for MMT-1.58x11.
             Dimensions are given as "11 x 2.14 x 2.14 in" - that's 54mm tube.  -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-1.52, MMT-1.52</PartNumber>
            <Description>Body tube, paper, BT-1.52/MMT-1.52 38mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">1.525</InsideDiameter>
            <OutsideDiameter Unit="in">1.635</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>
        <!-- MMT-2.14 mass 192 g / 6.72 oz per Apogee, 0.24 lb (3.84 oz) per LOC 2018 website, 7.50 oz per LOC 2010 accessories
             catalog.  Apparently has .060 wall.  Apogee lists as "BT-2.14 / MMT-2.14".  On 2018 website, SKU has changed to
             MMT-54 -->
        <!-- *** SOURCE ERROR: 2018 website has wrong dimensions for MMT-54-17 "10 x 1.58 x 1.58 in" -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-2.14, MMT-2.14, MMT-54</PartNumber>
            <Description>Body tube, paper, BT-2.14/MMT-2.14 54mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">2.14</InsideDiameter>
            <OutsideDiameter Unit="in">2.26</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <!-- BT-2.56 mass 158 g / 5.53 oz per Apogee, 4.50 oz per LOC 2010 accessories catalog
             The LOC mass yields a much more reasonable density so I adopted that.
             0.035 wall
             LOC co-owner Jason Turicek authoritatively stated in a rocketryforum post
             on 17 Mar 2019 that BT-2.56 has always been 30" long instead of the 34" used for
             other tube sizes (based on tube ordering records back into the 1990s)
        -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-2.56</PartNumber>
            <Description>Body tube, paper, BT-2.56, 30.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">2.56</InsideDiameter>
            <OutsideDiameter Unit="in">2.63</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- BT-3.00 - mass of 34" length is 226 g / 7.91 oz per Apogee, .050 wall -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-3.00</PartNumber>
            <Description>Body tube, paper, BT-3.00, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">3.0</InsideDiameter>
            <OutsideDiameter Unit="in">3.1</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>
        <!-- MMTHD-3.00 mass in 34 length is 392 g / 13.72 oz per Apogee
             .060 wall per Apogee and 2010-2012 accessories catalog
        -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>MMTHD-3.00</PartNumber>
            <Description>Body tube, paper, heavy, MMTHD-3.00 75mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC MMTHD avg</Material>
            <InsideDiameter Unit="in">3.0</InsideDiameter>
            <OutsideDiameter Unit="in">3.120</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <!-- BT-3.9 34" long mass is 298 g / 10.512 oz per Apogee, .050 wall -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-3.9</PartNumber>
            <Description>Body tube, paper, BT-3.9, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">3.9</InsideDiameter>
            <OutsideDiameter Unit="in">4.0</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>
        <!-- MMTHD-3.9 - mass is 508 gm / 17.78 oz per Apogee
             .060 wall per Apogee and 2010 accessories catalog
        -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>MMTHD-3.9</PartNumber>
            <Description>Body tube, paper, heavy, MMTHD-3.9 98mm, 34.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC MMTHD avg</Material>
            <InsideDiameter Unit="in">3.9</InsideDiameter>
            <OutsideDiameter Unit="in">4.020</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>
        <!-- BT-5.38 42" long mass is 784 gm / 27.44 oz per Apogee, .080 wall -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-5.38</PartNumber>
            <Description>Body tube, paper, BT-5.38, 42.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">5.38</InsideDiameter>
            <OutsideDiameter Unit="m">5.54</OutsideDiameter>
            <Length Unit="in">42.0</Length>
        </BodyTube>
        <!-- BT-7.51 60" long mass is 1726.7 g / 60.44 oz per Apogee, .080 wall
             LOC 2010 accessories catalog lists a 30" long BT-7.51 at 24.0 oz, no separate PN.
             LOC 2018 website shows BT-7.51 as 2x30" in the menu dropdown.
        -->
        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>BT-7.51</PartNumber>
            <Description>Body tube, paper, BT-7.51, 60.0"</Description>
            <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
            <InsideDiameter Unit="in">7.515</InsideDiameter>
            <OutsideDiameter Unit="in">7.675</OutsideDiameter>
            <Length Unit="in">60.0</Length>
        </BodyTube>

        <!-- ======================= -->
        <!-- PHENOLIC AIRFRAME TUBES -->
        <!-- ======================= -->
        <!-- No mass data is available from LOC, so we use generic kraft phenolic density.
             The dimensions are stated to be identical to the cardboard airframe tubes (website, fall 2018) -->

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-1.52</PartNumber>
            <Description>Body tube, phenolic, BT-1.52, 34.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">1.525</InsideDiameter>
            <OutsideDiameter Unit="in">1.635</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-2.14</PartNumber>
            <Description>Body tube, phenolic, FT-2.14, 34.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">2.14</InsideDiameter>
            <OutsideDiameter Unit="in">2.26</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-2.56</PartNumber>
            <Description>Body tube, phenolic, FT-2.56, 30.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">2.56</InsideDiameter>
            <OutsideDiameter Unit="in">2.63</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-3.00</PartNumber>
            <Description>Body tube, phenolic, FT-3.00, 34.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">3.0</InsideDiameter>
            <OutsideDiameter Unit="in">3.1</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-3.9</PartNumber>
            <Description>Body tube, phenolic, FT-3.9, 34.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">3.9</InsideDiameter>
            <OutsideDiameter Unit="in">4.0</OutsideDiameter>
            <Length Unit="in">34.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-5.38</PartNumber>
            <Description>Body tube, phenolic, FT-5.38, 42.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">5.38</InsideDiameter>
            <OutsideDiameter Unit="m">5.54</OutsideDiameter>
            <Length Unit="in">42.0</Length>
        </BodyTube>

        <BodyTube>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>FT-7.51</PartNumber>
            <Description>Body tube, phenolic, FT-7.51, 60.0"</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="in">7.515</InsideDiameter>
            <OutsideDiameter Unit="in">7.675</OutsideDiameter>
            <Length Unit="in">60.0</Length>
        </BodyTube>

        <!--
            LOC plastic nose cones

            These are the infamous hard-to-paint blow-molded polypropylene nose cones.  The built-in
            OpenRocket file erroneously had them all as polystyrene.
            Dimensions and masses are mostly from the Apogee site, supplemented with a few
            actual measurements of parts I have and some values from the 2018 LOC site.

            Thicknesses are set to make mass come out right PROVIDED you go in and manually set the
            shoulder thicknesses to be the same as what you see here, and turn on "end capped".
            This provides the most accurate CG location for the nose cone.
        -->

        <!-- LOC PNC-1.52, 8.0 in ogive, weight on 2018 site 3.5oz, measured mass 92g / 3.22 oz -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-1.52</PartNumber>
            <Description>Nose cone, polypropylene, PNC-1.52, ogive, 8.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.635</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.51</ShoulderDiameter>
            <ShoulderLength Unit="in">2.0</ShoulderLength>
            <Length Unit="in">8.0</Length>
            <Thickness Unit="in">0.180</Thickness>
        </NoseCone>
        <!-- LOC PNC-2.14, 9.5 in ogive, LOC site weight "4 oz", Apogee weight 112 g / 3.92 oz.  -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-2.14</PartNumber>
            <Description>Nose cone, polypropylene, PNC-2.14, ogive, 9.5"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.260</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.13</ShoulderDiameter>
            <ShoulderLength Unit="in">2.5</ShoulderLength>
            <Length Unit="in">9.5</Length>
            <Thickness Unit="in">0.120</Thickness>
        </NoseCone>
        <!-- LOC PNC-2.56, 9.0 in ogive, 94 g / 3.316 oz, LOC 2018 site shoulder len 2.62, weight "3 oz" -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-2.56</PartNumber>
            <Description>Nose cone, polypropylene, PNC-2.56, ogive, 9.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.63</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.55</ShoulderDiameter>
            <ShoulderLength Unit="in">2.62</ShoulderLength>
            <Length Unit="in">9.0</Length>
            <Thickness Unit="in">0.083</Thickness>
        </NoseCone>
        <!-- LOC PNC-3.00, 11.25" ogive.  Poorly documented anywhere, but I had one to measure:
                 Mass 133.3 g / 4.67 oz.
                 Exposed length: 11.25 in
                 Shoulder functional cylinder length: 2.85 in
                 Shoulder extension cone taper length: 0.80 in
             Apogee doesn't give diam or shoulder diameter.
             Old LOC catalogs give the shoulder as 3.75", which I used in hopes of getting the mass/volume calc to come
             out well.  Although the length is given in various places as 12.5", that is wrong.
             Length is given in the 2010 Accessories Catalog as 11.25" (15 - 3.75), which is correct.
             LOC 2018 website gives mass as 0.35 lb = 5.6 oz but no other useful dimensions.
        -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-3.00</PartNumber>
            <Description>Nose cone, polypropylene, PNC-3.00, ogive, 12.5"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.1</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.990</ShoulderDiameter>
            <ShoulderLength Unit="in">3.75</ShoulderLength>
            <Length Unit="in">11.25</Length>
            <Thickness Unit="in">0.077</Thickness>
        </NoseCone>
        <!-- LOC PNC-3.90, 12.75 in ogive, 5.0 oz and shoulder length 3.75 in per LOC, mass 200 g per Apogee
             I have a late 1980's example that weighs 151 gm -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-3.90</PartNumber>
            <Description>Nose cone, polypropylene, PNC-3.90, ogive, 12.75"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.0</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.88</ShoulderDiameter>
            <ShoulderLength Unit="in">3.75</ShoulderLength>
            <Length Unit="in">12.75</Length>
            <Thickness Unit="in">0.06</Thickness>
        </NoseCone>
        <!-- LOC PNC-5.38, 13.0 in ogive, Apogee mass 360g / 12.6 oz.  LOC 2017-2018 website now quotes this as "10 oz" -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-5.38</PartNumber>
            <Description>Nose cone, polypropylene, PNC-5.38, ogive, 13.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.54</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.37</ShoulderDiameter>
            <ShoulderLength Unit="in">4.0</ShoulderLength>
            <Length Unit="in">13.0</Length>
            <Thickness Unit="in">0.095</Thickness>
        </NoseCone>
        <!-- LOC PNC-5.38L, 21.0 in ogive, LOC 2018 website weight 22 oz, Apogee mass 594 g / 20.79 oz -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-5.38L</PartNumber>
            <Description>Nose cone, polypropylene, PNC-5.38L, ogive, 21.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.54</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.37</ShoulderDiameter>
            <ShoulderLength Unit="in">5.0</ShoulderLength>
            <Length Unit="in">21.0</Length>
            <Thickness Unit="in">0.111</Thickness>
        </NoseCone>
        <!-- LOC PNC-7.51, 22.0 in ogive, mass 876 g / 30.66 oz per Apogee.  TRF users have confirmed
             22.0 inch length and weight 30.6 oz., see
             https://www.rocketryforum.com/threads/loc-nosecone-length.152021/
             LOC 2018-2019 website gives no dimensions and weight as 1.2 lb, which is much too low. -->
        <NoseCone>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-7.51</PartNumber>
            <Description>Nose cone, polypropylene, PNC-7.51, ogive, 22.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">7.675</OutsideDiameter>
            <ShoulderDiameter Unit="in">7.49</ShoulderDiameter>
            <ShoulderLength Unit="in">5.0</ShoulderLength>
            <Length Unit="in">22.0</Length>
            <Thickness Unit="in">0.111</Thickness>
        </NoseCone>

        <!-- LOC nylon flat sheet parachutes -->
        <!-- Historically LOC made their own parachutes, which were originally sewn by
             Deb Schultz.  I don't know if LOC still makes their own.  On the 2018 website
             the material is stated to be 1.9 oz "urethane coated, calendared rip-stop" nylon.

             The masses listed for the parachutes mostly don't line up very well with the computed masses derived from
             1.7oz ripstop nylon with reasonable paracord sizes. Some are complete nonsense,
             such as having the LHPC-48 be 28 gm *heavier* than the LHPC-58.  (actually those
             would match a lot better if they were swapped!)  So I've just
             used a best guess at the materials and allow OpenRocket to compute the mass.  -->

        <!-- LP-12, mass given as 9.7 gm, calculated 7.13 .  LOC 2018 website gives 0.0625 lb = 1 oz -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LP-12</PartNumber>
            <Description>Parachute, 12 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- LP-14, mass given as 11.34 gm, calculated 9.14. -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LP-14</PartNumber>
            <Description>Parachute, 14 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">14.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">14.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- LP-18, mass given as 22.68 gm, calculated 13.9 -->
        <!--- *** SOURCE ERROR: LOC 2018 website gives identical masses 0.0625 lb for LP-12, LP-14, and LP-18.
              This is impossible. -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LP-18</PartNumber>
            <Description>Parachute, 18 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- LP-28, mass given as 28.35 gm, calculated 32.0.  LOC 2018 site gives weight 0.1875 lb == 3 oz -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LP-28</PartNumber>
            <Description>Parachute, 28 in., nylon, 8 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">28.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">28.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- LHPC series - construction differences vs smaller LP series not known,
             but we presume over-the-top shrouds which makes the masses match better -->

        <!-- LHPC-36, mass given as 85.0 gm, calculated 85.9 gm -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LHPC-36</PartNumber>
            <Description>Parachute, 36 in., nylon, 10 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>10</Sides>
            <LineCount>10</LineCount>
            <LineLength Unit="in">54.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 275 lb, 2.38 mm dia.</LineMaterial>
        </Parachute>

        <!-- In the 2017 website update, there is no LHPC-48; it's replaced by LHPC-50 -->
        <!-- LHPC-48, mass given as 170.1 gm, calculated 131.0 gm -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LHPC-48</PartNumber>
            <Description>Paraachute, 48 in., nylon, 10 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">48.00</Diameter>
            <Sides>10</Sides>
            <LineCount>10</LineCount>
            <LineLength Unit="in">72.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 275 lb, 2.38 mm dia.</LineMaterial>
        </Parachute>

        <!-- LHPC-58, mass given as 141.7 gm, calculated 176.0 gm -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LHPC-58</PartNumber>
            <Description>Parachute, 58 in., nylon, 10 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">58.0</Diameter>
            <Sides>10</Sides>
            <LineCount>10</LineCount>
            <LineLength Unit="in">87.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 275 lb, 2.38 mm dia.</LineMaterial>
        </Parachute>

        <!-- LHPC-78, mass given as 311.8 gm, calculated 343 gm.  2018 website gives 0.75 lb = 340 gm -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LHPC-78</PartNumber>
            <Description>Parachute, 78 in., nylon, 16 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">78.0</Diameter>
            <Sides>16</Sides>
            <LineCount>16</LineCount>
            <LineLength Unit="in">116.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 275 lb, 2.38 mm dia.</LineMaterial>
        </Parachute>

        <!-- The 86" parachute is mentioned in the 2017 website update but cannot be selected.
             It is completely gone from the 2018 website.  -->
        <!-- LHPC-86, mass given as 453.6 gm (crude conversion from 16 oz?), calculated 400 gm -->
        <Parachute>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>LHPC-86</PartNumber>
            <Description>Parachute, 86 in., nylon, 16 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.9 oz actual</Material>
            <Diameter Unit="in">86.0</Diameter>
            <Sides>16</Sides>
            <LineCount>16</LineCount>
            <LineLength Unit="in">129.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 275 lb, 2.38 mm dia.</LineMaterial>
        </Parachute>

        <!-- Angel series parachutes -->
        <!-- The LOC Angel parachutes were a bit of a mystery.  They are listed and extensively
             described on the Apogee website, but do not appear on the LOC site except in an
             an archived news entry dated 21 Oct 2013 that is now a dead link.  Apogee lists
             them in 44", 52", 60", and 80" sizes with 3 of the 4 sizes still in stock.
             They are the Sky Angle design with only 3 shroud lines.  The fabric is 1.9oz silicone
             coated nylon, and the shrouds are 3/8" diam 900 lb nylon (well beyond 550 paracord)
             It turns out that they were actually made by Sky Angle / B2 Rocketry, who still sells them.
        -->

        <!-- Transitions (Plastic) -->

        <!-- AR-3.00-2.14 with smaller shoulder forward (increasing diam), 4 oz per LOC
             LOC gives overall length 8.0", taper length 3.12"; we apportion the rest for shoulder
             lengths.
             1989 LOC catalog gives weight 5.5 oz, 2017 LOC site save "4 oz"
             I opted for 4.0 oz until we get better data.
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>AR-3.00-2.14</PartNumber>
            <Description>Transition, polypropylene, 2.14 to 3.00 size, increasing, 3.12"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">2.26</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.13</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">2.38</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">3.1</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.99</AftShoulderDiameter>
            <AftShoulderLength Unit="in">2.5</AftShoulderLength>
            <Length Unit="in">3.12</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>

        <!-- AR-3.00-2.14 with larger shoulder forward (reducer), 5.5 oz -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>AR-3.00-2.14[R]</PartNumber>
            <Description>Transition, polypropylene, 3.00 to 2.14 size, reducing, 3.12"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">3.1</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.99</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">2.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.26</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.13</AftShoulderDiameter>
            <AftShoulderLength Unit="in">2.38</AftShoulderLength>
            <Length Unit="in">3.12</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>

        <!-- AR-3.90-3.00 with smaller shoulder forward (increasing diam)
             LOC site and 2010 Accessories Catalog give overall len 8.75", taper length 2.5", weight 6 oz.
             *** 1989 LOC catalog gives weight 11.0 oz, 2017 LOC site gives "6 oz". ***
             I used the 6.0 oz value since it's a better match with the likely thickness.
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>AR-3.90-3.00</PartNumber>
            <Description>Transition, polypropylene, 3.00 to 3.90 size, increasing, 2.5"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">3.1</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.99</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">3.0</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">4.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">3.88</AftShoulderDiameter>
            <AftShoulderLength Unit="in">3.25</AftShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>

        <!-- AR-3.90-3.00 with larger shoulder forward (reducer), see mass conflict above -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>AR-3.90-3.00[R]</PartNumber>
            <Description>Transition, polypropylene, 3.90 to 3.00 size, reducing, 2.5"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">4.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">3.88</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">3.25</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">3.1</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.99</AftShoulderDiameter>
            <AftShoulderLength Unit="in">3.0</AftShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>


        <!--
             PNC-2.56 reversed as boattail with 1.75 rear diam and 4.25" cut off
             Mass should be around 2.5 oz, full NC is 3.316 oz.
             Does not appear on LOC site Jan 2017
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-2.56[R]</PartNumber>
            <Description>Transition, polypropylene, PNC-2.56 reversed, 4.75"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <ForeOutsideDiameter Unit="in">2.63</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.55</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">2.625</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.75</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">4.75</Length>
            <Thickness Unit="in">0.080</Thickness>
        </Transition>

        <!-- PNC-3.00 reversed as boattail with 6" remaining (matching shape to PNC-3.00)
             Ends up as 3.83 oz with shoulder wall thickness and end cap set
             Does not appear on LOC site Jan 2017
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-3.00[R]</PartNumber>
            <Description>Transition, polypropylene, PNC-3.00 reversed, 2.0" rear diam, 6.625"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <ForeOutsideDiameter Unit="in">3.1</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.99</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">3.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">6.0</Length>
            <Thickness Unit="in">0.077</Thickness>
        </Transition>

        <!-- PNC-3.90 reversed as boattail cut off to 2.75" rear diam.
             Does not appear on LOC site Jan 2017.  Using matching thickness, mass at 7.0" len is
             3.6 oz.
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-3.90[R]</PartNumber>
            <Description>Transition, polypropylene, PNC-3.90 reversed, 2.75" rear diam, 7.0"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <ForeOutsideDiameter Unit="in">4.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">3.88</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">3.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.75</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">7.0</Length>
            <Thickness Unit="in">0.077</Thickness>
        </Transition>

        <!-- Slotted PNC-3.90 boattail with 2.56 aft diam for LOC V2/R2 and Marvin's Wild Ride
             Part num not given on LOC site, so I created PNC-3.90-V2
             Original OR file length given was 8.11 which is too long, shape does not match.
             I matched the shape to a length of about 7.25 in
             In the 2010-2012 catalog there were both 3.90 and 5.38 size V2 and R2/ARO kits.
        -->
        <Transition>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>PNC-3.90-V2</PartNumber>
            <Description>Tail cone, polypropylene, PNC-3.90-V2, 4 slots, V2 type, 8.11"</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Mass Unit="oz">3.80</Mass>
            <Shape>OGIVE</Shape>
            <ForeOutsideDiameter Unit="in">4.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">3.88</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">3.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.56</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.0</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">7.25</Length>
            <Thickness Unit="in">0.06</Thickness>
        </Transition>

        <!--
            Tube couplers and Coupler Stiffeners
            LOC has two nested series of tube couplers, the TC and STC series.  The STC series fits
            *inside* the TC series and provides additional strength where desired.
            The STC stiffeners are made in ST-2.14 (54mm) size and up.

            There are several anomalies in the dimensions and mass info from Apogee and LOC:
                Length of STC-3.90 stiffener greater than length of matching coupler
                Oddly low wall thickness for TC-3.90
                Conflicts on length of TC-1.14, TC-5.38 and STC-5.38
        -->

        <!-- TC-1.14 coupler - Apogee gives length 2.5", mass 6.7 g / 0.23 oz. and a photo that
             shows it's about 2.5" long.  LOC gives length 2.0", clearly wrong or old version -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-1.14</PartNumber>
            <Description>Tube coupler, kraft, TC-1.14, 2.5"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">1.006</InsideDiameter>
            <OutsideDiameter Unit="in">1.127</OutsideDiameter>
            <Length Unit="in">2.5</Length>
        </TubeCoupler>

        <!-- TC-1.52 coupler - Apogee gives mass 9.3 g / 0.33 oz, too low if 4" long -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-1.52</PartNumber>
            <Description>Tube coupler, paper, TC-1.52, 4.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">1.398</InsideDiameter>
            <OutsideDiameter Unit="in">1.52</OutsideDiameter>
            <Length Unit="in">4.0</Length>
        </TubeCoupler>

        <!-- TC-2.14 coupler - Apogee gives mass 29.1 g / 1.02 oz, len 5.75"
             LOC gives len = 6.0" -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-2.14</PartNumber>
            <Description>Tube coupler, kraft, TC-2.14, 5.75"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">2.017</InsideDiameter>
            <OutsideDiameter Unit="in">2.138</OutsideDiameter>
            <Length Unit="in">5.75</Length>
        </TubeCoupler>

        <!-- STC-2.14 stiffener - Apogee gives 6.0" len (but LOC has 5.75"), mass 41.7 g / 1.46 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-2.14</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-2.14, 6.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">1.755</InsideDiameter>
            <OutsideDiameter Unit="in">2.000</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </TubeCoupler>

        <!-- TC-2.56 coupler - Apogee gives mass 23.5 g / 0.82 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-2.56</PartNumber>
            <Description>Tube coupler, kraft, TC-2.56, 6.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">2.479</InsideDiameter>
            <OutsideDiameter Unit="in">2.555</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </TubeCoupler>

        <!-- STC-2.56 stiffener - Apogee gives mass 50.2 g / 1.76 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-2.56</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-2.56, 6.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">2.245</InsideDiameter>
            <OutsideDiameter Unit="in">2.478</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </TubeCoupler>

        <!-- TC-3.00 coupler - Apogee gives mass 37.8 g / 1.32 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-3.00</PartNumber>
            <Description>Tube coupler, kraft, TC-3.00, 6.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">2.880</InsideDiameter>
            <OutsideDiameter Unit="in">2.990</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </TubeCoupler>

        <!-- STC-3.00 stiffener - Apogee gives mass 60.8 g / 2.13 oz, len 6", but
                                  I measured one at OD 2.873, ID 2.651, Len 5.5, mass 60.2 gm -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-3.00</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-3.00, 5.5"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">2.625</InsideDiameter>
            <OutsideDiameter Unit="in">2.878</OutsideDiameter>
            <Length Unit="in">5.5</Length>
        </TubeCoupler>

        <!-- TC-3.90 coupler - Apogee gives mass 32.4 g / 1.13 oz, len 6", correct density for 6" len
             2017 LOC site gives length as 8" which would make more sense given length of stiffy
             Wall thickness from apogee data (0.035) seems way too low -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-3.90</PartNumber>
            <Description>Tube coupler, kraft, TC-3.90, 8.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">3.814</InsideDiameter>
            <OutsideDiameter Unit="in">3.884</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </TubeCoupler>

        <!-- STC-3.90 stiffener - Apogee gives mass 74.9 g / 2.62 oz., len 7.5
             2017 LOC site also has len as 7.5" -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-3.90</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-3.90, 7.5"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">3.555</InsideDiameter>
            <OutsideDiameter Unit="in">3.785</OutsideDiameter>
            <Length Unit="in">7.5</Length>
        </TubeCoupler>

        <!-- LOC site gives TC-5.38 length as 9.0", Apogee gives 11.0", mass 111.4 g / 3.9 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-5.38</PartNumber>
            <Description>Tube coupler, kraft, TC-5.38, 11.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">5.272</InsideDiameter>
            <OutsideDiameter Unit="in">5.372</OutsideDiameter>
            <Length Unit="in">11.0</Length>
        </TubeCoupler>

        <!-- Length of STC-5.38 given as 9" on 2017 LOC site, Apogee gives 11.0" and mass 220.7g / 7.72 oz -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-5.38</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-5.38, 11.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">4.980</InsideDiameter>
            <OutsideDiameter Unit="in">5.240</OutsideDiameter>
            <Length Unit="in">11.0</Length>
        </TubeCoupler>

        <!-- Apogee doesn't list the shorter TC-7.51
             1989 LOC catalog gives length as 12".  2010 accessories catalog gives 11".
        -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-7.51-11</PartNumber>
            <Description>Tube coupler, kraft, TC-7.51-11, 11.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">7.398</InsideDiameter>
            <OutsideDiameter Unit="in">7.508</OutsideDiameter>
            <Length Unit="in">11.0</Length>
        </TubeCoupler>

        <!-- Apogee gives length 15.0" and mass 248g / 8.68 oz, 2017 LOC site gives 15" length
             2010 Accessories Catalog gives it a PN "TC-7.51-15" with 15" length
        -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>TC-7.51-15</PartNumber>
            <Description>Tube coupler, kraft, TC-7.51-15, 15.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler avg</Material>
            <InsideDiameter Unit="in">7.398</InsideDiameter>
            <OutsideDiameter Unit="in">7.508</OutsideDiameter>
            <Length Unit="m">15.0</Length>
        </TubeCoupler>

        <!-- Apogee gives length of 15.0" and mass 676g / 23.66 oz, and ID/OD.  LOC has length 14.0" -->
        <TubeCoupler>
            <Manufacturer>LOC Precision</Manufacturer>
            <PartNumber>STC-7.51</PartNumber>
            <Description>Tube coupler stiffener, kraft, STC-7.51, 15.0"</Description>
            <Material Type="BULK">Paper, kraft, LOC coupler stiffener avg</Material>
            <InsideDiameter Unit="in">6.995</InsideDiameter>
            <OutsideDiameter Unit="in">7.375</OutsideDiameter>
            <Length Unit="in">15.0</Length>
        </TubeCoupler>

        <!-- Launch lugs
             2017 site lists 1/4" and 1/2" lugs in 5" lengths.  Photo shows 3 sizes with a small
             one looking like 1/8" or 3/16".  Assuming .030 wall for 1/4" lug, .040 for 1/2" lug
        -->
        <!-- older LL-25 (2010 parts catalog and before) was 6" long -->
        <LaunchLug>
          <Manufacturer>LOC Precision</Manufacturer>
          <PartNumber>LL-25</PartNumber>
          <Description>Launch lug, paper, 1/4 x 6"</Description>
          <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
          <InsideDiameter Unit="in">0.275</InsideDiameter>
          <OutsideDiameter Unit="in">0.335</OutsideDiameter>
          <Length Unit="in">6.0</Length>
        </LaunchLug>
        <!-- 2017 version of 1/4" lug has no PN but is 5" long -->
        <LaunchLug>
          <Manufacturer>LOC Precision</Manufacturer>
          <PartNumber>LL-25-5in</PartNumber>
          <Description>Launch lug, paper, 1/4 x 5"</Description>
          <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
          <InsideDiameter Unit="in">0.275</InsideDiameter>
          <OutsideDiameter Unit="in">0.335</OutsideDiameter>
          <Length Unit="in">5.0</Length>
        </LaunchLug>
        <!-- older LL-50 (2010 parts catalog and before) was 6" long -->
        <LaunchLug>
          <Manufacturer>LOC Precision</Manufacturer>
          <PartNumber>LL-50</PartNumber>
          <Description>Launch lug, paper, 1/2 x 6"</Description>
          <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
          <InsideDiameter Unit="in">0.525</InsideDiameter>
          <OutsideDiameter Unit="in">0.605</OutsideDiameter>
          <Length Unit="in">6.0</Length>
        </LaunchLug>
        <!-- 2017 version of 1/2" lug has no PN but is 5" long -->
        <LaunchLug>
          <Manufacturer>LOC Precision</Manufacturer>
          <PartNumber>LL-50-5in</PartNumber>
          <Description>Launch lug, paper, 1/2 x 5"</Description>
          <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
          <InsideDiameter Unit="in">0.525</InsideDiameter>
          <OutsideDiameter Unit="in">0.605</OutsideDiameter>
          <Length Unit="in">5.0</Length>
        </LaunchLug>
        <!-- older LL-937 15/16" lug (2010 parts catalog and before) was 6" long
             I allow .030 clearance to ID and .060 wall but that is only an estimate
             No longer exists as of 2017 LOC site; large round lugs are not used much now -->
        <LaunchLug>
          <Manufacturer>LOC Precision</Manufacturer>
          <PartNumber>LL-937</PartNumber>
          <Description>Launch lug, paper, 15/16 x 6"</Description>
          <Material Type="BULK">Paper, kraft glassine, LOC tube avg</Material>
          <InsideDiameter Unit="in">0.967</InsideDiameter>
          <OutsideDiameter Unit="in">1.087</OutsideDiameter>
          <Length Unit="in">6.0</Length>
        </LaunchLug>

    </Components>
</OpenRocketComponent>
