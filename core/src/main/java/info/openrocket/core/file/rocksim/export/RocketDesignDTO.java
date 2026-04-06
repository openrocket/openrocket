package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * This class models a Rocksim XML element for a rocket design container. It's
 * really nothing more than
 * a bunch of boilerplate XML that does not change, coupled with the stage DTOs
 * that are part of the rocket design.
 */
public class RocketDesignDTO {

    @JacksonXmlProperty(localName = "Name")
    private String name;
    @JacksonXmlProperty(localName = "StageCount")
    private int stageCount = 1;
    @JacksonXmlProperty(localName = "DisplayFlags")
    private final int displayFlags = 7;
    @JacksonXmlProperty(localName = "ViewType")
    private final int viewType = 0;
    @JacksonXmlProperty(localName = "ViewStageCount")
    private final int viewStageCount = 3;
    @JacksonXmlProperty(localName = "ViewTypeEdit")
    private final int viewTypeEdit = 0;
    @JacksonXmlProperty(localName = "ViewStageCountEdit")
    private final int viewStageCountEdit = 3;
    @JacksonXmlProperty(localName = "ZoomFactor")
    private final double zoomFactor = 0.0d;
    @JacksonXmlProperty(localName = "ZoomFactorEdit")
    private final double zoomFactorEdit = 0.0d;
    @JacksonXmlProperty(localName = "ScrollPosX")
    private final int scrollPosX = 0;
    @JacksonXmlProperty(localName = "ScrollPosY")
    private final int scrollPosY = 0;
    @JacksonXmlProperty(localName = "ScrollPosXEdit")
    private final int scrollPosXEdit = 0;
    @JacksonXmlProperty(localName = "ScrollPosYEdit")
    private final int scrollPosYEdit = 0;
    @JacksonXmlProperty(localName = "ThreeDFlags")
    private final int threeDFlags = 0;
    @JacksonXmlProperty(localName = "ThreeDFlagsEdit")
    private final int threeDFlagsEdit = 0;
    @JacksonXmlProperty(localName = "LastSerialNumber")
    private int lastSerialNumber = -1;
    @JacksonXmlProperty(localName = "Stage3Mass")
    private double stage3Mass = 0.0d;
    @JacksonXmlProperty(localName = "Stage2Mass")
    private double stage2Mass = 0.0d;
    @JacksonXmlProperty(localName = "Stage1Mass")
    private double stage1Mass = 0.0d;
    @JacksonXmlProperty(localName = "Stage3CG")
    private double stage3CG = 0.0d;
    @JacksonXmlProperty(localName = "Stage2CGAlone")
    private double stage2CGAlone = 0.0d;
    @JacksonXmlProperty(localName = "Stage1CGAlone")
    private double stage1CGAlone = 0.0d;
    @JacksonXmlProperty(localName = "Stage321CG")
    private double stage321CG = 0.0d;
    @JacksonXmlProperty(localName = "Stage32CG")
    private double stage32CG = 0.0d;

    @JacksonXmlProperty(localName = "CPCalcFlags")
    private final int cpCalcFlags = 1;
    @JacksonXmlProperty(localName = "CPSimFlags")
    private final int cpSimFlags = 1;
    @JacksonXmlProperty(localName = "UseKnownMass")
    private int useKnownMass = 0;
    @JacksonXmlProperty(localName = "Stage3Parts")
    private StageDTO stage3 = new StageDTO();
    @JacksonXmlProperty(localName = "Stage2Parts")
    private StageDTO stage2 = new StageDTO();
    @JacksonXmlProperty(localName = "Stage1Parts")
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
