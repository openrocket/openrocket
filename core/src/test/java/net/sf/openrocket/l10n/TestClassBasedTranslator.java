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
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("TestClassBasedTranslator.fake.key1"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		cbt.get("fake.key1");
		assertEquals("TestClassBasedTranslator", cbt.getClassName());
		
		cbt = new ClassBasedTranslator(translator, "foobar");
		assertEquals("foobar", cbt.getClassName());
	}
	
	@Test
	public void testGetWithClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("TestClassBasedTranslator.fake.key1"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		assertEquals("foobar", cbt.get("fake.key1"));
	}
	
	
	@Test
	public void testGetWithoutClassName() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(translator).get("TestClassBasedTranslator.fake.key2"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key2"); will(returnValue("barbaz")); 
		}});
		// @formatter:on
		
		assertEquals("barbaz", cbt.get("fake.key2"));
	}
	
	
	@Test
	public void testMissing() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
			oneOf(translator).get("TestClassBasedTranslator.fake.key3"); will(throwException(new MissingResourceException("a", "b", "c"))); 
			oneOf(translator).get("fake.key3"); will(throwException(new MissingResourceException("a", "b", "c"))); 
		}});
		// @formatter:on
		
		try {
			fail("Returned: " + cbt.get("fake.key3"));
		} catch (MissingResourceException e) {
			assertEquals("Neither key 'TestClassBasedTranslator.fake.key3' nor 'fake.key3' could be found", e.getMessage());
		}
	}
	
	
	
	@Test
	public void testGetWithSubClass() {
		ClassBasedTranslator cbt = new ClassBasedTranslator(translator, 0);
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(translator).get("TestClassBasedTranslator.fake.key1"); will(returnValue("foobar")); 
		}});
		// @formatter:on
		
		assertEquals("foobar", new Subclass().get(cbt, "fake.key1"));
		assertEquals("TestClassBasedTranslator", cbt.getClassName());
	}
	
	
	private class Subclass {
		private String get(Translator trans, String key) {
			return trans.get(key);
		}
	}
}
