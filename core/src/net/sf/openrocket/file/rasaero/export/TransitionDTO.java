package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.MathUtil;

import java.util.Objects;

@XmlRootElement(name = RASAeroCommonConstants.TRANSITION)
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends BasePartDTO {

    @XmlElement(name = RASAeroCommonConstants.REAR_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rearDiameter;

    /**
     * We need a default no-args constructor.
     */
    public TransitionDTO() {
    }

    public TransitionDTO(Transition transition) throws RASAeroExportException {
        super(transition);

        if (!transition.getShapeType().equals(Transition.Shape.CONICAL)) {
            throw new RASAeroExportException("RASAero only supports conical transitions");
        }

        SymmetricComponent previousComp = transition.getPreviousSymmetricComponent();
        if (previousComp == null) {
            throw new RASAeroExportException(String.format("Transition '%s' has no previous component", transition.getName()));
        }
        if (!MathUtil.equals(transition.getForeRadius(), previousComp.getAftRadius())) {
            throw new RASAeroExportException(
                    String.format("Transition '%s' should have the same fore radius as the aft radius (%f) of its previous component, not (%f)",
                            transition.getName(), previousComp.getAftRadius(), transition.getForeRadius()));
        }

        setRearDiameter(transition.getAftRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
    }

    public Double getRearDiameter() {
        return rearDiameter;
    }

    public void setRearDiameter(Double rearDiameter) {
        this.rearDiameter = rearDiameter;
    }
}
