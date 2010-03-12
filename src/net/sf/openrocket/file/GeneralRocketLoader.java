package net.sf.openrocket.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.Arrays;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.openrocket.OpenRocketLoader;
import net.sf.openrocket.file.rocksim.RocksimLoader;


/**
 * A rocket loader that auto-detects the document type and uses the appropriate
 * loading.  Supports loading of GZIPed files as well with transparent
 * uncompression.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralRocketLoader extends RocketLoader {

	private static final int READ_BYTES = 300;
	
	private static final byte[] GZIP_SIGNATURE = { 31, -117 };  // 0x1f, 0x8b
	private static final byte[] OPENROCKET_SIGNATURE = 
		"<openrocket".getBytes(Charset.forName("US-ASCII"));
    private static final byte[] ROCKSIM_SIGNATURE = 
        "<RockSimDoc".getBytes(Charset.forName("US-ASCII"));
	
	private final OpenRocketLoader openRocketLoader = new OpenRocketLoader();
    
    private final RocksimLoader rocksimLoader = new RocksimLoader();
	
	@Override
	protected OpenRocketDocument loadFromStream(InputStream source) throws IOException,
			RocketLoadException {

		// Check for mark() support
		if (!source.markSupported()) {
			source = new BufferedInputStream(source);
		}
		
		// Read using mark()
		byte[] buffer = new byte[READ_BYTES];
		int count;
		source.mark(READ_BYTES + 10);
		count = source.read(buffer);
		source.reset();
		
		if (count < 10) {
			throw new RocketLoadException("Unsupported or corrupt file.");
		}
		
		// Detect the appropriate loader

		// Check for GZIP
		if (buffer[0] == GZIP_SIGNATURE[0]  &&  buffer[1] == GZIP_SIGNATURE[1]) {
			OpenRocketDocument doc = loadFromStream(new GZIPInputStream(source));
			doc.getDefaultStorageOptions().setCompressionEnabled(true);
			return doc;
		}
		
		// Check for OpenRocket
		int match = 0;
		for (int i=0; i < count; i++) {
			if (buffer[i] == OPENROCKET_SIGNATURE[match]) {
				match++;
				if (match == OPENROCKET_SIGNATURE.length) {
					return loadUsing(source, openRocketLoader);
				}
			} else {
				match = 0;
			}
		}

        byte[] typeIdentifier = Arrays.copyOf(buffer, ROCKSIM_SIGNATURE.length);
        if (Arrays.equals(ROCKSIM_SIGNATURE, typeIdentifier)) {
            return loadUsing(source, rocksimLoader);            
        }
		throw new RocketLoadException("Unsupported or corrupt file.");
	}
	
	private OpenRocketDocument loadUsing(InputStream source, RocketLoader loader) 
	throws RocketLoadException {
		warnings.clear();
		OpenRocketDocument doc = loader.load(source);
		warnings.addAll(loader.getWarnings());
		return doc;
	}
}
