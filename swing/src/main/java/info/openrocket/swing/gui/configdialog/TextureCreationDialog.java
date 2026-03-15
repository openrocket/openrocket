package info.openrocket.swing.gui.configdialog;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Color;
import java.awt.Component;
import java.text.Normalizer;
import java.util.Locale;

import info.openrocket.swing.gui.components.ColorChooserButton;

public class TextureCreationDialog {
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private final Component parent;
	private final RocketComponent component;
	private final JPanel dialogPanel;
	private final JSpinner dpiSpinner;
	private final JTextField fileNameField;
	private final JCheckBox resetTransformsCheckbox;
	private final JCheckBox outlineCheckbox;
	private final JSpinner outlineWidthSpinner;
	private final JLabel outlineColorLabel;
	private final ColorChooserButton outlineColorButton;

	public TextureCreationDialog(Component parent, RocketComponent component) {
		this.parent = parent;
		this.component = component;
		dialogPanel = new JPanel(new MigLayout("ins 0", "[right][grow]", "[][][][][][]"));

		SpinnerNumberModel dpiModel = new SpinnerNumberModel(prefs.getTextureGenerationDPI(), 10d, 2000d, 10d);
		dpiSpinner = new JSpinner(dpiModel);
		dpiSpinner.setEditor(new JSpinner.NumberEditor(dpiSpinner, "0"));
		JLabel dpiLabel = new JLabel(trans.get("AppearanceCfg.createTexture.lbl.dpi"));
		dpiLabel.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.dpi"));
		dpiSpinner.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.dpi"));
		dialogPanel.add(dpiLabel);
		dialogPanel.add(dpiSpinner, "wrap");

		fileNameField = new JTextField(20);
		fileNameField.setText(defaultFileName());
		dialogPanel.add(new JLabel(trans.get("AppearanceCfg.createTexture.lbl.filename")));
		dialogPanel.add(fileNameField, "growx, wrap");

		resetTransformsCheckbox = new JCheckBox(trans.get("AppearanceCfg.createTexture.lbl.resetTransforms"));
		resetTransformsCheckbox.setSelected(prefs.isTextureGenerationResetTransforms());
		resetTransformsCheckbox.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.resetTransforms"));
		dialogPanel.add(resetTransformsCheckbox, "skip, wrap");

		if (component instanceof FinSet) {
			// Outline checkbox
			outlineCheckbox = new JCheckBox(trans.get("AppearanceCfg.createTexture.lbl.outline"));
			outlineCheckbox.setSelected(prefs.isTextureGenerationDrawOutline());
			outlineCheckbox.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.outline"));
			dialogPanel.add(outlineCheckbox, "skip, wrap");

			// Outline width
			int storedWidth = prefs.getTextureGenerationOutlinePx();
			SpinnerNumberModel outlineModel = new SpinnerNumberModel(Math.max(0, storedWidth), 0, 50, 1);
			outlineWidthSpinner = new JSpinner(outlineModel);
			JLabel outlineWidthLabel = new JLabel(trans.get("AppearanceCfg.createTexture.lbl.outlineWidth"));
			outlineWidthLabel.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.outlineWidth"));
			outlineWidthSpinner.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.outlineWidth"));
			boolean outlineEnabled = outlineCheckbox.isSelected();
			outlineWidthLabel.setEnabled(outlineEnabled);
			outlineWidthSpinner.setEnabled(outlineEnabled);
			dialogPanel.add(outlineWidthLabel);
			dialogPanel.add(outlineWidthSpinner, "wrap");

			// Outline color
			outlineColorLabel = new JLabel(trans.get("AppearanceCfg.createTexture.lbl.outlineColor"));
			outlineColorLabel.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.outlineColor"));
			dialogPanel.add(outlineColorLabel);
			outlineColorButton = new ColorChooserButton(prefs.getTextureGenerationOutlineColor());
			outlineColorButton.setToolTipText(trans.get("AppearanceCfg.createTexture.lbl.ttip.outlineColor"));
			outlineColorButton.setEnabled(outlineCheckbox.isSelected());
			dialogPanel.add(outlineColorButton, "wrap");

			outlineCheckbox.addActionListener(e -> {
				boolean enabled = outlineCheckbox.isSelected();
				outlineWidthLabel.setEnabled(enabled);
				outlineWidthSpinner.setEnabled(enabled);
				outlineColorLabel.setEnabled(enabled);
				outlineColorButton.setEnabled(enabled);
			});
		} else {
			outlineCheckbox = null;
			outlineWidthSpinner = null;
			outlineColorLabel = null;
			outlineColorButton = null;
		}
	}

	public TextureCreationParameters showDialog() {
		int option = JOptionPane.showConfirmDialog(parent, dialogPanel,
				trans.get("AppearanceCfg.createTexture.dialog.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option != JOptionPane.OK_OPTION) {
			return null;
		}

		try {
			dpiSpinner.commitEdit();
		} catch (java.text.ParseException ex) {
			AppearancePanel.showCreateTextureError(parent,
					trans.get("AppearanceCfg.createTexture.msg.invalidDpi"));
			return null;
		}

		String requestedFileName = slugifyFileName(fileNameField.getText());
		if (requestedFileName.isEmpty()) {
			AppearancePanel.showCreateTextureError(parent,
					trans.get("AppearanceCfg.createTexture.msg.invalidFilename"));
			return null;
		}

		// Save preferences
		prefs.setTextureGenerationDPI((Double) dpiSpinner.getValue());
		if (outlineCheckbox != null) {
			prefs.setTextureGenerationDrawOutline(outlineCheckbox.isSelected());
			prefs.setTextureGenerationOutlinePx(((Number) outlineWidthSpinner.getValue()).intValue());
			prefs.setTextureGenerationOutlineColor(outlineColorButton.getSelectedColor());
		}
		prefs.setTextureGenerationResetTransforms(resetTransformsCheckbox.isSelected());

		boolean drawOutline = outlineCheckbox != null && outlineCheckbox.isSelected();
		int outlineWidth = outlineWidthSpinner != null ? ((Number) outlineWidthSpinner.getValue()).intValue() : 0;
		Color outlineColor = outlineColorButton != null ? outlineColorButton.getSelectedColor() : prefs.getTextureGenerationOutlineColor();
		return new TextureCreationParameters(((Number) dpiSpinner.getValue()).doubleValue(),
				requestedFileName, drawOutline, outlineWidth, resetTransformsCheckbox.isSelected(), outlineColor);
	}

	private String defaultFileName() {
		String name = component.getName();
		if (name == null || name.trim().isEmpty()) {
			name = component.getComponentName();
		}
		if (name == null) {
			name = "";
		}
		String slug = slugifyFileName(name);
		return slug.isEmpty() ? "texture" : slug;
	}

	private String slugifyFileName(String value) {
		if (value == null) {
			return "";
		}
		String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");
	}

	public record TextureCreationParameters(double dpi, String fileName, boolean drawFinOutline,
											int outlineWidthPx, boolean resetTransforms, Color outlineColor) { }
}
