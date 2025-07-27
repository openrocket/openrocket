package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class TestConfig {

	private final Config config = new Config();

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
		assertEquals(1.23, config.getDouble("foo", 1.23), 0);
		assertEquals(config.getString("foo", "bar"), "bar");
		assertEquals(Arrays.asList("foo"), config.getList("foo", List.of("foo")));
	}

	@Test
	public void testNullDefaultValue() {
		assertNull(config.getBoolean("foo", null));
		assertNull(config.getInt("foo", null));
		assertNull(config.getLong("foo", null));
		assertNull(config.getDouble("foo", null));
		assertNull(config.getString("foo", null));
		assertNull(config.getList("foo", null));
	}

	@Test
	public void testStoringList() {
		List<Object> list = new ArrayList<>();
		list.add("Foo");
		list.add(123);
		list.add(Math.PI);
		list.add(true);
		config.put("list", list);
		assertEquals(Arrays.asList("Foo", 123, Math.PI, true), config.getList("list", null));
	}

	@Test
	public void testModifyingStoredList() {
		List<Object> list = new ArrayList<>();
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

		List<Object> list = new ArrayList<>();
		list.add("Foo");
		config.put("list", list);

		Config copy = config.clone();

		config.put("extra", "foo");
		ai.incrementAndGet();

		assertFalse(copy.containsKey("extra"));
		assertEquals(copy.getString("string", null), "foo");
		assertEquals(123, (int) copy.getInt("int", null));
		assertEquals(100, (int) copy.getInt("atomicinteger", null));
		assertEquals(Math.PI, copy.getDouble("double", null), 0);
		assertEquals(List.of("Foo"), copy.getList("list", null));
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
		List<Object> list = new ArrayList<>();
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
		List<Object> list = new ArrayList<>();
		list.add("Foo");
		list.add(new Date());
		try {
			config.put("foo", list);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
