package net.sf.openrocket.gui.watcher;

import java.io.File;

public abstract class FileWatcher implements Watchable {
	
	private final File file;
	private long lastModifiedTimestamp = 0L;
	
	public FileWatcher(File file) {
		this.file = file;
	}
	
	protected File getFile() {
		return file;
	}
	
	@Override
	public WatchEvent monitor() {
		
		long modified = file.lastModified();
		if (modified == 0L) {
			// check for removal?
			return null;
		}
		if (modified > lastModifiedTimestamp) {
			long oldTimestamp = lastModifiedTimestamp;
			lastModifiedTimestamp = modified;
			return (oldTimestamp == 0L) ? null : WatchEvent.MODIFIED;
		}
		return null;
	}
	
	@Override
	public abstract void handleEvent(WatchEvent evt);
	
}
