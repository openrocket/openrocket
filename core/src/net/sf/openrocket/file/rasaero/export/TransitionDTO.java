package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

import java.util.Objects;

@XmlRootElement(name = RASAeroCommonConstants.TRANSITION)
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDTO extends BasePartDTO {

    @XmlElement(name = RASAeroCommonConstants.REAR_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rearDiameter;

    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default no-args constructor.
     */
    public TransitionDTO() {
    }

    public TransitionDTO(Transition transition, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(transition, warnings, errors);

        if (!transition.getShapeType().equals(Transition.Shape.CONICAL)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error26"));
        }

        SymmetricComponent previousComp = transition.getPreviousSymmetricComponent();
        if (previousComp == null) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error27"), transition.getName()));
        }
        if (!MathUtil.equals(transition.getForeRadius(), previousComp.getAftRadius())) {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error28"),
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
