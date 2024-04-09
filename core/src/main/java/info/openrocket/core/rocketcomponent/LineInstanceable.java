package info.openrocket.core.rocketcomponent;

import info.openrocket.core.rocketcomponent.position.AxialPositionable;

public interface LineInstanceable extends AxialPositionable, Instanceable {

	public double getInstanceSeparation();

	public void setInstanceSeparation(final double radius);

}
