/*
 * AttachedPartsHandler.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.aerodynamics.WarningSet;
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
        if ("FinSet".equals(element)) {
            return new FinSetHandler(component);
        }
        if ("CustomFinSet".equals(element)) {
            return new FinSetHandler(component);
        }
        if ("LaunchLug".equals(element)) {
            return new LaunchLugHandler(component);
        }
        if ("Parachute".equals(element)) {
            return new ParachuteHandler(component);
        }
        if ("Streamer".equals(element)) {
            return new StreamerHandler(component);
        }
        if ("MassObject".equals(element)) {
            return new MassObjectHandler(component);
        }
        if ("Ring".equals(element)) {
            return new RingHandler(component);
        }
        if ("BodyTube".equals(element)) {
            return new InnerBodyTubeHandler(component);
        }
        if ("Transition".equals(element)) {
            return new TransitionHandler(component);
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

