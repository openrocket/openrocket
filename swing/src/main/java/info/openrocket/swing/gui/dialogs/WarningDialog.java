package info.openrocket.swing.gui.dialogs;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import info.openrocket.swing.gui.util.BetterListCellRenderer;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;

@SuppressWarnings("serial")
public abstract class WarningDialog {

	public static void showWarnings(Component parent, Object message, String title, WarningSet warnings) {
		
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> list = new JList<Warning>(w);
		list.setCellRenderer(new BetterListCellRenderer());
		JScrollPane pane = new JScrollPane(list);
		
		JOptionPane.showMessageDialog(parent, new Object[] { message, pane }, 
				title, JOptionPane.WARNING_MESSAGE);
		
	}
	
}
