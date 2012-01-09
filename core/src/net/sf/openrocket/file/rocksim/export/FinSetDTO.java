package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.TipShapeCode;
import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "FinSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class FinSetDTO extends BasePartDTO {

    @XmlElement(name = "FinCount")
    private int finCount = 0;
    @XmlElement(name = "RootChord")
    private double rootChord = 0d;
    @XmlElement(name = "TipChord")
    private double tipChord = 0d;
    @XmlElement(name = "SemiSpan")
    private double semiSpan = 0d;
    @XmlElement(name = "SweepDistance")
    private double sweepDistance = 0d;
    @XmlElement(name = "Thickness")
    private double thickness = 0d;
    @XmlElement(name = "ShapeCode")
    private int shapeCode = 0;
    @XmlElement(name = "TipShapeCode")
    private int tipShapeCode = 0;
    @XmlElement(name = "TabLength")
    private double tabLength = 0d;
    @XmlElement(name = "TabDepth")
    private double tabDepth = 0d;
    @XmlElement(name = "TabOffset")
    private double tabOffset = 0d;
    @XmlElement(name = "SweepMode")
    private int sweepMode = 1;
    @XmlElement(name = "CantAngle")
    private double cantAngle = 0d;

    public FinSetDTO() {
    }

    public FinSetDTO(FinSet ec) {
        super(ec);

        setFinCount(ec.getFinCount());
        setCantAngle(ec.getCantAngle());
        setTabDepth(ec.getTabHeight() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabLength(ec.getTabLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabOffset(ec.getTabShift() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setThickness(ec.getThickness() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

        setRadialAngle(ec.getBaseRotation());
        setTipShapeCode(TipShapeCode.convertTipShapeCode(ec.getCrossSection()));
        if (ec instanceof TrapezoidFinSet) {
            TrapezoidFinSet tfs = (TrapezoidFinSet) ec;
            setShapeCode(0);
            setRootChord(ec.getLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(tfs.getHeight() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            setTipChord(tfs.getTipChord() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSweepDistance(tfs.getSweep() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (ec instanceof EllipticalFinSet) {
            EllipticalFinSet efs = (EllipticalFinSet) ec;
            setShapeCode(1);
            setRootChord(ec.getLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(efs.getHeight() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (ec instanceof FreeformFinSet) {
            setShapeCode(2);
        }
    }

    public int getFinCount() {
        return finCount;
    }

    public void setFinCount(int theFinCount) {
        finCount = theFinCount;
    }

    public double getRootChord() {
        return rootChord;
    }

    public void setRootChord(double theRootChord) {
        rootChord = theRootChord;
    }

    public double getTipChord() {
        return tipChord;
    }

    public void setTipChord(double theTipChord) {
        tipChord = theTipChord;
    }

    public double getSemiSpan() {
        return semiSpan;
    }

    public void setSemiSpan(double theSemiSpan) {
        semiSpan = theSemiSpan;
    }

    public double getSweepDistance() {
        return sweepDistance;
    }

    public void setSweepDistance(double theSweepDistance) {
        sweepDistance = theSweepDistance;
    }

    public double getThickness() {
        return thickness;
    }

    public void setThickness(double theThickness) {
        thickness = theThickness;
    }

    public int getShapeCode() {
        return shapeCode;
    }

    public void setShapeCode(int theShapeCode) {
        shapeCode = theShapeCode;
    }

    public int getTipShapeCode() {
        return tipShapeCode;
    }

    public void setTipShapeCode(int theTipShapeCode) {
        tipShapeCode = theTipShapeCode;
    }

    public double getTabLength() {
        return tabLength;
    }

    public void setTabLength(double theTabLength) {
        tabLength = theTabLength;
    }

    public double getTabDepth() {
        return tabDepth;
    }

    public void setTabDepth(double theTabDepth) {
        tabDepth = theTabDepth;
    }

    public double getTabOffset() {
        return tabOffset;
    }

    public void setTabOffset(double theTabOffset) {
        tabOffset = theTabOffset;
    }

    public int getSweepMode() {
        return sweepMode;
    }

    public void setSweepMode(int theSweepMode) {
        sweepMode = theSweepMode;
    }

    public double getCantAngle() {
        return cantAngle;
    }

    public void setCantAngle(double theCantAngle) {
        cantAngle = theCantAngle;
    }
}
