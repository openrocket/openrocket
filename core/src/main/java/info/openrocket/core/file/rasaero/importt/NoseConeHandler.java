package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * RASAero parser for OpenRocket nose cone.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class NoseConeHandler extends BaseHandler<NoseCone> {
    private final NoseCone noseCone = new NoseCone();

    /**
     * Constructor
     * 
     * @param context  current document loading context
     * @param parent   parent component to add this new component to
     * @param warnings warning set to add import warnings to
     * @throws IllegalArgumentException if the parent component is null
     */
    public NoseConeHandler(DocumentLoadingContext context, RocketComponent parent, WarningSet warnings)
            throws IllegalArgumentException {
        super(context);
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a nose cone may not be null.");
        }
        if (isCompatible(parent, NoseCone.class, warnings)) {
            parent.addChild(this.noseCone);
        } else {
            throw new IllegalArgumentException("Cannot add nose cone to parent of type " + parent.getClass().getName());
        }
        this.noseCone.setAftRadiusAutomatic(false);
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        if (RASAeroCommonConstants.SHAPE.equals(element)) {
            this.noseCone.setShapeType(RASAeroCommonConstants.RASAERO_TO_OPENROCKET_SHAPE(content));
            this.noseCone.setShapeParameter(RASAeroCommonConstants.RASAERO_TO_OPENROCKET_SHAPE_PARAMETER(content));
        }
        try {
            if (RASAeroCommonConstants.POWER_LAW.equals(element)) {
                this.noseCone.setShapeParameter(Double.parseDouble(content));
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
        // TODO: bluntradius (but we don't support it yet)
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        this.noseCone.setLength(length / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        this.noseCone.setBaseRadius(diameter / 2 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        this.noseCone.setThickness(0.002); // Arbitrary value; RASAero doesn't specify this
    }

    @Override
    protected RocketComponent getComponent() {
        return this.noseCone;
    }
}
