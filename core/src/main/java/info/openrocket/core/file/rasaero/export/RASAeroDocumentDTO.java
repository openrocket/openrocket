package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The top level RASAero document.
 */
@XmlRootElement(name = RASAeroCommonConstants.RASAERO_DOCUMENT)
@XmlAccessorType(XmlAccessType.FIELD)
public class RASAeroDocumentDTO {
    @XmlElement(name = RASAeroCommonConstants.FILE_VERSION)
    private final String version = "2";

    @XmlElement(name = RASAeroCommonConstants.ROCKET_DESIGN)
    private RocketDesignDTO design;

    @XmlElement(name = RASAeroCommonConstants.LAUNCH_SITE)
    private LaunchSiteDTO launchSite;

    @XmlElement(name = RASAeroCommonConstants.RECOVERY)
    private RecoveryDTO recovery;

    @XmlElement(name = RASAeroCommonConstants.MACH_ALT)
    private String machAlt = ""; // Currently not implemented

    @XmlElement(name = RASAeroCommonConstants.SIMULATION_LIST)
    private SimulationListDTO simulationList = null;

    /**
     * Get the subordinate design DTO.
     *
     * @return the RocketDesignDTO
     */
    public RocketDesignDTO getDesign() {
        return design;
    }

    public void setDesign(RocketDesignDTO theDesign) {
        this.design = theDesign;
    }

    public LaunchSiteDTO getLaunchSite() {
        return launchSite;
    }

    public void setLaunchSite(LaunchSiteDTO launchSite) {
        this.launchSite = launchSite;
    }

    public RecoveryDTO getRecovery() {
        return recovery;
    }

    public void setRecovery(RecoveryDTO recovery) {
        this.recovery = recovery;
    }

    public SimulationListDTO getSimulationList() {
        return simulationList;
    }

    public void setSimulationList(SimulationListDTO simulationList) {
        this.simulationList = simulationList;
    }

    public String getMachAlt() {
        return this.machAlt;
    }

    public void setMachAlt(String machAlt) {
        this.machAlt = machAlt;
    }

    public String getVersion() {
        return version;
    }
}
