package net.sf.openrocket.gui.dialogs;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.sf.openrocket.gui.util.BetterListCellRenderer;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.Warning;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.startup.Application;

@SuppressWarnings("serial")
public abstract class WarningDialog {
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	public static void showWarnings(Component parent, Object message, String title, WarningSet warnings) {
		
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> list = new JList<Warning>(w);
		list.setCellRenderer(new BetterListCellRenderer());
		JScrollPane pane = new JScrollPane(list);
		pane.setBorder(prefs.getUITheme().getBorder());
		
		JOptionPane.showMessageDialog(parent, new Object[] { message, pane }, 
				title, JOptionPane.WARNING_MESSAGE);
		
	}
	
}
