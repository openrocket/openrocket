<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
Apogee Components parts file for OpenRocket

Apogee (apogeerockets.com) primarily resells products from various rocketry vendors, but
they also sell a few unique parts such as the foam nose cones and egg protectors intended
for TARC.

by Dave Cook  NAR 21953  caveduck17@gmail.com 2021

-->

<OpenRocketComponent>
    <Version>0.1</Version>

 <!-- Materials used in this file -->
    <Materials>
        <Material UnitsOfMeasure="kg/m3">
          <Name>Foam, urethane, bulk</Name>
          <Density>48.1</Density>
          <Type>BULK</Type>
        </Material>

    </Materials>

    <Components>

        <!-- TARC foam egg protectors -->
        <!-- Egg protectors are modeled as BulkHead, nominal foam density 3 oz/ft**3 -->

        <!-- 14807 horizontal single egg protector for 3.0" LOC/Madcow cardboard tubes, mass 36.7 gm -->
        <!-- SOURCE ERROR: Apogee 14807 egg protector length is shown as 2.5" but that looks very wrong, assuming 2.5" per half? -->
        <BulkHead>
                <Manufacturer>Apogee</Manufacturer>
                <PartNumber>14807</PartNumber>
                <Description>Egg protector, urethane foam, 3.0in, single horizontal, PN 14807</Description>
                <Material Type="BULK">Foam, urethane, bulk</Material>
                <OutsideDiameter Unit="in">2.93</OutsideDiameter>
                <Length Unit="in">5.0</Length>
                <Mass Unit="g">36.7</Mass>

        </BulkHead>

        <!-- 14805 horizontal single egg protector for 2.6" BT-80 cardboard tubes, mass 23 gm -->
        <BulkHead>
                <Manufacturer>Apogee</Manufacturer>
                <PartNumber>14805</PartNumber>
                <Description>Egg protector, urethane foam, BT-80, single horizontal, PN 14805</Description>
                <Material Type="BULK">Foam, urethane, bulk</Material>
                <OutsideDiameter Unit="in">2.5</OutsideDiameter>
                <Length Unit="in">4.125</Length>
                <Mass Unit="g">23.0</Mass>
        </BulkHead>

        <!-- 14821 vertical single egg protector for 2.6" BT-80 tubes, specs not given, unable to list
             SOURCE ERROR: Apogee 14821 vertical single egg protector for BT-80 specs not given at all -->

        <!-- 14814 vertical dual egg protector for BT-70 tube, mass 26.4 gm 
             SOURCE ERROR: Apogee egg protector 14814 OD 2.217 is wrong, that's OD of BT-70, should be 2.175 or less -->
        <BulkHead>
                <Manufacturer>Apogee</Manufacturer>
                <PartNumber>14814</PartNumber>
                <Description>Egg protector, urethane foam, BT-70, dual vertical, PN 14814</Description>
                <Material Type="BULK">Foam, urethane, bulk</Material>
                <OutsideDiameter Unit="in">2.175</OutsideDiameter>
                <Length Unit="in">6.5</Length>
                <Mass Unit="g">26.4</Mass>
        </BulkHead>

        <!-- 14817 vertical dual egg protector for BT-80 tube, mass 55.4 gm -->
        <BulkHead>
                <Manufacturer>Apogee</Manufacturer>
                <PartNumber>14817</PartNumber>
                <Description>Egg protector, urethane foam, BT-80, dual vertical, PN 14817</Description>
                <Material Type="BULK">Foam, urethane, bulk</Material>
                <OutsideDiameter Unit="in">2.486</OutsideDiameter>
                <Length Unit="in">6.1</Length>
                <Mass Unit="g">55.4</Mass>
        </BulkHead>


        <!-- TARC foam nose cones -->

        <!-- 14811 BT-70 foam nose cone, 2.5" long, shoulder 1.0", mass 23.5 gm -->
        <!-- SOURCE ERROR: Apogee 14811 BT-70 foam NC mass 23.5 gm does not correspond to 3lb/ft^3 foam, volumetric mass 8 gm-->
        <NoseCone>
            <Manufacturer>Apogee</Manufacturer>
            <PartNumber>14811</PartNumber>
            <Description>Nose cone, foam, BT-70, 2.5", ellipsoid, PN 14811</Description>
            <Material Type="BULK">Foam, urethane, bulk</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">2.217</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.175</ShoulderDiameter>
            <ShoulderLength Unit="in">1.0</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Mass Unit="g">23.5</Mass>
        </NoseCone>

        <!-- 14812 BT-80 foam nose cone, 2.5" long, shoulder 1.0", mass 29.4 gm-->
        <!-- SOURCE ERROR: Apogee 14812 BT-80 foam NC specified mass 29.4 gm does not correspond to 3lb/ft^3 foam -->
        <NoseCone>
            <Manufacturer>Apogee</Manufacturer>
            <PartNumber>14812</PartNumber>
            <Description>Nose cone, foam, BT-80, 2.5", ellipsoid, PN 14812</Description>
            <Material Type="BULK">Foam, urethane, bulk</Material>
            <Filled>true</Filled>
            <Shape>ELLIPSOID</Shape>
            <OutsideDiameter Unit="in">2.60</OutsideDiameter>
            <ShoulderDiameter Unit="in">2.5</ShoulderDiameter>
            <ShoulderLength Unit="in">1.0</ShoulderLength>
            <Length Unit="in">2.5</Length>
            <Mass Unit="g">29.4</Mass>
        </NoseCone>

    </Components>
</OpenRocketComponent>
