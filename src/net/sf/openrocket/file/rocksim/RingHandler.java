/*
 * RingHandler.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for centering rings and bulkheads.
 */
class RingHandler extends PositionDependentHandler<CenteringRing> {

    /**
     * The OpenRocket Ring.
     */
    private final CenteringRing ring;

    /**
     * Constructor.
     *
     * @param c the parent component
     * @param warnings  the warning set
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public RingHandler(RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent of a ring may not be null.");
        }
        ring = new CenteringRing();
        if (isCompatible(c, CenteringRing.class, warnings)) {
            c.addChild(ring);
        }
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);

        try {
            if ("OD".equals(element)) {
                ring.setOuterRadius(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
            }
            if ("ID".equals(element)) {
                ring.setInnerRadius(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
            }
            if ("Len".equals(element)) {
                ring.setLength(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
            if ("Material".equals(element)) {
                setMaterialName(content);
            }
        }
        catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    /**
     * Get the ring component this handler is working upon.
     * 
     * @return a component
     */
    @Override
    public CenteringRing getComponent() {
        return ring;
    }

    /**
     * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not 
     * public in all components.
     * 
     * @param position  the OpenRocket position
     */
    @Override
    public void setRelativePosition(RocketComponent.Position position) {
        ring.setRelativePosition(position);
    }

    /**
     * Get the required type of material for this component.
     *
     * @return BULK
     */
    @Override
    public Material.Type getMaterialType() {
        return Material.Type.BULK;
    }
}

