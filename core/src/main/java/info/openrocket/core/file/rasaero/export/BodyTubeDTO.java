package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.BODY_TUBE)
public class BodyTubeDTO extends BasePartDTO implements BodyTubeDTOAdapter {
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_LUG_DIAMETER)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double launchLugDiameter = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_LUG_LENGTH)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double launchLugLength = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.RAIL_GUIDE_DIAMETER)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double railGuideDiameter = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.RAIL_GUIDE_HEIGHT)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double railGuideHeight = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_SHOE_AREA)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double launchShoeArea = 0.0d; // Currently not available in OR

    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOATTAIL_LENGTH)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double boattailLength = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOATTAIL_REAR_DIAMETER)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double boattailRearDiameter = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOATTAIL_OFFSET)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double boattailOffset = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.OVERHANG)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double overhang = 0.0d;

    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN)
    private FinDTO fin;

    @JsonIgnore
    private final WarningSet warnings;
    @JsonIgnore
    private final ErrorSet errors;
    @JsonIgnore
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
