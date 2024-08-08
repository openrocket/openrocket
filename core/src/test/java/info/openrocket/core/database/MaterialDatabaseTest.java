package info.openrocket.core.database;

import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialDatabaseTest extends BaseTestCase {
	@Test
	void testDatabasesInitialization() {
		assertNotNull(Databases.BULK_MATERIAL);
		assertNotNull(Databases.SURFACE_MATERIAL);
		assertNotNull(Databases.LINE_MATERIAL);

		assertFalse(Databases.BULK_MATERIAL.isEmpty());
		assertFalse(Databases.SURFACE_MATERIAL.isEmpty());
		assertFalse(Databases.LINE_MATERIAL.isEmpty());
	}

	@Test
	void testFindMaterialByTypeAndName() {
		Material aluminum = Databases.findMaterial(Material.Type.BULK, "Aluminum");
		assertNotNull(aluminum);
		assertEquals("[material:Aluminum]", aluminum.getName());
		assertEquals(Material.Type.BULK, aluminum.getType());
		assertEquals(2700, aluminum.getDensity(), 0.001);
	}

	@Test
	void testFindMaterialByTypeNameAndDensity() {
		Material customMaterial = Databases.findMaterial(Material.Type.BULK, "CustomMaterial", 1000, MaterialGroup.PLASTICS);
		assertNotNull(customMaterial);
		assertEquals("[material:CustomMaterial]", customMaterial.getName());
		assertEquals(Material.Type.BULK, customMaterial.getType());
		assertEquals(1000, customMaterial.getDensity(), 0.001);
		assertEquals(MaterialGroup.PLASTICS, customMaterial.getGroup());
		assertTrue(customMaterial.isUserDefined());
	}

	@Test
	void testGetDatabase() {
		assertSame(Databases.BULK_MATERIAL, Databases.getDatabase(Material.Type.BULK));
		assertSame(Databases.SURFACE_MATERIAL, Databases.getDatabase(Material.Type.SURFACE));
		assertSame(Databases.LINE_MATERIAL, Databases.getDatabase(Material.Type.LINE));
	}

	@Test
	void testGetDatabaseInvalidType() {
		assertThrows(NullPointerException.class, () -> {
			Databases.getDatabase(null);
		});
	}
}
