package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.BaseHandler;
import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.Parachute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "Parachute")
@XmlAccessorType(XmlAccessType.FIELD)
public class ParachuteDTO extends BasePartDTO {

    @XmlElement(name = "Dia")
    private double dia = 0d;
    @XmlElement(name = "SpillHoleDia")
    private double spillHoleDia = 0d;
    @XmlElement(name = "ShroudLineCount")
    private int ShroudLineCount = 0;
    @XmlElement(name = "Thickness")
    private double thickness = 0d;
    @XmlElement(name = "ShroudLineLen")
    private double shroudLineLen = 0d;
    @XmlElement(name = "ChuteCount")
    private int chuteCount = 1;
    @XmlElement(name = "ShroudLineMassPerMM")
    private double shroudLineMassPerMM = 0d;
    @XmlElement(name = "ShroudLineMaterial")
    private String shroudLineMaterial = "";
    @XmlElement(name = "DragCoefficient")
    private double dragCoefficient = 0.75d;
    
    public ParachuteDTO() {
    }

    public ParachuteDTO(Parachute ec) {
        super(ec);
        
        setChuteCount(1);
        setDia(ec.getDiameter() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setDragCoefficient(ec.getCD());
        setShroudLineCount(ec.getLineCount());
        setShroudLineLen(ec.getLineLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

        String material = ec.getLineMaterial().getName();
        setShroudLineMassPerMM(ec.getLineMaterial().getDensity() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LINE_DENSITY);

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

    public double getSpillHoleDia() {
        return spillHoleDia;
    }

    public void setSpillHoleDia(double theSpillHoleDia) {
        spillHoleDia = theSpillHoleDia;
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
