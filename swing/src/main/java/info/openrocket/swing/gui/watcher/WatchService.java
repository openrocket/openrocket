package info.openrocket.swing.gui.watcher;

public interface WatchService {
	
	public abstract WatchKey register(Watchable w);
	
}