package net.sf.openrocket.gui.dialogs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.ComparablePair;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Icons;

public class UpdateInfoDialog extends JDialog {
	
	private final JCheckBox remind;

	public UpdateInfoDialog(UpdateInfo info) {
		super((Window)null, "OpenRocket update available", ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		

		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-about.png", "OpenRocket")), 
				"spany 100, top");
		
		
		panel.add(new JLabel("<html><b>OpenRocket version " + info.getLatestVersion() +
				" is available!"), "wrap para");
		
		List<ComparablePair<Integer, String>> updates = info.getUpdates();
		if (updates.size() > 0) {
			panel.add(new JLabel("Updates include:"), "wrap rel");
			
			Collections.sort(updates);
			int count = 0;
			int n = -1;
			for (int i=updates.size()-1; i>=0; i--) {
				// Add only specific number of top features
				if (count >= 4 && n != updates.get(i).getU())
					break;
				n = updates.get(i).getU();
				panel.add(new JLabel("   " + Chars.BULLET + " " + updates.get(i).getV()), 
						"wrap 0px");
				count++;
			}
		}

		panel.add(new JLabel("Download the new version from:"), 
				"gaptop para, alignx 50%, wrap unrel");
		panel.add(new URLLabel(AboutDialog.OPENROCKET_URL), "alignx 50%, wrap para");
		
		remind = new JCheckBox("Remind me later");
		remind.setToolTipText("Show this update also the next time you start OpenRocket");
		remind.setSelected(true);
		panel.add(remind);
		
		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UpdateInfoDialog.this.dispose();
			}
		});
		panel.add(button, "right, gapright para");
		
		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		GUIUtil.setDisposableDialogOptions(this, button);
	}
	
	
	public boolean isReminderSelected() {
		return remind.isSelected();
	}
	
}
