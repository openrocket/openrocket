package net.sf.openrocket.communication;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.util.ComparablePair;
import net.sf.openrocket.util.Prefs;

public class UpdateInfo {

	private final String latestVersion;
	
	private final ArrayList<ComparablePair<Integer, String>> updates;
	
	
	public UpdateInfo() {
		this.latestVersion = Prefs.getVersion();
		this.updates = new ArrayList<ComparablePair<Integer, String>>();
	}
	
	public UpdateInfo(String version, List<ComparablePair<Integer, String>> updates) {
		this.latestVersion = version;
		this.updates = new ArrayList<ComparablePair<Integer, String>>(updates);
	}



	/**
	 * Get the latest OpenRocket version.  If it is the current version, then the value
	 * of {@link Prefs#getVersion()} is returned.
	 * 
	 * @return	the latest OpenRocket version.
	 */
	public String getLatestVersion() {
		return latestVersion;
	}


	/**
	 * Return a list of the new features/updates that are available.  The list has a
	 * priority for each update and a message text.  The returned list may be modified.
	 * 
	 * @return	a modifiable list of the updates.
	 */
	@SuppressWarnings("unchecked")
	public List<ComparablePair<Integer, String>> getUpdates() {
		return (List<ComparablePair<Integer, String>>) updates.clone();
	}
	
}
