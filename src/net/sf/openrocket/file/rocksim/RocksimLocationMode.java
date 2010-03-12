/*
 * RocksimLocationMode.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * Models the relative position of parts on a rocket.  Maps from Rocksim's notion to OpenRocket's.
 */
enum RocksimLocationMode {
    FRONT_OF_OWNING_PART (0, RocketComponent.Position.TOP),
    FROM_TIP_OF_NOSE     (1, RocketComponent.Position.ABSOLUTE),
    BACK_OF_OWNING_PART  (2, RocketComponent.Position.BOTTOM);

    /** The value Rocksim uses internally (and in the XML file). */
    private final int ordinal;
    
    /** The OpenRocket position equivalent. */
    private final RocketComponent.Position position;

    /**
     * Constructor.
     * 
     * @param idx   the rocksim enum value
     * @param theOpenRocketPosition  the corresponding OpenRocket position
     */
    RocksimLocationMode(int idx, RocketComponent.Position theOpenRocketPosition) {
        ordinal = idx;
        position = theOpenRocketPosition;
    }

    /**
     * Get the OpenRocket position.
     * 
     * @return  the position instance
     */
    public RocketComponent.Position asOpenRocket() {
        return position;
    }

    /**
     * Lookup an instance of this class from a rocksim enum value.
     * 
     * @param rocksimCode  the rocksim enum value
     * 
     * @return an instance of this enum
     */
    public static RocksimLocationMode fromCode(int rocksimCode) {
        RocksimLocationMode[] values = values();
        for (RocksimLocationMode value : values) {
            if (value.ordinal == rocksimCode) {
                return value;
            }
        }
        return FRONT_OF_OWNING_PART;
    }

}