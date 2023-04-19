package net.sf.openrocket.gui.adaptors;

import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.MutableComboBoxModel;

import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;


public class EnumModel<T extends Enum<T>> extends AbstractListModel<T> 
		implements ComboBoxModel<T>, MutableComboBoxModel<T>, StateChangeListener {
	private static final long serialVersionUID = 7766446027840316797L;
	private final Object source;
	private final String valueName;
	private final String nullText;
	
	private final T[] values;
	private T currentValue = null;
	
	ArrayList<T> displayedValues = new ArrayList<T>();
	
	private final Reflection.Method getMethod;
	private final Reflection.Method setMethod;
	
	
	
	public EnumModel(Object source, String valueName) {
		this(source,valueName,null,null);
	}
	
	public EnumModel(Object source, String valueName, T[] values) {
		this(source, valueName, values, null);
	}
	
	@SuppressWarnings("unchecked")
	public EnumModel(Object source, String valueName, T[] values, String nullText) {
		Class<? extends Enum<T>> enumClass;
		this.source = source;
		this.valueName = valueName;
		
		try {
			java.lang.reflect.Method getM = source.getClass().getMethod("get" + valueName);
			enumClass = (Class<? extends Enum<T>>) getM.getReturnType();
			if (!enumClass.isEnum()) {
				throw new IllegalArgumentException("Return type of get" + valueName +
						" not an enum type");
			}
			
			getMethod = new Reflection.Method(getM);
			setMethod = new Reflection.Method(source.getClass().getMethod("set" + valueName,
					enumClass));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get/is methods for enum '"+valueName+
					"' not present in class "+source.getClass().getCanonicalName());
		}
		
		if (values != null)
			this.values = values;
		else 
			this.values = (T[]) enumClass.getEnumConstants();
		
		for (T e : this.values){
			this.displayedValues.add( e );
		}
		this.nullText = nullText;
		
		stateChanged(null);  // Update current value
	}

	
		
	@Override
	public Object getSelectedItem() {
		if (currentValue==null)
			return nullText;
		return currentValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}
		if (item instanceof String) {
			if (currentValue != null)
				setMethod.invoke(source, (Object)null);
			return;
		}
		
		if (!(item instanceof Enum<?>)) {
			throw new IllegalArgumentException("Not String or Enum, item="+item);
		}
		
		// Comparison with == ok, since both are enums
		if (currentValue == item)
			return;
		
		this.currentValue = (T) item;
		setMethod.invoke(source, item);
	}

	@Override
	public T getElementAt(int index) {
		if( ( index < 0 ) || ( index >= this.displayedValues.size())){
			return null; // bad parameter
		}

		if (values[index] == null)
			return null;
		return displayedValues.get( index);
	}

	@Override
	public int getSize() {
		return displayedValues.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void stateChanged(EventObject e) {
		T value = (T) getMethod.invoke(source);
		if (value != currentValue) {
			currentValue = value;
			this.fireContentsChanged(this, 0, values.length);
		}
	}

	@Override
	public String toString() {
		return "EnumModel["+source.getClass().getCanonicalName()+":"+valueName+"]";
	}

	@Override
	public void addElement(T item) {
		// Not actually allowed.  The model starts out with all the enums, and only allows hiding some.
	}

	@Override
	public void removeElement(Object obj) {
		if( null == obj ){
			return;
		}
		this.displayedValues.remove( obj );
	}

	@Override
	public void insertElementAt(T item, int index) {
		// Not actually allowed.  The model starts out with all the enums, and only allows hiding some.
	}

	@Override
	public void removeElementAt(int index) {
		if( ( index < 0 ) || ( index >= this.displayedValues.size())){
			return; // bad parameter
		}

		this.displayedValues.remove( index );
	}

}
