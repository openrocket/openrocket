package net.sf.openrocket.gui;

import javax.swing.JSpinner;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Editable editor for a JSpinner.  Simply uses JSpinner.DefaultEditor, which has been made
 * editable.  Why the f*** isn't this possible in the normal API?
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

//public class SpinnerEditor extends JSpinner.NumberEditor {
public class SpinnerEditor extends JSpinner.DefaultEditor {

	public SpinnerEditor(JSpinner spinner) {
		super(spinner);
		//super(spinner,"0.0##");
		getTextField().setEditable(true);
		
		DefaultFormatterFactory dff = (DefaultFormatterFactory) getTextField().getFormatterFactory();
		DefaultFormatter formatter = (DefaultFormatter) dff.getDefaultFormatter();
		formatter.setOverwriteMode(false);
	}

	/**
	 * Constructor which sets the number of columns in the editor.
	 * @param spinner
	 * @param cols
	 */
	public SpinnerEditor(JSpinner spinner, int cols ) {
		this(spinner);
		getTextField().setColumns(cols);
	}
	
}
