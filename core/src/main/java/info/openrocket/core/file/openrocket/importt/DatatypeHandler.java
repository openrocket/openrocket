package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;

import org.xml.sax.SAXException;

class DatatypeHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final OpenRocketContentHandler contentHandler;
	private CustomExpressionHandler customExpressionHandler = null;

	public DatatypeHandler(OpenRocketContentHandler contentHandler, DocumentLoadingContext context) {
		this.context = context;
		this.contentHandler = contentHandler;
	}

	@Override
	public ElementHandler openElement(String element,
			HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {

		if (element.equals("type") && attributes.get("source").equals("customexpression")) {
			customExpressionHandler = new CustomExpressionHandler(contentHandler, context);
			return customExpressionHandler;
		} else {
			warnings.add(Warning.fromString("Unknown datatype " + element + " defined, ignoring"));
		}

		return this;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		attributes.remove("source");
		super.closeElement(element, attributes, content, warnings);

		if (customExpressionHandler != null) {
			contentHandler.getDocument().addCustomExpression(customExpressionHandler.currentExpression);
		}

	}

}