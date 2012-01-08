package net.sf.openrocket.gui;

import javax.swing.JSpinner;

/**
 * Editable editor for a JSpinner.  Simply uses JSpinner.DefaultEditor, which has been made
 * editable.  Why the f*** isn't this possible in the normal API?
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class SpinnerEditor extends JSpinner.NumberEditor {
//public class SpinnerEditor extends JSpinner.DefaultEditor {

	public SpinnerEditor(JSpinner spinner) {
		//super(spinner);
		super(spinner,"0.0##");
		//getTextField().setEditable(true);
	}

}
