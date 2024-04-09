package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AnglePositionable;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.ParallelStage;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class PodHandler extends PositionDependentHandler<ComponentAssembly> {
    /**
     * The OpenRocket Pod.
     */
    private final RocketComponent parent;
    private final PodSet podSet;
    private final ParallelStage parallelStage;      // A RockSim podset can be an OpenRocket PodSet, or a ParallelStage if it is detachable
    private boolean isDetachable = false;
    private boolean isEjected = false;

    public PodHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) {
        super(context);
        if (c == null) {
            throw new IllegalArgumentException("The parent component of a pod set may not be null.");
        }
        this.parent = c;
        podSet = new PodSet();
        parallelStage = new ParallelStage();
        podSet.addConfigListener(parallelStage);        // The booster will now follow the same config changes as the podset
        podSet.setInstanceCount(1);     // RockSim only supports one pod instance
        podSet.setRadiusMethod(RadiusMethod.FREE);   // RockSim radial offset is relative to the center of the parent
        if (isCompatible(c, PodSet.class, warnings)) {
            c.addChild(podSet);
        }
        if (isCompatible(c, ParallelStage.class, warnings)) {
            c.addChild(parallelStage);
        }
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (RockSimCommonConstants.BODY_TUBE.equals(element)) {     // RockSim pods allow body tubes, not inner tubes
            return new BodyTubeHandler(context, podSet, warnings);
        }
        if (RockSimCommonConstants.ATTACHED_PARTS.equals(element)) {
            return new AttachedPartsHandler(context, podSet);
        }
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        super.closeElement(element, attributes, content, warnings);
        if (RockSimCommonConstants.RADIAL_ANGLE.equals(element)) {
            podSet.setAngleOffset(Double.parseDouble(content));
        }
        if (RockSimCommonConstants.RADIAL_LOC.equals(element)) {
            podSet.setRadiusOffset(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        if (RockSimCommonConstants.DETACHABLE.equals(element)) {
            int value = Integer.parseInt(content);
            isDetachable = value == 1;
        }
        if (RockSimCommonConstants.REMOVED.equals(element)) {
            int value = Integer.parseInt(content);
            isEjected = value == 1;
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        super.endHandler(element, attributes, content, warnings);

        // Since RockSim stores the angle offset of pod children in absolute coordinates (not relative to the parent pod set),
        // we need to subtract the parent pod set angle offset from this component's offset
        subtractAngleOffset(podSet, podSet.getAngleOffset());

        if (isDetachable || isEjected) {
            // The offsets of the parallel stage can change due to the children, so copy them from the podset and then apply
            // it later
            double axialOffset = podSet.getAxialOffset();
            double radiusOffset = podSet.getRadiusOffset();

            // Copy the children from the pod to the booster
            int childCount = podSet.getChildCount();
            for (int i = 0; i < childCount; i++) {
                RocketComponent child = podSet.getChild(0);
                podSet.removeChild(child);
                if (parallelStage.isCompatible(child)) {
                    parallelStage.addChild(child);
                } else {
                    warnings.add("The child component " + child.getName() + " of the podset " + podSet.getName() + " is not compatible with a parallel stage.");
                }
            }

            // Apply the offsets
            parallelStage.setAxialOffset(axialOffset);
            parallelStage.setRadiusOffset(radiusOffset);

            // Remove the podset from the parent
            parent.removeChild(podSet);

            if (isEjected) {
                parent.getRocket().getSelectedConfiguration()._setStageActive(parallelStage.getStageNumber(), false);
            }
        } else {
            // It's a normal podset, so remove the booster placeholder
            parent.removeChild(parallelStage);
        }
    }

    private void subtractAngleOffset(RocketComponent c, double angleOffset) {
        for (RocketComponent child : c.getChildren()) {
            if (child instanceof AnglePositionable anglePositionable) {
                anglePositionable.setAngleOffset(anglePositionable.getAngleOffset() - angleOffset);
            }
            if (!(child instanceof ComponentAssembly)) {
                subtractAngleOffset(child, angleOffset);
            }
        }
    }

    @Override
    protected ComponentAssembly getComponent() {
        return podSet;
    }

    @Override
    protected Material.Type getMaterialType() {
        return Material.Type.BULK;
    }
}
