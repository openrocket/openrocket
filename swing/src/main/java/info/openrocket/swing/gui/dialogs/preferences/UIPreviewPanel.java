package info.openrocket.swing.gui.dialogs.preferences;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import net.miginfocom.swing.MigLayout;

/**
 * A panel that shows a preview of the current UI settings.
 */
public class UIPreviewPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();

	private final TitledBorder titledBorder;
	private final JLabel heading;
	private final JLabel normalText;
	private final JButton button;
	private final JTextField textField;
	private final JComboBox<String> comboBox;
	private final JSpinner spinner;
	private final JCheckBox checkBox;
	private final JLabel warning;
	private final JLabel error;

	public UIPreviewPanel() {
		super(new MigLayout("fill, wrap 2", "[grow][grow]"));

		titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				trans.get("UIPreviewPanel.title"));
		setBorder(titledBorder);

		// Create preview components
		heading = new JLabel(trans.get("UIPreviewPanel.lbl.PreviewHeading"));
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, heading.getFont().getSize() + 2f));

		normalText = new JLabel(trans.get("UIPreviewPanel.lbl.NormalText"));
		button = new JButton(trans.get("UIPreviewPanel.lbl.Button"));
		textField = new JTextField(trans.get("UIPreviewPanel.lbl.TextInput"));
		comboBox = new JComboBox<>(new String[]{
				trans.get("UIPreviewPanel.combo.item1"),
				trans.get("UIPreviewPanel.combo.item2")
		});
		spinner = new JSpinner(new SpinnerNumberModel(42, 0, 100, 1));
		checkBox = new JCheckBox(trans.get("UIPreviewPanel.check"), true);

		warning = new JLabel(trans.get("UIPreviewPanel.Warning"));
		error = new JLabel(trans.get("UIPreviewPanel.Error"));

		// Layout components
		add(heading, "span 2, wrap");
		add(normalText, "span 2, wrap 15");

		add(button, "growx");
		add(textField, "growx, wrap");

		add(comboBox, "growx");
		add(spinner, "growx, wrap");

		add(checkBox, "span 2, wrap 15");

		// TODO: add once theme preview is implemented
		//  add(warning, "span 2, wrap");
		//  add(error, "span 2");
	}

	public void updatePreview(String fontStyle, int fontSize, float letterSpacing) {
		// Create font attributes
		Map<TextAttribute, Object> attributes = new HashMap<>();
		attributes.put(TextAttribute.FAMILY, fontStyle);
		attributes.put(TextAttribute.SIZE, fontSize);
		attributes.put(TextAttribute.TRACKING, letterSpacing);

		// Create the new font
		Font newFont = Font.getFont(attributes);

		// Update all components with new font
		heading.setFont(newFont.deriveFont(Font.BOLD, newFont.getSize() + 2f));
		normalText.setFont(newFont);
		button.setFont(newFont);
		textField.setFont(newFont);
		comboBox.setFont(newFont);
		// Update spinner and its editor
		spinner.setFont(newFont);
		JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spinner.getEditor();
		spinnerEditor.getTextField().setFont(newFont);
		checkBox.setFont(newFont);
		warning.setFont(newFont);
		error.setFont(newFont);

		// Force a repaint
		revalidate();
		repaint();
	}

	public void updateTheme(UITheme.Theme theme) {
		warning.setForeground(theme.getWarningColor());
		error.setForeground(theme.getErrorColor());
	}
}
