package info.openrocket.core.file.motor;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.TextUtil;

/**
 * Writes a {@link ThrustCurveMotor} as a RockSim engine file (.rse) XML string.
 * <p>
 * The produced XML can be parsed back by {@link RockSimMotorLoader}.
 * Unit conversions applied: meters → millimeters (diameter, length, CG), kilograms → grams (mass).
 */
public class RockSimMotorWriter {

	/**
	 * Generate a complete .rse XML document for the given motor.
	 *
	 * @param motor the motor to serialize
	 * @return valid .rse XML content
	 */
	public String write(ThrustCurveMotor motor) {
		StringBuilder sb = new StringBuilder();
		sb.append("<engine-database>\n");
		sb.append(" <engine-list>\n");
		writeEngine(sb, motor);
		sb.append(" </engine-list>\n");
		sb.append("</engine-database>\n");
		return sb.toString();
	}

	private void writeEngine(StringBuilder sb, ThrustCurveMotor motor) {
		double diameterMM = motor.getDiameter() * 1000.0;
		double lengthMM = motor.getLength() * 1000.0;
		double initWtGrams = motor.getLaunchMass() * 1000.0;
		double propWtGrams = motor.getPropellantMass() * 1000.0;

		sb.append("  <engine");
		sb.append(" mfg=\"").append(escapeXmlAttr(motor.getManufacturer().getSimpleName())).append("\"");
		sb.append(" code=\"").append(escapeXmlAttr(motor.getDesignation())).append("\"");
		sb.append(" Type=\"").append(motorTypeString(motor.getMotorType())).append("\"");
		sb.append(" dia=\"").append(TextUtil.doubleToString(diameterMM)).append("\"");
		sb.append(" len=\"").append(TextUtil.doubleToString(lengthMM)).append("\"");
		sb.append(" initWt=\"").append(TextUtil.doubleToString(initWtGrams)).append("\"");
		sb.append(" propWt=\"").append(TextUtil.doubleToString(propWtGrams)).append("\"");

		// Delays
		double[] delays = motor.getStandardDelays();
		if (delays != null && delays.length > 0) {
			sb.append(" delays=\"").append(delaysString(delays)).append("\"");
		}

		// Explicit mass and CG data
		sb.append(" auto-calc-mass=\"0\"");
		sb.append(" auto-calc-cg=\"0\"");

		sb.append(">\n");

		// Description
		String desc = motor.getDescription();
		if (desc != null && !desc.isEmpty()) {
			sb.append("   <comments>").append(escapeXmlContent(desc)).append("</comments>\n");
		}

		// Thrust curve data
		writeData(sb, motor);

		sb.append("  </engine>\n");
	}

	private void writeData(StringBuilder sb, ThrustCurveMotor motor) {
		double[] time = motor.getTimePoints();
		double[] thrust = motor.getThrustPoints();
		CoordinateIF[] cgPoints = motor.getCGPoints();
		int count = Math.min(time.length, Math.min(thrust.length, cgPoints.length));

		sb.append("   <data>\n");
		for (int i = 0; i < count; i++) {
			CoordinateIF cg = cgPoints[i];
			double massGrams = cg.getWeight() * 1000.0;
			double cgMM = cg.getX() * 1000.0;

			sb.append("    <eng-data");
			sb.append(" t=\"").append(TextUtil.doubleToString(time[i])).append("\"");
			sb.append(" f=\"").append(TextUtil.doubleToString(thrust[i])).append("\"");
			sb.append(" m=\"").append(TextUtil.doubleToString(massGrams)).append("\"");
			sb.append(" cg=\"").append(TextUtil.doubleToString(cgMM)).append("\"");
			sb.append("/>\n");
		}
		sb.append("   </data>\n");
	}

	private static String motorTypeString(Motor.Type type) {
		return switch (type) {
			case SINGLE -> "single-use";
			case HYBRID -> "hybrid";
			case RELOAD -> "reloadable";
			default -> "single-use";
		};
	}

	private static String delaysString(double[] delays) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < delays.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			if (delays[i] == Motor.PLUGGED_DELAY) {
				sb.append("1000");
			} else {
				sb.append(ThrustCurveMotor.getDelayString(delays[i], "1000"));
			}
		}
		return sb.toString();
	}

	private static String escapeXmlAttr(String s) {
		return s.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private static String escapeXmlContent(String s) {
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}
}
