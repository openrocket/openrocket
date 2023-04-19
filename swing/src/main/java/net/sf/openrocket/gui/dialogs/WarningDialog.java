package net.sf.openrocket.gui.dialogs;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;

@SuppressWarnings("serial")
public class WarningDialog extends JDialog {

	public static void showWarnings(Component parent, Object message, String title, WarningSet warnings) {
		
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> list = new JList<Warning>(w);
		JScrollPane pane = new JScrollPane(list);
		
		JOptionPane.showMessageDialog(parent, new Object[] { message, pane }, 
				title, JOptionPane.WARNING_MESSAGE);
		
	}
	
}
