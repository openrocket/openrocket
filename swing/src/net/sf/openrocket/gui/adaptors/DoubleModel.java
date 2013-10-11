package net.sf.openrocket.gui.adaptors;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.ExpressionParser;
import net.sf.openrocket.util.InvalidExpressionException;
import net.sf.openrocket.util.Invalidatable;
import net.sf.openrocket.util.Invalidator;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.MemoryManagement;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;


/**
 * A model connector that can read and modify any value of any ChangeSource that
 * has the appropriate get/set methods defined.  
 * 
 * The variable is defined in the constructor by providing the variable name as a string
 * (e.g. "Radius" -> getRadius()/setRadius()).  Additional scaling may be applied, e.g. a 
 * DoubleModel for the diameter can be defined by the variable "Radius" and a multiplier of 2.
 * 
 * Sub-models suitable for JSpinners and other components are available from the appropriate
 * methods.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class DoubleModel implements StateChangeListener, ChangeSource, Invalidatable {
	private static final Logger log = LoggerFactory.getLogger(DoubleModel.class);
	
	
	public static final DoubleModel ZERO = new DoubleModel(0);
	
	//////////// JSpinner Model ////////////
	
	/**
	 * Model suitable for JSpinner. 
	 * Note: Previously used using JSpinner.NumberEditor and extended SpinnerNumberModel
	 * to be compatible with the NumberEditor, but only has the necessary methods defined.
	 * This is still the design, but now extends AbstractSpinnerModel to allow other characters
	 * to be entered so that fractional units and expressions can be used.
	 */
	public class ValueSpinnerModel extends AbstractSpinnerModel implements Invalidatable {
		
		private ExpressionParser parser = new ExpressionParser();
		
		@Override
		public Object getValue() {
			return currentUnit.toString(DoubleModel.this.getValue());
		}
		
		@Override
		public void setValue(Object value) {
			if (firing > 0) {
				// Ignore, if called when model is sending events
				log.trace("Ignoring call to SpinnerModel setValue for " + DoubleModel.this.toString() +
						" value=" + value + ", currently firing events");
				return;
			}
			
			Number num = Double.NaN;
			
			// Set num if possible
			if (value instanceof Number) {
				num = (Number) value;
			}
			else if (value instanceof String) {
				try {
					String newValString = (String) value;
					num = parser.parse(newValString);
				} catch (InvalidExpressionException e) {
					// Ignore
				}
			}
			
			// Update the doublemodel with the new number or return to the last number if not possible
			if (((Double) num).isNaN()) {
				DoubleModel.this.setValue(lastValue);
				log.info(Markers.USER_MARKER, "SpinnerModel could not set value for " + DoubleModel.this.toString() + ". Could not convert " + value.toString());
			}
			else {
				double newValue = num.doubleValue();
				double converted = currentUnit.fromUnit(newValue);
				
				log.info(Markers.USER_MARKER, "SpinnerModel setValue called for " + DoubleModel.this.toString() + " newValue=" + newValue +
						" converted=" + converted);
				DoubleModel.this.setValue(converted);
			}
			
			// Force a refresh if text doesn't match up exactly with the stored value
			if (!((Double) lastValue).toString().equals(this.getValue().toString())) {
				DoubleModel.this.fireStateChanged();
				log.debug("SpinnerModel " + DoubleModel.this.toString() + " refresh forced because string did not match actual value.");
			}
		}
		
		@Override
		public Object getNextValue() {
			double d = currentUnit.toUnit(DoubleModel.this.getValue());
			double max = currentUnit.toUnit(maxValue);
			if (MathUtil.equals(d, max))
				return null;
			d = currentUnit.getNextValue(d);
			if (d > max)
				d = max;
			return d;
		}
		
		@Override
		public Object getPreviousValue() {
			double d = currentUnit.toUnit(DoubleModel.this.getValue());
			double min = currentUnit.toUnit(minValue);
			if (MathUtil.equals(d, min))
				return null;
			d = currentUnit.getPreviousValue(d);
			if (d < min)
				d = min;
			return d;
		}
		
		@Override
		public void addChangeListener(ChangeListener l) {
			DoubleModel.this.addChangeListener(l);
		}
		
		@Override
		public void removeChangeListener(ChangeListener l) {
			DoubleModel.this.removeChangeListener(l);
		}
		
		@Override
		public void invalidate() {
			DoubleModel.this.invalidate();
		}
	}
	
	/**
	 * Returns a new SpinnerModel with the same base as the DoubleModel.
	 * The values given to the JSpinner are in the currently selected units.
	 * 
	 * @return  A compatibility layer for a SpinnerModel.
	 */
	public SpinnerModel getSpinnerModel() {
		return new ValueSpinnerModel();
	}
	
	////////////  JSlider model  ////////////
	
	private class ValueSliderModel implements BoundedRangeModel, StateChangeListener, Invalidatable {
		private static final int MAX = 1000;
		
		/*
		 * Use linear scale  value = linear1 * x + linear0  when x < linearPosition
		 * Use quadratic scale  value = quad2 * x^2 + quad1 * x + quad0  otherwise
		 */
		private final boolean islinear;
		
		// Linear in range x <= linearPosition
		private final double linearPosition;
		
		// May be changing DoubleModels when using linear model
		private final DoubleModel min, mid, max;
		
		// Linear multiplier and constant
		//private final double linear1;
		//private final double linear0;
		
		// Non-linear multiplier, exponent and constant
		private double quad2, quad1, quad0;
		
		public ValueSliderModel(DoubleModel min, DoubleModel max) {
			this.islinear = true;
			linearPosition = 1.0;
			
			this.min = min;
			this.mid = max; // Never use exponential scale
			this.max = max;
			
			min.addChangeListener(this);
			max.addChangeListener(this);
			
			quad2 = quad1 = quad0 = 0; // Not used
		}
		
		
		
		/**
		 * Generate a linear model from min to max.
		 */
		public ValueSliderModel(double min, double max) {
			this.islinear = true;
			linearPosition = 1.0;
			
			this.min = new DoubleModel(min);
			this.mid = new DoubleModel(max); // Never use exponential scale
			this.max = new DoubleModel(max);
			
			quad2 = quad1 = quad0 = 0; // Not used
		}
		
		public ValueSliderModel(double min, double mid, double max) {
			this(min, 0.5, mid, max);
		}
		
		public ValueSliderModel(double min, double mid, DoubleModel max) {
			this(min, 0.5, mid, max);
		}
		
		/*
		 * v(x)  = mul * x^exp + add
		 * 
		 * v(pos)  = mul * pos^exp + add = mid
		 * v(1)    = mul + add = max
		 * v'(pos) = mul*exp * pos^(exp-1) = linearMul
		 */
		public ValueSliderModel(double min, double pos, double mid, double max) {
			this(min, pos, mid, new DoubleModel(max));
		}
		
		public ValueSliderModel(double min, double pos, double mid, DoubleModel max) {
			this.min = new DoubleModel(min);
			this.mid = new DoubleModel(mid);
			this.max = max;
			
			this.islinear = false;
			
			max.addChangeListener(this);
			
			linearPosition = pos;
			//linear0 = min;
			//linear1 = (mid-min)/pos;
			
			if (!(min < mid && mid <= max.getValue() && 0 < pos && pos < 1)) {
				throw new IllegalArgumentException("Bad arguments for ValueSliderModel " +
						"min=" + min + " mid=" + mid + " max=" + max + " pos=" + pos);
			}
			
			updateExponentialParameters();
			
		}
		
		private void updateExponentialParameters() {
			double pos = this.linearPosition;
			double myMinValue = this.min.getValue();
			double myMidValue = this.mid.getValue();
			double myMaxValue = this.max.getValue();
			/*
			 * quad2..0 are calculated such that
			 *   f(pos)  = mid      - continuity
			 *   f(1)    = max      - end point
			 *   f'(pos) = linear1  - continuity of derivative
			 */
			double delta = (myMidValue - myMinValue) / pos;
			quad2 = (myMaxValue - myMidValue - delta + delta * pos) / pow2(pos - 1);
			quad1 = (delta + 2 * (myMidValue - myMaxValue) * pos - delta * pos * pos) / pow2(pos - 1);
			quad0 = (myMidValue - (2 * myMidValue + delta) * pos + (myMaxValue + delta) * pos * pos) / pow2(pos - 1);
		}
		
		private double pow2(double x) {
			return x * x;
		}
		
		@Override
		public int getValue() {
			double value = DoubleModel.this.getValue();
			if (value <= min.getValue())
				return 0;
			if (value >= max.getValue())
				return MAX;
			
			double x;
			if (value <= mid.getValue()) {
				// Use linear scale
				//linear0 = min;
				//linear1 = (mid-min)/pos;
				
				x = (value - min.getValue()) * linearPosition / (mid.getValue() - min.getValue());
			} else {
				// Use quadratic scale
				// Further solution of the quadratic equation
				//   a*x^2 + b*x + c-value == 0
				x = (MathUtil.safeSqrt(quad1 * quad1 - 4 * quad2 * (quad0 - value)) - quad1) / (2 * quad2);
			}
			return (int) (x * MAX);
		}
		
		
		@Override
		public void setValue(int newValue) {
			if (firing > 0) {
				// Ignore loops
				log.trace("Ignoring call to SliderModel setValue for " + DoubleModel.this.toString() +
						" value=" + newValue + ", currently firing events");
				return;
			}
			
			double x = (double) newValue / MAX;
			double scaledValue;
			
			if (x <= linearPosition) {
				// Use linear scale
				//linear0 = min;
				//linear1 = (mid-min)/pos;
				
				scaledValue = (mid.getValue() - min.getValue()) / linearPosition * x + min.getValue();
			} else {
				// Use quadratic scale
				scaledValue = quad2 * x * x + quad1 * x + quad0;
			}
			
			double converted = currentUnit.fromUnit(currentUnit.round(currentUnit.toUnit(scaledValue)));
			log.info(Markers.USER_MARKER, "SliderModel setValue called for " + DoubleModel.this.toString() + " newValue=" + newValue +
					" scaledValue=" + scaledValue + " converted=" + converted);
			DoubleModel.this.setValue(converted);
		}
		
		
		// Static get-methods
		private boolean isAdjusting;
		
		@Override
		public int getExtent() {
			return 0;
		}
		
		@Override
		public int getMaximum() {
			return MAX;
		}
		
		@Override
		public int getMinimum() {
			return 0;
		}
		
		@Override
		public boolean getValueIsAdjusting() {
			return isAdjusting;
		}
		
		// Ignore set-values
		@Override
		public void setExtent(int newExtent) {
		}
		
		@Override
		public void setMaximum(int newMaximum) {
		}
		
		@Override
		public void setMinimum(int newMinimum) {
		}
		
		@Override
		public void setValueIsAdjusting(boolean b) {
			isAdjusting = b;
		}
		
		@Override
		public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
			setValueIsAdjusting(adjusting);
			setValue(value);
		}
		
		// Pass change listeners to the underlying model
		@Override
		public void addChangeListener(ChangeListener l) {
			DoubleModel.this.addChangeListener(l);
		}
		
		@Override
		public void removeChangeListener(ChangeListener l) {
			DoubleModel.this.removeChangeListener(l);
		}
		
		@Override
		public void invalidate() {
			DoubleModel.this.invalidate();
		}
		
		@Override
		public void stateChanged(EventObject e) {
			// Min or max range has changed.
			if (!islinear) {
				double midValue = (max.getValue() - min.getValue()) / 3.0;
				mid.setValue(midValue);
				updateExponentialParameters();
			}
			// Fire if not already firing
			if (firing == 0)
				fireStateChanged();
		}
	}
	
	
	public BoundedRangeModel getSliderModel(DoubleModel min, DoubleModel max) {
		return new ValueSliderModel(min, max);
	}
	
	public BoundedRangeModel getSliderModel(double min, double max) {
		return new ValueSliderModel(min, max);
	}
	
	public BoundedRangeModel getSliderModel(double min, double mid, double max) {
		return new ValueSliderModel(min, mid, max);
	}
	
	public BoundedRangeModel getSliderModel(double min, double mid, DoubleModel max) {
		return new ValueSliderModel(min, mid, max);
	}
	
	public BoundedRangeModel getSliderModel(double min, double pos, double mid, double max) {
		return new ValueSliderModel(min, pos, mid, max);
	}
	
	
	
	
	
	////////////  Action model  ////////////
	
	private class AutomaticActionModel extends AbstractAction implements StateChangeListener, Invalidatable {
		private boolean oldValue = false;
		
		public AutomaticActionModel() {
			oldValue = isAutomatic();
			addChangeListener(this);
		}
		
		
		@Override
		public boolean isEnabled() {
			return isAutomaticAvailable();
		}
		
		@Override
		public Object getValue(String key) {
			if (key.equals(Action.SELECTED_KEY)) {
				oldValue = isAutomatic();
				return oldValue;
			}
			return super.getValue(key);
		}
		
		@Override
		public void putValue(String key, Object value) {
			if (firing > 0) {
				log.trace("Ignoring call to ActionModel putValue for " + DoubleModel.this.toString() +
						" key=" + key + " value=" + value + ", currently firing events");
				return;
			}
			if (key.equals(Action.SELECTED_KEY) && (value instanceof Boolean)) {
				log.info(Markers.USER_MARKER, "ActionModel putValue called for " + DoubleModel.this.toString() +
						" key=" + key + " value=" + value);
				oldValue = (Boolean) value;
				setAutomatic((Boolean) value);
			} else {
				log.debug("Passing ActionModel putValue call to supermethod for " + DoubleModel.this.toString() +
						" key=" + key + " value=" + value);
				super.putValue(key, value);
			}
		}
		
		// Implement a wrapper to the ChangeListeners
		ArrayList<PropertyChangeListener> propertyChangeListeners =
				new ArrayList<PropertyChangeListener>();
		
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeListeners.add(listener);
			DoubleModel.this.addChangeListener(this);
		}
		
		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeListeners.remove(listener);
			if (propertyChangeListeners.isEmpty())
				DoubleModel.this.removeChangeListener(this);
		}
		
		// If the value has changed, generate an event to the listeners
		@Override
		public void stateChanged(EventObject e) {
			boolean newValue = isAutomatic();
			if (oldValue == newValue)
				return;
			PropertyChangeEvent event = new PropertyChangeEvent(this, Action.SELECTED_KEY,
					oldValue, newValue);
			oldValue = newValue;
			Object[] l = propertyChangeListeners.toArray();
			for (int i = 0; i < l.length; i++) {
				((PropertyChangeListener) l[i]).propertyChange(event);
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// Setting performed in putValue
		}
		
		@Override
		public void invalidate() {
			DoubleModel.this.invalidate();
		}
	}
	
	/**
	 * Returns a new Action corresponding to the changes of the automatic setting
	 * property of the value model.  This may be used directly with e.g. check buttons.
	 * 
	 * @return  A compatibility layer for an Action.
	 */
	public Action getAutomaticAction() {
		return new AutomaticActionModel();
	}
	
	
	
	
	
	////////////  Main model  /////////////
	
	/*
	 * The main model handles all values in SI units, i.e. no conversion is made within the model.
	 */
	
	private final ChangeSource source;
	private final String valueName;
	private final double multiplier;
	
	private final Method getMethod;
	private final Method setMethod;
	
	private final Method getAutoMethod;
	private final Method setAutoMethod;
	
	private final ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	
	private final UnitGroup units;
	private Unit currentUnit;
	
	private final double minValue;
	private double maxValue;
	
	private String toString = null;
	
	
	private int firing = 0; //  >0 when model itself is sending events
	
	
	// Used to differentiate changes in valueName and other changes in the component:
	private double lastValue = 0;
	private boolean lastAutomatic = false;
	
	private Invalidator invalidator = new Invalidator(this);
	
	
	/**
	 * Generate a DoubleModel that contains an internal double value.
	 * 
	 * @param value		the initial value.
	 */
	public DoubleModel(double value) {
		this(value, UnitGroup.UNITS_NONE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Generate a DoubleModel that contains an internal double value.
	 * 
	 * @param value		the initial value.
	 * @param unit		the unit for the value.
	 */
	public DoubleModel(double value, UnitGroup unit) {
		this(value, unit, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Generate a DoubleModel that contains an internal double value.
	 * 
	 * @param value		the initial value.
	 * @param unit		the unit for the value.
	 * @param min		minimum value.
	 */
	public DoubleModel(double value, UnitGroup unit, double min) {
		this(value, unit, min, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Generate a DoubleModel that contains an internal double value.
	 * 
	 * @param value		the initial value.
	 * @param unit		the unit for the value.
	 * @param min		minimum value.
	 * @param max		maximum value.
	 */
	public DoubleModel(double value, UnitGroup unit, double min, double max) {
		this.lastValue = value;
		this.minValue = min;
		this.maxValue = max;
		
		source = null;
		valueName = "Constant value";
		multiplier = 1;
		
		getMethod = setMethod = null;
		getAutoMethod = setAutoMethod = null;
		units = unit;
		currentUnit = units.getDefaultUnit();
	}
	
	
	/**
	 * Generates a new DoubleModel that changes the values of the specified component.
	 * The double value is read and written using the methods "get"/"set" + valueName.
	 *  
	 * @param source Component whose parameter to use.
	 * @param valueName Name of methods used to get/set the parameter.
	 * @param multiplier Value shown by the model is the value from component.getXXX * multiplier
	 * @param min Minimum value allowed (in SI units)
	 * @param max Maximum value allowed (in SI units)
	 */
	public DoubleModel(ChangeSource source, String valueName, double multiplier, UnitGroup unit,
			double min, double max) {
		this.source = source;
		this.valueName = valueName;
		this.multiplier = multiplier;
		
		this.units = unit;
		currentUnit = units.getDefaultUnit();
		
		this.minValue = min;
		this.maxValue = max;
		
		try {
			getMethod = source.getClass().getMethod("get" + valueName);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get method for value '" + valueName +
					"' not present in class " + source.getClass().getCanonicalName());
		}
		
		Method s = null;
		try {
			s = source.getClass().getMethod("set" + valueName, double.class);
		} catch (NoSuchMethodException e1) {
		} // Ignore
		setMethod = s;
		
		// Automatic selection methods
		
		Method set = null, get = null;
		
		try {
			get = source.getClass().getMethod("is" + valueName + "Automatic");
			set = source.getClass().getMethod("set" + valueName + "Automatic", boolean.class);
		} catch (NoSuchMethodException e) {
		} // ignore
		
		if (set != null && get != null) {
			getAutoMethod = get;
			setAutoMethod = set;
		} else {
			getAutoMethod = null;
			setAutoMethod = null;
		}
		
	}
	
	public DoubleModel(ChangeSource source, String valueName, double multiplier, UnitGroup unit,
			double min) {
		this(source, valueName, multiplier, unit, min, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName, double multiplier, UnitGroup unit) {
		this(source, valueName, multiplier, unit, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName, UnitGroup unit,
			double min, double max) {
		this(source, valueName, 1.0, unit, min, max);
	}
	
	public DoubleModel(ChangeSource source, String valueName, UnitGroup unit, double min) {
		this(source, valueName, 1.0, unit, min, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName, UnitGroup unit) {
		this(source, valueName, 1.0, unit, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName) {
		this(source, valueName, 1.0, UnitGroup.UNITS_NONE,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName, double min) {
		this(source, valueName, 1.0, UnitGroup.UNITS_NONE, min, Double.POSITIVE_INFINITY);
	}
	
	public DoubleModel(ChangeSource source, String valueName, double min, double max) {
		this(source, valueName, 1.0, UnitGroup.UNITS_NONE, min, max);
	}
	
	
	
	/**
	 * Returns the value of the variable (in SI units).
	 */
	public double getValue() {
		if (getMethod == null) // Constant value
			return lastValue;
		
		try {
			return (Double) getMethod.invoke(source) * multiplier;
		} catch (IllegalArgumentException e) {
			throw new BugException("Unable to invoke getMethod of " + this, e);
		} catch (IllegalAccessException e) {
			throw new BugException("Unable to invoke getMethod of " + this, e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	/**
	 * Sets the value of the variable.
	 * @param v New value for parameter in SI units.
	 */
	public void setValue(double v) {
		checkState(true);
		
		log.debug("Setting value " + v + " for " + this);
		if (setMethod == null) {
			if (getMethod != null) {
				throw new BugException("setMethod not available for variable '" + valueName +
						"' in class " + source.getClass().getCanonicalName());
			}
			lastValue = v;
			fireStateChanged();
			return;
		}
		
		try {
			setMethod.invoke(source, v / multiplier);
		} catch (IllegalArgumentException e) {
			throw new BugException("Unable to invoke setMethod of " + this, e);
		} catch (IllegalAccessException e) {
			throw new BugException("Unable to invoke setMethod of " + this, e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	/**
	 * Returns whether setting the value automatically is available.
	 */
	public boolean isAutomaticAvailable() {
		return (getAutoMethod != null) && (setAutoMethod != null);
	}
	
	/**
	 * Returns whether the value is currently being set automatically.
	 * Returns false if automatic setting is not available at all.
	 */
	public boolean isAutomatic() {
		if (getAutoMethod == null)
			return false;
		
		try {
			return (Boolean) getAutoMethod.invoke(source);
		} catch (IllegalArgumentException e) {
			throw new BugException("Method call failed", e);
		} catch (IllegalAccessException e) {
			throw new BugException("Method call failed", e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	/**
	 * Sets whether the value should be set automatically.  Simply fires a
	 * state change event if automatic setting is not available.
	 */
	public void setAutomatic(boolean auto) {
		checkState(true);
		
		if (setAutoMethod == null) {
			log.debug("Setting automatic to " + auto + " for " + this + ", automatic not available");
			fireStateChanged(); // in case something is out-of-sync
			return;
		}
		
		log.debug("Setting automatic to " + auto + " for " + this);
		lastAutomatic = auto;
		try {
			setAutoMethod.invoke(source, auto);
		} catch (IllegalArgumentException e) {
			throw new BugException(e);
		} catch (IllegalAccessException e) {
			throw new BugException(e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	
	/**
	 * Returns the current Unit.  At the beginning it is the default unit of the UnitGroup.
	 * @return The most recently set unit.
	 */
	public Unit getCurrentUnit() {
		return currentUnit;
	}
	
	/**
	 * Sets the current Unit.  The unit must be one of those included in the UnitGroup.
	 * @param u  The unit to set active.
	 */
	public void setCurrentUnit(Unit u) {
		checkState(true);
		if (currentUnit == u)
			return;
		log.debug("Setting unit for " + this + " to '" + u + "'");
		currentUnit = u;
		fireStateChanged();
	}
	
	
	/**
	 * Returns the UnitGroup associated with the parameter value.
	 *
	 * @return The UnitGroup given to the constructor.
	 */
	public UnitGroup getUnitGroup() {
		return units;
	}
	
	
	
	/**
	 * Add a listener to the model.  Adds the model as a listener to the value source if this
	 * is the first listener.
	 * @param l Listener to add.
	 */
	@Override
	public void addChangeListener(StateChangeListener l) {
		addChangeListener((EventListener) l);
	}
	
	
	/**
	 * Add a listener to the model.  Adds the model as a listener to the value source if this
	 * is the first listener.
	 * @param l Listener to add.
	 */
	public void addChangeListener(EventListener l) {
		checkState(true);
		
		if (listeners.isEmpty()) {
			if (source != null) {
				source.addChangeListener(this);
				lastValue = getValue();
				lastAutomatic = isAutomatic();
			}
		}
		
		listeners.add(l);
		log.trace(this + " adding listener (total " + listeners.size() + "): " + l);
	}
	
	/**
	 * Remove a listener from the model.  Removes the model from being a listener to the Component
	 * if this was the last listener of the model.
	 * @param l Listener to remove.
	 */
	@Override
	public void removeChangeListener(StateChangeListener l) {
		removeChangeListener((EventListener) l);
	}
	
	/**
	 * Remove a listener from the model.  Removes the model from being a listener to the Component
	 * if this was the last listener of the model.
	 * @param l Listener to remove.
	 */
	public void removeChangeListener(EventListener l) {
		checkState(false);
		
		listeners.remove(l);
		if (listeners.isEmpty() && source != null) {
			source.removeChangeListener(this);
		}
		log.trace(this + " removing listener (total " + listeners.size() + "): " + l);
	}
	
	
	/**
	 * Invalidates this model by removing all listeners and removing this from
	 * listening to the source.  After invalidation no listeners can be added to this
	 * model and the value cannot be set.
	 */
	@Override
	public void invalidate() {
		log.trace("Invalidating " + this);
		invalidator.invalidate();
		
		if (!listeners.isEmpty()) {
			log.warn("Invalidating " + this + " while still having listeners " + listeners);
		}
		listeners.clear();
		if (source != null) {
			source.removeChangeListener(this);
		}
		MemoryManagement.collectable(this);
	}
	
	
	private void checkState(boolean error) {
		invalidator.check(error);
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!listeners.isEmpty()) {
			log.warn(this + " being garbage-collected while having listeners " + listeners);
		}
	};
	
	
	/**
	 * Fire a ChangeEvent to all listeners.
	 */
	protected void fireStateChanged() {
		checkState(true);
		
		EventObject event = new EventObject(this);
		ChangeEvent cevent = new ChangeEvent(this);
		firing++;
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] ls = listeners.toArray(new EventListener[0]);
		for (EventListener l : ls) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			} else if (l instanceof ChangeListener) {
				((ChangeListener) l).stateChanged(cevent);
			}
		}
		firing--;
	}
	
	/**
	 * Called when the component changes.  Checks whether the modeled value has changed, and if
	 * it has, updates lastValue and generates ChangeEvents for all listeners of the model.
	 */
	@Override
	public void stateChanged(EventObject e) {
		checkState(true);
		
		double v = getValue();
		boolean b = isAutomatic();
		if (lastValue == v && lastAutomatic == b)
			return;
		lastValue = v;
		lastAutomatic = b;
		fireStateChanged();
	}
	
	
	/**
	 * Explain the DoubleModel as a String.
	 */
	@Override
	public String toString() {
		if (toString == null) {
			if (source == null) {
				toString = "DoubleModel[constant=" + lastValue + "]";
			} else {
				toString = "DoubleModel[" + source.getClass().getSimpleName() + ":" + valueName + "]";
			}
		}
		return toString;
	}
}
