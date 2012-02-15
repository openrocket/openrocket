package net.sf.openrocket.android.rocket;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.RocketLoadException;

public class OpenRocketLoaderResult {

	public WarningSet warnings;
	public OpenRocketDocument rocket;
	public RocketLoadException loadingError;
	
}
