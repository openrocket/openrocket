/*
 * InnerBodyTubeHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for Rocksim inside tubes.
 */
class InnerBodyTubeHandler extends PositionDependentHandler<InnerTube> {

    /**
     * The OpenRocket InnerTube instance.
     */
    private final InnerTube bodyTube;

    /**
     * Constructor.
     *
     * @param c the parent component
     * @param warnings  the warning set
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public InnerBodyTubeHandler(RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent component of an inner tube may not be null.");
        }
        bodyTube = new InnerTube();
        if (isCompatible(c, InnerTube.class, warnings)) {
            c.addChild(bodyTube);
        }
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        if ("AttachedParts".equals(element)) {
            return new AttachedPartsHandler(bodyTube);
        }
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);

        try {
            if ("OD".equals(element)) {
                bodyTube.setOuterRadius(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
            }
            if ("ID".equals(element)) {
                final double r = Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS;
                bodyTube.setInnerRadius(r);
            }
            if ("Len".equals(element)) {
                bodyTube.setLength(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
            if ("IsMotorMount".equals(element)) {
                bodyTube.setMotorMount("1".equals(content));
            }
            if ("EngineOverhang".equals(element)) {
                bodyTube.setMotorOverhang(Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
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
     * Get the InnerTube component this handler is working upon.
     * 
     * @return an InnerTube component
     */
    @Override
    public InnerTube getComponent() {
        return bodyTube;
    }

    /**
     * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not 
     * public in all components.
     * 
     * @param position  the OpenRocket position
     */
    @Override
    public void setRelativePosition(RocketComponent.Position position) {
        bodyTube.setRelativePosition(position);
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
