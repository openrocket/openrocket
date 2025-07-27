package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;

/**
 * A class for storing the minimum and maximum float tuple values to keep track of the bounds of a model.
 */
public class FloatTupleBounds {
    private FloatTuple min;
    private FloatTuple max;

    /**
     * Default constructor. Initializes the bounds to the maximum and minimum values of a float.
     */
    public FloatTupleBounds() {
        min = new DefaultFloatTuple(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max = new DefaultFloatTuple(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    }

    /**
     * Updates the bounds to the values of the given tuple.
     * @param tuple The tuple to update the bounds with.
     */
    public void updateBounds(FloatTuple tuple) {
        min = new DefaultFloatTuple(Math.min(min.getX(), tuple.getX()), Math.min(min.getY(), tuple.getY()), Math.min(min.getZ(), tuple.getZ()));
        max = new DefaultFloatTuple(Math.max(max.getX(), tuple.getX()), Math.max(max.getY(), tuple.getY()), Math.max(max.getZ(), tuple.getZ()));
    }

    /**
     * Resets the bounds to the maximum and minimum values of a float.
     */
    public void resetBounds() {
        min = new DefaultFloatTuple(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max = new DefaultFloatTuple(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    }

    public FloatTuple getMin() {
        return min;
    }

    public FloatTuple getMax() {
        return max;
    }
}
