package net.sf.openrocket.gui.main;

public interface DocumentSelectionListener {

	public static final int COMPONENT_SELECTION_CHANGE = 1;
	public static final int SIMULATION_SELECTION_CHANGE = 2;
	
	/**
	 * Called when the selection changes.
	 * 
	 * @param changeType	a bitmask of the type of change.
	 */
	public void valueChanged(int changeType);
	
}
