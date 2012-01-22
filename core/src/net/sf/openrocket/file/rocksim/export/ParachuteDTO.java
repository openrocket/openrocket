package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.importt.BaseHandler;
import net.sf.openrocket.rocketcomponent.Parachute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RocksimCommonConstants.PARACHUTE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ParachuteDTO extends BasePartDTO {

    @XmlElement(name = RocksimCommonConstants.DIAMETER)
    private double dia = 0d;
    @XmlElement(name = RocksimCommonConstants.SHROUD_LINE_COUNT)
    private int ShroudLineCount = 0;
    @XmlElement(name = RocksimCommonConstants.THICKNESS)
    private double thickness = 0d;
    @XmlElement(name = RocksimCommonConstants.SHROUD_LINE_LEN)
    private double shroudLineLen = 0d;
    @XmlElement(name = RocksimCommonConstants.CHUTE_COUNT)
    private int chuteCount = 1;
    @XmlElement(name = RocksimCommonConstants.SHROUD_LINE_MASS_PER_MM)
    private double shroudLineMassPerMM = 0d;
    @XmlElement(name = RocksimCommonConstants.SHROUD_LINE_MATERIAL)
    private String shroudLineMaterial = "";
    @XmlElement(name = RocksimCommonConstants.DRAG_COEFFICIENT)
    private double dragCoefficient = 0.75d;

    /**
     * Default constructor.
     */
    public ParachuteDTO() {
    }

    /**
     * Copy constructor.  Fully populates this instance with values taken from the corresponding OR Parachute.
     *
     * @param theORParachute  the OR Parachute instance
     */
    public ParachuteDTO(Parachute theORParachute) {
        super(theORParachute);
        
        setChuteCount(1);
        setDia(theORParachute.getDiameter() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setDragCoefficient(theORParachute.getCD());
        setShroudLineCount(theORParachute.getLineCount());
        setShroudLineLen(theORParachute.getLineLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        String material = theORParachute.getLineMaterial().getName();
        setShroudLineMassPerMM(theORParachute.getLineMaterial().getDensity() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY);

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
