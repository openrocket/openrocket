package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.gui.main.AboutDialog;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Prefs;

public class BugDialog extends JDialog {

	public BugDialog(JFrame parent) {
		super(parent, "Bug reporing", true);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel("Please report any bugs you encounter as instructed at "), 
				"gap para, split 2");
		panel.add(new URLLabel(AboutDialog.OPENROCKET_URL), "wrap rel");
		panel.add(new JLabel("This allows us to make OpenRocket an even better simulator."),
				"gap para, wrap para");
		
		panel.add(new JLabel("<html><em>Please copy and paste the following information " +
				"to the end of your bug report:</em>"), "gap para, wrap");
		
		
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		sb.append("---------- Included system information ----------\n");
		sb.append("OpenRocket version: " + Prefs.getVersion() + "\n");
		sb.append("OpenRocket source: " + Prefs.getBuildSource() + "\n");
		sb.append("OpenRocket location: " + JarUtil.getCurrentJarFile() + "\n");
		sb.append("System properties:\n");

		// Sort the keys
		SortedSet<String> keys = new TreeSet<String>();
		for (Object key: System.getProperties().keySet()) {
			keys.add((String)key);
		}
		
		for (String key: keys) {
			String value = System.getProperty(key);
			sb.append("  " + key + "=");
			if (key.equals("line.separator")) {
				for (char c: value.toCharArray()) {
					sb.append(String.format("\\u%04x", (int)c));
				}
			} else {
				sb.append(value);
			}
			sb.append('\n');
		}
		
		sb.append("---------- End system information ----------\n");
		sb.append('\n');

		JTextArea text = new JTextArea(sb.toString(), 15, 70);
		text.setEditable(false);
		panel.add(new JScrollPane(text), "grow, wrap para");
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BugDialog.this.dispose();
			}
		});
		panel.add(close, "right");

		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(parent);
		GUIUtil.installEscapeCloseOperation(this);
		GUIUtil.setDefaultButton(close);
	}

}
