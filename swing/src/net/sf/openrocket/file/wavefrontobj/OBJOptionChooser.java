package net.sf.openrocket.file.wavefrontobj;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.file.wavefrontobj.export.OBJExportOptions;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class OBJOptionChooser extends JPanel {
    private static final Translator trans = Application.getTranslator();

    // Widgets
    private final JCheckBox exportChildren;
    private final JCheckBox exportAppearance;
    private final JCheckBox exportAsSeparateFiles;
    private final JCheckBox removeOffset;
    private final JCheckBox triangulate;
    private final JComboBox<ObjUtils.LevelOfDetail> LOD;

    private final List<RocketComponent> selectedComponents;

    public OBJOptionChooser(OBJExportOptions opts, List<RocketComponent> selectedComponents) {
        super(new MigLayout());

        this.selectedComponents = selectedComponents;

        // ------------ Basic options ------------
        //// Export children
        this.exportChildren = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportChildren"));
        this.add(exportChildren, "spanx, wrap");

        //// Export appearance
        this.exportAppearance = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAppearance"));
        this.exportAppearance.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAppearance.ttip"));
        this.add(exportAppearance, "spanx, wrap");

        //// Export as separate files
        this.exportAsSeparateFiles = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles"));
        this.exportAsSeparateFiles.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles.ttip"));
        this.add(exportAsSeparateFiles, "spanx, wrap");

        //// Remove offsets
        this.removeOffset = new JCheckBox(trans.get("OBJOptionChooser.checkbox.removeOffset"));
        this.removeOffset.setToolTipText(trans.get("OBJOptionChooser.checkbox.removeOffset.ttip"));
        this.add(removeOffset, "spanx, wrap para");


        // ------------ Advanced options ------------
        this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap");

        // Show advanced options toggle
        JToggleButton advancedToggle = new JToggleButton(trans.get("OBJOptionChooser.btn.showAdvanced"));
        this.add(advancedToggle, "spanx, wrap para");

        // Panel for advanced options
        JPanel advancedOptionsPanel = new JPanel();
        advancedOptionsPanel.setLayout(new MigLayout("ins 0"));
        advancedOptionsPanel.setVisible(false);

        //// Triangulate
        this.triangulate = new JCheckBox(trans.get("OBJOptionChooser.checkbox.triangulate"));
        this.triangulate.setToolTipText(trans.get("OBJOptionChooser.checkbox.triangulate.ttip"));
        advancedOptionsPanel.add(triangulate, "spanx, wrap");


        //// Level of detail
        JLabel LODLabel = new JLabel(trans.get("OBJOptionChooser.lbl.LevelOfDetail"));
        LODLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        advancedOptionsPanel.add(LODLabel);
        this.LOD = new JComboBox<>(ObjUtils.LevelOfDetail.values());
        this.LOD.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        advancedOptionsPanel.add(LOD, "spanx, wrap para");


        //// Scale
        // TODO: + (add tooltip text that mm is useful for 3D printing)


        //// Coordinate transformer
        // TODO


        // Add action listener to the toggle button
        advancedToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // Toggle the visibility of the advanced options panel
                advancedOptionsPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);

                // Refresh the UI after changing visibility
                OBJOptionChooser.this.revalidate();
                OBJOptionChooser.this.repaint();
            }
        });
        this.add(advancedOptionsPanel);

        loadOptions(opts);
    }

    public void loadOptions(OBJExportOptions opts) {
        boolean onlyComponentAssemblies = isOnlyComponentAssembliesSelected(selectedComponents);
        if (onlyComponentAssemblies) {
            exportChildren.setEnabled(false);
            exportChildren.setSelected(true);
            exportChildren.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportChildren.assemblies.ttip"));
        } else {
            exportChildren.setEnabled(true);
            exportChildren.setSelected(opts.isExportChildren());
            exportChildren.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportChildren.ttip"));
        }

        this.exportAppearance.setSelected(opts.isExportAppearance());
        this.exportAsSeparateFiles.setSelected(opts.isExportAsSeparateFiles());
        this.removeOffset.setSelected(opts.isRemoveOffset());
        this.triangulate.setSelected(opts.isTriangulate());

        this.LOD.setSelectedItem(opts.getLOD());
    }

    public void storeOptions(OBJExportOptions opts) {
        boolean onlyComponentAssemblies = isOnlyComponentAssembliesSelected(selectedComponents);
        // Don't save the state when the checkbox is set automatically due to component assemblies
        if (!onlyComponentAssemblies) {
            opts.setExportChildren(exportChildren.isSelected());
        }
        opts.setExportAppearance(exportAppearance.isSelected());
        opts.setExportAsSeparateFiles(exportAsSeparateFiles.isSelected());
        opts.setRemoveOffset(removeOffset.isSelected());
        opts.setTriangulate(triangulate.isSelected());
        opts.setLOD((ObjUtils.LevelOfDetail) LOD.getSelectedItem());
    }

    private static boolean isOnlyComponentAssembliesSelected(List<RocketComponent> selectedComponents) {
        boolean onlyComponentAssemblies = true;
        for (RocketComponent component : selectedComponents) {
            if (!(component instanceof ComponentAssembly)) {
                onlyComponentAssemblies = false;
                break;
            }
        }
        return onlyComponentAssemblies;
    }
}
