package net.sf.openrocket.gui.adaptors;


import java.awt.Component;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;

import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.DatabaseListener;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.dialogs.CustomMaterialDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Reflection;

public class MaterialModel extends AbstractListModel implements
		ComboBoxModel, ComponentChangeListener, DatabaseListener<Material> {
	
	private final String custom;

	
	private final Component parentUIComponent;
	
	private final RocketComponent rocketComponent;
	private final Material.Type type;
	private final Database<Material> database;
	
	private final Reflection.Method getMethod;
	private final Reflection.Method setMethod;
	private static final Translator trans = Application.getTranslator();
	
	
	public MaterialModel(Component parent, RocketComponent component, Material.Type type) {
		//// Material
		//this(parent, component, type, trans.get("MaterialModel.title.Material"));
		this(parent, component, type, "Material");
	}	

	public MaterialModel(Component parent, RocketComponent component, Material.Type type, 
			String name) {
		this.parentUIComponent = parent;
		this.rocketComponent = component;
		this.type = type;
		this.custom = trans.get ("Material.CUSTOM");
		
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
		
		component.addComponentChangeListener(this);
		database.addDatabaseListener(this);
	}
	
	@Override
	public Object getSelectedItem() {
		return getMethod.invoke(rocketComponent);
	}

	@Override
	public void setSelectedItem(Object item) {
		if (item == null) {
			// Clear selection - huh?
			return;
		}

		if (item == custom) {
			
			// Open custom material dialog in the future, after combo box has closed
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					CustomMaterialDialog dialog = new CustomMaterialDialog(
							SwingUtilities.getWindowAncestor(parentUIComponent), 
							(Material) getSelectedItem(), true,
							//// Define custom material
							trans.get("MaterialModel.title.Defcustmat"));

					dialog.setVisible(true);
					
					if (!dialog.getOkClicked())
						return;
					
					Material material = dialog.getMaterial();
					setMethod.invoke(rocketComponent, material);
					
					if (dialog.isAddSelected()) {
						database.add(material);
					}
				}
			});
			
		} else if (item instanceof Material) {
			
			setMethod.invoke(rocketComponent, item);
			
		} else {
			throw new IllegalArgumentException("Illegal item class " + item.getClass() + 
					" item=" + item);
		}
	}

	@Override
	public Object getElementAt(int index) {
		if (index == database.size()) {
			return custom;
		} else if (index >= database.size()+1) {
			return null;
		}
		return database.get(index);
	}

	@Override
	public int getSize() {
		return database.size() + 1;
	}

	public Material.Type getType() {
		return type;
	}

	////////  Change listeners

	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (((ComponentChangeEvent)e).isMassChange()) {
			this.fireContentsChanged(this, 0, 0);
		}
	}

	@Override
	public void elementAdded(Material element, Database<Material> source) {
		this.fireContentsChanged(this, 0, database.size());
	}

	@Override
	public void elementRemoved(Material element, Database<Material> source) {
		this.fireContentsChanged(this, 0, database.size());
	}
	
}
