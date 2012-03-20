package net.sf.openrocket.plugin;

import java.util.Collections;
import java.util.List;

import net.sf.openrocket.util.BugException;

/**
 * An abstract service implementation that returns plugins of type P.
 * 
 * @param <P>	the plugin type that this service returns.
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractService<P> implements Service {
	
	private final Class<P> type;
	
	protected AbstractService(Class<P> type) {
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getPlugins(Class<E> e, Object... args) {
		
		if (e != type) {
			return Collections.emptyList();
		}
		
		List<P> plugins = getPlugins(args);
		
		// Check list content types to avoid mysterious bugs later on
		for (P p : plugins) {
			if (!type.isInstance(p)) {
				throw new BugException("Requesting plugins of type " + type + " but received " +
						((p != null) ? p.getClass() : "null"));
			}
		}
		
		return (List<E>) plugins;
	}
	
	protected abstract List<P> getPlugins(Object... args);
	
}
