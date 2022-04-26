package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.rocketcomponent.position.AxialMethod;

public abstract class Tube extends ExternalComponent implements Coaxial {
	
	public Tube(AxialMethod relativePosition) {
		super(relativePosition);
	}
	
}
