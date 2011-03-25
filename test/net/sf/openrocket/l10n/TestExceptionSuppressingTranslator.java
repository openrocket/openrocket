package net.sf.openrocket.l10n;

import static org.junit.Assert.assertEquals;

import java.util.MissingResourceException;

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
	
	@Test
	public void testSuccessful() {
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("fake.key"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		assertEquals("foobar", est.get("fake.key"));
	}
	
	
	@Test
	public void testFailure() {
		ExceptionSuppressingTranslator est = new ExceptionSuppressingTranslator(translator);
		
		assertEquals("Prerequisite failed", 0, ExceptionSuppressingTranslator.failures);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(translator).get("fake.key"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key2"); will(throwException(new MissingResourceException("a", "b", "c"))); 
		}});
		// @formatter:on
		
		// Test first failure
		assertEquals("fake.key", est.get("fake.key"));
		assertEquals(1, ExceptionSuppressingTranslator.failures);
		
		// Test second failure
		assertEquals("fake.key", est.get("fake.key"));
		assertEquals(1, ExceptionSuppressingTranslator.failures);
		
		// Test failure with other key
		assertEquals("fake.key2", est.get("fake.key"));
		assertEquals(2, ExceptionSuppressingTranslator.failures);
	}
	

}
