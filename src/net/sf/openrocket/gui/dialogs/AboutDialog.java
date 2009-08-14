package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

public class AboutDialog extends JDialog {
	
	public static final String OPENROCKET_URL = "http://openrocket.sourceforge.net/";
	

	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = Prefs.getVersion();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new ResizeLabel("OpenRocket", 20), "ax 50%, wrap para");
		panel.add(new ResizeLabel("Version " + version, 3), "ax 50%, wrap 30lp");
		
		panel.add(new ResizeLabel("Copyright \u00A9 2007-2009 Sampo Niskanen"), "ax 50%, wrap para");
		
		panel.add(new URLLabel(OPENROCKET_URL), "ax 50%, wrap para");
		

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		panel.add(close, "right");
		
		this.add(panel);
		this.setTitle("OpenRocket " + version);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		GUIUtil.setDefaultButton(close);
		GUIUtil.installEscapeCloseOperation(this);
	}
	
	
}
