package info.openrocket.core.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Parsed metadata for the motor database.
 */
public class MotorDatabaseMetadata {
	private static final String KEY_SCHEMA_VERSION = "schema_version";
	private static final String KEY_DATABASE_VERSION = "database_version";
	private static final String KEY_SHA256 = "sha256";
	private static final String KEY_SHA256_GZ = "sha256_gz";
	private static final String KEY_DOWNLOAD_URL = "download_url";
	private static final String KEY_GENERATED_AT = "generated_at";
	private static final String KEY_SIGNATURE = "sig";
	private static final String KEY_KEY_ID = "key_id";

	private final int schemaVersion;
	private final long databaseVersion;
	private final String sha256;
	private final String sha256Gz;
	private final String downloadUrl;
	private final String generatedAt;
	private final String signature;
	private final String keyId;
	private final String rawJson;

	private MotorDatabaseMetadata(int schemaVersion, long databaseVersion, String sha256, String sha256Gz,
			String downloadUrl, String generatedAt, String signature, String keyId, String rawJson) {
		this.schemaVersion = schemaVersion;
		this.databaseVersion = databaseVersion;
		this.sha256 = sha256;
		this.sha256Gz = sha256Gz;
		this.downloadUrl = downloadUrl;
		this.generatedAt = generatedAt;
		this.signature = signature;
		this.keyId = keyId;
		this.rawJson = rawJson;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public long getDatabaseVersion() {
		return databaseVersion;
	}

	public String getSha256() {
		return sha256;
	}

	/**
	 * SHA-256 of the downloaded {@code motors.db.gz} bytes (hex).
	 */
	public String getSha256Gz() {
		return sha256Gz;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getGeneratedAt() {
		return generatedAt;
	}

	/**
	 * Base64-encoded Ed25519 signature over the canonical update message.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Optional identifier for the signing key (for key rotation).
	 */
	public String getKeyId() {
		return keyId;
	}

	public String getRawJson() {
		return rawJson;
	}

	public static MotorDatabaseMetadata parse(InputStream is) throws IOException {
		String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();

		if (!json.has(KEY_DATABASE_VERSION)) {
			throw new IOException("metadata.json does not contain " + KEY_DATABASE_VERSION);
		}

		int schemaVersion = json.has(KEY_SCHEMA_VERSION) ? json.get(KEY_SCHEMA_VERSION).getAsInt() : 0;
		long databaseVersion = json.get(KEY_DATABASE_VERSION).getAsLong();
		String sha256 = json.has(KEY_SHA256) ? json.get(KEY_SHA256).getAsString() : null;
		String sha256Gz = json.has(KEY_SHA256_GZ) ? json.get(KEY_SHA256_GZ).getAsString() : null;
		String downloadUrl = json.has(KEY_DOWNLOAD_URL) ? json.get(KEY_DOWNLOAD_URL).getAsString() : null;
		String generatedAt = json.has(KEY_GENERATED_AT) ? json.get(KEY_GENERATED_AT).getAsString() : null;
		String signature = json.has(KEY_SIGNATURE) ? json.get(KEY_SIGNATURE).getAsString() : null;
		String keyId = json.has(KEY_KEY_ID) ? json.get(KEY_KEY_ID).getAsString() : null;

		return new MotorDatabaseMetadata(schemaVersion, databaseVersion, sha256, sha256Gz, downloadUrl, generatedAt,
				signature, keyId, jsonContent);
	}
}
