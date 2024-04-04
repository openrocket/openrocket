package info.openrocket.core.file.wavefrontobj;

/**
 * Representation of an axis in 3D space.
 */
public enum Axis {
    X ("X"),
    X_MIN ("-X"),
    Y ("Y"),
    Y_MIN ("-Y"),
    Z ("Z"),
    Z_MIN ("-Z");

    private final String label;

    Axis(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Axis fromString(String label) {
        for (Axis axis : values()) {
            if (axis.label.equals(label)) {
                return axis;
            }
        }
        throw new IllegalArgumentException("Unknown axis: " + label);
    }

    public boolean isSameAxis(Axis other) {
        return this == other || this == getOppositeAxis(other);
    }

    public static Axis getOppositeAxis(Axis axis) {
        return switch (axis) {
            case X -> X_MIN;
            case X_MIN -> X;
            case Y -> Y_MIN;
            case Y_MIN -> Y;
            case Z -> Z_MIN;
            case Z_MIN -> Z;
            default -> throw new IllegalArgumentException("Unknown axis: " + axis);
        };
    }

    private static boolean isXAxis(Axis axis) {
        return axis == X || axis == X_MIN;
    }

    private static boolean isYAxis(Axis axis) {
        return axis == Y || axis == Y_MIN;
    }

    private static boolean isZAxis(Axis axis) {
        return axis == Z || axis == Z_MIN;
    }

    /**
     * Get the third axis given two axes, using a right-handed coordinate system.
     * @param firstAxis The first axis.
     * @param thirdAxis The third axis.
     * @return The third axis.
     */
    public static Axis getThirdAxis(Axis firstAxis, Axis thirdAxis) {
        if (isXAxis(firstAxis) && isYAxis(thirdAxis) || isYAxis(firstAxis) && isXAxis(thirdAxis)) {
            return Z;
        } else if (isXAxis(firstAxis) && isZAxis(thirdAxis) || isZAxis(firstAxis) && isXAxis(thirdAxis)) {
            return Y;
        } else if (isYAxis(firstAxis) && isZAxis(thirdAxis) || isZAxis(firstAxis) && isYAxis(thirdAxis)) {
            return X;
        } else {
            throw new IllegalArgumentException("Unknown axis: " + firstAxis + ", " + thirdAxis);
        }
    }
}
