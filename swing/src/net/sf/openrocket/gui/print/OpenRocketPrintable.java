/*
 * OpenRocketPrintable.java
 */
package net.sf.openrocket.gui.print;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * This enumeration identifies the various types of information that may be printed.
 */

public enum OpenRocketPrintable {
	// Design Report
	DESIGN_REPORT("OpenRocketPrintable.DesignReport", false, 1),
	// Parts detail
	PARTS_DETAIL("OpenRocketPrintable.Partsdetail", true, 2),
	// Nose Cone Templates
	NOSE_CONE_TEMPLATE("OpenRocketPrintable.Noseconetemplates", false, 3),
	// Transition Templates
	TRANSITION_TEMPLATE("OpenRocketPrintable.Transitiontemplates", false, 4),
	// Centering Ring Templates
	CENTERING_RING_TEMPLATE("OpenRocketPrintable.Centeringringtemplates", false, 5),
	// Finset shape
	FIN_TEMPLATE("OpenRocketPrintable.Fintemplates", true, 6),
	// Fin marking guide.
	FIN_MARKING_GUIDE("OpenRocketPrintable.Finmarkingguide", false, 7);


	private static final Translator trans = Application.getTranslator();

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
	OpenRocketPrintable(String s, boolean staged, int idx) {
		description = s;
		stageSpecific = staged;
		order = idx;
	}

	/**
	 * Get the description of this printable.
	 *
	 * @return a displayable string
	 */
	public String getDescription() {
		return trans.get(description);
	}

	/**
	 * Answers if this enum value has different meaning depending upon the stage.
	 *
	 * @return true if the printable is stage dependent
	 */
	public boolean isStageSpecific() {
		return stageSpecific;
	}

	/**
	 * Answer the print order.  This is relative to other enum values.  No two enum values will have the same print
	 * order value.
	 *
	 * @return a 0 based order (0 being first, or highest)
	 */
	public int getPrintOrder() {
		return order;
	}

	/**
	 * Look up an enum value based on the description.
	 *
	 * @param target the description
	 *
	 * @return an instance of this enum class or null if not found
	 */
	public static OpenRocketPrintable findByDescription(String target) {
		OpenRocketPrintable[] values = values();
		for (OpenRocketPrintable value : values) {
			if (value.getDescription().equalsIgnoreCase(target)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Get a list of ordered enum values that do not have stage affinity.
	 *
	 * @return a list of OpenRocketPrintable
	 */
	public static List<OpenRocketPrintable> getUnstaged() {
		List<OpenRocketPrintable> unstaged = new ArrayList<OpenRocketPrintable>();
		OpenRocketPrintable[] values = values();
		for (OpenRocketPrintable value : values) {
			if (!value.isStageSpecific()) {
				unstaged.add(value);
			}
		}
		return unstaged;
	}
}
