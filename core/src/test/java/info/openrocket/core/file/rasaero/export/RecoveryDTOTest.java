package info.openrocket.core.file.rasaero.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.util.BaseTestCase;

/**
 * Tests conversion of OpenRocket recovery settings to RASAero recovery settings.
 */
public class RecoveryDTOTest extends BaseTestCase {

	/**
	 * A flight configuration override must take precedence over the recovery component's default
	 * deployment configuration during export.
	 */
	@Test
	public void usesSelectedFlightConfigurationDeploymentOverride() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		BodyTube bodyTube = new BodyTube();
		Parachute parachute = new Parachute();
		rocket.addChild(stage);
		stage.addChild(bodyTube);
		bodyTube.addChild(parachute);

		// The component default is incompatible with RASAero.
		parachute.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.EJECTION);

		FlightConfigurationId configurationId = new FlightConfigurationId();
		rocket.createFlightConfiguration(configurationId);
		DeploymentConfiguration override = new DeploymentConfiguration();
		override.setDeployEvent(DeploymentConfiguration.DeployEvent.ALTITUDE);
		override.setDeployAltitude(350);
		parachute.getDeploymentConfigurations().set(configurationId, override);
		rocket.setSelectedConfiguration(configurationId);

		ErrorSet errors = new ErrorSet();
		RecoveryDTO recovery = new RecoveryDTO(rocket, new WarningSet(), errors);

		assertEquals(0, errors.size());
		assertFalse(recovery.getEvent1());
		assertTrue(recovery.getEvent2());
		assertEquals(RASAeroCommonConstants.RECOVERY_ALTITUDE, recovery.getEventType2());
		assertEquals(350 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE,
				recovery.getAltitude2());
	}

	/**
	 * RASAero deploys device 1 at apogee and device 2 at altitude 2, so a main parachute that comes before
	 * the drogue in the component tree must still be exported as device 2, with its own CD.
	 */
	@Test
	public void exportsApogeeDeviceFirstAndAltitudeDeviceSecond() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		BodyTube bodyTube = new BodyTube();
		rocket.addChild(stage);
		stage.addChild(bodyTube);

		Parachute main = new Parachute();
		main.setDiameter(1.0);
		main.setCD(1.5);
		main.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.ALTITUDE);
		main.getDeploymentConfigurations().getDefault().setDeployAltitude(150);
		bodyTube.addChild(main);

		Parachute drogue = new Parachute();
		drogue.setDiameter(0.3);
		drogue.setCD(0.8);
		drogue.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.APOGEE);
		bodyTube.addChild(drogue);

		ErrorSet errors = new ErrorSet();
		WarningSet warnings = new WarningSet();
		RecoveryDTO recovery = new RecoveryDTO(rocket, warnings, errors);

		assertEquals(0, errors.size());
		assertEquals(0, warnings.size());
		assertTrue(recovery.getEvent1());
		assertEquals(RASAeroCommonConstants.DEPLOYMENT_APOGEE, recovery.getEventType1());
		assertEquals(0.3 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, recovery.getSize1(), 1e-9);
		assertEquals(0.8, recovery.getCD1(), 1e-9);
		assertTrue(recovery.getEvent2());
		assertEquals(RASAeroCommonConstants.RECOVERY_ALTITUDE, recovery.getEventType2());
		assertEquals(150 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE, recovery.getAltitude2(), 1e-9);
		assertEquals(1.0 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, recovery.getSize2(), 1e-9);
		assertEquals(1.5, recovery.getCD2(), 1e-9);
	}

	/**
	 * A parachute deployed by the motor's ejection charge is exported as an apogee deployment, with a
	 * warning, instead of a recovery device that never deploys.
	 */
	@Test
	public void exportsEjectionDeploymentAsApogeeWithWarning() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		BodyTube bodyTube = new BodyTube();
		Parachute parachute = new Parachute();
		rocket.addChild(stage);
		stage.addChild(bodyTube);
		bodyTube.addChild(parachute);
		parachute.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.EJECTION);

		ErrorSet errors = new ErrorSet();
		WarningSet warnings = new WarningSet();
		RecoveryDTO recovery = new RecoveryDTO(rocket, warnings, errors);

		assertEquals(0, errors.size());
		assertEquals(1, warnings.size());
		assertTrue(recovery.getEvent1());
		assertEquals(RASAeroCommonConstants.DEPLOYMENT_APOGEE, recovery.getEventType1());
		assertFalse(recovery.getEvent2());
	}

	/**
	 * Recovery devices may be anywhere in a stage, for example in the nose cone. RASAero has no streamers, so a
	 * streamer gives a warning.
	 */
	@Test
	public void findsParachutesAnywhereInAStageAndWarnsAboutStreamers() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		NoseCone noseCone = new NoseCone();
		BodyTube bodyTube = new BodyTube();
		rocket.addChild(stage);
		stage.addChild(noseCone);
		stage.addChild(bodyTube);

		Parachute parachute = new Parachute();
		parachute.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.APOGEE);
		noseCone.addChild(parachute);
		bodyTube.addChild(new Streamer());

		ErrorSet errors = new ErrorSet();
		WarningSet warnings = new WarningSet();
		RecoveryDTO recovery = new RecoveryDTO(rocket, warnings, errors);

		assertEquals(0, errors.size());
		assertEquals(1, warnings.size());
		assertTrue(recovery.getEvent1());
		assertEquals(RASAeroCommonConstants.DEPLOYMENT_APOGEE, recovery.getEventType1());
	}
}
