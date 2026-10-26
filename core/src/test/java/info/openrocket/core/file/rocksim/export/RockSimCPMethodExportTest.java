package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;

import jakarta.xml.bind.JAXBContext;

/**
 * Tests the CP method that the RockSim export selects.
 */
public class RockSimCPMethodExportTest extends RockSimTestBase {

	/**
	 * RockSim only supports the Barrowman equations for fin sets with 3, 4 or 6 fins, so other designs must use
	 * RockSim's own stability equations.
	 */
	@Test
	public void usesRockSimEquationsWhenBarrowmanIsNotSupported() throws Exception {
		RocketDesignDTO design = exportWithFins(5);
		assertEquals(RocketDesignDTO.CP_METHOD_ROCKSIM, design.getCPCalcFlags());
		assertEquals(RocketDesignDTO.CP_METHOD_ROCKSIM, design.getCPSimFlags());
	}

	@Test
	public void usesBarrowmanEquationsForFourFins() throws Exception {
		RocketDesignDTO design = exportWithFins(4);
		assertEquals(RocketDesignDTO.CP_METHOD_BARROWMAN, design.getCPCalcFlags());
		assertEquals(RocketDesignDTO.CP_METHOD_BARROWMAN, design.getCPSimFlags());
	}

	private RocketDesignDTO exportWithFins(int finCount) throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		BodyTube body = new BodyTube();
		body.setLength(0.5);
		body.setOuterRadius(0.03);
		document.getRocket().getStage(0).addChild(body);
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(finCount);
		body.addChild(fins);

		String result = new RockSimSaver().marshalToRockSim(document);
		return ((RockSimDocumentDTO) JAXBContext.newInstance(RockSimDocumentDTO.class).createUnmarshaller()
				.unmarshal(new StringReader(result))).getDesign().getDesign();
	}
}
