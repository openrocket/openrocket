package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;

import org.xml.sax.SAXException;


/**
 * A "simple XML" element handler.  An object of this class handles a single element of
 * an XML file.  If the input file is:
 * 
 *   <foo>
 *     <bar>message</bar>
 *   </foo>
 * 
 * and the initial handler is initHandler, then the following methods will be called:
 * 
 * 1. initHandler.openElement(String, HashMap, WarningSet) is called for the opening element <foo>, which returns fooHandler
 * 2. fooHandler.openElement(String, HashMap, WarningSet) is called for the opening element <bar>, which returns barHandler
 * 3. barHandler.endHandler(String, HashMap, String, WarningSet) is called for the closing element </bar>
 * 4. fooHandler.closeElement(String, HashMap, String, WarningSet) is called for the closing element </bar>
 * 5. fooHandler.endHandler(String, HashMap, String, WarningSet) is called for the closing element </foo>
 * 6. initHandler.closeElement(String, HashMap, String, WarningSet) is called for the closing element </foo>
 * 
 * Note that endHandler(String, HashMap, String, WarningSet) is not called for the initial handler.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ElementHandler {
	
	/**
	 * Called when an opening tag of a contained element is encountered.  Returns the handler
	 * that will handle the elements within that element, or <code>null</code> if the element
	 * and all of its contents is to be ignored.
	 * <p>
	 * Note that this method may also return <code>this</code>, in which case this
	 * handler will also handle the subelement.
	 * 
	 * @param element		the element name.
	 * @param attributes	attributes of the element.
	 * @param warnings		the warning set to store warnings in.
	 * @return				the handler that handles elements encountered within this element,
	 * 						or <code>null</code> if the element is to be ignored.
	 */
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) throws SAXException;
	
	/**
	 * Called when a closing tag of a contained element is encountered.
	 * <p>
	 * This method can be used to handle the textual content of the element for simple text
	 * elements, which is passed in as the "content" parameter.
	 * 
	 * @param element		the element name.
	 * @param attributes	attributes of the element.
	 * @param content		the textual content of the element.
	 * @param warnings		the warning set to store warnings in.
	 */
	public abstract void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException;
	
	/**
	 * Called when the current element that this handler is handling is closed.
	 * 
	 * @param warnings		the warning set to store warnings in.
	 */
	public abstract void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException;
	
}