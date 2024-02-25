package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.*;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test application of ComponentPresets to EngineBlock RocketComponents through
 * the EngineBlock.loadFromPreset mechanism.
 * 
 * Test EngineBlock is well defined.
 * 
 * Test calling setters on EngineBlock will clear the ComponentPreset.
 * 
 */
public class EngineBlockComponentTests extends BaseTestCase {

	ComponentPreset preset;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
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
		EngineBlock eb = new EngineBlock();

		assertSame(ComponentPreset.Type.ENGINE_BLOCK, eb.getPresetType());
	}

	@Test
	public void testLoadFromPresetIsSane() {
		EngineBlock eb = new EngineBlock();

		eb.loadPreset(preset);

		assertEquals(2.0, eb.getLength(), 0.0);
		assertEquals(1.0, eb.getOuterRadius(), 0.0);
		assertEquals(0.5, eb.getInnerRadius(), 0.0);

		assertFalse(eb.isOuterRadiusAutomatic());

		assertSame(preset.get(ComponentPreset.MATERIAL), eb.getMaterial());
		assertEquals(100.0, eb.getMass(), 0.05);
	}

	// TODO - test fails, could not find when running Ant
	/*
	 * @Test
	 * public void changeLengthLeavesPreset() {
	 * EngineBlock eb = new EngineBlock();
	 * 
	 * eb.loadPreset(preset);
	 * 
	 * eb.setLength(1.0);
	 * 
	 * assertSame(preset, eb.getPresetComponent());
	 * }
	 */

	@Test
	public void changeODClearsPreset() {
		EngineBlock eb = new EngineBlock();

		eb.loadPreset(preset);

		eb.setOuterRadius(2.0);

		assertNull(eb.getPresetComponent());
	}

	@Test
	public void changeIDClearsPreset() {
		EngineBlock eb = new EngineBlock();

		eb.loadPreset(preset);

		eb.setInnerRadius(0.75);

		assertNull(eb.getPresetComponent());
	}

	@Test
	public void changeThicknessClearsPreset() {
		EngineBlock eb = new EngineBlock();

		eb.loadPreset(preset);

		eb.setThickness(0.1);

		assertNull(eb.getPresetComponent());
	}

	@Test
	public void changeMaterialClearsPreset() {
		EngineBlock eb = new EngineBlock();

		eb.loadPreset(preset);

		eb.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));

		assertNull(eb.getPresetComponent());
	}

}
