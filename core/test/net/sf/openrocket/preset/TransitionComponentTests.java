package net.sf.openrocket.preset;

import static org.junit.Assert.*;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Test application of ComponentPresets to Transition RocketComponents through
 * the Transition.loadFromPreset mechanism.
 * 
 * Test Transition is well defined.
 * 
 * Test calling setters on Transition will clear the ComponentPreset.
 * 
 */
public class TransitionComponentTests extends BaseTestCase {
	
	ComponentPreset preset;
	
	@Before
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FORE_OUTER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.AFT_SHOULDER_LENGTH, 1.0);
		presetspec.put(ComponentPreset.AFT_SHOULDER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.FORE_SHOULDER_LENGTH, 1.0);
		presetspec.put(ComponentPreset.FORE_SHOULDER_DIAMETER, 0.5);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		preset = ComponentPresetFactory.create(presetspec);
	}
	
	@Test
	public void testComponentType() {
		Transition tr = new Transition();
		
		assertSame(ComponentPreset.Type.TRANSITION, tr.getPresetType());
	}
	
	@Test
	public void testLoadFromPresetIsSane() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		assertEquals(2.0, tr.getLength(), 0.0);
		assertSame(Transition.Shape.CONICAL, tr.getType());
		assertEquals(1.0, tr.getAftRadius(), 0.0);
		assertEquals(1.0, tr.getForeShoulderLength(), 0.0);
		assertEquals(0.25, tr.getForeShoulderRadius(), 0.0);
		assertEquals(0.25, tr.getForeShoulderThickness(), 0.0);
		assertEquals(1.0, tr.getAftShoulderLength(), 0.0);
		assertEquals(0.5, tr.getAftShoulderRadius(), 0.0);
		assertEquals(0.5, tr.getAftShoulderThickness(), 0.0);
		
		assertFalse(tr.isForeRadiusAutomatic());
		assertFalse(tr.isAftRadiusAutomatic());
		assertTrue(tr.isFilled());
		
		assertSame(preset.get(ComponentPreset.MATERIAL), tr.getMaterial());
		assertEquals(100.0, tr.getMass(), 1.0);
	}
	
	@Test
	public void changeLengthClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setLength(1.0);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeAftRadiusClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setAftRadius(2.0);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeAftRadiusAutomaticClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setAftRadiusAutomatic(true);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeForeRadiusClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setForeRadius(2.0);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeForeRadiusAutomaticClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setForeRadiusAutomatic(true);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeForeShoulderRadiusClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setForeShoulderRadius(2.0);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeAftShoulderRadiusClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setAftShoulderRadius(2.0);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeAftSholderLengthLeavesPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setAftShoulderLength(2.0);
		
		assertSame(preset, tr.getPresetComponent());
	}
	
	@Test
	public void changeThicknessClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setThickness(0.1);
		
		assertNull(tr.getPresetComponent());
	}
	
	
	@Test
	public void changeFilledClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setFilled(false);
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeMaterialClearsPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setMaterial(Material.newMaterial(Material.Type.BULK, "new", 1.0, true));
		
		assertNull(tr.getPresetComponent());
	}
	
	@Test
	public void changeFinishLeavesPreset() {
		Transition tr = new Transition();
		
		tr.loadPreset(preset);
		
		tr.setFinish(Finish.POLISHED);
		
		assertSame(preset, tr.getPresetComponent());
	}
	
}
