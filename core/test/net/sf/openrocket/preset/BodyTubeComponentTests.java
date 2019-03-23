package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Test application of ComponentPresets to BodyTube RocketComponents through
 * the BodyTube.loadFromPreset mechanism.
 * 
 * Test BodyTube is well defined.
 * 
 * Test calling setters on BodyTube will clear the ComponentPreset.
 * 
 */
public class BodyTubeComponentTests extends BaseTestCase {
	
	ComponentPreset preset;
	
	@Before
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
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
		BodyTube bt = new BodyTube();
		
		assertSame(ComponentPreset.Type.BODY_TUBE, bt.getPresetType());
	}
	
	@Test
	public void testLoadFromPresetIsSane() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		assertEquals(2.0, bt.getLength(), 0.0);
		assertEquals(1.0, bt.getOuterRadius(), 0.0);
		assertEquals(1.0, bt.getAftRadius(), 0.0);
		assertEquals(0.5, bt.getInnerRadius(), 0.0);
		
		assertFalse(bt.isAftRadiusAutomatic());
		assertFalse(bt.isFilled());
		assertFalse(bt.isForeRadiusAutomatic());
		assertFalse(bt.isOuterRadiusAutomatic());
		
		assertSame(preset.get(ComponentPreset.MATERIAL), bt.getMaterial());
		assertEquals(100.0, bt.getMass(), 0.05);
	}
	
	@Test
	public void changeLengthLeavesPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setLength(1.0);
		
		assertSame(preset, bt.getPresetComponent());
	}
	
	@Test
	public void changeODClearsPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setOuterRadius(2.0);
		
		assertNull(bt.getPresetComponent());
	}
	
	@Test
	public void changeIDClearsPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setInnerRadius(0.75);
		
		assertNull(bt.getPresetComponent());
	}
	
	@Test
	public void changeThicknessClearsPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setThickness(0.1);
		
		assertNull(bt.getPresetComponent());
	}
	
	@Test
	public void changeMaterialClearsPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));
		
		assertNull(bt.getPresetComponent());
	}
	
	@Test
	public void changeFinishLeavesPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setFinish(Finish.POLISHED);
		
		assertSame(preset, bt.getPresetComponent());
	}
	
	@Test
	public void changeFillClearsPreset() {
		BodyTube bt = new BodyTube();
		
		bt.loadPreset(preset);
		
		bt.setFilled(true);
		
		assertNull(bt.getPresetComponent());
	}
}
