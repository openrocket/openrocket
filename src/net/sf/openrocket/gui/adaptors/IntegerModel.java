package net.sf.openrocket.gui.adaptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Reflection;


public class IntegerModel implements ChangeListener {


	//////////// JSpinner Model ////////////
	
	private class IntegerSpinnerModel extends SpinnerNumberModel {
		@Override
		public Object getValue() {
			return IntegerModel.this.getValue();
		}

		@Override
		public void setValue(Object value) {
			if (firing > 0)   // Ignore, if called when model is sending events
				return;
			Number num = (Number)value;
			int newValue = num.intValue();
			IntegerModel.this.setValue(newValue);
			
//			try {
//				int newValue = Integer.parseInt((String)value);
//				IntegerModel.this.setValue(newValue);
//			} catch (NumberFormatException e) { 
//				IntegerModel.this.fireStateChanged();
//			};
		}
			
		@Override
		public Object getNextValue() {
			int d = IntegerModel.this.getValue();
			if (d >= maxValue)
				return null;
			return (d+1);
		}

		@Override
		public Object getPreviousValue() {
			int d = IntegerModel.this.getValue();
			if (d <= minValue)
				return null;
			return (d-1);
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
	
	


	////////////  Main model  /////////////

	/*
	 * The main model handles all values in SI units, i.e. no conversion is made within the model.
	 */
	
	private final ChangeSource source;
	private final String valueName;
	
	private final Method getMethod;
	private final Method setMethod;
	
	private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	private final int minValue;
	private final int maxValue;

	
	private int firing = 0;  //  >0 when model itself is sending events
	
	
	// Used to differentiate changes in valueName and other changes in the source:
	private int lastValue = 0;
		

	
	/**
	 * Generates a new DoubleModel that changes the values of the specified source.
	 * The double value is read and written using the methods "get"/"set" + valueName.
	 *  
	 * @param source Component whose parameter to use.
	 * @param valueName Name of metods used to get/set the parameter.
	 * @param multiplier Value shown by the model is the value from source.getXXX * multiplier
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
			setMethod = source.getClass().getMethod("set" + valueName,int.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get/set methods for value '"+valueName+
					"' not present in class "+source.getClass().getCanonicalName());
		}
	}

	public IntegerModel(ChangeSource source, String valueName, int min) {
		this(source,valueName,min,Integer.MAX_VALUE);
	}
	
	public IntegerModel(ChangeSource source, String valueName) {
		this(source,valueName,Integer.MIN_VALUE,Integer.MAX_VALUE);
	}
	

	
	
	/**
	 * Returns the value of the variable.
	 */
	public int getValue() {
		try {
			return (Integer)getMethod.invoke(source);
		} catch (IllegalArgumentException e) {
			throw new BugException(e);
		} catch (IllegalAccessException e) {
			throw new BugException(e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleInvocationTargetException(e);
		}
	}
	
	/**
	 * Sets the value of the variable.
	 */
	public void setValue(int v) {
		try {
			setMethod.invoke(source, v);
		} catch (IllegalArgumentException e) {
			throw new BugException(e);
		} catch (IllegalAccessException e) {
			throw new BugException(e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleInvocationTargetException(e);
		}
	}

	
	/**
	 * Add a listener to the model.  Adds the model as a listener to the Component if this
	 * is the first listener.
	 * @param l Listener to add.
	 */
	public void addChangeListener(ChangeListener l) {
		if (listeners.isEmpty()) {
			source.addChangeListener(this);
			lastValue = getValue();
		}

		listeners.add(l);
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
	}
	
	public void fireStateChanged() {
		Object[] l = listeners.toArray();
		ChangeEvent event = new ChangeEvent(this);
		firing++;
		for (int i=0; i<l.length; i++)
			((ChangeListener)l[i]).stateChanged(event);
		firing--;
	}

	/**
	 * Called when the source changes.  Checks whether the modeled value has changed, and if
	 * it has, updates lastValue and generates ChangeEvents for all listeners of the model.
	 */
	public void stateChanged(ChangeEvent e) {
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
		return "IntegerModel["+source.getClass().getCanonicalName()+":"+valueName+"]";
	}
	
}
