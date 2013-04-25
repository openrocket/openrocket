package net.sf.openrocket.preset;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

/**
 * Test construction of TRANSITION type ComponentPresets based on TypedPropertyMap through the
 * ComponentPresetFactory.create() method.
 * 
 * Ensure required properties are populated
 * 
 * Ensure any computed values are correctly computed.
 * 
 */
public class TransitionPresetTests extends BaseTestCase {
	
	@Test
	public void testManufacturerRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.MANUFACTURER,
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.FORE_OUTER_DIAMETER
					},
					new String[] {
							"No Manufacturer specified",
							"No PartNo specified",
							"No Length specified",
							"No AftOuterDiameter specified",
							"No ForeOuterDiameter specified"
					}
					);
		}
	}
	
	@Test
	public void testPartNoRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.FORE_OUTER_DIAMETER
					},
					new String[] {
							"No PartNo specified",
							"No Length specified",
							"No AftOuterDiameter specified",
							"No ForeOuterDiameter specified"
					}
					);
		}
	}
	
	@Test
	public void testLengthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.FORE_OUTER_DIAMETER
					},
					new String[] {
							"No Length specified",
							"No AftOuterDiameter specified",
							"No ForeOuterDiameter specified"
					}
					);
		}
	}
	
	@Test
	public void testAftOuterDiameterRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.FORE_OUTER_DIAMETER
					},
					new String[] {
							"No AftOuterDiameter specified",
							"No ForeOuterDiameter specified"
					}
					);
		}
	}
	
	
	@Test
	public void testForeOuterDiameterRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
			presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
					ComponentPreset.FORE_OUTER_DIAMETER
					},
					new String[] {
					"No ForeOuterDiameter specified"
					}
					);
		}
	}
	
	@Test
	public void testComputeDensityNoMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FORE_OUTER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// constants put into the presetspec above.
		double volume = /*base area*/Math.PI * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5);
		
		volume *= 2.0 /* times height *// 3.0; /* one third */
		
		double density = 100.0 / volume;
		
		assertEquals("[material:TransitionCustom]", preset.get(ComponentPreset.MATERIAL).getName());
		
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.01 * density);
	}
	
	@Test
	public void testMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FORE_OUTER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals("test", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(2.0, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);
		
	}
	
	@Test
	public void testComputeDensityWithMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FORE_OUTER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// constants put into the presetspec above.
		double totvolume = /*base area*/Math.PI;
		
		totvolume *= 4.0 /* times height *// 3.0; /* one third */
		
		double uppervolume = /*fore area*/Math.PI * 0.5 * 0.5;
		uppervolume *= 2.0 /* times height *// 3.0; /* one third */
		
		double volume = totvolume - uppervolume;
		
		double density = 100.0 / volume;
		
		assertEquals("[material:test]", preset.get(ComponentPreset.MATERIAL).getName());
		
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.01 * density);
	}
	
}
