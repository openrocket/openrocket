<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
Madcow Rocketry components file for OpenRocket

by Dave Cook  NAR 21953  caveduck17@gmail.com  2017-2018

Data in this file is primarily from the Madcow website madcowrocketry.com
As far as I know, Madcow Rocketry has never offered a print catalog or a comprehensive
electronic catalog.

I have not made individual components for all the fiberglass tube colors as it would make the tube
listings 7x larger, to the point of being unwieldy.  Only the natural tube SKU is listed.

COMPLETED:

Cardboard body tubes
Cardboard couplers
Thin wall fiberglass tubes
Std wall fiberglass tubes
Std wall fiberglass couplers
Fiberglass Switch band tube segments
Carbon fiber composite body tubes
Carbon fiber couplers
Fiberglass composite tip nose cones - thin wall
Fiberglass composite tip nose cones - std wall
Fiberglass metal tip nose cones - std wall (starting at 4")
Plastic nose cones
Balsa tail cones for 2.6" to 38mm and 29mm mounts
Fiberglass 2.6 to 29mm and 4.0 to 54mm tail cones
Centering rings - G10
Centering rings - plywood
Launch lugs 1/4 x 1"
Fiberglass transition 4" to 54mm (DoubleShot)
Parachutes - generic nylon

TODO
====
Nike-Apache 38mm FG nose cone (7.75" cone plus 1.0" cylindrical extension, mass 78 gm)

DEFERRED
========
Balsa ramjet cones - no published data

RESEARCH NOTES
==============

Product Line and Nomenclature:

Madcow sells G12 airframe tubes by the foot, with "full" 60 inch and "half" 30 inch lengths selectable,
as well as custom lengths for sizes 6.0" and below.

G12 airframes FT60 and smaller are offered in colors (dyed in the resin) with the following
suffixes on the SKU:
  NAT  : Natural green (not dyed)
  BLU  : Blue
  RED  : Red
  FLY  : Yellow
  FLG  : Lime Green
  FLO  : Orange
  BLK  : Black

Fiberglass coupler SKU nomenclature is inconsistent.  Mostly the SKUs reflect the size in
inches, but FC29, FC38, and FC54 and the corresponding cardboard T29, T38 and T54 are in mm.

Size indicators in the SKUs are also inconsistent with respect to the actual size.
For some tubes the SKU designator seems to reflect the OD (FT16, FT40), but for others the
SKU prefix reflects the ID (FT30, FT50).

Fiberglass coupler and tube SKUs do not all match.  The FT11, FT16 and FT22 tubes 
mate with FC29, FC38 and FC54 couplers.

There is no cardboard 29mm coupler, and no G12 11.5" coupler.

There is no 29mm thin wall G12 tube.

There is no 2.6" heavy wall tube; FT26 is offered in thin wall only.

There are no thin wall tubes larger than FT30 listed.  However the description of the "HV ARCAS FS
Lite" kit (SKU KIT-ARCAS_HV_FS-LT) implies that 4.5" diameter thin wall tube exists.

ERRATA IN UPSTREAM DATA
=======================

SOURCE ERROR: The SKUs generated for all sizes of G12 fiberglass couplers are wacky.  Looks to
be website programming problems.  This remains true as of October 2020.

MISSING UPSTREAM DATA
=====================

SOURCE ERROR: Dimensions and weights are missing entirely for tube FT115 and coupler FC45.

SOURCE ERROR: ID/OD dimensions are missing for all cardboard tube couplers; only the length is
given.  The C39-800HD coupler for T39 tube has a noticeably thicker wall in the photo, and it is
designated as "heavy duty".

SOURCE ERROR: Thickness of plywood bulkheads and all G10 centering rings not specified.

SOURCE ERROR: Thickness of all avbay lid G10 bulkheads not specified.

SOURCE ERROR: Fiberglass nose cone dimensions and weights are very spotty.  Most are identified as "5:1 conical"
at least; though in some cases where both the fineness and exposed length are given they do not
agree very well.

SOURCE ERROR: Weights per foot of coupler tubes larger than FC30 are not given, except for FT80.

-->
      
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, spiral kraft glassine, bulk</Name>
            <Density>798.85</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/cm3">
            <Name>Polystyrene, cast, bulk</Name>
            <Density>1050.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, light, bulk</Name>
            <Density>352.4</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, bulk</Name>
            <Density>657.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- G10 is quoted on MatWeb.com as 1.80 g/cc -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, G10, bulk</Name>
            <Density>1800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, G12, filament wound tube, bulk</Name>
            <Density>1820.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- Derived density of Madcow filament wound carbon fiber tubes
             The quoted weight of FWCF-29 is much less than it should be and probably
             implies there is considerable excess resin.
        -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Carbon fiber epoxy composite, Madcow FWCF, bulk</Name>
            <Density>1250.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Polypropylene, bulk</Name>
            <Density>946.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- Madcow has very few balsa parts; I adopted 8 lb/ft3 balsa as a likely average -->
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 8 lb/ft3</Name>
          <Density>128.1</Density>
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

        <Material UnitsOfMeasure="g/m">
            <Name>Nylon cord, flat braid, 325 lb, 1/8 in.</Name>
            <Density>0.00417</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 1.1 oz is Top Flight thin mill material -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.1 oz actual</Name>
            <Density>0.03735</Density>
            <Type>SURFACE</Type>
        </Material>

        <!-- 1.7oz ripstop nylon is Top Flight and LOC standard parachute material -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.7 oz actual</Name>
            <Density>0.05764</Density>
            <Type>SURFACE</Type>
        </Material>

        
    </Materials>

    <Components>

      <!-- Body Tubes - cardboard, complete per 2017 website
           Madcow tubes are mostly the same dimensions as LOC tubes,
           but come in different lengths, and with more slotted versions.
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T29-800</PartNumber>
        <Description>Body tube, paper, 29mm MMT, 8.0 in., PN T29-800</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.140</InsideDiameter>
        <OutsideDiameter Unit="in">1.215</OutsideDiameter>
        <Length Unit="in">22.0</Length>
      </BodyTube>
      <!-- 38mm "thin wall" tube has .0425 wall -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>TW38-220</PartNumber>
        <Description>Body tube, paper, 1.6" dia, 22.0 in., PN TW38-220</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.525</InsideDiameter>
        <OutsideDiameter Unit="in">1.610</OutsideDiameter>
        <Length Unit="in">22.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>TW38-220-S3</PartNumber>
        <Description>Body tube, paper, 1.6" dia, 22.0 in., 3 slots, PN TW38-220-S3</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.525</InsideDiameter>
        <OutsideDiameter Unit="in">1.610</OutsideDiameter>
        <Length Unit="in">22.0</Length>
      </BodyTube>
      <!-- 38mm MMT has .055 wall -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T38-800</PartNumber>
        <Description>Body tube, paper, 38mm MMT, 8.0 in., PN T38-800</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.525</InsideDiameter>
        <OutsideDiameter Unit="in">1.635</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T38-180</PartNumber>
        <Description>Body tube, paper, 38mm MMT, 18.0 in., PN T38-180</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.525</InsideDiameter>
        <OutsideDiameter Unit="in">1.635</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- T54-180 54mm MMT dimensions not given.  Used LOC dimensions here with .060 wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T54-180</PartNumber>
        <Description>Body tube, paper, 54mm MMT, 18.0 in., PN T54-180</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.14</InsideDiameter>
        <OutsideDiameter Unit="in">2.26</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </BodyTube>
      <!-- Regular 54mm tube has different dimensions than T54 MMT -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T22-215</PartNumber>
        <Description>Body tube, paper, T22, 21.5 in., PN T22-215</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.245</OutsideDiameter>
        <Length Unit="in">21.5</Length>
      </BodyTube>
      <!-- 3 slot T22-215S3 has slots 4.25" long, 0.5" from end, 1/8" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T22-215S3</PartNumber>
        <Description>Body tube, paper, T22, 21.5 in., 3 slots, PN T22-215S3</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.175</InsideDiameter>
        <OutsideDiameter Unit="in">2.245</OutsideDiameter>
        <Length Unit="in">21.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T26-255</PartNumber>
        <Description>Body tube, paper, T26, 25.5 in., PN T26-255</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.558</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">25.5</Length>
      </BodyTube>
      <!-- 3 slot T26-255S3 has slots 4.25" long, 0.5" from end, 1/8" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T26-255S3</PartNumber>
        <Description>Body tube, paper, T26, 25.5 in., 3 slots, PN T26-255</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.558</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">25.5</Length>
      </BodyTube>
      <!-- 4 slot T26-255S3 has slots 4.25" long, 0.5" from end, 1/8" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T26-255S4</PartNumber>
        <Description>Body tube, paper, T26, 25.5 in., 4 slots, PN T26-255</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.558</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">25.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T30-210</PartNumber>
        <Description>Body tube, paper, T30, 21.0 in., PN T30-210</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.100</OutsideDiameter>
        <Length Unit="in">21.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T30-240</PartNumber>
        <Description>Body tube, paper, T30, 24.0 in., PN T30-240</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.100</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <!-- 4 slots for T30-240S4 are 3.5" long, 1.0" from end, 1/8" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T30-240S4</PartNumber>
        <Description>Body tube, paper, T30, 24.0 in., 4 slots, PN T30-240S4</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.100</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <!-- T40 tube was formerly called T39 before about 2020 -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-310, T39-310</PartNumber>
        <Description>Body tube, paper, T39, 31.0 in., PN T40-310</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">31.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-310S3, T39-310S3</PartNumber>
        <Description>Body tube, paper, T39, 31.0 in., 3 slots, PN T40-310S3</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">31.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-310S4, T39-310S4</PartNumber>
        <Description>Body tube, paper, T39, 31.0 in., 4 slots, PN T40-310S4</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">31.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-190, T39-190</PartNumber>
        <Description>Body tube, paper, T39, 19.0 in., PN T40-190</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">19.0</Length>
      </BodyTube>
      <!-- T40-190S3 3 slots are 4.25" long, 0.5" from end, 1/4" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-190S3, T39-190S3</PartNumber>
        <Description>Body tube, paper, T39, 19.0 in., 3 slots, PN T40-190S3</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">19.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-120, T39-120</PartNumber>
        <Description>Body tube, paper, T39, 12.0 in., PN T40-120</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- T40-120S3 3 slots are 4.25" long, 0.5" from end, 1/4" wide -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-120S3, T39-120S3</PartNumber>
        <Description>Body tube, paper, T39, 12.0 in., 3 slots, PN T40-120S3</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <!-- T40-010 is 1" switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>T40-010, T39-010</PartNumber>
        <Description>Switch band, paper, T39, 1.0 in., PN T40-010</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.000</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>

      <!-- Cardboard tube couplers -->
      <!-- Dimensions for all these are estimated based on .061 wall for ones below C39, and
           a .115 wall for C39-800HD
      -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>C38-400</PartNumber>
        <Description>Tube coupler, paper, T38, 4.0 in., PN C38-400</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">1.400</InsideDiameter>
        <OutsideDiameter Unit="in">1.522</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>C22-400</PartNumber>
        <Description>Tube coupler, paper, T54, 4.0 in., PN C22-400</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.050</InsideDiameter>
        <OutsideDiameter Unit="in">2.172</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>C26-500</PartNumber>
        <Description>Tube coupler, paper, T26, 5.0 in., PN C26-500</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.433</InsideDiameter>
        <OutsideDiameter Unit="in">2.555</OutsideDiameter>
        <Length Unit="in">5.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>C30-600</PartNumber>
        <Description>Tube coupler, paper, T30, 6.0 in., PN C30-600</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">2.875</InsideDiameter>
        <OutsideDiameter Unit="in">2.997</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>C40-800, C39-800HD</PartNumber>
        <Description>Tube coupler, paper, T40, heavy wall, 8.0 in., PN C40-800</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">3.667</InsideDiameter>
        <OutsideDiameter Unit="in">3.897</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>
      <!-- website (2020) also lists a C55-900, said to be for Polecat 5.5" cardboard airframe -->
      

      <!-- ============================== -->
      <!-- Thin wall G12 fiberglass tubes -->
      <!-- ============================== -->

      <!-- No 29mm thin wall tube listed -->

      <!-- FT16 thin wall weight per foot 2.570 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-THIN-600-NAT</PartNumber>
        <Description>Body tube, 38mm, G12 FWFG, thin wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.600</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-THIN-300-NAT</PartNumber>
        <Description>Body tube, 38mm, G12 FWFG, thin wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.600</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT22 thin wall weight per foot 3.980 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-THIN-600-NAT</PartNumber>
        <Description>Body tube, 54mm, G12 FWFG, thin wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.230</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-THIN-300-NAT</PartNumber>
        <Description>Body tube, 54mm, G12 FWFG, thin wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.230</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT26 thin wall weight per foot quoted as 5.200 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT26-THIN-600-NAT</PartNumber>
        <Description>Body tube, 2.6in, G12 FWFG, thin wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT26-THIN-300-NAT</PartNumber>
        <Description>Body tube, 2.6in, G12 FWFG, thin wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT30 thin wall weight per foot 5.730 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-THIN-600-NAT</PartNumber>
        <Description>Body tube, 3.0in, G12 FWFG, thin wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.098</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-THIN-300-NAT</PartNumber>
        <Description>Body tube, 3.0in, G12 FWFG, thin wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.098</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>
      <!-- No thin wall fiberglass tubing above 3.0" size -->
      

      <!-- ================================================ -->
      <!-- Heavy wall (0.055 to 0.067) G12 fiberglass tubes -->
      <!-- ================================================ -->

      <!-- 29mm FT11: mass of FT11 tube quoted as 2.428 oz per ft, so 5 ft tube should be 11.240 oz
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT11-STD-600-NAT</PartNumber>
        <Description>Body tube, 29mm, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- half length FT11 should weigh 5.62 oz. -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT11-STD-300-NAT</PartNumber>
        <Description>Body tube, 29mm, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>
      
      <!-- FT16 heavy wall weight per foot 4.028 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-STD-600-NAT</PartNumber>
        <Description>Body tube, 38mm, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-STD-300-NAT</PartNumber>
        <Description>Body tube, 38mm, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>
      
      <!-- FT22 heavy wall weight per foot 6.028 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-STD-600-NAT</PartNumber>
        <Description>Body tube, 54mm, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-STD-300-NAT</PartNumber>
        <Description>Body tube, 54mm, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>
      <!-- There is no FT26 heavy wall; thin only -->

      <!-- FT30 heavy wall weight per foot 7.580 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-STD-600-NAT</PartNumber>
        <Description>Body tube, 3.0in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-STD-300-NAT</PartNumber>
        <Description>Body tube, 3.0in, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT40 heavy wall weight per foot 12.80 oz -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT40-STD-600-NAT</PartNumber>
        <Description>Body tube, 4.0in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT40-STD-300-NAT</PartNumber>
        <Description>Body tube, 4.0in, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT45 heavy wall weight per foot 16.80 oz *** value appears too high *** -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT45-STD-600-NAT</PartNumber>
        <Description>Body tube, 4.5in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.375</InsideDiameter>
        <OutsideDiameter Unit="in">4.500</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT45-STD-300-NAT</PartNumber>
        <Description>Body tube, 4.5in, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.375</InsideDiameter>
        <OutsideDiameter Unit="in">4.500</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT50 heavy wall weight per foot 19 oz. -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT50-STD-600-NAT</PartNumber>
        <Description>Body tube, 5.0in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.000</InsideDiameter>
        <OutsideDiameter Unit="in">5.150</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT50-STD-300-NAT</PartNumber>
        <Description>Body tube, 5.0in, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.000</InsideDiameter>
        <OutsideDiameter Unit="in">5.150</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT55 heavy wall weight per foot given as 16.80 oz
           SOURCE ERROR: FT55 weight per foot cannot be the same as for FT45 and less than FT50
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT55-STD-600-NAT</PartNumber>
        <Description>Body tube, 5.5in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.375</InsideDiameter>
        <OutsideDiameter Unit="in">5.525</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT55-STD-300-NAT</PartNumber>
        <Description>Body tube, 5.5in, G12 FWFG, heavy wall, natural, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.375</InsideDiameter>
        <OutsideDiameter Unit="in">5.525</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT60 heavy wall weight per foot 24 oz. -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT60-STD-600-NAT</PartNumber>
        <Description>Body tube, 6.0in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT60-STD-300-NAT</PartNumber>
        <Description>Body tube, 6.0in, G12 FWFG, heavy wall, 30.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">30.0</Length>
      </BodyTube>

      <!-- FT75 7.5" fiberglass heavy wall tube only offered in 60" length and appears to
           be custom order (quoted two week lead time, and no custom cuts offered).
           Color limited to natural green.
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT75</PartNumber>
        <Description>Body tube, 7.5in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.518</InsideDiameter>
        <OutsideDiameter Unit="in">7.708</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>

      <!-- FT80 heavy wall weight per foot quoted as 27 oz.
           Only offered in 60" length with two week lead time and no custom cuts.
           Color limited to natural green.
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT80</PartNumber>
        <Description>Body tube, 8.0in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.800</InsideDiameter>
        <OutsideDiameter Unit="in">8.005</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>

      <!-- FT115 11.5" heavy wall has no dimensions given.  I have assumed OD=11.500 and ID=11.298,
           wall=.102 as per FT80.
           Only offered in 60" length with 4-5 week lead time and no custom cuts.
           Color limited to natural green.
           SOURCE ERROR: dimensions for FT115 missing on Madcow site.
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT115</PartNumber>
        <Description>Body tube, 11.5in, G12 FWFG, heavy wall, natural, 60.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">11.298</InsideDiameter>
        <OutsideDiameter Unit="in">11.500</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>


      <!-- ================================================================ -->
      <!-- G12 Fiberglass Switch Bands -->
      <!-- ================================================================ -->
      <!-- SOURCE ERROR: The SKUs shown on the Madcow website don't generate correctly for the
           selected wall thickness and color (natural, blue, red, yellow, green, orange, black).

           I use designators consistent with other tubes of the same size and wall thickness here to
           avoid confusion.  Erroneous and inconsistent SKUs are appended to the PN field.
      -->

      <!-- FT16 38mm G12 thin wall switch band -->
      <!--
           SOURCE ERROR: Madcow website shows "FT11-010" desination for 38mm switch bands.
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-THIN-010-NAT FT11-010</PartNumber>
        <Description>Switch band, 38mm, G12 FWFG, thin wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.600</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      <!-- 38mm G12 std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT16-STD-010-NAT FT11-010</PartNumber>
        <Description>Switch band, 38mm, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>

      <!-- FT22 thin wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-THIN-010-NAT FT22-010</PartNumber>
        <Description>Switch band, 54mm, G12 FWFG, thin wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.230</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      <!-- FT22 std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT22-STD-010-NAT FT22-010</PartNumber>
        <Description>Switch band, 54mm, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      
      <!-- FT26 thin wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT26-THIN-010-NAT FT26-010</PartNumber>
        <Description>Switch band, 2.6in, G12 FWFG, thin wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.560</InsideDiameter>
        <OutsideDiameter Unit="in">2.640</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      
      <!-- No FT26 std wall switch band -->

      <!-- 3in thin wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-THIN-010-NAT FT30-010</PartNumber>
        <Description>Switch band, 3.0in, G12 FWFG, thin wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.098</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      
      <!-- 3in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT30-STD-010-NAT FT30-010</PartNumber>
        <Description>Switch band, 3.0in, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>

      <!-- 4in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT40-STD-010-NAT FT40-010</PartNumber>
        <Description>Switch band, 4.0in, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>

      <!-- 4.5in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT45-STD-010-NAT FT45-010</PartNumber>
        <Description>Switch band, 4.5in, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.375</InsideDiameter>
        <OutsideDiameter Unit="in">4.500</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>
      
      <!-- 5in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT50-STD-010-NAT FT50-010</PartNumber>
        <Description>Switch band, 5.0in, G12 FWFG, heavy wall, natural, 1.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.000</InsideDiameter>
        <OutsideDiameter Unit="in">5.150</OutsideDiameter>
        <Length Unit="in">1.0</Length>
      </BodyTube>

      <!-- 5.5in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT55-STD-020-NAT FT55-020</PartNumber>
        <Description>Switch band, 5.5in, G12 FWFG, heavy wall, natural, 2.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.375</InsideDiameter>
        <OutsideDiameter Unit="in">5.525</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>

      <!-- 6in std wall switch band -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT60-STD-020-NAT FT6-020</PartNumber>
        <Description>Switch band, 6.0in, G12 FWFG, heavy wall, natural, 2.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>
      
      <!-- 8in std wall switch band.  Not offered in colors -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FT80-020 FT8-020</PartNumber>
        <Description>Switch band, 8.0in, G12 FWFG, heavy wall, natural, 2.0 in.</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.815</InsideDiameter>
        <OutsideDiameter Unit="in">8.005</OutsideDiameter>
        <Length Unit="in">2.0</Length>
      </BodyTube>


      <!-- ================================================================ -->
      <!-- G12, filament wound fiberglass coupler tubes, wall 0.072 typical -->
      <!-- ================================================================ -->
      
      <!-- FC29, weight per foot quoted as 2.428 oz = 0.202 oz/in -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC29_4in</PartNumber>
        <Description>Tube coupler, 29mm, G12 FWFG, 4.0 in., PN FC29</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.000</InsideDiameter>
        <OutsideDiameter Unit="in">1.143</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC29_6in</PartNumber>
        <Description>Tube coupler, 29mm, G12 FWFG, 6.0 in., PN FC29</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.000</InsideDiameter>
        <OutsideDiameter Unit="in">1.143</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>

      <!-- FC38, weight per foot quoted as 4.028 oz = 0.335 oz/in -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC38_4in</PartNumber>
        <Description>Tube coupler, 38mm, G12 FWFG, 4.0 in., PN FC38</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC38_6in</PartNumber>
        <Description>Tube coupler, 38mm, G12 FWFG, 6.0 in., PN FC38</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC38_8in</PartNumber>
        <Description>Tube coupler, 38mm, G12 FWFG, 8.0 in., PN FC38</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>

      <!-- FC54, weight per foot quoted as 6.028 oz = 0.502 oz/in -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC54_6in</PartNumber>
        <Description>Tube coupler, 54mm, G12 FWFG, 6.0 in., PN FC54</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.000</InsideDiameter>
        <OutsideDiameter Unit="in">2.150</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC54_8in</PartNumber>
        <Description>Tube coupler, 54mm, G12 FWFG, 8.0 in., PN FC54</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.000</InsideDiameter>
        <OutsideDiameter Unit="in">2.150</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>

      <!-- FC26 (2.6" tube), weight per foot quoted as 5.2 oz = 0.433 oz/in
           This is less than for FC54 but FC26 is thin-wall.
      -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC26_6in</PartNumber>
        <Description>Tube coupler, 2.6in, G12 FWFG, 6.0 in., PN FC26</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.475</InsideDiameter>
        <OutsideDiameter Unit="in">2.558</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC26_8in</PartNumber>
        <Description>Tube coupler, 2.6in, G12 FWFG, 8.0 in., PN FC26</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.475</InsideDiameter>
        <OutsideDiameter Unit="in">2.558</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>

      <!-- FC30 (3.0" tube), weight per foot not quoted.  Using G12 density gives 7.16 oz/ft -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC30_6in</PartNumber>
        <Description>Tube coupler, 3.0in, G12 FWFG, 6.0 in., PN FC30</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.875</InsideDiameter>
        <OutsideDiameter Unit="in">2.998</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC30_9in</PartNumber>
        <Description>Tube coupler, 3.0in, G12 FWFG, 9.0 in., PN FC30</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">2.875</InsideDiameter>
        <OutsideDiameter Unit="in">2.998</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </TubeCoupler>

      <!-- FC40 (4.0" tube), weight per foot not quoted.  Using G12 density gives 10.9 oz/ft
           Prior to 2020 the SKU was FC39 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC40_8in</PartNumber>
        <Description>Tube coupler, 4.0in, G12 FWFG, 8.0 in., PN FC40</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.755</InsideDiameter>
        <OutsideDiameter Unit="in">3.899</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC40_9in</PartNumber>
        <Description>Tube coupler, 4.0in, G12 FWFG, 9.0 in., PN FC40</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.755</InsideDiameter>
        <OutsideDiameter Unit="in">3.899</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC40_12in</PartNumber>
        <Description>Tube coupler, 4.0in, G12 FWFG, 12.0 in., PN FC40</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">3.755</InsideDiameter>
        <OutsideDiameter Unit="in">3.899</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>

      <!-- FC45: dimensions not specified - reconstructed to use same offsets as others
           Using G12 density gives 12.3 oz/ft
           SOURCE ERROR: FC45 dimensions not given on Madcow website.
      -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC45_9in</PartNumber>
        <Description>Tube coupler, 4.5in, G12 FWFG, 9.0 in., PN FC45</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.229</InsideDiameter>
        <OutsideDiameter Unit="in">4.373</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC45_12in</PartNumber>
        <Description>Tube coupler, 4.5in, G12 FWFG, 12.0 in., PN FC45</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.229</InsideDiameter>
        <OutsideDiameter Unit="in">4.373</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>

      <!-- FC50 (5.0" tube), weight per foot not specified.  Using G12 density gives 17.8 oz/ft -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC50_10in</PartNumber>
        <Description>Tube coupler, 5.0in, G12 FWFG, 10.0 in., PN FC50</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.815</InsideDiameter>
        <OutsideDiameter Unit="in">4.998</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC50_12in</PartNumber>
        <Description>Tube coupler, 5.0in, G12 FWFG, 12.0 in., PN FC50</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">4.815</InsideDiameter>
        <OutsideDiameter Unit="in">4.998</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>

      <!-- FC55 (5.5" tube) - weight per foot not specified, using G12 density gives 19.1 oz/ft -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC55_12in</PartNumber>
        <Description>Tube coupler, 5.5in, G12 FWFG, 12.0 in., PN FC55</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.180</InsideDiameter>
        <OutsideDiameter Unit="in">5.373</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC55_14in</PartNumber>
        <Description>Tube coupler, 5.5in, G12 FWFG, 14.0 in., PN FC55</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.180</InsideDiameter>
        <OutsideDiameter Unit="in">5.373</OutsideDiameter>
        <Length Unit="in">14.0</Length>
      </TubeCoupler>

      <!-- FC60 (6.0" tube), weight per foot not specified.  Using G12 density gives 26.0 oz/ft -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC60_12in</PartNumber>
        <Description>Tube coupler, 6.0in, G12 FWFG, 12.0 in., PN FC60</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.775</InsideDiameter>
        <OutsideDiameter Unit="in">5.998</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC60_14in</PartNumber>
        <Description>Tube coupler, 6.0in, G12 FWFG, 14.0 in., PN FC60</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">5.775</InsideDiameter>
        <OutsideDiameter Unit="in">5.998</OutsideDiameter>
        <Length Unit="in">14.0</Length>
      </TubeCoupler>

      <!-- FC75 (7.5" tube), new prior to Apr 2020 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC75-160</PartNumber>
        <Description>Tube coupler, 7.5in, G12 FWFG, 16.0 in., PN FC75-160</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.330</InsideDiameter>
        <OutsideDiameter Unit="in">7.516</OutsideDiameter>
        <Length Unit="in">16.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC75-180</PartNumber>
        <Description>Tube coupler, 7.5in, G12 FWFG, 18.0 in., PN FC75-180</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.330</InsideDiameter>
        <OutsideDiameter Unit="in">7.516</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </TubeCoupler>

      <!-- FC80 (8.0" tube)
           Mfr data: in the FWNC80 nose cone section, Madcow says 11 in of FC80 weighs 25 oz
           or 27.3 oz/ft.
      -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC80-160</PartNumber>
        <Description>Tube coupler, 8.0in, G12 FWFG, 16.0 in., PN FC80-160</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.500</InsideDiameter>
        <OutsideDiameter Unit="in">7.795</OutsideDiameter>
        <Length Unit="in">16.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FC80-180</PartNumber>
        <Description>Tube coupler, 8.0in, G12 FWFG, 18.0 in., PN FC80-180</Description>
        <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
        <InsideDiameter Unit="in">7.500</InsideDiameter>
        <OutsideDiameter Unit="in">7.795</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </TubeCoupler>
      
      <!-- Coupler for 11.5" tube (which would be FC115) doesn't exist on website -->

      <!-- ========================== -->
      <!-- Carbon Fiber Airframe Tube -->
      <!-- ========================== -->
      <!-- Madcow resells CF tube from another vendor, with lead times quoted at 1 week.
           Tubes are available in 12, 24, 36, 48, and 60 inch lengths in black only.
      -->
      <!-- FWCF-29 CF tubing quoted weight is 1.8 oz/ft, ID 1.145", OD 1.255", .055 wall
           *** Standard 1.6 density of CF composite makes the weight come out at 2.3 oz/ft,
           significantly too high.  Suspect the tube may have a high resin proportion ***
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29-120</PartNumber>
        <Description>Body tube, 29mm, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29-240</PartNumber>
        <Description>Body tube, 29mm, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29-360</PartNumber>
        <Description>Body tube, 29mm, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29-480</PartNumber>
        <Description>Body tube, 29mm, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29-600</PartNumber>
        <Description>Body tube, 29mm, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.145</InsideDiameter>
        <OutsideDiameter Unit="in">1.255</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- FWCF-38 CF tubing size and weight not given, dimensions here assumed same as FT38
           standard wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38-120</PartNumber>
        <Description>Body tube, 38mm, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38-240</PartNumber>
        <Description>Body tube, 38mm, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38-360</PartNumber>
        <Description>Body tube, 38mm, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38-480</PartNumber>
        <Description>Body tube, 38mm, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38-600</PartNumber>
        <Description>Body tube, 38mm, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.520</InsideDiameter>
        <OutsideDiameter Unit="in">1.645</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- FWCF-54 CF tubing size and weight not given, dimensions here assumed same as FT54
           standard wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54-120</PartNumber>
        <Description>Body tube, 54mm, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54-240</PartNumber>
        <Description>Body tube, 54mm, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54-360</PartNumber>
        <Description>Body tube, 54mm, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54-480</PartNumber>
        <Description>Body tube, 54mm, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54-600</PartNumber>
        <Description>Body tube, 54mm, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.152</InsideDiameter>
        <OutsideDiameter Unit="in">2.277</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- FWCF-3 CF tubing size and weight not given, dimensions here assumed same as FT30
           standard wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3-120</PartNumber>
        <Description>Body tube, 3.0in, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3-240</PartNumber>
        <Description>Body tube, 3.0in, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3-360</PartNumber>
        <Description>Body tube, 3.0in, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3-480</PartNumber>
        <Description>Body tube, 3.0in, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3-600</PartNumber>
        <Description>Body tube, 3.0in, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.000</InsideDiameter>
        <OutsideDiameter Unit="in">3.125</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- FWCF-4 CF tubing size and weight not given, dimensions here assumed same as FT40
           standard wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4-120</PartNumber>
        <Description>Body tube, 4.0in, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4-240</PartNumber>
        <Description>Body tube, 4.0in, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4-360</PartNumber>
        <Description>Body tube, 4.0in, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4-480</PartNumber>
        <Description>Body tube, 4.0in, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4-600</PartNumber>
        <Description>Body tube, 4.0in, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.900</InsideDiameter>
        <OutsideDiameter Unit="in">4.024</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
     <!-- FWCF-6 CF tubing size and weight not given, dimensions here assumed same as FT60
           standard wall
      -->
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6-120</PartNumber>
        <Description>Body tube, 6.0in, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6-240</PartNumber>
        <Description>Body tube, 6.0in, carbon fiber, black, 24.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">24.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6-360</PartNumber>
        <Description>Body tube, 6.0in, carbon fiber, black, 36.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">36.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6-480</PartNumber>
        <Description>Body tube, 6.0in, carbon fiber, black, 48.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">48.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6-600</PartNumber>
        <Description>Body tube, 6.0in, carbon fiber, black, 60.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">6.000</InsideDiameter>
        <OutsideDiameter Unit="in">6.170</OutsideDiameter>
        <Length Unit="in">60.0</Length>
      </BodyTube>
      <!-- ========================== -->
      <!-- Carbon Fiber Tube Couplers -->
      <!-- ========================== -->
      <!-- FWCF-29C coupler size and weight not given, dimensions here assumed same as FC29 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29C-040</PartNumber>
        <Description>Tube coupler, 29mm, carbon fiber, black, 4.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.000</InsideDiameter>
        <OutsideDiameter Unit="in">1.143</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-29C-060</PartNumber>
        <Description>Tube coupler, 29mm, carbon fiber, black, 6.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.000</InsideDiameter>
        <OutsideDiameter Unit="in">1.143</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <!-- FWCF-38C coupler size and weight not given, dimensions here assumed same as FC38 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38C-040</PartNumber>
        <Description>Tube coupler, 38mm, carbon fiber, black, 4.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-38C-060</PartNumber>
        <Description>Tube coupler, 38mm, carbon fiber, black, 6.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <!-- FWCF-54C coupler size and weight not given, dimensions here assumed same as FC54 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54C-060</PartNumber>
        <Description>Tube coupler, 54mm, carbon fiber, black, 6.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-54C-080</PartNumber>
        <Description>Tube coupler, 54mm, carbon fiber, black, 8.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">1.370</InsideDiameter>
        <OutsideDiameter Unit="in">1.518</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </TubeCoupler>
      <!-- FWCF-3C coupler size and weight not given, dimensions here assumed same as FC30 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3C-060</PartNumber>
        <Description>Tube coupler, 3in, carbon fiber, black, 6.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.875</InsideDiameter>
        <OutsideDiameter Unit="in">2.998</OutsideDiameter>
        <Length Unit="in">6.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-3C-090</PartNumber>
        <Description>Tube coupler, 3in, carbon fiber, black, 9.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">2.875</InsideDiameter>
        <OutsideDiameter Unit="in">2.998</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </TubeCoupler>
      <!-- FWCF-4C coupler size and weight not given, dimensions here assumed same as FC39 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4C-090</PartNumber>
        <Description>Tube coupler, 4in, carbon fiber, black, 9.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.755</InsideDiameter>
        <OutsideDiameter Unit="in">3.899</OutsideDiameter>
        <Length Unit="in">9.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-4C-120</PartNumber>
        <Description>Tube coupler, 4in, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">3.755</InsideDiameter>
        <OutsideDiameter Unit="in">3.899</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>
      <!-- FWCF-6C coupler size and weight not given, dimensions here assumed same as FC60 -->
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6C-120</PartNumber>
        <Description>Tube coupler, 6in, carbon fiber, black, 12.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">5.775</InsideDiameter>
        <OutsideDiameter Unit="in">5.998</OutsideDiameter>
        <Length Unit="in">12.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>Madcow</Manufacturer>
        <PartNumber>FWCF-6C-180</PartNumber>
        <Description>Tube coupler, 6in, carbon fiber, black, 18.0 in.</Description>
        <Material Type="BULK">Carbon fiber epoxy composite, Madcow FWCF, bulk</Material>
        <InsideDiameter Unit="in">5.775</InsideDiameter>
        <OutsideDiameter Unit="in">5.998</OutsideDiameter>
        <Length Unit="in">18.0</Length>
      </TubeCoupler>
 

      <!-- ===================== -->
      <!-- Fiberglass Nose Cones -->
      <!-- ===================== -->

      <!-- For many of these parts the shoulder length is set zero here; you must put in the
           nose cone coupler as a separate part since the position of the coupler in the
           nose cone is not fixed by the manufacturer.

           About mass of metal tip nose cones: The density of aluminum at 2.7 g/cm3 is a little more
           than that of fiberglass (1.8-2.2 g/cm3). Metal tip nose cones will weight slighly more
           than composite tip versions and have their CG slightly further forward, but the delta is
           not that large and OpenRocket has no good way to model this in a single component.  For
           highest accuracy in mass, CG and moments of inertia, you can add a small mass object at
           the nose cone tip to make up the difference.

           Overall the state of the nose cone listings on the Madcow site as of Oct 2020 is a bit messy.
      -->

      <!-- No nose cones listed for 1.1" / 29mm tube -->

      <!-- =============================== -->
      <!-- 1.6" 38mm fiberglass nose cones -->
      <!-- =============================== -->

      <!-- 1.6" thin wall composite tip shapes
           Mfr data (*** not clear whether weight includes the NC coupler but looks like it does ***)
              4" FC38 coupler weight = 1.34 oz
              3:1 ogive: 5" exposed length, 1.61" diameter, 2.1 oz
              4:1 ogive: 6 1/2" exposed length, 1.61" diameter, 2.6 oz
              5:1 ogive: 8" exposed length, 1.61" diameter, 2.9 oz
              5:1 conical: 9 1/8" exposed length, 1.61" diameter, 3.6 oz
              5.5:1 von Karman: 9 3/8" exposed length, 1.61" diameter, 3.5 oz
      -->
        <!-- FWNC38T-K net wt 0.76 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC38T-K</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 3:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.518</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">5.0</Length>
            <Thickness Unit="in">0.045</Thickness>
        </NoseCone>
        <!-- FWNC38T-Y net wt 1.26 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC38T-Y</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 4:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.518</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">6.5</Length>
            <Thickness Unit="in">0.0585</Thickness>
        </NoseCone>
        <!-- FWNC38T-YY net wt 1.56 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC38T-YY</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.518</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">8.0</Length>
            <Thickness Unit="in">0.0590</Thickness>
        </NoseCone>
        <!-- FWNC38T-C net wt 2.26 oz
             SOURCE ERROR: FWNC38T-C Have to use anomalous .1085 thickness to reach specified weight.
                            Quoted weight not for thinwall?
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC38T-C</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.518</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">9.125</Length>
            <Thickness Unit="in">0.1085</Thickness>
        </NoseCone>
        <!-- FWNC38T-VK net wt 2.16 oz
             SOURCE ERROR?: Have to use anomalous .072 thickness to reach specified weight for FWNC38T-VK.
                            Quoted weight not for thinwall?
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC38T-VK</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5.5:1 von Karman, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.518</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">9.375</Length>
            <Thickness Unit="in">0.0725</Thickness>
        </NoseCone>

        <!-- There are no 1.6" fiberglass standard wall composite tip shapes -->
        <!-- There are no 1.6" fiberglass standard wall aluminum tip shapes -->

        <!-- =============================== -->
        <!-- 2.2" 54mm fiberglass nose cones -->
        <!-- =============================== -->

        <!-- 2.2" 54mm fiberglass thin wall composite tip shapes
             *** No longer exist on website as of Oct 2020 - erroneously removed? ***
             *** No mfr data given except fineness ratio ***
             *** Get actual weights and set thickness to put weight to actual ***
             For now have set thickness equal to .040 wall, mass should be reasonably close
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54T-YY</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.230</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">11.0</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54T-C</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">2.230</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">11.0</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54T-VK</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5.5:1 von Karman, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">2.230</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">12.1</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>


        <!-- 2.2" 54mm fiberglass standard wall composite tip shapes
             *** No mfr data given except fineness ratio ***
             *** Get actual weights and set thickness to put weight to actual ***
             For now have set thickness to tube wall thickness, should be reasonable
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54-YY</PartNumber>
            <Description>Nose cone, fiberglass, std wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.277</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">11.0</Length>
            <Thickness Unit="in">0.063</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54-C</PartNumber>
            <Description>Nose cone, fiberglass, std wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">2.277</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">11.0</Length>
            <Thickness Unit="in">0.063</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC54-VK</PartNumber>
            <Description>Nose cone, fiberglass, std wall, 5.5:1 von Karman, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">2.277</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.150</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">12.1</Length>
            <Thickness Unit="in">0.063</Thickness>
        </NoseCone>
      
        <!-- There are no 2.2" 54mm fiberglass standard wall aluminum tip shapes -->

        <!-- =============================== -->
        <!-- 2.6" fiberglass nose cones      -->
        <!-- =============================== -->

        <!-- 2.6" fiberglass thin wall nose cone shapes
             Mfr Data:
                6" FC26 coupler weighs 2.60 oz
                5:1 ogive:  13 1/2" exposed length, 7 oz with coupler => 4.40 oz
                *** no data for FWNC26 3:1 ogive or 5:1 conical ***
        -->
        <!-- SOURCE ERROR:  Madcow website page says "Fiberglass 2.6" (38mm) Filament...".
             The "38mm" is wrong for the 2.6" size -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC26T-K</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 3:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.640</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.558</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">7.92</Length>
            <Thickness Unit="in">0.041</Thickness>
        </NoseCone>
        <!-- FWNC26T-YY quoted as 7 oz with coupler.  Removing 2.60 oz weight of a 6 in FC26 coupler,
             the nose cone should weigh 4.4 oz by itself -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC26T-YY</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.640</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.558</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.50</Length>
            <Thickness Unit="in">0.0583</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC26T-C</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">2.640</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.558</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.20</Length>
            <Thickness Unit="in">0.041</Thickness>
        </NoseCone>

        <!-- There are no 2.6" fiberglass standard wall composite tip nose cone shapes -->
        <!-- There are no 2.6" fiberglass standard wall aluminum tip nose cone shapes -->
        

        <!-- =============================== -->
        <!-- 3" fiberglass nose cones        -->
        <!-- =============================== -->

        <!-- SOURCE ERROR: Madcow website gives same weight for FWNC30-VK and FWNC30T-VK, and
             4.7 oz for FWNC30-Y and FWNC30T-Y.  This is definitely incorrect. -->

        <!-- 3" 75mm fiberglass thin wall composite tip nose cone shapes
             Mfr Data:
                 FC30 6" coupler is 3.51 oz
                 4:1 ogive: 13 3/8" exposed length, 13.4 oz (***weight wrong for thin wall***)
                 5.5:1 VK:  17 1/4" exposed length, 17.5 oz (***weight wrong for thin wall***)
             For now I have set NC thickness to tube wall thickness to get close
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30T-Y</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 4:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.098</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.375</Length>
            <Thickness Unit="in">0.050</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30T-YY</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.098</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">15.49</Length>
            <Thickness Unit="in">0.050</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30T-C</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">3.098</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.375</Length>
            <Thickness Unit="in">0.050</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30T-VK</PartNumber>
            <Description>Nose cone, fiberglass, thin wall, 5.5:1 von Karman, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">3.098</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">17.25</Length>
            <Thickness Unit="in">0.050</Thickness>
        </NoseCone>
        
        <!-- 3" 75mm fiberglass standard wall composite tip nose cone shapes
             Mfr Data:
                 4:1 ogive: 13 3/8" exposed length, 13.4 oz (see note above about erroneous weights)
                 5.5:1 VK:  17 1/4" exposed length, 17.5 oz (see note above about erroneous weights)
                 *** I am suspicious of these weights due to the thickness required to reach them ***
        -->
        <!-- FWNC30-Y net wt 9.89 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.125</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.375</Length>
            <Thickness Unit="in">0.114</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.125</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">15.49</Length>
            <Thickness Unit="in">0.114</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30-C</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 conical, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">3.125</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">13.375</Length>
            <Thickness Unit="in">0.114</Thickness>
        </NoseCone>
        <!-- FWNC30-VK net wt 13.99 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC30-VK</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5.5:1 von Karman, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">3.125</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">17.25</Length>
            <Thickness Unit="in">0.130</Thickness>
        </NoseCone>

        <!-- There are no 3" 75mm fiberglass standard wall aluminum tip nose cone shapes -->

        <!-- =============================== -->
        <!-- 4" fiberglass nose cones        -->
        <!-- =============================== -->

        <!-- 4" fiberglass standard wall composite tip nose cone shapes
             *** no mfr data except fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39-K</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 3:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">12.072</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>
        <!-- SOURCE ERROR: Madcow 2017 website lists this as "FWNC38-Y" when you click on the
             4:1 Ogive button.  It should be FWNC39-Y. -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, composite tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">16.096</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>

        <!-- 4" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data except fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39M-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">16.096</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39M-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">20.12</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39M-C</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 conical, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">20.12</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC39M-VK</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5.5:1 von Karman, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">22.17</Length>
            <Thickness Unit="in">0.061</Thickness>
        </NoseCone>

        <!-- 4.5" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data for 4:1 ogive except fineness ratio ***
             *** no mfr data at all for Arcas FWNC45M-ARCAS ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC45M-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.500</OutsideDiameter>
            <ShoulderDiameter Unit="in">4.371</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">18.0</Length>
            <Thickness Unit="in">0.065</Thickness>
        </NoseCone>
        <!-- FWNC45M-ARCAS.  According to the 1960 ONR development report on the Arcas,
             the nose cone was a 4 caliber secant ogive. This would be identical to the
             FWNC45M-Y 4:1 ogive.  A NASA drawing of the Mod 0 Arcas shows the NC being 18.122
             inches long, with internal diameter given at three stations, but no shape
             type is mentioned.  See
             https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19700020646.pdf
             Station   Diam (Internal!)
             6.750     2.116"
             10.750    3.075"
             18.122    4.133"
             The Arcas nose cone is plastic and of non-uniform thickness.
             The Mod 1 Arcas had a 21.2" long nose cone.  See
             http://www.dtic.mil/dtic/tr/fulltext/u2/437681.pdf

             Some scale models of the Arcas have used a von Karman shape, but thus far it's not
             clear that this would be correct.  Nonetheless since this is clearly a different
             PN for Madcow I've used the VK shape with the correct 1:1 scale length.
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC45M-ARCAS</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, ARCAS, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">4.500</OutsideDiameter>
            <ShoulderDiameter Unit="in">4.371</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">18.122</Length>
            <Thickness Unit="in">0.065</Thickness>
        </NoseCone>

        <!-- 5" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data except fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC50M-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.150</OutsideDiameter>
            <ShoulderDiameter Unit="in">4.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">20.0</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC50M-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.150</OutsideDiameter>
            <ShoulderDiameter Unit="in">4.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">25.0</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC50M-VK</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5.5:1 von Karman, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.150</OutsideDiameter>
            <ShoulderDiameter Unit="in">4.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">27.5</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>

        <!-- 5.5" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data excepet fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC55M-K</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 3:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.525</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.372</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">16.5</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC55M-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">5.525</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.372</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">22.0</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC55M-C</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 conical, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">5.525</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.372</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">27.5</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC55M-VK</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5.5:1 von Karman, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">5.525</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.372</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">30.25</Length>
            <Thickness Unit="in">0.076</Thickness>
        </NoseCone>

        <!-- 6" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data except fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC60M-Y</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 4:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">6.170</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">24.0</Length>
            <Thickness Unit="in">0.086</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC60M-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">6.170</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">30.0</Length>
            <Thickness Unit="in">0.086</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC60M-C</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 conical, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <OutsideDiameter Unit="in">6.170</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">30.0</Length>
            <Thickness Unit="in">0.086</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC60M-C</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5.5:1 von Karman, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>HAACK</Shape>
            <OutsideDiameter Unit="in">6.170</OutsideDiameter>
            <ShoulderDiameter Unit="in">5.997</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">33.0</Length>
            <Thickness Unit="in">0.086</Thickness>
        </NoseCone>

        <!-- 7.5" fiberglass standard wall metal tip nose cone shapes
             *** no mfr data except fineness ratios ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC75M-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">7.708</OutsideDiameter>
            <ShoulderDiameter Unit="in">7.515</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">37.5</Length>
            <Thickness Unit="in">0.092</Thickness>
        </NoseCone>

        <!-- 8" fiberglass standard wall metal tip nose cone shapes
             Mfr data:
               3:1 ogive: weight 4 lb 6 oz (72 oz), plus 25 oz for 11" coupler section
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC80M-K</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 3:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">8.005</OutsideDiameter>
            <ShoulderDiameter Unit="in">7.812</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">24.0</Length>
            <Thickness Unit="in">0.175</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FWNC80M-YY</PartNumber>
            <Description>Nose cone, fiberglass, heavy wall, 5:1 ogive, metal tip</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">8.005</OutsideDiameter>
            <ShoulderDiameter Unit="in">7.812</ShoulderDiameter>
            <ShoulderLength Unit="in">0.0</ShoulderLength>
            <Length Unit="in">40.0</Length>
            <Thickness Unit="in">0.175</Thickness>
        </NoseCone>

        <!-- ================== -->
        <!-- Plastic nose cones -->
        <!-- ================== -->

        <!-- Plastic Nose Cones for 1.6" cardboard and thin wall fiberglass airframes
             *** mass and shoulder dimensions not given, need actual weights ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC16K</PartNumber>
            <Description>Nose cone, plastic, blow molded, black, 3:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.517</ShoulderDiameter>
            <ShoulderLength Unit="in">2.0</ShoulderLength>
            <Length Unit="in">4.8</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC16Y</PartNumber>
            <Description>Nose cone, plastic, blow molded, black, 4:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.517</ShoulderDiameter>
            <ShoulderLength Unit="in">2.0</ShoulderLength>
            <Length Unit="in">6.4</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        
        <!-- Plastic Nose Cones for 2.6" cardboard and thin wall fiberglass airframes
             *** shoulder dimensions not given ***
        -->
        <!-- PNC26K-B mass given as 4.4 oz, len as 8.25" -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC26K-B</PartNumber>
            <Description>Nose cone, plastic, blow molded, black, 3:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">1.600</OutsideDiameter>
            <ShoulderDiameter Unit="in">1.517</ShoulderDiameter>
            <ShoulderLength Unit="in">2.0</ShoulderLength>
            <Length Unit="in">8.25</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        <!-- PNC26K-W mass given as 4.4 oz, len as 8.25" -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC26K-W</PartNumber>
            <Description>Nose cone, plastic, blow molded, white, 3:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">2.638</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.557</ShoulderDiameter>
            <ShoulderLength Unit="in">2.0</ShoulderLength>
            <Length Unit="in">8.25</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>

        <!-- Plastic Nose Cones for 3" cardboard and thin wall fiberglass airframes
             *** shoulder dimensions not given ***
        -->
        <!-- PNC30Y length given as 12.5", weight 5 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC30Y</PartNumber>
            <Description>Nose cone, plastic, blow molded, white, 4:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">3.098</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.997</ShoulderDiameter>
            <ShoulderLength Unit="in">2.5</ShoulderLength>
            <Length Unit="in">12.5</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        
        <!-- Plastic Nose Cones for 4" cardboard airframes -->
        
        <!-- PNC39AJ is 2.5:1 ogive, length given as 9.5", weight 7 oz -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC39AJ</PartNumber>
            <Description>Nose cone, plastic, blow molded, white, 2.5:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.000</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">3.0</ShoulderLength>
            <Length Unit="in">9.5</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>
        <!-- PNC39Y is 4:1 ogive
             *** weight and shoulder dimensions not given ***
        -->
        <NoseCone>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>PNC39Y</PartNumber>
            <Description>Nose cone, plastic, blow molded, white, 4:1 ogive</Description>
            <Material Type="BULK">Polypropylene, bulk</Material>
            <Shape>OGIVE</Shape>
            <OutsideDiameter Unit="in">4.000</OutsideDiameter>
            <ShoulderDiameter Unit="in">3.897</ShoulderDiameter>
            <ShoulderLength Unit="in">3.0</ShoulderLength>
            <Length Unit="in">16.0</Length>
            <Thickness Unit="in">0.040</Thickness>
        </NoseCone>

        <!-- ============================= -->
        <!-- Balsa nacelles and tail cones -->
        <!-- ============================= -->
        <!--
             SOURCE ERROR: no data of any kind for BNC55RJ and BNC70RJH
        -->

        <!-- 2.6" cardboard airframe to 29mm cardboard MMT tail cone
             Website gives exposed length 2.5", diam at base 2.0".  Photo shows conical shape.
             *** weight and shoulder length unknown ***

             Mass override used b/c OpenRocket can't handle this kind of part
             Vol solid cone frustum 10.64 in3, vol of drilled out cylinder 29mm 2.91 in3
             Net volume 7.73 in3 = .0001267 m3
             with 8 lb balsa density 128.1 kg/m3, expected mass is 16 gm = 0.56 oz
        -->
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>BTC2629 [R]</PartNumber>
            <Description>Tail cone, balsa, 2.6in to 29mm, decreasing</Description>
            <Material Type="BULK">Balsa, bulk, 8 lb/ft3</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <Mass unit="oz">0.56</Mass>
            <ForeOutsideDiameter Unit="in">2.640</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.548</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">1.25</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.218</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">2.5</Length>
        </Transition>

        <!-- Balsa 2.6" cardboard airframe to 38mm cardboard MMT
             Website gives 2.5" exposed length, 2.0" base diameter.
             *** weight and shoulder len not given ***
             Mass override used as OpenRocket can't do this geometry.
             Vol solid cone frustum 10.64 in3, vol of drilled out cylinder 38mm 5.1 in3
             Net volume 5.54 in3 = .0000908 m3
             with 8 lb balsa density 128.1 kg/m3, expected mass is 11.6 gm = 0.41 oz
        -->
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>BTC2638 [R]</PartNumber>
            <Description>Tail cone, balsa, 2.6in to 38mm, decreasing</Description>
            <Material Type="BULK">Balsa, bulk, 8 lb/ft3</Material>
            <Shape>CONICAL</Shape>
            <Filled>true</Filled>
            <Mass unit="oz">0.56</Mass>
            <ForeOutsideDiameter Unit="in">2.640</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.548</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">1.25</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.638</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">2.5</Length>
        </Transition>

        <!-- Balsa ramjet nose cones
             SOURCE ERROR: no data of any kind for BNC55RJ and BNC70RJH
        -->

        <!-- ========================= -->
        <!-- G10 fiberglass tail cones -->
        <!-- ========================= -->

        <!-- FTC26 2.6in thin wall G12 airframe to 38mm std wall G12 MMT tail cone
             Dimensions obtained from RockSim file for 2.6" FG Black Brant II on Madcow website:
             Len: 2.5"
             Fore diam: 2.64"
             Aft diam: 2.0" - (leaves clearance for retainer on MMT?)
             shoulder diam: 2.558"
             shoulder len: 0.5"
             wall: 0.125"
             OR computed mass with 1.80 g/cm3 G10: 2.76 oz
        -->
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FTC26 [R]</PartNumber>
            <Description>Tail cone, G10 fiberglass, 2.6in to 38mm, decreasing</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">2.640</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.548</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.5</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.0</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">1.638</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">2.5</Length>
            <Thickness Unit="in">0.125</Thickness>
        </Transition>

        <!-- FTC39 4in std wall G12 airframe to 38mm std wall G12 MMT tail cone
             Used in K-156 4" Black Brant.  This is hard because there is no RockSim file for this kit.
             Known and estimated dimensions:
               Len: 4.0"  (known from other BB kit scale, should be 1.0x body diameter)
               Fore diam: 4.024 (known from tube diameter)
               Shoulder diam:  3.898 (known from tube ID + clearance)
               Aft diam:  2.75 (good estimate)
               Shoulder len:   0.75 (good estimate based on scaling the short shoulder from FTC26)
               Wall:  0.125"  (fuzzy estimate)
        -->
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>FTC39 [R]</PartNumber>
            <Description>Tail cone, G10 fiberglass, 4in to 54mm, decreasing</Description>
            <Material Type="BULK">Fiberglass, G10, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">4.024</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">3.898</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">0.75</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.75</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.279</AftShoulderDiameter>
            <AftShoulderLength Unit="in">0.0</AftShoulderLength>
            <Length Unit="in">4.0</Length>
            <Thickness Unit="in">0.125</Thickness>
        </Transition>


        <!-- ========================= -->
        <!-- Centering Rings - Plywood -->
        <!-- ========================= -->

        <!-- 29mm MMT to 54mm cardboard airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR2229A-CK4</PartNumber>
          <Description>Centering ring, 1/8 ply, aft (no slot), 29mm MMT to 54mm cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.218</InsideDiameter>
          <OutsideDiameter Unit="in">2.137</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR2229F-CK3</PartNumber>
          <Description>Centering ring, 1/8 ply, fwd (slotted), 29mm MMT to 54mm cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.218</InsideDiameter>
          <OutsideDiameter Unit="in">2.137</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <!-- 29mm MMT to 2.6in cardboard airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR2629A-CJ3</PartNumber>
          <Description>Centering ring, 1/8 ply, aft (no slot), 29mm MMT to 2.6in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.218</InsideDiameter>
          <OutsideDiameter Unit="in">2.555</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR2629F-CJ4</PartNumber>
          <Description>Centering ring, 1/8 ply, fwd (slotted), 29mm MMT to 2.6in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.218</InsideDiameter>
          <OutsideDiameter Unit="in">2.555</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <!-- 38mm cardboard MMT to 3in cardboard airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3038A-CG4</PartNumber>
          <Description>Centering ring, 1/4 ply, aft (no hole), 38mm MMT to 3in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.638</InsideDiameter>
          <OutsideDiameter Unit="in">2.997</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3038F-CG5</PartNumber>
          <Description>Centering ring, 1/4 ply, fwd (drilled), 38mm MMT to 3in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.638</InsideDiameter>
          <OutsideDiameter Unit="in">2.997</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>
        <!-- 38mm cardboard MMT to 4 in cardboard airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3938A-AH1</PartNumber>
          <Description>Centering ring, 1/4 ply, aft (no hole), 38mm MMT to 4in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.638</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3938F-AH2</PartNumber>
          <Description>Centering ring, 1/4 ply, fwd (drilled), 38mm MMT to 4in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">1.638</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>
        <!-- 54mm cardboard MMT to 4 in cardboard airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3954A-DM3</PartNumber>
          <Description>Centering ring, 1/4 ply, aft (no hole), 54mm MMT to 4in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">2.248</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>CR3954F-DM4</PartNumber>
          <Description>Centering ring, 1/4 ply, fwd (drilled), 54mm MMT to 4in cardboard</Description>
          <Material Type="BULK">Plywood, light, bulk</Material>
          <InsideDiameter Unit="in">2.248</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.250</Length>
        </CenteringRing>

        <!-- ================================ -->
        <!-- Centering Rings - G10 Fiberglass -->
        <!-- ================================ -->
        <!-- *** thicknesses not documented (assumed .092 - 0.125), check some of my kits *** -->

        <!-- 29mm FG MMT to 54mm FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5429-GDL5</PartNumber>
          <Description>Centering ring, G10 FG, 29mm FG MMT to 54mm FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">1.258</InsideDiameter>
          <OutsideDiameter Unit="in">2.149</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 38mm FG MMT to 54mm FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5438-GCF5</PartNumber>
          <Description>Centering ring, G10 FG, 38mm FG MMT to 54mm FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">1.648</InsideDiameter>
          <OutsideDiameter Unit="in">2.149</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>


        <!-- 29mm FG MMT to 2.6" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR2629A-GDL3</PartNumber>
          <Description>Centering ring, G10 FG, aft (no hole), 29mm FG MMT to 2.6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.1.258</InsideDiameter>
          <OutsideDiameter Unit="in">2.557</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR2629F-GDL4</PartNumber>
          <Description>Centering ring, G10 FG, fwd (drilled), 29mm FG MMT to 2.6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.1.258</InsideDiameter>
          <OutsideDiameter Unit="in">2.557</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 38mm FG MMT to 2.6" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR2638A-GDL1</PartNumber>
          <Description>Centering ring, G10 FG, aft (no hole), 38mm FG MMT to 2.6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.1.648</InsideDiameter>
          <OutsideDiameter Unit="in">2.557</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR2638F-DL2</PartNumber>
          <Description>Centering ring, G10 FG, fwd (drilled), 38mm FG MMT to 2.6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.1.648</InsideDiameter>
          <OutsideDiameter Unit="in">2.557</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 2.6" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR2654-GCL5</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 2.6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.2.280</InsideDiameter>
          <OutsideDiameter Unit="in">2.557</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 38mm FG MMT to 3" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR3038-GDH3</PartNumber>
          <Description>Centering ring, G10 FG, 38mm FG MMT to 3in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.1.648</InsideDiameter>
          <OutsideDiameter Unit="in">2.997</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 3" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR3054-GDH4</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 3in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.2.280</InsideDiameter>
          <OutsideDiameter Unit="in">2.997</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 4" FG coupler tube -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR39C54-RBD3</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 4in FG coupler</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.2.280</InsideDiameter>
          <OutsideDiameter Unit="in">3.752</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 4" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR3954-GAJ1</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 4in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.2.280</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 75mm FG MMT to 4" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR3975-GAJ2</PartNumber>
          <Description>Centering ring, G10 FG, 75mm FG MMT to 4in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.3.128</InsideDiameter>
          <OutsideDiameter Unit="in">3.897</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 4.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR4554</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 4.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">2.280</InsideDiameter>
          <OutsideDiameter Unit="in">4.371</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>
        
        <!-- 75mm FG MMT to 4.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR4575</PartNumber>
          <Description>Centering ring, G10 FG, 75mm FG MMT to 4.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.3.128</InsideDiameter>
          <OutsideDiameter Unit="in">4.371</OutsideDiameter>
          <Length Unit="in">0.092</Length>
        </CenteringRing>

        <!-- 75mm FG MMT to 5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5075-DJ1</PartNumber>
          <Description>Centering ring, G10 FG, 75mm FG MMT to 5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">.3.128</InsideDiameter>
          <OutsideDiameter Unit="in">4.997</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5098-RDJ3</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.027</InsideDiameter>
          <OutsideDiameter Unit="in">4.997</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <!-- 98mm FG MMT to 5" FG airframe, black -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5098-BLK-RDJ3</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 5in FG, black</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.027</InsideDiameter>
          <OutsideDiameter Unit="in">4.997</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 38mm FG MMT to 5.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5538-RDE5</PartNumber>
          <Description>Centering ring, G10 FG, 38mm FG MMT to 5.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">1.648</InsideDiameter>
          <OutsideDiameter Unit="in">5.372</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 5.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5554-GBL1</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 5.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">2.280</InsideDiameter>
          <OutsideDiameter Unit="in">5.372</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 75mm FG MMT to 5.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5575</PartNumber>
          <Description>Centering ring, G10 FG, 75mm FG MMT to 5.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">3.128</InsideDiameter>
          <OutsideDiameter Unit="in">5.372</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 5.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR5598-GBL3</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 5.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.027</InsideDiameter>
          <OutsideDiameter Unit="in">5.372</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 54mm FG MMT to 6" FG airframe
             OD is specified as 5.998
        -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR6054</PartNumber>
          <Description>Centering ring, G10 FG, 54mm FG MMT to 6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">2.280</InsideDiameter>
          <OutsideDiameter Unit="in">5.998</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 75mm FG MMT to 6" FG airframe
             OD is specified as 5.998
        -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR6075-RCK2</PartNumber>
          <Description>Centering ring, G10 FG, 75mm FG MMT to 6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">3.128</InsideDiameter>
          <OutsideDiameter Unit="in">5.998</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 6" FG airframe
             OD is specified as 5.998
        -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR6098-CK1</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 6in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.027</InsideDiameter>
          <OutsideDiameter Unit="in">5.998</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 7.5" FG airframe -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR7598</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 7.5in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.026</InsideDiameter>
          <OutsideDiameter Unit="in">7.516</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 75mm FG MMT to 8" FG airframe, natural -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR8075-BK1</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 8in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.026</InsideDiameter>
          <OutsideDiameter Unit="in">7.813</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>
        <!-- 75mm FG MMT to 8" FG airframe, black -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR8075-BLK-BK1</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 8in FG, black</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.026</InsideDiameter>
          <OutsideDiameter Unit="in">7.813</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 8" FG airframe, natural -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR8098-GBK2</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 8in FG</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.026</InsideDiameter>
          <OutsideDiameter Unit="in">7.813</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- 98mm FG MMT to 8" FG airframe, black -->
        <CenteringRing>
          <Manufacturer>Madcow</Manufacturer>
          <PartNumber>FCR8098-BLK-BK2</PartNumber>
          <Description>Centering ring, G10 FG, 98mm FG MMT to 8in FG, black</Description>
          <Material Type="BULK">Fiberglass, G10, bulk</Material>
          <InsideDiameter Unit="in">4.026</InsideDiameter>
          <OutsideDiameter Unit="in">7.813</OutsideDiameter>
          <Length Unit="in">0.125</Length>
        </CenteringRing>

        <!-- ====================== -->
        <!-- Fiberglass Transitions -->
        <!-- ====================== -->

        <!-- 54mm to 4" G12 transition
             SOURCE ERROR: photogrammetry shows the TRANS-4-54 pic is not 4" to 54mm, it's a much better
             fit to 4" to 3".

             But the RW DoubleShot uses this, so I scaled the exposed length
             from the picture (which may just be CGI) and estimated the other dimensions.
             Thickness is an educated guess, and the weight is unknown.

             exposed len: 7.9" (measurement of DoubleShot photo)
             fwd diam: 2.277  (known from tube size)
             fwd shoulder diam: 2.145 (7 mil clearance on 54mm ID)
             Aft diam: 4.024 (known from tube size)
             Aft shoulder diam: 3.893 (7 mil clearance on 4in tube ID)
             fwd shoulder len: 2.25" (assumed equal to fwd diameter)
             aft shoulder len: 4.0" (assumed equal to aft diameter)
        -->
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>TRANS-4-54</PartNumber>
            <Description>Transition, G12 FG, 54mm to 4in, increasing</Description>
            <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">2.277</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">2.145</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">2.25</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">4.024</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">3.893</AftShoulderDiameter>
            <AftShoulderLength Unit="in">4.0</AftShoulderLength>
            <Length Unit="in">7.9</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>
        <Transition>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>TRANS-4-54 [R]</PartNumber>
            <Description>Transition, G12 FG, 54mm to 4in, decreasing</Description>
            <Material Type="BULK">Fiberglass, G12, filament wound tube, bulk</Material>
            <Shape>CONICAL</Shape>
            <ForeOutsideDiameter Unit="in">4.024</ForeOutsideDiameter>
            <ForeShoulderDiameter Unit="in">3.893</ForeShoulderDiameter>
            <ForeShoulderLength Unit="in">4.0</ForeShoulderLength>
            <AftOutsideDiameter Unit="in">2.277</AftOutsideDiameter>
            <AftShoulderDiameter Unit="in">2.145</AftShoulderDiameter>
            <AftShoulderLength Unit="in">2.25</AftShoulderLength>
            <Length Unit="in">7.9</Length>
            <Thickness Unit="in">0.100</Thickness>
        </Transition>

        
        <!-- =========== -->
        <!-- Launch Lugs -->
        <!-- =========== -->
        
        <!-- 1/4" lug ***ID/OD estimated using 9/32 ID and giving it .021 wall -->
        <LaunchLug>
          <Manufacturer>Maadcow</Manufacturer>
          <PartNumber>P-182</PartNumber>
          <Description>Launch lug, paper, 1/4 x 1"</Description>
          <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
          <InsideDiameter Unit="in">0.281</InsideDiameter>
          <OutsideDiameter Unit="in">0.323</OutsideDiameter>
          <Length Unit="in">1.0</Length>
        </LaunchLug>

        <!-- ========== -->
        <!-- Parachutes -->
        <!-- ========== -->
        <!-- Madcow lists various brands of parachutes including Fruity Chutes and Sky Angle, as
             well as some generic nylon chutes.  These look essentially identical to Top Flight
             standard parachutes, though I am not sure if they are actually the supplier to Madcow.
             For now I have duplicated the Top Flight entries here.
        -->
        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-177</PartNumber>
            <Description>Parachute, 15 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">15.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">15.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- 18" nylon chute weight given as 0.55 oz -->
        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-187</PartNumber>
            <Description>Parachute, 18 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>
        
        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-186</PartNumber>
            <Description>Parachute, 24 in., nylon, 6 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-185</PartNumber>
            <Description>Parachute, 30 in., nylon, 8 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">30.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">30.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <!-- 36" nylon chute weight given as 2.3 oz -->
        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-184</PartNumber>
            <Description>Parachute, 36 in., nylon, 8 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">36.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

        <Parachute>
            <Manufacturer>Madcow</Manufacturer>
            <PartNumber>P-178</PartNumber>
            <Description>Parachute, 50 in., nylon, 8 lines</Description>
            <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
            <Diameter Unit="in">50.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">50.0</LineLength>
            <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
        </Parachute>

    </Components>

</OpenRocketComponent>
