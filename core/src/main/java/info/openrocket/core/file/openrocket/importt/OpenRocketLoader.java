package info.openrocket.core.file.openrocket.importt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.file.AbstractRocketLoader;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.simplesax.SimpleSAX;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.extension.SimulationExtension;

/**
 * Class that loads a rocket definition from an OpenRocket rocket file.
 * <p>
 * This class uses SAX to read the XML file format. The
 * #loadFromStream(InputStream) method simply sets the system up and
 * starts the parsing, while the actual logic is in the private inner class
 * <code>OpenRocketHandler</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocketLoader extends AbstractRocketLoader {
	private static final Logger log = LoggerFactory.getLogger(OpenRocketLoader.class);

	@Override
	public void loadFromStream(DocumentLoadingContext context, InputStream source, String fileName)
			throws RocketLoadException,
			IOException {
		log.info("Loading .ork file");

		InputSource xmlSource = new InputSource(source);
		OpenRocketHandler handler = new OpenRocketHandler(context);

		OpenRocketDocument doc = context.getOpenRocketDocument();

		try {
			SimpleSAX.readXML(xmlSource, handler, warnings);
		} catch (SAXException e) {
			log.warn("Malformed XML in input");
			throw new RocketLoadException("Malformed XML in input.", e);
		}

		// load the stage activeness
		for (FlightConfiguration config : doc.getRocket().getFlightConfigurations()) {
			config.applyPreloadedStageActiveness();
		}

		// If we saved data for a simulation before, we'll use that as our default
		// option this time
		// Also, updaet all the sims' modIDs to agree with flight config
		for (Simulation s : doc.getSimulations()) {
			s.syncModID(); // The config's modID can be out of sync with the simulation's after the whole
							// loading process
			if (s.getStatus() == Simulation.Status.EXTERNAL ||
					s.getStatus() == Simulation.Status.NOT_SIMULATED)
				continue;
			if (s.getSimulatedData() == null)
				continue;
			if (s.getSimulatedData().getBranchCount() == 0)
				continue;
			FlightDataBranch branch = s.getSimulatedData().getBranch(0);
			if (branch == null)
				continue;
			List<Double> list = branch.get(FlightDataType.TYPE_TIME);
			if (list == null)
				continue;

			doc.getDefaultStorageOptions().setSaveSimulationData(true);
		}

		doc.getDefaultStorageOptions().setExplicitlySet(false);
		doc.getDefaultStorageOptions().setFileType(FileType.OPENROCKET);

		// Call simulation extensions
		for (Simulation sim : doc.getSimulations()) {
			for (SimulationExtension ext : sim.getSimulationExtensions()) {
				ext.documentLoaded(doc, sim, warnings);
			}
		}

		doc.clearUndo();
		log.info("Loading done");
	}

}
