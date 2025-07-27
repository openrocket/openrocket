package info.openrocket.core.file.openrocket.importt;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.material.Material;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class DocumentMaterialHandler extends AbstractElementHandler {
	private final OpenRocketDocument document;

	public DocumentMaterialHandler(OpenRocketDocument document) {
		this.document = document;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
		if (element.equals("material")) {
			return PlainTextHandler.INSTANCE;
		}
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		if ("material".equals(element)) {
			Material mat = Material.fromStorableString(content, true);
			mat.setDocumentMaterial(true);
			document.getDocumentPreferences().addMaterial(mat);
		}
	}
}
