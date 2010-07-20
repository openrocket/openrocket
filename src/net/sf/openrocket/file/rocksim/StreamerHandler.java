/*
 * StreamerHandler.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for Streamer components.
 */
class StreamerHandler extends RecoveryDeviceHandler<Streamer> {

    /**
     * The OpenRocket Streamer.
     */
    private final Streamer streamer;

    /**
     * Constructor.
     *
     * @param c the parent component
     * @param warnings  the warning set
     * 
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public StreamerHandler(RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
        if (c == null) {
            throw new IllegalArgumentException("The parent of a streamer may not be null.");
        }
        streamer = new Streamer();
        if (isCompatible(c, Streamer.class, warnings)) {
            c.addChild(streamer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        return PlainTextHandler.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Streamer getComponent() {
        return streamer;
    }

}

