package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.LAUNCH_SITE)
public class LaunchSiteDTO {

    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_ALTITUDE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double altitude = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_PRESSURE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double pressure = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_ROD_ANGLE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double rodAngle = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_ROD_LENGTH)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double rodLength = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_TEMPERATURE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double temperature = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LAUNCH_WIND_SPEED)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double windSpeed = 0.0d;

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
            setWindSpeed(options.getAverageWindModel().getAverage() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
            return;
        }

        // If we can't get settings from the sims, use the launch site settings from the preferences
        ApplicationPreferences prefs = Application.getPreferences();
        setAltitude(prefs.getLaunchAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        setPressure(prefs.getLaunchPressure() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_PRESSURE);
        setTemperature(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(prefs.getLaunchTemperature()));
        setRodAngle(prefs.getLaunchRodAngle() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
        setRodLength(prefs.getLaunchRodLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);     // It's a length, but stored in RASAero in feet instead of inches
        setWindSpeed(prefs.getAverageWindModel().getAverage() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
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
