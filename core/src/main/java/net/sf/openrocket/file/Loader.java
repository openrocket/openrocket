package net.sf.openrocket.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface Loader<T> {

	public Collection<T> load(InputStream stream, String filename) throws IOException;
	
}
