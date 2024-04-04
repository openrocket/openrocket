package info.openrocket.swing.gui.adaptors;

import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Invalidatable;
import info.openrocket.core.util.Invalidator;
import info.openrocket.core.util.MemoryManagement;
import info.openrocket.core.util.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

/**
 * A helper model for invalidating value models.
 * This class has nothing to do with the actual setting/getting of the value, it's just here to avoid duplicate code
 * in invalidating value models.
 * This model should probably be used in a composition pattern, where this model is instantiated in a class.
 * This class should then delegate the methods of the interface
 * to this class.
 * Thanks, Java, for not allowing multiple inheritance.
 */
public class ModelInvalidator implements StateChangeListener, Invalidatable, ChangeSource {
	private static final Logger log = LoggerFactory.getLogger(ModelInvalidator.class);

	private final Invalidator invalidator;
	private final Object source;
	private final EventListener model;
	protected final List<EventListener> listeners = new ArrayList<>();

	public ModelInvalidator(Object source, EventListener model) {
		this.source = source;
		this.model = model;
		this.invalidator = new Invalidator(model);
	}

	/**
	 * Add a listener to the model.  Adds the model as a listener to the value source if this
	 * is the first listener.
	 * @param listener Listener to add.
	 */
	@Override
	public void addChangeListener(StateChangeListener listener) {
		addChangeListener((EventListener) listener);
	}

	public void addChangeListener(EventListener listener) {
		checkState(true);

		if (listeners.add(listener)) {
			log.trace(this + " adding listener (total " + listeners.size() + "): " + listener);
		} else {
			log.warn(this + " adding listener that was already registered: " + listener);
		}
	}

	/**
	 * Remove a listener from the model.  Removes the model from being a listener to the Component
	 * if this was the last listener of the model.
	 * @param listener Listener to remove.
	 */
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		removeChangeListener((EventListener) listener);
	}

	/**
	 * Remove a listener from the model.  Removes the model from being a listener to the Component
	 * if this was the last listener of the model.
	 * @param l Listener to remove.
	 */
	public void removeChangeListener(EventListener l) {
		checkState(false);

		if (listeners.remove(l)) {
			log.trace(this + " removing listener (total " + listeners.size() + "): " + l);
		} else {
			log.warn(this + " removing listener that was not registered: " + l);
		}
	}

	/**
	 * Check the state of this model.  If the model is invalid, throw an IllegalStateException if `errpr` is true.
	 * @param error If true, throw an IllegalStateException if the model is invalid.
	 */
	protected void checkState(boolean error) {
		invalidator.check(error);
	}

	/**
	 * Invalidates this model by removing all listeners and removing this from
	 * listening to the source.  After invalidation no listeners can be added to this
	 * model and the value cannot be set.
	 */
	@Override
	public void invalidateMe() {
		log.trace("Invalidating " + this);
		invalidator.invalidateMe();

		if (!listeners.isEmpty()) {
			log.warn("Invalidating " + this + " while still having listeners " + listeners);
		}
		listeners.clear();
		if (source instanceof ChangeSource && model instanceof StateChangeListener) {
			((ChangeSource) source).removeChangeListener((StateChangeListener) model);
		} else if (source instanceof RocketComponent && model instanceof ComponentChangeListener) {
			((RocketComponent) source).removeComponentChangeListener((ComponentChangeListener) model);
		}
		MemoryManagement.collectable(model);
		MemoryManagement.collectable(this);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!listeners.isEmpty()) {
			log.warn(model + " being garbage-collected while having listeners " + listeners);
		}
	}

	@Override
	public void stateChanged(EventObject e) {
		// Do nothing
	}
}
