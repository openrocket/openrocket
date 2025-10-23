package info.openrocket.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import info.openrocket.core.util.Chars;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class L10NGeneratorTest {

	private PrintStream originalOut;
	private ByteArrayOutputStream captured;

	private Method outputMethod;

	@BeforeEach
	void setUp() throws Exception {
		originalOut = System.out;
		captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));

		outputMethod = L10NGenerator.class.getDeclaredMethod("output", char.class);
		outputMethod.setAccessible(true);
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	void outputMapsFractionSlashToForwardSlash() throws Exception {
		invokeOutput(Chars.FRACTION);
		String generated = captured.toString(StandardCharsets.UTF_8);
		assertEquals("m.put('\\u2044', \"/\");" + System.lineSeparator(), generated);
	}

	@Test
	void outputProducesNoMappingForUnsupportedCharacters() throws Exception {
		invokeOutput('€');
		String generated = captured.toString(StandardCharsets.UTF_8);
		assertTrue(generated.isEmpty());
	}

	private void invokeOutput(char ch) throws IllegalAccessException {
		captured.reset();
		try {
			outputMethod.invoke(null, ch);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}
