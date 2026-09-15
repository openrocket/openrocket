package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

/**
 * The base class for most RASAero components.
 */
@JacksonXmlRootElement
public class BasePartDTO {
    @JacksonXmlProperty(localName = RASAeroCommonConstants.PART_TYPE)
    private String partType;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LENGTH)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double length;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.DIAMETER)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double diameter;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LOCATION)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double location;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.COLOR)
    private String color;

    @JsonIgnore
    private final RocketComponent component;
    @JsonIgnore
    private final WarningSet warnings;
    @JsonIgnore
    private final ErrorSet errors;
    @JsonIgnore
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default no-args constructor.
     */
    public BasePartDTO() {
        this.component = null;
        this.warnings = null;
        this.errors = null;
    }

    protected BasePartDTO(RocketComponent component, WarningSet warnings, ErrorSet errors)
            throws RASAeroExportException {
        this.component = component;
        this.warnings = warnings;
        this.errors = errors;

        if (component instanceof BodyTube) {
            setPartType(RASAeroCommonConstants.BODY_TUBE);
            setDiameter(
                    ((BodyTube) component).getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else if (component instanceof NoseCone) {
            setPartType(RASAeroCommonConstants.NOSE_CONE);
            NoseCone noseCone = (NoseCone) component;
            if (noseCone.isFlipped()) {
                throw new RASAeroExportException(trans.get("RASAeroExport.warning1"));
            }
            setDiameter(
                    ((NoseCone) component).getAftRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else if (component instanceof Transition) {
            setPartType(RASAeroCommonConstants.TRANSITION);
            // This is a bit strange: I would expect diameter to be the fore radius, since
            // you also have a rearDiamter
            // field for transitions, but okay
            setDiameter(
                    ((Transition) component).getAftRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else if (component instanceof AxialStage) {
            setPartType(RASAeroCommonConstants.BOOSTER);
            AxialStage stage = (AxialStage) component;
            if (stage.getChildCount() == 0 || !(stage.getChild(0) instanceof BodyTube)) {
                throw new RASAeroExportException(trans.get("RASAeroExport.warning2"));
            }
            setDiameter(stage.getBoundingRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error1"), component.getComponentName()));
        }

        setLength(component.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setLocation(
                component.getAxialOffset(AxialMethod.ABSOLUTE) * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setColor(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_COLOR(component.getColor()));
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) throws RASAeroExportException {
        if (MathUtil.equals(length, 0)) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error2"), component.getName()));
        }
        this.length = length;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) throws RASAeroExportException {
        if (MathUtil.equals(diameter, 0)) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error3"), component.getName()));
        }
        this.diameter = diameter;
    }

    public Double getLocation() {
        return location;
    }

    public void setLocation(Double location) {
        this.location = location;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
