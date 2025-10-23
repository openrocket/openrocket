package info.openrocket.core.preset.loader;

import info.openrocket.core.material.Material;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialLoaderTest extends BaseTestCase {

	@Test
	void convertsGramPerCentimeterToLineMaterial() {
		Material material = loadSingleMaterial("Line", "g/cm", 4.0);
		assertEquals(Material.Type.LINE, material.getType());
		assertEquals(0.4, material.getDensity(), 1e-9);
		assertTrue(material.getName().startsWith("Line"));
	}

	@Test
	void convertsGramPerSquareCentimeterToSurfaceMaterial() {
		Material material = loadSingleMaterial("Surface", "g/cm2", 0.6);
		assertEquals(Material.Type.SURFACE, material.getType());
		assertEquals(6.0, material.getDensity(), 1e-9);
	}

	@Test
	void convertsGramPerCubicCentimeterToBulkMaterial() {
		Material material = loadSingleMaterial("Bulk", "g/cm3", 1.2);
		assertEquals(Material.Type.BULK, material.getType());
		assertEquals(1200.0, material.getDensity(), 1e-9);
	}

	@Test
	void keepsKilogramPerCubicMeterAsIs() {
		Material material = loadSingleMaterial("MetricBulk", "kg/m3", 500.0);
		assertEquals(Material.Type.BULK, material.getType());
		assertEquals(500.0, material.getDensity(), 1e-9);
	}

	@Test
	void convertsPoundsPerCubicFootToBulk() {
		Material material = loadSingleMaterial("ImperialBulk", "lb/ft3", 10.0);
		assertEquals(Material.Type.BULK, material.getType());
		assertEquals(160.184634, material.getDensity(), 1e-6);
	}

	@Test
	void convertsOuncePerInchToLine() {
		Material material = loadSingleMaterial("ImperialLine", "oz/in", 2.0);
		assertEquals(Material.Type.LINE, material.getType());
		assertEquals(2.23224592, material.getDensity(), 1e-8);
	}

	@Test
	void convertsOuncePerSquareInchToSurface() {
		Material material = loadSingleMaterial("ImperialSurface", "oz/in2", 0.5);
		assertEquals(Material.Type.SURFACE, material.getType());
		assertEquals(21.97092438, material.getDensity(), 1e-8);
	}

	@Test
	void throwsForUnknownUnit() {
		TestMaterialLoader loader = new TestMaterialLoader();
		loader.parseHeaders(new String[] { "Material Name", "Units", "Density" });
		assertThrows(info.openrocket.core.util.BugException.class,
				() -> loader.parseData(new String[] { "\"Mystery\"", "unknown", "1.0" }));
	}

	private Material loadSingleMaterial(String baseName, String unit, double density) {
		TestMaterialLoader loader = new TestMaterialLoader();
		loader.parseHeaders(new String[] { "Material Name", "Units", "Density" });
		loader.parseData(new String[] { "\"" + baseName + "\"", unit, Double.toString(density) });
		Collection<Material> values = loader.getMaterialMap().values();
		assertEquals(1, values.size());
		Iterator<Material> iterator = new ArrayList<>(values).iterator();
		return iterator.next();
	}

	private static final class TestMaterialLoader extends MaterialLoader {
		TestMaterialLoader() {
			super(new File("."));
		}
	}
}
