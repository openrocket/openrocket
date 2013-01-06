package net.sf.openrocket.util.watcher;

import com.google.inject.Inject;

import net.sf.openrocket.logging.LogHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for monitoring changes to files and directories and dispatching handle events appropriately. In order to use the watcher
 * subsystem, clients may use the default change reactor (this class), or use the WatchService directly.  This class is more of a convenience
 * abstraction and doesn't necessarily represent the most efficient mechanism in all situations, but is sufficient for general purpose file watching
 * (short of using JDK 7).
 * <p/>
 * This reactor creates a new WatchService for each handler.  A handler owns the business logic to be performed whenever a state change is detected in a
 * monitored directory.  Each handler can monitor one directory (and optionally that directory's subdirectories).  It's possible to implement a
 * different reactor that allows one handler to monitor many different directories or files.
 * <p/>
 * The default polling interval is 5 seconds.  To override the polling interval a system property (openrocket.watcher.poll) can be specified.  The time
 * value must be specified in milliseconds.
 * <p/>
 * For example, to change the interval to 10 seconds: -Dopenrocket.watcher.poll=10000
 * <p/>
 * <p/>
 * Example Usage of this class:
 * <pre>
 * <code>
 *
 * class MyHandler extends WatchedEventHandler<Directory>  {
 *
 *   private Directory dirToWatch = new Directory(new File("/tmp"));
 *
 *   public Directory watchTarget() {
 *     return dirToWatch;
 *   }
 *
 *   public boolean watchRecursively() {
 *     return true;
 *   }
 *
 *   public void handleEvents(List<WatchEvent<?>> theEvents) {
 *     for (int i = 0; i < events.size(); i++) {
 *       WatchEvent<?> watchEvent = events.get(i);
 *       // process the event
 *     }
 *   }
 * }
 * </code>
 *
 * Guice (assuming that the binding is performed in an appropriate module):
 * {@literal @}Inject
 *  DirectoryChangeReactor reactor;
 *  reactor.registerHandler(new MyHandler());
 *
 * Programmatically:
 *  DirectoryChangeReactor reactor = new DirectoryChangeReactorImpl();
 *  reactor.registerHandler(new MyHandler());
 * </pre>
 */
public class DirectoryChangeReactorImpl implements DirectoryChangeReactor {

    /**
     * Property for polling frequency.
     */
    public static final String WATCHER_POLLING_INTERVAL_PROPERTY = "openrocket.watcher.poll";
    /**
     * The polling delay.  Defaults to 5 seconds.
     */
    public static final long   DEFAULT_POLLING_DELAY             = 5000;
    /**
     * The polling delay.  Defaults to 5 seconds.
     */
    private static      long   pollingDelay                      = DEFAULT_POLLING_DELAY;

    @Inject
    private LogHelper log;

    /**
     * Scheduler.
     */
    private ScheduledExecutorService scheduler;
    /**
     * The ScheduledFuture.
     */
    private ScheduledFuture          directoryPollerFuture;
    /**
     * The runnable that does most of the work.
     */
    private final DirectoryPoller                        directoryPoller = new DirectoryPoller();
    /**
     * The collection of registered handlers.  A new watch service is created for each handler.
     */
    private final Map<WatchedEventHandler, WatchService> activeWatchers  = new ConcurrentHashMap<WatchedEventHandler, WatchService>();
    /**
     * Synchronization object.
     */
    private final Object                                 lock            = new Object();

    static {
        try {
            if (System.getProperty(WATCHER_POLLING_INTERVAL_PROPERTY) != null) {
                pollingDelay = Long.parseLong(System.getProperty(WATCHER_POLLING_INTERVAL_PROPERTY));
            }
        }
        catch (Exception e) {
            pollingDelay = DEFAULT_POLLING_DELAY;
        }
    }

    /**
     * Constructor.
     */
    public DirectoryChangeReactorImpl() {
        this(LogHelper.getInstance());
    }

    /**
     * Injected constructor.
     */
    @Inject
    public DirectoryChangeReactorImpl(LogHelper theLogger) {
        log = theLogger;
        startup();
    }

    /**
     * Idempotent initialization.
     */
    private void startup() {

        synchronized (lock) {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            scheduler = Executors.newScheduledThreadPool(1);

            scheduleFuture();
        }
    }

    /**
     * Cause all watch services to close.  This method is idempotent.
     */
    public void shutdown() {
        synchronized (lock) {

            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            for (Iterator<WatchedEventHandler> iterator = activeWatchers.keySet().iterator(); iterator.hasNext(); ) {
                WatchedEventHandler next = iterator.next();
                activeWatchers.get(next).close();
                iterator.remove();
            }
        }
    }

    @Override
    public void registerHandler(WatchedEventHandler<Directory> theEventHandler) {
        synchronized (lock) {
            unregisterHandler(theEventHandler);
            final WatchService watchService = new WatchService(theEventHandler.watchRecursively());
            watchService.register(theEventHandler.watchTarget());
            activeWatchers.put(theEventHandler, watchService);
        }

    }

    @Override
    public void unregisterHandler(WatchedEventHandler<Directory> theEventHandler) {
        synchronized (lock) {
            if (activeWatchers.containsKey(theEventHandler)) {
                activeWatchers.get(theEventHandler).close();
                activeWatchers.remove(theEventHandler);
            }
        }
    }

    /**
     * Schedule the periodic poll.
     */
    private void scheduleFuture() {
        if (directoryPollerFuture != null
                && !directoryPollerFuture.isDone()
                && !directoryPollerFuture.isCancelled()) {
            directoryPollerFuture.cancel(false);
        }
        directoryPollerFuture = scheduler.schedule(directoryPoller, pollingDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * This runnable is responsible for checking all watch services for new events.
     */
    private final class DirectoryPoller implements Runnable {
        @Override
        public void run() {
            try {
                if (!activeWatchers.isEmpty()) {
                    for (Iterator<WatchedEventHandler> iterator = activeWatchers.keySet().iterator(); iterator.hasNext(); ) {
                        WatchedEventHandler next = iterator.next();
                        try {
                            WatchService watchService = activeWatchers.get(next);
                            Collection<? extends WatchService.WatchKey> watchKeyCollection = watchService.poll();
                            if (!watchKeyCollection.isEmpty()) {
                                for (WatchService.WatchKey watchKey : watchKeyCollection) {
                                    next.handleEvents(watchKey.pollEvents());
                                }
                            }
                        }
                        catch (Exception e) {
                            log.error("Error notifying handler of watch event. Removing registration.", e);
                            iterator.remove();
                        }
                    }
                }
            }
            finally {
                scheduleFuture();
            }
        }
    }
}