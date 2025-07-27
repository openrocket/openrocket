/*
 * RockSimLoader.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.file.AbstractRocketLoader;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.simplesax.SimpleSAX;

/**
 * This class is the main entry point for Rocksim design file imported to
 * OpenRocket. Currently only Rocksim v9
 * file formats are supported, although it is possible that v8 formats will work
 * for most components.
 * 
 * In the cases of v9 components that exist in Rocksim but have no corollary in
 * OpenRocket a message is added to
 * a warning set and presented to the user. In effect, this loading is a
 * 'best-effort' mapping and is not meant to
 * be an exact representation of any possible Rocksim design in an OpenRocket
 * format.
 * 
 * Rocksim simulations are not imported.
 * 
 * Wish List:
 * Material interface (or at least make them abstract in RocketComponent)
 * setMaterial
 * getMaterial
 */
public class RockSimLoader extends AbstractRocketLoader {
	/**
	 * This method is called by the default implementations of
	 * {@link #load(java.io.File)}
	 * and {@link #load(java.io.InputStream)} to load the rocket.
	 *
	 * @throws info.openrocket.core.file.RocketLoadException
	 *                                                       if an error occurs
	 *                                                       during loading.
	 */
	@Override
	public void loadFromStream(DocumentLoadingContext context, InputStream source, String fileName)
			throws IOException, RocketLoadException {

		InputSource xmlSource = new InputSource(source);

		RockSimHandler handler = new RockSimHandler(context);

		try {
			SimpleSAX.readXML(xmlSource, handler, warnings);
		} catch (SAXException e) {
			throw new RocketLoadException("Malformed XML in input.", e);
		}

		context.getOpenRocketDocument().setFile(null);
		context.getOpenRocketDocument().clearUndo();
		context.getOpenRocketDocument().getDefaultStorageOptions().setFileType(FileType.ROCKSIM);
	}
}
