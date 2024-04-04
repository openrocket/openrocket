package info.openrocket.swing.gui.configdialog;

import info.openrocket.core.util.Invalidatable;

public interface InvalidatingWidget {
	/**
	 * Register a model to be invalidated when this widget is invalidated.
	 * @param model the model to register
	 */
	void register(Invalidatable model);
}
