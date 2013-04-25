package net.sf.openrocket.preset;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;

import org.junit.Test;

/**
 * Test construction of ENGINE_BLOCK type ComponentPresets based on TypedPropertyMap through the
 * ComponentPresetFactory.create() method.
 * 
 * Ensure required properties are populated
 * 
 * Ensure any computed values are correctly computed.
 * 
 */
public class EngineBlockPresetTests {
	
	@Test
	public void testManufacturerRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.MANUFACTURER,
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH
					},
					new String[] {
							"No Manufacturer specified",
							"No PartNo specified",
							"No Length specified",
							"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testPartNoRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
							ComponentPreset.PARTNO,
							ComponentPreset.LENGTH
					},
					new String[] {
							"No PartNo specified",
							"No Length specified",
							"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testLengthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					new TypedKey<?>[] {
					ComponentPreset.LENGTH
					},
					new String[] {
							"No Length specified",
							"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testOnlyOuterDiameter() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					null,
					new String[] {
					"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testOnlyInnerDiameter() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.INNER_DIAMETER, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					null,
					new String[] {
					"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testOnlyThicknessDiameter() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put(ComponentPreset.PARTNO, "partno");
			presetspec.put(ComponentPreset.LENGTH, 2.0);
			presetspec.put(ComponentPreset.THICKNESS, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch (InvalidComponentPresetException ex) {
			PresetAssertHelper.assertInvalidPresetException(ex,
					null,
					new String[] {
					"Preset dimensions underspecified"
					}
					);
		}
	}
	
	@Test
	public void testComputeInnerDiameter() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.THICKNESS, 0.5);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals(1.0, preset.get(ComponentPreset.INNER_DIAMETER).doubleValue(), 0.0);
	}
	
	@Test
	public void testComputeOuterDiameter() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.THICKNESS, 0.5);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals(2.0, preset.get(ComponentPreset.OUTER_DIAMETER).doubleValue(), 0.0);
	}
	
	@Test
	public void testComputeThickness() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals(0.5, preset.get(ComponentPreset.THICKNESS).doubleValue(), 0.0);
	}
	
	@Test
	public void testComputeThicknessLooses() throws Exception {
		// If all OUTER_DIAMETER, INNER_DIAMETER and THICKNESS are
		// specified, THICKNESS is recomputed.
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.THICKNESS, 15.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals(0.5, preset.get(ComponentPreset.THICKNESS).doubleValue(), 0.0);
	}
	
	@Test
	public void testComputeDensityNoMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MASS, 100.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// Compute the volume by hand here using a slightly different formula from
		// the real implementation.  The magic numbers are based on the 
		// constants put into the presetspec above.
		double volume = /*outer area*/(Math.PI * 1.0) - /* inner area */(Math.PI * .25);
		volume *= 2.0; /* times length */
		
		double density = 100.0 / volume;
		
		assertEquals("[material:EngineBlockCustom]", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);
	}
	
	@Test
	public void testMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		assertEquals("test", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(2.0, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);
		
	}
	
	@Test
	public void testComputeDensityWithMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		
		// Compute the volume by hand here using a slightly different formula from
		// the real implementation.  The magic numbers are based on the 
		// constants put into the presetspec above.
		double volume = /*outer area*/(Math.PI * 1.0) - /* inner area */(Math.PI * .25);
		volume *= 2.0; /* times length */
		
		double density = 100.0 / volume;
		
		assertEquals("[material:test]", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);
	}
	
}
