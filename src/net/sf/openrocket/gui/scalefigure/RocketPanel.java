package net.sf.openrocket.gui.scalefigure;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.MotorConfigurationModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StageSelector;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.figureelements.CGCaret;
import net.sf.openrocket.gui.figureelements.CPCaret;
import net.sf.openrocket.gui.figureelements.Caret;
import net.sf.openrocket.gui.figureelements.RocketInfo;
import net.sf.openrocket.gui.main.SimulationWorker;
import net.sf.openrocket.gui.main.componenttree.ComponentTreeModel;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.simulation.listeners.system.ApogeeEndListener;
import net.sf.openrocket.simulation.listeners.system.InterruptListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A JPanel that contains a RocketFigure and buttons to manipulate the figure. 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketPanel extends JPanel implements TreeSelectionListener, ChangeSource {
	
	private static final Translator trans = Application.getTranslator();
	private final RocketFigure figure;
	private final ScaleScrollPane scrollPane;
	
	private JLabel infoMessage;
	
	private TreeSelectionModel selectionModel = null;
	

	/* Calculation of CP and CG */
	private AerodynamicCalculator aerodynamicCalculator;
	private MassCalculator massCalculator;
	

	private final OpenRocketDocument document;
	private final Configuration configuration;
	
	private Caret extraCP = null;
	private Caret extraCG = null;
	private RocketInfo extraText = null;
	

	private double cpAOA = Double.NaN;
	private double cpTheta = Double.NaN;
	private double cpMach = Double.NaN;
	private double cpRoll = Double.NaN;
	
	// The functional ID of the rocket that was simulated
	private int flightDataFunctionalID = -1;
	private String flightDataMotorID = null;
	

	private SimulationWorker backgroundSimulationWorker = null;
	
	private List<EventListener> listeners = new ArrayList<EventListener>();
	

	/**
	 * The executor service used for running the background simulations.
	 * This uses a fixed-sized thread pool for all background simulations
	 * with all threads in daemon mode and with minimum priority.
	 */
	private static final Executor backgroundSimulationExecutor;
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
	
	
	public RocketPanel(OpenRocketDocument document) {
		
		this.document = document;
		configuration = document.getDefaultConfiguration();
		
		// TODO: FUTURE: calculator selection
		aerodynamicCalculator = new BarrowmanCalculator();
		massCalculator = new BasicMassCalculator();
		
		// Create figure and custom scroll pane
		figure = new RocketFigure(configuration);
		
		scrollPane = new ScaleScrollPane(figure) {
			@Override
			public void mouseClicked(MouseEvent event) {
				handleMouseClick(event);
			}
		};
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setFitting(true);
		
		createPanel();
		
		configuration.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				// System.out.println("Configuration changed, calling updateFigure");
				updateExtras();
				figure.updateFigure();
			}
		});
	}
	
	
	/**
	 * Creates the layout and components of the panel.
	 */
	private void createPanel() {
		setLayout(new MigLayout("", "[shrink][grow]", "[shrink][shrink][grow][shrink]"));
		
		setPreferredSize(new Dimension(800, 300));
		

		//// Create toolbar
		
		// Side/back buttons
		FigureTypeAction action = new FigureTypeAction(RocketFigure.TYPE_SIDE);
		//// Side view
		action.putValue(Action.NAME, trans.get("RocketPanel.FigTypeAct.Sideview"));
		//// Side view
		action.putValue(Action.SHORT_DESCRIPTION, trans.get("RocketPanel.FigTypeAct.ttip.Sideview"));
		JToggleButton toggle = new JToggleButton(action);
		add(toggle, "spanx, split");
		
		action = new FigureTypeAction(RocketFigure.TYPE_BACK);
		//// Back view
		action.putValue(Action.NAME, trans.get("RocketPanel.FigTypeAct.Backview"));
		//// Back view
		action.putValue(Action.SHORT_DESCRIPTION, trans.get("RocketPanel.FigTypeAct.ttip.Backview"));
		toggle = new JToggleButton(action);
		add(toggle, "gap rel");
		

		// Zoom level selector
		ScaleSelector scaleSelector = new ScaleSelector(scrollPane);
		add(scaleSelector);
		


		// Stage selector
		StageSelector stageSelector = new StageSelector(configuration);
		add(stageSelector, "");
		


		// Motor configuration selector
		//// Motor configuration:
		JLabel label = new JLabel(trans.get("RocketPanel.lbl.Motorcfg"));
		label.setHorizontalAlignment(JLabel.RIGHT);
		add(label, "growx, right");
		add(new JComboBox(new MotorConfigurationModel(configuration)), "wrap");
		




		// Create slider and scroll pane
		
		DoubleModel theta = new DoubleModel(figure, "Rotation",
				UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
		UnitSelector us = new UnitSelector(theta, true);
		us.setHorizontalAlignment(JLabel.CENTER);
		add(us, "alignx 50%, growx");
		
		// Add the rocket figure
		add(scrollPane, "grow, spany 2, wmin 300lp, hmin 100lp, wrap");
		

		// Add rotation slider
		// Minimum size to fit "360deg"
		JLabel l = new JLabel("360" + Chars.DEGREE);
		Dimension d = l.getPreferredSize();
		
		add(new BasicSlider(theta.getSliderModel(0, 2 * Math.PI), JSlider.VERTICAL, true),
				"ax 50%, wrap, width " + (d.width + 6) + "px:null:null, growy");
		

		//// <html>Click to select &nbsp;&nbsp; Shift+click to select other &nbsp;&nbsp; Double-click to edit &nbsp;&nbsp; Click+drag to move
		infoMessage = new JLabel(trans.get("RocketPanel.lbl.infoMessage"));
		infoMessage.setFont(new Font("Sans Serif", Font.PLAIN, 9));
		add(infoMessage, "skip, span, gapleft 25, wrap");
		

		addExtras();
	}
	
	

	public RocketFigure getFigure() {
		return figure;
	}
	
	public AerodynamicCalculator getAerodynamicCalculator() {
		return aerodynamicCalculator;
	}
	
	public Configuration getConfiguration() {
		return configuration;
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
		figure.updateFigure();
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
		figure.updateFigure();
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
		figure.updateFigure();
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
		figure.updateFigure();
		fireChangeEvent();
	}
	
	

	@Override
	public void addChangeListener(EventListener listener) {
		listeners.add(0, listener);
	}
	
	@Override
	public void removeChangeListener(EventListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
		EventObject e = new EventObject(this);
		for (EventListener l : listeners) {
			if ( l instanceof StateChangeListener ) {
				((StateChangeListener)l).stateChanged(e);
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
	
	private void handleMouseClick(MouseEvent event) {
		if (event.getButton() != MouseEvent.BUTTON1)
			return;
		Point p0 = event.getPoint();
		Point p1 = scrollPane.getViewport().getViewPosition();
		int x = p0.x + p1.x;
		int y = p0.y + p1.y;
		
		RocketComponent[] clicked = figure.getComponentsByPoint(x, y);
		
		// If no component is clicked, do nothing
		if (clicked.length == 0)
			return;
		
		// Check whether the currently selected component is in the clicked components.
		TreePath path = selectionModel.getSelectionPath();
		if (path != null) {
			RocketComponent current = (RocketComponent) path.getLastPathComponent();
			path = null;
			for (int i = 0; i < clicked.length; i++) {
				if (clicked[i] == current) {
					if (event.isShiftDown() && (event.getClickCount() == 1)) {
						path = ComponentTreeModel.makeTreePath(clicked[(i + 1) % clicked.length]);
					} else {
						path = ComponentTreeModel.makeTreePath(clicked[i]);
					}
					break;
				}
			}
		}
		
		// Currently selected component not clicked
		if (path == null) {
			if (event.isShiftDown() && event.getClickCount() == 1 && clicked.length > 1) {
				path = ComponentTreeModel.makeTreePath(clicked[1]);
			} else {
				path = ComponentTreeModel.makeTreePath(clicked[0]);
			}
		}
		
		// Set selection and check for double-click
		selectionModel.setSelectionPath(path);
		if (event.getClickCount() == 2) {
			RocketComponent component = (RocketComponent) path.getLastPathComponent();
			
			ComponentConfigDialog.showDialog(SwingUtilities.getWindowAncestor(this),
					document, component);
		}
	}
	
	


	/**
	 * Updates the extra data included in the figure.  Currently this includes
	 * the CP and CG carets.
	 */
	private WarningSet warnings = new WarningSet();
	
	private void updateExtras() {
		Coordinate cp, cg;
		double cpx, cgx;
		
		// TODO: MEDIUM: User-definable conditions
		FlightConditions conditions = new FlightConditions(configuration);
		warnings.clear();
		
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
			cp = aerodynamicCalculator.getCP(configuration, conditions, warnings);
		} else {
			cp = aerodynamicCalculator.getWorstCP(configuration, conditions, warnings);
		}
		extraText.setTheta(cpTheta);
		

		cg = massCalculator.getCG(configuration, MassCalcType.LAUNCH_MASS);
		//		System.out.println("CG computed as "+cg+ " CP as "+cp);
		
		if (cp.weight > 0.000001)
			cpx = cp.x;
		else
			cpx = Double.NaN;
		
		if (cg.weight > 0.000001)
			cgx = cg.x;
		else
			cgx = Double.NaN;
		
		// Length bound is assumed to be tight
		double length = 0, diameter = 0;
		Collection<Coordinate> bounds = configuration.getBounds();
		if (!bounds.isEmpty()) {
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (Coordinate c : bounds) {
				if (c.x < minX)
					minX = c.x;
				if (c.x > maxX)
					maxX = c.x;
			}
			length = maxX - minX;
		}
		
		for (RocketComponent c : configuration) {
			if (c instanceof SymmetricComponent) {
				double d1 = ((SymmetricComponent) c).getForeRadius() * 2;
				double d2 = ((SymmetricComponent) c).getAftRadius() * 2;
				diameter = MathUtil.max(diameter, d1, d2);
			}
		}
		
		extraText.setCG(cgx);
		extraText.setCP(cpx);
		extraText.setLength(length);
		extraText.setDiameter(diameter);
		extraText.setMass(cg.weight);
		extraText.setWarnings(warnings);
		

		if (figure.getType() == RocketFigure.TYPE_SIDE && length > 0) {
			
			// TODO: LOW: Y-coordinate and rotation
			extraCP.setPosition(cpx * RocketFigure.EXTRA_SCALE, 0);
			extraCG.setPosition(cgx * RocketFigure.EXTRA_SCALE, 0);
			
		} else {
			
			extraCP.setPosition(Double.NaN, Double.NaN);
			extraCG.setPosition(Double.NaN, Double.NaN);
			
		}
		

		////////  Flight simulation in background
		
		// Check whether to compute or not
		if (!((SwingPreferences) Application.getPreferences()).computeFlightInBackground()) {
			extraText.setFlightData(null);
			extraText.setCalculatingData(false);
			stopBackgroundSimulation();
			return;
		}
		
		// Check whether data is already up to date
		if (flightDataFunctionalID == configuration.getRocket().getFunctionalModID() &&
				flightDataMotorID == configuration.getMotorConfigurationID()) {
			return;
		}
		
		flightDataFunctionalID = configuration.getRocket().getFunctionalModID();
		flightDataMotorID = configuration.getMotorConfigurationID();
		
		// Stop previous computation (if any)
		stopBackgroundSimulation();
		
		// Check that configuration has motors
		if (!configuration.hasMotors()) {
			extraText.setFlightData(FlightData.NaN_DATA);
			extraText.setCalculatingData(false);
			return;
		}
		
		// Start calculation process
		extraText.setCalculatingData(true);
		
		Rocket duplicate = (Rocket) configuration.getRocket().copy();
		Simulation simulation = ((SwingPreferences)Application.getPreferences()).getBackgroundSimulation(duplicate);
		simulation.getOptions().setMotorConfigurationID(
				configuration.getMotorConfigurationID());
		
		backgroundSimulationWorker = new BackgroundSimulationWorker(simulation);
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
		
		public BackgroundSimulationWorker(Simulation sim) {
			super(sim);
		}
		
		@Override
		protected FlightData doInBackground() {
			
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
			extraText.setFlightData(simulation.getSimulatedData());
			extraText.setCalculatingData(false);
			figure.repaint();
		}
		
		@Override
		protected SimulationListener[] getExtraListeners() {
			return new SimulationListener[] {
					InterruptListener.INSTANCE,
					ApogeeEndListener.INSTANCE };
		}
		
		@Override
		protected void simulationInterrupted(Throwable t) {
			// Do nothing on cancel, set N/A data otherwise
			if (isCancelled() || backgroundSimulationWorker != this) // Double-check
				return;
			
			backgroundSimulationWorker = null;
			extraText.setFlightData(FlightData.NaN_DATA);
			extraText.setCalculatingData(false);
			figure.repaint();
		}
	}
	
	

	/**
	 * Adds the extra data to the figure.  Currently this includes the CP and CG carets.
	 */
	private void addExtras() {
		figure.clearRelativeExtra();
		extraCG = new CGCaret(0, 0);
		extraCP = new CPCaret(0, 0);
		extraText = new RocketInfo(configuration);
		updateExtras();
		figure.addRelativeExtra(extraCP);
		figure.addRelativeExtra(extraCG);
		figure.addAbsoluteExtra(extraText);
	}
	
	
	/**
	 * Updates the selection in the FigureParameters and repaints the figure.  
	 * Ignores the event itself.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] paths = selectionModel.getSelectionPaths();
		if (paths == null) {
			figure.setSelection(null);
			return;
		}
		
		RocketComponent[] components = new RocketComponent[paths.length];
		for (int i = 0; i < paths.length; i++)
			components[i] = (RocketComponent) paths[i].getLastPathComponent();
		figure.setSelection(components);
	}
	
	

	/**
	 * An <code>Action</code> that shows whether the figure type is the type
	 * given in the constructor.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private class FigureTypeAction extends AbstractAction implements StateChangeListener {
		private final int type;
		
		public FigureTypeAction(int type) {
			this.type = type;
			stateChanged(null);
			figure.addChangeListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean state = (Boolean) getValue(Action.SELECTED_KEY);
			if (state == true) {
				// This view has been selected
				figure.setType(type);
				updateExtras();
			}
			stateChanged(null);
		}
		
		@Override
		public void stateChanged(EventObject e) {
			putValue(Action.SELECTED_KEY, figure.getType() == type);
		}
	}
	
}
