/*
 * RocksimFinishCode.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.rocketcomponent.ExternalComponent;

/**
 * Models the finish of a component.
 */
public enum RocksimFinishCode {
    POLISHED(0, ExternalComponent.Finish.POLISHED),
    GLOSS(1, ExternalComponent.Finish.SMOOTH),
    MATT(2, ExternalComponent.Finish.NORMAL),
    UNFINISHED(3, ExternalComponent.Finish.UNFINISHED);

    /** The Rocksim code (from XML). */
    private final int ordinal;
    
    /** The corresponding OpenRocket finish. */
    private final ExternalComponent.Finish finish;

    /**
     * Constructor.
     * 
     * @param idx   the Rocksim enum value
     * @param theFinish  the OpenRocket finish
     */
    private RocksimFinishCode(int idx, ExternalComponent.Finish theFinish) {
        ordinal = idx;
        finish = theFinish;
    }

    /**
     * Get the OpenRocket finish.
     * 
     * @return a Finish instance
     */
    public ExternalComponent.Finish asOpenRocket() {
        return finish;
    }

    /**
     * Lookup an instance of this enum from a Rocksim value.
     * 
     * @param rocksimFinishCode  the Rocksim value
     * 
     * @return an instance of this enum; Defaults to MATT
     */
    public static RocksimFinishCode fromCode(int rocksimFinishCode) {
        RocksimFinishCode[] values = values();
        for (RocksimFinishCode value : values) {
            if (value.ordinal == rocksimFinishCode) {
                return value;
            }
        }
        return MATT; //Default
    }

    /**
     * Get the ordinal code.
     *
     * @param type  the OR type
     *
     * @return  the Rocksim XML value
     */
    public static int toCode(ExternalComponent.Finish type) {
        if (type.equals(ExternalComponent.Finish.UNFINISHED)) {
            return UNFINISHED.ordinal;
        }
        if (type.equals(ExternalComponent.Finish.POLISHED)) {
            return POLISHED.ordinal;
        }
        if (type.equals(ExternalComponent.Finish.SMOOTH)) {
            return GLOSS.ordinal;
        }
        return MATT.ordinal;
    }

}

