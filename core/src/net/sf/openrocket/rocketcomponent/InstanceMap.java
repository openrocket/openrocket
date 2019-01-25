package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.util.Transformation;


/**
 * 
 * @author teyrana (aka Daniel Williams) <equipoise@gmail.com> 
 *
 */
public class InstanceMap extends HashMap<RocketComponent, ArrayList<InstanceContext>> {
	
	// =========== Public Functions ========================
	
	// public InstanceMap() {}
	
	public int count(final RocketComponent key) {
		if(containsKey(key)){
			return get(key).size();
		}else {
			return 0;
		}
	}
	
	public void emplace(final RocketComponent component, boolean active, int number, final Transformation xform) {
		final RocketComponent key = component;
		
		if(!this.containsKey(component)) {
			this.put(key, new ArrayList<>());
		}

		final InstanceContext context = new InstanceContext(component, active, number, xform);
		this.get(key).add(context);
	}

	public List<InstanceContext> getInstanceContexts(final RocketComponent key) {
		return this.get(key);
	}
	
	// this is primarily for debugging.
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int outerIndex = 0;
		System.err.println(">> Printing InstanceMap:");
		for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: entrySet() ) {
			final RocketComponent key = entry.getKey();
			final ArrayList<InstanceContext> contexts = entry.getValue();
			System.err.println(String.format("....[% 2d]:[%s]", outerIndex, key.getName()));
			outerIndex++;
			
			int innerIndex = 0;
			for(InstanceContext ctxt: contexts ) {
				System.err.println(String.format("........[@% 2d][% 2d]  %s", innerIndex, ctxt.instanceNumber, ctxt.getLocation().toPreciseString()));
				innerIndex++;
			}
		}
		
		return buffer.toString();
	}
	
	// =========== Instance Member Variables ========================
	
	// =========== Private Instance Functions ========================

	
}

