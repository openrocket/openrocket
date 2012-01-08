package net.sf.openrocket.communication;

import java.util.List;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.ComparablePair;

public class UpdateInfo {
	
	private final String latestVersion;
	
	private final ArrayList<ComparablePair<Integer, String>> updates;
	
	
	public UpdateInfo() {
		this.latestVersion = BuildProperties.getVersion();
		this.updates = new ArrayList<ComparablePair<Integer, String>>();
	}
	
	public UpdateInfo(String version, List<ComparablePair<Integer, String>> updates) {
		this.latestVersion = version;
		this.updates = new ArrayList<ComparablePair<Integer, String>>(updates);
	}
	
	

	/**
	 * Get the latest OpenRocket version.  If it is the current version, then the value
	 * of {@link BuildProperties#getVersion()} is returned.
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
	public List<ComparablePair<Integer, String>> getUpdates() {
		return updates.clone();
	}
	
	@Override
	public String toString() {
		return "UpdateInfo[version=" + latestVersion + "; updates=" + updates.toString() + "]";
	}
	
}
