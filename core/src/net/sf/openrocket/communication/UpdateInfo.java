package net.sf.openrocket.communication;

import net.sf.openrocket.util.BuildProperties;

/**
 * Class that stores the update information of the application
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class UpdateInfo {
	private final String latestVersion;

	 // Release info of the latest release. If null, the current build is the latest version
	private final ReleaseInfo latestRelease;
	
	/**
	 * loads the default information
	 */
	public UpdateInfo() {
		this.latestVersion = BuildProperties.getVersion();
		this.latestRelease = null;
	}
	
	/**
	 * loads a custom update information into the cache
	 * @param latestRelease	The release info object of the latest GitHub release
	 */
	public UpdateInfo(ReleaseInfo latestRelease) {
		this.latestRelease = latestRelease;
		this.latestVersion = latestRelease.getReleaseTag();
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
	 * Get the latest release info object.
	 * @return the latest GitHub release object
	 */
	public ReleaseInfo getLatestRelease() {
		return latestRelease;
	}
	
	@Override
	public String toString() {
		return "UpdateInfo[version=" + latestVersion + "; latestRelease=" + latestRelease.toString() + "]";
	}
	
}
