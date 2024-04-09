package info.openrocket.core.file;

import java.io.InputStream;

import info.openrocket.core.logging.WarningSet;

public interface RocketLoader {

	public void load(DocumentLoadingContext context, InputStream source, String fileName) throws RocketLoadException;

	public WarningSet getWarnings();

}
