<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
Estes classic parts file for OpenRocket

by Dave Cook  NAR 21953  caveduck17@gmail.com 2014-2017

This file provides separate entries for each part per the original Estes part numbering scheme with
prefixes and suffixes such as "BT-20J".  The newer non-significant all-numeric part numbers are also
included in the PartNumber and Description string where known.  Be aware that there are many
conflicts in the Estes assigned PNs.

Many items are based on part definitions from the stock OpenRocket component files, but
with various cleanups and fixes:

    * Descriptions normalized to comma-separated list of attributes in increasing specificity
    * Material types all matched to generic_materials.orc
    * Dimension units changed to those specified in reference materials such as catalogs
    * Excess significant digits removed from dimensions; generally kept 3-4 significant figures
    * Numerous dimension/mass/material/part number errors fixed (sorry, WAY too many to list)
    * Mass overrides have been eliminated wherever feasible

Using this file:
    Drop this file in the OS-dependent location where OpenRocket looks for component databases:
        Windows:  $APPDATA/OpenRocket/Components/ (you need to set $APPDATA)
        OSX:      $HOME/Library/Application Support/OpenRocket/Components/
        Linux:    $HOME/.openrocket/Components/

    You need to restart OpenRocket after adding these files before the parts will be available.

    When you start up OpenRocket and use the "from Database..." option when selecting components,
    you will see a LOT more Estes components than before.  Unfortunately unless you do some advanced
    surgery to remove the built-ins from your OpenRocket jar, you will have some duplicating entries
    from the baked-in OpenRocket components.  You can easily recognize the new components from this
    file because they will have much longer descriptions.

Known issues:
         Parallel wound material for BT-30 not supported yet (need density)
         Unusual nose cone shapes such as Honest John and Spaceman noses not supported by OpenRocket code
         All Estes balsa assumed same density
-->
<!--     -->
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
        <!-- Would be nice to be able to 'include' generic_materials.orc -->
        <!-- Materials have to be in this file, and can only be referenced by components in this file.
             Unfortunately they will not show up in any dropdown menus.
             The UnitsOfMeasure attribute for materials is IGNORED.  See preset/xml/MaterialDTO.java -
             it just calls a default constructor that sets the units to default.
             -->

        <!--
            Estes catalog weights for balsa parts imply a typical density of about 10 lb/ft3 for smaller cones
            and about 8 lb/ft3 for larger ones, but they are very inconsistent.
        -->
        <Material UnitsOfMeasure="kg/m3">
          <Name>Balsa, bulk, Estes typical</Name>
          <Density>160.2</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
          <Name>Balsa, bulk, 10lb/ft3</Name>
          <Density>160.2</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
          <Name>Balsa, bulk, 8lb/ft3</Name>
          <Density>128.1</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
          <Name>Balsa, bulk, 7lb/ft3</Name>
          <Density>112.0</Density>
          <Type>BULK</Type>
        </Material>

        <!-- Average paper tube density for Estes kraft+glassine tubes -->
        <!--
          Here are the computed densities of Estes tubing, based on 1975 and 1985 catalog specs
           BT-5    0.9745   g/cm3    0.013" wall
           BT-20   0.9350            0.013" wall
           BT-50   0.9221            0.013" wall
           BT-55   0.7510            0.021" wall
           BT-60   0.8655            0.021" wall
           BT-70   0.8869            0.021" wall
           BT-80   0.4787            0.021" wall     bogus catalog mass for this tube
           BT-80   0.8944            0.021" wall     *if they had weighed the correct length tube
           BT-101  0.8006            0.021" wall

           Throwing out the BT-55 value, the average density is 894.4 kg/m3.

           Notice that BT-55 and BT-80 are outliers.

           Presuming that glassine is denser than the kraft layer, the BT-55 density ought
           to be at least as high as the BT-60 value.  At this point I don't have any
           explanation for the BT-55 anomaly; we need to weigh a real one.

           The catalog value of .637 oz for the weight of a 14.2" BT-80 is drastically too low.
           However the density comes out to a near perfect 0.8944 g/cm3 if you assume that they
           mistakenly weighed the 7.6" long BT-80 that was used in the Saturn V kit.

        -->
        <Material UnitsOfMeasure="kg/m3">
            <Name>Paper, spiral kraft glassine, bulk</Name>
            <Density>799.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Paper, spiral kraft glassine, Estes avg, bulk</Name>
            <Density>894.4</Density>
            <Type>BULK</Type>
        </Material>
        <!-- fiber for centering rings from built-in semroc file -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, bulk</Name>
            <Density>657.0</Density>
            <Type>BULK</Type>
        </Material>
        

        <Material UnitsOfMeasure="kg/cm3">
            <Name>Polystyrene, cast, bulk</Name>
            <Density>1050.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Polyethylene, HDPE, bulk</Name>
            <Density>950.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Polyethylene, LDPE, bulk</Name>
            <Density>925.0</Density>
            <Type>BULK</Type>
        </Material>
        
        <!-- Mylar (polyester) density quoted by DuPont as 1.39 g/cc -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Mylar, bulk</Name>
            <Density>1390.0</Density>
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
        
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.7 oz actual</Name>
            <Density>0.05764</Density>
            <Type>SURFACE</Type>
        </Material>

        <!-- LINE materials for parachute shroud lines -->

        <Material UnitsOfMeasure="kg/m">
            <Name>Carpet Thread</Name>
            <Density>3.3E-4</Density>
            <Type>LINE</Type>
        </Material>


    </Materials>
    <Components>
      <!-- Body Tubes -->
      <!--
           *** fix up HBT-20 ***
           
           HEAVY WALL TUBES:

           In the Brohm body tube kit reference, various PNs and lengths are given.  In a
           note on p. 51, he notes that HBT-1000 replaced the older BT-50H / HBT-50 type.
           Brohm also says that HBT and HD are equivalent, and I believe that BTH is yet
           another synonym.  SEMROC gives sizes for BTH-50, BTH-52, and BTH-80.

           Also see Apogee newsletter 09 indicating the (newer) HBT series was created by Mike
           Dorffler and were named for OD of tube, notably HBT-1090 and HBT-3000 used in
           Pro Series Patriot, which is too new to appear in the Brohm index.

        .  The "OD-specifying" PNs seen in the Brohm list that are
           not found in Appendix II are:
               HBT-1000 (G) clear from context it's green
               85878 HBT-1090 (Slt) slotted for #2114 Corkscrew.  Brohm doesn't note the slots.
           
           To date, we have no full dimensions for these tubes.  But it is safe to infer
           that, due to use as motor mount tubes, the ID's of, say, BT-50 and HBT-50 are
           identical, while the OD is increased.  Likewise, for the HBT-xxx and HBT-xxxx,
           we are given the OD by the designator.

           HBT-760 is nearly identical to Centuri ST-7 (ID .715, OD .759, .022 wall)
           HBT-1000 would have .008 bigger OD than SEMROC's OD for BTH-50, and probably
           the same .950 ID, implying a .025 wall so it can be 24mm MMT.

           HBT-1090 is a bit of a puzzle.  OD of 1.090 is .010 smaller than BT-51.  But
           what are the ID and wall thickness?  If wall is .021, ID would be 1.048.  The
           only functional fit for that is slip fit over Centuri ST-10.

           HBT-1800 and HBT-2000 could be similar to the Centuri #18 and #20 tubes, which
           had .020 wall, but those have 1.800 and 2.000 ID's, not OD as mentioned in the
           Apogee newsletter.

           The Estes Nose Cone/Kit List Ref rev Feb 2008 on psc473.org (Pittsburth Space
           Command) indicates various HBT-xxxx rockets exist, using PNC-1090 with PNs
           072630, 072634, 072636 and 072663 in different (unspecified #$%!) colors

           The Brohm kit reference lists for HBT-5/20/50:

           31217 HBT-20 9.25" long for #2077 Sky Winder.
           31291 HBT-50 9.0" long for #2112 Transwing Super Glider
           ????? HBT-5 1.688" long MMT for #2122 RediRoc Intruder (Brohm: Invader) and
           #2123 RediRoc Raider - both of these are saucers.  MMT was said to be plugged
           by John Lee on RocketReviews.  #2123 conflicts with Eggscaliber, a rare kit PN
           clash.  See 1996 Estes catalog.
           
           OTHER UNUSUAL TUBE DESIGNATORS

           There is an "SBT" designator seen in certain kits.  Brohm describes these as
           meaning "special" body tube, and it appears that the number gives the OD of the tube.
           See Brohm body tube reference Appendix II.

              Used in #1287 LTV Scout:
                SBT-139BJ  PN 30488  2.0" long
                SBT-123BE  PN 30446  2.5" long
                SBT-127GC  PN 30447  7.26" long

              Used in #1288 Starlab and #1369 S.S. Cassiopeeia, #1383 Hyperion:
                SBT-394AJ  PN 30449  1.0" long  Brohm notes same OD as BT-101 but with 0.042" wall
                Listed here alongside the BT-101 since it's a variant of that size.

              Used in #2048 Saturn 1B  (SBT designators created by Brohm in keeping with earlier SBTs)
                Brohm notes that in the kit instructions they are identified only by part numbers
                SBT-267  PN 30438   1.375" long
                SBT-705  PN 30329   7.813" long  (stage 1 tanks)
                SBT-262  PN 30436   7.625" long
                SBT-116  PN 30379  11.00" long
      -->

      <!-- Special Body Tube sizes.  Larger sizes presumed to be 0.021 wall, smaller ones .013 wall -->

      <!-- PN 030329 SBT-705 from #2048 Saturn 1B, using 0.013 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-705, 030329</PartNumber>
        <Description>Body tube, SBT-705, 7.813 in., PN 030329</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.679</InsideDiameter>
        <OutsideDiameter Unit="in">0.705</OutsideDiameter>
        <Length Unit="in">7.813</Length>
      </BodyTube>

      <!-- SBT-116 used in #2048 Saturn 1B, using 0.013 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-116, 030379</PartNumber>
        <Description>Body tube, SBT-116, 11.0 in., PN 030379</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.134</InsideDiameter>
        <OutsideDiameter Unit="in">1.160</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>

      <!-- SBT-261 used in #2048 Saturn 1B, using 0.021 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-261, 030436</PartNumber>
        <Description>Body tube, SBT-261, 7.625 in., PN 030436</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.568</InsideDiameter>
        <OutsideDiameter Unit="in">2.610</OutsideDiameter>
        <Length Unit="in">7.625</Length>
      </BodyTube>

      <!-- SBT-267 used in #2048 Saturn 1B, using 0.021 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-267, 030438</PartNumber>
        <Description>Body tube, SBT-267, 1.375 in., PN 030438</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.628</InsideDiameter>
        <OutsideDiameter Unit="in">2.670</OutsideDiameter>
        <Length Unit="in">1.375</Length>
      </BodyTube>


      <!-- SBT-123BE from #1287 LTV Scout, using 0.021 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-123BE, 030446</PartNumber>
        <Description>Body tube, SBT-123BE, 2.5 in., PN 030446</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.188</InsideDiameter>
        <OutsideDiameter Unit="in">1.230</OutsideDiameter>
        <Length Unit="in">2.5</Length>
      </BodyTube>

      <!-- SBT-127GC from #1287 LTV Scout, using 0.021 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-127GC, 030447</PartNumber>
        <Description>Body tube, SBT-127GC, 7.26 in., PN 030447</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.228</InsideDiameter>
        <OutsideDiameter Unit="in">1.270</OutsideDiameter>
        <Length Unit="in">7.26</Length>
      </BodyTube>

      <!-- SBT-139BJ from #1287 LTV Scout, using 0.021 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-139BJ, 030488</PartNumber>
        <Description>Body tube, SBT-139BJ, 2.0 in., PN 030488</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.346</InsideDiameter>
        <OutsideDiameter Unit="in">1.390</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>


      <!-- BT-5 -->

      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5, 030302</PartNumber>
        <Description>Body tube, BT-5, 18 in., PN 030302</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- BT-5 12.25" PN ????? used in #2009 Rain Maker -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_12.25in</PartNumber>
        <Description>Body tube, BT-5, 12.25 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">12.25</Length>
      </BodyTube>
      <!-- BT-5 9.0" PN ????? used in #0880 Skinny Mini, #0885 Sprite, #0891 Prime Number Explorer -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_9.0in</PartNumber>
        <Description>Body tube, BT-5, 9.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <!-- black 9.0" BT-5 PN 030293 used in #2159 Fireflash -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_9.0in_black, 030293</PartNumber>
        <Description>Body tube, BT-5, 9.0 in., black, PN 030293</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <!-- silver 9.0" BT-5 PN 030292 used in #0835 Nike-Arrow -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_9.0in_silver, 030292</PartNumber>
        <Description>Body tube, BT-5, 9.0 in., silver, PN 030292</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <!-- black 8.0" BT-5 PN 030291 used in #0834 X-Ray -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_8.0in_black, 030291</PartNumber>
        <Description>Body tube, BT-5, 8.0 in., black, PN 030291</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- silver 8.0" BT-5 PN ???? used in #0886 Gnome (silver version) -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_8.0in_silver</PartNumber>
        <Description>Body tube, BT-5, 8.0 in., silver</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- white 8.0" WBT-5 PN 030301 used in #0886 Gnome (white version) and #0887 Leprechaun -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_8.0in_white, 030301</PartNumber>
        <Description>Body tube, WBT-5, 8.0 in., white, PN 030301</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- PN 030306 overload for 6.5in and 5.1 in parts -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_6.5in, 030306</PartNumber>
        <Description>Body tube, BT-5, 6.5 in., PN 030306</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">6.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_6.0in, 030303</PartNumber>
        <Description>Body tube, BT-5, 6.0 in., PN 030303</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_5.5in_beige, 030307</PartNumber>
        <Description>Body tube, BT-5, beige, 5.5 in., PN 030307</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <!-- See note below about ambiguity between the 5.0" and BT-5P 5 3/32" tube -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5P, 030306</PartNumber>
        <Description>Body tube, BT-5, 5.1 in., PN 030306</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">5.1</Length>
      </BodyTube>
      <!--
          BT-5 5.0" PN ???? in used in #0870 Pulsar and #0871 Vector, #2015 Strike Fighter
          Brohm discussion suggests possibly intended by Estes as same part as BT-5P which was 5 5/32"
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_5.0in</PartNumber>
        <Description>Body tube, BT-5, 5.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_4.0in, 030305</PartNumber>
        <Description>Body tube, BT-5, 4.0 in., PN 030305</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>
      <!-- BT-5 3.75" PN ???? used in #2019 Titan IIIE -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_3.75in</PartNumber>
        <Description>Body tube, BT-5, 3.75 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">3.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5CJ, 030310</PartNumber>
        <Description>Body tube, BT-5, 3.0 in., PN 030310</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">3.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5BJ, 030304</PartNumber>
        <Description>Body tube, BT-5, 2.0 in., PN 030304</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>
      <!-- PN 030304 re-used for #0802 Quark tube at 1.75" vs 2.0" for regular BT-5BJ -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.75in, 030304</PartNumber>
        <Description>Body tube, BT-5, 1.75 in., PN 030304</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.75</Length>
      </BodyTube>
      <!-- #0810 Swift and #0809 Gauchito uses a 1.75" BT-5 with PN 030290 (possible fix after Quark?) -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.75in, 030290</PartNumber>
        <Description>Body tube, BT-5, 1.75 in., PN 030390</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.75</Length>
      </BodyTube>
      <!-- BT-5 1.625" used in Bandito -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.625in, 030309</PartNumber>
        <Description>Body tube, BT-5, 1.625 in., PN 030309</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.625</Length>
      </BodyTube>
      <!-- BT-5 1.56" PN ????? used in #0881 Mini Mars Lander -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.56in</PartNumber>
        <Description>Body tube, BT-5, 1.56 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.56</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5T, 030308</PartNumber>
        <Description>Body tube, BT-5, 1.5 in., PN 030308</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </BodyTube>
      <!--
          BT-5 1.434" used in #1202 Mini Meanie and #1203 Freaky Flyer
          The Brohm reference gives the length as 1.438" in one place with same PN 031174
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.434in, 031174</PartNumber>
        <Description>Body tube, BT-5, 1.434 in., PN 031174</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.434</Length>
      </BodyTube>
      <!-- 1.375" 030468 used in #0807 Lucky Seven -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.375in, 030468</PartNumber>
        <Description>Body tube, BT-5, 1.375 in., PN 030468</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.375</Length>
      </BodyTube>
      <!-- 1.375" 030311 used in #1298 X-Wing -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5XW, 030311</PartNumber>
        <Description>Body tube, BT-5, 1.375 in., PN 030311</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.375</Length>
      </BodyTube>
      <!-- 1.375" 030295 used in #2077 Sky Winder -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_1.375in, 030295</PartNumber>
        <Description>Body tube, BT-5, 1.375 in., PN 030295</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">1.375</Length>
      </BodyTube>
      <!-- 0.75" 30409 used in #2109 Renegade -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-5_0.75in, 30409</PartNumber>
        <Description>Body tube, BT-5, 0.75 in., PN 30409</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.541</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </BodyTube>

      <!--
          HBT-5 1.688" used by #2122 Invader, #2123 Raider.  PN not known.
          *** exact OD unknown, estimating .021 wall ***
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BTH-5, HBT-5, 1.688in</PartNumber>
        <Description>Body tube, HBT-5/BTH-5, heavy wall, 1.688 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.515</InsideDiameter>
        <OutsideDiameter Unit="in">0.557</OutsideDiameter>
        <Length Unit="in">1.688</Length>
      </BodyTube>

      <!-- BT-10 Mylar -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-10, 30312</PartNumber>
        <Description>Body tube, BT-10, 9.0 in., PN 30312</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.720</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-10H</PartNumber>
        <Description>Body tube, BT-10, 3.062 in.</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.720</OutsideDiameter>
        <Length Unit="in">3.062</Length>
      </BodyTube>

      <!-- BT-20 -->

      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20, 30316</PartNumber>
        <Description>Body tube, BT-20, 18 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- BT-20P 13.75" used in #1931 Delta Wedge -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20P, 30333</PartNumber>
        <Description>Body tube, BT-20, 13.75 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">13.75</Length>
      </BodyTube>
      <!-- PBT-20 18" for gliding BOMARC KC-5/#0654, PN unknown, may not have one -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PBT-20</PartNumber>
        <Description>Body tube, BT-20, 18 in., punched, BOMARC</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- Alt PN 30328 for 12" BT-20 used in #2184 Metor Masher -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20L, 30330, 30328</PartNumber>
        <Description>Body tube, BT-20, 12.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-20 12" Variant PN 30451 used in #1265 Scissor Wing Transport 2005 version -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_12in, 30451</PartNumber>
        <Description>Body tube, BT-20, 12.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-20 12" black used in #2186 Eagle Boosted Glider -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_12in_blk, 30452</PartNumber>
        <Description>Body tube, BT-20, 12.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- PBT-20KB 12.0" punched used in K-57/#1257 Sky Dart, PN unknown -->
      <!-- Brohm fails to give length but it's in the K-57 Sky Dart instructions -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PBT-20KB</PartNumber>
        <Description>Body tube, BT-20, 12.0 in., punched, Sky Dart</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-20 11" white used in #2096 Turbo Copter -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_11in_white</PartNumber>
        <Description>Body tube, BT-20, white, 11.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <!-- BT-20 10.719" used in #2027 Pop Fly -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_10.719in, 31703</PartNumber>
        <Description>Body tube, BT-20, 10.719 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">10.719</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20N, 30336</PartNumber>
        <Description>Body tube, BT-20, 9.75.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">9.75</Length>
      </BodyTube>
      <!-- PN 30325 used in #2039 Space Racer.  PN 30314 is from #2170 Star Dart and
      conflicts with old BT-10H -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_9in, 30325, 30314</PartNumber>
        <Description>Body tube, BT-20, 9.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20B, 30320</PartNumber>
        <Description>Body tube, BT-20, 8.65 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">8.65</Length>
      </BodyTube>
      <!-- PBT-20B, punched for K-48/#1248 Bandit, no PN known -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PBT-20B</PartNumber>
        <Description>Body tube, BT-20, 8.65 in., punched, Bandit</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">8.65</Length>
      </BodyTube>
      <!-- BT-20XW 8.0" used in #1298 X-Wing Fighter -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20XW, 30335</PartNumber>
        <Description>Body tube, BT-20, 8.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20E, 30323</PartNumber>
        <Description>Body tube, BT-20, 7.75 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">7.75</Length>
      </BodyTube>
      <!-- BT-20 7.5" black used in #0803 Bandito -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_7.5in_black, 30333</PartNumber>
        <Description>Body tube, BT-20, black, 7.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">7.75</Length>
      </BodyTube>
      <!-- BT-20 7" silver used in #0804 Firehawk -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_7in_silver, 31763</PartNumber>
        <Description>Body tube, BT-20, silver, 7.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>
      <!-- BT-20 6.906" PN unknown used in #2071 CATO -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_6.906in</PartNumber>
        <Description>Body tube, BT-20, 6.906 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">6.906</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20D, 30322</PartNumber>
        <Description>Body tube, BT-20, 6.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">6.5</Length>
      </BodyTube>
      <!-- Alt PN 30330 is from #2136 Gemini DC, conflicts with older BT-20L - 12" -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_6in, 30327, 30330</PartNumber>
        <Description>Body tube, BT-20, 6.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </BodyTube>
      <!-- BT-20 5.69" PN 30318 used in #0896 Mini Patriot, #2125 Sidewinder.  PN
           conflicts with earlier BT-20AE -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_5.69in, 30318</PartNumber>
        <Description>Body tube, BT-20, 5.69 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">5.69</Length>
      </BodyTube>
      <!-- PN 30329 conflicts with SBT-705 from #2048 Saturn IB.  Alt PN 30310 is from
           #2110 Outlander and conflicts with BT-5CJ -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_5in, 30329, 30310</PartNumber>
        <Description>Body tube, BT-20, 5.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      <!-- alternate PN 30325 is from #2185 Screamin' Mini -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20DJ, 30332, 30325</PartNumber>
        <Description>Body tube, BT-20, 4.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20G, 30324</PartNumber>
        <Description>Body tube, BT-20, 3.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">3.5</Length>
      </BodyTube>
      <!-- BT-20 2.813" PN 30322 used in #2125 AIM-9 Sidewinder, PN conflicts with older BT-20D -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_2.813in, 30322</PartNumber>
        <Description>Body tube, BT-20, 2.813 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.813</Length>
      </BodyTube>
      <!-- alt PN 30408 from various kits conflicts with BT-60FG -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20J, 30326, 30332, 30408</PartNumber>
        <Description>Body tube, BT-20, 2.75 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>
      <!-- BT-20 2.5" PN 30335 used in #2169 Dragonite.  Conflicts with older BT-20XW -->
      <!-- PN 30331 used in #2060 Bandit and others -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_2.5in, 30335, 30331</PartNumber>
        <Description>Body tube, BT-20, 2.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.50</Length>
      </BodyTube>
      <!-- BT-20 2.5" punched, PN 85870 used in #2105 Hijax -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_2.5in_punched, 85870</PartNumber>
        <Description>Body tube, BT-20, 2.5 in., punched</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.50</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20M, 30334</PartNumber>
        <Description>Body tube, BT-20, 2.25 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20AE, 30318</PartNumber>
        <Description>Body tube, BT-20, 1.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </BodyTube>
      <!-- BT-20 1.5" variant PN 30319 used in #0895 Solar Warrior, #2004 Tornado and others -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_1.5in, 30319</PartNumber>
        <Description>Body tube, BT-20, 1.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </BodyTube>
      <!-- BT-20 1.25" PN unknown used in #0896 Mini Patriot -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20_1.25in</PartNumber>
        <Description>Body tube, BT-20, 1.25 in., Mini Patriot</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">1.25</Length>
      </BodyTube>

      <!-- BTH-20 / HBT-20.  Assuming .021 wall with standard BT-20 ID -->
      <!-- PN 31217 used in #2077 Sky Winder -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-20H, HBT-20, 31217</PartNumber>
        <Description>Body tube, BTH-20/HBT-20, heavy wall, 9.25 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.752</OutsideDiameter>
        <Length Unit="in">9.25</Length>
      </BodyTube>

      <!-- PST-20 clear payload tube and Phantom body -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-20, 30602</PartNumber>
        <Description>Body tube, clear, PST-20, 8.0 in.</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-20J</PartNumber>
        <Description>Body tube, clear, PST-20J, 2.75 in.</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.710</InsideDiameter>
        <OutsideDiameter Unit="in">0.736</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>
      
      <!-- BT-30 -->
      <!-- BT-30 parallel material type and spiral/parallel variants not covered -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 9 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30F</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 7 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30B</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 6.125 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">6.125</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30C</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 5.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30A</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 3.5 in., Scout perforated</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">3.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-30J</PartNumber>
        <Description>Body tube, BT-30, parallel-wound, 2.75 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.725</InsideDiameter>
        <OutsideDiameter Unit="in">0.765</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>

      <!-- BT-50 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50, 30352</PartNumber>
        <Description>Body tube, BT-50, 18 in., PN 30352</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50 (yellow), 30355</PartNumber>
        <Description>Body tube, BT-50, yellow, 18 in., PN 30355</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50WH, 31177</PartNumber>
        <Description>Body tube, BT-50, white, 18 in., PN 31177</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50V, 30370</PartNumber>
        <Description>Body tube, BT-50, 16.5 in., PN 30370</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">16.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50SV, 30370</PartNumber>
        <Description>Body tube, BT-50, 16.25 in., PN 30370</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">16.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50TF, 30369</PartNumber>
        <Description>Body tube, BT-50, 16.0 in., PN 30369</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">16.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50XW, 30371</PartNumber>
        <Description>Body tube, BT-50, 15.5 in., PN 30371</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">15.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50KE, 30364</PartNumber>
        <Description>Body tube, BT-50, 15.0 in., PN 30364</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">15.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50N, 30367</PartNumber>
        <Description>Body tube, BT-50, 14.0 in., PN 30367</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">14.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50L, 30366</PartNumber>
        <Description>Body tube, BT-50, 12.7 in., PN 30366</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">12.7</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_11.25in, 30381</PartNumber>
        <Description>Body tube, BT-50, 11.25 in., PN 30381</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">11.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50P, 30365</PartNumber>
        <Description>Body tube, BT-50, 11 in., PN 30365</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50WH, 31180</PartNumber>
        <Description>Body tube, BT-50, white, 11 in., PN 31180</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_10.75in, 31682</PartNumber>
        <Description>Body tube, BT-50, 10.75 in., PN 31682</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">10.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50B, 30379</PartNumber>
        <Description>Body tube, BT-50, 10.25 in., PN 30379</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">10.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50A, 30456</PartNumber>
        <Description>Body tube, BT-50, 10 in., PN 30456</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50W, 30372</PartNumber>
        <Description>Body tube, BT-50, 9.5 in., PN 30372</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_9.5in, 31168</PartNumber>
        <Description>Body tube, BT-50, 9.5 in., PN 31168</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>WBT-50W, 30373</PartNumber>
        <Description>Body tube, BT-50, white, 9.5 in., PN 30373</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_9.5in (yellow), 30349</PartNumber>
        <Description>Body tube, BT-50, yellow, 9.5 in., PN 30349</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_9.0in (gray), 30372</PartNumber>
        <Description>Body tube, BT-50, gray, 9 in., PN 30372</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_9.0in (black), 30351</PartNumber>
        <Description>Body tube, BT-50, black, 9 in., PN 30351</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_8.25in, 30353</PartNumber>
        <Description>Body tube, BT-50, 8.25 in., PN 30353</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">8.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50H, 30360</PartNumber>
        <Description>Body tube, BT-50, 7.75 in., PN 30360</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">7.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PBT-50H</PartNumber>
        <Description>Body tube, BT-50, punched, 7.75 in., no PN</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">7.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50FE, 30359</PartNumber>
        <Description>Body tube, BT-50, 6.5 in., PN 30359</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">6.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>WBT-50EE, 30357</PartNumber>
        <Description>Body tube, BT-50, white, 5.5 in., PN 30357</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <!-- Beige 5.5 BT-50 has same PN as white WBT-50EE -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_5.5in (beige), 30357</PartNumber>
        <Description>Body tube, BT-50, beige, 5.5 in., PN 30357</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50EE, 30382</PartNumber>
        <Description>Body tube, BT-50, 5.5 in., PN 30382</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50EE (red), 30380</PartNumber>
        <Description>Body tube, BT-50, red, 5.5 in., PN 30357</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50F, 30377</PartNumber>
        <Description>Body tube, BT-50, 5 in., PN 30377</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_5.0in, 30415</PartNumber>
        <Description>Body tube, BT-50, 5 in., PN 30415</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50S, 30368</PartNumber>
        <Description>Body tube, BT-50, 4 in., PN 30368</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_3.5in, 30357</PartNumber>
        <Description>Body tube, BT-50, 3.5 in., PN 30357</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">3.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_3.0in, 30412</PartNumber>
        <Description>Body tube, BT-50, 3 in., PN 30412</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">3.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50J, 30362</PartNumber>
        <Description>Body tube, BT-50, 2.75 in., PN 30362</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_2.5in, 30383</PartNumber>
        <Description>Body tube, BT-50, 2.5 in., PN 30383</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">2.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50AH, 30356</PartNumber>
        <Description>Body tube, BT-50, 1.875 in., PN 30356</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">1.875</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50_1.875in (yellow), 30356</PartNumber>
        <Description>Body tube, BT-50, yellow, 1.875 in., PN 30356</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">1.875</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-50AE, 30354</PartNumber>
        <Description>Body tube, BT-50, 1.5 in., PN 30354</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </BodyTube>

      <!-- HBT-50/BTH-50 are heavy-wall with 0.021 wall.  Only one length is known so far -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>HBT-50, BTH-50, 31291</PartNumber>
        <Description>Body tube, HBT-50, 9 in., PN 31291</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.992</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>

      <!-- PST-50 clear payload tube -->
      <!-- *** should material be PETG? *** -->
      <!-- PST_50FJ 6" was only found in the original K-7B Phantom.  The updated #1207
      Phantom used a PST-50S instead -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-50FJ, 30620</PartNumber>
        <Description>Body tube, clear, PST-50, 6 in., PN 30620</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-50S, 30608</PartNumber>
        <Description>Body tube, clear, PST-50, 4 in., PN 30608</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">0.950</InsideDiameter>
        <OutsideDiameter Unit="in">0.976</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>

      <!-- BT-51 -->
      <!-- These parts were created specifically for some scale models.
           ID/OD dimensions from the old Semroc website and Brohm discussion
       -->
      <!-- BT-51N 12.42" used in K29/#1229 Saturn V -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-51N, 30376</PartNumber>
        <Description>Body tube, BT-51, 12.42 in., PN 30376</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.985</InsideDiameter>
        <OutsideDiameter Unit="in">1.011</OutsideDiameter>
        <Length Unit="in">12.42</Length>
      </BodyTube>

      <!-- BT-51CI 4.0" used in K-48/#1248 Bandit -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-51CI, 30374</PartNumber>
        <Description>Body tube, BT-51, 3.875 in., PN 30374</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.985</InsideDiameter>
        <OutsideDiameter Unit="in">1.011</OutsideDiameter>
        <Length Unit="in">3.875</Length>
      </BodyTube>

      <!-- BT-51SE 2.0" used in #1373 Screaming Eagle -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-51SE, 30375</PartNumber>
        <Description>Body tube, BT-51, 2.0 in., PN 30375</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.985</InsideDiameter>
        <OutsideDiameter Unit="in">1.011</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>

      <!-- BT-52 -->
      <!-- This tube size is only slightly different (3 mils larger) than BT-51.  It's
           used in a few scale models and more commonly as a slip-fit engine hook retainer
           tube.  Dimensions from Semroc website and Brohm discussion -->

      <!-- BT-52S 3.938" used in K-59 SPEV and as hook retainer in a few large kits-->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-52S, 30380</PartNumber>
        <Description>Body tube, BT-52, 2.1 in., PN 30378</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.988</InsideDiameter>
        <OutsideDiameter Unit="in">1.014</OutsideDiameter>
        <Length Unit="in">3.938</Length>
      </BodyTube>

      <!-- BT-52AG PN 30378 used in K-29/#1239 Saturn V semi-scale and many kits as hook
           retainer sleeve.  This tube has dimensions listed in Brohm and Semroc.
           1974 parts catalog has wrong dimensions for this.
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-52AG, 30378</PartNumber>
        <Description>Body tube, BT-52, 2.1 in., PN 30378</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.988</InsideDiameter>
        <OutsideDiameter Unit="in">1.014</OutsideDiameter>
        <Length Unit="in">2.1</Length>
      </BodyTube>

      <!-- Later BT-52AG PN 30450 used as sleeve in #1952 Maxi V-2 re-release.  This
           version has different dimensions and much thicker wall than the original
           BT-52AG, which nearly match the 1974 parts catalog dimensions.  This brings up
           the question of whether the PN 30450 tubes were specified to the erroneous 1974
           parts catalog sizes.  See Brohm discussion. -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-52AG, 30450</PartNumber>
        <Description>Body tube, BT-52*, 2.1 in., PN 30450</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.984</InsideDiameter>
        <OutsideDiameter Unit="in">1.118</OutsideDiameter>
        <Length Unit="in">2.1</Length>
      </BodyTube>

      
      <!-- BT-55 -->
      
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55, 30382</PartNumber>
        <Description>Body tube, BT-55, 18 in., PN 30382</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55G, 30386</PartNumber>
        <Description>Body tube, BT-55, 16.75 in., PN 30386</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">16.75</Length>
      </BodyTube>
      <!-- BT-55KG used in K-51/#1251 Sandhawk semi-scale -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55KG, 30388</PartNumber>
        <Description>Body tube, BT-55, 16.69 in., PN 30388</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">16.69</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55V, 30392</PartNumber>
        <Description>Body tube, BT-55, 16.35 in., PN 30392</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">16.35</Length>
      </BodyTube>
      <!-- BT-55 PN 30383 14.5" (#2115 SR-X) conflicts with PN 30383 for BT-55C 14" below -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_14.5in, 30383</PartNumber>
        <Description>Body tube, BT-55, 14.5 in., PN 30383</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">14.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_14.125in, 30470</PartNumber>
        <Description>Body tube, BT-55, 14.125 in., PN 30470</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">14.125</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55C, 30383</PartNumber>
        <Description>Body tube, BT-55, 14 in., PN 30383</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">14.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_13.75in, 31188</PartNumber>
        <Description>Body tube, BT-55, 13.75 in., PN 31188</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">13.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_13.75in, 31188, slotted</PartNumber>
        <Description>Body tube, BT-55, 13.75 in., slotted, SM-3 Seahawk type, PN 31188</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">13.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_12.5in, 30403</PartNumber>
        <Description>Body tube, BT-55, 12.5 in., PN 30403</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">12.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55W, 30391</PartNumber>
        <Description>Body tube, BT-55, 12 in., PN 30391</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">12.00</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_11.25in, 31172</PartNumber>
        <Description>Body tube, BT-55, 11.25 in., slotted, AIM-9 type, PN 31172</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">11.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_11.19in, 30381</PartNumber>
        <Description>Body tube, BT-55, 11.19 in., PN 30381</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">11.19</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_11.0in, 31191</PartNumber>
        <Description>Body tube, BT-55, 11 in., PN 31191</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <!-- BT-55 PN 30374 conflicts with BT-51CI 3.875" long per Brohm -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_10.75in, 30374</PartNumber>
        <Description>Body tube, BT-55, 10.75 in., PN 30374</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">10.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55KA, 30387</PartNumber>
        <Description>Body tube, BT-55, 10.625 in., PN 30387</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">10.625</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_10.625in, 31174</PartNumber>
        <Description>Body tube, BT-55, 10.625 in., slotted, F-22 type, PN 31174</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">10.625</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55IJ, 30384</PartNumber>
        <Description>Body tube, BT-55, 9 in., PN 30384</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <!-- Unclear how 30379 (#2184 Metor Masher) differs from 30384 BT-55IJ -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_9.0in, 30379</PartNumber>
        <Description>Body tube, BT-55, 9 in., PN 30379</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_8.65in, 30375</PartNumber>
        <Description>Body tube, BT-55, 8.65 in., PN 30375</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">8.65</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_7.5in, 31167</PartNumber>
        <Description>Body tube, BT-55, 7.5 in., PN 31167</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">7.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55V, 30454</PartNumber>
        <Description>Body tube, BT-55, 7 in., PN 30454</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_6.875in, 30376</PartNumber>
        <Description>Body tube, BT-55, 6.875 in., green, PN 30376</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">6.875</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_5.625in, 30380</PartNumber>
        <Description>Body tube, BT-55, 5.625 in., PN 30380</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">5.625</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_4.625in, 60174</PartNumber>
        <Description>Body tube, BT-55, 4.625 in., printed, Shuttle Xpress type 2, PN 60174</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">4.625</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_4.313in, 30391</PartNumber>
        <Description>Body tube, BT-55, 4.313 in., PN 30391</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">4.313</Length>
      </BodyTube>
      <!-- #2183 Shuttle Xpress had two different printed BT-55 lengths -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_4.25in, 60368</PartNumber>
        <Description>Body tube, BT-55, 4.25 in., printed, Shuttle Xpress type 1, PN 60368</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">4.625</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55S, 30390</PartNumber>
        <Description>Body tube, BT-55, 4 in., PN 30390</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>
      <!-- BT-55 PN 31166 is slotted for #2120 Venus Probe -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_4.0in, 31166</PartNumber>
        <Description>Body tube, BT-55, 4 in., slotted, Venus Probe type, PN 31166</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>
      <!-- #2125 AIM-9 Sidewinder has 2 different lengths of slotted BT-55 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_3.75in, 31173</PartNumber>
        <Description>Body tube, BT-55, 3.75 in., slotted, AIM-9 type 2nd, PN 31173</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">3.75</Length>
      </BodyTube>
      <!-- Note "BT-55" PN 30387 duplicates PN for BT-55KA above -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_3.5in, 30387</PartNumber>
        <Description>Body tube, BT-55, 3.5 in., PN 30387</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">3.5</Length>
      </BodyTube>
      <!-- BT-55 PN 30392 3.25" (#2054 Beta Launch Vehicle) conflicts with same PN for BT-55V above -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55_3.25in, 30392</PartNumber>
        <Description>Body tube, BT-55, 3.25 in., PN 30392</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">3.25</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55J, 30386</PartNumber>
        <Description>Body tube, BT-55, 2.75 in., PN 30386</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-55E, 30381</PartNumber>
        <Description>Body tube, BT-55, 2.1 in., PN 30381</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">2.1</Length>
      </BodyTube>

      <!-- PST-55 -->
      <!-- PST-55 12.0" used in #2155 Super Nova Payloader -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-55, 30612</PartNumber>
        <Description>Body tube, PST-55, clear, 12 in., PN 30612</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">1.283</InsideDiameter>
        <OutsideDiameter Unit="in">1.325</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>

      <!-- BT-56, ID/OD ref 1995 Estes catalog -->
      <!-- NOTE: BT-56 is (nearly?) identical to old Centuri ST-13; only slightly larger than BT-55
      -->

      <!-- Standard 18" BT-56 is white like Centuri tubes were -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>WBT-56, 30393</PartNumber>
        <Description>Body tube, BT-56, white, 18 in., PN 30393</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>

      <!-- 12.0" purple BT-56 used in #1950 Eliminator -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 31300</PartNumber>
        <Description>Body tube, BT-56, purple, 12.0 in., PN 31300</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">12.00</Length>
      </BodyTube>

      <!-- 12.0" chrome foil BT-56 used in #2168 Metalizer -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 60003</PartNumber>
        <Description>Body tube, BT-56, chrome foil, 12.0 in., PN 60003</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">12.00</Length>
      </BodyTube>

      <!-- 12.0" BT-56 (yellow) PN is unknown -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56_12.0in_yellow</PartNumber>
        <Description>Body tube, BT-56, yellow, 12.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">12.00</Length>
      </BodyTube>

      <!-- 12.0" BT-56 with no color specified in Brohm used in #2072 Scramble
           and later in #2128 Long Shot.  PN conflicts with old BT-30A -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 30340</PartNumber>
        <Description>Body tube, BT-56, 12.0 in., PN 30340</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">12.00</Length>
      </BodyTube>

      <!-- 10.25" yellow BT-56 used in #1262 Cosmic Cobra -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 31660</PartNumber>
        <Description>Body tube, BT-56, yellow, 10.25 in., PN 31660</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">10.25</Length>
      </BodyTube>

      <!-- 8.5" blue BT-56 used in #2180 Chrome Dome Silver -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 60370</PartNumber>
        <Description>Body tube, BT-56, blue, 8.5 in., PN 60370</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">8.5</Length>
      </BodyTube>

      <!-- 8.5" red BT-56 used in #2180 Chrome Dome Gold -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 60371</PartNumber>
        <Description>Body tube, BT-56, red, 8.5 in., PN 60371</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">8.5</Length>
      </BodyTube>

      <!-- 8.25" black BT-56 with pre-installed launch lug used in #2029 Converter -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 31665</PartNumber>
        <Description>Body tube, BT-56, black, 8.25 in., with launch lug, PN 31665</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">8.25</Length>
      </BodyTube>

      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 30346</PartNumber>
        <Description>Body tube, BT-56, white, 8.0 in., PN 30346</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>

      <!-- 7" yellow BT-56 used in #2130 Mach-12 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 30342</PartNumber>
        <Description>Body tube, BT-56, yellow, 7.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>

      <!-- unknown PN for 7" blue BT-56 used in #2078 Omloid -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56_7.0in_blue</PartNumber>
        <Description>Body tube, BT-56, blue, 7.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>

      <!-- 6.0" black BT-56 used in #2029 Converter -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 31710</PartNumber>
        <Description>Body tube, BT-56, black, 6.0 in., PN 31710</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </BodyTube>

      <!--  4" white BT-56 used in #2133 Aero-Sat LSX -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 30347</PartNumber>
        <Description>Body tube, BT-56, white, 4.0 in., PN 30347</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>

      <!--  4" yellow BT-56 used in #2130 Mach-12 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 30343</PartNumber>
        <Description>Body tube, BT-56, yellow, 4.0 in., PN 30343</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>

      <!-- 4" blue BT-56 (unknown PN) used in #2078 Omloid -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56_4.0in_blue</PartNumber>
        <Description>Body tube, BT-56, blue, 4.0 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </BodyTube>

      <!-- 3.75" BT-56 (color not specified) used in #2128 Long Shot -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-56, 31341</PartNumber>
        <Description>Body tube, BT-56, 3.75 in., PN 31341</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.304</InsideDiameter>
        <OutsideDiameter Unit="in">1.346</OutsideDiameter>
        <Length Unit="in">3.75</Length>
      </BodyTube>

      <!-- BT-58 - used only in scale models -->
      <!-- Semroc BT-58 has ID 1.498", OD 1.540" for .042" wall.
           BT-58 was 6.375", used in #2048 Saturn 1B
           BT-58SV was 6.125", used in #2001 Saturn V and later #0809 Gauchito
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-58, 30397</PartNumber>
        <Description>Body tube, BT-58, 6.375 in., PN 30397</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.498</InsideDiameter>
        <OutsideDiameter Unit="in">1.540</OutsideDiameter>
        <Length Unit="in">6.375</Length>
      </BodyTube>

      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-58SV, 30466</PartNumber>
        <Description>Body tube, BT-58, 6.125 in., PN 30466</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.498</InsideDiameter>
        <OutsideDiameter Unit="in">1.540</OutsideDiameter>
        <Length Unit="in">6.125</Length>
      </BodyTube>

      <!-- BT-60 -->

      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60, 30396</PartNumber>
        <Description>Body tube, BT-60, 18 in., PN 30396</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- BT-60WH used in #2127 Sizzler -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60WH, 31175</PartNumber>
        <Description>Body tube, BT-60, white, 18 in., PN 31175</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- BT-60 18" alternate PN used only in #2109 Renegade.  The tube has no special
           features that would justify a separate PN -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60, 30401</PartNumber>
        <Description>Body tube, BT-60, 18 in., Renegade, PN 30401</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- BT-60KF 16.1" used in K-41/#1241 Semi Scale Mercury Redstone -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60KF, 30416</PartNumber>
        <Description>Body tube, BT-60, 16.1 in., PN 30416</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">16.1</Length>
      </BodyTube>
      <!-- BT-60P 16.0" used in #2192 Thunderstar -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60P, 30455</PartNumber>
        <Description>Body tube, BT-60, 16 in., PN 30455</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">16.0</Length>
      </BodyTube>
      <!-- BT-60 15.56" used in #2187 Oracle -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_15.56in, 60865</PartNumber>
        <Description>Body tube, BT-60, 15.56 in., PN 60865</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">15.56</Length>
      </BodyTube>
      <!-- BT-60 14.7" used in #2173 Menace -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_14.7in, 30395</PartNumber>
        <Description>Body tube, BT-60, 14.7 in., PN 30395</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">14.7</Length>
      </BodyTube>
      <!-- BT-60AE 14.25" used in #1340 SCUD-B -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60AE, 30404</PartNumber>
        <Description>Body tube, BT-60, 14.25 in., PN 30404</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">14.25</Length>
      </BodyTube>
      <!-- BT-60AD 14" used in K-52/#1200P Omega -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60AD, 30398</PartNumber>
        <Description>Body tube, BT-60, 14 in., PN 30398</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">14.0</Length>
      </BodyTube>
      <!-- BT-60 13.313" used in #2019 Titan IIIE, no PN known -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_13.313in</PartNumber>
        <Description>Body tube, BT-60, 13.313 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">13.313</Length>
      </BodyTube>
      <!-- BT-60 13.25" used in #2054 Beta Launch Vehicle. PN 30400 conflicts with the
           7.5" BT-60 used in #1261 Baby Bertha.  PN 30408 is used for this length BT-60
           in #2056 Patriot, but conflicts with BT-60FG 6.7" -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_13.25in, 30400, 30408</PartNumber>
        <Description>Body tube, BT-60, 13.25 in., PN 30400/30408</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">13.313</Length>
      </BodyTube>
      <!-- BT-60KC 12.875" used in #1287 LTV Scout -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60KC, 30415</PartNumber>
        <Description>Body tube, BT-60, 12.875 in., PN 30415</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.875</Length>
      </BodyTube>
      <!-- BT-60DS 12.5" used in #1345 Dragon Ship 7. -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60DS, 30407</PartNumber>
        <Description>Body tube, BT-60, 12.5 in., PN 30407</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.5</Length>
      </BodyTube>
      <!--  BT-60 12.5" (nominally same as BT-60DS)  appears without PN in #2086 Tomcat
           but with punched holes/slots, so it is not the same part  -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.5in_tomcat_punched</PartNumber>
        <Description>Body tube, BT-60, tomcat punched, 12.5 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.5</Length>
      </BodyTube>
      
      <!-- BT-60 12.0" used in #1301 Storm Caster and #2156 Prower.  PN 30397 conflicts with
           BT-58 used in #2048 Saturn 1B.  Brohm says this BT-60 is white -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.0in, 30397</PartNumber>
        <Description>Body tube, BT-60, white, 12 in., PN 30397</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-60 12.0" with 60000 series PN used in #2187 Oracle.  May be white. -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.0in, 60974</PartNumber>
        <Description>Body tube, BT-60, 12 in., PN 60974</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-60 12.0" blue used in #2155 Super Nova Payloader.  PN 30398 conflicts with BT-60AD -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.0in_blue, 30398</PartNumber>
        <Description>Body tube, BT-60, blue, 12 in., PN 30398</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-60 12.0" slotted used in #2156 Prowler -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.0in_slotted_prowler, 31189</PartNumber>
        <Description>Body tube, BT-60, slotted, 12 in., Prowler, PN 31189</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-60 12.0" blue prismatic used in #1300 Blue Ninja and .  PN 30406 conflicts with
           parts catalog entry for BT-60D 11.0" --> 
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_12.0in_blue_prism, 30406</PartNumber>
        <Description>Body tube, BT-60, blue prismatic, 12 in., PN 30406</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- BT-60D 11" appears in the 1974 parts catalog -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60D, 30406</PartNumber>
        <Description>Body tube, BT-60, 11 in., PN 30406</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <!-- BT-60 10.375" used in #2019 Titan IIIE, no PN known -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_10.375in</PartNumber>
        <Description>Body tube, BT-60, 10.375 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">10.375</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60AJ, 30402</PartNumber>
        <Description>Body tube, BT-60, 10 in., PN 30402</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </BodyTube>
      <!-- BT-60 10.0" orange used in #2071 CATO, no PN known -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_10.0in_orange</PartNumber>
        <Description>Body tube, BT-60, orange, 10 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </BodyTube>
      <!-- BT-60HE 8.5" used in #0651/KC-2 Der Red Max -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60HE, 30410</PartNumber>
        <Description>Body tube, BT-60, 8.5 in., PN 30410</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">8.5</Length>
      </BodyTube>
      <!-- BT-60 8.0" used in #2119 36 D Squared -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_8.0in, 31681</PartNumber>
        <Description>Body tube, BT-60, 8 in., PN 31681</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- BT-60 8.0" blue prismatic used in #1300 Blue Ninja. PN 30407 conflicts with
           older BT-60DS 12.5" -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_8.0in_blue, 30407</PartNumber>
        <Description>Body tube, BT-60, blue prismatic, 8 in., PN 30407</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- BT-60 7.5" black used in #2121 Liquidator -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_7.5in_black, 31713</PartNumber>
        <Description>Body tube, BT-60, black, 7.5 in., PN 31713</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">7.5</Length>
      </BodyTube>
      
      <!-- In #1261 Baby Bertha and #2196 Space Ship One, a 7.5" BT-60 is given PN 30400,
           which conflicts with the 13.25" BT-60 used in the #2054 Beta Launch Vehicle -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_7.5in, 30400</PartNumber>
        <Description>Body tube, BT-60, 7.5 in., PN 30400</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">7.5</Length>
      </BodyTube>
      
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60K, 30414</PartNumber>
        <Description>Body tube, BT-60, 7 in., PN 30414</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">7.0</Length>
      </BodyTube>
      <!-- BT-60FG 6.7" used in K-43 Mars Lander and K-59 SPEV and #1387 Maxi Streak -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60FG, 30408</PartNumber>
        <Description>Body tube, BT-60, 6.7 in., PN 30408</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">6.7</Length>
      </BodyTube>
      <!-- BT-60 6.656" used in re-release of #2056 Patriot -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_6.656in, 30414</PartNumber>
        <Description>Body tube, BT-60, 6.656 in., PN 30414</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">6.656</Length>
      </BodyTube>
      <!-- BT-60 5.688" used in #2110 Outlander.  PN conflicts with BT-60AJ -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_5.688in, 30402</PartNumber>
        <Description>Body tube, BT-60, 5.688 in., PN 30402</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">5.688</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60R, 30418</PartNumber>
        <Description>Body tube, BT-60, 5 in., PN 30418</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      <!-- BT-60V 4.313" used in #2193 Vanguard Eagle -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60V, 30453</PartNumber>
        <Description>Body tube, BT-60, 4.313 in., PN 30453</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">4.313</Length>
      </BodyTube>
      <!-- BT-60 3.25" yellow used in #2056 Patriot and its re-release (under different
      PNs).  PN 30409 from the original Patriot conflicts with 0.5" BT-5 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_3.25in_yellow, 30409, 30426</PartNumber>
        <Description>Body tube, BT-60, 3.25 in., PN 30409/30426</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">3.25</Length>
      </BodyTube>
      <!-- BT-60 3.25" used in #2109 Renegade PN 30404, which conflicts with BT-60AE PN -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_3.25in, 30404</PartNumber>
        <Description>Body tube, BT-60, 3.25 in., PN 30404</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">3.25</Length>
      </BodyTube>
      <!-- BT-60XW 3.0" used in #1302 Maxi X-Wing -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60XW, 30419</PartNumber>
        <Description>Body tube, BT-60, 3 in., PN 30419</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">3.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60J, 30412</PartNumber>
        <Description>Body tube, BT-60, 2.75 in., PN 30412</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">2.75</Length>
      </BodyTube>
      <!-- BT-60 2.0" used in #2037 National Aero Space Plane and #2110 Outlander-->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_2.0in, 30411</PartNumber>
        <Description>Body tube, BT-60, 2.0 in., PN 30411</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>
      <!-- BT-60 1.56" used in #0881 Mini Mars Lander, no PN known -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60_1.56in</PartNumber>
        <Description>Body tube, BT-60, 1.56 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">1.56</Length>
      </BodyTube>
      <!-- BT-60C 1.0" used in #1383 Hyperion -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-60C, 30405</PartNumber>
        <Description>Body tube, BT-60, 1.0 in., PN 30405</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      
      <!-- PST-60 clear payload tube -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-60R, 30614</PartNumber>
        <Description>Body tube, clear, PST-60R, 5 in., PN 30614</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">1.595</InsideDiameter>
        <OutsideDiameter Unit="in">1.637</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>

      <!-- PST-65 clear payload tube 
           SOURCE ERROR: There are conflicts on PST-65 / BNC-65 dimensions.
           The 1974 CPC gives grossly incorrect OD 1.641  ID 1.595 (nearly the same as BT-60)
           The 1974 Estes print catalog gives   OD 1.796  ID 1.750
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>PST-65R, 30616</PartNumber>
        <Description>Body tube, clear, PST-65R, 5 in., PN 30616</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="in">1.750</InsideDiameter>
        <OutsideDiameter Unit="in">1.796</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </BodyTube>
      

      <!-- BT-70 -->

      <!-- BT-70 17.5in used in K-21 Gemini-Titan -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70, 30424</PartNumber>
        <Description>Body Tube, BT-70, 17.5 in., PN 30424</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">17.5</Length>
      </BodyTube>

      <!-- BT-70V 10.6in used in K-29/#1229 Saturn 1B -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70V, 30430</PartNumber>
        <Description>Body Tube, BT-70, 10.6 in., PN 30430</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">10.6</Length>
      </BodyTube>

      <!-- BT-70 10.0in used in #2055 BLU-97B -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70_10in, 30413</PartNumber>
        <Description>Body Tube, BT-70, 10.0 in., PN 30413</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </BodyTube>

      <!-- BT-70H 7.15in used in K-30 Little Joe II and K-59 SPEV -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70H, 30428</PartNumber>
        <Description>Body Tube, BT-70, 7.15 in., PN 30428</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">7.15</Length>
      </BodyTube>

      <!-- BT-70 6.25in used in #2019 Titan IIIE, no numeric PN in Brohm -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70_6.25in</PartNumber>
        <Description>Body Tube, BT-70, 6.25 in.</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">6.25</Length>
      </BodyTube>

      <!-- BT-70 4.5in used in #2119 36 D Squared -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-70_4.5in, 31680</PartNumber>
        <Description>Body Tube, BT-70, 4.5 in., PN 31680</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.217</OutsideDiameter>
        <Length Unit="in">4.5</Length>
      </BodyTube>

      <!-- BT-80 -->
      <!-- BT-80 (no suffix) is an oddball as it is not the longest tube in the series,
           and has been used for multiple lengths, differentiated only by the new PN.  The
           WBT-80x are among the first white tubes.   -->
      <!-- BT-80KD is from #1269 Maxi Honest John, #2018 Super Big Bertha, etc. -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80KD, 30433</PartNumber>
        <Description>Body tube, BT-80, 14.2 in., PN 30433</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">14.2</Length>
      </BodyTube>
      <!-- BT-80 14.2" PN 31180 is slotted, used in #1951 Executioner.  PN conflicts with
           older BT-50WH -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80, 31180</PartNumber>
        <Description>Body tube, BT-80, 14.2 in., slotted, Executioner type, PN 31180</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">14.2</Length>
      </BodyTube>
      <!-- BT-80 12" PN 30458 is from #2188 Canadian Arrow -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80, 30458</PartNumber>
        <Description>Body tube, BT-80, 12.0 in., PN 30458</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">7.6</Length>
      </BodyTube>
      <!-- Per Brohm, PN of BT-80T conflicts with older BT-100D -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80T, 30435</PartNumber>
        <Description>Body tube, BT-80, 11.0 in., PN 30427</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">11.0</Length>
      </BodyTube>
      <!-- BT-80 11" PN 30437 is white, used in #1380 Phoenix re-release, I give it "TW" suffix -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80TW, 30437</PartNumber>
        <Description>Body tube, BT-80, 11.0 in., white, PN 30437</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">7.6</Length>
      </BodyTube>
      <!-- BT-80SV is from the #2001 Saturn V per Brohm -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80SV, 30434</PartNumber>
        <Description>Body tube, BT-80, 8.81 in., PN 30434</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">8.81</Length>
      </BodyTube>
      <!-- BT-80WH from #2139 Fat Boy, ref instructions -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80WH, 31179</PartNumber>
        <Description>Body tube, BT-80, 8.0 in., slotted, Fat Boy type, PN 31179</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <!-- BT-80 7.6" was used in the K-36/#1236 Saturn V and #1926 V-2 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80_7.6in, 30432</PartNumber>
        <Description>Body tube, BT-80, 7.6 in., PN 30432</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">7.6</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-80S, 30427</PartNumber>
        <Description>Body tube, BT-80, 4.5 in., PN 30427</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">4.5</Length>
      </BodyTube>
      
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>WBT-80A, 30429</PartNumber>
        <Description>Body tube, BT-80, 9.0 in., white, PN 30429</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>WBT-80MA, 30431</PartNumber>
        <Description>Body tube, BT-80, 3.22 in., white, PN 30431</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.600</OutsideDiameter>
        <Length Unit="in">3.22</Length>
      </BodyTube>

      <!-- BT-101 and SBT-394AJ -->

      <!-- BT-101SV used in #2001 Saturn V -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101SV, 30449</PartNumber>
        <Description>Body tube, BT-101, 24.625 in., PN 30449</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">24.625</Length>
      </BodyTube>

      <!-- BT-101LA used in #1268 Pershing 1-A -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101LA, 30445</PartNumber>
        <Description>Body tube, BT-101, 21.4 in., PN 30445</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">21.4</Length>
      </BodyTube>

      <!-- BT-101 16.5" used in K-36/#1236 Saturn V -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101, 30438</PartNumber>
        <Description>Body tube, BT-101, 16.5 in., PN 30438</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">16.5</Length>
      </BodyTube>

      <!-- BT-101KJ used in #1267 Maxi-V2 -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101KJ, 30441</PartNumber>
        <Description>Body tube, BT-101, 10.5 in., PN 30441</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">10.5</Length>
      </BodyTube>

      <!-- BT-101K used in K-36/#1236 Saturn V -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101K, 30440</PartNumber>
        <Description>Body tube, BT-101, 7.59 in., PN 30440</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">7.59</Length>
      </BodyTube>

      <!-- BT-101T used in K-29/#1229 Saturn 1B -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>BT-101T, 30442</PartNumber>
        <Description>Body tube, BT-101, 2.78 in., PN 30442</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.896</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">2.78</Length>
      </BodyTube>

      <!-- SBT-394AJ used in #1288 Starlab etc. BT-101 OD with 0.042 wall -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>SBT-394AJ, 30449</PartNumber>
        <Description>Body tube, BT-101, 2.78 in., PN 30442</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.854</InsideDiameter>
        <OutsideDiameter Unit="in">3.938</OutsideDiameter>
        <Length Unit="in">2.78</Length>
      </BodyTube>


      <!-- Couplers (tube) All validated except JT-80C -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-5C, 30252</PartNumber>
        <Description>Tube coupler, paper, JT-5C, PN 30252</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.455</InsideDiameter>
        <OutsideDiameter Unit="in">0.513</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-20C, 30254</PartNumber>
        <Description>Tube coupler, paper, JT-20C, PN 30254</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.650</InsideDiameter>
        <OutsideDiameter Unit="in">0.708</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-30C, 30256</PartNumber>
        <Description>Tube coupler, paper, JT-30C, PN 30256</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.650</InsideDiameter>
        <OutsideDiameter Unit="in">0.724</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-50C, 30260</PartNumber>
        <Description>Tube coupler, paper, JT-50C, PN 30260</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">0.920</InsideDiameter>
        <OutsideDiameter Unit="in">0.949</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-55C, 30262</PartNumber>
        <Description>Tube coupler, paper, JT-55C, PN 30262</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.25</InsideDiameter>
        <OutsideDiameter Unit="in">1.28</OutsideDiameter>
        <Length Unit="in">1.25</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-60C, 30266</PartNumber>
        <Description>Tube coupler, paper, JT-60C, PN 30266</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.55</InsideDiameter>
        <OutsideDiameter Unit="in">1.59</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-70A, 30270</PartNumber>
        <Description>Tube coupler, paper, JT-70A, PN 30270</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.115</InsideDiameter>
        <OutsideDiameter Unit="in">2.175</OutsideDiameter>
        <Length Unit="in">1.25</Length>
      </TubeCoupler>
      <!-- JT-80C complications!  See http://www.rocketryforum.com/archive/index.php/t-128230.html
           There are two different versions:

           "Old" JT-80C was a .021" wall glassine finish tube, not fish paper.
           Thus it has OD 2.554, ID 2.512

           "New" JT-80 is fish paper and has a .040 wall, and thus OD 2.554, ID 2.474  (per BMS specs)
      -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-80C legacy</PartNumber>
        <Description>Tube coupler, paper, BT-80, glassine, 1.0"</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.512</InsideDiameter>
        <OutsideDiameter Unit="in">2.554</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>JT-80C new</PartNumber>
        <Description>Tube coupler, paper, BT-80, 3.0"</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.474</InsideDiameter>
        <OutsideDiameter Unit="in">2.554</OutsideDiameter>
        <Length Unit="in">3.0</Length>
      </TubeCoupler>

      <!-- Couplers (balsa) all validated -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>NB-20, 70152</PartNumber>
        <Description>Tube coupler, balsa, NB-20, PN 70152</Description>
        <Material Type="BULK">Balsa, bulk, Estes typical</Material>
        <InsideDiameter Unit="in">0.0</InsideDiameter>
        <OutsideDiameter Unit="in">0.710</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>NB-30, 70156</PartNumber>
        <Description>Tube coupler, balsa, NB-30, PN 70156</Description>
        <Material Type="BULK">Balsa, bulk, Estes typical</Material>
        <InsideDiameter Unit="in">0.0</InsideDiameter>
        <OutsideDiameter Unit="in">0.725</OutsideDiameter>
        <Length Unit="in">0.75</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>NB-50, 70158</PartNumber>
        <Description>Tube coupler, balsa, NB-50, PN 70158</Description>
        <Material Type="BULK">Balsa, bulk, Estes typical</Material>
        <InsideDiameter Unit="in">0.0</InsideDiameter>
        <OutsideDiameter Unit="in">0.950</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>NB-55, 70162</PartNumber>
        <Description>Tube coupler, balsa, NB-55, PN 70162</Description>
        <Material Type="BULK">Balsa, bulk, Estes typical</Material>
        <InsideDiameter Unit="in">0.0</InsideDiameter>
        <OutsideDiameter Unit="in">1.283</OutsideDiameter>
        <Length Unit="in">1.25</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>NB-60, 70164</PartNumber>
        <Description>Tube coupler, balsa, NB-60, PN 70164</Description>
        <Material Type="BULK">Balsa, bulk, Estes typical</Material>
        <InsideDiameter Unit="in">0.0</InsideDiameter>
        <OutsideDiameter Unit="in">1.595</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </TubeCoupler>
      
      <!-- Transitions (balsa) -->
      <!-- Two versions are given for each part -->
      <!-- Normal version has fore diameter smaller than aft diameter -->
      <!-- [R] means a reducer, fore diameter is larger than aft diameter -->

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-520, 70002</PartNumber>
            <Description>Transition, balsa, TA-520, increasing, PN 70002</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.541</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">0.75</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-520 [R], 70002</PartNumber>
            <Description>Transition, balsa, TA-520, reducing, PN 70002</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.541</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">0.75</Length>
        </Transition>
        
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-550, 70004</PartNumber>
            <Description>Transition, balsa, TA-550, increasing, PN 70004</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.541</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.515</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-550 [R], 70004</PartNumber>
            <Description>Transition, balsa, TA-550, reducing, PN 70004</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.541</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.515</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2050A, 70008</PartNumber>
            <Description>Transition, balsa, TA-2050A, increasing, PN 70008</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2050A [R], 70008</PartNumber>
            <Description>Transition, balsa, TA-2050A, reducing, PN 70008</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2055, 70010</PartNumber>
            <Description>Transition, balsa, TA-2055, increasing, PN 70010</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2055 [R], 70010</PartNumber>
            <Description>Transition, balsa, TA-2055, reducing, PN 70010</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2060, 70012</PartNumber>
            <Description>Transition, balsa, TA-2060, increasing, PN 70012</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.736</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.710</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2060 [R], 70012</PartNumber>
            <Description>Transition, balsa, TA-2060, reducing, PN 70012</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.736</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.710</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5055, 70014</PartNumber>
            <Description>Transition, balsa, TA-5055, increasing, PN 70014</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5055 [R], 70014</PartNumber>
            <Description>Transition, balsa, TA-5055, reducing, PN 70014</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5050, 70016</PartNumber>
            <Description>Transition, balsa, TA-5060, increasing, PN 70016</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5060 [R], 70016</PartNumber>
            <Description>Transition, balsa, TA-5060, reducing, PN 70016</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5065, 70020</PartNumber>
            <Description>Transition, balsa, TA-5065, increasing, PN 70020</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.796</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5065 [R], 70020</PartNumber>
            <Description>Transition, balsa, TA-5065, reducing, PN 70020</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.796</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.5</AftShoulderLength>
            <Length Unit="in">2.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5560, 70028</PartNumber>
            <Description>Transition, balsa, TA-5560, increasing, PN 70028</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5560 [R], 70028</PartNumber>
            <Description>Transition, balsa, TA-5560, reducing, PN 70028</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.0</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5565, 70030</PartNumber>
            <Description>Transition, balsa, TA-5565, increasing, PN 70030</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.325</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.283</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.796</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-5565 [R], 70030</PartNumber>
            <Description>Transition, balsa, TA-5565, reducing, PN 70030</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.796</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.325</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.283</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-6065, 70032</PartNumber>
            <Description>Transition, balsa, TA-6065, increasing, PN 70032</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.796</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.750</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.75</AftShoulderLength>
            <Length Unit="in">0.5</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-6065 [R], 70032</PartNumber>
            <Description>Transition, balsa, TA-6065, reducing, PN 70032</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.796</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.750</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.75</AftShoulderLength>
            <Length Unit="in">0.5</Length>
        </Transition>

        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-6070, 70034</PartNumber>
            <Description>Transition, balsa, TA-6070, increasing, PN 70034</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">1.637</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">1.595</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.217</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.175</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-6070 [R], 70034</PartNumber>
            <Description>Transition, balsa, TA-6070, reducing, PN 70034</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <ForeOutsideDiameter Unit="in">2.217</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.175</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.6</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.637</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.595</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.6</AftShoulderLength>
            <Length Unit="in">1.5</Length>
        </Transition>

      <!-- Transitions (plastic) -->


        <!-- Nose cones (balsa)
             This list is perhaps still not complete, but much better than the stock OpenRocket data file.
        -->

        <!-- BNC-3 Nose Cones -->
        
        <!-- BNC-3A ref 1974 custom parts catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-3A, 70204</PartNumber>
            <Description>Nose cone, balsa, BNC-3A, 0.75", conical, PN 70204</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.375</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.347</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.75</Length>
        </NoseCone>
        <!-- BTC-3 ref 1974 custom parts catalog
             This is the escape tower nozzle from the NCK-29 Apollo Capsule
             Included as a NoseCone in hopes that OpenRocket will someday be able to handle rear facing
             nose cones.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BTC-3, 70530</PartNumber>
            <Description>Tail cone nozzle, balsa, BTC-3, 0.75", escape tower nozzle, PN 70530</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.024</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.375</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.347</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.75</Length>
        </NoseCone>

        <!-- BWP-EJ (a BT-5 nose cone) called "Wing Pod Balsa" in 1974 parts catalog.  This was the long, rounded-tip
             ogive cone in the K-50 Interceptor BT-5 wing tip pods. Unclear why it didn't get a BNC-5xx designation.
             Shoulder length is quoted as 0.372" which is odd, would have expected 3/8" as all other nose cones in the 1974
             parts catalog are listed that way.  Weight given as 0.035 oz.
             Another strange fact is that Semroc never made a repro of this part despite the ongoing popularity of the
             Interceptor in all versions.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BWP-EJ, 70500</PartNumber>
            <Description>Nose cone, balsa, BWP-EJ, 2.75", Interceptor wing pod, PN 70500</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.372</ShoulderLength>
            <Length Unit="in">2.75</Length>
        </NoseCone>
        <!-- BNC-5AW (Star Dart) ref 1974 custom parts catalog.  Semroc weight 0.02 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5AW, 70206</PartNumber>
            <Description>Nose cone, balsa, BNC-5AW, 2.25", elliptical, PN 70206</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">2.25</Length>
        </NoseCone>
        <!-- BNC-5AX, PN 070208 (Screamer, Javelin) ref 1974 custom parts catalog. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5AX, 70208</PartNumber>
            <Description>Nose cone, balsa, BNC-5AX, 2.25", ogive, PN 70208</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.25</Length>
        </NoseCone>
        <!-- BNC-5BA (Mini-BOMARC and #1220 Mars Snooper) ref 1974 custom parts catalog.
             Semroc weight 0.01 oz, Estes .013 oz
             Shape is shown as a "ram jet" style nacelle.  Approximated as an ogive. -->
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5BA, 70210</PartNumber>
            <Description>Nose cone, balsa, BNC-5BA, 0.625", ramjet nacelle, PN 70210</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.18</ShoulderLength>
            <Length Unit="in">0.625</Length>
        </NoseCone>
        <!-- BNC-5E, ref 1974 parts catalog, 0.020 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5E, 70212</PartNumber>
            <Description>Nose cone, balsa, BNC-5E, 1.375", fat ogive, PN 70212</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">1.375</Length>
        </NoseCone>
        <!-- BNC-5RA*, from #0893 Red Alert (1991-1992).

             * "BNC-5RA" is unsubstantiated except through Semroc's kit parts listing at
             http://www.semroc.com/store/scripts/ClassicParts.asp?ID=379.  Numeric PN 070217 is given on the Estes
             instructions.  Semroc gives length 2.0", max diam 0.810, and weight 0.1 oz.  It's a flared ogive and will
             not be well modeled by OpenRocket.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5RA*, 70217</PartNumber>
            <Description>Nose cone, balsa, BNC-5RA, 2.0", flared ogive, Red Alert, PN 70217</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.10</Mass>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">2.0</Length>
        </NoseCone>
        <!-- BNC-5S, ref 1974 custom parts catalog, 0.016 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5S, 70214</PartNumber>
            <Description>Nose cone, balsa, BNC-5S, 1.5", conical, PN 70214</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">1.5</Length>
        </NoseCone>
        <!-- BNC-5V ref 1974 parts catalog,  0.013 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5V, 70216</PartNumber>
            <Description>Nose cone, balsa, BNC-5V, 0.75", elliptical, PN 70216</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.75</Length>
        </NoseCone>
        <!-- BNC-5W ref 1974 parts catalog, 0.039 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-5W, 70218</PartNumber>
            <Description>Nose cone, balsa, BNC-5W, 2.875", ogive, PN 70218</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">2.875</Length>
        </NoseCone>

        <!-- BNC-10xx -->
        
        <!-- The stated OD of the BNC-10xx (see 1975 cat) of .728 is different than the
             given OD of 0.720 for the BT-10 mylar tubes.  This is real; the nose cones were
             slightly oversized for the tubes -->

        <!-- BNC-10A, 0.03 oz.  Used in K-4/#1204 Streak -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-10A, 70220</PartNumber>
            <Description>Nose cone, balsa, BNC-10A, 0.8", elliptical, PN 70220</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.728</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.812</Length>
        </NoseCone>
        <!-- BNC-10B not known to have ever been used in any Estes kit, 0.05 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-10B, 70222</PartNumber>
            <Description>Nose cone, balsa, BNC-10B, 1.69", elliptical, PN 70222</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.728</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.312</ShoulderLength>
            <Length Unit="in">1.687</Length>
        </NoseCone>

        <!-- BNC-20xx -->

        <!-- BNC-20A (K-7 Phantom, K-13 Falcon) ref 1975 catalog, 0.03 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20A, 70224</PartNumber>
            <Description>Nose cone, balsa, BNC-20A, 0.8", rounded ogive, PN 70224</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.812</Length>
        </NoseCone>
        <!-- BNC-20AM (K-53 Stinger etc) ref 1988 catalog, 0.06 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20AM, 70226</PartNumber>
            <Description>Nose cone, balsa, BNC-20AM, 2.0", rounded cone, PN 70226</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.00</Length>
        </NoseCone>
        <!-- BNC-20AZ (#2033 Trident II etc.), ref 1974 parts catalog.
             BMS drawing shows slightly off 2.4 in length but gives length really incorrectly in a note as 1.25 -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20AZ, 70288</PartNumber>
            <Description>Nose cone, balsa, BNC-20AZ, 2.5", ogive, PN 70288</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.5</Length>
        </NoseCone>
        <!-- BNC-20B (K-5 Apogee II etc.) ref 1988 catalog, 0.05 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20B, 70230</PartNumber>
            <Description>Nose cone, balsa, BNC-20B, 1.7", elliptical, PN 70230</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.312</ShoulderLength>
            <Length Unit="in">1.687</Length>
        </NoseCone>
        <!-- BNC-20CB 70231 (#1279 Nike-Ajax), dimensions from Semroc legacy site, weight 0.04 oz
             Not in the 1974 custom parts catalog.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20CB, 70231</PartNumber>
            <Description>Nose cone, balsa, BNC-20CB, 1.75", ogive, PN 70231</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">1.75</Length>
        </NoseCone>
        <!-- BNC-20L 70234 (Mini-Bertha #0803 only) ref 1974 parts catalog
             SOURCE ERROR: Semroc legacy gives incorrect length 2.0", weight .04 oz
                           Estes official length is 1 3/8" but:
                           Semroc new gives length 1.4"
                           balsamachining.com also gives length 1.4"
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20L, 70234</PartNumber>
            <Description>Nose cone, balsa, BNC-20L, 1.375", parabolic, PN 70234</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">1.375</Length>
        </NoseCone>
        <!-- BNC-20N 70236 ref 1975 catalog, 0.08 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20N, 70236</PartNumber>
            <Description>Nose cone, balsa, BNC-20N, 2.75", ogive, PN 70236</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.75</Length>
        </NoseCone>
        <!-- BNC-20P 70238 (Spaceman only), shape is approximation, 0.07 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20P, 70238</PartNumber>
            <Description>Nose cone, balsa, BNC-20P, 1.3", spaceman, PN 70238</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.07</Mass>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.90</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.437</ShoulderLength>
            <Length Unit="in">1.312</Length>
        </NoseCone>
        <!-- BNC-20R 70240 ref 1975 catalog, 0.07 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20R, 70240</PartNumber>
            <Description>Nose cone, balsa, BNC-20R, 2.75", conical, PN 70240</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.711</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">2.75</Length>
        </NoseCone>
        <!-- BNC-20Y 70241 (Yankee #1381 up to 1994 only) ref 1988 catalog, 0.02 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-20Y, 70241</PartNumber>
            <Description>Nose cone, balsa, BNC-20Y, 1.0", conical, PN 70241</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">1.0</Length>
        </NoseCone>

        <!-- BNC-30XX -->
        <!-- Some of the BNC-30xx were never used in kits according to the Brohm index -->
        
        <!-- BNC-30C 70242 ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-30C, 70242</PartNumber>
            <Description>Nose cone, balsa, BNC-30C, 0.75", spherical, PN 70242</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.767</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.725</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">0.75</Length>
        </NoseCone>
        <!-- BNC-30D 70244 ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-30D, 70244</PartNumber>
            <Description>Nose cone, balsa, BNC-30D, 1.5", fat ogive, PN 70244</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.767</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.725</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">1.5</Length>
        </NoseCone>
        <!-- BNC-30E 70246 ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-30E, 70246</PartNumber>
            <Description>Nose cone, balsa, BNC-30E, 2.2", ogive, PN 70246</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.767</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.725</ShoulderDiameter>
            <ShoulderLength Unit="in">0.437</ShoulderLength>
            <Length Unit="in">2.187</Length>
        </NoseCone>
        <!-- BNC-30M 70248 ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-30M, 70248</PartNumber>
            <Description>Nose cone, balsa, BNC-30M, 1.5", ogive, PN 70248</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.767</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.725</ShoulderDiameter>
            <ShoulderLength Unit="in">0.500</ShoulderLength>
            <Length Unit="in">1.5</Length>
        </NoseCone>
        <!-- BNC-30N 70250 ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-30N, 70250</PartNumber>
            <Description>Nose cone, balsa, BNC-30N, 2.75", ogive, PN 70250</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.767</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.725</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.75</Length>
        </NoseCone>


        <!-- BNC-50xx BT-50 nose cones -->

        <!-- BNC-2 was the Apollo capsule nose cone for the NCK-29 Apollo Capsule
             Bizarrely, BNC-2 was a fit for a BT-50 tube and should have been called BNC-50xxx.
             Dimensions and PN from 1974 Custom Parts Catalog
             Mass override used because it is significantly flared, OD = 1.360
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-2, 70200</PartNumber>
            <Description>Nose cone, balsa, BNC-2, 1.0", Apollo capsule, PN 70200</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.19</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">1.0</Length>
        </NoseCone>

        <!-- BNC-50AD 70252 Honest John ref 1975 catalog, shape PARABOLIC is approximation -->
        <!-- mass override needed since it's significantly heavier than the shape implies -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50AD, 70252</PartNumber>
            <Description>Nose cone, balsa, BNC-50AD, 4.1", Honest John, PN 70252</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.25</Mass>
            <Filled>true</Filled>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">1.30</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">4.062</Length>
        </NoseCone>
        <!--
             BNC-50AR (old Starship Vega #0653 only) ref 1974 custom parts catalog, weight .375 oz
             Semroc legacy site has shape/length, weight 0.15 oz
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50AR, 70254</PartNumber>
            <Description>Nose cone, balsa, BNC-50AR, 5.5", rounded cone, PN 70254</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.30</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">5.5</Length>
        </NoseCone>
        
        <!-- BNC-50BA (old BOMARC #0657) ref Semroc legacy website.
             conical shape looks like best match.  Semroc weight given as 0.05 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50BA, 70257</PartNumber>
            <Description>Nose cone, balsa, BNC-50BA, 1.3", ramjet nacelle, PN 70257</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">1.3</Length>
        </NoseCone>
        <!-- BNC-50BC (Wolverine #0816 etc.), ref 1974 parts catalog. Ramjet style cone a la BOMARC pods.
             Estes gives len 2 3/4" and weight .156 oz but Semroc gives length 2.6" and weight 0.11 oz.
             Scaling from scans on spacemodeling.com, length of the ramjet cone is 0.5".
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50BC, 70258</PartNumber>
            <Description>Nose cone, balsa, BNC-50BC, 2.6", ramjet nacelle, PN 70258</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">2.75</Length>
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
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50BD, 70260</PartNumber>
            <Description>Nose cone, balsa, BNC-50BD, 4.5", flared ogive, PN 70260</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.523</Mass>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.30</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">5.0</Length>
        </NoseCone>
        <!-- BNC-50J ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50J, 70256</PartNumber>
            <Description>Nose cone, balsa, BNC-50J, 1.375", elliptical, PN 70256</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">1.375</Length>
        </NoseCone>
        <!-- BNC-50K ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50K, 70262</PartNumber>
            <Description>Nose cone, balsa, BNC-50K, 2.75", ogive, PN 70262</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.75</Length>
        </NoseCone>
        <!-- BNC-50X ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50X, 70264</PartNumber>
            <Description>Nose cone, balsa, BNC-50X, 3.25", PN 70264</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">3.25</Length>
        </NoseCone>
        <!-- BNC-50Y ref 1975 catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-50Y, 70266</PartNumber>
            <Description>Nose cone, balsa, BNC-50Y, 4.375", ogive, PN 70266</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.16</Mass>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">4.375</Length>
        </NoseCone>

        <!-- ***Investigate PN 70401 "Sergeant (7047)" and 70402 "Sandpiper (7048)" BT-50 size NCs from 1974 parts catalog.
             They are unique in lacking BNC series names, and have a specified 1.000" OD instead of BT-50 0.976"
             These do not appear in the Brohm NC list, where the #1389 Sandpiper has a PNC-50S, and there is no
             listing for a Sergeant.  Possibly cross-listed from Centuri series 10 size?
        -->

        <!-- BNC-52xx -->

        <!-- BNC-52G PN 70270 (K-28 Thor Agena B capsule, later in K-59 SPEV) is found in the Brohm nose cone reference.
             PN is from the 1974 custom parts catalog.  Neither the the K-28 nor K-59 instructions have numeric PNs.
             Dimensions and mass override from 1974 custom parts catalog.
             Conical is just as wrong as any other choice for an OpenRocket shape.
             
             SOURCE ERROR:  eRockets/Semroc 2017 site erroneously notates a BNC-50G long ogive as "Thor Agena nose cone"
             but it is the wrong shape, and the K-28 Thor Agena used BT-52, not BT-50.
             -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-52G, 70270</PartNumber>
            <Description>Nose cone, balsa, BNC-52G, 1.25", capsule, PN 70270</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.056</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.014</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.986</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">1.25</Length>
        </NoseCone>
        <!-- BNC-52AG PN 70268 (K-39/#1239 Semi Scale Saturn V) is a 1/242 scale Apollo capsule.  PN and dimensions
             and unrealistically precise weight are from 1974 custom parts catalog.
             The CONICAL shape is of course a complete lie but will give a closer drag coefficient.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-52AG, 70268</PartNumber>
            <Description>Nose cone, balsa, BNC-52AG, 3.25", capsule, PN 70268</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.106</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.014</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.986</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">3.25</Length>
        </NoseCone>

        <!-- BNC-55xx  -->

        
        <!-- BTC-55Z tailcone for V-2.  OR cannot model drilled transition mass/inertia correctly.
             Data from 1974 custom parts catalog:  Length 3", shoulder 0.5", weight 0.25 oz, drilled for BT-20.
             Model as reducing ogive transition.
        -->
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BTC-55Z [R]</PartNumber>
            <Description>Transition, balsa, BTC-55Z, reducing</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
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
        
        <!-- BNC-55AA ref 1975 catalog.  Brohm index shows no uses in kits -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55AA, 70272</PartNumber>
            <Description>Nose cone, balsa, BNC-55AA, 3.125", ogive, PN 70272</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">3.125</Length>
        </NoseCone>
        <!-- BNC-55AC PN 70274 (K-26/#1226 ARCAS, K-47 Cherokee-D).  Replaced with PNC-55AC by 1975. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55AC, 70274</PartNumber>
            <Description>Nose cone, balsa, BNC-55AC, 5.375", ogive, PN 70274</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">5.375</Length>
        </NoseCone>
        <!-- BNC-55AM PN 70280 (#1258 Demon) from Brohm list and Semroc legacy site.  Semroc also carries an upscale as
             BC-27589.
             The actual shape is a rounded-tip cone with a ~0.5" cylindrical section at the aft end.  It's far enough
             away from any OpenRocket shape that we use a mass override.  Shoulder len estimated from Semroc drawing.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55AM, 70280</PartNumber>
            <Description>Nose cone, balsa, BNC-55AM, 4.2", rounded cone, PN 70280</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.27</Mass>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.2</Length>
        </NoseCone>
        <!-- BNC-55AO (K-48 Bandit etc.).  OR built in file has typo as "BNC-55AD" -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55AO, 70276</PartNumber>
            <Description>Nose cone, balsa, BNC-55AO, 5.0", elliptical, PN 70276</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">5.0</Length>
        </NoseCone>
        <!-- BNC-55AZ ref 1974 custom parts catalog.  Missing from Semroc legacy nose cone list.
             Estes weight 0.134 oz. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55AZ, 70282</PartNumber>
            <Description>Nose cone, balsa, BNC-55AZ, 4.75", ogive, PN 70282</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.75</Length>
        </NoseCone>
        <!-- BNC-55BE (#1272 Vostok) 1.75" 2-section cone, no Estes weight, Semroc weight 0.14 oz,
             no data for shoulder length -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55BE, 70277</PartNumber>
            <Description>Nose cone, balsa, BNC-55BE, 1.75", dual conic (Vostok), PN 70277</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">1.75</Length>
        </NoseCone>
        <!-- BNC-55F (K-22/1222 V-2 only), ref 1975 catalog, Brohm fails to list the PN, Estes weight 0.19 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-55F, 70278</PartNumber>
            <Description>Nose cone, balsa, BNC-55F, 3.875", ogive, PN 70278</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">3.875</Length>
        </NoseCone>

        <!-- BNC-60xx (see 1971 catalog) -->

        <!-- BNC-60AB PN 70284 ref 1971 catalog is a Gemini capsule, conical shape is approximate -->
        <!-- SOURCE ERROR: Brohm says BNC-60AB goes with K-21 Gemini Titan, but that was a BT-70 model! -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60AB, 70284</PartNumber>
            <Description>Nose cone, balsa, BNC-60AB, 2.625", Gemini Capsule, PN 70284</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.23</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">2.625</Length>
        </NoseCone>
        <!-- BNC-60AH, ref 1971 catalog, weight 1.0 oz.  PN from 1974 custom parts catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60AH, 70286</PartNumber>
            <Description>Nose cone, balsa, BNC-60AH, 7.25", elliptical, PN 70286</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.875</ShoulderLength>
            <Length Unit="in">7.25</Length>
        </NoseCone>
        <!-- BNC-60AK PN 70290 (K-41/#1241 Mercury Redstone) is Mercury capsule, conical shape is approximate
             Ref 1974 custom parts catalog and K-41 instructions found on JimZ site
             BNC-60AK also appears in Brohm and on the Semroc parts list page for K-41/#1241.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60AK, 70290</PartNumber>
            <Description>Nose cone, balsa, BNC-60AK, 3.0", Mercury Capsule, PN 70290</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.308</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">3.0</Length>
        </NoseCone>
        <!-- BNC-60AL PN 70288 (K-43/#1243 Mars Lander).  Dimensions, PN and weight from 1974 parts catalog -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60AL, 70288</PartNumber>
            <Description>Nose cone, balsa, BNC-60AL, 1.25", Mars Lander, PN 70288</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.268</Mass>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">1.25</Length>
        </NoseCone>
        <!-- BNC-60L PN 70292 (K-6 Ranger, K-23 Big Bertha). Ref 1971 catalog and 1974 custom parts catalog. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60L, 70292</PartNumber>
            <Description>Nose cone, balsa, BNC-60L, 3.125", rounded ogive, PN 70292</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.625</ShoulderLength>
            <Length Unit="in">3.125</Length>
        </NoseCone>
        <!-- BNC-60T ref 1974 custom parts catalog is a Mercury capsule, conical shape is approximate, 0.17 oz; not in Brohm
             though Brohm does list a PNC-60T blow molded cone for #1918 Titan II (which would be a Gemini).
             Not clear how this relates to BNC-60AK which is also a BT-60 Mercury capsule of almost identical size.
             Freedom 7 vs Liberty Bell 7?  BNC-60AK has more detail.  Both are shown in the 1974 parts catalog.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-60T, 70294</PartNumber>
            <Description>Nose cone, balsa, BNC-60T, 2.875", Mercury capsule, PN 70294</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Mass Unit="oz">0.17</Mass>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">2.875</Length>
        </NoseCone>

        <!-- BNC-65xx (see 1971, 1975 catalogs and 1974 custom parts catalog) -->

        <!-- BNC-65AF ref 1974 parts catalog, elliptical, 0.50 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-65AF, 70296</PartNumber>
            <Description>Nose cone, balsa, BNC-65AF, 4.0", elliptical, PN 70296</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.796</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.750</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.0</Length>
        </NoseCone>
        <!-- BNC-65L, rounded tip ogive, 0.41 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-65L, 70298</PartNumber>
            <Description>Nose cone, balsa, BNC-65L, 3.25", rounded tip ogive, PN 70298</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.796</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.750</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">3.25</Length>
        </NoseCone>

        <!-- BNC-70xx (see 1971 catalog) -->

        <!--
            BNC-70AJ, ref 1977 catalog and 1974 custom parts catalog, rounded tip ogive, 0.85 oz
            PN 70300 in 1974 custom parts catalog, then was PN 8019 in catalogs thru 1984, changed
            back to PN 70300 in 1985 catalog.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNC-70AJ, 8019/70300</PartNumber>
            <Description>Nose cone, balsa, BNC-70AJ, PN 70300, old PN 8019</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.217</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
            <ShoulderLength Unit="in">1.0</ShoulderLength>
            <Length Unit="in">4.25</Length>
        </NoseCone>

        <!-- Estes special balsa parts resembling nose cones
             These 3 items formed part of the Mercury Redstone Capsule and are listed in the 1974 Custom Parts Catalog.
             None of them had shoulders, but these can be used as nose cones if you add a shoulder after you drop them
             into your design.
        -->
        <!-- BNP-41 is the main body of the Mercury Redstone escape tower motor.  It's roughly cylindrical with some
             ridges turned into the part. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>BNP-41, 70540</PartNumber>
            <Description>Nose plug, balsa, BNP-41, PN 70540, MR escape tower motor</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.406</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.400</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">1.280</Length>
        </NoseCone>
        <!-- PN 71030 PSM-1 is the nose of the Mercury capsule that contained the parachutes.  This part actually resembles
             a nose cone, though it was shoulderless. -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PSM-1, 71030</PartNumber>
            <Description>Nose cone, balsa, PSM-1, PN 71030, MR capsule nose</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.787</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.780</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">1.228</Length>
        </NoseCone>
        <!-- PN 71032 (also referred to as PSM-1) is the aft heat shield end of the Mercury capsule.  To actually use
             this as a tail cone, you'd need to create a transition definition for it.  -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PSM-1, 71032</PartNumber>
            <Description>Nose cone, balsa, PSM-1, PN 71032, MR capsule base</Description>
            <Material Type="BULK">Balsa, bulk, Estes typical</Material>
            <Filled>true</Filled>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.820</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.800</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">0.712</Length>
        </NoseCone>

        <!--
          ========================================================================================
          Nose cones (plastic).  Getting good PNs, dimensions and masses for Estes plastic nose cones is very difficult;
          for most modern plastic nose cones it's a research project requiring an actual example.
          
          In 1975 there were only a couple of plastic nose cones in the catalog.  By 1995 Estes no longer used balsa
          nose cones at all and no longer sold discrete nose cones; nearly everything changed to variety packs.  Newer plastic
          nose cones also do not have traditional designators with suffix like "PNC-20Y".  In some cases they don't even have
          publicly known numeric PNs (when the NC is only sold in assortments and the relevant kit instructions do not list
          PNs). If you have a defective NC in your kit, Estes sends you a whole new kit because they don't even stock
          nose cones in the USA on a per-type basis.

          Very recently (post Brohm) Estes has again been using balsa nose cones in certain kits. To
          date I have no information about any of these.
          
          Brohm's encyclopedic nose cone reference list has numerous errors in the plastic nose cone listings, which I'm
          documenting as they are discovered.

          The Semroc classic cross-reference page contains clues to various Estes PNCs, most including a PNC-xx
          designator as well as numeric PN, about which little else is known.
          
          To handle cases where there is no traditional suffix designator, I am generating obviously synthetic part IDs of the form
              PNC-50_kitname    where 'kitname' is the name of a representative kit using the nose cone,
          and only listing numeric PNs where they can be determined.  This will allow nose cones to be found in the alphabetic
          listings in OpenRocket dialogs by tube series.
          ========================================================================================
        -->

        <!--
            PNC-5A ref Brohm v10.1, PN 072600, #0802 Quark etc., OpenRocket file mass 2.835 gm
            Uses base plate insert PN 072601 (PN found from other kit instructions using PNC-5 series cones)
            PN 72600 confirmed in Quark instructions (but not PNC-5A), base insert PN 072601 not shown here
            #0886 Gnome and #0870 Pulsar instructions have no PNs but show the glue-in base insert cap
            Source of OpenRocket mass value is unknown.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-5A, 072600</PartNumber>
            <Description>Nose cone, plastic, BT-5, 2.125", parabolic, PNC-5A, PN 072600</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.125</Length>
            <Thickness Unit="in">0.088</Thickness>  <!-- to make mass = 2.8 gm -->
        </NoseCone>

        <!--
            PNC-5V ref Brohm v10.1, PN 070305, injection molded, #0801 Mosquito
            Uses base plate insert PN 72601
            It's a plastic version of legacy balsa BNC-5V, so using those dimensions.
            *** actual mass unknown, assuming 1 gm ***
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-5V, 070305</PartNumber>
            <Description>Nose cone, plastic, BT-5, 0.75", ellipsoid, injection molded, PNC-5V, PN 070305</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">0.75</Length>
            <Thickness Unit="in">0.068</Thickness>  <!-- to make mass = 1 gm -->
        </NoseCone>

        <!--
            PNC-5_swift  PN 072609 ref #0810 Swift instructions on Estes instructions archive page
            This is an ogive about 2 calibers (1.0") long (ref drawing in instructions)
            Shorter than old BNC-5E (1.375")
            *** actual mass and length unknown, assuming 1.25 gm and 1.0 in ***
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-5_swift, 072609</PartNumber>
            <Description>Nose cone, plastic, BT-5, 1.0", ogive, injection molded, PNC-5_swift, PN 072609</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.541</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.25</ShoulderLength>
            <Length Unit="in">1.0</Length>
            <Thickness Unit="in">0.078</Thickness>  <!-- to make mass = 1.25 gm -->
        </NoseCone>
        
        <!--
            Brohm gives a PNC-20 color series as
               PN 072606 injection molded for #1259 Orbital Transport glider only (no color specified, white?)
                         "PNC-20" usage confirmed in #1259 instructions.
               PN 072701 injection molded for #0804 Firehawk only (red plastic)
                         Firehawk instructions only give 072701 PN
                         Firehawk uses nose cone base insert PN 072603 as well, color unknown
               PN 072702 injection molded for #0803 Bandito only (green plastic)
                         PN confirmed by Bandito instructions.  Also uses PN 072603 insert, color unknown
               The length given here from built-in OR file seems too long given the OT instructions drawings
               but looks more plausible in the Firehawk and Bandito drawings.
               I'm not totally sure that the OT glider nose is the same size as the other two.
               *** TODO *** add red/green ones after we get the dimensions for sure
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-20, 072606</PartNumber>
            <Description>Nose cone, plastic, BT-20, 2.65", ogive, PNC-20, white, PN 072606</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.12</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="m">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="m">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.5</ShoulderLength>
            <Length Unit="in">2.65</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- PN 071095 from #1999 Corsair, #2039 Space Racer, #1941 Fox Fire, blow molded per Brohm -->

        <!--
        -->

        <!-- PNC-20A from #1292 Wizard, #1917 Zinger, etc. 2-piece injection molded per Brohm -->

        <!-- PNC-20ED from #1254 SAROS, #1344 NOMAD -->

        <!-- PNC-20N from #0846 Eclipse, #0868 Big Yank, #1390 Aero-Fin -->

        <!-- PNC-20Y, mass 0.1 oz.  Not mentioned as such in Brohm, nor in Semroc cross-ref. 
             There is discussion on YORF http://www.oldrocketforum.com/archive/index.php/t-6197.html
             that was likely the 1994 plastic successor to the BNC-20Y used in the Estes #1381 Yankee
             Per Brohm there was a PN 070323 from #1381 Yankee only, injection molded.
             Yankee instructions confirm PN and use of a PN 072603 base insert.
              -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-20Y, PN 070323</PartNumber>
            <Description>Nose cone, plastic, BT-20, 1.0", conical, PNC-20Y, 2-pc inj molded, PN 070323</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.10</Mass>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.736</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.710</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">1.0</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- *** PNC-50xx known from Brohm but missing dimensions because they never appeared in a catalog ***
             Most have no direct balsa equivalent.

               PNC-50V PN 71025 / PNC-50BB PN 71027 from #1296 Satellite Interceptor etc.  Brohm notes that PNC-50V and PNC-50BB nose
               cones are identical, but the PNC-50BB mold also includes a tail cone.
               Semroc offers a BNC-50V with length 5.75" and unspecified shoulder length.  The Semroc drawing shows the
               shape to be a hybrid cone/cylinder where the cone is about 4.75" and the cylinder about 1".
               Comparing to PNC-55AO and PNC-55AC (similar length) we can estimate the mass at around 0.35 oz.

               PNC-50BB tailcone section (#1296 Satellite Interceptor)  ***get dimensions from built model***

               PNC-50F PN 071030 (#1371 Starship Nova)

               PN 72023 blow molded (#1942 SR-71)

               PNC-50A PN 072044 (#1929 Stealth)

               PNC-50U PN 027046 (#1915 Harpoon) derived from Centuri PNC-102 from #KB-4 SkyLab

               PNC-50CA PN 071005 (#1281 Alien Invader, #1383 Hyperion)

               PN 071005 (#2010 Star Rider, #2175 Nemesis) - NOT the same as PNC-50CA; the PN got re-used

               PN 033229 (#1337 NASA Space Shuttle Orbiter) injection molded

               PNC-50M1 PN 071004 (#KC-1 Quasar) crhome plated

               PNC-50SV PN 071002 (#1310 Colonial Viper)

               PNC-50(?) PN 071000 (#1374 Orion, #1928 Manta Bomber)

               PNC-50E PN 061299 (#2117 Screaming Eagle) Brohm says 4 7/8" long with canopy

        -->


        <!--
            PRP-1H PN 032487/032492 (#1328 Kadet, #1416 Challenger I).  Brohm says 4 1/4" long and very similar to
            PNC-50Y. The alternate PN 032492 is used by #1375 SAM-4.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PRP-1H, 032487, 032492</PartNumber>
            <Description>Nose cone, plastic, PRP-1H, PN 32487, PN 32492</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.228</Mass>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">0.974</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.625</Length>
            <Thickness Unit="in">4.25</Thickness>
        </NoseCone>

        <!-- PNC-50K ref 1974 Custom Parts catalog.  Red, slightly shorter than PNC-50KA
             Clear version is CPNC-50K 45015, chrome is PNC-M1 71004.  They have slightly different masses listed.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50K, 71008</PartNumber>
            <Description>Nose cone, plastic, red, PNC-50K, PN 71008</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.228</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.974</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.625</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>cPNC-50K, 45015</PartNumber>
            <Description>Nose cone, plastic, clear, CPNC-50K, PN 45015, Phantom</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.228</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.974</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.625</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>
        
        <!-- PNC-50KA ref 1988 catalog, mass 0.13 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50KA</PartNumber>
            <Description>Nose cone, plastic, PNC-50KA</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.13</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">2.75</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- PNC-50SP ref 1993 catalog, mass 7.1 g, irregular shape -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50SP, 71001</PartNumber>
            <Description>Nose cone, plastic, PNC-50SP, Argosy type, PN 71001</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.500</ShoulderLength>
            <Length Unit="in">4.720</Length>
            <Thickness Unit="in">0.0457</Thickness>
        </NoseCone>
        
        <!-- PNC-50X PN 071010 (#1282 Photon Disruptor), dimensions taken from Semroc BNC-50XP -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50X, 71010</PartNumber>
            <Description>Nose cone, plastic, PNC-50X, PN 71010</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.228</Mass>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">0.974</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.50</ShoulderLength>
            <Length Unit="in">3.25</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- PNC-50Y ref 1988 catalog, mass 0.16 oz = 4.5 g -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50Y, 71009</PartNumber>
            <Description>Nose cone, plastic, PNC-50Y, PN 71009</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.16</Mass>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">4.375</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- PNC-50V (#1296 Satellite Interceptor) - dimensions recovered
             from an actual sample from my own (built) vintage Satellite Interceptor.
             Brohm notes that PNC-50V and PNC-50BB nose cones are identical, but the PNC-50BB mold also includes a tail cone.
             From my sample:  Mass is 0.305 oz.
             The shape is a hybrid cone/cylinder where the cone is 4.75" and the cylinder section is 1.0".
             Shoulder:  cylinder part is 0.5" long.  There follows a reverse cone 0.375" long, with a molded-in
             eyelet and a .2375" hole.  Base diam of the reverse cone is 0.320".  For interference purposes
             the shoulder length is 0.875".
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50V, PNC-50BB, PN 71027</PartNumber>
            <Description>Nose cone, plastic, PNC-50V/PNC-50BB, PN 71027</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.305</Mass>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">0.976</OutsideDiameter>
            <ShoulderDiameter Unit="in">0.950</ShoulderDiameter>
            <ShoulderLength Unit="in">0.875</ShoulderLength>
            <Length Unit="in">5.75</Length>
            <Thickness Unit="in">0.062</Thickness>
        </NoseCone>

        <!-- PNC-50BB tail cone section (#1296 Satellite Interceptor etc).  This is molded in one unit with the PNC-50BB
             and has no separate PN.  I have an actual example in a built model; the plastic is white.
             Overall exposed length: 0.375".  There is a cylindrical flange that seats against the tube with OD 0.975"
             and length 0.67" (1/16").  After the flange the shape is a straight diverging cone, whose diameter
             at the junction with the flange is 0.850", and 0.976" at the rear.  The inside diameter of the cone at
             the rear is about 0.922".  Inside throat diameter of the cone is 0.756".  I measured the thickness of
             the rim at 0.046" but there is a bit of a ring/flashing right at the rim so the actual wall thickness
             is probably a bit less.

             Finally there is a square recess for the engine hook that extends the length
             of the cone part and is 0.172" wide externally, and about 0.1265" internally, an exact match for the
             width of my engine hook.  The height of this recess tunnel exactly matches the OD of the flange.

             Shoulder length of the tailcone is not determined since it's glued into the model, but I'd estimate
             it at 0.25".  Likewise I cannot measure the mass but would estimate it around 1/8 of the mass of the
             nose cone part, or about 0.04 oz.

             There is no reasonable way to visually model this in OpenRocket with a single component.  You will need to
             use a bulkhead for the flange, with a phantom tube used to mount an increasing conical hollow transition
             (zero length shoulders) to represent the cone.

             To provide a mass-realistic dummy part, I here give a cylindrical hollow transitiion with no rear shoulder,
             and a mass override so CG and inertia calculations will be realistic.
          -->
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-50BB-tailcone</PartNumber>
            <Description>Tailcone, plastic, white, PNC-50BB-tailcone, PN 71027</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>CONICAL</Shape>
            <Mass Unit="oz">0.04</Mass>
            <ForeOutsideDiameter Unit="in">0.976</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">0.950</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.25</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">0.976</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">0.950</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">0.375</Length>
            <Thickness Unit="in">0.046</Thickness>
        </Transition>

        <!-- *** Estes PNC-55xxx that appear in Brohm but for which we have no good data.  Some of these do not
            have any known PN.  I have actual examples of a few of these in unopened kits.

            PNC-55EK PN ???? (K-51 Sandhawk, #1251 Sandhawk).  ***See Semroc BNC-55SH for possible equivalent.***

            PNC-55HJ PN 72058 (#1919 Honest John).  Dimensions of balsa version are on Semroc website.
            We could have a good go at this using shoulder dimensions of PNC-55EX, but at this point
            I have no good idea of the mass.

            PNC-55EJ PN ???? (K-50 Interceptor, #1250 Interceptor)

            PN ???? (#2003 SDI Satellite, #1977 GeoSat LV)

            PN ???? (#2016 Explorer Aquarius)

            PNC-55CB PN 71036 (#1289 Odyssey)

            PNC-55IR (#1973 Interceptor II).  Also prepainted version PN 72055 used in #2183 Shuttle Xpress.

            PNC-55B PN 72056 (#1367 Vindicator, #1910 USS Pleiaades) - blow molded version of PNC-55EJ

            PN 72683 (#2125 AIM-9 Sidewinder) - no PNC-55xxx designation

            PN 72701 (#2013 Recruiter, #2054 Beta Launch Vehicle) - no PNC-55xxx description

            PNC-1287 PN 71067 (#1287 LTV Scout) plastic payload fairing nose+transition parts set.  I have a
            built model.  This would need to be broken into 3 components (NC and two transitions) plus an SBT-139BJ tube.

        -->


        <!--
            NC-55 with molded-in canopy for #1903 Xarconian Cruiser and #2000 Voyager II
            Brohm v10.1 gives "PNC-55" PN 071037 for these kits; but they are too new to have a
            genuine unique "PNC-55" designation.
            HOWEVER, Estes Xarconian Cruiser current instructions on Estes site give PN 072689 so I'm using that.
            Voyager II instructions clearly show the same cone though no PN given.
            This is a recent blow molded cone, apparently white, with a large bulbous canopy
            Dimensions in original OR file don't make sense, length 8.375 is ridiculous
            There is a scan on JimZ plans site of cone + centering rings that allows approx scaling of dimensions
            Len ~= 3.0 x diameter = 4.0 in, shoulder len 0.75 not counting trailing cone
            ***need to confirm mass*** OpenRocket built-in file mass was 0.875 oz which is reasonable.
            The base shape is an ogive with a canopy bulge added.
         -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55_Xarconian, 072689</PartNumber>
            <Description>Nose cone, plastic, Xarconian type, PN 072689</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.0</Length>
            <Thickness Unit="in">0.154</Thickness> <!-- to give mass 0.875 oz = 25.4 gm -->
        </NoseCone>

        <!-- PNC-55AC ref 1990 Estes Catalog, mass 0.32 oz = 9.1 g -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55AC, 71070</PartNumber>
            <Description>Nose cone, plastic, PNC-55AC, PN 71070</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.375</ShoulderLength>
            <Length Unit="in">5.375</Length>
            <Thickness Unit="in">0.0367</Thickness>
        </NoseCone>
        
        <!--
            PNC-55AO ref 1982 Estes Catalog, #1335 Blue Bird Zero and others, quoted mass 0.43 oz = 12.2 gm
            However I have an actual PNC-55AO from a 1980s Blue Bird Zero that only weighs 9.0 gm.
            PNC-55AD PN 71076 as used in #2155 Super Nova Payloader is identical.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55AO, 71075, PNC-55AD, 71076</PartNumber>
            <Description>Nose cone, plastic, PNC-55AO, PN 71075</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">5.0</Length>
            <Thickness Unit="in">0.045</Thickness>
        </NoseCone>
        
        <!--
            PNC-55BB for #1958 Black Brant II, blow molded, PN 071044 per Brohm
            OR original file gave mass 14.17 gm
            Functional shoulder len from gridded photo on BRSrocketry site, add ~0.5" for aft cone taper
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55BB, 071044</PartNumber>
            <Description>Nose cone, plastic, Black Brant II type, PNC-55BB, PN 071044</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">1.0</ShoulderLength>
            <Length Unit="in">6.5</Length>
            <Thickness Unit="in">0.0675</Thickness>
        </NoseCone>
        
        <!--
             *** PNC-55CB PN 71036 per Brohm #1289 Odyssey, #1363 Rigel 3 ***
             This is the infamous Odyssey nose cone with lots of surface detail
        -->

        <!-- PNC-55D (Sea Dart nacelle) ref 1990 Estes Catalog, mass 0.36 oz = 10.2 g
             Performance will be worse than the given ogive shape.
             You could make a better model with a short conical cone and a couple of
             transitions but the aerodynamic fidelity wouln't be much better.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55D, 71038, Sea Dart</PartNumber>
            <Description>Nose cone, plastic, PNC-55D, Sea Dart, PN 71038</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">0.750</ShoulderLength>
            <Length Unit="in">3.750</Length>
            <Thickness Unit="in">0.0385</Thickness>
        </NoseCone>

        <!-- PNC-55EX (#1925 Exocet, #1935 Neptune, #1955 Ranger).  Never appeared in a catalog,
             but I have a Ranger, and Semroc cloned it in balsa as well.  Blow molded in white plastic.
             Measured:  mass 0.400 oz (painted), shoulder len 1.0 functional, 1.375 total, len 3.375.
             I removed .02 oz to correct for paint and tuned the thickness to give mass of 0.38 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-55EX, PN 71031</PartNumber>
            <Description>Nose cone, plastic, PNC-55EX, PN 71031</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.325</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.283</ShoulderDiameter>
            <ShoulderLength Unit="in">1.375</ShoulderLength>
            <Length Unit="in">3.375</Length>
            <Thickness Unit="in">0.071</Thickness>
        </NoseCone>

        <!--
            PNC-56 ref John Brohm Estes Nose Cone Reference List v10.1
            Brohm calls this "PNC-56A" but I cannot find any upstream source for the "A" sufffix.
            The only known usage of "PNC-56" is in the instructions for the #2029 Converter,
            where it's cross-identified as a PN 72013.

            Numeric PNs for "PNC-56" are as follows: (Note that Brohm's list from ca. 2007 is not correct or up to date in all respects)
               072015   : molded in black plastic, #1262 Cosmic Cobra, #2077 Sky Winder, #2130 Mach-12, #2128 Long Shot.
                          Has separate heli blade attachment structure, molded in white or black
                          Ref: Cosmic Cobra instructions, which (incorrectly) make it look like the NC has an
                          integrated heli blade hub:
                          http://www.estesrockets.com/media/instructions/001262_COSMIC_COBRA.pdf
                          Ref: This Cosmic Cobra build page with photo showing the black NC and separate white heli hub:
                          http://www.seed-solutions.com/gregordy/Rockets/Cobra.htm
                          Ref: TRF thread where user 'snuggles' confirms that the Long Shot had a black nose cone and fin can:
                          http://www.rocketryforum.com/archive/index.php/t-62733.html                          
               072013   : molded in yellow plastic, #1950 Eliminator, #2029 Converter, #1330 Challenger II, #1995 Helio Copter
                          Ref: Eliminator instructions
                          http://www.estesrockets.com/media/instructions/001950_ELIMINATOR.pdf
                          Ref: Converter instructions, where it's cross-listed as "PNC-56"
                          https://www.estesrockets.com/media/instructions/002029_CONVERTER.pdf
                          Ref: 1982 Estes catalog pp 28-29. Challenger II #1330 "No painting required with...yellow nose cone..."
                          So it is evidently a PN 072013; Brohm errs by not designating it as molded in yellow.
                          Ref: photo of Helio Copter kit with yellow NC and separate white heli blade hub posted to TRF at
                          http://www.rocketryforum.com/showthread.php?137939-Info-on-the-Estes-PNC-56-nosecone&p=1651303#post1651303
                          Estes has used kit #1330 more than once:
                          Challenger II is #1330 in 1982 catalog, Estes site has #1330 instructions for the Free Fall
               060312   : chrome plated over plastic: black plastic in #2168 Metalizer, #2180 Chrome Dome Silver.  Seen over white
                          plastic with the same PN in a #1417 Astrobeam.
                          Ref: Metalizer instructions http://www.estesrockets.com/media/instructions/002168_METALIZER.pdf
               060340   : gold tone plated (black plastic?), #2181 Chrome Dome Gold
                          Ref: Chrome Dome instructions http://www.estesrockets.com/media/instructions/002181_Chrome_Domes_Gold.pdf
               072017   : molded in blue plastic, #2091 Maniac, #2443 Splendor.  No PNs in Maniac instructions.
                          Splendor is an E2X and gives PN; 2014 catalog shows it's blue and has 1.35" diam, correct for BT-56
               
               There is a PN 303164 "NC-56 Nose Cone (4 pk)" on the Estes site showing
               black nose cones that look to not be identical.  Discount Rocketry states on their site (2016)
               that the contents of PN 303164 change randomly among varaious shapes.  To
               date we don't have any information on the variants except a photo showing 3 black + 1 blue in an old 4-pack.
               
             Physical data here was measured from a PN 060312 from a #2168 Metalizer, mass 19.0 g
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-56, 072015</PartNumber>
            <Description>Nose cone, plastic, PNC-56, black, blow molded, PN 072015</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">1.346</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.289</ShoulderDiameter>
            <ShoulderLength Unit="in">0.983</ShoulderLength>
            <Length Unit="in">6.410</Length>
            <Thickness Unit="in">0.0730</Thickness>
        </NoseCone>

        <!--
            *** There is at least one transition available for BT-56/ST-13 ***
            see the Magnum Load in the #1401 Hunter's Choice Launch Set (2 rockets, 2014 catalog), which
            transitions from a 1.35" diam tube to something around 1.0", likely ST-10
            size.  We have no actual data on this part, and that's a rare kit.
        -->
            

        <!--
            Estes #2072 Scrambler and #1996 Eggspress - plastic egg capsule for ST-13/BT-56
            *** Get exact masses from kit I have ***

            The Scrambler/Eggspress capsule is identical to the old Centuri/Enerjet blow molded egg capsule from 1972
            that was designed to fit the Centuri ST-13 tubes.  It's shipped as a combined NC+transition part that has to
            be cut apart in the middle.  A 2" piece of ST-20 tube is inserted between the two parts.  For simulation
            purposes it has to be modeled as a separate NC and transition, so artificial part numbers are needed.

            Part numbers:  Unknown.  Neither the #2072 Scrambler nor #1996 Eggspress kit instructions give part numbers,
            and Brohm lists the PNs as unknown.  Oddly, Brohm has separate listings for the Scrambler and Eggspress
            capsules, where the Eggspress is shown (erroneously I believe) as BT-55.

            Colors: Enerjet versions of the capsule are molded in black (ref: I had one)
            Eggspress capsules were molded in yellow (ref: actual instance and kit card illustration)
            
            Dimensions: I have a #1996 Eggspress kit, and we also have the intended external dimensions from the fortuitous
            inclusion of a small dimensioned drawing in the 1972 Enerjet catalog.  Both the Egg Crate and later Estes
            Eggspress kit dimensions confirm (by subtracting out known body tube lengths) that the *designed* length of
            the egg capsule was 9.0", with a 2.5" long cone and 4.5" transition.  However, dimensions used here are from
            measurement of an actual molded-in-yellow Eggspress capsule, which has a 2.5" nose cone but a 4.60" long
            transition. Jim Parsons (K'Tesh on TRF) has posted a nicely done Eggspress NC.ork file with dimensions
            that agree with mine within 0.022".  At this point we don't know if the tooling has changed
            over time or if it's always been 0.1" off from the catalog specs.
            
            The functional shoulder length of the nose cone is 0.70", and there an additional very short conical section
            leading down to the cut point that is about 0.07" long.
            
            The functional shoulder length of the forward large end of the transition is also 0.70" but with a slightly
            longer conical taper down to the cut of about 0.10"
            
            The functional shoulder length of the rear small end of the transition is 1.18" with a further conical taper
            of 0.60" down to a oval with axis of 0.32" perpendicular to the seams and 0.36" aligned with the seams.

            Masses:  Parsons .ork has NC 17.7 g, transition 34.53 g (cyl + conical aft taper)
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-56-S, Scrambler</PartNumber>
            <Description>Nose cone, plastic, Scrambler, PNC-56-S, black</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">2.04</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.00</ShoulderDiameter>
            <ShoulderLength Unit="in">0.7</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="in">0.079</Thickness>
        </NoseCone>
        <Transition>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>TA-2013-S [R], Scrambler</PartNumber>
            <Description>Transition, plastic, Scrambler, TA-2013-S [R], reducing</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">2.04</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.00</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.70</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">1.346</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.304</AftShoulderDiameter>
            <AftShoulderLength Unit="in">1.18</AftShoulderLength>
            <Length Unit="in">4.60</Length>
            <Thickness Unit="in">0.079</Thickness>
        </Transition>

        <!-- PNC-60xx known to exist but with missing or incomplete data:
            
            PNC-60RL  PN 72305 (#1987 Sentinel, #1998 Mega Sizz' etc.).  Listed in Brohm.  Semroc has balsa clone so we have
               len = 8.4", shape slightly round tip ogive, shoulder length 1.0" in Semroc balsa version.  Weight should be
               nearly identical to PNC-80K at 1.6 oz.
        -->

        <!--
            "PNC-60A" - another complicated case.  There is no such NC in the Brohm index.  rocketreviews.com lists it
            as an ogive: http://www.rocketreviews.com/estes-ogive-nose-cone-4320.html
            with diam 1.6370 in, len 4.750 in, shoulder diam 1.5950 in, shoulder len 0.750 in, mass and PN not
            given. That is what is used here.  OR built-in file gave the mass as 24.38 gm.
            
            More recently there is a PN 303165 BT-60 nose cone assortment currently (Dec 2016) described
            on the Estes website as "003165 - NC-60 Long Nose Cone Asst. (3 pk)", and described as "NC-60A"
            or "PNC-60A" on some vendor sites.  The Estes "Quick Overview" says "NC-60 Nose cone will fit the
            BT-60 body tube"; here they are clearly using "NC-60" generically.  The photo on the Estes
            site shows 3 nose cones, all obviously blow molded in white plastic.  One is an ogive that looks
            to have similar dimensions to what is given here.  The other two have secondary pieces molded
            into the same unit - one is a nozzle flare and the other a boattail with engine hook channel.  The
            latter is very likely the unit used in the Astron Sprint XL.
            SiriusRocketry.biz shows what they called aa "PNC-60NA" nose cone that they say was broken
            out from the Estes 003165 set.  It is in black and is the ogive without a secondary piece, so
            a black variant is proven.
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60A, white</PartNumber>
            <Description>Nose cone, plastic, ogive, white, PNC-60A</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.75</Length>
            <Thickness Unit="in">0.093</Thickness>  <!-- set to make mass 24.3 g per original OR file -->
        </NoseCone>
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60A, black</PartNumber>
            <Description>Nose cone, plastic, ogive, black, PNC-60A</Description>
            <Material Type="BULK">Polyethylene, HDPE, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.75</Length>
            <Thickness Unit="in">0.093</Thickness>  <!-- set to make mass 24.3 g per original OR file -->
        </NoseCone>
        
        <!-- PNC-60AH ref 1988 catalog, KC-2 Der Red Max, KC-3 Patriot, injection molded, mass 28.4 g -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60AH, 071014</PartNumber>
            <Description>Nose cone, plastic, PNC-60AH, PN 071014</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.875</ShoulderLength>
            <Length Unit="in">6.625</Length>
            <Thickness Unit="in">0.0775</Thickness>
        </NoseCone>
        
        <!-- PNC-60L used for Estes Gooneybirds and Camroc Carrier, mass 11.1 g -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60L</PartNumber>
            <Description>Nose cone, plastic, PNC-60L, PN 071019</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="kg">0.01106</Mass>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="in">0.125</Thickness>
        </NoseCone>

        <!-- PNC-60MS ref 1988 catalog, mass 0.39 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60MS</PartNumber>
            <Description>Nose cone, plastic, PNC-60MS</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.39</Mass>
            <Shape>PARABOLIC</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.625</ShoulderLength>
            <Length Unit="in">3.125</Length>
            <Thickness Unit="in">0.125</Thickness>
        </NoseCone>
        
        <!-- PNC-60NA is for D-Region Tomahawk, etc. Data from RocketReviews -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-60NA</PartNumber>
            <Description>Nose cone, plastic, PNC-60NA</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">0.60</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">0.75</ShoulderLength>
            <Length Unit="in">4.75</Length>
            <Thickness Unit="in">0.125</Thickness>
        </NoseCone>
        
        <!--  PNC-60RL  PN 72305 (#1987 Sentinel, #1998 Mega Sizz' etc.).  Listed in Brohm.  Semroc has balsa clone so we have
              len = 8.4", shape slightly round tip ogive, shoulder length 1.0" in Semroc balsa version.  Weight estimated at
              65% more than PNC-60NA so around 1.0 oz -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-80RL, 72305</PartNumber>
            <Description>Nose cone, plastic, BT-60, 8.4", ogive, PNC-60RL, PN 72305</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.637</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.595</ShoulderDiameter>
            <ShoulderLength Unit="in">1.00</ShoulderLength>
            <Length Unit="in">8.4</Length>
            <Thickness Unit="in">0.125</Thickness>
            <Mass Unit="oz">1.0</Mass>
        </NoseCone>


        <!--
            PNC-80BB (#2018 Super Big Bertha, #1273 Fat Boy) 4 inch ellipsoid, blow molded
            Dimensions partially derived, looking for more definitive
            According to Brohm NC list and the Fat Boy instructions this is the *same* PN used on the Fat Boy; thus the
            "PNC-80FB" designation found only on RocketReviews is bogus.
            Length estimate:  Analysis of Fat Boy and Super Big Bertha catalog lengths vs known tube lengths and fin
            sweep estimates puts length of PNC-80BB between 4 and 4 3/8".
        -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-80BB, 72080</PartNumber>
            <Description>Nose cone, plastic, PNC-80BB, PN 72080</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">1.30</Mass>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">2.60</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.56</ShoulderDiameter>
            <ShoulderLength Unit="in">1.75</ShoulderLength>
            <Length Unit="in">4.0</Length>
            <Thickness Unit="in">0.125</Thickness>
        </NoseCone>

        <!-- PNC-80K ref 1985-1988 catalog.  Catalogs give incorrect OD of 2.555; BT-80 ID is 2.560, OD is 2.600 -->
        <NoseCone>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PNC-80K, 71035</PartNumber>
            <Description>Nose cone, plastic, PNC-80K, PN 71035</Description>
            <Material Type="BULK">Polystyrene, cast, bulk</Material>
            <Mass Unit="oz">1.68</Mass>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.60</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.560</ShoulderDiameter>
            <ShoulderLength Unit="in">1.625</ShoulderLength>
            <Length Unit="in">8.125</Length>
            <Thickness Unit="in">0.125</Thickness>
        </NoseCone>


        
        <!-- PNC-80SC, PN 072664 used only in #2141 Silver Comet, have no dimensions -->

        <!-- Parachutes (polyethylene) -->
        
        <!-- PK-8 ref 1975 catalog, which also has PK-12, PK-18, PK-24.  "A" versions
             appeared later.  There are many variations in printing and shroud line
             attachment methods.  Functionally they are virtually identical so only one
             listing is given for each size.  1974 Custom Parts Catalog gives PN 2260 for
             PK-8 and net wt of 0.35 oz.
        -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-8, 2260</PartNumber>
            <Description>Parachute kit, plastic, 8 in., PN 2260</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">8.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">8.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- Estes 9" preassembled parachute has PN "002268" on 2017 website -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>parachute_9in, 002268</PartNumber>
            <Description>Parachute, plastic, preassembled, 9 in., PN 002268</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">9.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">9.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-10 original, 1974 Custom Parts Catalog gives PN 2262, weight .074 oz, and preassembled -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-10, 2262</PartNumber>
            <Description>Parachute, plastic, preassembled, 10 in., PN 2262</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">10.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">10.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-12 original, 1974 custom parts catalog gives PN 2263, weight .078 oz -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-12, 2263</PartNumber>
            <Description>Parachute kit, plastic, 12 in., PN 2263</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-12A - #1214 Drifter instructions show PN 85564 -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-12A, 85564</PartNumber>
            <Description>Parachute kit, plastic, 12 in., PN 85564</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- 12in preassembled parachute: PN 002264 on 2017 website, 302264 in 2007 catalog -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>parachute_12in, 002264, 302264</PartNumber>
            <Description>Parachute, plastic, preassembled, 12 in., PN 002264/302264</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-18 original has PN 2266 in 1974 custom parts catalog, weight .144 oz -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-18, 2266</PartNumber>
            <Description>Parachute kit, plastic, 18 in., PN 2266</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PN for PK-18A was 85566 according to #1294 Cobra instructions -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-18A, 85566</PartNumber>
            <Description>Parachute kit, plastic, 18 in., PN 85566</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- 18in preassembled parachute:  PN 302267 in 2007 catalog, 002267 on 2017 website -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>parachute_18in, 302267, 002267</PartNumber>
            <Description>Parachute, plastic, preassembled, 18 in., PN 302267/002267</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- 18in Der Red Max plastic parachute PN 035823 on 2017 website -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>parachute_18in, 035823</PartNumber>
            <Description>Parachute, plastic, preassembled, 18 in., Der Red Max, PN 035823</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-24 was PN 2270 in 1974 custom parts catalog, weight .298 oz -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-24, 2270</PartNumber>
            <Description>Parachute kit, plastic, 24 in., PN 2270</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- PK-24A #1214 Drifter instructions show PN 85568 -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>PK-24A, 85568</PartNumber>
            <Description>Parachute kit, plastic, 24 in., PN 85568</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>
        <!-- 24in preassembled parachute PN 302271 in 2007 catalog, PN 002271 in 2017 website -->
        <Parachute>
            <Manufacturer>Estes</Manufacturer>
            <PartNumber>parachute_24in, 302271, 002271</PartNumber>
            <Description>Parachute, plastic, preassembled, 24 in., PN 302271/002271</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
        </Parachute>

        <!-- Launch lugs -->
        
        <!-- OD of 1/8 lugs measured at 0.173.  ID specified as 5/32 -->
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-2A, 2321</PartNumber>
          <Description>Launch lug, paper, 1/8 x 1.25", PN 2321</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.156</InsideDiameter>
          <OutsideDiameter Unit="in">0.173</OutsideDiameter>
          <Length Unit="in">1.25</Length>
        </LaunchLug>
    
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-2B, 2322</PartNumber>
          <Description>Launch lug, paper, 1/8 x 2.375", PN 2322</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.156</InsideDiameter>
          <OutsideDiameter Unit="in">0.173</OutsideDiameter>
          <Length Unit="in">2.375</Length>
        </LaunchLug>
    
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-2C, 2325</PartNumber>
          <Description>Launch lug, paper, 1/8 x 5", PN 2325</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.156</InsideDiameter>
          <OutsideDiameter Unit="in">0.173</OutsideDiameter>
          <Length Unit="in">5.0</Length>
        </LaunchLug>

        <!-- LL-2D had PNs 2326 and 2327 in its lifetime -->
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-2D, 2326, 2327</PartNumber>
          <Description>Launch lug, paper, 1/8 x 8", PN 2326/2327</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.156</InsideDiameter>
          <OutsideDiameter Unit="in">0.173</OutsideDiameter>
          <Length Unit="in">8.0</Length>
        </LaunchLug>

        <!-- LL-2E 9.5" long was used in the K-43 Mars Lander.  Plans on JimZ do not give numeric PN,
             instructions not on Estes site as of 2018. -->
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-2E</PartNumber>
          <Description>Launch lug, paper, 1/8 x 9.5"</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.156</InsideDiameter>
          <OutsideDiameter Unit="in">0.173</OutsideDiameter>
          <Length Unit="in">9.5</Length>
        </LaunchLug>

        <!-- PN 2328 3/16 x 2 possibly has LL designation; no Estes ref but Semroc calls
             it LL-3B.  Carl McLawhorn was pretty careful about his PNs for exact Estes parts -->
        <!-- 3/16 lug measured OD = 0.227, set ID to give same wall thickness as 1/8 lugs -->
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LL-3B, 2328</PartNumber>
          <Description>Launch lug, paper, 3/16 x 2", PN 2328</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.210</InsideDiameter>
          <OutsideDiameter Unit="in">0.227</OutsideDiameter>
          <Length Unit="in">2.0</Length>
        </LaunchLug>

        <!-- 1/4" lug exists in PN 302320 Launch Lug Pack but length and PN not specified -->
        <!-- ***ID/OD estimated using 9/32 ID and giving it .021 wall -->
        <LaunchLug>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>LaunchLug_0.25_x_2.0</PartNumber>
          <Description>Launch lug, paper, 1/4 x 2"</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
          <InsideDiameter Unit="in">0.281</InsideDiameter>
          <OutsideDiameter Unit="in">0.323</OutsideDiameter>
          <Length Unit="in">2.0</Length>
        </LaunchLug>

        
        
        <!-- Centering rings -->
        <!-- InsideDiameter, OutsideDiameter, Length -->
        
        <!-- AR type thick fiber centering rings -->
        <!-- AR-2050, 1974 parts catalog weight 0.143 oz -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>AR-2050, 3101</PartNumber>
          <Description>Centering ring, fiber, thick, BT-20 to BT-50, PN 3101</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">0.948</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </CenteringRing>
    
        <!-- AR-5055, 1974 parts catalog weight 0.62 oz (seems anomalous)-->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>AR-5055, 3103</PartNumber>
          <Description>Centering ring, fiber, thick, BT-50 to BT-55, PN 3103</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.978</InsideDiameter>
          <OutsideDiameter Unit="in">1.281</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </CenteringRing>

        <!-- AR-520, 1974 parts catalog weight 0.105 oz -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>AR-520, 30162</PartNumber>
          <Description>Centering ring, fiber, thick, BT-5 to BT-20, PN 30162</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.543</InsideDiameter>
          <OutsideDiameter Unit="in">0.708</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </CenteringRing>

        <!-- Thin fiber centering rings -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-550, 30118</PartNumber>
          <Description>Centering ring, fiber, BT-5 to BT-50, PN 30118</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.543</InsideDiameter>
          <OutsideDiameter Unit="in">0.948</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </CenteringRing>

        <!-- Thin fiber centering rings -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-560, 30120</PartNumber>
          <Description>Centering ring, fiber, BT-5 to BT-60, PN 30120</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.543</InsideDiameter>
          <OutsideDiameter Unit="in">1.593</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- RA-3050 appeared in PN 85013 Multi-Purpose Set (1990 catalog) -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-3050</PartNumber>
          <Description>Centering ring, fiber, BT-30 to BT-50</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.767</InsideDiameter>
          <OutsideDiameter Unit="in">0.948</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- RA-2050 managed to keep the same PN, avoiding the renumbering -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-2050, 3110</PartNumber>
          <Description>Centering ring, fiber, BT-20 to BT-50, PN 3110</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">0.948</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- RA-2055 was PN 3112 in 1974 part catalog, 3111 in 1990 catalog -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-2055, 3112, 3111</PartNumber>
          <Description>Centering ring, fiber, BT-20 to BT-55, PN 3112/3111</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">1.281</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- RA-2056 appeared in PN 302295 Flat Centering Ring Pack and later in PN 3158
             Engine Mount Kit -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-2056</PartNumber>
          <Description>Centering ring, fiber, BT-20 to BT-56</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">1.302</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- RA-2060 was PN 3114 in 1974 parts catalog, 3113 in 1990 catalog -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-2060, 3114</PartNumber>
          <Description>Centering ring, fiber, BT-20 to BT-60, PN 3114</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">1.593</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-2070, 3115</PartNumber>
          <Description>Centering ring, fiber, BT-20 to BT-70, PN 3115</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.738</InsideDiameter>
          <OutsideDiameter Unit="in">2.173</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        <!-- A 6080 ring appeared in PN 302295 Flat Centering Ring Pack.  There is no
             known history of it being called RA-6080 but I gave it that designataion
             since everyone will know what it is -->
        <CenteringRing>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>RA-6080*</PartNumber>
          <Description>Centering ring, fiber, BT-60 to BT-80</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">1.639</InsideDiameter>
          <OutsideDiameter Unit="in">2.556</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>

        

        <!-- Engine Blocks -->
        <EngineBlock>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>EB-5B, 3130</PartNumber>
          <Description>Engine block, fiber, BT-5, 0.188" len, PN 3130</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.49</InsideDiameter>
          <OutsideDiameter Unit="in">0.512</OutsideDiameter>
          <Length Unit="in">0.188</Length>
        </EngineBlock>

        <EngineBlock>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>EB-20A, 3132</PartNumber>
          <Description>Engine block, fiber, BT-20, 0.25" len, PN 3132</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.65</InsideDiameter>
          <OutsideDiameter Unit="in">0.708</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </EngineBlock>

        <EngineBlock>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>EB-20B, 3134</PartNumber>
          <Description>Engine block, fiber, BT-20, 0.125" len, PN 3134</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.65</InsideDiameter>
          <OutsideDiameter Unit="in">0.708</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </EngineBlock>

        <EngineBlock>
          <Manufacturer>Estes</Manufacturer>
          <PartNumber>EB-30A, 3136</PartNumber>
          <Description>Engine block, fiber, BT-30, 0.25" len, PN 3136</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="in">0.65</InsideDiameter>
          <OutsideDiameter Unit="in">0.724</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </EngineBlock>



        <!-- *** Bulkheads *** -->
        <!-- OutsideDiameter, Length, Filled (usually true) -->
        <!-- may not need, NB-xx are defined as TubeCoupler -->
        
        
    </Components>
</OpenRocketComponent>
