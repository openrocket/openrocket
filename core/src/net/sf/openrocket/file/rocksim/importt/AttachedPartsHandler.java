/*
 * AttachedPartsHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * A SAX handler for the Rocksim AttachedParts XML type.
 */
class AttachedPartsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;

	/** The parent component. */
	private final RocketComponent component;

	/**
	 * Constructor.
	 *
	 * @param c  the parent
	 *
	 * @throws IllegalArgumentException   thrown if <code>c</code> is null
	 */
	public AttachedPartsHandler(DocumentLoadingContext context, RocketComponent c) throws IllegalArgumentException {
		if (c == null) {
			throw new IllegalArgumentException("The parent component of any attached part may not be null.");
		}
		this.context = context;
		this.component = c;
	}

    DocumentLoadingContext getContext() {
        return context;
    }

    RocketComponent getComponent() {
        return component;
    }

    @Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RockSimCommonConstants.FIN_SET.equals(element)) {
			return new FinSetHandler(context, component);
		}
		if (RockSimCommonConstants.CUSTOM_FIN_SET.equals(element)) {
			return new FinSetHandler(context, component);
		}
		if (RockSimCommonConstants.LAUNCH_LUG.equals(element)) {
			return new LaunchLugHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.PARACHUTE.equals(element)) {
			return new ParachuteHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.STREAMER.equals(element)) {
			return new StreamerHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.MASS_OBJECT.equals(element)) {
			return new MassObjectHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.RING.equals(element)) {
			return new RingHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.BODY_TUBE.equals(element)) {
			// Pods can have BodyTubes as attached parts, but not inner tubes. All other components can't have BodyTubes as
			// attached parts.
			if (component instanceof PodSet) {
				return new BodyTubeHandler(context, component, warnings);
			} else {
				return new InnerBodyTubeHandler(context, component, warnings);
			}
		}
		if (RockSimCommonConstants.TRANSITION.equals(element)) {
			return new TransitionHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.NOSE_CONE.equals(element)) {
			return new NoseConeHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.SUBASSEMBLY.equals(element)) {
			return new SubAssemblyHandler(context, component);
		}
		if (RockSimCommonConstants.TUBE_FIN_SET.equals(element)) {
			return new TubeFinSetHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.EXTERNAL_POD.equals(element)) {
			return new PodHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.RING_TAIL.equals(element)) {
			warnings.add("Ring tails are not currently supported. Ignoring.");
		}
		return null;
	}
}
