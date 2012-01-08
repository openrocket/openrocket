package net.sf.openrocket.gui.dialogs.motor;

public interface CloseableDialog {
	
	/**
	 * Close this dialog.
	 * 
	 * @param ok	whether "OK" should be considered to have been clicked.
	 */
	public void close(boolean ok);
	
}
