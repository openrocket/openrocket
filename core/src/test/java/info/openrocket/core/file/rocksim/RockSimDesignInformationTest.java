package info.openrocket.core.file.rocksim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.export.RockSimSaver;
import info.openrocket.core.file.rocksim.importt.RockSimLoader;
import info.openrocket.core.rocketcomponent.AxialStage;
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

	/**
	 * When RockSim's Cd calculation is off, a single-stage design flies with its fixed Cd, which must become the
	 * stage's Cd override, and the reverse on export.
	 */
	@Test
	public void importsAndExportsFixedCd() throws Exception {
		AxialStage stage = (AxialStage) load(design("<CalculateCD>0</CalculateCD><FixedCd>0.55</FixedCd>")).getChild(0);
		assertTrue(stage.isCDOverridden());
		assertTrue(stage.isSubcomponentsOverriddenCD());
		assertEquals(0.55, stage.getOverrideCD(), 1e-9);

		stage = (AxialStage) load(design("<CalculateCD>1</CalculateCD><FixedCd>0.55</FixedCd>")).getChild(0);
		assertFalse(stage.isCDOverridden());

		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		AxialStage sustainer = document.getRocket().getStage(0);
		sustainer.addChild(new BodyTube());
		sustainer.setCDOverridden(true);
		sustainer.setSubcomponentsOverriddenCD(true);
		sustainer.setOverrideCD(0.65);
		stage = (AxialStage) load(new RockSimSaver().marshalToRockSim(document)).getChild(0);
		assertTrue(stage.isCDOverridden());
		assertEquals(0.65, stage.getOverrideCD(), 1e-9);
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
