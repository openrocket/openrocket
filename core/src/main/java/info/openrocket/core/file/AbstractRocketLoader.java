package info.openrocket.core.file;

import java.io.IOException;
import java.io.InputStream;

import info.openrocket.core.logging.WarningSet;

public abstract class AbstractRocketLoader implements RocketLoader {
	protected final WarningSet warnings = new WarningSet();

	/**
	 * Loads a rocket from the specified InputStream.
	 */
	@Override
	public final void load(DocumentLoadingContext context, InputStream source, String fileName)
			throws RocketLoadException {
		warnings.clear();

		try {
			loadFromStream(context, source, fileName);
		} catch (RocketLoadException e) {
			throw e;
		} catch (IOException e) {
			throw new RocketLoadException("I/O error: " + e.getMessage(), e);
		}
	}

	/**
	 * This method is called by the default implementations of #load(File)
	 * and load(InputStream) to load the rocket.
	 * 
	 * @throws RocketLoadException if an error occurs during loading.
	 */
	protected abstract void loadFromStream(DocumentLoadingContext context, InputStream source, String fileName)
			throws IOException, RocketLoadException;

	@Override
	public final WarningSet getWarnings() {
		return warnings;
	}
}
