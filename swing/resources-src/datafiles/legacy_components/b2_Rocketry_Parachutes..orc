<?xml version="1.0" encoding="UTF-8" standalone="yes"?>					
					
<!--	b2 Rocketry Company [http://www.b2rocketry.com]				
	parachute database file for OpenRocket				
					
	Data for this file was derived from information provided by 				
	Specifications shown on website: http://www.b2rocketry.com				
					
	Copyright 2022 by H. Craig Miller  NAR #89750  TRA #09190  hcraigmiller@gmail.com				
					
	See the file LICENSE in this distribution for license information.				
					
	This file defines an array of sizes of rocketry parachute recovery systems of various weight capacities.				
-->					
					
<OpenRocketComponent>					
					
	<Version>0.1</Version>				
					
	<Materials>				
					
		<!-- 	Fabrics		
		-->			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Name>		
			<Density>0.040304440</Density>		
			<Thickness Unit="in">.004</Thickness>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<Material UnitsOfMeasure="kg/m2">			
			<Name>Ripstop Nylon, 1.9 oz 5 mil</Name>		
			<Density>0.058906483</Density>		
			<Thickness Unit="in">.005</Thickness>		
			<Type>SURFACE</Type>		
		</Material>			
					
		<!-- 	Suspension lines		
		-->			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</Name>		
			<Density>0.012401366</Density>		
			<Thickness Unit="in">.2073</Thickness>		
			<Type>LINE</Type>		
		 </Material>			
					
		<Material UnitsOfMeasure="kg/m">			
			<Name>5625 Nylon woven tubular #2,250 [flat 5/8 x 7/64 in (15.8 x 2.5 mm)]</Name>		
			<Density>0.023252562</Density>		
			<Thickness Unit="in">.2821</Thickness>		
			<Type>LINE</Type>		
		 </Material>			
					
	</Materials>				
					
	<Components>				
					
		<!-- 	b2 Rocketry Company		
			SkyAngle Classic		
		-->			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-20</PartNumber>		
			<Description>SkyAngle Classic 20 [Cd 0.8 (3 oz) 20.8 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">20</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">20</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>0.8</DragCoefficient>		
			<Mass Unit="oz">3</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">6</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-24-N</PartNumber>		
			<Description>SkyAngle Classic 24 [Cd 1.16 (3.5 oz) 38.1 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.16</DragCoefficient>		
			<Mass Unit="oz">3.5</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-28</PartNumber>		
			<Description>SkyAngle Classic 28 [Cd 0.93 (4 oz) 24.3 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">28</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">28</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>0.93</DragCoefficient>		
			<Mass Unit="oz">4</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">7</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-32-N</PartNumber>		
			<Description>SkyAngle Classic 32 [Cd 1.14 (4.5 oz) 38.1 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">32</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">32</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.14</DragCoefficient>		
			<Mass Unit="oz">4.5</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-36</PartNumber>		
			<Description>SkyAngle Classic 36 [Cd 1.34 (5 oz) 51.5 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.34</DragCoefficient>		
			<Mass Unit="oz">5</Mass>		
			<PackedDiameter Unit="in">2.56</PackedDiameter>		
			<PackedLength Unit="in">10</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-44</PartNumber>		
			<Description>SkyAngle Classic 44 [Cd 1.87 (7 oz) 63.6 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">44</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">44</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.87</DragCoefficient>		
			<Mass Unit="oz">7</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">9</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-52</PartNumber>		
			<Description>SkyAngle Classic 52 [Cd 1.46 (9 oz) 77.8 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">52</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">52</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.46</DragCoefficient>		
			<Mass Unit="oz">9</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL-60</PartNumber>		
			<Description>SkyAngle Classic 60 [Cd 1.89 (10 oz) 131 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, lightweight, 1.3 oz. 4 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.89</DragCoefficient>		
			<Mass Unit="oz">10</Mass>		
			<PackedDiameter Unit="in">3.9</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		<!-- 	b2 Rocketry Company		
			SkyAngle Classic II		
		-->			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-20</PartNumber>		
			<Description>SkyAngle Classic II 20 [Cd 0.8 (5.6 oz) 36.0 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">20</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">20</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>0.8</DragCoefficient>		
			<Mass Unit="oz">5.6</Mass>		
			<PackedDiameter Unit="in">2.56</PackedDiameter>		
			<PackedLength Unit="in">7</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-28</PartNumber>		
			<Description>SkyAngle Classic II 28 [Cd 0.93 (7 oz) 34.6 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">28</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">28</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>0.93</DragCoefficient>		
			<Mass Unit="oz">7</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">10</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-32-N</PartNumber>		
			<Description>SkyAngle Classic II 32 [Cd 1.14 (7.7 oz) 38.1 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">32</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">32</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.14</DragCoefficient>		
			<Mass Unit="oz">7.7</Mass>		
			<PackedDiameter Unit="in">2.1</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-36</PartNumber>		
			<Description>SkyAngle Classic II 36 [Cd 1.34 (8.4 oz) 56.6 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">36</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">36</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.34</DragCoefficient>		
			<Mass Unit="oz">8.4</Mass>		
			<PackedDiameter Unit="in">2.56</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-44</PartNumber>		
			<Description>SkyAngle Classic II 44 [Cd 1.87 (10.5 oz) 77.8 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">44</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">44</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.87</DragCoefficient>		
			<Mass Unit="oz">10.5</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">11</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-52</PartNumber>		
			<Description>SkyAngle Classic II 52 [Cd 1.46 (13.2 oz) 91.9 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">52</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">52</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.46</DragCoefficient>		
			<Mass Unit="oz">13.2</Mass>		
			<PackedDiameter Unit="in">3</PackedDiameter>		
			<PackedLength Unit="in">13</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CL2-60</PartNumber>		
			<Description>SkyAngle Classic II 60 [Cd 1.89 (18.2 oz) 143 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">60</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">60</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.89</DragCoefficient>		
			<Mass Unit="oz">18.2</Mass>		
			<PackedDiameter Unit="in">3.9</PackedDiameter>		
			<PackedLength Unit="in">12</PackedLength>		
		</Parachute>			
					
		<!-- 	b2 Rocketry Company		
			SkyAngle Cert-3		
		-->			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CRT-024-D</PartNumber>		
			<Description>SkyAngle Cert-3 Drogue [Cd 1.16 (6 oz) 36 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">24</Diameter>		
			<Sides>3</Sides>		
			<LineCount>3</LineCount>		
			<LineLength Unit="in">24</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #950 [flat 3/8 x 3/32 in (12.7 x 1.9 mm)]</LineMaterial>		
			<DragCoefficient>1.16</DragCoefficient>		
			<Mass Unit="oz">6</Mass>		
			<PackedDiameter Unit="in">2.56</PackedDiameter>		
			<PackedLength Unit="in">7</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CRT-080 L</PartNumber>		
			<Description>SkyAngle Classic Cert-3 L [Cd 1.26 (34 oz) 203 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">80</Diameter>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">80</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #2,250 [flat 5/8 x 7/64 in (15.8 x 2.5 mm)]</LineMaterial>		
			<DragCoefficient>1.26</DragCoefficient>		
			<Mass Unit="oz">34</Mass>		
			<PackedDiameter Unit="in">3.9</PackedDiameter>		
			<PackedLength Unit="in">17</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CRT-100-XL</PartNumber>		
			<Description>SkyAngle Classic Cert-3 XL [Cd 2.59 (45 oz) 299 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">100</Diameter>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">100</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #2,250 [flat 5/8 x 7/64 in (15.8 x 2.5 mm)]</LineMaterial>		
			<DragCoefficient>2.59</DragCoefficient>		
			<Mass Unit="oz">45</Mass>		
			<PackedDiameter Unit="in">3.9</PackedDiameter>		
			<PackedLength Unit="in">25</PackedLength>		
		</Parachute>			
					
		 <Parachute>			
			<Manufacturer>b2 Rocketry Company</Manufacturer>		
			<PartNumber>CRT-120-XXL</PartNumber>		
			<Description>SkyAngle Classic Cert-3 XXL [Cd 2.92 (64 oz) 394 in^3]</Description>		
			<Material Type="SURFACE">Ripstop Nylon, 1.9 oz 5 mil</Material>		
			<Finish></Finish>		
			<CG></CG>		
			<Diameter Unit="in">120</Diameter>		
			<Sides>4</Sides>		
			<LineCount>4</LineCount>		
			<LineLength Unit="in">120</LineLength>		
			<LineMaterial Type="LINE">5625 Nylon woven tubular #2,250 [flat 5/8 x 7/64 in (15.8 x 2.5 mm)]</LineMaterial>		
			<DragCoefficient>2.92</DragCoefficient>		
			<Mass Unit="oz">64</Mass>		
			<PackedDiameter Unit="in">3.9</PackedDiameter>		
			<PackedLength Unit="in">33</PackedLength>		
		</Parachute>			
					
	</Components>				
					
</OpenRocketComponent>					
