package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.*;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.ExternalComponent.Finish;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Transition;

import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test application of ComponentPresets to NoseCone RocketComponents through
 * the NoseCone.loadFromPreset mechanism.
 * 
 * Test NoseCone is well defined.
 * 
 * Test calling setters on NoseCone will clear the ComponentPreset.
 * 
 */
public class NoseConeComponentTests extends BaseTestCase {

	ComponentPreset preset;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.AFT_SHOULDER_LENGTH, 1.0);
		presetspec.put(ComponentPreset.AFT_SHOULDER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		preset = ComponentPresetFactory.create(presetspec);
	}

	@Test
	public void testComponentType() {
		NoseCone nc = new NoseCone();

		assertSame(ComponentPreset.Type.NOSE_CONE, nc.getPresetType());
	}

	@Test
	public void testLoadFromPresetIsSane() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		assertEquals(2.0, nc.getLength(), 0.0);
		assertSame(Transition.Shape.CONICAL, nc.getShapeType());
		assertEquals(1.0, nc.getAftRadius(), 0.0);
		assertEquals(0.0, nc.getForeShoulderLength(), 0.0);
		assertEquals(0.0, nc.getForeShoulderRadius(), 0.0);
		assertEquals(1.0, nc.getAftShoulderLength(), 0.0);
		assertEquals(0.5, nc.getAftShoulderRadius(), 0.0);
		assertEquals(0.5, nc.getAftShoulderThickness(), 0.0);

		assertFalse(nc.isForeRadiusAutomatic());
		assertFalse(nc.isAftRadiusAutomatic());
		assertTrue(nc.isFilled());

		assertSame(preset.get(ComponentPreset.MATERIAL), nc.getMaterial());
		assertEquals(100.0, nc.getMass(), 0.05);
	}

	@Test
	public void changeLengthClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setLength(1.0);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeAftRadiusClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setAftRadius(2.0);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeAftRadiusAutomaticClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setAftRadiusAutomatic(true);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeAftShoulderRadiusClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setAftShoulderRadius(2.0);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeAftSholderLengthLeavesPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setAftShoulderLength(2.0);

		assertSame(preset, nc.getPresetComponent());
	}

	@Test
	public void changeThicknessClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setThickness(0.1);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeFilledClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setFilled(false);

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeMaterialClearsPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));

		assertNull(nc.getPresetComponent());
	}

	@Test
	public void changeFinishLeavesPreset() {
		NoseCone nc = new NoseCone();

		nc.loadPreset(preset);

		nc.setFinish(Finish.POLISHED);

		assertSame(preset, nc.getPresetComponent());
	}

}
