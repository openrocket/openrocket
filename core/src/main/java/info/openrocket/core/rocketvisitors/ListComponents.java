package info.openrocket.core.rocketvisitors;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.RocketComponent;

public class ListComponents<T extends RocketComponent> extends DepthFirstRecursiveVisitor<List<T>> {

	private final Class<T> componentClazz;
	protected List<T> components = new ArrayList<>();

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
