package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.MassComponent;

import jakarta.xml.bind.JAXBContext;

/**
 * Tests the export of component mass overrides to RockSim.
 */
public class RockSimMassOverrideExportTest extends RockSimTestBase {

	/**
	 * RockSim's known mass of a part excludes its attached parts, so the parts inside an OpenRocket mass override
	 * that includes the subcomponents must be exported without mass.
	 */
	@Test
	public void exportsSubcomponentMassOverrideOnTheOverriddenComponentOnly() throws Exception {
		BodyTubeDTO body = exportBodyTube(true);
		assertEquals(1, body.getUseKnownCG());
		assertEquals(200.0, body.getKnownMass(), 1e-9);
		MassObjectDTO mass = (MassObjectDTO) body.getAttachedParts().get(0);
		assertEquals(1, mass.getUseKnownCG());
		assertEquals(0.0, mass.getKnownMass(), 1e-9);
	}

	/**
	 * An override of the component alone keeps the mass of its subcomponents.
	 */
	@Test
	public void exportsComponentMassOverrideWithSubcomponentMasses() throws Exception {
		BodyTubeDTO body = exportBodyTube(false);
		assertEquals(200.0, body.getKnownMass(), 1e-9);
		MassObjectDTO mass = (MassObjectDTO) body.getAttachedParts().get(0);
		assertEquals(50.0, mass.getKnownMass(), 1e-9);
	}

	private BodyTubeDTO exportBodyTube(boolean overrideSubcomponents) throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		BodyTube body = new BodyTube();
		body.setName("Body");
		body.setLength(0.5);
		body.setOuterRadius(0.03);
		document.getRocket().getStage(0).addChild(body);
		body.setMassOverridden(true);
		body.setOverrideMass(0.2);
		body.setSubcomponentsOverriddenMass(overrideSubcomponents);
		MassComponent mass = new MassComponent();
		mass.setComponentMass(0.05);
		body.addChild(mass);

		String result = new RockSimSaver().marshalToRockSim(document);
		RocketDesignDTO design = ((RockSimDocumentDTO) JAXBContext.newInstance(RockSimDocumentDTO.class)
				.createUnmarshaller().unmarshal(new StringReader(result))).getDesign().getDesign();
		return (BodyTubeDTO) design.getStage3().getExternalPart().get(0);
	}
}
