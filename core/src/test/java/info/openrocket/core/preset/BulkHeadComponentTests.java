package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.*;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test application of ComponentPresets to Bulkhead RocketComponents through
 * the Bulkhead.loadFromPreset mechanism.
 * 
 * Test Bulkhead is well defined.
 * 
 * Test calling setters on Bulkhead will clear the ComponentPreset.
 * 
 */
public class BulkHeadComponentTests extends BaseTestCase {

	ComponentPreset preset;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.BULK_HEAD);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.MASS, 100.0);
		preset = ComponentPresetFactory.create(presetspec);
	}

	@Test
	public void testComponentType() {
		Bulkhead bt = new Bulkhead();

		assertSame(ComponentPreset.Type.BULK_HEAD, bt.getPresetType());
	}

	@Test
	public void testLoadFromPresetIsSane() {
		Bulkhead bt = new Bulkhead();

		bt.loadPreset(preset);

		assertEquals(2.0, bt.getLength(), 0.0);
		assertEquals(1.0, bt.getOuterRadius(), 0.0);

		assertFalse(bt.isOuterRadiusAutomatic());

		assertSame(preset.get(ComponentPreset.MATERIAL), bt.getMaterial());
		assertEquals(100.0, bt.getMass(), 0.05);
	}

	// TODO - test fails, could not find when running Ant
	/*
	 * @Test
	 * public void changeLengthLeavesPreset() {
	 * Bulkhead bt = new Bulkhead();
	 * 
	 * bt.loadPreset(preset);
	 * 
	 * bt.setLength(1.0);
	 * 
	 * assertSame(preset, bt.getPresetComponent());
	 * }
	 */

	@Test
	public void changeODClearsPreset() {
		Bulkhead bt = new Bulkhead();

		bt.loadPreset(preset);

		bt.setOuterRadius(2.0);

		assertNull(bt.getPresetComponent());
	}

	@Test
	public void changeODAutomaticClearsPreset() {
		Bulkhead bt = new Bulkhead();

		bt.loadPreset(preset);

		bt.setOuterRadiusAutomatic(true);

		assertNull(bt.getPresetComponent());
	}

	@Test
	public void changeMaterialClearsPreset() {
		Bulkhead bt = new Bulkhead();

		bt.loadPreset(preset);

		bt.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));

		assertNull(bt.getPresetComponent());
	}

}
