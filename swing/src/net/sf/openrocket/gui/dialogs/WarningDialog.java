package net.sf.openrocket.gui.dialogs;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import net.sf.openrocket.gui.util.BetterListCellRenderer;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.UITheme;
import net.sf.openrocket.logging.Warning;
import net.sf.openrocket.logging.WarningSet;

@SuppressWarnings("serial")
public abstract class WarningDialog {
	private static Border border;

	static {
		initColors();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(WarningDialog::updateColors);
	}

	private static void updateColors() {
		border = GUIUtil.getUITheme().getBorder();
	}

	public static void showWarnings(Component parent, Object message, String title, WarningSet warnings) {
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> list = new JList<Warning>(w);
		list.setCellRenderer(new BetterListCellRenderer());
		JScrollPane pane = new JScrollPane(list);
		pane.setBorder(border);
		
		JOptionPane.showMessageDialog(parent, new Object[] { message, pane }, 
				title, JOptionPane.WARNING_MESSAGE);
		
	}
	
}
