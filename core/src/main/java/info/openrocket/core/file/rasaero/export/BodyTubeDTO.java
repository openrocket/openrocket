package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

@XmlRootElement(name = RASAeroCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "partType",
        "length",
        "diameter",
        "launchLugDiameter",
        "launchLugLength",
        "railGuideDiameter",
        "railGuideHeight",
        "launchShoeArea",
        "location",
        "color",
        "boattailLength",
        "boattailRearDiameter",
        "boattailOffset",
        "overhang",
        "fin"
})
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
    private Double launchShoeArea = 0d; // Currently not available in OR

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

    @XmlTransient
    private final WarningSet warnings;
    @XmlTransient
    private final ErrorSet errors;
    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default no-args constructor.
     */
    public BodyTubeDTO() {
        this.warnings = null;
        this.errors = null;
    }

    public BodyTubeDTO(BodyTube bodyTube, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(bodyTube, warnings, errors);
        this.warnings = warnings;
        this.errors = errors;
        applyBodyTubeSettings(bodyTube, warnings, errors);
    }

    public Double getLaunchLugDiameter() {
        return launchLugDiameter;
    }

    public void setLaunchLugDiameter(Double launchLugDiameter) throws RASAeroExportException {
        if (MathUtil.equals(launchLugDiameter, 0)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error4"));
        }
        this.launchLugDiameter = launchLugDiameter;
    }

    public Double getLaunchLugLength() {
        return launchLugLength;
    }

    public void setLaunchLugLength(Double launchLugLength) throws RASAeroExportException {
        if (MathUtil.equals(launchLugLength, 0)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error5"));
        }
        this.launchLugLength = launchLugLength;
    }

    public Double getRailGuideDiameter() {
        return railGuideDiameter;
    }

    public void setRailGuideDiameter(Double railGuideDiameter) throws RASAeroExportException {
        if (MathUtil.equals(railGuideDiameter, 0)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error6"));
        }
        this.railGuideDiameter = railGuideDiameter;
    }

    public Double getRailGuideHeight() {
        return railGuideHeight;
    }

    public void setRailGuideHeight(Double railGuideHeight) throws RASAeroExportException {
        if (MathUtil.equals(railGuideHeight, 0)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error7"));
        }
        this.railGuideHeight = railGuideHeight;
    }

    public Double getLaunchShoeArea() {
        return launchShoeArea;
    }

    public void setLaunchShoeArea(Double launchShoeArea) throws RASAeroExportException {
        if (MathUtil.equals(launchShoeArea, 0)) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error8"));
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
