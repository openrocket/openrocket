package net.sf.openrocket.gui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.openrocket.gui.util.GUIUtil;

public class DetailDialog {
	
	public static void showDetailedMessageDialog(Component parentComponent, Object message,
			String details, String title, int messageType) {
		
		if (details != null) {
			JTextArea textArea = null;
			textArea = new JTextArea(5, 40);
			textArea.setText(details);
			textArea.setCaretPosition(0);
			textArea.setEditable(false);
			GUIUtil.changeFontSize(textArea, -2);
			JOptionPane.showMessageDialog(parentComponent,
					new Object[] { message, new JScrollPane(textArea) },
					title, messageType, null);
		} else {
			JOptionPane.showMessageDialog(parentComponent, message, title, messageType, null);
		}
		
	}
	

}
