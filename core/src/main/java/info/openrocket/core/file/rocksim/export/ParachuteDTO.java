package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.importt.BaseHandler;
import info.openrocket.core.rocketcomponent.Parachute;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RockSimCommonConstants.PARACHUTE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ParachuteDTO extends BasePartDTO {

    @XmlElement(name = RockSimCommonConstants.DIAMETER)
    private double dia = 0d;
    @XmlElement(name = RockSimCommonConstants.SHROUD_LINE_COUNT)
    private int ShroudLineCount = 0;
    @XmlElement(name = RockSimCommonConstants.THICKNESS)
    private double thickness = 0d;
    @XmlElement(name = RockSimCommonConstants.SHROUD_LINE_LEN)
    private double shroudLineLen = 0d;
    @XmlElement(name = RockSimCommonConstants.CHUTE_COUNT)
    private int chuteCount = 1;
    @XmlElement(name = RockSimCommonConstants.SHROUD_LINE_MASS_PER_MM)
    private double shroudLineMassPerMM = 0d;
    @XmlElement(name = RockSimCommonConstants.SHROUD_LINE_MATERIAL)
    private String shroudLineMaterial = "";
    @XmlElement(name = RockSimCommonConstants.DRAG_COEFFICIENT)
    private double dragCoefficient = 0.75d;

    /**
     * Default constructor.
     */
    public ParachuteDTO() {
    }

    /**
     * Copy constructor. Fully populates this instance with values taken from the
     * corresponding OR Parachute.
     *
     * @param theORParachute the OR Parachute instance
     */
    public ParachuteDTO(Parachute theORParachute) {
        super(theORParachute);

        setChuteCount(1);
        setDia(theORParachute.getDiameter() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setDragCoefficient(theORParachute.getCD());
        setShroudLineCount(theORParachute.getLineCount());
        setShroudLineLen(theORParachute.getLineLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        String material = theORParachute.getLineMaterial().getName();
        setShroudLineMassPerMM(theORParachute.getLineMaterial().getDensity()
                * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY);

        if (material.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
            material = material.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
        }
        setShroudLineMaterial(material);
    }

    public double getDia() {
        return dia;
    }

    public void setDia(double theDia) {
        dia = theDia;
    }

    public int getShroudLineCount() {
        return ShroudLineCount;
    }

    public void setShroudLineCount(int theShroudLineCount) {
        ShroudLineCount = theShroudLineCount;
    }

    public double getThickness() {
        return thickness;
    }

    public void setThickness(double theThickness) {
        thickness = theThickness;
    }

    public double getShroudLineLen() {
        return shroudLineLen;
    }

    public void setShroudLineLen(double theShroudLineLen) {
        shroudLineLen = theShroudLineLen;
    }

    public int getChuteCount() {
        return chuteCount;
    }

    public void setChuteCount(int theChuteCount) {
        chuteCount = theChuteCount;
    }

    public double getShroudLineMassPerMM() {
        return shroudLineMassPerMM;
    }

    public void setShroudLineMassPerMM(double theShroudLineMassPerMM) {
        shroudLineMassPerMM = theShroudLineMassPerMM;
    }

    public String getShroudLineMaterial() {
        return shroudLineMaterial;
    }

    public void setShroudLineMaterial(String theShroudLineMaterial) {
        shroudLineMaterial = theShroudLineMaterial;
    }

    public double getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(double theDragCoefficient) {
        dragCoefficient = theDragCoefficient;
    }
}
