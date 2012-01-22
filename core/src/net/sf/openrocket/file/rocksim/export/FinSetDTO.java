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
 * This class models XML elements for Rocksim finsets.
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

    /**
     * Constructor.
     */
    public FinSetDTO() {
    }

    /**
     * Full copy constructor.
     *
     * @param theORFinSet  the OpenRocket finset
     */
    public FinSetDTO(FinSet theORFinSet) {
        super(theORFinSet);

        setFinCount(theORFinSet.getFinCount());
        setCantAngle(theORFinSet.getCantAngle());
        setTabDepth(theORFinSet.getTabHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabLength(theORFinSet.getTabLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabOffset(theORFinSet.getTabShift() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setThickness(theORFinSet.getThickness() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        setRadialAngle(theORFinSet.getBaseRotation());
        setTipShapeCode(TipShapeCode.convertTipShapeCode(theORFinSet.getCrossSection()));
        if (theORFinSet instanceof TrapezoidFinSet) {
            TrapezoidFinSet tfs = (TrapezoidFinSet) theORFinSet;
            setShapeCode(0);
            setRootChord(theORFinSet.getLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(tfs.getHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setTipChord(tfs.getTipChord() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSweepDistance(tfs.getSweep() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (theORFinSet instanceof EllipticalFinSet) {
            EllipticalFinSet efs = (EllipticalFinSet) theORFinSet;
            setShapeCode(1);
            setRootChord(theORFinSet.getLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(efs.getHeight() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (theORFinSet instanceof FreeformFinSet) {
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
