package net.sf.openrocket.rocketvisitors;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.rocketcomponent.RocketComponent;

public class ListComponents<T extends RocketComponent> extends DepthFirstRecusiveVisitor<List<T>> {
	
	private final Class<T> componentClazz;
	protected List<T> components = new ArrayList<T>();
	
	public ListComponents(Class<T> componentClazz) {
		super();
		this.componentClazz = componentClazz;
	}
	
	@Override
	public List<T> getResult() {
		return components;
	}
	
	@Override
	protected void doAction(RocketComponent visitable) {
		if (componentClazz.isAssignableFrom(visitable.getClass())) {
			components.add((T) visitable);
		}
		
	}
}
