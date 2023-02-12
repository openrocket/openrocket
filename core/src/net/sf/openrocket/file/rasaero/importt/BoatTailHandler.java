package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A handler for the boat tail element RASAero. This is just an OpenRocket transition.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BoatTailHandler extends TransitionHandler {
    /**
     * Constructor
     *
     * @param context  current document loading context
     * @param parent   parent component to add this new component to
     * @param warnings warning set to add import warnings to
     * @throws IllegalArgumentException if the parent component is null
     */
    public BoatTailHandler(DocumentLoadingContext context, RocketComponent parent, WarningSet warnings) throws IllegalArgumentException {
        super(context, parent, warnings);
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (RASAeroCommonConstants.FIN.equals(element)) {
            return new FinHandler(this.transition, warnings);
        }
        return super.openElement(element, attributes, warnings);
    }
}
