package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
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
	
	public RocketConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		rocket = (Rocket) c;
		
		this.removeAll();
		setLayout(new MigLayout("fill"));
		
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
