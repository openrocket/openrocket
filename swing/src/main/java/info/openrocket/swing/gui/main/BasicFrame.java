package info.openrocket.swing.gui.main;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.swing.gui.choosers.OptionChooser;
import info.openrocket.swing.gui.choosers.StorageOptionChooser;
import info.openrocket.swing.gui.util.UpdateInfoRunner;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.file.wavefrontobj.export.OBJExportOptions;
import info.openrocket.core.file.wavefrontobj.export.OBJExporterFactory;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.core.document.events.DocumentChangeListener;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.svg.export.SVGExportOptions;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.MemoryManagement;
import info.openrocket.core.util.MemoryManagement.MemoryData;
import info.openrocket.core.util.Reflection;
import info.openrocket.core.util.TestRockets;

import info.openrocket.swing.gui.configdialog.SaveDesignInfoPanel;
import info.openrocket.swing.gui.dialogs.ErrorWarningDialog;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.configdialog.ComponentConfigDialog;
import info.openrocket.swing.gui.customexpression.CustomExpressionDialog;
import info.openrocket.swing.gui.export.SVGRocketPartsExporter;
import info.openrocket.swing.gui.export.SvgOptionsDialog;
import info.openrocket.swing.gui.dialogs.AboutDialog;
import info.openrocket.swing.gui.dialogs.BugReportDialog;
import info.openrocket.swing.gui.dialogs.componentanalysis.ComponentAnalysisDialog;
import info.openrocket.swing.gui.dialogs.DebugLogDialog;
import info.openrocket.swing.gui.dialogs.DecalNotFoundDialog;
import info.openrocket.swing.gui.dialogs.DetailDialog;
import info.openrocket.swing.gui.dialogs.LicenseDialog;
import info.openrocket.swing.gui.dialogs.PrintDialog;
import info.openrocket.swing.gui.dialogs.SwingWorkerDialog;
import info.openrocket.swing.gui.dialogs.WarningDialog;
import info.openrocket.swing.gui.dialogs.optimization.GeneralOptimizationDialog;
import info.openrocket.swing.gui.dialogs.preferences.PreferencesDialog;
import info.openrocket.swing.gui.figure3d.photo.PhotoFrame;
import info.openrocket.swing.gui.help.tours.GuidedTourSelectionDialog;
import info.openrocket.swing.gui.main.componenttree.ComponentTree;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.util.DummyFrameMenuOSX;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.OpenFileWorker;
import info.openrocket.swing.gui.util.SaveFileWorker;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.util.URLUtil;
import info.openrocket.swing.utils.ComponentPresetEditor;
import info.openrocket.swing.gui.figureelements.BananaForScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFrame extends JFrame {
	private static final long serialVersionUID = 948877655223365313L;

private static final Logger log = LoggerFactory.getLogger(BasicFrame.class);

private static final GeneralRocketSaver ROCKET_SAVER = new GeneralRocketSaver();
private static final int PREVIEW_WIDTH = 1000;
private static final int PREVIEW_MIN_HEIGHT = 600;
private static final int PREVIEW_MAX_HEIGHT = 800;

private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	public static final int DESIGN_TAB = 0;
	public static final int FLIGHT_CONFIGURATION_TAB = 1;
	public static final int SIMULATION_TAB = 2;
	private int previousTab = DESIGN_TAB;

	private static final int CASCADE_OFFSET_X = 30;
	private static final int CASCADE_OFFSET_Y = 30;


	/**
	 * List of currently open frames.  When the list goes empty
	 * it is time to exit the application.
	 */
	private static final List<BasicFrame> frames = new ArrayList<>();
	private static BasicFrame startupFrame = null;	// the frame that was created at startup


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
	private final JPopupMenu popupMenu;

	private final DocumentSelectionModel selectionModel;
	private final TreeSelectionModel componentSelectionModel;
	private final ListSelectionModel simulationSelectionModel;

	/** Actions available for rocket modifications */
	private final RocketActions actions;

	private final DesignPanel designPanel;
	private final FlightConfigurationPanel flightConfigurationPanel;
	private final SimulationPanel simulationPanel;

	private boolean showBananaForScaleInToolsMenu = false;
	private JCheckBoxMenuItem bananaForScaleMenuItem = null;
	private JPopupMenu.Separator bananaForScaleSeparator = null;
	private BananaForScale bananaForScaleElement = null;
	private volatile boolean bananaAltKeyDown = false;
	private KeyEventDispatcher bananaAltKeyDispatcher = null;

	public static BasicFrame lastFrameInstance = null;		// Latest BasicFrame that was created
	private static boolean quitCalled = false;				// Keeps track whether the quit action has been called


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
		BasicFrame.lastFrameInstance = this;

		//	Create the component tree selection model that will be used
		componentSelectionModel = new DefaultTreeSelectionModel();
		componentSelectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		// ----- Create the different BasicFrame panels -----
		log.debug("Constructing the BasicFrame UI");

		////	Top segment, tabbed pane
		simulationPanel = new SimulationPanel(this, document);
		{
			//	Obtain the simulation selection model that will be used
			simulationSelectionModel = simulationPanel.getSimulationListSelectionModel();

			//	Combine into a DocumentSelectionModel
			selectionModel = new DocumentSelectionModel(document);
			selectionModel.attachComponentTreeSelectionModel(componentSelectionModel);
			selectionModel.attachSimulationListSelectionModel(simulationSelectionModel);

			// Create RocketActions
			actions = new RocketActions(document, selectionModel, this, simulationPanel);
		}
		{
			// Create the component tree
			tree = new ComponentTree(document);
			tree.setSelectionModel(componentSelectionModel);
		}

		designPanel = new DesignPanel(this, document, tree);
		flightConfigurationPanel = new FlightConfigurationPanel(this, document);
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(trans.get("BasicFrame.tab.Rocketdesign"), null, designPanel);
		tabbedPane.addTab(trans.get("BasicFrame.tab.Flightconfig"), null, flightConfigurationPanel);
		tabbedPane.addTab(trans.get("BasicFrame.tab.Flightsim"), null, simulationPanel);

		//	Add change listener to catch when the tabs are changed.  This is to run simulations
		//	automatically when the simulation tab is selected.
		tabbedPane.addChangeListener(new BasicFrame_changeAdapter(this));

		////  Bottom segment, rocket figure
		rocketpanel = new RocketPanel(document, this);
		rocketpanel.setSelectionModel(tree.getSelectionModel());

		//// The main vertical split pane
		JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		vertical.setResizeWeight(0.5);
		vertical.setTopComponent(tabbedPane);
		vertical.setBottomComponent(rocketpanel);
		this.add(vertical);

		// Populate the popup menu
		{
			popupMenu = new JPopupMenu();
			popupMenu.add(actions.getEditAction());
			popupMenu.add(actions.getCutAction());
			popupMenu.add(actions.getCopyAction());
			popupMenu.add(actions.getPasteAction());
			popupMenu.add(actions.getDuplicateAction());
			popupMenu.add(actions.getDeleteAction());

			popupMenu.addSeparator();
			JMenu selectMenu = new JMenu(trans.get("RocketActions.Select"));
			selectMenu.add(actions.getSelectSameColorAction());
			selectMenu.add(actions.getDeselectAllAction());
			popupMenu.add(selectMenu);

			popupMenu.addSeparator();
			popupMenu.add(actions.getScaleAction());
			popupMenu.add(actions.getToggleVisibilityAction());

			popupMenu.addSeparator();
			popupMenu.add(actions.getExportOBJAction());
			popupMenu.add(actions.getExportSVGAction());
		}

		installBananaAltKeyTracker();
		createMenu();


		rocket.addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				setTitle();
			}
		});

		document.addDocumentChangeListener(new DocumentChangeListener() {
			@Override
			public void documentChanged(DocumentChangeEvent e) {
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
		GUIUtil.rememberWindowPosition(this);
		positionRelativeToExistingFrames();

		GUIUtil.setWindowIcons(this);

		GUIUtil.getUITheme().applyThemeToRootPane(getRootPane());

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
		if( componentSelectionModel.isSelectionEmpty() ){
			final Rocket rocket = document.getRocket();
			if( rocket != null ) {
				final RocketComponent topStage = rocket.getChild(0);
				if (topStage != null) {
					final TreePath selectionPath = new TreePath(topStage);
					componentSelectionModel.setSelectionPath(selectionPath);
					tree.setSelectionRow(1);
					// Don't select children components at startup (so override the default behavior with this new selection)
					rocketpanel.getFigure().setSelection(new RocketComponent[] { topStage });
					rocketpanel.getFigure3d().setSelection(new RocketComponent[] { topStage });
					log.debug("... Setting Initial Selection: " + tree.getSelectionPath() );
				}
			}
		}
		log.debug("BasicFrame instantiation complete");
	}

	@Override
	public void dispose() {
		uninstallBananaAltKeyTracker();
		super.dispose();
	}

	private void installBananaAltKeyTracker() {
		if (bananaAltKeyDispatcher != null) {
			return;
		}
		bananaAltKeyDispatcher = new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ALT) {
					bananaAltKeyDown = (e.getID() == KeyEvent.KEY_PRESSED);
				}
				return false;
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(bananaAltKeyDispatcher);
	}

	private void uninstallBananaAltKeyTracker() {
		if (bananaAltKeyDispatcher == null) {
			return;
		}
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(bananaAltKeyDispatcher);
		bananaAltKeyDispatcher = null;
	}


	/**
	 * Cascade this frame relative to the previously opened frame while keeping it on-screen.
	 */
	private void positionRelativeToExistingFrames() {
		if (frames.isEmpty()) {
			return;
		}

		BasicFrame previousFrame = frames.get(frames.size() - 1);
		Point baseLocation = previousFrame.getLocation();

		GraphicsConfiguration targetConfiguration = previousFrame.getGraphicsConfiguration();
		if (targetConfiguration == null) {
			targetConfiguration = this.getGraphicsConfiguration();
		}
		if (targetConfiguration == null) {
			targetConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		}

		Rectangle usableBounds = targetConfiguration.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(targetConfiguration);

		int minX = usableBounds.x + screenInsets.left;
		int minY = usableBounds.y + screenInsets.top;
		int maxX = usableBounds.x + usableBounds.width - screenInsets.right - this.getWidth();
		int maxY = usableBounds.y + usableBounds.height - screenInsets.bottom - this.getHeight();

		if (maxX < minX) {
			maxX = minX;
		}
		if (maxY < minY) {
			maxY = minY;
		}

		int x = baseLocation.x + CASCADE_OFFSET_X;
		int y = baseLocation.y + CASCADE_OFFSET_Y;

		if (x > maxX) {
			x = minX;
		}
		if (y > maxY) {
			y = minY;
		}

		x = Math.max(x, minX);
		y = Math.max(y, minY);

		this.setLocationByPlatform(false);
		this.setLocation(x, y);
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
	 * Return the currently selected rocket component, or <code>null</code> if none selected.
	 */
	private List<RocketComponent> getSelectedComponents() {
		TreePath[] paths = componentSelectionModel.getSelectionPaths();
		if (paths == null || paths.length == 0)
			return null;

		List<RocketComponent> result = new LinkedList<>();
		for (TreePath path : paths) {
			tree.scrollPathToVisible(path);
			RocketComponent component = (RocketComponent) path.getLastPathComponent();
			result.add(component);
		}

		return result;
	}

	public RocketPanel getRocketPanel() {
		return rocketpanel;
	}

	/**
	 * Creates the menu for the window.
	 */
	private void createMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu;
		JMenuItem item;

		//  File
		fileMenu = new JMenu(trans.get("main.menu.file"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
		menubar.add(fileMenu);

		//// 	New etc.
		addFileCreateAndOpenMenuItems(fileMenu, this);

		// ------------------------------------------------------------------------------------------

		fileMenu.addSeparator();

		//// 	Save
		item = new JMenuItem(trans.get("main.menu.file.save"), KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK));
		//// Save the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.save.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_SAVE));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Save selected");
				saveAction();
			}
		});
		fileMenu.add(item);

		//// 	Save as...
		item = new JMenuItem(trans.get("main.menu.file.saveAs"), KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK));
		//// Save the current rocket design to a new file
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.saveAs.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_SAVE_AS));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Save as... selected");
				saveAsAction();
			}
		});
		fileMenu.add(item);


		//// 	Export as
		JMenu exportSubMenu = new JMenu(trans.get("main.menu.file.exportAs"));
		exportSubMenu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportAs.desc"));
		exportSubMenu.setIcon(Icons.deriveMenuIcon(Icons.FILE_EXPORT));

		////// 		Export RASAero
		JMenuItem exportRASAero = new JMenuItem(trans.get("main.menu.file.exportAs.RASAero"));
		exportRASAero.setIcon(Icons.deriveMenuIcon(Icons.RASAERO));
		exportRASAero.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportAs.RASAero.desc"));
		exportRASAero.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportRASAeroAction();}
		});
		exportSubMenu.add(exportRASAero);

		////// 		Export RockSim
		JMenuItem exportRockSim = new JMenuItem(trans.get("main.menu.file.exportAs.RockSim"));
		exportRockSim.setIcon(Icons.deriveMenuIcon(Icons.ROCKSIM));
		exportRockSim.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportAs.RockSim.desc"));
		exportRockSim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportRockSimAction();}
		});
		exportSubMenu.add(exportRockSim);

		exportSubMenu.addSeparator();

		////// 		Export Wavefront OBJ
		JMenuItem exportOBJ = new JMenuItem(trans.get("main.menu.file.exportAs.WavefrontOBJ"));
		exportOBJ.setIcon(Icons.deriveMenuIcon(Icons.EXPORT_3D));
		exportOBJ.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportAs.WavefrontOBJ.desc"));
		exportOBJ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportWavefrontOBJAction();}
		});
		selectionModel.addDocumentSelectionListener(new DocumentSelectionListener() {
			@Override
			public void valueChanged(int changeType) {
				exportOBJ.setEnabled(getSelectedComponents() != null && !getSelectedComponents().isEmpty());
			}
		});
		exportSubMenu.add(exportOBJ);

		//////		Export SVG profiles
		JMenuItem exportSvgProfiles = new JMenuItem(trans.get("main.menu.file.exportAs.SVGProfiles"));
		exportSvgProfiles.setIcon(Icons.deriveMenuIcon(Icons.EXPORT_SVG));
		exportSvgProfiles.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportAs.SVGProfiles.desc"));
		exportSvgProfiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportSvgProfilesAction();
			}
		});
		exportSubMenu.add(exportSvgProfiles);

		fileMenu.add(exportSubMenu);
		fileMenu.addSeparator();

		// ------------------------------------------------------------------------------------------

		////	Save decal image...
		item = new JMenuItem(trans.get("main.menu.file.exportDecal"));
		item.setIcon(Icons.deriveMenuIcon(Icons.SAVE_DECAL));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.exportDecal.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportDecalAction();
			}
		});
		item.setEnabled(!document.getDecalList().isEmpty());

		// TODO
		/* document.getRocket().addChangeListener(new StateChangeListener() {

		@Override
		public void stateChanged(EventObject e) {
			exportMenuItem.setEnabled(document.getDecalList().size() > 0);
		}
		}); */
		fileMenu.add(item);

		//// 	Print design info...
		item = new JMenuItem(trans.get("main.menu.file.print"), KeyEvent.VK_P);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_DOWN_MASK));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.print.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_PRINT));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Print action selected");
				printAction();
			}
		});
		fileMenu.add(item);

		//  export sim table...
		AbstractAction simTableExportAction = simulationPanel.getExportSimulationTableAsCSVAction();
		JMenuItem exportSimTableToCSVMenuItem = createMenuItemFromAction(simTableExportAction);
		fileMenu.add(exportSimTableToCSVMenuItem);

		fileMenu.addSeparator();

		// ------------------------------------------------------------------------------------------


		//// Properties
		item = new JMenuItem(trans.get("main.menu.file.properties"), KeyEvent.VK_I);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.properties.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.CONFIGURE));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.META_DOWN_MASK));
		item.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Properties selected");
				ComponentConfigDialog.showDialog(BasicFrame.this,document, rocket);
			}
		});
		fileMenu.add(item);

		////	Close
		item = new JMenuItem(trans.get("main.menu.file.close"), KeyEvent.VK_C);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK));
		//// Close the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.close.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_CLOSE));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Close selected");
				closeAction();
			}
		});

		fileMenu.add(item);

		fileMenu.addSeparator();

		////	Quit
		item = new JMenuItem(trans.get("main.menu.file.quit"), KeyEvent.VK_Q);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK));
		//// Quit the program
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.quit.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_QUIT));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Quit selected");
				quitAction();
			}
		});
		fileMenu.add(item);

		////	Edit
		JMenu editMenu = new JMenu(trans.get("main.menu.edit"));
		editMenu.setMnemonic(KeyEvent.VK_E);

		////	Rocket editing
		editMenu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.desc"));
		menubar.add(editMenu);

		Action action = UndoRedoAction.newUndoAction(document);
		item = createMenuItemFromAction(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK));
		item.setMnemonic(KeyEvent.VK_U);

		////	Undo the previous operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.undo.desc"));

		editMenu.add(item);

		action = UndoRedoAction.newRedoAction(document);
		item = createMenuItemFromAction(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK));
		item.setMnemonic(KeyEvent.VK_R);

		////	Redo the previously undone operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.redo.desc"));
		editMenu.add(item);

		editMenu.addSeparator();


		item = createMenuItemFromAction(actions.getEditAction());
		editMenu.add(item);

		item = createMenuItemFromAction(actions.getCutAction());
		editMenu.add(item);

		item = createMenuItemFromAction(actions.getCopyAction());
		editMenu.add(item);

		item = createMenuItemFromAction(actions.getPasteAction());
		editMenu.add(item);

		item = createMenuItemFromAction(actions.getDuplicateAction());
		editMenu.add(item);

		item = createMenuItemFromAction(actions.getDeleteAction());
		editMenu.add(item);

		editMenu.addSeparator();

		JMenu selectSubMenu = new JMenu(trans.get("RocketActions.Select"));
		editMenu.add(selectSubMenu);
		item = createMenuItemFromAction(actions.getSelectSameColorAction());
		selectSubMenu.add(item);
		item = createMenuItemFromAction(actions.getDeselectAllAction());
		selectSubMenu.add(item);

		editMenu.addSeparator();

		item = createMenuItemFromAction(actions.getScaleAction());
		editMenu.add(item);

		////	Visibility
		JMenu visibilitySubMenu = new JMenu(trans.get("RocketActions.Visibility"));
		editMenu.add(visibilitySubMenu);
		item = createMenuItemFromAction(actions.getToggleVisibilityAction());
		visibilitySubMenu.add(item);
		item = createMenuItemFromAction(actions.getShowAllComponentsAction());
		visibilitySubMenu.add(item);

		editMenu.addSeparator();

		////	Preferences
		item = new JMenuItem(trans.get("main.menu.edit.preferences"));
		item.setIcon(Icons.deriveMenuIcon(Icons.PREFERENCES));

		////	Setup the application preferences
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.preferences.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Preferences selected");
				PreferencesDialog.showPreferences(BasicFrame.this);
			}
		});
		editMenu.add(item);

		////	Edit Component Preset File
		if (System.getProperty("openrocket.preseteditor.fileMenu") != null) {
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
			editMenu.add(item);
		}


		//	Tools
		JMenu toolsMenu = new JMenu(trans.get("main.menu.tools"));
		menubar.add(toolsMenu);

		////	Component analysis
		item = new JMenuItem(trans.get("main.menu.tools.componentAnalysis"), KeyEvent.VK_C);

		////	Analyze the rocket components separately
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.tools.componentAnalysis.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Component analysis selected");
				ComponentAnalysisDialog.showDialog(document, rocketpanel);
			}
		});
		toolsMenu.add(item);

		////	Optimize
		item = new JMenuItem(trans.get("main.menu.tools.optimization"), KeyEvent.VK_O);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.tools.optimization.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Rocket optimization selected");
				try {
					new GeneralOptimizationDialog(document, BasicFrame.this).setVisible(true);
				} catch (InterruptedException ex) {
					log.warn(ex.getMessage());
				}
			}
		});
		toolsMenu.add(item);

		////	Custom expressions
		item = new JMenuItem(trans.get("main.menu.tools.customExpressions"), KeyEvent.VK_E);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.tools.customExpressions.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("Custom expressions selected");
				new CustomExpressionDialog(document, BasicFrame.this).setVisible(true);
			}
		});
		toolsMenu.add(item);

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
		toolsMenu.add(item);

		bananaForScaleSeparator = new JPopupMenu.Separator();
		bananaForScaleMenuItem = new JCheckBoxMenuItem("Banana for scale");
		bananaForScaleMenuItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setBananaForScaleEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		toolsMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				boolean show = showBananaForScaleInToolsMenu || bananaAltKeyDown || isAltDownInCurrentAwtEvent();
				setBananaForScaleMenuVisible(toolsMenu, show);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				showBananaForScaleInToolsMenu = false;
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				showBananaForScaleInToolsMenu = false;
			}
		});

		toolsMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.isAltDown()) {
					showBananaForScaleInToolsMenu = true;
				}
			}
		});

		toolsMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				boolean show = showBananaForScaleInToolsMenu || bananaAltKeyDown || isAltDownInCurrentAwtEvent();
				setBananaForScaleMenuVisible(toolsMenu, show);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				showBananaForScaleInToolsMenu = false;
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				showBananaForScaleInToolsMenu = false;
			}
		});

		////	Debug
		//	//	(shown if openrocket.debug.fileMenu is defined)
		if (System.getProperty("openrocket.debug.fileMenu") != null) {
			menubar.add(makeDebugMenu());
		}

		////	Help
		generateHelpMenu(menubar, this);

		this.setJMenuBar(menubar);
	}

	private static boolean isAltDownInCurrentAwtEvent() {
		AWTEvent event = EventQueue.getCurrentEvent();
		if (event instanceof InputEvent) {
			return (((InputEvent) event).getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
		}
		if (event instanceof java.awt.event.KeyEvent) {
			return ((((java.awt.event.KeyEvent) event).getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
		}
		return false;
	}

	private void setBananaForScaleMenuVisible(JMenu toolsMenu, boolean visible) {
		if (toolsMenu == null || bananaForScaleSeparator == null || bananaForScaleMenuItem == null) {
			return;
		}

		if (visible) {
			if (bananaForScaleSeparator.getParent() == null) {
				toolsMenu.add(bananaForScaleSeparator);
			}
			if (bananaForScaleMenuItem.getParent() == null) {
				toolsMenu.add(bananaForScaleMenuItem);
			}
		} else {
			toolsMenu.remove(bananaForScaleMenuItem);
			toolsMenu.remove(bananaForScaleSeparator);
		}
		toolsMenu.revalidate();
		toolsMenu.repaint();
	}

	private void setBananaForScaleEnabled(boolean enabled) {
		if (rocketpanel == null) {
			return;
		}

		if (bananaForScaleElement == null) {
			bananaForScaleElement = new BananaForScale(rocketpanel.getFigure());
		}

		rocketpanel.getFigure().removeAbsoluteExtra(bananaForScaleElement);
		if (enabled) {
			rocketpanel.getFigure().addAbsoluteExtra(bananaForScaleElement);
		}
		rocketpanel.getFigure().repaint();
	}

	/**
	 * Create a JMenuItem from an Action. It styles the icon appropriately.
	 * @param action the action
	 * @return the menu item
	 */
	private static JMenuItem createMenuItemFromAction(Action action) {
		JMenuItem item = new JMenuItem(action);
		item.setIcon(Icons.deriveMenuIcon(item.getIcon()));
		return item;
	}

	public static void generateHelpMenu(JMenuBar menubar, JFrame parent) {
		JMenu menu;
		JMenuItem item;

		menu = new JMenu(trans.get("main.menu.help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.desc"));
		menubar.add(menu);

		////	Guided tours
		item = new JMenuItem(trans.get("main.menu.help.tours"), KeyEvent.VK_L);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_TOURS));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.tours.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Guided tours selected");
				GuidedTourSelectionDialog.showDialog(parent);
			}
		});
		menu.add(item);

		////	Online Documentation
		item = new JMenuItem(trans.get("main.menu.help.documentation"));
		item.setIcon(Icons.deriveMenuIcon(Icons.DOCUMENTATION));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.documentation.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Documentation selected");
				URLUtil.openWebpage(URLUtil.DOCS_URL);
			}
		});
		menu.add(item);

		menu.addSeparator();

		////	Bug report
		item = new JMenuItem(trans.get("main.menu.help.bugReport"), KeyEvent.VK_B);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_BUG_REPORT));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.bugReport.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Bug report selected");
				BugReportDialog.showBugReportDialog(parent);
			}
		});
		menu.add(item);

		////	Debug log
		item = new JMenuItem(trans.get("main.menu.help.debugLog"), KeyEvent.VK_D);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_DEBUG_LOG));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.debugLog.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Debug log selected");
				new DebugLogDialog(parent).setVisible(true);
			}
		});
		menu.add(item);

		menu.addSeparator();

		////	License
		item = new JMenuItem(trans.get("main.menu.help.license"), KeyEvent.VK_L);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_LICENSE));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.license.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "License selected");
				new LicenseDialog(parent).setVisible(true);
			}
		});
		menu.add(item);

		////	Check for updates
		item = new JMenuItem(trans.get("main.menu.help.checkForUpdates"), KeyEvent.VK_U);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_CHECK_FOR_UPDATES));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.checkForUpdates.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Check for updates selected");
				UpdateInfoRunner.checkForUpdates(parent);
			}
		});
		menu.add(item);

		////	About
		item = new JMenuItem(trans.get("main.menu.help.about"), KeyEvent.VK_A);
		item.setIcon(Icons.deriveMenuIcon(Icons.HELP_ABOUT));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.about.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "About selected");
				new AboutDialog(parent).setVisible(true);
			}
		});
		menu.add(item);
	}

	public static void addFileCreateAndOpenMenuItems(JMenu fileMenu, Window parent) {
		JMenuItem item;

		//// New
		item = new JMenuItem(trans.get("main.menu.file.new"), KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));
		item.setMnemonic(KeyEvent.VK_N);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.new.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_NEW));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "New... selected");
				newAction();
				if (parent instanceof BasicFrame) {
					((BasicFrame) parent).closeIfReplaceable();
				}
			}
		});
		fileMenu.add(item);

		//// 	Open...
		item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK));
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.open.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_OPEN));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Open... selected");
				openAction(parent);
			}
		});
		fileMenu.add(item);

		//// 	Open Recent
		item = new MRUDesignFileAction(trans.get("main.menu.file.openRecent"), parent);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openRecent.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_OPEN_RECENT));
		fileMenu.add(item);

		//// 	Open example
		BasicFrame basicFrame = parent instanceof BasicFrame ? (BasicFrame) parent : null;
		item = new ExampleDesignFileAction(trans.get("main.menu.file.openExample"), basicFrame);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openExample.desc"));
		item.setIcon(Icons.deriveMenuIcon(Icons.FILE_OPEN_EXAMPLE));
		fileMenu.add(item);

		//// 	Import
		JMenu importSubMenu = new JMenu(trans.get("main.menu.file.import"));
		importSubMenu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.import.desc"));
		importSubMenu.setIcon(Icons.deriveMenuIcon(Icons.FILE_IMPORT));
		fileMenu.add(importSubMenu);

		////// 		Import RASAero
		JMenuItem importRASAero = new JMenuItem(trans.get("main.menu.file.import.RASAero"));
		importRASAero.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.import.RASAero.desc"));
		importRASAero.setIcon(Icons.deriveMenuIcon(Icons.RASAERO));
		importRASAero.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importRASAeroAction(parent);
			}
		});
		importSubMenu.add(importRASAero);

		////// 		Import RockSim
		JMenuItem importRockSim = new JMenuItem(trans.get("main.menu.file.import.RockSim"));
		importRockSim.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.import.RockSim.desc"));
		importRockSim.setIcon(Icons.deriveMenuIcon(Icons.ROCKSIM));
		importRockSim.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importRockSimAction(parent);
			}
		});
		importSubMenu.add(importRockSim);

	}

	public RocketActions getRocketActions() {
		return actions;
	}

	public void doComponentTreePopup(MouseEvent e) {
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private JMenu makeDebugMenu() {
		JMenu menu;
		JMenuItem item;

		/*
		 * This menu is intentionally left untranslated.
		 */

		////	Debug menu
		menu = new JMenu("Debug");

		////	OpenRocket debugging tasks
		menu.getAccessibleContext().setAccessibleDescription("OpenRocket debugging tasks");

		////	What is this menu?
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

		////	Create test rocket
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

				//	//	Get discarded but remaining objects (this also runs System.gc multiple times)
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
					//	//	Explicitly null the strong reference to avoid possibility of invisible references
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

		////	Exhaust memory
		item = new JMenuItem("Exhaust memory");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Exhaust memory selected");
				LinkedList<byte[]> data = new LinkedList<>();
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

		////	Exception here
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
	 * Return the frame that was created at the application's startup.
	 */
	public static BasicFrame getStartupFrame() {
		return startupFrame;
	}

	/**
	 * Set the frame that is created at the application's startup.
	 */
	public static void setStartupFrame(BasicFrame startupFrame) {
		BasicFrame.startupFrame = startupFrame;
	}

	/**
	 * Select the tab on the main pane.
	 *
	 * @param tab	one of {@link #DESIGN_TAB}, {@link #FLIGHT_CONFIGURATION_TAB} or {@link #SIMULATION_TAB}.
	 */
	public void selectTab(int tab) {
		tabbedPane.setSelectedIndex(tab);
	}

	public int getSelectedTab() {
		return tabbedPane.getSelectedIndex();
	}


	/**
	 * Open a custom design file, specified by the file filter.
	 * @param parent parent window to open the file chooser on
	 * @param filter the file filter to use, or null for no filter. E.g. use "RockSim" for RockSim files.
	 */
	public static void openAction(Window parent, FileFilter filter) {
		JFileChooser chooser = new JFileChooser();

		chooser.addChoosableFileFilter(FileHelper.ALL_DESIGNS_FILTER);
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
		int option = chooser.showOpenDialog(parent);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "Decided not to open files, option=" + option);
			return;
		}

		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());

		File[] files = chooser.getSelectedFiles();
		log.info(Markers.USER_MARKER, "Opening files " + Arrays.toString(files));

		for (File file : files) {
			log.info("Opening file: " + file);
			if (open(file, parent) != null) {
				MRUDesignFile opts = MRUDesignFile.getInstance();
				opts.addFile(file.getAbsolutePath());
			}
		}
	}

	/**
	 * Open an OpenRocket file.
	 */
	public static void openAction(Window parent) {
		openAction(parent, FileHelper.OPENROCKET_DESIGN_FILTER);
	}


	/**
	 * Import a RockSim file.
	 * @param parent parent window to open the file chooser on
	 */
	public static void importRockSimAction(Window parent) {
		log.info(Markers.USER_MARKER, "Import RockSim selected");
		openAction(parent, FileHelper.ROCKSIM_DESIGN_FILTER);
	}

	/**
	 * Import a RASAero file.
	 * @param parent parent window to open the file chooser on
	 */
	public static void importRASAeroAction(Window parent) {
		log.info(Markers.USER_MARKER, "Import RASAero selected");
		openAction(parent, FileHelper.RASAERO_DESIGN_FILTER);
	}


	private void closeIfReplaceable() {
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

		////	Open the file
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
	 * @return			the BasicFrame that was created, or null if not created successfully.
	 */
	public static BasicFrame open(File file, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(file);
		BasicFrame frame = open(worker, file.getName(), parent, false);
		if (frame != null) {
			MRUDesignFile.getInstance().addFile(file.getAbsolutePath());
		}
		return frame;
	}


	/**
	 * Open the specified file using the provided worker.
	 *
	 * @param worker	the OpenFileWorker that loads the file.
	 * @param displayName	the file name to display in dialogs.
	 * @param parent
	 * @param openRocketConfigDialog if true, will open the configuration dialog of the rocket.  This is useful for examples.
	 * @return the BasicFrame that was created, or null if not created successfully.
	 */
	private static BasicFrame open(OpenFileWorker worker, String displayName, Window parent, boolean openRocketConfigDialog) {
		////	Open the file in a Swing worker thread
		log.info("Starting OpenFileWorker");
		if (!SwingWorkerDialog.runWorker(parent, "Opening file", "Reading " + displayName + "...", worker)) {
			//	//	User cancelled the operation
			log.info("User cancelled the OpenFileWorker");
			return null;
		}

		////	Handle the document
		final OpenRocketDocument doc;
		try {
			doc = worker.get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof FileNotFoundException) {
				log.warn("File not found", cause);
				JOptionPane.showMessageDialog(parent,
						"File not found: " + displayName,
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return null;
			} else if (cause instanceof RocketLoadException) {
				log.warn("Error loading the file", cause);
				JOptionPane.showMessageDialog(parent,
						"Unable to open file '" + displayName + "': "
								+ cause.getMessage(),
								"Error opening file", JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				throw new BugException("Unknown error when opening file", e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		if (doc == null) {
			throw new BugException("Document loader returned null");
		}

		////	Show warnings
		WarningSet warnings = worker.getRocketLoader().getWarnings();
		if (!warnings.isEmpty()) {
			log.info("Warnings while reading file: " + warnings);
			WarningDialog.showWarnings(parent,
					new Object[] {
							//	//	The following problems were encountered while opening
							trans.get("BasicFrame.WarningDialog.txt1") + " " + displayName + ".",
							//	//	Some design features may not have been loaded correctly.
							trans.get("BasicFrame.WarningDialog.txt2")
			},
					//	//	Warnings while opening file
					trans.get("BasicFrame.WarningDialog.title"), warnings);
		}

		////	Open the frame
		log.debug("Opening new frame with the document");
		BasicFrame frame = new BasicFrame(doc);
		frame.setVisible(true);

		if (parent instanceof BasicFrame) {
			((BasicFrame) parent).closeIfReplaceable();
		}
		if (openRocketConfigDialog) {
			SwingUtilities.invokeLater(() -> ComponentConfigDialog.showDialog(frame, doc, doc.getRocket()));
		}

		return frame;
	}


	/**
	 * Restore focus to this frame after a dialog chain completes.
	 * On some platforms (notably macOS), when a chain of modal dialogs opens and closes
	 * (e.g. file chooser followed by overwrite confirmation), the parent frame may not
	 * automatically regain focus, leaving the application menu bar disabled.
	 * Using invokeLater ensures the focus request happens after all dialog-related events are processed.
	 */
	private void restoreFocus() {
		SwingUtilities.invokeLater(() -> {
			toFront();
			requestFocus();
		});
	}

	/**
	 * "Save" action.  If the design is new, then this is identical to "Save As", with a default file filter for .ork.
	 * If the rocket being edited previously was opened from a .ork file, then it will be saved immediately to the same
	 * file.  But clicking on 'Save' for an existing design file with a RockSim or RASAero file will bring up a confirmation
	 * dialog because it's potentially a destructive write (loss of some fidelity if it's truly an original RockSim/RASAero
	 * generated file).
	 *
	 * @return true if the file was saved, false otherwise
	 */
	private boolean saveAction() {
		try {
			document.fireDocumentSavingEvent(new DocumentChangeEvent(this));
			File file = document.getFile();
			if (file == null || document.getDefaultStorageOptions().getFileType().equals(FileType.ROCKSIM)
					|| document.getDefaultStorageOptions().getFileType().equals(FileType.RASAERO)) {
				log.info("Document does not contain file, opening save as dialog instead");
				return saveAsAction();
			}
			log.info("Saving document to " + file);
			return saveAsOpenRocket(file);
		} finally {
			restoreFocus();
		}
	}

	/**
	 * Opens a file chooser dialog for saving a new file, and returns the selected file.
	 * @param fileType file type to use (e.g. RASAero)
	 * @param selectedComponents list of selected components in the design
	 * @return the file selected from the dialog, or null if no file was selected.
	 */
	private File openFileSaveAsDialog(FileType fileType, List<RocketComponent> selectedComponents) {
		final DesignFileSaveAsFileChooser chooser = DesignFileSaveAsFileChooser.build(document, fileType, selectedComponents);

		int option = chooser.showSaveDialog(BasicFrame.this);

		if (option != JFileChooser.APPROVE_OPTION) {
			log.info(Markers.USER_MARKER, "User decided not to save, option=" + option);
			return null;
		}
		if(chooser.getAccessory() instanceof OptionChooser optionChooser){
			optionChooser.storeOptions(document,prefs);
		}

		File file = chooser.getSelectedFile();
		if (file == null) {
			log.info(Markers.USER_MARKER, "User did not select a file");
			return null;
		}

		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());

		return file;
	}

	/**
	 * Opens a file chooser dialog for saving a new file, and returns the selected file.
	 * @param fileType file type to use (e.g. RASAero)
	 * @return the file selected from the dialog, or null if no file was selected.
	 */
	private File openFileSaveAsDialog(FileType fileType) {
		return openFileSaveAsDialog(fileType, null);
	}


	////	BEGIN RASAero Save/Export Action
	 /**
	 * MODEL "Export as" RASAero file format
	 *
	 *	@return true if the file was saved, false otherwise
	 */


	public boolean exportRASAeroAction() {
		try {
			File file = openFileSaveAsDialog(FileType.RASAERO);
			if (file == null) {
				return false;
			}

			file = FileHelper.forceExtension(file, RASAeroCommonConstants.FILE_EXTENSION);
			if (FileHelper.confirmWrite(file, BasicFrame.this)) {
				boolean result = saveAsRASAero(file);
				if (!result) {
					file.delete();
				}
				return result;
			}
			return false;
		} finally {
			restoreFocus();
		}
	}

	/**
	 * Perform the writing of the design to the given file in RASAero format.
	 * @param file  the chosen file
	 * @return true if the file was written
	 */
	private boolean saveAsRASAero(File file) {
		if (prefs.getShowRASAeroFormatWarning())  {
			// Show RASAero format warning
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new StyledLabel(trans.get("SaveRASAeroWarningDialog.txt1")), "wrap");
			final JCheckBox check = new JCheckBox(trans.get("SaveRASAeroWarningDialog.donotshow"));
			check.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					prefs.setShowRASAeroFormatWarning(!check.isSelected());
				}
			});
			panel.add(check);
			int sel = JOptionPane.showOptionDialog(BasicFrame.this,
					panel,
					"", // title
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, // icon
					null, // options
					null // default option
			);
			if (sel == 1) {
				return false;
			}
		}

		StorageOptions options = new StorageOptions();
		options.setFileType(FileType.RASAERO);
		return saveRASAeroFile(file, options);
	}

	/**
	 * Perform the actual saving of the RASAero file
	 * @param file file to be stored
	 * @param options storage options to use
	 * @return true if the file was written
	 */
	private boolean saveRASAeroFile(File file, StorageOptions options) {
		try {
			ROCKET_SAVER.save(file, document, options);

			WarningSet warnings = ROCKET_SAVER.getWarnings();
			ErrorSet errors = ROCKET_SAVER.getErrors();

			if (!warnings.isEmpty() && errors.isEmpty()) {
				WarningDialog.showWarnings(BasicFrame.this,
						new Object[]{
								//	//	The following problems were encountered while saving
								trans.get("BasicFrame.WarningDialog.saving.txt1") + " '" + file.getName() + "'.",
								//	//	Some design features may not have been exported correctly.
								trans.get("BasicFrame.WarningDialog.saving.txt2")
						},
						////	Warnings while saving file
						trans.get("BasicFrame.WarningDialog.saving.title"),
						warnings);
			} else if (!errors.isEmpty()) {
				ErrorWarningDialog.showErrorsAndWarnings(BasicFrame.this,
						new Object[]{
								//	//	The following problems were encountered while saving
								trans.get("BasicFrame.WarningDialog.saving.txt1") + " '" + file.getName() + "'.",
								//	//	Please correct the errors.
								trans.get("BasicFrame.ErrorWarningDialog.txt1")
						},
						//	//	Errors/Warnings while saving file
						trans.get("BasicFrame.ErrorWarningDialog.saving.title"), errors, warnings);
			}
			// Do not update the save state of the document.
			return errors.isEmpty();
		} catch (IOException e) {
			return false;
		} catch (DecalNotFoundException decex) {
			DecalImage decal = decex.getDecal();
			// Check if the user replaced the source file, if not, just ignore the faulty decal on the next save
			if (!DecalNotFoundDialog.showDialog(null, decex) && decal != null) {
				decal.setIgnored(true);
			}
			return saveRASAeroFile(file, options);	// Re-save
		}
	}
	////	END RASAero Save/Export Action


	////	BEGIN ROCKSIM Save/Export Action
	/**
	* MODEL "Export as" RASAero file format
	*
	* @return true if the file was saved, false otherwise
	*/
	public boolean exportRockSimAction() {
		try {
			File file = openFileSaveAsDialog(FileType.ROCKSIM);
			if (file == null) {
				return false;
			}

			file = FileHelper.forceExtension(file, "rkt");
			if (FileHelper.confirmWrite(file, BasicFrame.this)) {
				return saveAsRockSim(file);
			}
			return false;
		} finally {
			restoreFocus();
		}
	}

	/**
	 * Perform the writing of the design to the given file in RockSim format.
	 *
	 * @param file  the chosen file
	 *
	 * @return true if the file was written
	 */
	private boolean saveAsRockSim(File file) {
		if ( prefs.getShowRockSimFormatWarning() ) {
			// Show RockSim format warning
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
			int sel = JOptionPane.showOptionDialog(BasicFrame.this,
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

		StorageOptions options = new StorageOptions();
		options.setFileType(StorageOptions.FileType.ROCKSIM);
		return saveRockSimFile(file, options);
	}


	/**
	 * Perform the actual saving of the RockSim file
	 * @param file file to be stored
	 * @param options storage options to use
	 * @return true if the file was written
	 */
	private boolean saveRockSimFile(File file, StorageOptions options) {
		try {
			ROCKET_SAVER.save(file, document, options);
			// Do not update the save state of the document.
			return true;
		} catch (IOException e) {
			return false;
		} catch (DecalNotFoundException decex) {
			DecalImage decal = decex.getDecal();
			// Check if the user replaced the source file, if not, just ignore the faulty decal on the next save
			if (!DecalNotFoundDialog.showDialog(null, decex) && decal != null) {
				decal.setIgnored(true);
			}
			return saveRockSimFile(file, options);	// Re-save
		}
	}

	////	END ROCKSIM Save/Export Action


	////	BEGIN WAVEFRONT OBJ Save/Export Action
	/**
	 * MODEL "Export as" Wavefront OBJ file format
	 *
	 * @return true if the file was saved, false otherwise
	 */
	public boolean exportWavefrontOBJAction() {
		try {
			File file = openFileSaveAsDialog(FileType.WAVEFRONT_OBJ, getSelectedComponents());
			if (file == null) {
				return false;
			}

			file = FileHelper.forceExtension(file, "obj");
			OBJExportOptions options = document.getDefaultOBJOptions();
			boolean isExportAsSeparateFiles = options.isExportAsSeparateFiles();
			if (isExportAsSeparateFiles || FileHelper.confirmWrite(file, BasicFrame.this)) {		// No overwrite warning for separate files
				return saveAsWavefrontOBJ(file);
			}
			return false;
		} finally {
			restoreFocus();
		}
	}

	private boolean saveAsWavefrontOBJ(File file) {
		OBJExportOptions options = document.getDefaultOBJOptions();
		return saveWavefrontOBJFile(file, options);
	}

	/**
	 * Perform the actual saving of the Wavefront OBJ file
	 * @param file file to be stored
	 * @param options OBJ export options to use
	 * @return true if the file was written
	 */
	private boolean saveWavefrontOBJFile(File file, OBJExportOptions options) {
		WarningSet warnings = new WarningSet();
		OBJExporterFactory exporter = new OBJExporterFactory(getSelectedComponents(), rocket.getSelectedConfiguration(),
				file, options, warnings);
		exporter.doExport();

		// Show warning dialog
		if (!warnings.isEmpty()) {
			WarningDialog.showWarnings(this,
					////	The following problems were encountered while saving
					trans.get("BasicFrame.WarningDialog.saving.txt1") + " '" + file.getName() + "'.",
					////	Warnings while saving file
					trans.get("BasicFrame.WarningDialog.saving.title"),
					warnings);
		}

		return true;
	}

	/**
	 * Export SVG profiles. If components are provided, exports only those components;
	 * otherwise exports all exportable components from the document.
	 *
	 * @param components Components to export, or null to export all from document
	 */
	private void exportSvgProfilesAction(List<RocketComponent> components) {
		try {
			// Get currently selected components from design (if components parameter is null)
			List<RocketComponent> initiallySelected = components;
			if (initiallySelected == null) {
				initiallySelected = getSelectedComponents();
				if (initiallySelected == null) {
					initiallySelected = new ArrayList<>();
				}
			}

			// Show SVG options dialog first
			SvgOptionsDialog optionsDialog = new SvgOptionsDialog(BasicFrame.this, document, initiallySelected);
			optionsDialog.setFromPreferences(prefs);
			if (!optionsDialog.showDialog()) {
				return; // User cancelled
			}

			// Get the selected tab to determine export type
			int selectedTab = optionsDialog.getSelectedTab();

			// Get options from dialog (includes spacing)
			SVGExportOptions options = optionsDialog.getExportOptions();

			// Now show file chooser
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(FileHelper.SVG_FILTER);

			SwingPreferences swingPrefs = (SwingPreferences) Application.getPreferences();
			File defaultDir = swingPrefs.getDefaultDirectory();
			if (defaultDir != null) {
				chooser.setCurrentDirectory(defaultDir);
			}

			// Determine default filename based on selected tab
			String defaultName;
			String fileSuffix;
			if (selectedTab == SvgOptionsDialog.COMPONENTS_TAB) {
				// Components tab
				if (components != null && !components.isEmpty()) {
					if (components.size() == 1) {
						defaultName = components.get(0).getName();
						if (defaultName == null || defaultName.isBlank()) {
							defaultName = components.get(0).getComponentName();
						}
					} else {
						defaultName = "components";
					}
				} else {
					defaultName = document.getRocket().getName();
					if (defaultName == null || defaultName.isBlank()) {
						defaultName = "rocket";
					}
				}
				fileSuffix = "-profile.svg";
			} else {
				// Fin Guides tab
				defaultName = document.getRocket().getName();
				if (defaultName == null || defaultName.isBlank()) {
					defaultName = "rocket";
				}
				fileSuffix = "-finguides.svg";
			}
			File parentDir = defaultDir != null ? defaultDir : new File(System.getProperty("user.home", "."));
			chooser.setSelectedFile(new File(parentDir, defaultName + fileSuffix));

			if (chooser.showSaveDialog(BasicFrame.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File target = FileHelper.forceExtension(chooser.getSelectedFile(), "svg");
			if (!FileHelper.confirmWrite(target, BasicFrame.this)) {
				return;
			}

			swingPrefs.setDefaultDirectory(chooser.getCurrentDirectory());

			// Save SVG preferences
			prefs.setSVGStrokeColor(optionsDialog.getStrokeColor());
			prefs.setSVGStrokeWidth(optionsDialog.getStrokeWidth());
			prefs.setSVGDrawCrosshair(optionsDialog.isDrawCrosshair());
			prefs.setSVGCrosshairColor(optionsDialog.getCrosshairColor());
			prefs.setSVGCrosshairSize(optionsDialog.getCrosshairSize());
			prefs.setSVGShowLabels(optionsDialog.isShowLabels());
			prefs.setSVGLabelColor(optionsDialog.getLabelColor());

			try {
				if (selectedTab == SvgOptionsDialog.COMPONENTS_TAB) {
					// Export components
					List<RocketComponent> selectedComponents = optionsDialog.getSelectedComponents();
					if (!selectedComponents.isEmpty()) {
						new SVGRocketPartsExporter().export(selectedComponents, target, options);
					} else {
						new SVGRocketPartsExporter().export(document, target, options);
					}
					log.info(Markers.USER_MARKER, "Exported SVG profiles to {}", target.getAbsolutePath());
				}
				// TODO: other tabs here (e.g. fin guides)
			} catch (UnsupportedOperationException ex) {
				log.warn("Fin guide export not implemented", ex);
				JOptionPane.showMessageDialog(BasicFrame.this,
						trans.get("SVGOptionPanel.finGuides.notImplemented"),
						trans.get("SVGOptionPanel.finGuides.notImplemented.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IllegalStateException noParts) {
				JOptionPane.showMessageDialog(BasicFrame.this,
						trans.get("main.menu.file.exportAs.SVGProfiles.empty"),
						trans.get("main.menu.file.exportAs.SVGProfiles.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				log.warn("Failed to export SVG", ex);
				JOptionPane.showMessageDialog(BasicFrame.this,
						String.format(trans.get("main.menu.file.exportAs.SVGProfiles.error"), ex.getMessage()),
						trans.get("main.menu.file.exportAs.SVGProfiles.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		} finally {
			restoreFocus();
		}
	}

	/**
	 * Export all exportable components from the document as SVG profiles.
	 */
	private void exportSvgProfilesAction() {
		exportSvgProfilesAction(null);
	}

	/**
	 * Export selected components as SVG profiles.
	 */
	public void exportSVGAction() {
		List<RocketComponent> selectedComponents = getSelectedComponents();
		if (selectedComponents == null || selectedComponents.isEmpty()) {
			return;
		}
		exportSvgProfilesAction(selectedComponents);
	}


	/**
	 * "Save As" action.
	 *
	 * @return true if the file was saved, false otherwise
	 */
	private boolean saveAsAction() {
		try {
			// Open dialog for saving rocket info
			showSaveRocketInfoDialog();

			File file = openFileSaveAsDialog(FileType.OPENROCKET);
			if (file == null) {
				return false;
			}

			file = FileHelper.forceExtension(file, "ork");
			boolean result = FileHelper.confirmWrite(file, BasicFrame.this) && saveAsOpenRocket(file);
			if (result) {
				MRUDesignFile opts = MRUDesignFile.getInstance();
				opts.addFile(file.getAbsolutePath());
			}
			return result;
		} finally {
			restoreFocus();
		}
	}

	private void showSaveRocketInfoDialog() {
		if (!prefs.isShowSaveRocketInfo()) {
			return;
		}

		// Select the rocket in the component tree to indicate to users that they can edit the rocket info by editing the rocket
		setSelectedComponent(rocket);

		// Open the save rocket info
		JDialog dialog = new JDialog();
		SaveDesignInfoPanel panel = new SaveDesignInfoPanel(document, rocket, dialog);
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setTitle(trans.get("BasicFrame.lbl.SaveRocketInfo"));
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
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

		if (!StorageOptionChooser.verifyStorageOptions(document, BasicFrame.this)) {
			// User cancelled the dialog
			log.info(Markers.USER_MARKER, "User cancelled saving in storage options dialog");
			return false;
		}

		// Generate file preview image
		byte[] previewImage = generatePreviewImage();
		if (previewImage != null) {
			document.getDefaultStorageOptions().setPreviewImage(previewImage);
		} else {
			document.getDefaultStorageOptions().clearPreviewImage();
		}

		document.getDefaultStorageOptions().setFileType(FileType.OPENROCKET);
		SaveFileWorker worker = new SaveFileWorker(document, file, ROCKET_SAVER);

		if (!SwingWorkerDialog.runWorker(BasicFrame.this, "Saving file",
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
				JOptionPane.showMessageDialog(BasicFrame.this, new String[] {
						"An I/O error occurred while saving:",
						e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else if (cause instanceof DecalNotFoundException) {
				DecalNotFoundException decex = (DecalNotFoundException) cause;
				DecalImage decal = decex.getDecal();
				// Check if the user replaced the source file, if not, just ignore the faulty decal on the next save
				if (!DecalNotFoundDialog.showDialog(null, decex) && decal != null) {
					decal.setIgnored(true);
				}
				return saveAsOpenRocket(file);	// Re-save

			} else {
				Reflection.handleWrappedException(e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		return false;
	}

	/**
	 * Generate a file preview image for saving in the design file.
	 * @return the PNG image data, or null if no preview could be generated
	 */
	private byte[] generatePreviewImage() {
		if (rocketpanel == null) {
			return null;
		}

		String viewPreference = prefs.getString(ApplicationPreferences.FILE_PREVIEW_VIEW_TYPE,
				RocketPanel.VIEW_TYPE.SideView.name());
		RocketPanel.VIEW_TYPE previewView = RocketPanel.VIEW_TYPE.fromName(viewPreference);
		if (previewView == null) {
			previewView = RocketPanel.VIEW_TYPE.SideView;
		}

		byte[] previewBytes = rocketpanel.createPreviewPng(previewView, PREVIEW_WIDTH, PREVIEW_MIN_HEIGHT, PREVIEW_MAX_HEIGHT);
		if (previewBytes == null || previewBytes.length == 0) {
			return null;
		}
		return previewBytes;
	}


	private boolean closeAction() {
		if (!document.isSaved()) {
			log.info("Confirming whether to save the design");
			ComponentConfigDialog.disposeDialog();
			int result = JOptionPane.showConfirmDialog(BasicFrame.this,
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
		BasicFrame.this.dispose();

		ComponentConfigDialog.disposeDialog();
		ComponentAnalysisDialog.hideDialog();

		frames.remove(BasicFrame.this);
		if (frames.isEmpty()) {
			// Don't quit the application on macOS, but keep the application open
			if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
				DummyFrameMenuOSX.createDummyDialog();
			} else {
				log.info("Last frame closed, exiting");
				System.exit(0);
			}
		}
		return true;
	}

	public void exportDecalAction() {
		ExportDecalAction.export(BasicFrame.this, document);
	}


	public void printAction() {
		double rotation = rocketpanel.getFigure().getRotation();
		new PrintDialog(BasicFrame.this, document, rotation).setVisible(true);
	}

	/**
	 * Opens a new design file or the last design file, if set in the preferences.
	 * Can be used for reopening the application or opening it the first time.
	 * @return the BasicFrame that was created
	 */
	public static BasicFrame reopen() {
		if (!Application.getPreferences().isAutoOpenLastDesignOnStartupEnabled()) {
			return BasicFrame.newAction();
		} else {
			String lastFile = MRUDesignFile.getInstance().getLastEditedDesignFile();
			if (lastFile != null) {
				log.info("Opening last design file: " + lastFile);
				BasicFrame frame = BasicFrame.open(new File(lastFile), null);
				if (frame == null) {
					MRUDesignFile.getInstance().removeFile(lastFile);
					return BasicFrame.newAction();
				}
				else {
					MRUDesignFile.getInstance().addFile(lastFile);
					return frame;
				}
			}
			else {
				return BasicFrame.newAction();
			}
		}
	}


	/**
	 * Open a new design window with a basic rocket+stage.
	 * @return the BasicFrame that was created
	 */
	public static BasicFrame newAction() {
		log.info("New action initiated");

		OpenRocketDocument doc = OpenRocketDocumentFactory.createNewRocket();

		BasicFrame frame = new BasicFrame(doc);
		frame.replaceable = true;
		frame.setVisible(true);
		return frame;
	}


	/**
	 * Quit the application.  Confirms saving unsaved designs.  The action of File->Quit.
	 */
	public static void quitAction() {
		if (quitCalled) return;
		quitCalled = true;
		log.info("Quit action initiated");
		for (int i = frames.size() - 1; i >= 0; i--) {
			log.debug("Closing frame " + frames.get(i));
			if (!frames.get(i).closeAction()) {
				// Close canceled
				log.info("Quit was cancelled");
				quitCalled = false;
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
	 * Return all BasicFrame instances
	 */
	public static List<BasicFrame> getAllFrames() {
		return frames;
	}

	/**
	 * Checks whether all the BasicFrames are closed.
	 * @return true if all the BasicFrames are closed, false if not
	 */
	public static boolean isFramesEmpty() {
		return frames.isEmpty();
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

	public void setSelectedComponent(RocketComponent component) {
		this.selectionModel.setSelectedComponent(component);
	}

	public void setSelectedComponents(List<RocketComponent> components) {
		this.selectionModel.setSelectedComponents(components);
	}


	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabSource = (JTabbedPane) e.getSource();
		int tab = tabSource.getSelectedIndex();
		if (previousTab == SIMULATION_TAB) {
			simulationPanel.updatePreviousSelection();
		}
		previousTab = tab;
		switch (tab) {
			case DESIGN_TAB:
				designPanel.takeTheSpotlight();
				break;
			case FLIGHT_CONFIGURATION_TAB:
				flightConfigurationPanel.takeTheSpotlight();
				break;
			case SIMULATION_TAB:
				simulationPanel.takeTheSpotlight();
				simulationPanel.activating();
				break;
		}
	}

	public void open() {
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
