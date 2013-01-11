package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;

import org.xml.sax.SAXException;

/**
 * The starting point of the handlers.  Accepts a single <openrocket> element and hands
 * the contents to be read by a OpenRocketContentsHandler.
 */
class OpenRocketHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private OpenRocketContentHandler handler = null;
	
	public OpenRocketHandler(DocumentLoadingContext context) {
		this.context = context;
	}
	
	/**
	 * Return the OpenRocketDocument read from the file, or <code>null</code> if a document
	 * has not been read yet.
	 * 
	 * @return	the document read, or null.
	 */
	public OpenRocketDocument getDocument() {
		return handler.getDocument();
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		// Check for unknown elements
		if (!element.equals("openrocket")) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}
		
		// Check for first call
		if (handler != null) {
			warnings.add(Warning.fromString("Multiple document elements found, ignoring later "
					+ "ones."));
			return null;
		}
		
		// Check version number
		String version = null;
		String creator = attributes.remove("creator");
		String docVersion = attributes.remove("version");
		for (String v : DocumentConfig.SUPPORTED_VERSIONS) {
			if (v.equals(docVersion)) {
				version = v;
				break;
			}
		}
		if (version == null) {
			String str = "Unsupported document version";
			if (docVersion != null)
				str += " " + docVersion;
			if (creator != null && !creator.trim().equals(""))
				str += " (written using '" + creator.trim() + "')";
			str += ", attempting to read file anyway.";
			warnings.add(str);
		}
		
		context.setFileVersion(parseVersion(docVersion));
		
		handler = new OpenRocketContentHandler(context);
		return handler;
	}
	
	
	private int parseVersion(String docVersion) {
		if (docVersion == null)
			return 0;
		
		Matcher m = Pattern.compile("^([0-9]+)\\.([0-9]+)$").matcher(docVersion);
		if (m.matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			return major * DocumentConfig.FILE_VERSION_DIVISOR + minor;
		} else {
			return 0;
		}
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		attributes.remove("version");
		attributes.remove("creator");
		super.closeElement(element, attributes, content, warnings);
	}
	
	
}