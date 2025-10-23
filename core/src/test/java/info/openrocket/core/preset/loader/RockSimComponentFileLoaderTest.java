package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RockSimComponentFileLoaderTest extends BaseTestCase {

	private static final class StubLoader extends RockSimComponentFileLoader {
		private final List<TypedPropertyMap> capturedProps = new ArrayList<>();

		StubLoader() {
			super(new File("."));
		}

		@Override
		protected RockSimComponentFileType getFileType() {
			return RockSimComponentFileType.BODY_TUBE;
		}

		@Override
		protected void postProcess(TypedPropertyMap props) {
			capturedProps.add(props);
		}
	}

	private static final class CapturingColumnParser implements RockSimComponentFileColumnParser {
		private String lastValue;
		private boolean configured;

		@Override
		public void configure(String[] headers) {
			configured = true;
		}

		@Override
		public void parse(String[] data, TypedPropertyMap props) {
			lastValue = data[0];
		}
	}

	@Test
	void isInchesRecognizesNumericAndTextualFlags() {
		assertTrue(RockSimComponentFileLoader.isInches("0"));
		assertTrue(RockSimComponentFileLoader.isInches(" In."));
		assertFalse(RockSimComponentFileLoader.isInches("1"));
		assertFalse(RockSimComponentFileLoader.isInches("mm"));
	}

	@Test
	void convertLengthHandlesBothSupportedUnits() {
		assertEquals(0.0254, RockSimComponentFileLoader.convertLength("0", 1.0), 1e-9);
		assertEquals(0.010, RockSimComponentFileLoader.convertLength("1", 10.0), 1e-12);
	}

	@Test
	void convertMassConvertsOuncesAndLeavesMetricUntouched() {
		assertEquals(0.0283495231, RockSimComponentFileLoader.convertMass("oz", 1.0), 1e-9);
		assertEquals(5.0, RockSimComponentFileLoader.convertMass("g", 5.0), 1e-9);
	}

	@Test
	void stripAllRemovesEveryOccurrenceOfTargetCharacter() {
		assertEquals("ABC", RockSimComponentFileLoader.stripAll("\"A\"B\"C\"", '"'));
		assertEquals("nochange", RockSimComponentFileLoader.stripAll("nochange", '\''));
	}

	@Test
	void toCamelCaseCapitalizesEachWord() {
		assertEquals("Open Rocket", RockSimComponentFileLoader.toCamelCase("open rocket"));
		assertEquals("Single", RockSimComponentFileLoader.toCamelCase("SINGLE"));
		assertEquals("", RockSimComponentFileLoader.toCamelCase(""));
	}

	@Test
	void parseDataTrimsWhitespaceAndQuotesBeforeDelegating() {
		StubLoader loader = new StubLoader();
		CapturingColumnParser parser = new CapturingColumnParser();
		loader.fileColumns.add(parser);

		loader.parseHeaders(new String[] { "Dummy" });
		loader.parseData(new String[] { "  \"value\"  " });

		assertTrue(parser.configured);
		assertEquals("value", parser.lastValue);
		assertEquals(1, loader.capturedProps.size());
	}

	@Test
	void parseDataIgnoresNullOrEmptyRows() {
		StubLoader loader = new StubLoader();
		CapturingColumnParser parser = new CapturingColumnParser();
		loader.fileColumns.add(parser);

		loader.parseHeaders(new String[] { "Dummy" });
		loader.parseData(null);
		loader.parseData(new String[0]);

		assertNull(parser.lastValue);
		assertTrue(loader.capturedProps.isEmpty());
	}
}
