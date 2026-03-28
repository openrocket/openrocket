package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentChangeEventTest extends BaseTestCase {

	private ComponentChangeEvent createEvent(int type) {
		return new ComponentChangeEvent(new BodyTube(), type);
	}

	@Test
	public void testNonFunctionalChangeIsNonFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		assertTrue(event.isNonFunctionalChange());
		assertFalse(event.isFunctionalChange());
	}

	@Test
	public void testTextureChangeIsNonFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.TEXTURE_CHANGE);
		assertTrue(event.isNonFunctionalChange());
		assertFalse(event.isFunctionalChange());
	}

	@Test
	public void testGraphicChangeIsNonFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.GRAPHIC_CHANGE);
		assertTrue(event.isNonFunctionalChange());
		assertFalse(event.isFunctionalChange());
	}

	@Test
	public void testTextureWithNonFunctionalIsNonFunctional() {
		ComponentChangeEvent event = createEvent(
				ComponentChangeEvent.TEXTURE_CHANGE | ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		assertTrue(event.isNonFunctionalChange());
		assertFalse(event.isFunctionalChange());
	}

	@Test
	public void testMassChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.MASS_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}

	@Test
	public void testAerodynamicChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}

	@Test
	public void testTextureWithMassChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(
				ComponentChangeEvent.TEXTURE_CHANGE | ComponentChangeEvent.MASS_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}

	@Test
	public void testTreeChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.TREE_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}

	@Test
	public void testMotorChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.MOTOR_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}

	@Test
	public void testEventChangeIsFunctional() {
		ComponentChangeEvent event = createEvent(ComponentChangeEvent.EVENT_CHANGE);
		assertFalse(event.isNonFunctionalChange());
		assertTrue(event.isFunctionalChange());
	}
}
