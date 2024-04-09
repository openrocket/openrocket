package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A RASAero fin can is basically a body tube with fins on that slides over
 * another body tube.
 * The start of the fin can is a transition from the outer diameter of the fin
 * can tube, to the
 * outer diameter of the parent tube.
 * We will represent the fin can with an inline pod set.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class FinCanHandler extends BodyTubeHandler {
    private final PodSet finCan = new PodSet();

    private double insideDiameter;
    private double shoulderLength;

    public FinCanHandler(DocumentLoadingContext context, RocketComponent parent) {
        super(context);
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a body tube may not be null.");
        }
        // The body tube that the fin can is attached to should be added previously.
        RocketComponent lastChild = parent.getChild(parent.getChildCount() - 1);
        if (lastChild == null) {
            throw new IllegalArgumentException("There is no component to attach the fin can to.");
        }
        if (!(lastChild instanceof BodyTube)) {
            throw new IllegalArgumentException("The parent component of a fin can must be a body tube.");
        }

        // The fin can is a pod set child of the parent body tube.
        BodyTube parentBodyTube = (BodyTube) lastChild;
        parentBodyTube.addChild(this.finCan);
        this.finCan.setInstanceCount(1);
        this.finCan.setRadius(RadiusMethod.FREE, 0);
        this.finCan.addChild(this.bodyTube);
        this.finCan.setName("Fin Can");
        this.bodyTube.setName("Fin Can Tube");

        // A fin can is always positioned at the end of the parent body tube.
        this.finCan.setAxialMethod(AxialMethod.BOTTOM);
        this.finCan.setAngleOffset(0);
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        try {
            if (RASAeroCommonConstants.INSIDE_DIAMETER.equals(element)) {
                insideDiameter = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
            } else if (RASAeroCommonConstants.SHOULDER_LENGTH.equals(element)) {
                shoulderLength = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        this.bodyTube.setOuterRadiusAutomatic(false);

        // Add the shoulder to the front of the fin can
        Transition shoulder = new Transition();
        shoulder.setName("Fin Can Shoulder");
        shoulder.setShapeType(Transition.Shape.CONICAL);
        shoulder.setForeRadiusAutomatic(false);
        shoulder.setAftRadiusAutomatic(false);
        shoulder.setLength(shoulderLength);
        shoulder.setForeRadius(insideDiameter / 2);
        shoulder.setAftRadius(bodyTube.getOuterRadius());
        shoulder.setThickness(bodyTube.getThickness());
        shoulder.setColor(bodyTube.getColor());

        finCan.addChild(shoulder, 0);
    }
}
