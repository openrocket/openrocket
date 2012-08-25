package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Test application of ComponentPresets to TubeCoupler RocketComponents through
 * the TubeCoupler.loadFromPreset mechanism.
 * 
 * Test TubeCoupler is well defined.
 * 
 * Test calling setters on TubeCoupler will clear the ComponentPreset.
 * 
 */
public class TubeCouplerComponentTests extends BaseTestCase {
	
	ComponentPreset preset;
	
	@Before
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TUBE_COUPLER);
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
		TubeCoupler tc = new TubeCoupler();
		
		assertSame(ComponentPreset.Type.TUBE_COUPLER, tc.getPresetType());
	}
	
	@Test
	public void testLoadFromPresetIsSane() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		assertEquals(2.0, tc.getLength(), 0.0);
		assertEquals(1.0, tc.getOuterRadius(), 0.0);
		assertEquals(0.5, tc.getInnerRadius(), 0.0);
		
		assertFalse(tc.isInnerRadiusAutomatic());
		assertFalse(tc.isOuterRadiusAutomatic());
		
		assertSame(preset.get(ComponentPreset.MATERIAL), tc.getMaterial());
		assertEquals(100.0, tc.getMass(), 0.05);
	}
	
	@Test
	public void changeLengthLeavesPreset() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		tc.setLength(1.0);
		
		assertSame(preset, tc.getPresetComponent());
	}
	
	@Test
	public void changeODClearsPreset() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		tc.setOuterRadius(2.0);
		
		assertNull(tc.getPresetComponent());
	}
	
	@Test
	public void changeIDClearsPreset() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		tc.setInnerRadius(0.75);
		
		assertNull(tc.getPresetComponent());
	}
	
	@Test
	public void changeThicknessClearsPreset() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		tc.setThickness(0.1);
		
		assertNull(tc.getPresetComponent());
	}
	
	@Test
	public void changeMaterialClearsPreset() {
		TubeCoupler tc = new TubeCoupler();
		
		tc.loadPreset(preset);
		
		tc.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));
		
		assertNull(tc.getPresetComponent());
	}
	
}
