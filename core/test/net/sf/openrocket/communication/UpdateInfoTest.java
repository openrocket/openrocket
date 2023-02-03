package net.sf.openrocket.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class UpdateInfoTest extends BaseTestCase {
	
	/** The connection delay */
	private static final int DELAY = 100;
	
	/** How much long does the test allow it to take */
	private static final int ALLOWANCE = 2000;

	@Test
	public void testCompareLatest() throws UpdateInfoRetriever.UpdateInfoFetcher.UpdateCheckerException {
		// Test normal official releases
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("14.03", "15.03"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "22.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "15.03"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "22.02.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.01", "22.02.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.01", "22.02"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "22.03"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "22.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "23"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "21"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22", "23"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22", "21"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.00", "22.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.00", "22.00"));


		// Test alpha/beta releases
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.alpha.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.02", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.beta.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.02", "22.02.beta.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.01", "22.alpha.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.01", "22.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.02", "22.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.01", "22.beta.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.01", "22.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.02", "22.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.01", "22.RC.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.LATEST,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.01", "22.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.02", "22.RC.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.01", "22.02.alpha.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.01", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.alpha.02", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.01", "22.02.beta.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.beta.02", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.01", "22.02.RC.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.01", "22.02.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.RC.02", "22.02.RC.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.beta.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.02", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.RC.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.02", "22.02.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.RC.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.02", "22.02.RC.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.alpha.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.02", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.alpha.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.02", "22.02.alpha.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.beta.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.02", "22.02.beta.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.03"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.alpha.01", "22.02.02"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.03"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.beta.01", "22.02.02"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.03", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.01", "22.02.beta.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.02", "22.02.beta.01"));

		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.03"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.NEWER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.01"));
		assertEquals(UpdateInfoRetriever.ReleaseStatus.OLDER,
				UpdateInfoRetriever.UpdateInfoFetcher.compareLatest("22.02.RC.01", "22.02.02"));


		// Test bogus releases
		assertExceptionCompareLatest("22.02.gamma.01", "22.02");
		assertExceptionCompareLatest(null, "22.02");
		assertExceptionCompareLatest("22.02", null);
		assertExceptionCompareLatest(null, null);
		assertExceptionCompareLatest("", "15.03");
		assertExceptionCompareLatest("22.02", "");
		assertExceptionCompareLatest(" ", "15.03");
		assertExceptionCompareLatest("22.02", " ");
		assertExceptionCompareLatest("Hello", "22.02");
		assertExceptionCompareLatest("22.02", "world");
		assertExceptionCompareLatest("22.02", "15,03");
		assertExceptionCompareLatest("22..02", "15.03");
		assertExceptionCompareLatest("22.02", "15..03");
		assertExceptionCompareLatest("22.02a", "15.03");
		assertExceptionCompareLatest("22.02", "15.03b");
		assertExceptionCompareLatest("alpha.22.02", "15.03");
		assertExceptionCompareLatest("gamma.22.02", "15.03");
		assertExceptionCompareLatest("15.03", "alpha.22.02");
		assertExceptionCompareLatest("15.03", "gamma.22.02");
		assertExceptionCompareLatest("-22.02", "15.03");
		assertExceptionCompareLatest("22.-02", "15.03");
		assertExceptionCompareLatest("22.02", "-15.03");
		assertExceptionCompareLatest("22.02", "15.-03");
	}

	private void assertExceptionCompareLatest(String tag1, String tag2) {
		try {
			UpdateInfoRetriever.UpdateInfoFetcher.compareLatest(tag1, tag2);
			fail("Should have thrown an exception");
		} catch (UpdateInfoRetriever.UpdateInfoFetcher.UpdateCheckerException e) {
			// Expected
		}
	}

	@Test
	public void testFilterReleaseTags() {
		String[] temp = {"22.02", "22", "15.03", "0.1", "22.02.beta.01", "23.alpha.01", "20.beta.01", "22.gamma.01",
				"beta.01", "alpha.02"};
		List<String> releases = Arrays.asList(temp);
		String[] expectedReleases = {"22.02.beta.01", "20.beta.01", "22.gamma.01", "beta.01"};

		String[] filters = {"beta", "gamma"};
		List<String> results = UpdateInfoRetriever.UpdateInfoFetcher.filterReleaseTags(releases, filters);
		System.out.println(results);
		assertEquals(" filtered results have different size", expectedReleases.length, results.size());
		for (String r : expectedReleases) {
			assertTrue(String.format(" Filtered results does not contain %s", r), results.contains(r));
		}

		results = UpdateInfoRetriever.UpdateInfoFetcher.filterReleaseTags(releases, null);
		assertEquals(" filtered results have different size", releases.size(), results.size());
		for (String r : releases) {
			assertTrue(String.format(" Filtered results does not contain %s", r), results.contains(r));
		}

		assertNull(UpdateInfoRetriever.UpdateInfoFetcher.filterReleaseTags(null, null));
	}

	@Test
	public void testFilterOfficialRelease() {
		String[] temp = {"22.02", "22", "15.03", "0.1", "22.02.beta.01", "23.alpha.01", "20.gamma.01"};
		List<String> releases = Arrays.asList(temp);
		String[] expectedReleases = {"22.02", "22", "15.03", "0.1", "20.gamma.01"};

		releases = UpdateInfoRetriever.UpdateInfoFetcher.filterOfficialRelease(releases);
		assertEquals(" filtered results have different size", expectedReleases.length, releases.size());
		for (String r : expectedReleases) {
			assertTrue(String.format(" Filtered results does not contain %s", r), releases.contains(r));
		}

		assertNull(UpdateInfoRetriever.UpdateInfoFetcher.filterOfficialRelease(null));
	}

	@Test
	public void testFilterReleasePreTag() {
		String[] temp = {"22.03", "22", "15.03", "0.2", "22.02.beta.01", "23.alpha.01", "20.gamma.01",
				"android-22.02", "22-android", "15.03", "android-0.1", "android.22.02.beta.01", "android23.alpha.01", "20.gamma.01"};
		List<String> releases = Arrays.asList(temp);
		String[] expectedReleases = {"22.02", "0.1"};

		List<String> results = UpdateInfoRetriever.UpdateInfoFetcher.filterReleasePreTag(releases, "android");
		assertEquals(" filtered results have different size", expectedReleases.length, results.size());
		for (String r : expectedReleases) {
			assertTrue(String.format(" Filtered results does not contain %s", r), results.contains(r));
		}

		assertNull(UpdateInfoRetriever.UpdateInfoFetcher.filterReleasePreTag(null, "android"));
		assertNull(UpdateInfoRetriever.UpdateInfoFetcher.filterReleasePreTag(null, null));

		expectedReleases = new String[]{"22.03", "22", "15.03", "0.2", "22.02.beta.01", "23.alpha.01", "20.gamma.01",
				"15.03","20.gamma.01"};
		results = UpdateInfoRetriever.UpdateInfoFetcher.filterReleasePreTag(releases, null);
		assertEquals(" filtered results have different size", expectedReleases.length, results.size());
		for (String r : expectedReleases) {
			assertTrue(String.format(" Filtered results does not contain %s", r), results.contains(r));
		}
	}

	@Test
	public void testParseJsonArray() throws UpdateInfoRetriever.UpdateInfoFetcher.UpdateCheckerException {
		JsonArray jsonArr = null;
		JsonObject latestObj = UpdateInfoRetriever.UpdateInfoFetcher.getLatestReleaseJSON(jsonArr, null, null, false);
		assertNull(latestObj);

		// Generate a dummy json array, containing GitHub release info
		JsonArrayBuilder builder = Json.createArrayBuilder();
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		objectBuilder.add("tag_name", "release-22.02");
		objectBuilder.add("body", "Release notes");
		objectBuilder.add("html_url", "localhost");
		JsonArrayBuilder assetsBuilder = Json.createArrayBuilder();
		JsonObjectBuilder assetObject = Json.createObjectBuilder();
		assetObject.add("name", "OpenRocket-22.02-macOS.dmg");
		assetObject.add("browser_download_url", "https://github.com/openrocket/openrocket/releases/download/release-22.02/OpenRocket-22.02-macOS.dmg");
		assetsBuilder.add(assetObject.build());
		objectBuilder.add("assets", assetsBuilder.build());
		builder.add(objectBuilder.build());
		jsonArr = builder.build();

		latestObj = UpdateInfoRetriever.UpdateInfoFetcher.getLatestReleaseJSON(jsonArr, null, null, false);
		ReleaseInfo release = new ReleaseInfo(latestObj);
		String latestName = release.getReleaseName();
		String releaseNotes = release.getReleaseNotes();
		String releaseUrl = release.getReleaseURL();
		List<String> assetURLs = release.getAssetURLs();
		assertEquals("22.02", latestName);
		assertEquals("Release notes", releaseNotes);
		assertEquals("localhost", releaseUrl);
		assertEquals(1, assetURLs.size());
		assertEquals("https://github.com/openrocket/openrocket/releases/download/release-22.02/OpenRocket-22.02-macOS.dmg", assetURLs.get(0));

		// Test bogus releases
		try {
			new ReleaseInfo(null);
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }

		jsonArr = Json.createArrayBuilder().build();
		latestObj = UpdateInfoRetriever.UpdateInfoFetcher.getLatestReleaseJSON(jsonArr, null, null, false);
		assertNull(latestObj);

		builder = Json.createArrayBuilder();
		builder.add(Json.createObjectBuilder().build());
		jsonArr = builder.build();
		try {
			UpdateInfoRetriever.UpdateInfoFetcher.getLatestReleaseJSON(jsonArr, null, null, false);
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }

		release = new ReleaseInfo(Json.createObjectBuilder().build());
		try {
			release.getReleaseName();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }
		try {
			release.getReleaseNotes();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }
		try {
			release.getReleaseURL();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }
		try {
			release.getAssetURLs();
			fail("Should have thrown NullPointerException");
		} catch (NullPointerException ignore) { }
	}

	@Test
	public void testFetchReleases() {
		// TODO: fetch releases from GitHub (= test UpdateInfoRetriever.UpdateInfoFetcher.retrieveAllReleaseObjects)
	}

	// TODO: these are the old unit tests; leaving them in to be used as reference for testFetchReleases()
	/*private HttpURLConnectionMock setup() {
		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		Communicator.setConnectionSource(new ConnectionSourceStub(connection));

		connection.setConnectionDelay(DELAY);
		connection.setUseCaches(true);
		connection.setContentType("text/plain");
		return connection;
	}

	private void check(HttpURLConnectionMock connection) {
		assertEquals(Communicator.UPDATE_URL + "?version=" + BuildProperties.getVersion(),
				connection.getTrueUrl());
		assertTrue(connection.getConnectTimeout() > 0);
		assertEquals(BuildProperties.getVersion() + "+" + BuildProperties.getBuildSource(),
				connection.getRequestProperty("X-OpenRocket-Version"));
		assertNotNull(connection.getRequestProperty("X-OpenRocket-Country"));
		assertNotNull(connection.getRequestProperty("X-OpenRocket-ID"));
		assertNotNull(connection.getRequestProperty("X-OpenRocket-OS"));
		assertNotNull(connection.getRequestProperty("X-OpenRocket-Java"));
		assertTrue(connection.getInstanceFollowRedirects());
		assertEquals("GET", connection.getRequestMethod());
		assertFalse(connection.getUseCaches());
	}


	@Test
	public void testUpdateAvailable() throws IOException {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);

		String content =
				"Version: 6.6.6pre A \n" +
						"Extra:  information\n" +
						"100:hundred\n" +
						"50:  m\u00e4 \n\n" +
						"1:     one\n" +
						"-2: none";
		connection.setContent(content);

		UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();

		// Info is null while processing
		assertNull(retriever.getUpdateInfo());

		waitfor(retriever);
		assertFalse(connection.hasFailed());

		UpdateInfo info = retriever.getUpdateInfo();
		assertNotNull(info);

		check(connection);

		assertEquals("6.6.6pre A", info.getLatestVersion());

		List<ComparablePair<Integer, String>> updates = info.getUpdates();
		assertEquals(3, updates.size());
		Collections.sort(updates);
		assertEquals(1, (int) updates.get(0).getU());
		assertEquals("one", updates.get(0).getV());
		assertEquals(50, (int) updates.get(1).getU());
		assertEquals("m\u00e4", updates.get(1).getV());
		assertEquals(100, (int) updates.get(2).getU());
		assertEquals("hundred", updates.get(2).getV());
	}




	@Test
	public void testUpdateNotAvailable() throws IOException {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_NO_UPDATE_CODE);

		String content =
				"Version: 6.6.6pre A \n" +
						"Extra:  information\n" +
						"100:hundred\n" +
						"50:  m\u00e4 \n\n" +
						"1:     one\n" +
						"-2: none";
		connection.setContent(content);

		UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();

		// Info is null while processing
		assertNull(retriever.getUpdateInfo());

		waitfor(retriever);
		assertFalse(connection.hasFailed());

		UpdateInfo info = retriever.getUpdateInfo();
		assertNotNull(info);

		check(connection);

		assertEquals(BuildProperties.getVersion(), info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
	}



	@Test
	public void testInvalidResponses() {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(404);
		connection.setContent("Version: 1.2.3");

		UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);


		connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
		connection.setContentType("text/xml");

		retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);



		connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
		String content =
				"100:hundred\n" +
						"50:  m\u00e4 \n\n" +
						"1:     one\n";
		connection.setContent(content);

		retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);


		connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
		connection.setContent(new byte[0]);

		retriever = new UpdateInfoRetriever();
		retriever.startFetchUpdateInfo();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);

	}

	@Test
	public void testRandomInputData() {

		Random rnd = new Random();
		for (int i = 0; i < 10; i++) {
			int size = Math.abs((int) ((1 + 0.3 * rnd.nextGaussian()) * Math.pow(i, 6)));
			byte[] buf = new byte[size];
			rnd.nextBytes(buf);

			HttpURLConnectionMock connection = setup();
			connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
			connection.setContent(buf);

			UpdateInfoRetriever retriever = new UpdateInfoRetriever();
			retriever.startFetchUpdateInfo();
			assertNull(retriever.getUpdateInfo());
			waitfor(retriever);
			assertFalse(connection.hasFailed());
			assertNull(retriever.getUpdateInfo());
			check(connection);
		}

	}



	private void waitfor(UpdateInfoRetriever retriever) {
		long t = System.currentTimeMillis();

		while (retriever.isRunning()) {
			if (System.currentTimeMillis() >= t + ALLOWANCE) {
				fail("retriever took too long to respond");
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		//System.out.println("Waiting took " + (System.currentTimeMillis()-t) + " ms");
	}*/

}
