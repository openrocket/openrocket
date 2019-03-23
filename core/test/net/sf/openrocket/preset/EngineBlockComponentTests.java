package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

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
	
	@Before
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
	
	@Test
	public void changeLengthLeavesPreset() {
		EngineBlock eb = new EngineBlock();
		
		eb.loadPreset(preset);
		
		eb.setLength(1.0);
		
		assertSame(preset, eb.getPresetComponent());
	}
	
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
