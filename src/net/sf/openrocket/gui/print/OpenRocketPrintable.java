/*
 * OpenRocketPrintable.java
 */
package net.sf.openrocket.gui.print;

/**
 * This enumeration identifies the various types of information that may be printed.
 */
public enum OpenRocketPrintable {
    //PARTS_LIST("Parts list", true, 0),
    PARTS_DETAIL("Parts detail", true, 1),
    FIN_TEMPLATE("Fin templates", true, 2),
    DESIGN_REPORT("Design Report", false, 3);

    /**
     * The description - will be displayed in the JTree.
     */
    private String description;

    /**
     * Flag that indicates if the enum value is different depending upon stage.
     */
    private boolean stageSpecific;

    /**
     * The order of the item as it appears in the printed document.
     */
    private int order;

    /**
     * Constructor.
     *
     * @param s      the displayable description
     * @param staged indicates if the printable is stage dependent
     * @param idx    the relative print order
     */
    OpenRocketPrintable (String s, boolean staged, int idx) {
        description = s;
        stageSpecific = staged;
        order = idx;
    }

    /**
     * Get the description of this printable.
     *
     * @return a displayable string
     */
    public String getDescription () {
        return description;
    }

    /**
     * Answers if this enum value has different meaning depending upon the stage.
     *
     * @return true if the printable is stage dependent
     */
    public boolean isStageSpecific () {
        return stageSpecific;
    }

    /**
     * Answer the print order.  This is relative to other enum values.  No two enum values will have the same print
     * order value.
     *
     * @return a 0 based order (0 being first, or highest)
     */
    public int getPrintOrder () {
        return order;
    }

    /**
     * Look up an enum value based on the description.
     *
     * @param target the description
     *
     * @return an instance of this enum class or null if not found
     */
    public static OpenRocketPrintable findByDescription (String target) {
        OpenRocketPrintable[] values = values();
        for (OpenRocketPrintable value : values) {
            if (value.getDescription().equalsIgnoreCase(target)) {
                return value;
            }
        }
        return null;
    }
}
