package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.SimulationOptions;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for simulation importing from a RASAero file.
 * A SimulationList is a collection of RASAero simulations.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SimulationListHandler extends AbstractElementHandler {
    private final DocumentLoadingContext context;
    private final Rocket rocket;
    private final SimulationOptions launchSiteSettings;
    private int nrOfSimulations = 0;

    public SimulationListHandler(DocumentLoadingContext context, Rocket rocket, SimulationOptions launchSiteSettings) {
        this.context = context;
        this.rocket = rocket;
        this.launchSiteSettings = launchSiteSettings;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        if (RASAeroCommonConstants.SIMULATION.equals(element)) {
            nrOfSimulations++;
            return new SimulationHandler(context, rocket, launchSiteSettings, nrOfSimulations);
        }
        return null;
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        RASAeroMotorsLoader.clearAllMotors();
    }
}
