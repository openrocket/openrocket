package net.sf.openrocket.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;

/**
 * A panel that shows options for saving CSV files.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class CsvOptionPanel extends JPanel {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final String SPACE = trans.get("CsvOptionPanel.separator.space");
	private static final String TAB = trans.get("CsvOptionPanel.separator.tab");
	
	private final String baseClassName;
	
	private final JComboBox fieldSeparator;
	private final JCheckBox[] options;
	private final JComboBox commentCharacter;
	
	/**
	 * Sole constructor.
	 * 
	 * @param includeComments	a list of comment inclusion options to provide;
	 * 							every second item is the option name and every second the tooltip
	 */
	public CsvOptionPanel(Class<?> baseClass, String... includeComments) {
		super(new MigLayout("fill, insets 0"));
		
		this.baseClassName = baseClass.getSimpleName();
		
		JPanel panel;
		JLabel label;
		String tip;
		

		// TODO: HIGH: Rename the translation keys
		
		// Field separator panel
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Fieldsep")));
		
		label = new JLabel(trans.get("SimExpPan.lbl.Fieldsepstr"));
		tip = trans.get("SimExpPan.lbl.longA1") +
				trans.get("SimExpPan.lbl.longA2");
		label.setToolTipText(tip);
		panel.add(label, "gapright unrel");
		
		fieldSeparator = new JComboBox(new String[] { ",", ";", SPACE, TAB });
		fieldSeparator.setEditable(true);
		fieldSeparator.setSelectedItem(Application.getPreferences().getString(Preferences.EXPORT_FIELD_SEPARATOR, ","));
		fieldSeparator.setToolTipText(tip);
		panel.add(fieldSeparator, "growx");
		
		this.add(panel, "growx, wrap unrel");
		


		// Comments separator panel
		panel = new JPanel(new MigLayout("fill"));
		panel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Comments")));
		

		// List of include comments options
		if (includeComments.length % 2 == 1) {
			throw new IllegalArgumentException("Invalid argument length, must be even, length=" + includeComments.length);
		}
		options = new JCheckBox[includeComments.length / 2];
		for (int i = 0; i < includeComments.length / 2; i++) {
			options[i] = new JCheckBox(includeComments[i * 2]);
			options[i].setToolTipText(includeComments[i * 2 + 1]);
			options[i].setSelected(Application.getPreferences().getBoolean("csvOptions." + baseClassName + "." + i, true));
			panel.add(options[i], "wrap");
		}
		

		label = new JLabel(trans.get("SimExpPan.lbl.Commentchar"));
		tip = trans.get("SimExpPan.lbl.ttip.Commentchar");
		label.setToolTipText(tip);
		panel.add(label, "split 2, gapright unrel");
		
		commentCharacter = new JComboBox(new String[] { "#", "%", ";" });
		commentCharacter.setEditable(true);
		commentCharacter.setSelectedItem(Application.getPreferences().getString(Preferences.EXPORT_COMMENT_CHARACTER, "#"));
		commentCharacter.setToolTipText(tip);
		panel.add(commentCharacter, "growx");
		
		this.add(panel, "growx, wrap");
	}
	
	
	public String getFieldSeparator() {
		return fieldSeparator.getSelectedItem().toString();
	}
	
	public String getCommentCharacter() {
		return commentCharacter.getSelectedItem().toString();
	}
	
	public boolean getSelectionOption(int index) {
		return options[index].isSelected();
	}
	
	/**
	 * Store the selected options to the user preferences.
	 */
	public void storePreferences() {
		Application.getPreferences().putString(Preferences.EXPORT_FIELD_SEPARATOR, getFieldSeparator());
		Application.getPreferences().putString(Preferences.EXPORT_COMMENT_CHARACTER, getCommentCharacter());
		for (int i = 0; i < options.length; i++) {
			Application.getPreferences().putBoolean("csvOptions." + baseClassName + "." + i, options[i].isSelected());
		}
	}
	
}
