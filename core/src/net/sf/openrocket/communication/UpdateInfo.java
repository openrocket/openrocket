package net.sf.openrocket.communication;

import net.sf.openrocket.communication.UpdateInfoRetriever.ReleaseStatus;
import net.sf.openrocket.communication.UpdateInfoRetriever.UpdateInfoFetcher.UpdateCheckerException;

/**
 * Class that stores the update information of the application
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class UpdateInfo {
	private final ReleaseInfo latestRelease;
	private final ReleaseStatus releaseStatus;
	private final UpdateCheckerException exception;		// Exception that was thrown during the release fetching process. If null, the fetching was successful.
	
	/**
	 * Constructor for when a valid release is found.
	 * @param latestRelease	the release info object of the latest GitHub release
	 * @param releaseStatus the release status of the current build version compared to the latest GitHub release version
	 */
	public UpdateInfo(ReleaseInfo latestRelease, ReleaseStatus releaseStatus) {
		this.latestRelease = latestRelease;
		this.releaseStatus = releaseStatus;
		this.exception = null;
	}

	/**
	 * Constructor for when an error occurred when checking the latest release.
	 * @param exception exception that was thrown when checking the releases
	 */
	public UpdateInfo(UpdateCheckerException exception) {
		this.latestRelease = null;
		this.releaseStatus = null;
		this.exception = exception;
	}

	/**
	 * Get the release status of the current build version compared to the latest GitHub release version.
	 * @return the release status of the current
	 */
	public ReleaseStatus getReleaseStatus() {
		return this.releaseStatus;
	}

	/**
	 * Get the latest release info object.
	 * @return the latest GitHub release object
	 */
	public ReleaseInfo getLatestRelease() {
		return this.latestRelease;
	}

	/**
	 * Get the exception that was thrown when fetching the latest release. If the fetching was successful, null is returned.
	 * @return UpdateCheckerException exception that was thrown when fetching the release. Null if fetching was successful
	 */
	public UpdateCheckerException getException() {
		return this.exception;
	}
	
	@Override
	public String toString() {
		return "UpdateInfo[releaseStatus=" + releaseStatus + "; latestRelease=" + (latestRelease == null ? "null" : latestRelease.toString()) + "]";
	}
}
