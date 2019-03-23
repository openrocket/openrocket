package net.sf.openrocket.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.ComparablePair;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class UpdateInfoTest extends BaseTestCase {
	
	/** The connection delay */
	private static final int DELAY = 100;
	
	/** How much long does the test allow it to take */
	private static final int ALLOWANCE = 2000;
	
	
	private HttpURLConnectionMock setup() {
		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		Communicator.setConnectionSource(new ConnectionSourceStub(connection));
		
		connection.setConnectionDelay(DELAY);
		connection.setUseCaches(true);
		connection.setContentType("text/plain");
		return connection;
	}
	
	private void check(HttpURLConnectionMock connection) {
		assertEquals(Communicator.UPDATE_INFO_URL + "?version=" + BuildProperties.getVersion(),
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
		retriever.start();
		
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
		retriever.start();
		
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
		retriever.start();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);
		
		
		connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
		connection.setContentType("text/xml");
		
		retriever = new UpdateInfoRetriever();
		retriever.start();
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
		retriever.start();
		assertNull(retriever.getUpdateInfo());
		waitfor(retriever);
		assertFalse(connection.hasFailed());
		assertNull(retriever.getUpdateInfo());
		check(connection);
		
		
		connection = setup();
		connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
		connection.setContent(new byte[0]);
		
		retriever = new UpdateInfoRetriever();
		retriever.start();
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
			int size = (int) ((1 + 0.3 * rnd.nextGaussian()) * Math.pow(i, 6));
			byte[] buf = new byte[size];
			rnd.nextBytes(buf);
			
			HttpURLConnectionMock connection = setup();
			connection.setResponseCode(Communicator.UPDATE_INFO_UPDATE_AVAILABLE);
			connection.setContent(buf);
			
			UpdateInfoRetriever retriever = new UpdateInfoRetriever();
			retriever.start();
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
	}
	
}
