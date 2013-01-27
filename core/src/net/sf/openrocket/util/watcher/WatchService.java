package net.sf.openrocket.util.watcher;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A service that allows consumers to watch for changes to a directory or directories. This class manages the checking
 * of state changes for one or more directories. Directories are the primary entity being monitored.
 * Events detected include the creation of a file, creation of a subdirectory, the modification
 * of a file, and the deletion of a file or subdirectory.
 * <p/>
 * JDK 7 includes a WatchService, upon which this is loosely based.  This implementation is JDK 6 compatible.
 * <p/>
 * A limitation of this implementation is that it is polling based, not event driven.  Also note, that this only monitors directories.  If you want
 * to monitor an individual file, it is recommended that you monitor the directory that the file resides within,
 * then filter the WatchEvents accordingly.
 *
 * Example usage:
 * <p>
 *     <pre>
 *     WatchService watcher = new WatchService();
 *     watcher.register(new File("/tmp"));
 *     ...
 *     Collection<? extends WatchKey> changed = watcher.poll();
 *     for (Iterator<WatchKey> iterator = co.iterator(); iterator.hasNext(); ) {
 *        WatchKey key = iterator.next();
 *        //Do something with the WatchEvents in the key
 *     }
 *
 *     </pre>
 * </p>
 */
public class WatchService {

    /**
     * An interface that defines keys of events.
     */
    public static interface WatchKey {
        /**
         * Cancel the registration of the directory for which this key relates.
         */
        void cancel();

        /**
         * Get a list of events detected for this key.
         *
         * @return a list, not null, of events
         */
        List<WatchEvent<?>> pollEvents();
    }

    /**
     * The manager of all directory monitoring.
     */
    private final DirectoryMonitor monitor;

    /**
     * Constructor.
     */
    public WatchService() {
        monitor = new DirectoryMonitor();
    }

    /**
     * Constructor.
     *
     * @param watchRecursively if true, directories will be watched recursively
     */
    public WatchService(boolean watchRecursively) {
        monitor = new DirectoryMonitor(watchRecursively);
    }

    /**
     * Polling method to get a collection of keys that indicate events detected upon one or more files within a
     * monitored directory.  Each key represents one directory.  The list of events represents changes to one or more
     * files within that directory. Will not block and return immediately, even if there are no keys ready.
     *
     * @return a collection of keys; guaranteed not to be null but may be empty
     */
    public Collection<? extends WatchKey> poll() {
        return monitor.check();
    }

    /**
     * Polling method to get a collection of keys that indicate events detected upon one or more files within a
     * monitored directory.  Each key represents one directory.  The list of events represents changes to one or more
     * files within that directory.  Will block for a defined length of time if there are no keys ready.
     *
     * @param time the amount of time before the method returns
     * @param unit the unit of time
     *
     * @return a collection of keys; guaranteed not to be null but may be empty
     */
    public Collection<? extends WatchKey> poll(long time, TimeUnit unit) {
        Collection<? extends WatchKey> result = monitor.check();
        if (result != null && !result.isEmpty()) {
            return result;
        }

        try {
            Thread.sleep(unit.toMillis(time));
        } catch (InterruptedException e) {
        }
        return monitor.check();
    }

    /**
     * Blocking method to get a collection of keys that indicate events detected upon one or more files within a
     * monitored directory.  Each key represents one directory.  The list of events represents changes to one or more
     * files within that directory.
     *
     * @return a collection of keys; guaranteed not to be null but may be empty
     */
    public Collection<? extends WatchKey> take() {
        long wait = 60000;
        Collection<? extends WatchKey> result = null;
        do {
            result = poll(wait, TimeUnit.MILLISECONDS);
        } while (result == null || result.isEmpty());
        return result;
    }

    /**
     * Close the service and release any resources currently being used.
     */
    public void close() {
        monitor.close();
    }

    /**
     * Register a directory to be watched by this service.  The file f must not be null and must refer to a directory
     * that exists.  Registration is idempotent - it can be called multiple times for the same directory with no ill
     * effect.
     *
     * @param f the target directory to watch
     *
     * @throws IllegalArgumentException thrown if f is null, does not exist, or is not a directory
     */
    public void register(File f) throws IllegalArgumentException {
        monitor.register(new Directory(f));
    }

    /**
     * Register a directory to be watched by this service.  The file f must not be null and must refer to a directory
     * that exists.  Registration is idempotent - it can be called multiple times for the same directory with no ill
     * effect.
     *
     * @param dir the target directory to watch
     *
     * @throws IllegalArgumentException thrown if dir is null
     */
    public void register(Directory dir) throws IllegalArgumentException {
        if (dir == null) {
            throw new IllegalArgumentException("The directory may not be null.");
        }
        monitor.register(dir);
    }
}
