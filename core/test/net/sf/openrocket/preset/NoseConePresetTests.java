package net.sf.openrocket.preset;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

/**
 * Test construction of NOSE_CONE type ComponentPresets based on TypedPropertyMap through the
 * ComponentPresetFactory.create() method.
 * 
 * Ensure required properties are populated
 * 
 * Ensure any computed values are correctly computed.
 * 
 */
public class NoseConePresetTests extends BaseTestCase {
	
	@Test
	public void testManufacturerRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.MANUFACTURER,
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.SHAPE
					},
					new String[] {
							"No Manufacturer specified",
							"No PartNo specified",
							"No Length specified",
							"No AftOuterDiameter specified",
							"No Shape specified"
					}
					);
		}
	}
	
	@Test
	public void testPartNoRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.SHAPE
					},
					new String[] {
							"No PartNo specified",
							"No Length specified",
							"No AftOuterDiameter specified",
							"No Shape specified"
					}
					);
		}
	}
	
	@Test
	public void testLengthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.LENGTH,
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.SHAPE
					},
					new String[] {
							"No Length specified",
							"No AftOuterDiameter specified",
							"No Shape specified"
					}
					);
		}
	}
	
	@Test
	public void testShapeRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.AFT_OUTER_DIAMETER,
							ComponentPreset.SHAPE
					},
					new String[] {
							"No AftOuterDiameter specified",
							"No Shape specified"
					}
					);
		}
	}
	
	@Test
	public void testAftOuterDiameterRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
					ComponentPreset.AFT_OUTER_DIAMETER
					},
					new String[] {
					"No AftOuterDiameter specified"
					}
					);
		}
	}
	
	@Test
	public void testComputeDensityNoMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// constants put into the presetspec above.
		double volume = /*base area*/Math.PI;
		volume *= 2.0 /* times height *// 3.0; /* one third */
		
		double density = 100.0 / volume;
		
		assertEquals("[material:NoseConeCustom]", preset.get(ComponentPreset.MATERIAL).getName());
		// note - epsilon is 1% of the simple computation of density
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.01 * density);
	}
	
	@Test
	public void testMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals("test", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(2.0, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);
		
	}
	
	@Test
	public void testComputeDensityWithMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// constants put into the presetspec above.
		double volume = /*base area*/Math.PI;
		volume *= 2.0 /* times height *// 3.0; /* one third */
		
		double density = 100.0 / volume;
		
		assertEquals("[material:test]", preset.get(ComponentPreset.MATERIAL).getName());
		// note - epsilon is 1% of the simple computation of density
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.01 * density);
	}
	
}
