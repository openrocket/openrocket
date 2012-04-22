/*
 * RocksimNoseConeCode.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.rocketcomponent.Transition;

import java.util.HashSet;
import java.util.Set;

/**
 * Models the nose cone shape of a rocket.  Maps from Rocksim's notion to OpenRocket's.
 */
public enum RocksimNoseConeCode {
    CONICAL(0, Transition.Shape.CONICAL, "Conic", "Cone"),
    OGIVE(1, Transition.Shape.OGIVE),
    PARABOLIC(2, Transition.Shape.ELLIPSOID),  //Rocksim' PARABOLIC most closely resembles an ELLIPSOID in OpenRocket
    ELLIPTICAL(3, Transition.Shape.ELLIPSOID),
    POWER_SERIES(4, Transition.Shape.POWER),
    PARABOLIC_SERIES(5, Transition.Shape.PARABOLIC),
    HAACK(6, Transition.Shape.HAACK);

    /**
     * The Rocksim enumeration value. Sent in XML.
     */
    private final int ordinal;

    /**
     * The corresponding OpenRocket shape.
     */
    private final Transition.Shape shape;

    /**
     * Names of the shape that are sometimes found in NCDATA.CSV
     */
    private Set<String> shapeNames = new HashSet<String>();

    /**
     * Constructor.
     *
     * @param idx           the Rocksim shape code
     * @param aShape        the corresponding OpenRocket shape
     * @param theShapeNames an array of alternate names
     */
    private RocksimNoseConeCode(int idx, Transition.Shape aShape, String... theShapeNames) {
        ordinal = idx;
        shape = aShape;
        shapeNames.add(this.name().toLowerCase());
        if (theShapeNames != null) {
            for (String theShapeName : theShapeNames) {
                shapeNames.add(theShapeName.toLowerCase());
            }
        }
    }

    /**
     * Get the OpenRocket shape that corresponds to the Rocksim shape.
     *
     * @return a shape
     */
    public Transition.Shape asOpenRocket() {
        return shape;
    }

    /**
     * Lookup an instance of this enum based upon the Rocksim code.
     *
     * @param rocksimShapeCode the Rocksim code (from XML)
     * @return an instance of this enum
     */
    public static RocksimNoseConeCode fromCode(int rocksimShapeCode) {
        RocksimNoseConeCode[] values = values();
        for (RocksimNoseConeCode value : values) {
            if (value.ordinal == rocksimShapeCode) {
                return value;
            }
        }
        return PARABOLIC; //Default
    }

    /**
     * Lookup an ordinal value for the Rocksim code.
     *
     * @param type the OR Shape
     * @return the Rocksim code
     */
    public static int toCode(Transition.Shape type) {
        RocksimNoseConeCode[] values = values();
        for (RocksimNoseConeCode value : values) {
            if (value.shape.equals(type)) {
                if (value.ordinal == 2) {
                    return 3;
                }
                return value.ordinal;
            }
        }
        return ELLIPTICAL.ordinal; //Default
    }

    /**
     * Given the name of a shape, map it into an instance of this enum.
     *
     * @param theName the name of the shape; case does not matter
     * @return the corresponding enum instance; defaults to PARABOLIC if not found.
     */
    public static RocksimNoseConeCode fromShapeName(String theName) {
        RocksimNoseConeCode[] values = values();
        for (RocksimNoseConeCode value : values) {
            if (value.shapeNames.contains(theName.toLowerCase())) {
                return value;
            }
        }
        return PARABOLIC; //Default
    }

    /**
     * Convenience method that determines if the parameter is an integer that refers to a shape code, or the name
     * of the shape itself.  This basically combines fromCode and fromShapeName into one method.
     *
     * @param nameOrOrdinalString the shape number or shape name
     * @return an instance of this enum; defaults to PARABOLIC if not found
     */
    public static RocksimNoseConeCode fromShapeNameOrCode(String nameOrOrdinalString) {
        try {
            return fromCode(Integer.parseInt(nameOrOrdinalString));
        }
        catch (NumberFormatException nfe) {
            return fromShapeName(nameOrOrdinalString);
        }
    }
}
