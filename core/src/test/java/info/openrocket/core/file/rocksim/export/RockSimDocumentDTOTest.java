package info.openrocket.core.file.rocksim.export;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.importt.RockSimLoader;
import info.openrocket.core.file.rocksim.importt.RockSimLoaderTest;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 */
public class RockSimDocumentDTOTest extends RockSimTestBase {
	
	@Test
	public void testDTO() throws Exception {
		JAXBContext binder = JAXBContext.newInstance(RockSimDocumentDTO.class);
		Marshaller marshaller = binder.createMarshaller();
		marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
		
		NoseConeDTO noseCone = new NoseConeDTO();
		noseCone.setBaseDia(10.0d);
		noseCone.setCalcCG(1.3d);
		
		StageDTO stage1 = new StageDTO();
		stage1.addExternalPart(noseCone);
		
		RocketDesignDTO design2 = new RocketDesignDTO();
		design2.setName("Test");
		design2.setStage3(stage1);
		
		RockSimDesignDTO design = new RockSimDesignDTO();
		design.setDesign(design2);
		RockSimDocumentDTO message = new RockSimDocumentDTO();
		message.setDesign(design);
		
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(message, stringWriter);
		
		String response = stringWriter.toString();
		
		// TODO need checks here to validation that correct things were done
		//System.err.println(response);
	}
	
	@Test
	public void testRoundTrip() throws Exception {
		// TODO need checks here to validate that correct things were done
		OpenRocketDocument ord = RockSimLoaderTest.loadRockSimRocket(new RockSimLoader(), "rocksimTestRocket3.rkt");

		Assertions.assertNotNull(ord);
		String result = new RockSimSaver().marshalToRockSim(ord);
		
		//  System.err.println(result);
		
		File output = new File("rt.rkt");
		FileWriter fw = new FileWriter(output);
		fw.write(result);
		fw.flush();
		fw.close();
		
		output.delete();
	}

	/**
	 * Tests exporting a rocket with pods, and whether importing that same file results in the same pod configuration.
	 */
	@Test
	public void testPodsExport() throws Exception {
		OpenRocketDocument originalDocument = makePodsRocket();
		Rocket originalRocket = originalDocument.getRocket();

		// Convert to RockSim XML
		String result = new RockSimSaver().marshalToRockSim(originalDocument);

		// Write to .rkt file
		Path output = Files.createTempFile("podsRocket", ".rkt");
		Files.write(output, result.getBytes(StandardCharsets.UTF_8));

		// Read the file
		RockSimLoader loader = new RockSimLoader();
		InputStream stream = new FileInputStream(output.toFile());
		Assertions.assertNotNull(stream, "Could not open podsRocket.rkt");
		OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(importedDocument);
		context.setMotorFinder(new DatabaseMotorFinder());
		loader.loadFromStream(context, new BufferedInputStream(stream), null);
		Rocket importedRocket = importedDocument.getRocket();

		// Test children counts
		List<RocketComponent> originalChildren = originalRocket.getAllChildren();
		List<RocketComponent> importedChildren = importedRocket.getAllChildren();
		assertEquals(originalChildren.size() + 5, importedChildren.size(), " Number of total children doesn't match");		// More children because RockSim has individual podsets
		assertEquals(1, importedRocket.getChildCount(), " Number of rocket children doesn't match");
		AxialStage stage = (AxialStage) importedRocket.getChild(0);
		assertEquals(2, stage.getChildCount(), " Number of stage children doesn't match");
		BodyTube tube = (BodyTube) stage.getChild(1);
		assertEquals(6, tube.getChildCount(), " Number of body tube children doesn't match");
		PodSet pod1 = (PodSet) tube.getChild(0);
		assertEquals(1, pod1.getChildCount(), " Number of pod 1 children doesn't match");
		PodSet pod2 = (PodSet) tube.getChild(1);
		assertEquals(2, pod2.getChildCount(), " Number of pod 2 children doesn't match");
		PodSet pod3 = (PodSet) tube.getChild(3);
		assertEquals(0, pod3.getChildCount(), " Number of pod 3 children doesn't match");

		// Test component names
		assertEquals("Pod 1", importedChildren.get(3).getName(), " Name does not match");
		assertEquals("Pod 2 #1", importedChildren.get(5).getName(), " Name does not match");
		assertEquals("Pod 2 #2", importedChildren.get(8).getName(), " Name does not match");
		assertEquals("Pod 3 #1", importedChildren.get(11).getName(), " Name does not match");
		assertEquals("Pod 3 #2", importedChildren.get(12).getName(), " Name does not match");
		assertEquals("Pod 3 #3", importedChildren.get(13).getName(), " Name does not match");

		// Test pod parameters
		assertEquals(-0.14, pod1.getAxialOffset(), 0.0001);
		assertEquals(0.065, pod1.getRadiusOffset(), 0.0001);
		assertEquals(Math.PI, pod1.getAngleOffset(), 0.0001);
		assertEquals(1, pod1.getInstanceCount());
		assertEquals(0.02, pod2.getAxialOffset(), 0.0001);
		assertEquals(0.025, pod2.getRadiusOffset(), 0.0001);
		assertEquals(- Math.PI / 2, pod2.getAngleOffset(), 0.0001);
		assertEquals(1, pod2.getInstanceCount());
		assertEquals(0.23, pod3.getAxialOffset(), 0.0001);
		assertEquals(0.06, pod3.getRadiusOffset(), 0.0001);
		assertEquals(Math.PI / 3, pod3.getAngleOffset(), 0.0001);
		assertEquals(1, pod3.getInstanceCount());

		stream.close();
		Files.delete(output);
	}
	/**
	 * Tests exporting a design where a tube coupler has children, which is not supported by RockSim, so the children
	 * need to be moved outside the tube coupler.
	 */
	@Test
	public void testTubeCouplerChildrenExport() throws Exception {
		OpenRocketDocument originalDocument = makeTubeCouplerRocket();
		Rocket originalRocket = originalDocument.getRocket();

		// Convert to RockSim XML
		String result = new RockSimSaver().marshalToRockSim(originalDocument);

		// Write to .rkt file
		Path output = Files.createTempFile("tubeCouplerRocket", ".rkt");
		Files.write(output, result.getBytes(StandardCharsets.UTF_8));

		// Read the file
		RockSimLoader loader = new RockSimLoader();
		InputStream stream = new FileInputStream(output.toFile());
		Assertions.assertNotNull(stream, "Could not open tubeCouplerRocket.rkt");
		OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(importedDocument);
		context.setMotorFinder(new DatabaseMotorFinder());
		loader.loadFromStream(context, new BufferedInputStream(stream), null);
		Rocket importedRocket = importedDocument.getRocket();

		// Test children counts
		List<RocketComponent> originalChildren = originalRocket.getAllChildren();
		List<RocketComponent> importedChildren = importedRocket.getAllChildren();
		assertEquals(originalChildren.size(), importedChildren.size(), " Number of total children doesn't match");
		assertEquals(1, importedRocket.getChildCount(), " Number of rocket children doesn't match");
		AxialStage stage = (AxialStage) importedRocket.getChild(0);
		assertEquals(2, stage.getChildCount(), " Number of stage children doesn't match");
		BodyTube tube = (BodyTube) stage.getChild(1);
		assertEquals(12, tube.getChildCount(), " Number of body tube children doesn't match");

		// Test component names
		for (int i = 1; i < originalChildren.size(); i++) {
			assertEquals(originalChildren.get(i).getName(), importedChildren.get(i).getName(),
					" Child " + i + " does not match");
		}

		stream.close();
		Files.delete(output);
	}

	private OpenRocketDocument makePodsRocket() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);

		// Stage children
		NoseCone noseCone = new NoseCone();
		noseCone.setName("Nose Cone");
		stage.addChild(noseCone);
		BodyTube tube = new BodyTube();
		tube.setName("Body Tube");
		stage.addChild(tube);

		// Body tube children
		PodSet pod1 = new PodSet();
		pod1.setName("Pod 1");
		tube.addChild(pod1);
		PodSet pod2 = new PodSet();
		pod2.setName("Pod 2");
		tube.addChild(pod2);
		PodSet pod3 = new PodSet();
		pod3.setName("Pod 3");
		tube.addChild(pod3);

		// Pod 1 children
		NoseCone noseCone1 = new NoseCone();
		noseCone1.setName("Nose Cone 1");
		pod1.addChild(noseCone1);

		// Pod 2 children
		NoseCone noseCone2 = new NoseCone();
		noseCone2.setName("Nose Cone 2");
		pod2.addChild(noseCone2);
		BodyTube tube2 = new BodyTube();
		tube2.setName("Body Tube 2");
		pod2.addChild(tube2);

		// Set pod parameters
		pod1.setInstanceCount(1);
		pod2.setInstanceCount(2);
		pod3.setInstanceCount(3);

		pod1.setAxialMethod(AxialMethod.ABSOLUTE);
		pod1.setAxialOffset(0.01);
		pod2.setAxialMethod(AxialMethod.TOP);
		pod2.setAxialOffset(0.02);
		pod3.setAxialMethod(AxialMethod.BOTTOM);
		pod3.setAxialOffset(0.03);

		pod1.setRadiusMethod(RadiusMethod.RELATIVE);
		pod1.setRadiusOffset(0.015);
		pod2.setRadiusMethod(RadiusMethod.FREE);
		pod2.setRadiusOffset(0.025);
		pod3.setRadiusMethod(RadiusMethod.RELATIVE);
		pod3.setRadiusOffset(0.035);

		pod1.setAngleOffset(Math.PI);
		pod2.setAngleOffset(- Math.PI / 2);
		pod3.setAngleOffset(Math.PI / 3);

		return document;
	}

	private OpenRocketDocument makeTubeCouplerRocket() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);

		// Stage children
		NoseCone noseCone = new NoseCone();
		noseCone.setName("Nose Cone");
		stage.addChild(noseCone);
		BodyTube tube = new BodyTube();
		tube.setName("Body Tube");
		stage.addChild(tube);

		// Body tube children
		TubeCoupler coupler = new TubeCoupler();
		coupler.setName("Tube coupler 1");
		tube.addChild(coupler);
		TubeCoupler coupler3 = new TubeCoupler();
		coupler3.setName("Tube Coupler 3");
		tube.addChild(coupler3);

		// Tube coupler 1 children
		InnerTube innerTube = new InnerTube();
		innerTube.setName("Inner Tube");
		coupler.addChild(innerTube);
		TubeCoupler coupler2 = new TubeCoupler();
		coupler2.setName("Tube Coupler 2");
		coupler.addChild(coupler2);
		CenteringRing centeringRing = new CenteringRing();
		centeringRing.setName("Centering Ring");
		coupler.addChild(centeringRing);
		Bulkhead bulkhead = new Bulkhead();
		bulkhead.setName("Bulkhead");
		coupler.addChild(bulkhead);
		EngineBlock engineBlock = new EngineBlock();
		engineBlock.setName("Engine Block");
		coupler.addChild(engineBlock);
		Parachute parachute = new Parachute();
		parachute.setName("Parachute 1");
		coupler.addChild(parachute);
		Streamer streamer = new Streamer();
		streamer.setName("Streamer");
		coupler.addChild(streamer);
		ShockCord shockCord = new ShockCord();
		shockCord.setName("Shock Cord");
		coupler.addChild(shockCord);
		MassComponent massComponent = new MassComponent();
		massComponent.setName("Mass Component");
		coupler.addChild(massComponent);

		// Tube coupler 3 children
		Parachute parachute2 = new Parachute();
		parachute2.setName("Parachute 2");
		coupler3.addChild(parachute2);

		return document;
	}
}
