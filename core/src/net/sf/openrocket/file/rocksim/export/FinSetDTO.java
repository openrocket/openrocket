package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.TipShapeCode;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
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
@XmlRootElement(name = RocksimCommonConstants.FIN_SET)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinSetDTO extends BasePartDTO {

    @XmlElement(name = RocksimCommonConstants.FIN_COUNT)
    private int finCount = 0;
    @XmlElement(name = RocksimCommonConstants.ROOT_CHORD)
    private double rootChord = 0d;
    @XmlElement(name = RocksimCommonConstants.TIP_CHORD)
    private double tipChord = 0d;
    @XmlElement(name = RocksimCommonConstants.SEMI_SPAN)
    private double semiSpan = 0d;
    @XmlElement(name = RocksimCommonConstants.SWEEP_DISTANCE)
    private double sweepDistance = 0d;
    @XmlElement(name = RocksimCommonConstants.THICKNESS)
    private double thickness = 0d;
    @XmlElement(name = RocksimCommonConstants.SHAPE_CODE)
    private int shapeCode = 0;
    @XmlElement(name = RocksimCommonConstants.TIP_SHAPE_CODE)
    private int tipShapeCode = 0;
    @XmlElement(name = RocksimCommonConstants.TAB_LENGTH)
    private double tabLength = 0d;
    @XmlElement(name = RocksimCommonConstants.TAB_DEPTH)
    private double tabDepth = 0d;
    @XmlElement(name = RocksimCommonConstants.TAB_OFFSET)
    private double tabOffset = 0d;
    @XmlElement(name = RocksimCommonConstants.SWEEP_MODE)
    private int sweepMode = 1;
    @XmlElement(name = RocksimCommonConstants.CANT_ANGLE)
    private double cantAngle = 0d;

    public FinSetDTO() {
    }

    public FinSetDTO(FinSet ec) {
        super(ec);

        setFinCount(ec.getFinCount());
        setCantAngle(ec.getCantAngle());
        setTabDepth(ec.getTabHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabLength(ec.getTabLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabOffset(ec.getTabShift() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setThickness(ec.getThickness() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        setRadialAngle(ec.getBaseRotation());
        setTipShapeCode(TipShapeCode.convertTipShapeCode(ec.getCrossSection()));
        if (ec instanceof TrapezoidFinSet) {
            TrapezoidFinSet tfs = (TrapezoidFinSet) ec;
            setShapeCode(0);
            setRootChord(ec.getLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(tfs.getHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setTipChord(tfs.getTipChord() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSweepDistance(tfs.getSweep() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (ec instanceof EllipticalFinSet) {
            EllipticalFinSet efs = (EllipticalFinSet) ec;
            setShapeCode(1);
            setRootChord(ec.getLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(efs.getHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
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
