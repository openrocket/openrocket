/*
 * BodyTubeHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for Rocksim Body Tubes.
 */
class BodyTubeHandler extends BaseHandler<BodyTube> {
    /**
     * The OpenRocket BodyTube.
     */
    private final BodyTube bodyTube;

    /**
     * Constructor.
     *
     * @param c parent component
     * @param warnings  the warning set
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public BodyTubeHandler(RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent component of a body tube may not be null.");
        }
        bodyTube = new BodyTube();
        if (isCompatible(c, BodyTube.class, warnings)) {
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
            if ("FinishCode".equals(element)) {
                bodyTube.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
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
     * Get the component this handler is working upon.
     * 
     * @return a component
     */
    @Override
    public BodyTube getComponent() {
        return bodyTube;
    }

    /**
     * Get the required type of material for this component.
     *
     * @return BULK
     */
    public Material.Type getMaterialType() {
        return Material.Type.BULK;
    }
}

