package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.importt.RockSimLoader;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;

import jakarta.xml.bind.JAXBContext;

/**
 * Tests the export of stage mass and CG overrides to RockSim.
 */
public class RockSimStageOverrideExportTest extends RockSimTestBase {

	/**
	 * RockSim's stage overrides are all-or-nothing, so overriding only the booster must still export the mass of
	 * every stage and the CG of every stack of stages. Importing the exported design must reproduce the stack
	 * masses and CGs of the original rocket.
	 */
	@Test
	public void exportsMassAndCGOfEveryStageWhenOneStageIsOverridden() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage sustainer = rocket.getStage(0);
		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.2);
		noseCone.setAftRadius(0.03);
		sustainer.addChild(noseCone);
		sustainer.addChild(makeBodyTube(0.6));

		AxialStage booster = new AxialStage();
		booster.setName("Booster");
		rocket.addChild(booster);
		booster.addChild(makeBodyTube(0.4));
		booster.setMassOverridden(true);
		booster.setSubcomponentsOverriddenMass(true);
		booster.setOverrideMass(0.3);
		booster.setCGOverridden(true);
		booster.setSubcomponentsOverriddenCG(true);
		booster.setOverrideCGX(0.15);

		RigidBody sustainerStack = calculateStack(rocket, 1);
		RigidBody fullStack = calculateStack(rocket, 2);

		String exported = new RockSimSaver().marshalToRockSim(document);
		RocketDesignDTO design = ((RockSimDocumentDTO) JAXBContext.newInstance(RockSimDocumentDTO.class)
				.createUnmarshaller().unmarshal(new StringReader(exported))).getDesign().getDesign();

		assertEquals(1, design.getUseKnownMass());
		assertEquals(sustainerStack.getMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS,
				design.getStage3Mass(), 1e-6);
		assertEquals(0.3 * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS, design.getStage2Mass(), 1e-6);
		assertEquals(sustainerStack.getCM().getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
				design.getStage3CG(), 1e-6);
		assertEquals(fullStack.getCM().getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
				design.getStage32CG(), 1e-6);

		OpenRocketDocument imported = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(imported);
		context.setMotorFinder(new DatabaseMotorFinder());
		new RockSimLoader().loadFromStream(context,
				new ByteArrayInputStream(exported.getBytes(StandardCharsets.UTF_8)), null);
		Rocket importedRocket = imported.getRocket();

		assertEquals(sustainerStack.getMass(), calculateStack(importedRocket, 1).getMass(), 1e-6);
		assertEquals(sustainerStack.getCM().getX(), calculateStack(importedRocket, 1).getCM().getX(), 1e-6);
		assertEquals(fullStack.getMass(), calculateStack(importedRocket, 2).getMass(), 1e-6);
		assertEquals(fullStack.getCM().getX(), calculateStack(importedRocket, 2).getCM().getX(), 1e-6);
	}

	private BodyTube makeBodyTube(double length) {
		BodyTube tube = new BodyTube();
		tube.setLength(length);
		tube.setOuterRadius(0.03);
		tube.setThickness(0.001);
		return tube;
	}

	private RigidBody calculateStack(Rocket rocket, int stageCount) {
		FlightConfiguration configuration = new FlightConfiguration(rocket);
		configuration.clearAllStages();
		for (int i = 0; i < stageCount; i++) {
			configuration._setStageActive(rocket.getChild(i).getStageNumber(), true);
		}
		return MassCalculator.calculateStructure(configuration);
	}
}
