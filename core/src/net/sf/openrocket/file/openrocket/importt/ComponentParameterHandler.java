package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;

/**
 * A handler that populates the parameters of a previously constructed rocket component.
 * This uses the setters, or delegates the handling to another handler for specific
 * elements.
 */
class ComponentParameterHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent component;
	
	public ComponentParameterHandler(RocketComponent c, DocumentLoadingContext context) {
		this.component = c;
		this.context = context;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		// Check for specific elements that contain other elements
		if (element.equals("subcomponents")) {
			return new ComponentHandler(component, context);
		}
		if ( element.equals("appearance")) {
			return new AppearanceHandler(component,context);
		}
		if (element.equals("motormount")) {
			if (!(component instanceof MotorMount)) {
				warnings.add(Warning.fromString("Illegal component defined as motor mount."));
				return null;
			}
			return new MotorMountHandler((MotorMount) component, context);
		}
		if (element.equals("finpoints")) {
			if (!(component instanceof FreeformFinSet)) {
				warnings.add(Warning.fromString("Illegal component defined for fin points."));
				return null;
			}
			return new FinSetPointHandler((FreeformFinSet) component, context);
		}
		if (element.equals("motorconfiguration")) {
			if (!(component instanceof Rocket)) {
				warnings.add(Warning.fromString("Illegal component defined for motor configuration."));
				return null;
			}
			return new MotorConfigurationHandler((Rocket) component, context);
		}
		if ( element.equals("deploymentconfiguration")) {
			if ( !(component instanceof RecoveryDevice) ) {
				warnings.add(Warning.fromString("Illegal component defined as recovery device."));
				return null;
			}
			return new DeploymentConfigurationHandler( (RecoveryDevice) component, context );
		}
		if ( element.equals("separationconfiguration")) {
			if ( !(component instanceof Stage) ) {
				warnings.add(Warning.fromString("Illegal component defined as stage."));
				return null;
			}
			return new StageSeparationConfigurationHandler( (Stage) component, context );
		}
		
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		
		if (element.equals("subcomponents") || element.equals("motormount") ||
				element.equals("finpoints") || element.equals("motorconfiguration") ||
				element.equals("appearance") || element.equals("deploymentconfiguration") ||
				element.equals("separationconfiguration")) {
			return;
		}
		
		// Search for the correct setter class
		
		Class<?> c;
		for (c = component.getClass(); c != null; c = c.getSuperclass()) {
			String setterKey = c.getSimpleName() + ":" + element;
			Setter s = DocumentConfig.setters.get(setterKey);
			if (s != null) {
				// Setter found
				s.set(component, content, attributes, warnings);
				break;
			}
			if (DocumentConfig.setters.containsKey(setterKey)) {
				// Key exists but is null -> invalid parameter
				c = null;
				break;
			}
		}
		if (c == null) {
			warnings.add(Warning.fromString("Unknown parameter type '" + element + "' for "
					+ component.getComponentName() + ", ignoring."));
		}
	}
}