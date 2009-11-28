package net.sf.openrocket.gui.adaptors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Reflection;


/**
 * A class that adapts an isXXX/setXXX boolean variable.  It functions as an Action suitable
 * for usage in JCheckBox or JToggleButton.  You can create a suitable button with
 * <code>
 *   check = new JCheckBox(new BooleanModel(component,"Value"))
 *   check.setText("Label");
 * </code>
 * This will produce a button that uses isValue() and setValue(boolean) of the corresponding
 * component.
 * <p>
 * Additionally a number of component enabled states may be controlled by this class using
 * the method {@link #addEnableComponent(Component, boolean)}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BooleanModel extends AbstractAction implements ChangeListener {

	private final ChangeSource source;
	private final String valueName;
	
	private final Method getMethod;
	private final Method setMethod;
	private final Method getEnabled;
	
	private final List<Component> components = new ArrayList<Component>();
	private final List<Boolean> componentEnableState = new ArrayList<Boolean>();
	
	private int firing = 0;
	
	private boolean oldValue;
	private boolean oldEnabled;
	
	public BooleanModel(ChangeSource source, String valueName) {
		this.source = source;
		this.valueName = valueName;
		
		Method getter=null, setter=null;
		
		
		// Try get/is and set
		try {
			getter = source.getClass().getMethod("is" + valueName);
		} catch (NoSuchMethodException ignore) { }
		if (getter == null) {
			try {
				getter = source.getClass().getMethod("get" + valueName);
			} catch (NoSuchMethodException ignore) { }
		}
		try {
			setter = source.getClass().getMethod("set" + valueName,boolean.class);
		} catch (NoSuchMethodException ignore) { }
		
		if (getter==null || setter==null) {
			throw new IllegalArgumentException("get/is methods for boolean '"+valueName+
					"' not present in class "+source.getClass().getCanonicalName());
		}

		getMethod = getter;
		setMethod = setter;
		
		Method e = null;
		try {
			e = source.getClass().getMethod("is" + valueName + "Enabled");
		} catch (NoSuchMethodException ignore) { }
		getEnabled = e;
		
		oldValue = getValue();
		oldEnabled = getIsEnabled();
		
		this.setEnabled(oldEnabled);
		this.putValue(SELECTED_KEY, oldValue);
		
		source.addChangeListener(this);
	}
	
	public boolean getValue() {
		try {
			return (Boolean)getMethod.invoke(source);
		} catch (IllegalAccessException e) {
			throw new BugException("getMethod execution error for source "+source,e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleInvocationTargetException(e);
		}
	}
	
	public void setValue(boolean b) {
		try {
			setMethod.invoke(source, new Object[] { (Boolean)b });
		} catch (IllegalAccessException e) {
			throw new BugException("setMethod execution error for source "+source,e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleInvocationTargetException(e);
		}
	}
	
	
	/**
	 * Add a component the enabled status of which will be controlled by the value
	 * of this boolean.  The <code>component</code> will be enabled exactly when
	 * the state of this model is equal to that of <code>enableState</code>.
	 * 
	 * @param component		the component to control.
	 * @param enableState	the state in which the component should be enabled.
	 */
	public void addEnableComponent(Component component, boolean enableState) {
		components.add(component);
		componentEnableState.add(enableState);
		updateEnableStatus();
	}
	
	/**
	 * Add a component which will be enabled when this boolean is <code>true</code>.
	 * This is equivalent to <code>booleanModel.addEnableComponent(component, true)</code>.
	 * 
	 * @param component		the component to control.
	 * @see #addEnableComponent(Component, boolean)
	 */
	public void addEnableComponent(Component component) {
		addEnableComponent(component, true);
	}
	
	private void updateEnableStatus() {
		boolean state = getValue();
		
		for (int i=0; i < components.size(); i++) {
			Component c = components.get(i);
			boolean b = componentEnableState.get(i);
			c.setEnabled(state == b);
		}
	}
	
	
//	@Override
//	public boolean isEnabled() {
//		if (getEnabled == null)
//			return true;
//		try {
//			return (Boolean)getEnabled.invoke(source);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException("getEnabled execution error for source "+source,e);
//		} catch (InvocationTargetException e) {
//			throw new RuntimeException("getEnabled execution error for source "+source,e);
//		}
//	}


	private boolean getIsEnabled() {
		if (getEnabled == null)
			return true;
		try {
			return (Boolean)getEnabled.invoke(source);
		} catch (IllegalAccessException e) {
			throw new BugException("getEnabled execution error for source "+source,e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleInvocationTargetException(e);
		}
	}
	
//	@Override
//	public Object getValue(String key) {
//		if (key.equals(SELECTED_KEY)) {
//			return getValue();
//		}
//		return super.getValue(key);
//	}
//
//	@Override
//	public void putValue(String key, Object value) {
//		if (firing > 0)  // Ignore if currently firing event
//			return;
//		if (key.equals(SELECTED_KEY) && (value instanceof Boolean)) {
//			setValue((Boolean)value);
//		} else {
//			super.putValue(key, value);
//		}
//		updateEnableStatus();
//	}
	
	
	@Override
	public void stateChanged(ChangeEvent event) {
		if (firing > 0)
			return;
		
		boolean v = getValue();
		boolean e = getIsEnabled();
		if (oldValue != v) {
			oldValue = v;
			firing++;
			this.putValue(SELECTED_KEY, getValue());
//			this.firePropertyChange(SELECTED_KEY, !v, v);
			updateEnableStatus();
			firing--;
		}
		if (oldEnabled != e) {
			oldEnabled = e;
			setEnabled(e);
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (firing > 0)
			return;
		
		boolean v = (Boolean)this.getValue(SELECTED_KEY);
		if (v != oldValue) {
			firing++;
			setValue(v);
			oldValue = getValue();
			// Update all states
			this.putValue(SELECTED_KEY, oldValue);
			this.setEnabled(getIsEnabled());
			updateEnableStatus();
			firing--;
		}
	}
	
	@Override
	public String toString() {
		return "BooleanModel["+source.getClass().getCanonicalName()+":"+valueName+"]";
	}
}
