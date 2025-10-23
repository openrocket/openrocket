package info.openrocket.core.preset.loader;

import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static info.openrocket.core.material.Material.Type.BULK;
import static info.openrocket.core.material.Material.Type.LINE;
import static info.openrocket.core.material.Material.Type.SURFACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnParsersTest extends BaseTestCase {

	@Test
	void baseColumnParserFindsHeaderCaseInsensitively() {
		StringColumnParser parser = new StringColumnParser("Part No.", ComponentPreset.PARTNO);
		parser.configure(new String[] { "mfg.", "part no.", "desc." });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "Estes", "BT-20", "Body tube" }, props);

		assertEquals("BT-20", props.get(ComponentPreset.PARTNO));
	}

	@Test
	void baseColumnParserSkipsWhenHeaderMissing() {
		IntegerColumnParser parser = new IntegerColumnParser("Line Count", ComponentPreset.LINE_COUNT);
		parser.configure(new String[] { "Other", "Columns" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "10", "20" }, props);

		assertTrue(props.isEmpty());
	}

	@Test
	void doubleUnitColumnParserConvertsUsingUnitsColumn() {
		DoubleUnitColumnParser parser = new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH);
		parser.configure(new String[] { "Units", "Length" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "0", "10" }, props);

		double expected = UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(10.0);
		assertEquals(expected, props.get(ComponentPreset.LENGTH), 1e-9);
	}

	@Test
	void doubleUnitColumnParserFallsBackWhenUnitsMissing() {
		DoubleUnitColumnParser parser = new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH);
		parser.configure(new String[] { "Units", "Length" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "?", "5" }, props);

		double expected = UnitGroup.UNITS_LENGTH.getUnit("in").fromUnit(5.0);
		assertEquals(expected, props.get(ComponentPreset.LENGTH), 1e-9);
	}

	@Test
	void doubleUnitColumnParserUnderstandsNamedUnits() {
		DoubleUnitColumnParser parser = new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH);
		parser.configure(new String[] { "Units", "Length" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "mm", "25.4" }, props);

		double expected = UnitGroup.UNITS_LENGTH.getUnit("mm").fromUnit(25.4);
		assertEquals(expected, props.get(ComponentPreset.LENGTH), 1e-9);
	}

	@Test
	void doubleUnitColumnParserIgnoresNonNumericValues() {
		DoubleUnitColumnParser parser = new DoubleUnitColumnParser("Length", "Units", ComponentPreset.LENGTH);
		parser.configure(new String[] { "Units", "Length" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "0", "not-a-number" }, props);

		assertTrue(props.isEmpty());
	}

	@Test
	void massColumnParserSkipsEmptyOrZeroValues() {
		MassColumnParser parser = new MassColumnParser("Mass", "Mass units");
		parser.configure(new String[] { "Mass units", "Mass" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "oz", "" }, props);
		parser.parse(new String[] { "oz", " ? " }, props);
		parser.parse(new String[] { "oz", "0" }, props);

		assertTrue(props.isEmpty());
	}

	@Test
	void massColumnParserStoresPositiveMassValues() {
		MassColumnParser parser = new MassColumnParser("Mass", "Mass units");
		parser.configure(new String[] { "Mass units", "Mass" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "oz", "2.5" }, props);

		double expected = UnitGroup.UNITS_MASS.getUnit("oz").fromUnit(2.5);
		assertEquals(expected, props.get(ComponentPreset.MASS), 1e-9);
	}

	@Test
	void shapeColumnParserUnderstandsCommonAliases() {
		ShapeColumnParser parser = new ShapeColumnParser();
		parser.configure(new String[] { "Shape" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "Ogive" }, props);
		assertEquals(Transition.Shape.OGIVE, props.get(ComponentPreset.SHAPE));

		props = new TypedPropertyMap();
		parser.parse(new String[] { "cone" }, props);
		assertEquals(Transition.Shape.CONICAL, props.get(ComponentPreset.SHAPE));

		props = new TypedPropertyMap();
		parser.parse(new String[] { "ps" }, props);
		assertEquals(Transition.Shape.POWER, props.get(ComponentPreset.SHAPE));

		props = new TypedPropertyMap();
		parser.parse(new String[] { "3" }, props);
		assertEquals(Transition.Shape.ELLIPSOID, props.get(ComponentPreset.SHAPE));
	}

	@Test
	void shapeColumnParserRejectsUnknownValues() {
		ShapeColumnParser parser = new ShapeColumnParser();
		parser.configure(new String[] { "Shape" });

		assertThrows(info.openrocket.core.util.BugException.class,
				() -> parser.parse(new String[] { "mystery" }, new TypedPropertyMap()));
	}

	@Test
	void materialColumnParserPrefersMaterialsFromHolder() {
		Material.Bulk stored =
				(Material.Bulk) Material.newMaterial(BULK, "Catalog Bulk", 1200.0, MaterialGroup.PLASTICS, true);
		MaterialHolder holder = new MaterialHolder();
		holder.put(stored);

		MaterialColumnParser parser = new MaterialColumnParser(holder);
		parser.configure(new String[] { "Material" });

		TypedPropertyMap props = new TypedPropertyMap();
		parser.parse(new String[] { "Catalog Bulk" }, props);

		assertSame(stored, props.get(ComponentPreset.MATERIAL));
	}

	@Test
	void lineAndSurfaceParsersFallbackToDatabaseWhenUnknown() {
		MaterialHolder holder = new MaterialHolder();

		LineMaterialColumnParser lineParser = new LineMaterialColumnParser(holder, "Shroud Material",
				ComponentPreset.LINE_MATERIAL);
		lineParser.configure(new String[] { "Shroud Material" });

		SurfaceMaterialColumnParser surfaceParser = new SurfaceMaterialColumnParser(holder, "Chute Material",
				ComponentPreset.MATERIAL);
		surfaceParser.configure(new String[] { "Chute Material" });

		TypedPropertyMap props = new TypedPropertyMap();
		lineParser.parse(new String[] { "Kevlar cord" }, props);
		surfaceParser.parse(new String[] { "Ripstop Nylon" }, props);

		Material line = props.get(ComponentPreset.LINE_MATERIAL);
		Material surface = props.get(ComponentPreset.MATERIAL);

		assertNotNull(line);
		assertEquals(LINE, line.getType());
		assertNotNull(surface);
		assertEquals(SURFACE, surface.getType());
	}
}
