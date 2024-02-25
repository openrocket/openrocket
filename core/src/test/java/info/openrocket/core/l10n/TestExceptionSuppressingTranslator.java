package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.MissingResourceException;

import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.ExceptionHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestExceptionSuppressingTranslator {

	@Mock
	Translator translator;

	@Mock
	ExceptionHandler exceptionHandler;

	@Test
	public void testSuccessful() {
		Application.setExceptionHandler(exceptionHandler);
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);

		when(translator.get("fake.key4")).thenReturn("foobar");

		assertEquals("foobar", est.get("fake.key4"));
	}

	@Test
	public void testFailure() {
		Application.setExceptionHandler(exceptionHandler);
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);

		assertFalse("Prerequisite failed", ExceptionSuppressingTranslator.errorReported);

		when(translator.get("fake.key5")).thenThrow(new MissingResourceException("a", "b", "c"));
		when(translator.get("fake.key6")).thenThrow(new MissingResourceException("a", "b", "c"));

		// Test first failure
		assertEquals("fake.key5", est.get("fake.key5"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);

		// Test second failure
		assertEquals("fake.key5", est.get("fake.key5"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);

		// Test failure with other key
		assertEquals("fake.key6", est.get("fake.key6"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);

		// Verify that handleErrorCondition is called with any string and any MissingResourceException
		verify(exceptionHandler).handleErrorCondition(any(String.class), any(MissingResourceException.class));
	}

}
