<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--

Competition parachutes and streamers database file for OpenRocket

Copyright 2017 by Dave Cook  NAR 21953  caveduck17@gmail.com

See the file LICENSE in this distribution for license information.

This file defines an array of sizes of competition parachutes and streamers in
various thicknesses.

  Mylar parachutes:
      0.25 mil for general duration chutes
      0.50 mil for egglofters and higher power duration events

  Polyethylene (cleaner/produce bag) parachutes:
      0.30 mil for general duration chutes
      0.50 mil for egglofters etc.

  Mylar streamers:
      0.25 mil for recovery streamers for altitude models etc.
      0.50 mil for competition
      1.00 mil for competition, typically with ironed-in folds
      2.00 mil for higher power competition events (E/F/G streamer duration)

  Tracing paper streamers:
      63 gsm weight paper for competition streamers

-->
<OpenRocketComponent>
    <Version>0.1</Version>
    <Materials>

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
           on up.  So we choose here a set of thicknesses 0.30, .40, 0.50, 0.75, and 1.0 mils to cover the
           useful range for model rocket parachutes.  Poly sheet is not used in any higher thicknesses because
           ripstop nylon is far superior.
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
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 0.75 mil, bare</Name>
            <Density>0.0176</Density>
            <Type>SURFACE</Type>
        </Material>
        <Material UnitsOfMeasure="kg/m2">
            <Name>Polyethylene film, HDPE, 1.0 mil, bare</Name>
            <Density>0.0235</Density>
            <Type>SURFACE</Type>
        </Material>

      <!-- SURFACE papers -->

        <!-- Tracing paper is useful for streamers. See wikipedia for typical grammages -->
        <Material UnitsOfMeasure="kg/m2">
          <Name>Paper, tracing, 63 gsm</Name>
          <Density>.063</Density>
          <Type>SURFACE</Type>
        </Material>

      <!-- LINE materials for shroud lines -->

        <!-- Rod wrapping nylon thread (for competition parachutes).
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

      

    </Materials>

    <Components>

      <!-- 0.25 mil mylar parachute, 9" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_9in</PartNumber>
            <Description>Parachute, 9 in., Mylar, 0.25 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">9.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">9.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 12" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_12in</PartNumber>
            <Description>Parachute, 12 in., Mylar, 0.25 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 15" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_15in</PartNumber>
            <Description>Parachute, 15 in., Mylar, 0.25 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">15.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">15.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 18" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_18in</PartNumber>
            <Description>Parachute, 18 in., Mylar, 0.25 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 21" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_21in</PartNumber>
            <Description>Parachute, 21 in., Mylar, 0.25 mil, 8 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">21.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">21.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 24" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_24in</PartNumber>
            <Description>Parachute, 24 in., Mylar, 0.25 mil, 8 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 30" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_30in</PartNumber>
            <Description>Parachute, 30 in., Mylar, 0.25 mil, 12 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">30.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">30.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.25 mil mylar parachute, 36" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.25_36in</PartNumber>
            <Description>Parachute, 36 in., Mylar, 0.25 mil, 12 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.25 mil</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">36.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>


        <!-- 0.5 mil mylar parachute, 9" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_9in</PartNumber>
            <Description>Parachute, 9 in., Mylar, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">9.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">9.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 12" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_12in</PartNumber>
            <Description>Parachute, 12 in., Mylar, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 15" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_15in</PartNumber>
            <Description>Parachute, 15 in., Mylar, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">15.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">15.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 18" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_18in</PartNumber>
            <Description>Parachute, 18 in., Mylar, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 21" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_21in</PartNumber>
            <Description>Parachute, 21 in., Mylar, 0.5 mil, 8 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">21.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">21.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 24" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_24in</PartNumber>
            <Description>Parachute, 24 in., Mylar, 0.5 mil, 8 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 30" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_30in</PartNumber>
            <Description>Parachute, 30 in., Mylar, 0.5 mil, 12 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">30.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">30.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil mylar parachute, 36" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_Mylar_0.5_36in</PartNumber>
            <Description>Parachute, 36 in., Mylar, 0.5 mil, 12 lines</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">36.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>

        
      <!-- 0.3 mil polyethylene parachute, 9" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_9in</PartNumber>
            <Description>Parachute, 9 in., polyethylene, 0.3 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">9.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">9.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 12" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_12in</PartNumber>
            <Description>Parachute, 12 in., polyethylene, 0.3mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 15" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_15in</PartNumber>
            <Description>Parachute, 15 in., polyethylene, 0.3 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">15.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">15.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 18" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_18in</PartNumber>
            <Description>Parachute, 18 in., polyethylene, 0.3 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 21" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_21in</PartNumber>
            <Description>Parachute, 21 in., polyethylene, 0.3 mil, 8 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">21.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">21.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 24" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_24in</PartNumber>
            <Description>Parachute, 24 in., polyethylene, 0.3 mil, 8 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 30" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_30in</PartNumber>
            <Description>Parachute, 30 in., polyethylene, 0.3 mil, 12 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">30.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">30.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.3 mil polyethylene parachute, 36" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.3_36in</PartNumber>
            <Description>Parachute, 36 in., polyethylene, 0.3 mil, 12 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.30 mil, bare</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">36.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>


      <!-- 0.5 mil polyethylene parachute, 9" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_9in</PartNumber>
            <Description>Parachute, 9 in., polyethylene, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">9.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">9.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 12" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_12in</PartNumber>
            <Description>Parachute, 12 in., polyethylene, 0.5mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">12.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">12.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 15" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_15in</PartNumber>
            <Description>Parachute, 15 in., polyethylene, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">15.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">15.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 18" diam, 6 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_18in</PartNumber>
            <Description>Parachute, 18 in., polyethylene, 0.5 mil, 6 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">18.0</Diameter>
            <Sides>6</Sides>
            <LineCount>6</LineCount>
            <LineLength Unit="in">18.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 21" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_21in</PartNumber>
            <Description>Parachute, 21 in., polyethylene, 0.5 mil, 8 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">21.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">21.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 24" diam, 8 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_24in</PartNumber>
            <Description>Parachute, 24 in., polyethylene, 0.5 mil, 8 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">24.0</Diameter>
            <Sides>8</Sides>
            <LineCount>8</LineCount>
            <LineLength Unit="in">24.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 30" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_30in</PartNumber>
            <Description>Parachute, 30 in., polyethylene, 0.5 mil, 12 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">30.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">30.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>
      <!-- 0.5 mil polyethylene parachute, 36" diam, 12 lines -->
        <Parachute>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>chute_poly_0.5_36in</PartNumber>
            <Description>Parachute, 36 in., polyethylene, 0.5 mil, 12 lines</Description>
            <Material Type="SURFACE">Polyethylene film, HDPE, 0.50 mil, bare</Material>
            <Diameter Unit="in">36.0</Diameter>
            <Sides>12</Sides>
            <LineCount>12</LineCount>
            <LineLength Unit="in">36.0</LineLength>
            <LineMaterial Type="LINE">Nylon thread, rod wrapping, size D</LineMaterial>
        </Parachute>


      <!-- streamers - plowing new ground here, none exist in OpenRocket distribution -->

      <!-- 0.5 mil mylar streamer, 4x40 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_0.5_4x40in</PartNumber>
            <Description>Streamer, 4x40 in., mylar, 0.5 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Thickness Unit="in">0.0005</Thickness>
            <Width Unit="in">4.0</Width>
            <Length Unit="in">40.0</Length>
        </Streamer>
      <!-- 0.5 mil mylar streamer, 5x50 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_0.5_5x50in</PartNumber>
            <Description>Streamer, 5x50 in., mylar, 0.5 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Thickness Unit="in">0.0005</Thickness>
            <Width Unit="in">5.0</Width>
            <Length Unit="in">50.0</Length>
        </Streamer>
      <!-- 0.5 mil mylar streamer, 6x60 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_0.5_6x60in</PartNumber>
            <Description>Streamer, 6x60 in., mylar, 0.5 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Thickness Unit="in">0.0005</Thickness>
            <Width Unit="in">6.0</Width>
            <Length Unit="in">60.0</Length>
        </Streamer>
      <!-- 0.5 mil mylar streamer, 7x70 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_0.5_7x70in</PartNumber>
            <Description>Streamer, 7x70 in., mylar, 0.5 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Thickness Unit="in">0.0005</Thickness>
            <Width Unit="in">7.0</Width>
            <Length Unit="in">70.0</Length>
        </Streamer>
      <!-- 0.5 mil mylar streamer, 8x80 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_0.5_8x80in</PartNumber>
            <Description>Streamer, 8x80 in., mylar, 0.5 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 0.5 mil</Material>
            <Thickness Unit="in">0.0005</Thickness>
            <Width Unit="in">8.0</Width>
            <Length Unit="in">80.0</Length>
        </Streamer>
        

      <!-- 1 mil mylar streamer, 4x40 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_1.0_4x40in</PartNumber>
            <Description>Streamer, 4x40 in., mylar, 1.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 1.0 mil</Material>
            <Thickness Unit="in">0.001</Thickness>
            <Width Unit="in">4.0</Width>
            <Length Unit="in">40.0</Length>
        </Streamer>
      <!-- 1 mil mylar streamer, 5x50 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_1.0_5x50in</PartNumber>
            <Description>Streamer, 5x50 in., mylar, 1.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 1.0 mil</Material>
            <Thickness Unit="in">0.001</Thickness>
            <Width Unit="in">5.0</Width>
            <Length Unit="in">50.0</Length>
        </Streamer>
      <!-- 1 mil mylar streamer, 6x60 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_1.0_6x60in</PartNumber>
            <Description>Streamer, 6x60 in., mylar, 1.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 1.0 mil</Material>
            <Thickness Unit="in">0.001</Thickness>
            <Width Unit="in">6.0</Width>
            <Length Unit="in">60.0</Length>
        </Streamer>
      <!-- 1 mil mylar streamer, 7x70 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_1.0_7x70in</PartNumber>
            <Description>Streamer, 7x70 in., mylar, 1.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 1.0 mil</Material>
            <Thickness Unit="in">0.001</Thickness>
            <Width Unit="in">7.0</Width>
            <Length Unit="in">70.0</Length>
        </Streamer>
      <!-- 1 mil mylar streamer, 8x80 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_1.0_8x80in</PartNumber>
            <Description>Streamer, 8x80 in., mylar, 1.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 1.0 mil</Material>
            <Thickness Unit="in">0.001</Thickness>
            <Width Unit="in">8.0</Width>
            <Length Unit="in">80.0</Length>
        </Streamer>


      <!-- 2 mil mylar streamer, 4x40 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_2.0_4x40in</PartNumber>
            <Description>Streamer, 4x40 in., mylar, 2.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 2.0 mil</Material>
            <Thickness Unit="in">0.002</Thickness>
            <Width Unit="in">4.0</Width>
            <Length Unit="in">40.0</Length>
        </Streamer>
      <!-- 2 mil mylar streamer, 5x50 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_2.0_5x50in</PartNumber>
            <Description>Streamer, 5x50 in., mylar, 2.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 2.0 mil</Material>
            <Thickness Unit="in">0.002</Thickness>
            <Width Unit="in">5.0</Width>
            <Length Unit="in">50.0</Length>
        </Streamer>
      <!-- 2 mil mylar streamer, 6x60 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_2.0_6x60in</PartNumber>
            <Description>Streamer, 6x60 in., mylar, 2.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 2.0 mil</Material>
            <Thickness Unit="in">0.002</Thickness>
            <Width Unit="in">6.0</Width>
            <Length Unit="in">60.0</Length>
        </Streamer>
      <!--2 mil mylar streamer, 7x70 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_2.0_7x70in</PartNumber>
            <Description>Streamer, 7x70 in., mylar, 2.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 2.0 mil</Material>
            <Thickness Unit="in">0.002</Thickness>
            <Width Unit="in">7.0</Width>
            <Length Unit="in">70.0</Length>
        </Streamer>
      <!-- 2 mil mylar streamer, 8x80 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_mylar_2.0_8x80in</PartNumber>
            <Description>Streamer, 8x80 in., mylar, 2.0 mil</Description>
            <Material Type="SURFACE">Mylar polyester film, 2.0 mil</Material>
            <Thickness Unit="in">0.002</Thickness>
            <Width Unit="in">8.0</Width>
            <Length Unit="in">80.0</Length>
        </Streamer>

      <!-- tracing paper streamer, 4x40 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_paper_63gsm_4x40in</PartNumber>
            <Description>Streamer, 4x40 in., tracing paper, 63gsm</Description>
            <Material Type="SURFACE">Paper, tracing, 63 gsm</Material>
            <Thickness Unit="in">0.003</Thickness>
            <Width Unit="in">4.0</Width>
            <Length Unit="in">40.0</Length>
        </Streamer>
      <!-- tracing paper streamer, 5x50 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_paper_63gsm_5x50in</PartNumber>
            <Description>Streamer, 5x50 in., tracing paper, 63gsm</Description>
            <Material Type="SURFACE">Paper, tracing, 63 gsm</Material>
            <Thickness Unit="in">0.003</Thickness>
            <Width Unit="in">5.0</Width>
            <Length Unit="in">50.0</Length>
        </Streamer>
      <!-- tracing paper streamer, 6x60 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_paper_63gsm_6x60in</PartNumber>
            <Description>Streamer, 6x60 in., tracing paper, 63gsm</Description>
            <Material Type="SURFACE">Paper, tracing, 63 gsm</Material>
            <Thickness Unit="in">0.003</Thickness>
            <Width Unit="in">6.0</Width>
            <Length Unit="in">60.0</Length>
        </Streamer>
      <!--tracing paper streamer, 7x70 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_paper_63gsm_7x70in</PartNumber>
            <Description>Streamer, 7x70 in., tracing paper, 63 gsm</Description>
            <Material Type="SURFACE">Paper, tracing, 63 gsm</Material>
            <Thickness Unit="in">0.003</Thickness>
            <Width Unit="in">7.0</Width>
            <Length Unit="in">70.0</Length>
        </Streamer>
      <!-- tracing paper streamer, 8x80 in -->
        <Streamer>
            <Manufacturer>Generic competition</Manufacturer>
            <PartNumber>strm_paper_63gsm_8x80in</PartNumber>
            <Description>Streamer, 8x80 in., tracing paper, 63 gsm</Description>
            <Material Type="SURFACE">Paper, tracing, 63 gsm</Material>
            <Thickness Unit="in">0.003</Thickness>
            <Width Unit="in">8.0</Width>
            <Length Unit="in">80.0</Length>
        </Streamer>

    </Components>
    
</OpenRocketComponent>
