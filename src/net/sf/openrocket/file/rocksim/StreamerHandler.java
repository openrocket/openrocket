/*
 * StreamerHandler.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for Streamer components.
 */
class StreamerHandler extends PositionDependentHandler<Streamer> {

    /**
     * The OpenRocket Streamer.
     */
    private final Streamer streamer;

    /**
     * Constructor.
     *
     * @param c the parent component
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public StreamerHandler(RocketComponent c) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent of a streamer may not be null.");
        }
        streamer = new Streamer();
        c.addChild(streamer);
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
            if ("Width".equals(element)) {
                streamer.setStripWidth(Math.max(0, Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH));
            }
            if ("Len".equals(element)) {
                streamer.setStripLength(Math.max(0, Double.parseDouble(content) / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH));
            }
            if ("DragCoefficient".equals(element)) {
                streamer.setCD(Double.parseDouble(content));
            }
            if ("Material".equals(element)) {
                setMaterialName(content);
            }
        }
        catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    @Override
    public Streamer getComponent() {
        return streamer;
    }

    /**
     * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not
     * public in all components.
     *
     * @param position the OpenRocket position
     */
    @Override
    public void setRelativePosition(RocketComponent.Position position) {
        streamer.setRelativePosition(position);
    }

    /**
     * Get the required type of material for this component.
     *
     * @return BULK
     */
    @Override
    public Material.Type getMaterialType() {
        return Material.Type.SURFACE;
    }
}

