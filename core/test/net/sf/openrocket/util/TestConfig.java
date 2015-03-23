package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TestConfig {
	
	private Config config = new Config();
	
	@Test
	public void testDoubles() {
		config.put("double", Math.PI);
		config.put("bigdecimal", new BigDecimal(Math.PI));
		assertEquals(Math.PI, config.getDouble("double", null), 0);
		assertEquals(Math.PI, config.getDouble("bigdecimal", null), 0);
		assertEquals(3, (int) config.getInt("double", null));
	}
	
	@Test
	public void testInts() {
		config.put("int", 123);
		config.put("biginteger", new BigDecimal(Math.PI));
		config.put("bigdecimal", new BigDecimal(Math.PI));
		assertEquals(123, (int) config.getInt("int", null));
		assertEquals(3, (int) config.getInt("bigdecimal", null));
		assertEquals(3, (int) config.getInt("biginteger", null));
	}
	
	
	@Test
	public void testDefaultValue() {
		assertEquals(true, config.getBoolean("foo", true));
		assertEquals(123, (int) config.getInt("foo", 123));
		assertEquals(123L, (long) config.getLong("foo", 123L));
		assertEquals(1.23, (double) config.getDouble("foo", 1.23), 0);
		assertEquals("bar", config.getString("foo", "bar"));
		assertEquals(Arrays.asList("foo"), config.getList("foo", Arrays.asList("foo")));
	}
	
	
	@Test
	public void testNullDefaultValue() {
		assertEquals(null, config.getBoolean("foo", null));
		assertEquals(null, config.getInt("foo", null));
		assertEquals(null, config.getLong("foo", null));
		assertEquals(null, config.getDouble("foo", null));
		assertEquals(null, config.getString("foo", null));
		assertEquals(null, config.getList("foo", null));
	}
	
	@Test
	public void testStoringList() {
		List<Object> list = new ArrayList<Object>();
		list.add("Foo");
		list.add(123);
		list.add(Math.PI);
		list.add(true);
		config.put("list", list);
		assertEquals(Arrays.asList("Foo", 123, Math.PI, true), config.getList("list", null));
	}
	
	@Test
	public void testModifyingStoredList() {
		List<Object> list = new ArrayList<Object>();
		list.add("Foo");
		list.add(123);
		list.add(Math.PI);
		list.add(true);
		config.put("list", list);
		list.add("hello");
		assertEquals(Arrays.asList("Foo", 123, Math.PI, true), config.getList("list", null));
	}
	
	@Test
	public void testModifyingStoredNumber() {
		AtomicInteger ai = new AtomicInteger(100);
		config.put("ai", ai);
		ai.incrementAndGet();
		assertEquals(100, (int) config.getInt("ai", null));
	}
	
	@Test
	public void testClone() {
		config.put("string", "foo");
		config.put("int", 123);
		config.put("double", Math.PI);
		
		AtomicInteger ai = new AtomicInteger(100);
		config.put("atomicinteger", ai);
		
		List<Object> list = new ArrayList<Object>();
		list.add("Foo");
		config.put("list", list);
		
		Config copy = config.clone();
		
		config.put("extra", "foo");
		ai.incrementAndGet();
		
		assertFalse(copy.containsKey("extra"));
		assertEquals("foo", copy.getString("string", null));
		assertEquals(123, (int) copy.getInt("int", null));
		assertEquals(100, (int) copy.getInt("atomicinteger", null));
		assertEquals(Math.PI, (double) copy.getDouble("double", null), 0);
		assertEquals(Arrays.asList("Foo"), copy.getList("list", null));
	}
	
	@Test
	public void testStoringNullValue() {
		try {
			config.put("foo", (Boolean) null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			config.put("foo", (String) null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			config.put("foo", (Number) null);
			fail();
		} catch (NullPointerException e) {
		}
		try {
			config.put("foo", (List<?>) null);
			fail();
		} catch (NullPointerException e) {
		}
	}
	
	@Test
	public void testStoringListWithInvalidTypes() {
		List<Object> list = new ArrayList<Object>();
		list.add("Foo");
		list.add(new Date());
		try {
			config.put("foo", list);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testStoringListWithNull() {
		List<Object> list = new ArrayList<Object>();
		list.add("Foo");
		list.add(new Date());
		try {
			config.put("foo", list);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
