package net.sf.openrocket.l10n;

import static org.junit.Assert.*;

import java.util.MissingResourceException;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.ExceptionHandler;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TestExceptionSuppressingTranslator {
	Mockery context = new JUnit4Mockery();
	
	@Mock
	Translator translator;
	
	@Mock
	ExceptionHandler exceptionHandler;
	
	@Test
	public void testSuccessful() {
		Application.setExceptionHandler(exceptionHandler);
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("fake.key4"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		assertEquals("foobar", est.get("fake.key4"));
	}
	
	
	@Test
	public void testFailure() {
		Application.setExceptionHandler(exceptionHandler);
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);
		
		assertFalse("Prerequisite failed", ExceptionSuppressingTranslator.errorReported);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(exceptionHandler).handleErrorCondition(with(any(String.class)), with(any(MissingResourceException.class)));
			oneOf(translator).get("fake.key5"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key5"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key6"); will(throwException(new MissingResourceException("a", "b", "c"))); 
		}});
		// @formatter:on
		
		// Test first failure
		assertEquals("fake.key5", est.get("fake.key5"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);
		
		// Test second failure
		assertEquals("fake.key5", est.get("fake.key5"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);
		
		// Test failure with other key
		assertEquals("fake.key6", est.get("fake.key6"));
		assertTrue(ExceptionSuppressingTranslator.errorReported);
	}
	
	
}
