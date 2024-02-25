package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.*;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test application of ComponentPresets to Streamer RocketComponents through
 * the Streamer.loadFromPreset mechanism.
 * 
 * Test Streamer is well defined.
 * 
 * Test calling setters on Streamer will clear the ComponentPreset.
 * 
 */
public class StreamerComponentTests extends BaseTestCase {

	ComponentPreset preset;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 20.0);
		presetspec.put(ComponentPreset.WIDTH, 2.0);
		Material m = Material.newMaterial(Material.Type.SURFACE, "testMaterial", 2.0, true);
		presetspec.put(ComponentPreset.MATERIAL, m);
		preset = ComponentPresetFactory.create(presetspec);
	}

	@Test
	public void testComponentType() {
		Streamer cr = new Streamer();

		assertSame(ComponentPreset.Type.STREAMER, cr.getPresetType());
	}

	@Test
	public void testLoadFromPresetIsSane() {
		Streamer cr = new Streamer();

		cr.loadPreset(preset);

		assertEquals(20.0, cr.getStripLength(), 0.0);
		assertEquals(2.0, cr.getStripWidth(), 0.0);
		assertEquals(2.0, cr.getLength(), 0.0);

		assertSame(preset.get(ComponentPreset.MATERIAL), cr.getMaterial());
		assertEquals(80.0, cr.getMass(), 0.05);
	}

	// TODO - test fails, could not find when running Ant
	/*
	 * @Test
	 * public void changeLengthClearsPreset() {
	 * Streamer cr = new Streamer();
	 * 
	 * cr.loadPreset(preset);
	 * 
	 * cr.setStripLength(1.0);
	 * 
	 * assertNull(cr.getPresetComponent());
	 * }
	 */

	@Test
	public void changeWidthClearsPreset() {
		Streamer cr = new Streamer();

		cr.loadPreset(preset);

		cr.setStripWidth(1.0);

		assertNull(cr.getPresetComponent());
	}

	@Test
	public void changeMaterialClearsPreset() {
		Streamer cr = new Streamer();

		cr.loadPreset(preset);

		cr.setMaterial(Material.newMaterial(Material.Type.SURFACE, "new", 1.0, true));

		assertNull(cr.getPresetComponent());
	}

}
