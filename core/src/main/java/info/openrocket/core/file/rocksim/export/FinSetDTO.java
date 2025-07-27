package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.TipShapeCode;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class models XML elements for Rocksim finsets.
 */
@XmlRootElement(name = RockSimCommonConstants.FIN_SET)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinSetDTO extends BasePartDTO {

    @XmlElement(name = RockSimCommonConstants.FIN_COUNT)
    private int finCount = 0;
    @XmlElement(name = RockSimCommonConstants.ROOT_CHORD)
    private double rootChord = 0.0d;
    @XmlElement(name = RockSimCommonConstants.TIP_CHORD)
    private double tipChord = 0.0d;
    @XmlElement(name = RockSimCommonConstants.SEMI_SPAN)
    private double semiSpan = 0.0d;
    @XmlElement(name = RockSimCommonConstants.SWEEP_DISTANCE)
    private double sweepDistance = 0.0d;
    @XmlElement(name = RockSimCommonConstants.THICKNESS)
    private double thickness = 0.0d;
    @XmlElement(name = RockSimCommonConstants.SHAPE_CODE)
    private int shapeCode = 0;
    @XmlElement(name = RockSimCommonConstants.TIP_SHAPE_CODE)
    private int tipShapeCode = 0;
    @XmlElement(name = RockSimCommonConstants.TAB_LENGTH)
    private double tabLength = 0.0d;
    @XmlElement(name = RockSimCommonConstants.TAB_DEPTH)
    private double tabDepth = 0.0d;
    @XmlElement(name = RockSimCommonConstants.TAB_OFFSET)
    private double tabOffset = 0.0d;
    @XmlElement(name = RockSimCommonConstants.SWEEP_MODE)
    private int sweepMode = 1;
    @XmlElement(name = RockSimCommonConstants.CANT_ANGLE)
    private double cantAngle = 0.0d;

    /**
     * Constructor.
     */
    public FinSetDTO() {
    }

    /**
     * Full copy constructor.
     *
     * @param theORFinSet the OpenRocket finset
     */
    public FinSetDTO(FinSet theORFinSet) {
        super(theORFinSet);

        setFinCount(theORFinSet.getFinCount());
        setCantAngle(theORFinSet.getCantAngle());
        setTabDepth(theORFinSet.getTabHeight() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabLength(theORFinSet.getTabLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setTabOffset(theORFinSet.getTabOffset(AxialMethod.TOP) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setThickness(theORFinSet.getThickness() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        setRadialAngle(theORFinSet.getBaseRotation());
        setTipShapeCode(TipShapeCode.convertTipShapeCode(theORFinSet.getCrossSection()));
        if (theORFinSet instanceof TrapezoidFinSet) {
            TrapezoidFinSet tfs = (TrapezoidFinSet) theORFinSet;
            setShapeCode(0);
            setRootChord(theORFinSet.getLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(tfs.getHeight() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setTipChord(tfs.getTipChord() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSweepDistance(tfs.getSweep() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        } else if (theORFinSet instanceof EllipticalFinSet) {
            EllipticalFinSet efs = (EllipticalFinSet) theORFinSet;
            setShapeCode(1);
            setRootChord(theORFinSet.getLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            setSemiSpan(efs.getHeight() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        } else if (theORFinSet instanceof FreeformFinSet) {
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
