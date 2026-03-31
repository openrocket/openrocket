package info.openrocket.core.database;

import info.openrocket.core.communication.ConnectionSource;
import info.openrocket.core.communication.DefaultConnectionSource;
import info.openrocket.core.database.motor.ThrustCurveMotorSQLiteDatabase;
import info.openrocket.core.util.FileUtils;
import info.openrocket.core.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Utility class to fetch and install the remote motor database.
 * <p>
 * Security properties and assumptions:
 * <ul>
 *   <li><b>Transport:</b> Uses HTTPS endpoints and refuses redirects to non-HTTPS URLs or different hosts.</li>
 *   <li><b>Integrity:</b> Verifies the downloaded {@code motors.db.gz} against the SHA-256 published in the metadata
 *       field {@code sha256_gz}. If the metadata contains a missing or invalid SHA-256 value, installation is aborted.</li>
 *   <li><b>Authenticity:</b> Requires a valid Ed25519 signature in {@code metadata.json} and verifies it using an
 *       embedded public key. This provides offline authenticity independent of TLS.</li>
 *   <li><b>Validation:</b> Always validates the downloaded SQLite database schema before replacing the local database.</li>
 *   <li><b>Resource limits:</b> Enforces hard byte limits on downloaded metadata, downloaded compressed data, and
 *       decompressed database size to reduce risk from oversized downloads or decompression bombs.</li>
 * </ul>
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class MotorDatabaseRemoteUpdater {
	private static final Logger log = LoggerFactory.getLogger(MotorDatabaseRemoteUpdater.class);

	public static final String METADATA_URL = "https://openrocket.info/motor-database/metadata.json";
	public static final String DATABASE_GZ_URL = "https://openrocket.info/motor-database/motors.db.gz";

	/**
	 * Ed25519 public key (X.509 SubjectPublicKeyInfo), base64-encoded.
	 */
	private static final String SIGNING_PUBLIC_KEY_SPKI_BASE64 =
			"MCowBQYDK2VwAyEA6CLrOzMhnpYon+H01z3uzRp6sjEHii608GdtTbCLRGs=";
	private static final String SIGNATURE_MESSAGE_PREFIX = "openrocket-motordb-v1\n";

	private static final Set<String> ALLOWED_REMOTE_HOSTS = Set.of(
			"openrocket.info",
			"openrocket.github.io"
	);

	private static final int CONNECT_TIMEOUT_MS = 10_000;
	private static final int READ_TIMEOUT_MS = 30_000;

	// Safety limits against unexpectedly large downloads/decompression bombs.
	private static final long MAX_METADATA_BYTES = 128 * 1024;
	private static final long MAX_DB_GZ_BYTES = 50L * 1024 * 1024;
	private static final long MAX_DB_BYTES = 200L * 1024 * 1024;

	private static final String MOTORS_DB_FILENAME = "motors.db";
	private static final String METADATA_FILENAME = "metadata.json";

	private final ConnectionSource connectionSource;
	private final PublicKey trustedSigningKey;

	public MotorDatabaseRemoteUpdater() {
		this(new DefaultConnectionSource(), defaultSigningKey());
	}

	public MotorDatabaseRemoteUpdater(ConnectionSource connectionSource) {
		this(connectionSource, defaultSigningKey());
	}

	public MotorDatabaseRemoteUpdater(ConnectionSource connectionSource, PublicKey trustedSigningKey) {
		this.connectionSource = Objects.requireNonNull(connectionSource, "connectionSource");
		this.trustedSigningKey = Objects.requireNonNull(trustedSigningKey, "trustedSigningKey");
	}

	/**
	 * Fetch the remote motor database metadata from the default URL.
	 * @return the remote motor database metadata
	 * @throws IOException if an I/O error occurs
	 */
	public MotorDatabaseMetadata fetchRemoteMetadata() throws IOException {
		return fetchRemoteMetadata(METADATA_URL);
	}

	/**
	 * Fetch the remote motor database metadata from the given URL.
	 * @param metadataUrl the URL of the remote motor database metadata
	 * @return the remote motor database metadata
	 * @throws IOException if an I/O error occurs
	 */
	public MotorDatabaseMetadata fetchRemoteMetadata(String metadataUrl) throws IOException {
		try (InputStream in = openStream(metadataUrl);
			 InputStream limited = new ThrowingLimitedInputStream(in, MAX_METADATA_BYTES)) {
			MotorDatabaseMetadata metadata = MotorDatabaseMetadata.parse(limited);
			verifySignedMetadata(metadata);
			return metadata;
		}
	}

	/**
	 * Check if the remote motor database is newer than the local one.
	 * @param motorLibraryDir the motor library directory
	 * @param remoteMetadata the remote motor database metadata
	 * @return true if the remote database is newer, false otherwise
	 */
	public boolean isRemoteNewer(File motorLibraryDir, MotorDatabaseMetadata remoteMetadata) {
		if (motorLibraryDir == null || remoteMetadata == null) {
			return false;
		}

		File localMetadataFile = new File(motorLibraryDir, METADATA_FILENAME);
		long localVersion = 0;
		if (localMetadataFile.isFile()) {
			try {
				localVersion = MotorDatabaseMetadataIO.readDatabaseVersion(localMetadataFile);
			} catch (IOException e) {
				log.debug("Unable to read local motor database metadata from {}: {}",
						localMetadataFile.getAbsolutePath(), e.getMessage());
			}
		}

		return remoteMetadata.getDatabaseVersion() > localVersion;
	}

	/**
	 * Install the remote motor database into the given motor library directory.
	 * @param motorLibraryDir the motor library directory
	 * @param remoteMetadata the remote motor database metadata
	 * @throws IOException if an I/O error occurs
	 */
	public void installRemoteDatabase(File motorLibraryDir, MotorDatabaseMetadata remoteMetadata) throws IOException {
		String url = DATABASE_GZ_URL;
		if (remoteMetadata != null && remoteMetadata.getDownloadUrl() != null &&
				!remoteMetadata.getDownloadUrl().trim().isEmpty()) {
			url = remoteMetadata.getDownloadUrl().trim();
		}
		installRemoteDatabase(motorLibraryDir, remoteMetadata, url);
	}

	/**
	 * Install the remote motor database into the given motor library directory.
	 * @param motorLibraryDir the motor library directory
	 * @param remoteMetadata the remote motor database metadata
	 * @param databaseGzUrl the URL of the remote motor database GZIP archive
	 * @throws IOException if an I/O error occurs
	 */
	public void installRemoteDatabase(File motorLibraryDir, MotorDatabaseMetadata remoteMetadata, String databaseGzUrl)
			throws IOException {
		if (motorLibraryDir == null) {
			throw new IOException("Motor library directory is null");
		}
		if (!motorLibraryDir.exists() && !motorLibraryDir.mkdirs()) {
			throw new IOException("Unable to create motor library directory: " + motorLibraryDir.getAbsolutePath());
		}
		if (remoteMetadata == null) {
			throw new IOException("Remote metadata is null");
		}
		verifySignedMetadata(remoteMetadata);
		validateDatabaseDownloadUrl(databaseGzUrl);

		File targetDbFile = new File(motorLibraryDir, MOTORS_DB_FILENAME);
		File targetMetadataFile = new File(motorLibraryDir, METADATA_FILENAME);

		File tmpDbFile = new File(motorLibraryDir, MOTORS_DB_FILENAME + ".download");
		File tmpMetadataFile = new File(motorLibraryDir, METADATA_FILENAME + ".download");

		log.info("Downloading motor database from {}", databaseGzUrl);
		String rawSha256Gz = remoteMetadata.getSha256Gz();
		String expectedGzipSha256 = normalizeSha256(rawSha256Gz);
		if (expectedGzipSha256 == null) {
			throw new IOException("Remote metadata missing or invalid sha256_gz");
		}

		try (InputStream rawIn0 = openStream(databaseGzUrl);
			 InputStream rawIn = new ThrowingLimitedInputStream(rawIn0, MAX_DB_GZ_BYTES);
			 FileOutputStream out = new FileOutputStream(tmpDbFile)) {

			MessageDigest gzipDigest = sha256Digest();
			MessageDigest dbDigest = sha256Digest();
			String actualGzipSha256;
			String actualDbSha256;

			try (DigestInputStream gzipDigestStream = new DigestInputStream(rawIn, gzipDigest);
				 GZIPInputStream gzIn = new GZIPInputStream(nonClosing(gzipDigestStream));
				 DigestInputStream dbDigestStream = new DigestInputStream(gzIn, dbDigest);
				 InputStream dbLimited = new ThrowingLimitedInputStream(dbDigestStream, MAX_DB_BYTES)) {
				FileUtils.copy(dbLimited, out);
				// Ensure we hash the full response body for the gzip digest (not only what the decompressor consumed).
				drain(gzipDigestStream);
			}

			actualGzipSha256 = TextUtil.bytesToHex(gzipDigest.digest());
			actualDbSha256 = TextUtil.bytesToHex(dbDigest.digest());

			if (!expectedGzipSha256.equals(actualGzipSha256)) {
				throw new IOException("Downloaded motor database SHA-256 mismatch (expected sha256_gz=" +
						expectedGzipSha256 + ", got sha256_gz=" + actualGzipSha256 +
						", sha256_db=" + actualDbSha256 + ")");
			}

			// Always validate the downloaded DB before replacing the local one (even if SHA is missing).
			validateDownloadedDatabase(tmpDbFile);
		} catch (IOException e) {
			if (tmpDbFile.isFile() && !tmpDbFile.delete()) {
				log.debug("Unable to delete partial download {}", tmpDbFile.getAbsolutePath());
			}
			throw e;
		}

		MotorDatabaseMetadataIO.writeRawJson(tmpMetadataFile, remoteMetadata.getRawJson());

		FileUtils.replaceFile(tmpDbFile, targetDbFile);
		FileUtils.replaceFile(tmpMetadataFile, targetMetadataFile);
		log.info("Installed updated motor database (database_version={})", remoteMetadata.getDatabaseVersion());
	}

	/**
	 * Open an InputStream to the given URL.
	 * @param url the URL
	 * @return the InputStream
	 * @throws IOException if an I/O error occurs
	 */
	private InputStream openStream(String url) throws IOException {
		validateHttpsUrl(url);
		final int maxRedirects = 3;
		String current = url;

		for (int redirects = 0; redirects <= maxRedirects; redirects++) {
			HttpURLConnection connection = connectionSource.getConnection(current);
			connection.setInstanceFollowRedirects(false);
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);
			connection.setRequestProperty("User-Agent", "OpenRocket");
			connection.setRequestProperty("Accept", "application/json, */*");
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.connect();

			int code = connection.getResponseCode();
			if (code >= 200 && code < 300) {
				return connection.getInputStream();
			}
			if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
				String location = connection.getHeaderField("Location");
				if (location == null || location.trim().isEmpty()) {
					throw new IOException("HTTP " + code + " without Location header when fetching " + current);
				}
				String next = resolveRedirect(current, location);
				validateRedirect(url, next);
				current = next;
				continue;
			}
			throw new IOException("HTTP " + code + " when fetching " + current);
		}

		throw new IOException("Too many redirects when fetching " + url);
	}

	private static void validateHttpsUrl(String url) throws IOException {
		try {
			URI uri = new URI(url);
			if (!"https".equalsIgnoreCase(uri.getScheme())) {
				throw new IOException("Refusing non-HTTPS URL: " + url);
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Invalid URL: " + url, e);
		}
	}

	private static void validateDatabaseDownloadUrl(String url) throws IOException {
		validateHttpsUrl(url);
		try {
			URI uri = new URI(url);
			String host = uri.getHost();
			if (host == null) {
				throw new IOException("Database download URL missing host: " + url);
			}
			String normalizedHost = host.toLowerCase(Locale.ENGLISH);
			if (!ALLOWED_REMOTE_HOSTS.contains(normalizedHost)) {
				throw new IOException("Refusing database download from unexpected host: " + normalizedHost);
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Invalid database download URL: " + url, e);
		}
	}

	private static PublicKey defaultSigningKey() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(SIGNING_PUBLIC_KEY_SPKI_BASE64);
			KeyFactory factory = KeyFactory.getInstance("Ed25519");
			return factory.generatePublic(new X509EncodedKeySpec(keyBytes));
		} catch (GeneralSecurityException | IllegalArgumentException e) {
			throw new IllegalStateException("Unable to load embedded motor database signing key", e);
		}
	}

	private void verifySignedMetadata(MotorDatabaseMetadata metadata) throws IOException {
		if (metadata == null) {
			throw new IOException("Remote metadata is null");
		}
		if (metadata.getDatabaseVersion() <= 0) {
			throw new IOException("Remote metadata has an invalid database_version");
		}

		String sha256Gz = normalizeSha256(metadata.getSha256Gz());
		if (sha256Gz == null) {
			throw new IOException("Remote metadata missing or invalid sha256_gz");
		}

		String sigB64 = metadata.getSignature();
		if (sigB64 == null || sigB64.trim().isEmpty()) {
			throw new IOException("Remote metadata missing signature");
		}

		byte[] sigBytes;
		try {
			sigBytes = Base64.getDecoder().decode(sigB64.trim());
		} catch (IllegalArgumentException e) {
			throw new IOException("Remote metadata signature is not valid base64", e);
		}

		String canonicalMessage = SIGNATURE_MESSAGE_PREFIX +
				Long.toString(metadata.getDatabaseVersion()) + "\n" +
				sha256Gz + "\n";

		try {
			Signature sig = Signature.getInstance("Ed25519");
			sig.initVerify(trustedSigningKey);
			sig.update(canonicalMessage.getBytes(StandardCharsets.UTF_8));
			if (!sig.verify(sigBytes)) {
				throw new IOException("Remote metadata signature verification failed");
			}
		} catch (GeneralSecurityException e) {
			throw new IOException("Unable to verify remote metadata signature", e);
		}
	}

	/**
	 * Create a SHA-256 MessageDigest instance.
	 * @return the MessageDigest
	 * @throws IOException if SHA-256 is not supported
	 */
	private static MessageDigest sha256Digest() throws IOException {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("SHA-256 digest not supported by JRE", e);
		}
	}

	/**
	 * Normalize a SHA-256 string: trim whitespace and convert to lowercase.
	 * @param sha256 the SHA-256 string
	 * @return the normalized string, or null if the input was null or empty
	 */
	private static String normalizeSha256(String sha256) {
		if (sha256 == null) {
			return null;
		}
		String trimmed = sha256.trim().toLowerCase(Locale.ENGLISH);
		if (trimmed.isEmpty()) {
			return null;
		}
		// Enforce expected format so we don't accept garbage.
		if (!trimmed.matches("^[0-9a-f]{64}$")) {
			return null;
		}
		return trimmed;
	}

	private static void validateDownloadedDatabase(File dbFile) throws IOException {
		try {
			ThrustCurveMotorSQLiteDatabase.validateDatabase(dbFile);
		} catch (SQLException e) {
			throw new IOException("Downloaded motor database failed validation: " + e.getMessage(), e);
		}
	}

	private static void drain(InputStream in) throws IOException {
		byte[] buffer = new byte[8192];
		while (in.read(buffer) != -1) {
			// drain
		}
	}

	private static InputStream nonClosing(InputStream in) {
		return new java.io.FilterInputStream(in) {
			@Override
			public void close() {
				// Do not close the underlying stream; the caller owns it.
			}
		};
	}

	private static String resolveRedirect(String currentUrl, String location) throws IOException {
		try {
			URL base = new URL(currentUrl);
			URL resolved = new URL(base, location);
			return resolved.toString();
		} catch (Exception e) {
			throw new IOException("Invalid redirect Location: " + location, e);
		}
	}

	private static void validateRedirect(String originalUrl, String redirectedUrl) throws IOException {
		try {
			URI next = new URI(redirectedUrl);
			if (!"https".equalsIgnoreCase(next.getScheme())) {
				throw new IOException("Refusing redirect to non-HTTPS URL: " + redirectedUrl);
			}
			// Only allow redirects to well-known, allowlisted hosts.
			String host = next.getHost();
			if (host == null) {
				throw new IOException("Refusing redirect without host: " + redirectedUrl);
			}
			String normalizedHost = host.toLowerCase(Locale.ENGLISH);
			if (!ALLOWED_REMOTE_HOSTS.contains(normalizedHost)) {
				throw new IOException("Refusing redirect to unexpected host: " + normalizedHost);
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Invalid redirect URL: " + redirectedUrl, e);
		}
	}

	private static class ThrowingLimitedInputStream extends InputStream {
		private final InputStream delegate;
		private long remaining;

		ThrowingLimitedInputStream(InputStream delegate, long maxBytes) {
			this.delegate = delegate;
			this.remaining = maxBytes;
		}

		@Override
		public int read() throws IOException {
			if (remaining <= 0) {
				throw new IOException("Input exceeded maximum size");
			}
			int r = delegate.read();
			if (r >= 0) {
				remaining--;
			}
			return r;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (remaining <= 0) {
				throw new IOException("Input exceeded maximum size");
			}
			int toRead = (int) Math.min(len, Math.min(Integer.MAX_VALUE, remaining));
			int r = delegate.read(b, off, toRead);
			if (r > 0) {
				remaining -= r;
			}
			return r;
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}
}
