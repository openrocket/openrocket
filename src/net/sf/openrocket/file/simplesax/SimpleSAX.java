package net.sf.openrocket.file.simplesax;

import java.io.IOException;

import net.sf.openrocket.aerodynamics.WarningSet;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * A "simple SAX" XML reader.  This system imposes the limit that an XML element may
 * contain either textual (non-whitespace) content OR additional elements, but not
 * both.  This holds true for both the OpenRocket and RockSim design formats and the
 * RockSim engine definition format.
 * <p>
 * The actual handling is performed by subclasses of {@link ElementHandler}.  The 
 * initial handler is provided to the {@link #readXML(InputSource, ElementHandler, WarningSet)}
 * method.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimpleSAX {

	/**
	 * Read a simple XML file.
	 * 
	 * @param source			the SAX input source.
	 * @param initialHandler	the initial content handler.
	 * @param warnings			a warning set to store warning (cannot be <code>null</code>).
	 * @throws IOException		if an I/O exception occurs while reading.
	 * @throws SAXException		if e.g. malformed XML is encountered.
	 */
	public static void readXML(InputSource source, ElementHandler initialHandler,
			WarningSet warnings) throws IOException, SAXException {

		DelegatorHandler xmlhandler = new DelegatorHandler(initialHandler, warnings);

		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(xmlhandler);
		reader.setErrorHandler(xmlhandler);
		reader.parse(source);
	}
	
}
