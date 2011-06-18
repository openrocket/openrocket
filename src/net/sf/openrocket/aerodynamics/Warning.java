package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public abstract class Warning {
	
	private static final Translator trans = Application.getTranslator();

	/**
	 * Return a Warning with the specific text.
	 */
	public static Warning fromString(String text) {
		return new Warning.Other(text);
	}
	
	
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
	 * Two <code>Warning</code>s are by default considered equal if they are of
	 * the same class.  Therefore only one instance of a particular warning type 
	 * is stored in a {@link WarningSet}.  Subclasses may override this method for
	 * more specific functionality.
	 */
	@Override
	public boolean equals(Object o) {
		return (o.getClass() == this.getClass());
	}
	
	/**
	 * A <code>hashCode</code> method compatible with the <code>equals</code> method.
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
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
		public String toString() {
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
			
			LargeAOA o = (LargeAOA)other;
			if (Double.isNaN(this.aoa))   // If this has value NaN then replace
				return true;
			return (o.aoa > this.aoa);
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
		public String toString() {
			return description;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Other))
				return false;
			
			Other o = (Other)other;
			return (o.description.equals(this.description));
		}
		
		@Override
		public int hashCode() {
			return description.hashCode();
		}

		@Override
		public boolean replaceBy(Warning other) {
			return false;
		}
	}
	
	
	/** A <code>Warning</code> that the body diameter is discontinuous. */
////Discontinuity in rocket body diameter.
	public static final Warning DISCONTINUITY = 
		new Other(trans.get("Warning.DISCONTINUITY"));
	
	/** A <code>Warning</code> that the fins are thick compared to the rocket body. */
////Thick fins may not be modeled accurately.
	public static final Warning THICK_FIN =
		new Other(trans.get("Warning.THICK_FIN"));
	
	/** A <code>Warning</code> that the fins have jagged edges. */
////Jagged-edged fin predictions may be inaccurate.
	public static final Warning JAGGED_EDGED_FIN =
		new Other(trans.get("Warning.JAGGED_EDGED_FIN"));
	
	/** A <code>Warning</code> that simulation listeners have affected the simulation */
////Listeners modified the flight simulation
	public static final Warning LISTENERS_AFFECTED =
		new Other(trans.get("Warning.LISTENERS_AFFECTED"));
	
////Recovery device opened while motor still burning.
	public static final Warning RECOVERY_DEPLOYMENT_WHILE_BURNING =
		new Other(trans.get("Warning.RECOVERY_DEPLOYMENT_WHILE_BURNING"));
	
	
	//// Invalid parameter encountered, ignoring.
	public static final Warning FILE_INVALID_PARAMETER =
		new Other(trans.get("Warning.FILE_INVALID_PARAMETER"));
}
