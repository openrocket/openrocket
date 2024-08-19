package info.openrocket.swing.gui.widgets;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.adaptors.MaterialModel;
import info.openrocket.swing.gui.dialogs.preferences.PreferencesDialog;
import info.openrocket.swing.gui.main.BasicFrame;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

public abstract class MaterialComboBox extends JComboBox<Material> {
	private static final Translator trans = Application.getTranslator();

	public static GroupableAndSearchableComboBox<MaterialGroup, Material> createComboBox(OpenRocketDocument document, MaterialModel mm) {
		// Set custom material button
		JButton customMaterialButton = new JButton(trans.get("MaterialPanel.but.AddCustomMaterial"));
		customMaterialButton.addActionListener(e -> {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mm.addCustomMaterial();
				}
			});
		});

		// Edit materials button
		JButton editMaterialsButton = new JButton(trans.get("MaterialPanel.but.EditMaterials"));
		editMaterialsButton.addActionListener(e -> {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (BasicFrame frame : BasicFrame.getAllFrames()) {
						if (frame.getRocketPanel().getDocument() == document) {
							PreferencesDialog.showPreferences(frame, 5);
							return;
						}
					}
				}
			});
		});

		GroupableAndSearchableComboBox<MaterialGroup, Material> comboBox = new GroupableAndSearchableComboBox<>(mm,
				trans.get("MaterialPanel.MaterialComboBox.placeholder"), customMaterialButton, editMaterialsButton) {
			@Override
			public String getDisplayString(Material item) {
				String baseText = item.toString();
				if (item.isUserDefined()) {
					baseText = "(ud) " + baseText;
				}
				return baseText;
			}
		};

		// Ensure combobox is hidden when buttons are clicked
		customMaterialButton.addActionListener(e -> {
			comboBox.hidePopups();
		});
		editMaterialsButton.addActionListener(e -> {
			comboBox.hidePopups();
		});

		return comboBox;
	}
}
