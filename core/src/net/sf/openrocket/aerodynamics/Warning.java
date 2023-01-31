package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.unit.UnitGroup;

import java.util.Arrays;

public abstract class Warning implements Cloneable {
	
	/** support to multiple languages warning */
	private static final Translator trans = Application.getTranslator();

	/** The rocket component(s) that are the source of this warning **/
	private RocketComponent[] sources = null;
	
	/**
	 * @return a Warning with the specific text.
	 */
	public static Warning fromString(String text) {
		return new Warning.Other(text);
	}

	/**
	 * Returns the warning text + warning source objects.
	 * @return the warning text + warning source objects.
	 */
	@Override
	public String toString() {
		return Warning.addSourcesToWarningText(getWarningDescription(), getSources());
	}

	/**
	 * Returns the warning text.  The text should be short and descriptive.
	 * @return the warning text.
	 */
	public abstract String getWarningDescription();
	
	/**
	 * Return <code>true</code> if the <code>other</code> warning should replace
	 * this warning.  The method should return <code>true</code> if the other
	 * warning indicates a "worse" condition than the current warning.
	 * 
	 * @param other  the warning to compare to
	 * @return       whether this warning should be replaced
	 */
	public abstract boolean replaceBy(Warning other);

	/**
	 * Return the rocket component(s) that are the source of this warning.
	 * @return the rocket component(s) that are the source of this warning. Returns null if no sources are specified.
	 */
	public RocketComponent[] getSources() {
		return sources;
	}

	/**
	 * Set the rocket component(s) that are the source of this warning.
	 * @param sources the rocket component(s) that are the source of this warning.
	 */
	public void setSources(RocketComponent[] sources) {
		this.sources = sources;
	}

	private static String addSourcesToWarningText(String text, RocketComponent[] sources) {
		if (sources != null && sources.length > 0) {
			String[] sourceNames = new String[sources.length];
			for (int i = 0; i < sources.length; i++) {
				sourceNames[i] = sources[i].getName();
			}
			return text + ": \"" + String.join(", ", sourceNames) + "\"";
		}
		return text;
	}

	/**
	 * Two <code>Warning</code>s are by default considered equal if they are of
	 * the same class.  Therefore only one instance of a particular warning type 
	 * is stored in a {@link WarningSet}.  Subclasses may override this method for
	 * more specific functionality.
	 */
	@Override
	public boolean equals(Object o) {
		return o != null && (o.getClass() == this.getClass()) && sourcesEqual(this.sources, ((Warning) o).sources);
	}

	protected boolean sourcesEqual(RocketComponent[] a, RocketComponent[] b) {
		return Arrays.equals(a, b);
	}
	
	/**
	 * A <code>hashCode</code> method compatible with the <code>equals</code> method.
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Warning clone = (Warning) super.clone();
		clone.sources = this.sources;
		return clone;
	}
	
	
	/////////////  Specific warning classes  /////////////
	
	
	/**
	 * A <code>Warning</code> indicating a large angle of attack was encountered.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public static class LargeAOA extends Warning {
		private double aoa;
		
		/**
		 * Sole constructor.  The argument is the AOA that caused this warning.
		 * 
		 * @param aoa  the angle of attack that caused this warning
		 */
		public LargeAOA(double aoa) {
			this.aoa = aoa;
		}

		@Override
		public String getWarningDescription() {
			if (Double.isNaN(aoa))
				//// Large angle of attack encountered.
				return trans.get("Warning.LargeAOA.str1");
			//// Large angle of attack encountered (
			return (trans.get("Warning.LargeAOA.str2") +
					UnitGroup.UNITS_ANGLE.getDefaultUnit().toString(aoa) + ").");
		}

		@Override
		public boolean replaceBy(Warning other) {
			if (!(other instanceof LargeAOA))
				return false;
			
			LargeAOA o = (LargeAOA) other;
			if (Double.isNaN(this.aoa)) // If this has value NaN then replace
				return true;
			return (o.aoa > this.aoa);
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			LargeAOA clone = (LargeAOA) super.clone();
			clone.aoa = this.aoa;
			return clone;
		}
	}
	
	/**
	 * A <code>Warning</code> indicating recovery device deployment at high speed was encountered.
	 * 
	 * @author Craig Earls <enderw88@gmail.com>
	 */
	public static class HighSpeedDeployment extends Warning {
		private double recoverySpeed;
		
		/**
		 * Sole constructor.  The argument is the speed that caused this warning.
		 * 
		 * @param speed  the speed that caused this warning
		 */
		public HighSpeedDeployment(double speed) {
			this.recoverySpeed = speed;
		}

		@Override
		public String getWarningDescription() {
			if (Double.isNaN(recoverySpeed)) {
				return trans.get("Warning.RECOVERY_HIGH_SPEED");
			}
			return trans.get("Warning.RECOVERY_HIGH_SPEED") + " (" + UnitGroup.UNITS_VELOCITY.toStringUnit(recoverySpeed) + ")";
		}

		@Override
		public boolean replaceBy(Warning other) {
			return false;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			HighSpeedDeployment clone = (HighSpeedDeployment) super.clone();
			clone.recoverySpeed = this.recoverySpeed;
			return clone;
		}
	}

	/**
	 * A <code>Warning</code> indicating flight events occurred after ground hit
	 *
	 */
	public static class EventAfterLanding extends Warning {
		private FlightEvent event;
		
		/**
		 * Sole constructor.  The argument is an event which has occurred after landing
		 *
		 * @param _event the event that caused this warning
		 */
		public EventAfterLanding(FlightEvent _event)  {
			this.event = _event;
		}

		// I want a warning on every event that occurs after we land,
		// so severity of problem is clear to the user
		@Override
		public boolean equals(Object o) {
			return false;
		}
		

		@Override
		public String getWarningDescription() {
			return trans.get("Warning.EVENT_AFTER_LANDING") + event.getType();
		}

		@Override
		public boolean replaceBy(Warning other) {
			return false;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			EventAfterLanding clone = (EventAfterLanding) super.clone();
			clone.event = this.event;
			return clone;
		}
	}
	
	public static class MissingMotor extends Warning {
		
		private Motor.Type type = null;
		private String manufacturer = null;
		private String designation = null;
		private String digest = null;
		private double diameter = Double.NaN;
		private double length = Double.NaN;
		private double delay = Double.NaN;
		
		@Override
		public String getWarningDescription() {
			String str = "No motor with designation '" + designation + "'";
			if (manufacturer != null)
				str += " for manufacturer '" + manufacturer + "'";
			str += " found.";
			return str;
		}
		
		public Motor.Type getType() {
			return type;
		}
		
		
		public void setType(Motor.Type type) {
			this.type = type;
		}
		
		
		public String getManufacturer() {
			return manufacturer;
		}
		
		
		public void setManufacturer(String manufacturer) {
			this.manufacturer = manufacturer;
		}
		
		
		public String getDesignation() {
			return designation;
		}
		
		
		public void setDesignation(String designation) {
			this.designation = designation;
		}
		
		
		public String getDigest() {
			return digest;
		}
		
		
		public void setDigest(String digest) {
			this.digest = digest;
		}
		
		
		public double getDiameter() {
			return diameter;
		}
		
		
		public void setDiameter(double diameter) {
			this.diameter = diameter;
		}
		
		
		public double getLength() {
			return length;
		}
		
		
		public void setLength(double length) {
			this.length = length;
		}
		
		
		public double getDelay() {
			return delay;
		}
		
		
		public void setDelay(double delay) {
			this.delay = delay;
		}
		
		
		@Override
		public boolean replaceBy(Warning other) {
			return false;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(delay);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((designation == null) ? 0 : designation.hashCode());
			temp = Double.doubleToLongBits(diameter);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((digest == null) ? 0 : digest.hashCode());
			temp = Double.doubleToLongBits(length);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((manufacturer == null) ? 0 : manufacturer.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MissingMotor other = (MissingMotor) obj;
			if (Double.doubleToLongBits(delay) != Double
					.doubleToLongBits(other.delay))
				return false;
			if (designation == null) {
				if (other.designation != null)
					return false;
			} else if (!designation.equals(other.designation))
				return false;
			if (Double.doubleToLongBits(diameter) != Double
					.doubleToLongBits(other.diameter))
				return false;
			if (digest == null) {
				if (other.digest != null)
					return false;
			} else if (!digest.equals(other.digest))
				return false;
			if (Double.doubleToLongBits(length) != Double
					.doubleToLongBits(other.length))
				return false;
			if (manufacturer == null) {
				if (other.manufacturer != null)
					return false;
			} else if (!manufacturer.equals(other.manufacturer))
				return false;
			if (type != other.type)
				return false;
			if (!sourcesEqual(other.getSources(), this.getSources())) {
				return false;
			}
			return true;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			MissingMotor clone = (MissingMotor) super.clone();
			clone.type = this.type;
			clone.manufacturer = this.manufacturer;
			clone.designation = this.designation;
			clone.digest = this.digest;
			clone.diameter = this.diameter;
			clone.length = this.length;
			clone.delay = this.delay;
			return clone;
		}
	}
	
	
	/**
	 * An unspecified warning type.  This warning type holds a <code>String</code>
	 * describing it.  Two warnings of this type are considered equal if the strings
	 * are identical.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public static class Other extends Warning {
		private String description;
		
		public Other(String description) {
			this.description = description;
		}
		
		@Override
		public String getWarningDescription() {
			return description;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Other))
				return false;
			
			Other o = (Other) other;
			return o.description.equals(this.description) && sourcesEqual(o.getSources(), this.getSources());
		}
		
		@Override
		public int hashCode() {
			return description.hashCode();
		}
		
		@Override
		public boolean replaceBy(Warning other) {
			return false;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			Other clone = (Other) super.clone();
			clone.description = this.description;
			return clone;
		}
	}
	
	
	/** A <code>Warning</code> that the body diameter is discontinuous. */
	////Discontinuity in rocket body diameter.
	public static final Warning DIAMETER_DISCONTINUITY = new Other(trans.get("Warning.DISCONTINUITY"));

	/** A <code>Warning</code> that a ComponentAssembly has an open forward end */	
	public static final Warning OPEN_AIRFRAME_FORWARD = new Other(trans.get("Warning.OPEN_AIRFRAME_FORWARD"));

	/** A <code>Warning</code> that there is a gap in the airframe */
	public static final Warning AIRFRAME_GAP = new Other(trans.get("Warning.AIRFRAME_GAP"));

	/** A <code>Warning</code> that there are overlapping airframe components */
	public static final Warning AIRFRAME_OVERLAP = new Other(trans.get("Warning.AIRFRAME_OVERLAP"));

	/** A <code>Warning</code> that an inline podset is completely forward of its parent component */
	public static final Warning PODSET_FORWARD = new Other(trans.get("Warning.PODSET_FORWARD"));

	/** A <code>Warning</code> that an inline podset overlaps its parent component */
	public static final Warning PODSET_OVERLAP = new Other(trans.get("Warning.PODSET_OVERLAP"));
	
	/** A <code>Warning</code> that the fins are thick compared to the rocket body. */
	////Thick fins may not be modeled accurately.
	public static final Warning THICK_FIN = new Other(trans.get("Warning.THICK_FIN"));
	
	/** A <code>Warning</code> that the fins have jagged edges. */
	////Jagged-edged fin predictions may be inaccurate.
	public static final Warning JAGGED_EDGED_FIN = new Other(trans.get("Warning.JAGGED_EDGED_FIN"));
	
	/** A <code>Warning</code> that simulation listeners have affected the simulation */
	////Listeners modified the flight simulation
	public static final Warning LISTENERS_AFFECTED = new Other(trans.get("Warning.LISTENERS_AFFECTED"));
	
	////Recovery device opened while motor still burning.
	public static final Warning RECOVERY_DEPLOYMENT_WHILE_BURNING = new Other(trans.get("Warning.RECOVERY_DEPLOYMENT_WHILE_BURNING"));
	
	////No recovery device for simulation
	public static final Warning NO_RECOVERY_DEVICE = new Other(trans.get("Warning.NO_RECOVERY_DEVICE"));
	
	//// Invalid parameter encountered, ignoring.
	public static final Warning FILE_INVALID_PARAMETER = new Other(trans.get("Warning.FILE_INVALID_PARAMETER"));
	
	public static final Warning PARALLEL_FINS = new Other(trans.get("Warning.PARALLEL_FINS"));

	public static final Warning SUPERSONIC = new Other(trans.get("Warning.SUPERSONIC"));
	
	public static final Warning RECOVERY_LAUNCH_ROD = new Other(trans.get("Warning.RECOVERY_LAUNCH_ROD"));
	
	public static final Warning TUMBLE_UNDER_THRUST = new Other(trans.get("Warning.TUMBLE_UNDER_THRUST"));

	public static final Warning EVENT_AFTER_LANDING = new Other(trans.get("Warning.EVENT_AFTER_LANDING"));

	public static final Warning ZERO_VOLUME_BODY = new Other(trans.get("Warning.ZERO_VOLUME_BODY"));

	public static final Warning TUBE_SEPARATION = new Other(trans.get("Warning.TUBE_SEPARATION"));
	public static final Warning TUBE_OVERLAP = new Other(trans.get("Warning.TUBE_OVERLAP"));

	public static final Warning SEPARATION_ORDER = new Other(trans.get("Warning.SEPARATION_ORDER"));

	public static final Warning EMPTY_BRANCH = new Other(trans.get("Warning.EMPTY_BRANCH"));
}
