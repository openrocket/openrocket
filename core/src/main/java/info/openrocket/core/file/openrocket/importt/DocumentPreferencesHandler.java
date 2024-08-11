package info.openrocket.core.file.openrocket.importt;

import info.openrocket.core.preferences.DocumentPreferences;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class DocumentPreferencesHandler extends EntryHandler {
	private final OpenRocketDocument document;

	public DocumentPreferencesHandler(OpenRocketDocument document) {
		this.document = document;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
		if (element.equals("pref") && "list".equals(attributes.get("type"))) {
			listHandler = new ConfigHandler();
			return listHandler;
		} else if (element.equals("docmaterials")) {
			return new DocumentMaterialHandler(document);
		} else {
			return PlainTextHandler.INSTANCE;
		}
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		if (element.equals("pref")) {
			String key = attributes.get("key");
			String type = attributes.get("type");
			Object value = EntryHelper.getValueFromEntry(DocumentPreferencesHandler.this, attributes, content);
			if (value == null) {
				return;
			}
			if (key != null) {
				addValueToDocumentPreferences(key, value, type);
			} else {
				list.add(value);
			}
		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}

	private void addValueToDocumentPreferences(String key, Object value, String type) {
		DocumentPreferences docPrefs = document.getDocumentPreferences();

		if ("boolean".equals(type)) {
			docPrefs.putBoolean(key, (Boolean) value);
		} else if ("string".equals(type)) {
			docPrefs.putString(key, (String) value);
		} else if ("integer".equals(type)) {
			docPrefs.putInt(key, (Integer) value);
		} else if ("double".equals(type)) {
			docPrefs.putDouble(key, (Double) value);
		} else if ("number".equals(type)) {
			throw new RuntimeException("Number preferences are not supported");
		} else if ("list".equals(type)) {
			// We don't support nested preferences
			throw new RuntimeException("Nested preferences are not supported");
		}
	}
}