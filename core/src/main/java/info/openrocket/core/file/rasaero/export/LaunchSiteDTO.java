package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.models.atmosphere.AtmosphericModel;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.MathUtil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = RASAeroCommonConstants.LAUNCH_SITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class LaunchSiteDTO {

    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ALTITUDE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double altitude = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_PRESSURE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double pressure = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ROD_ANGLE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rodAngle = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_ROD_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double rodLength = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_TEMPERATURE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double temperature = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_WIND_SPEED)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double windSpeed = 0.0d;

    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default, no-args constructor.
     */
    public LaunchSiteDTO() {
    }

    public LaunchSiteDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        // RASAero has a single launch site for all simulations
        LaunchSiteDTO firstSite = null;
        for (Simulation sim : document.getSimulations()) {
            if (firstSite == null) {
                setLaunchSite(sim, warnings);
                firstSite = this;
            } else {
                LaunchSiteDTO site = new LaunchSiteDTO();
                site.setLaunchSite(sim, new WarningSet());
                if (!site.isSameLaunchSite(firstSite)) {
                    warnings.add(String.format(trans.get("RASAeroExport.warning21"),
                            document.getSimulation(0).getName()));
                    break;
                }
            }
        }
        if (firstSite != null) {
            return;
        }

        // If we can't get settings from the sims, use the launch site settings from the preferences
        ApplicationPreferences prefs = Application.getPreferences();
        setAltitude(prefs.getLaunchAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        setPressure(prefs.isISAAtmosphere() ? 0 : RASAeroCommonConstants.OPENROCKET_TO_RASAERO_BAROMETRIC_PRESSURE(
                prefs.getLaunchPressure(), getAltitude()));
        setTemperature(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(prefs.getLaunchTemperature()));
        setRodAngle(prefs.getLaunchRodAngle() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
        setRodLength(prefs.getLaunchRodLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);     // It's a length, but stored in RASAero in feet instead of inches
        setWindSpeed(prefs.getAverageWindModel().getAverage() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
    }

    /**
     * Sets the launch site from the simulation's options, using the atmospheric conditions that OpenRocket simulates.
     */
    private void setLaunchSite(Simulation simulation, WarningSet warnings) {
        SimulationOptions options = simulation.getOptions();
        double altitude = options.getLaunchAltitude();
        AtmosphericModel atmosphere = options.isISAAtmosphere() ? new ExtendedISAModel() :
                new ExtendedISAModel(altitude, options.getLaunchTemperature(), options.getLaunchPressure(),
                        options.getLaunchRelativeHumidity());
        AtmosphericConditions conditions = atmosphere.getConditions(altitude);

        setAltitude(altitude * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
        setTemperature(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_TEMPERATURE(conditions.getTemperature()));
        // RASAero uses the standard pressure at the launch site when no barometric pressure is entered
        setPressure(options.isISAAtmosphere() ? 0 : RASAeroCommonConstants.OPENROCKET_TO_RASAERO_BAROMETRIC_PRESSURE(
                conditions.getPressure(), getAltitude()));
        setRodAngle(options.getLaunchRodAngle() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
        setRodLength(options.getLaunchRodLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);     // It's a length, but stored in RASAero in feet instead of inches

        // RASAero has a single constant wind, with a positive speed for a headwind
        double windSpeed;
        double windDirection;
        if (options.getWindModelType() == WindModelType.MULTI_LEVEL
                && !options.getMultiLevelWindModel().getLevels().isEmpty()) {
            LevelWindModel level = options.getMultiLevelWindModel().getLevels().get(0);
            windSpeed = level.getSpeed();
            windDirection = level.getDirection();
            warnings.add(String.format(trans.get("RASAeroExport.warning22"), simulation.getName()));
        } else {
            windSpeed = options.getAverageWindModel().getAverage();
            windDirection = options.getAverageWindModel().getDirection();
        }
        double headwind = windSpeed * Math.cos(windDirection - options.getLaunchRodDirection());
        setWindSpeed(headwind * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
    }

    private boolean isSameLaunchSite(LaunchSiteDTO other) {
        return MathUtil.equals(altitude, other.altitude) && MathUtil.equals(pressure, other.pressure)
                && MathUtil.equals(temperature, other.temperature) && MathUtil.equals(rodAngle, other.rodAngle)
                && MathUtil.equals(rodLength, other.rodLength) && MathUtil.equals(windSpeed, other.windSpeed);
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
