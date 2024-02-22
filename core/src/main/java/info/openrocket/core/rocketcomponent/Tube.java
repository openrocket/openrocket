package info.openrocket.core.rocketcomponent;

import info.openrocket.core.rocketcomponent.position.AxialMethod;

public abstract class Tube extends ExternalComponent implements Coaxial {

	public Tube(AxialMethod relativePosition) {
		super(relativePosition);
	}

}
