package net.sf.openrocket.util.watcher;

import java.io.File;
import java.util.HashMap;

/**
 * A kind of watched file that is a directory.
 */
public class Directory extends WatchedFile {

    /**
     * The contents.
     */
    private final HashMap<String, WatchedFile> contents = new HashMap<String, WatchedFile>();

    /**
     * Internal lock object.
     */
    private final Object lock = new Object();

    /**
     * Construct a directory to be watched.
     *
     * @param dir the directory to be watched
     *
     * @throws IllegalArgumentException if dir is null, does not exist, or is not a directory
     */
    public Directory(File dir) throws IllegalArgumentException {
        super(dir);
        if (dir == null || !dir.isDirectory() || !dir.exists()) {
            throw new IllegalArgumentException("Invalid directory.");
        }

        init();
    }

    /**
     * Initialize the directory handling.
     */
    private void init() {
        String[] result = list();
        for (String s : result) {
            File t = new File(getTarget(), s);
            if (t.exists()) {
                contents.put(s, new WatchedFile(t));
            }
        }
    }

    /**
     * Get the size of the directory's immediate contents (the number of files or subdirectories).
     *
     * @return the number of files and directories in this directory (not deep)
     */
    public int size() {
        return list().length;
    }

    /**
     * Get the list of filenames within this directory.
     *
     * @return an array of filenames
     */
    String[] list() {
        synchronized (lock) {
            return getTarget().list();
        }
    }

    /**
     * Get the watched file contents.
     *
     * @return  a map of watched files
     */
    protected final HashMap<String, WatchedFile> getContents() {
        return contents;
    }

    /**
     * Shared lock.
     *
     * @return a lock
     */
    protected final Object getLock() {
        return lock;
    }
}
