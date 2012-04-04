package net.sf.openrocket.gui.adaptors;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class PresetModel extends AbstractListModel implements ComboBoxModel, ComponentChangeListener {
	
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	
	private static final String SELECT_PRESET = trans.get("lbl.select");
	private static final String SELECT_DATABASE = trans.get("lbl.database");
	
	
	private final RocketComponent component;
	private ComponentPreset previousPreset;
	
	private final List<ComponentPreset> presets;
	
	public PresetModel(RocketComponent component) {
		presets = Application.getComponentPresetDao().listForType(component.getPresetType(), true);
		this.component = component;
		previousPreset = component.getPresetComponent();
		component.addComponentChangeListener(this);
	}
	
	@Override
	public int getSize() {
		return presets.size() + 2;
	}
	
	@Override
	public Object getElementAt(int index) {
		if (index == 0) {
			return SELECT_PRESET;
		}
		if (index == getSize() - 1) {
			return SELECT_DATABASE;
		}
		return presets.get(index - 1);
	}
	
	@Override
	public void setSelectedItem(Object item) {
		log.user("User selected preset item '" + item + "' for component " + component);
		
		if (item == null) {
			// FIXME:  What to do?
		} else if (item.equals(SELECT_PRESET)) {
			component.clearPreset();
		} else if (item.equals(SELECT_DATABASE)) {
			// FIXME:  Open database dialog
		} else {
			// FIXME: Add undo point here
			component.loadPreset((ComponentPreset) item);
		}
	}
	
	@Override
	public Object getSelectedItem() {
		ComponentPreset preset = component.getPresetComponent();
		if (preset == null) {
			return SELECT_PRESET;
		} else {
			return preset;
		}
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (previousPreset != component.getPresetComponent()) {
			previousPreset = component.getPresetComponent();
			fireContentsChanged(this, 0, getSize());
		}
	}
	
	// FIXME:  Make model invalidatable
	
}
