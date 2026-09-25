package info.openrocket.core.file.rasaero.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Rocket;
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
		assertEquals(RASAeroCommonConstants.RECOVERY_ALTITUDE, recovery.getEventType1());
		assertEquals(350 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE,
				recovery.getAltitude1());
	}
}
