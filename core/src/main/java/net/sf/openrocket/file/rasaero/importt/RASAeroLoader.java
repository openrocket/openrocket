package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.AbstractRocketLoader;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is the main entry point for RASAero II design file imported to OpenRocket.
 * <p>
 * RASAero simulations are not imported.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RASAeroLoader extends AbstractRocketLoader {
    /**
     * This method is called by the default implementations of {@link #load(java.io.File)}
     * and {@link #load(java.io.InputStream)} to load the rocket.
     *
     * @throws net.sf.openrocket.file.RocketLoadException
     *          if an error occurs during loading.
     */
    @Override
    public void loadFromStream(DocumentLoadingContext context, InputStream source, String fileName) throws IOException, RocketLoadException {
        InputSource xmlSource = new InputSource(source);

        RASAeroHandler handler = new RASAeroHandler(context, fileName);

        try {
            SimpleSAX.readXML(xmlSource, handler, warnings);
        } catch (SAXException e) {
            throw new RocketLoadException("Malformed XML in input.", e);
        }

        context.getOpenRocketDocument().setFile(null);
        context.getOpenRocketDocument().clearUndo();
        context.getOpenRocketDocument().getDefaultStorageOptions().setFileType(StorageOptions.FileType.RASAERO);
    }
}
