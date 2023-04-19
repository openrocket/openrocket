package net.sf.openrocket.gui.watcher;

public interface WatchService {
	
	public abstract WatchKey register(Watchable w);
	
}