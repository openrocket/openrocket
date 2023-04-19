package net.sf.openrocket.gui.main;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a most-recently-used list backed by preferences.  This is modified/adapted from an example on the
 * Netbeans wiki.
 */
public class MRUDesignFile {

    public static final String MRU_FILE_LIST_PROPERTY = "MRUFileList";

    public static final int MAX_SIZE = 9;

    private List<String> mruFileList;

    private static MRUDesignFile instance; // The single instance

    private EventListenerList listenerList;

    static {
        instance = new MRUDesignFile();
    }

    /**
     * Returns the single instance, creating one if it's the first time this method is called.
     *
     * @return The single instance.
     */
    public static MRUDesignFile getInstance() {
        return instance;
    }

    /**
     * Constructor.
     */
    protected MRUDesignFile() {
        mruFileList = new ArrayList<String>(MAX_SIZE);
        listenerList = new EventListenerList();
        retrieve();
    }

    /**
     * Get the current most-recently-used list of design files.
     *
     * @return a list that contains absolute file names
     */
    public List<String> getMRUFileList() {
        return mruFileList;
    }

    /**
     * Get the last edited design file.
     *
     * @return an absolute file name, or null
     */
    public String getLastEditedDesignFile() {
        if (!mruFileList.isEmpty()) {
            return mruFileList.get(0);
        }
        else {
            return null;
        }
    }

    /**
     * Set the most-recently-used list to the given parameter and fire change events.
     *
     * @param list
     */
    public void setMRUFileList(List<String> list) {
        this.mruFileList.clear();
        this.mruFileList.addAll(list.subList(0, Math.min(list.size(), MAX_SIZE)));
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    /**
     * Remove a file from the MRU list and fire change events.
     *
     * @param absolutePath the filename to be removed
     */
    public void removeFile(String absolutePath) {
        mruFileList.remove(absolutePath);
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    /**
     * Add a file to the MRU list and fire change events.
     *
     * @param absolutePath the filename to be added
     */
    public void addFile(String absolutePath) {
        // remove the old
        mruFileList.remove(absolutePath);

        // add to the top
        mruFileList.add(0, absolutePath);
        while (mruFileList.size() > MAX_SIZE) {
            mruFileList.remove(mruFileList.size() - 1);
        }
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    /**
     * Clear all existing MRU references in the preferences backing store.
     */
    private void clear() {
        Preferences prefs = getPreferences();

        for (int i = 0; i < MAX_SIZE; i++) {
            prefs.putString(MRU_FILE_LIST_PROPERTY + i, null);
        }
    }

    /**
     * Store the MRU list into the preferences backing store.
     */
    protected void store() {
        Preferences prefs = getPreferences();

        // clear the backing store
        clear();

        for (int i = 0; i < mruFileList.size(); i++) {
            String str = mruFileList.get(i);
            prefs.putString(MRU_FILE_LIST_PROPERTY + i, str);
        }
    }

    /**
     * Load the MRU list from the backing store.
     */
    protected void retrieve() {
        mruFileList.clear();
        Preferences prefs = getPreferences();

        for (int i = 0; i < MAX_SIZE; i++) {
            String str = prefs.getString(MRU_FILE_LIST_PROPERTY + i, null);
            if (str != null) {
                mruFileList.add(str);
            }
            else {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
            }
        }
    }

    /**
     * Return the Preferences
     *
     * @return Preferences
     */
    protected final Preferences getPreferences() {
        return Application.getPreferences();
    }
}