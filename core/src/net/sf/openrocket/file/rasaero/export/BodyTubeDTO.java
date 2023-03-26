package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import net.sf.openrocket.util.MathUtil;

@XmlRootElement(name = RASAeroCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
public class BodyTubeDTO extends BasePartDTO implements BodyTubeDTOAdapter {
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_LUG_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchLugDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_LUG_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchLugLength = 0d;
    @XmlElement(name = RASAeroCommonConstants.RAIL_GUIDE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double railGuideDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.RAIL_GUIDE_HEIGHT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double railGuideHeight = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_SHOE_AREA)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchShoeArea = 0d;      // Currently not available in OR

    @XmlElement(name = RASAeroCommonConstants.BOATTAIL_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double boattailLength = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOATTAIL_REAR_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double boattailRearDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOATTAIL_OFFSET)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double boattailOffset = 0d;
    @XmlElement(name = RASAeroCommonConstants.OVERHANG)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double overhang = 0d;

    @XmlElementRef(name = RASAeroCommonConstants.FIN, type = FinDTO.class)
    private FinDTO fin;

    /**
     * We need a default no-args constructor.
     */
    public BodyTubeDTO() { }

    public BodyTubeDTO(BodyTube bodyTube) throws RASAeroExportException {
        super(bodyTube);
        applyBodyTubeSettings(bodyTube);
    }

    public Double getLaunchLugDiameter() {
        return launchLugDiameter;
    }

    public void setLaunchLugDiameter(Double launchLugDiameter) throws RASAeroExportException {
        if (MathUtil.equals(launchLugDiameter, 0)) {
            throw new RASAeroExportException("Launch lug diameter can not be 0");
        }
        this.launchLugDiameter = launchLugDiameter;
    }

    public Double getLaunchLugLength() {
        return launchLugLength;
    }

    public void setLaunchLugLength(Double launchLugLength) throws RASAeroExportException {
        if (MathUtil.equals(launchLugLength, 0)) {
            throw new RASAeroExportException("Launch lug length can not be 0");
        }
        this.launchLugLength = launchLugLength;
    }

    public Double getRailGuideDiameter() {
        return railGuideDiameter;
    }

    public void setRailGuideDiameter(Double railGuideDiameter) throws RASAeroExportException {
        if (MathUtil.equals(railGuideDiameter, 0)) {
            throw new RASAeroExportException("Rail button diameter can not be 0");
        }
        this.railGuideDiameter = railGuideDiameter;
    }

    public Double getRailGuideHeight() {
        return railGuideHeight;
    }

    public void setRailGuideHeight(Double railGuideHeight) throws RASAeroExportException {
        if (MathUtil.equals(railGuideHeight, 0)) {
            throw new RASAeroExportException("Rail button height can not be 0");
        }
        this.railGuideHeight = railGuideHeight;
    }

    public Double getLaunchShoeArea() {
        return launchShoeArea;
    }

    public void setLaunchShoeArea(Double launchShoeArea) throws RASAeroExportException {
        if (MathUtil.equals(launchShoeArea, 0)) {
            throw new RASAeroExportException("Launch shoe area can not be 0");
        }
        this.launchShoeArea = launchShoeArea;
    }

    public Double getBoattailLength() {
        return boattailLength;
    }

    public void setBoattailLength(Double boattailLength) {
        this.boattailLength = boattailLength;
    }

    public Double getBoattailRearDiameter() {
        return boattailRearDiameter;
    }

    public void setBoattailRearDiameter(Double boattailRearDiameter) {
        this.boattailRearDiameter = boattailRearDiameter;
    }

    public Double getBoattailOffset() {
        return boattailOffset;
    }

    public void setBoattailOffset(Double boattailOffset) {
        this.boattailOffset = boattailOffset;
    }

    public Double getOverhang() {
        return overhang;
    }

    public void setOverhang(Double overhang) {
        this.overhang = overhang;
    }

    public FinDTO getFin() {
        return fin;
    }

    public void setFin(FinDTO fin) {
        this.fin = fin;
    }
}
