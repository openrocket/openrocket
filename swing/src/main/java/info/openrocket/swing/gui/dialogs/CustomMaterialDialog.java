package info.openrocket.swing.gui.dialogs;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import info.openrocket.core.database.Databases;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.startup.Application;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.GUIUtil;

@SuppressWarnings("serial")
public class CustomMaterialDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	
	private final Material originalMaterial;
	private final boolean onlyCopyTypeFromMaterial;
	
	private boolean okClicked = false;
	private JComboBox<Material.Type> typeBox;
	private final JTextField nameField;
	private DoubleModel density;
	private final JSpinner densitySpinner;
	private final UnitSelector densityUnit;
	private JComboBox<MaterialGroup> groupBox;
	private JCheckBox addBox;

	public CustomMaterialDialog(Window parent, Material material, boolean saveOption, boolean addToApplicationDatabase,
								String title) {
		this(parent, material, saveOption, addToApplicationDatabase, title, null);
	}

	public CustomMaterialDialog(Window parent, Material material, boolean saveOption, boolean addToApplicationDatabase,
								boolean onlyCopyTypeFromMaterial, String title) {
		this(parent, material, saveOption, addToApplicationDatabase, onlyCopyTypeFromMaterial, title, null);
	}

	public CustomMaterialDialog(Window parent, Material material, boolean saveOption,
			String title) {
		this(parent, material, saveOption, material != null && !material.isDocumentMaterial(), title);
	}

	public CustomMaterialDialog(Window parent, Material material, boolean saveOption, boolean addToApplicationDatabase,
								String title, String note) {
		this(parent, material, saveOption, addToApplicationDatabase, false, title, note);
	}

	public CustomMaterialDialog(Window parent, Material material, boolean saveOption, boolean addToApplicationDatabase,
								boolean onlyCopyTypeFromMaterial, String title, String note) {
		//// Custom material
		super(parent, trans.get("custmatdlg.title.Custommaterial"), Dialog.ModalityType.APPLICATION_MODAL);

		this.originalMaterial = material;
		this.onlyCopyTypeFromMaterial = onlyCopyTypeFromMaterial;

		JPanel panel = new JPanel(new MigLayout("fill, gap rel unrel"));


		// Add title and note
		if (title != null) {
			panel.add(new JLabel("<html><b>" + title + ":"),
					"gapleft para, span, wrap" + (note == null ? " para" : ""));
		}
		if (note != null) {
			panel.add(new StyledLabel(note, -1), "span, wrap para");
		}


		//// Material name
		panel.add(new JLabel(trans.get("custmatdlg.lbl.Materialname")));
		nameField = new JTextField(15);
		if (!onlyCopyTypeFromMaterial && material != null) {
			nameField.setText(material.getName());
		}
		panel.add(nameField, "span, growx, wrap");


		// Material type (if not known)
		panel.add(new JLabel(trans.get("custmatdlg.lbl.Materialtype")));
		if (material == null) {
			// Remove the CUSTOM material option from the dropdown box
			Material.Type[] values = Material.Type.values();
			List<Material.Type> values_list = new LinkedList<>(Arrays.asList(values));
			values_list.remove(Material.Type.CUSTOM);
			values = values_list.toArray(new Material.Type[0]);

			typeBox = new JComboBox<>(values);
			typeBox.setSelectedItem(Material.Type.BULK);
			typeBox.setEditable(false);
			typeBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateDensityModel();
				}
			});
			panel.add(typeBox, "span, growx, wrap");
		} else {
			panel.add(new JLabel(material.getType().toString()), "span, growx, wrap");
		}


		// Material density:
		panel.add(new JLabel(trans.get("custmatdlg.lbl.Materialdensity")));
		densitySpinner = new JSpinner();
		panel.add(densitySpinner, "w 70lp");
		densityUnit = new UnitSelector((DoubleModel) null);
		panel.add(densityUnit, "w 30lp");
		panel.add(new JPanel(), "growx, wrap");
		updateDensityModel();


		// Material group
		panel.add(new JLabel(trans.get("custmatdlg.lbl.MaterialGroup")));
		groupBox = new JComboBox<>(MaterialGroup.ALL_GROUPS);
		if (!onlyCopyTypeFromMaterial && material != null) {
			groupBox.setSelectedItem(material.getGroup());
		} else {
			groupBox.setSelectedItem(MaterialGroup.CUSTOM);
		}
		panel.add(groupBox, "span, growx, wrap");


		// Save option
		if (saveOption) {
			//// Add material to application database
			addBox = new JCheckBox(trans.get("custmatdlg.checkbox.Addmaterial"));
			addBox.setSelected(addToApplicationDatabase);
			panel.add(addBox, "span, wrap");
		}

		//// OK button
		JButton okButton = new JButton(trans.get("dlg.but.ok"));

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				CustomMaterialDialog.this.setVisible(false);
			}
		});
		panel.add(okButton, "span, split, tag ok");

		////  Cancel
		JButton closeButton = new JButton(trans.get("dlg.but.cancel"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okClicked = false;
				CustomMaterialDialog.this.setVisible(false);
			}
		});
		panel.add(closeButton, "tag cancel");

		this.setContentPane(panel);
		this.pack();
		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
	
	public CustomMaterialDialog(Window parent, Material material, boolean saveOption,
			String title, String note) {
		this(parent, material, saveOption, material != null && !material.isDocumentMaterial(), title, note);
	}
	
	
	public boolean getOkClicked() {
		return okClicked;
	}
	
	
	public boolean isAddSelected() {
		return addBox.isSelected();
	}
	
	
	public Material getMaterial() {
		Material.Type type;
		String name;
		double materialDensity;
		MaterialGroup group;
		
		if (typeBox != null) {
			type = (Material.Type) typeBox.getSelectedItem();
		} else {
			type = originalMaterial.getType();
		}
		
		name = nameField.getText().trim();
		materialDensity = this.density.getValue();
		group = (MaterialGroup) groupBox.getSelectedItem();
		
		return Databases.findMaterial(type, name, materialDensity, group);
	}
	
	
	private void updateDensityModel() {
		if (originalMaterial != null) {
			if (density == null) {
				double densityValue = onlyCopyTypeFromMaterial ? 0 : originalMaterial.getDensity();
				density = new DoubleModel(densityValue,
						originalMaterial.getType().getUnitGroup(), 0);
				densitySpinner.setModel(density.getSpinnerModel());
				densitySpinner.setEditor(new SpinnerEditor(densitySpinner));
				densityUnit.setModel(density);
			}
		} else {
			Material.Type type = (Material.Type) typeBox.getSelectedItem();
			density = new DoubleModel(0, type.getUnitGroup(), 0);
			densitySpinner.setModel(density.getSpinnerModel());
			densitySpinner.setEditor(new SpinnerEditor(densitySpinner));
			densityUnit.setModel(density);
		}
	}
}
