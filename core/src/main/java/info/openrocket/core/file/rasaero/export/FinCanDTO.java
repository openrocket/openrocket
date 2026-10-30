package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.MathUtil;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A RASAero fin can. The RASAero importer represents a fin can as an inline pod set at the end of the body tube it
 * slides over, containing the fin can tube and optionally its shoulder.
 */
@XmlRootElement(name = RASAeroCommonConstants.FIN_CAN)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "insideDiameter",
        "shoulderLength"
})
public class FinCanDTO extends BodyTubeDTO {
    @XmlElement(name = RASAeroCommonConstants.INSIDE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double insideDiameter = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.SHOULDER_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double shoulderLength = 0.0d;

    /**
     * We need a default no-args constructor.
     */
    public FinCanDTO() {
        super();
    }

    public FinCanDTO(PodSet finCan, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(getTube(finCan), warnings, errors);
        setPartType(RASAeroCommonConstants.FIN_CAN);

        Transition shoulder = getShoulder(finCan);
        double insideRadius = shoulder != null ? shoulder.getForeRadius() : ((BodyTube) finCan.getParent()).getOuterRadius();
        insideDiameter = insideRadius * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        shoulderLength = shoulder != null ? shoulder.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH : 0;
    }

    /**
     * Returns whether a component is a fin can as created by the RASAero importer: a single, centered pod set on a
     * body tube that contains one body tube and at most one shoulder transition.
     *
     * @param component the component to check
     * @return true if the component is a fin can
     */
    public static boolean isFinCan(RocketComponent component) {
        if (!isInlinePod(component) || !(component.getParent() instanceof BodyTube) || getTube(component) == null) {
            return false;
        }
        int tubes = 0;
        int transitions = 0;
        for (RocketComponent child : component.getChildren()) {
            if (child instanceof BodyTube) {
                tubes++;
            } else if (child instanceof Transition) {
                transitions++;
            } else {
                return false;
            }
        }
        return tubes == 1 && transitions <= 1;
    }

    /**
     * Returns whether a component is a single, centered pod set, which the RASAero importer uses to place fin cans
     * and boattails.
     *
     * @param component the component to check
     * @return true if the component is a single, centered pod set
     */
    static boolean isInlinePod(RocketComponent component) {
        return component instanceof PodSet && ((PodSet) component).getInstanceCount() == 1
                && MathUtil.equals(((PodSet) component).getRadiusOffset(), 0);
    }

    private static BodyTube getTube(RocketComponent finCan) {
        for (RocketComponent child : finCan.getChildren()) {
            if (child instanceof BodyTube) {
                return (BodyTube) child;
            }
        }
        return null;
    }

    private static Transition getShoulder(RocketComponent finCan) {
        for (RocketComponent child : finCan.getChildren()) {
            if (child instanceof Transition) {
                return (Transition) child;
            }
        }
        return null;
    }

    public Double getInsideDiameter() {
        return insideDiameter;
    }

    public Double getShoulderLength() {
        return shoulderLength;
    }
}
