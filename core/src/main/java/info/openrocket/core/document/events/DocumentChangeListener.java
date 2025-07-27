package info.openrocket.core.document.events;

public interface DocumentChangeListener {

	void documentChanged(DocumentChangeEvent event);

	default void documentSaving(DocumentChangeEvent event) {
		// Do nothing
	}

}
