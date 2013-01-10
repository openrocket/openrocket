package net.sf.openrocket.file.openrocket.importt;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.AbstractRocketLoader;
import net.sf.openrocket.file.MotorFinder;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Class that loads a rocket definition from an OpenRocket rocket file.
 * <p>
 * This class uses SAX to read the XML file format.  The 
 * #loadFromStream(InputStream) method simply sets the system up and 
 * starts the parsing, while the actual logic is in the private inner class
 * <code>OpenRocketHandler</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocketLoader extends AbstractRocketLoader {
	private static final LogHelper log = Application.getLogger();
	
	
	@Override
	public void loadFromStream(OpenRocketDocument doc, InputStream source, MotorFinder motorFinder) throws RocketLoadException,
			IOException {
		log.info("Loading .ork file");
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setMotorFinder(motorFinder);
		context.setOpenRocketDocument(doc);
		
		InputSource xmlSource = new InputSource(source);
		OpenRocketHandler handler = new OpenRocketHandler(context);
		
		
		try {
			SimpleSAX.readXML(xmlSource, handler, warnings);
		} catch (SAXException e) {
			log.warn("Malformed XML in input");
			throw new RocketLoadException("Malformed XML in input.", e);
		}
		
		doc.getDefaultConfiguration().setAllStages();
		
		// Deduce suitable time skip
		double timeSkip = StorageOptions.SIMULATION_DATA_NONE;
		for (Simulation s : doc.getSimulations()) {
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
			
			double previousTime = Double.NaN;
			for (double time : list) {
				if (time - previousTime < timeSkip)
					timeSkip = time - previousTime;
				previousTime = time;
			}
		}
		// Round value
		timeSkip = Math.rint(timeSkip * 100) / 100;
		
		doc.getDefaultStorageOptions().setSimulationTimeSkip(timeSkip);
		doc.getDefaultStorageOptions().setExplicitlySet(false);
		
		doc.clearUndo();
		log.info("Loading done");
	}
	
}
