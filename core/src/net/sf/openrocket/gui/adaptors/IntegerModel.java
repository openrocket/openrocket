package net.sf.openrocket.gui.adaptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;


public class IntegerModel implements StateChangeListener {
	private static final Logger log = LoggerFactory.getLogger(IntegerModel.class);
	
	
	//////////// JSpinner Model ////////////
	
	private class IntegerSpinnerModel extends SpinnerNumberModel {
		@Override
		public Object getValue() {
			return IntegerModel.this.getValue();
		}
		
		@Override
		public void setValue(Object value) {
			if (firing > 0) {
				// Ignore, if called when model is sending events
				log.trace("Ignoring call to SpinnerModel setValue for " + IntegerModel.this.toString() +
						" value=" + value + ", currently firing events");
				return;
				
			}
			Number num = (Number) value;
			int newValue = num.intValue();
			log.info(Markers.USER_MARKER, "SpinnerModel setValue called for " + IntegerModel.this.toString() + " newValue=" + newValue);
			IntegerModel.this.setValue(newValue);
		}
		
		@Override
		public Object getNextValue() {
			int d = IntegerModel.this.getValue();
			if (d >= maxValue)
				return null;
			return (d + 1);
		}
		
		@Override
		public Object getPreviousValue() {
			int d = IntegerModel.this.getValue();
			if (d <= minValue)
				return null;
			return (d - 1);
		}
		
		@Override
		public void addChangeListener(ChangeListener l) {
			IntegerModel.this.addChangeListener(l);
		}
		
		@Override
		public void removeChangeListener(ChangeListener l) {
			IntegerModel.this.removeChangeListener(l);
		}
	}
	
	/**
	 * Returns a new SpinnerModel with the same base as the DoubleModel.
	 * The values given to the JSpinner are in the currently selected units.
	 * 
	 * @return  A compatibility layer for a SpinnerModel.
	 */
	public SpinnerModel getSpinnerModel() {
		return new IntegerSpinnerModel();
	}
	
	private class ValueSliderModel extends DefaultBoundedRangeModel implements BoundedRangeModel, StateChangeListener {
		ValueSliderModel(){
			super(IntegerModel.this.getValue(), 0, minValue, maxValue);
		}
		@Override
		public void setValue(int newValue) {
			IntegerModel.this.setValue(newValue);
		}

		@Override
		public int getValue(){
			return IntegerModel.this.getValue();
		}
		@Override
		public void stateChanged(EventObject e) {
			IntegerModel.this.fireStateChanged();
		}
		
		@Override
		public void addChangeListener(ChangeListener l) {
			IntegerModel.this.addChangeListener(l);
		}
		
		@Override
		public void removeChangeListener(ChangeListener l) {
			IntegerModel.this.removeChangeListener(l);
		}
		
	}
	
	/**
	 * Returns a new BoundedRangeModel with the same base as the IntegerModel.
	 * 
	 * @return  A compatibility layer for Sliders.
	 */
	public BoundedRangeModel getSliderModel(){
		return new ValueSliderModel();
	}


	////////////  Main model  /////////////
	
	/*
	 * The main model handles all values in SI units, i.e. no conversion is made within the model.
	 */

	private final ChangeSource source;
	private final String valueName;
	
	private final Method getMethod;
	private final Method setMethod;
	
	private final ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	
	private final int minValue;
	private final int maxValue;
	
	private String toString = null;
	

	private int firing = 0; //  >0 when model itself is sending events
	

	// Used to differentiate changes in valueName and other changes in the source:
	private int lastValue = 0;
	
	

	/**
	 * Generates a new DoubleModel that changes the values of the specified source.
	 * The double value is read and written using the methods "get"/"set" + valueName.
	 *  
	 * @param source Component whose parameter to use.
	 * @param valueName Name of methods used to get/set the parameter.
	 * @param min Minimum value allowed (in SI units)
	 * @param max Maximum value allowed (in SI units)
	 */
	public IntegerModel(ChangeSource source, String valueName, int min, int max) {
		this.source = source;
		this.valueName = valueName;
		
		this.minValue = min;
		this.maxValue = max;
		
		try {
			getMethod = source.getClass().getMethod("get" + valueName);
			setMethod = source.getClass().getMethod("set" + valueName, int.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get/set methods for value '" + valueName +
					"' not present in class " + source.getClass().getCanonicalName());
		}
	}
	
	public IntegerModel(ChangeSource source, String valueName, int min) {
		this(source, valueName, min, Integer.MAX_VALUE);
	}
	
	public IntegerModel(ChangeSource source, String valueName) {
		this(source, valueName, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	


	/**
	 * Returns the value of the variable.
	 */
	public int getValue() {
		try {
			return (Integer) getMethod.invoke(source);
		} catch (IllegalArgumentException e) {
			throw new BugException(e);
		} catch (IllegalAccessException e) {
			throw new BugException(e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	/**
	 * Sets the value of the variable.
	 */
	public void setValue(int v) {
		log.debug("Setting value " + v + " for " + this);
		try {
			setMethod.invoke(source, v);
		} catch (IllegalArgumentException e) {
			throw new BugException(e);
		} catch (IllegalAccessException e) {
			throw new BugException(e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	
	/**
	 * Add a listener to the model.  Adds the model as a listener to the Component if this
	 * is the first listener.
	 * @param l Listener to add.
	 */
	public void addChangeListener(EventListener l) {
		if (listeners.isEmpty()) {
			source.addChangeListener(this);
			lastValue = getValue();
		}
		
		listeners.add(l);
		log.trace(this + " adding listener (total " + listeners.size() + "): " + l);
	}
	
	/**
	 * Remove a listener from the model.  Removes the model from being a listener to the Component
	 * if this was the last listener of the model.
	 * @param l Listener to remove.
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) {
			source.removeChangeListener(this);
		}
		log.trace(this + " removing listener (total " + listeners.size() + "): " + l);
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!listeners.isEmpty()) {
			log.warn(this + " being garbage-collected while having listeners " + listeners);
		}
	};
	
	
	public void fireStateChanged() {
		EventListener[] list = listeners.toArray(new EventListener[0] );
		EventObject event = new EventObject(this);
		ChangeEvent cevent = new ChangeEvent(this);
		firing++;
		for( EventListener l : list ) {
			if ( l instanceof ChangeListener) {
				((ChangeListener)l).stateChanged(cevent);
			} else if ( l instanceof StateChangeListener ) {
				((StateChangeListener)l).stateChanged(event);
			}
		}
		firing--;
	}
	
	/**
	 * Called when the source changes.  Checks whether the modeled value has changed, and if
	 * it has, updates lastValue and generates ChangeEvents for all listeners of the model.
	 */
	@Override
	public void stateChanged(EventObject e) {
		int v = getValue();
		if (lastValue == v)
			return;
		lastValue = v;
		fireStateChanged();
	}
	
	/**
	 * Explain the DoubleModel as a String.
	 */
	@Override
	public String toString() {
		if (toString == null) {
			toString = "IntegerModel[" + source.getClass().getSimpleName() + ":" + valueName + "]";
		}
		return toString;
	}
	
}
