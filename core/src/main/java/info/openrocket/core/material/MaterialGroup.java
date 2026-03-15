package info.openrocket.core.material;

import info.openrocket.core.database.Databases;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Group;
import info.openrocket.core.util.MathUtil;

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
  	public static final MaterialGroup ELASTICS = new MaterialGroup(trans.get("MaterialGroup.Elastics"), "Elastics", 80, false);
  	public static final MaterialGroup KEVLARS = new MaterialGroup(trans.get("MaterialGroup.Kevlars"), "Kevlars", 90, false);
  	public static final MaterialGroup NYLONS = new MaterialGroup(trans.get("MaterialGroup.Nylons"), "Nylons", 100, false);
	public static final MaterialGroup OTHER = new MaterialGroup(trans.get("MaterialGroup.Other"), "Other", 110, false);

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
			ELASTICS,
			KEVLARS,
			NYLONS,
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

	/**
	 * Load a material group from a database string with backward compatibility support.
	 * If the group string is "ThreadsLines" (the old name), this method will search
	 * the material database to determine the correct group (ELASTICS, KEVLARS, or NYLONS).
	 * If the material is not found in any of those groups, it returns OTHER.
	 *
	 * @param groupString the group string from the database
	 * @param type the material type (required for backward compatibility)
	 * @param materialName the material name (required for backward compatibility)
	 * @param density the material density (required for backward compatibility)
	 * @return the resolved material group
	 */
	public static MaterialGroup loadFromDatabaseStringWithBackwardCompatibility(String groupString,
			Material.Type type, String materialName, double density) {
		// Handle backward compatibility for "ThreadsLines"
		if ("ThreadsLines".equals(groupString)) {
			// Search the database for the material by name and density
			// We need to search directly in the database to check the group
			var db = Databases.getDatabase(type);
			for (Material m : db) {
				if (m.getName().equalsIgnoreCase(materialName) && MathUtil.equals(m.getDensity(), density)) {
					MaterialGroup foundGroup = m.getGroup();
					// Check if the material belongs to one of the groups that replaced ThreadsLines
					if (foundGroup == ELASTICS || foundGroup == KEVLARS || foundGroup == NYLONS) {
						return foundGroup;
					}
				}
			}
			// Material not found in ELASTICS, KEVLARS, or NYLONS, return OTHER
			return OTHER;
		}
		// For all other groups, use the standard loading method
		return loadFromDatabaseString(groupString);
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
