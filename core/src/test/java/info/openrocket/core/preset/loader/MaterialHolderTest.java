package info.openrocket.core.preset.loader;

import info.openrocket.core.database.Database;
import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static info.openrocket.core.material.Material.Type.BULK;
import static info.openrocket.core.material.Material.Type.LINE;
import static info.openrocket.core.material.Material.Type.SURFACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialHolderTest extends BaseTestCase {

	@Test
	void putAndGetMaterialsByType() {
		Material.Bulk bulk = (Material.Bulk) Material.newMaterial(BULK, "Bulk", 900.0, MaterialGroup.WOODS, true);
		Material.Surface surface =
				(Material.Surface) Material.newMaterial(SURFACE, "Surface", 0.1, MaterialGroup.FABRICS, true);
		Material.Line line = (Material.Line) Material.newMaterial(LINE, "Line", 0.02, MaterialGroup.ELASTICS, true);

		MaterialHolder holder = new MaterialHolder();
		holder.put(bulk);
		holder.put(surface);
		holder.put(line);

		assertSame(bulk, holder.getBulkMaterial(bulk));
		assertSame(surface, holder.getSurfaceMaterial(surface, null));
		assertSame(line, holder.getLineMaterial(line));
		assertSame(bulk, holder.getMaterial(bulk));
		assertSame(surface, holder.getMaterial(surface));
		assertSame(line, holder.getMaterial(line));
	}

	@Test
	void getSurfaceMaterialCreatesDerivedEntryWhenNeeded() {
		Material.Bulk bulk =
				(Material.Bulk) Material.newMaterial(BULK, "Composite", 1500.0, MaterialGroup.COMPOSITES, true);
		MaterialHolder holder = new MaterialHolder();
		holder.put(bulk);

		Material.Surface requested =
				(Material.Surface) Material.newMaterial(SURFACE, "Composite", 0.0, MaterialGroup.COMPOSITES, true);
		double thickness = UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(0.75);

		Material.Surface converted = holder.getSurfaceMaterial(requested, thickness);
		assertNotNull(converted);
		String thicknessName = UnitGroup.UNITS_LENGTH.getUnit("mm").toString(thickness);
		assertEquals("Composite(" + thicknessName + ")", converted.getName());
		assertEquals(bulk.getDensity() * thickness, converted.getDensity(), 1e-9);

		// Subsequent lookups should return the cached instance.
		assertSame(converted, holder.getSurfaceMaterial(requested, thickness));
	}

	@Test
	void valuesAndSizeReflectAllStoredMaterials() {
		Material.Bulk bulk = (Material.Bulk) Material.newMaterial(BULK, "Bulk", 500.0, MaterialGroup.WOODS, true);
		Material.Surface surface =
				(Material.Surface) Material.newMaterial(SURFACE, "Surface", 0.1, MaterialGroup.FABRICS, true);
		Material.Line line = (Material.Line) Material.newMaterial(LINE, "Line", 0.02, MaterialGroup.ELASTICS, true);

		MaterialHolder holder = new MaterialHolder(List.of(bulk, surface, line));

		assertEquals(3, holder.size());
		assertTrue(holder.values().containsAll(List.of(bulk, surface, line)));
	}

	@Test
	void asDatabaseReturnsOnlyRequestedType() {
		Material.Bulk bulk = (Material.Bulk) Material.newMaterial(BULK, "Bulk", 700.0, MaterialGroup.WOODS, true);
		Material.Surface surface =
				(Material.Surface) Material.newMaterial(SURFACE, "Surface", 0.3, MaterialGroup.FABRICS, true);
		Material.Line line = (Material.Line) Material.newMaterial(LINE, "Line", 0.01, MaterialGroup.ELASTICS, true);

		MaterialHolder holder = new MaterialHolder(List.of(bulk, surface, line));

		Database<Material> bulkDb = holder.asDatabase(BULK);
		assertEquals(1, bulkDb.size());
		assertTrue(bulkDb.contains(bulk));

		Database<Material> surfaceDb = holder.asDatabase(SURFACE);
		assertEquals(1, surfaceDb.size());
		assertTrue(surfaceDb.contains(surface));

		Database<Material> lineDb = holder.asDatabase(LINE);
		assertEquals(1, lineDb.size());
		assertTrue(lineDb.contains(line));
	}
}
