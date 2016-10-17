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
	 * @throws	IllegalStateException if {@link #startFetchUpdateInfo()} has not been called
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
		BufferedReader reader = convertToBufferedReader(r);
		String version = null;
		
		ArrayList<ComparablePair<Integer, String>> updates =
				new ArrayList<ComparablePair<Integer, String>>();
		
		String str = reader.readLine();
		while (str != null) {
			if (isHeader(str)) {
				version = str.substring(8).trim();
			} else if (isUpdateToken(str)) {
				ComparablePair<Integer, String> update = parseUpdateToken(str);
				if(update != null)
					updates.add(update);
			}
			str = reader.readLine();
		}
		
		if (version == null) 
			return null;
		return new UpdateInfo(version, updates);
	}
	
	/**
	 * parses a line of a connection content into the information of an update
	 * @param str	the line of the connection
	 * @return		the update information
	 */
	private static ComparablePair<Integer, String> parseUpdateToken(String str){
		int index = str.indexOf(':');
		int value = Integer.parseInt(str.substring(0, index));
		String desc = str.substring(index + 1).trim();
		
		if (desc.equals("")) 
			return null;
		return new ComparablePair<Integer, String>(value, desc);
	}

	/**
	 * checks if a string contains and update information
	 * @param str	the string itself
	 * @return		true for when the string has an update
	 * 				false otherwise
	 */
	private static boolean isUpdateToken(String str) {
		return str.matches("^[0-9]+:\\p{Print}+$");
	}

	/**
	 * check if the string is formated as an update list header
	 * @param str	the string to be checked
	 * @return		true if str is a header, false otherwise
	 */
	private static boolean isHeader(String str) {
		return str.matches("^Version: *[0-9]+\\.[0-9]+\\.[0-9]+[a-zA-Z0-9.-]* *$");
	}
	
	/**
	 * convert, if not yet converted, a Reader into a buffered reader
	 * @param r		the Reader object
	 * @return		the Reader as a BufferedReader Object
	 */
	private static BufferedReader convertToBufferedReader(Reader r) {
		if (r instanceof BufferedReader) 
			return (BufferedReader) r;
		return new BufferedReader(r);
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
		
		/**
		 * Establishes a connection with data of previous updates
		 * @throws IOException
		 */
		private void doConnection() throws IOException {
			HttpURLConnection connection = getConnection(getUrl());
			InputStream is = null;
			
			try {
				connection.connect();
				if(!checkConnection(connection))
					return;
				if(!checkContentType(connection))
					return;
				is = new LimitedInputStream(connection.getInputStream(), Communicator.MAX_INPUT_BYTES);
				parseUpdateInput(buildBufferedReader(connection,is));
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
		
		/**
		 * Parses the data received in a buffered reader
		 * @param reader		The reader object
		 * @throws IOException	If anything bad happens
		 */
		private void parseUpdateInput(BufferedReader reader) throws IOException{
			String version = null;
			ArrayList<ComparablePair<Integer, String>> updates =
					new ArrayList<ComparablePair<Integer, String>>();
			
			String line = reader.readLine();
			while (line != null) {
				if (isHeader(line)) {
					version = parseHeader(line);
				} else if (isUpdateInfo(line)) {
					updates.add(parseUpdateInfo(line));
				}
				line = reader.readLine();
			}
			
			if (isInvalidVersion(version)) {
				log.warn("Invalid version received, ignoring.");
				return;
			}
			
			info = new UpdateInfo(version, updates);
			log.info("Found update: " + info);
		}

		/**
		 * parses a line into it's version name
		 * @param 	line	the string of the header
		 * @return	the version in it's right format
		 */
		private String parseHeader(String line) {
			return line.substring(8).trim();
		}
		
		/**
		 * parses a line into it's correspondent update information
		 * @param line	the line to be parsed
		 * @return		update information from the line
		 */
		private ComparablePair<Integer,String> parseUpdateInfo(String line){
			String[] split = line.split(":", 2);
			int n = Integer.parseInt(split[0]);
			return new ComparablePair<Integer, String>(n, split[1].trim());
		}

		/**
		 * checks if a line contains an update information
		 * @param 	line	the line to be checked
		 * @return	true if the line caontain an update information
		 * 			false otherwise 
		 */
		private boolean isUpdateInfo(String line) {
			return line.matches("^[0-9]{1,9}:\\P{Cntrl}{1,300}$");
		}

		/**
		 * checks if a line is a header of an update list
		 * @param line	the line to be checked
		 * @return		true if line is a header, false otherwise
		 */
		private boolean isHeader(String line) {
			return line.matches("^Version:[a-zA-Z0-9._ -]{1,30}$");
		}

		/**
		 * checks if a String is a valid version
		 * @param version	the String to be checked
		 * @return			true if it's valid, false otherwise
		 */
		private boolean isInvalidVersion(String version) {
			return version == null || version.length() == 0 ||
					version.equalsIgnoreCase(BuildProperties.getVersion());
		}
		
		/**
		 * builds a buffered reader from an open connection and a stream
		 * @param connection	The connection
		 * @param is			The input stream
		 * @return				The Buffered reader created
		 * @throws IOException
		 */
		private BufferedReader buildBufferedReader(HttpURLConnection connection, InputStream is) throws IOException {
			String encoding = connection.getContentEncoding();
			if (encoding == null || encoding.equals(""))
				encoding = "UTF-8";
			return new BufferedReader(new InputStreamReader(is, encoding));
		}

		/**
		 * check if the content of a connection is valid
		 * @param connection	the connection to be checked
		 * @return				true if the content is valid, false otherwise
		 */
		private boolean checkContentType(HttpURLConnection connection) {
			String contentType = connection.getContentType();
			if (contentType == null ||
					contentType.toLowerCase(Locale.ENGLISH).indexOf(Communicator.UPDATE_INFO_CONTENT_TYPE) < 0) {
				// Unknown response type
				log.warn("Unknown Content-type received:" + contentType);
				return false;
			}
			return true;
		}

		/**
		 * check if a connection is responsive and valid
		 * @param connection	the connection to be checked
		 * @return				true if connection is ok, false otherwise
		 * @throws IOException
		 */
		private boolean checkConnection(HttpURLConnection connection) throws IOException{
			log.debug("Update response code: " + connection.getResponseCode());
			
			if (noUpdatesAvailable(connection)) {
				log.info("No updates available");
				info = new UpdateInfo();
				return false;
			}
			
			if (!updateAvailable(connection)) {
				// Error communicating with server
				log.warn("Unknown server response code: " + connection.getResponseCode());
				return false;
			}
			return true;
		}

		/**
		 * checks if a connection sent an update available flag
		 * @param connection	the connection to be checked
		 * @return				true if the response was an update available flag
		 * 						false otherwise
		 * @throws IOException	if anything goes wrong
		 */
		private boolean updateAvailable(HttpURLConnection connection) throws IOException {
			return connection.getResponseCode() == Communicator.UPDATE_INFO_UPDATE_AVAILABLE;
		}

		/**
		 * checks if a connection sent an update unavailable flag
		 * @param connection	the connection to be checked
		 * @return				true if the response was an no update available flag
		 * 						false otherwise
		 * @throws IOException	if anything goes wrong
		 */
		private boolean noUpdatesAvailable(HttpURLConnection connection) throws IOException {
			return connection.getResponseCode() == Communicator.UPDATE_INFO_NO_UPDATE_CODE;
		}

		/**
		 * Builds a connection with the given url
		 * @param url	the url
		 * @return		connection base on the url
		 * @throws IOException
		 */
		private HttpURLConnection getConnection(String url) throws IOException{
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
			return connection;
		}
		
		/**
		 * builds the default url for fetching updates
		 * @return	the string with an url for fethcing updates
		 */
		private String getUrl() {
			return Communicator.UPDATE_INFO_URL + "?" + Communicator.VERSION_PARAM + "="
					+ Communicator.encode(BuildProperties.getVersion());
		}
	}
}
