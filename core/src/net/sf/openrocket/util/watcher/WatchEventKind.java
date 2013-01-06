package net.sf.openrocket.util.watcher;

import java.io.File;

/**
 * Mimics the kind of watch event in JDK 7.
 */
public final class WatchEventKind {

    /**
     * An entry was created.
     */
    public static final WatchEvent.Kind<File> ENTRY_CREATE = new WatchEvent.Kind<File>() {
        @Override
        public String name() {
            return "ENTRY_CREATE";
        }

        @Override
        public Class<File> type() {
            return File.class;
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /**
     * An existing entry was deleted.
     */
    public static final WatchEvent.Kind<File> ENTRY_DELETE = new WatchEvent.Kind<File>() {
        @Override
        public String name() {
            return "ENTRY_DELETE";
        }

        @Override
        public Class<File> type() {
            return File.class;
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /**
     * An existing entry was modified.
     */
    public static final WatchEvent.Kind<File> ENTRY_MODIFY = new WatchEvent.Kind<File>() {
        @Override
        public String name() {
            return "ENTRY_MODIFY";
        }

        @Override
        public Class<File> type() {
            return File.class;
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /**
     * Disallow instantiation.
     */
    private WatchEventKind() {
    }
}
