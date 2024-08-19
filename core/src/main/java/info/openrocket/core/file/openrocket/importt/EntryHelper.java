package info.openrocket.core.file.openrocket.importt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * Helper class for parsing entry elements that have a key and type attribute, and a value.
 * For example: <entry key="key" type="string">value</entry>
 */
public abstract class EntryHelper {
	public static Object getValueFromEntry(EntryHandler handler, HashMap<String, String> attributes, String content) {
		String type = attributes.get("type");
		Object value = null;
		if ("boolean".equals(type)) {
			value = Boolean.valueOf(content);
		} else if ("string".equals(type)) {
			value = content;
		} else if ("number".equals(type)) {
			value = parseNumber(content);
		} else if ("list".equals(type)) {
			value = handler.getNestedList();
		}
		return value;
	}

	private static Number parseNumber(String str) {
		try {
			str = str.trim();
			if (str.matches("^[+-]?[0-9]+$")) {
				BigInteger value = new BigInteger(str, 10);
				if (value.equals(BigInteger.valueOf(value.intValue()))) {
					return value.intValue();
				} else if (value.equals(BigInteger.valueOf(value.longValue()))) {
					return value.longValue();
				} else {
					return value;
				}
			} else {
				BigDecimal value = new BigDecimal(str);
				if (value.equals(BigDecimal.valueOf(value.doubleValue()))) {
					return value.doubleValue();
				} else {
					return value;
				}
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
