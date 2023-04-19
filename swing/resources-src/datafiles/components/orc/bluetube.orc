<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
Blue Tube (from Always Ready Rocketry) parts file for OpenRocket.

by Dave Cook NAR 21953  caveduck17@gmail.com 2017

Summary
=======
This file provides Blue Tube parts definitions per the dimensions now shown on the vendor's web
site alwaysreadyrocketry.com.  

The Blue Tube product line is now in the hands of Always Ready Rocketry (alwaysreadyrocketry.com).
As of 2017, they produce CNC machined plywood bulkheads and centering rings to fit the blue tube
sizes, and also sell nose cones of unspecified origin.

The stock OpenRocket bluetube.orc only lists tubes and couplers; it does not have any of the
bulkheads, rings or nose cones currently listed by ARR.  We should be able to add the bulkheads
and centering rings; howeever the nose cones sold by ARR have no dimensions listed.  It's not
clear from where they are sourced.

Dimension Evolution over Time
=============================
The current (2017) dimensions are complete, but well hidden in the dropdown menu items on the ARR store page.
Dimensions previously available from vendors were very inconsistent.

It appears likely that various batches of Blue Tube produced over the years have not all had
identical dimensions.  There is an old RockSim parts file BTDATA.CSV that once was linked from the ARR
website that had mildly variant dimensions for some sizes and wall thicknesses.  Notably, the IDs
in that file were essentially identical to LOC tube IDs except for the 5.5" tube which was smaller by 0.035".

There is an interesting statement on the "Bulkheads" page in the ARR store that "we individually
fit each ring and bulkplate to current production Blue Tube 2.0 sizes...".  This rather strongly
implies that the Blue Tube sizes used to be different.

The current tube inside diameters (IDs) mostly align with the prevalent LOC Precision tube sizes;
the major exception is the 5.5" series tubes where the diameter is more than 0.1" different.

In the stock OpenRocket, the tubes were found in "bluetube.orc", and there is no mention of
Alwaays Ready Rocketry or the additional parts they make.  The dimensions in that file are from
non-current data; possibly they are derived from the old RockSim file.

Overall, there is not enough data available to track the historical Blue Tube size variants,
so I have elected to only show the current 2017 published dimensions.

Tube Density
============

I have a tube density analysis based on my own actual and externally reported measurements that finds the
density of Blue Tube to be 1153.5 kg/m3 with a very good standard deviation of 17.3.  The stock
OpenRocket file has a density value of 1250, which is nearly 10% too high.

Part Numbers
============

The PNs shown in the stock OpenRocket file came from previous editions of the ARR website.  On the current
(2017) site there are no PNs for any product - even when you add the items to your cart, they are shown by full description only.
The old tube PNs looked like "BT20-139C"; all started with "BT20", which is a bit confusing
until you know it means "BlueTube 2.0" and does not refer to a size.
Four foot lengths had an A suffix, while one foot lengths had a "C suffix."  The 6 foot lengths now
available for the 5.5, 6.0 and 7.5" sizes were not then represented.

There is a quirk in the listings for 1.15" and 1.52" x 48" tubes:  there are separate listings
for two apparently different items, one designated as "airframe", the other as "MMT ONLY".  The
dimensions shown are identical so it's not clear what the difference is.  For larger tubes 2.15"
and up, all are shown as "airframe/MMT".

Tube couplers are listed in "standard" 12-18 inch lengths, and also in 48" "full length" pieces.
Fortunately, as for the body tubes, complete dimensions are given in the store menu dropdowns.

To fix all this I have created consistent pseudo-PNs using the diameter and length in inches.

BT_1.15_12_MMT     1.15" (29mm) MMT tube 12" long
BT_1.15_48         1.15" (29mm) Airframe tube 48" long
BT_1.52_48_MMT     1.52" (38mm) MMT tube 48" long
BT_2.15_48         2.15" (54MM) airframe/MMT tube 48" long
BT_7.5_72          7.5" airframe tube 72" long
TC_5.5_12          Coupler for BT_5.5, 12" long
-->
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
        <!-- Blue Tube density from measurements.  Standard deviation is 17.3 kg/m3 -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Vulcanized Fiber</Name>
            <Density>1153.5</Density>
            <Type>BULK</Type>
        </Material>
    </Materials>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 1/4 in. bulk</Name>
            <Density>344.3</Density>
            <Type>BULK</Type>
       </Material>

    <Components>

        <!-- ================================ -->
        <!-- AIRFRAME / MMT TUBES             -->
        <!-- ================================ -->

        <!-- 1.15" (29mm) tubes.  MMT sold in 12, 24, 48" lengths, aiframe in 48" only -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.15_12_MMT</PartNumber>
            <Description>Blue Tube, 1.15"/29mm, MMT, 12" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.150</InsideDiameter>
            <OutsideDiameter Unit="in">1.274</OutsideDiameter>
            <Length Unit="in">12.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.15_24_MMT</PartNumber>
            <Description>Blue Tube, 1.15"/29mm, MMT, 24" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.150</InsideDiameter>
            <OutsideDiameter Unit="in">1.274</OutsideDiameter>
            <Length Unit="in">24.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.15_48_MMT</PartNumber>
            <Description>Blue Tube, 1.15"/29mm, MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.150</InsideDiameter>
            <OutsideDiameter Unit="in">1.274</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.15_48</PartNumber>
            <Description>Blue Tube, 1.15"/29mm, airframe, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.150</InsideDiameter>
            <OutsideDiameter Unit="in">1.274</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 1.52" (38mm) tubes, 48" lengths only, separate airframe and MMT types -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.52_48_MMT</PartNumber>
            <Description>Blue Tube, 1.52"/38mm, MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.520</InsideDiameter>
            <OutsideDiameter Unit="in">1.644</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_1.52_48</PartNumber>
            <Description>Blue Tube, 1.52"/38mm, airframe, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.520</InsideDiameter>
            <OutsideDiameter Unit="in">1.644</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 2.15" (54mm) tube, combined airframe/MMT, 48" len only -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_2.15_48</PartNumber>
            <Description>Blue Tube, 2.15"/54mm, airframe/MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.15</InsideDiameter>
            <OutsideDiameter Unit="in">2.274</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 2.56" (63mm) tube, airframe, 48" len only -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_2.56_48</PartNumber>
            <Description>Blue Tube, 2.56"/63mm, airframe, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.56</InsideDiameter>
            <OutsideDiameter Unit="in">2.684</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 3.0" (75mm) tube, combined airframe/MMT, 48" len only -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_3.00_48</PartNumber>
            <Description>Blue Tube, 3.00"/75mm, airframe/MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">3.00</InsideDiameter>
            <OutsideDiameter Unit="in">3.124</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 4" (3.9" / 98mm) tube, combined airframe/MMT, 48" len only -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_3.90_48</PartNumber>
            <Description>Blue Tube, 3.90"/98mm, airframe/MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">3.9</InsideDiameter>
            <OutsideDiameter Unit="in">4.024</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- 5.5" tube, 48" and 72" lengths available -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_5.5_48</PartNumber>
            <Description>Blue Tube, 5.5", airframe, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.5</InsideDiameter>
            <OutsideDiameter Unit="in">5.654</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_5.5_72</PartNumber>
            <Description>Blue Tube, 5.5", airframe, 72" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.5</InsideDiameter>
            <OutsideDiameter Unit="in">5.654</OutsideDiameter>
            <Length Unit="in">72.0</Length>
        </BodyTube>

        <!-- 6" (150mm) tube, 48" and 72" lengths available -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_6.0_48</PartNumber>
            <Description>Blue Tube, 6.0", airframe/MMT, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">6.0</InsideDiameter>
            <OutsideDiameter Unit="in">6.148</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_6.0_72</PartNumber>
            <Description>Blue Tube, 6.0", airframe, 72" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">6.0</InsideDiameter>
            <OutsideDiameter Unit="in">6.148</OutsideDiameter>
            <Length Unit="in">72.0</Length>
        </BodyTube>

        <!-- 7.5" tube, 48" and 72" lengths available -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_7.5_48</PartNumber>
            <Description>Blue Tube, 7.5", airframe, 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">7.5</InsideDiameter>
            <OutsideDiameter Unit="in">7.660</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>BT_7.5_72</PartNumber>
            <Description>Blue Tube, 7.5", airframe, 72" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">7.5</InsideDiameter>
            <OutsideDiameter Unit="in">7.660</OutsideDiameter>
            <Length Unit="in">72.0</Length>
        </BodyTube>

        <!-- ================================ -->
        <!-- TUBE COUPLERS                    -->
        <!-- ================================ -->

        <!-- TC_1.15 available in 8" and 48" lengths -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_1.15_8</PartNumber>
            <Description>Blue Tube coupler, 1.15", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.023</InsideDiameter>
            <OutsideDiameter Unit="in">1.147</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_1.15_48</PartNumber>
            <Description>Blue Tube coupler, 1.15", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.023</InsideDiameter>
            <OutsideDiameter Unit="in">1.147</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-1.52 available in 8" and 48" lengths -->
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_1.52_8</PartNumber>
            <Description>Blue Tube coupler, 1.52", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.393</InsideDiameter>
            <OutsideDiameter Unit="in">1.517</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_1.52_48</PartNumber>
            <Description>Blue Tube coupler, 1.52", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">1.393</InsideDiameter>
            <OutsideDiameter Unit="in">1.517</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-2.15 available in 8" and 48" lengths -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_2.15_8</PartNumber>
            <Description>Blue Tube coupler, 2.15", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.023</InsideDiameter>
            <OutsideDiameter Unit="in">2.147</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_2.15_48</PartNumber>
            <Description>Blue Tube coupler, 2.15", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.023</InsideDiameter>
            <OutsideDiameter Unit="in">2.147</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-2.56 available in 8" and 48" lengths -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_2.56_8</PartNumber>
            <Description>Blue Tube coupler, 2.56", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.433</InsideDiameter>
            <OutsideDiameter Unit="in">2.557</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_2.56_48</PartNumber>
            <Description>Blue Tube coupler, 2.56", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.433</InsideDiameter>
            <OutsideDiameter Unit="in">2.557</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-3.00 available in 8" and 48" lengths -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_3.00_8</PartNumber>
            <Description>Blue Tube coupler, 3.00", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.873</InsideDiameter>
            <OutsideDiameter Unit="in">2.997</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_3.00_48</PartNumber>
            <Description>Blue Tube coupler, 3.00", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">2.873</InsideDiameter>
            <OutsideDiameter Unit="in">2.997</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-3.90 available in 8" and 48" lengths -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_3.90_8</PartNumber>
            <Description>Blue Tube coupler, 3.90", 8" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">3.773</InsideDiameter>
            <OutsideDiameter Unit="in">3.897</OutsideDiameter>
            <Length Unit="in">8.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_3.90_48</PartNumber>
            <Description>Blue Tube coupler, 3.90", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">3.773</InsideDiameter>
            <OutsideDiameter Unit="in">3.897</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-5.5 available in 12", 16" and 48" lengths, 0.077" wall -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_5.5_12</PartNumber>
            <Description>Blue Tube coupler, 5.5", 12" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.343</InsideDiameter>
            <OutsideDiameter Unit="in">5.497</OutsideDiameter>
            <Length Unit="in">12.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_5.5_16</PartNumber>
            <Description>Blue Tube coupler, 5.5", 16" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.343</InsideDiameter>
            <OutsideDiameter Unit="in">5.497</OutsideDiameter>
            <Length Unit="in">16.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_5.5_48</PartNumber>
            <Description>Blue Tube coupler, 5.5", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.343</InsideDiameter>
            <OutsideDiameter Unit="in">5.497</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-6.0 available in 12", 16" and 48" lengths, 0.074" wall -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_6.0_12</PartNumber>
            <Description>Blue Tube coupler, 6.0", 12" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.849</InsideDiameter>
            <OutsideDiameter Unit="in">5.997</OutsideDiameter>
            <Length Unit="in">12.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_6.0_16</PartNumber>
            <Description>Blue Tube coupler, 6.0", 16" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.849</InsideDiameter>
            <OutsideDiameter Unit="in">5.997</OutsideDiameter>
            <Length Unit="in">16.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_6.0_48</PartNumber>
            <Description>Blue Tube coupler, 6.0", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">5.849</InsideDiameter>
            <OutsideDiameter Unit="in">5.997</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- TC-7.5 available in 12", 16" and 48" lengths, 0.080" wall -->>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_7.5_12</PartNumber>
            <Description>Blue Tube coupler, 7.5", 12" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">7.337</InsideDiameter>
            <OutsideDiameter Unit="in">7.497</OutsideDiameter>
            <Length Unit="in">12.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_7.5_16</PartNumber>
            <Description>Blue Tube coupler, 7.5", 16" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">7.337</InsideDiameter>
            <OutsideDiameter Unit="in">7.497</OutsideDiameter>
            <Length Unit="in">16.0</Length>
        </BodyTube>
        <BodyTube>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>TC_7.5_48</PartNumber>
            <Description>Blue Tube coupler, 7.5", 48" len</Description>
            <Material Type="BULK">Vulcanized Fiber</Material>
            <InsideDiameter Unit="in">7.337</InsideDiameter>
            <OutsideDiameter Unit="in">7.497</OutsideDiameter>
            <Length Unit="in">48.0</Length>
        </BodyTube>

        <!-- Centering Rings.  No dimensional or weight data given; using offsets from tube sizes. -->
        <!-- Up to 4" are 6mm Baltic Birch ply, 5.5" are 9mm ply, and 6.0 and 7.5" sizes are 12mm plywood -->
        <!-- 29mm OD to 38mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-38/29</PartNumber>
            <Description>Centering ring, plywood, 29mm to 38mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.279</InsideDiameter>
            <OutsideDiameter Unit="in">1.515</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 29mm OD to 54mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-54/29</PartNumber>
            <Description>Centering ring, plywood, 29mm to 54mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.279</InsideDiameter>
            <OutsideDiameter Unit="in">2.269</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 38mm OD to 54mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-54/38</PartNumber>
            <Description>Centering ring, plywood, 38mm to 54mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.649</InsideDiameter>
            <OutsideDiameter Unit="in">2.145</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 29mm OD to 63mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-63/29</PartNumber>
            <Description>Centering ring, plywood, 29mm to 63mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.279</InsideDiameter>
            <OutsideDiameter Unit="in">2.555</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 38mm OD to 63mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-63/38</PartNumber>
            <Description>Centering ring, plywood, 38mm to 63mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.649</InsideDiameter>
            <OutsideDiameter Unit="in">2.555</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 29mm OD to 75mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-75/29</PartNumber>
            <Description>Centering ring, plywood, 29mm to 75mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.279</InsideDiameter>
            <OutsideDiameter Unit="in">2.995</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 38mm OD to 75mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-75/38</PartNumber>
            <Description>Centering ring, plywood, 38mm to 75mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.649</InsideDiameter>
            <OutsideDiameter Unit="in">2.995</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 54mm OD to 75mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-75/54</PartNumber>
            <Description>Centering ring, plywood, 54mm to 75mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">2.279</InsideDiameter>
            <OutsideDiameter Unit="in">2.995</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 29mm OD to 98mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-98/29</PartNumber>
            <Description>Centering ring, plywood, 29mm to 98mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.279</InsideDiameter>
            <OutsideDiameter Unit="in">3.895</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 38mm OD to 98mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-98/38</PartNumber>
            <Description>Centering ring, plywood, 38mm to 98mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">1.649</InsideDiameter>
            <OutsideDiameter Unit="in">3.895</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 54mm OD to 98mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-98/54</PartNumber>
            <Description>Centering ring, plywood, 54mm to 98mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">2.279</InsideDiameter>
            <OutsideDiameter Unit="in">3.895</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 75mm OD to 98mm ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-98/75</PartNumber>
            <Description>Centering ring, plywood, 75mm to 98mm, .25"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/4 in. bulk</Material>
            <InsideDiameter Unit="in">3.129</InsideDiameter>
            <OutsideDiameter Unit="in">3.895</OutsideDiameter>
            <Length Unit="in">0.25</Length>
        </CenteringRing>
        <!-- 38mm OD to 5.5" ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-139/38</PartNumber>
            <Description>Centering ring, plywood, 38mm to 5.5", .50"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/2 in. bulk</Material>
            <InsideDiameter Unit="in">1.649</InsideDiameter>
            <OutsideDiameter Unit="in">5.495</OutsideDiameter>
            <Length Unit="in">0.50</Length>
        </CenteringRing>
        <!-- 54mm OD to 5.5" ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-139/54</PartNumber>
            <Description>Centering ring, plywood, 54mm to 5.5", .50"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/2 in. bulk</Material>
            <InsideDiameter Unit="in">2.279</InsideDiameter>
            <OutsideDiameter Unit="in">5.495</OutsideDiameter>
            <Length Unit="in">0.50</Length>
        </CenteringRing>
        <!-- 75mm OD to 5.5" ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-139/75</PartNumber>
            <Description>Centering ring, plywood, 75mm to 5.5", .50"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/2 in. bulk</Material>
            <InsideDiameter Unit="in">3.129</InsideDiameter>
            <OutsideDiameter Unit="in">5.495</OutsideDiameter>
            <Length Unit="in">0.50</Length>
        </CenteringRing>
        <!-- 98mm OD to 5.5" ID -->
        <CenteringRing>
            <Manufacturer>Always Ready Rocketry</Manufacturer>
            <PartNumber>CR-139/98</PartNumber>
            <Description>Centering ring, plywood, 98mm to 5.5", .50"</Description>
            <Material Type="BULK">Plywood, aircraft, 1/2 in. bulk</Material>
            <InsideDiameter Unit="in">4.029</InsideDiameter>
            <OutsideDiameter Unit="in">5.495</OutsideDiameter>
            <Length Unit="in">0.50</Length>
        </CenteringRing>
    </Components>
</OpenRocketComponent>
