package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Icons;
import net.sf.openrocket.util.Prefs;

public class AboutDialog extends JDialog {
	
	public static final String OPENROCKET_URL = "http://openrocket.sourceforge.net/";
	

	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = Prefs.getVersion();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-about.png", "OpenRocket")), 
				"spany 5, top");
		
		panel.add(new StyledLabel("OpenRocket", 20), "ax 50%, growy, wrap para");
		panel.add(new StyledLabel("Version " + version, 3), "ax 50%, growy, wrap rel");
		
		String source = Prefs.getBuildSource();
		if (!Prefs.DEFAULT_BUILD_SOURCE.equalsIgnoreCase(source)) {
			panel.add(new StyledLabel("Distributed by " + source, -1), 
					"ax 50%, growy, wrap para");
		} else {
			panel.add(new StyledLabel(" ", -1), "ax 50%, growy, wrap para");
		}
		
		panel.add(new StyledLabel("Copyright " + Chars.COPY +" 2007-2009 Sampo Niskanen"), 
				"ax 50%, growy, wrap para");
		
		panel.add(new URLLabel(OPENROCKET_URL), "ax 50%, growy, wrap para");
		

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle("OpenRocket " + version);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
	
}
