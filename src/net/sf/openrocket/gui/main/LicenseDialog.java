package net.sf.openrocket.gui.main;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.util.GUIUtil;

public class LicenseDialog extends JDialog {
	private static final String LICENSE_FILENAME = "LICENSE.TXT";
	
	private static final String DEFAULT_LICENSE_TEXT =
		"\n" +
		"Error:  Unable to load " + LICENSE_FILENAME + "!\n" +
		"\n" +
		"OpenRocket is licensed under the GNU GPL version 3, with additional permissions.\n" +
		"See http://openrocket.sourceforge.net/ for details.";

	public LicenseDialog(JFrame parent) {
		super(parent, true);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new ResizeLabel("OpenRocket license", 10), "ax 50%, wrap para");

		String licenseText;
		try {
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(ClassLoader.getSystemResourceAsStream(LICENSE_FILENAME)));
			StringBuffer sb = new StringBuffer();
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				sb.append(s);
				sb.append('\n');
			}
			licenseText = sb.toString();
			
		} catch (Exception e) {

			licenseText = DEFAULT_LICENSE_TEXT;
			
		}
		
		JTextArea text = new JTextArea(licenseText);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		text.setRows(20);
		text.setColumns(80);
		text.setEditable(false);
		panel.add(new JScrollPane(text),"grow, wrap para");
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.dispose();
			}
		});
		panel.add(close, "right");
		
		this.add(panel);
		this.setTitle("OpenRocket license");
		this.pack();
		this.setLocationByPlatform(true);
		GUIUtil.setDefaultButton(close);
		GUIUtil.installEscapeCloseOperation(this);
	}
	
}
