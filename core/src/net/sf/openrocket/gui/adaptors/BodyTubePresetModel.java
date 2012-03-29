package net.sf.openrocket.gui.adaptors;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class BodyTubePresetModel extends AbstractListModel implements
		ComboBoxModel, ComponentChangeListener {

	private final RocketComponent component;

	private List<ComponentPreset> presets;
	
	public BodyTubePresetModel(RocketComponent component) {
		presets = Application.getDaos().getBodyTubePresetDao().listAll();
		this.component = component;
	}
	
	public static class BodyTubePresetAdapter {
		private ComponentPreset bt;
		private BodyTubePresetAdapter( ComponentPreset bt ) {
			this.bt = bt;
		}
		@Override
		public String toString() {
			return bt.getManufacturer() + " " + bt.getPartNo();
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bt == null) ? 0 : bt.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BodyTubePresetAdapter other = (BodyTubePresetAdapter) obj;
			if (bt == null) {
				if (other.bt != null)
					return false;
			} else if (!bt.equals(other.bt))
				return false;
			return true;
		}
	}
	
	@Override
	public int getSize() {
		return presets.size();
	}

	@Override
	public Object getElementAt(int index) {
		return new BodyTubePresetAdapter(presets.get(index));
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentChanged(ComponentChangeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectedItem(Object anItem) {
		BodyTubePresetAdapter selected = (BodyTubePresetAdapter) anItem;
		component.loadPreset(selected.bt);
	}

	@Override
	public Object getSelectedItem() {
		ComponentPreset preset = (ComponentPreset) component.getPresetComponent();
		if ( preset == null ) {
			return null;
		} else {
			return new BodyTubePresetAdapter(preset);
		}
	}

}
