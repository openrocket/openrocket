package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

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
	
	@Before
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
	
	@Test
	public void changeLengthLeavesPreset() {
		Bulkhead bt = new Bulkhead();
		
		bt.loadPreset(preset);
		
		bt.setLength(1.0);
		
		assertSame(preset, bt.getPresetComponent());
	}
	
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
