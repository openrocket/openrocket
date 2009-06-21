package net.sf.openrocket.gui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

public class DetailDialog {

	public static void showDetailedMessageDialog(Component parentComponent, Object message, 
			String details, String title, int messageType)  {
		
		// TODO: HIGH: Detailed dialog
		JOptionPane.showMessageDialog(parentComponent, message, title, messageType, null);
		
	}
	
	
}
