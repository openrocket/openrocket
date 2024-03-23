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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;

@XmlAccessorType(XmlAccessType.FIELD)
public class RocketDesignDTO {
    @XmlElementRefs({
            @XmlElementRef(name = RASAeroCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RASAeroCommonConstants.NOSE_CONE, type = NoseConeDTO.class),
            @XmlElementRef(name = RASAeroCommonConstants.TRANSITION, type = TransitionDTO.class),
            @XmlElementRef(name = RASAeroCommonConstants.BOOSTER, type = BoosterDTO.class)
    })
    private final List<BasePartDTO> externalPart = new ArrayList<>();

    @XmlElementRefs({
            @XmlElementRef(name = RASAeroCommonConstants.BOOSTER, type = BoosterDTO.class),
    })
    private final List<BoosterDTO> boosters = new ArrayList<>();

    @XmlElement(name = RASAeroCommonConstants.SURFACE_FINISH)
    private String surface = RASAeroCommonConstants.FINISH_SMOOTH;
    @XmlElement(name = RASAeroCommonConstants.CD)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double CD = 0d;
    @XmlElement(name = RASAeroCommonConstants.MODIFIED_BARROWMAN)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean modifiedBarrowman = false;
    @XmlElement(name = RASAeroCommonConstants.TURBULENCE)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean turbulence = false;
    @XmlElement(name = RASAeroCommonConstants.SUSTAINER_NOZZLE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sustainerNozzle = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER1_NOZZLE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster1Nozzle = 0d;
    @XmlElement(name = RASAeroCommonConstants.BOOSTER2_NOZZLE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double booster2Nozzle = 0d;
    @XmlElement(name = RASAeroCommonConstants.USE_BOOSTER1)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean useBooster1 = false;
    @XmlElement(name = RASAeroCommonConstants.USE_BOOSTER2)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean useBooster2 = false;
    @XmlElement(name = RASAeroCommonConstants.COMMENTS)
    private String comments = "";

    @XmlTransient
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
