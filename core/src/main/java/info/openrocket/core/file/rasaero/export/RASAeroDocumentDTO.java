package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * The top level RASAero document.
 */
@JacksonXmlRootElement(localName = RASAeroCommonConstants.RASAERO_DOCUMENT)
public class RASAeroDocumentDTO {
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FILE_VERSION)
    private final String version = "2";

    @JacksonXmlProperty(localName = RASAeroCommonConstants.ROCKET_DESIGN)
    private RocketDesignDTO design;

    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_SITE)
    private LaunchSiteDTO launchSite;

    @JacksonXmlProperty(localName = RASAeroCommonConstants.RECOVERY)
    private RecoveryDTO recovery;

    @JacksonXmlProperty(localName = RASAeroCommonConstants.MACH_ALT)
    private String machAlt = ""; // Currently not implemented

    @JacksonXmlProperty(localName = RASAeroCommonConstants.SIMULATION_LIST)
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
