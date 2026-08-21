package info.openrocket.core.rocketcomponent;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

public class UUIDSearchTest extends BaseTestCase {

	/**
	 * find the first child component of the given type
	 */
	RocketComponent findComponent(RocketComponent parent, Class componentClass) {
		RocketComponent ret = null;
		for (RocketComponent child : parent.getChildren()) {
			if (child.getClass() == componentClass) {
				ret = child;
			}
		}
		return ret;
	}
	
	/**
	 * Test search for rocket component using UUID
	 */
	@Test
	public void testUUIDSearch() {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();

		// Get the fin set
		RocketComponent stage = findComponent(rocket, AxialStage.class);
		assertNotNull(stage, "Failed to find stage");
		
		RocketComponent noseCone = findComponent(stage, NoseCone.class);
		assertNotNull(noseCone, "Failed to find NoseCone");

		// If I search for the NoseCone using its UUID I should get it back
		UUID noseConeID = noseCone.getID();
		assertEquals(noseCone, rocket.findComponent(noseConeID), "UUID search didn't find NoseCone");

		// If I remove the NoseCone from the rocket and search for it by UUID, I should get a RemovedComponent back
		assertTrue(stage.removeChild(noseCone, false), "failed to remove NoseCone");
		assertEquals(rocket.findComponent(noseConeID), RocketComponent.REMOVED, "Search for NoseCone failed to find REMOVED");

		// If I explicitly search for the null UUID I should get the RemovedComponent
		assertEquals(rocket.findComponent(new UUID(0, 0)), RocketComponent.REMOVED, "Failed to find REMOVED");
	}
}

	
