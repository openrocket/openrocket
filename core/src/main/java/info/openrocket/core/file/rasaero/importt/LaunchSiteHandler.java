package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
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
                        Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_PRESSURE.equals(element)) {
                launchSiteSettings.setLaunchPressure(
                        Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_PRESSURE);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_ANGLE.equals(element)) {
                launchSiteSettings.setLaunchRodAngle(
                        Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ANGLE);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_LENGTH.equals(element)) {
                launchSiteSettings.setLaunchRodLength(
                        Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_TEMPERATURE.equals(element)) {
                launchSiteSettings.setLaunchTemperature(
                        RASAeroCommonConstants.RASAERO_TO_OPENROCKET_TEMPERATURE(Double.parseDouble(content)));
            } else if (RASAeroCommonConstants.LAUNCH_WIND_SPEED.equals(element)) {
                launchSiteSettings.setWindSpeedAverage(
                        Double.parseDouble(content) / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED);
            }
        } catch (NumberFormatException e) {
            warnings.add("Invalid number format for element " + element + ", ignoring.");
        }
    }
}
