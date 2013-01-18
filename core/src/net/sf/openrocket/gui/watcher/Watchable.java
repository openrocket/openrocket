package net.sf.openrocket.gui.watcher;

public interface Watchable {
	
	public WatchEvent monitor();
	
	public void handleEvent(WatchEvent evt);
	
}
