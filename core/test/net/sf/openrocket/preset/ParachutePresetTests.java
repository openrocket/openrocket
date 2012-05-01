package net.sf.openrocket.preset;

import static org.junit.Assert.assertTrue;
import net.sf.openrocket.motor.Manufacturer;

import org.junit.Test;

/**
 * Test construction of PARACHUTE type ComponentPresets based on TypedPropertyMap through the
 * ComponentPresetFactory.create() method.
 * 
 * Ensure required properties are populated
 * 
 * Ensure any computed values are correctly computed.
 * 
 */
public class ParachutePresetTests {

	@Test
	public void testManufacturerRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			assertTrue("Wrong Exception Thrown", ex.getMessage().contains("No Manufacturer specified"));
		}
	}

	@Test
	public void testPartNoRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			assertTrue("Wrong Exception Thrown", ex.getMessage().contains("No PartNo specified"));
		}
	}

	@Test
	public void testDiameterRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put( ComponentPreset.PARTNO, "partno");
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			assertTrue("Wrong Exception Thrown", ex.getMessage().contains("No Diameter specified"));
		}
	}

	@Test
	public void testLineCountRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put( ComponentPreset.PARTNO, "partno");
			presetspec.put( ComponentPreset.DIAMETER, 2.0);
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			assertTrue("Wrong Exception Thrown", ex.getMessage().contains("No LineCount specified"));
		}
	}

	@Test
	public void testLineLengthRequired() {
		try {
			TypedPropertyMap presetspec = new TypedPropertyMap();
			presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			presetspec.put( ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
			presetspec.put( ComponentPreset.PARTNO, "partno");
			presetspec.put( ComponentPreset.DIAMETER, 2.0);
			presetspec.put( ComponentPreset.LINE_COUNT, 6);
			ComponentPresetFactory.create(presetspec);
		} catch ( InvalidComponentPresetException ex ) {
			assertTrue("Wrong Exception Thrown", ex.getMessage().contains("No LineLength specified"));
		}
	}

}
