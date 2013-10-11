package net.sf.openrocket.utils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBException;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.preset.ButtonColumn;
import net.sf.openrocket.gui.preset.PresetEditorDialog;
import net.sf.openrocket.gui.preset.PresetResultListener;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.loader.MaterialHolder;
import net.sf.openrocket.preset.xml.OpenRocketComponentDTO;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UI for editing component presets.  Currently this is a standalone application - run the main within this class.
 * TODO: Full I18n TODO: Save As .csv
 */
public class ComponentPresetEditor extends JPanel implements PresetResultListener {
	
	/**
	 * The logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(ComponentPresetEditor.class);
	
	/**
	 * The table of presets.
	 */
	private JTable table;
	
	/**
	 * The table's data model.
	 */
	private DataTableModel model;
	
	private final OpenedFileContext editContext = new OpenedFileContext();
	
	/**
	 * Create the panel.
	 *
	 * @param frame the parent window
	 */
	public ComponentPresetEditor(final JFrame frame) {
		setLayout(new MigLayout("", "[82.00px, grow][168.00px, grow][84px, grow][117.00px, grow][][222px]",
				"[346.00px, grow][29px]"));
		
		model = new DataTableModel(new String[] { "Manufacturer", "Type", "Part No", "Description", "" });
		
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
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				int selectedColumn = table.getColumnModel().getColumnIndexAtX(target.getSelectedColumn());
				final int targetSelectedRow = target.getSelectedRow();
				if (targetSelectedRow > -1 && targetSelectedRow < model.getRowCount()) {
					int selectedRow = table.getRowSorter().convertRowIndexToModel(targetSelectedRow);
					if (selectedColumn == 4) {
						if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ComponentPresetEditor.this,
								"Do you want to delete this preset?",
								"Confirm Delete", JOptionPane.YES_OPTION,
								JOptionPane.QUESTION_MESSAGE)) {
							model.removeRow(selectedRow);
						}
					}
					else {
						if (e.getClickCount() == 2) {
							editContext.setEditingSelected(true);
							new PresetEditorDialog(ComponentPresetEditor.this,
									(ComponentPreset) model.getAssociatedObject(selectedRow), editContext.getMaterialsLoaded()).setVisible(true);
						}
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
					Object[] options = { "Save",
							"Merge",
							"Discard",
							"Cancel" };
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
		
		JMenuItem mntmExit = new JMenuItem("Close");
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Window w = SwingUtilities.getWindowAncestor(ComponentPresetEditor.this);
				w.dispose();
			}
		});
		
		
		JButton addBtn = new JButton("Add");
		addBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				editContext.setEditingSelected(false);
				new PresetEditorDialog(ComponentPresetEditor.this).setVisible(true);
			}
		});
		add(addBtn, "cell 0 1,alignx left,aligny top");
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
			DataTableModel myModel = (DataTableModel) table.getModel();
			//Is this a new preset?
			String description = preset.has(ComponentPreset.DESCRIPTION) ? preset.get(ComponentPreset.DESCRIPTION) :
					preset.getPartNo();
			if (!editContext.isEditingSelected() || table.getSelectedRow() == -1) {
				myModel.addRow(new Object[] { preset.getManufacturer().getDisplayName(), preset.getType().name(),
						preset.getPartNo(), description, Icons.EDIT_DELETE }, preset);
			}
			else {
				//This is a modified preset; update all of the columns and the stored associated instance.
				int row = table.getSelectedRow();
				myModel.setValueAt(preset.getManufacturer().getDisplayName(), row, 0);
				myModel.setValueAt(preset.getType().name(), row, 1);
				myModel.setValueAt(preset.getPartNo(), row, 2);
				myModel.setValueAt(description, row, 3);
				myModel.associated.set(row, preset);
			}
		}
		editContext.setEditingSelected(false);
	}
	
	/**
	 * Launch the test main.
	 */
	public static void main(String[] args) {
		BasicApplication app = new BasicApplication();
		app.initializeApplication();
		try {
			// Application.setPreferences(new SwingPreferences());
			JFrame dialog = new JFrame();
			dialog.getContentPane().add(new ComponentPresetEditor(dialog));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setVisible(true);
		} catch (Exception e) {
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
		
		@Override
		public void removeRow(int row) {
			super.removeRow(row);
			associated.remove(row);
		}
		
		public Object getAssociatedObject(int row) {
			return associated.get(row);
		}
		
		@Override
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
		chooser.addChoosableFileFilter(FileHelper.CSV_FILE_FILTER);
		chooser.setFileFilter(FileHelper.OPEN_ROCKET_COMPONENT_FILTER);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (editContext.getLastDirectory() != null) {
			chooser.setCurrentDirectory(editContext.getLastDirectory());
		}
		else {
			chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultUserComponentDirectory());
		}
		
		int option = chooser.showOpenDialog(ComponentPresetEditor.this);
		if (option != JFileChooser.APPROVE_OPTION) {
			editContext.setOpenedFile(null);
			log.info(Markers.USER_MARKER, "User decided not to open, option=" + option);
			return false;
		}
		
		File file = chooser.getSelectedFile();
		try {
			if (file == null) {
				log.info(Markers.USER_MARKER, "User did not select a file");
				return false;
			}
			
			editContext.setLastDirectory(file.getParentFile());
			editContext.setMaterialsLoaded(null);
			List<ComponentPreset> presets = null;
			
			if (file.getName().toLowerCase().endsWith(".orc")) {
				OpenRocketComponentDTO fileContents = new OpenRocketComponentSaver().unmarshalFromOpenRocketComponent(new FileReader(file));
				editContext.setMaterialsLoaded(new MaterialHolder(fileContents.asMaterialList()));
				presets = fileContents.asComponentPresets();
			}
			else {
				if (file.getName().toLowerCase().endsWith(".csv")) {
					file = file.getParentFile();
				}
				presets = new ArrayList<ComponentPreset>();
				MaterialHolder materialHolder = RocksimComponentFileTranslator.loadAll(presets, file);
				editContext.setMaterialsLoaded(materialHolder);
			}
			if (presets != null) {
				for (ComponentPreset next : presets) {
					notifyResult(next);
				}
				editContext.setOpenedFile(file);
			}
		} catch (Exception e) {
			String fileName = (file == null) ? "(file is null, can't get name)" : file.getName();
			JOptionPane.showMessageDialog(ComponentPresetEditor.this, "Unable to open OpenRocket component file: " +
					fileName + " Invalid format. " + e.getMessage());
			editContext.setOpenedFile(null);
			editContext.setEditingSelected(false);
			return false;
		}
		return true;
	}
	
	private boolean saveAndHandleError() {
		try {
			return saveAsORC();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ComponentPresetEditor.this, e1.getLocalizedMessage(),
					"Error saving ORC file.", JOptionPane.ERROR_MESSAGE);
			return false;
		}
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
		if (editContext.getOpenedFile() != null) {
			chooser.setSelectedFile(editContext.getOpenedFile());
		}
		else {
			chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultUserComponentDirectory());
		}
		
		int option = chooser.showSaveDialog(ComponentPresetEditor.this);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "User decided not to save, option=" + option);
			return false;
		}
		
		file = chooser.getSelectedFile();
		if (file == null) {
			log.info(Markers.USER_MARKER, "User did not select a file");
			return false;
		}
		
		file = FileHelper.forceExtension(file, "orc");
		
		MaterialHolder materials = new MaterialHolder();
		List<ComponentPreset> presets = new ArrayList<ComponentPreset>();
		
		for (int x = 0; x < model.getRowCount(); x++) {
			ComponentPreset preset = (ComponentPreset) model.getAssociatedObject(x);
			// If we don't have a material already defined for saving...
			if (materials.getMaterial(preset.get(ComponentPreset.MATERIAL)) == null) {
				// Check if we loaded a material with this name.
				Material m = null;
				if (editContext.getMaterialsLoaded() != null) {
					m = editContext.getMaterialsLoaded().getMaterial(preset.get
							(ComponentPreset.MATERIAL));
				}
				// If there was no material loaded with that name, use the component's material.
				if (m == null) {
					m = preset.get(ComponentPreset.MATERIAL);
				}
				materials.put(m);
			}
			presets.add(preset);
		}
		
		return FileHelper.confirmWrite(file, this) && new OpenRocketComponentSaver().save(file, new ArrayList<Material>(materials.values()), presets);
	}
	
	class OpenedFileContext {
		
		/**
		 * State variable to keep track of which file was opened, in case it needs to be saved back to that file.
		 */
		private File openedFile = null;
		
		/**
		 * Last directory; file chooser is set here so user doesn't have to keep navigating to a common area.
		 */
		private File lastDirectory = null;
		
		private boolean editingSelected = false;
		
		private MaterialHolder materialsLoaded = null;
		
		OpenedFileContext() {
		}
		
		public File getOpenedFile() {
			return openedFile;
		}
		
		public void setOpenedFile(final File theOpenedFile) {
			openedFile = theOpenedFile;
		}
		
		public File getLastDirectory() {
			return lastDirectory;
		}
		
		public void setLastDirectory(final File theLastDirectory) {
			lastDirectory = theLastDirectory;
		}
		
		public boolean isEditingSelected() {
			return editingSelected;
		}
		
		public void setEditingSelected(final boolean theEditingSelected) {
			editingSelected = theEditingSelected;
		}
		
		public MaterialHolder getMaterialsLoaded() {
			return materialsLoaded;
		}
		
		public void setMaterialsLoaded(final MaterialHolder theMaterialsLoaded) {
			materialsLoaded = theMaterialsLoaded;
		}
	}
}
