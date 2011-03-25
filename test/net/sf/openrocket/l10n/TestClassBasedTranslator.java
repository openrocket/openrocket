package net.sf.openrocket.l10n;

import static org.junit.Assert.*;

import java.util.MissingResourceException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class TestClassBasedTranslator {
	Mockery context = new JUnit4Mockery();
	
	@Mock
	Translator translator;
	
	@Test
	public void testClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(null, 0);
		assertEquals("TestClassBasedTranslator", cbt.getClassName());
		
		cbt = new ClassBasedTranslator(null, "foobar");
		assertEquals("foobar", cbt.getClassName());
	}
	
	@Test
	public void testGetWithClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("TestClassBasedTranslator.fake.key"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		assertEquals("foobar", cbt.get("fake.key"));
	}
	
	
	@Test
	public void testGetWithoutClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(translator).get("TestClassBasedTranslator.fake.key"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key"); will(returnValue("barbaz")); 
		}});
		// @formatter:on
		
		assertEquals("barbaz", cbt.get("fake.key"));
	}
	
	
	@Test
	public void testMissing() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(translator).get("TestClassBasedTranslator.fake.key"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key"); will(throwException(new MissingResourceException("a", "b", "c"))); 
		}});
		// @formatter:on
		
		try {
			fail("Returned: " + cbt.get("fake.key"));
		} catch (MissingResourceException e) {
			assertEquals("Neither key 'TestClassBasedTranslator.fake.key' nor 'fake.key' could be found", e.getMessage());
		}
		
	}
}
