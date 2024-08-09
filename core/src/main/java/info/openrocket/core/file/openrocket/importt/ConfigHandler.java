package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;
import java.util.List;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.Config;

import org.xml.sax.SAXException;

public class ConfigHandler extends EntryHandler {

	private ConfigHandler listHandler;
	private final Config config = new Config();
	private final List<Object> list = new ArrayList<>();

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		if (element.equals("entry") && "list".equals(attributes.get("type"))) {
			listHandler = new ConfigHandler();
			return listHandler;
		} else {
			return PlainTextHandler.INSTANCE;
		}
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		if (element.equals("entry")) {
			String key = attributes.get("key");
			Object value = EntryHelper.getValueFromEntry(ConfigHandler.this, attributes, content);
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

	public Config getConfig() {
		return config;
	}

}
