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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

/**
 * The base class for most RASAero components.
 */
@XmlRootElement
@XmlType(name = "RASAeroBasePartDTO")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
public class BasePartDTO {
    @XmlElement(name = RASAeroCommonConstants.PART_TYPE)
    private String partType;
    @XmlElement(name = RASAeroCommonConstants.LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double length;
    @XmlElement(name = RASAeroCommonConstants.DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double diameter;
    @XmlElement(name = RASAeroCommonConstants.LOCATION)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double location;
    @XmlElement(name = RASAeroCommonConstants.COLOR)
    private String color;

    @XmlTransient
    private final RocketComponent component;
    @XmlTransient
    private final WarningSet warnings;
    @XmlTransient
    private final ErrorSet errors;
    @XmlTransient
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
