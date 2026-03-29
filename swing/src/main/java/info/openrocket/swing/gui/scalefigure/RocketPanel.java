package info.openrocket.swing.gui.scalefigure;


import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.componentanalysis.CAParameters;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.events.SimulationChangeEvent;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.customexpression.CustomExpression;
import info.openrocket.core.simulation.customexpression.CustomExpressionSimulationListener;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.simulation.listeners.system.GroundHitListener;
import info.openrocket.core.simulation.listeners.system.InterruptListener;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.preferences.DocumentPreferences;
import info.openrocket.swing.gui.components.ConfigurationComboBox;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.components.StageSelector;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.configdialog.ComponentConfigDialog;
import info.openrocket.swing.gui.dialogs.DisplaySettingsDialog;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;
import info.openrocket.swing.gui.figureelements.CGCaret;
import info.openrocket.swing.gui.figureelements.CPCaret;
import info.openrocket.swing.gui.figureelements.Caret;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;
import info.openrocket.swing.gui.widgets.ThemedToggleButton;
import info.openrocket.swing.gui.scalefigure.caliper.snap.CaliperSnapTarget;
import info.openrocket.swing.gui.util.ColorConversion;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.simulation.SimulationWorker;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.utils.CustomClickCountListener;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.EventObject;
import java.awt.geom.Point2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import info.openrocket.swing.gui.theme.UITheme;

import static info.openrocket.core.preferences.DocumentPreferences.PREF_SHOW_WARNINGS;


/**
 * A JPanel that contains a RocketFigure and buttons to manipulate the figure.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @author Bill Kuker <bkuker@billkuker.com>
 */
@SuppressWarnings("serial")
public class RocketPanel extends JPanel implements TreeSelectionListener, ChangeSource, CAParameters.CAParametersListener {

	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(RocketPanel.class);

	private static final String VIEW_TYPE_SEPARATOR = "__SEPARATOR__";		// Dummy string to indicate a horizontal separator item in the view type combobox
	public enum VIEW_TYPE {
		TopView(false, RocketFigure.VIEW_TOP),
		SideView(false, RocketFigure.VIEW_SIDE),
		BackView(false, RocketFigure.VIEW_BACK),
		SEPARATOR(false, -248),		// Horizontal combobox separator dummy item
		Figure3D(true, RocketFigure3d.TYPE_FIGURE),
		Unfinished(true, RocketFigure3d.TYPE_UNFINISHED),
		Finished(true, RocketFigure3d.TYPE_FINISHED);

		public final boolean is3d;
		private final int type;

		VIEW_TYPE(final boolean is3d, final int type) {
			this.is3d = is3d;
			this.type = type;
		};

		@Override
		public String toString() {
			if (type == -248) {
				return VIEW_TYPE_SEPARATOR;
			}
			return trans.get("RocketPanel.FigTypeAct." + super.toString());
		}

		public static VIEW_TYPE getDefaultViewType() {
			return SideView;
		}

		/**
		 * Get VIEW_TYPE from its name (string).
		 * @param name the name of the view type (as returned by name())
		 * @return the VIEW_TYPE, or null if not found
		 */
		public static VIEW_TYPE fromName(String name) {
			if (name == null) {
				return null;
			}
			for (VIEW_TYPE value : VIEW_TYPE.values()) {
				if (value == VIEW_TYPE.SEPARATOR) {
					continue;
				}
				if (value.name().equalsIgnoreCase(name)) {
					return value;
				}
			}
			return null;
		}

	}

	private boolean is3d;
	private final RocketFigure figure;
	private final RocketFigure3d figure3d;
	private VIEW_TYPE currentViewType = VIEW_TYPE.getDefaultViewType();

	private final ScaleScrollPane scrollPane;

	private final JPanel figureHolder;

	private JLabel infoMessage;
	private JCheckBox showWarnings;

	private TreeSelectionModel selectionModel = null;

	private ViewRotationControl rotationControl;
	private ScaleSelector scaleSelector;

	/* Calculation of CP and CG */
	private AerodynamicCalculator aerodynamicCalculator;

	private final OpenRocketDocument document;

	private Caret extraCP = null;
	private Caret extraCG = null;
	private RocketInfo extraText = null;

	/* Caliper tool */
	private CaliperManager caliperManager = null;
	private JPanel ribbon = null;  // Reference to ribbon for panel positioning

	private double cpAOA = Double.NaN;
	private double cpTheta = Double.NaN;
	private double cpMach = Double.NaN;
	private double cpRoll = Double.NaN;

	// The functional ID of the rocket that was simulated
	private ModID flightDataFunctionalID = ModID.INVALID;
    private FlightConfigurationId flightDataMotorID = null;

	private SimulationWorker backgroundSimulationWorker = null;

	private List<EventListener> listeners = new ArrayList<>();

	// Store the basic frame to know which tab is selected (Rocket design, Motors & Configuration, Flight simulations)
	private final BasicFrame basicFrame;


	/**
	 * The executor service used for running the background simulations.
	 * This uses a fixed-sized thread pool for all background simulations
	 * with all threads in daemon mode and with minimum priority.
	 */
	private static final ExecutorService backgroundSimulationExecutor;
	static {
		backgroundSimulationExecutor = Executors.newFixedThreadPool(SwingPreferences.getMaxThreadCount(),
				new ThreadFactory() {
										private ThreadFactory factory = Executors.defaultThreadFactory();

										@Override
										public Thread newThread(Runnable r) {
												Thread t = factory.newThread(r);
												t.setDaemon(true);
												t.setPriority(Thread.MIN_PRIORITY);
												return t;
										}
								});
	}

	public OpenRocketDocument getDocument(){
		return this.document;
	}

	public RocketPanel(OpenRocketDocument document) {
		this(document, null);
	}

	public RocketPanel(OpenRocketDocument document, BasicFrame basicFrame) {
		this.document = document;
		this.basicFrame = basicFrame;
		Rocket rkt = document.getRocket();
		
		
		// TODO: FUTURE: calculator selection
		aerodynamicCalculator = new BarrowmanCalculator();
		
		// Create figure and custom scroll pane
		figure = new RocketFigure(rkt);
		figure3d = new RocketFigure3d(document);

		// Set document-specific background colors if available
		updateBackgroundColors();

		figureHolder = new JPanel(new BorderLayout());

		scrollPane = new ScaleScrollPane(figure) {
			private static final long serialVersionUID = 1L;
			final CustomClickCountListener clickCountListener = new CustomClickCountListener();
			private Point mousePressedLoc = null;
			private double originalFigureRotation = 0;
			private boolean dragPanning = false;
			private boolean dragRotating = false;

			@Override
			public void mouseClicked(MouseEvent event) {
				// Check if snap mode is active - if so, only handle snap mode click, disable normal behavior
				if (caliperManager != null && caliperManager.isSnapModeActive()) {
					Point p0 = event.getPoint();
					Point p1 = getViewport().getViewPosition();
					int x = p0.x + p1.x;
					int y = p0.y + p1.y;

					// Try to snap, but don't process normal click regardless of whether snap occurred
					caliperManager.handleSnapModeMouseClicked(x, y, (p) -> screenToModel(p.x, p.y));
					return;  // Always return - don't process normal click behavior in snap mode
				}

				clickCountListener.click();
				handleMouseClick(event, clickCountListener.getClickCount());
			}

			public void mousePressed(MouseEvent e) {
				if (is3d) {
					return;
				}

				// In snap mode, disable normal mouse press behavior (component selection, etc.)
				if (caliperManager != null && caliperManager.isSnapModeActive()) {
					return;  // Don't process normal mouse press in snap mode
				}

				dragPanning = shouldPanOnDrag(e);
				dragRotating = shouldRotateOnDrag(e);
				mousePressedLoc = dragRotating ? e.getPoint() : null;
				originalFigureRotation = dragRotating ? figure.getRotation() : 0;

				// Check if clicking on a caliper handle or out-of-view indicator
				if (caliperManager != null && e.getButton() == MouseEvent.BUTTON1) {
					Point p0 = e.getPoint();
					Point p1 = getViewport().getViewPosition();
					int x = p0.x + p1.x;
					int y = p0.y + p1.y;

					if (caliperManager.handleMousePressed(x, y, (p) -> screenToModel(p.x, p.y))) {
						return;
					}
					// Block panning if the press is on an out-of-view indicator so the
					// subsequent mouseClicked can handle it without interference.
					if (caliperManager.isPressingIndicator(x, y)) {
						dragPanning = false;
						return;
					}
				}

				if (dragPanning) {
					super.mousePressed(e);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// In snap mode, disable rotation dragging
				if (caliperManager != null && caliperManager.isSnapModeActive()) {
					return;  // Don't process rotation dragging in snap mode
				}

				// Try to handle caliper dragging
				// Note: We don't check e.getButton() here because getButton() returns NOBUTTON
				// during drag events on most platforms (especially Windows). The button was
				// already verified in mousePressed, and CaliperManager tracks the drag state.
				if (caliperManager != null) {
					Point p0 = e.getPoint();
					Point p1 = getViewport().getViewPosition();
					int x = p0.x + p1.x;
					int y = p0.y + p1.y;

					if (caliperManager.handleMouseDragged(x, y, (p) -> screenToModel(p.x, p.y), e.isShiftDown())) {
						return;
					}
				}

				if (dragPanning) {
					super.mouseDragged(e);
					return;
				}
				if (dragRotating) {
					handleMouseDragged(e, mousePressedLoc, originalFigureRotation);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (caliperManager != null) {
					caliperManager.handleMouseReleased();
				}
				dragPanning = false;
				dragRotating = false;
				mousePressedLoc = null;
				super.mouseReleased(e);
			}

			private boolean shouldPanOnDrag(MouseEvent e) {
				if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
					return true;
				}
				return SwingUtilities.isLeftMouseButton(e) && rotationControl.isDragRotationLocked();
			}

			private boolean shouldRotateOnDrag(MouseEvent e) {
				return SwingUtilities.isLeftMouseButton(e) && !rotationControl.isDragRotationLocked();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (caliperManager != null && !is3d) {
					Point p0 = e.getPoint();
					Point p1 = getViewport().getViewPosition();
					int x = p0.x + p1.x;
					int y = p0.y + p1.y;

					// Handle snap mode mouse move first
					if (caliperManager.isSnapModeActive()) {
						caliperManager.handleSnapModeMouseMoved(x, y, (p) -> screenToModel(p.x, p.y));
					} else {
						caliperManager.handleMouseMoved(x, y, (p) -> screenToModel(p.x, p.y));
					}

					Cursor cursor = caliperManager.isAnyLineHovered()
							? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
							: Cursor.getDefaultCursor();
					getViewport().setCursor(cursor);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// Clear hover state when mouse leaves
				if (caliperManager != null) {
					caliperManager.handleMouseExited();
					getViewport().setCursor(Cursor.getDefaultCursor());
				}
			}

			/**
			 * Convert screen coordinates to model coordinates.
			 */
			private java.awt.geom.Point2D.Double screenToModel(int screenX, int screenY) {
				return figure.screenToModel(screenX, screenY);
			}
		};

		// Add keyboard action for Escape key to exit snap mode
		// Use WHEN_IN_FOCUSED_WINDOW so it works regardless of which component has focus
		// Register on the root pane once the component hierarchy is established
		scrollPane.addHierarchyListener(new java.awt.event.HierarchyListener() {
			@Override
			public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
				if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.PARENT_CHANGED) != 0) {
					javax.swing.JRootPane rootPane = javax.swing.SwingUtilities.getRootPane(scrollPane);
					if (rootPane != null) {
						javax.swing.Action escapeAction = new javax.swing.AbstractAction() {
							@Override
							public void actionPerformed(java.awt.event.ActionEvent e) {
								if (caliperManager != null && caliperManager.isSnapModeActive()) {
									caliperManager.exitSnapMode();
								}
							}
						};
						javax.swing.KeyStroke escapeKey = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
						rootPane.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "exitSnapMode");
						rootPane.getActionMap().put("exitSnapMode", escapeAction);
						// Remove listener after registration to avoid re-registering
						scrollPane.removeHierarchyListener(this);
					}
				}
			}
		});

		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setFitting(true);

		// Initialize caliper manager
		caliperManager = new CaliperManager(figure, document,
				() -> getCurrentViewType(),
				() -> {
					updateCaliperElements();
					updateFigures();
				},
				() -> {
					// Request focus on scrollPane when entering snap mode
					if (!is3d && scrollPane != null) {
						scrollPane.requestFocusInWindow();
					}
				});

		// Set info message updater for caliper manager
		if (caliperManager != null) {
			caliperManager.setInfoMessageUpdater((messageKey) -> {
				if (infoMessage != null) {
					infoMessage.setText(trans.get(messageKey));
				}
			});
		}

		createPanel();

		is3d = true;
		go2D();

		rkt.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				updateExtras();
				updateFigures();
				scrollPane.componentResized(null);	// Triggers a resize so that when the rocket becomes smaller, the scrollPane updates its size
			}
		});

		rkt.addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				updateExtras();
				if (is3d) {
					if (e.isTextureChange()) {
						figure3d.flushTextureCaches();
					}
				}
				updateFigures();
			}
		});

		figure3d.addComponentSelectionListener(new RocketFigure3d.ComponentSelectionListener() {
			final CustomClickCountListener clickCountListener = new CustomClickCountListener();

			@Override
			public void componentClicked(RocketComponent[] clicked, MouseEvent event) {
				clickCountListener.click();
				handleComponentClick(clicked, event, clickCountListener.getClickCount());
			}
		});
	}

	public void updateFigures() {
		if (!is3d)
			figure.updateFigure();
		else
			figure3d.updateFigure();
	}

	/**
	 * Updates the rulers of the rocket panel to the currently selected default unit.
	 */
	public void updateRulers() {
		scrollPane.updateRulerUnit();
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	private void go3D() {
		if (is3d)
			return;
		if (caliperManager != null) {
			caliperManager.onSwitchTo3D();
		}
		is3d = true;

		figureHolder.remove(scrollPane);
		figureHolder.add(figure3d, BorderLayout.CENTER);
		rotationControl.setEnabled(false);
		scaleSelector.setEnabled(false);

		// Update text colors for 3D view
		updateTextColors();

		revalidate();
		figureHolder.revalidate();

		figure3d.repaint();
	}

	private void go2D() {
		if (!is3d)
			return;
		is3d = false;

		if (caliperManager != null) {
			caliperManager.onSwitchTo2D();
		}

		figureHolder.remove(figure3d);
		figureHolder.add(scrollPane, BorderLayout.CENTER);
		rotationControl.setEnabled(true);
		scaleSelector.setEnabled(true);
		
		// Re-apply the current L&F to the scroll pane (rulers, etc.) which missed
		// FlatLaf.updateUI() while detached from the component hierarchy in 3D mode.
		SwingUtilities.updateComponentTreeUI(scrollPane);

		// Update background and text colors for 2D view
		updateBackgroundColors();
		updateTextColors();

		scrollPane.revalidate();
		scrollPane.repaint();
		revalidate();
		figureHolder.revalidate();
		figure.repaint();
	}

	/**
	 * Get the current view type.
	 * @return the current VIEW_TYPE
	 */
	public VIEW_TYPE getCurrentViewType() {
		return currentViewType;
	}

	/**
	 * Creates the layout and components of the panel.
	 */
	private void createPanel() {
		final Rocket rkt = document.getRocket();

		rkt.addChangeListener(new StateChangeListener(){
			@Override
			public void stateChanged(EventObject eo) {
				updateExtras();
				updateFigures();
			}
		});

		setLayout(new MigLayout("", "[shrink][grow]", "[shrink][grow][shrink]"));

		setPreferredSize(new Dimension(800, 300));

		ribbon = new JPanel(new MigLayout("insets 0, fill, hidemode 2"));

		// View Type drop-down
		ComboBoxModel<VIEW_TYPE> cm = new ViewTypeComboBoxModel(VIEW_TYPE.values(), VIEW_TYPE.getDefaultViewType()) {

			@Override
			public void setSelectedItem(Object o) {
				VIEW_TYPE v = (VIEW_TYPE) o;
				if (v == VIEW_TYPE.SEPARATOR) {
					return;
				}

				// Save caliper state before switching views
				if (caliperManager != null) {
					caliperManager.saveCurrentCaliperState();
				}

				super.setSelectedItem(o);
				currentViewType = v;
				if (v.is3d) {
					figure3d.setType(v.type);
					go3D();
					updateRulers();
				} else {
					figure.setType(v);
					if (caliperManager != null) {
						caliperManager.loadCaliperStateForView(getCurrentViewType());
						// Update snap targets when view type changes
						if (caliperManager.isSnapModeActive()) {
							caliperManager.updateSnapTargets();
						}
					}
					updateExtras(); // when switching from side view to back view, need to clear CP & CG markers
					go2D();
					updateRulers();
				}
			}
		};
		ribbon.add(new JLabel(trans.get("RocketPanel.lbl.ViewType")), "cell 0 0");
		final JComboBox<RocketPanel.VIEW_TYPE> viewSelector = new JComboBox<>(cm);
		viewSelector.setRenderer(new SeparatorComboBoxRenderer(viewSelector.getRenderer()));
		ribbon.add(viewSelector, "cell 0 1");

		// Zoom level selector
		scaleSelector = new ScaleSelector(scrollPane);
		JButton zoomOutButton = scaleSelector.getZoomOutButton();
		JComboBox<String> scaleSelectorCombo = scaleSelector.getScaleSelectorCombo();
		JButton zoomInButton = scaleSelector.getZoomInButton();
		JButton zoomFitButton = scaleSelector.getZoomFitButton();
		ribbon.add(zoomOutButton, "gapleft para, cell 1 1");
		ribbon.add(new JLabel(trans.get("RocketPanel.lbl.Zoom")), "cell 2 0, spanx 2");
		ribbon.add(scaleSelectorCombo, "cell 2 1");
		ribbon.add(zoomInButton, "cell 3 1, split 2");
		ribbon.add(zoomFitButton, "cell 3 1");

		// Show CG/CP
		final JCheckBox showCGCP = new JCheckBox();
		showCGCP.setText(trans.get("RocketPanel.checkbox.ShowCGCP"));
		showCGCP.setSelected(true);
		showCGCP.setToolTipText(trans.get("RocketPanel.checkbox.ShowCGCP.ttip"));
		ribbon.add(showCGCP, "cell 4 0, gapleft para");

		showCGCP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (figure != null) {
					figure.setDrawCarets(showCGCP.isSelected());
				}
				if (figure3d != null) {
					figure3d.setDrawCarets(showCGCP.isSelected());
				}
				updateFigures();
			}
		});

		// Calipers toggle button - directly enables/disables the caliper tool
		final ThemedToggleButton showCalipers = new ThemedToggleButton(trans.get("RocketPanel.checkbox.Calipers"), Icons.RULER);
		showCalipers.setToolTipText(trans.get("RocketPanel.checkbox.Calipers.ttip"));
		showCalipers.setSelected(false);
		ribbon.add(showCalipers, "cell 4 1, gapleft para");

		// Inline caliper controls (mode, snap, units, distance) — shown only when calipers enabled
		if (caliperManager != null) {
			JPanel caliperRibbonPanel = buildCaliperRibbonPanel();
			caliperRibbonPanel.setVisible(false);
			ribbon.add(caliperRibbonPanel, "cell 5 0, spany 2, gapleft para, gapright para, aligny center");

			showCalipers.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean enabled = showCalipers.isSelected();
					caliperManager.setEnabled(enabled);
					caliperRibbonPanel.setVisible(enabled);
					updateFigures();
				}
			});
		}

		// Vertical separator
		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		Dimension d_sep = sep.getPreferredSize();
		d_sep.height = (int) (0.7 * ribbon.getPreferredSize().height);
		sep.setPreferredSize(d_sep);
		ribbon.add(sep, "cell 6 0, spany 2, gapright para");

		// Stage selector
		StageSelector stageSelector = new StageSelector( rkt );
		rkt.addChangeListener(stageSelector);
		ribbon.add(new JLabel(trans.get("RocketPanel.lbl.Stages")), "cell 7 0, pushx");
		ribbon.add(stageSelector, "cell 7 1, pushx");

		// Flight configuration selector
		//// Flight configuration:
		JLabel label = new JLabel(trans.get("RocketPanel.lbl.Flightcfg"));
		ribbon.add(label, "cell 8 0");

		final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rkt);
		ribbon.add(configComboBox, "cell 8 1, width 16%, wmin 100");

		JScrollPane ribbonScroll = new JScrollPane(ribbon,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				if (getHorizontalScrollBar().isVisible()) {
					d.height += getHorizontalScrollBar().getPreferredSize().height;
				}
				return d;
			}
		};
		ribbonScroll.setBorder(null);
		ribbonScroll.getHorizontalScrollBar().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				RocketPanel.this.revalidate();
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				RocketPanel.this.revalidate();
			}
		});
		add(ribbonScroll, "growx, span, wrap");

		// Create rotation control
		rotationControl = new ViewRotationControl(figure);
		add(rotationControl, "growx, growy");

		rotationControl.getRotationSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateExtras();
			}
		});

		// Add the rocket figure
		add(figureHolder, "grow, wmin 300lp, hmin 100lp, wrap");

		// Bottom row
		JPanel bottomRow = new JPanel(new MigLayout("fillx, gapy 0, ins 0"));

		//// <html>Click to select &nbsp;&nbsp; Shift+click to select other &nbsp;&nbsp; Double-click to edit &nbsp;&nbsp; Click+drag to move
		infoMessage = new StyledLabel(trans.get("RocketPanel.lbl.infoMessage"), -3);
		bottomRow.add(infoMessage);

		//// Configure display button
		JButton configureDisplayButton = new JButton(Icons.CONFIGURE_DISPLAY);
		configureDisplayButton.setToolTipText(trans.get("RocketPanel.btn.configureDisplay.ttip"));
		configureDisplayButton.addActionListener(e -> showDisplaySettingsDialog());
		bottomRow.add(configureDisplayButton, "pushx, right, gapright unrel");

		//// Screenshot button
		JButton screenshotButton = new JButton(Icons.SCREENSHOT);
		screenshotButton.setToolTipText(trans.get("RocketPanel.btn.captureDesignView.ttip"));
		screenshotButton.addActionListener(e -> showCaptureDesignViewDialog());
		bottomRow.add(screenshotButton, "right, gapright unrel");

		//// Show warnings
		this.showWarnings = new JCheckBox(trans.get("RocketPanel.check.showWarnings"));
		showWarnings.setSelected(document.getDocumentPreferences().getBoolean(PREF_SHOW_WARNINGS, true));
		showWarnings.setToolTipText(trans.get("RocketPanel.check.showWarnings.ttip"));
		bottomRow.add(showWarnings);
		showWarnings.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				document.getDocumentPreferences().putBoolean(PREF_SHOW_WARNINGS, showWarnings.isSelected());
				updateExtras();
				updateFigures();
			}
		});

		add(bottomRow, "skip, growx, gapleft 25");

		addExtras();
	}

	public RocketFigure getFigure() {
		return figure;
	}

	public RocketFigure3d getFigure3d() {
		return figure3d;
	}

	public AerodynamicCalculator getAerodynamicCalculator() {
		return aerodynamicCalculator;
	}

	/**
	 * Get the center of pressure figure element.
	 *
	 * @return center of pressure info
	 */
	public Caret getExtraCP() {
		return extraCP;
	}

	/**
	 * Get the center of gravity figure element.
	 *
	 * @return center of gravity info
	 */
	public Caret getExtraCG() {
		return extraCG;
	}

	/**
	 * Get the extra text figure element.
	 *
	 * @return extra text that contains info about the rocket design
	 */
	public RocketInfo getExtraText() {
		return extraText;
	}

	public void setSelectionModel(TreeSelectionModel m) {
		if (selectionModel != null) {
			selectionModel.removeTreeSelectionListener(this);
		}
		selectionModel = m;
		selectionModel.addTreeSelectionListener(this);
		valueChanged((TreeSelectionEvent) null); // updates FigureParameters
	}

	public void setSelectedComponent(RocketComponent component) {
		if (component == null) {
			selectionModel.setSelectionPath(null);
			return;
		}
		TreePath path = ComponentTreeModel.makeTreePath(component);
		selectionModel.setSelectionPath(path);
	}

	/**
	 * Return the angle of attack used in CP calculation.  NaN signifies the default value
	 * of zero.
	 * @return   the angle of attack used, or NaN.
	 */
	public double getCPAOA() {
		return cpAOA;
	}

	/**
	 * Set the angle of attack to be used in CP calculation.  A value of NaN signifies that
	 * the default AOA (zero) should be used.
	 * @param aoa	the angle of attack to use, or NaN
	 */
	public void setCPAOA(double aoa) {
		if (MathUtil.equals(aoa, cpAOA) ||
				(Double.isNaN(aoa) && Double.isNaN(cpAOA)))
			return;
		cpAOA = aoa;
		updateExtras();
		updateFigures();
		fireChangeEvent();
	}

	public double getCPTheta() {
		return cpTheta;
	}

	public void setCPTheta(double theta) {
		if (MathUtil.equals(theta, cpTheta) ||
				(Double.isNaN(theta) && Double.isNaN(cpTheta)))
			return;
		cpTheta = theta;
		if (!Double.isNaN(theta))
			figure.setRotation(theta);
		updateExtras();
		updateFigures();
		fireChangeEvent();
	}

	public double getCPMach() {
		return cpMach;
	}

	public void setCPMach(double mach) {
		if (MathUtil.equals(mach, cpMach) ||
				(Double.isNaN(mach) && Double.isNaN(cpMach)))
			return;
		cpMach = mach;
		updateExtras();
		updateFigures();
		fireChangeEvent();
	}

	public double getCPRoll() {
		return cpRoll;
	}

	public void setCPRoll(double roll) {
		if (MathUtil.equals(roll, cpRoll) ||
				(Double.isNaN(roll) && Double.isNaN(cpRoll)))
			return;
		cpRoll = roll;
		updateExtras();
		updateFigures();
		fireChangeEvent();
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(0, listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	protected void fireChangeEvent() {
		EventObject e = new EventObject(this);
		for (EventListener l : listeners) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(e);
			}
		}
	}

	/**
	 * Handle clicking on figure shapes.  The functioning is the following:
	 *
	 * Get the components clicked.
	 * If no component is clicked, do nothing.
	 * If the currently selected component is in the set, keep it,
	 * unless the selector specified is pressed.  If it is pressed, cycle to
	 * the next component. Otherwise select the first component in the list.
	 */
	public static final int CYCLE_SELECTION_MODIFIER = InputEvent.SHIFT_DOWN_MASK;

	private void handleMouseClick(MouseEvent event, int clickCount) {
		// Don't process normal clicks when in snap mode
		if (caliperManager != null && caliperManager.isSnapModeActive()) {
			return;
		}

		// Check if click is on a caliper indicator (before checking components)
		if (caliperManager != null && caliperManager.isEnabled() && !is3d) {
			Point vp = scrollPane.getViewport().getViewPosition();
			Point raw = event.getPoint();
			Point clickPoint = new Point(raw.x + vp.x, raw.y + vp.y);
			Rectangle visibleRect = figure.getVisibleRect();
			if (visibleRect != null) {
				// Get the transform from the figure
				java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
				Point origin = figure.getSubjectOrigin();
				double scale = figure.getAbsoluteScale();
				transform.translate(origin.x, origin.y);
				transform.scale(scale, -scale); // Y is inverted

				// Create screenToModel function
				java.util.function.Function<Point, java.awt.geom.Point2D.Double> screenToModelFunc =
						(p) -> figure.screenToModel(p.x, p.y);

				// Check vertical caliper lines
				if (caliperManager.getMode() == CaliperManager.CaliperMode.VERTICAL) {
					info.openrocket.swing.gui.figureelements.CaliperLine cal1Line = caliperManager.getCaliper1Line();
					info.openrocket.swing.gui.figureelements.CaliperLine cal2Line = caliperManager.getCaliper2Line();

					if (cal1Line != null) {
						double screenX = cal1Line.getScreenX(transform);
						double siblingX = cal2Line != null ? cal2Line.getScreenX(transform) : Double.NaN;
						java.awt.geom.Rectangle2D.Double bounds = cal1Line.getIndicatorBounds(screenX, visibleRect, siblingX);
						if (bounds != null && bounds.contains(clickPoint.x, clickPoint.y)) {
							caliperManager.moveCaliperLineIntoView(true, visibleRect, screenToModelFunc);
							return; // Handled, don't process component click
						}
					}

					if (cal2Line != null) {
						double screenX = cal2Line.getScreenX(transform);
						double siblingX = cal1Line != null ? cal1Line.getScreenX(transform) : Double.NaN;
						java.awt.geom.Rectangle2D.Double bounds = cal2Line.getIndicatorBounds(screenX, visibleRect, siblingX);
						if (bounds != null && bounds.contains(clickPoint.x, clickPoint.y)) {
							caliperManager.moveCaliperLineIntoView(false, visibleRect, screenToModelFunc);
							return; // Handled, don't process component click
						}
					}
				} else {
					// Check horizontal caliper lines
					info.openrocket.swing.gui.figureelements.HorizontalCaliperLine cal1Line = caliperManager.getCaliper1HorizontalLine();
					info.openrocket.swing.gui.figureelements.HorizontalCaliperLine cal2Line = caliperManager.getCaliper2HorizontalLine();

					if (cal1Line != null) {
						double screenY = cal1Line.getScreenY(transform);
						double siblingY = cal2Line != null ? cal2Line.getScreenY(transform) : Double.NaN;
						java.awt.geom.Rectangle2D.Double bounds = cal1Line.getIndicatorBounds(screenY, visibleRect, siblingY);
						if (bounds != null && bounds.contains(clickPoint.x, clickPoint.y)) {
							caliperManager.moveCaliperLineIntoView(true, visibleRect, screenToModelFunc);
							return; // Handled, don't process component click
						}
					}

					if (cal2Line != null) {
						double screenY = cal2Line.getScreenY(transform);
						double siblingY = cal1Line != null ? cal1Line.getScreenY(transform) : Double.NaN;
						java.awt.geom.Rectangle2D.Double bounds = cal2Line.getIndicatorBounds(screenY, visibleRect, siblingY);
						if (bounds != null && bounds.contains(clickPoint.x, clickPoint.y)) {
							caliperManager.moveCaliperLineIntoView(false, visibleRect, screenToModelFunc);
							return; // Handled, don't process component click
						}
					}
				}
			}
		}

		// Get the component that is clicked on
		Point p0 = event.getPoint();
		Point p1 = scrollPane.getViewport().getViewPosition();
		int x = p0.x + p1.x;
		int y = p0.y + p1.y;

		RocketComponent[] clicked = figure.getComponentsByPoint(x, y);

		// If no component is clicked, do nothing
		if (clicked.length == 0) {
			selectionModel.setSelectionPath(null);
			return;
		}

		if (event.getButton() == MouseEvent.BUTTON1) {
			handleComponentClick(clicked, event, clickCount);
		} else if (event.getButton() == MouseEvent.BUTTON3) {
			List<RocketComponent> selectedComponents = Arrays.stream(selectionModel.getSelectionPaths())
					.map(c -> (RocketComponent) c.getLastPathComponent()).collect(Collectors.toList());

			boolean newClick = true;
			for (RocketComponent component : clicked) {
				if (selectedComponents.contains(component)) {
					newClick = false;
					break;
				}
			}

			if (newClick) {
				for (RocketComponent rocketComponent : clicked) {
					if (!selectedComponents.contains(rocketComponent)) {
						setSelectedComponent(rocketComponent);
					}
				}
			}

			basicFrame.doComponentTreePopup(event);
		}
	}

	private void handleComponentClick(RocketComponent[] clicked, MouseEvent event, int clickCount) {
		List<RocketComponent> selectedComponents = Arrays.stream(selectionModel.getSelectionPaths())
				.map(c -> (RocketComponent) c.getLastPathComponent()).collect(Collectors.toList());

		if (clicked == null || clicked.length == 0) {
			selectionModel.setSelectionPaths(null);
			return;
		}

		// Check for double-click.
		// If the shift/meta key is not pressed and the component was not already selected, ignore the double click and treat it as a single click
		if (clickCount >= 2) {
			handleDoubleComponentClick(clicked, event, selectedComponents);
		} else if (clickCount == 1) {
			handleSingleComponentClick(clicked, event, selectedComponents);
		}
	}

	private void handleDoubleComponentClick(RocketComponent[] clicked, MouseEvent event, List<RocketComponent> selectedComponents) {
		// Multi-component edit if shift/meta key is pressed
		if (!selectedComponents.isEmpty() && (event.isShiftDown() || GUIUtil.isMenuShortcutDown(event))) {
			List<TreePath> paths = new ArrayList<>(Arrays.asList(selectionModel.getSelectionPaths()));
			RocketComponent component = selectedComponents.get(selectedComponents.size() - 1);
			component.clearConfigListeners();

			// Make sure the clicked component is selected
			for (RocketComponent c : clicked) {
				if (!selectedComponents.contains(c)) {
					TreePath path = ComponentTreeModel.makeTreePath(c);
					paths.add(path);
					selectionModel.setSelectionPaths(paths.toArray(new TreePath[0]));
					selectedComponents = Arrays.stream(selectionModel.getSelectionPaths())
							.map(c1 -> (RocketComponent) c1.getLastPathComponent()).toList();
					component = c;
					break;
				}
			}

			// Multi-component edit if shift/meta key is pressed
			for (RocketComponent c : selectedComponents) {
				if (c == component) continue;
				c.clearConfigListeners();
				component.addConfigListener(c);
			}
			ComponentConfigDialog.showDialog(SwingUtilities.getWindowAncestor(this), document, component);
		}
		// Normal double click (no shift or meta key)
		else {
			// If the clicked component is not in the selection, treat it as a single click
			if (!selectedComponents.contains(clicked[0])) {
				TreePath path = ComponentTreeModel.makeTreePath(clicked[0]);
				selectionModel.setSelectionPath(path);
			}
			// Open the configuration dialog for the first clicked component
			else {
				TreePath path = ComponentTreeModel.makeTreePath(clicked[0]);
				selectionModel.setSelectionPath(path);        // Revert to single selection
				RocketComponent component = (RocketComponent) path.getLastPathComponent();

				ComponentConfigDialog.showDialog(SwingUtilities.getWindowAncestor(this),
						document, component);
			}
		}
	}

	private void handleSingleComponentClick(RocketComponent[] clicked, MouseEvent event, List<RocketComponent> selectedComponents) {
		// If the shift-button is held, add a newly clicked component to the selection path
		if (event.isShiftDown() || GUIUtil.isMenuShortcutDown(event)) {
			List<TreePath> paths = new ArrayList<>(Arrays.asList(selectionModel.getSelectionPaths()));
			for (int i = 0; i < clicked.length; i++) {
				if (!selectedComponents.contains(clicked[i])) {
					TreePath path = ComponentTreeModel.makeTreePath(clicked[i]);
					paths.add(path);
					break;
				}
				// If all the clicked components are already in the selection, then deselect an object
				if (i == clicked.length - 1) {
					paths.removeIf(path -> path.getLastPathComponent() == clicked[0]);
				}
			}
			try {
				selectionModel.setSelectionPaths(paths.toArray(new TreePath[0]));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		// Single click, so set the selection to the first clicked component
		else {
			if (!selectedComponents.contains(clicked[0])) {
				TreePath path = ComponentTreeModel.makeTreePath(clicked[0]);
				selectionModel.setSelectionPath(path);
			}
		}
	}

	private void handleMouseDragged(MouseEvent event, Point originalDragLocation, double originalRotation) {
		// Don't allow rotation dragging when in snap mode
		if (caliperManager != null && caliperManager.isSnapModeActive()) {
			return;
		}
		if (originalDragLocation == null || is3d || rotationControl.isDragRotationLocked()) {
			return;
		}

		int dy = event.getY() - originalDragLocation.y;

		double rotationOffset = ((double) dy / scrollPane.getHeight()) * Math.PI;
		double newRotation = originalRotation - rotationOffset;
		// Ensure the rotation is within the range [0, 2*PI]
		newRotation = (newRotation + 2 * Math.PI) % (2 * Math.PI);

		// Apply snapping if Shift key is pressed
		if (event.isShiftDown()) {
			newRotation = ViewRotationControl.snapRotation(newRotation);
		}

		figure.setRotation(newRotation);
	}

	/**
	 * Updates the extra data included in the figure.  Currently this includes
	 * the CP and CG carets. Also start the background simulator.
	 */
	private WarningSet warnings = new WarningSet();

	public void updateExtras() {
		CoordinateIF cp, cg;
		double cgx = Double.NaN;
		double cgy = Double.NaN;
		double cpx = Double.NaN;
		double cpy = Double.NaN;
		final double rotation = figure.getRotation(true);

		FlightConfiguration curConfig = document.getSelectedConfiguration();
		// TODO: MEDIUM: User-definable conditions
		FlightConditions conditions = new FlightConditions(curConfig);
		warnings.clear();

		extraText.setCurrentConfig(curConfig);

		if (!Double.isNaN(cpMach)) {
			conditions.setMach(cpMach);
			extraText.setMach(cpMach);
		} else {
			conditions.setMach(Application.getPreferences().getDefaultMach());
			extraText.setMach(Application.getPreferences().getDefaultMach());
		}

		if (!Double.isNaN(cpAOA)) {
			conditions.setAOA(cpAOA);
		} else {
			conditions.setAOA(0);
		}
		extraText.setAOA(cpAOA);

		if (!Double.isNaN(cpRoll)) {
			conditions.setRollRate(cpRoll);
		} else {
			conditions.setRollRate(0);
		}

		if (!Double.isNaN(cpTheta)) {
			conditions.setTheta(cpTheta);
			cp = aerodynamicCalculator.getCP(curConfig, conditions, warnings);
		} else {
			cp = aerodynamicCalculator.getWorstCP(curConfig, conditions, warnings);
		}
		extraText.setTheta(cpTheta);
		if (cp.getWeight() > MathUtil.EPSILON){
			cpx = cp.getX();
			// map the 3D value into the 2D Display Panel
			cpy = cp.getY() * Math.cos(rotation) + cp.getZ()*Math.sin(rotation);
		}
		
		cg = MassCalculator.calculateLaunch( curConfig).getCM();
		if (cg.getWeight() > MassCalculator.MIN_MASS){
			cgx = cg.getX();
			// map the 3D value into the 2D Display Panel
			cgy = cg.getY() * Math.cos(rotation) + cg.getZ()*Math.sin(rotation);
		}

		// We need to flip the y coordinate if we are in top view
		if (figure.getCurrentViewType() == RocketPanel.VIEW_TYPE.TopView) {
			cgy = -cgy;
		}

		double length = curConfig.getLength();
		
		double diameter = Double.NaN;
		for (RocketComponent c : curConfig.getCoreComponents()) {
			if (c instanceof SymmetricComponent) {
				double d1 = ((SymmetricComponent) c).getForeRadius() * 2;
				double d2 = ((SymmetricComponent) c).getAftRadius() * 2;
				diameter = MathUtil.max(diameter, d1, d2);
			}
		}

		RigidBody emptyInfo = MassCalculator.calculateStructure( curConfig );
		
		extraText.setCG(cgx);
		extraText.setCP(cpx);
		extraText.setLength(length);
		extraText.setDiameter(diameter);
		extraText.setMassWithMotors(cg.getWeight());
		extraText.setMassWithoutMotors( emptyInfo.getMass() );
		extraText.setWarnings(warnings);
		if (this.showWarnings != null) {
			extraText.setShowWarnings(showWarnings.isSelected());
		}

		if (length > 0) {
			figure3d.setCG(cg);
			figure3d.setCP(cp);
		} else {
			figure3d.setCG(new Coordinate(Double.NaN, Double.NaN));
			figure3d.setCP(new Coordinate(Double.NaN, Double.NaN));
		}

		if (length > 0 &&
				((figure.getCurrentViewType() == RocketPanel.VIEW_TYPE.TopView) || (figure.getCurrentViewType() == RocketPanel.VIEW_TYPE.SideView))) {
			extraCP.setPosition(cpx, cpy);
			extraCG.setPosition(cgx, cgy);

			// Update CG/CP positions in CaliperManager for snap targets
			if (caliperManager != null) {
				caliperManager.setCGPosition(cgx, cgy);
				caliperManager.setCPPosition(cpx, cpy);
				// Update snap targets if in snap mode
				if (caliperManager.isSnapModeActive()) {
					caliperManager.updateSnapTargets();
				}
			}
		} else {
			extraCP.setPosition(Double.NaN, Double.NaN);
			extraCG.setPosition(Double.NaN, Double.NaN);

			// Clear CG/CP positions in CaliperManager
			if (caliperManager != null) {
				caliperManager.setCGPosition(Double.NaN, Double.NaN);
				caliperManager.setCPPosition(Double.NaN, Double.NaN);
				// Update snap targets if in snap mode
				if (caliperManager.isSnapModeActive()) {
					caliperManager.updateSnapTargets();
				}
			}
		}

		////////  Flight simulation in background

		// Check whether to compute or not
		if (!((SwingPreferences) Application.getPreferences()).computeFlightInBackground()) {
			extraText.setSimulation(null);
			extraText.setCalculatingData(false);
			stopBackgroundSimulation();
			return;
		}

		// Check whether data is already up to date
		if (flightDataFunctionalID == curConfig.getRocket().getFunctionalModID() &&
				flightDataMotorID == curConfig.getId()) {
			return;
		}

		flightDataFunctionalID = curConfig.getRocket().getFunctionalModID();
		flightDataMotorID = curConfig.getId();

		// Stop previous computation (if any)
		stopBackgroundSimulation();

		// Check that configuration has motors
		if (!curConfig.hasMotors()) {
			extraText.setSimulation(null);
			extraText.setFlightData(FlightData.NaN_DATA);
			extraText.setCalculatingData(false);
			return;
		}

		// Update simulations
		if (Application.getPreferences().getAutoRunSimulations()) {
			// Update only current flight config simulation when you are not in the simulations tab
			updateSims(this.basicFrame != null && this.basicFrame.getSelectedTab() == BasicFrame.SIMULATION_TAB);
		}
		else {
			// Always update the simulation of the current configuration
			updateSims(false);
		}

		// Update flight data and add flight data update trigger upon simulation changes
		for (Simulation sim : document.getSimulations()) {
			sim.addChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(EventObject e) {
					if (updateFlightData(sim) && sim.getFlightConfigurationId() == document.getSelectedConfiguration().getFlightConfigurationID()) {
						// TODO: HIGH: this gets updated for every sim run; not necessary...
						updateFigures();
					}
				}
			});
			if (updateFlightData(sim)) {
				break;
			}
		}
	}

	/**
	 * Updates the simulations. If *currentConfig* is false, only update the simulation of the current flight
	 * configuration. If it is true, update all the simulations.
	 *
	 * @param updateAllSims flag to check whether to update all the simulations (true) or only the current
	 *                      flight config sim (false)
	 */
	private void updateSims(boolean updateAllSims) {
		// Stop previous computation (if any)
		stopBackgroundSimulation();

		FlightConfigurationId curID = document.getSelectedConfiguration().getFlightConfigurationID();
		extraText.setCalculatingData(true);
		Rocket duplicate = (Rocket)document.getRocket().copy();

		// Re-run the present simulation(s)
		List<Simulation> sims = new LinkedList<>();
		for (Simulation sim : document.getSimulations()) {
			if (Simulation.isStatusUpToDate(sim.getStatus()) ||
					!document.getRocket().getFlightConfiguration(sim.getFlightConfigurationId()).hasMotors())
				continue;

			// Find a Simulation based on the current flight configuration
			if (!updateAllSims) {
				if (sim.getFlightConfigurationId().compareTo(curID) == 0) {
					sims.add(sim);
					break;
				}
			}
			else {
				sims.add(sim);
			}
		}
		runBackgroundSimulations(sims, duplicate);
	}

	/**
	 * Update the flight data text with the data of {sim}. Only update if sim is the simulation of the current flight
	 * configuration.
	 * @param sim: simulation from which the flight data is taken
	 * @return true if the flight data was updated, false if not
	 */
	private boolean updateFlightData(Simulation sim) {
		FlightConfigurationId curID = document.getSelectedConfiguration().getFlightConfigurationID();
		if (sim.getFlightConfigurationId().compareTo(curID) == 0) {
			extraText.setSimulation(sim);
			return true;
		}
		return false;
	}

	/**
	 * Runs a new background simulation for simulations *sims*. It will run all the simulations in sims sequentially
	 * in the background.
	 *
	 * @param sims simulations which should be run
	 * @param rkt rocket for which the simulations are run
	 */
	private void runBackgroundSimulations(List<Simulation> sims, Rocket rkt) {
		if (sims.size() == 0) {
			extraText.setCalculatingData(false);
			for (Simulation sim : document.getSimulations()) {
				if (updateFlightData(sim)) {
					return;
				}
			}
			extraText.setFlightData(FlightData.NaN_DATA);
			return;
		}

		// I *think* every FlightConfiguration has at least one associated simulation; just in case I'm wrong,
		// if there isn't one we'll create a new simulation to update the statistics in the panel using the
		// default simulation conditions
		for (Simulation sim : sims) {
			if (sim == null) {
				log.info("creating new simulation");
				sim = ((SwingPreferences) Application.getPreferences()).getBackgroundSimulation(rkt);
				sim.setFlightConfigurationId(document.getSelectedConfiguration().getId());
			} else
				log.info("using pre-existing simulation");
		}

		backgroundSimulationWorker = new BackgroundSimulationWorker(document, sims);
		backgroundSimulationExecutor.execute(backgroundSimulationWorker);
	}

	/**
	 * Cancels the current background simulation worker, if any.
	 */
	private void stopBackgroundSimulation() {
		if (backgroundSimulationWorker != null) {
			backgroundSimulationWorker.cancel(true);
			backgroundSimulationWorker = null;
		}
	}

	/**
	 * A SimulationWorker that simulates the rocket flight in the background and
	 * sets the results to the extra text when finished.  The worker can be cancelled
	 * if necessary.
	 */
	private class BackgroundSimulationWorker extends SimulationWorker {

		private final CustomExpressionSimulationListener exprListener;
		private final OpenRocketDocument doc;
		private List<Simulation> sims;

		public BackgroundSimulationWorker(OpenRocketDocument doc, List<Simulation> sims) {
			super(sims.get(0));
			this.sims = sims;
			this.doc = doc;
			List<CustomExpression> exprs = doc.getCustomExpressions();
			exprListener = new CustomExpressionSimulationListener(exprs);
		}

		@Override
		protected FlightData doInBackground() {
			extraText.setCalculatingData(true);
			// Pause a little while to allow faster UI reaction
			try {
				Thread.sleep(300);
			} catch (InterruptedException ignore) {
			}
			if (isCancelled() || backgroundSimulationWorker != this)
				return null;
			return super.doInBackground();
		}

		@Override
		protected void simulationDone() {
			// Do nothing if cancelled
			if (isCancelled() || backgroundSimulationWorker != this)
				return;
			backgroundSimulationWorker = null;

			// Only set the flight data information of the current flight configuration
			extraText.setCalculatingData(false);
			if (!is3d)
				figure.repaint();
			else
				figure3d.repaint();
			document.fireDocumentChangeEvent(new SimulationChangeEvent(simulation));

			// Run the new simulation after this one has ended
			this.sims.remove(0);
			if (this.sims.size() > 0) {
				backgroundSimulationWorker = new BackgroundSimulationWorker(this.doc, this.sims);
				backgroundSimulationExecutor.execute(backgroundSimulationWorker);
			}
		}

		@Override
		protected SimulationListener[] getExtraListeners() {
			return new SimulationListener[] {
					InterruptListener.INSTANCE,
					GroundHitListener.INSTANCE,
					exprListener };

		}

		@Override
		protected void simulationInterrupted(Throwable t) {
			// Do nothing on cancel, set N/A data otherwise
			if (isCancelled() || backgroundSimulationWorker != this) // Double-check
				return;

			backgroundSimulationWorker = null;
			extraText.setFlightData(FlightData.NaN_DATA);
			extraText.setCalculatingData(false);
			if (!is3d)
				figure.repaint();
			else
				figure3d.repaint();
		}
	}

	/**
	 * Adds the extra data to the figure.  Currently this includes the CP and CG carets.
	 */
	private void addExtras() {
		FlightConfiguration curConfig = document.getSelectedConfiguration();
		extraCG = new CGCaret(0, 0);
		extraCP = new CPCaret(0, 0);
		extraText = new RocketInfo(curConfig);
		
		// Set document-specific text colors if available
		updateTextColors();

		if (caliperManager != null) {
			caliperManager.loadCaliperStateForView(getCurrentViewType());
		}

		updateExtras();
		updateCaliperElements();
	}

	/**
	 * Updates the caliper elements in the figure based on caliperEnabled state.
	 */
	void updateCaliperElements() {
		figure.clearRelativeExtra();
		figure.clearAbsoluteExtra();
		figure.clearRelativeTopExtra();

		// Check if we're in snap mode or dragging
		boolean inSnapMode = caliperManager != null && caliperManager.isSnapModeActive() && !is3d;
		boolean isDragging = caliperManager != null && caliperManager.isDragging() && !is3d;

		// Always show CP and CG carets (even in snap mode or when dragging)
		figure.addRelativeExtra(extraCP);
		figure.addRelativeExtra(extraCG);

		if (inSnapMode) {
			// In snap mode: show snap mode message instead of RocketInfo
			Integer activeCaliper = caliperManager.getActiveSnapCaliper();
			if (activeCaliper != null) {
				info.openrocket.swing.gui.figureelements.SnapModeInfo snapInfo =
						new info.openrocket.swing.gui.figureelements.SnapModeInfo(activeCaliper);
				figure.addAbsoluteExtra(snapInfo);
			}
		} else if (!isDragging) {
			// Normal mode: show RocketInfo text (not when dragging)
			figure.addAbsoluteExtra(extraText);
		}
		// When dragging, don't show RocketInfo (but CP/CG are still shown above)

		if (caliperManager != null && caliperManager.isEnabled() && !is3d) {
			caliperManager.updateCaliperElements();

			// Add snap target highlights
			if (inSnapMode) {
				// If always show is enabled (debug mode), show all snap targets
				if (caliperManager.isAlwaysShowSnapTargets()) {
					for (CaliperSnapTarget target : caliperManager.getCurrentSnapTargets()) {
						info.openrocket.swing.gui.figureelements.SnapTargetHighlight highlight =
								new info.openrocket.swing.gui.figureelements.SnapTargetHighlight(target, figure.getCurrentViewType());
						figure.addRelativeTopExtra(highlight);
			}
		} else {
					// Normal mode: only show hovered target
					CaliperSnapTarget hoveredTarget =
							caliperManager.getHoveredSnapTarget();
					if (hoveredTarget != null) {
						info.openrocket.swing.gui.figureelements.SnapTargetHighlight highlight =
								new info.openrocket.swing.gui.figureelements.SnapTargetHighlight(hoveredTarget, figure.getCurrentViewType());
						figure.addRelativeTopExtra(highlight);
					}
				}
			}

			// Show shift-drag snapped target highlight (even when not in explicit snap mode)
			CaliperSnapTarget shiftDragTarget = caliperManager.getShiftDragSnappedTarget();
			if (shiftDragTarget != null) {
				info.openrocket.swing.gui.figureelements.SnapTargetHighlight highlight =
						new info.openrocket.swing.gui.figureelements.SnapTargetHighlight(shiftDragTarget, figure.getCurrentViewType());
				figure.addRelativeTopExtra(highlight);
			}
		}

		figure3d.clearRelativeExtra();
		figure3d.clearAbsoluteExtra();
		//figure3d.addRelativeExtra(extraCP);
		//figure3d.addRelativeExtra(extraCG);
		figure3d.addAbsoluteExtra(extraText);
	}


	/**
	 * Capture a preview image of the rocket in the specified view type and size.
	 * @param viewType the view type to capture
	 * @param targetWidth the target width of the image
	 * @param minHeight the minimum height of the image
	 * @param maxHeight the maximum height of the image
	 * @return the captured image, or null if 3D preview is requested
	 */
	public BufferedImage capturePreviewImage(VIEW_TYPE viewType, int targetWidth, int minHeight, int maxHeight) {
		final BufferedImage source;

		if (viewType.is3d) {
			source = create3DPreviewFigure(viewType, targetWidth, minHeight, maxHeight);
		} else {
			source = create2DPreviewFigure(viewType, targetWidth, minHeight, maxHeight);
		}
		return scaleForPreview(source, targetWidth, minHeight, maxHeight);
	}

	public byte[] createPreviewPng(VIEW_TYPE requestedView, int targetWidth, int minHeight, int maxHeight) {
		BufferedImage image = capturePreviewImage(requestedView, targetWidth, minHeight, maxHeight);
		if (image == null) {
			return null;
		}
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", output);
			return output.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to encode preview image", e);
		}
	}

	public boolean attachPreviewToDocument(OpenRocketDocument doc, VIEW_TYPE requestedView, int targetWidth,
										   int minHeight, int maxHeight) {
		byte[] png = createPreviewPng(requestedView, targetWidth, minHeight, maxHeight);
		if (png == null || png.length == 0) {
			doc.getDefaultStorageOptions().clearPreviewImage();
			return false;
		}
		doc.getDefaultStorageOptions().setPreviewImage(png);
		return true;
	}

	/**
	 * Updates the background colors of the 2D and 3D figures from document preferences or defaults.
	 * Uses document preference if set, otherwise falls back to SwingPreferences default, otherwise theme default.
	 */
	public void updateBackgroundColors() {
		DocumentPreferences docPrefs = document.getDocumentPreferences();
		SwingPreferences swingPrefs = (SwingPreferences) Application.getPreferences();

		// Update 2D view background: document preference -> SwingPreferences default -> theme default (null)
		Color docColor2D = docPrefs.getColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, null);
		Color defaultColor2D = swingPrefs.getDefault2DBackgroundColor();
		Color color2D = docColor2D != null ? docColor2D :
			(defaultColor2D != null ? defaultColor2D : null);
		if (figure != null) {
			figure.setCustomBackgroundColor(color2D); // null means use theme default
		}

		// Update 3D view background: document preference -> SwingPreferences default -> theme default (null)
		Color docColor3D = docPrefs.getColor(DocumentPreferences.PREF_3D_BACKGROUND_COLOR, null);
		Color defaultColor3D = swingPrefs.getDefault3DBackgroundColor();
		Color color3D = docColor3D != null ? docColor3D :
			(defaultColor3D != null ? defaultColor3D : null);
		if (figure3d != null) {
			figure3d.setCustomBackgroundColor(color3D); // null means use theme default
		}
	}

	/**
	 * Updates the text colors of the design view from document preferences or defaults.
	 * Uses document preference if set, otherwise falls back to SwingPreferences default, otherwise theme default.
	 */
	public void updateTextColors() {
		DocumentPreferences docPrefs = document.getDocumentPreferences();
		SwingPreferences swingPrefs = (SwingPreferences) Application.getPreferences();

		if (extraText != null) {
			// Get 2D text color: document preference -> SwingPreferences default -> theme default (null)
			Color doc2DTextColor = docPrefs.getColor(DocumentPreferences.PREF_2D_TEXT_COLOR, null);
			Color default2DTextColor = swingPrefs.getDefault2DTextColor();
			Color textColor2D = doc2DTextColor != null ? doc2DTextColor :
				(default2DTextColor != null ? default2DTextColor : null);

			// Get 3D text color: document preference -> SwingPreferences default -> theme default (null)
			Color doc3DTextColor = docPrefs.getColor(DocumentPreferences.PREF_3D_TEXT_COLOR, null);
			Color default3DTextColor = swingPrefs.getDefault3DTextColor();
			Color textColor3D = doc3DTextColor != null ? doc3DTextColor :
				(default3DTextColor != null ? default3DTextColor : null);

			// Set the custom text colors (when set, applies to all text types)
			extraText.setCustomTextColors(textColor2D, textColor3D);

			// Set the current view type
			extraText.set3DView(is3d);
		}
	}

	/**
	 * Shows a dialog for configuring display settings (background colors, etc.).
	 */
	private void showDisplaySettingsDialog() {
		DisplaySettingsDialog dialog = new DisplaySettingsDialog(SwingUtilities.getWindowAncestor(this), document);
		dialog.setVisible(true);
		// Update colors after dialog is closed
		updateBackgroundColors();
		updateTextColors();
		updateFigures();
	}

	/**
	 * Shows a dialog asking the user where to save the design view screenshot.
	 */
	private void showCaptureDesignViewDialog() {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
				trans.get("RocketPanel.dlg.captureDesignView.title"), Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new MigLayout("fill, ins 15", "[grow]", "[]15[]"));

		// Where would you like to save the design view screenshot to?
		JLabel messageLabel = new JLabel(trans.get("RocketPanel.dlg.captureDesignView.message"));
		panel.add(messageLabel, "wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("ins 0", "[grow][grow]", "[]"));

		// Save to file
		JButton saveToFileButton = new JButton(trans.get("RocketPanel.btn.saveToFile"), Icons.FILE_SAVE);
		saveToFileButton.addActionListener(e -> {
			dialog.dispose();
			saveDesignViewToFile();
		});
		buttonPanel.add(saveToFileButton, "grow");

		// Copy to clipboard
		JButton copyToClipboardButton = new JButton(trans.get("RocketPanel.btn.copyToClipboard"), Icons.EDIT_COPY);
		copyToClipboardButton.addActionListener(e -> {
			dialog.dispose();
			copyDesignViewToClipboard();
		});
		buttonPanel.add(copyToClipboardButton, "grow");

		panel.add(buttonPanel, "growx");

		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	private BufferedImage captureViewScreenshot() {
		int width = figureHolder.getWidth();
		int height = figureHolder.getHeight();
		BufferedImage image = capturePreviewImage(currentViewType, width, height, height);
		if (image == null) {
			JOptionPane.showMessageDialog(this,
					"Failed to capture design view image.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}

		return image;
	}

	/**
	 * Saves the current design view to a file selected by the user.
	 */
	private void saveDesignViewToFile() {
		BufferedImage image = captureViewScreenshot();
		if (image == null) {
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(trans.get("RocketPanel.dlg.captureDesignView.title"));
		fileChooser.setFileFilter(FileHelper.PNG_FILTER);
		fileChooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());

		// Suggest a default filename based on the rocket name and view type
		String rocketName = document.getRocket().getName();
		if (rocketName == null || rocketName.isEmpty()) {
			rocketName = "rocket";
		}

		// Sanitize filename
		rocketName = rocketName.replaceAll("[^a-zA-Z0-9.-]", "_");
		String viewTypeSuffix = "_" + currentViewType.toString();
		viewTypeSuffix = viewTypeSuffix.replaceAll("[^a-zA-Z0-9.-]", "_");
		fileChooser.setSelectedFile(new File(rocketName + viewTypeSuffix + ".png"));

		int result = fileChooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// Save the image
		File file = fileChooser.getSelectedFile();
		file = FileHelper.forceExtension(file, "png");
		if (FileHelper.confirmWrite(file, RocketPanel.this)) {
			Application.getPreferences().setDefaultDirectory(fileChooser.getCurrentDirectory());
			try {
				ImageIO.write(image, "png", file);
			} catch (IOException ex) {
				log.error("Failed to save design view image", ex);
				JOptionPane.showMessageDialog(this,
						"Failed to save image: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Copies the current design view to the system clipboard on macOS in native PNG format.
	 */
	private void copyDesignViewToClipboardMacOS(BufferedImage image) {
		try {
			// Save to temporary file
			File tempFile = File.createTempFile("clipboard_", ".png");
			ImageIO.write(image, "png", tempFile);

			// Use AppleScript to copy to clipboard in native format
			String script = String.format(
					"set the clipboard to (read (POSIX file \"%s\") as «class PNGf»)",
					tempFile.getAbsolutePath()
			);

			ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
			Process process = pb.start();
			process.waitFor();

			// Clean up
			tempFile.deleteOnExit();

		} catch (IOException | InterruptedException e) {
			log.error("Failed to copy design view to clipboard", e);
		}
	}

	/**
	 * Copies the current design view to the system clipboard.
	 */
	private void copyDesignViewToClipboard() {
		BufferedImage image = captureViewScreenshot();
		if (image == null) {
			return;
		}

		if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
			copyDesignViewToClipboardMacOS(image);
		} else {
			TransferableImage transferableImage = new TransferableImage(image);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(transferableImage, null);
		}
	}

	/**
	 * A Transferable implementation for copying images to the clipboard.
	 */
	private static class TransferableImage implements Transferable {
		private final BufferedImage image;

		public TransferableImage(BufferedImage image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}

	/**
	 * Compute the height that should be used when rendering preview images.
	 * <p>
	 * The value is anchored to the currently visible design panel so previews match
	 * what the user sees, yet it is clamped to any caller-specified {@code minHeight}
	 * or {@code maxHeight} bounds.  A minimum of one pixel is always returned to
	 * protect downstream scaling math.
	 */
	private int resolveRenderHeight(int minHeight, int maxHeight) {
		int base = figureHolder != null && figureHolder.getHeight() > 0 ? figureHolder.getHeight() : 700;
		if (maxHeight > 0) {
			base = Math.min(base, maxHeight);
		}
		if (minHeight > 0) {
			base = Math.max(base, minHeight);
		}
		return Math.max(base, 1);
	}

	/**
	 * Render the current rocket in a temporary 2D figure for use as a preview image.
	 * <p>
	 * The method reuses the live CG/CP carets and {@link RocketInfo} overlay so the preview
	 * carries the same annotations as the editor.  The figure is scaled to fit the supplied
	 * width/height bounds and drawn into a translucent {@link BufferedImage}.
	 */
	private BufferedImage create2DPreviewFigure(VIEW_TYPE viewType, int targetWidth, int minHeight, int maxHeight) {
		if (viewType == null || viewType.is3d) {
			viewType = VIEW_TYPE.getDefaultViewType();
		}

		// Create a temporary figure for rendering
		RocketFigure previewFigure = new RocketFigure(document.getRocket());
		previewFigure.setType(viewType);
		previewFigure.setDrawCarets(true);

		// Apply custom background color to preview figure
		DocumentPreferences docPrefs = document.getDocumentPreferences();
		SwingPreferences swingPrefs = (SwingPreferences) Application.getPreferences();
		Color docColor2D = docPrefs.getColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, null);
		Color defaultColor2D = swingPrefs.getDefault2DBackgroundColor();
		Color color2D = docColor2D != null ? docColor2D :
			(defaultColor2D != null ? defaultColor2D : null);
		previewFigure.setCustomBackgroundColor(color2D); // null means use theme default

		// Ensure text colors are correct for 2D view
		if (extraText != null) {
			extraText.set3DView(false); // 2D view
		}

		previewFigure.addRelativeExtra(extraCP);
		previewFigure.addRelativeExtra(extraCG);
		previewFigure.addAbsoluteExtra(extraText);

		// Scale and layout the figure
		int renderHeight = resolveRenderHeight(minHeight, maxHeight);
		Dimension renderBounds = new Dimension(targetWidth, renderHeight);
		previewFigure.scaleTo(renderBounds);
		previewFigure.updateFigure();

		// Determine canvas size
		Dimension canvasSize = previewFigure.getPreferredSize();
		if (canvasSize.width <= 0 || canvasSize.height <= 0) {
			canvasSize = new Dimension(targetWidth, renderHeight);
		}

		// Layout the figure at the final size
		previewFigure.setSize(canvasSize);
		previewFigure.doLayout();

		// Render the figure into a BufferedImage
		BufferedImage raw = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = raw.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			previewFigure.paint(g2);
		} finally {
			g2.dispose();
		}
		return raw;
	}

	private BufferedImage create3DPreviewFigure(VIEW_TYPE viewType, int targetWidth, int minHeight, int maxHeight) {
		// Only capture if we're currently in 3D mode
		if (currentViewType != viewType) {
			return null;
		}

		// Ensure text colors are correct for 3D view before capture
		if (extraText != null) {
			extraText.set3DView(true); // 3D view
		}

		// Capture the current 3D view
		return figure3d.captureImage();
	}

	/**
	 * Scale the rendered preview so it fits the requested target size while preserving aspect ratio,
	 * adding letter-box style padding when needed.
	 *
	 * @param source      the raw preview image
	 * @param targetWidth width the caller wishes to occupy
	 * @param minHeight   optional minimum canvas height (0 disables)
	 * @param maxHeight   optional maximum canvas height (0 disables)
	 * @return an image ready to be embedded in the ORK archive
	 */
	private BufferedImage scaleForPreview(BufferedImage source, int targetWidth, int minHeight, int maxHeight) {
		if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0 || targetWidth <= 0) {
			return source;
		}

		// Step 1: scale the image so its width matches the requested target while keeping aspect ratio.
		double widthScale = targetWidth / (double) source.getWidth();
		int scaledWidth = targetWidth;
		int scaledHeight = (int) Math.round(source.getHeight() * widthScale);

		// Step 2: if that height would exceed the allowed maximum, recompute using the max height instead.
		if (maxHeight > 0 && scaledHeight > maxHeight) {
			widthScale = maxHeight / (double) source.getHeight();
			scaledWidth = (int) Math.round(source.getWidth() * widthScale);
			scaledHeight = maxHeight;
		}

		if (scaledWidth <= 0) {
			scaledWidth = 1;
		}
		if (scaledHeight <= 0) {
			scaledHeight = 1;
		}

		// Perform the actual resampling with high-quality hints.
		BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaled.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
		} finally {
			g2.dispose();
		}

		// Step 3: determine the final canvas size, padding to meet minHeight/targetWidth when necessary.
		int canvasWidth = targetWidth;
		if (scaledWidth > canvasWidth) {
			canvasWidth = scaledWidth;
		}
		int canvasHeight = scaledHeight;
		if (minHeight > 0 && canvasHeight < minHeight) {
			canvasHeight = minHeight;
		}

		if (maxHeight > 0 && canvasHeight > maxHeight) {
			canvasHeight = maxHeight;
		}

		if (canvasWidth == scaledWidth && canvasHeight == scaledHeight) {
			return scaled;
		}

		// Step 4: center the scaled image on a background tinted to the current UI theme.
		BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D canvasGraphics = canvas.createGraphics();
		try {
			canvasGraphics.setColor(UITheme.getColor(UITheme.Keys.BACKGROUND));
			canvasGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
			int x = (canvasWidth - scaledWidth) / 2;
			int y = (canvasHeight - scaledHeight) / 2;
			canvasGraphics.drawImage(scaled, x, y, null);
		} finally {
			canvasGraphics.dispose();
		}

		return canvas;
	}

	/**
	 * Updates the selection in the FigureParameters and repaints the figure.
	 * Ignores the event itself.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] paths = selectionModel.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			figure.setSelection(null);
			figure3d.setSelection(null);
			return;
		}

		RocketComponent[] components = new RocketComponent[paths.length];
		for (int i = 0; i < paths.length; i++)
			components[i] = (RocketComponent) paths[i].getLastPathComponent();
		figure.setSelection(components);

		figure3d.setSelection(components);
	}

	@Override
	public void onThetaChanged(double theta) {
		setCPTheta(theta);
	}

	@Override
	public void onAOAChanged(double aoa) {
		setCPAOA(aoa);
	}

	@Override
	public void onMachChanged(double mach) {
		setCPMach(mach);
	}

	@Override
	public void onRollRateChanged(double rollRate) {
		setCPRoll(rollRate);
	}

	private static class ViewTypeComboBoxModel extends DefaultComboBoxModel<VIEW_TYPE> {
		public ViewTypeComboBoxModel(VIEW_TYPE[] items, VIEW_TYPE initialItem) {
			super(items);
			super.setSelectedItem(initialItem);
		}
	}

	/**
	 * Custom combobox renderer that supports the display of horizontal separators between items.
	 * ComboBox objects with the text {@link VIEW_TYPE_SEPARATOR} objects in the combobox are replaced by a separator object.
	 */
	private static class SeparatorComboBoxRenderer extends JLabel implements ListCellRenderer {
		private final JSeparator separator;
		private final ListCellRenderer defaultRenderer;

		public SeparatorComboBoxRenderer(ListCellRenderer defaultRenderer) {
			this.defaultRenderer = defaultRenderer;
			this.separator = new JSeparator(JSeparator.HORIZONTAL);
		}

		public Component getListCellRendererComponent(JList list, Object value,
													  int index, boolean isSelected, boolean cellHasFocus) {
			String str = (value == null) ? "" : value.toString();
			if (VIEW_TYPE_SEPARATOR.equals(str)) {
				return separator;
			};
			return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	/**
	 * Build the inline caliper controls panel shown in the ribbon when calipers are enabled.
	 * Contains mode toggle buttons, snap checkbox, unit selector, and live distance display.
	 */
	private JPanel buildCaliperRibbonPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0, fill", "[][]para[]", "[][]"));
		panel.setOpaque(false);

		final Color caliperColor = GUIUtil.getUITheme().getCaliperColor();
		final Color valueBg = GUIUtil.getUITheme().getCaliperValueBackgroundColor();
		final Color valueFg = GUIUtil.getUITheme().getCaliperValueForegroundColor();
		final Color diamondLabelColor = GUIUtil.getUITheme().getCaliperDiamondLabelColor();
		final Color caliperHoverColor = ColorConversion.brightenColor(caliperColor, 50);

		// Mode radio buttons (vertical / horizontal)
		// The icon is kept in a separate JLabel because FlatLaf replaces the radio circle with a
		// custom icon when set directly on the JRadioButton.
		ButtonGroup modeGroup = new ButtonGroup();
		JRadioButton verticalRadio = new JRadioButton();
		JRadioButton horizontalRadio = new JRadioButton();
		JLabel verticalModeIcon = new JLabel(createCaliperDoubleArrowIcon(true, caliperColor));
		JLabel horizontalModeIcon = new JLabel(createCaliperDoubleArrowIcon(false, caliperColor));
		verticalRadio.setToolTipText(trans.get("RocketPanel.radio.CaliperVertical.ttip"));
		horizontalRadio.setToolTipText(trans.get("RocketPanel.radio.CaliperHorizontal.ttip"));
		verticalModeIcon.setToolTipText(trans.get("RocketPanel.radio.CaliperVertical.ttip"));
		horizontalModeIcon.setToolTipText(trans.get("RocketPanel.radio.CaliperHorizontal.ttip"));
		verticalModeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		horizontalModeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		verticalModeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseClicked(java.awt.event.MouseEvent e) { verticalRadio.doClick(); }
		});
		horizontalModeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseClicked(java.awt.event.MouseEvent e) { horizontalRadio.doClick(); }
		});
		modeGroup.add(verticalRadio);
		modeGroup.add(horizontalRadio);
		verticalRadio.setSelected(caliperManager.getMode() == CaliperManager.CaliperMode.VERTICAL);
		horizontalRadio.setSelected(caliperManager.getMode() == CaliperManager.CaliperMode.HORIZONTAL);
		panel.add(verticalRadio, "cell 0 0, split 2");
		panel.add(verticalModeIcon, "cell 0 0");
		panel.add(horizontalRadio, "cell 1 0, split 2");
		panel.add(horizontalModeIcon, "cell 1 0");

		// Snap toggle button
		ThemedToggleButton snapToggle = new ThemedToggleButton(trans.get("RocketPanel.checkbox.CaliperSnap"), Icons.SNAP);
		snapToggle.setToolTipText(trans.get("RocketPanel.checkbox.CaliperSnap.ttip"));
		snapToggle.setSelected(caliperManager.isSnapEnabled());
		snapToggle.addActionListener(e -> caliperManager.setSnapEnabled(snapToggle.isSelected()));
		panel.add(snapToggle, "cell 2 0");

		// Units + distance in one dedicated panel spanning cols 1–2 in row 1
		JPanel unitsDistPanel = new JPanel(new MigLayout("ins 0", "[]4[]para[]", ""));
		unitsDistPanel.setOpaque(false);
		unitsDistPanel.add(new JLabel(trans.get("RocketPanel.lbl.CaliperUnits")));
		unitsDistPanel.add(caliperManager.getUnitSelector());

		// Distance display
		JTextField distanceField = new JTextField("–", 6);
		distanceField.setEditable(false);
		distanceField.setOpaque(true);
		distanceField.setBackground(valueBg);
		distanceField.setForeground(valueFg);
		distanceField.setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.LineBorder(caliperColor, 1, true),
				new javax.swing.border.EmptyBorder(1, 4, 1, 4)));
		distanceField.setFont(distanceField.getFont().deriveFont(Font.BOLD));
		distanceField.setHorizontalAlignment(JTextField.CENTER);

		JPanel distancePanel = new JPanel(new MigLayout("ins 0", "[]2[]2[]2[]2[]", ""));
		distancePanel.setOpaque(false);

		JButton diamond1Btn = new JButton(createCaliperDiamondIcon("1", caliperColor, diamondLabelColor));
		diamond1Btn.setRolloverIcon(createCaliperDiamondIcon("1", caliperHoverColor, diamondLabelColor));
		diamond1Btn.setRolloverEnabled(true);
		diamond1Btn.setBorderPainted(false);
		diamond1Btn.setContentAreaFilled(false);
		diamond1Btn.setFocusPainted(false);
		diamond1Btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
		diamond1Btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		diamond1Btn.setToolTipText(trans.get("RocketPanel.popup.CaliperDiamond1.ttip"));
		diamond1Btn.addActionListener(e -> showCaliperPositionPopup(1, diamond1Btn));

		JButton diamond2Btn = new JButton(createCaliperDiamondIcon("2", caliperColor, diamondLabelColor));
		diamond2Btn.setRolloverIcon(createCaliperDiamondIcon("2", caliperHoverColor, diamondLabelColor));
		diamond2Btn.setRolloverEnabled(true);
		diamond2Btn.setBorderPainted(false);
		diamond2Btn.setContentAreaFilled(false);
		diamond2Btn.setFocusPainted(false);
		diamond2Btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
		diamond2Btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		diamond2Btn.setToolTipText(trans.get("RocketPanel.popup.CaliperDiamond2.ttip"));
		diamond2Btn.addActionListener(e -> showCaliperPositionPopup(2, diamond2Btn));

		JLabel leftArrow = new JLabel(createCaliperSingleArrowIcon(true, caliperColor));
		JLabel rightArrow = new JLabel(createCaliperSingleArrowIcon(false, caliperColor));
		distancePanel.add(diamond1Btn);
		distancePanel.add(leftArrow);
		distancePanel.add(distanceField);
		distancePanel.add(rightArrow);
		distancePanel.add(diamond2Btn);
		unitsDistPanel.add(distancePanel);
		panel.add(unitsDistPanel, "cell 0 1, spanx 3");

		// Live distance update
		final StateChangeListener[] listenerRef = new StateChangeListener[1];
		final DoubleModel[] modelRef = new DoubleModel[1];

		Runnable setupDistanceListener = () -> {
			if (listenerRef[0] != null && modelRef[0] != null) {
				modelRef[0].removeChangeListener(listenerRef[0]);
			}
			modelRef[0] = caliperManager.getCurrentDistanceModel();
			listenerRef[0] = (EventObject ev) -> SwingUtilities.invokeLater(() -> {
				DoubleModel m = caliperManager.getCurrentDistanceModel();
				distanceField.setText(m.getCurrentUnit().toString(m.getValue()));
			});
			modelRef[0].addChangeListener(listenerRef[0]);
			// Initial update
			distanceField.setText(modelRef[0].getCurrentUnit().toString(modelRef[0].getValue()));
		};
		setupDistanceListener.run();

		caliperManager.getUnitSelector().addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				DoubleModel m = caliperManager.getCurrentDistanceModel();
				distanceField.setText(m.getCurrentUnit().toString(m.getValue()));
			}
		});

		// Mode button listeners
		verticalRadio.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				caliperManager.setMode(CaliperManager.CaliperMode.VERTICAL);
				setupDistanceListener.run();
			}
		});
		horizontalRadio.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				caliperManager.setMode(CaliperManager.CaliperMode.HORIZONTAL);
				setupDistanceListener.run();
			}
		});

		// Update colors when the UI theme changes
		UITheme.Theme.addUIThemeChangeListener(() -> {
			Color newCaliperColor = GUIUtil.getUITheme().getCaliperColor();
			Color newValueBg = GUIUtil.getUITheme().getCaliperValueBackgroundColor();
			Color newValueFg = GUIUtil.getUITheme().getCaliperValueForegroundColor();
			Color newDiamondLabelColor = GUIUtil.getUITheme().getCaliperDiamondLabelColor();

			verticalModeIcon.setIcon(createCaliperDoubleArrowIcon(true, newCaliperColor));
			horizontalModeIcon.setIcon(createCaliperDoubleArrowIcon(false, newCaliperColor));

			leftArrow.setIcon(createCaliperSingleArrowIcon(true, newCaliperColor));
			rightArrow.setIcon(createCaliperSingleArrowIcon(false, newCaliperColor));

			distanceField.setBackground(newValueBg);
			distanceField.setForeground(newValueFg);
			distanceField.setBorder(new javax.swing.border.CompoundBorder(
					new javax.swing.border.LineBorder(newCaliperColor, 1, true),
					new javax.swing.border.EmptyBorder(1, 4, 1, 4)));

			Color newHoverColor = ColorConversion.brightenColor(newCaliperColor, 50);
			diamond1Btn.setIcon(createCaliperDiamondIcon("1", newCaliperColor, newDiamondLabelColor));
			diamond1Btn.setRolloverIcon(createCaliperDiamondIcon("1", newHoverColor, newDiamondLabelColor));
			diamond2Btn.setIcon(createCaliperDiamondIcon("2", newCaliperColor, newDiamondLabelColor));
			diamond2Btn.setRolloverIcon(createCaliperDiamondIcon("2", newHoverColor, newDiamondLabelColor));

			panel.repaint();
		});

		return panel;
	}

	/**
	 * Shows a small popup below the given ribbon component, allowing position editing and snap entry
	 * for the specified caliper.
	 *
	 * @param caliperNumber which caliper (1 or 2)
	 * @param source        the ribbon component to anchor the popup below
	 */
	private void showCaliperPositionPopup(int caliperNumber, Component source) {
		double currentPos = caliperManager.getCaliperPosition(caliperNumber);
		if (Double.isNaN(currentPos)) return;

		DoubleModel positionModel = new DoubleModel(currentPos, UnitGroup.UNITS_LENGTH);
		positionModel.setCurrentUnit(caliperManager.getUnitSelector().getSelectedUnit());
		positionModel.addChangeListener((EventObject e) ->
				caliperManager.setCaliperPosition(caliperNumber, positionModel.getValue()));

		JPanel content = new JPanel(new MigLayout("ins 6, fill", "[]4[grow]4[]4[]", "[]"));
		content.add(new JLabel(trans.get("RocketPanel.popup.CaliperPosition")));

		JSpinner spinner = new EditableSpinner(positionModel.getSpinnerModel());
		((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(6);
		content.add(spinner, "growx");

		content.add(new UnitSelector(positionModel));

		JToggleButton snapBtn = new JToggleButton(Icons.SNAP_CLICK);
		snapBtn.setToolTipText(trans.get("RocketPanel.popup.CaliperSnapClick.ttip"));
		snapBtn.setSelected(caliperManager.isSnapModeActive()
				&& Integer.valueOf(caliperNumber).equals(caliperManager.getActiveSnapCaliper()));

		JPopupMenu popup = new JPopupMenu();
		snapBtn.addActionListener(e -> {
			if (snapBtn.isSelected()) {
				caliperManager.enterSnapMode(caliperNumber);
			} else {
				caliperManager.exitSnapMode();
			}
			popup.setVisible(false);
		});
		content.add(snapBtn);

		popup.add(content);
		popup.show(source, 0, source.getHeight());
	}

	/**
	 * Create a twin-arrow icon for the caliper mode radio buttons.
	 * Vertical mode shows two upward arrows side by side (↑↑);
	 * horizontal mode shows two leftward arrows stacked (←←).
	 *
	 * @param vertical true for vertical mode (↑↑), false for horizontal (←←)
	 * @param color    the arrow color
	 * @return an ImageIcon of the twin arrows
	 */
	private ImageIcon createCaliperDoubleArrowIcon(boolean vertical, Color color) {
		int size = 16;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);

		int head = 4; // arrowhead depth
		int hw = 3;   // arrowhead half-width
		int stemTop = head + 1;
		int stemBot = size - 2;

		g2.setStroke(new BasicStroke(1.5f));
		if (vertical) {
			// Two upward arrows side by side at x=4 and x=11
			for (int cx : new int[]{4, 11}) {
				g2.drawLine(cx, stemTop, cx, stemBot);
				g2.fillPolygon(new int[]{cx - hw, cx, cx + hw}, new int[]{head, 0, head}, 3);
			}
		} else {
			// Two leftward arrows stacked at y=4 and y=11
			for (int cy : new int[]{4, 11}) {
				g2.drawLine(stemTop, cy, stemBot, cy);
				g2.fillPolygon(new int[]{head, 0, head}, new int[]{cy - hw, cy, cy + hw}, 3);
			}
		}

		g2.dispose();
		return new ImageIcon(image);
	}

	/**
	 * Create a single-headed arrow icon (← or →) for the caliper distance display.
	 *
	 * @param left  true for a left-pointing arrow (←), false for right-pointing (→)
	 * @param color the arrow color
	 * @return an ImageIcon of the single-headed arrow
	 */
	private ImageIcon createCaliperSingleArrowIcon(boolean left, Color color) {
		int w = 12, h = 10;
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);

		int head = 4;
		int cy = h / 2;

		g2.setStroke(new BasicStroke(1.5f));
		if (left) {
			g2.drawLine(head, cy, w - 1, cy);
			g2.fillPolygon(new int[]{head, 0, head}, new int[]{cy - head, cy, cy + head}, 3);
		} else {
			g2.drawLine(0, cy, w - head, cy);
			g2.fillPolygon(new int[]{w - head, w, w - head}, new int[]{cy - head, cy, cy + head}, 3);
		}

		g2.dispose();
		return new ImageIcon(image);
	}

	/**
	 * Create a small diamond icon with a number label inside, using the given color.
	 *
	 * @param number the label to draw inside the diamond ("1" or "2")
	 * @param color  the fill color of the diamond
	 * @return an ImageIcon of the diamond
	 */
	private ImageIcon createCaliperDiamondIcon(String number, Color color, Color labelColor) {
		int size = 18;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int half = size / 2;
		int[] xPts = {half, size - 1, half, 0};
		int[] yPts = {0, half, size - 1, half};
		g2.setColor(color);
		g2.fillPolygon(xPts, yPts, 4);
		Color border = new Color(
				Math.max(0, color.getRed() - 80),
				Math.max(0, color.getGreen() - 80),
				Math.max(0, color.getBlue() - 80));
		g2.setColor(border);
		g2.drawPolygon(xPts, yPts, 4);
		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
		FontMetrics fm = g2.getFontMetrics();
		int tx = (size - fm.stringWidth(number)) / 2;
		int ty = half + fm.getAscent() / 2 - 1;
		g2.setColor(labelColor);
		g2.drawString(number, tx, ty);
		g2.dispose();
		return new ImageIcon(image);
	}

}
