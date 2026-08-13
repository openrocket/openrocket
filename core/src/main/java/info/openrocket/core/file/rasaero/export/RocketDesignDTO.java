package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomBooleanAdapter;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;

@SuppressWarnings("unused")
public class RocketDesignDTO {
    // A single ordered list of external parts preserves the original component order
    // (e.g. NoseCone → BodyTube → Transition → BodyTube). This matters because the
    // importer constructs components sequentially, so grouping them by type would
    // change the resulting geometry. PartListSerializer derives each element's XML name
    // from its runtime type, mirroring the former JAXB @XmlElementRefs behaviour.
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonSerialize(contentUsing = PartListSerializer.class)
    private final List<BasePartDTO> externalPart = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOOSTER)
    private final List<BoosterDTO> boosters = new ArrayList<>();

    @JacksonXmlProperty(localName = RASAeroCommonConstants.SURFACE_FINISH)
    private String surface = RASAeroCommonConstants.FINISH_SMOOTH;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.CD)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double CD = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.MODIFIED_BARROWMAN)
    @JsonSerialize(using = CustomBooleanAdapter.Serializer.class)
    private Boolean modifiedBarrowman = false;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.TURBULENCE)
    @JsonSerialize(using = CustomBooleanAdapter.Serializer.class)
    private Boolean turbulence = false;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.SUSTAINER_NOZZLE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double sustainerNozzle = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOOSTER1_NOZZLE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double booster1Nozzle = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BOOSTER2_NOZZLE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double booster2Nozzle = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.USE_BOOSTER1)
    @JsonSerialize(using = CustomBooleanAdapter.Serializer.class)
    private Boolean useBooster1 = false;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.USE_BOOSTER2)
    @JsonSerialize(using = CustomBooleanAdapter.Serializer.class)
    private Boolean useBooster2 = false;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.COMMENTS)
    private String comments = "";

    @JsonIgnore
    private static final Translator trans = Application.getTranslator();

    public RocketDesignDTO(Rocket rocket, WarningSet warnings, ErrorSet errors) {
        setComments(rocket.getComment());
        if (rocket.getChildCount() > 3) {
            warnings.add(trans.get("RASAeroExport.warning10"));
        }
        setUseBooster1(rocket.getChildCount() >= 2);
        setUseBooster2(rocket.getChildCount() == 3);

        AxialStage sustainer = rocket.getStage(0);

        // Export components from sustainer
        for (int i = 0; i < sustainer.getChildCount(); i++) {
            try {
                RocketComponent component = sustainer.getChild(i);
                if (i == 0 && !(component instanceof NoseCone)) {
                    errors.add(trans.get("RASAeroExport.error22"));
                    return;
                } else if (i == 1 && !(component instanceof BodyTube ||
                        (component instanceof Transition && !(component instanceof NoseCone)
                                && (i == sustainer.getChildCount() - 1)))) {
                    errors.add(trans.get("RASAeroExport.error23"));
                    return;
                }
                if (component instanceof BodyTube) {
                    addExternalPart(new BodyTubeDTO((BodyTube) component, warnings, errors));
                } else if (component instanceof NoseCone) {
                    if (i != 0) {
                        errors.add(trans.get("RASAeroExport.error24"));
                        return;
                    }
                    addExternalPart(new NoseConeDTO((NoseCone) component, warnings, errors));
                    // Set the global surface finish to that of the first nose cone
                    setSurface(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SURFACE(((NoseCone) component).getFinish(),
                            warnings));
                } else if (component instanceof Transition) {
                    // If there is only a sustainer & this is the last child of the sustainer, it's
                    // a boattail
                    if (rocket.getChildCount() == 1 && (i == sustainer.getChildCount() - 1)) {
                        addExternalPart(new BoattailDTO((Transition) component, warnings, errors));
                    } else {
                        addExternalPart(new TransitionDTO((Transition) component, warnings, errors));
                    }
                } else {
                    throw new RASAeroExportException(
                            String.format(trans.get("RASAeroExport.error33"), component.getComponentName()));
                }
            } catch (RASAeroExportException e) {
                errors.add(e.getMessage());
            }
        }

        // Export components from other stages
        for (int i = 1; i < Math.min(rocket.getChildCount(), 3); i++) {
            try {
                addBooster(new BoosterDTO(rocket, (AxialStage) rocket.getChild(i), warnings, errors));
            } catch (RASAeroExportException e) {
                errors.add(e.getMessage());
            }
        }
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public double getCD() {
        return CD;
    }

    public void setCD(double CD) {
        this.CD = CD;
    }

    public boolean isModifiedBarrowman() {
        return modifiedBarrowman;
    }

    public void setModifiedBarrowman(boolean modifiedBarrowman) {
        this.modifiedBarrowman = modifiedBarrowman;
    }

    public Boolean isTurbulence() {
        return turbulence;
    }

    public void setTurbulence(Boolean turbulence) {
        this.turbulence = turbulence;
    }

    public Double getSustainerNozzle() {
        return sustainerNozzle;
    }

    public void setSustainerNozzle(Double sustainerNozzle) {
        this.sustainerNozzle = sustainerNozzle;
    }

    public Double getBooster1Nozzle() {
        return booster1Nozzle;
    }

    public void setBooster1Nozzle(Double booster1Nozzle) {
        this.booster1Nozzle = booster1Nozzle;
    }

    public Double getBooster2Nozzle() {
        return booster2Nozzle;
    }

    public void setBooster2Nozzle(Double booster2Nozzle) {
        this.booster2Nozzle = booster2Nozzle;
    }

    public Boolean isUseBooster1() {
        return useBooster1;
    }

    public void setUseBooster1(Boolean useBooster1) {
        this.useBooster1 = useBooster1;
    }

    public Boolean isUseBooster2() {
        return useBooster2;
    }

    public void setUseBooster2(Boolean useBooster2) {
        this.useBooster2 = useBooster2;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<BasePartDTO> getExternalPart() {
        return externalPart;
    }

    public void addExternalPart(BasePartDTO theExternalPartDTO) {
        externalPart.add(theExternalPartDTO);
    }

    public List<BoosterDTO> getBoosters() {
        return boosters;
    }

    public void addBooster(BoosterDTO boosterDTO) {
        boosters.add(boosterDTO);
    }
}
