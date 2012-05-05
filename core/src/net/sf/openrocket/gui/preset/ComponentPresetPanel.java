package net.sf.openrocket.gui.preset;


import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A UI for editing component presets.  Currently this is a standalone application - run the main within this class.
 * TODO: Full I18n
 * TODO: Delete component
 * TODO: Open .orc for editing
 * TODO: Import .csv
 * TODO: Export .csv
 * TODO: proper mass unit conversion
 */
public class ComponentPresetPanel extends JPanel implements PresetResultListener {

    private static final LogHelper log = Application.getLogger();

    private static ResourceBundleTranslator trans = null;

    static {
        trans = new ResourceBundleTranslator("l10n.messages");
        net.sf.openrocket.startup.Application.setBaseTranslator(trans);
    }

    private JTable table;
    private DataTableModel model;
    private boolean editingSelected = false;

    /**
     * Create the panel.
     */
    public ComponentPresetPanel() {
        setLayout(new MigLayout("", "[82.00px][168.00px][84px][117.00px][][222px]", "[346.00px][29px]"));

        model = new DataTableModel(new String[]{"Manufacturer", "Type", "Part No", "Description"});

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        add(scrollPane, "cell 0 0 6 1,grow");

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    editingSelected = true;
                    new PresetEditorDialog(ComponentPresetPanel.this, (ComponentPreset) model.getAssociatedObject(row)).setVisible(true);
                }
            }
        });
        JButton addBtn = new JButton("Add");
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                editingSelected = false;
                new PresetEditorDialog(ComponentPresetPanel.this).setVisible(true);
            }
        });
        add(addBtn, "cell 0 1,alignx left,aligny top");

        JButton saveBtn = new JButton("Save...");
        saveBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        add(saveBtn, "flowx,cell 5 1,alignx right,aligny center");
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    saveAsORC();
                }
                catch (JAXBException e1) {
                    //TODO
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    //TODO
                    e1.printStackTrace();
                }
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        add(cancelBtn, "cell 5 1,alignx right,aligny top");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });

    }

    @Override
    public void notifyResult(final ComponentPreset preset) {
        if (preset != null) {
            DataTableModel model = (DataTableModel) table.getModel();
            if (!editingSelected) {
                model.addRow(new String[]{preset.getManufacturer().getDisplayName(), preset.getType().name(), preset.getPartNo(), preset.get(ComponentPreset.DESCRIPTION)},
                        preset);
            }
            else {
                int row = table.getSelectedRow();
                model.setValueAt(preset.getManufacturer().getDisplayName(), row, 0);
                model.setValueAt(preset.getType().name(), row, 1);
                model.setValueAt(preset.getPartNo(), row, 2);
                model.setValueAt(preset.get(ComponentPreset.DESCRIPTION), row, 3);
                model.associated.set(row, preset);
            }
        }
        editingSelected = false;
    }

    /**
     * Launch the test main.
     */
    public static void main(String[] args) {
        try {
            Application.setPreferences(new SwingPreferences());
            JFrame dialog = new JFrame();
            dialog.getContentPane().add(new ComponentPresetPanel());
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DataTableModel extends DefaultTableModel {

        private List<Object> associated = new ArrayList<Object>();

        /**
         * Constructs a <code>DefaultTableModel</code> with as many columns as there are elements in
         * <code>columnNames</code> and <code>rowCount</code> of <code>null</code> object values.  Each column's name
         * will be taken from the <code>columnNames</code> array.
         *
         * @param columnNames <code>array</code> containing the names of the new columns; if this is <code>null</code>
         *                    then the model has no columns
         *
         * @see #setDataVector
         * @see #setValueAt
         */
        DataTableModel(final Object[] columnNames) {
            super(columnNames, 0);
        }

        public void addRow(Object[] data, Object associatedData) {
            super.addRow(data);
            associated.add(getRowCount() - 1, associatedData);
        }

        public void removeRow(int row) {
            super.removeRow(row);
            associated.remove(row);
        }

        public Object getAssociatedObject(int row) {
            return associated.get(row);
        }

        public boolean isCellEditable(int rowIndex, int mColIndex) {
            return false;
        }
    }

    private boolean saveAsORC() throws JAXBException, IOException {
        File file = null;

        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);

        chooser.setFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

        int option = chooser.showSaveDialog(ComponentPresetPanel.this);
        if (option != JFileChooser.APPROVE_OPTION) {
            log.user("User decided not to save, option=" + option);
            return false;
        }

        file = chooser.getSelectedFile();
        if (file == null) {
            log.user("User did not select a file");
            return false;
        }

        ((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

        file = FileHelper.forceExtension(file, "orc");

        List<Material> materials = new ArrayList<Material>();
        List<ComponentPreset> presets = new ArrayList<ComponentPreset>();

        for (int x = 0; x< model.getRowCount(); x++) {
            ComponentPreset preset = (ComponentPreset)model.getAssociatedObject(x);
            if (!materials.contains(preset.get(ComponentPreset.MATERIAL))) {
                materials.add(preset.get(ComponentPreset.MATERIAL));
            }
            presets.add(preset);
        }

        return FileHelper.confirmWrite(file, this) && new OpenRocketComponentSaver().save(file, materials, presets);
    }
}
