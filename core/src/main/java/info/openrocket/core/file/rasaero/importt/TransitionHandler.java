package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * RASAero parser for OpenRocket transition.
 * Transitions in RASAero can only be conical.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class TransitionHandler extends BaseHandler<Transition> {
    protected final Transition transition = new Transition();

    /**
     * Constructor
     * 
     * @param context  current document loading context
     * @param parent   parent component to add this new component to
     * @param warnings warning set to add import warnings to
     * @throws IllegalArgumentException if the parent component is null
     */
    public TransitionHandler(DocumentLoadingContext context, RocketComponent parent, WarningSet warnings)
            throws IllegalArgumentException {
        super(context);
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a transition may not be null.");
        }
        if (isCompatible(parent, Transition.class, warnings)) {
            parent.addChild(this.transition);
        } else {
            throw new IllegalArgumentException(
                    "Cannot add transition to parent of type " + parent.getClass().getName());
        }
        this.transition.setAftRadiusAutomatic(false);
        this.transition.setShapeType(Transition.Shape.CONICAL); // RASAero only supports conical transitions
    }

    public TransitionHandler(DocumentLoadingContext context) {
        super(context);
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
        try {
            if (RASAeroCommonConstants.REAR_DIAMETER.equals(element)) {
                this.transition.setAftRadius(
                        Double.parseDouble(content) / 2 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        this.transition.setLength(length / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        this.transition.setForeRadius(diameter / 2 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH); // Not really
                                                                                                           // useful,
                                                                                                           // but adding
                                                                                                           // it for
                                                                                                           // completeness
        this.transition.setForeRadiusAutomatic(true);
        this.transition.setThickness(0.002); // Arbitrary value; RASAero doesn't specify this
    }

    @Override
    protected RocketComponent getComponent() {
        return this.transition;
    }
}
