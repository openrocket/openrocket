package net.sf.openrocket.gui.components;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;


/**
 * A Swing component that allows one to choose a unit from a UnitGroup within
 * a DoubleModel model.  The current unit of the model is shown as a JLabel, and
 * the unit can be changed by clicking on the label.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class UnitSelector extends ResizeLabel implements ChangeListener, MouseListener,
		ItemSelectable {

	private final DoubleModel model;
	private final Action[] extraActions;

	private UnitGroup unitGroup;
	private Unit currentUnit;

	private final boolean showValue;

	private final Border normalBorder;
	private final Border withinBorder;


	private final List<ItemListener> itemListeners = new ArrayList<ItemListener>();


	/**
	 * Common private constructor that sets the values and sets up the borders.
	 * Either model or group must be null.
	 * 
	 * @param model
	 * @param showValue
	 * @param group
	 * @param actions
	 */
	private UnitSelector(DoubleModel model, boolean showValue, UnitGroup group,
			Action[] actions) {
		super();

		this.model = model;
		this.showValue = showValue;

		if (model != null) {
			this.unitGroup = model.getUnitGroup();
			this.currentUnit = model.getCurrentUnit();
		} else {
			this.unitGroup = group;
			this.currentUnit = group.getDefaultUnit();
		}

		this.extraActions = actions;

		addMouseListener(this);

		// Define borders to use:

		normalBorder = new CompoundBorder(
				new LineBorder(new Color(0f, 0f, 0f, 0.08f), 1), new EmptyBorder(1, 1, 1,
						1));
		withinBorder = new CompoundBorder(new LineBorder(new Color(0f, 0f, 0f, 0.6f)),
				new EmptyBorder(1, 1, 1, 1));

		setBorder(normalBorder);
		updateText();
	}



	public UnitSelector(DoubleModel model, Action... actions) {
		this(model, false, actions);
	}

	public UnitSelector(DoubleModel model, boolean showValue, Action... actions) {
		this(model, showValue, null, actions);

		// Add model listener
		this.model.addChangeListener(this);
	}


	public UnitSelector(UnitGroup group, Action... actions) {
		this(null, false, group, actions);
	}




	/**
	 * Return the DoubleModel that is backing this selector up, or <code>null</code>.
	 * Either this method or {@link #getUnitGroup()} always returns <code>null</code>.
	 * 
	 * @return		the DoubleModel being used, or <code>null</code>.
	 */
	public DoubleModel getModel() {
		return model;
	}


	/**
	 * Return the unit group that is being shown, or <code>null</code>.  Either this method
	 * or {@link #getModel()} always returns <code>null</code>.
	 * 
	 * @return		the UnitGroup being used, or <code>null</code>.
	 */
	public UnitGroup getUnitGroup() {
		return unitGroup;
	}


	public void setUnitGroup(UnitGroup group) {
		if (model != null) {
			throw new IllegalStateException(
					"UnitGroup cannot be set when backed up with model.");
		}

		if (this.unitGroup == group)
			return;

		this.unitGroup = group;
		this.currentUnit = group.getDefaultUnit();
		updateText();
	}


	/**
	 * Return the currently selected unit.  Works both when backup up with a DoubleModel
	 * and UnitGroup.
	 * 
	 * @return		the currently selected unit.
	 */
	public Unit getSelectedUnit() {
		return currentUnit;
	}


	/**
	 * Set the currently selected unit.  Sets it to the DoubleModel if it is backed up
	 * by it.
	 * 
	 * @param unit		the unit to select.
	 */
	public void setSelectedUnit(Unit unit) {
		if (!unitGroup.contains(unit)) {
			throw new IllegalArgumentException("unit " + unit
					+ " not contained in group " + unitGroup);
		}

		this.currentUnit = unit;
		if (model != null) {
			model.setCurrentUnit(unit);
		}
		updateText();
		fireItemEvent();
	}



	/**
	 * Updates the text of the label
	 */
	private void updateText() {
		if (model != null) {

			Unit unit = model.getCurrentUnit();
			if (showValue) {
				setText(unit.toStringUnit(model.getValue()));
			} else {
				setText(unit.getUnit());
			}

		} else if (unitGroup != null) {

			setText(currentUnit.getUnit());

		} else {
			throw new IllegalStateException("Both model and unitGroup are null.");
		}
	}


	/**
	 * Update the component when the DoubleModel changes.
	 */
	public void stateChanged(ChangeEvent e) {
		updateText();
	}



	////////  ItemListener handling  ////////

	public void addItemListener(ItemListener listener) {
		itemListeners.add(listener);
	}

	public void removeItemListener(ItemListener listener) {
		itemListeners.remove(listener);
	}

	protected void fireItemEvent() {
		ItemEvent event = null;
		ItemListener[] listeners = itemListeners.toArray(new ItemListener[0]);
		for (ItemListener l: listeners) {
			if (event == null) {
				event = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, getSelectedUnit(),
						ItemEvent.SELECTED);
			}
			l.itemStateChanged(event);
		}
	}



	////////  Popup  ////////

	private void popup() {
		JPopupMenu popup = new JPopupMenu();

		for (int i = 0; i < unitGroup.getUnitCount(); i++) {
			Unit unit = unitGroup.getUnit(i);
			JMenuItem item = new JMenuItem(unit.getUnit());
			item.addActionListener(new UnitSelectorItem(unit));
			popup.add(item);
		}

		for (int i = 0; i < extraActions.length; i++) {
			if (extraActions[i] == null && i < extraActions.length - 1) {
				popup.addSeparator();
			} else {
				popup.add(new JMenuItem(extraActions[i]));
			}
		}

		Dimension d = getSize();
		popup.show(this, 0, d.height);
	}


	/**
	 * ActionListener class that sets the currently selected unit.
	 */
	private class UnitSelectorItem implements ActionListener {
		private final Unit unit;

		public UnitSelectorItem(Unit u) {
			unit = u;
		}

		public void actionPerformed(ActionEvent e) {
			setSelectedUnit(unit);
		}
	}


	@Override
	public Object[] getSelectedObjects() {
		return new Object[]{ getSelectedUnit() };
	}



	////////  Mouse handling ////////

	public void mouseClicked(MouseEvent e) {
		if (unitGroup.getUnitCount() > 1)
			popup();
	}

	public void mouseEntered(MouseEvent e) {
		if (unitGroup.getUnitCount() > 1)
			setBorder(withinBorder);
	}

	public void mouseExited(MouseEvent e) {
		setBorder(normalBorder);
	}

	public void mousePressed(MouseEvent e) {
	} // Ignore

	public void mouseReleased(MouseEvent e) {
	} // Ignore

}
