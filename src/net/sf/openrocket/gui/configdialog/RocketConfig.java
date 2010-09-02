package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.GUIUtil;

public class RocketConfig extends RocketComponentConfig {
	
	private TextFieldListener textFieldListener;
	
	private JTextArea designerTextArea;
	private JTextArea revisionTextArea;
	
	private final Rocket rocket;
	
	public RocketConfig(RocketComponent c) {
		super(c);
		
		rocket = (Rocket) c;
		
		this.removeAll();
		setLayout(new MigLayout("fill"));
		
		this.add(new JLabel("Design name:"), "top, pad 4lp, gapright 10lp");
		this.add(componentNameField, "growx, wrap para");
		

		this.add(new JLabel("Designer:"), "top, pad 4lp, gapright 10lp");
		
		textFieldListener = new TextFieldListener();
		designerTextArea = new JTextArea(rocket.getDesigner());
		designerTextArea.setLineWrap(true);
		designerTextArea.setWrapStyleWord(true);
		designerTextArea.setEditable(true);
		GUIUtil.setTabToFocusing(designerTextArea);
		designerTextArea.addFocusListener(textFieldListener);
		this.add(new JScrollPane(designerTextArea), "wmin 400lp, height 60lp:60lp:, grow 30, wrap para");
		

		this.add(new JLabel("Comments:"), "top, pad 4lp, gapright 10lp");
		this.add(new JScrollPane(commentTextArea), "wmin 400lp, height 155lp:155lp:, grow 100, wrap para");
		

		this.add(new JLabel("Revision history:"), "top, pad 4lp, gapright 10lp");
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
		public void actionPerformed(ActionEvent e) {
			setName();
		}
		
		public void focusGained(FocusEvent e) {
		}
		
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
