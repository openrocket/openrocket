package net.sf.openrocket.file.rocksim.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
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

    @XmlElement(name = "CPCalcFlags")
    private String cpCalcFlags = "1";
    @XmlElement(name = "UseKnownMass")
    private String useKnownMass = "0";
    @XmlElement(name = "Stage3Parts")
    private StageDTO stage3 = new StageDTO();
    @XmlElement(name = "Stage2Parts", required = true, nillable = false)
    private StageDTO stage2 = new StageDTO();
    @XmlElement(name = "Stage1Parts", required = false, nillable = false)
    private StageDTO stage1 = new StageDTO();

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
}
