package info.openrocket.swing.gui.adaptors;


import java.awt.Component;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;

import info.openrocket.core.database.Database;
import info.openrocket.core.database.DatabaseListener;
import info.openrocket.core.database.Databases;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Invalidatable;
import info.openrocket.core.util.Reflection;

import info.openrocket.swing.gui.dialogs.CustomMaterialDialog;

public class MaterialModel extends AbstractListModel<Material> implements
		ComboBoxModel<Material>, ComponentChangeListener, DatabaseListener<Material>, Invalidatable {
	private static final long serialVersionUID = 4552478532933113655L;
	private final ModelInvalidator modelInvalidator;
	
	private final Component parentUIComponent;
	
	private final RocketComponent rocketComponent;
	private final OpenRocketDocument document;
	private final Material.Type type;
	private final Database<Material> applicationDatabase;
	private final Database<Material> documentDatabase;
	
	private final Reflection.Method getMethod;
	private final Reflection.Method setMethod;
	private static final Translator trans = Application.getTranslator();
	
	
	public MaterialModel(Component parent, OpenRocketDocument document, RocketComponent component, Material.Type type) {
		//// Material
		//this(parent, component, type, trans.get("MaterialModel.title.Material"));
		this(parent, document, component, type, "Material");
	}	

	public MaterialModel(Component parent, OpenRocketDocument document, RocketComponent component, Material.Type type,
			String name) {
		this.modelInvalidator = new ModelInvalidator(component, this);
		this.parentUIComponent = parent;
		this.document = document;
		this.rocketComponent = component;
		this.type = type;

		this.applicationDatabase = Databases.getDatabase(type);
		this.documentDatabase = document.getDocumentPreferences().getDatabase(type);
		
		try {
			getMethod = new Reflection.Method(component.getClass().getMethod("get" + name));
			setMethod = new Reflection.Method(component.getClass().getMethod("set" + name,
					Material.class));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("get/is methods for material " +
					"not present in class "+component.getClass().getCanonicalName());
		}
		
		component.addComponentChangeListener(this);
		applicationDatabase.addDatabaseListener(this);
		documentDatabase.addDatabaseListener(this);
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

		if (item instanceof Material) {
			setMethod.invoke(rocketComponent, item);
		} else {
			throw new IllegalArgumentException("Illegal item class " + item.getClass() + 
					" item=" + item);
		}
	}

	public void addCustomMaterial() {
		CustomMaterialDialog dialog = new CustomMaterialDialog(
				SwingUtilities.getWindowAncestor(parentUIComponent),
				(Material) getSelectedItem(), true, false, true,
				trans.get("MaterialModel.title.Defcustmat"));

		dialog.setVisible(true);

		if (!dialog.getOkClicked())
			return;

		Material material = dialog.getMaterial();
		if (dialog.isAddSelected()) {
			material.setDocumentMaterial(false);
			this.applicationDatabase.add(material);
		} else {
			material.setDocumentMaterial(true);
			this.documentDatabase.add(material);
		}

		this.setMethod.invoke(this.rocketComponent, material);
	}

	@Override
	public Material getElementAt(int index) {
		if (index < applicationDatabase.size()) {
			return applicationDatabase.get(index);
		} else if (index < applicationDatabase.size() + documentDatabase.size()) {
			return documentDatabase.get(index - applicationDatabase.size());
		}
		return null;
	}

	public Material[] getAllMaterials() {
		Material[] materials = new Material[applicationDatabase.size()+documentDatabase.size()];
		for (int i = 0; i < applicationDatabase.size(); i++) {
			materials[i] = applicationDatabase.get(i);
		}
		for (int i = 0; i < documentDatabase.size(); i++) {
			materials[i+applicationDatabase.size()] = documentDatabase.get(i);
		}
		return materials;
	}

	@Override
	public int getSize() {
		return applicationDatabase.size() + documentDatabase.size();
	}

	public Material.Type getType() {
		return type;
	}

	////////  Change listeners

	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isMassChange()) {
			this.fireContentsChanged(this, 0, 0);
		}
	}

	@Override
	public void elementAdded(Material element, Database<Material> source) {
		this.fireContentsChanged(this, 0, applicationDatabase.size() + documentDatabase.size());
	}

	@Override
	public void elementRemoved(Material element, Database<Material> source) {
		this.fireContentsChanged(this, 0, applicationDatabase.size() + documentDatabase.size());
	}

	@Override
	public void invalidateMe() {
		modelInvalidator.invalidateMe();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		modelInvalidator.finalize();
	}
}
