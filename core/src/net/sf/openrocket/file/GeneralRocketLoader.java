package net.sf.openrocket.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.openrocket.importt.OpenRocketLoader;
import net.sf.openrocket.file.rocksim.importt.RocksimLoader;
import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.util.TextUtil;


/**
 * A rocket loader that auto-detects the document type and uses the appropriate
 * loading.  Supports loading of GZIPed files as well with transparent
 * uncompression.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralRocketLoader {
	
	protected final WarningSet warnings = new WarningSet();
	
	private static final int READ_BYTES = 300;
	
	private static final byte[] GZIP_SIGNATURE = { 31, -117 }; // 0x1f, 0x8b
	private static final byte[] ZIP_SIGNATURE = TextUtil.asciiBytes("PK");
	private static final byte[] OPENROCKET_SIGNATURE = TextUtil.asciiBytes("<openrocket");
	private static final byte[] ROCKSIM_SIGNATURE = TextUtil.asciiBytes("<RockSimDoc");
	
	private final OpenRocketLoader openRocketLoader = new OpenRocketLoader();
	
	private final RocksimLoader rocksimLoader = new RocksimLoader();
	
	private File baseFile;
	private URL jarURL;
	
	private final MotorFinder motorFinder;
	
	public GeneralRocketLoader(File file) {
		this.baseFile = file;
		this.jarURL = null;
		this.motorFinder = new DatabaseMotorFinder();
	}
	
	public GeneralRocketLoader(URL jarURL) {
		this.baseFile = null;
		this.jarURL = jarURL;
		this.motorFinder = new DatabaseMotorFinder();
	}
	
	/**
	 * Loads a rocket from the File object used in the constructor
	 */
	public final OpenRocketDocument load() throws RocketLoadException {
		warnings.clear();
		InputStream stream = null;
		
		try {
			
			stream = new BufferedInputStream(new FileInputStream(baseFile));
			OpenRocketDocument doc = load(stream);
			return doc;
			
		} catch (Exception e) {
			throw new RocketLoadException("Exception loading file: " + baseFile, e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public final OpenRocketDocument load(InputStream source) throws RocketLoadException {
		try {
			OpenRocketDocument doc = loadFromStream(source);
			return doc;
		} catch (Exception e) {
			throw new RocketLoadException("Exception loading stream", e);
		}
	}
	
	public final WarningSet getWarnings() {
		return warnings;
	}
	
	private OpenRocketDocument loadFromStream(InputStream source) throws IOException,
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
		if (buffer[0] == GZIP_SIGNATURE[0] && buffer[1] == GZIP_SIGNATURE[1]) {
			OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentForFile(baseFile, false);
			loadFromStream(doc, new GZIPInputStream(source));
			return doc;
		}
		
		// Check for ZIP (for future compatibility)
		if (buffer[0] == ZIP_SIGNATURE[0] && buffer[1] == ZIP_SIGNATURE[1]) {
			OpenRocketDocument doc;
			if (baseFile != null) {
				doc = OpenRocketDocumentFactory.createDocumentForFile(baseFile, true);
			} else {
				doc = OpenRocketDocumentFactory.createDocumentForUrl(jarURL, true);
			}
			// Search for entry with name *.ork
			ZipInputStream in = new ZipInputStream(source);
			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry == null) {
					throw new RocketLoadException("Unsupported or corrupt file.");
				}
				if (entry.getName().matches(".*\\.[oO][rR][kK]$")) {
					loadFromStream(doc, in);
					return doc;
				} else if (entry.getName().matches(".*\\.[rR][kK][tT]$")) {
					loadFromStream(doc, in);
					return doc;
				}
			}
		}
		
		OpenRocketDocument doc = null;
		if (baseFile != null) {
			doc = OpenRocketDocumentFactory.createDocumentForFile(baseFile, false);
		} else {
			doc = OpenRocketDocumentFactory.createDocumentForUrl(jarURL, false);
		}
		loadFromStream(doc, source);
		return doc;
		
	}
	
	private void loadFromStream(OpenRocketDocument doc, InputStream source) throws IOException, RocketLoadException {
		
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
		
		// Check for OpenRocket
		int match = 0;
		for (int i = 0; i < count; i++) {
			if (buffer[i] == OPENROCKET_SIGNATURE[match]) {
				match++;
				if (match == OPENROCKET_SIGNATURE.length) {
					loadUsing(doc, openRocketLoader, source);
					return;
				}
			} else {
				match = 0;
			}
		}
		
		byte[] typeIdentifier = ArrayUtils.copyOf(buffer, ROCKSIM_SIGNATURE.length);
		if (Arrays.equals(ROCKSIM_SIGNATURE, typeIdentifier)) {
			loadUsing(doc, rocksimLoader, source);
			return;
		}
		throw new RocketLoadException("Unsupported or corrupt file.");
		
	}
	
	private void loadUsing(OpenRocketDocument doc, RocketLoader loader, InputStream source) throws RocketLoadException {
		warnings.clear();
		loader.load(doc, source, motorFinder);
		warnings.addAll(loader.getWarnings());
	}
}
