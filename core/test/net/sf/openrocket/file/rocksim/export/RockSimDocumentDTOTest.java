package net.sf.openrocket.file.rocksim.export;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.importt.RockSimLoader;
import net.sf.openrocket.file.rocksim.importt.RockSimLoaderTest;
import net.sf.openrocket.file.rocksim.importt.RockSimTestBase;

import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 */
public class RockSimDocumentDTOTest extends RockSimTestBase {
	
	@Test
	public void testDTO() throws Exception {
		JAXBContext binder = JAXBContext.newInstance(RockSimDocumentDTO.class);
		Marshaller marshaller = binder.createMarshaller();
		marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
		
		NoseConeDTO noseCone = new NoseConeDTO();
		noseCone.setBaseDia(10d);
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
		
		Assert.assertNotNull(ord);
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
		Assert.assertNotNull("Could not open tubeCouplerRocket.rkt", stream);
		OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(importedDocument);
		context.setMotorFinder(new DatabaseMotorFinder());
		loader.loadFromStream(context, new BufferedInputStream(stream));
		Rocket importedRocket = importedDocument.getRocket();

		// Test children counts
		List<RocketComponent> originalChildren = originalRocket.getAllChildren();
		List<RocketComponent> importedChildren = importedRocket.getAllChildren();
		assertEquals(" Number of total children doesn't match",
				originalChildren.size(), importedChildren.size());
		assertEquals(" Number of rocket children doesn't match", 1, importedRocket.getChildCount());
		AxialStage stage = (AxialStage) importedRocket.getChild(0);
		assertEquals(" Number of stage children doesn't match", 2, stage.getChildCount());
		BodyTube tube = (BodyTube) stage.getChild(1);
		assertEquals(" Number of body tube children doesn't match", 12, tube.getChildCount());

		// Test component names
		for (int i = 1; i < originalChildren.size(); i++) {
			assertEquals(" Child " + i + " does not match",
					originalChildren.get(i).getName(), importedChildren.get(i).getName());
		}

		stream.close();
		Files.delete(output);
	}

	private OpenRocketDocument makeTubeCouplerRocket() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);
		NoseCone noseCone = new NoseCone();
		noseCone.setName("Nose Cone");
		stage.addChild(noseCone);
		BodyTube tube = new BodyTube();
		tube.setName("Body Tube");
		stage.addChild(tube);
		TubeCoupler coupler = new TubeCoupler();
		coupler.setName("Tube coupler 1");
		tube.addChild(coupler);
		InnerTube innerTube = new InnerTube();
		innerTube.setName("Inner Tube");
		coupler.addChild(innerTube);
		TubeCoupler coupler2 =  new TubeCoupler();
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
		TubeCoupler coupler3 = new TubeCoupler();
		coupler3.setName("Tube Coupler 3");
		tube.addChild(coupler3);
		Parachute parachute2 = new Parachute();
		parachute2.setName("Parachute 2");
		coupler3.addChild(parachute2);

		return document;
	}
}
