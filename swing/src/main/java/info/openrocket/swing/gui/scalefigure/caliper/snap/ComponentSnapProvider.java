package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.List;

/**
 * Interface for components to provide snap targets for caliper lines.
 * Implementations should provide geometry edges and points that caliper lines can snap to.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public interface ComponentSnapProvider {
	
	/**
	 * Get the class of component this provider handles.
	 *
	 * @return the component class
	 */
	Class<? extends RocketComponent> getComponentClass();
	
	/**
	 * Get snap targets for a component in the given view type and caliper mode.
	 *
	 * @param component the component to get snap targets for
	 * @param viewType the current view type (SideView, TopView, BackView)
	 * @param caliperMode the current caliper mode (VERTICAL or HORIZONTAL)
	 * @param transformation the transformation from component-local to absolute coordinates
	 * @return list of snap targets, filtered by view type and caliper mode compatibility
	 */
	List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
										   RocketPanel.VIEW_TYPE viewType,
										   CaliperManager.CaliperMode caliperMode,
										   Transformation transformation);
}

