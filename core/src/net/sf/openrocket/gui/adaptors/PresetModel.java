package net.sf.openrocket.gui.adaptors;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.DatabaseListener;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.preset.ComponentPresetChooserDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class PresetModel extends AbstractListModel implements ComboBoxModel, ComponentChangeListener, DatabaseListener<ComponentPreset> {
	
	private static final Logger log = LoggerFactory.getLogger(PresetModel.class);
	private static final Translator trans = Application.getTranslator();
	
	private static final String NONE_SELECTED = trans.get("lbl.select");
	private static final String SELECT_DATABASE = trans.get("lbl.database");
	
	private final Component parent;
	private final RocketComponent component;
	private final OpenRocketDocument document;
	private ComponentPreset previousPreset;
	
	private List<ComponentPreset> presets;
	
	public PresetModel(Component parent, OpenRocketDocument document, RocketComponent component) {
		this.parent = parent;
		this.document = document;
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
			return NONE_SELECTED;
		}
		if (index == getSize() - 1) {
			return SELECT_DATABASE;
		}
		return presets.get(index - 1);
	}
	
	@Override
	public void setSelectedItem(Object item) {
		log.info(Markers.USER_MARKER, "User selected preset item '" + item + "' for component " + component);
		
		if (item == null) {
			throw new BugException("item is null");
		} else if (item.equals(NONE_SELECTED)) {
			component.clearPreset();
		} else if (item.equals(SELECT_DATABASE)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ComponentPresetChooserDialog dialog =
							new ComponentPresetChooserDialog(SwingUtilities.getWindowAncestor(parent), component);
					dialog.setVisible(true);
					ComponentPreset preset = dialog.getSelectedComponentPreset();
					if (preset != null) {
						setSelectedItem(preset);
					}
				}
			});
		} else {
			document.addUndoPosition("Use Preset " + component.getComponentName());
			component.loadPreset((ComponentPreset) item);
		}
	}
	
	@Override
	public Object getSelectedItem() {
		ComponentPreset preset = component.getPresetComponent();
		if (preset == null) {
			return NONE_SELECTED;
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
	
	@Override
	public void elementAdded(ComponentPreset element, Database<ComponentPreset> source) {
		presets = Application.getComponentPresetDao().listForType(component.getPresetType(), true);
		this.fireContentsChanged(this, 0, getSize());
	}
	
	@Override
	public void elementRemoved(ComponentPreset element, Database<ComponentPreset> source) {
		presets = Application.getComponentPresetDao().listForType(component.getPresetType(), true);
		this.fireContentsChanged(this, 0, getSize());
	}
	
}
