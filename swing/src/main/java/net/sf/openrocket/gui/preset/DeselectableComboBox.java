package net.sf.openrocket.gui.preset;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A combo box that allows for items to be deselected.
 */
public class DeselectableComboBox<T> extends JComboBox<T> {
	private static final long serialVersionUID = 1803702330221425938L;

	@SuppressWarnings("unchecked")
	public DeselectableComboBox() {
        super();
        super.setRenderer(new DeselectedItemsRenderer());
    }

    private Set<Integer> disabled_items = new HashSet<Integer>();

    public void addItem(T anObject, boolean disabled) {
        super.addItem(anObject);
        if (disabled) {
            disabled_items.add(getItemCount() - 1);
        }
    }

    @Override
    public void removeAllItems() {
        super.removeAllItems();
        disabled_items = new HashSet<Integer>();
    }

    @Override
    public void removeItemAt(final int anIndex) {
        super.removeItemAt(anIndex);
        disabled_items.remove(anIndex);
    }

    @Override
    public void removeItem(final Object anObject) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemAt(i) == anObject) {
                disabled_items.remove(i);
            }
        }
        super.removeItem(anObject);
    }

    @Override
    public void setSelectedIndex(int index) {
        if (!disabled_items.contains(index)) {
            super.setSelectedIndex(index);
        }
    }

    private class DeselectedItemsRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 6149806777306976399L;

		 // is raw because its super-method-signature is also a raw generic
		@SuppressWarnings("rawtypes")
		@Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (disabled_items.contains(index)) {
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
