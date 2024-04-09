package info.openrocket.core.logging;

/**
 * The priority of a message.
 */
public enum MessagePriority {
	LOW("LOW"),
	NORMAL("NORMAL"),
	HIGH("HIGH");

	private String exportLabel;

	MessagePriority(String exportLabel) {
		this.exportLabel = exportLabel;
	}

	public String getExportLabel() {
		return exportLabel;
	}

	public static MessagePriority fromExportLabel(String exportLabel) {
		for (MessagePriority priority : MessagePriority.values()) {
			if (priority.exportLabel.equals(exportLabel)) {
				return priority;
			}
		}
		return NORMAL;
	}
}
