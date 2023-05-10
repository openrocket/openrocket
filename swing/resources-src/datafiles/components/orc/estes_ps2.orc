<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<!--
Estes Pro Series II PSII components file for OpenRocket

by Dave Cook  NAR 21953  caveduck17@gmail.com 2014-2017

Estes gives some dimensions for PSII parts on www.estesrockets.com/rockets/pro-series/parts
However this is generally limited to the PN, OD and length.
This TRF thread
http://www.rocketryforum.com/showthread.php?43514-Estes-Pro-Series-2-tube-sizes
gives some additional information, though I believe based on my own measurements that
the tube wall is 0.034", not 0.040 as given there.

As of late 2016 all the plywood-fin PSII kits have been discontinued due to the Chinese
plywood not meeting US non-toxic requirements.  Only the PS2-E2X models with plastic fins
have survived into 2017.  This is likely to lead to discontinuance of some PSII parts.

Parts from Partizon #9702:
  Slotted tube 85865 2.5 x 15.5"
  Regular tube 31390 2.5 x 15.5"
  29mm motor mount tube 31360
  29mm to 2.5 rings, plywood, 66475
  29mm motor block 31362

Parts from #9701 Ventris
  29mm to 2.0 rings, plywood, 66540
  2.5 payload tube PN 31380
  
*** Need accurate masses for all NCs and the plastic tube transition ***
*** Need PN 72415 Mega Der Red Max 4.0" nose cone.  Similar to LOC 4.0" NC, material=? ***
*** Need launch lugs 38181 (fiber) and 38182 (MDRM, plastic molded) ***

-->
      
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Paper, spiral kraft glassine, Estes avg, bulk</Name>
            <Density>894.4</Density>
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
        <!-- 1.7oz ripstop nylon is Top Flight and LOC standard parachute material -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.7 oz actual</Name>
            <Density>0.05764</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 110 lb, 1/16 in. dia.</Name>
            <Density>0.00160</Density>
            <Type>LINE</Type>
        </Material>
    </Materials>

    <Components>

      <!-- Centering rings all light ply 1/8" -->
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66540</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 2.0", PN 66540</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">1.927</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66475</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 2.5", PN 66475</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">2.427</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <!--- Nike Smoke has slotted CR for fin tabs PN 66611 -->
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66611</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 3.0", slotted, PN 66611</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">2.927</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <!--- PN 66468 plain ply centering ring used in #9704 Nike Smoke -->
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66468</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 3.0", PN 66468</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">2.927</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <!-- Mega Der Red Max #9705 has 3 different types of CRs: plain, fin slotted, shock cord slot -->
      <!-- 29mm - 4.0" plain centering ring -->
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66735</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 4.0", PN 66735</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">3.895</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66736</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 4.0", 3 fin slots, PN 66736</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">3.895</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>
      <CenteringRing>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>66743</PartNumber>
        <Description>Centering ring, plywood, 29mm to PSII 4.0", shock cord slot, PN 66743</Description>
        <Material Type="BULK">Plywood, light, bulk</Material>
        <InsideDiameter Unit="in">1.213</InsideDiameter>
        <OutsideDiameter Unit="in">3.895</OutsideDiameter>
        <Length Unit="in">0.125</Length>
      </CenteringRing>


      <!-- PSII 29mm motor mount tube 8" long.  Using .034 wall until good measurement -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31360</PartNumber>
        <Description>Body tube, paper, 29mm ID, white, 8.0 in., PN 31360</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.14</InsideDiameter>
        <OutsideDiameter Unit="in">1.208</OutsideDiameter>
        <Length Unit="in">8.0</Length>
      </BodyTube>

      <!-- PSII 29mm engine block 0.25" green -->
      <EngineBlock>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31362</PartNumber>
        <Description>Engine block, fiber, green, 29mm, 0.25" len, PN 31362</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="in">0.875</InsideDiameter>
        <OutsideDiameter Unit="in">1.130</OutsideDiameter>
        <Length Unit="in">0.25</Length>
      </EngineBlock>
      
      <!-- PN 31361 "Green spacer ring" is slip fit over the 29mm motor tube
           Dimensions measured - wall is .039, ID 1.216, OD 1.292
           Used in #9702 Partizon etc. to position centering rings on the motor mount
      -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31361</PartNumber>
        <Description>Body tube, paper, 29mm+, green, 0.5 in., PN 31361</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="in">1.216</InsideDiameter>
        <OutsideDiameter Unit="in">1.92</OutsideDiameter>
        <Length Unit="in">0.5</Length>
      </BodyTube>

      
      <!-- PSII 2.0" tubes have OD 2.00", white outer wrap. Assuming .034 wall til proven otherwise -->
      <!-- PN 30615 is upper body tube of #9703 Argent -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>30615</PartNumber>
        <Description>Body tube, 2.0 dia, white, 13.5 in., PN 30615</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.932</InsideDiameter>
        <OutsideDiameter Unit="in">2.00</OutsideDiameter>
        <Length Unit="in">13.5</Length>
      </BodyTube>
      <!-- PSII 2.0" PN 85866 4 slot tube used in Ventris -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>85866</PartNumber>
        <Description>Body tube, 2.0 dia, white, 13.5 in., 4 slots, PN 85866</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.932</InsideDiameter>
        <OutsideDiameter Unit="in">2.00</OutsideDiameter>
        <Length Unit="in">13.5</Length>
      </BodyTube>
      <!-- PSII 2.0" coupler - Estes gives len 4.0", using .030 wall and .012 OD offset from 2.5"
           Example I have is white outer wrap like regular tubes so I used tube material, not fiber
      -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>30618</PartNumber>
        <Description>Tube coupler, 2.0" series, paper, white,PN 30618</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">1.860</InsideDiameter>
        <OutsideDiameter Unit="in">1.920</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
      
      <!-- PSII 2.0" series ogive nose conePN 72653 given as 9.6" long, mass unknown -->
      <NoseCone>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72653</PartNumber>
        <Description>Nose cone, plastic, 2.0" series, 9.6" len, PN 72653</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.000</OutsideDiameter>
        <ShoulderDiameter Unit="in">1.920</ShoulderDiameter>
        <ShoulderLength Unit="in">2.0</ShoulderLength>
        <Length Unit="in">9.6</Length>
        <Thickness>0.125</Thickness>
      </NoseCone>

      <!-- PSII 2.0" to 2.5" transition PN 72657 - no dimensions given by Estes.  However see
           http://www.rocketryforum.com/showthread.php?43514-Estes-Pro-Series-2-tube-sizes
           which gives approximate dimensions, but no masses -->
      <Transition>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72657</PartNumber>
        <Description>Transition, plastic, PSII 2.0 to 2.5 series, increasing, PN 72657</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>CONICAL</Shape>
        <ForeOutsideDiameter Unit="in">2.0</ForeOutsideDiameter>
        <ForeShoulderDiameter Unit="in">1.920</ForeShoulderDiameter>
        <ForeShoulderLength Unit="in">2.0</ForeShoulderLength>
        <AftOutsideDiameter Unit="in">2.5</AftOutsideDiameter>
        <AftShoulderDiameter Unit="in">2.420</AftShoulderDiameter>
        <AftShoulderLength Unit="in">2.5</AftShoulderLength>
        <Length Unit="in">2.25</Length>
        <Thickness Unit="in">0.125</Thickness>
      </Transition>

      <!-- PSII 2.5" tubes have OD 2.50" and 0.034" wall, white outer wrap -->
      <!-- PN 31390 2.5 x 15.5 tube mass 58.0 gm measured -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31390</PartNumber>
        <Description>Body tube, 2.5 dia, white, 15.5 in., PN 31390</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.432</InsideDiameter>
        <OutsideDiameter Unit="in">2.50</OutsideDiameter>
        <Length Unit="in">15.5</Length>
      </BodyTube>
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>85865</PartNumber>
        <Description>Body tube, 2.5 dia, white, 15.5 in., 3 slots, PN 85865</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.432</InsideDiameter>
        <OutsideDiameter Unit="in">2.50</OutsideDiameter>
        <Length Unit="in">15.5</Length>
      </BodyTube>
      <!-- PN 31380 2.5 x 6.5" Ventris payload tube -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31380</PartNumber>
        <Description>Body tube, 2.5 dia, white, 6.5 in., PN 31390</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.432</InsideDiameter>
        <OutsideDiameter Unit="in">2.50</OutsideDiameter>
        <Length Unit="in">6.5</Length>
      </BodyTube>

      <!-- 2.5" series coupler 4.0" long, wall measures .030", mass 12 gm
           examples I have from Partizon kits are natural fiber outside -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>30189</PartNumber>
        <Description>Tube coupler, 2.5" series, paper, kraft, PN 30189</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="in">2,360</InsideDiameter>
        <OutsideDiameter Unit="in">2.420</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>
     
      <!-- 2.5" series ogive nose cone 9.75" long, shoulder len measured 2.43", mass ~= 60 gm -->
      <NoseCone>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72413</PartNumber>
        <Description>Nose cone, plastic, 2.5" series, 10.75" len, PN 72413</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">2.500</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.420</ShoulderDiameter>
        <ShoulderLength Unit="in">2.43</ShoulderLength>
        <Length Unit="in">9.75</Length>
        <Thickness>0.125</Thickness>
      </NoseCone>

      <!-- PSII 3.0" tube OD given as 3 in, len 21.6", used in #9700 Leviathan -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31799</PartNumber>
        <Description>Body tube, paper, 3.0 dia, white, 21.5 in., PN 31799</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.932</InsideDiameter>
        <OutsideDiameter Unit="in">3.0</OutsideDiameter>
        <Length Unit="in">21.5</Length>
      </BodyTube>
      <!-- PSII 3.0" slotted tube PN 31751 OD given as 3 in, len 10.0" -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31751</PartNumber>
        <Description>Body tube, paper, 3.0 dia, white, 10.0 in., 4 slots, PN 31751</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.932</InsideDiameter>
        <OutsideDiameter Unit="in">3.0</OutsideDiameter>
        <Length Unit="in">10.0</Length>
      </BodyTube>
      <!-- PSII 3.0" 4 slot tube PN 85867 OD given as 3 in, len 23.67", used in #9704 Nike Smoke -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>85867</PartNumber>
        <Description>Body tube, paper, 3.0 dia, white, 23.67 in., 4 slots, PN 31751</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">2.932</InsideDiameter>
        <OutsideDiameter Unit="in">3.0</OutsideDiameter>
        <Length Unit="in">23.67</Length>
      </BodyTube>
      <!-- PSII 3.0" series coupler, given len4.0" long, using measured wall and offset from 2.5" coupler -->
      <TubeCoupler>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>31800</PartNumber>
        <Description>Tube coupler, 3.0" series, paper, PN 31800</Description>
        <Material Type="BULK">Fiber, bulk</Material>
        <InsideDiameter Unit="in">2,860</InsideDiameter>
        <OutsideDiameter Unit="in">2.920</OutsideDiameter>
        <Length Unit="in">4.0</Length>
      </TubeCoupler>

      <!-- PSII 3.0" series nose cone, 8.0" ogive used in #9700 Leviathan
           Length obtained by photogrammetry on picture in Leviathan instructions -->
      <NoseCone>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72695</PartNumber>
        <Description>Nose cone, plastic, 3.0" series, 8.0" len, PN 72695</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">3.000</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.920</ShoulderDiameter>
        <ShoulderLength Unit="in">3.0</ShoulderLength>
        <Length Unit="in">8.0</Length>
        <Thickness>0.125</Thickness>
      </NoseCone>
      <!-- PSII 3.0" series nose cone PN 72753, 17.4" modified cone used in #9704 Nike Smoke
           Length obtained by photogrammetry on picture in Nike Smoke instructions
           The shape is really a 16.75" increasing cone to about 3.1" diam, follwed by a
           .65" decreasing cone back to 3.0" at the base of the nose.  Mass unknown.
      -->
      <NoseCone>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72753</PartNumber>
        <Description>Nose cone, plastic, 3.0" series, 17.4" len, PN 72753</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Filled>false</Filled>
        <Shape>CONICAL</Shape>
        <OutsideDiameter Unit="in">3.000</OutsideDiameter>
        <ShoulderDiameter Unit="in">2.920</ShoulderDiameter>
        <ShoulderLength Unit="in">3.0</ShoulderLength>
        <Length Unit="in">17.4</Length>
        <Thickness>0.125</Thickness>
      </NoseCone>


      <!-- PSII 4.0" 3 slot tube used in Mega Der Red Max, Estes gives len 20.75
           Using LOC tube wall thickness as they are said to be compatible -->
      <BodyTube>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>85868</PartNumber>
        <Description>Body tube, paper, 4.0" dia, white, 20.75 in., PN 85868</Description>
        <Material Type="BULK">Paper, spiral kraft glassine, Estes avg, bulk</Material>
        <InsideDiameter Unit="in">3.9</InsideDiameter>
        <OutsideDiameter Unit="in">4.0</OutsideDiameter>
        <Length Unit="in">20.75</Length>
      </BodyTube>

      <!-- PSII 4.0" nose cone 12.5" blunt tip ogive used in Megaa Der Red Max PN 72415
           Using LOC PNC-3.9 dimensions as they are quite close.  Should weigh about 151 gm -->
      <NoseCone>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>72415</PartNumber>
        <Description>Nose cone, plastic, 4.0" series, 12.5" ogive, PN 72415</Description>
        <Material Type="BULK">Polystyrene, cast, bulk</Material>
        <Shape>OGIVE</Shape>
        <OutsideDiameter Unit="in">4.0</OutsideDiameter>
        <ShoulderDiameter Unit="in">3.88</ShoulderDiameter>
        <ShoulderLength Unit="in">3.75</ShoulderLength>
        <Length Unit="in">12.75</Length>
        <Thickness Unit="in">0.125</Thickness>
      </NoseCone>

      <!-- Parachutes (nylon) -->
      
      <!-- PSII 24" nylon parachute PN 002261 on 2017 website
           No info on materials/weight, looks like Top Flight -->
      <Parachute>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>002261</PartNumber>
        <Description>Parachute, 24 in., nylon, 6 lines, PN 002261</Description>
        <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
        <Diameter Unit="in">24.0</Diameter>
        <Sides>6</Sides>
        <LineCount>6</LineCount>
        <LineLength Unit="in">24.0</LineLength>
        <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
      </Parachute>
      <!-- PSII 30" nylon parachute PN 002273 on 2017 website
           No info on materials/weight, looks like Top Flight -->
      <Parachute>
        <Manufacturer>Estes</Manufacturer>
        <PartNumber>002273</PartNumber>
        <Description>Parachute, 30 in., nylon, 8 lines, PN 002273</Description>
        <Material Type="SURFACE">Nylon fabric, ripstop, 1.7 oz actual</Material>
        <Diameter Unit="in">30.0</Diameter>
        <Sides>8</Sides>
        <LineCount>8</LineCount>
        <LineLength Unit="in">30.0</LineLength>
        <LineMaterial Type="LINE">Nylon Paracord, 110 lb, 1/16 in. dia.</LineMaterial>
      </Parachute>

    </Components>
</OpenRocketComponent>
