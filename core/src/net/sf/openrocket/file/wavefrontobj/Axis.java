package net.sf.openrocket.file.wavefrontobj;

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
}
