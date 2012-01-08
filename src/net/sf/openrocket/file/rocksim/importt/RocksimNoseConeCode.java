/*
 * RocksimNoseConeCode.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.rocketcomponent.Transition;

/**
 * Models the nose cone shape of a rocket.  Maps from Rocksim's notion to OpenRocket's.
 */
public enum RocksimNoseConeCode {
    CONICAL         (0, Transition.Shape.CONICAL),
    OGIVE           (1, Transition.Shape.OGIVE),
    PARABOLIC       (2, Transition.Shape.ELLIPSOID),  //Rocksim' PARABOLIC most closely resembles an ELLIPSOID in OpenRocket
    ELLIPTICAL      (3, Transition.Shape.ELLIPSOID),
    POWER_SERIES    (4, Transition.Shape.POWER),
    PARABOLIC_SERIES(5, Transition.Shape.PARABOLIC),
    HAACK           (6, Transition.Shape.HAACK);

    /** The Rocksim enumeration value. Sent in XML. */
    private final int ordinal;
    
    /** The corresponding OpenRocket shape. */
    private final Transition.Shape shape;

    /**
     * Constructor.
     * 
     * @param idx    the Rocksim shape code
     * @param aShape the corresponding OpenRocket shape
     */
    private RocksimNoseConeCode(int idx, Transition.Shape aShape) {
        ordinal = idx;
        shape = aShape;
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
     * @param rocksimShapeCode  the Rocksim code (from XML)
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
     * @param type  the OR Shape
     *              
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
}
