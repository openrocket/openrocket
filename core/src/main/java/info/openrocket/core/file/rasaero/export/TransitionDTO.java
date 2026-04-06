package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.TRANSITION)
public class TransitionDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RASAeroCommonConstants.REAR_DIAMETER)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double rearDiameter;

    @JsonIgnore
    private static final Translator trans = Application.getTranslator();
    @JsonIgnore
    private Transition component = null;

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
