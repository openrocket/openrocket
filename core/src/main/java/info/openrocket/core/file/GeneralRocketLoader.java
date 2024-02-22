package info.openrocket.core.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.openrocket.importt.OpenRocketLoader;
import info.openrocket.core.file.rasaero.importt.RASAeroLoader;
import info.openrocket.core.file.rocksim.importt.RockSimLoader;
import info.openrocket.core.util.ArrayUtils;
import info.openrocket.core.util.TextUtil;

/**
 * A rocket loader that auto-detects the document type and uses the appropriate
 * loading. Supports loading of GZIPed files as well with transparent
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
	private static final byte[] RASAERO_SIGNATURE = TextUtil.asciiBytes("<RASAeroDoc");

	private final OpenRocketLoader openRocketLoader = new OpenRocketLoader();

	private final RockSimLoader rocksimLoader = new RockSimLoader();
	private final RASAeroLoader rasaeroLoader = new RASAeroLoader();

	private final File baseFile;
	private final URL jarURL;
	private boolean isContainer;

	private final MotorFinder motorFinder;
	private AttachmentFactory attachmentFactory;
	private final OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();

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
			String fileName = baseFile != null && baseFile.getName() != null
					? baseFile.getName().replaceFirst("[.][^.]+$", "")
					: null;
			stream = new BufferedInputStream(new FileInputStream(baseFile));
			load(stream, fileName);
			return doc;

		} catch (Exception e) {
			throw new RocketLoadException("Exception loading file: " + baseFile + " , " + e.getMessage(), e);
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

	public final OpenRocketDocument load(InputStream source, String fileName) throws RocketLoadException {
		try {
			loadStep1(source, fileName);
			doc.getRocket().enableEvents();
			return doc;
		} catch (Exception e) {
			throw new RocketLoadException("Exception loading stream: " + e.getMessage(), e);
		}
	}

	public final WarningSet getWarnings() {
		return warnings;
	}

	/**
	 * This method determines the type file contained in the stream then calls the
	 * appropriate loading mechanism.
	 * 
	 * If the stream is a gzip file, the argument is wrapped in a GzipInputStream
	 * and the rocket loaded.
	 * 
	 * If the stream is a zip container, the first zip entry with name ending in
	 * .ork or .rkt is loaded as the rocket.
	 * 
	 * If the stream is neither, then it is assumed to be an xml file containing
	 * either an ork or rkt format rocket.
	 * 
	 * @param source
	 * @throws IOException
	 * @throws RocketLoadException
	 */
	private void loadStep1(InputStream source, String fileName) throws IOException, RocketLoadException {

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
			isContainer = false;
			setAttachmentFactory();
			loadRocket(new GZIPInputStream(source), fileName);
			return;
		}

		// Check for ZIP (for future compatibility)
		if (buffer[0] == ZIP_SIGNATURE[0] && buffer[1] == ZIP_SIGNATURE[1]) {
			isContainer = true;
			setAttachmentFactory();
			// Search for entry with name *.ork
			ZipInputStream in = new ZipInputStream(source);
			ZipEntry entry = in.getNextEntry();
			if (entry == null) {
				throw new RocketLoadException("Unsupported or corrupt file.");
			}
			if (entry.getName().matches(".*\\.[oO][rR][kK]$")) {
				loadRocket(in, fileName);
			} else if (entry.getName().matches(".*\\.[rR][kK][tT]$")) {
				loadRocket(in, fileName);
			} else if (entry.getName().matches(".*\\.[cC][dD][xX]1$")) {
				loadRocket(in, fileName);
			}
			in.close();
			return;
		}

		isContainer = false;
		setAttachmentFactory();
		loadRocket(source, fileName);
	}

	private void loadRocket(InputStream source, String fileName) throws IOException, RocketLoadException {

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
					loadUsing(openRocketLoader, source, fileName);
					return;
				}
			} else {
				match = 0;
			}
		}

		// Check for RockSim
		byte[] typeIdentifier = ArrayUtils.copyOf(buffer, ROCKSIM_SIGNATURE.length);
		if (Arrays.equals(ROCKSIM_SIGNATURE, typeIdentifier)) {
			loadUsing(rocksimLoader, source, fileName);
			return;
		}

		// Check for RASAero
		typeIdentifier = ArrayUtils.copyOf(buffer, RASAERO_SIGNATURE.length);
		if (Arrays.equals(RASAERO_SIGNATURE, typeIdentifier)) {
			loadUsing(rasaeroLoader, source, fileName);
			return;
		}
		throw new RocketLoadException("Unsupported or corrupt file.");

	}

	private void setAttachmentFactory() {
		attachmentFactory = new FileSystemAttachmentFactory(null);
		if (jarURL != null && isContainer) {
			attachmentFactory = new ZipFileAttachmentFactory(jarURL);
		} else {
			if (isContainer) {
				try {
					attachmentFactory = new ZipFileAttachmentFactory(baseFile.toURI().toURL());
				} catch (MalformedURLException mex) {
				}
			} else if (baseFile != null) {
				attachmentFactory = new FileSystemAttachmentFactory(baseFile.getParentFile());
			}
		}
	}

	private void loadUsing(RocketLoader loader, InputStream source, String fileName) throws RocketLoadException {
		warnings.clear();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(doc);
		context.setMotorFinder(motorFinder);
		context.setAttachmentFactory(attachmentFactory);
		loader.load(context, source, fileName);
		warnings.addAll(loader.getWarnings());
	}
}
