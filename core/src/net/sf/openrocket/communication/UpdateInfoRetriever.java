package net.sf.openrocket.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.openrocket.l10n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.net.ssl.HttpsURLConnection;

/**
 * Class that initiates fetching software update information.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class UpdateInfoRetriever {
	private UpdateInfoFetcher fetcher = null;

	// Map of development tags for releases and their corresponding priority (higher number = more priority; newer release)
	private static final Map<String, Integer> devTags = Stream.of(new Object[][] {
			{ "alpha", 1 },
			{ "beta", 2 },
	}).collect(Collectors.toMap(c -> (String) c[0], c -> (Integer) c[1]));

	/* Enum for the current build version. Values:
          OLDER: current build version is older than the latest official release
          LATEST: current build is the latest official release
          NEWER: current build is "newer" than the latest official release (in the case of beta software)
     */
	public enum ReleaseStatus {
		OLDER,
		LATEST,
		NEWER
	}

	/**
	 * Start an asynchronous task that will fetch information about the latest
	 * OpenRocket version.  This will overwrite any previous fetching operation.
	 * This call will return immediately.
	 */
	public void startFetchUpdateInfo() {
		this.fetcher = new UpdateInfoFetcher();
		this.fetcher.setName("UpdateInfoFetcher");
		this.fetcher.setDaemon(true);
		this.fetcher.start();
	}
	
	
	/**
	 * Check whether the update info fetching is still in progress.
	 * 
	 * @return	<code>true</code> if the communication is still in progress.
	 * @throws	IllegalStateException if {@link #startFetchUpdateInfo()} has not been called
	 */
	public boolean isRunning() {
		if (this.fetcher == null) {
			throw new IllegalStateException("startFetchUpdateInfo() has not been called"); 
		}
		return this.fetcher.isAlive();
	}
	
	
	/**
	 * Retrieve the result of the background update info fetcher.  This method returns 
	 * the result of the previous call to {@link #startFetchUpdateInfo()}. It must be
	 * called before calling this method.
	 * <p>
	 * This method will return <code>null</code> if the info fetcher is still running or
	 * if it encountered a problem in communicating with the server.  The difference can
	 * be checked using {@link #isRunning()}.
	 * 
	 * @return	the update result, or <code>null</code> if the fetching is still in progress
	 * 			or an error occurred while communicating with the server.
	 * @throws	IllegalStateException	if {@link #startFetchUpdateInfo()} has not been called.
	 */
	public UpdateInfo getUpdateInfo() {
		if (this.fetcher == null) {
			throw new IllegalStateException("startFetchUpdateInfo() has not been called");
		}
		return this.fetcher.info;
	}


	/**
	 * An asynchronous task that fetches the latest GitHub release.
	 * 
	 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
	 */
	public static class UpdateInfoFetcher extends Thread {
		private static final Logger log = LoggerFactory.getLogger(UpdateInfoFetcher.class);
		private static final Translator trans = Application.getTranslator();

		private final String preTag = null;       		// Change e.g. to 'android' for Android release
		private final String[] filterTags = null;		// Change to e.g. ["beta"] to only retrieve beta releases
		private final boolean onlyOfficial = false;		// Change to false for beta testing

		private volatile UpdateInfo info;
		
		@Override
		public void run() {
			try {
				runUpdateFetcher();
			} catch (UpdateCheckerException e) {
				info = new UpdateInfo(e);
			}
		}

		/**
		 * Fetch the latest release name from the GitHub repository, compare it with the current build version and change
		 * the UpdateInfo with the result.
		 * @throws UpdateCheckerException if something went wrong in the process
		 */
		public void runUpdateFetcher() throws UpdateCheckerException {
			String buildVersion = BuildProperties.getVersion();

			// Get the latest release name from the GitHub release page
			JsonArray jsonArr = retrieveAllReleaseObjects();
			JsonObject latestObj = getLatestReleaseJSON(jsonArr, preTag, filterTags, onlyOfficial);
			ReleaseInfo release = new ReleaseInfo(latestObj);
			String latestName = release.getReleaseName();

			ReleaseStatus status = compareLatest(buildVersion, latestName);

			switch (status) {
				case OLDER:
					log.info("Found update: " + latestName);
					break;
				case LATEST:
					log.info("Current build is latest version");
					break;
				case NEWER:
					log.info("Current build is newer");
			}

			this.info = new UpdateInfo(release, status);
		}

		/**
		 * Retrieve all the GitHub release JSON objects from OpenRocket's repository
		 *
		 * We need to both check the '/releases' and '/releases/latest' URL, because the '/releases/latest' JSON object
		 * is not included in the '/releases' page.
		 *
		 * @return JSON array containing all the GitHub release JSON objects
		 * @throws UpdateCheckerException if an error occurred (e.g. no internet connection)
		 */
		private JsonArray retrieveAllReleaseObjects() throws UpdateCheckerException {
			// Extra parameters to add to the connection request
			Map<String, String> params = new HashMap<>();
			params.put("accept", "application/vnd.github.v3+json");     // Recommended by the GitHub API

			// Get release tags from release page
			String relUrl = Communicator.UPDATE_URL;
			relUrl = generateUrlWithParameters(relUrl, params);
			JsonArray arr1 = retrieveReleaseJSONArr(relUrl);

			if (arr1 == null) return null;
			if (Communicator.UPDATE_ADDITIONAL_URL == null) return arr1;

			// Get release tags from latest release page
			String latestRelUrl = Communicator.UPDATE_ADDITIONAL_URL;
			latestRelUrl = generateUrlWithParameters(latestRelUrl, params);
			JsonArray arr2 = retrieveReleaseJSONArr(latestRelUrl);

			if (arr2 == null) return null;

			// Combine both arrays
			JsonArrayBuilder builder = Json.createArrayBuilder();
			for (int i = 0; i < arr1.size(); i++) {
				JsonObject obj = arr1.getJsonObject(i);
				builder.add(obj);
			}
			for (int i = 0; i < arr2.size(); i++) {
				JsonObject obj = arr2.getJsonObject(i);
				builder.add(obj);
			}

			return builder.build();
		}

		/**
		 * Retrieve the JSON array of GitHub release objects from the specified URL link
		 * @param urlLink URL link from which to retrieve the JSON array
		 * @return JSON array containing the GitHub release objects
		 * @throws UpdateCheckerException if an error occurred (e.g. no internet connection)
		 */
		private JsonArray retrieveReleaseJSONArr(String urlLink) throws UpdateCheckerException {
			JsonArray jsonArr;

			HttpsURLConnection connection = null;
			try {
				// Set up connection info to the GitHub release page
				URL url = new URL(urlLink);
				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/json");
				connection.setUseCaches(false);
				connection.setAllowUserInteraction(false);
				connection.setConnectTimeout(Communicator.CONNECTION_TIMEOUT);
				connection.setReadTimeout(Communicator.CONNECTION_TIMEOUT);

				// Connect to the GitHub page and get the status response code
				connection.connect();
				int status = connection.getResponseCode();
				log.debug("Update checker response code: " + status);

				// Invalid response code
				if (status != 200) {
					log.warn(String.format("Bad response code from server: %d", status));
					throw new UpdateCheckerException(String.format(trans.get("update.fetcher.badResponse"), status));
				}

				// Read the response JSON data into a StringBuilder
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();

				// Read the release page as a JSON array
				JsonReader reader = Json.createReader(new StringReader(sb.toString()));

				// The reader-content can be a JSON array or just a JSON object
				try {                                   // Case: JSON array
					jsonArr = reader.readArray();
				} catch (JsonParsingException e) {      // Case: JSON object
					JsonArrayBuilder builder = Json.createArrayBuilder();
					reader = Json.createReader(new StringReader(sb.toString()));
					JsonObject obj = reader.readObject();
					builder.add(obj);
					jsonArr = builder.build();
				}
			} catch (UnknownHostException | SocketTimeoutException | ConnectException e) {
				log.warn(String.format("Could not connect to URL: %s. Please check your internet connection.", urlLink));
				throw new UpdateCheckerException(trans.get("update.fetcher.badConnection"));
			} catch (MalformedURLException e) {
				log.warn("Malformed URL: " + urlLink);
				throw new UpdateCheckerException(String.format(trans.get("update.fetcher.malformedURL"), urlLink));
			} catch (IOException e) {
				throw new UpdateCheckerException(String.format("Exception - %s: %s", e, e.getMessage()));
			} finally {     // Close the connection to the release page
				if (connection != null) {
					try {
						connection.disconnect();
					} catch (Exception ex) {
						log.warn("Could not disconnect update checker connection");
					}
				}
			}

			return jsonArr;
		}

		/**
		 * Sometimes release names start with a pre-tag, as is the case for e.g. 'android-13.11', where 'android' is the pre-tag.
		 * This function extracts all the release names that start with the specified preTag.
		 * If preTag is null, the default release names without a pre-tag, starting with a number, are returned (e.g. '15.03').
		 * @param names list of release names to filter
		 * @param preTag pre-tag to filter the names on. If null, no special preTag filtering is applied
		 * @return list of names starting with the preTag
		 */
		public List<String> filterReleasePreTag(List<String> names, String preTag) {
			List<String> filteredTags = new LinkedList<>();

			// Filter out the names that are not related to the preTag
			if (preTag != null) {
				for (String tag : names) {
					if (tag.startsWith(preTag + "-")) {
						// Remove the preTag + '-' delimiter from the tag
						tag = tag.substring(preTag.length() + 1);
						filteredTags.add(tag);
					}
				}
			}
			else {
				// Add every name that starts with a number
				for (String tag : names) {
					if (tag.split("\\.")[0].matches("\\d+")) {
						filteredTags.add(tag);
					}
				}
			}
			return filteredTags;
		}

		/**
		 * Filter out release names that contain certain tags. This could be useful if you are for example running a
		 * beta release and only want releases containing the 'beta'-tag to show up.
		 * If tag is null, the original list is returned.
		 * @param names list of release names to filter
		 * @param tags filter tags
		 * @return list of release names containing the filter tag
		 */
		public List<String> filterReleaseTags(List<String> names, String[] tags) {
			if (names == null) return null;
			if (tags == null) return names;
			return names.stream().filter(c -> Arrays.stream(tags)
					.anyMatch(c::contains)).collect(Collectors.toList());
		}

		/**
		 * Filter a list of release names to only contain official releases, i.e. releases without a devTag (e.g. 'beta').
		 * This could be useful if you're running an official release and don't want to get updates from beta releases.
		 * @param names list of release names to filter
		 * @return list of release names that do not contain a devTag
		 */
		public List<String> filterOfficialRelease(List<String> names) {
			if (names == null) return null;
			return names.stream().filter(c -> Arrays.stream(devTags.keySet().toArray(new String[0]))
					.noneMatch(c::contains)).collect(Collectors.toList());
		}

		/**
		 * Return the latest JSON GitHub release object from a JSON array of release objects.
		 * E.g. from a JSON array where JSON objects have release tags {"14.01", "15.03", "11.01"} return the JSON object
		 * with release tag "15.03"?
		 * @param jsonArr JSON array containing JSON GitHub release objects
		 * @param preTag pre-tag to filter the names on. If null, no special preTag filtering is applied
		 * @param tags tags to filter the names on. If null, no tag filtering is applied
		 * @param onlyOfficial bool to check whether to only include official (non-test) releases
		 * @return latest JSON GitHub release object
		 */
		public JsonObject getLatestReleaseJSON(JsonArray jsonArr, String preTag, String[] tags, boolean onlyOfficial) throws UpdateCheckerException {
			if (jsonArr == null) return null;

			JsonObject latestObj = null;
			String latestName = null;

			// Find the tag with the latest version out of the filtered tags
			for (int i = 0; i < jsonArr.size(); i++) {
				JsonObject obj = jsonArr.getJsonObject(i);
				ReleaseInfo release = new ReleaseInfo(obj);
				String releaseName = release.getReleaseName();

				// Filter the release name
				List<String> temp = new ArrayList<>(List.of(releaseName));
				temp = filterReleasePreTag(temp, preTag);
				temp = filterReleaseTags(temp, tags);
				if (onlyOfficial) {
					temp = filterOfficialRelease(temp);
				}
				if (temp.size() == 0) continue;

				// Init latestObj and latestName here so that only filtered objects and tags can be assigned to them
				if (latestObj == null && latestName == null) {
					latestObj = obj;
					latestName = releaseName;
				}
				else if (compareLatest(releaseName, latestName) == ReleaseStatus.NEWER) {
					latestName = releaseName;
					latestObj = obj;
				}
			}

			return latestObj;
		}

		/**
		 * Compares if the version of tag1 is OLDER, NEWER or equals (LATEST) than the version of tag2
		 * @param tag1 first tag to compare (e.g. "15.03")
		 * @param tag2 second tag to compare (e.g. "14.11")
		 * @return ReleaseStatus of tag1 compared to tag2 (e.g. 'ReleaseStatus.NEWER')
		 * @throws UpdateCheckerException if one of the tags if malformed or null
		 */
		public static ReleaseStatus compareLatest(String tag1, String tag2) throws UpdateCheckerException {
			if (tag1 == null) {
				log.debug("tag1 is null");
				throw new UpdateCheckerException("Malformed release tag");
			}
			if (tag2 == null) {
				log.debug("tag2 is null");
				throw new UpdateCheckerException("Malformed release tag");
			}

			// Each tag should have the format 'XX.XX...' where 'XX' is a number or a text (e.g. 'alpha'). Separator '.'
			// can also be '-'.
			String[] tag1Split = tag1.split("[.-]");
			String[] tag2Split = tag2.split("[.-]");

			for (int i = 0; i < tag2Split.length; i++) {
				// If the loop is still going until this condition, you have the situation where tag1 is e.g.
				// '15.03' and tag2 '15.03.01', so tag is in that case the more recent version.
				if (i >= tag1Split.length) {
					return ReleaseStatus.OLDER;
				}

				try {
					int tag1Value = Integer.parseInt(tag1Split[i]);
					int tag2Value = Integer.parseInt(tag2Split[i]);
					if (tag1Value > tag2Value) {
						return ReleaseStatus.NEWER;
					}
					else if (tag2Value > tag1Value) {
						return ReleaseStatus.OLDER;
					}
				} catch (NumberFormatException e) {     // Thrown when one of the tag elements is a String
					// In case tag1 is e.g. '20.beta.01', and tag2 '20.alpha.16', tag1 is newer
					if (devTags.containsKey(tag1Split[i]) && devTags.containsKey(tag2Split[i])) {
						// In case when e.g. tag1 is '20.beta.01' and tag2 '20.alpha.01', tag1 is newer
						if (devTags.get(tag1Split[i]) > devTags.get(tag2Split[i])) {
							return ReleaseStatus.NEWER;
						}
						// In case when e.g. tag1 is '20.alpha.01' and tag2 '20.beta.01', tag1 is older
						else if (devTags.get(tag1Split[i]) < devTags.get(tag2Split[i])) {
							return ReleaseStatus.OLDER;
						}
						// In case when e.g. tag1 is '20.alpha.01' and tag2 '20.alpha.02', go to the next loop to compare '01' and '02'
						continue;
					}

					// In case tag1 is e.g. '20.alpha.01', but tag2 is already an official release with a number instead of
					// a text, e.g. '20.01'
					if (tag2Split[i].matches("\\d+")) {
						return ReleaseStatus.NEWER;
					}

					String message = String.format("Unrecognized release tag format, tag 1: %s, tag 2: %s", tag1, tag2);
					log.warn(message);
					throw new UpdateCheckerException(message);
				}
			}

			// If tag 1 is bigger than tag 2 and by this point, all the other elements of the tags were the same, tag 1
			// must be newer (e.g. tag 1 = '15.03.01' and tag 2 = '15.03').
			if (tag1Split.length > tag2Split.length) {
				return ReleaseStatus.NEWER;
			}

			return ReleaseStatus.LATEST;
		}

		/**
		 * Generate a URL with a set of parameters included.
		 * E.g. url = github.com/openrocket/openrocket/releases, params = {"lorem", "ipsum"}
		 *      => formatted url: github.com/openrocket/openrocket/releases?lorem=ipsum
		 * @param url base URL
		 * @param params parameters to include
		 * @return formatted URL (= base URL with parameters)
		 */
		private String generateUrlWithParameters(String url, Map<String, String> params) {
			StringBuilder formattedUrl = new StringBuilder(url);
			formattedUrl.append("?");        // Identifier for start of query string (for parameters)

			// Append the parameters to the URL
			int idx = 0;
			for (Map.Entry<String, String> e : params.entrySet()) {
				formattedUrl.append(String.format("%s=%s", e.getKey(), e.getValue()));
				if (idx < params.size() - 1) {
					formattedUrl.append("&");    // Identifier for more parameters
				}
				idx++;
			}
			return formattedUrl.toString();
		}

		/**
		 * Exception for the update checker
		 */
		public static class UpdateCheckerException extends Exception {
			public UpdateCheckerException(String message) {
				super(message);
			}
		}
	}
}
