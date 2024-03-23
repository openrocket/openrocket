package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomBooleanAdapter;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = RASAeroCommonConstants.RECOVERY)
@XmlAccessorType(XmlAccessType.FIELD)
public class RecoveryDTO {
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_ALTITUDE + 1)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double altitude1 = 0d;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_ALTITUDE + 2)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double altitude2 = 0d;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_DEVICE_TYPE + 1)
    private String deviceType1 = "None";
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_DEVICE_TYPE + 2)
    private String deviceType2 = "None";
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_EVENT + 1)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean event1 = false;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_EVENT + 2)
    @XmlJavaTypeAdapter(CustomBooleanAdapter.class)
    private Boolean event2 = false;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_SIZE + 1)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double size1 = 0d;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_SIZE + 2)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double size2 = 0d;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_EVENT_TYPE + 1)
    private String eventType1 = "None";
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_EVENT_TYPE + 2)
    private String eventType2 = "None";
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_CD + 1)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double CD1 = 0d;
    @XmlElement(name = RASAeroCommonConstants.RECOVERY_CD + 2)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double CD2 = 0d;

    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(RecoveryDTO.class);
    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default, no-args constructor.
     */
    public RecoveryDTO() {
    }

    public RecoveryDTO(Rocket rocket, WarningSet warnings, ErrorSet errors) {
        List<Parachute> parachutes = getParachutesFromRocket(rocket);

        switch (parachutes.size()) {
            case 0:
                log.debug("No parachutes present");
                break;
            case 1:
                configureRecoveryDevice1(parachutes.get(0), errors);
                break;
            case 2:
                configureRecoveryDevice1(parachutes.get(0), errors);
                configureRecoveryDevice2(parachutes.get(1), errors);
                break;
        }
    }

    private List<Parachute> getParachutesFromRocket(Rocket rocket) {
        List<Parachute> parachutes = new LinkedList<>();

        for (int i = 0; i < Math.min(rocket.getChildCount(), 3); i++) {
            AxialStage stage = (AxialStage) rocket.getChild(i);

            for (RocketComponent stageChild : stage.getChildren()) {
                if (stageChild instanceof BodyTube) {
                    for (RocketComponent child : stageChild) {
                        if (child instanceof Parachute) {
                            parachutes.add((Parachute) child);
                            if (parachutes.size() == 2) {
                                return parachutes;
                            }
                        }
                    }
                }
            }
        }

        return parachutes;
    }

    private void configureRecoveryDevice1(Parachute device1, ErrorSet errors) {
        setCD1(device1.getCD());
        setDeviceType1("Parachute");
        DeploymentConfiguration deployConfig = device1.getDeploymentConfigurations().getDefault();
        setAltitude1(deployConfig.getDeployAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        if (deployConfig.getDeployEvent() == DeploymentConfiguration.DeployEvent.APOGEE) {
            setEventType1(RASAeroCommonConstants.DEPLOYMENT_APOGEE);
        } else if (deployConfig.getDeployEvent() == DeploymentConfiguration.DeployEvent.ALTITUDE) {
            setEventType1(RASAeroCommonConstants.RECOVERY_ALTITUDE);
        } else {
            errors.add(String.format(trans.get("RASAeroExport.error21"),
                    device1.getName(), deployConfig.getDeployEvent().toString()));
        }
        setEvent1(true);
        setSize1(device1.getDiameter() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
    }

    private void configureRecoveryDevice2(Parachute device2, ErrorSet errors) {
        setCD1(device2.getCD());
        setDeviceType2("Parachute");
        DeploymentConfiguration deployConfig = device2.getDeploymentConfigurations().getDefault();
        setAltitude2(deployConfig.getDeployAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        if (deployConfig.getDeployEvent() == DeploymentConfiguration.DeployEvent.APOGEE) {
            setEventType2(RASAeroCommonConstants.DEPLOYMENT_APOGEE);
        } else if (deployConfig.getDeployEvent() == DeploymentConfiguration.DeployEvent.ALTITUDE) {
            setEventType2(RASAeroCommonConstants.RECOVERY_ALTITUDE);
        } else {
            errors.add(String.format(trans.get("RASAeroExport.error21"),
                    device2.getName(), deployConfig.getDeployEvent().toString()));
        }
        setEvent2(true);
        setSize2(device2.getDiameter() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
    }

    public Double getAltitude1() {
        return altitude1;
    }

    public void setAltitude1(Double altitude1) {
        this.altitude1 = altitude1;
    }

    public Double getAltitude2() {
        return altitude2;
    }

    public void setAltitude2(Double altitude2) {
        this.altitude2 = altitude2;
    }

    public String getDeviceType1() {
        return deviceType1;
    }

    public void setDeviceType1(String deviceType1) {
        this.deviceType1 = deviceType1;
    }

    public String getDeviceType2() {
        return deviceType2;
    }

    public void setDeviceType2(String deviceType2) {
        this.deviceType2 = deviceType2;
    }

    public Boolean getEvent1() {
        return event1;
    }

    public void setEvent1(Boolean event1) {
        this.event1 = event1;
    }

    public Boolean getEvent2() {
        return event2;
    }

    public void setEvent2(Boolean event2) {
        this.event2 = event2;
    }

    public Double getSize1() {
        return size1;
    }

    public void setSize1(Double size1) {
        this.size1 = size1;
    }

    public Double getSize2() {
        return size2;
    }

    public void setSize2(Double size2) {
        this.size2 = size2;
    }

    public String getEventType1() {
        return eventType1;
    }

    public void setEventType1(String eventType1) {
        this.eventType1 = eventType1;
    }

    public String getEventType2() {
        return eventType2;
    }

    public void setEventType2(String eventType2) {
        this.eventType2 = eventType2;
    }

    public Double getCD1() {
        return CD1;
    }

    public void setCD1(Double CD1) {
        this.CD1 = CD1;
    }

    public Double getCD2() {
        return CD2;
    }

    public void setCD2(Double CD2) {
        this.CD2 = CD2;
    }
}
