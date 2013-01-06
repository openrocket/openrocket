package net.sf.openrocket.util.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class checks for changes in directories. Supported events: file creation, subdirectory creation, file modification, file and subdirectory
 * deletion.
 * <p/>
 * Synchronize calls to this class externally to ensure thread safety.
 */
final class DirectoryMonitor {

    /**
     * The directories being monitored.
     */
    private Set<Directory> monitored = new HashSet<Directory>();

    /**
     * Flag that indicates if subdirectories should automatically be monitored when they are created.
     */
    private boolean monitorSubdirectories = false;

    /**
     * Constructor.  Self registration is set to false.
     */
    DirectoryMonitor() {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param monitorOnCreate if true auto-monitor new subdirectories
     */
    DirectoryMonitor(boolean monitorOnCreate) {
        monitorSubdirectories = monitorOnCreate;
    }

    /**
     * Register a directory to be monitored for changes.
     *
     * @param dir directory to monitor
     */
    void register(final Directory dir) {
        if (dir != null && !monitored.contains(dir)) {
            monitored.add(dir);
            if (monitorSubdirectories) {
                recurse(dir);
            }
        }
    }

    private void recurse(final Directory dir) {
        synchronized (dir.getLock()) {
            String[] list = dir.list();
            for (String file : list) {
                final File f = new File(dir.getTarget(), file);
                if (f.isDirectory()) {
                    register(new Directory(f));
                }
            }
        }
    }

    /**
     * Clear/close and resources being monitored.
     */
    void close() {
        monitored.clear();
    }

    /**
     * Unregister a directory.
     *
     * @param dir
     */
    private void unregister(Directory dir) {
        if (dir != null) {
            monitored.remove(dir);
        }
    }

    /**
     *
     * The main business logic of this directory monitor.
     *
     * @return a WatchEvent instance or null
     */
    Collection<? extends WatchService.WatchKey> check() {
        Map<Directory, WatchKeyImpl> result = new HashMap<Directory, WatchKeyImpl>();

        for (Directory directory : monitored) {
            synchronized (directory.getLock()) {
                if (directory.exists()) {
                    Map<String, WatchedFile> watchedFiles = (Map<String, WatchedFile>) directory.getContents().clone();
                    String[] filesCurrentlyInDirectory = directory.list();
                    for (String s : filesCurrentlyInDirectory) {
                        if (directory.getContents().containsKey(s)) {
                            WatchEvent we = directory.getContents().get(s).check();
                            if (!we.equals(WatchEvent.NO_EVENT)) {
                                add(result, directory, we);
                            }
                        }
                        else {
                            final File f = new File(directory.getTarget(), s);
                            WatchedFile nf = new WatchedFile(f);
                            add(result, directory, nf.createEvent());
                            directory.getContents().put(s, nf);
                            if (f.isDirectory() && monitorSubdirectories) {
                                register(new Directory(f));
                            }
                        }
                        watchedFiles.remove(s);
                    }

                    for (String file : watchedFiles.keySet()) {
                        WatchEvent we = watchedFiles.get(file).check();
                        if (!we.equals(WatchEvent.NO_EVENT)) {
                            add(result, directory, we);
                            if (we.kind().equals(WatchEventKind.ENTRY_DELETE)) {
                                directory.getContents().remove(file);
                            }
                        }
                    }
                }
                else {
                    add(result, directory, new DirectoryWatchEvent(WatchEventKind.ENTRY_DELETE, directory.getTarget()));
                    unregister(directory);
                }
            }
        }
        return result.values();
    }

    private void add(Map<Directory, WatchKeyImpl> keyList, Directory dir, WatchEvent we) {
        WatchKeyImpl key = keyList.get(dir);
        if (key == null) {
            key = new WatchKeyImpl(dir);
            keyList.put(dir, key);
        }
        key.add(we);
    }

    /**
     * WatchKey impl.
     */
    private class WatchKeyImpl implements WatchService.WatchKey {

        /**
         * The target.
         */
        private Directory watchKey;

        /**
         * The list of events.
         */
        private List<WatchEvent<?>> list = new ArrayList<WatchEvent<?>>();

        /**
         * Constructor.
         *
         * @param theKey
         */
        WatchKeyImpl(Directory theKey) {
            watchKey = theKey;
        }

        @Override
        public void cancel() {
            unregister(watchKey);
        }

        @Override
        public List<WatchEvent<?>> pollEvents() {
            return list;
        }

        /**
         * Add an event.
         *
         * @param event an event
         */
        void add(WatchEvent<?> event) {
            list.add(event);
        }
    }

    /**
     * A class that depicts an event upon a directory.
     */
    private class DirectoryWatchEvent implements WatchEvent<File> {

        /**
         * The type of the event.
         */
        private final Kind<File> type;

        /**
         * The resource target.
         */
        private final File target;

        /**
         * Constructor.
         *
         * @param theType   the kind of the event
         * @param theTarget the target directory file
         */
        DirectoryWatchEvent(Kind<File> theType, File theTarget) {
            type = theType;
            target = theTarget;
        }

        @Override
        public File context() {
            return target;
        }

        @Override
        public Kind<File> kind() {
            return type;
        }
    }
}
