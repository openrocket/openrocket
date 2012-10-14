package net.sf.openrocket.startup.jij;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A URLStreamHandlerFactory that can be configured with various
 * handlers.
 * 
 * From http://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 */
public class ConfigurableStreamHandlerFactory implements URLStreamHandlerFactory {
	private final Map<String, URLStreamHandler> protocolHandlers;
	
	public ConfigurableStreamHandlerFactory() {
		protocolHandlers = new HashMap<String, URLStreamHandler>();
	}
	
	public void addHandler(String protocol, URLStreamHandler urlHandler) {
		protocolHandlers.put(protocol, urlHandler);
	}
	
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		return protocolHandlers.get(protocol);
	}
}
