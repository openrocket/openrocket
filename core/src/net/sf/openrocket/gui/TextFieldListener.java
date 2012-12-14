package net.sf.openrocket.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public abstract class TextFieldListener implements ActionListener, FocusListener {
	private JTextField field;
		
	public void listenTo(JTextField newField) {
		if (field != null) {
			field.removeActionListener(this);
			field.removeFocusListener(this);
		}
		field = newField;
		if (field != null) {
			field.addActionListener(this);
			field.addFocusListener(this);
		}
	}

	public abstract void setText(String text);

	@Override
	public void actionPerformed(ActionEvent e) {
		setText(field.getText());
	}
	
	@Override
	public void focusGained(FocusEvent e) { }
	
	@Override
	public void focusLost(FocusEvent e) {
		setText(field.getText());
	}
	
}