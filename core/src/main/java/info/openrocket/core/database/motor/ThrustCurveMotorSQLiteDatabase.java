package info.openrocket.core.database.motor;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorDigest;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite database reader/writer for thrust curve motor data.
 * 
 * The database uses a normalized schema with four main tables:
 * - meta: Schema metadata (schema_version, database_version, etc.)
 * - manufacturers: Motor manufacturer information
 * - motors: Motor specifications and metadata
 * - thrust_curves: Different thrust curve sources for each motor
 * - thrust_data: Time/thrust data points for each thrust curve
 * 
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public final class ThrustCurveMotorSQLiteDatabase {
	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSQLiteDatabase.class);
	private static final int SCHEMA_VERSION = 3;
	private static final int MIN_SUPPORTED_SCHEMA_VERSION = 2;

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
				writeMetadata(connection, motors.size());
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
			int schemaVersion = validateSchema(connection);
			// Only include optional columns in queries if they actually exist.
			boolean hasMotorDescriptionSource =
					columnExists(connection, "motors", "description") && columnExists(connection, "motors", "source");
			return readMotors(connection, hasMotorDescriptionSource);
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
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
		// Enable foreign keys
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
		}
		return conn;
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
			// Meta table for schema version and metadata
			stmt.execute("CREATE TABLE IF NOT EXISTS meta (" +
					"key TEXT PRIMARY KEY, " +
					"value TEXT NOT NULL" +
					")");

			// Manufacturers table
			stmt.execute("CREATE TABLE IF NOT EXISTS manufacturers (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"name TEXT NOT NULL UNIQUE, " +
					"abbrev TEXT" +
					")");

			// Motors table
			stmt.execute("CREATE TABLE IF NOT EXISTS motors (" +
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
					"description TEXT, " +
					"source TEXT, " +
					"FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id)" +
					")");

			// Thrust curves table (one motor can have multiple curves)
			stmt.execute("CREATE TABLE IF NOT EXISTS thrust_curves (" +
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

			// Thrust data table
			stmt.execute("CREATE TABLE IF NOT EXISTS thrust_data (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"curve_id INTEGER NOT NULL, " +
					"time_seconds REAL NOT NULL, " +
					"force_newtons REAL NOT NULL, " +
					"FOREIGN KEY (curve_id) REFERENCES thrust_curves(id) ON DELETE CASCADE" +
					")");

			// Create indices for performance
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_motor_mfr ON motors(manufacturer_id)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_motor_diameter ON motors(diameter)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_motor_impulse ON motors(total_impulse)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_motor_impulse_class ON motors(impulse_class)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_motor_tc_id ON motors(tc_motor_id)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_curve_motor ON thrust_curves(motor_id)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_curve_simfile ON thrust_curves(tc_simfile_id)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_thrust_curve ON thrust_data(curve_id)");
		}
	}

	private static void writeMetadata(Connection connection, int motorCount) throws SQLException {
		String sql = "INSERT INTO meta (key, value) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			// Schema version
			stmt.setString(1, "schema_version");
			stmt.setString(2, Integer.toString(SCHEMA_VERSION));
			stmt.executeUpdate();

			// Database version (timestamp)
			stmt.setString(1, "database_version");
			stmt.setString(2, Long.toString(System.currentTimeMillis() / 1000));
			stmt.executeUpdate();

			// Generated at
			stmt.setString(1, "generated_at");
			stmt.setString(2, java.time.Instant.now().toString());
			stmt.executeUpdate();

			// Motor count
			stmt.setString(1, "motor_count");
			stmt.setString(2, Integer.toString(motorCount));
			stmt.executeUpdate();
		}
	}

	private static int validateSchema(Connection connection) throws SQLException {
		if (!tableExists(connection, "meta")) {
			throw new SQLException("SQLite motor database missing meta table");
		}
		if (!tableExists(connection, "manufacturers")) {
			throw new SQLException("SQLite motor database missing manufacturers table");
		}
		if (!tableExists(connection, "motors")) {
			throw new SQLException("SQLite motor database missing motors table");
		}
		if (!tableExists(connection, "thrust_curves")) {
			throw new SQLException("SQLite motor database missing thrust_curves table");
		}
		if (!tableExists(connection, "thrust_data")) {
			throw new SQLException("SQLite motor database missing thrust_data table");
		}

		int schemaVersion = readSchemaVersion(connection);
		if (schemaVersion < MIN_SUPPORTED_SCHEMA_VERSION || schemaVersion > SCHEMA_VERSION) {
			throw new SQLException("Unsupported thrust curve database schema version: " + schemaVersion +
					" (supported " + MIN_SUPPORTED_SCHEMA_VERSION + "-" + SCHEMA_VERSION + ")");
		}

		requireColumns(connection, "meta", "key", "value");
		requireColumns(connection, "manufacturers", "id", "name", "abbrev");
		requireColumns(connection, "thrust_curves",
				"id", "motor_id", "tc_simfile_id", "source", "format", "license", "info_url", "data_url",
				"total_impulse", "avg_thrust", "max_thrust", "burn_time");
		requireColumns(connection, "thrust_data", "id", "curve_id", "time_seconds", "force_newtons");

		if (schemaVersion >= 3) {
			requireColumns(connection, "motors",
					"id", "manufacturer_id", "tc_motor_id", "designation", "common_name", "impulse_class",
					"diameter", "length", "total_impulse", "avg_thrust", "max_thrust", "burn_time",
					"propellant_weight", "total_weight", "type", "delays", "case_info", "prop_info",
					"sparky", "info_url", "data_files", "updated_on", "description", "source");
		} else {
			// Schema v2: no description/source columns.
			requireColumns(connection, "motors",
					"id", "manufacturer_id", "tc_motor_id", "designation", "common_name", "impulse_class",
					"diameter", "length", "total_impulse", "avg_thrust", "max_thrust", "burn_time",
					"propellant_weight", "total_weight", "type", "delays", "case_info", "prop_info",
					"sparky", "info_url", "data_files", "updated_on");
		}

		return schemaVersion;
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

	private static void insertMotors(Connection connection, List<ThrustCurveMotor> motors) throws SQLException {
		// Cache for manufacturer IDs
		Map<String, Integer> manufacturerCache = new HashMap<>();

		String insertManufacturer = "INSERT OR IGNORE INTO manufacturers (name, abbrev) VALUES (?, ?)";
		String selectManufacturer = "SELECT id FROM manufacturers WHERE name = ?";
		String insertMotor = "INSERT INTO motors (" +
				"manufacturer_id, tc_motor_id, designation, common_name, impulse_class, diameter, length, " +
				"total_impulse, avg_thrust, max_thrust, burn_time, propellant_weight, total_weight, " +
				"type, delays, case_info, prop_info, sparky, info_url, data_files, updated_on, description, source" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String insertCurve = "INSERT INTO thrust_curves (motor_id, source, format) VALUES (?, ?, ?)";
		String insertThrust = "INSERT INTO thrust_data (curve_id, time_seconds, force_newtons) VALUES (?, ?, ?)";

		try (PreparedStatement mfrInsertStmt = connection.prepareStatement(insertManufacturer);
			 PreparedStatement mfrSelectStmt = connection.prepareStatement(selectManufacturer);
			 PreparedStatement motorStmt = connection.prepareStatement(insertMotor, Statement.RETURN_GENERATED_KEYS);
			 PreparedStatement curveStmt = connection.prepareStatement(insertCurve, Statement.RETURN_GENERATED_KEYS);
			 PreparedStatement thrustStmt = connection.prepareStatement(insertThrust)) {

			for (ThrustCurveMotor motor : motors) {
				// Get or create manufacturer
				String mfrName = motor.getManufacturer().getDisplayName();
				String mfrAbbrev = motor.getManufacturer().getSimpleName();
				
				Integer mfrId = manufacturerCache.get(mfrName);
				if (mfrId == null) {
					mfrInsertStmt.setString(1, mfrName);
					mfrInsertStmt.setString(2, mfrAbbrev);
					mfrInsertStmt.executeUpdate();

					mfrSelectStmt.setString(1, mfrName);
					try (ResultSet rs = mfrSelectStmt.executeQuery()) {
						if (rs.next()) {
							mfrId = rs.getInt(1);
							manufacturerCache.put(mfrName, mfrId);
						} else {
							throw new SQLException("Failed to retrieve manufacturer ID for: " + mfrName);
						}
					}
				}

				// Calculate motor properties
				double[] timePoints = motor.getTimePoints();
				double[] thrustPoints = motor.getThrustPoints();
				double impulse = calculateTotalImpulse(timePoints, thrustPoints);
				double burnTime = timePoints.length > 0 ? timePoints[timePoints.length - 1] : 0;
				double avgThrust = burnTime > 0 ? impulse / burnTime : 0;
				double maxThrust = 0;
				for (double t : thrustPoints) {
					if (t > maxThrust) {
						maxThrust = t;
					}
				}
				double propellantWeight = (motor.getInitialMass() - motor.getBurnoutMass()) * 1000; // kg to g
				String impulseClass = getImpulseClass(impulse);
				String motorType = getMotorTypeCode(motor.getMotorType());
				String delays = formatDelays(motor.getStandardDelays());

				// Insert motor
				motorStmt.setInt(1, mfrId);
				motorStmt.setString(2, nullIfBlank(motor.getTcMotorId()));
				motorStmt.setString(3, safeString(motor.getDesignation()));
				motorStmt.setString(4, safeString(motor.getCommonName()));
				motorStmt.setString(5, impulseClass);
				motorStmt.setDouble(6, motor.getDiameter() * 1000); // m to mm
				motorStmt.setDouble(7, motor.getLength() * 1000);   // m to mm
				motorStmt.setDouble(8, impulse);
				motorStmt.setDouble(9, avgThrust);
				motorStmt.setDouble(10, maxThrust);
				motorStmt.setDouble(11, burnTime);
				motorStmt.setDouble(12, propellantWeight);
				motorStmt.setDouble(13, motor.getInitialMass() * 1000); // kg to g
				motorStmt.setString(14, motorType);
				motorStmt.setString(15, delays);
				motorStmt.setString(16, safeString(motor.getCaseInfo()));
				motorStmt.setString(17, safeString(motor.getPropellantInfo()));
				motorStmt.setInt(18, motor.isSparky() ? 1 : 0);
				motorStmt.setString(19, nullIfBlank(motor.getInfoUrl()));
				if (motor.getDataFiles() == null) {
					motorStmt.setObject(20, null);
				} else {
					motorStmt.setInt(20, motor.getDataFiles());
				}
				motorStmt.setString(21, nullIfBlank(motor.getUpdatedOn()));
				motorStmt.setString(22, normalizeDescriptionForDb(motor.getDescription()));
				motorStmt.setString(23, nullIfBlank(motor.getDataSource()));
				motorStmt.executeUpdate();

				int motorId;
				try (ResultSet rs = motorStmt.getGeneratedKeys()) {
					if (rs.next()) {
						motorId = rs.getInt(1);
					} else {
						throw new SQLException("Failed to retrieve generated motor ID");
					}
				}

				// Insert thrust curve
				curveStmt.setInt(1, motorId);
				curveStmt.setString(2, "openrocket"); // source
				curveStmt.setString(3, "internal");   // format
				curveStmt.executeUpdate();

				int curveId;
				try (ResultSet rs = curveStmt.getGeneratedKeys()) {
					if (rs.next()) {
						curveId = rs.getInt(1);
					} else {
						throw new SQLException("Failed to retrieve generated curve ID");
					}
				}

				// Insert thrust data points
				for (int i = 0; i < timePoints.length; i++) {
					thrustStmt.setInt(1, curveId);
					thrustStmt.setDouble(2, timePoints[i]);
					thrustStmt.setDouble(3, thrustPoints[i]);
					thrustStmt.addBatch();
				}
				thrustStmt.executeBatch();
			}
		}
	}

	private static List<ThrustCurveMotor> readMotors(Connection connection, boolean hasMotorDescriptionSource) throws SQLException {
		// First, log some stats about the database
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM motors");
			if (rs.next()) {
				log.info("Total motors in database: {}", rs.getInt(1));
			}
			rs = stmt.executeQuery("SELECT COUNT(*) FROM thrust_curves");
			if (rs.next()) {
				log.info("Total thrust curves in database: {}", rs.getInt(1));
			}
			rs = stmt.executeQuery("SELECT COUNT(*) FROM thrust_data");
			if (rs.next()) {
				log.info("Total thrust data points in database: {}", rs.getInt(1));
			}
		}

		// Query to get motors with their manufacturers
		// For motors with multiple thrust curves, we'll use the first one (prefer "cert" or "mfr" source)
		String motorSql = "SELECT m.id, m.designation, m.common_name, m.diameter, m.length, " +
				"m.total_impulse, m.avg_thrust, m.burn_time, m.propellant_weight, m.total_weight, " +
				"m.type, m.delays, m.case_info, m.prop_info, " +
				"m.tc_motor_id, m.sparky, m.info_url, m.data_files, m.updated_on" +
				(hasMotorDescriptionSource ? ", m.description, m.source" : "") + ", " +
				"mfr.name, mfr.abbrev " +
				"FROM motors m " +
				"JOIN manufacturers mfr ON m.manufacturer_id = mfr.id";

		// Get the preferred thrust curve for a motor (prefer cert > mfr > user)
		String curveSql = "SELECT id FROM thrust_curves WHERE motor_id = ? " +
				"ORDER BY CASE source " +
				"  WHEN 'cert' THEN 1 " +
				"  WHEN 'mfr' THEN 2 " +
				"  WHEN 'user' THEN 3 " +
				"  ELSE 4 END " +
				"LIMIT 1";

		String thrustSql = "SELECT time_seconds, force_newtons FROM thrust_data " +
				"WHERE curve_id = ? ORDER BY time_seconds";

		List<ThrustCurveMotor> motors = new ArrayList<>();
		int skippedNoCurve = 0;
		int skippedNoData = 0;
		int skippedInvalid = 0;
		int totalProcessed = 0;
		
		try (PreparedStatement motorStmt = connection.prepareStatement(motorSql);
			 PreparedStatement curveStmt = connection.prepareStatement(curveSql);
			 PreparedStatement thrustStmt = connection.prepareStatement(thrustSql);
			 ResultSet motorRs = motorStmt.executeQuery()) {

			while (motorRs.next()) {
				totalProcessed++;
				int motorId = motorRs.getInt(1);
				String designation = motorRs.getString(2);
				String commonName = motorRs.getString(3);
				double diameter = getDoubleOrDefault(motorRs, 4, 0.0) / 1000;  // mm to m
				double length = getDoubleOrDefault(motorRs, 5, 0.0) / 1000;    // mm to m
				double propellantWeight = getDoubleOrDefault(motorRs, 9, 0.0) / 1000; // g to kg
				double totalWeight = getDoubleOrDefault(motorRs, 10, 0.0) / 1000;     // g to kg
				String typeCode = motorRs.getString(11);
				String delaysStr = motorRs.getString(12);
				String caseInfo = motorRs.getString(13);
				String propInfo = motorRs.getString(14);
				String tcMotorId = motorRs.getString(15);
				boolean sparky = getIntOrDefault(motorRs, 16, 0) != 0;
				String infoUrl = motorRs.getString(17);
				Integer dataFiles = getIntegerOrNull(motorRs, 18);
				String updatedOn = motorRs.getString(19);
				String description = null;
				String dataSource = null;
				String manufacturerName;
				String manufacturerAbbrev;

				if (hasMotorDescriptionSource) {
					description = motorRs.getString(20);
					dataSource = motorRs.getString(21);
					manufacturerName = motorRs.getString(22);
					manufacturerAbbrev = motorRs.getString(23);
				} else {
					manufacturerName = motorRs.getString(20);
					manufacturerAbbrev = motorRs.getString(21);
				}

				// Get the preferred thrust curve ID
				Integer curveId = null;
				curveStmt.setInt(1, motorId);
				try (ResultSet curveRs = curveStmt.executeQuery()) {
					if (curveRs.next()) {
						curveId = curveRs.getInt(1);
					}
				}

				if (curveId == null) {
					log.debug("Skipping motor with no thrust curves (designation={})", designation);
					skippedNoCurve++;
					continue;
				}

				// Read thrust data
				List<Double> timeList = new ArrayList<>();
				List<Double> thrustList = new ArrayList<>();
				
				thrustStmt.setInt(1, curveId);
				try (ResultSet thrustRs = thrustStmt.executeQuery()) {
					while (thrustRs.next()) {
						timeList.add(thrustRs.getDouble(1));
						thrustList.add(thrustRs.getDouble(2));
					}
				}

				if (timeList.isEmpty()) {
					log.debug("Skipping motor with no thrust data (designation={})", designation);
					skippedNoData++;
					continue;
				}

				double[] timePoints = timeList.stream().mapToDouble(Double::doubleValue).toArray();
				double[] thrustPoints = thrustList.stream().mapToDouble(Double::doubleValue).toArray();

				// Normalize thrust curve data
				NormalizedThrustData normalized = normalizeThrustData(timePoints, thrustPoints, designation);
				if (normalized == null) {
					log.debug("Skipping motor with invalid thrust data (designation={})", designation);
					skippedInvalid++;
					continue;
				}
				timePoints = normalized.timePoints;
				thrustPoints = normalized.thrustPoints;

				// Ensure we have valid length (required for CG calculation)
				if (length <= 0) {
					// Estimate length from diameter if available, otherwise use a default
					length = diameter > 0 ? diameter * 3 : 0.1; // Default 100mm
				}

				// Calculate CG points
				CoordinateIF[] cgPoints = calculateCGPoints(timePoints, thrustPoints, totalWeight, propellantWeight, length);

				// Parse delays
				double[] delays = parseDelays(delaysStr);

				Motor.Type motorType = parseMotorType(typeCode);

				String digest = computeDigest(timePoints, thrustPoints, cgPoints);

				ThrustCurveMotor.Builder builder = new ThrustCurveMotor.Builder();
				builder.setManufacturer(Manufacturer.getManufacturer(chooseManufacturerName(manufacturerName, manufacturerAbbrev)))
						.setCode(safeString(designation))
						.setDesignation(safeString(designation))
						.setCommonName(safeString(commonName))
						.setDescription(description == null ? "" : description)
						.setTcMotorId(tcMotorId == null ? "" : tcMotorId)
						.setInfoUrl(infoUrl == null ? "" : infoUrl)
						.setDataFiles(dataFiles)
						.setUpdatedOn(updatedOn == null ? "" : updatedOn)
						.setDataSource(dataSource == null ? "" : dataSource)
						.setSparky(sparky)
						.setMotorType(motorType)
						.setDiameter(diameter)
						.setLength(length)
						.setCaseInfo(safeString(caseInfo))
						.setPropellantInfo(safeString(propInfo))
						.setInitialMass(totalWeight)
						.setDigest(digest)
						.setAvailability(true)
						.setStandardDelays(delays)
						.setTimePoints(timePoints)
						.setThrustPoints(thrustPoints)
						.setCGPoints(cgPoints);

				try {
					motors.add(builder.build());
				} catch (IllegalArgumentException e) {
					log.warn("Skipping invalid motor (designation={}): {} [time[0]={}, length={}]", 
							designation, e.getMessage(), 
							timePoints.length > 0 ? timePoints[0] : "empty",
							length);
					skippedInvalid++;
				}
			}
		}

		log.info("Motor loading summary: processed={}, loaded={}, skippedNoCurve={}, skippedNoData={}, skippedInvalid={}",
				totalProcessed, motors.size(), skippedNoCurve, skippedNoData, skippedInvalid);

		return motors;
	}

	/**
	 * Container for normalized thrust data.
	 */
	private static class NormalizedThrustData {
		final double[] timePoints;
		final double[] thrustPoints;

		NormalizedThrustData(double[] timePoints, double[] thrustPoints) {
			this.timePoints = timePoints;
			this.thrustPoints = thrustPoints;
		}
	}

	/**
	 * Normalize thrust data to meet ThrustCurveMotor requirements:
	 * - Must start at time 0
	 * - Time values must be strictly increasing
	 * - Must have at least 2 data points
	 * - Thrust values must be non-negative
	 */
	private static NormalizedThrustData normalizeThrustData(double[] timePoints, double[] thrustPoints, 
			String designation) {
		if (timePoints.length < 2) {
			log.debug("Motor {} has less than 2 data points", designation);
			return null;
		}

		final double timeEpsilon = 0.0001;
		List<Double> normTime = new ArrayList<>();
		List<Double> normThrust = new ArrayList<>();

		// Always ensure the curve starts at t=0
		double firstTime = timePoints[0];
		if (Math.abs(firstTime) > timeEpsilon) {
			// Curve doesn't start at 0, prepend a zero point
			log.debug("Motor {}: prepending t=0 point (original starts at {})", designation, firstTime);
			normTime.add(0.0);
			normThrust.add(0.0);
		}

		double lastTime = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < timePoints.length; i++) {
			double t = timePoints[i];
			double thrust = thrustPoints[i];

			// Ensure thrust is non-negative
			if (thrust < 0) {
				thrust = 0;
			}

			// Ensure time is strictly increasing.
			// If multiple points exist at the same time, keep the maximum thrust (this
			// matches OpenRocket's motor-file loaders which typically prefer a non-zero
			// point at t=0 if one exists).
			if (t > lastTime + timeEpsilon) {
				normTime.add(t);
				normThrust.add(thrust);
				lastTime = t;
			} else if (Math.abs(t - lastTime) <= timeEpsilon && !normThrust.isEmpty()) {
				int lastIndex = normThrust.size() - 1;
				if (thrust > normThrust.get(lastIndex)) {
					normThrust.set(lastIndex, thrust);
				}
			}
			// Skip decreasing time points
		}

		if (normTime.size() < 2) {
			log.debug("Motor {} has less than 2 valid data points after normalization", designation);
			return null;
		}

		double[] resultTime = normTime.stream().mapToDouble(Double::doubleValue).toArray();
		double[] resultThrust = normThrust.stream().mapToDouble(Double::doubleValue).toArray();
		
		// Verify the result starts at 0
		if (resultTime[0] != 0.0) {
			log.warn("Motor {}: normalization failed, still starts at {}", designation, resultTime[0]);
		}

		return new NormalizedThrustData(resultTime, resultThrust);
	}

	/**
	 * Calculate CG points based on mass loss during burn.
	 * Assumes CG is at center of motor and propellant burn follows thrust (constant exhaust velocity).
	 */
	private static CoordinateIF[] calculateCGPoints(double[] timePoints, double[] thrustPoints, double totalMass,
			double propellantMass, double length) {
		CoordinateIF[] cgPoints = new CoordinateIF[timePoints.length];
		double cgX = length / 2;  // CG at center of motor
		double burnoutMass = totalMass - propellantMass;
		if (burnoutMass < 0) {
			burnoutMass = 0;
		}

		if (timePoints.length == 0) {
			return cgPoints;
		}

		if (propellantMass <= 0) {
			for (int i = 0; i < timePoints.length; i++) {
				cgPoints[i] = new Coordinate(cgX, 0, 0, totalMass);
			}
			return cgPoints;
		}

		if (thrustPoints == null || thrustPoints.length != timePoints.length || timePoints.length < 2) {
			// Fallback to a linear mass decrease if thrust data is missing or inconsistent.
			double burnTime = timePoints.length > 0 ? timePoints[timePoints.length - 1] : 1;
			for (int i = 0; i < timePoints.length; i++) {
				double t = timePoints[i];
				double massAtTime = totalMass - (propellantMass * (t / burnTime));
				if (massAtTime < burnoutMass) {
					massAtTime = burnoutMass;
				}
				cgPoints[i] = new Coordinate(cgX, 0, 0, massAtTime);
			}
			return cgPoints;
		}

		// Calculate mass change between points using trapezoidal integration of thrust.
		double totalMassChange = 0;
		double t0 = timePoints[0];
		double f0 = thrustPoints[0];
		double[] deltaMass = new double[timePoints.length - 1];
		for (int i = 1; i < timePoints.length; i++) {
			double t1 = timePoints[i];
			double f1 = thrustPoints[i];
			double dm = 0.5 * (f0 + f1) * (t1 - t0);
			if (dm < 0) {
				dm = 0;
			}
			deltaMass[i - 1] = dm;
			totalMassChange += dm;
			t0 = t1;
			f0 = f1;
		}

		if (totalMassChange <= 0) {
			// No thrust -> cannot scale consumption by impulse; fall back to linear.
			double burnTime = timePoints[timePoints.length - 1] == 0 ? 1 : timePoints[timePoints.length - 1];
			for (int i = 0; i < timePoints.length; i++) {
				double t = timePoints[i];
				double massAtTime = totalMass - (propellantMass * (t / burnTime));
				if (massAtTime < burnoutMass) {
					massAtTime = burnoutMass;
				}
				cgPoints[i] = new Coordinate(cgX, 0, 0, massAtTime);
			}
			return cgPoints;
		}

		double scale = propellantMass / totalMassChange;

		double mass = totalMass;
		cgPoints[0] = new Coordinate(cgX, 0, 0, mass);
		for (int i = 1; i < timePoints.length; i++) {
			mass -= deltaMass[i - 1] * scale;
			if (mass < burnoutMass) {
				mass = burnoutMass;
			}
			cgPoints[i] = new Coordinate(cgX, 0, 0, mass);
		}
		return cgPoints;
	}

	/**
	 * Calculate total impulse using trapezoidal integration.
	 */
	private static double calculateTotalImpulse(double[] timePoints, double[] thrustPoints) {
		if (timePoints.length < 2) {
			return 0;
		}
		double impulse = 0;
		for (int i = 1; i < timePoints.length; i++) {
			double dt = timePoints[i] - timePoints[i - 1];
			double avgThrust = (thrustPoints[i] + thrustPoints[i - 1]) / 2;
			impulse += avgThrust * dt;
		}
		return impulse;
	}

	/**
	 * Get the impulse class letter (A, B, C, ... O) based on total impulse.
	 */
	private static String getImpulseClass(double impulse) {
		if (impulse <= 0) return null;
		if (impulse <= 1.25) return "1/4A";
		if (impulse <= 2.5) return "1/2A";
		if (impulse <= 5) return "A";
		if (impulse <= 10) return "B";
		if (impulse <= 20) return "C";
		if (impulse <= 40) return "D";
		if (impulse <= 80) return "E";
		if (impulse <= 160) return "F";
		if (impulse <= 320) return "G";
		if (impulse <= 640) return "H";
		if (impulse <= 1280) return "I";
		if (impulse <= 2560) return "J";
		if (impulse <= 5120) return "K";
		if (impulse <= 10240) return "L";
		if (impulse <= 20480) return "M";
		if (impulse <= 40960) return "N";
		return "O";
	}

	private static String getMotorTypeCode(Motor.Type type) {
		if (type == null) {
			return null;
		}
		switch (type) {
			case SINGLE:
				return "SU";
			case RELOAD:
				return "reload";
			case HYBRID:
				return "hybrid";
			default:
			return null;
		}
	}

	private static Motor.Type parseMotorType(String typeCode) {
		if (typeCode == null || typeCode.trim().isEmpty()) {
			return Motor.Type.UNKNOWN;
		}
		switch (typeCode.toLowerCase()) {
			case "su":
			case "single":
			case "single-use":
				return Motor.Type.SINGLE;
			case "re":
			case "reload":
				return Motor.Type.RELOAD;
			case "hy":
			case "hybrid":
				return Motor.Type.HYBRID;
			default:
				return Motor.Type.UNKNOWN;
		}
	}

	private static String formatDelays(double[] delays) {
		if (delays == null || delays.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < delays.length; i++) {
			if (i > 0) sb.append(",");
			// Format as integer if whole number
			if (delays[i] == Math.floor(delays[i])) {
				sb.append((int) delays[i]);
			} else {
				sb.append(delays[i]);
			}
		}
		return sb.toString();
	}

	private static double[] parseDelays(String delaysStr) {
		if (delaysStr == null || delaysStr.trim().isEmpty()) {
			return new double[0];
		}
		String[] parts = delaysStr.split(",");
		List<Double> delays = new ArrayList<>();
		for (String part : parts) {
			try {
				delays.add(Double.parseDouble(part.trim()));
			} catch (NumberFormatException e) {
				// Skip invalid delay values
			}
		}
		return delays.stream().mapToDouble(Double::doubleValue).toArray();
	}

	private static String safeString(String value) {
		return value == null ? "" : value;
	}

	private static String nullIfBlank(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String normalizeDescriptionForDb(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.replace('\r', '\n').replaceAll("\\s*\\n\\s*", " ").replaceAll("\\s+", " ").trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String chooseManufacturerName(String manufacturerName, String manufacturerAbbrev) {
		if (manufacturerAbbrev != null) {
			String trimmed = manufacturerAbbrev.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}
		if (manufacturerName != null) {
			String trimmed = manufacturerName.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}
		return "Unknown";
	}

	private static void requireColumns(Connection connection, String tableName, String... requiredColumns) throws SQLException {
		List<String> missing = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement("PRAGMA table_info(" + tableName + ")")) {
			try (ResultSet rs = stmt.executeQuery()) {
				List<String> existing = new ArrayList<>();
				while (rs.next()) {
					existing.add(rs.getString("name"));
				}
				for (String col : requiredColumns) {
					if (!existing.contains(col)) {
						missing.add(col);
					}
				}
			}
		}
		if (!missing.isEmpty()) {
			throw new SQLException("SQLite motor database table '" + tableName + "' missing required columns: " + missing);
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

	private static int getIntOrDefault(ResultSet rs, int columnIndex, int defaultValue) throws SQLException {
		Object value = rs.getObject(columnIndex);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static Integer getIntegerOrNull(ResultSet rs, int columnIndex) throws SQLException {
		Object value = rs.getObject(columnIndex);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return Integer.valueOf(value.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
		try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
			return rs.next();
		}
	}

	private static String computeDigest(double[] timePoints, double[] thrustPoints, CoordinateIF[] cgPoints) {
		MotorDigest motorDigest = new MotorDigest();
		motorDigest.update(MotorDigest.DataType.TIME_ARRAY, timePoints);

		double[] cgx = new double[cgPoints.length];
		double[] mass = new double[cgPoints.length];
		for (int i = 0; i < cgPoints.length; i++) {
			cgx[i] = cgPoints[i].getX();
			mass[i] = cgPoints[i].getWeight();
		}

		motorDigest.update(MotorDigest.DataType.MASS_PER_TIME, mass);
		motorDigest.update(MotorDigest.DataType.CG_PER_TIME, cgx);
		motorDigest.update(MotorDigest.DataType.FORCE_PER_TIME, thrustPoints);
		return motorDigest.getDigest();
	}
}
