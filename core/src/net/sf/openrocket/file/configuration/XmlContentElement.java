package net.sf.openrocket.file.configuration;

/**
 * A simple XML element that contains textual content.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class XmlContentElement extends XmlElement {
	
	private String content = "";
	
	public XmlContentElement(String name) {
		super(name);
	}
	
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		if (content == null) {
			throw new IllegalArgumentException("XML content cannot be null");
		}
		this.content = content;
	}
	
}
