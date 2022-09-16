<?xml version="1.0" encoding="UTF-8" standalone="yes"?>					
					
<!--	Rocketman Parachutes- Ky “Rocketman” Michaelson [https://the-rocketman.com]				
	database file for OpenRocket				
					
	Data for this file was derived from information provided by 				
	Buddy Michaelson, General Manager of Rocketman Parachutes				
					
	Copyright 2022 by H. Craig Miller  NAR #89750  TRA #09190  hcraigmiller@gmail.com				
					
	See the file LICENSE in this distribution for license information.				
					
	This file defines an array of sizes of rocketry parachute recovery systems of various weight capacities.				ls
-->					
					
<OpenRocketComponent>					
					
	<Version>0.1</Version>				
					
	<Materials>				
					
		<!-- 	SURFACE Lightweight 1.1oz mil-spec calendared ripstop nylon.		
			Rocketman quotes the bulk density of 30 denier double silicone coated		
			calendared ripstop nylon fabric at 1.15 oz/yd^2 or .035653924 kg/m^2.		
		-->			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop nylon, ultra lightweight, 2 mil</Name>		
			<Density>0.035653924</Density>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop nylon, lightweight, 3 mil</Name>		
			<Density>0.035653924</Density>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop nylon, 1.9 oz 5 mil</Name>		
			<Density>0.058906483</Density>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop nylon, bulletproof 3 mil</Name>		
			<Density>0.035653924</Density>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<!-- 	LINE materials for shroud lines.		
			Small high-quality Nylon paraline shock cord for lightweight applications		
				Fruity Chutes quotes the bulk density of #200 Nylon IIa paraline as 2.7 g/yd 	
				or 0.002952756 kg/m^2.	
			Small high-quality Nylon paraline shock cord for lightweight applications		
				Fruity Chutes quotes the bulk density of #400 Nylon IIIa paraline as 3.4 g/yd 	
				or 0.003718285 kg/m^2.	
			Small high-quality Spectra shock cord for lightweight applications		
				Fruity Chutes quotes the bulk density of #200 Spectra as 0.6 g/yd 	
				or 0.000656168 kg/m^2.	
			Small high-quality Spectra shock cord for lightweight applications		
				Fruity Chutes quotes the bulk density of #400 Spectra as 1.1 g/yd 	
				or 0.001202975 kg/m^2.	
		-->			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Spectra #200 [Round 1.5 mm, 1/16 in]</Name>		
			<Density>0.000656168</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Spectra #400 [Oval 2.55 mm, 3/32in]</Name>		
			<Density>0.001202975</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</Name>		
			<Density>0.003499563</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Braided Nylon [ 9.5 mm, 3/8 in]</Name>		
			<Density>0.009623797</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Braided Polyester [Flat 9.5 mm, 3/8 in]</Name>		
			<Density>0.012029746</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>Tubular Nylon #2500 [12.7 mm, 1/2 in]</Name>		
			<Density>0.012685914</Density>		
			<Type>LINE</Type>		
		 </Material>			
					
	</Materials>				
					
	<Components>				
					
		<!-- 	BEGIN Rocketman Proud American parachutes, 2' to 14'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-02</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (1.23 oz) 7.23 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.03486991</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>10</Sides>		
			<LineCount>10</LineCount>		
			<LineLength Unit="in">28.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">2.3</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-03</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (2.22 oz) 13.05 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.062935934</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>10</Sides>		
			<LineCount>10</LineCount>		
			<LineLength Unit="in">43.2</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">4.15</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-04</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (3.76 oz) 22.1 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.106594195</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">57.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">7.03</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-05</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (5.33 oz) 31.34 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.151102942</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.43</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-06</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (7.58 oz) 31.34 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.214889362</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">86.4</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.43</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-07</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (15.62 oz) 91.84 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.442819502</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">100.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.30</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-08</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (18.86 oz) 110.89 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.534671947</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">115.2</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">8.82</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-09</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (22.36 oz) 131.47 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.633895267</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">108</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">129.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">10.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-10</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (22.36 oz) 131.47 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.794637046</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>16</Sides>		
			<LineCount>16</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">13.11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-12</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (22.36 oz) 131.47 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">1.105347785</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">172.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">18.24</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PA-14</PartNumber>		
			<Description>PolyConical Parachute [Cd .99 (22.36 oz) 131.47 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">1.542497383</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>.99</DragCoefficient>		
			<Sides>22</Sides>		
			<LineCount>22</LineCount>		
			<LineLength Unit="in">201.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">26.45</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Proud American parachutes, 2' to 14'		
		-->			
					
		<!--	BEGIN Rocketman Hexagon parachutes, 9" to 120"		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-009</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (.3895 oz) 1.94 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.011042138</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">9</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">11.25</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.0</PackedDiameter>		
			<PackedLength Unit="in">2.47</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-012</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (.4651 oz) 2.32 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.013185362</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">15</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.0</PackedDiameter>		
			<PackedLength Unit="in">2.95</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-015</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (.5541 oz) 2.77 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.015708469</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">15</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">18.75</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.0</PackedDiameter>		
			<PackedLength Unit="in">3.52</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-018</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (.6560 oz) 3.28 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.018597285</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">18</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">22.5</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">1.04</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-024</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (.93 oz) 4.65 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.026365054</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">1.48</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-030</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (1.228 oz) 6.14 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.034813211</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">30</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">37.5</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">1.95</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-036</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (1.578 oz) 7.89 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.044735543</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">45</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">2.51</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-042</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (1.578 oz) 7.89 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.056982535</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">42</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">52.5</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">3.20</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-048</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (2.467 oz) 12.33 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.069938266</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">3.92</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-060</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (3.568 oz) 17.84 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.101151087</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">75</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">5.67</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-072</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (4.88 oz) 24.4 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.138345658</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">90</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">3.45</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-084</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (6.4 oz) 32.01 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.181436928</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">105</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.52</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-096</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (8.1 oz) 40.53 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.229631112</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">5.73</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>HX-120</PartNumber>		
			<Description>Hexagonal Parachute [Cd .75 (12.01 oz) 60.06 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.340477735</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.75</DragCoefficient>		
			<Sides>6</Sides>		
			<LineCount>6</LineCount>		
			<LineLength Unit="in">150</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">8.49</PackedLength>		
		</Parachute>			
					
		<!--	END Rocketman Hexagon parachutes, 9" to 120"		
		-->			
					
		<!-- 	BEGIN Rocketman TARC/Model Rocketry Parachutes, 12" to 30"		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>TC-12</PartNumber>		
			<Description>TARC Parachute [Cd 1.5 (9.0 g) 1.77 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.009</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>1.5</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">12</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">2.25</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>TC-15</PartNumber>		
			<Description>TARC Parachute [Cd 1.5 (10.0 g) 1.84 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.010</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">15</Diameter>		
			<DragCoefficient>1.5</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">15</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">2.75</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>TC-18</PartNumber>		
			<Description>TARC Parachute [Cd 1.5 (15.0 g) 2.62 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.015</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">18</Diameter>		
			<DragCoefficient>1.5</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">18</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">3.75</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>TC-24</PartNumber>		
			<Description>TARC Parachute [Cd 1.5 (15.0 g) 2.62 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.015</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>1.5</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">5.75</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>TC-30</PartNumber>		
			<Description>TARC Parachute [Cd 1.5 (34.0 g) 5.69 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.034</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">30</Diameter>		
			<DragCoefficient>1.5</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">30</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1</PackedDiameter>		
			<PackedLength Unit="in">7.25</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman TARC/Model Rocketry Parachutes, 12" to 30"		
		-->			
					
		<!-- 	BEGIN Rocketman Elliptical parachutes, 24" to 120"		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-024</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (2.1 oz) 12.16 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.059533992</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.9</PackedDiameter>		
			<PackedLength Unit="in">4.29</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-036</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (4.4 oz) 24.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.124737888</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.6</PackedDiameter>		
			<PackedLength Unit="in">4.52</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-048</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (7.2 oz) 36.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.204116544</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.6</PackedDiameter>		
			<PackedLength Unit="in">6.78</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-060</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (9.9 oz) 50.5 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.280660248</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">4.01</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-072</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (12.6 oz) 63.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.357203952</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">5.01</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-084</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (15.3 oz) 76.5 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.433737656</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">6.08</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-096</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (18.5 oz) 92.5 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.52446612</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.36</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>EL-120</PartNumber>		
			<Description>Elliptical Parachute [Cd 1.6 (25.0 oz) 140.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.708738</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>1.6</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Braided Nylon #250 [Flat 6.35 mm, 1/4 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">11.19</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Elliptical parachutes, 24" to 120"		
		-->			
					
		<!-- 	BEGIN Rocketman Ballistic Mach II Parachutes, 1' to 16'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-01</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (3.3 oz) 28.27 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.093553416</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">12</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-02</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (6.0 oz) 62.83 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.17009712</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">5.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-03</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (11.3 oz) 87.96 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.320349576</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-04</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (18.0 oz) 106.81 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.51029136</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">8.5</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-05</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (23.6 oz) 163.36 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.669048672</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">13.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-06</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (31.6 oz) 213.63 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">.895844832</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">17.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-07</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (40.0 oz) 367.57 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">1.1339808</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">6.0</PackedDiameter>		
			<PackedLength Unit="in">13.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>BA-12</PartNumber>		
			<Description>Ballistic Parachute [Cd .97 (160.0 oz) 890.64 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, bulletproof 3 mil</Material>		
			<Mass Unit="kg">4.5359232</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Tubular Nylon #2500 [12.7 mm, 1/2 in]</LineMaterial>		
			<PackedDiameter Unit="in">8.0</PackedDiameter>		
			<PackedLength Unit="in">17.7</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Ballistic Mach II Parachutes, 1' to 16'		
		-->			
					
		<!-- 	BEGIN Rocketman Pro Experimental Drogue Parachutes, 1' to 36'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-01</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (1.6 oz) 11.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.045359232</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">12</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">3.5</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-02</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (2.7 oz) 18.85 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.076543704</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">6.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-03</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (4.5 oz) 42.41 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.12757284</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">8.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-04</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (7.7 oz) 56.54 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.218291304</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">8.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-05</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (8.5 oz) 28.27 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.24097092</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">9.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-06</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (12.2 oz) 87.96 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.345864144</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-07</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (14.3 oz) 113.10 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.405398136</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">9.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-08</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (3.3 oz) 28.27 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.518796216</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">10.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-09</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (21.3 oz) 150.80 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.603844776</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">108</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">108</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">12.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-10</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (22.8 oz) 163.36 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.646369056</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">13.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-12</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (29.4 oz) 188.50 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">.833475888</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">15.0</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>PX-14</PartNumber>		
			<Description>Pro X Drogue Parachute [Cd .97 (40.0 oz) 339.29 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, 1.9 oz 5 mil</Material>		
			<Mass Unit="kg">1.1339808</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">168</LineLength>		
			<LineMaterial Type="LINE">Braided Polyester [Flat 9.5 mm, 3/8 in]</LineMaterial>		
			<PackedDiameter Unit="in">6.0</PackedDiameter>		
			<PackedLength Unit="in">12.0</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Pro Experimental Drogue Parachutes, 1' to 36'		
		-->			
					
		<!-- 	BEGIN Rocketman Disk Gap Band Parachutes, 2' to 14'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-02</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (1.6 oz) 11.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.045359232</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>10</Sides>		
			<LineCount>10</LineCount>		
			<LineLength Unit="in">28.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">2.3</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-03</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (2.22 oz) 13.05 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.062935934</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>10</Sides>		
			<LineCount>10</LineCount>		
			<LineLength Unit="in">43.2</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">4.15</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-04</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (3.76 oz) 22.10 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.106594195</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">57.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2.0</PackedDiameter>		
			<PackedLength Unit="in">7.03</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-05</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (5.33 oz) 31.34 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.151102942</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.43</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-06</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (7.58 oz) 31.34 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.214889362</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">86.4</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">4.43</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-07</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (15.62 oz) 91.84 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.442819502</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">100.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.3</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-08</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (18.86 oz) 110.89 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.534671947</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">115.2</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">8.82</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-09</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (22.36 oz) 131.47 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.633895267</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">108</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">129.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">10.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-10</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (28.03 oz) 164.81 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.794637046</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>16</Sides>		
			<LineCount>16</LineCount>		
			<LineLength Unit="in">144.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">13.11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-12</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (38.99 oz) 229.26 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">1.105347785</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">172.8</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">18.24</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>DG-14</PartNumber>		
			<Description>Disk Gap Parachute [Cd .85 (54.41 oz) 319.93 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">1.542497383</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>22</Sides>		
			<LineCount>22</LineCount>		
			<LineLength Unit="in">201.6</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">26.45</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Disk Gap Band Parachutes, 2' to 14'		
		-->			
					
		<!-- 	BEGIN Rocketman Ultra Light Annular Parachutes, 3' to 16'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-03</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (3.5 oz) 20.58 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.09922322</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">2.91</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-04</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (4.7 oz) 27.63 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.133242744</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">3.9</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-05</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (6.2 oz) 36.45 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.175767024</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">5.15</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-06</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (7.5 oz) 44.10 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.2126214</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">6.23</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-07</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (8.9 oz) 52.33 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.252310728</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>16</Sides>		
			<LineCount>16</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.0</PackedDiameter>		
			<PackedLength Unit="in">7.4</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-08</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (10.1 oz) 59.28 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.286330152</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>16</Sides>		
			<LineCount>16</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">4.72</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-09</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (11.6 oz) 68.20 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.328854432</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">108</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">108</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">5.42</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-10</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (12.9 oz) 75.85 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.365708808</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">6.03</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-12</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (15.6 oz) 91.72 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.442252512</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>20</Sides>		
			<LineCount>20</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">7.29</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-14</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (18.1 oz) 106.42 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.513126312</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>20</Sides>		
			<LineCount>20</LineCount>		
			<LineLength Unit="in">168</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">8.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LA-16</PartNumber>		
			<Description>Light Annular Parachute [Cd .85 (20.9 oz) 122.89 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, lightweight, 3 mil</Material>		
			<Mass Unit="kg">.592504968</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">192</Diameter>		
			<DragCoefficient>.85</DragCoefficient>		
			<Sides>22</Sides>		
			<LineCount>22</LineCount>		
			<LineLength Unit="in">192</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4.0</PackedDiameter>		
			<PackedLength Unit="in">9.77</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Ultra Light Annular Parachutes, 3' to 16'		
		-->			
					
		<!-- 	BEGIN Rocketman Ultra Light High Performance CD 2.2 Parachutes, 24" to 192"		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-024</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (.7 oz) 2.331 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.019844664</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.375</PackedDiameter>		
			<PackedLength Unit="in">1.569</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-036</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (1.5 oz) 4.57 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.04252428</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">1.375</PackedDiameter>		
			<PackedLength Unit="in">3.08</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-048</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (2.2 oz) 6.66 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.062368944</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>8</Sides>		
			<LineCount>8</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-060</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (3.4 oz) 11.322 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.096388368</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">3.6</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-072</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (4.6 oz) 15.318 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.130407792</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">4.875</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-084</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (5.95 oz) 19.813 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.168679644</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">2.388</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-096</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (7.3 oz) 24.30 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.206951496</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">2.929</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-120</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (10.4 oz) 34.62 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.294835008</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>12</Sides>		
			<LineCount>12</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">4.12</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-144</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (12.4 oz) 41.29 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.351534048</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>14</Sides>		
			<LineCount>14</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">4.977</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-168</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (14.6 oz) 48.61 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.413902992</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>16</Sides>		
			<LineCount>16</LineCount>		
			<LineLength Unit="in">168</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">5.85</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LP-192</PartNumber>		
			<Description>Light HP Parachute [Cd 2.2 (17.7 oz) 58.94 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.501786504</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">192</Diameter>		
			<DragCoefficient>2.2</DragCoefficient>		
			<Sides>18</Sides>		
			<LineCount>18</LineCount>		
			<LineLength Unit="in">192</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3.25</PackedDiameter>		
			<PackedLength Unit="in">7.1</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Ultra Light High Performance CD 2.2 Parachutes, 24" to 192"		
		-->			
					
		<!-- 	BEGIN Rocketman Ultra Light Standard Parabolic Parachutes, 1'-20'		
		-->			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-01</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (.32 oz)1.878 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.009071846</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">12</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">12</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">.6</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-02</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (.664oz) 3.78 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.018824081</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">1.2</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-03</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (1.112 oz) 6.52 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.031524666</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">2.08</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-04</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (1.67 oz) 9.8 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.047343698</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">48</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">48</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">3.12</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-05</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (2.27 oz) 12.70 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.06435341</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">4.04</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-06</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (3.4 oz) 12.70 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.096388368</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">72</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">72</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">4.04</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-07</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (3.72 oz) 21.85 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.105460214</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">84</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">84</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">2</PackedDiameter>		
			<PackedLength Unit="in">6.95</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-08</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (4.878 oz) 28.63 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.138288959</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">96</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">96</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">4.05</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-09</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (5.75 oz) 33.77 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.163000974</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">108</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">108</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">4.78</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-10</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (6.57 oz) 38.57 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.186256346</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">5.46</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-12</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (8.37 oz) 49.14 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.237285482</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">144</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">144</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">6.95</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-14</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (10.82 oz) 63.53 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.306741806</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">168</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">168</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">8.99</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-16</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (12.71 oz) 75.54 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.360322399</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">192</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">192</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">10.69</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-18</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (14.58 oz) 85.59 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.413336002</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">216</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">216</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">6.81</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-19</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (20.885 oz) 122.59 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.592079725</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">228</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">228</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">9.76</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>Rocketman</Manufacturer>		
			<PartNumber>LS-20</PartNumber>		
			<Description>Light Std Parabolic Parachute [Cd .97 (21.858 oz) 128.30 in^3]</Description>		
			<Material Type="SURFACE">Ripstop nylon, ultra lightweight, 2 mil</Material>		
			<Mass Unit="kg">.619663808</Mass>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">240</Diameter>		
			<DragCoefficient>.97</DragCoefficient>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">240</LineLength>		
			<LineMaterial Type="LINE">Spectra #200 [Round 1.5 mm, 1/16 in]</LineMaterial>		
			<PackedDiameter Unit="in">4</PackedDiameter>		
			<PackedLength Unit="in">10.21</PackedLength>		
		</Parachute>			
					
		<!-- 	END Rocketman Ultra Light Standard Parabolic Parachutes, 1'-20'		
		-->			
					
	</Components>				
					
</OpenRocketComponent>					
