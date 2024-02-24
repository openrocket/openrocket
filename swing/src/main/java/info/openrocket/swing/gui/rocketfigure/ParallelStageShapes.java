package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class ParallelStageShapes extends ComponentAssemblyShapes {
	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return ParallelStage.class;
	}

	// Everything is handled by the ComponentAssemblyShapes class.
}
