package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

@XmlRootElement(name = RASAeroCommonConstants.TRANSITION)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "partType",
        "length",
        "diameter",
        "rearDiameter",
        "location",
        "color"
})
@XmlSeeAlso({ BoattailDTO.class })
public class TransitionDTO extends BasePartDTO {

    @XmlElement(name = RASAeroCommonConstants.REAR_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rearDiameter;

    @XmlTransient
    private static final Translator trans = Application.getTranslator();
    @XmlTransient
    private static Transition component = null;

    /**
     * We need a default no-args constructor.
     */
    public TransitionDTO() {
    }

    public TransitionDTO(Transition transition, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(transition, warnings, errors);

        component = transition;

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

    public void setRearDiameter(Double rearDiameter) throws RASAeroExportException {
        if (rearDiameter < 0.0001) {
            throw new RASAeroExportException(
                    String.format("'%s' rear diameter must be greater than 0.0001 inch", component));
        }
        this.rearDiameter = rearDiameter;
    }
}
