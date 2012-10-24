package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponentVisitor;

public abstract class BredthFirstRecusiveVisitor implements RocketComponentVisitor {

	@Override
	public final void visit(RocketComponent visitable) {

		for ( RocketComponent child: visitable.getChildren() ) {
			this.visit(child);
		}
		
		this.doAction(visitable);
		
	}
	
	protected abstract void doAction( RocketComponent visitable );

}
