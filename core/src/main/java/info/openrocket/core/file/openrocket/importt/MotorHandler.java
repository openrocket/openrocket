package info.openrocket.core.file.openrocket.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorDigest;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

import org.xml.sax.SAXException;

class MotorHandler extends AbstractElementHandler {
	/** File version where latest digest format was introduced */
	private static final int MOTOR_DIGEST_VERSION = 104;

	private final DocumentLoadingContext context;
	private Motor.Type type = null;
	private String manufacturer = null;
	private String designation = null;
	private String digest = null;
	private double diameter = Double.NaN;
	private double length = Double.NaN;
	private double delay = Double.NaN;

	private final List<Double> standardDelays = new ArrayList<>();
	private final List<Double> thrustCurveTime = new ArrayList<>();
	private final List<Double> thrustCurveThrust = new ArrayList<>();
	private final List<Double> thrustCurveCGx = new ArrayList<>();
	private final List<Double> thrustCurveMass = new ArrayList<>();

	public MotorHandler(DocumentLoadingContext context) {
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	/**
	 * Resolve the {@link Motor} for this {@code <motor>} element.
	 * <p>
	 * Preference order:
	 * <ol>
	 *   <li>Lookup via {@link DocumentLoadingContext#getMotorFinder()} (typically the motor database).</li>
	 *   <li>Embedded thrust curve data (one or more {@code <thrustcurvepoint>} elements), if present.</li>
	 * </ol>
	 *
	 * @param warnings warnings sink
	 * @return the resolved motor, or {@code null} if not found/invalid
	 */
	public Motor getMotor(WarningSet warnings) {
		// First try to locate an equivalent motor in the motor database.
		// If that fails, fall back to the embedded thrust curve data (if present).
		WarningSet databaseWarnings = new WarningSet();
		Motor databaseMotor = context.getMotorFinder().findMotor(type, manufacturer, designation, Double.NaN, Double.NaN, digest,
				databaseWarnings);
		if (databaseMotor != null) {
			warnings.addAll(databaseWarnings);
			return databaseMotor;
		}

		if (!thrustCurveTime.isEmpty()) {
			Motor embedded = buildEmbeddedThrustCurveMotor(warnings);
			if (embedded != null) {
				// Deliberately discard databaseWarnings here: the motor isn't missing since we loaded it from the file.
				return embedded;
			}
		}

		// Nothing worked: surface any database lookup warnings (e.g. missing motor).
		warnings.addAll(databaseWarnings);
		return null;
	}

	/**
	 * Build a {@link ThrustCurveMotor} from motor data embedded in the .ork file.
	 * <p>
	 * The embedded file format stores:
	 * <ul>
	 *   <li>{@code <standarddelays>} as a comma-separated list of delays in seconds; {@code none} represents
	 *       {@link Motor#PLUGGED_DELAY}.</li>
	 *   <li>One or more {@code <thrustcurvepoint>} elements, each containing {@code time,thrust,cg_x,mass} with units
	 *       seconds, Newtons, meters and kilograms.</li>
	 * </ul>
	 *
	 * @param warnings warnings sink
	 * @return the reconstructed motor, or {@code null} if embedded data is incomplete/invalid
	 */
	private Motor buildEmbeddedThrustCurveMotor(WarningSet warnings) {
		// Validate that we have a complete set of point arrays.
		int count = thrustCurveTime.size();
		if (count < 2) {
			warnings.add(Warning.fromString("Embedded motor thrust curve is too short, ignoring."));
			return null;
		}
		if (count != thrustCurveThrust.size() ||
				count != thrustCurveCGx.size() ||
				count != thrustCurveMass.size()) {
			warnings.add(Warning.fromString("Embedded motor thrust curve point data is inconsistent, ignoring."));
			return null;
		}

		String manufacturerName = (manufacturer == null || manufacturer.isBlank()) ? "Unknown" : manufacturer.trim();
		String motorDesignation = (designation == null || designation.isBlank()) ? null : designation.trim();
		if (motorDesignation == null) {
			warnings.add(Warning.fromString("Embedded motor is missing designation, ignoring."));
			return null;
		}

		Motor.Type motorType = (type == null) ? Motor.Type.UNKNOWN : type;

		// Convert list data into primitive arrays expected by ThrustCurveMotor and build CG points.
		double[] timeArray = new double[count];
		double[] thrustArray = new double[count];
		double[] cgxArray = new double[count];
		double[] massArray = new double[count];
		CoordinateIF[] cgArray = new CoordinateIF[count];
		for (int i = 0; i < count; i++) {
			timeArray[i] = thrustCurveTime.get(i);
			thrustArray[i] = thrustCurveThrust.get(i);
			cgxArray[i] = thrustCurveCGx.get(i);
			massArray[i] = thrustCurveMass.get(i);
			cgArray[i] = new Coordinate(cgxArray[i], 0, 0, massArray[i]);
		}

		double[] delaysArray = new double[standardDelays.size()];
		for (int i = 0; i < standardDelays.size(); i++) {
			delaysArray[i] = standardDelays.get(i);
		}

		// Prefer the digest stored in the file; if it's missing (e.g. old file versions), compute one from the embedded
		// thrust curve data to preserve identity across load/save.
		String motorDigest = (digest == null || digest.isBlank()) ? null : digest.trim();
		if (motorDigest == null) {
			MotorDigest md = new MotorDigest();
			md.update(MotorDigest.DataType.TIME_ARRAY, timeArray);
			md.update(MotorDigest.DataType.MASS_PER_TIME, massArray);
			md.update(MotorDigest.DataType.CG_PER_TIME, cgxArray);
			md.update(MotorDigest.DataType.FORCE_PER_TIME, thrustArray);
			motorDigest = md.getDigest();
		}

		try {
			Manufacturer m = Manufacturer.getManufacturer(manufacturerName);
			ThrustCurveMotor.Builder builder = new ThrustCurveMotor.Builder()
					.setManufacturer(m)
					.setDesignation(motorDesignation)
					.setMotorType(motorType)
					.setStandardDelays(delaysArray)
					.setDiameter(diameter)
					.setLength(length)
					.setTimePoints(timeArray)
					.setThrustPoints(thrustArray)
					.setCGPoints(cgArray)
					.setDigest(motorDigest);
			return builder.build();
		} catch (IllegalArgumentException e) {
			warnings.add(Warning.fromString(
					"Invalid embedded thrust curve data for motor '" + motorDesignation + "', ignoring."));
			return null;
		}
	}

	/**
	 * Return the delay to use for the motor.
	 */
	public double getDelay(WarningSet warnings) {
		if (Double.isNaN(delay)) {
			warnings.add(Warning.fromString("Motor delay not specified, assuming no ejection charge."));
			return Motor.PLUGGED_DELAY;
		}
		return delay;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		content = content.trim();

		if (element.equals("type")) {

			// Motor type
			type = null;
			for (Motor.Type t : Motor.Type.values()) {
				if (t.name().toLowerCase(Locale.ENGLISH).equals(content.trim())) {
					type = t;
					break;
				}
			}
			if (type == null) {
				warnings.add(Warning.fromString("Unknown motor type '" + content + "', ignoring."));
			}

		} else if (element.equals("manufacturer")) {

			// Manufacturer
			manufacturer = content.trim();

		} else if (element.equals("designation")) {

			// Designation
			designation = content.trim();

		} else if (element.equals("digest")) {

			// Digest is used only for file versions saved using the same digest algorithm
			if (context.getFileVersion() >= MOTOR_DIGEST_VERSION) {
				digest = content.trim();
			}

		} else if (element.equals("diameter")) {

			// Diameter
			diameter = Double.NaN;
			try {
				diameter = Double.parseDouble(content.trim());
			} catch (NumberFormatException e) {
				// Ignore
			}
			if (Double.isNaN(diameter)) {
				warnings.add(Warning.fromString("Illegal motor diameter specified, ignoring."));
			}

		} else if (element.equals("length")) {

			// Length
			length = Double.NaN;
			try {
				length = Double.parseDouble(content.trim());
			} catch (NumberFormatException ignore) {
			}

			if (Double.isNaN(length)) {
				warnings.add(Warning.fromString("Illegal motor diameter specified, ignoring."));
			}

		} else if (element.equals("delay")) {

			// Delay
			delay = Double.NaN;
			if (content.equals("none")) {
				delay = Motor.PLUGGED_DELAY;
			} else {
				try {
					delay = Double.parseDouble(content.trim());
				} catch (NumberFormatException ignore) {
				}

				if (Double.isNaN(delay)) {
					warnings.add(Warning.fromString("Illegal motor delay specified, ignoring."));
				}

			}

		} else if (element.equals("standarddelays")) {
			// Parse a comma-separated list of delays; accept "none"/"p"/"plugged" as a plugged motor delay.
			standardDelays.clear();
			if (content.isEmpty()) {
				return;
			}
			for (String s : content.split(",")) {
				String token = s.trim();
				if (token.isEmpty()) {
					continue;
				}
				if (token.equalsIgnoreCase("none") || token.equalsIgnoreCase("p") ||
						token.equalsIgnoreCase("plugged")) {
					standardDelays.add(Motor.PLUGGED_DELAY);
					continue;
				}
				try {
					standardDelays.add(Double.parseDouble(token));
				} catch (NumberFormatException ignore) {
					warnings.add(Warning.fromString("Illegal motor standard delay specified, ignoring."));
				}
			}

		} else if (element.equals("thrustcurvepoint")) {
			// Parse "time,thrust,cg_x,mass" (s, N, m, kg) into lists.
			if (content.isEmpty()) {
				return;
			}
			String[] parts = content.split("\\s*,\\s*");
			if (parts.length != 4) {
				warnings.add(Warning.fromString("Illegal motor thrust curve point specified, ignoring."));
				return;
			}

			try {
				thrustCurveTime.add(Double.parseDouble(parts[0]));
				thrustCurveThrust.add(Double.parseDouble(parts[1]));
				thrustCurveCGx.add(Double.parseDouble(parts[2]));
				thrustCurveMass.add(Double.parseDouble(parts[3]));
			} catch (NumberFormatException e) {
				warnings.add(Warning.fromString("Illegal motor thrust curve point specified, ignoring."));
			}

		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}

}
