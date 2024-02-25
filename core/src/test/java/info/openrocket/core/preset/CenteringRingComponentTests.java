package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.*;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test application of ComponentPresets to CenteringRing RocketComponents
 * through
 * the CenteringRing.loadFromPreset mechanism.
 * 
 * Test CenteringRing is well defined.
 * 
 * Test calling setters on CenteringRing will clear the ComponentPreset.
 * 
 */
public class CenteringRingComponentTests extends BaseTestCase {

	ComponentPreset preset;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.CENTERING_RING);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MASS, 100.0);
		preset = ComponentPresetFactory.create(presetspec);
	}

	@Test
	public void testComponentType() {
		CenteringRing cr = new CenteringRing();

		assertSame(ComponentPreset.Type.CENTERING_RING, cr.getPresetType());
	}

	@Test
	public void testLoadFromPresetIsSane() {
		CenteringRing cr = new CenteringRing();

		cr.loadPreset(preset);

		assertEquals(2.0, cr.getLength(), 0.0);
		assertEquals(1.0, cr.getOuterRadius(), 0.0);
		assertEquals(0.5, cr.getInnerRadius(), 0.0);

		assertFalse(cr.isOuterRadiusAutomatic());

		assertSame(preset.get(ComponentPreset.MATERIAL), cr.getMaterial());
		assertEquals(100.0, cr.getMass(), 0.05);
	}

	// TODO - test fails, could not find when running Ant
	/*
	 * @Test
	 * public void changeLengthLeavesPreset() {
	 * CenteringRing cr = new CenteringRing();
	 * 
	 * cr.loadPreset(preset);
	 * 
	 * cr.setLength(1.0);
	 * 
	 * assertSame(preset, cr.getPresetComponent());
	 * }
	 */

	@Test
	public void changeODClearsPreset() {
		CenteringRing cr = new CenteringRing();

		cr.loadPreset(preset);

		cr.setOuterRadius(2.0);

		assertNull(cr.getPresetComponent());
	}

	@Test
	public void changeIDClearsPreset() {
		CenteringRing cr = new CenteringRing();

		cr.loadPreset(preset);

		cr.setInnerRadius(0.75);

		assertNull(cr.getPresetComponent());
	}

	@Test
	public void changeThicknessClearsPreset() {
		CenteringRing cr = new CenteringRing();

		cr.loadPreset(preset);

		cr.setThickness(0.1);

		assertNull(cr.getPresetComponent());
	}

	@Test
	public void changeMaterialClearsPreset() {
		CenteringRing cr = new CenteringRing();

		cr.loadPreset(preset);

		cr.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));

		assertNull(cr.getPresetComponent());
	}

}
