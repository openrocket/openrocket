/*
 * AttachedPartsHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A SAX handler for the Rocksim AttachedParts XML type.  
 */
class AttachedPartsHandler extends AbstractElementHandler {
	private final OpenRocketDocument document;
	
    /** The parent component. */
    private final RocketComponent component;

    /**
     * Constructor.
     * 
     * @param c  the parent
     * 
     * @throws IllegalArgumentException   thrown if <code>c</code> is null
     */
    public AttachedPartsHandler(OpenRocketDocument document, RocketComponent c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent component of any attached part may not be null.");
        }
        this.document = document;
        component = c;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        if (RocksimCommonConstants.FIN_SET.equals(element)) {
            return new FinSetHandler(document, component);
        }
        if (RocksimCommonConstants.CUSTOM_FIN_SET.equals(element)) {
            return new FinSetHandler(document, component);
        }
        if (RocksimCommonConstants.LAUNCH_LUG.equals(element)) {
            return new LaunchLugHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.PARACHUTE.equals(element)) {
            return new ParachuteHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.STREAMER.equals(element)) {
            return new StreamerHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.MASS_OBJECT.equals(element)) {
            return new MassObjectHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.RING.equals(element)) {
            return new RingHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.BODY_TUBE.equals(element)) {
            return new InnerBodyTubeHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.TRANSITION.equals(element)) {
            return new TransitionHandler(document, component, warnings);
        }
        if (RocksimCommonConstants.TUBE_FIN_SET.equals(element)) {
            warnings.add("Tube fins are not currently supported. Ignoring.");
        }
        if (RocksimCommonConstants.RING_TAIL.equals(element)) {
            warnings.add("Ring tails are not currently supported. Ignoring.");
        }
        if (RocksimCommonConstants.EXTERNAL_POD.equals(element)) {
            warnings.add("Pods are not currently supported. Ignoring.");
        }
        return null;
    }
}

