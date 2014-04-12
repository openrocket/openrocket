package net.sf.openrocket.file.openrocket.importt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Config;

import org.xml.sax.SAXException;

public class ConfigHandler extends AbstractElementHandler {
	
	private ConfigHandler listHandler;
	private Config config = new Config();
	private List<Object> list = new ArrayList<Object>();
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
		if (element.equals("entry") && "list".equals(attributes.get("type"))) {
			listHandler = new ConfigHandler();
			return listHandler;
		} else {
			return PlainTextHandler.INSTANCE;
		}
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		if (element.equals("entry")) {
			String key = attributes.get("key");
			String type = attributes.get("type");
			Object value = null;
			if ("boolean".equals(type)) {
				value = Boolean.valueOf(content);
			} else if ("string".equals(type)) {
				value = content;
			} else if ("number".equals(type)) {
				value = parseNumber(content);
			} else if ("list".equals(type)) {
				value = listHandler.list;
			}
			if (value != null) {
				if (key != null) {
					config.put(key, value);
				} else {
					list.add(value);
				}
			}
		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}
	
	private Number parseNumber(String str) {
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
	
	public Config getConfig() {
		return config;
	}
	
}
