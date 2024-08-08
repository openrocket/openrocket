package info.openrocket.core.material;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Group;

/**
 * A class for categorizing materials.
 */
public class MaterialGroup implements Comparable<MaterialGroup>, Group {
	private static final Translator trans = Application.getTranslator();

	// When modifying this list, also update the MaterialGroupDTO class in the preset.xml package! (and the ALL_GROUPS array)
	public static final MaterialGroup METALS = new MaterialGroup(trans.get("MaterialGroup.Metals"), "Metals", 0, false);
	public static final MaterialGroup WOODS = new MaterialGroup(trans.get("MaterialGroup.Woods"), "Woods", 10, false);
	public static final MaterialGroup PLASTICS = new MaterialGroup(trans.get("MaterialGroup.Plastics"), "Plastics", 20, false);
	public static final MaterialGroup FABRICS = new MaterialGroup(trans.get("MaterialGroup.Fabrics"), "Fabrics", 30, false);
	public static final MaterialGroup PAPER = new MaterialGroup(trans.get("MaterialGroup.PaperProducts"), "PaperProducts", 40, false);
	public static final MaterialGroup FOAMS = new MaterialGroup(trans.get("MaterialGroup.Foams"), "Foams", 50, false);
	public static final MaterialGroup COMPOSITES = new MaterialGroup(trans.get("MaterialGroup.Composites"), "Composites", 60, false);
	public static final MaterialGroup FIBERS = new MaterialGroup(trans.get("MaterialGroup.Fibers"), "Fibers", 70, false);
	public static final MaterialGroup THREADS_LINES = new MaterialGroup(trans.get("MaterialGroup.ThreadsLines"), "ThreadsLines", 80, false);
	public static final MaterialGroup OTHER = new MaterialGroup(trans.get("MaterialGroup.Other"), "Other", 90, false);

	public static final MaterialGroup CUSTOM = new MaterialGroup(trans.get("MaterialGroup.Custom"), "Custom", 1000, true);

	public static final MaterialGroup[] ALL_GROUPS = {
			METALS,
			WOODS,
			PLASTICS,
			FABRICS,
			PAPER,
			FOAMS,
			COMPOSITES,
			FIBERS,
			THREADS_LINES,
			OTHER,
			CUSTOM
	};

	private final String name;
	private final String databaseString;
	private final int priority;
	private final boolean userDefined;

	/**
	 * Create a new material group.
	 * @param name the name of the group
	 * @param dataBaseName the name of the group to be used when saving it in a database
	 * @param priority the priority of the group (lower number = higher priority)
	 * @param userDefined whether the group is user-defined
	 */
	private MaterialGroup(String name, String dataBaseName, int priority, boolean userDefined) {
		this.name = name;
		this.databaseString = dataBaseName;
		this.priority = priority;
		this.userDefined = userDefined;
	}

	public String getName() {
		return name;
	}

	public String getDatabaseString() {
		return databaseString;
	}

	public int getPriority() {
		return priority;
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public static MaterialGroup loadFromDatabaseString(String name) {
		if (name == null) {
			return MaterialGroup.OTHER;
		}
		for (MaterialGroup group : ALL_GROUPS) {
			if (group.getDatabaseString().equals(name)) {
				return group;
			}
		}
		throw new IllegalArgumentException("Unknown material group: " + name);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MaterialGroup))
			return false;
		return this.compareTo((MaterialGroup) o) == 0;
	}

	@Override
	public int compareTo(MaterialGroup o) {
		return this.priority  - o.priority;
	}
}
