package net.sf.openrocket.gui;

import net.sf.openrocket.gui.adaptors.TextComponentSelectionKeyListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

		addListeners();
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

	private void addListeners() {
		// Select all the text when the field is focussed
		getTextField().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				selectAllText(getTextField());
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		// Select all the text when the field is first clicked upon
		getTextField().addMouseListener(new MouseAdapter() {
			private boolean isFocussed = false;    // Checks whether the text field was focussed when it was clicked upon

			@Override
			public void mouseClicked(MouseEvent e) {
				/*
				If the text field was focussed when it was clicked upon instead of e.g. tab-switching to gain focus,
				then the select all action from the focus listener is ignored (it is replaced by a cursor-click event).
				So if we detect such a focus change, then redo the select all action.
				*/
				if (!isFocussed) {
					selectAllText((JTextField) e.getSource());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				JTextField tf = (JTextField) e.getSource();
				isFocussed = tf.hasFocus();
			}
		});

		// Fix key behavior on text selection
		getTextField().addKeyListener(new TextComponentSelectionKeyListener(getTextField()));
	}

	/**
	 * Highlights all the text in the text field.
	 */
	private void selectAllText(JTextField tf) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tf.selectAll();
			}
		});
	}
	
}
