package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class PodSetShapes extends ComponentAssemblyShapes {
	public Class<? extends RocketComponent> getShapeClass() {
		return PodSet.class;
	}

    // Everything is handled by the ComponentAssemblyShapes class.
}
