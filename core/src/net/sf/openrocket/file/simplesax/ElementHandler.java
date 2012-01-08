package net.sf.openrocket.file.simplesax;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
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
 * 1. initHandler.{@link #openElement(String, HashMap, WarningSet)} is called for
 *       the opening element <bar>, which returns fooHandler
 * 2. fooHandler.{@link #openElement(String, HashMap, WarningSet)} is called for
 *       the opening element <bar>, which returns barHandler
 * 3. barHandler.{@link #endHandler(String, HashMap, String, WarningSet)} is called for
 *       the closing element </bar>
 * 4. fooHandler.{@link #closeElement(String, HashMap, String, WarningSet)} is called for
 *       the closing element </bar>
 * 5. fooHandler.{@link #endHandler(String, HashMap, String, WarningSet)} is called for
 *       the closing element </foo>
 * 6. initHandler.{@link #closeElement(String, HashMap, String, WarningSet)} is called for
 *       the closing element </foo>
 * 
 * Note that {@link #endHandler(String, HashMap, String, WarningSet)} is not called for
 * the initial handler.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class ElementHandler {

	/**
	 * Called when an opening element is encountered.  Returns the handler that will handle
	 * the elements within that element, or <code>null</code> if the element and all of
	 * its contents is to be ignored.
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
	public abstract ElementHandler openElement(String element,
			HashMap<String, String> attributes, WarningSet warnings) throws SAXException;

	/**
	 * Called when an element is closed.  The default implementation checks whether there is
	 * any non-space text within the element and if there exists any attributes, and adds
	 * a warning of both.  This can be used at the and of the method to check for 
	 * spurious data.
	 * 
	 * @param element		the element name.
	 * @param attributes	attributes of the element.
	 * @param content		the textual content of the element.
	 * @param warnings		the warning set to store warnings in.
	 */
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		if (!content.trim().equals("")) {
			warnings.add(Warning.fromString("Unknown text in element '" + element
					+ "', ignoring."));
		}
		if (!attributes.isEmpty()) {
			warnings.add(Warning.fromString("Unknown attributes in element '" + element
					+ "', ignoring."));
		}
	}
	
	
	/**
	 * Called when the element block that this handler is handling ends.
	 * The default implementation is a no-op.
	 * 
	 * @param warnings		the warning set to store warnings in.
	 */
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		// No-op
	}
	
}
