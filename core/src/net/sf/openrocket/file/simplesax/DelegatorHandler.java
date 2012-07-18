package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.util.SimpleStack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The actual SAX handler class.  Contains the necessary methods for parsing the SAX source.
 * Delegates the actual content parsing to {@link ElementHandler} objects.
 */
class DelegatorHandler extends DefaultHandler {
	private final WarningSet warnings;
	
	private final SimpleStack<ElementHandler> handlerStack = new SimpleStack<ElementHandler>();
	private final SimpleStack<StringBuilder> elementData = new SimpleStack<StringBuilder>();
	private final SimpleStack<HashMap<String, String>> elementAttributes = new SimpleStack<HashMap<String, String>>();
	
	
	// Ignore all elements as long as ignore > 0
	private int ignore = 0;
	
	
	public DelegatorHandler(ElementHandler initialHandler, WarningSet warnings) {
		this.warnings = warnings;
		handlerStack.add(initialHandler);
		elementData.add(new StringBuilder()); // Just in case
	}
	
	
	/////////  SAX handlers
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		// Check for ignore
		if (ignore > 0) {
			ignore++;
			return;
		}
		
		// Check for unknown namespace
		if (!uri.equals("")) {
			warnings.add(Warning.fromString("Unknown namespace element '" + uri
					+ "' encountered, ignoring."));
			ignore++;
			return;
		}
		
		// Add layer to data stacks
		elementData.push(new StringBuilder());
		elementAttributes.push(copyAttributes(attributes));
		
		// Call the handler
		ElementHandler h = handlerStack.peek();
		h = h.openElement(localName, elementAttributes.peek(), warnings);
		if (h != null) {
			handlerStack.push(h);
		} else {
			// Start ignoring elements
			ignore++;
		}
	}
	
	
	/**
	 * Stores encountered characters in the elementData stack.
	 */
	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		// Check for ignore
		if (ignore > 0)
			return;
		
		StringBuilder sb = elementData.peek();
		sb.append(chars, start, length);
	}
	
	
	/**
	 * Removes the last layer from the stack.
	 */
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		// Check for ignore
		if (ignore > 0) {
			ignore--;
			return;
		}
		
		// Remove data from stack
		String data = elementData.pop().toString(); // throws on error
		HashMap<String, String> attr = elementAttributes.pop();
		
		// Remove last handler and call the next one
		ElementHandler h;
		
		h = handlerStack.pop();
		h.endHandler(localName, attr, data, warnings);
		
		h = handlerStack.peek();
		h.closeElement(localName, attr, data, warnings);
	}
	
	
	private static HashMap<String, String> copyAttributes(Attributes atts) {
		HashMap<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i < atts.getLength(); i++) {
			ret.put(atts.getLocalName(i), atts.getValue(i));
		}
		return ret;
	}
}
