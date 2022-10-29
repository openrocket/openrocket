package net.sf.openrocket.file.rocksim.export;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.rocksim.importt.RockSimLoader;
import net.sf.openrocket.file.rocksim.importt.RockSimLoaderTest;
import net.sf.openrocket.file.rocksim.importt.RockSimTestBase;

import org.junit.Assert;
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
		OpenRocketDocument ord = RockSimLoaderTest.loadRockSimRocket3(new RockSimLoader());
		
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
}
