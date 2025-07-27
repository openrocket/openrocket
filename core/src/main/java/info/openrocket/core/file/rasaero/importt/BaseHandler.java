package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Base class for most RASAero components.
 * 
 * @param <C> type of OpenRocket component this base handler is supposed to
 *            create
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class BaseHandler<C extends RocketComponent> extends AbstractElementHandler {
    protected final DocumentLoadingContext context;

    protected Double length;
    protected Double diameter;
    protected Double location;

    public BaseHandler(DocumentLoadingContext context) {
        this.context = context;
    }

    /**
     * The SAX method called when the closing element tag is reached.
     *
     * @param element    the element name.
     * @param attributes attributes of the element.
     * @param content    the textual content of the element.
     * @param warnings   the warning set to store warnings in.
     *
     * @throws SAXException
     */

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        try {
            // There is also a PartType element, but we don't need it

            if (RASAeroCommonConstants.LENGTH.equals(element)) {
                length = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.DIAMETER.equals(element)) {
                diameter = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.LOCATION.equals(element)) {
                location = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.COLOR.equals(element)) {
                ColorHandler.applyRASAeroColor(getComponent(), content);
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    /**
     * Get the component this handler is working upon.
     *
     * @return a component
     */
    protected abstract RocketComponent getComponent();

    /**
     * Add child to parent only if the child is compatible. Otherwise add to warning
     * set.
     *
     * @param parent   the parent component
     * @param child    the child component
     * @param warnings the warning set
     *
     * @return true if the child is compatible with parent
     */
    protected static boolean isCompatible(RocketComponent parent, Class<? extends RocketComponent> child,
            WarningSet warnings) {
        return isCompatible(parent, child, warnings, false);
    }

    /**
     * Add child to parent only if the child is compatible. Otherwise add to warning
     * set.
     *
     * @param parent           the parent component
     * @param child            the child component
     * @param warnings         the warning set
     * @param suppressWarnings suppress warnings, just return the boolean
     *
     * @return true if the child is compatible with parent
     */
    protected static boolean isCompatible(RocketComponent parent, Class<? extends RocketComponent> child,
            WarningSet warnings,
            boolean suppressWarnings) {
        if (!parent.isCompatible(child)) {
            if (!suppressWarnings) {
                warnings.add(child.getName() + " can not be attached to "
                        + parent.getComponentName() + ", ignoring component.");
            }
            return false;
        } else {
            return true;
        }
    }
}
