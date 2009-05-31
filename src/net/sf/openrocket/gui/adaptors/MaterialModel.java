package net.sf.openrocket.gui.adaptors;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Reflection;

public class MaterialModel extends AbstractListModel implements
		ComboBoxModel, ChangeListener {
	
	private static final String CUSTOM = "Custom";

	
	private final RocketComponent component;
	private final Material.Type type;
	private final Database<Material> database;
	
	private final Reflection.Method getMethod;
	private final Reflection.Method setMethod;
	
	
	public MaterialModel(RocketComponent component, Material.Type type) {
		this(component, type, "Material");
	}	

	public MaterialModel(RocketComponent component, Material.Type type, String name) {
		this.component = component;
		this.type = type;
		
		switch (type) {
		case LINE:
			this.database = Databases.LINE_MATERIAL;
			break;
			
		case BULK:
			this.database = Databases.BULK_MATERIAL;
			break;
			
		case SURFACE:
			this.database = Databases.SURFACE_MATERIAL;
			break;
			
		default:
			throw new IllegalArgumentException("Unknown material type:"+type);
		}
		
		try {
			getMethod = new Reflection.Method(component.getClass().getMethod("get"+name));
			setMethod = new Reflection.Method(component.getClass().getMethod("set"+name,
					Material.class));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get/is methods for material " +
					"not present in class "+component.getClass().getCanonicalName());
		}
		
		component.addChangeListener(this);
		database.addChangeListener(this);
	}
	
	@Override
	public Object getSelectedItem() {
		return getMethod.invoke(component);
	}

	@Override
	public void setSelectedItem(Object item) {
		if (item == CUSTOM) {
			
			// Open custom material dialog in the future, after combo box has closed
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AddMaterialDialog dialog = new AddMaterialDialog();
					dialog.setVisible(true);
					
					if (!dialog.okClicked)
						return;
					
					Material material = Material.newMaterial(type, 
							dialog.nameField.getText().trim(),
							dialog.density.getValue());
					setMethod.invoke(component, material);
					
					// TODO: HIGH: Allow saving added material to database
//					if (dialog.addBox.isSelected()) {
//						database.add(material);
//					}
				}
			});
			
		} else if (item instanceof Material) {
			
			setMethod.invoke(component, item);
			
		} else {
			assert(false): "Should not occur";
		}
	}

	@Override
	public Object getElementAt(int index) {
		if (index == database.size()) {
			return CUSTOM;
		} else if (index >= database.size()+1) {
			return null;
		}
		return database.get(index);
	}

	@Override
	public int getSize() {
		return database.size() + 1;
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		if (e instanceof ComponentChangeEvent) {
			if (((ComponentChangeEvent)e).isMassChange()) {
				this.fireContentsChanged(this, 0, 0);
			}
		} else {
			this.fireContentsChanged(this, 0, database.size());
		}
	}
	
	
	
	
	private class AddMaterialDialog extends JDialog {
		
		private boolean okClicked = false;
		private JTextField nameField;
		private DoubleModel density;
//		private JCheckBox addBox;
		
		public AddMaterialDialog() {
			super((JFrame)null, "Custom material", true);
			
			Material material = (Material) getSelectedItem();
			
			JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]"));
			
			panel.add(new JLabel("Material name:"));
			nameField = new JTextField(15);
			nameField.setText(material.getName());
			panel.add(nameField,"span 2, growx, wrap");
			
			panel.add(new JLabel("Material density:"));
			density = new DoubleModel(material.getDensity(),UnitGroup.UNITS_DENSITY_BULK,0);
			JSpinner spinner = new JSpinner(density.getSpinnerModel());
			panel.add(spinner, "growx");
			panel.add(new UnitSelector(density),"wrap");
			
//			addBox = new JCheckBox("Add material to database");
//			panel.add(addBox,"span, wrap");
			
			JButton button = new JButton("OK");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					okClicked = true;
					AddMaterialDialog.this.setVisible(false);
				}
			});
			panel.add(button,"span, split, tag ok");
			
			button = new JButton("Cancel");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddMaterialDialog.this.setVisible(false);
				}
			});
			panel.add(button,"tag cancel");
			
			this.setContentPane(panel);
			this.pack();
			this.setAlwaysOnTop(true);
			this.setLocationRelativeTo(null);
		}
		
	}
}
