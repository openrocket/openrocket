package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = RASAeroCommonConstants.LAUNCH_SITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchSiteDTO {

    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ALTITUDE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double altitude = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_PRESSURE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double pressure = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ROD_ANGLE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rodAngle = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ROD_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rodLength = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_TEMPERATURE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double temperature = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_WIND_SPEED)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double windSpeed = 0d;

    /**
     * We need a default, no-args constructor.
     */
    public LaunchSiteDTO() {
    }

    public LaunchSiteDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        for (Simulation sim : document.getSimulations()) {
            SimulationOptions options = sim.getSimulatedConditions();
            if (options == null) {
                continue;
            }

            setAltitude(options.getLaunchAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
            setPressure(options.getLaunchPressure() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_PRESSURE);
            setTemperature(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(options.getLaunchTemperature()));
            setRodAngle(options.getLaunchRodAngle() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
            setRodLength(options.getLaunchRodLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);     // It's a length, but stored in RASAero in feet instead of inches
            setWindSpeed(options.getWindSpeedAverage() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
            return;
        }

        // If we can't get settings from the sims, use the launch site settings from the preferences
        Preferences prefs = Application.getPreferences();
        setAltitude(prefs.getLaunchAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        setPressure(prefs.getLaunchPressure() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_PRESSURE);
        setTemperature(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(prefs.getLaunchTemperature()));
        setRodAngle(prefs.getLaunchRodAngle() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
        setRodLength(prefs.getLaunchRodLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);     // It's a length, but stored in RASAero in feet instead of inches
        setWindSpeed(prefs.getWindSpeedAverage() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getRodAngle() {
        return rodAngle;
    }

    public void setRodAngle(Double rodAngle) {
        this.rodAngle = rodAngle;
    }

    public Double getRodLength() {
        return rodLength;
    }

    public void setRodLength(Double rodLength) {
        this.rodLength = rodLength;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }
}
