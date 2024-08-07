package info.openrocket.swing.gui.configdialog;

import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.util.Invalidatable;
import info.openrocket.swing.gui.dialogs.preferences.PreferencesDialog;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.widgets.SearchableAndCategorizableComboBox;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.MaterialModel;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for configuring a component's material and finish properties.
 */
public class MaterialPanel extends JPanel implements Invalidatable, InvalidatingWidget {
    private static final Translator trans = Application.getTranslator();
    private final List<Invalidatable> invalidatables = new ArrayList<>();
    private SearchableAndCategorizableComboBox<MaterialGroup, Material> materialCombo = null;

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, String materialString, String finishString,
                         String partName, List<Component> order) {
        super(new MigLayout());
        this.setBorder(BorderFactory.createTitledBorder(trans.get("MaterialPanel.title.Material")));

        //// Component material
        JLabel label = new JLabel(materialString);
        label.setToolTipText(trans.get("MaterialPanel.lbl.ttip.ComponentMaterialAffects"));
        this.add(label, "spanx 4, wrap rel");

        MaterialModel mm = new MaterialModel(this, document, component, type, partName);
        register(mm);

        // Set custom material button
        JButton customMaterialButton = new JButton(trans.get("MaterialPanel.but.AddCustomMaterial"));
        customMaterialButton.addActionListener(e -> {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mm.addCustomMaterial();
                    if (MaterialPanel.this.materialCombo != null) {
                        MaterialComboBox.updateComboBoxItems(MaterialPanel.this.materialCombo, MaterialGroup.ALL_GROUPS,
                                mm.getAllMaterials());
                        MaterialPanel.this.materialCombo.setSelectedItem(mm.getSelectedItem());
                    }
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

        // Material selection combo box
        this.materialCombo = MaterialComboBox.createComboBox(mm, MaterialGroup.ALL_GROUPS, mm.getAllMaterials(),
                customMaterialButton, editMaterialsButton);
        this.materialCombo.setSelectedItem(mm.getSelectedItem());
        this.materialCombo.setToolTipText(trans.get("MaterialPanel.combo.ttip.ComponentMaterialAffects"));
        this.add(this.materialCombo, "spanx 4, growx, wrap paragraph");
        order.add(this.materialCombo);

        // No surface finish for internal components
        if (!(component instanceof ExternalComponent)) {
            return;
        }

        //// Surface finish
        label = new JLabel(finishString);
        String tip = trans.get("MaterialPanel.lbl.ComponentFinish.ttip.longA1")
                //// The value indicated is the average roughness height of the surface.
                + trans.get("MaterialPanel.lbl.ComponentFinish.ttip.longA2");
        label.setToolTipText(tip);
        this.add(label, "spanx 4, wmin 220lp, wrap rel");

        EnumModel<ExternalComponent.Finish> em = new EnumModel<>(component, "Finish");
        register(em);
        JComboBox<ExternalComponent.Finish> finishCombo = new JComboBox<>(em);
        finishCombo.setToolTipText(tip);
        this.add(finishCombo, "spanx 4, growx, split");
        order.add(finishCombo);

        //// Set for all
        JButton button = new SelectColorButton(trans.get("MaterialPanel.but.SetForAll"));
        //// Set this finish for all components of the rocket.
        button.setToolTipText(trans.get("MaterialPanel.but.SetForAll.ttip"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExternalComponent.Finish f = ((ExternalComponent) component).getFinish();
                try {
                    document.startUndo("Set rocket finish");

                    // Do changes
					for (RocketComponent c : component.getRocket()) {
						if (c instanceof ExternalComponent) {
							((ExternalComponent) c).setFinish(f);
						}
					}
                } finally {
                    document.stopUndo();
                }
            }
        });
        this.add(button);
        order.add(button);
    }

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, String partName, List<Component> order) {
        this(component, document, type, trans.get("MaterialPanel.lbl.ComponentMaterial"),
                trans.get("MaterialPanel.lbl.ComponentFinish"), partName, order);
    }

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, List<Component> order) {
        this(component, document, type, trans.get("MaterialPanel.lbl.ComponentMaterial"),
                trans.get("MaterialPanel.lbl.ComponentFinish"), "Material", order);
    }

    @Override
    public void register(Invalidatable model) {
        this.invalidatables.add(model);
    }

    @Override
    public void invalidateMe() {
        super.invalidate();
        for (Invalidatable i : invalidatables) {
            i.invalidateMe();
        }
    }

    public static class MaterialComboBox extends JComboBox<Material> {
        private static final Translator trans = Application.getTranslator();

        public static SearchableAndCategorizableComboBox<MaterialGroup, Material> createComboBox(
                MaterialModel mm, MaterialGroup[] allGroups, Material[] materials, Component... extraCategoryWidgets) {
            final Map<MaterialGroup, List<Material>> materialGroupMap =
                    createMaterialGroupMap(allGroups, materials);
            return new SearchableAndCategorizableComboBox<>(mm, materialGroupMap,
                    trans.get("MaterialPanel.MaterialComboBox.placeholder"), extraCategoryWidgets) {
                @Override
                public String getDisplayString(Material item) {
                    String baseText = item.toString();
                    if (item.isUserDefined()) {
                        baseText = "(ud) " + baseText;
                    }
                    return baseText;
                }
            };
        }

        public static void updateComboBoxItems(SearchableAndCategorizableComboBox<MaterialGroup, Material> comboBox,
                                               MaterialGroup[] allGroups, Material[] materials) {
            final Map<MaterialGroup, List<Material>> materialGroupMap = createMaterialGroupMap(allGroups, materials);
            comboBox.updateItems(materialGroupMap);
            comboBox.invalidate();
            comboBox.repaint();
        }

        /**
         * Create a map of material group and corresponding material.
         * @param groups the groups
         * @param materials the materials
         * @return the map linking the materials to their groups
         */
        private static Map<MaterialGroup, List<Material>> createMaterialGroupMap(
                MaterialGroup[] groups, Material[] materials) {
            // Sort the groups based on priority (lower number = higher priority)
            MaterialGroup[] sortedGroups = groups.clone();
            Arrays.sort(sortedGroups, Comparator.comparingInt(MaterialGroup::getPriority));

            Map<MaterialGroup, List<Material>> map = new LinkedHashMap<>();
            MaterialGroup materialGroup;
            for (MaterialGroup group : sortedGroups) {
                List<Material> itemsForGroup = new ArrayList<>();
                for (Material material : materials) {
                    materialGroup = material.getGroup();
                    if (materialGroup.equals(group)) {
                        itemsForGroup.add(material);
                    }
                }
                // Sort the types within each group based on priority
                itemsForGroup.sort(Comparator.comparingInt(Material::getGroupPriority));

                map.put(group, itemsForGroup);
            }

            // Remove empty groups
            map.entrySet().removeIf(entry -> entry.getValue().isEmpty());

            return map;
        }
    }
}
