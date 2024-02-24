package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

public interface RocketComponentShapeService {
	Class<? extends RocketComponent> getShapeClass();
	RocketComponentShapes[] getShapesSide(RocketComponent component, Transformation transformation);
	RocketComponentShapes[] getShapesBack(RocketComponent component, Transformation transformation);
}
