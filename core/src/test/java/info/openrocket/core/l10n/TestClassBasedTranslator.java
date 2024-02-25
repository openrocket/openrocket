package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.MissingResourceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestClassBasedTranslator {
	@Mock
	Translator translator;

	@Test
	public void testClassName() {
		// Mock setup
		when(translator.get("TestClassBasedTranslator.fake.key1")).thenReturn("foobar");

		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		cbt.get("fake.key1");
		assertEquals(cbt.getClassName(), "TestClassBasedTranslator");

		cbt = new ClassBasedTranslator(translator, "foobar");
		assertEquals(cbt.getClassName(), "foobar");
	}

	@Test
	public void testGetWithClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);

		when(translator.get("TestClassBasedTranslator.fake.key1")).thenReturn("foobar");

		assertEquals(cbt.get("fake.key1"), "foobar");
	}

	@Test
	public void testGetWithoutClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);

		// @formatter:off
		when(translator.get("TestClassBasedTranslator.fake.key2")).thenThrow(new MissingResourceException("a", "b", "c"));
		when(translator.get("fake.key2")).thenReturn("barbaz");

		assertEquals(cbt.get("fake.key2"), "barbaz");
	}

	@Test
	public void testMissing() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);

		when(translator.get("TestClassBasedTranslator.fake.key3")).thenThrow(new MissingResourceException("a", "b", "c"));
		when(translator.get("fake.key3")).thenThrow(new MissingResourceException("a", "b", "c"));

		try {
			fail("Returned: " + cbt.get("fake.key3"));
		} catch (MissingResourceException e) {
			assertEquals(e.getMessage(), "Neither key 'TestClassBasedTranslator.fake.key3' nor 'fake.key3' could be found");
		}
	}

	@Test
	public void testGetWithSubClass() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);

		when(translator.get("TestClassBasedTranslator.fake.key1")).thenReturn("foobar");

		assertEquals(new Subclass().get(cbt, "fake.key1"), "foobar");
		assertEquals(cbt.getClassName(), "TestClassBasedTranslator");
	}

	private class Subclass {
		private String get(Translator trans, String key) {
			return trans.get(key);
		}
	}
}
