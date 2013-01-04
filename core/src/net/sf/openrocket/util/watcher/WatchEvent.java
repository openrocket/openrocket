package net.sf.openrocket.util.watcher;

/**
 * Mimics the JDK 7 implementation.
 * <p/>
 * <T>  the type of the context object of the event
 */
public interface WatchEvent<T> {

    /**
     * Defines the target of the event.
     *
     * @return the entity for which the event was generated
     */
    T context();

    /**
     * Defines the type of event.
     *
     * @return the kind of event
     */
    WatchEvent.Kind<T> kind();

    /**
     * Defines an API for a kind of event.
     *
     * @param <T>
     */
    static interface Kind<T> {
        String name();

        Class<T> type();
    }

    /**
     * A null-object idiom event.
     */
    public static final WatchEvent<Void> NO_EVENT = new WatchEvent<Void>() {
        @Override
        public Void context() {
            return null;
        }

        @Override
        public Kind<Void> kind() {
            return null;
        }
    };
}
