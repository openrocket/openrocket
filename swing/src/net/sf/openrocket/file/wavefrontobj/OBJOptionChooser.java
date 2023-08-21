package net.sf.openrocket.file.wavefrontobj;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.file.wavefrontobj.export.OBJExportOptions;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import java.awt.Window;
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
    private final JCheckBox sRGB;
    private final JComboBox<ObjUtils.LevelOfDetail> LOD;
    private final DoubleModel scalingModel;
    private final JComboBox<Axis> axialCombo;
    private final JComboBox<Axis> forwardCombo;

    private final List<RocketComponent> selectedComponents;
    private final Rocket rocket;

    private boolean isProgrammaticallyChanging = false;

    public OBJOptionChooser(OBJExportOptions opts, List<RocketComponent> selectedComponents, Rocket rocket) {
        super(new MigLayout("hidemode 3"));

        this.selectedComponents = selectedComponents;
        this.rocket = rocket;

        // ------------ Basic options ------------
        //// Export children
        this.exportChildren = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportChildren"));
        this.add(exportChildren, "spanx, wrap");

        //// Remove origin offset
        this.removeOffset = new JCheckBox(trans.get("OBJOptionChooser.checkbox.removeOffset"));
        this.removeOffset.setToolTipText(trans.get("OBJOptionChooser.checkbox.removeOffset.ttip"));
        this.add(removeOffset, "spanx, wrap unrel");

        //// Export appearance
        this.exportAppearance = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAppearance"));
        this.exportAppearance.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAppearance.ttip"));
        this.add(exportAppearance, "spanx, wrap");

        //// Export as separate files
        this.exportAsSeparateFiles = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles"));
        this.exportAsSeparateFiles.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles.ttip"));
        this.add(exportAsSeparateFiles, "spanx, wrap unrel");

        //// Scaling
        JLabel scalingLabel = new JLabel(trans.get("OBJOptionChooser.lbl.Scaling"));
        scalingLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.Scaling.ttip"));
        this.add(scalingLabel, "spanx, split 2");
        this.scalingModel = new DoubleModel(opts, "ScalingDouble", UnitGroup.UNITS_SCALING, 0, 10000);
        JSpinner spin = new JSpinner(scalingModel.getSpinnerModel());
        spin.setToolTipText(trans.get("OBJOptionChooser.lbl.Scaling.ttip"));
        spin.setEditor(new SpinnerEditor(spin));
        this.add(spin, "wrap para");


        // ------------ Advanced options ------------
        this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap");

        // Show advanced options toggle
        JToggleButton advancedToggle = new JToggleButton(trans.get("OBJOptionChooser.btn.showAdvanced"));
        this.add(advancedToggle, "spanx, wrap para");

        // Panel for advanced options
        JPanel advancedOptionsPanel = new JPanel();
        advancedOptionsPanel.setLayout(new MigLayout("ins 0"));
        advancedOptionsPanel.setVisible(false);

        //// Export colors in sRGB
        this.sRGB = new JCheckBox(trans.get("OBJOptionChooser.checkbox.sRGB"));
        this.sRGB.setToolTipText(trans.get("OBJOptionChooser.checkbox.sRGB.ttip"));
        advancedOptionsPanel.add(sRGB, "spanx, wrap");

        //// Triangulate
        this.triangulate = new JCheckBox(trans.get("OBJOptionChooser.checkbox.triangulate"));
        this.triangulate.setToolTipText(trans.get("OBJOptionChooser.checkbox.triangulate.ttip"));
        advancedOptionsPanel.add(triangulate, "spanx, wrap");

        //// Level of detail
        JLabel LODLabel = new JLabel(trans.get("OBJOptionChooser.lbl.LevelOfDetail"));
        LODLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        advancedOptionsPanel.add(LODLabel, "spanx, split 2");
        this.LOD = new JComboBox<>(ObjUtils.LevelOfDetail.values());
        this.LOD.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        advancedOptionsPanel.add(LOD, "growx, wrap unrel");


        //// Coordinate transformer
        JLabel coordTransLabel = new JLabel(trans.get("OBJOptionChooser.lbl.CoordinateTransform"));
        coordTransLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.CoordinateTransform.ttip"));
        advancedOptionsPanel.add(coordTransLabel, "spanx, wrap");

        ////// Axial (up) axis
        JLabel axialLabel = new JLabel(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Axial"));
        axialLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Axial.ttip"));
        advancedOptionsPanel.add(axialLabel, "gapleft 10lp");
        this.axialCombo = new JComboBox<>(Axis.values());
        this.axialCombo.setToolTipText(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Axial.ttip"));
        advancedOptionsPanel.add(axialCombo, "wrap");

        ////// Forward axis
        JLabel forwardLabel = new JLabel(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Forward"));
        forwardLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Forward.ttip"));
        advancedOptionsPanel.add(forwardLabel, "gapleft 10lp");
        this.forwardCombo = new JComboBox<>(Axis.values());
        this.forwardCombo.setToolTipText(trans.get("OBJOptionChooser.lbl.CoordinateTransform.Forward.ttip"));
        advancedOptionsPanel.add(forwardCombo, "wrap");

        //// Set up the listeners for the coordinate transformer combo boxes
        this.axialCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!isProgrammaticallyChanging) {
                    coordTransComboAction(e, forwardCombo);
                }
            }
        });
        // Let's just keep all the options for the axial axis, and only remove the forward axis options
        /*this.forwardCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!isProgrammaticallyChanging) {
                    coordTransComboAction(e, axialCombo);
                }
            }
        });*/


        // Add action listener to the toggle button
        advancedToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // Toggle the visibility of the advanced options panel
                advancedOptionsPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);

                // Refresh the UI after changing visibility
                OBJOptionChooser.this.revalidate();
                OBJOptionChooser.this.repaint();

                // Adjust the size of the parent dialog
                Window window = GUIUtil.getWindowAncestor(OBJOptionChooser.this);
                if (window != null) {
                    window.pack();
                }
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
        this.sRGB.setSelected(opts.isUseSRGB());

        this.scalingModel.setValue(opts.getScaling());

        this.LOD.setSelectedItem(opts.getLOD());

        CoordTransform transformer = opts.getTransformer();
        this.axialCombo.setSelectedItem(transformer.getAxialAxis());
        this.forwardCombo.setSelectedItem(transformer.getForwardAxis());
    }

    /**
     * Store the options from this GUI in the given {@link OBJExportOptions} object.
     * @param opts The options to store the options in
     * @param alwaysStoreExportChildren if true, store the export children option even. If false, only store it if the
     *                                  checkbox was not disabled.
     */
    public void storeOptions(OBJExportOptions opts, boolean alwaysStoreExportChildren) {
        boolean onlyComponentAssemblies = isOnlyComponentAssembliesSelected(selectedComponents);
        // Don't save the state when the checkbox is set automatically due to component assemblies
        if (alwaysStoreExportChildren || !onlyComponentAssemblies) {
            opts.setExportChildren(exportChildren.isSelected());
        }
        opts.setExportAppearance(exportAppearance.isSelected());
        opts.setExportAsSeparateFiles(exportAsSeparateFiles.isSelected());
        opts.setRemoveOffset(removeOffset.isSelected());
        opts.setTriangulate(triangulate.isSelected());
        opts.setUseSRGB(sRGB.isSelected());
        opts.setScaling((float) scalingModel.getValue());
        opts.setLOD((ObjUtils.LevelOfDetail) LOD.getSelectedItem());

        CoordTransform transformer = CoordTransform.generateUsingLongitudinalAndForwardAxes(
                (Axis) axialCombo.getSelectedItem(), (Axis) forwardCombo.getSelectedItem(),
                rocket.getLength(), 0, 0);
        opts.setTransformer(transformer);
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

    private void coordTransComboAction(ItemEvent e, JComboBox<Axis> otherCombo) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        Axis selected = (Axis) e.getItem();
        Object otherAxis = otherCombo.getSelectedItem();
        if (!(otherAxis instanceof Axis)) {
            return;
        }

        // Set the flag to denote we're changing combo box items programmatically
        this.isProgrammaticallyChanging = true;

        // Change the combobox items to axes that don't conflict with the selected axis
        otherCombo.removeAllItems();
        for (Axis axis : Axis.values()) {
            if (!axis.isSameAxis(selected)) {
                otherCombo.addItem(axis);
            }
        }

        // Select the first item in the combobox
        if (!((Axis) otherAxis).isSameAxis(selected)) {
            otherCombo.setSelectedItem(otherAxis);
        } else {
            otherCombo.setSelectedIndex(0);
        }

        // Reset the flag after changes are done
        this.isProgrammaticallyChanging = false;
    }
}
