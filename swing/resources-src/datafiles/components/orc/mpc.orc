<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
MPC (1969-1973) parts file for OpenRocket

by Dave Cook  NAR 21953  caveduck17@gmail.com 2014-2017

This file contains everything from the 1969 Catalog 1, 1970 Catalog 2, and the 1973 Minirocs catalog.
The original vendor data is very incomplete; various minor dimensions have been estimated, usually with
reasonable justification.

MPC did not give the weight of anything, so I have used standard densities for paper tubes, balsa, and
styrene, along with an estimate for thickness of the plastic parts.

I recovered data about centering rings, stage couplers, and a couple of nose cones by reading kit instructions
and kit descriptions from the catalogs, even though the parts were never listed, and the kit instructions
don't give stock numbers (and usually not sizes) for any of the parts in the kit.

There are two plastic nose cone assemblies known to exist that are not listed because we have neither the
lengths nor the weights:

1) The plastic space capsule used in the Moon Go.  This is a 3-piece unit; see Moon Go instructions on
   plans.rocketshoppe.org 
2) The 2 piece plastic Martian Patrol nose cone, which was a double taper cone.  Approximate dimensions could
   be obtained by photogrammetry of the Martian Patrol instructions drawing.
-->
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, spiral kraft glassine, bulk</Name>
            <Density>798.85</Density>
            <Type>BULK</Type>
        </Material>
        
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, bulk</Name>
            <Density>657.0</Density>
            <Type>BULK</Type>
        </Material>
        
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 5lb/ft3</Name>
          <Density>80.0</Density>
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
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 10lb/ft3</Name>
          <Density>160.0</Density>
          <Type>BULK</Type>
        </Material>
        
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polystyrene, cast, bulk</Name>
            <Density>1050.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Mylar, bulk</Name>
            <Density>1390.0</Density>
            <Type>BULK</Type>
        </Material>

        <Material UnitsOfMeasure="g/m2">
            <Name>Polyethylene film, HDPE, 1.0 mil, bare</Name>
            <Density>0.0235</Density>
            <Type>SURFACE</Type>
        </Material>
        

    </Materials>

    <Components>

      <!-- Body Tubes, from 1970 Catalog 2.  Masses and ID/wall not given.  Wall taken to be .5 mm
           to match Quest tube dimensions, which have the same nominal ODs and come from a company
           run by the same family. -->
      
      <!-- T5 tubes -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-5, 30508</PartNumber>
        <Description>Body tube, T-5, 8 cm, PN 30508</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">4.0</InsideDiameter>
        <OutsideDiameter Unit="mm">5.0</OutsideDiameter>
        <Length Unit="cm">8.0</Length>
      </BodyTube>

      <!-- T-14 engine mount tube for 13mm engines
           This tube is referred to in the Minirocs brochure, though no dimensions are given.
           Length is 57mm to match the 2.25" length of the MPC mini-engines
      -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-14</PartNumber>
        <Description>Body tube, T-14, 57 mm, engine mount tube</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">13.0</InsideDiameter>
        <OutsideDiameter Unit="mm">14.0</OutsideDiameter>
        <Length Unit="mm">57.0</Length>
      </BodyTube>

      <!-- T15 tubes -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-15, 31510</PartNumber>
        <Description>Body tube, T-15, 10 cm, PN 31510</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">14.0</InsideDiameter>
        <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
        <Length Unit="cm">10.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-15, 31520</PartNumber>
        <Description>Body tube, T-15, 20 cm, PN 31520</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">14.0</InsideDiameter>
        <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
        <Length Unit="cm">20.0</Length>
      </BodyTube>

      <!-- T19 motor mount tubes for 18mm motor
           These tubes are only seen in the Stock No 420, 425 and 430 engine mounts and are presumed
           to be 70mm (2.75") long, OD=19.0mm, ID=18.0 mm.
           No stock no. is given for the tube itself.
           In reality the OD must be slightly less to be a slip fit inside a T-20.
           See instructions for Icarus-C on plans.rocketshoppe.com
      -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-19</PartNumber>
        <Description>Body tube, T-19, 7 cm, engine mount tube</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">18.0</InsideDiameter>
        <OutsideDiameter Unit="mm">19.0</OutsideDiameter>
        <Length Unit="cm">7.0</Length>
      </BodyTube>
      
      <!-- T20 tubes -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-20, 32007</PartNumber>
        <Description>Body tube, T-20, 7 cm, PN 32007</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">19.0</InsideDiameter>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <Length Unit="cm">7.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-20, 32015</PartNumber>
        <Description>Body tube, T-20, 15 cm, PN 32015</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">19.0</InsideDiameter>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <Length Unit="cm">15.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-20, 32022</PartNumber>
        <Description>Body tube, T-20, 22 cm, PN 32022</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">19.0</InsideDiameter>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <Length Unit="cm">22.0</Length>
      </BodyTube>

      <!-- T25 tubes -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-25, 32515</PartNumber>
        <Description>Body tube, T-25, 15 cm, PN 32515</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">24.0</InsideDiameter>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <Length Unit="cm">15.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-25, 32530</PartNumber>
        <Description>Body tube, T-25, 30 cm, PN 32530</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">24.0</InsideDiameter>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <Length Unit="cm">30.0</Length>
      </BodyTube>

      <!-- There is a 4" T-25 clear payload tube shown in Payload Section stock no. 113.
           Stock No of the clear tube by itself is unknown -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-25 clear</PartNumber>
        <Description>Body tube, clear, T-25, 10 cm</Description>
        <Material Type="BULK">Mylar, bulk</Material>
        <InsideDiameter Unit="mm">24.0</InsideDiameter>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <Length Unit="cm">10.0</Length>
      </BodyTube>

      <!-- T30 tubes -->
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-30, 33015</PartNumber>
        <Description>Body tube, T-30, 15 cm, PN 33015</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">29.0</InsideDiameter>
        <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
        <Length Unit="cm">15.0</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>T-30, 33022</PartNumber>
        <Description>Body tube, T-30, 22 cm, PN 33022</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">29.0</InsideDiameter>
        <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
        <Length Unit="cm">22.0</Length>
      </BodyTube>

      <!-- Centering rings (fiber), existence determined from the T-25 and T-30 engine mounts, and Moon Go
           instructions.  The Moon Go drawings show a thicker 0.25" type, similar to Estes AR-2050  -->
        <CenteringRing>
          <Manufacturer>MPC</Manufacturer>
          <PartNumber>CenteringRing-T19-T25</PartNumber>
          <Description>Centering ring, fiber, thick, T-19 to T-25</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="mm">19.1</InsideDiameter>
          <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
          <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- T-30 motor centering ring as used in Martian Patrol.  These are thinner die-cut cardstock rings. -->
        <CenteringRing>
          <Manufacturer>MPC</Manufacturer>
          <PartNumber>CenteringRing-T19-T30</PartNumber>
          <Description>Centering ring, fiber, T-19 to T-30</Description>
          <Material Type="BULK">Fiber, bulk</Material>
          <InsideDiameter Unit="mm">19.1</InsideDiameter>
          <OutsideDiameter Unit="mm">28.9</OutsideDiameter>
          <Length Unit="in">0.05</Length>
        </CenteringRing>
      
      
      <!-- Launch lugs 1/8" #107, exact length unknown; estimated 2.25" based on catalog drawing.
           ID/OD taken to match Estes lugs -->
      <LaunchLug>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>107</PartNumber>
        <Description>Launch lug, paper, 1/8 x 2.25", PN 107</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="in">0.156</InsideDiameter>
        <OutsideDiameter Unit="in">0.173</OutsideDiameter>
        <Length Unit="in">2.25</Length>
      </LaunchLug>

      <!-- Parachutes.  10" and 14" red plastic "less than a thousandth thick" chutes shown in Catalog 2 -->
      <Parachute>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>110</PartNumber>
        <Description>Parachute, plastic, 10 in., PN 110</Description>
        <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
        <Diameter Unit="in">10.0</Diameter>
        <Sides>6</Sides>
        <LineCount>6</LineCount>
        <LineLength Unit="in">10.0</LineLength>
        <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
      </Parachute>
      <Parachute>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>109</PartNumber>
        <Description>Parachute, plastic, 14 in., PN 109</Description>
        <Material Type="SURFACE">Polyethylene film, HDPE, 1.0 mil, bare</Material>
        <Diameter Unit="in">14.0</Diameter>
        <Sides>6</Sides>
        <LineCount>6</LineCount>
        <LineLength Unit="in">14.0</LineLength>
        <LineMaterial Type="LINE">Carpet Thread</LineMaterial>
      </Parachute>
      
      
      <!-- Balsa Nose Cones -->
      <!-- Nose cone shapes:
           I obtained the nose cone fineness ratios by photogrammetry of the shape reference
           drawings in 1970 Catalog 2 from the vintagevendingwarehouse.weebly.com site.  The images
           are low res but usable.  Values are as follows:
           Shape A:  Base diam 22.0 px, length 88.0 px, shoulder len 10.8 px =>  Ogive 4.0:1,
                     shoulder len = .50 * diam
           Shape B:  Base diam 23.0 px, len 31.1 px, shoulder len 12.0 px => Ogive 1.35:1
                     shoulder len = .50 * diam
           Shape C:  Base diam 22.4 px, len 87.9 px, shoulder len 12.2 px => cone (rounded tip) 3.9:1
                     shoulder len = .50 * diam
           From these measurements I think it is clear that both the long cone and ogive were
           intended to be 4:1 shapes, so I have adopted that in computing the nose cone lengths.
           Shoulder diameters have been set at 0.1mm (0.004") smaller than the tube IDs.
      -->
      <!-- T15 Nose Cones -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>615A</PartNumber>
        <Description>Nose cone, balsa, T-15, 60mm, ogive, PN 615A</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">13.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">7.5</ShoulderLength>
        <Length Unit="mm">60.0</Length>
      </NoseCone>
      <!-- Pipsqueak (#3-0915) 3 caliber ellipsoid balsa nose cone.  Described in the Minirocs brochure so we know the length and
      shape authoritatively.  No PN known. -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>615-Pipsqueak</PartNumber>
        <Description>Nose cone, balsa, T-15, 45mm, ellipsoid, Pipsqueak</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">13.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">7.5</ShoulderLength>
        <Length Unit="mm">45.0</Length>
      </NoseCone>
      <!-- Taurus-1 (#3-0920) 5 caliber ogive balsa nose cone.  Described in the Minirocs brochure so we know the length and
      shape authoritatively.  No PN known. -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>615-Taurus-1</PartNumber>
        <Description>Nose cone, balsa, T-15, 75mm, ogive, Taurus-1</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">15.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">13.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">7.5</ShoulderLength>
        <Length Unit="mm">75.0</Length>
      </NoseCone>

      <!-- T20 Nose Cones -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>620A</PartNumber>
        <Description>Nose cone, balsa, T-20, 80mm, ogive, PN 620A</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">18.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">10.0</ShoulderLength>
        <Length Unit="mm">80.0</Length>
      </NoseCone>
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>620B</PartNumber>
        <Description>Nose cone, balsa, T-20, 27mm, short ogive, PN 620B</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">18.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">10.0</ShoulderLength>
        <Length Unit="mm">27.0</Length>
      </NoseCone>
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>620C</PartNumber>
        <Description>Nose cone, balsa, T-20, 80mm, long ellipsoid, PN 620C</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">18.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">10.0</ShoulderLength>
        <Length Unit="mm">80.0</Length>
      </NoseCone>

      <!-- T25 balsa nose cones -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>625A</PartNumber>
        <Description>Nose cone, balsa, T-25, 100mm, ogive, PN 625A</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">23.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">12.5</ShoulderLength>
        <Length Unit="mm">100.0</Length>
      </NoseCone>
      
      <!-- T30 balsa nose cones -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>630A</PartNumber>
        <Description>Nose cone, balsa, T-30, 120mm, ogive, PN 630A</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Filled>true</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">28.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">15.0</ShoulderLength>
        <Length Unit="mm">120.0</Length>
      </NoseCone>

      <!-- Plastic nose cones -->
      <!-- MPC part numbering ("Stock No") for the 3 plastic nose cones 620PP, 625PA, and 630PS does
           not match the convention for the balsa nose cones where the last character indicates the
           shape.  625PA might be the A shape, but given that 620PP and 630PS do not end in A-B-C,
           they are all in some doubt.  My interpretation is that the P suffix means
           "parabola" and indicates the round tip shape C, while the S suffix means "short" and
           indicates shape B.

           We have no data on the mass, so the plastic thickness is an educated estimate.

           ***MISSING*** There is a "plastic space capsule" resembling a Gemini capsule used on the R-841 Moon
           Go that does not appear in the catalog parts.
      -->
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>620PP</PartNumber>
        <Description>Nose cone, polystyrene, T-20, 80mm, long ellipsoid, PN 620PP</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="mm">20.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">18.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">10.0</ShoulderLength>
        <Length Unit="mm">80.0</Length>
        <Thickness Unit="in">0.040</Thickness>
      </NoseCone>
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>625PA</PartNumber>
        <Description>Nose cone, polystyrene, T-25, 100mm, ogive, PN 625PA</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">23.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">12.5</ShoulderLength>
        <Length Unit="mm">100.0</Length>
        <Thickness Unit="in">0.040</Thickness>
      </NoseCone>
      <NoseCone>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>630PS</PartNumber>
        <Description>Nose cone, polystyrene, T-30, 40mm, short ogive, PN 630PS</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>ELLIPSOID</Shape>
        <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
        <ShoulderDiameter Unit="mm">28.9</ShoulderDiameter>
        <ShoulderLength Unit="mm">15.0</ShoulderLength>
        <Length Unit="mm">40.0</Length>
        <Thickness Unit="in">0.040</Thickness>
      </NoseCone>

      <!-- Transitions (called "Couplings" in MPC Catalog 2)

           We have little hard data at all on the balsa and plastic transitions except the tube sizes they
           connect.  There are couplers in both plastic and balsa that couple the same size tubes as well as
           differing sizes.  We do know from the Microsonde-3 instructions that the plastic couplers had
           separate end caps.  Also from a photo of the Microsonde-3 with its T20-T25 coupler we can determine
           that the exposed length is 25mm +/- 2mm.

           Here are the assumptions made to model these parts:
              Differing tube sizes
                  shoulder length = 0.5 * tube OD (roughly confirmed by catalog drawings)
                  exposed length = 25mm per 5mm of tube size change
                  shape is CONICAL (confirmed by catalog drawings)
              Same size tubes
                  total length of bulkhead = 1.0 * tube size
      -->
      <!-- T-15 smaller end -->
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>61520</PartNumber>
        <Description>Transition, balsa, T15-T20, increasing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">15.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">13.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">7.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">18.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">10.0</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>61520 [R]</PartNumber>
        <Description>Transition, balsa, T20-T15, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">18.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">10.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">15.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">13.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">7.5</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>

      <!-- T-20 smaller end -->
      <TubeCoupler>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62020</PartNumber>
        <Description>Tube coupler, balsa, T-20, PN 62020</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <InsideDiameter Unit="mm">0.0</InsideDiameter>
        <OutsideDiameter Unit="mm">18.9</OutsideDiameter>
        <Length Unit="mm">20.0</Length>
      </TubeCoupler>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62025</PartNumber>
        <Description>Transition, balsa, T20-T25, increasing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">18.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">10.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">12.5</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62025 [R]</PartNumber>
        <Description>Transition, balsa, T25-T20, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">12.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">18.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">10.0</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62520P</PartNumber>
        <Description>Transition, plastic, T20-T25, increasing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">18.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">10.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">12.5</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62520P [R]</PartNumber>
        <Description>Transition, plastic, T25-T20, reducing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">12.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">18.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">10.0</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>63020P</PartNumber>
        <Description>Transition, plastic, T20-T30, increasing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">20.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">18.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">10.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">28.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">15.0</AftShoulderLength>
        <Length Unit="mm">50.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>63020P [R]</PartNumber>
        <Description>Transition, plastic, T30-T20, reducing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">28.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">15.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">20.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">18.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">10.0</AftShoulderLength>
        <Length Unit="mm">50.0</Length>
      </Transition>
      

      <!-- T-25 smaller end -->
      <TubeCoupler>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62525</PartNumber>
        <Description>Tube coupler, balsa, T-25, PN 62525</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <InsideDiameter Unit="mm">0.0</InsideDiameter>
        <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
        <Length Unit="mm">25.0</Length>
      </TubeCoupler>
      <TubeCoupler>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62525P</PartNumber>
        <Description>Tube coupler, plastic, T-25, PN 62525P</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <InsideDiameter Unit="mm">23.0</InsideDiameter>
        <OutsideDiameter Unit="mm">25.0</OutsideDiameter>
        <Length Unit="mm">25.0</Length>
      </TubeCoupler>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62530</PartNumber>
        <Description>Transition, balsa, T25-T30, increasing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">12.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">28.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">15.0</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>62530 [R]</PartNumber>
        <Description>Transition, balsa, T30-T25, reducing</Description>
        <Material Type="BULK">Balsa, bulk, 7lb/ft3</Material>
        <Shape>CONICAL</Shape>
        <Filled>true</Filled>
        <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">28.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">15.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">12.5</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>63025P</PartNumber>
        <Description>Transition, plastic, T25-T30, increasing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">25.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">23.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">12.5</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">30.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">28.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">15.0</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>
      <Transition>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>63025P [R]</PartNumber>
        <Description>Transition, plastic, T30-T25, decreasing</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <Thickness Unit="in">0.040</Thickness>
        <ForeOutsideDiameter Unit="mm">30.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="mm">28.9</ForeShoulderDiameter>
        <ForeShoulderLength Unit="mm">15.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="mm">25.0</AftOutsideDiameter>
        <AftShoulderDiameter Unit="mm">23.9</AftShoulderDiameter>
        <AftShoulderLength Unit="mm">12.5</AftShoulderLength>
        <Length Unit="mm">25.0</Length>
      </Transition>


      <!-- T-30 smaller end -->
      <TubeCoupler>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>63030P</PartNumber>
        <Description>Tube coupler, plastic, T-30, PN 63030P</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <InsideDiameter Unit="mm">28.0</InsideDiameter>
        <OutsideDiameter Unit="mm">30.0</OutsideDiameter>
        <Length Unit="mm">30.0</Length>
      </TubeCoupler>

      <!-- Thrust rings -->

      <!-- T-14 thrust ring for 13mm motor mount. No PN / Stock No. known -->
      <EngineBlock>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>Thrust Ring T-14</PartNumber>
        <Description>Engine block, fiber, T-14, 6 mm len</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="mm">11.5</InsideDiameter>
        <OutsideDiameter Unit="mm">13.0</OutsideDiameter>
        <Length Unit="mm">6.0</Length>
      </EngineBlock>
      <!-- T-19 thrust ring for 18mm motor mount.  No PN / Stock No. known
           Length taken as about 6mm (ref drawing in Catalog 2)
           Wall thickness taken as about 0.75mm
      -->
      <EngineBlock>
        <Manufacturer>MPC</Manufacturer>
        <PartNumber>Thrust Ring T-19</PartNumber>
        <Description>Engine block, fiber, T-19, 6 mm len</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="mm">16.5</InsideDiameter>
        <OutsideDiameter Unit="mm">18.0</OutsideDiameter>
        <Length Unit="mm">6.0</Length>
      </EngineBlock>

      <!-- Stage couplers -->
      <!-- Black stage couplers shown in the drawings for the T-25 and T-30 engine mounts and (for 30mm) in
           the Martian Patrol instructions, where it is called a "spacer tube" and used in the engine
           mount. The gluing instructions prove that the spacer tube is a slip fit in a T-30, and the drawing
           makes it look about 25-30mm long. They were never sold separately.  Existence of the T-25 stage
           coupler is indicated in the Zenith 2 Payloader instructions, where it is documented as 1 1/4" long.
           That sets a minimum length on the T-30 coupler.
      -->
      
      <!-- T-25 stage coupler is documented as 1.25" long.  Wall thickness unknown, taken to be 1.0 mm (0.040") -->
      <TubeCoupler>
        <Manufacturer>MRC</Manufacturer>
        <PartNumber>StageCoupler-T25</PartNumber>
        <Description>Tube coupler, paper, T-25, 1.25"</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">21.9</InsideDiameter>
        <OutsideDiameter Unit="mm">23.9</OutsideDiameter>
        <Length Unit="in">1.25</Length>
      </TubeCoupler>
      <!-- T-30 stage coupler length estimated from drawing at 1.5" long.  Wall thickness unknown, taken to be 1.0 mm (0.040") -->
      <TubeCoupler>
        <Manufacturer>MRC</Manufacturer>
        <PartNumber>StageCoupler-T30</PartNumber>
        <Description>Tube coupler, paper, T-30, 1.50"</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, bulk</Material>
        <InsideDiameter Unit="mm">26.9</InsideDiameter>
        <OutsideDiameter Unit="mm">28.9</OutsideDiameter>
        <Length Unit="in">1.5</Length>
      </TubeCoupler>
      

    </Components>
</OpenRocketComponent>
