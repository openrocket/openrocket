package net.sf.openrocket.communication;

import java.util.List;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.ComparablePair;

 /**
  * 
  * class that stores the update information of the application
  *
  */
public class UpdateInfo {
	
	private final String latestVersion;
	
	private final ArrayList<ComparablePair<Integer, String>> updates;
	
	/**
	 * loads the default information
	 */
	public UpdateInfo() {
		this.latestVersion = BuildProperties.getVersion();
		this.updates = new ArrayList<ComparablePair<Integer, String>>();
	}
	
	/**
	 * loads a custom update information into the cache
	 * @param version	String with the version
	 * @param updates	The list of updates contained in the version
	 */
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
