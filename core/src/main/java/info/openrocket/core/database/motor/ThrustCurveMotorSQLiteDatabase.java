package info.openrocket.core.database.motor;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ThrustCurveMotorSQLiteDatabase {
	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSQLiteDatabase.class);
	private static final int SCHEMA_VERSION = 1;
	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	private static final List<String> REQUIRED_COLUMNS = Arrays.asList(
			"manufacturer",
			"code",
			"common_name",
			"designation",
			"description",
			"motor_type",
			"diameter",
			"length",
			"case_info",
			"propellant_info",
			"initial_mass",
			"digest",
			"available",
			"delays",
			"time_points",
			"thrust_points",
			"cg_points");

	private ThrustCurveMotorSQLiteDatabase() {
	}

	public static void writeDatabase(File dbFile, List<ThrustCurveMotor> motors) throws IOException, SQLException {
		if (dbFile == null) {
			throw new IllegalArgumentException("dbFile cannot be null");
		}
		if (motors == null) {
			throw new IllegalArgumentException("motors cannot be null");
		}
		ensureParentDirectory(dbFile);
		if (dbFile.exists() && !dbFile.delete()) {
			throw new IOException("Unable to delete existing SQLite database: " + dbFile.getAbsolutePath());
		}

		try (Connection connection = openConnection(dbFile)) {
			connection.setAutoCommit(false);
			try {
				createSchema(connection);
				writeMetadata(connection);
				insertMotors(connection, motors);
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			}
		}
	}

	public static List<ThrustCurveMotor> readDatabase(File dbFile) throws SQLException {
		if (dbFile == null || !dbFile.isFile()) {
			throw new SQLException("SQLite motor database not found: " + dbFile);
		}

		try (Connection connection = openConnection(dbFile)) {
			validateSchema(connection);
			return readMotors(connection);
		}
	}

	public static void validateDatabase(File dbFile) throws SQLException {
		if (dbFile == null || !dbFile.isFile()) {
			throw new SQLException("SQLite motor database not found: " + dbFile);
		}
		try (Connection connection = openConnection(dbFile)) {
			validateSchema(connection);
		}
	}

	private static Connection openConnection(File dbFile) throws SQLException {
		ensureDriverLoaded();
		return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
	}

	private static void ensureDriverLoaded() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new SQLException("SQLite JDBC driver not found", e);
		}
	}

	private static void ensureParentDirectory(File dbFile) throws IOException {
		File parent = dbFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
		}
	}

	private static void createSchema(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS meta (" +
					"key TEXT PRIMARY KEY, " +
					"value TEXT NOT NULL" +
					")");
			stmt.execute("CREATE TABLE IF NOT EXISTS motors (" +
					"id INTEGER PRIMARY KEY, " +
					"manufacturer TEXT, " +
					"code TEXT, " +
					"common_name TEXT, " +
					"designation TEXT, " +
					"description TEXT, " +
					"motor_type TEXT, " +
					"diameter REAL, " +
					"length REAL, " +
					"case_info TEXT, " +
					"propellant_info TEXT, " +
					"initial_mass REAL, " +
					"digest TEXT, " +
					"available INTEGER, " +
					"delays BLOB, " +
					"time_points BLOB NOT NULL, " +
					"thrust_points BLOB NOT NULL, " +
					"cg_points BLOB NOT NULL" +
					")");
		}
	}

	private static void writeMetadata(Connection connection) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(
				"INSERT INTO meta (key, value) VALUES (?, ?)")) {
			stmt.setString(1, "schema_version");
			stmt.setString(2, Integer.toString(SCHEMA_VERSION));
			stmt.executeUpdate();
		}
	}

	private static void validateSchema(Connection connection) throws SQLException {
		if (!tableExists(connection, "meta")) {
			throw new SQLException("SQLite motor database missing meta table");
		}
		if (!tableExists(connection, "motors")) {
			throw new SQLException("SQLite motor database missing motors table");
		}

		int schemaVersion = readSchemaVersion(connection);
		if (schemaVersion != SCHEMA_VERSION) {
			throw new SQLException("Unsupported thrust curve database schema version: " + schemaVersion);
		}

		Set<String> columns = readMotorColumns(connection);
		Set<String> missing = new HashSet<>();
		for (String required : REQUIRED_COLUMNS) {
			if (!columns.contains(required)) {
				missing.add(required);
			}
		}
		if (!missing.isEmpty()) {
			throw new SQLException("SQLite motor database missing required columns: " + missing);
		}
	}

	private static boolean tableExists(Connection connection, String tableName) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(
				"SELECT name FROM sqlite_master WHERE type='table' AND name = ?")) {
			stmt.setString(1, tableName);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static int readSchemaVersion(Connection connection) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(
				"SELECT value FROM meta WHERE key = ?")) {
			stmt.setString(1, "schema_version");
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new SQLException("SQLite motor database missing schema_version metadata");
				}
				String value = rs.getString(1);
				if (value == null) {
					throw new SQLException("SQLite motor database missing schema_version metadata");
				}
				return Integer.parseInt(value);
			}
		} catch (NumberFormatException e) {
			throw new SQLException("SQLite motor database has invalid schema_version metadata", e);
		}
	}

	private static Set<String> readMotorColumns(Connection connection) throws SQLException {
		Set<String> columns = new HashSet<>();
		try (PreparedStatement stmt = connection.prepareStatement("PRAGMA table_info(motors)");
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String name = rs.getString("name");
				if (name != null) {
					columns.add(name.toLowerCase());
				}
			}
		}
		return columns;
	}

	private static void insertMotors(Connection connection, List<ThrustCurveMotor> motors) throws SQLException {
		String sql = "INSERT INTO motors (" +
				"manufacturer, code, common_name, designation, description, motor_type, " +
				"diameter, length, case_info, propellant_info, initial_mass, digest, " +
				"available, delays, time_points, thrust_points, cg_points" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			int batchCount = 0;
			for (ThrustCurveMotor motor : motors) {
				bindMotor(stmt, motor);
				stmt.addBatch();
				batchCount++;
				if (batchCount % 500 == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		}
	}

	private static void bindMotor(PreparedStatement stmt, ThrustCurveMotor motor) throws SQLException {
		Motor.Type type = motor.getMotorType() == null ? Motor.Type.UNKNOWN : motor.getMotorType();
		stmt.setString(1, safeString(motor.getManufacturer().getDisplayName()));
		stmt.setString(2, safeString(motor.getCode()));
		stmt.setString(3, safeString(motor.getCommonName()));
		stmt.setString(4, safeString(motor.getDesignation()));
		stmt.setString(5, safeString(motor.getDescription()));
		stmt.setString(6, type.name());
		stmt.setDouble(7, motor.getDiameter());
		stmt.setDouble(8, motor.getLength());
		stmt.setString(9, safeString(motor.getCaseInfo()));
		stmt.setString(10, safeString(motor.getPropellantInfo()));
		stmt.setDouble(11, motor.getInitialMass());
		stmt.setString(12, safeString(motor.getDigest()));
		stmt.setInt(13, motor.isAvailable() ? 1 : 0);
		stmt.setBytes(14, encodeDoubleArray(motor.getStandardDelays()));
		stmt.setBytes(15, encodeDoubleArray(motor.getTimePoints()));
		stmt.setBytes(16, encodeDoubleArray(motor.getThrustPoints()));
		stmt.setBytes(17, encodeCoordinates(motor.getCGPoints()));
	}

	private static List<ThrustCurveMotor> readMotors(Connection connection) throws SQLException {
		String sql = "SELECT manufacturer, code, common_name, designation, description, motor_type, " +
				"diameter, length, case_info, propellant_info, initial_mass, digest, " +
				"available, delays, time_points, thrust_points, cg_points " +
				"FROM motors";

		List<ThrustCurveMotor> motors = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				ThrustCurveMotor motor = readMotorRow(rs);
				if (motor != null) {
					motors.add(motor);
				}
			}
		}
		return motors;
	}

	private static ThrustCurveMotor readMotorRow(ResultSet rs) throws SQLException {
		String manufacturer = rs.getString(1);
		String code = rs.getString(2);
		String commonName = rs.getString(3);
		String designation = rs.getString(4);
		String description = rs.getString(5);
		Motor.Type type = parseMotorType(rs.getString(6));
		double diameter = getDoubleOrDefault(rs, 7, 0.0);
		double length = getDoubleOrDefault(rs, 8, 0.0);
		String caseInfo = rs.getString(9);
		String propellantInfo = rs.getString(10);
		double initialMass = getDoubleOrDefault(rs, 11, 0.0);
		String digest = rs.getString(12);
		boolean available = getBooleanOrDefault(rs, 13, true);

		double[] delays;
		double[] timePoints;
		double[] thrustPoints;
		CoordinateIF[] cgPoints;
		try {
			delays = decodeDoubleArray(rs.getBytes(14));
			timePoints = decodeDoubleArray(rs.getBytes(15));
			thrustPoints = decodeDoubleArray(rs.getBytes(16));
			cgPoints = decodeCoordinates(rs.getBytes(17));
		} catch (IllegalArgumentException e) {
			log.warn("Skipping motor with invalid SQLite payload (manufacturer={})", manufacturer, e);
			return null;
		}

		if (timePoints.length == 0 || thrustPoints.length == 0 || cgPoints.length == 0) {
			log.warn("Skipping motor with missing thrust curve data (manufacturer={})", manufacturer);
			return null;
		}
		if (timePoints.length != thrustPoints.length || timePoints.length != cgPoints.length) {
			log.warn("Skipping motor with mismatched data lengths (manufacturer={}, time={}, thrust={}, cg={})",
					manufacturer, timePoints.length, thrustPoints.length, cgPoints.length);
			return null;
		}

		ThrustCurveMotor.Builder builder = new ThrustCurveMotor.Builder();
		builder.setManufacturer(Manufacturer.getManufacturer(defaultManufacturer(manufacturer)))
				.setCode(safeString(code))
				.setCommonName(safeString(commonName))
				.setDesignation(safeString(designation))
				.setDescription(safeString(description))
				.setMotorType(type)
				.setDiameter(diameter)
				.setLength(length)
				.setCaseInfo(safeString(caseInfo))
				.setPropellantInfo(safeString(propellantInfo))
				.setInitialMass(initialMass)
				.setDigest(safeString(digest))
				.setAvailability(available)
				.setStandardDelays(delays)
				.setTimePoints(timePoints)
				.setThrustPoints(thrustPoints)
				.setCGPoints(cgPoints);

		try {
			return builder.build();
		} catch (IllegalArgumentException e) {
			log.warn("Skipping invalid motor entry from SQLite database (manufacturer={}, designation={})",
					manufacturer, designation, e);
			return null;
		}
	}

	private static String safeString(String value) {
		return value == null ? "" : value;
	}

	private static String defaultManufacturer(String manufacturer) {
		if (manufacturer == null || manufacturer.trim().isEmpty()) {
			return "Unknown";
		}
		return manufacturer;
	}

	private static Motor.Type parseMotorType(String motorType) {
		if (motorType == null || motorType.trim().isEmpty()) {
			return Motor.Type.UNKNOWN;
		}
		try {
			return Motor.Type.valueOf(motorType);
		} catch (IllegalArgumentException e) {
			return Motor.Type.UNKNOWN;
		}
	}

	private static double getDoubleOrDefault(ResultSet rs, int columnIndex, double defaultValue) throws SQLException {
		Object value = rs.getObject(columnIndex);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static boolean getBooleanOrDefault(ResultSet rs, int columnIndex, boolean defaultValue) throws SQLException {
		Object value = rs.getObject(columnIndex);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue() != 0;
		}
		return Boolean.parseBoolean(value.toString());
	}

	private static byte[] encodeDoubleArray(double[] values) {
		if (values == null || values.length == 0) {
			return new byte[0];
		}
		ByteBuffer buffer = ByteBuffer.allocate(values.length * Double.BYTES).order(BYTE_ORDER);
		for (double value : values) {
			buffer.putDouble(value);
		}
		return buffer.array();
	}

	private static double[] decodeDoubleArray(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return new double[0];
		}
		if (bytes.length % Double.BYTES != 0) {
			throw new IllegalArgumentException("Invalid double array length: " + bytes.length);
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes).order(BYTE_ORDER);
		double[] values = new double[bytes.length / Double.BYTES];
		for (int i = 0; i < values.length; i++) {
			values[i] = buffer.getDouble();
		}
		return values;
	}

	private static byte[] encodeCoordinates(CoordinateIF[] points) {
		if (points == null || points.length == 0) {
			return new byte[0];
		}
		ByteBuffer buffer = ByteBuffer.allocate(points.length * 4 * Double.BYTES).order(BYTE_ORDER);
		for (CoordinateIF point : points) {
			buffer.putDouble(point.getX());
			buffer.putDouble(point.getY());
			buffer.putDouble(point.getZ());
			buffer.putDouble(point.getWeight());
		}
		return buffer.array();
	}

	private static CoordinateIF[] decodeCoordinates(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return new CoordinateIF[0];
		}
		int stride = 4 * Double.BYTES;
		if (bytes.length % stride != 0) {
			throw new IllegalArgumentException("Invalid coordinate array length: " + bytes.length);
		}
		int count = bytes.length / stride;
		ByteBuffer buffer = ByteBuffer.wrap(bytes).order(BYTE_ORDER);
		CoordinateIF[] points = new CoordinateIF[count];
		for (int i = 0; i < count; i++) {
			double x = buffer.getDouble();
			double y = buffer.getDouble();
			double z = buffer.getDouble();
			double weight = buffer.getDouble();
			points[i] = new Coordinate(x, y, z, weight);
		}
		return points;
	}
}
