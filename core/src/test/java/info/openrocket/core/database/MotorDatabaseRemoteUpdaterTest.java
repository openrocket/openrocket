package info.openrocket.core.database;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.communication.ConnectionSourceStub;
import info.openrocket.core.communication.HttpURLConnectionMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class MotorDatabaseRemoteUpdaterTest {

	@TempDir
	Path tempDir;

	@Test
	public void testInstallAcceptsGzipSha256() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		String gzSha = sha256Hex(gzBytes);

		MotorDatabaseMetadata metadata = signedMetadata(123, gzSha, keyPair.getPrivate());

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(new ConnectionSourceStub(connection),
				keyPair.getPublic());
		updater.installRemoteDatabase(tempDir.toFile(), metadata, "https://openrocket.info/motor-database/motors.db.gz");

		byte[] installed = Files.readAllBytes(new File(tempDir.toFile(), "motors.db").toPath());
		assertArrayEquals(dbBytes, installed);
	}

	@Test
	public void testInstallRejectsDbSha256WhenSha256GzDoesNotMatch() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		String dbSha = sha256Hex(dbBytes);

		MotorDatabaseMetadata metadata = signedMetadata(123, dbSha, keyPair.getPrivate());

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(new ConnectionSourceStub(connection),
				keyPair.getPublic());
		assertThrows(Exception.class,
				() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
						"https://openrocket.info/motor-database/motors.db.gz"));
	}

	@Test
	public void testInstallRejectsMismatchedSha256() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);

		MotorDatabaseMetadata metadata = signedMetadata(
				123,
				"0000000000000000000000000000000000000000000000000000000000000000",
				keyPair.getPrivate());

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(new ConnectionSourceStub(connection),
				keyPair.getPublic());
		assertThrows(Exception.class,
				() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
						"https://openrocket.info/motor-database/motors.db.gz"));
	}

	@Test
	public void testInstallRejectsMissingSignature() throws Exception {
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		String gzSha = sha256Hex(gzBytes);

		MotorDatabaseMetadata metadata = MotorDatabaseMetadata.parse(new ByteArrayInputStream((
				"{\"schema_version\":2,\"database_version\":123,\"sha256_gz\":\"" + gzSha + "\"}").getBytes(StandardCharsets.UTF_8)));

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);

		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(new ConnectionSourceStub(connection),
				keyPair.getPublic());
		assertThrows(Exception.class,
				() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
						"https://openrocket.info/motor-database/motors.db.gz"));
	}

	@Test
	public void testInstallDoesNotDrainClosedStream() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		String gzSha = sha256Hex(gzBytes);

		MotorDatabaseMetadata metadata = signedMetadata(123, gzSha, keyPair.getPrivate());

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(
				url -> new StrictHttpURLConnection(new URL(url), gzBytes),
				keyPair.getPublic());

		updater.installRemoteDatabase(tempDir.toFile(), metadata,
				"https://openrocket.info/motor-database/motors.db.gz");

		byte[] installed = Files.readAllBytes(new File(tempDir.toFile(), "motors.db").toPath());
		assertArrayEquals(dbBytes, installed);
	}

	@Test
	public void testMetadataFetchEnforcesTotalDeadline() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		MotorDatabaseMetadata metadata = signedMetadata(
				123,
				"0000000000000000000000000000000000000000000000000000000000000000",
				keyPair.getPrivate());

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(metadata.getRawJson());
		connection.setConnectionDelay(25);

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(
				new ConnectionSourceStub(connection), keyPair.getPublic(), 1, 1_000);

		assertThrows(SocketTimeoutException.class, updater::fetchRemoteMetadata);
	}

	@Test
	public void testInstallRejectsConcurrentInstaller() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		MotorDatabaseMetadata metadata = signedMetadata(123, sha256Hex(gzBytes), keyPair.getPrivate());

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);
		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(
				new ConnectionSourceStub(connection), keyPair.getPublic());

		Path lockPath = tempDir.resolve(".motor-database-update.lock");
		try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			 FileLock ignored = channel.lock()) {
			IOException error = assertThrows(IOException.class,
					() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
							"https://openrocket.info/motor-database/motors.db.gz"));
			assertTrue(error.getMessage().contains("already running"));
		}
	}

	@Test
	public void testInstallRejectsStaleVersionAtCommit() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		MotorDatabaseMetadata metadata = signedMetadata(123, sha256Hex(gzBytes), keyPair.getPrivate());
		Files.writeString(tempDir.resolve("metadata.json"), "{\"database_version\":124}");

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);
		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(
				new ConnectionSourceStub(connection), keyPair.getPublic());

		IOException error = assertThrows(IOException.class,
				() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
						"https://openrocket.info/motor-database/motors.db.gz"));
		assertTrue(error.getMessage().contains("Refusing stale motor database update"));
		assertNoDownloadFiles();
	}

	@Test
	public void testDatabaseIsRolledBackWhenMetadataCommitFails() throws Exception {
		KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		byte[] dbBytes = createValidMotorDbBytes();
		byte[] gzBytes = gzip(dbBytes);
		MotorDatabaseMetadata metadata = signedMetadata(123, sha256Hex(gzBytes), keyPair.getPrivate());
		byte[] originalDatabase = "existing database".getBytes(StandardCharsets.UTF_8);
		Files.write(tempDir.resolve("motors.db"), originalDatabase);
		Files.createDirectory(tempDir.resolve("metadata.json"));
		Files.writeString(tempDir.resolve("metadata.json").resolve("block-replacement"), "keep");

		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		connection.setDoInput(true);
		connection.setResponseCode(200);
		connection.setContent(gzBytes);
		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater(
				new ConnectionSourceStub(connection), keyPair.getPublic());

		assertThrows(IOException.class,
				() -> updater.installRemoteDatabase(tempDir.toFile(), metadata,
						"https://openrocket.info/motor-database/motors.db.gz"));
		assertArrayEquals(originalDatabase, Files.readAllBytes(tempDir.resolve("motors.db")));
		assertNoDownloadFiles();
	}

	private void assertNoDownloadFiles() throws IOException {
		try (Stream<Path> files = Files.list(tempDir)) {
			assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".download")));
		}
	}

	private static byte[] gzip(byte[] content) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (GZIPOutputStream gzOut = new GZIPOutputStream(bout)) {
			gzOut.write(content);
		}
		return bout.toByteArray();
	}

	private byte[] createValidMotorDbBytes() throws Exception {
		Class.forName("org.sqlite.JDBC");
		File dbFile = tempDir.resolve("motors.db").toFile();
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
			 Statement stmt = connection.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
			stmt.execute("CREATE TABLE meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
			stmt.execute("INSERT INTO meta (key, value) VALUES ('schema_version', '2')");

			stmt.execute("CREATE TABLE manufacturers (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"name TEXT NOT NULL UNIQUE, " +
					"abbrev TEXT" +
					")");

			stmt.execute("CREATE TABLE motors (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"manufacturer_id INTEGER NOT NULL, " +
					"tc_motor_id TEXT, " +
					"designation TEXT NOT NULL, " +
					"common_name TEXT, " +
					"impulse_class TEXT, " +
					"diameter REAL, " +
					"length REAL, " +
					"total_impulse REAL, " +
					"avg_thrust REAL, " +
					"max_thrust REAL, " +
					"burn_time REAL, " +
					"propellant_weight REAL, " +
					"total_weight REAL, " +
					"type TEXT, " +
					"delays TEXT, " +
					"case_info TEXT, " +
					"prop_info TEXT, " +
					"sparky INTEGER, " +
					"info_url TEXT, " +
					"data_files INTEGER, " +
					"updated_on TEXT, " +
					"FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id)" +
					")");

			stmt.execute("CREATE TABLE thrust_curves (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"motor_id INTEGER NOT NULL, " +
					"tc_simfile_id TEXT, " +
					"source TEXT, " +
					"format TEXT, " +
					"license TEXT, " +
					"info_url TEXT, " +
					"data_url TEXT, " +
					"total_impulse REAL, " +
					"avg_thrust REAL, " +
					"max_thrust REAL, " +
					"burn_time REAL, " +
					"FOREIGN KEY (motor_id) REFERENCES motors(id) ON DELETE CASCADE" +
					")");

			stmt.execute("CREATE TABLE thrust_data (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"curve_id INTEGER NOT NULL, " +
					"time_seconds REAL NOT NULL, " +
					"force_newtons REAL NOT NULL, " +
					"FOREIGN KEY (curve_id) REFERENCES thrust_curves(id) ON DELETE CASCADE" +
					")");
		}

		return Files.readAllBytes(dbFile.toPath());
	}

	private static MotorDatabaseMetadata signedMetadata(long databaseVersion, String sha256Gz, PrivateKey privateKey)
			throws Exception {
		String canonicalMessage = "openrocket-motordb-v1\n" + databaseVersion + "\n" + sha256Gz + "\n";
		Signature signature = Signature.getInstance("Ed25519");
		signature.initSign(privateKey);
		signature.update(canonicalMessage.getBytes(StandardCharsets.UTF_8));
		String sigB64 = Base64.getEncoder().encodeToString(signature.sign());

		String json = "{\"schema_version\":2,\"database_version\":" + databaseVersion +
				",\"sha256_gz\":\"" + sha256Gz + "\",\"sig\":\"" + sigB64 + "\"}";
		return MotorDatabaseMetadata.parse(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
	}

	private static String sha256Hex(byte[] content) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(content);
		StringBuilder sb = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	private static final class StrictHttpURLConnection extends HttpURLConnection {
		private final byte[] content;
		private final Map<String, String> headers = new HashMap<>();
		private boolean connected;

		StrictHttpURLConnection(URL url, byte[] content) {
			super(url);
			this.content = content;
		}

		@Override
		public void connect() {
			connected = true;
		}

		@Override
		public void disconnect() {
			connected = false;
		}

		@Override
		public boolean usingProxy() {
			return false;
		}

		@Override
		public int getResponseCode() {
			return 200;
		}

		@Override
		public String getHeaderField(String name) {
			return headers.get(name);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!connected) {
				connect();
			}
			return new InputStream() {
				private int idx;
				private boolean closed;

				@Override
				public int read() throws IOException {
					if (closed) {
						throw new IOException("stream is closed");
					}
					if (idx >= content.length) {
						return -1;
					}
					return content[idx++] & 0xff;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (closed) {
						throw new IOException("stream is closed");
					}
					if (idx >= content.length) {
						return -1;
					}
					int n = Math.min(len, content.length - idx);
					System.arraycopy(content, idx, b, off, n);
					idx += n;
					return n;
				}

				@Override
				public void close() {
					closed = true;
				}
			};
		}
	}
}
