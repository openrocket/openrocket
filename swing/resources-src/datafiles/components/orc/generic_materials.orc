<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- 

Generic materials file for OpenRocket

Copyright 2014-2018 by Dave Cook  NAR 21953  caveduck17@gmail.com

This is a much more curated materials file for OpenRocket.

*** This file is not actually read or processed by OpenRocket ***
It is here as a reference when editing the individual .orc parts database files.
You must insert the material definitions used in any .orc directly into that .orc file.

Improvements
* Blue tube densities from actual measurements
* Paper densities from authoritative sources
* Excess significant figures removed
* Many materials added

NOTE: Many of the materials in the original OR component files have incorrect UnitsOfMeasure
attributes.  This seems to have no effect; looks like everything is actually kg/m3

-->

<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>
      
      <!-- ===================================== -->
      <!-- BULK materials (volumetric) -->
      <!-- ===================================== -->

      <!-- BULK paper -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Poster board, 6oz and 2oz nominal</Name>
            <Density>798.85</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, spiral kraft glassine, bulk</Name>
            <Density>798.85</Density>
            <Type>BULK</Type>
        </Material>
        <!-- computed avg density of Estes BT-5 thru BT-101 based on catalog dimensions
        and masses, from my density analysis spreadsheet.  There is a lot of scatter in
        these; we need some actual weighings on a good scale -->
        <Material UnitsOfMeasure="kg/m3">
            <Name>Paper, spiral kraft glassine, Estes avg, bulk</Name>
            <Density>894.4</Density>
            <Type>BULK</Type>
        </Material>

        <!-- Blue tube value is the average of 7 full length 48" tubes in 38mm to 98mm
             range.  The standard deviation is 23.1 or just about 2%.  The original
             OpenRocket values were way too high (1237 and 1250 kg/m3).  The latter value
             is actually present in the Rocksim files - seemingly from ARR ca. 2010 - in
             the openrocket-decal project on GitHub.  It is definitely wrong, at least for
             tubes sold in 2013-2016.
             
             Blue tube measured sizes: (move this to bluetube.orc)
             BlueTube-38mm  OD 1.632      ID 1.510
             BlueTube-2.6   OD 2.66-2.67, ID 2.545
             Measured lengths of nominal 48" tubes ranges from 48.0" to 48 3/16"
        -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Blue Tube, bulk</Name>
            <Density>1162.0</Density>
            <Type>BULK</Type>
        </Material>
        
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

        <!-- Paper product densities, many from paperonweb.com/density.htm -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, tag, 0.53 g/cm3, bulk</Name>
            <Density>530.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, corrugated medium, 0.61 g/cm3, bulk</Name>
            <Density>610.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, bond, 0.75 g/cm3, bulk</Name>
            <Density>750.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, bleached kraft, 0.83 g/cm3, bulk</Name>
            <Density>830.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, cover, 0.92 g/cm3, bulk</Name>
            <Density>920.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, coated, 1.13 g/cm3, bulk</Name>
            <Density>1130.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, super calendared glassine, 1.30 g/cm3, bulk</Name>
            <Density>1300.0</Density>
            <Type>BULK</Type>
        </Material>

        <Material UnitsOfMeasure="g/cm3">
            <Name>Cardboard, 0.689 g/cm3, bulk</Name>
            <Density>689.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, mailing tube, 0.8 g/cm3, bulk</Name>
            <Density>800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, mat board, 4-ply, 0.71 g/cm3, bulk</Name>
            <Density>710.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, spiral/glassine tubing, thick wall, .769 g/cm3, bulk</Name>
            <Density>769.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Paper, spiral/glassine tubing, thin wall, .849 g/cm3, bulk</Name>
            <Density>849.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- This is from the OpenRocket semroc file and represents material used for
             spiral wound centering rings -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, bulk</Name>
            <Density>657.0</Density>
            <Type>BULK</Type>
        </Material>
        
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiber, vulcanized, bulk</Name>
            <Density>1250.0</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK woods -->
      
        <!-- what the heck was "rocketwood"? -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Rocketwood, bulk</Name>
            <Density>529.1</Density>
            <Type>BULK</Type>
        </Material>

        <Material UnitsOfMeasure="g/cm3">
          <Name>Ash, bulk</Name>
          <Density>680.8</Density>
          <Type>BULK</Type>
        </Material>

        <!-- Balsa seen from rocketry vendors can range from 5-10 lb/ft3.
             The average inferred from various manufacturer's weight data is around
             7-8 lb/ft3 -->
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 5 lb/ft3</Name>
          <Density>80.0</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 6 lb/ft3</Name>
          <Density>96.0</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 7 lb/ft3</Name>
          <Density>112.0</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 8 lb/ft3</Name>
          <Density>128.1</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Balsa, bulk, 10 lb/ft3</Name>
          <Density>160.0</Density>
          <Type>BULK</Type>
        </Material>
        
        <Material UnitsOfMeasure="g/cm3">
          <Name>Basswood, bulk</Name>
          <Density>424.5</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Beech, bulk</Name>
          <Density>720.8</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Birch, bulk</Name>
          <Density>680.8</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Cork, bulk</Name>
          <Density>240.0</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
          <Name>Cottonwood, bulk</Name>
          <Density>416.0</Density>
          <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fir, Douglas, bulk</Name>
            <Density>560.6</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fir, White, bulk</Name>
            <Density>400.5</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Maple, hard, bulk</Name>
            <Density>632.7</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Oak, brown/red, bulk</Name>
            <Density>721.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Oak, white, bulk</Name>
            <Density>753.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Pine, white northern, bulk</Name>
            <Density>401.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Pine, white western, bulk</Name>
            <Density>432.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Poplar, yellow, bulk</Name>
            <Density>481.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Spruce, bulk</Name>
            <Density>448.5</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Sycamore, bulk</Name>
            <Density>560.6</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK plywoods -->

        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 1/16 in. bulk</Name>
            <Density>361.3</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 1/8 in. bulk</Name>
            <Density>337.5</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 1/4 in. bulk</Name>
            <Density>344.3</Density>
            <Type>BULK</Type>
       </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 3/16 in. bulk</Name>
            <Density>344.3</Density>
            <Type>BULK</Type>
        </Material>
        <!-- Note large disagreement with previous value for 1/8 aircraft ply -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, aircraft, 1/8 in. bulk (HI DENSITY)</Name>
            <Density>672.8</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, birch, Revell 1/8 in. bulk</Name>
            <Density>640.74</Density>
            <Type>BULK</Type>
        </Material>
        <!-- ***NOTE discrepancy with previous value for Revell 1/8in bulk *** -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, birch, Revell generic, bulk</Name>
            <Density>656.757</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, birch, LOC type, bulk</Name>
            <Density>725.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, light, bulk</Name>
            <Density>352.4</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Plywood, Russian, 1/8 in, bulk</Name>
            <Density>685.1</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK metals -->
        <!-- ***See if this is correct value for 6061*** -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Aluminum, generic, bulk</Name>
            <Density>2698.9</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Aluminum, 2024, bulk</Name>
            <Density>2780.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Aluminum, 7075, bulk</Name>
            <Density>2810.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Brass, bulk</Name>
            <Density>8553.9</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Copper, cast, bulk</Name>
            <Density>8682.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Copper, rolled, bulk</Name>
            <Density>8906.3</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Gold, 24 kt, bulk</Name>
            <Density>19286.2</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Iron, bulk</Name>
            <Density>7850.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Lead, bulk</Name>
            <Density>11340.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Magnesium, bulk</Name>
            <Density>1738.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, 200, bulk</Name>
            <Density>8890.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, 400, bulk</Name>
            <Density>8800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, 600, bulk</Name>
            <Density>8410.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, 625, bulk</Name>
            <Density>8440.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, 718, bulk</Name>
            <Density>8230.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nickel, C276, bulk</Name>
            <Density>8890.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Silver, bulk</Name>
            <Density>10490.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- *** These are not the most common SS varieties, need 316 etc. -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Stainless steel, 17-4PH, bulk</Name>
            <Density>7600.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Stainless steel, 17-5PH, bulk</Name>
            <Density>7800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Stainless steel, 17-7PH, bulk</Name>
            <Density>7800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Steel, generic rolled, bulk</Name>
            <Density>7850.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Steel, 4130, bulk</Name>
            <Density>7850.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Steel, 4340, bulk</Name>
            <Density>7850.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Titanium, bulk</Name>
            <Density>4500.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Zinc, bulk</Name>
            <Density>7135.0</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK plastics -->
      <!-- ***Add 3D print materials:  PLA, ABS, Nylon, HIPS -->

        <Material UnitsOfMeasure="g/cm3">
            <Name>Cellulose acetate propionate, bulk</Name>
            <Density>1199.8</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Lexan, cast/extruded, bulk</Name>
            <Density>1218.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- Mylar (polyester) density quoted by DuPont as 1.39 g/cc -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Mylar, bulk</Name>
            <Density>1390.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Nylon, bulk</Name>
            <Density>1140.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>PVC, bulk</Name>
            <Density>1301.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polycarbonate, cast/extruded, bulk</Name>
            <Density>1200.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- HDPE poly range given as 0.941 to 0.965 g/cm3 on usplastics.com -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polyethylene, HDPE, bulk</Name>
            <Density>950.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- LDPE poly range given as 0.910 to 0.940 g/cm3 on usplastics.com and wikipedia -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polyethylene, LDPE, bulk</Name>
            <Density>925.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Polystyrene, cast, bulk</Name>
            <Density>1050.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Polypropylene, bulk</Name>
            <Density>946.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Urethane, (ethyl carbamate) bulk</Name>
            <Density>847.06</Density>
            <Type>BULK</Type>
        </Material>
      
      <!-- BULK resins -->

        <Material UnitsOfMeasure="g/cm3">
            <Name>Acrylic, cast, bulk</Name>
            <Density>1185.4</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Epoxy, West System 105/205, cured, bulk</Name>
            <Density>1180.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Epoxy, West System 105/206, cured, bulk</Name>
            <Density>1180.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Epoxy, West System 105/207, cured, bulk</Name>
            <Density>1150.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Epoxy, Cotronics 4525, cured, bulk</Name>
            <Density>1900.0</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK composites -->

        <!-- This is density of carbon fiber itself, without resin -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Carbon fiber, bulk</Name>
            <Density>1740.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- Standard density of properly made carbon fiber composite -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Carbon fiber epoxy composite, bulk</Name>
            <Density>1600.0</Density>
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
        
        <Material UnitsOfMeasure="g/cm3">
            <Name>Kevlar epoxy composite, bulk</Name>
            <Density>1400.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- E-glass composite usually quoted at 1.90 -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, generic, bulk</Name>
            <Density>1900.0</Density>
            <Type>BULK</Type>
        </Material>
        <!-- G10 is glass cloth + epoxy, usually in convolute wound tubes -->
        <!-- G12 is filament wound -->
        <!-- G10 is quoted on MatWeb.com as 1.80 g/cc -->
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, G10, bulk</Name>
            <Density>1800.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, G12, filament wound generic, bulk</Name>
            <Density>1934.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, G12, filament wound tube, bulk</Name>
            <Density>1820.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, honeycomb, 0.125in, bulk</Name>
            <Density>461.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Fiberglass, honeycomb, 0.25in, bulk</Name>
            <Density>235.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, bulk</Name>
            <Density>1905.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, glassed, bulk</Name>
            <Density>1900.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, kraft, bulk</Name>
            <Density>943.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, kraft, glassed, bulk</Name>
            <Density>1153.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Phenolic, Magna tube type, bulk</Name>
            <Density>1100.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Quantum tubing, PML, bulk</Name>
            <Density>1100.0</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK misc solids -->
      
        <Material UnitsOfMeasure="g/cm3">
            <Name>Chalk, powdered, fine</Name>
            <Density>1121.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Glass, window, bulk</Name>
            <Density>2579.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="g/cm3">
            <Name>Graphite, 2.2 gm/cm3, bulk</Name>
            <Density>2200.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Leather, bulk</Name>
            <Density>945.0</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m3">
            <Name>Sand, dry, bulk</Name>
            <Density>1602.0</Density>
            <Type>BULK</Type>
        </Material>

      <!-- BULK liquids -->
      
        <Material UnitsOfMeasure="kg/m3">
            <Name>Water, bulk</Name>
            <Density>>1000.0</Density>
            <Type>BULK</Type>
        </Material>

        <!-- ***TBD*** add liquid propellants: LOX, kerosene, LH2, RFNA -->

      <!-- BULK gases -->
      
        <!-- ***TBD*** add compressed gases incl NO, N, O, He, H -->

      <!-- ===================================== -->
      <!-- LINE materials (mass per linear unit) -->
      <!-- ===================================== -->

      <!-- Flat Elastic -->
      
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, flat, 1/8 in. width</Name>
            <Density>0.00205</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, flat, 1/4 in. width</Name>
            <Density>0.00402</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/cm3">
            <Name>Elastic, flat, 3/8 in. width</Name>
            <Density>0.006087</Density>
            <Type>BULK</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, flat, 1/2 in. width</Name>
            <Density>0.008</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, flat, 3/4 in. width</Name>
            <Density>0.01227</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, flat, 1.0 in width</Name>
            <Density>0.01551</Density>
            <Type>LINE</Type>
        </Material>
        
      <!-- Round/Oval Elastic -->
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, round, 1/16 in. dia.</Name>
            <Density>0.00183</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Elastic, round, 5/64 in. dia.</Name>
            <Density>0.002420</Density>
            <Type>LINE</Type>
        </Material>
        
      <!-- Kevlar Line -->

        <!-- NOTE - many kevlar line listings in the built-in OpenRocket files are of unknown
             origin, and some had obviously discrepant values, so they have been removed in favor
             of well documented suppliers.
        -->

        <!--
            thethreadexchange.com gives diam and weight for many sizes of monocord/bonded thread
        -->
        <!-- size 15 monocord, 2 oz = 1969 yd, diam 0.1 mm = 4 mil, 4 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 15, 0.1 mm dia, 4 lb. test</Name>
            <Density>3.15E-5</Density>
            <Type>LINE</Type>
        </Material>
        
        <!-- size 23 bonded, 4 oz = 6250 yd, diam 0.15 mm = 6 mil (lighter than size 15), 6 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 23, 0.15 mm dia, 6 lb. test</Name>
            <Density>1.98E-5</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 46 monocord, 2 oz = 1250 yd, diam 0.21 mm = 8 mil, 14 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 46, 0.21 mm dia, 14 lb. test</Name>
            <Density>4.96E-5</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 69 bonded, 4 oz = 1672 yd, diam 0.25 mm = 10 mil, 23 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 69, 0.25 mm dia, 23 lb. test</Name>
            <Density>7.42E-5</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 92 bonded, 4 oz = 1250 yd, diam 0.29 mm = 11.4 mil, 30 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 92, 0.29 mm dia, 30 lb. test</Name>
            <Density>9.92E-5</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 138 bonded, 4 oz = 836 yd, diam 0.36 mm = 14 mil, 45 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 138, 0.36 mm dia, 45 lb. test</Name>
            <Density>1.48E-4</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 207 bonded, 4 oz = 524 yd, diam 0.46 mm = 18 mil, 64 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 207, 0.46 mm dia, 64 lb. test</Name>
            <Density>2.37E-4</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 346 bonded, 2 oz = 132 yd, diam 0.65 mm = 26 mil, 135 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 346, 0.65 mm dia, 135 lb. test</Name>
            <Density>4.70E-4</Density>
            <Type>LINE</Type>
        </Material>

        <!-- size 415 bonded, 8 oz = 450 yd, diam 0.82 mm = 27 mil, 150 lb test -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar thread, size 415, 0.82 mm dia, 150 lb. test</Name>
            <Density>5.51E-4</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Note: I did not list thread exchange size 800 (225 lb) as it is the soft (non-bonded) type,
             which is not suitable as shock cord or shroud line -->

        <!-- end of thethreadexchange listings -->


        <!--
            emmakites.com - braided types only, twisted types not listed
            Diameters only given for some sizes
        -->

        <!-- 100 lb braided, 95 gm = 1000 ft -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 100 lb test, Emmakites</Name>
            <Density>0.000312</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 150 lb braided, 165 gm = 1000 ft -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 150 lb test, Emmakites</Name>
            <Density>0.000541</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 250 lb braided, 235 gm = 1000 ft -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 250 lb test, Emmakites</Name>
            <Density>0.000771</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 380 lb braided, 365 gm = 1000 ft, 1.3 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 380 lb test, 1.3 mm dia, Emmakites</Name>
            <Density>.00120</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 500 lb braided, 260 gm = 500 ft, 1.5 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 500 lb test, 1.5 mm dia, Emmakites</Name>
            <Density>.00171</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 750 lb braided, 357 gm = 500 ft, 2.0 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 750 lb test, 2.0 mm dia, Emmakites</Name>
            <Density>.00234</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 1000 lb braided, 540 gm = 500 ft, 2.5 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 1000 lb test, 2.5 mm dia, Emmakites</Name>
            <Density>.00354</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 1500 lb braided, 820 gm = 500 ft, 2.8 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 1500 lb test, 2.8 mm dia, Emmakites</Name>
            <Density>.00538</Density>
            <Type>LINE</Type>
        </Material>

        <!-- 2000 lb braided, 1100 gm = 500 ft, 3.0 mm dia -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar line, braided, 2000 lb test, 3.0 mm dia, Emmakites</Name>
            <Density>.00722</Density>
            <Type>LINE</Type>
        </Material>

        <!-- end of emmakites.com listings -->

        <!-- Apogee 29502 from original OpenRocket file is no longer listed on the Apogee site.
             Judging from density it was probably in the 25-30 lb range.  The current Apogee
             listings for 100, 300 and 1500 line agree well with the Emmakites values -->
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar cord, 30 lb test, Apogee 29502</Name>
            <Density>8.858E-5</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Formerly Apogee 29505 -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar cord, 100 lb test, Apogee 30325</Name>
            <Density>.000371</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Formerly Apogee 29506 -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar cord, 300 lb test, Apogee 30326</Name>
            <Density>.00102</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Formerly Apogee 29507 -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar cord, 1500 lb test, Apogee 30327</Name>
            <Density>.00661</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Data on larger Kevlar ropes has to be obtained from upstream mfrs.
             Pelicanrope.com has the following table for 12-strand single braid, which we
             adopt for 1/8" through 1" sizes:

             Size   Strength  Weight (lb/100ft)
             1/8    2100       .65
             5/32   2700       .76
             3/16   5500      1.2
             1/4    9500      2.0
             5/16   11700     3.0
             3/8    17800     4.0
             7/16   21200     5.0
             1/2    31000     7.8
             9/16   35000    14.0
             5/8    39000    19.3
             3/4    50000    23.6
             1"     78000    30.7

             Some of these sizes are sold online with identical specs by USNetting.com
             You'd think that 20,000+ lb strength would not be needed, but onebadhawk.com sells
             kevlar recovery harnesses up to 1", so I list them.
        -->

        <!-- 12-strand single braid kevlar rope from Pelican Rope -->
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 2100 lb test, 1/8 in.</Name>
            <Density>0.00967</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 2700 lb test, 5/32 in.</Name>
            <Density>0.0113</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 5500 lb test, 3/16 in.</Name>
            <Density>0.0178</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 9500 lb test, 1/4 in.</Name>
            <Density>0.0298</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 11700 lb test, 5/16 in.</Name>
            <Density>0.0447</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 17800 lb test, 3/8 in.</Name>
            <Density>0.0595</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 21200 lb test, 7/16 in.</Name>
            <Density>0.0744</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 31000 lb test, 1/2 in.</Name>
            <Density>0.116</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 35000 lb test, 9/16 in.</Name>
            <Density>0.208</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 39000 lb test, 5/8 in.</Name>
            <Density>0.287</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 50000 lb test, 3/4 in.</Name>
            <Density>0.351</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar rope, braided, 78000 lb test, 1 in.</Name>
            <Density>0.457</Density>
            <Type>LINE</Type>
        </Material>

        <!-- end Pelican Rope listings -->

        <!-- Flat braided Kevlar webbing (not rope) -->
        <!-- ***TBD*** Add other widths, esp. 1 inch.  Others are not commonly available; if you
             look at sourcing kevlar flat web you end up with Chinese suppliers on Alibaba pretty quick. -->

        <Material UnitsOfMeasure="kg/m">
            <Name>Kevlar Cord, flat braided, 7/16 in. width</Name>
            <Density>0.05023</Density>
            <Type>LINE</Type>
        </Material>

        
        <!-- Tubular Nylon Cord / webbing.  MIL-W-5625 defines 3/8 through 1" sizes, and gives the following
             properties: (note the progression is not completely linear due to construction details)
             These are the values we use.

             Size    oz/yd (max)       strength (min)
             3/8     0.40              950 lb
             1/2     0.50              1000 lb
             9/16    0.60              1500 lb
             5/8     0.75              2250 lb
             3/4     1.05              2300 lb
             7/8     1.00              3100 lb
             1.0     1.70              4000 lb

             Note that 3/8 and 7/8 tubular are not typically available from parachute suppliers
        -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 3/8 in., 950 lb</Name>
            <Density>0.0124</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 1/2 in., 1000 lb</Name>
            <Density>0.0155</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 9/16 in., 1500 lb</Name>
            <Density>0.0186</Density>
            <Type>LINE</Type>
        </Material>
        <!-- REI gives mass of 8.2 gm/ft = .0269 kg/m, good agreement though slightly over mil spec mass -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 5/8 in., 1850 lb</Name>
            <Density>0.0232</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 3/4 in., 2300 lb</Name>
            <Density>0.0325</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 7/8 in., 3100 lb</Name>
            <Density>0.0310</Density>
            <Type>LINE</Type>
        </Material>
        <!-- REI gives mass of 12.8 gm/ft = .0420 kg/m, we use this.  MIL-W-5625 max is 0.0527 kg/m -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Cord, tubular, 1.0 in., 4000 lb</Name>
            <Density>0.0420</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Braided round nylon (Paracord).  This is complicated. -->
        <!-- Paracord strength/sizes from paracordplanet.com
             36  lb  0.75mm  "nano"
             90  lb  1.18mm  "micro"
             95  lb  "1/14" diameter = 1.8 mm
             110 lb  1/16" diameter = 1.6 mm "mini"
             275 lb  2.38mm
             425 lb  1/8" = 3mm
             550 lb  4mm

             There is a good chart of braid sizes, strength and weight for round nylon cord
             on franklinbraid.com:
             diam     strength  lb/1000ft   g/m
             =====    ========  =========   ======
             3/32     233 lb    2.03        3.02
             7/64     325       2.80        4.17
             1/8      514       3.85        5.73
             9/64     580       4.59        6.83
             5/32     667       5.27        7.84
             11/64    835       6.56        9.76
             3/16     857       8.03       11.95           (almost certainly 957 lb, not 857)
             13/64    1067      8.85       13.2
             7/32     1307      10.78      16.0
             1/4      1565      12.45      18.5
             9/32     2100      15.35      22.8
             5/16     2857      24.94      37.1
             
            Some useful nylon cord specs from MIL-C-5040H (see Wikipedia)
            Note that diameters are not specified.  Paracord Planet gives diameters for some
            of the cords they sell.  Mass per length from the Wikipedia chart:
             
               Type I   95 lb   max 1.57 g/m    1 core
               Type IA  100 lb  max 1.42 g/m    coreless
               Type IIA 225 lb  max 3.00 g/m    coreless
               Type II  400 lb  max 5.62 g/m    4-7 cores
               Type III 550 lb  max 6.61 g/m    7-9 cores   diam typ. 4 mm

            The MIL-C-5040H and Franklin Braid tables line up very well.

            The following paracord materials are based on interpolating the Franklin Braid
            table, with Paracord Planet values used at the low end.
        -->

        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 90 lb, 1.18 mm dia.</Name>
            <Density>0.00145</Density>
            <Type>LINE</Type>
        </Material>
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 110 lb, 1/16 in. dia.</Name>
            <Density>0.00160</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 275 lb, 2.38 mm dia.</Name>
            <Density>0.00350</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 425 lb, 3 mm dia.</Name>
            <Density>0.00562</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon Paracord, 550 lb, 4 mm dia.</Name>
            <Density>0.00661</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Flat braided nylon, estimated from 1 size lower round -->
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon cord, flat braid, 325 lb, 1/8 in.</Name>
            <Density>0.00417</Density>
            <Type>LINE</Type>
        </Material>

        
        
        <!-- Rubber shock cord -->
        
        <Material UnitsOfMeasure="kg/m">
            <Name>Rubber Cord, flat, 1/16 in. wide</Name>
            <Density>0.00115</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Rubber Cord, flat, 1/8 in. wide</Name>
            <Density>0.00231</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Rubber Cord, flat, 3/16 in. wide</Name>
            <Density>0.00346</Density>
            <Type>LINE</Type>
        </Material>

        <Material UnitsOfMeasure="kg/m">
            <Name>Rubber Cord, flat, 1/4 in. wide</Name>
            <Density>0.00462</Density>
            <Type>LINE</Type>
        </Material>

        <!-- Apogee PN 30330 (mfr Sunward) is 3/8" flat rubber, mass 9.1 gm / 6 ft -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Rubber Cord, flat, 3/8 in. wide, Apogee 30330</Name>
            <Density>0.00498</Density>
            <Type>LINE</Type>
        </Material>

        

        <!-- String / thread -->
        
        <!-- Carpet thread with this value was formerly Apogee 29500 -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Carpet Thread</Name>
            <Density>3.3E-4</Density>
            <Type>LINE</Type>
        </Material>
        
        <!-- Rod wraapping nylon thread (for competition parachutes).
             Sizes exist(ed) 00, A, B, C, D, E, EE.  Most mfrs only make A and D.  ProWrap gives diameters:
                 A   0.006" diameter
                 D   0.010" diameter
             I am giving A and D size with weights computed assuming a nylon monofilament construction.
             Should be close enough given it takes many meters to weigh 1 gram.
         -->
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon thread, rod wrapping, size A</Name>
            <Density>2.075E-5</Density>
            <Type>LINE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m">
            <Name>Nylon thread, rod wrapping, size D</Name>
            <Density>5.77E-5</Density>
            <Type>LINE</Type>
        </Material>

        

      <!-- SURFACE materials (mass per area unit) -->
      <!-- The only types of components that have built-in use of SURFACE materials are parachute and streamer
           Thus no need to define anything here that is not useful as a recovery device material.
      -->

      <!-- SURFACE plastics -->
      
      <!-- polyester (mylar) film -->
      <!-- DuPont quotes the bulk density of Mylar as 1.39 g/cc or 1390 kg/m3 -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Mylar polyester film, 0.25 mil</Name>
            <Density>0.00834</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Mylar polyester film, 0.50 mil</Name>
            <Density>0.0167</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Mylar polyester film, 0.75 mil</Name>
            <Density>0.0250</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Mylar polyester film, 1.0 mil</Name>
            <Density>0.0334</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Mylar polyester film, 2.0 mil</Name>
            <Density>0.0668</Density>
            <Type>SURFACE</Type>
        </Material>
      
      <!-- HDPE bag / tube film
           Atlanticpoly.com indicates that HDPE film can be had in 1-micron increments from 5 microns (.19 mil)
           on up.  So we choose here a set of thicknesses 0.30, .40, 0.50, 0.75, 1.0, 1.75 and 2.0 mils to cover the
           useful range for model rocket parachutes and streamers.  Poly sheet is not used in any higher thicknesses because ripstop nylon is far superior.

           As of 2018, clear poly dry clean bags are commonly available in 0.8 and 1.0 mil thicknesses.
           Estes printed poly parachutes at this date are now 1.75 mils thick.
      -->

      <!-- Bare poly film (cleaner bag type) -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.30 mil, bare</Name>
            <Density>0.00705</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.40 mil, bare</Name>
            <Density>0.00940</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.50 mil, bare</Name>
            <Density>0.0117</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- legacy Estes parachutes ca. 1970s to 1980s were about 3/4 mil -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.75 mil, bare</Name>
            <Density>0.0176</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- dry clean bags are commonly available in 0.8 mil -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.80 mil, bare</Name>
            <Density>0.0188</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- dry clean bags are commonly available in 1.0 mil -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 1.0 mil, bare</Name>
            <Density>0.0235</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- Estes typical parachutes ca. 2018 are 1.75 mil -->
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 1.75 mil, bare</Name>
            <Density>0.0411</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 2.0 mil, bare</Name>
            <Density>0.0470</Density>
            <Type>SURFACE</Type>
        </Material>

      <!-- Fully printed film (Estes parachute type) -->
      <!--
          TRF user micromeister reported a complete Estes 18" chute with #10 snap swivel weighs 7.4 g
          TRF user gpoehlein reports 4 layers of an Estes 12" chute are .005" or .00125" per layer
          Apogee says on their website that standard model rocket parachutes are 1 mil thick.
          Apogee also sells a very large (32") printed polyethylene parachute that is quite heavy.
          It has been reported that Estes was forced to make their parachute film thicker for
          anti-suffocation properties and that it is now (2018) about 1.0 mil.
      -->

      <!-- SURFACE papers -->

      <!-- Tracing paper is useful for streamers. See wikipedia for typical grammages -->
      <!-- lighter weight tracing paper is about 3 mils thick; thickness usually not precisely specified -->
      <Material UnitsOfMeasure="kg/m2">
        <Name>Paper, tracing, 63 gsm</Name>
        <Density>.063</Density>
        <Type>SURFACE</Type>
      </Material>
      
      
      <!-- SURFACE fabrics -->

      <!-- ripstop nylon parachute / streamer material -->
      <!-- weight may vary significantly from nominal fabric weight due to coatings -->
      
        <!-- 1.1 oz is Top Flight thin mill material -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.1 oz actual</Name>
            <Density>0.0341</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.3 oz actual</Name>
            <Density>0.0403</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- 1.7oz ripstop nylon is Top Flight and LOC standard parachute material -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.7 oz actual</Name>
            <Density>0.0527</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- 1.9oz ripstop nylon is LOC (2018) and Sky Angle standard parachute material.
             LOC material is stated to be urethane coated. -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.9 oz actual</Name>
            <Density>0.0589</Density>
            <Type>SURFACE</Type>
        </Material>
        <!-- This "1.9 oz" nylon is heavier, maybe silicone-coated non-porous like B2 chutes -->
        <Material UnitsOfMeasure="g/m2">
            <Name>Nylon fabric, ripstop, 1.9 oz nominal, PML type</Name>
            <Density>0.08788</Density>
            <Type>SURFACE</Type>
        </Material>

      <!-- ***TODO*** model airplane iron-on coverings used for competition streamers
           Coverite Micafilm was formerly very popular but has not been made for at least a decade.
           Coverite "Microlite" is 20.3 gm / m2
           Coverite "Coverlite" is 28 g/m2
           Coverite Black Baron polypropylene film is 57 g/m2
           Polyspan (mfr ??) is a non-woven polyester fabric now popular as airplane covering
           
           Not clear whether any of the above are useful competition streamer materials.
      -->

    </Materials>
    <Components>
    </Components>
</OpenRocketComponent>
