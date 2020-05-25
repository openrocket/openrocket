package net.sf.openrocket.gui.main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.document.StorageOptions.FileType;
import net.sf.openrocket.file.GeneralRocketSaver;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.customexpression.CustomExpressionDialog;
import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.BugReportDialog;
import net.sf.openrocket.gui.dialogs.ComponentAnalysisDialog;
import net.sf.openrocket.gui.dialogs.DebugLogDialog;
import net.sf.openrocket.gui.dialogs.DetailDialog;
import net.sf.openrocket.gui.dialogs.LicenseDialog;
import net.sf.openrocket.gui.dialogs.PrintDialog;
import net.sf.openrocket.gui.dialogs.ScaleDialog;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.gui.dialogs.WarningDialog;
import net.sf.openrocket.gui.dialogs.optimization.GeneralOptimizationDialog;
import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import net.sf.openrocket.gui.figure3d.photo.PhotoFrame;
import net.sf.openrocket.gui.help.tours.GuidedTourSelectionDialog;
import net.sf.openrocket.gui.main.componenttree.ComponentTree;
import net.sf.openrocket.gui.main.flightconfigpanel.FlightConfigurationPanel;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.OpenFileWorker;
import net.sf.openrocket.gui.util.SaveFileWorker;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MemoryManagement;
import net.sf.openrocket.util.MemoryManagement.MemoryData;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.utils.ComponentPresetEditor;


public class BasicFrame extends JFrame {
	private static final long serialVersionUID = 948877655223365313L;

	private static final Logger log = LoggerFactory.getLogger(BasicFrame.class);

	private static final GeneralRocketSaver ROCKET_SAVER = new GeneralRocketSaver();

	private static final Translator trans = Application.getTranslator();
	private static final Preferences prefs = Application.getPreferences();

	private static final int SHORTCUT_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	public static final int COMPONENT_TAB = 0;
	public static final int CONFIGURATION_TAB = 1;
	public static final int SIMULATION_TAB = 2;


	/**
	 * List of currently open frames.  When the list goes empty
	 * it is time to exit the application.
	 */
	private static final ArrayList<BasicFrame> frames = new ArrayList<BasicFrame>();


	/**
	 * Whether "New" and "Open" should replace this frame.
	 * Should be set to false on the first rocket modification.
	 */
	private boolean replaceable = false;

	private final OpenRocketDocument document;
	private final Rocket rocket;

	private JTabbedPane tabbedPane;
	private RocketPanel rocketpanel;
	private ComponentTree tree = null;

	private final DocumentSelectionModel selectionModel;
	private final TreeSelectionModel componentSelectionModel;
	private final ListSelectionModel simulationSelectionModel;

	/** Actions available for rocket modifications */
	private final RocketActions actions;

	private SimulationPanel simulationPanel;


	/**
	 * Sole constructor.  Creates a new frame based on the supplied document
	 * and adds it to the current frames list.
	 *
	 * @param document	the document to show.
	 */
	public BasicFrame(OpenRocketDocument document) {
		log.debug("Instantiating new BasicFrame");

		this.document = document;
		this.rocket = document.getRocket();
		this.rocket.getSelectedConfiguration().setAllStages();

		// Create the component tree selection model that will be used
		componentSelectionModel = new DefaultTreeSelectionModel();
		componentSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Obtain the simulation selection model that will be used
		simulationPanel = new SimulationPanel(document);
		simulationSelectionModel = simulationPanel.getSimulationListSelectionModel();

		// Combine into a DocumentSelectionModel
		selectionModel = new DocumentSelectionModel(document);
		selectionModel.attachComponentTreeSelectionModel(componentSelectionModel);
		selectionModel.attachSimulationListSelectionModel(simulationSelectionModel);


		actions = new RocketActions(document, selectionModel, this);


		log.debug("Constructing the BasicFrame UI");

		// The main vertical split pane
		JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		vertical.setResizeWeight(0.5);
		this.add(vertical);


		// The top tabbed pane
		tabbedPane = new JTabbedPane();
		//// Rocket design
		tabbedPane.addTab(trans.get("BasicFrame.tab.Rocketdesign"), null, designTab());
		//// Flight configurations
		tabbedPane.addTab(trans.get("BasicFrame.tab.Flightconfig"), null, new FlightConfigurationPanel(document));
		//// Flight simulations
		tabbedPane.addTab(trans.get("BasicFrame.tab.Flightsim"), null, simulationPanel);

		// Add change listener to catch when the tabs are changed.  This is to run simulations 
		// automagically when the simulation tab is selected.
		tabbedPane.addChangeListener(new BasicFrame_changeAdapter(this));


		vertical.setTopComponent(tabbedPane);



		//  Bottom segment, rocket figure

		rocketpanel = new RocketPanel(document);
		vertical.setBottomComponent(rocketpanel);

		rocketpanel.setSelectionModel(tree.getSelectionModel());


		createMenu();


		rocket.addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				setTitle();
			}
		});

		setTitle();
		this.pack();


		// Set initial window size
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width = size.width * 9 / 10;
		size.height = size.height * 9 / 10;
		this.setSize(size);

		// Remember changed size
		GUIUtil.rememberWindowSize(this);

		this.setLocationByPlatform(true);

		GUIUtil.setWindowIcons(this);

		this.validate();
		vertical.setDividerLocation(0.4);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeAction();
			}
		});

		frames.add(this);
		log.debug("BasicFrame instantiation complete");
	}

	/**
	 * Construct the "Rocket design" tab.  This contains a horizontal split pane
	 * with the left component the design tree and the right component buttons
	 * for adding components.
	 */
	private JComponent designTab() {
		JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		horizontal.setResizeWeight(0.5);


		//  Upper-left segment, component tree

		JPanel panel = new JPanel(new MigLayout("fill, flowy", "[grow][grow 0]","[grow]"));

		tree = new ComponentTree(document);
		tree.setSelectionModel(componentSelectionModel);

		// Remove JTree key events that interfere with menu accelerators
		InputMap im = SwingUtilities.getUIInputMap(tree, JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_KEY), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_KEY), null);



		// Double-click opens config dialog
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if ((e.getClickCount() == 2) && !ComponentConfigDialog.isDialogVisible()) {
						// Double-click
						RocketComponent c = (RocketComponent) selPath.getLastPathComponent();
						ComponentConfigDialog.showDialog(BasicFrame.this,
								BasicFrame.this.document, c);
					}
				}
			}
		};
		tree.addMouseListener(ml);

		// Update dialog when selection is changed
		componentSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// Scroll tree to the selected item
				TreePath path = componentSelectionModel.getSelectionPath();
				if (path == null)
					return;
				tree.scrollPathToVisible(path);

				if (!ComponentConfigDialog.isDialogVisible())
					return;
				RocketComponent c = (RocketComponent) path.getLastPathComponent();
				ComponentConfigDialog.showDialog(BasicFrame.this,
						BasicFrame.this.document, c);
			}
		});

		// Place tree inside scroll pane
		JScrollPane scroll = new JScrollPane(tree);
		panel.add(scroll, "spany, grow, wrap");


		// Buttons
		JButton button = new JButton(actions.getMoveUpAction());
		panel.add(button, "sizegroup buttons, aligny 65%");

		button = new JButton(actions.getMoveDownAction());
		panel.add(button, "sizegroup buttons, aligny 0%");

		button = new JButton(actions.getEditAction());
		panel.add(button, "sizegroup buttons");

		button = new JButton(actions.getDeleteAction());
		button.setIcon(null);
		button.setMnemonic(0);
		panel.add(button, "sizegroup buttons");

		horizontal.setLeftComponent(panel);


		//  Upper-right segment, component addition buttons

		panel = new JPanel(new MigLayout("fill, insets 0", "[0::]"));

		scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setViewportView(new ComponentAddButtons(document, componentSelectionModel,
				scroll.getViewport()));
		scroll.setBorder(null);
		scroll.setViewportBorder(null);

		TitledBorder border = BorderFactory.createTitledBorder(trans.get("BasicFrame.title.Addnewcomp"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		scroll.setBorder(border);

		panel.add(scroll, "grow");

		horizontal.setRightComponent(panel);

		return horizontal;
	}



	/**
	 * Return the currently selected rocket component, or <code>null</code> if none selected.
	 */
	private RocketComponent getSelectedComponent() {
		TreePath path = componentSelectionModel.getSelectionPath();
		if (path == null)
			return null;
		tree.scrollPathToVisible(path);

		return (RocketComponent) path.getLastPathComponent();
	}


	/**
	 * Creates the menu for the window.
	 */
	private void createMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		JMenuItem item;

		////  File
		menu = new JMenu(trans.get("main.menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		//// File-handling related tasks
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
		menubar.add(menu);

		//// New
		item = new JMenuItem(trans.get("main.menu.file.new"), KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_KEY));
		item.setMnemonic(KeyEvent.VK_N);
		//// Create a new rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.new.desc"));
		item.setIcon(Icons.FILE_NEW);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "New... selected");
				newAction();
				closeIfReplaceable();
			}
		});
		menu.add(item);

		//// Open...
		item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_KEY));
		//// Open a rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.open.desc"));
		item.setIcon(Icons.FILE_OPEN);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Open... selected");
				openAction();
			}
		});
		menu.add(item);

		//// Open Recent...
		item = new MRUDesignFileAction(trans.get("main.menu.file.openRecent"), this);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openRecent.desc"));
		item.setIcon(Icons.FILE_OPEN);
		menu.add(item);

		//// Open example...
		item = new ExampleDesignFileAction(trans.get("main.menu.file.openExample"), this);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openExample.desc"));
		item.setIcon(Icons.FILE_OPEN_EXAMPLE);
		menu.add(item);

		menu.addSeparator();

		//// Save
		item = new JMenuItem(trans.get("main.menu.file.save"), KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_KEY));
		//// Save the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.save.desc"));
		item.setIcon(Icons.FILE_SAVE);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Save selected");
				saveAction();
			}
		});
		menu.add(item);

		//// Save as...
		item = new JMenuItem(trans.get("main.menu.file.saveAs"), KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				SHORTCUT_KEY | ActionEvent.SHIFT_MASK));
		//// Save the current rocket design to a new file
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.saveAs.desc"));
		item.setIcon(Icons.FILE_SAVE_AS);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Save as... selected");
				saveAsAction();
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Import Rocksim
		item = new JMenuItem(trans.get("main.menu.file.import"));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.import.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Import... selected");
				importAction();
			}
		});
		menu.add(item);

		//// Export Rocksim
		item = new JMenuItem(trans.get("main.menu.file.export"));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.export.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Export... selected");
				exportAction();
			}
		});
		menu.add(item);

		//// Export decal...
		item = new JMenuItem(trans.get("main.menu.file.exportDecal"));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportDecal.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportDecalAction();
			}
		});
		item.setEnabled(document.getDecalList().size() > 0);
		final JMenuItem exportMenuItem = item;
		document.getRocket().addChangeListener(new StateChangeListener() {

			@Override
			public void stateChanged(EventObject e) {
				exportMenuItem.setEnabled(document.getDecalList().size() > 0);
			}

		});
		menu.add(item);


		//// Print...
		item = new JMenuItem(trans.get("main.menu.file.print"), KeyEvent.VK_P);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, SHORTCUT_KEY));
		//// Print parts list and fin template
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.print.desc"));
		item.setIcon(Icons.FILE_PRINT);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Print action selected");
				printAction();
			}
		});
		menu.add(item);


		menu.addSeparator();

		//// Close
		item = new JMenuItem(trans.get("main.menu.file.close"), KeyEvent.VK_C);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, SHORTCUT_KEY));
		//// Close the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.close.desc"));
		item.setIcon(Icons.FILE_CLOSE);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Close selected");
				closeAction();
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Quit
		item = new JMenuItem(trans.get("main.menu.file.quit"), KeyEvent.VK_Q);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_KEY));
		//// Quit the program
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.quit.desc"));
		item.setIcon(Icons.FILE_QUIT);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Quit selected");
				quitAction();
			}
		});
		menu.add(item);



		////  Edit
		menu = new JMenu(trans.get("main.menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		//// Rocket editing
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.desc"));
		menubar.add(menu);


		Action action = UndoRedoAction.newUndoAction(document);
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, SHORTCUT_KEY));
		item.setMnemonic(KeyEvent.VK_U);
		//// Undo the previous operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.undo.desc"));

		menu.add(item);

		action = UndoRedoAction.newRedoAction(document);
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, SHORTCUT_KEY));
		item.setMnemonic(KeyEvent.VK_R);
		//// Redo the previously undone operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.redo.desc"));
		menu.add(item);

		menu.addSeparator();


		item = new JMenuItem(actions.getCutAction());
		menu.add(item);

		item = new JMenuItem(actions.getCopyAction());
		menu.add(item);

		item = new JMenuItem(actions.getPasteAction());
		menu.add(item);

		item = new JMenuItem(actions.getDeleteAction());
		menu.add(item);

		menu.addSeparator();



		item = new JMenuItem(trans.get("main.menu.edit.resize"));
		item.setIcon(Icons.EDIT_SCALE);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.resize.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Scale... selected");
				ScaleDialog dialog = new ScaleDialog(document, getSelectedComponent(), BasicFrame.this);
				dialog.setVisible(true);
				dialog.dispose();
			}
		});
		menu.add(item);



		//// Preferences
		item = new JMenuItem(trans.get("main.menu.edit.preferences"));
		item.setIcon(Icons.PREFERENCES);
		//// Setup the application preferences
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.preferences.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Preferences selected");
				PreferencesDialog.showPreferences(BasicFrame.this);
			}
		});
		menu.add(item);

		//// Edit Component Preset File
		if (System.getProperty("openrocket.preseteditor.menu") != null) {
			item = new JMenuItem(trans.get("main.menu.edit.editpreset"));
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFrame dialog = new JFrame();
					dialog.getContentPane().add(new ComponentPresetEditor(dialog));
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setVisible(true);
				}
			});
			menu.add(item);
		}


		////  Analyze
		menu = new JMenu(trans.get("main.menu.analyze"));
		menu.setMnemonic(KeyEvent.VK_A);
		//// Analyzing the rocket
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.desc"));
		menubar.add(menu);


		//// Component analysis
		item = new JMenuItem(trans.get("main.menu.analyze.componentAnalysis"), KeyEvent.VK_C);
		//// Analyze the rocket components separately
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.componentAnalysis.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Component analysis selected");
				ComponentAnalysisDialog.showDialog(rocketpanel);
			}
		});
		menu.add(item);

// TODO: reimplement this dialog
//		//// Optimize
//		item = new JMenuItem(trans.get("main.menu.analyze.optimization"), KeyEvent.VK_O);
//		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.optimization.desc"));
//		item.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				log.info(Markers.USER_MARKER, "Rocket optimization selected");
//				new GeneralOptimizationDialog(document, BasicFrame.this).setVisible(true);
//			}
//		});
//		menu.add(item);

		//// Custom expressions
		item = new JMenuItem(trans.get("main.menu.analyze.customExpressions"), KeyEvent.VK_E);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.customExpressions.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("Custom expressions selected");
				new CustomExpressionDialog(document, BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);

		item = new JMenuItem(trans.get("PhotoFrame.title"), KeyEvent.VK_P);
		item.getAccessibleContext().setAccessibleDescription(trans.get("PhotoFrame.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Photo... selected");
				PhotoFrame pa = new PhotoFrame(document, BasicFrame.this);
				pa.setVisible(true);
			}
		});
		menu.add(item);

		////  Debug
		// (shown if openrocket.debug.menu is defined)
		if (System.getProperty("openrocket.debug.menu") != null) {
			menubar.add(makeDebugMenu());
		}



		////  Help

		menu = new JMenu(trans.get("main.menu.help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.desc"));
		menubar.add(menu);


		// Guided tours

		item = new JMenuItem(trans.get("main.menu.help.tours"), KeyEvent.VK_L);
		item.setIcon(Icons.HELP_TOURS);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.tours.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Guided tours selected");
				GuidedTourSelectionDialog.showDialog(BasicFrame.this);
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Bug report
		item = new JMenuItem(trans.get("main.menu.help.bugReport"), KeyEvent.VK_B);
		item.setIcon(Icons.HELP_BUG_REPORT);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.bugReport.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Bug report selected");
				BugReportDialog.showBugReportDialog(BasicFrame.this);
			}
		});
		menu.add(item);

		//// Debug log
		item = new JMenuItem(trans.get("main.menu.help.debugLog"), KeyEvent.VK_D);
		item.setIcon(Icons.HELP_DEBUG_LOG); 
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, SHORTCUT_KEY));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.debugLog.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Debug log selected");
				new DebugLogDialog(BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);

		menu.addSeparator();


		//// License
		item = new JMenuItem(trans.get("main.menu.help.license"), KeyEvent.VK_L);
		item.setIcon(Icons.HELP_LICENSE);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.license.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "License selected");
				new LicenseDialog(BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);


		//// About
		item = new JMenuItem(trans.get("main.menu.help.about"), KeyEvent.VK_A);
		item.setIcon(Icons.HELP_ABOUT);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.about.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "About selected");
				new AboutDialog(BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);


		this.setJMenuBar(menubar);
	}

	private JMenu makeDebugMenu() {
		JMenu menu;
		JMenuItem item;

		/*
		 * This menu is intentionally left untranslated.
		 */

		////  Debug menu
		menu = new JMenu("Debug");
		//// OpenRocket debugging tasks
		menu.getAccessibleContext().setAccessibleDescription("OpenRocket debugging tasks");

		//// What is this menu?
		item = new JMenuItem("What is this menu?");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "What is this menu? selected");
				JOptionPane.showMessageDialog(BasicFrame.this,
						new Object[] {
								"The 'Debug' menu includes actions for testing and debugging " +
										"OpenRocket.", " ",
										"The menu is made visible by defining the system property " +
												"'openrocket.debug.menu' when starting OpenRocket.",
				"It should not be visible by default." },
						"Debug menu", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Create test rocket
		item = new JMenuItem("Create test rocket");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Create test rocket selected");
				JTextField field = new JTextField();
				int sel = JOptionPane.showOptionDialog(BasicFrame.this, new Object[] {
						"Input text key to generate random rocket:",
						field
				}, "Generate random test rocket", JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, new Object[] {
								"Random", "OK"
				}, "OK");

				Rocket r;
				if (sel == 0) {
					r = new TestRockets(null).makeTestRocket();
				} else if (sel == 1) {
					r = new TestRockets(field.getText()).makeTestRocket();
				} else {
					return;
				}

				OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(r);
				doc.setSaved(true);
				BasicFrame frame = new BasicFrame(doc);
				frame.setVisible(true);
			}
		});
		menu.add(item);



		item = new JMenuItem("Create 'Iso-Haisu'");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Create Iso-Haisu selected");
				Rocket r = TestRockets.makeIsoHaisu();
				OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(r);
				doc.setSaved(true);
				BasicFrame frame = new BasicFrame(doc);
				frame.setVisible(true);
			}
		});
		menu.add(item);


		item = new JMenuItem("Create 'Big Blue'");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Create Big Blue selected");
				Rocket r = TestRockets.makeBigBlue();
				OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(r);
				doc.setSaved(true);
				BasicFrame frame = new BasicFrame(doc);
				frame.setVisible(true);
			}
		});
		menu.add(item);

		menu.addSeparator();


		item = new JMenuItem("Memory statistics");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Memory statistics selected");

				// Get discarded but remaining objects (this also runs System.gc multiple times)
				List<MemoryData> objects = MemoryManagement.getRemainingCollectableObjects();
				StringBuilder sb = new StringBuilder();
				sb.append("Objects that should have been garbage-collected but have not been:\n");
				int count = 0;
				for (MemoryData data : objects) {
					Object o = data.getReference().get();
					if (o == null)
						continue;
					sb.append("Age ").append(System.currentTimeMillis() - data.getRegistrationTime())
					.append(" ms:  ").append(o).append('\n');
					count++;
					// Explicitly null the strong reference to avoid possibility of invisible references
					o = null;
				}
				sb.append("Total: " + count);

				// Get basic memory stats
				System.gc();
				long max = Runtime.getRuntime().maxMemory();
				long free = Runtime.getRuntime().freeMemory();
				long used = max - free;
				String[] stats = new String[4];
				stats[0] = "Memory usage:";
				stats[1] = String.format("   Max memory:  %.1f MB", max / 1024.0 / 1024.0);
				stats[2] = String.format("   Used memory: %.1f MB (%.0f%%)", used / 1024.0 / 1024.0, 100.0 * used / max);
				stats[3] = String.format("   Free memory: %.1f MB (%.0f%%)", free / 1024.0 / 1024.0, 100.0 * free / max);


				DetailDialog.showDetailedMessageDialog(BasicFrame.this, stats, sb.toString(),
						"Memory statistics", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(item);

		//// Exhaust memory
		item = new JMenuItem("Exhaust memory");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Exhaust memory selected");
				LinkedList<byte[]> data = new LinkedList<byte[]>();
				int count = 0;
				final int bytesPerArray = 10240;
				try {
					while (true) {
						byte[] array = new byte[bytesPerArray];
						for (int i = 0; i < bytesPerArray; i++) {
							array[i] = (byte) i;
						}
						data.add(array);
						count++;
					}
				} catch (OutOfMemoryError error) {
					data = null;
					long size = bytesPerArray * (long) count;
					String s = String.format("OutOfMemory occurred after %d iterations (approx. %.1f MB consumed)",
							count, size / 1024.0 / 1024.0);
					log.debug(s, error);
					JOptionPane.showMessageDialog(BasicFrame.this, s);
				}
			}
		});
		menu.add(item);


		menu.addSeparator();

		//// Exception here
		item = new JMenuItem("Exception here");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Exception here selected");
				throw new RuntimeException("Testing exception from menu action listener");
			}
		});
		menu.add(item);

		item = new JMenuItem("Exception from EDT");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Exception from EDT selected");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						throw new RuntimeException("Testing exception from " +
								"later invoked EDT thread");
					}
				});
			}
		});
		menu.add(item);

		item = new JMenuItem("Exception from other thread");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Exception from other thread selected");
				new Thread() {
					@Override
					public void run() {
						throw new RuntimeException("Testing exception from newly created thread");
					}
				}.start();
			}
		});
		menu.add(item);

		item = new JMenuItem("OutOfMemoryError here");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "OutOfMemoryError here selected");
				throw new OutOfMemoryError("Testing OutOfMemoryError from menu action listener");
			}
		});
		menu.add(item);


		menu.addSeparator();


		item = new JMenuItem("Test popup");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Test popup selected");
				JPanel panel = new JPanel();
				panel.add(new JTextField(40));
				panel.add(new JSpinner());
				JPopupMenu popup = new JPopupMenu();
				popup.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				popup.add(panel);
				popup.show(BasicFrame.this, -50, 100);
			}
		});
		menu.add(item);




		return menu;
	}


	/**
	 * Select the tab on the main pane.
	 *
	 * @param tab	one of {@link #COMPONENT_TAB} or {@link #SIMULATION_TAB}.
	 */
	public void selectTab(int tab) {
		tabbedPane.setSelectedIndex(tab);
	}



	private void openAction() {
		JFileChooser chooser = new JFileChooser();

		chooser.addChoosableFileFilter(FileHelper.ALL_DESIGNS_FILTER);
		chooser.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
		chooser.setFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		int option = chooser.showOpenDialog(this);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "Decided not to open files, option=" + option);
			return;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

		File[] files = chooser.getSelectedFiles();
		log.info(Markers.USER_MARKER, "Opening files " + Arrays.toString(files));

		for (File file : files) {
			log.info("Opening file: " + file);
			if (open(file, this)) {
				MRUDesignFile opts = MRUDesignFile.getInstance();
				opts.addFile(file.getAbsolutePath());
			}
		}
	}

	private void importAction() {
		JFileChooser chooser = new JFileChooser();

		chooser.addChoosableFileFilter(FileHelper.ALL_DESIGNS_FILTER);
		chooser.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
		chooser.setFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		int option = chooser.showOpenDialog(this);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "Decided not to open files, option=" + option);
			return;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

		File[] files = chooser.getSelectedFiles();
		log.info(Markers.USER_MARKER, "Opening files " + Arrays.toString(files));

		for (File file : files) {
			log.info("Opening file: " + file);
			if (open(file, this)) {
				MRUDesignFile opts = MRUDesignFile.getInstance();
				opts.addFile(file.getAbsolutePath());
			}
		}
	}


	void closeIfReplaceable() {
		// Close previous window if replacing
		if (replaceable && document.isSaved()) {
			// We are replacing the frame, make new window have current location
			BasicFrame newFrame = frames.get(frames.size() - 1);
			newFrame.setLocation(this.getLocation());

			log.info("Closing window because it is replaceable");
			closeAction();
		}

	}

	/**
	 * Open a file based on a URL.
	 * @param url		the file to open.
	 * @param parent	the parent window for dialogs.
	 * @return			<code>true</code> if opened successfully.
	 */
	public static void open(URL url, BasicFrame parent) {
		String displayName = null;
		// First figure out the file name from the URL

		// Try using URI.getPath();
		try {
			URI uri = url.toURI();
			displayName = uri.getPath();
		} catch (URISyntaxException ignore) {
		}

		// Try URL-decoding the URL
		if (displayName == null) {
			try {
				displayName = URLDecoder.decode(url.toString(), "UTF-8");
			} catch (UnsupportedEncodingException ignore) {
			}
		}

		if (displayName == null) {
			displayName = "";
		}

		// Remove path from filename
		if (displayName.lastIndexOf('/') >= 0) {
			displayName = displayName.substring(displayName.lastIndexOf('/') + 1);
		}


		// Open the file
		log.info("Opening file from url=" + url + " filename=" + displayName);

		OpenFileWorker worker = new OpenFileWorker(url);
		open(worker, displayName, parent, true);
	}


	/**
	 * Open the specified file in a new design frame.  If an error occurs, an error
	 * dialog is shown and <code>false</code> is returned.
	 *
	 * @param file		the file to open.
	 * @param parent	the parent component for which a progress dialog is opened.
	 * @return			whether the file was successfully loaded and opened.
	 */
	public static boolean open(File file, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(file);
		return open(worker, file.getName(), parent, false);
	}


	/**
	 * Open the specified file using the provided worker.
	 *
	 * @param worker	the OpenFileWorker that loads the file.
	 * @param displayName	the file name to display in dialogs.
	 * @param file		the File to set the document to (may be null).
	 * @param parent
	 * @param openRocketConfigDialog if true, will open the configuration dialog of the rocket.  This is useful for examples.
	 * @return
	 */
	private static boolean open(OpenFileWorker worker, String displayName, Window parent, boolean openRocketConfigDialog) {
		// Open the file in a Swing worker thread
		log.info("Starting OpenFileWorker");
		if (!SwingWorkerDialog.runWorker(parent, "Opening file", "Reading " + displayName + "...", worker)) {
			// User cancelled the operation
			log.info("User cancelled the OpenFileWorker");
			return false;
		}


		// Handle the document
		OpenRocketDocument doc = null;
		try {

			doc = worker.get();

		} catch (ExecutionException e) {

			Throwable cause = e.getCause();

			if (cause instanceof FileNotFoundException) {

				log.warn("File not found", cause);
				JOptionPane.showMessageDialog(parent,
						"File not found: " + displayName,
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else if (cause instanceof RocketLoadException) {

				log.warn("Error loading the file", cause);
				JOptionPane.showMessageDialog(parent,
						"Unable to open file '" + displayName + "': "
								+ cause.getMessage(),
								"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else {

				throw new BugException("Unknown error when opening file", e);

			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		if (doc == null) {
			throw new BugException("Document loader returned null");
		}


		// Show warnings
		WarningSet warnings = worker.getRocketLoader().getWarnings();
		if (!warnings.isEmpty()) {
			log.info("Warnings while reading file: " + warnings);
			WarningDialog.showWarnings(parent,
					new Object[] {
							//// The following problems were encountered while opening
							trans.get("BasicFrame.WarningDialog.txt1") + " " + displayName + ".",
							//// Some design features may not have been loaded correctly.
							trans.get("BasicFrame.WarningDialog.txt2")
			},
					//// Warnings while opening file
					trans.get("BasicFrame.WarningDialog.title"), warnings);
		}

		// Open the frame
		log.debug("Opening new frame with the document");
		BasicFrame frame = new BasicFrame(doc);
		frame.setVisible(true);

		if (parent != null && parent instanceof BasicFrame) {
			((BasicFrame) parent).closeIfReplaceable();
		}
		if (openRocketConfigDialog) {
			ComponentConfigDialog.showDialog(frame, doc, doc.getRocket());
		}

		return true;
	}

	/**
	 * "Save" action.  If the design is new, then this is identical to "Save As", with a default file filter for .ork.
	 * If the rocket being edited previously was opened from a .ork file, then it will be saved immediately to the same
	 * file.  But clicking on 'Save' for an existing design file with a .rkt will bring up a confirmation dialog because
	 * it's potentially a destructive write (loss of some fidelity if it's truly an original Rocksim generated file).
	 *
	 * @return true if the file was saved, false otherwise
	 */
	private boolean saveAction() {
		File file = document.getFile();
		if (file == null || document.getDefaultStorageOptions().getFileType().equals(FileType.ROCKSIM)) {
			log.info("Document does not contain file, opening save as dialog instead");
			return saveAsAction();
		}
		
		log.info("Saving document to " + file);

		return saveAsOpenRocket(file);
	}


	/**
	 * "Export" action.
	 *
	 * @return true if the file was saved, false otherwise
	 */
	private boolean exportAction() {
		File file = null;

		final SaveAsFileChooser chooser = SaveAsFileChooser.build(document, FileType.ROCKSIM);

		int option = chooser.showSaveDialog(BasicFrame.this);

		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "User decided not to save, option=" + option);
			return false;
		}

		file = chooser.getSelectedFile();
		if (file == null) {
			log.info(Markers.USER_MARKER, "User did not select a file");
			return false;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

		file = FileHelper.forceExtension(file, "ork");
		if (FileHelper.confirmWrite(file, this) ) {
			return saveAsRocksim(file);
		}
		return false;
	}

	/**
	 * Perform the writing of the design to the given file in Rocksim format.
	 *
	 * @param file  the chosen file
	 *
	 * @return true if the file was written
	 */
	private boolean saveAsRocksim(File file) {
		if ( prefs.getShowRockSimFormatWarning() ) {
			// Show Rocksim format warning
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new StyledLabel(trans.get("SaveRktWarningDialog.txt1")), "wrap");
			final JCheckBox check = new JCheckBox(trans.get("SaveRktWarningDialog.donotshow"));
			check.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					prefs.setShowRockSimFormatWarning(!check.isSelected());
				}
			});
			panel.add(check);
			int sel = JOptionPane.showOptionDialog(null,
					panel,
					"", // title
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, // icon
					null, // options
					null // default option
					);
			if ( sel == 1  ) {
				return false;
			}
		}
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}

		try {
			StorageOptions options = new StorageOptions();
			options.setFileType(StorageOptions.FileType.ROCKSIM);
			ROCKET_SAVER.save(file, document, options);
			// Do not update the save state of the document.
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * "Save As" action.
	 *
	 * @return true if the file was saved, false otherwise
	 */
	private boolean saveAsAction() {
		File file = null;

		final SaveAsFileChooser chooser = SaveAsFileChooser.build(document, FileType.OPENROCKET);

		int option = chooser.showSaveDialog(BasicFrame.this);

		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "User decided not to save, option=" + option);
			return false;
		}

		file = chooser.getSelectedFile();
		if (file == null) {
			log.info(Markers.USER_MARKER, "User did not select a file");
			return false;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		chooser.storeOptions(document.getDefaultStorageOptions());

		file = FileHelper.forceExtension(file, "ork");
		boolean result = FileHelper.confirmWrite(file, this) && saveAsOpenRocket(file);
		if (result) {
			MRUDesignFile opts = MRUDesignFile.getInstance();
			opts.addFile(file.getAbsolutePath());
		}
		return result;
	}

	/**
	 * Perform the writing of the design to the given file in OpenRocket format.
	 *
	 * @param file  the chosen file
	 *
	 * @return true if the file was written
	 */
	private boolean saveAsOpenRocket(File file) {
		file = FileHelper.forceExtension(file, "ork");
		log.info("Saving document as " + file);

		if (!StorageOptionChooser.verifyStorageOptions(document, this)) {
			// User cancelled the dialog
			log.info(Markers.USER_MARKER, "User cancelled saving in storage options dialog");
			return false;
		}

		document.getDefaultStorageOptions().setFileType(FileType.OPENROCKET);
		SaveFileWorker worker = new SaveFileWorker(document, file, ROCKET_SAVER);

		if (!SwingWorkerDialog.runWorker(this, "Saving file",
				"Writing " + file.getName() + "...", worker)) {

			// User cancelled the save
			log.info(Markers.USER_MARKER, "User cancelled the save, deleting the file");
			file.delete();
			return false;
		}

		try {
			worker.get();
			document.setFile(file);
			document.setSaved(true);
			setTitle();
			return true;
		} catch (ExecutionException e) {

			Throwable cause = e.getCause();

			if (cause instanceof IOException) {
				log.warn("An I/O error occurred while saving " + file, cause);
				JOptionPane.showMessageDialog(this, new String[] {
						"An I/O error occurred while saving:",
						e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				Reflection.handleWrappedException(e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		return false;
	}


	private boolean closeAction() {
		if (!document.isSaved()) {
			log.info("Confirming whether to save the design");
			ComponentConfigDialog.hideDialog();
			int result = JOptionPane.showConfirmDialog(this,
					trans.get("BasicFrame.dlg.lbl1") + rocket.getName() +
					trans.get("BasicFrame.dlg.lbl2") + "  " +
					trans.get("BasicFrame.dlg.lbl3"),
					trans.get("BasicFrame.dlg.title"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				// Save
				log.info(Markers.USER_MARKER, "User requested file save");
				if (!saveAction()) {
					log.info("File save was interrupted, not closing");
					return false;
				}
			} else if (result == JOptionPane.NO_OPTION) {
				// Don't save: No-op
				log.info(Markers.USER_MARKER, "User requested to discard design");
			} else {
				// Cancel or close
				log.info(Markers.USER_MARKER, "User cancelled closing, result=" + result);
				return false;
			}
		}

		// Rocket has been saved or discarded
		log.debug("Disposing window");
		this.dispose();

		ComponentConfigDialog.hideDialog();
		ComponentAnalysisDialog.hideDialog();

		frames.remove(this);
		if (frames.isEmpty()) {
			log.info("Last frame closed, exiting");
			System.exit(0);
		}
		return true;
	}

	public void exportDecalAction() {
		new ExportDecalDialog(this, document).setVisible(true);
	}


	public void printAction() {
		double rotation = rocketpanel.getFigure().getRotation();
		new PrintDialog(this, document, rotation).setVisible(true);
	}

	/**
	 * Open a new design window with a basic rocket+stage.
	 */
	public static void newAction() {
		log.info("New action initiated");

		OpenRocketDocument doc = OpenRocketDocumentFactory.createNewRocket();

		BasicFrame frame = new BasicFrame(doc);
		frame.replaceable = true;
		frame.setVisible(true);
	}

	/**
	 * Quit the application.  Confirms saving unsaved designs.  The action of File->Quit.
	 */
	public static void quitAction() {
		log.info("Quit action initiated");
		for (int i = frames.size() - 1; i >= 0; i--) {
			log.debug("Closing frame " + frames.get(i));
			if (!frames.get(i).closeAction()) {
				// Close canceled
				log.info("Quit was cancelled");
				return;
			}
		}
		// Should not be reached, but just in case
		log.error("Should already have exited application");
		System.exit(0);
	}


	/**
	 * Set the title of the frame, taking into account the name of the rocket, file it
	 * has been saved to (if any) and saved status.
	 */
	private void setTitle() {
		File file = document.getFile();
		boolean saved = document.isSaved();
		String title;

		title = rocket.getName();
		if (file != null) {
			title = title + " (" + file.getName() + ")";
		}
		if (!saved)
			title = "*" + title;

		setTitle(title);
	}



	/**
	 * Find a currently open BasicFrame containing the specified rocket.  This method
	 * can be used to map a Rocket to a BasicFrame from GUI methods.
	 *
	 * @param rocket the Rocket.
	 * @return		 the corresponding BasicFrame, or <code>null</code> if none found.
	 */
	public static BasicFrame findFrame(Rocket rocket) {
		for (BasicFrame f : frames) {
			if (f.rocket == rocket) {
				log.debug("Found frame " + f + " for rocket " + rocket);
				return f;
			}
		}
		log.debug("Could not find frame for rocket " + rocket);
		return null;
	}

	/**
	 * Find a currently open document by the rocket object.  This method can be used
	 * to map a Rocket to OpenRocketDocument from GUI methods.
	 *
	 * @param rocket the Rocket.
	 * @return		 the corresponding OpenRocketDocument, or <code>null</code> if not found.
	 */
	public static OpenRocketDocument findDocument(Rocket rocket) {
		BasicFrame frame = findFrame(rocket);
		if (frame != null) {
			return frame.document;
		} else {
			return null;
		}
	}

	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabSource = (JTabbedPane) e.getSource();
		String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
		if (tab.equals(trans.get("BasicFrame.tab.Flightsim"))) {
			simulationPanel.activating();
		}
	}
}


class BasicFrame_changeAdapter implements javax.swing.event.ChangeListener {
	BasicFrame adaptee;

	BasicFrame_changeAdapter(BasicFrame adaptee) {
		this.adaptee = adaptee;
	}
	public void stateChanged(ChangeEvent e) {
		adaptee.stateChanged(e);
	}
}

