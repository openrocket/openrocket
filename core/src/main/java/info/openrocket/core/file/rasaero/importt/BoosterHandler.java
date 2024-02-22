package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for the Booster element.
 * A booster in RASAero is an OpenRocket AxialStage with a BodyTube as its
 * child.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BoosterHandler extends BodyTubeHandler {
    final AxialStage boosterStage;
    private double boatTailLength;
    private double boatTailRearDiameter;
    private double shoulderLength;

    public BoosterHandler(DocumentLoadingContext context, RocketComponent parent) {
        super(context);
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a body tube may not be null.");
        }
        final Rocket rocket = parent.getRocket();
        this.boosterStage = new AxialStage();
        String stageName = rocket.getStageCount() <= 1 ? "Booster 1" : "Booster 2";
        this.boosterStage.setName(stageName);

        rocket.addChild(this.boosterStage);
        this.boosterStage.addChild(this.bodyTube);
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.BOATTAIL_LENGTH.equals(element)
                || RASAeroCommonConstants.BOATTAIL_REAR_DIAMETER.equals(element)
                || RASAeroCommonConstants.SHOULDER_LENGTH.equals(element)) {
            return PlainTextHandler.INSTANCE;
        }
        return super.openElement(element, attributes, warnings);
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        if (RASAeroCommonConstants.BOATTAIL_LENGTH.equals(element)) {
            this.boatTailLength = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        } else if (RASAeroCommonConstants.BOATTAIL_REAR_DIAMETER.equals(element)) {
            this.boatTailRearDiameter = Double.parseDouble(content)
                    / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        } else if (RASAeroCommonConstants.SHOULDER_LENGTH.equals(element)) {
            this.shoulderLength = Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        this.bodyTube.setName(this.boosterStage.getName() + " Body Tube");
        this.bodyTube.setOuterRadiusAutomatic(false);

        // Add shoulder (transition) if it exists
        if (shoulderLength > 0) {
            Transition shoulder = new Transition();
            shoulder.setName(boosterStage.getName() + " Shoulder");
            shoulder.setColor(this.bodyTube.getColor());
            shoulder.setShapeType(Transition.Shape.CONICAL);
            shoulder.setLength(shoulderLength);
            shoulder.setAftRadiusAutomatic(false);
            shoulder.setForeRadiusAutomatic(true);
            shoulder.setAftRadius(this.bodyTube.getOuterRadius());
            this.boosterStage.addChild(shoulder, 0);
        }

        // Add boat tail if it exists
        if (this.boatTailLength > 0 && this.boatTailRearDiameter > 0) {
            Transition boatTail = new Transition();
            boatTail.setName(boosterStage.getName() + " Boat Tail");
            boatTail.setColor(this.bodyTube.getColor());
            boatTail.setShapeType(Transition.Shape.CONICAL);
            boatTail.setForeRadiusAutomatic(false);
            boatTail.setAftRadiusAutomatic(false);
            boatTail.setForeRadius(this.bodyTube.getOuterRadius());
            boatTail.setAftRadius(this.boatTailRearDiameter / 2);
            boatTail.setLength(this.boatTailLength);

            this.boosterStage.addChild(boatTail);
        }
    }
}
