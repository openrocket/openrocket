package info.openrocket.core.models.wind;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.StateChangeListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

public interface WindModel extends Monitorable, Cloneable, ChangeSource {
	List<StateChangeListener> listeners = new ArrayList<>();

	Coordinate getWindVelocity(double time, double altitude);

	WindModel clone();

	@Override
	default void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	default void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	default void fireChangeEvent() {
		EventObject event = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}
}
