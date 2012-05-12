package net.sf.openrocket.gui.preset;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.xml.OpenRocketComponentLoader;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A UI for editing component presets.  Currently this is a standalone application - run the main within this class.
 * TODO: Full I18n
 * TODO: Open .csv
 * TODO: Save As .csv
 */
public class ComponentPresetPanel extends JPanel implements PresetResultListener {

    /**
     * The logger.
     */
    private static final LogHelper log = Application.getLogger();

    /**
     * The I18N translator.
     */
    private static ResourceBundleTranslator trans = null;

    /**
     * State variable to keep track of which file was opened, in case it needs to be saved back to that file.
     */
    private File openedFile = null;

    /**
     * Last directory; file chooser is set here so user doesn't have to keep navigating to a common area.
     */
    private File lastDirectory = null;

    /**
     * The table of presets.
     */
    private JTable table;

    /**
     * The table's data model.
     */
    private DataTableModel model;

    /**
     * Flag that indicates if an existing Preset is currently being edited.
     */
    private boolean editingSelected = false;

    static {
        trans = new ResourceBundleTranslator("l10n.messages");
        net.sf.openrocket.startup.Application.setBaseTranslator(trans);
    }

    /**
     * Create the panel.
     *
     * @param frame the parent window
     */
    public ComponentPresetPanel(final JFrame frame) {
        setLayout(new MigLayout("", "[82.00px, grow][168.00px, grow][84px, grow][117.00px, grow][][222px]",
                "[346.00px, grow][29px]"));

        model = new DataTableModel(new String[]{"Manufacturer", "Type", "Part No", "Description", ""});

        table = new JTable(model);
        table.getTableHeader().setFont(new JLabel().getFont());
        //The action never gets called because the table MouseAdapter intercepts it first.  Still need an empty
        // instance though.
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        };
        //Create a editor/renderer for the delete operation.  Instantiation self-registers into the table.
        new ButtonColumn(table, action, 4);
        table.getColumnModel().getColumn(4).setMaxWidth(Icons.EDIT_DELETE.getIconWidth());
        table.getColumnModel().getColumn(4).setMinWidth(Icons.EDIT_DELETE.getIconWidth());

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        add(scrollPane, "cell 0 0 6 1,grow");

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                if (target.getSelectedColumn() == 4) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ComponentPresetPanel.this,
                            "Do you want to delete this preset?",
                            "Confirm Delete", JOptionPane.YES_OPTION,
                            JOptionPane.QUESTION_MESSAGE)) {
                        model.removeRow(target.getSelectedRow());
                    }
                }
                else {
                    if (e.getClickCount() == 2) {
                        int row = target.getSelectedRow();
                        editingSelected = true;
                        new PresetEditorDialog(ComponentPresetPanel.this, (ComponentPreset) model.getAssociatedObject(row)).setVisible(true);
                    }
                }
            }
        });


        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmOpen = new JMenuItem("Open...");
        mnFile.add(mntmOpen);
        mntmOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (model.getRowCount() > 0) {
                    /*
                     *  If the table model already contains presets, ask the user if they a) want to discard those
                     *  presets, b) save them before reading in another component file, or c) merge the read component file
                     *  with the current contents of the table model.
                     */
                    Object[] options = {"Save",
                            "Merge",
                            "Discard",
                            "Cancel"};
                    int n = JOptionPane.showOptionDialog(frame,
                            "The editor contains existing component presets.  What would you like to do with them?",
                            "Existing Component Presets",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (n == 0) { //Save.  Then remove existing rows and open.
                        if (saveAndHandleError()) {
                            model.removeAllRows();
                        }
                        else { //Save failed; bail out.
                            return;
                        }
                    }
                    else if (n == 2) { //Discard and open
                        model.removeAllRows();
                    }
                    else if (n == 3) { //Cancel.  Bail out.
                        return;
                    }
                }
                //Open file dialog
                openComponentFile();
            }
        });

        JSeparator separator = new JSeparator();
        mnFile.add(separator);

        JMenuItem mntmSave = new JMenuItem("Save As...");
        mnFile.add(mntmSave);
        mntmSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveAndHandleError();
            }
        });

        JSeparator separator_1 = new JSeparator();
        mnFile.add(separator_1);

        JMenuItem mntmExit = new JMenuItem("Exit");
        mnFile.add(mntmExit);
        mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
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
    }

    private boolean saveAndHandleError() {
        try {
            return saveAsORC();
        }
        catch (Exception e1) {
            JOptionPane.showMessageDialog(ComponentPresetPanel.this, e1.getLocalizedMessage(),
                    "Error saving ORC file.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Callback method from the PresetEditorDialog to notify this class when a preset has been saved.  The 'save' is
     * really just a call back here so the preset can be added to the master table.  It's not to be confused with the
     * save to disk.
     *
     * @param preset the new or modified preset
     */
    @Override
    public void notifyResult(final ComponentPreset preset) {
        if (preset != null) {
            DataTableModel model = (DataTableModel) table.getModel();
            //Is this a new preset?
            if (!editingSelected) {
                model.addRow(new Object[]{preset.getManufacturer().getDisplayName(), preset.getType().name(),
                        preset.getPartNo(), preset.get(ComponentPreset.DESCRIPTION), Icons.EDIT_DELETE}, preset);
            }
            else {
                //This is a modified preset; update all of the columns and the stored associated instance.
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
            dialog.getContentPane().add(new ComponentPresetPanel(dialog));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A table model that adds associated objects to each row, allowing for easy retrieval.
     */
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

        public void removeAllRows() {
            for (int x = getRowCount(); x > 0; x--) {
                super.removeRow(x - 1);
            }
            associated.clear();
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

    /**
     * Open the component file.  Present a chooser for the user to navigate to the file.
     *
     * @return true if the file was successfully opened; Note: side effect, is that the ComponentPresets read from the
     *         file are written to the table model.
     */
    private boolean openComponentFile() {
        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);

        chooser.setFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }
        else {
            chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        }

        int option = chooser.showOpenDialog(ComponentPresetPanel.this);
        if (option != JFileChooser.APPROVE_OPTION) {
            openedFile = null;
            log.user("User decided not to open, option=" + option);
            return false;
        }

        File file = chooser.getSelectedFile();
        try {
            if (file == null) {
                log.user("User did not select a file");
                return false;
            }

            lastDirectory = file.getParentFile();

            Collection<ComponentPreset> presets = new OpenRocketComponentLoader().load(new FileInputStream(file),
                    file.getName());
            if (presets != null) {
                for (ComponentPreset next : presets) {
                    notifyResult(next);
                }
                openedFile = file;
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(ComponentPresetPanel.this, "Unable to open OpenRocket component file: " +
                    file.getName() + " Invalid format. " + e.getMessage());
            openedFile = null;
            return false;
        }
        return true;
    }

    /**
     * Save the contents of the table model as XML in .orc format.
     *
     * @return true if the file was written
     *
     * @throws JAXBException thrown if the data could not be marshaled
     * @throws IOException   thrown if there was a problem with writing the file
     */
    private boolean saveAsORC() throws JAXBException, IOException {
        File file = null;

        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);

        chooser.setFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);
        if (openedFile != null) {
            chooser.setSelectedFile(openedFile);
        }
        else {
            chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        }

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

        for (int x = 0; x < model.getRowCount(); x++) {
            ComponentPreset preset = (ComponentPreset) model.getAssociatedObject(x);
            if (!materials.contains(preset.get(ComponentPreset.MATERIAL))) {
                materials.add(preset.get(ComponentPreset.MATERIAL));
            }
            presets.add(preset);
        }

        return FileHelper.confirmWrite(file, this) && new OpenRocketComponentSaver().save(file, materials, presets);
    }
}
