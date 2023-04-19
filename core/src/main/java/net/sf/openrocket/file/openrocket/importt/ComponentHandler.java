package net.sf.openrocket.file.openrocket.importt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Reflection;

/**
 * A handler that creates components from the corresponding elements.  The control of the
 * contents is passed on to ComponentParameterHandler.
 */
class ComponentHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent parent;
	
	public ComponentHandler(RocketComponent parent, DocumentLoadingContext context) {
		this.parent = parent;
		this.context = context;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		// Attempt to construct new component
		Constructor<? extends RocketComponent> constructor = DocumentConfig.constructors
				.get(element);
		if (constructor == null) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}
		
		RocketComponent c;
		try {
			c = constructor.newInstance();
		} catch (InstantiationException e) {
			throw new BugException("Error constructing component.", e);
		} catch (IllegalAccessException e) {
			throw new BugException("Error constructing component.", e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
		
		parent.addChild(c);
		
		return new ComponentParameterHandler(c, context);
	}
}