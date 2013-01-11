package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.simulation.customexpression.CustomExpression;

import org.xml.sax.SAXException;

class SimulationsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final OpenRocketDocument doc;
	private SingleSimulationHandler handler;
	
	public SimulationsHandler(OpenRocketDocument doc, DocumentLoadingContext context) {
		this.doc = doc;
		this.context = context;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (!element.equals("simulation")) {
			warnings.add("Unknown element '" + element + "', ignoring.");
			return null;
		}
		
		handler = new SingleSimulationHandler(doc, context);
		return handler;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		attributes.remove("status");
		
		//Finished loading. Rebuilding custom expressions in case something has changed such as listener variable come available.
		for (CustomExpression exp : doc.getCustomExpressions()){
			exp.setExpression(exp.getExpressionString());
		}
		
		super.closeElement(element, attributes, content, warnings);
	}
}