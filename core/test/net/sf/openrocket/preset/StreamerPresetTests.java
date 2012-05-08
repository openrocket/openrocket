package net.sf.openrocket.preset;

import net.sf.openrocket.motor.Manufacturer;

import org.junit.Test;

/**
 * Test construction of STREAMER type ComponentPresets based on TypedPropertyMap through the
 * ComponentPresetFactory.create() method.
 * 
 * Ensure required properties are populated
 * 
 * Ensure any computed values are correctly computed.
 * 
 */
public class StreamerPresetTests {

	@Test
	public void testManufacturerRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			PresetAssertHelper.assertInvalidPresetException( ex,
					new TypedKey<?>[] {
					ComponentPreset.MANUFACTURER, 
					ComponentPreset.PARTNO, 
					ComponentPreset.LENGTH,
					ComponentPreset.WIDTH
			},
			new String[] {
					"No Manufacturer specified",
					"No PartNo specified",
					"No Length specified",
					"No Width specified"
			}
					);
		}
	}

	@Test
	public void testPartNoRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			PresetAssertHelper.assertInvalidPresetException( ex,
					new TypedKey<?>[] {
					ComponentPreset.PARTNO, 
					ComponentPreset.LENGTH,
					ComponentPreset.WIDTH
			},
			new String[] {
					"No PartNo specified",
					"No Length specified",
					"No Width specified"
			}
					);
		}
	}

	@Test
	public void testLengthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put( ComponentPreset.PARTNO, "partno");
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			PresetAssertHelper.assertInvalidPresetException( ex,
					new TypedKey<?>[] {
					ComponentPreset.LENGTH,
					ComponentPreset.WIDTH
			},
			new String[] {
					"No Length specified",
					"No Width specified"
			}
					);
		}
	}

	@Test
	public void testWidthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put( ComponentPreset.PARTNO, "partno");
			presetspec.put( ComponentPreset.LENGTH, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			PresetAssertHelper.assertInvalidPresetException( ex,
					new TypedKey<?>[] {
					ComponentPreset.WIDTH
			},
			new String[] {
					"No Width specified"
			}
					);
		}
	}

}
