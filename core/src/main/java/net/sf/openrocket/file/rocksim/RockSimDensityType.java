/*
 * RockSimDensityType.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.material.Material;

/**
 * Models the nose cone shape of a rocket.  Maps from RockSim's notion to OpenRocket's.
 */
public enum RockSimDensityType {
    ROCKSIM_BULK   (0, RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY),
    ROCKSIM_SURFACE(1, RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY),
    ROCKSIM_LINE   (2, RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY);

    /** The RockSim enumeration value. Sent in XML. */
    private final int ordinal;

    /** The corresponding OpenRocket shape. */
    private final double conversion;

    /**
     * Constructor.
     *
     * @param idx            the RockSim shape code
     * @param theConversion  the numerical conversion ratio to OpenRocket
     */
    private RockSimDensityType(int idx, double theConversion) {
        ordinal = idx;
        conversion = theConversion;
    }

    /**
     * Get the OpenRocket shape that corresponds to the RockSim value.
     *
     * @return a conversion
     */
    public double asOpenRocket() {
        return conversion;
    }

    /**
     * Lookup an instance of this enum based upon the RockSim code.
     *
     * @param rocksimDensityType  the RockSim code (from XML)
     * @return an instance of this enum
     */
    public static RockSimDensityType fromCode(int rocksimDensityType) {
        RockSimDensityType[] values = values();
        for (RockSimDensityType value : values) {
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
     * @return  the RockSim XML value
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

