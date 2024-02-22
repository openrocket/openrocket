package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class PodHandler extends PositionDependentHandler<PodSet> {
    /**
     * The OpenRocket BodyTube.
     */
    private final PodSet podSet;

    public PodHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) {
        super(context);
        if (c == null) {
            throw new IllegalArgumentException("The parent component of a pod set may not be null.");
        }
        podSet = new PodSet();
        podSet.setInstanceCount(1); // RockSim only supports one pod instance
        podSet.setRadiusMethod(RadiusMethod.FREE); // RockSim radial offset is relative to the center of the parent
        if (isCompatible(c, PodSet.class, warnings)) {
            c.addChild(podSet);
        }
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RockSimCommonConstants.BODY_TUBE.equals(element)) { // RockSim pods allow body tubes, not inner tubes
            return new BodyTubeHandler(context, podSet, warnings);
        }
        if (RockSimCommonConstants.ATTACHED_PARTS.equals(element)) {
            return new AttachedPartsHandler(context, podSet);
        }
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        if (RockSimCommonConstants.RADIAL_ANGLE.equals(element)) {
            podSet.setAngleOffset(Double.parseDouble(content));
        }
        if (RockSimCommonConstants.RADIAL_LOC.equals(element)) {
            podSet.setRadiusOffset(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
    }

    @Override
    protected PodSet getComponent() {
        return podSet;
    }

    @Override
    protected Material.Type getMaterialType() {
        return Material.Type.BULK;
    }
}
