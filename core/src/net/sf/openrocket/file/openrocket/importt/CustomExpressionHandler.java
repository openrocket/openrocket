package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.simulation.customexpression.CustomExpression;

import org.xml.sax.SAXException;

class CustomExpressionHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final OpenRocketContentHandler contentHandler;
	public CustomExpression currentExpression;
	
	public CustomExpressionHandler(OpenRocketContentHandler contentHandler, DocumentLoadingContext context) {
		this.context = context;
		this.contentHandler = contentHandler;
		currentExpression = new CustomExpression(contentHandler.getDocument());
		
	}
	
	@Override
	public ElementHandler openElement(String element,
			HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		
		return this;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		if (element.equals("type")) {
			contentHandler.getDocument().addCustomExpression(currentExpression);
		}
		
		if (element.equals("name")) {
			currentExpression.setName(content);
		}
		
		if (element.equals("symbol")) {
			currentExpression.setSymbol(content);
		}
		
		if (element.equals("unit") && attributes.get("unittype").equals("auto")) {
			currentExpression.setUnit(content);
		}
		
		if (element.equals("expression")) {
			currentExpression.setExpression(content);
		}
	}
}