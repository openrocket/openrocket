package info.openrocket.swing.gui.components;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import info.openrocket.swing.gui.SpinnerEditor;

/**
 * A JSpinner that allows edits.
 * 
 * @author Billy Olsen
 */
public class EditableSpinner extends JSpinner {

	private static final long serialVersionUID = 1L;


	/**
	 * Creates a new EditableSpinner for the SpinnerModel
	 * 
	 * @param model
	 */
	public EditableSpinner(SpinnerModel model) {
		super(model);
		this.setEditor(new SpinnerEditor(this));
	}

}
