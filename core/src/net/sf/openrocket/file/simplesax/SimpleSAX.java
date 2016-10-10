package net.sf.openrocket.file.simplesax;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

	static final XMLReaderCache cache = new XMLReaderCache(10);

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

		XMLReader reader = cache.createXMLReader();
		reader.setContentHandler(xmlhandler);
		reader.setErrorHandler(xmlhandler);
		try {
			reader.parse(source);
		} finally {
			reader.setContentHandler(null);
			reader.setErrorHandler(null);
			cache.releaseXMLReader(reader);
		}
	}

	private static class XMLReaderCache {

		private final BlockingQueue<XMLReader> queue;
		private XMLReaderCache( int maxSize ) {
			this.queue = new LinkedBlockingQueue<XMLReader>(maxSize);
		}

		private XMLReader createXMLReader() throws SAXException {

			XMLReader reader = queue.poll();
			if ( reader == null ) {
				reader = XMLReaderFactory.createXMLReader();
			}
			return reader;
		}

		private void releaseXMLReader( XMLReader reader ) {
			// force references to null to encourage garbage collection.
			reader.setContentHandler(null);
			reader.setErrorHandler(null);
			queue.offer( reader );
		}
	}

}
