/*
 * AttachedPartsHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * A SAX handler for the Rocksim AttachedParts XML type.  
 */
class AttachedPartsHandler extends ElementHandler {
    /** The parent component. */
    private final RocketComponent component;

    /**
     * Constructor.
     * 
     * @param c  the parent
     * 
     * @throws IllegalArgumentException   thrown if <code>c</code> is null
     */
    public AttachedPartsHandler(RocketComponent c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent component of any attached part may not be null.");
        }
        component = c;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        if (RocksimCommonConstants.FIN_SET.equals(element)) {
            return new FinSetHandler(component);
        }
        if (RocksimCommonConstants.CUSTOM_FIN_SET.equals(element)) {
            return new FinSetHandler(component);
        }
        if (RocksimCommonConstants.LAUNCH_LUG.equals(element)) {
            return new LaunchLugHandler(component, warnings);
        }
        if (RocksimCommonConstants.PARACHUTE.equals(element)) {
            return new ParachuteHandler(component, warnings);
        }
        if (RocksimCommonConstants.STREAMER.equals(element)) {
            return new StreamerHandler(component, warnings);
        }
        if (RocksimCommonConstants.MASS_OBJECT.equals(element)) {
            return new MassObjectHandler(component, warnings);
        }
        if (RocksimCommonConstants.RING.equals(element)) {
            return new RingHandler(component, warnings);
        }
        if (RocksimCommonConstants.BODY_TUBE.equals(element)) {
            return new InnerBodyTubeHandler(component, warnings);
        }
        if (RocksimCommonConstants.TRANSITION.equals(element)) {
            return new TransitionHandler(component, warnings);
        }
        if ("TubeFinSet".equals(element)) {
            warnings.add("Tube fins are not currently supported. Ignoring.");
        }
        if ("RingTail".equals(element)) {
            warnings.add("Ring tails are not currently supported. Ignoring.");
        }
        if ("ExternalPod".equals(element)) {
            warnings.add("Pods are not currently supported. Ignoring.");
        }
        return null;
    }
}

