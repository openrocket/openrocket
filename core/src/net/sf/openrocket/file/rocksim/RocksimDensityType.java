/*
 * RocksimDensityType.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.material.Material;

/**
 * Models the nose cone shape of a rocket.  Maps from Rocksim's notion to OpenRocket's.
 */
public enum RocksimDensityType {
    ROCKSIM_BULK   (0, RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY),
    ROCKSIM_SURFACE(1, RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY),
    ROCKSIM_LINE   (2, RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY);

    /** The Rocksim enumeration value. Sent in XML. */
    private final int ordinal;

    /** The corresponding OpenRocket shape. */
    private final double conversion;

    /**
     * Constructor.
     *
     * @param idx            the Rocksim shape code
     * @param theConversion  the numerical conversion ratio to OpenRocket
     */
    private RocksimDensityType(int idx, double theConversion) {
        ordinal = idx;
        conversion = theConversion;
    }

    /**
     * Get the OpenRocket shape that corresponds to the Rocksim value.
     *
     * @return a conversion
     */
    public double asOpenRocket() {
        return conversion;
    }

    /**
     * Lookup an instance of this enum based upon the Rocksim code.
     *
     * @param rocksimDensityType  the Rocksim code (from XML)
     * @return an instance of this enum
     */
    public static RocksimDensityType fromCode(int rocksimDensityType) {
        RocksimDensityType[] values = values();
        for (RocksimDensityType value : values) {
            if (value.ordinal == rocksimDensityType) {
                return value;
            }
        }
        return ROCKSIM_BULK; //Default
    }

    /**
     * Get the ordinal code.
     *
     * @param type  the OR type
     *
     * @return  the Rocksim XML value
     */
    public static int toCode(Material.Type type) {
        if (type.equals(Material.Type.BULK)) {
            return ROCKSIM_BULK.ordinal;
        }
        if (type.equals(Material.Type.LINE)) {
            return ROCKSIM_LINE.ordinal;
        }
        if (type.equals(Material.Type.SURFACE)) {
            return ROCKSIM_SURFACE.ordinal;
        }
        return ROCKSIM_BULK.ordinal;
    }
}

