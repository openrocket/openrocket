package info.openrocket.core.rocketcomponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import info.openrocket.core.util.Transformation;

/**
 *
 * @author teyrana (aka Daniel Williams) <equipoise@gmail.com>
 *
 */
public class InstanceMap extends ConcurrentHashMap<RocketComponent, ArrayList<InstanceContext>> {

	// =========== Public Functions ========================

	// public InstanceMap() {}

	public int count(final RocketComponent key) {
		if (containsKey(key)) {
			return get(key).size();
		} else {
			return 0;
		}
	}

	public void emplace(final RocketComponent component, int number, final Transformation transform) {
		if (!containsKey(component)) {
			put(component, new ArrayList<>());
		}

		final InstanceContext context = new InstanceContext(component, number, transform);
		get(component).add(context);
	}

	public List<InstanceContext> getInstanceContexts(final RocketComponent key) {
		return get(key);
	}

	// this is primarily for debugging.
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int outerIndex = 0;
		buffer.append(">> Printing InstanceMap:\n");
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : entrySet()) {
			final RocketComponent key = entry.getKey();
			final ArrayList<InstanceContext> contexts = entry.getValue();
			buffer.append(String.format("....[% 2d]:[%s]\n", outerIndex, key.getName()));
			outerIndex++;

			int innerIndex = 0;
			for (InstanceContext ctxt : contexts) {
				buffer.append(String.format("........[@% 2d][% 2d]  %s\n", innerIndex, ctxt.instanceNumber,
						ctxt.getLocation().toPreciseString()));
				innerIndex++;
			}
		}

		return buffer.toString();
	}

	// =========== Instance Member Variables ========================

	// =========== Private Instance Functions ========================

}
