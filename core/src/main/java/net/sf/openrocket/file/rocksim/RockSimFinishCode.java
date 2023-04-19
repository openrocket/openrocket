/*
 * RockSimFinishCode.java
 */
package net.sf.openrocket.file.rocksim;

import net.sf.openrocket.rocketcomponent.ExternalComponent;

/**
 * Models the finish of a component.
 */
public enum RockSimFinishCode {
    POLISHED(0, ExternalComponent.Finish.POLISHED),
    GLOSS(1, ExternalComponent.Finish.SMOOTH),
    MATT(2, ExternalComponent.Finish.NORMAL),
    UNFINISHED(3, ExternalComponent.Finish.UNFINISHED);

    /** The RockSim code (from XML). */
    private final int ordinal;
    
    /** The corresponding OpenRocket finish. */
    private final ExternalComponent.Finish finish;

    /**
     * Constructor.
     * 
     * @param idx   the RockSim enum value
     * @param theFinish  the OpenRocket finish
     */
    private RockSimFinishCode(int idx, ExternalComponent.Finish theFinish) {
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
     * Lookup an instance of this enum from a RockSim value.
     * 
     * @param rocksimFinishCode  the RockSim value
     * 
     * @return an instance of this enum; Defaults to MATT
     */
    public static RockSimFinishCode fromCode(int rocksimFinishCode) {
        RockSimFinishCode[] values = values();
        for (RockSimFinishCode value : values) {
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
     * @return  the RockSim XML value
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

