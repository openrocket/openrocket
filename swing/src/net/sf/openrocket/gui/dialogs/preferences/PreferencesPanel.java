package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.LayoutManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;

public abstract class PreferencesPanel extends JPanel {
	protected static final Logger log = LoggerFactory.getLogger(PreferencesDialog.class);
	
	protected final List<DefaultUnitSelector> unitSelectors = new ArrayList<DefaultUnitSelector>();
	
	protected File defaultDirectory = null;
	protected static final Translator trans = Application.getTranslator();
	
	protected final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();

	protected JDialog parentDialog;
	protected PreferencesPanel(JDialog parent, LayoutManager layout) {
		super(layout);
		this.parentDialog=parent;
	}

	protected PreferencesPanel(LayoutManager layout) {
		super(layout);
	}

	protected class DefaultUnitSelector extends AbstractListModel<Object> implements ComboBoxModel<Object> {
		
		private final UnitGroup group;
		
		public DefaultUnitSelector(UnitGroup group) {
			this.group = group;
			unitSelectors.add(this);
		}
		
		@Override
		public Object getSelectedItem() {
			return group.getDefaultUnit();
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof Unit)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			group.setDefaultUnit(group.getUnitIndex((Unit) item));
		}
		
		@Override
		public Object getElementAt(int index) {
			return group.getUnit(index);
		}
		
		@Override
		public int getSize() {
			return group.getUnitCount();
		}
		
		
		public void fireChange() {
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}
	
	
	
	protected class PrefChoiceSelector extends AbstractListModel<Object> implements ComboBoxModel<Object> {
		private final String preference;
		private final String[] descriptions;
		
		public PrefChoiceSelector(String preference, String... descriptions) {
			this.preference = preference;
			this.descriptions = descriptions;
		}
		
		@Override
		public Object getSelectedItem() {
			return descriptions[preferences.getChoice(preference, descriptions.length, 0)];
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			int index;
			for (index = 0; index < descriptions.length; index++) {
				if (((String) item).equalsIgnoreCase(descriptions[index]))
					break;
			}
			if (index >= descriptions.length) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			
			preferences.putChoice(preference, index);
		}
		
		@Override
		public Object getElementAt(int index) {
			return descriptions[index];
		}
		
		@Override
		public int getSize() {
			return descriptions.length;
		}
	}
	
	
	protected class PrefBooleanSelector extends AbstractListModel<Object> implements ComboBoxModel<Object> {
		private final String preference;
		private final String trueDesc, falseDesc;
		private final boolean def;
		
		public PrefBooleanSelector(String preference, String falseDescription,
				String trueDescription, boolean defaultState) {
			this.preference = preference;
			this.trueDesc = trueDescription;
			this.falseDesc = falseDescription;
			this.def = defaultState;
		}
		
		@Override
		public Object getSelectedItem() {
			if (preferences.getBoolean(preference, def)) {
				return trueDesc;
			} else {
				return falseDesc;
			}
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			
			if (trueDesc.equals(item)) {
				preferences.putBoolean(preference, true);
			} else if (falseDesc.equals(item)) {
				preferences.putBoolean(preference, false);
			} else {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
		}
		
		@Override
		public Object getElementAt(int index) {
			switch (index) {
			case 0:
				return def ? trueDesc : falseDesc;
				
			case 1:
				return def ? falseDesc : trueDesc;
				
			default:
				throw new IndexOutOfBoundsException("Boolean asked for index=" + index);
			}
		}
		
		@Override
		public int getSize() {
			return 2;
		}
	}
}
