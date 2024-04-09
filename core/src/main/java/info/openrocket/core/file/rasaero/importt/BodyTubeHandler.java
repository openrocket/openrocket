package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * RASAero parser for OpenRocket body tube.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BodyTubeHandler extends BaseHandler<BodyTube> {
    protected final BodyTube bodyTube = new BodyTube();

    private double launchLugDiameter;
    private double launchLugLength;
    private double railGuideDiameter;
    private double railGuideHeight;

    public BodyTubeHandler(DocumentLoadingContext context, RocketComponent parent, WarningSet warnings) {
        super(context);
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a body tube may not be null.");
        }
        if (isCompatible(parent, BodyTube.class, warnings)) {
            parent.addChild(this.bodyTube);
        } else {
            throw new IllegalArgumentException("Cannot add body tube to parent of type " + parent.getClass().getName());
        }
    }

    public BodyTubeHandler(DocumentLoadingContext context) {
        super(context);
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.FIN.equals(element)) {
            return new FinHandler(this.bodyTube, warnings);
        }
        if (RASAeroCommonConstants.LENGTH.equals(element) || RASAeroCommonConstants.DIAMETER.equals(element) ||
                RASAeroCommonConstants.LAUNCH_LUG_DIAMETER.equals(element)
                || RASAeroCommonConstants.LAUNCH_LUG_LENGTH.equals(element) ||
                RASAeroCommonConstants.RAIL_GUIDE_DIAMETER.equals(element)
                || RASAeroCommonConstants.RAIL_GUIDE_HEIGHT.equals(element) ||
                RASAeroCommonConstants.LOCATION.equals(element) || RASAeroCommonConstants.COLOR.equals(element)) {
            return PlainTextHandler.INSTANCE;
        }
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        try {
            if (RASAeroCommonConstants.LAUNCH_LUG_DIAMETER.equals(element)) {
                launchLugDiameter = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.LAUNCH_LUG_LENGTH.equals(element)) {
                launchLugLength = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.RAIL_GUIDE_DIAMETER.equals(element)) {
                railGuideDiameter = Double.parseDouble(content);
            } else if (RASAeroCommonConstants.RAIL_GUIDE_HEIGHT.equals(element)) {
                railGuideHeight = Double.parseDouble(content);
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        this.bodyTube.setLength(length / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        this.bodyTube.setOuterRadius(diameter / 2 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH); // Not really
                                                                                                          // useful, but
                                                                                                          // included
                                                                                                          // for
                                                                                                          // completeness
        this.bodyTube.setOuterRadiusAutomatic(true);
        this.bodyTube.setThickness(0.002); // Arbitrary value; RASAero doesn't specify this

        if (launchLugDiameter > 0 && launchLugLength > 0) {
            LaunchLugHandler.addLaunchLug(this.bodyTube, launchLugDiameter, launchLugLength);
        }
        if (railGuideDiameter > 0 && railGuideHeight > 0) {
            RailGuideHandler.addRailGuide(this.bodyTube, railGuideDiameter, railGuideHeight);
        }
    }

    @Override
    protected RocketComponent getComponent() {
        return this.bodyTube;
    }
}
