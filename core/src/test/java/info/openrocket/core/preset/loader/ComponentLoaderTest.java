package info.openrocket.core.preset.loader;

import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static info.openrocket.core.material.Material.Type.BULK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentLoaderTest extends BaseTestCase {

	@Test
	void bodyTubeLoaderProducesPresetWithConvertedDimensions() {
		MaterialHolder materials = new MaterialHolder();
		BodyTubeLoader loader = new BodyTubeLoader(materials, new File("."));

		String[] headers = { "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "Engine" };
		String[] data = { "Estes", "BT-20", "Marlon", "0", "1.0", "1.1", "10.0", "Paper (office)", "" };

		loader.parseHeaders(headers);
		loader.parseData(data);

		List<ComponentPreset> presets = loader.getPresets();
		assertEquals(1, presets.size());
		ComponentPreset preset = presets.get(0);

		assertEquals(ComponentPreset.Type.BODY_TUBE, preset.get(ComponentPreset.TYPE));
		assertEquals("BT-20", preset.get(ComponentPreset.PARTNO));
		assertEquals("Marlon", preset.get(ComponentPreset.DESCRIPTION));
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(10.0), preset.get(ComponentPreset.LENGTH), 1e-9);
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(1.0),
				preset.get(ComponentPreset.INNER_DIAMETER), 1e-9);
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(1.1),
				preset.get(ComponentPreset.OUTER_DIAMETER), 1e-9);
		assertNotNull(preset.get(ComponentPreset.MANUFACTURER));
	}

	@Test
	void noseConeLoaderMarksHollowPartsAsFilledWhenThicknessIsZero() {
		MaterialHolder materials = new MaterialHolder();
		NoseConeLoader loader = new NoseConeLoader(materials, new File("."));

		String[] headers = { "Mfg.", "Part No.", "Desc.", "Units", "Length", "Outer Dia", "L/D Ratio", "Insert Length",
				"Insert OD", "Thickness", "Shape", "Config", "Material", "CG Loc", "Mass Units", "Mass",
				"Base Ext. Len" };
		String[] data = { "LOC/Precision", "NC-1", "Hello", "1", "250.0", "50.0", "4.0", "25.0", "45.0", "0", "ogive", "",
				"Balsa", "", "", "", "" };

		loader.parseHeaders(headers);
		loader.parseData(data);

		List<ComponentPreset> presets = loader.getPresets();
		assertEquals(1, presets.size());
		ComponentPreset preset = presets.get(0);

		assertEquals(ComponentPreset.Type.NOSE_CONE, preset.get(ComponentPreset.TYPE));
		assertTrue(preset.get(ComponentPreset.FILLED));
		assertFalse(preset.has(ComponentPreset.THICKNESS));
		assertEquals("NC-1", preset.get(ComponentPreset.PARTNO));
		assertEquals("Hello", preset.get(ComponentPreset.DESCRIPTION));
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(250.0), preset.get(ComponentPreset.LENGTH), 1e-9);
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(50.0),
				preset.get(ComponentPreset.AFT_OUTER_DIAMETER), 1e-9);
	}

	@Test
	void parachuteLoaderDerivesSurfaceMaterialFromBulkThickness() {
		MaterialHolder materials = new MaterialHolder();
		Material.Bulk bulk =
				(Material.Bulk) Material.newMaterial(BULK, "BulkOnly", 400.0, MaterialGroup.COMPOSITES, true);
		materials.put(bulk);

		ParachuteLoader loader = new ParachuteLoader(materials, new File("."));
		String[] headers = { "Mfg.", "Part No.", "Desc.", "Units", "OD", "Shroud Count", "Shroud Len",
				"Shroud Material", "Chute Thickness", "Chute Material", "n sides", "Mass Units", "Mass" };
		String[] data = { "Top Flight", "TF-36", "", "1", "914.0", "12", "610.0", "Kevlar", "0.5", "BulkOnly", "6", "oz",
				"" };

		loader.parseHeaders(headers);
		loader.parseData(data);

		List<ComponentPreset> presets = loader.getPresets();
		assertEquals(1, presets.size());
		ComponentPreset preset = presets.get(0);

		assertEquals(ComponentPreset.Type.PARACHUTE, preset.get(ComponentPreset.TYPE));
		assertEquals(6, preset.get(ComponentPreset.SIDES));
		assertEquals(12, preset.get(ComponentPreset.LINE_COUNT));
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(914.0), preset.get(ComponentPreset.DIAMETER), 1e-9);
		assertEquals(UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(610.0),
				preset.get(ComponentPreset.LINE_LENGTH), 1e-9);

		Material surface = preset.get(ComponentPreset.MATERIAL);
		assertEquals(Material.Type.SURFACE, surface.getType());
		assertEquals("BulkOnly", surface.getName());
		assertNotNull(preset.get(ComponentPreset.LINE_MATERIAL));
	}
}
