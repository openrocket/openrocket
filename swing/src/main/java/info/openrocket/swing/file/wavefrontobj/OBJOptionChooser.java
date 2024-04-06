package info.openrocket.swing.file.wavefrontobj;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.file.wavefrontobj.export.OBJExportOptions;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import info.openrocket.core.file.wavefrontobj.DefaultCoordTransform;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.file.wavefrontobj.ObjUtils;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OBJOptionChooser extends JPanel {
    private static final Translator trans = Application.getTranslator();
    private final JComponent parent;

    // Widgets
    private final JButton opt3DPrint;
    private final JButton optRend;
    private final JLabel componentsLabel;
    private final JCheckBox exportChildren;
    private final JCheckBox exportMotors;
    private final JCheckBox exportAppearance;
    private final JCheckBox exportAsSeparateFiles;
    private final JCheckBox removeOffset;
    private final JCheckBox triangulate;
    private final JLabel tmLabel;
    private final JComboBox<ObjUtils.TriangulationMethod> triangulationMethod;
    private final JCheckBox sRGB;
    private final JComboBox<ObjUtils.LevelOfDetail> LOD;
    private final DoubleModel scalingModel;
    //private final JComboBox<Axis> axialCombo;
    //private final JComboBox<Axis> forwardCombo;

    private final List<RocketComponent> selectedComponents;
    private final Rocket rocket;

    //private boolean isProgrammaticallyChanging = false;

    private int totallyNormalCounter = 0;

    private static Color darkWarningColor;

    static {
        initColors();
    }

    public OBJOptionChooser(JComponent parent, OBJExportOptions opts, List<RocketComponent> selectedComponents, Rocket rocket) {
        super(new MigLayout("hidemode 3"));

        this.parent = parent;

        this.selectedComponents = selectedComponents;
        this.rocket = rocket;

        // ------------ Component/optimization selection ------------
        // Component
        componentsLabel = new JLabel();
        updateComponentsLabel(selectedComponents);
        this.add(componentsLabel, "spanx, wrap unrel");

        // Optimize for:
        JLabel label = new JLabel(trans.get("OBJOptionChooser.lbl.optimizeFor"));
        this.add(label);

        //// 3D printing
        this.opt3DPrint = new JButton(trans.get("OBJOptionChooser.btn.opt3DPrint"));
        this.opt3DPrint.setToolTipText(trans.get("OBJOptionChooser.btn.opt3DPrint.ttip"));
        this.opt3DPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int temp = totallyNormalCounter;

                optimizeSettingsFor3DPrinting();

                // Highlight the button to show that it is selected
                highlightButton(opt3DPrint, optRend);

                // Shhhh...
                totallyNormalCounter = temp + 1;
                youMayIgnoreThisCode();
            }
        });

        this.add(opt3DPrint);

        //// Rendering
        this.optRend = new JButton(trans.get("OBJOptionChooser.btn.optRend"));
        this.optRend.setToolTipText(trans.get("OBJOptionChooser.btn.optRend.ttip"));
        this.optRend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optimizeSettingsForRendering();

                // Highlight the button to show that it is selected
                highlightButton(optRend, opt3DPrint);
            }
        });
        destroyTheMagic(optRend);
        this.add(optRend, "wrap");

        this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap para");


        // ------------ Basic options ------------
        //// Export children
        this.exportChildren = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportChildren"));
        this.exportChildren.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                List<RocketComponent> components;
                // Case: export children
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final Set<RocketComponent> allComponents = new HashSet<>();
                    for (RocketComponent component : selectedComponents) {
                        allComponents.add(component);
                        allComponents.addAll(component.getAllChildren());
                    }
                    components = new ArrayList<>(allComponents);
                }
                // Case: don't export children
                else {
                    components = selectedComponents;
                }
                updateComponentsLabel(components);
            }
        });
        destroyTheMagic(exportChildren);
        this.add(exportChildren, "spanx, wrap");

        //// Export motors
        this.exportMotors = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportMotors"));
        this.exportMotors.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportMotors.ttip"));
        destroyTheMagic(exportMotors);
        addOptimizationListener(exportMotors);
        this.add(exportMotors, "spanx, wrap");

        //// Remove origin offset
        this.removeOffset = new JCheckBox(trans.get("OBJOptionChooser.checkbox.removeOffset"));
        this.removeOffset.setToolTipText(trans.get("OBJOptionChooser.checkbox.removeOffset.ttip"));
        destroyTheMagic(removeOffset);
        addOptimizationListener(removeOffset);
        this.add(removeOffset, "spanx, wrap unrel");

        //// Export appearance
        this.exportAppearance = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAppearance"));
        this.exportAppearance.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAppearance.ttip"));
        destroyTheMagic(exportAppearance);
        addOptimizationListener(exportAppearance);
        this.add(exportAppearance, "spanx, wrap");

        //// Export as separate files
        this.exportAsSeparateFiles = new JCheckBox(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles"));
        this.exportAsSeparateFiles.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAsSeparateFiles.ttip"));
        destroyTheMagic(exportAsSeparateFiles);
        addOptimizationListener(exportAsSeparateFiles);
        this.add(exportAsSeparateFiles, "spanx, wrap unrel");

        this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap para");

        // ------------ Advanced options ------------
        // Show advanced options toggle
        JToggleButton advancedToggle = new JToggleButton(trans.get("OBJOptionChooser.btn.showAdvanced"));
        this.add(advancedToggle, "spanx, wrap para");

        // Panel for advanced options
        JPanel advancedOptionsPanel = new JPanel();
        advancedOptionsPanel.setLayout(new MigLayout("ins 0"));
        advancedOptionsPanel.setVisible(false);

        //// Scaling
        JLabel scalingLabel = new JLabel(trans.get("OBJOptionChooser.lbl.Scaling"));
        scalingLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.Scaling.ttip"));
        advancedOptionsPanel.add(scalingLabel, "spanx, split 2");
        this.scalingModel = new DoubleModel(opts, "ScalingDouble", UnitGroup.UNITS_SCALING, 0, 10000);
        JSpinner spin = new JSpinner(scalingModel.getSpinnerModel());
        spin.setToolTipText(trans.get("OBJOptionChooser.lbl.Scaling.ttip"));
        spin.setEditor(new SpinnerEditor(spin, 5));
        destroyTheMagic(scalingModel);
        addOptimizationListener(scalingModel);
        advancedOptionsPanel.add(spin, "wrap");

        //// Export colors in sRGB
        this.sRGB = new JCheckBox(trans.get("OBJOptionChooser.checkbox.sRGB"));
        this.sRGB.setToolTipText(trans.get("OBJOptionChooser.checkbox.sRGB.ttip"));
        destroyTheMagic(sRGB);
        advancedOptionsPanel.add(sRGB, "spanx, wrap");

        //// Triangulate
        this.triangulate = new JCheckBox(trans.get("OBJOptionChooser.checkbox.triangulate"));
        this.triangulate.setToolTipText(trans.get("OBJOptionChooser.checkbox.triangulate.ttip"));
        destroyTheMagic(triangulate);
        addOptimizationListener(triangulate);
        advancedOptionsPanel.add(triangulate, "spanx, wrap");
        this.triangulate.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tmLabel.setEnabled(true);
                    triangulationMethod.setEnabled(true);
                    // Disable the export appearance, it is not supported in combination with triangulate
                    exportAppearance.setEnabled(false);
                    exportAppearance.setSelected(false);
                    exportAppearance.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAppearance.ttip.triangulate"));
                } else {
                    tmLabel.setEnabled(false);
                    triangulationMethod.setEnabled(false);
                    // Re-enable
                    exportAppearance.setEnabled(true);
                    exportAppearance.setSelected(opts.isExportAppearance());
                    exportAppearance.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportAppearance.ttip"));
                }
            }
        });

        //// Triangulation method
        this.tmLabel = new JLabel(trans.get("OBJOptionChooser.lbl.triangulationMethod"));
        this.tmLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.triangulationMethod.ttip"));
        advancedOptionsPanel.add(this.tmLabel, "spanx, split 2");
        this.triangulationMethod = new JComboBox<>(ObjUtils.TriangulationMethod.values());
        this.triangulationMethod.setToolTipText(trans.get("OBJOptionChooser.lbl.triangulationMethod.ttip"));
        this.triangulationMethod.setRenderer(new TriangulationMethodRenderer());
        destroyTheMagic(triangulationMethod);
        addOptimizationListener(triangulationMethod);
        advancedOptionsPanel.add(triangulationMethod, "growx, wrap unrel");

        //// Level of detail
        JLabel LODLabel = new JLabel(trans.get("OBJOptionChooser.lbl.LevelOfDetail"));
        LODLabel.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        advancedOptionsPanel.add(LODLabel, "spanx, split 2");
        this.LOD = new JComboBox<>(ObjUtils.LevelOfDetail.values());
        this.LOD.setToolTipText(trans.get("OBJOptionChooser.lbl.LevelOfDetail.ttip"));
        destroyTheMagic(LOD);
        addOptimizationListener(LOD);
        advancedOptionsPanel.add(LOD, "growx, wrap unrel");


        // TODO: there were just too many bugs with the coordinate transform options, so I'm disabling them for now
        //   future Sibo, if you want to work on this, good luck...
        //// Coordinate transformer
        /*JLabel coordTransLabel = new JLabel(trans.get("OBJOptionChooser.lbl.CoordinateTransform"));
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
        });*/
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
        this.add(advancedOptionsPanel, "spanx");

        loadOptions(opts);
    }

    private static void initColors() {
        updateColors();
        UITheme.Theme.addUIThemeChangeListener(OBJOptionChooser::updateColors);
    }

    private static void updateColors() {
        darkWarningColor = GUIUtil.getUITheme().getDarkErrorColor();
    }

    /**
     * Highlight the given button and un-highlight the other button.
     * @param highlightButton The button to highlight
     * @param loserButton The button to un-highlight
     */
    private void highlightButton(JButton highlightButton, JButton loserButton) {
        highlightButton.setBorder(BorderFactory.createLineBorder(darkWarningColor));
        loserButton.setBorder(UIManager.getBorder("Button.border"));
    }

    private void updateComponentsLabel(List<RocketComponent> components) {
        final String labelText;
        final String tooltip;

        final boolean isSingleComponent = components.size() == 1;
        final String componentName = isSingleComponent ? "<b>" + components.get(0).getName() + "</b>":
                trans.get("OBJOptionChooser.lbl.multipleComponents");
        labelText = String.format(trans.get("OBJOptionChooser.lbl.component"), componentName);
        tooltip = createComponentsTooltip(components);
        componentsLabel.setText(labelText);
        componentsLabel.setToolTipText(tooltip);
    }

    private static String createComponentsTooltip(List<RocketComponent> selectedComponents) {
        if (selectedComponents.size() <= 1) {
            return "";
        }

        StringBuilder tooltipBuilder = new StringBuilder("<html>");
        int counter = 0;
        for (int i = 0; i < selectedComponents.size()-1; i++) {
            tooltipBuilder.append(selectedComponents.get(i).getName()).append(", ");

            // Add line break every 4 components
            if (counter == 4) {
                tooltipBuilder.append("<br>");
                counter = 0;
            } else {
                counter++;
            }
        }
        tooltipBuilder.append(selectedComponents.get(selectedComponents.size()-1).getComponentName());
        tooltipBuilder.append("</html>");
        return tooltipBuilder.toString();
    }

    public void loadOptions(OBJExportOptions opts) {
        boolean onlyComponentAssemblies = isOnlyComponentAssembliesSelected(selectedComponents);
        boolean hasChildren = isComponentsHaveChildren(selectedComponents);
        if (onlyComponentAssemblies || !hasChildren) {
            exportChildren.setSelected(true);
            exportChildren.setEnabled(false);
            if (onlyComponentAssemblies) {
                exportChildren.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportChildren.assemblies.ttip"));
            } else {
                exportChildren.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportChildren.noChildren.ttip"));
            }
        } else {
            exportChildren.setEnabled(true);
            exportChildren.setSelected(opts.isExportChildren());
            exportChildren.setToolTipText(trans.get("OBJOptionChooser.checkbox.exportChildren.ttip"));
        }

        this.exportMotors.setSelected(opts.isExportMotors());
        this.exportAppearance.setSelected(opts.isExportAppearance());
        this.exportAsSeparateFiles.setSelected(opts.isExportAsSeparateFiles());
        this.removeOffset.setSelected(opts.isRemoveOffset());
        this.triangulate.setSelected(opts.isTriangulate());
        // Re-apply if no triangulate, because the triangulation can mess with the export appearance setting
        if (!opts.isTriangulate()) {
            this.exportAppearance.setSelected(opts.isExportAppearance());
        }
        this.triangulationMethod.setSelectedItem(opts.getTriangulationMethod());
        this.sRGB.setSelected(opts.isUseSRGB());

        this.scalingModel.setValue(opts.getScaling());

        this.LOD.setSelectedItem(opts.getLOD());

        /*CoordTransform transformer = opts.getTransformer();
        this.axialCombo.setSelectedItem(transformer.getAxialAxis());
        this.forwardCombo.setSelectedItem(transformer.getForwardAxis());*/
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
        opts.setExportMotors(exportMotors.isSelected());
        opts.setExportAppearance(exportAppearance.isSelected());
        opts.setExportAsSeparateFiles(exportAsSeparateFiles.isSelected());
        opts.setRemoveOffset(removeOffset.isSelected());
        opts.setTriangulate(triangulate.isSelected());
        opts.setTriangulationMethod((ObjUtils.TriangulationMethod) triangulationMethod.getSelectedItem());
        opts.setUseSRGB(sRGB.isSelected());
        opts.setScaling((float) scalingModel.getValue());
        opts.setLOD((ObjUtils.LevelOfDetail) LOD.getSelectedItem());

        /*CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(
                (Axis) axialCombo.getSelectedItem(), (Axis) forwardCombo.getSelectedItem(),
                rocket.getLength(), 0, 0);*/
        opts.setTransformer(new DefaultCoordTransform(rocket.getLength()));
    }

    private void optimizeSettingsFor3DPrinting() {
        OBJExportOptions options = new OBJExportOptions(rocket);
        storeOptions(options, true);

        options.setExportMotors(false);
        options.setExportAppearance(false);
        options.setRemoveOffset(true);
        options.setScaling(1000);
        options.setTriangulate(true);
        options.setTriangulationMethod(ObjUtils.TriangulationMethod.DELAUNAY);
        options.setLOD(ObjUtils.LevelOfDetail.HIGH_QUALITY);

        loadOptions(options);
    }

    /**
     * Check whether the current settings are optimized for 3D printing.
     * @param options The options to check
     * @return True if the settings are optimized for 3D printing, false otherwise
     */
    private boolean isOptimizedFor3DPrinting(OBJExportOptions options) {
        return !options.isExportMotors() && !options.isExportAppearance() && options.isTriangulate() &&
                options.getTriangulationMethod() == ObjUtils.TriangulationMethod.DELAUNAY &&
                options.getLOD() == ObjUtils.LevelOfDetail.HIGH_QUALITY && options.isRemoveOffset() && options.getScaling() == 1000;
    }

    private void optimizeSettingsForRendering() {
        OBJExportOptions options = new OBJExportOptions(rocket);
        storeOptions(options, true);

        options.setExportMotors(true);
        options.setExportAppearance(true);
        options.setScaling(20);     // Idk, pretty arbitrary
        options.setTriangulate(false);
        options.setLOD(ObjUtils.LevelOfDetail.NORMAL_QUALITY);

        loadOptions(options);
    }

    /**
     * Check whether the current settings are optimized for rendering.
     * @param options The options to check
     * @return True if the settings are optimized for rendering, false otherwise
     */
    private boolean isOptimizedForRendering(OBJExportOptions options) {
        return options.isExportMotors() && options.isExportAppearance() && !options.isTriangulate() &&
                options.getLOD() == ObjUtils.LevelOfDetail.NORMAL_QUALITY && options.getScaling() == 20;
    }

    private static boolean isOnlyComponentAssembliesSelected(List<RocketComponent> selectedComponents) {
        for (RocketComponent component : selectedComponents) {
            if (!(component instanceof ComponentAssembly)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isComponentsHaveChildren(List<RocketComponent> selectedComponents) {
        for (RocketComponent component : selectedComponents) {
            if (component.getChildCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void addOptimizationListener(AbstractButton component) {
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOptimizationButtons();
            }
        });
    }

    private void addOptimizationListener(DoubleModel model) {
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateOptimizationButtons();
            }
        });
    }

    private void addOptimizationListener(JComboBox comboBox) {
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateOptimizationButtons();
            }
        });
    }

    private void updateOptimizationButtons() {
        OBJExportOptions options = new OBJExportOptions(rocket);
        storeOptions(options, true);
        if (isOptimizedFor3DPrinting(options)) {
            highlightButton(opt3DPrint, optRend);
        } else if (isOptimizedForRendering(options)) {
            highlightButton(optRend, opt3DPrint);
        } else {
            opt3DPrint.setBorder(UIManager.getBorder("Button.border"));
            optRend.setBorder(UIManager.getBorder("Button.border"));
        }
    }

    private void destroyTheMagic(AbstractButton component) {
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totallyNormalCounter = 0;
            }
        });
    }

    private void destroyTheMagic(DoubleModel model) {
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                totallyNormalCounter = 0;
            }
        });
    }

    private void destroyTheMagic(JComboBox comboBox) {
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                totallyNormalCounter = 0;
            }
        });
    }

    private void youMayIgnoreThisCode() {
        if (totallyNormalCounter == 4) {
            JOptionPane.showMessageDialog(parent, trans.get("OBJOptionChooser.easterEgg.msg"),
                    trans.get("OBJOptionChooser.easterEgg.title"), JOptionPane.INFORMATION_MESSAGE);
        } else if (totallyNormalCounter == 15) {
            JOptionPane.showMessageDialog(parent, trans.get("OBJOptionChooser.easterEgg.msg2"),
                    trans.get("OBJOptionChooser.easterEgg.title"), JOptionPane.INFORMATION_MESSAGE);
        } else if (totallyNormalCounter == 25) {
            JOptionPane.showMessageDialog(parent, trans.get("OBJOptionChooser.easterEgg.msg3"),
                    trans.get("OBJOptionChooser.easterEgg.title"), JOptionPane.INFORMATION_MESSAGE);
        } else if (totallyNormalCounter == 40) {
            JOptionPane.showMessageDialog(parent, trans.get("OBJOptionChooser.easterEgg.msg4"),
                    trans.get("OBJOptionChooser.easterEgg.title"), JOptionPane.INFORMATION_MESSAGE);
            totallyNormalCounter = 0;
        }
    }

    private static class TriangulationMethodRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (index > -1 && value instanceof ObjUtils.TriangulationMethod) {
                list.setToolTipText(((ObjUtils.TriangulationMethod) value).getTooltip());
            }
            return comp;
        }
    }

    /*private void coordTransComboAction(ItemEvent e, JComboBox<Axis> otherCombo) {
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
    }*/
}
