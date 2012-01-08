package net.sf.openrocket.file.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * A base simple XML element.  A simple XML element can contain either other XML elements
 * (XmlContainerElement) or textual content (XmlContentElement), but not both.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class XmlElement {
	
	private final String name;
	private final HashMap<String, String> attributes = new HashMap<String, String>();
	
	

	public XmlElement(String name) {
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public void removeAttribute(String key) {
		attributes.remove(key);
	}
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getAttributes() {
		return (Map<String, String>) attributes.clone();
	}
	
}
