package net.sf.openrocket.gui.adaptors;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;


public class BasicEnumModel<T extends Enum<T>> extends AbstractListModel implements ComboBoxModel {

	private final String nullText;
	
	private final Enum<T>[] values;
	private Enum<T> currentValue = null;
	
	public BasicEnumModel( Class<? extends Enum<T>> clazz ) {
		this(clazz, null);
	}
	
	@SuppressWarnings("unchecked")
	public BasicEnumModel(Class<? extends Enum<T>> clazz, String nullText) {
		this.values = clazz.getEnumConstants();
		this.nullText = nullText;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		currentValue = (Enum<T>) anItem;
	}

	@Override
	public Object getSelectedItem() {
		if (currentValue==null)
			return nullText;
		return currentValue;
	}

	@Override
	public Object getElementAt(int index) {
		if (values[index] == null)
			return nullText;
		return values[index];
	}

	@Override
	public int getSize() {
		return values.length;
	}

}
