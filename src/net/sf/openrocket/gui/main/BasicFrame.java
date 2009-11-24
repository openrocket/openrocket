package net.sf.openrocket.gui.main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.OpenRocketSaver;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.RocketLoader;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.gui.StorageOptionChooser;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.BugReportDialog;
import net.sf.openrocket.gui.dialogs.ComponentAnalysisDialog;
import net.sf.openrocket.gui.dialogs.ExampleDesignDialog;
import net.sf.openrocket.gui.dialogs.LicenseDialog;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.dialogs.WarningDialog;
import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Icons;
import net.sf.openrocket.util.OpenFileWorker;
import net.sf.openrocket.util.Prefs;
import net.sf.openrocket.util.SaveFileWorker;
import net.sf.openrocket.util.TestRockets;

public class BasicFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * The RocketLoader instance used for loading all rocket designs.
	 */
	private static final RocketLoader ROCKET_LOADER = new GeneralRocketLoader();
	
	private static final RocketSaver ROCKET_SAVER = new OpenRocketSaver();

	
	/**
	 * File filter for filtering only rocket designs.
	 */
	private static final FileFilter ROCKET_DESIGN_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return "OpenRocket designs (*.ork)";
		}
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName().toLowerCase();
			return name.endsWith(".ork") || name.endsWith(".ork.gz");
		}
    };
    
    
    
    public static final int COMPONENT_TAB = 0;
    public static final int SIMULATION_TAB = 1;
    

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
	
	
	
	/**
	 * Sole constructor.  Creates a new frame based on the supplied document
	 * and adds it to the current frames list.
	 * 
	 * @param document	the document to show.
	 */
	public BasicFrame(OpenRocketDocument document) {

		this.document = document;
		this.rocket = document.getRocket();
		this.rocket.getDefaultConfiguration().setAllStages();
		
		
		// Set replaceable flag to false at first modification
		rocket.addComponentChangeListener(new ComponentChangeListener() {
			public void componentChanged(ComponentChangeEvent e) {
				replaceable = false;
				BasicFrame.this.rocket.removeComponentChangeListener(this);
			}
		});
		
		
		// Create the component tree selection model that will be used
		componentSelectionModel = new DefaultTreeSelectionModel();
		componentSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Obtain the simulation selection model that will be used
		SimulationPanel simulationPanel = new SimulationPanel(document);
		simulationSelectionModel = simulationPanel.getSimulationListSelectionModel();
		
		// Combine into a DocumentSelectionModel
		selectionModel = new DocumentSelectionModel(document);
		selectionModel.attachComponentTreeSelectionModel(componentSelectionModel);
		selectionModel.attachSimulationListSelectionModel(simulationSelectionModel);
		
		
		actions = new RocketActions(document, selectionModel, this);
		
		
		// The main vertical split pane		
		JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		vertical.setResizeWeight(0.5);
		this.add(vertical);

		
		// The top tabbed pane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Rocket design", null, designTab());
		tabbedPane.addTab("Flight simulations", null, simulationPanel);
		
		vertical.setTopComponent(tabbedPane);
		


		//  Bottom segment, rocket figure
		
		rocketpanel = new RocketPanel(document);
		vertical.setBottomComponent(rocketpanel);

		rocketpanel.setSelectionModel(tree.getSelectionModel());

					
		createMenu();
		
		
		rocket.addComponentChangeListener(new ComponentChangeListener() {
			public void componentChanged(ComponentChangeEvent e) {
				setTitle();
			}
		});
		
		setTitle();
		this.pack();

		Dimension size = Prefs.getWindowSize(this.getClass());
		if (size == null) {
			size = Toolkit.getDefaultToolkit().getScreenSize();
			size.width = size.width*9/10;
			size.height = size.height*9/10;
		}
		this.setSize(size);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Prefs.setWindowSize(BasicFrame.this.getClass(), BasicFrame.this.getSize());
			}
		});
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
	}
	
	
	/**
	 * Construct the "Rocket design" tab.  This contains a horizontal split pane
	 * with the left component the design tree and the right component buttons
	 * for adding components.
	 */
	private JComponent designTab() {
		JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true);
		horizontal.setResizeWeight(0.5);


		//  Upper-left segment, component tree

		JPanel panel = new JPanel(new MigLayout("fill, flowy","","[grow]"));

		tree = new ComponentTree(rocket);
		tree.setSelectionModel(componentSelectionModel);

		// Remove JTree key events that interfere with menu accelerators
		InputMap im = SwingUtilities.getUIInputMap(tree, JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), null);


		
		// Double-click opens config dialog
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if((e.getClickCount() == 2) && !ComponentConfigDialog.isDialogVisible()) {
						// Double-click
						RocketComponent c = (RocketComponent)selPath.getLastPathComponent();
						ComponentConfigDialog.showDialog(BasicFrame.this, 
								BasicFrame.this.document, c);
					}
				}
			}
		};
		tree.addMouseListener(ml);

		// Update dialog when selection is changed
		componentSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				// Scroll tree to the selected item
				TreePath path = componentSelectionModel.getSelectionPath();
				if (path == null)
					return;
				tree.scrollPathToVisible(path);
				
				if (!ComponentConfigDialog.isDialogVisible())
					return;
				RocketComponent c = (RocketComponent)path.getLastPathComponent();
				ComponentConfigDialog.showDialog(BasicFrame.this, 
						BasicFrame.this.document, c);
			}
		});

		// Place tree inside scroll pane
		JScrollPane scroll = new JScrollPane(tree);
		panel.add(scroll,"spany, grow, wrap");
		
		
		// Buttons
		JButton button = new JButton(actions.getMoveUpAction());
		panel.add(button,"sizegroup buttons, aligny 65%");
		
		button = new JButton(actions.getMoveDownAction());
		panel.add(button,"sizegroup buttons, aligny 0%");
		
		button = new JButton(actions.getEditAction());
		panel.add(button, "sizegroup buttons");
		
		button = new JButton(actions.getNewStageAction());
		panel.add(button,"sizegroup buttons");
		
		button = new JButton(actions.getDeleteAction());
		button.setIcon(null);
		button.setMnemonic(0);
		panel.add(button,"sizegroup buttons");

		horizontal.setLeftComponent(panel);


		//  Upper-right segment, component addition buttons

		panel = new JPanel(new MigLayout("fill, insets 0","[0::]"));

		scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setViewportView(new ComponentAddButtons(document, componentSelectionModel,
				scroll.getViewport()));
		scroll.setBorder(null);
		scroll.setViewportBorder(null);

		TitledBorder border = new TitledBorder("Add new component");
		border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
		scroll.setBorder(border);

		panel.add(scroll,"grow");

		horizontal.setRightComponent(panel);

		return horizontal;
	}
	
	
	
	/**
	 * Creates the menu for the window.
	 */
	private void createMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		JMenuItem item;
		
		////  File
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File-handling related tasks");
		menubar.add(menu);
		
		item = new JMenuItem("New",KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_N);
		item.getAccessibleContext().setAccessibleDescription("Create a new rocket design");
		item.setIcon(Icons.FILE_NEW);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newAction();
				if (replaceable)
					closeAction();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Open...",KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		item.getAccessibleContext().setAccessibleDescription("Open a rocket design");
		item.setIcon(Icons.FILE_OPEN);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Open example...");
		item.getAccessibleContext().setAccessibleDescription("Open an example rocket design");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		item.setIcon(Icons.FILE_OPEN_EXAMPLE);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				URL[] urls = ExampleDesignDialog.selectExampleDesigns(BasicFrame.this);
				if (urls != null) {
					for (URL u: urls) {
						open(u, BasicFrame.this);
					}
				}
			}
		});
		menu.add(item);
		
		menu.addSeparator();
		
		item = new JMenuItem("Save",KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		item.getAccessibleContext().setAccessibleDescription("Save the current rocket design");
		item.setIcon(Icons.FILE_SAVE);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Save as...",KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		item.getAccessibleContext().setAccessibleDescription("Save the current rocket design "+
				"to a new file");
		item.setIcon(Icons.FILE_SAVE_AS);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction();
			}
		});
		menu.add(item);
		
//		menu.addSeparator();
		menu.add(new JSeparator());
		
		item = new JMenuItem("Close",KeyEvent.VK_C);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		item.getAccessibleContext().setAccessibleDescription("Close the current rocket design");
		item.setIcon(Icons.FILE_CLOSE);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}
		});
		menu.add(item);
		
		menu.addSeparator();

		item = new JMenuItem("Quit",KeyEvent.VK_Q);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		item.getAccessibleContext().setAccessibleDescription("Quit the program");
		item.setIcon(Icons.FILE_QUIT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quitAction();
			}
		});
		menu.add(item);
		
		

		////  Edit
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.getAccessibleContext().setAccessibleDescription("Rocket editing");
		menubar.add(menu);
		
		
		Action action = document.getUndoAction();
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_U);
		item.getAccessibleContext().setAccessibleDescription("Undo the previous operation");

		menu.add(item);

		action = document.getRedoAction();
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_R);
		item.getAccessibleContext().setAccessibleDescription("Redo the previously undone " +
				"operation");
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
		
		item = new JMenuItem("Preferences");
		item.setIcon(Icons.PREFERENCES);
		item.getAccessibleContext().setAccessibleDescription("Setup the application "+
				"preferences");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog.showPreferences();
			}
		});
		menu.add(item);
	
		


		////  Analyze
		menu = new JMenu("Analyze");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Analyzing the rocket");
		menubar.add(menu);
		
		item = new JMenuItem("Component analysis",KeyEvent.VK_C);
		item.getAccessibleContext().setAccessibleDescription("Analyze the rocket components " +
				"separately");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComponentAnalysisDialog.showDialog(rocketpanel);
			}
		});
		menu.add(item);
		
		
		////  Debug
		// (shown if openrocket.debug.menu is defined)
		if (System.getProperty("openrocket.debug.menu") != null) {
			menubar.add(makeDebugMenu());
		}

		
		
		////  Help
		
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription("Information about OpenRocket");
		menubar.add(menu);
		
		
		
		item = new JMenuItem("License",KeyEvent.VK_L);
		item.getAccessibleContext().setAccessibleDescription("OpenRocket license information");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new LicenseDialog(BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Bug report",KeyEvent.VK_B);
		item.getAccessibleContext().setAccessibleDescription("Information about reporting " +
				"bugs in OpenRocket");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				new BugDialog(BasicFrame.this).setVisible(true);
				BugReportDialog.showBugReportDialog(BasicFrame.this);
			}
		});
		menu.add(item);
		
		item = new JMenuItem("About",KeyEvent.VK_A);
		item.getAccessibleContext().setAccessibleDescription("About OpenRocket");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(BasicFrame.this).setVisible(true);
			}
		});
		menu.add(item);
		
		
		this.setJMenuBar(menubar);
	}
	
	
	private JMenu makeDebugMenu() {
		JMenu menu;
		JMenuItem item;
		
		////  Debug menu
		menu = new JMenu("Debug");
		menu.getAccessibleContext().setAccessibleDescription("OpenRocket debugging tasks");
		
		item = new JMenuItem("What is this menu?");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		
		item = new JMenuItem("Create test rocket");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
				
				OpenRocketDocument doc = new OpenRocketDocument(r);
				doc.setSaved(true);
				BasicFrame frame = new BasicFrame(doc);
				frame.setVisible(true);
			}
		});
		menu.add(item);
		
		menu.addSeparator();
		
		item = new JMenuItem("Exception here");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				throw new RuntimeException("Testing exception from menu action listener");
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Exception from EDT");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					@Override
					public void run() {
						throw new RuntimeException("Testing exception from " +
								"newly created thread");
					}
				}.start();
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
	    chooser.setFileFilter(ROCKET_DESIGN_FILTER);
	    chooser.setMultiSelectionEnabled(true);
	    chooser.setCurrentDirectory(Prefs.getDefaultDirectory());
	    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
	    	return;
	    
	    Prefs.setDefaultDirectory(chooser.getCurrentDirectory());

	    File[] files = chooser.getSelectedFiles();
	    
	    for (File file: files) {
	    	System.out.println("Opening file: " + file);
	    	if (open(file, this)) {
	    		
	    		// Close previous window if replacing
	    		if (replaceable && document.isSaved()) {
	    			closeAction();
	    			replaceable = false;
	    		}
	    	}
	    }
	}
	
	
	
	
	private static boolean open(URL url, BasicFrame parent) {
		String filename = null;
		
		// Try using URI.getPath();
		try {
			URI uri = url.toURI();
			filename = uri.getPath();
		} catch (URISyntaxException ignore) { }

		// Try URL-decoding the URL
		if (filename == null) {
			try {
				filename = URLDecoder.decode(url.toString(), "UTF-8");
			} catch (UnsupportedEncodingException ignore) { }
		}
		
		// Last resort
		if (filename == null) {
			filename = "";
		}
		
		// Remove path from filename
		if (filename.lastIndexOf('/') >= 0) {
			filename = filename.substring(filename.lastIndexOf('/')+1);
		}
		
		try {
			InputStream is = url.openStream();
			if (open(is, filename, parent)) {
	    		// Close previous window if replacing
	    		if (parent.replaceable && parent.document.isSaved()) {
	    			parent.closeAction();
	    			parent.replaceable = false;
	    		}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, 
					"An error occurred while opening the file " + filename,
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}
		
		return false;
	}
	
	
	/**
	 * Open the specified file from an InputStream in a new design frame.  If an error
	 * occurs, an error dialog is shown and <code>false</code> is returned.
	 * 
	 * @param stream	the stream to load from.
	 * @param filename	the file name to display in dialogs (not set to the document).
	 * @param parent	the parent component for which a progress dialog is opened.
	 * @return			whether the file was successfully loaded and opened.
	 */
	private static boolean open(InputStream stream, String filename, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(stream, ROCKET_LOADER);
		return open(worker, filename, null, parent);
	}
	

	/**
	 * Open the specified file in a new design frame.  If an error occurs, an error
	 * dialog is shown and <code>false</code> is returned.
	 * 
	 * @param file		the file to open.
	 * @param parent	the parent component for which a progress dialog is opened.
	 * @return			whether the file was successfully loaded and opened.
	 */
	private static boolean open(File file, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(file, ROCKET_LOADER);
		return open(worker, file.getName(), file, parent);
	}
	

	/**
	 * Open the specified file using the provided worker.
	 * 
	 * @param worker	the OpenFileWorker that loads the file.
	 * @param filename	the file name to display in dialogs.
	 * @param file		the File to set the document to (may be null).
	 * @param parent
	 * @return
	 */
	private static boolean open(OpenFileWorker worker, String filename, File file, 
			Window parent) {

		// Open the file in a Swing worker thread
		if (!SwingWorkerDialog.runWorker(parent, "Opening file", 
				"Reading " + filename + "...", worker)) {

			// User cancelled the operation
			return false;
		}

		
		// Handle the document
		OpenRocketDocument doc = null;
		try {

			doc = worker.get();

		} catch (ExecutionException e) {

			Throwable cause = e.getCause();

			if (cause instanceof FileNotFoundException) {

				JOptionPane.showMessageDialog(parent, 
						"File not found: " + filename,
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else if (cause instanceof RocketLoadException) {

				JOptionPane.showMessageDialog(parent, 
						"Unable to open file '" + filename +"': " 
						+ cause.getMessage(),
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else {

				throw new RuntimeException("Unknown error when opening file", e);

			}

		} catch (InterruptedException e) {
			throw new RuntimeException("EDT was interrupted", e);
		}
		
		if (doc == null) {
			throw new RuntimeException("BUG: Document loader returned null");
		}
		
		
	    // Show warnings
		WarningSet warnings = worker.getRocketLoader().getWarnings();
		if (!warnings.isEmpty()) {
			WarningDialog.showWarnings(parent,
					new Object[] {
					"The following problems were encountered while opening " + filename + ".",
					"Some design features may not have been loaded correctly."
					},
					"Warnings while opening file", warnings);
		}
		
	    
	    // Set document state
	    doc.setFile(file);
	    doc.setSaved(true);

	    // Open the frame
	    BasicFrame frame = new BasicFrame(doc);
	    frame.setVisible(true);

	    return true;
	}
	
	
	
	
	
	
	
	
	
	private boolean saveAction() {
		File file = document.getFile();
		if (file==null) {
			return saveAsAction();
		} else {
			return saveAs(file);
		}
	}
	
	
	private boolean saveAsAction() {
		File file = null;
		while (file == null) {
			StorageOptionChooser storageChooser = 
				new StorageOptionChooser(document, document.getDefaultStorageOptions());
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(ROCKET_DESIGN_FILTER);
			chooser.setCurrentDirectory(Prefs.getDefaultDirectory());
			chooser.setAccessory(storageChooser);
			if (document.getFile() != null)
				chooser.setSelectedFile(document.getFile());
			
			if (chooser.showSaveDialog(BasicFrame.this) != JFileChooser.APPROVE_OPTION)
				return false;
			
			file = chooser.getSelectedFile();
			if (file == null)
				return false;

			Prefs.setDefaultDirectory(chooser.getCurrentDirectory());
			storageChooser.storeOptions(document.getDefaultStorageOptions());
			
			if (file.getName().indexOf('.') < 0) {
				String name = file.getAbsolutePath();
				name = name + ".ork";
				file = new File(name);
			}
			
			if (file.exists()) {
				int result = JOptionPane.showConfirmDialog(this, 
						"File '"+file.getName()+"' exists.  Do you want to overwrite it?", 
						"File exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result != JOptionPane.YES_OPTION)
					return false;
			}
		}
	    saveAs(file);
	    return true;
	}
	
	
	private boolean saveAs(File file) {
	    System.out.println("Saving to file: " + file.getName());
	    boolean saved = false;
	    
	    if (!StorageOptionChooser.verifyStorageOptions(document, this)) {
	    	// User cancelled the dialog
	    	return false;
	    }


	    SaveFileWorker worker = new SaveFileWorker(document, file, ROCKET_SAVER);

	    if (!SwingWorkerDialog.runWorker(this, "Saving file", 
	    		"Writing " + file.getName() + "...", worker)) {
	    	
	    	// User cancelled the save
	    	file.delete();
	    	return false;
	    }
	    
	    try {
			worker.get();
			document.setFile(file);
			document.setSaved(true);
			saved = true;
		    setTitle();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof IOException) {
		    	JOptionPane.showMessageDialog(this, new String[] { 
		    			"An I/O error occurred while saving:",
		    			e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
		    	return false;
			} else {
				throw new RuntimeException("Unknown error when saving file", e);
			}
			
		} catch (InterruptedException e) {
			throw new RuntimeException("EDT was interrupted", e);
		}
	    
	    return saved;
	}
	
	
	private boolean closeAction() {
		if (!document.isSaved()) {
			ComponentConfigDialog.hideDialog();
			int result = JOptionPane.showConfirmDialog(this, 
					"Design '"+rocket.getName()+"' has not been saved.  " +
							"Do you want to save it?", 
					"Design not saved", JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				// Save
				if (!saveAction())
					return false;  // If save was interrupted
			} else if (result == JOptionPane.NO_OPTION) {
				// Don't save: No-op
			} else {
				// Cancel or close
				return false;
			}
		}
		
		// Rocket has been saved or discarded
		this.dispose();

		// TODO: LOW: Close only dialogs that have this frame as their parent
		ComponentConfigDialog.hideDialog();
		ComponentAnalysisDialog.hideDialog();
		
		frames.remove(this);
		if (frames.isEmpty())
			System.exit(0);
		return true;
	}
	
	
	/**
	 * Closes this frame if it is replaceable.
	 */
	public void closeIfReplaceable() {
		if (this.replaceable && document.isSaved()) {
			closeAction();
		}
	}
	
	/**
	 * Open a new design window with a basic rocket+stage.
	 */
	public static void newAction() {
		Rocket rocket = new Rocket();
		Stage stage = new Stage();
		stage.setName("Sustainer");
		rocket.addChild(stage);
		OpenRocketDocument doc = new OpenRocketDocument(rocket);
		doc.setSaved(true);
		
		BasicFrame frame = new BasicFrame(doc);
		frame.replaceable = true;
		frame.setVisible(true);
		ComponentConfigDialog.showDialog(frame, doc, rocket);
	}
	
	/**
	 * Quit the application.  Confirms saving unsaved designs.  The action of File->Quit.
	 */
	public static void quitAction() {
		for (int i=frames.size()-1; i>=0; i--) {
			if (!frames.get(i).closeAction()) {
				// Close canceled
				return;
			}
		}
		// Should not be reached, but just in case
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
		if (file!=null) {
			title = title + " ("+file.getName()+")";
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
		for (BasicFrame f: frames) {
			if (f.rocket == rocket)
				return f;
		}
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
		for (BasicFrame f: frames) {
			if (f.rocket == rocket)
				return f.document;
		}
		return null;
	}
	
	
	public static void main(final String[] args) {
		
		// Run the actual startup method in the EDT since it can use progress dialogs etc.
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					runMain(args);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void runMain(String[] args) {
		
		// Start update info fetching
		final UpdateInfoRetriever updateInfo;
		if (Prefs.getCheckUpdates()) {
			updateInfo = new UpdateInfoRetriever();
			updateInfo.start();
		} else {
			updateInfo = null;
		}
		
		
		
		/*
		 * Set the look-and-feel.  On Linux, Motif/Metal is sometimes incorrectly used 
		 * which is butt-ugly, so if the system l&f is Motif/Metal, we search for a few
		 * other alternatives.
		 */
		try {
			// Set system L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Check whether we have an ugly L&F
			LookAndFeel laf = UIManager.getLookAndFeel();
			if (laf == null ||
					laf.getName().matches(".*[mM][oO][tT][iI][fF].*") ||
					laf.getName().matches(".*[mM][eE][tT][aA][lL].*")) {
				
				// Search for better LAF
				UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
				String lafNames[] = {
						".*[gG][tT][kK].*",
						".*[wW][iI][nN].*",
						".*[mM][aA][cC].*",
						".*[aA][qQ][uU][aA].*",
						".*[nN][iI][mM][bB].*"
				};
				
				lf: for (String lafName: lafNames) {
					for (UIManager.LookAndFeelInfo l: info) {
						if (l.getName().matches(lafName)) {
							UIManager.setLookAndFeel(l.getClassName());
							break lf;
						}
					}					
				}
			}
		} catch (Exception e) {
			System.err.println("Error setting LAF: " + e);
		}

		// Set tooltip delay time.  Tooltips are used in MotorChooserDialog extensively.
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		
		
		// Setup the uncaught exception handler
		ExceptionHandler.registerExceptionHandler();
		
		
		// Load defaults
		Prefs.loadDefaultUnits();
		

		// Load motors etc.
		Databases.fakeMethod();
		
		// Starting action (load files or open new document)
		if (!handleCommandLine(args)) {
			newAction();
		}
		
		
		// Check whether update info has been fetched or whether it needs more time
		checkUpdateStatus(updateInfo);
	}
	
	
	private static void checkUpdateStatus(final UpdateInfoRetriever updateInfo) {
		if (updateInfo == null)
			return;

		int delay = 1000;
		if (!updateInfo.isRunning())
			delay = 100;

		final Timer timer = new Timer(delay, null);

		ActionListener listener = new ActionListener() {
			private int count = 5;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updateInfo.isRunning()) {
					timer.stop();
					
					String current = Prefs.getVersion();
					String last = Prefs.getString(Prefs.LAST_UPDATE, "");

					UpdateInfo info = updateInfo.getUpdateInfo();
					if (info != null && info.getLatestVersion() != null &&
							!current.equals(info.getLatestVersion()) &&
							!last.equals(info.getLatestVersion())) {
						Prefs.putString(Prefs.LAST_UPDATE, info.getLatestVersion());
						new UpdateInfoDialog(info).setVisible(true);
					}
				}
				count--;
				if (count <= 0)
					timer.stop();
			}
		};
		timer.addActionListener(listener);
		timer.start();
	}
	
	
	/**
	 * Handles arguments passed from the command line.  This may be used either
	 * when starting the first instance of OpenRocket or later when OpenRocket is
	 * executed again while running.
	 * 
	 * @param args	the command-line arguments.
	 * @return		whether a new frame was opened or similar user desired action was
	 * 				performed as a result.
	 */
	public static boolean handleCommandLine(String[] args) {
		
		// Check command-line for files
		boolean opened = false;
		for (String file: args) {
			if (open(new File(file), null)) {
				opened = true;
			}
		}
		return opened;
	}

}
