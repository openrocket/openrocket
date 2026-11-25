package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import java.util.Locale;
import java.util.Objects;

import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;

/**
 * Identifies the origin of a {@link ThrustCurveMotor} for filtering and display in the motor
 * selection dialog (e.g. ThrustCurve.org, RASAero, OpenRocket manual curves, or user thrust curves).
 */
public final class MotorSource implements Comparable<MotorSource> {

	public static final MotorSource USER_DEFINED = new MotorSource(true, "", "");
	public static final MotorSource UNKNOWN = new MotorSource(false, "", "");

	private static final String THRUSTCURVE_KEY = "thrustcurve.org";
	private static final String MANUAL_KEY = "manual";

	private final boolean userDefined;
	private final String normalizedKey;
	private final String rawSource;

	private MotorSource(boolean userDefined, String normalizedKey, String rawSource) {
		this.userDefined = userDefined;
		this.normalizedKey = normalizedKey;
		this.rawSource = rawSource;
	}

	/**
	 * Returns the {@link MotorSource} for the given motor.
	 */
	public static MotorSource of(ThrustCurveMotor motor) {
		if (motor == null) {
			return UNKNOWN;
		}
		if (motor.isUserDefined()) {
			return USER_DEFINED;
		}
		return forDatabaseSource(motor.getDataSource());
	}

	/**
	 * Returns a {@link MotorSource} representing a non-user database source string.
	 */
	public static MotorSource forDatabaseSource(String dataSource) {
		if (dataSource == null) {
			return UNKNOWN;
		}
		String trimmed = dataSource.trim();
		if (trimmed.isEmpty()) {
			return UNKNOWN;
		}
		return new MotorSource(false, trimmed.toLowerCase(Locale.ROOT), trimmed);
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public String getDisplayName() {
		if (userDefined) {
			return Application.getTranslator().get("TCMotorSelPan.source.user");
		}
		if (normalizedKey.isEmpty()) {
			return Application.getTranslator().get("TCMotorSelPan.source.unknown");
		}
		if (THRUSTCURVE_KEY.equals(normalizedKey)) {
			return "ThrustCurve.org";
		}
		if (MANUAL_KEY.equals(normalizedKey)) {
			return Application.getTranslator().get("TCMotorSelPan.source.manual");
		}
		return rawSource;
	}

	private int sortRank() {
		if (!userDefined && THRUSTCURVE_KEY.equals(normalizedKey)) {
			return 0;
		}
		if (!userDefined && !normalizedKey.isEmpty()) {
			return 1;
		}
		if (userDefined) {
			return 2;
		}
		return 3;
	}

	@Override
	public int compareTo(MotorSource other) {
		int rankCmp = Integer.compare(this.sortRank(), other.sortRank());
		if (rankCmp != 0) {
			return rankCmp;
		}
		return this.getDisplayName().compareToIgnoreCase(other.getDisplayName());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MotorSource other)) {
			return false;
		}
		return userDefined == other.userDefined && normalizedKey.equals(other.normalizedKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userDefined, normalizedKey);
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
