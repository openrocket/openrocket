package net.sf.openrocket.gui.configdialog;


import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class RocketConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	private TextFieldListener textFieldListener;
	
	private JTextArea designerTextArea;
	private JTextArea revisionTextArea;
	
	private final Rocket rocket;
	
	public RocketConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		rocket = (Rocket) c;
		
		this.removeAll();
		setLayout(new MigLayout("fill, hideMode 3"));
		
		//// Design name:
		this.add(new JLabel(trans.get("RocketCfg.lbl.Designname")), "top, pad 4lp, gapright 10lp");
		this.add(componentNameField, "growx, wrap para");
		
		//// Designer:
		this.add(new JLabel(trans.get("RocketCfg.lbl.Designer")), "top, pad 4lp, gapright 10lp");
		
		textFieldListener = new TextFieldListener();
		designerTextArea = new JTextArea(rocket.getDesigner());
		designerTextArea.setLineWrap(true);
		designerTextArea.setWrapStyleWord(true);
		designerTextArea.setEditable(true);
		GUIUtil.setTabToFocusing(designerTextArea);
		designerTextArea.addFocusListener(textFieldListener);
		this.add(new JScrollPane(designerTextArea), "wmin 400lp, height 60lp:60lp:, grow 30, wrap para");
		
		//// Comments:
		this.add(new JLabel(trans.get("RocketCfg.lbl.Comments")), "top, pad 4lp, gapright 10lp");
		this.add(new JScrollPane(commentTextArea), "wmin 400lp, height 155lp:155lp:, grow 100, wrap para");
		
		//// Revision history:
		this.add(new JLabel(trans.get("RocketCfg.lbl.Revisionhistory")), "top, pad 4lp, gapright 10lp");
		revisionTextArea = new JTextArea(rocket.getRevision());
		revisionTextArea.setLineWrap(true);
		revisionTextArea.setWrapStyleWord(true);
		revisionTextArea.setEditable(true);
		GUIUtil.setTabToFocusing(revisionTextArea);
		revisionTextArea.addFocusListener(textFieldListener);
		
		this.add(new JScrollPane(revisionTextArea), "wmin 400lp, height 60lp:60lp:, grow 30, wrap para");
		

		addButtons();
		addEasterEgg();
	}

	/**
	 * Little method that adds a fun easter-egg to the rocket config dialog.
	 */
	private void addEasterEgg() {
		componentNameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String text = componentNameField.getText() + e.getKeyChar();
				String msg = null;
				String title = null;
				switch (text) {
					case "SA-508":
						msg = "Houston, we have a problem.\n\nJust kidding, have fun building your 'Apollo 13' rocket!";
						title = "Oh oh...";
						break;
					case "SA-506":
						msg = "One small step for a rocket, one giant leap for rocketkind.";
						title = "Or was that not the quote?";
						break;
					case "Vega":
						msg = "Viva las Vega!";
						title = "Vega, Ready for Launch and Laughs!";
						break;
					case "Ariane 5":
						msg = "Non, je ne regrette rien\u2026 except for that one overflow error\u2026";
						title = "Happens to the best of us";
						break;
				}
				if (msg != null) {
					JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
					JDialog dialog = optionPane.createDialog(RocketConfig.this, title);
					// Make sure title doesn't get cut off
					FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(dialog.getFont());
					int width = Math.max(dialog.getPreferredSize().width, fontMetrics.stringWidth(title) + 100);
					int height = dialog.getPreferredSize().height;
					dialog.setSize(new Dimension(width, height));
					dialog.setVisible(true);
				}
			}
		});
	}
	

	private class TextFieldListener implements ActionListener, FocusListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			setName();
		}
		
		@Override
		public void focusGained(FocusEvent e) {
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			setName();
		}
		
		private void setName() {
			if (!rocket.getDesigner().equals(designerTextArea.getText())) {
				rocket.setDesigner(designerTextArea.getText());
			}
			if (!rocket.getRevision().equals(revisionTextArea.getText())) {
				rocket.setRevision(revisionTextArea.getText());
			}
		}
	}
	


}
