package net.sf.openrocket.gui.preset;

import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.DatabaseListener;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.dialogs.CustomMaterialDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.startup.Application;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * A material model specifically for presets.
 */
public class MaterialModel extends DefaultComboBoxModel<Material> implements DatabaseListener<Material> {
	private static final long serialVersionUID = -8850670173491660708L;

	private static final Material CUSTOM_MATERIAL = Material.newMaterial(Type.CUSTOM, "Custom", 1.0, true);

    private final Database<Material> database;

    private static final Translator trans = Application.getTranslator();

    private Material.Type type;

    private Component parent;

    public MaterialModel(Component theParent, Material.Type theType, Database<Material> materials) {
        parent = theParent;
        type = theType;
        database = materials;
        database.addDatabaseListener(this);
    }

    public MaterialModel(Component theParent, Material.Type theType) {
        parent = theParent;
        type = theType;

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
                throw new IllegalArgumentException("Unknown material type:" + type);
        }

        database.addDatabaseListener(this);
    }

    @Override
    public void setSelectedItem(Object item) {
        if (item == null) {
            // Clear selection - huh?
            return;
        }

        if (item == CUSTOM_MATERIAL) {

            // Open custom material dialog in the future, after combo box has closed
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CustomMaterialDialog dialog = new CustomMaterialDialog(SwingUtilities.getWindowAncestor(parent), (Material) getSelectedItem(), true,
                            //// Define custom material
                            trans.get("MaterialModel.title.Defcustmat"));

                    dialog.setVisible(true);

                    if (!dialog.getOkClicked()) {
                        return;
                    }

                    Material material = dialog.getMaterial();
                    MaterialModel.super.setSelectedItem(material);
                    if (dialog.isAddSelected()) {
                        database.add(material);
                    }
                }
            });

        }
        else if (item instanceof Material) {
            super.setSelectedItem(item);
        }
        else {
            throw new IllegalArgumentException("Illegal item class " + item.getClass() +
                    " item=" + item);
        }
    }

    @Override
    public Material getElementAt(int index) {
        if (index == database.size()) {
            return CUSTOM_MATERIAL;
        }
        else if (index >= database.size() + 1) {
            return null;
        }
        return database.get(index);
    }

    @Override
    public int getSize() {
        return database.size() + 1;
    }

    ////////  Change listeners

    @Override
    public void elementAdded(Material element, Database<Material> source) {
        this.fireContentsChanged(this, 0, database.size());
    }

    @Override
    public void elementRemoved(Material element, Database<Material> source) {
        this.fireContentsChanged(this, 0, database.size());
    }

    public Material.Type getType() {
        return type;
    }

    public void removeListener() {
        database.removeChangeListener(this);
    }
}
