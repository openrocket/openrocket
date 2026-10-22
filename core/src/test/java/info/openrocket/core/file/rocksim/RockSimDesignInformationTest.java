package info.openrocket.core.file.rocksim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.export.RockSimSaver;
import info.openrocket.core.file.rocksim.importt.RockSimLoader;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BaseTestCase;

/**
 * Tests the import and export of RockSim's rocket design information.
 */
public class RockSimDesignInformationTest extends BaseTestCase {

	/**
	 * RockSim's design comments, designer credits and revision history must survive an import and an export.
	 */
	@Test
	public void importsAndExportsDesignMetadata() throws Exception {
		Rocket imported = load(design("<Comments>KV-64</Comments><Designer>Semroc Astronautics Corp</Designer>"
				+ "<Revisions>2-21-2010: new nose cone</Revisions>"));
		assertEquals("KV-64", imported.getComment());
		assertEquals("Semroc Astronautics Corp", imported.getDesigner());
		assertEquals("2-21-2010: new nose cone", imported.getRevision());

		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		rocket.getStage(0).addChild(new BodyTube());
		rocket.setComment("A comment");
		rocket.setDesigner("A designer");
		rocket.setRevision("A revision");
		Rocket roundTripped = load(new RockSimSaver().marshalToRockSim(document));
		assertEquals("A comment", roundTripped.getComment());
		assertEquals("A designer", roundTripped.getDesigner());
		assertEquals("A revision", roundTripped.getRevision());
	}

	static String design(String designElements) {
		return "<RockSimDocument><FileVersion>4</FileVersion><DesignInformation><RocketDesign>"
				+ "<Name>Test</Name>" + designElements + "<StageCount>1</StageCount>"
				+ "<Stage3Parts><BodyTube><Name>Tube</Name><OD>40</OD><ID>38</ID><Len>300</Len></BodyTube></Stage3Parts>"
				+ "</RocketDesign></DesignInformation></RockSimDocument>";
	}

	static Rocket load(String rockSimDocument) throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(document);
		context.setMotorFinder(new DatabaseMotorFinder());
		new RockSimLoader().loadFromStream(context,
				new ByteArrayInputStream(rockSimDocument.getBytes(StandardCharsets.UTF_8)), null);
		return document.getRocket();
	}
}
