package net.sf.openrocket.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.ComparablePair;
import net.sf.openrocket.util.LimitedInputStream;

public class UpdateInfoRetriever {
	
	private static final Logger log = LoggerFactory.getLogger(UpdateInfoRetriever.class);
	
	private UpdateInfoFetcher fetcher = null;
	
	
	/**
	 * Start an asynchronous task that will fetch information about the latest
	 * OpenRocket version.  This will overwrite any previous fetching operation.
	 * This call will return immediately.
	 */
	public void start() {
		fetcher = new UpdateInfoFetcher();
		fetcher.setName("UpdateInfoFetcher");
		fetcher.setDaemon(true);
		fetcher.start();
	}
	
	
	/**
	 * Check whether the update info fetching is still in progress.
	 * 
	 * @return	<code>true</code> if the communication is still in progress.
	 */
	public boolean isRunning() {
		if (fetcher == null) {
			throw new IllegalStateException("startFetchUpdateInfo() has not been called");
		}
		return fetcher.isAlive();
	}
	
	
	/**
	 * Retrieve the result of the background update info fetcher.  This method returns 
	 * the result of the previous call to {@link #start()}. It must be
	 * called before calling this method.
	 * <p>
	 * This method will return <code>null</code> if the info fetcher is still running or
	 * if it encountered a problem in communicating with the server.  The difference can
	 * be checked using {@link #isRunning()}.
	 * 
	 * @return	the update result, or <code>null</code> if the fetching is still in progress
	 * 			or an error occurred while communicating with the server.
	 * @throws	IllegalStateException	if {@link #start()} has not been called.
	 */
	public UpdateInfo getUpdateInfo() {
		if (fetcher == null) {
			throw new IllegalStateException("start() has not been called");
		}
		return fetcher.info;
	}
	
	
	
	/**
	 * Parse the data received from the server.
	 * 
	 * @param r		the Reader from which to read.
	 * @return		an UpdateInfo construct, or <code>null</code> if the data was invalid.
	 * @throws IOException	if an I/O exception occurs.
	 */
	/* package-private */
	static UpdateInfo parseUpdateInput(Reader r) throws IOException {
		BufferedReader reader;
		if (r instanceof BufferedReader) {
			reader = (BufferedReader) r;
		} else {
			reader = new BufferedReader(r);
		}
		
		
		String version = null;
		ArrayList<ComparablePair<Integer, String>> updates =
				new ArrayList<ComparablePair<Integer, String>>();
		
		String str = reader.readLine();
		while (str != null) {
			if (str.matches("^Version: *[0-9]+\\.[0-9]+\\.[0-9]+[a-zA-Z0-9.-]* *$")) {
				version = str.substring(8).trim();
			} else if (str.matches("^[0-9]+:\\p{Print}+$")) {
				int index = str.indexOf(':');
				int value = Integer.parseInt(str.substring(0, index));
				String desc = str.substring(index + 1).trim();
				if (!desc.equals("")) {
					updates.add(new ComparablePair<Integer, String>(value, desc));
				}
			}
			// Ignore anything else
			str = reader.readLine();
		}
		
		if (version != null) {
			return new UpdateInfo(version, updates);
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * An asynchronous task that fetches and parses the update info.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private class UpdateInfoFetcher extends Thread {
		
		private volatile UpdateInfo info = null;
		
		@Override
		public void run() {
			try {
				doConnection();
			} catch (IOException e) {
				log.info("Fetching update failed: " + e);
				return;
			}
		}
		
		
		private void doConnection() throws IOException {
			String url = Communicator.UPDATE_INFO_URL + "?" + Communicator.VERSION_PARAM + "="
					+ Communicator.encode(BuildProperties.getVersion());
			
			HttpURLConnection connection = Communicator.connectionSource.getConnection(url);
			
			connection.setConnectTimeout(Communicator.CONNECTION_TIMEOUT);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setRequestProperty("X-OpenRocket-Version",
					Communicator.encode(BuildProperties.getVersion() + " " + BuildProperties.getBuildSource()));
			connection.setRequestProperty("X-OpenRocket-ID",
					Communicator.encode(Application.getPreferences().getUniqueID()));
			connection.setRequestProperty("X-OpenRocket-OS",
					Communicator.encode(System.getProperty("os.name") + " " +
							System.getProperty("os.arch")));
			connection.setRequestProperty("X-OpenRocket-Java",
					Communicator.encode(System.getProperty("java.vendor") + " " +
							System.getProperty("java.version")));
			connection.setRequestProperty("X-OpenRocket-Country",
					Communicator.encode(System.getProperty("user.country") + " " +
							System.getProperty("user.timezone")));
			connection.setRequestProperty("X-OpenRocket-Locale",
					Communicator.encode(Locale.getDefault().toString()));
			connection.setRequestProperty("X-OpenRocket-CPUs", "" + Runtime.getRuntime().availableProcessors());
			
			InputStream is = null;
			try {
				connection.connect();
				
				log.debug("Update response code: " + connection.getResponseCode());
				
				if (connection.getResponseCode() == Communicator.UPDATE_INFO_NO_UPDATE_CODE) {
					// No updates are available
					log.info("No updates available");
					info = new UpdateInfo();
					return;
				}
				
				if (connection.getResponseCode() != Communicator.UPDATE_INFO_UPDATE_AVAILABLE) {
					// Error communicating with server
					log.warn("Unknown server response code: " + connection.getResponseCode());
					return;
				}
				
				String contentType = connection.getContentType();
				if (contentType == null ||
						contentType.toLowerCase(Locale.ENGLISH).indexOf(Communicator.UPDATE_INFO_CONTENT_TYPE) < 0) {
					// Unknown response type
					log.warn("Unknown Content-type received:" + contentType);
					return;
				}
				
				// Update is available, parse input
				is = connection.getInputStream();
				is = new LimitedInputStream(is, Communicator.MAX_INPUT_BYTES);
				String encoding = connection.getContentEncoding();
				if (encoding == null || encoding.equals(""))
					encoding = "UTF-8";
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
				
				String version = null;
				ArrayList<ComparablePair<Integer, String>> updates =
						new ArrayList<ComparablePair<Integer, String>>();
				
				String line = reader.readLine();
				while (line != null) {
					
					if (line.matches("^Version:[a-zA-Z0-9._ -]{1,30}$")) {
						version = line.substring(8).trim();
					} else if (line.matches("^[0-9]{1,9}:\\P{Cntrl}{1,300}$")) {
						String[] split = line.split(":", 2);
						int n = Integer.parseInt(split[0]);
						updates.add(new ComparablePair<Integer, String>(n, split[1].trim()));
					}
					// Ignore line otherwise
					line = reader.readLine();
				}
				
				// Check version input
				if (version == null || version.length() == 0 ||
						version.equalsIgnoreCase(BuildProperties.getVersion())) {
					// Invalid response
					log.warn("Invalid version received, ignoring.");
					return;
				}
				
				
				info = new UpdateInfo(version, updates);
				log.info("Found update: " + info);
			} finally {
				try {
					if (is != null)
						is.close();
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
