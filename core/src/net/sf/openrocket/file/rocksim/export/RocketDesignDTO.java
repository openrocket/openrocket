package net.sf.openrocket.file.rocksim.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class models a Rocksim XML element for a rocket design container.  It's really nothing more than
 * a bunch of boilerplate XML that does not change, coupled with the stage DTOs that are part of the rocket design.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RocketDesignDTO {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "StageCount")
    private int stageCount = 1;
    @XmlElement(name = "DisplayFlags")
    private int displayFlags = 7;
    @XmlElement(name = "ViewType")
    private int viewType = 0;
    @XmlElement(name = "ViewStageCount")
    private int viewStageCount = 3;
    @XmlElement(name = "ViewTypeEdit")
    private int viewTypeEdit = 0;
    @XmlElement(name = "ViewStageCountEdit")
    private int viewStageCountEdit = 3;
    @XmlElement(name = "ZoomFactor")
    private double zoomFactor = 0d;
    @XmlElement (name = "ZoomFactorEdit")
    private double zoomFactorEdit = 0d;
    @XmlElement(name = "ScrollPosX")
    private int scrollPosX = 0;
    @XmlElement(name = "ScrollPosY")
    private int scrollPosY = 0;
    @XmlElement(name = "ScrollPosXEdit")
    private int scrollPosXEdit = 0;
    @XmlElement(name = "ScrollPosYEdit")
    private int scrollPosYEdit = 0;
    @XmlElement(name = "ThreeDFlags")
    private int threeDFlags = 0;
    @XmlElement(name = "ThreeDFlagsEdit")
    private int threeDFlagsEdit = 0;
    @XmlElement(name = "LastSerialNumber")
    private int lastSerialNumber = -1;
    @XmlElement(name = "Stage3Mass")
    private double stage3Mass = 0d;
    @XmlElement(name = "Stage2Mass")
    private double stage2Mass = 0d;
    @XmlElement(name = "Stage1Mass")
    private double stage1Mass = 0d;
    @XmlElement(name = "Stage3CG")
    private double stage3CG = 0d;
    @XmlElement(name = "Stage2CGAlone")
    private double stage2CGAlone = 0d;
    @XmlElement(name = "Stage1CGAlone")
    private double stage1CGAlone = 0d;
    @XmlElement(name = "Stage321CG")
    private double stage321CG = 0d;
    @XmlElement(name = "Stage32CG")
    private double stage32CG = 0d;

    @XmlElement(name = "CPCalcFlags")
    private int cpCalcFlags = 1;
    @XmlElement(name = "CPSimFlags")
    private int cpSimFlags = 1;
    @XmlElement(name = "UseKnownMass")
    private int useKnownMass = 0;
    @XmlElement(name = "Stage3Parts")
    private StageDTO stage3 = new StageDTO();
    @XmlElement(name = "Stage2Parts", required = true, nillable = false)
    private StageDTO stage2 = new StageDTO();
    @XmlElement(name = "Stage1Parts", required = false, nillable = false)
    private StageDTO stage1 = new StageDTO();

    /**
     * Default constructor.
     */
    public RocketDesignDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String theName) {
        name = theName;
    }

    public int getStageCount() {
        return stageCount;
    }

    public void setStageCount(int theStageCount) {
        stageCount = theStageCount;
    }

    public StageDTO getStage3() {
        return stage3;
    }

    public void setStage3(StageDTO theStage3) {
        stage3 = theStage3;
    }

    public StageDTO getStage2() {
        return stage2;
    }

    public void setStage2(StageDTO theStage2) {
        stage2 = theStage2;
    }

    public StageDTO getStage1() {
        return stage1;
    }

    public void setStage1(StageDTO theStage1) {
        stage1 = theStage1;
    }

    public int getUseKnownMass() {
        return useKnownMass;
    }

    public void setUseKnownMass(int useKnownMass) {
        this.useKnownMass = useKnownMass;
    }

    public double getStage3Mass() {
        return stage3Mass;
    }

    public void setStage3Mass(double stage3Mass) {
        this.stage3Mass = stage3Mass;
    }

    public double getStage2Mass() {
        return stage2Mass;
    }

    public void setStage2Mass(double stage2Mass) {
        this.stage2Mass = stage2Mass;
    }

    public double getStage1Mass() {
        return stage1Mass;
    }

    public void setStage1Mass(double stage1Mass) {
        this.stage1Mass = stage1Mass;
    }

    public double getStage3CG() {
        return stage3CG;
    }

    public void setStage3CG(double stage3CG) {
        this.stage3CG = stage3CG;
    }

    public double getStage2CGAlone() {
        return stage2CGAlone;
    }

    public void setStage2CGAlone(double stage2CGAlone) {
        this.stage2CGAlone = stage2CGAlone;
    }

    public double getStage1CGAlone() {
        return stage1CGAlone;
    }

    public void setStage1CGAlone(double stage1CGAlone) {
        this.stage1CGAlone = stage1CGAlone;
    }

    public double getStage321CG() {
        return stage321CG;
    }

    public void setStage321CG(double stage321CG) {
        this.stage321CG = stage321CG;
    }

    public double getStage32CG() {
        return stage32CG;
    }

    public void setStage32CG(double stage32CG) {
        this.stage32CG = stage32CG;
    }

    public int getLastSerialNumber() {
        return lastSerialNumber;
    }

    public void setLastSerialNumber(int lastSerialNumber) {
        this.lastSerialNumber = lastSerialNumber;
    }
}
