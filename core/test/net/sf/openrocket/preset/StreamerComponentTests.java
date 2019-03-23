package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

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
	
	@Before
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
	
	@Test
	public void changeLengthClearsPreset() {
		Streamer cr = new Streamer();
		
		cr.loadPreset(preset);
		
		cr.setStripLength(1.0);
		
		assertNull(cr.getPresetComponent());
	}
	
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
