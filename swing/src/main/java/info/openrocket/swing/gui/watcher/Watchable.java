package info.openrocket.swing.gui.watcher;

public interface Watchable {
	
	public WatchEvent monitor();
	
	public void handleEvent(WatchEvent evt);
	
}
