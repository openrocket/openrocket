<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
Quest parts file for OpenRocket

Copyright 2018 by Dave Cook NAR 21953  caveduck17@gmail.com

See the file LICENSE in this distribution for license information.

This file is for use with OpenRocket and provides parts definitions for Quest Aerospace products.
It is completely different from the original Quest.orc distributed with OpenRocket 15.03 and
has many improvements:

    * Descriptions normalized to comma-separated list of attributes in increasing specificity
    * Material types all matched to generic_materials.orc
    * No materials listed that are not referenced in this file
    * Dimension units are now those specified in reference materials such as catalogs
    * No excess significant digits in dimensions
    * Unspecified or missing dimensions estimated by photogrammetry of drawings and photos
    * Mass overrides have been eliminated wherever feasible
    * All discovered errors fixed or documented if unresolvable

Quest is notable for maintaining the old MPC metric tube system.  The 2018 website only gives
dimensions for a few parts, but certain things are known from the MPC part system:
    * The quoted tube sizes refer to the outside diameter
    * Tube wall thickness is 0.50 mm for the smaller tubes

There is a little bit of units mayhem when dealing with Quest tube series.  When Quest specifies
dimensions on their website, they are always in decimal inches, but the primary tube series
outside diameters are nearly all exact millimeter sizes, e.g. 5.0, 10.0, 15.0, etc.  To keep
things readable I have used the metric tube diameters, but most lengths are in inches because
Quest specifies them that way and they are usually in round inch fractions.

Using this file:
    Drop this file in the OS-dependent location where OpenRocket looks for component databases:
        Windows:  $APPDATA/OpenRocket/Components/ (you need to set $APPDATA)
        OSX:      $HOME/Library/Application Support/OpenRocket/Components/
        Linux:    $HOME/.openrocket/Components/

    You need to restart OpenRocket after adding these files before the parts will be available.

DONE
====
Body tubes
Tube couplers
Engine blocks
Centering rings
Parachute and streamer
Nose cones (balsa)
Nose cones (plastic)
Transitions (plastic)
Launch lugs

TODO
====
none known

-->
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>

        <!-- Materials referenced in this file -->

        <!-- Phenolic kraft used for tube couplers -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, kraft, bulk</Name>
            <Density>943.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- Regular body tube -->
        <Material UnitsOfMeasure="kg/m3">
            <Name>Paper, spiral kraft glassine, Estes avg, bulk</Name>
            <Density>894.4</Density>
            <Type>BULK</Type>
        </Material>

        <!-- Regular balsa -->
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 7 lb/ft3</Name>
          <Density>112.0</Density>
          <Type>BULK</Type>
        </Material>

        <!-- injection molded plastic parts -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polystyrene, cast, bulk</Name>
            <Density>1050.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- clear payload tubes -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Mylar, bulk</Name>
            <Density>1390.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- lower density fiber for thrust rings etc. -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, bulk</Name>
            <Density>657.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- recent quest parachute measured at 1.1 mil, with printing -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 1.0 mil, bare</Name>
            <Density>0.0235</Density>
            <Type>SURFACE</Type>
        </Material>

        <!-- Streamer material, 2 mil polyethylene -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 2.0 mil, bare</Name>
            <Density>0.0470</Density>
            <Type>SURFACE</Type>
        </Material>

        <!-- Shroud line material - carpet thread -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Carpet Thread</Name>
            <Density>3.3E-4</Density>
            <Type>LINE</Type>
        </Material>

    </Materials>

    <Components>

        <!-- ============= -->
        <!-- Tube Couplers -->
        <!-- ============= -->
        <!-- Quest does not offer tube couplers smaller than 25mm on the 2009 nor 2018 websites.
             On the 2009 website the 25mm paper tube coupler is described as "fishpaper", which would
             be the same dark blue vulcanized paper tube used by Estes and others.  The 35mm paper
             coupler is "Kraft paper".  The photos were not archived on the wayback machine so
             I can't tell if these are the same or different materials.

             No couplers nor transitions had PNs on the 2009 Quest website.

             The original OR file had things seriously wrong for couplers with incorrect ODs, IDs and
             bogus mass overrides, some based on very low fidelity weights listed by Quest.
             We don't have good data on coupler thickness yet; until we know
             better I am adopting an 0.50 mm wall thickness, same as the body tubes.
             OR original file omitted the 30mm plastic coupler.  -->

        <!-- Q10100 25mm paper tube coupler is quoted on 2018 website as 1.6875" long, which is 1 11/16" -->
        <!-- *** original OR file had incorrect ID/OD (OD was 25mm, should be 24) and mass override of 28 grams -->
        <TubeCoupler>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10100</PartNumber>
            <Description>Tube coupler, paper, 25mm, 1.6875", PN Q10100</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="mm">23.0</InsideDiameter>
            <OutsideDiameter Unit="mm">24.0</OutsideDiameter>
            <Length Unit="in">1.6875</Length>
        </TubeCoupler>

        <!-- Q21085 plastic 30mm tube coupler is quoted as 2" long.  It has a short central section and
             molded in launch lug like its 35mm big brother. -->
        <TubeCoupler>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q21085</PartNumber>
            <Description>Tube coupler, plastic, 30mm, 2", PN Q21085</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <InsideDiameter Unit="mm">27.0</InsideDiameter>
            <OutsideDiameter Unit="mm">29.0</OutsideDiameter>
            <Length Unit="in">2.0</Length>
        </TubeCoupler>

        <!-- Q10104 35mm paper coupler had bogus mass override of 28 grams -->
        <TubeCoupler>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10104</PartNumber>
            <Description>Tube coupler, paper, 35mm, 2.5", PN Q10104</Description>
            <Material Type="BULK">Phenolic, kraft, bulk</Material>
            <InsideDiameter Unit="mm">33.0</InsideDiameter>
            <OutsideDiameter Unit="mm">34.0</OutsideDiameter>
            <Length Unit="in">2.5</Length>
        </TubeCoupler>

        <!-- Q21086 35mm plastic tube coupler is 2.125" long; has 0.125" center section and molded in launch lug.
             Quest website says it's injection molded. Wall thickness and mass unknown;
             assuming polystyrene material.  Ignoring the center section here
             since OpenRocket cannot model that, and it's very small.  Original OR file had a
             PN of 3535 and an ID of 30.9 mm, but it's hard to see the wall being 1.55mm -->
        <TubeCoupler>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q21086, 3535</PartNumber>
            <Description>Tube coupler, plastic, 35mm, 2.125", PN Q21086</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <InsideDiameter Unit="mm">32.0</InsideDiameter>
            <OutsideDiameter Unit="mm">34.0</OutsideDiameter>
            <Length Unit="in">2.125</Length>
        </TubeCoupler>


        <!-- ========== -->
        <!-- Body Tubes -->
        <!-- ========== -->

        <!-- MMX minimum diameter tube no longer exists on Quest website as of Jan 2021 -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q9527</PartNumber>
            <Description>Body tube, paper, 7mm, white, 6", PN Q9527</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">6.5</InsideDiameter>
            <OutsideDiameter Unit="mm">7.0</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </BodyTube>

        <!-- MMX motor tube has specs OD 0.276, ID 0.252, len 1.0".  I've kept them metric for
             consistency. -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10311</PartNumber>
            <Description>Body tube, paper, 7mm, white, MMX engine tube, 1.0", PN Q10311</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">6.4</InsideDiameter>
            <OutsideDiameter Unit="mm">7.0</OutsideDiameter>
            <Length Unit="in">1.0</Length>
        </BodyTube>

        <!-- MMX 10mm tube 6" long
             Dimensions on 2021 website given as ID 0.372", OD 0.392", 6.0" long -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q9528</PartNumber>
            <Description>Body tube, paper, 10mm, white, 6", PN Q9528</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.372</InsideDiameter>
            <OutsideDiameter Unit="in">0.392</OutsideDiameter>
            <Length Unit="in">6.0</Length>
        </BodyTube>

        <!-- 15mm body tube 30" long 
             As of Jan 2021 website, dimensions are given as ID 0.550", OD 0.590", 30.0" length-->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T153000</PartNumber>
            <Description>Body tube, paper, 15mm, white, 30"</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.550</InsideDiameter>
            <OutsideDiameter Unit="in">0.590</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- 18mm motor mount tube is a slip fit inside a 20mm body tube.
            Specs on 2021 website are ID 0.712", OD 0.738", length 2.6875"; using these as authoritative.
             Had no PN on 2009 website.
             I have an actual instance of this tube from a recent (2016?) Striker AGM kit.  It is
             yellow and very flimsy.  I get a wall thickness of 0.32 mm but it is hard to measure
             accurately as the tube is very soft and compressible.  Outside diameter over an engine block as
             support is 18.65mm.  Measured length is 2.687".  Wall .032 mm would give an ID of 18.01 mm.
             This is close enough to the factory specs to stick with those.  -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10303</PartNumber>
            <Description>Body tube, paper, 18mm, yellow, 2.6875", motor mount, PN Q10303</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.712</InsideDiameter>
            <OutsideDiameter Unit="in">0.738</OutsideDiameter>
            <Length Unit="in">2.6875</Length>
        </BodyTube>
        
        <!-- Legacy listing of 20mm tube in 30" length with old style PN -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T203000</PartNumber>
            <Description>Body tube, paper, 20mm, white, 30", PN T203000</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">19.1</InsideDiameter>
            <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- On 2021 Quest website there is a new 12" long 20mm tube in yellow with
             dimensions specified as ID 0.752", OD 0.788" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11210</PartNumber>
            <Description>Body tube, paper, 20mm, yellow, 12", PN Q11210</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.752</InsideDiameter>
            <OutsideDiameter Unit="in">0.788</OutsideDiameter>
            <Length Unit="in">12.0</Length>
        </BodyTube>

        <!-- On 2018 Quest website the only 20mm tube was listed as 11" long in white
             As of Jan 2021 dimensions are specified as ID 0.752", OD 0.788" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11214</PartNumber>
            <Description>Body tube, paper, 20mm, white, 11", PN Q11214</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.752</InsideDiameter>
            <OutsideDiameter Unit="in">0.788</OutsideDiameter>
            <Length Unit="in">11.0</Length>
        </BodyTube>

        <!-- 21mm motor mount tube for the old Quest D5-0 motor is still on 2021 website.  In the photo, it's red.
             Dimensions are now given as ID 0.795", OD 0.835" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10315</PartNumber>
            <Description>Body tube, paper, 21mm, red, D5 Motor Mount Tube, 3.65", PN Q10315</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.795</InsideDiameter>
            <OutsideDiameter Unit="in">0.835</OutsideDiameter>
            <Length Unit="in">3.65</Length>
        </BodyTube>

        <!-- 24mm motor mount aka "adapter" tube is on 2018-2021+ website, length 2.75", size given only as 1".
             ID/OD offset from 25mm tube with same offsets as 18mm motor tube, for which dimensions are given. -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11300</PartNumber>
            <Description>Body tube, paper, 24mm, white, 24mm MMT, 2.75", PN Q11300</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">23.25</InsideDiameter>
            <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
            <Length Unit="in">2.75</Length>
        </BodyTube>

        <!-- Clear 25mm x 4" payload tube exists on 2018+ website
             Dimensions are confusingly given as "PETG-CLEAR - cut in 4" lengths7/8" x 4" med. wall tube"
             This does confirm that the clear tube is PETG -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q28700</PartNumber>
            <Description>Body tube, plastic, 25mm, clear, 4.0"</Description>
            <Material Type="BULK">Mylar, bulk</Material>
            <InsideDiameter Unit="mm">24.0</InsideDiameter>
            <OutsideDiameter Unit="m">25.0</OutsideDiameter>
            <Length Unit="in">4.0</Length>
        </BodyTube>

        <!-- 25mm clear red tint payload tube no longer appears on + website.
             On the 2009 website is had no PN and was just called "Red Tint Payload Tube"
             The 25mm payload tube is shown as clear in 2018 -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>25mm_red_tint_payload_tube</PartNumber>
            <Description>Body tube, plastic, 25mm, red tint, 4.0"</Description>
            <Material Type="BULK">Mylar, bulk</Material>
            <InsideDiameter Unit="mm">24.0</InsideDiameter>
            <OutsideDiameter Unit="m">25.0</OutsideDiameter>
            <Length Unit="in">4.0</Length>
        </BodyTube>

        <!-- 25mm x 30" paper tube has old style PN in original OR file
             As of Jan 2021 website, dimensions are given as ID 0.950", OD 0.976", len 30" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T253000, Q9522</PartNumber>
            <Description>Body tube, paper, 25mm, white, 30", PN T253000/Q9522</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.950</InsideDiameter>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- 29mm MMT has heavier wall, dimensions given in inch units on 2018+ website -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10316</PartNumber>
            <Description>Body tube, paper, 29mm, white, 29mm MMT, 4.5", PN Q10316</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">1.14</InsideDiameter>
            <OutsideDiameter Unit="in">1.215</OutsideDiameter>
            <Length Unit="in">4.5</Length>
        </BodyTube>

        <!-- 30mm x 30" tube has old style PN in original OR file, 30" length no longer on website -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T303000, Q9523</PartNumber>
            <Description>Body tube, paper, 30mm, white, 30", PN T303000/Q9523</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">29.0</InsideDiameter>
            <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- 30mm x 5" tube is on 2018+ website, specified only as "Body Tube 5" 30mm(white)" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T303000, Q9523</PartNumber>
            <Description>Body tube, paper, 30mm, white, 5", PN Q11421</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">29.0</InsideDiameter>
            <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
            <Length Unit="in">5.0</Length>
        </BodyTube>

        <!-- 35mm x 30" tube no longer exists on 2018 website -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T353000</PartNumber>
            <Description>Body tube, paper, 35mm, white, 30", PN T353000</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="mm">34.0</InsideDiameter>
            <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- 35mm x 18" tube only exists on 2018+ website.  A sample acquired in 2018 has OD of 34.93 mm
             and ID of 34.01 mm.  Dimensions now specified in inch units as ID 1.340, OD 1.380, 18 long -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11503</PartNumber>
            <Description>Body tube, paper, 35mm, white, 18", PN Q11503</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">1.340</InsideDiameter>
            <OutsideDiameter Unit="in">1.380</OutsideDiameter>
            <Length Unit="in">18.0</Length>
        </BodyTube>

        <!-- 40mm x 30" tube
             On 2018+ website, dimensions are only given as 40mm diam, 30 inches long -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T403000, Q9525</PartNumber>
            <Description>Body tube, paper, 40mm, white, 30", PN T403000/Q9525</Description>
            <Material Type="BULK">Paper</Material>
            <InsideDiameter Unit="mm">39.0</InsideDiameter>
            <OutsideDiameter Unit="mm">40.0</OutsideDiameter>
            <Length Unit="in">30.0</Length>
        </BodyTube>

        <!-- 40mm 18" tube is only on the 2018+ website, not on 2009 site
             Dimensions now specified as ID 1.530", OD 1.570", 18" long -->      
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11511</PartNumber>
            <Description>Body tube, paper, 40mm, white, 18", PN Q11511</Description>
            <Material Type="BULK">Paper</Material>
            <InsideDiameter Unit="in">1.530</InsideDiameter>
            <OutsideDiameter Unit="in">1.570</OutsideDiameter>
            <Length Unit="in">18.0</Length>
        </BodyTube>
        
        <!-- 50mm x 24" has dimensions specified on 2009 and 2018+ websites
             Dimensions now specified as ID 1.921", OD 1.970" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>T502400, Q11700</PartNumber>
            <Description>Body tube, paper, 50mm, white, 24", PN T502400/Q11700</Description>
            <Material Type="BULK">Paper</Material>
            <InsideDiameter Unit="in">1.921</InsideDiameter>
            <OutsideDiameter Unit="in">1.970</OutsideDiameter>
            <Length Unit="in">24.0</Length>
        </BodyTube>

        <!-- 50mm x 18" tube is on 2018+ website, but not on 2009 website
             Dimensions now specified as ID 1.92", OD 1.97" -->
        <BodyTube>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q11701</PartNumber>
            <Description>Body tube, paper, 50mm, white, 18", PN Q11701</Description>
            <Material Type="BULK">Paper</Material>
            <InsideDiameter Unit="in">1.92</InsideDiameter>
            <OutsideDiameter Unit="in">1.97</OutsideDiameter>
            <Length Unit="in">18.0</Length>
        </BodyTube>


        <!-- ============= -->
        <!-- Engine Blocks -->
        <!-- ============= -->

        <!-- MicroMaxx thrust ring -->
        <EngineBlock>
          <Manufacturer>Quest</Manufacturer>
          <PartNumber>Q14005</PartNumber>
          <Description>Engine block, fiber, 7mm, 0.25" len, Micromaxx, PN Q14005</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="mm">3.75</InsideDiameter>
          <OutsideDiameter Unit="mm">6.3</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </EngineBlock>

        <!-- 18mm thrust ring.  Dimensions are specified on 2018 website. However a recent sample
             from the 2016-2081 timeframe has ID of 13.6mm and OD of 18.0 mm.  I'm going with the
             measured ID here since it's 0.6mm different than spec. -->
        <EngineBlock>
            <Manufacturer>Quest Aerospace</Manufacturer>
            <PartNumber>Q14000</PartNumber>
            <Description>Engine block, fiber, 18mm, 0.25" len, PN Q14000</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">13.6</InsideDiameter>
            <OutsideDiameter Unit="mm">18.0</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </EngineBlock>

        <!-- 20mm thrust ring for old D5 motors.  Still on 2018 website, dimensions not given. -->
        <EngineBlock>
            <Manufacturer>Quest Aerospace</Manufacturer>
            <PartNumber>Q14101</PartNumber>
            <Description>Engine block, fiber, 20mm, 0.25" len, old D5 motor, PN Q14101</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">13.8</InsideDiameter>
            <OutsideDiameter Unit="mm">20.2</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </EngineBlock>

        <!-- 29mm thrust ring.  Original OR file had OD only microns larger than ID.  Dimensions are given
             on the 2018 Quest website.  -->
        <EngineBlock>
            <Manufacturer>Quest Aerospace</Manufacturer>
            <PartNumber>Q14103</PartNumber>
            <Description>Engine block, fiber, 29mm, 0.25" len, PN Q14103</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">22.9</InsideDiameter>
            <OutsideDiameter Unit="m">28.9</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </EngineBlock>


        <!-- =============== -->
        <!-- Centering Rings -->
        <!-- =============== -->

        <!-- thickness of thin fiber centering rings mostly not specified by Quest, but a couple
             of them are so we use those values to fill in the blanks.
             The 2009 website has very inconsistent PNs for the centering rings.
        -->

        <!-- 7mm Micromaxx MMT to 10mm -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q14007</PartNumber>
            <Description>Centering ring, fiber, 7mm to 10mm MMX, 0.5", PN Q14007</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">7.06</InsideDiameter>
            <OutsideDiameter Unit="mm">9.34</OutsideDiameter>
            <Length Unit="in">0.5</Length>
        </CenteringRing>

        <!-- 21mm D5 MMT to 25mm -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2125, Q16036</PartNumber>
            <Description>Centering ring, fiber, 21mm D5 MMT to 25mm, 0.045", PN Q16036</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">21.45</InsideDiameter>
            <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 21mm D5 MMT to 30mm -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2130, Q16037</PartNumber>
            <Description>Centering ring, fiber, 21mm D5 MMT to 30mm, 0.045", PN Q16037</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">21.45</InsideDiameter>
            <OutsideDiameter Unit="mm">28.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 21mm D5 MMT to 35mm -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2135, Q16038</PartNumber>
            <Description>Centering ring, fiber, 21mm D5 MMT to 35mm, 0.045", PN Q16038</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">21.45</InsideDiameter>
            <OutsideDiameter Unit="mm">33.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 21mm D5 MMT to 40mm -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2140, Q16039</PartNumber>
            <Description>Centering ring, fiber, 21mm D5 MMT to 40mm, 0.045", PN Q16039</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">21.45</InsideDiameter>
            <OutsideDiameter Unit="mm">38.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 21mm D5 MMT to 50mm, 0.07" thick (per old OpenRocket file) -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2150, Q16040</PartNumber>
            <Description>Centering ring, fiber, 21mm D5 MMT to 50mm, 0.07", PN Q16040</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">21.45</InsideDiameter>
            <OutsideDiameter Unit="mm">48.75</OutsideDiameter>
            <Length Unit="in">0.07</Length>
        </CenteringRing>

        <!-- 18mm MMT to 25mm, specs given on 2018 website as ID .752", OD .942", len 0.25".
             Old PN CR24 confirmed -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR24, Q14050</PartNumber>
            <Description>Centering ring, fiber, 18mm MMT to 25mm, 0.25", PN Q14050</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">19.1</InsideDiameter>
            <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>

        <!-- 18mm MMT to 30mm.  Old PN CR29 confirmed. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR29, Q16001</PartNumber>
            <Description>Centering ring, fiber, 18mm MMT to 30mm, 0.045", PN Q16001</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">19.1</InsideDiameter>
            <OutsideDiameter Unit="mm">28.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 18mm MMT to 35mm, thickness specified on 2018 website as 0.045".  Old PN CR34 confirmed. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR34, Q16002</PartNumber>
            <Description>Centering ring, fiber, 18mm MMT to 35mm, 0.045", PN Q16002</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">19.1</InsideDiameter>
            <OutsideDiameter Unit="mm">33.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 18mm MMT to 40mm, thickness specified on 2018 website as 0.045".  Old PN CF39 confirmed. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR39, Q16007</PartNumber>
            <Description>Centering ring, fiber, 18mm MMT to 40mm, 0.045", PN Q16007</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">19.1</InsideDiameter>
            <OutsideDiameter Unit="mm">38.75</OutsideDiameter>
            <Length Unit="in">0.045</Length>
        </CenteringRing>

        <!-- 25mm to 30 mm, 0.5" long, dimensions given on 2018 website as ID .994", OD 1.134", 0.5" long.
             Old PN looks backwards, doesn't exist on 2009 website, need to check a later version. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2924, Q14022</PartNumber>
            <Description>Centering ring, fiber, 25mm to 30mm, 0.5", PN Q14022</Description>
            <Material Type="BULK">Paper</Material>
            <InsideDiameter Unit="mm">28.8</InsideDiameter>
            <OutsideDiameter Unit="mm">25.25</OutsideDiameter>
            <Length Unit="in">0.50</Length>
        </CenteringRing>

        <!-- 25mm to 35mm ring is specified as .050" thick on 2009 and 2018 websites (inconsistent with others).
             Old PN CR2534 confirmed. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR2534, Q16008</PartNumber>
            <Description>Centering ring, fiber, 25mm to 35mm, .05", PN Q16008</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">25.25</InsideDiameter>
            <OutsideDiameter Unit="mm">33.75</OutsideDiameter>
            <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- 25mm to 40mm - no such animal.  -->

        <!-- 25mm to 50mm.  Old PN not confirmed, does not exist on 2009 website. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR4924, Q16035</PartNumber>
            <Description>Centering ring, fiber, 25mm to 50mm, 0.07"</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">25.1</InsideDiameter>
            <OutsideDiameter Unit="mm">48.70</OutsideDiameter>
            <Length Unit="in">0.07</Length>
        </CenteringRing>

        <!-- 29mm MMT to 50mm.  Old PN not confirmed, not on 2009 website.  Thickness 0.070 specified on 2018 site. -->
        <CenteringRing>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>CR4929, Q16035</PartNumber>
            <Description>Centering ring, fiber, 25mm to 50mm, 0.07"</Description>
            <Material Type="BULK">Fiber, bulk</Material>
            <InsideDiameter Unit="mm">28.9</InsideDiameter>
            <OutsideDiameter Unit="mm">48.7</OutsideDiameter>
            <Length Unit="in">0.07</Length>
        </CenteringRing>

        <!-- ========== -->
        <!-- Nose Cones -->
        <!-- ========== -->

        <!-- Balsa nose cones -->

        <!-- 10mm Micromaxx nose cone is the only balsa nose cone.  Old PN unconfirmed. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>19990</PartNumber>
            <Description>Nose cone, balsa, 10mm, ogive, 1.0"</Description>
            <Material Type="BULK">Balsa, bulk, 7 lb/ft3</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">10.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">9.3</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">1.0</Length>
        </NoseCone>

        <!-- Plastic nose cones -->
        <!-- Original OR file has thickness of all the PNCs at 3.175mm, way too high.
             All had mass overrides, data source unknown but the masses look reasonable.
             Thicknesses set to match a sample of PNC35Nike that I have.  -->

        <!-- 15mm plastic nose, no base.  Original OR file had mass override 2.3 gm -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC15, Q20050</PartNumber>
            <Description>Nose cone, plastic, 15mm, white, ogive, 1.0", PN PNC15/Q20050</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">2.3</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">13.9</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">1.0</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC20 is black blowmolded 1-piece.  OR mass override was 2.8 gm -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC20, Q20075</PartNumber>
            <Description>Nose cone, plastic, 20mm, black, ogive, 2.5", PN PNC20/Q20075</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">2.8</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">19.0</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC25 exists on the 2009 site, but there is no 25mm nose cone on the 2018 website.
             OR file had mass override 7.1 gm and listed as 1-piece blowmold 3.125" long.
             No info on shoulder length. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC25</PartNumber>
            <Description>Nose cone, plastic, 25mm, ogive, 3.125", PN PNC25</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">7.1</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">23.9</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">3.125</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC30 is listed on 2009 site as a 3.25" ogive without base.  Original OR file
             has mass override of 14.2 gm, and 0.75" shoulder length, both unsubstantiated. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC30, Q20304</PartNumber>
            <Description>Nose cone, plastic, 30mm, black, ogive, 3.25", PN PNC30/Q20304</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">14.2</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
            <ShoulderDiameter Unit="m">28.9</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">3.25</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC35 is listed on 2009 site as 4.25" one piece blowmolded ogive; OR file has mass 10.2 gm and
             shoulder length 0.75" -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC35, Q20200</PartNumber>
            <Description>Nose cone, plastic, 35mm, white, ogive, 4.25", PN PNC35/Q20200</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">10.2</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">33.9</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.25</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC35B2 is 4.5" white blowmolded X-15 nose cone, white in 2018 site. OR file has mass 12.2 gm
             and shoulder length 0.75".  Might be able to pull real shoulder length from 2018 site photo.  -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC35B2, Q20222</PartNumber>
            <Description>Nose cone, plastic, 35mm, white, X-15 shape, 4.5", PN PNC35B2/Q20222</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">12.2</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">33.9</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.5</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC35Nike is an 8.0" 2-piece clamshell white injection molded cone with separate tip.  Length given as
             8" on 2009 site.  OR file has mass 25.5 gm, shoulder length 0.75".
             I have acquired an actual example that is missing the tip piece.  Shoulder length is 0.75",
             exposed length without the tip is 7.925", and shoulder diam on the ribbed shoulder is 33.70mm
             when avoiding the ribs, and 33.9 mm across the ribs.  Mass is 23.0 gm without the tip.  The missing
             tip is well under 1 gram so I'm putting the total mass at 23.5 gm based on this measurement. Wall
             thickness samples run from 1.25 to 1.40 mm so adopting 1.3 mm here. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC35Nike, Q20201</PartNumber>
            <Description>Nose cone, plastic, 35mm, white, Nike Smoke, 8.0", PN PNC35Nike/Q20201</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">23.5</Mass>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">33.7</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">8.0</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC35Egg / Q20224 is a black blowmolded cut-apart egg capsule.  2018 site says max diam is 1.9"
             with length 5.0".  Shoulder length in the 2018 photo is about 1".  No data on the mass but
             I'm estimating it around 25 gm. From the shape it would be more accurately modeled as
             a ~3.5" ogive nose with a 1.5" conical rear transition.  For here I'm just making an ogive
             with the right mass...drag will be somewhat off. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC35Egg, Q20224</PartNumber>
            <Description>Nose cone, plastic, 35mm, black, egg capsule, 5.0", PN PNC35Egg/Q20224</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">25.0</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">35.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">33.9</ShoulderDiameter>
            <ShoulderLength Unit="in">1.0</ShoulderLength>
            <Length Unit="in">5.0</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC40 is a 2.5" white one piece blowmoded ellipsoid.  OR file has
             mass 11.3 gm, shoulder len 0.75" -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC40, Q20400</PartNumber>
            <Description>Nose cone, plastic, 40mm, white, ellipsoid, 2.5", PN PNC40/Q20400</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">11.3</Mass>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="mm">40.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">38.8</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- PNC50 is a 6.0" white blowmold.  OR file had mass 42.5 gm, shoulder len 2.25".  Data on 2018 site
             actually states that the shoulder is 2.5" long. -->
        <NoseCone>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>PNC50, Q20250</PartNumber>
            <Description>Nose cone, plastic, 50mm, white, ogive, 6.0", PN PNC50/Q20250</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">42.5</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="mm">50.0</OutsideDiameter>
            <ShoulderDiameter Unit="mm">48.7</ShoulderDiameter>
            <ShoulderLength Unit="in">2.5</ShoulderLength>
            <Length Unit="in">6.0</Length>
            <Thickness Unit="mm">1.3</Thickness>
        </NoseCone>

        <!-- =========== -->
        <!-- Transitions -->
        <!-- =========== -->

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>2520, Q21057</PartNumber>
            <Description>Transition, plastic, 20mm to 25mm, black, increasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">5.7</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">19.0</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.75</AftShoulderLength>
            <Length Unit="in">0.5</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>2520 [R], Q21057 [R]</PartNumber>
            <Description>Transition, plastic, 25mm to 20mm, black, decreasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">5.7</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">19.0</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.75</AftShoulderLength>
            <Length Unit="in">0.5</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <!-- 25-30mm transition 21061 weighs 7.7 gm.  Exposed length is 0.55", and houlder max diameters 
             are 23.34 mm and 28.75 mm, measured on a new sample obtained direct from Quest in late 2018.
             The part is blow-molded in black, and the small shoulder is measurably
             out of round; the minimum diameter is about 23.12 mm.  Both ends of this transition are
             closed and have molded-in loops for shock line attachment.  The functional cylindrical
             part of the smaller shoulder is 0.75" and 0.70" for the larger.  There is a deep groove
             on each end that allows the smaller section of the shoulder with the molded loop to
             be optionally cut off. -->
        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3025 [R], Q21061 [R]</PartNumber>
            <Description>Transition, plastic, 30mm to 25mm, black, decreasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">7.7</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">28.75</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.79</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">23.34</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.75</AftShoulderLength>
            <Length Unit="in">0.55</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3025, Q21061</PartNumber>
            <Description>Transition, plastic, 25mm to 30mm, black, increasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">7.7</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">23.34</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">28.75</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.70</AftShoulderLength>
            <Length Unit="in">0.55</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <!-- Mass from OR file is probably wrong, identical to spec for 3025 transition.  2018 site says 35mm end
             shoulder is 0.50", on 25mm end shoulder is 0.375" -->
        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3525 [R], Q21052 [R]</PartNumber>
            <Description>Tranasition, plastic, 35mm to 25mm, white, decreasing, 0.75" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">7.1</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">35.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">33.9</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.50</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.375</AftShoulderLength>
            <Length Unit="in">0.75</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3525, Q21052</PartNumber>
            <Description>Transition, plastic, 25mm to 35mm, white, increasing, 0.75" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">7.1</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.375</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">35.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">33.9</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.50</AftShoulderLength>
            <Length Unit="in">0.75</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <!-- NOTE - there are two 35/30 reducers listed on both the 2009 and 2018 sites.  They are not the
             same.  Nominally, Q21056 has .75" exposed length and .5 and .375 shoulders, while Q21071 has
             0.5" exposed length and 0.75" shoulders on both ends.

             There is some contradictory data about colors of some parts.  Close inspection of the Quest
             website as of Jan 2019 shows that various photos of the transitions are wrong.  For example,
             the picture for the 21052 35/25 mm reducer is clearly of the 21056 35/30mm part.  Likewise,
             the photo for the 21056 35/30mm reducer has obviously wrong proportions and is too small on
             the background grid; it looks like another photo of the 25/20 mm part.

             The website description and photo for 21056 indicate black color, but the photo is obviously
             of the wrong part - the exposed length and shoulder proportions are toally wrong.  A newly
             purchased part in late 2018 is white, as is another example from a Striker AGM kit.  
             On a 2018 invoice for a purchased part, the Q21056 description is just "35mm/30mm Reducer",
             while for Q21071 it's "35mm/30mm Reducer Black".  I also have a 21071 purchased at the same
             time, and it's a black blow-molded part.

             Given these data errors, I'm treating the website specs as less than authoritative.
             -->

        <!-- 35/30 transition Q21056 per 2018 website is nominally 0.75" long, shoulders 0.50" on 35mm end 
             and 0.375" on 30mm end.  This is an injection molded part and has longitudinal ridges along the
             exposed length, as well as four deeper valleys at 90 degree intervals.  Internally there are 8
             good-sized ribs around the inside of the 35 mm end that extend forward for about 2/3 of the
             overall length.

             On both my 21056 examples, the T35 shoulder diam is 33.66mm and the T30 shoulder diam is 28.66 mm.
             Shoulder lengths are 0.346" for the 30mm end and 0.480" for the 35mm end; exposed length
             is 0.75".  Masses are 6.3 and 6.4 gm.  Thickness of the shoulders averages about 0.040" on the
             30 mm end and 0.057" on the 35 mm end.

             The max diam of the ribbed section is 1.343" = 34.13 mm at the 35mm end, and the diam at the 30mm end
             is 1.168" = 29.66 mm.  This matches the impression of an obvious under-sized match to the T35 tube.

             Actual values from measured samples are used here.  As for color, given the website photo
             errors discussed above, I am only listing this as white until proven otherwise.
             -->
        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3530 [R], Q21056 [R]</PartNumber>
            <Description>Transition, plastic, 35mm to 30mm, white, decreasing, 0.75" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">6.4</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">35.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">33.66</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.48</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">28.66</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.346</AftShoulderLength>
            <Length Unit="in">0.75</Length>
            <Thickness Unit="in">0.050</Thickness>
        </Transition>

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3530, Q21056</PartNumber>
            <Description>Transition, plastic, 30mm to 35mm, white, increasing, 0.75" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">6.4</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">28.66</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.346</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">35.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">33.66</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.48</AftShoulderLength>
            <Length Unit="in">0.75</Length>
            <Thickness Unit="in">0.050</Thickness>
        </Transition>

        <!-- 35/30 short transition per 2009 website (maps to modern PN Q21071) is nominally 0.50" long,
             with shoulders 0.75" on both ends.  I have an actual 21071, purchased late 2018.  It is
             black blow-molded plastic, with the molded-loop sections on each end and grooves to facilitate
             cutting them off.

             Actual mass: 10.9 gm (without cutting off the ends)  Measured dimensions: 30mm shoulder end
             diam 28.16 to 28.60 mm, 35mm shoulder diam 33.18 to 33.80 mm, cylindrical shoulder lengths
             0.772" on 30mm end, 0.697" on 35mm end. Exposed length 0.53".  There's no way to measure
             the thickness of the molding without cutting off an end. -->
        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3530_short [R], Q21071 [R]</PartNumber>
            <Description>Transition, plastic, 35mm to 30mm, black, decreasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">10.9</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">35.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">33.8</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.70</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">28.6</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.77</AftShoulderLength>
            <Length Unit="in">0.53</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>

        <Transition>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>3530_short, Q21071</PartNumber>
            <Description>Transition, plastic, 30mm to 35mm, black, increasing, 0.5" length</Description>
            <Material Type="BULK">Polystyrene, bulk</Material>
            <Mass Unit="g">10.9</Mass>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="mm">28.6</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.77</ForeShoulderLength>
            <AftOutsideDiameter Unit="mm">35.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="mm">33.8</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.70</AftShoulderLength>
            <Length Unit="in">0.53</Length>
            <Thickness Unit="mm">1.0</Thickness>
        </Transition>


        <!-- ======================== -->
        <!-- Parachutes and Streamers -->
        <!-- ======================== -->

        <!-- PN 7810 14" parachute - acquired a sample of this.  Thickness including printing is 1.1 mil  -->
        <Parachute>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>7810</PartNumber>
            <Description>Parachute, plastic, 1.0 mil, 14" diam</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">14.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">13.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <Streamer>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>7811</PartNumber>
            <Description>Streamer, plastic, 2.0 mil, 3 x 36"</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 2.0 mil, bare</Material>
            <Length Unit="in">36.0</Length>
            <Width Unit="in">3.0</Width>
            <Thickness Unit="in">0.002</Thickness>
        </Streamer>

        <!-- =========== -->
        <!-- Launch Lugs -->
        <!-- =========== -->

        <LaunchLug>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10000</PartNumber>
            <Description>Launch lug, paper, 1/8 x 1.0 in</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.156</InsideDiameter>
            <OutsideDiameter Unit="in">0.173</OutsideDiameter>
            <Length Unit="in">1.0</Length>
        </LaunchLug>

        <LaunchLug>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10001</PartNumber>
            <Description>Launch lug, paper, 1/8 x 2.0 in</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.156</InsideDiameter>
            <OutsideDiameter Unit="in">0.173</OutsideDiameter>
            <Length Unit="in">2.0</Length>
        </LaunchLug>

        <LaunchLug>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10009</PartNumber>
            <Description>Launch lug, paper, 1/8 x 2.75 in</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.156</InsideDiameter>
            <OutsideDiameter Unit="in">0.173</OutsideDiameter>
            <Length Unit="in">2.75</Length>
        </LaunchLug>

        <LaunchLug>
            <Manufacturer>Quest</Manufacturer>
            <PartNumber>Q10008</PartNumber>
            <Description>Launch lug, paper, 1/8 x 3.0 in</Description>
            <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
            <InsideDiameter Unit="in">0.156</InsideDiameter>
            <OutsideDiameter Unit="in">0.173</OutsideDiameter>
            <Length Unit="in">3.0</Length>
        </LaunchLug>

    </Components>
</OpenRocketComponent>
