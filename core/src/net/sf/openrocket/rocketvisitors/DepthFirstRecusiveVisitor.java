package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponentVisitor;

public abstract class DepthFirstRecusiveVisitor<R> implements RocketComponentVisitor<R> {
	
	@Override
	public final void visit(RocketComponent visitable) {
		
		this.doAction(visitable);
		
		for (RocketComponent child : visitable.getChildren()) {
			this.visit(child);
		}
		
	}
	
	protected abstract void doAction(RocketComponent visitable);
	
}
