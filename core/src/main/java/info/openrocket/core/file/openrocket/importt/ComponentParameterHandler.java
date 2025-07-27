package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.AxialStage;
import org.xml.sax.SAXException;

/**
 * A handler that populates the parameters of a previously constructed rocket
 * component.
 * This uses the setters, or delegates the handling to another handler for
 * specific
 * elements.
 */
class ComponentParameterHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent component;

	public ComponentParameterHandler(RocketComponent c, DocumentLoadingContext context) {
		this.component = c;
		this.context = context;

		// Sometimes setting certain component parameters will clear the preset. We don't want that to happen, so
		// ignore preset clearing.
		this.component.setIgnorePresetClearing(true);
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		// Check for specific elements that contain other elements
		if (element.equals("subcomponents")) {
			return new ComponentHandler(component, context);
		}
		if (element.equals("appearance")) {
			return new AppearanceHandler(component, context);
		}
		// TODO: delete 'inside-appearance' when backward compatibility with
		// 22.02.beta.01-22.02.beta.05 is not needed anymore
		if (element.equals("insideappearance") || element.equals("inside-appearance")) {
			return new InsideAppearanceHandler(component, context);
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
		if (element.equals("flightconfiguration")) {
			if (!(component instanceof Rocket)) {
				warnings.add(Warning.fromString("Illegal component defined for flight configuration."));
				return null;
			}
			return new MotorConfigurationHandler((Rocket) component, context);
		}
		if (element.equals("deploymentconfiguration")) {
			if (!(component instanceof RecoveryDevice)) {
				warnings.add(Warning.fromString("Illegal component defined as recovery device."));
				return null;
			}
			return new DeploymentConfigurationHandler((RecoveryDevice) component, context);
		}
		if (element.equals("separationconfiguration")) {
			if (!(component instanceof AxialStage)) {
				warnings.add(Warning.fromString("Illegal component defined as stage."));
				return null;
			}
			return new StageSeparationConfigurationHandler((AxialStage) component, context);
		}

		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		// TODO: delete 'inside-appearance' when backward compatibility with
		// 22.02.beta.01-22.02.beta.05 is not needed anymore
		if (element.equals("subcomponents") || element.equals("motormount") ||
				element.equals("finpoints") || element.equals("motorconfiguration") ||
				element.equals("appearance") || element.equals("insideappearance")
				|| element.equals("inside-appearance") ||
				element.equals("deploymentconfiguration") || element.equals("separationconfiguration")) {
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

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		super.endHandler(element, attributes, content, warnings);

		// Restore the preset clearing behavior
		this.component.setIgnorePresetClearing(false);
	}
}