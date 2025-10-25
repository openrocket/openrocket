package info.openrocket.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class L10nPropertyReportTest {

	private Method getSortedKeys;

	@BeforeEach
	void setUp() throws Exception {
		getSortedKeys = L10nPropertyReport.class.getDeclaredMethod("getSortedKeys", Properties.class);
		getSortedKeys.setAccessible(true);
	}

	@Test
	void getSortedKeysReturnsAlphabeticalOrder() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("rocket.description", "Rocket");
		properties.setProperty("rocket.title", "Title");
		properties.setProperty("rocket.name", "Name");

		@SuppressWarnings("unchecked")
		List<String> sorted = (List<String>) invokeGetSortedKeys(properties);

		assertEquals(List.of("rocket.description", "rocket.name", "rocket.title"), sorted);
	}

	private Object invokeGetSortedKeys(Properties properties) {
		try {
			return getSortedKeys.invoke(null, properties);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}
