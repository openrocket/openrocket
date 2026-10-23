package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.SimulationOptions;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Reads the RASAero launch site information, creates a new (empty) simulation
 * in the OpenRocket document and
 * applies the RASAero launch site information to the simulation.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class LaunchSiteHandler extends AbstractElementHandler {
    private final SimulationOptions launchSiteSettings;
    private double barometricPressure = 0;
    private double windSpeed = 0;

    public LaunchSiteHandler(final SimulationOptions launchSiteSettings) {
        this.launchSiteSettings = launchSiteSettings;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.LAUNCH_ALTITUDE.equals(element)
                || RASAeroCommonConstants.LAUNCH_PRESSURE.equals(element)
                || RASAeroCommonConstants.LAUNCH_ROD_ANGLE.equals(element)
                || RASAeroCommonConstants.LAUNCH_ROD_LENGTH.equals(element)
                || RASAeroCommonConstants.LAUNCH_TEMPERATURE.equals(element)
                || RASAeroCommonConstants.LAUNCH_WIND_SPEED.equals(element)) {
            return PlainTextHandler.INSTANCE;
        }
        warnings.add("Unknown element " + element + " for launch site, ignoring.");
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        try {
            if (RASAeroCommonConstants.LAUNCH_ALTITUDE.equals(element)) {
                launchSiteSettings.setLaunchAltitude(
                        BaseHandler.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_PRESSURE.equals(element)) {
                barometricPressure = BaseHandler.parseDouble(content);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_ANGLE.equals(element)) {
                launchSiteSettings.setLaunchRodAngle(
                        BaseHandler.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_LENGTH.equals(element)) {
                launchSiteSettings.setLaunchRodLength(
                        BaseHandler.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_TEMPERATURE.equals(element)) {
                launchSiteSettings.setLaunchTemperature(
                        RASAeroCommonConstants.RASAERO_TO_OPENROCKET_TEMPERATURE(BaseHandler.parseDouble(content)));
            } else if (RASAeroCommonConstants.LAUNCH_WIND_SPEED.equals(element)) {
                windSpeed = BaseHandler.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED;
            }
        } catch (NumberFormatException e) {
            warnings.add("Invalid number format for element " + element + ", ignoring.");
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        // RASAero always uses the entered temperature, and a barometric pressure that is reduced to sea level; when
        // no pressure is entered, it uses the standard pressure at the launch site elevation
        launchSiteSettings.setISAAtmosphere(false);
        launchSiteSettings.setLaunchPressure(RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LAUNCH_PRESSURE(
                barometricPressure,
                launchSiteSettings.getLaunchAltitude() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE));

        // RASAero's wind is constant, and blows towards the launch site (a headwind) when it is positive
        launchSiteSettings.setWindModelType(WindModelType.AVERAGE);
        launchSiteSettings.getAverageWindModel().setAverage(Math.abs(windSpeed));
        launchSiteSettings.setLaunchIntoWind(false);
        double windDirection = launchSiteSettings.getAverageWindModel().getDirection();
        launchSiteSettings.setLaunchRodDirection(windSpeed >= 0 ? windDirection : windDirection + Math.PI);
    }
}
