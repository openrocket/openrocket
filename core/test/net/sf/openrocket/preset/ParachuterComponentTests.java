package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Test application of ComponentPresets to Parachute RocketComponents through
 * the Parachute.loadFromPreset mechanism.
 * 
 * Test Parachute is well defined.
 * 
 * Test calling setters on Parachute will clear the ComponentPreset.
 * 
 */
public class ParachuterComponentTests extends BaseTestCase {
	
	ComponentPreset preset;
	
	@Before
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.DIAMETER, 20.0);
		presetspec.put(ComponentPreset.LINE_COUNT, 8);
		presetspec.put(ComponentPreset.LINE_LENGTH, 12.0);
		Material m = Material.newMaterial(Material.Type.SURFACE, "testMaterial", 2.0, true);
		presetspec.put(ComponentPreset.MATERIAL, m);
		m = Material.newMaterial(Material.Type.LINE, "testLineMaterial", 3, true);
		presetspec.put(ComponentPreset.LINE_MATERIAL, m);
		preset = ComponentPresetFactory.create(presetspec);
	}
	
	@Test
	public void testComponentType() {
		Parachute cr = new Parachute();
		
		assertSame(ComponentPreset.Type.PARACHUTE, cr.getPresetType());
	}
	
	@Test
	public void testLoadFromPresetIsSane() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		assertEquals(20.0, cr.getDiameter(), 0.0);
		assertEquals(8, cr.getLineCount(), 0.0);
		assertEquals(12.0, cr.getLineLength(), 0.0);
		
		assertSame(preset.get(ComponentPreset.MATERIAL), cr.getMaterial());
		assertSame(preset.get(ComponentPreset.LINE_MATERIAL), cr.getLineMaterial());
	}
	
	@Test
	public void changeDiameterClearsPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setDiameter(1.0);
		
		assertNull(cr.getPresetComponent());
	}
	
	@Test
	public void changeAreaClearsPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setArea(1.0);
		
		assertNull(cr.getPresetComponent());
	}
	
	@Test
	public void changeLineCountClearsPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setLineCount(12);
		
		assertNull(cr.getPresetComponent());
	}
	
	@Test
	public void changeLineLengthLeavesPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setLineLength(24);
		
		assertSame(preset, cr.getPresetComponent());
	}
	
	@Test
	public void changeMaterialClearsPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setMaterial(Material.newMaterial(Material.Type.SURFACE, "new", 1.0, true));
		
		assertNull(cr.getPresetComponent());
	}
	
	@Test
	public void changeLineMaterialLeavesPreset() {
		Parachute cr = new Parachute();
		
		cr.loadPreset(preset);
		
		cr.setLineMaterial(Material.newMaterial(Material.Type.LINE, "new", 1.0, true));
		
		assertSame(preset, cr.getPresetComponent());
	}
	
}
