package info.openrocket.core.componentanalysis;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Group;

public class CADataTypeGroup implements Comparable<CADataTypeGroup>, Group {
	private static final Translator trans = Application.getTranslator();

	public static final CADataTypeGroup DOMAIN_PARAMETER = new CADataTypeGroup(trans.get("CADataTypeGroup.DOMAIN"), 0);
	public static final CADataTypeGroup STABILITY = new CADataTypeGroup(trans.get("CADataTypeGroup.STABILITY"), 10);
	public static final CADataTypeGroup DRAG = new CADataTypeGroup(trans.get("CADataTypeGroup.DRAG"), 20);
	public static final CADataTypeGroup ROLL = new CADataTypeGroup(trans.get("CADataTypeGroup.ROLL"), 30);

	private final String name;
	private final int priority;

	private CADataTypeGroup(String groupName, int priority) {
		this.name = groupName;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CADataTypeGroup))
			return false;
		return this.compareTo((CADataTypeGroup) o) == 0;
	}

	@Override
	public int compareTo(CADataTypeGroup o) {
		return this.priority  - o.priority;
	}
}
