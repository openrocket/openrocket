package info.openrocket.core.file.rocksim.export;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;

/**
 * Converts OpenRocket (ThrustCurve) motor designations and manufacturer names to those of RockSim's engine
 * database. RockSim looks up the motor of an engine set by its code, which must match exactly (ignoring case), and
 * by its manufacturer, which must match a vendor name in RockSim's EngineVendors.dat or be empty. Many ThrustCurve
 * designations differ from RockSim's codes, for example in how the propellant and the delay are written. Motors
 * without a rule keep their designation.
 */
final class RockSimEngineCode {

	private static final Map<String, String> CESARONI_PROPELLANTS = Map.ofEntries(
			Map.entry("Classic", "CL"), Map.entry("Blue Streak", "BS"), Map.entry("White", "WH"),
			Map.entry("Skidmark", "SK"), Map.entry("Smoky Sam", "SS"), Map.entry("White Thunder", "WT"),
			Map.entry("Red Lightning", "RL"), Map.entry("Imax", "IM"), Map.entry("Vmax", "VM"),
			Map.entry("Green3", "GR"), Map.entry("C-Star", "CS"), Map.entry("Pink", "PK"), Map.entry("Mellow", "MY"),
			Map.entry("Classic/Dual Thrust", "DT"), Map.entry("Imax/Dual Thrust", "IM"));

	private static final Map<String, String> ANIMAL_MOTOR_WORKS_PROPELLANTS = Map.of(
			"White Wolf", "WW", "Wild Wolf", "WW", "Skidmark", "SK", "Blue Baboon", "BB", "Green Gorilla", "GG",
			"Red Rhino", "RR", "Super Tiger", "ST", "Tamed Tiger", "TT");

	// Cesaroni: 24E22-13A, K650-SK -> E22 + propellant, K650SK
	private static final Pattern CESARONI = Pattern.compile("\\d*([A-Z]\\d+)-?([A-Z]{2})(-\\d+A)?");
	// Animal Motor Works: WT-54, 2551K1130 -> common name + propellant
	private static final Pattern ANIMAL_MOTOR_WORKS = Pattern.compile("[A-Z]{2}-\\d+|\\d+[A-Z]\\d+");
	private static final Pattern ANIMAL_MOTOR_WORKS_PROPELLANT = Pattern.compile("([A-Z]{2})-\\d+");
	// AMW ProX: K1130-BB -> K1130BB
	private static final Pattern PROX = Pattern.compile("([A-Z]\\d+)-([A-Z]{2})");
	// HyperTEK: 300CC098J-I130 -> I130-300CC098J
	private static final Pattern HYPERTEK = Pattern.compile("(\\d+CC[A-Z0-9]+)-([A-Z]\\d+)[A-Z]*");
	// Loki: G66-LR, J-326-LR -> G66LR, J326LR
	private static final Pattern LOKI = Pattern.compile("(?:HP-)?([A-Z])-?(\\d+)-([A-Z]{2})");
	// Estes 13 mm motors: A10 -> A10T
	private static final Pattern ESTES_13MM = Pattern.compile("[0-9/]*[A-Z]\\d+");
	// AeroTech: HP-H135W, G72DM-14A, F20W/L -> H135W, G72DM, F20W
	private static final Pattern AEROTECH_SUFFIX = Pattern.compile("(/L|_DMS|-\\d+A|-(PS|SU|OLD|L|PROTO))$");
	// AeroTech RC motors: E12-RCJ, E12RCJ, E12JRC -> E12J-RC
	private static final Pattern[] AEROTECH_RC = {
			Pattern.compile("([A-Z]\\d+)-RC([A-Z])"),
			Pattern.compile("([A-Z]\\d+)RC([A-Z])"),
			Pattern.compile("([A-Z]\\d+)([A-Z]?)RC") };

	// OpenRocket manufacturers with motors in RockSim 11's bundled engine database
	private static final Set<String> ROCKSIM_ENGINE_DATABASE_MANUFACTURERS = Set.of("AeroTech",
			"Animal Motor Works", "AMW/ProX", "Apogee", "Cesaroni Technology Inc.", "Contrail Rockets", "Estes",
			"Gorilla Rocket Motors", "HyperTEK", "Klima", "Loki Research", "Quest", "Sky Ripper Systems");

	private RockSimEngineCode() {
	}

	/**
	 * Whether RockSim's bundled engine database has motors of the motor's manufacturer. If not, RockSim can only
	 * find the motor if the user added it to their own engine database.
	 *
	 * @param motor the OpenRocket motor
	 * @return true if RockSim's bundled engine database has motors of this manufacturer
	 */
	static boolean isInRockSimEngineDatabase(Motor motor) {
		return motor instanceof ThrustCurveMotor && ROCKSIM_ENGINE_DATABASE_MANUFACTURERS.contains(
				((ThrustCurveMotor) motor).getManufacturer().getDisplayName());
	}

	/**
	 * Get the RockSim engine code of a motor.
	 *
	 * @param motor the OpenRocket motor
	 * @return the code that RockSim's engine database most likely uses for the motor
	 */
	static String getCode(Motor motor) {
		String code = motor.getCode();
		code = code == null || code.isBlank() ? motor.getDesignation() : code;
		if (!(motor instanceof ThrustCurveMotor)) {
			return code;
		}
		ThrustCurveMotor thrustCurveMotor = (ThrustCurveMotor) motor;
		String designation = code.trim().toUpperCase(Locale.ROOT);
		String commonName = thrustCurveMotor.getCommonName() == null ? "" : thrustCurveMotor.getCommonName().trim();
		String propellant = thrustCurveMotor.getPropellantInfo();
		Matcher m;

		switch (thrustCurveMotor.getManufacturer().getDisplayName()) {
			case "Cesaroni Technology Inc.":
				m = CESARONI.matcher(designation);
				if (m.matches()) {
					return m.group(1) + m.group(2);
				}
				if (!commonName.isEmpty() && CESARONI_PROPELLANTS.containsKey(propellant)) {
					return commonName + CESARONI_PROPELLANTS.get(propellant);
				}
				return code;
			case "AeroTech":
				String aerotech = AEROTECH_SUFFIX.matcher(designation.replaceFirst("^HP-", "")).replaceFirst("")
						.replaceFirst("NBT$", "NT");
				for (Pattern rc : AEROTECH_RC) {
					m = rc.matcher(aerotech);
					if (m.matches()) {
						return m.group(1) + m.group(2) + "-RC";
					}
				}
				return aerotech.equals(designation) ? code : aerotech;
			case "Animal Motor Works":
				if (!commonName.isEmpty() && ANIMAL_MOTOR_WORKS_PROPELLANTS.containsKey(propellant)
						&& ANIMAL_MOTOR_WORKS.matcher(designation).matches()) {
					return commonName + ANIMAL_MOTOR_WORKS_PROPELLANTS.get(propellant);
				}
				m = ANIMAL_MOTOR_WORKS_PROPELLANT.matcher(designation);
				if (!commonName.isEmpty() && m.matches()) {
					return commonName + m.group(1);
				}
				return code;
			case "AMW/ProX":
				m = PROX.matcher(designation);
				return m.matches() ? m.group(1) + m.group(2) : code;
			case "HyperTEK":
				m = HYPERTEK.matcher(designation);
				return m.matches() && !commonName.isEmpty() ? commonName + "-" + m.group(1) : code;
			case "Loki Research":
				m = LOKI.matcher(designation);
				return m.matches() ? m.group(1) + m.group(2) + m.group(3) : code;
			case "Contrail Rockets":
				return !commonName.isEmpty() && designation.startsWith(commonName.toUpperCase(Locale.ROOT) + "-")
						? commonName : code;
			case "Sky Ripper Systems":
				return commonName.isEmpty() ? code : commonName;
			case "Estes":
				if (Math.abs(motor.getDiameter() - 0.013) < 0.0005 && !designation.endsWith("T")
						&& ESTES_13MM.matcher(designation).matches()) {
					return code + "T";
				}
				return code;
			default:
				return code;
		}
	}

	/**
	 * Get the RockSim engine manufacturer of a motor.
	 *
	 * @param motor the OpenRocket motor
	 * @return the vendor name that RockSim's engine database uses, or an empty string to match on the code only
	 */
	static String getManufacturer(Motor motor) {
		if (!(motor instanceof ThrustCurveMotor)) {
			return "";
		}
		String name = ((ThrustCurveMotor) motor).getManufacturer().getDisplayName();
		switch (name) {
			case "Contrail Rockets":
				return "Contrail";
			case "Gorilla Rocket Motors":
				return "Gorilla";
			case "AMW/ProX":
				return "Animal Motor Works";
			case "Loki Research":
				// RockSim's engine database lists Loki motors under an unknown manufacturer
				return "";
			default:
				return name;
		}
	}
}
