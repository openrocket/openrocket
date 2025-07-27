package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A handler for the boattail element RASAero.
 * A boattail is just an OpenRocket transition, but because it can be recessed
 * in next symmetric components
 * (e.g. a body tube), we will add the boattail using an inline pod set to the
 * previous component.
 * In case the previous component is a nose cone, we will first add a phantom
 * body tube and then add the pod to
 * that body tube. I know, not the most elegant solution, but it grants us the
 * best cross-compatibility with RASAero.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BoattailHandler extends TransitionHandler {

    /**
     * Constructor
     *
     * @param context  current document loading context
     * @param parent   parent component to add this new component to
     * @param warnings warning set to add import warnings to
     * @throws IllegalArgumentException if the parent component is null
     */
    public BoattailHandler(DocumentLoadingContext context, RocketComponent parent, WarningSet warnings)
            throws IllegalArgumentException {
        super(context);

        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a boattail may not be null.");
        }
        if (parent.getChildCount() == 0) {
            throw new IllegalArgumentException("The parent component of a boattail must have at least one child.");
        }

        // Pod to add the boattail to
        PodSet pod = new PodSet();
        pod.setInstanceCount(1);
        pod.setRadius(RadiusMethod.FREE, 0);
        pod.setName("Boattail pod");
        pod.setComment(
                "Because boattails in RASAero can be recessed, we will add the boattail using an inline pod set to the previous component.");

        // Add the pod to the parent's last child, or to a phantom tube if the last
        // child is a nose cone/transition
        RocketComponent lastChild = parent.getChild(parent.getChildCount() - 1);
        if (lastChild instanceof BodyTube) {
            lastChild.addChild(pod);
            pod.setAxialMethod(AxialMethod.TOP);
            pod.setAxialOffset(lastChild.getLength());
        } else if (lastChild instanceof Transition) {
            BodyTube phantomBodyTube = new BodyTube();
            phantomBodyTube.setLength(0);
            phantomBodyTube.setOuterRadiusAutomatic(true);
            phantomBodyTube.setName("Boattail phantom tube");
            ColorHandler.applyRASAeroColor(phantomBodyTube, null); // Set the color to the default RASAero color
            parent.addChild(phantomBodyTube);
            phantomBodyTube.addChild(pod);
            pod.setAxialMethod(AxialMethod.TOP);
            pod.setAxialOffset(0);
        } else {
            throw new IllegalArgumentException(
                    "Cannot add boattail after component of type " + parent.getClass().getName());
        }

        pod.addChild(this.transition);
        this.transition.setAftRadiusAutomatic(false);
        this.transition.setShapeType(Transition.Shape.CONICAL); // RASAero only supports conical boattails
        this.transition.setName("Boattail");
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.FIN.equals(element)) {
            return new FinHandler(this.transition, warnings);
        }
        return super.openElement(element, attributes, warnings);
    }
}
