package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.simulation.SimulationOptions;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Reads the RASAero launch site information, creates a new (empty) simulation in the OpenRocket document and
 * applies the RASAero launch site information to the simulation.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class LaunchSiteHandler extends AbstractElementHandler {
    private final DocumentLoadingContext context;
    private final SimulationOptions simulationOptions;

    public LaunchSiteHandler(DocumentLoadingContext context) {
        this.context = context;
        Simulation simulation = new Simulation(context.getOpenRocketDocument().getRocket());
        simulation.setName("RASAero II Launch Site");
        this.simulationOptions = simulation.getOptions();
        this.context.getOpenRocketDocument().addSimulation(simulation);
    }
    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (RASAeroCommonConstants.LAUNCH_ALTITUDE.equals(element) || RASAeroCommonConstants.LAUNCH_PRESSURE.equals(element)
                || RASAeroCommonConstants.LAUNCH_ROD_ANGLE.equals(element) || RASAeroCommonConstants.LAUNCH_ROD_LENGTH.equals(element)
                || RASAeroCommonConstants.LAUNCH_TEMPERATURE.equals(element) || RASAeroCommonConstants.LAUNCH_WIND_SPEED.equals(element)) {
            return PlainTextHandler.INSTANCE;
        }
        warnings.add("Unknown element " + element + ", ignoring.");
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        try {
            if (RASAeroCommonConstants.LAUNCH_ALTITUDE.equals(element)) {
                simulationOptions.setLaunchAltitude(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_PRESSURE.equals(element)) {
                simulationOptions.setLaunchPressure(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_PRESSURE);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_ANGLE.equals(element)) {
                simulationOptions.setLaunchRodAngle(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_ANGLE);
            } else if (RASAeroCommonConstants.LAUNCH_ROD_LENGTH.equals(element)) {
                simulationOptions.setLaunchRodLength(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_ALTITUDE);
            } else if (RASAeroCommonConstants.LAUNCH_TEMPERATURE.equals(element)) {
                simulationOptions.setLaunchTemperature(
                        RASAeroCommonConstants.RASAERO_TO_OPENROCKET_TEMPERATURE(Double.parseDouble(content)));
            } else if (RASAeroCommonConstants.LAUNCH_WIND_SPEED.equals(element)) {
                simulationOptions.setWindSpeedAverage(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_SPEED);
            }
        } catch (NumberFormatException e) {
            warnings.add("Invalid number format for element " + element + ", ignoring.");
        }
    }
}
