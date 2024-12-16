package info.openrocket.core.preset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Test construction of NOSE_CONE type ComponentPresets based on
 * TypedPropertyMap through the
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
					});
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
					});
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
					});
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
					});
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
					});
		}
	}

	@Test
	public void testFilledParameterExplicitlyTrue() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		// Set required parameters
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		// Set filled parameter explicitly to true
		presetspec.put(ComponentPreset.FILLED, true);

		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		assertTrue(preset.get(ComponentPreset.FILLED), "Preset should be filled when explicitly set to true");
	}

	@Test
	public void testFilledParameterExplicitlyFalse() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		// Set required parameters
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		// Set filled parameter explicitly to false
		presetspec.put(ComponentPreset.FILLED, false);

		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		assertFalse(preset.get(ComponentPreset.FILLED), "Preset should not be filled when explicitly set to false");
	}

	@Test
	public void testFilledParameterWithThickness() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		// Set required parameters
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		// Set thickness (which should imply not filled)
		presetspec.put(ComponentPreset.THICKNESS, 0.002);

		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		assertFalse(preset.has(ComponentPreset.FILLED), "Preset with thickness should not be filled");
		assertEquals(0.002, preset.get(ComponentPreset.THICKNESS), 0.0001);
	}

	@Test
	public void testOverriddenMass() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		// Set required parameters
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 0.123); // Override calculated mass

		ComponentPreset preset = ComponentPresetFactory.create(presetspec);
		assertEquals(0.123, preset.get(ComponentPreset.MASS), 0.0001);
	}

	// TODO - test fails, could not find when running Ant
	@Test
	public void testComputeDensityNoMaterial() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER,
		Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);

		// constants put into the presetspec above.
		double volume = Math.PI; // base area
		volume *= 2.0 / 3.0; // times height / one third

		double density = 100.0 / volume;

		//assertEquals("NoseConeCustom", preset.get(ComponentPreset.MATERIAL).getName());
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

		//assertEquals("test", preset.get(ComponentPreset.MATERIAL).getName());
		assertEquals(2.0, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.0005);

	}

	@Test
	public void testComputeDensityWithMaterial() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
		presetspec.put(ComponentPreset.MANUFACTURER,
		Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.SHAPE, Transition.Shape.CONICAL);
		presetspec.put(ComponentPreset.AFT_OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.FILLED, true);
		presetspec.put(ComponentPreset.MASS, 100.0);
		presetspec.put(ComponentPreset.MATERIAL,
		Material.newMaterial(Material.Type.BULK, "test", 2.0, true));
		ComponentPreset preset = ComponentPresetFactory.create(presetspec);

		// constants put into the presetspec above.
		double volume = Math.PI; //base area
		volume *= 2.0 / 3.0; // times height / one third

		double density = 100.0 / volume;

		//assertEquals("test", preset.get(ComponentPreset.MATERIAL).getName());
		// note - epsilon is 1% of the simple computation of density
		assertEquals(density, preset.get(ComponentPreset.MATERIAL).getDensity(), 0.01 * density);
	}

}
