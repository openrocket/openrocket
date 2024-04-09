package info.openrocket.core.file.openrocket.importt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Reflection;

/**
 * A handler that creates components from the corresponding elements. The
 * control of the
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