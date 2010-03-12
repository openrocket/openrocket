/*
 * LaunchLugHandler.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * The SAX handler for Rocksim Launch Lugs.
 */
class LaunchLugHandler extends PositionDependentHandler<LaunchLug> {

    /**
     * The OpenRocket LaunchLug instance.
     */
    private final LaunchLug lug;

    /**
     * Constructor.
     *
     * @param c the parent
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public LaunchLugHandler(RocketComponent c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent component of a launch lug may not be null.");
        }
        lug = new LaunchLug();
        c.addChild(lug);
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
                lug.setRadius(Math.max(0, Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS));
            }
            if ("ID".equals(element)) {
                lug.setInnerRadius(Math.max(0, Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS));
            }
            if ("Len".equals(element)) {
                lug.setLength(Math.max(0, Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH));
            }
            if ("Material".equals(element)) {
                setMaterialName(content);
            }
            if ("FinishCode".equals(element)) {
                lug.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
            }
        }
        catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    /**
     * Get the LaunchLug component this handler is working upon.
     * 
     * @return a LaunchLug component
     */
    @Override
    public LaunchLug getComponent() {
        return lug;
    }

    /**
     * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not 
     * public in all components.
     * 
     * @param position  the OpenRocket position
     */
    @Override
    public void setRelativePosition(RocketComponent.Position position) {
        lug.setRelativePosition(position);
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

