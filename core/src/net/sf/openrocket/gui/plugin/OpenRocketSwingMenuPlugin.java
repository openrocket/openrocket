package net.sf.openrocket.gui.plugin;

import java.util.List;

import net.sf.openrocket.plugin.framework.Service;

public abstract class OpenRocketSwingMenuPlugin implements Service, SwingMenuPlugin {
	
	@Override
	public String[] getMenuPosition() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public <E> List<E> getPlugins(Class<E> type, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
