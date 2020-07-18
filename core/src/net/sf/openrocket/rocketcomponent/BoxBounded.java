package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.BoundingBox;

public interface BoxBounded {

    /**
     * Get a bounding box for a single instance of this component, from its own reference point.
     * This is expected to be compbined with a InstanceContext for bounds in the global / rocket frame.
     *
     * @return BoundingBox from the instance's reference point.
     */
    BoundingBox getInstanceBoundingBox();

}
