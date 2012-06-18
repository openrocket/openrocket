package net.sf.openrocket.plugin.framework;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

/**
 * A discovery service that returns plugins of a specified type with
 * provided arguments.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Service extends Plugin {
	
	/**
	 * Return the plugins that match the provided type and are applicable
	 * for the arguments.  The arguments depend on the class type.
	 * <p>
	 * This method may return different plugins for different arguments.
	 * For example, if the arguments contain the OpenRocketDocument, the
	 * service may return only plugins applicable for the specified document.
	 * 
	 * @param type	the plugin interface type
	 * @param args	arguments for the interface.
	 * @return		the plugin instances applicable.
	 */
	public <E> List<E> getPlugins(Class<E> type, Object... args);
	
	
}
