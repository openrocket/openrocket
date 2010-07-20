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
            return new LaunchLugHandler(component, warnings);
        }
        if ("Parachute".equals(element)) {
            return new ParachuteHandler(component, warnings);
        }
        if ("Streamer".equals(element)) {
            return new StreamerHandler(component, warnings);
        }
        if ("MassObject".equals(element)) {
            return new MassObjectHandler(component, warnings);
        }
        if ("Ring".equals(element)) {
            return new RingHandler(component, warnings);
        }
        if ("BodyTube".equals(element)) {
            return new InnerBodyTubeHandler(component, warnings);
        }
        if ("Transition".equals(element)) {
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

