package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.file.rasaero.RASAeroMotorsLoader;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.SimulationOptions;
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
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (RASAeroCommonConstants.SIMULATION.equals(element)) {
            nrOfSimulations++;
            return new SimulationHandler(context, rocket, launchSiteSettings, nrOfSimulations);
        }
        return null;
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        RASAeroMotorsLoader.clearAllMotors();
    }
}
