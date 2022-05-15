<?xml version="1.0" encoding="UTF-8" standalone="yes"?>					
					
<!--	Spherachutes [http://spherachutes.com]				
	parachute database file for OpenRocket				
					
	Data for this file was derived from information provided by 				
	Julie Aanonson, Owner/Operator of Spherachutes				
					
	Copyright 2022 by H. Craig Miller  NAR #89750  TRA #09190  hcraigmiller@gmail.com				
					
	See the file LICENSE in this distribution for license information.				
					
	This file defines an array of sizes of rocketry parachute recovery systems of various weight capacities.				
-->					
					
<OpenRocketComponent>					
					
	<Version>0.1</Version>				
					
	<Materials>				
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Name>		
			<Density>0.020462254</Density>		
			<Thickness Unit="in">.001</Thickness>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop Nylon, 1.9 oz 5 mil</Name>		
			<Density>0.058906483</Density>		
			<Thickness Unit="in">.005</Thickness>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>0555 Nylon webbing #440 [flat 1/2 x 3/64 in (12.7 x 1 mm)]</Name>		
			<Density>0.004090114</Density>		
			<Thickness Unit="in">.5</Thickness>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</Name>		
			<Density>0.00142</Density>		
			<Thickness Unit="in">.0625</Thickness>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</Name>		
			<Density>0.003007331</Density>		
			<Thickness Unit="in">.0988</Thickness>		
			<Type>LINE</Type>		
		 </Material>			
					
	</Materials>				
					
	<Components>				
					
	<!-- 	Spherachutes			
		Apex Drogue Parachute			
	-->				
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>AD-12-SM</PartNumber>		
			<Description>Apex Drouge Parachute [Cd .61 (1.2 oz) 7 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="oz">1.2</Mass>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>.61</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">12</LineLength>		
			<LineMaterial Type="LINE">0555 Nylon webbing #440 [flat 1/2 x 3/64 in (12.7 x 1 mm)]</LineMaterial>		
			<PackedDiameter Unit="in">1.8</PackedDiameter>		
			<PackedLength Unit="in">2.75</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>AD-18-MD</PartNumber>		
			<Description>Apex Drouge Parachute [Cd .61 (1.9 oz) 18 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="oz">1.9</Mass>		
			<Diameter Unit="in">18</Diameter>		
			<DragCoefficient>.61</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">18</LineLength>		
			<LineMaterial Type="LINE">0555 Nylon webbing #440 [flat 1/2 x 3/64 in (12.7 x 1 mm)]</LineMaterial>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">3.65</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>AD-24-LG</PartNumber>		
			<Description>Apex Drouge Parachute [Cd .61 (2.8 oz) 24.5 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="oz">2.8</Mass>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.61</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">0555 Nylon webbing #440 [flat 1/2 x 3/64 in (12.7 x 1 mm)]</LineMaterial>		
			<PackedDiameter Unit="in">2.75</PackedDiameter>		
			<PackedLength Unit="in">4.14</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>AD-30-XL</PartNumber>		
			<Description>Apex Drouge Parachute [Cd .61 (4.1 oz) 39.375 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="oz">4.1</Mass>		
			<Diameter Unit="in">30</Diameter>		
			<DragCoefficient>.61</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">0555 Nylon webbing #440 [flat 1/2 x 3/64 in (12.7 x 1 mm)]</LineMaterial>		
			<PackedDiameter Unit="in">2.75</PackedDiameter>		
			<PackedLength Unit="in">6.62</PackedLength>		
		</Parachute>			
					
	<!-- 	Spherachutes			
		Hemispherical Spherachute			
	-->				
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-018-CL</PartNumber>		
			<Description>Classic 18" Hemispherical Spherachute [Cd .75 (.6 oz)  4 in^3]</Description>		
			<Diameter Unit="in">11.46</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">18</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.6</Mass>		
			<PackedDiameter Unit="in">1.5</PackedDiameter>		
			<PackedLength Unit="in">2.6</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-018-HD</PartNumber>		
			<Description>Heavy Duty 18" Hemispherical Spherachute [Cd .75 (.9 oz)  in^3]</Description>		
			<Diameter Unit="in">11.46</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">18</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.9</Mass>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.23</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-018-UL</PartNumber>		
			<Description>UltraLight 18" Hemispherical Spherachute [Cd .75 (.2 oz)  2.25 in^3]</Description>		
			<Diameter Unit="in">11.46</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">18</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.2</Mass>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">2.86</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-024-CL</PartNumber>		
			<Description>Classic 24" Hemispherical Spherachute [Cd .75 (1.1 oz)  9.375 in^3]</Description>		
			<Diameter Unit="in">15.28</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.1</Mass>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.98</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-024-HD</PartNumber>		
			<Description>Heavy Duty 24" Hemispherical Spherachute [Cd .75 (1.3 oz)  in^3]</Description>		
			<Diameter Unit="in">15.28</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.3</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">2.67</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-024-UL</PartNumber>		
			<Description>UltraLight 24" Hemispherical Spherachute [Cd .75 (.4 oz)  2.25 in^3]</Description>		
			<Diameter Unit="in">15.28</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.4</Mass>		
			<PackedDiameter Unit="in">1.5</PackedDiameter>		
			<PackedLength Unit="in">2.6</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-030-CL</PartNumber>		
			<Description>Classic 30" Hemispherical Spherachute [Cd .75 (1.2 oz)  9 in^3]</Description>		
			<Diameter Unit="in">19.10</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.2</Mass>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.86</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-030-HD</PartNumber>		
			<Description>Heavy Duty 30" Hemispherical Spherachute [Cd .75 (1.8 oz)  18.375 in^3]</Description>		
			<Diameter Unit="in">19.10</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.8</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">3.74</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-030-UL</PartNumber>		
			<Description>UltraLight 30" Hemispherical Spherachute [Cd .75 (.6 oz)  4.386 in^3]</Description>		
			<Diameter Unit="in">19.10</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.6</Mass>		
			<PackedDiameter Unit="in">1.5</PackedDiameter>		
			<PackedLength Unit="in">2.48</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-036-CL</PartNumber>		
			<Description>Classic 36" Hemispherical Spherachute [Cd .75 (1.8 oz)  13.5 in^3]</Description>		
			<Diameter Unit="in">22.92</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.8</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">2.75</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-036-HD</PartNumber>		
			<Description>Heavy Duty 36" Hemispherical Spherachute [Cd .75 (2.3 oz)  24.5 in^3]</Description>		
			<Diameter Unit="in">22.92</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">2.3</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">3.47</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-036-UL</PartNumber>		
			<Description>UltraLight 36" Hemispherical Spherachute [Cd .75 (.8 oz)  7 in^3]</Description>		
			<Diameter Unit="in">22.92</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">.8</Mass>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.4</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-042-CL</PartNumber>		
			<Description>Classic 42" Hemispherical Spherachute [Cd .75 (2.7 oz)  18 in^3]</Description>		
			<Diameter Unit="in">26.74</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">42</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">2.7</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">3.67</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-042-HD</PartNumber>		
			<Description>Heavy Duty 42" Hemispherical Spherachute [Cd .75 (3.2 oz)  37.5 in^3]</Description>		
			<Diameter Unit="in">26.74</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">42</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">3.2</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">5.31</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-042-UL</PartNumber>		
			<Description>UltraLight 42" Hemispherical Spherachute [Cd .75 (3.2 oz)  10.94 in^3]</Description>		
			<Diameter Unit="in">26.74</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">42</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">3.2</Mass>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">3.48</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-048-CL</PartNumber>		
			<Description>Classic 48" Hemispherical Spherachute [Cd .75 (3.1 oz)  22.5 in^3]</Description>		
			<Diameter Unit="in">30.56</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">3.1</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">3.18</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-048-HD</PartNumber>		
			<Description>Heavy Duty 48" Hemispherical Spherachute [Cd .75 (3.7 oz)  40 in^3]</Description>		
			<Diameter Unit="in">30.56</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">3.7</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">5.66</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-048-UL</PartNumber>		
			<Description>UltraLight 48" Hemispherical Spherachute [Cd .75 (1.5 oz)  13.13 in^3]</Description>		
			<Diameter Unit="in">30.56</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.5</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">2.67</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-054-CL</PartNumber>		
			<Description>Classic 54" Hemispherical Spherachute [Cd .75 (3.8 oz)  36.75 in^3]</Description>		
			<Diameter Unit="in">34.38</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">54</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">3.8</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">5.2</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-054-HD</PartNumber>		
			<Description>Heavy Duty 54" Hemispherical Spherachute [Cd .75 (3.8 oz)  56.25 in^3]</Description>		
			<Diameter Unit="in">34.38</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">54</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">4.6</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">4.8</PackedLength>		
		</Parachute>			
					
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-054-UL</PartNumber>		
			<Description>UltraLight 54" Hemispherical Spherachute [Cd .75 (1.7 oz)  18 in^3]</Description>		
			<Diameter Unit="in">34.38</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">54</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">1.7</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">3.67</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-060-CL</PartNumber>		
			<Description>Classic 60" Hemispherical Spherachute [Cd .75 (5 oz)  31.5 in^3]</Description>		
			<Diameter Unit="in">38.20</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">5</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">4.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-060-HD</PartNumber>		
			<Description>Heavy Duty 60" Hemispherical Spherachute [Cd .75 (5.6 oz)  59 in^3]</Description>		
			<Diameter Unit="in">38.20</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">5.6</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">4.7</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-060-UL</PartNumber>		
			<Description>UltraLight 60" Hemispherical Spherachute [Cd .75 (1.5 oz)  18 in^3]</Description>		
			<Diameter Unit="in">38.20</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">2.1</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">3.68</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-066-CL</PartNumber>		
			<Description>Classic 66" Hemispherical Spherachute [Cd .75 (5.2 oz)  35 in^3</Description>		
			<Diameter Unit="in">42.02</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">66</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">5.2</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">4.9</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-066-HD</PartNumber>		
			<Description>Heavy Duty 66" Hemispherical Spherachute [Cd .75 (3.7 oz)  50 in^3]</Description>		
			<Diameter Unit="in">42.02</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">66</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">6.4</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">7.07</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-066-UL</PartNumber>		
			<Description>UltraLight 66" Hemispherical Spherachute [Cd .75 (2.6 oz)  19.69 in^3]</Description>		
			<Diameter Unit="in">42.02</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">66</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">2.6</Mass>		
			<PackedDiameter Unit="in">2.5</PackedDiameter>		
			<PackedLength Unit="in">4.01</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-072-CL</PartNumber>		
			<Description>Classic 72" Hemispherical Spherachute [Cd .75 (5.9 oz)  50 in^3]</Description>		
			<Diameter Unit="in">45.84</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">5.9</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">7.07</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-072-HD</PartNumber>		
			<Description>Heavy Duty 72" Hemispherical Spherachute [Cd .75 (7.1 oz)  59.06 in^3]</Description>		
			<Diameter Unit="in">45.84</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">7.1</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">4.7</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-072-UL</PartNumber>		
			<Description>UltraLight 72" Hemispherical Spherachute [Cd .75 (2.9 oz)  22.31 in^3]</Description>		
			<Diameter Unit="in">45.84</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">2.9</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">3.16</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-084-CL</PartNumber>		
			<Description>Classic 84" Hemispherical Spherachute [Cd .75 (7.2 oz)  72 in^3]</Description>		
			<Diameter Unit="in">53.48</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">7.2</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">5.73</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-084-UL</PartNumber>		
			<Description>UltraLight 84" Hemispherical Spherachute [Cd .75 (4.6 oz)  45 in^3]</Description>		
			<Diameter Unit="in">53.48</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">4.6</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">6.37</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-096-CL</PartNumber>		
			<Description>Classic 96" Hemispherical Spherachute [Cd .75 (13 oz)  90 in^3]</Description>		
			<Diameter Unit="in">61.12</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">13</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">7.16</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-096-UL</PartNumber>		
			<Description>UltraLight 96" Hemispherical Spherachute [Cd .75 (5.3 oz)  48 in^3]</Description>		
			<Diameter Unit="in">61.12</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">5.3</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">6.9</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-108-CL</PartNumber>		
			<Description>Classic 108" Hemispherical Spherachute [Cd .75 (16 oz)  108 in^3]</Description>		
			<Diameter Unit="in">68.76</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">108</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">16</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">8.58</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-108-UL</PartNumber>		
			<Description>UltraLight 108" Hemispherical Spherachute [Cd .75 (6.5 oz)  68 in^3]</Description>		
			<Diameter Unit="in">68.76</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">108</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">6.5</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">5.41</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-120-CL</PartNumber>		
			<Description>Classic 120" Hemispherical Spherachute [Cd .75 (19 oz)  108 in^3]</Description>		
			<Diameter Unit="in">76.40</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">19</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">8.59</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-120-UL</PartNumber>		
			<Description>UltraLight 120" Hemispherical Spherachute [Cd .75 (7.7 oz)  87.75 in^3]</Description>		
			<Diameter Unit="in">76.40</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">7.7</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">6.98</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-144-CL</PartNumber>		
			<Description>Classic 144" Hemispherical Spherachute [Cd .75 (7.2 oz)  198 in^3]</Description>		
			<Diameter Unit="in">91.68</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">28.6</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">15.76</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-144-UL</PartNumber>		
			<Description>UltraLight 144" Hemispherical Spherachute [Cd .75 (11.7 oz)  106.25 in^3]</Description>		
			<Diameter Unit="in">91.68</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, ultra-lightweight 66 oz 1 mil</Material>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IA paraline 1/16 in (1.6 mm)</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">11.7</Mass>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">8.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-168-CL</PartNumber>		
			<Description>Classic 168" Hemispherical Spherachute [Cd .75 (39 oz)  252 in^3]</Description>		
			<Diameter Unit="in">106.96</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">168</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">39</Mass>		
			<PackedDiameter Unit="in">6</PackedDiameter>		
			<PackedLength Unit="in">8.91</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Spherachutes</Manufacturer>		
			<PartNumber>HS-192-CL</PartNumber>		
			<Description>Classic 192" Hemispherical Spherachute [Cd .75 (53 oz)  445.5 in^3]</Description>		
			<Diameter Unit="in">122.23</Diameter>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">192</LineLength>		
			<LineMaterial Type="LINE">MIL-C-5040 Type IIA paracord #225 [flat 3/16 x 3/64 in (4.5 x 1.1 mm)]</LineMaterial>		
			<DragCoefficient>.75</DragCoefficient>		
			<Mass Unit="oz">53</Mass>		
			<PackedDiameter Unit="in">6</PackedDiameter>		
			<PackedLength Unit="in">15.7</PackedLength>		
		</Parachute>			
					
	</Components>				
					
</OpenRocketComponent>					
