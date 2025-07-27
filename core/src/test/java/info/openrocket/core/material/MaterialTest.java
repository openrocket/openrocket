package info.openrocket.core.material;

import info.openrocket.core.database.Database;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialTest extends BaseTestCase {
	private final double EPSILON = 1e-6;
	ComponentPreset preset;
	Database<Material> BULK_MATERIAL;

	@BeforeEach
	public void createPreset() throws Exception {
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MATERIAL, Material.newMaterial(Material.Type.BULK, "Preset Material", 980, true, true));
		preset = ComponentPresetFactory.create(presetspec);
	}

	@BeforeEach
	public void createDatabase() {
		BULK_MATERIAL = new Database<>();
		BULK_MATERIAL.add(Material.newMaterial(Material.Type.BULK, "Aluminum", 2700, MaterialGroup.METALS, false, false));
		BULK_MATERIAL.add(Material.newMaterial(Material.Type.BULK, "Balsa", 170, MaterialGroup.WOODS, false, false));
		BULK_MATERIAL.add(Material.newMaterial(Material.Type.BULK, "Delrin", 1420, MaterialGroup.PLASTICS, false, false));
	}


	@Test
	public void testMaterial() {
		String name = "Test Material";
		Material.Type type = Material.Type.BULK;
		double density = 1.0;

		// Test user-defined material
		Material m = Material.newMaterial(type, name, density, true);
		assertEquals(name, m.getName());
		assertEquals(type, m.getType());
		assertEquals(density, m.getDensity(), EPSILON);
		assertTrue(m.isUserDefined());
		assertFalse(m.isDocumentMaterial());
		assertEquals(MaterialGroup.CUSTOM, m.getGroup());

		// Test non-user-defined material
		m = Material.newMaterial(type, name, density, false);
		assertFalse(m.isUserDefined());
		assertFalse(m.isDocumentMaterial());
		assertEquals(MaterialGroup.OTHER, m.getGroup());

		// Test defined material group
		m = Material.newMaterial(type, name, density, MaterialGroup.WOODS, false);
		assertFalse(m.isUserDefined());
		assertFalse(m.isDocumentMaterial());
		assertEquals(MaterialGroup.WOODS, m.getGroup());

		// Test storable string
		String storable = m.toStorableString();
		assertEquals("BULK|Test Material|1.0|Woods", storable);
	}

	@Test
	public void testDocumentDatabase() {
		// Create a document and rocket
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);
		BodyTube bodyTube = new BodyTube();
		stage.addChild(bodyTube);

		// Check current document material database
		assertEquals(0, document.getDocumentPreferences().getAllMaterials().size());

		// Create a material
		String name = "Tube Material";
		Material.Type type = Material.Type.BULK;
		double density = 314;
		Material m = Material.newMaterial(type, name, density, true, true);

		// Check document material database
		assertEquals(0, document.getDocumentPreferences().getAllMaterials().size());

		// Assign material to body tube
		bodyTube.setMaterial(m);
		assertEquals(1, document.getDocumentPreferences().getAllMaterials().size());
		assertEquals(name, bodyTube.getMaterial().getName());
		assertEquals(type, bodyTube.getMaterial().getType());
		assertEquals(density, bodyTube.getMaterial().getDensity(), EPSILON);
		assertTrue(bodyTube.getMaterial().isUserDefined());
		assertTrue(bodyTube.getMaterial().isDocumentMaterial());
		assertEquals(MaterialGroup.CUSTOM, bodyTube.getMaterial().getGroup());

		// Assign a new material
		name = "New Material";
		density = 271;
		Material m2 = Material.newMaterial(type, name, density, true, true);
		bodyTube.setMaterial(m2);
		assertEquals(2, document.getDocumentPreferences().getAllMaterials().size());
		assertEquals(name, bodyTube.getMaterial().getName());
		assertEquals(type, bodyTube.getMaterial().getType());
		assertEquals(density, bodyTube.getMaterial().getDensity(), EPSILON);
		assertTrue(bodyTube.getMaterial().isUserDefined());
		assertTrue(bodyTube.getMaterial().isDocumentMaterial());
		assertEquals(MaterialGroup.CUSTOM, bodyTube.getMaterial().getGroup());

		// Remove a material
		document.getDocumentPreferences().removeMaterial(m);
		assertEquals(1, document.getDocumentPreferences().getAllMaterials().size());
	}

	@Test
	public void testLoadFromPreset() {
		// Create a document and rocket
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);
		BodyTube bodyTube = new BodyTube();
		stage.addChild(bodyTube);

		// Check document material database
		assertEquals(0, document.getDocumentPreferences().getAllMaterials().size());

		// Load from preset
		bodyTube.loadPreset(preset);
		assertSame(preset.get(ComponentPreset.MATERIAL), bodyTube.getMaterial());
		assertEquals("Preset Material", bodyTube.getMaterial().getName());
		assertEquals(Material.Type.BULK, bodyTube.getMaterial().getType());
		assertEquals(980, bodyTube.getMaterial().getDensity(), EPSILON);
		assertTrue(bodyTube.getMaterial().isUserDefined());
		assertTrue(bodyTube.getMaterial().isDocumentMaterial());
		assertEquals(MaterialGroup.CUSTOM, bodyTube.getMaterial().getGroup());

		// Check document material database
		assertEquals(1, document.getDocumentPreferences().getAllMaterials().size());
	}
}
