package info.openrocket.swing.gui.dialogs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RadiusRingComponent;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.ThicknessRingComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Reflection;
import info.openrocket.core.util.Reflection.Method;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.GUIUtil;

/**
 * Dialog that allows scaling the rocket design.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ScaleDialog extends JDialog {
	private static final long serialVersionUID = -8558418577377862794L;
	private static final Logger log = LoggerFactory.getLogger(ScaleDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	
	/*
	 * Scaler implementations
	 * 
	 * Each scaled value (except override cg/mass) is defined using a Scaler instance.
	 * There are two scaler instances; one for when the offset distances (axial/radial offset) don't need to be scaled
	 * together with the other dimensions and one for when the offsets do need to scale.
	 */
	private static final Map<Class<? extends RocketComponent>, List<Scaler>> SCALERS_NO_OFFSET = new HashMap<>();
	private static final Map<Class<? extends RocketComponent>, List<Scaler>> SCALERS_OFFSET = new HashMap<>();
	static {
		List<Scaler> list;

		// RocketComponent
		addScaler(RocketComponent.class, "AxialOffset", SCALERS_OFFSET);
		SCALERS_OFFSET.get(RocketComponent.class).add(new OverrideScaler());

		// ComponentAssembly
		addScaler(ParallelStage.class, "RadiusOffset", SCALERS_OFFSET);
		addScaler(PodSet.class, "RadiusOffset", SCALERS_OFFSET);

		// BodyComponent
		addScaler(BodyComponent.class, "Length", SCALERS_NO_OFFSET);
		
		// SymmetricComponent
		addScaler(SymmetricComponent.class, "Thickness", "isFilled", SCALERS_NO_OFFSET);
		
		// Transition
		list = new ArrayList<>(1);
		list.add(new TransitionScaler());
		SCALERS_NO_OFFSET.put(Transition.class, list);
		addScaler(Transition.class, "ForeShoulderThickness", SCALERS_NO_OFFSET);
		addScaler(Transition.class, "ForeShoulderLength", SCALERS_NO_OFFSET);
		addScaler(Transition.class, "AftShoulderThickness", SCALERS_NO_OFFSET);
		addScaler(Transition.class, "AftShoulderLength", SCALERS_NO_OFFSET);

		// Nose cone
		list = new ArrayList<>(1);
		list.add(new NoseConeScaler());
		SCALERS_NO_OFFSET.put(NoseCone.class, list);
		addScaler(NoseCone.class, "ShoulderThickness", SCALERS_NO_OFFSET);
		addScaler(NoseCone.class, "ShoulderLength", SCALERS_NO_OFFSET);
		
		// Body tube
		addScaler(BodyTube.class, "OuterRadius", "isOuterRadiusAutomatic", SCALERS_NO_OFFSET);
		addScaler(BodyTube.class, "MotorOverhang", SCALERS_NO_OFFSET);

		// Rail button
		list = new ArrayList<>(1);
		list.add(new RailButtonScaler());
		SCALERS_NO_OFFSET.put(RailButton.class, list);
		addScaler(RailButton.class, "InstanceSeparation", SCALERS_OFFSET);

		// Launch lug
		addScaler(LaunchLug.class, "OuterRadius", SCALERS_NO_OFFSET);
		addScaler(LaunchLug.class, "Thickness", SCALERS_NO_OFFSET);
		addScaler(LaunchLug.class, "Length", SCALERS_NO_OFFSET);
		
		// FinSet
		list = new ArrayList<>(1);
		list.add(new FinSetScaler());
		SCALERS_NO_OFFSET.put(FinSet.class, list);
		
		// TrapezoidFinSet
		addScaler(TrapezoidFinSet.class, "Sweep", SCALERS_NO_OFFSET);
		addScaler(TrapezoidFinSet.class, "RootChord", SCALERS_NO_OFFSET);
		addScaler(TrapezoidFinSet.class, "TipChord", SCALERS_NO_OFFSET);
		addScaler(TrapezoidFinSet.class, "Height", SCALERS_NO_OFFSET);
		
		// EllipticalFinSet
		addScaler(EllipticalFinSet.class, "Length", SCALERS_NO_OFFSET);
		addScaler(EllipticalFinSet.class, "Height", SCALERS_NO_OFFSET);
		
		// FreeformFinSet
		list = new ArrayList<>(1);
		list.add(new FreeformFinSetScaler());
		SCALERS_NO_OFFSET.put(FreeformFinSet.class, list);
		
		// MassObject
		addScaler(MassObject.class, "Radius", "isRadiusAutomatic", SCALERS_NO_OFFSET);
		addScaler(MassObject.class, "Length", SCALERS_NO_OFFSET);
		addScaler(MassObject.class, "RadialPosition", SCALERS_OFFSET);
		
		// MassComponent
		list = new ArrayList<>(1);
		list.add(new MassComponentScaler());
		SCALERS_NO_OFFSET.put(MassComponent.class, list);
		
		// Parachute
		addScaler(Parachute.class, "Diameter", SCALERS_NO_OFFSET);
		addScaler(Parachute.class, "LineLength", SCALERS_NO_OFFSET);
		
		// Streamer
		addScaler(Streamer.class, "StripLength", SCALERS_NO_OFFSET);
		addScaler(Streamer.class, "StripWidth", SCALERS_NO_OFFSET);
		
		// ShockCord
		addScaler(ShockCord.class, "CordLength", SCALERS_NO_OFFSET);
		
		// RingComponent
		addScaler(RingComponent.class, "Length", SCALERS_NO_OFFSET);
		addScaler(RingComponent.class, "RadialPosition", SCALERS_OFFSET);
		
		// ThicknessRingComponent
		list = new ArrayList<>(1);
		list.add(new ThicknessRingComponentScaler());
		SCALERS_NO_OFFSET.put(ThicknessRingComponent.class, list);
		
		// InnerTube
		addScaler(InnerTube.class, "MotorOverhang", SCALERS_NO_OFFSET);
		
		// RadiusRingComponent
		list = new ArrayList<>(1);
		list.add(new RadiusRingComponentScaler());
		SCALERS_NO_OFFSET.put(RadiusRingComponent.class, list);
	}
	
	private static void addScaler(Class<? extends RocketComponent> componentClass, String methodName,
								  Map<Class<? extends RocketComponent>, List<Scaler>> scaler, Object... arguments) {
		addScaler(componentClass, methodName, null, scaler, arguments);
	}
	
	private static void addScaler(Class<? extends RocketComponent> componentClass, String methodName, String autoMethodName,
								  Map<Class<? extends RocketComponent>, List<Scaler>> scaler, Object... arguments) {
		List<Scaler> list = scaler.computeIfAbsent(componentClass, k -> new ArrayList<>());
		list.add(new GeneralScaler(componentClass, methodName, autoMethodName, arguments));
	}
	
	
	
	
	
	private static final double DEFAULT_INITIAL_SIZE = 0.1; // meters
	private static final double SCALE_MIN = 0.01;
	private static final double SCALE_MAX = 100.0;
	
	private static final String SCALE_ROCKET = trans.get("lbl.scaleRocket");
	private static final String SCALE_SUBSELECTION = trans.get("lbl.scaleSubselection");
	private static final String SCALE_SELECTION = trans.get("lbl.scaleSelection");
	
	
	
	
	private final DoubleModel multiplier = new DoubleModel(1.0, UnitGroup.UNITS_RELATIVE, SCALE_MIN, SCALE_MAX);
	private UnitSelector multiplierUnit;
	private final DoubleModel fromField = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
	private final DoubleModel toField = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
	
	private final OpenRocketDocument document;
	private final List<RocketComponent> selection;
	private final boolean onlySelection;
	
	private JComboBox<String> selectionOption;
	private JCheckBox scaleMassValues;
	private JCheckBox scaleOffsets;
	
	private boolean changing = false;

	/**
	 * Sole constructor.
	 * 
	 * @param document		the document to modify.
	 * @param selection		the currently selected componentents (or <code>null</code> if none selected).
	 * @param parent		the parent window.
	 */
	public ScaleDialog(OpenRocketDocument document, List<RocketComponent> selection, Window parent) {
		this(document, selection, parent, false);
	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param document		the document to modify.
	 * @param selection		the currently selected component (or <code>null</code> if none selected).
	 * @param parent		the parent window.
	 * @param onlySelection	true to only allow scaling on the selected component (not the whole rocket)
	 */
	public ScaleDialog(OpenRocketDocument document, List<RocketComponent> selection, Window parent, Boolean onlySelection) {
		super(parent, trans.get("title"), ModalityType.APPLICATION_MODAL);
		
		this.document = document;
		this.selection = new ArrayList<>(selection);
		this.onlySelection = onlySelection;
		
		init();
	}
	
	private void init() {
		// Generate options for scaling
		List<String> options = new ArrayList<>();
		if (!onlySelection)
			options.add(SCALE_ROCKET);

		boolean subPartsPresent = false;
		if (selection != null) {
			for (RocketComponent component : selection) {
				if (component.getChildCount() > 0) {
					subPartsPresent = true;
					break;
				}
			}
		}
		if (selection != null && subPartsPresent) {
			options.add(SCALE_SUBSELECTION);
		}

		if (selection != null && selection.size() > 0) {
			options.add(SCALE_SELECTION);
		}
		
		
		/*
		 * Select initial size for "from" field.
		 * 
		 * If a component is selected, either its diameter (for SymmetricComponents) or length is selected.
		 * Otherwise the maximum body diameter is selected.  As a fallback DEFAULT_INITIAL_SIZE is used.
		 */
		double initialSize = 0;
		if (selection != null && selection.size() == 1) {
			RocketComponent component = selection.get(0);
			if (component instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) component;
				initialSize = s.getForeRadius() * 2;
				initialSize = MathUtil.max(initialSize, s.getAftRadius() * 2);
			}else if ((component instanceof ParallelStage) || (component instanceof PodSet )) {
				initialSize = component.getRadiusOffset();
			} else {
				initialSize = component.getLength();
			}
		} else {
			for (RocketComponent c : document.getRocket()) {
				if (c instanceof SymmetricComponent) {
					SymmetricComponent s = (SymmetricComponent) c;
					initialSize = s.getForeRadius() * 2;
					initialSize = MathUtil.max(initialSize, s.getAftRadius() * 2);
				}
			}
		}
		if (initialSize < 0.001) {
			Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
			initialSize = unit.fromUnit(unit.round(unit.toUnit(DEFAULT_INITIAL_SIZE)));
		}
		
		fromField.setValue(initialSize);
		toField.setValue(initialSize);
		
		
		// Add actions to the values
		multiplier.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!changing) {
					changing = true;
					updateToField();
					changing = false;
				}
			}
		});
		fromField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!changing) {
					changing = true;
					updateToField();
					changing = false;
				}
			}
		});
		toField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!changing) {
					changing = true;
					updateMultiplier();
					changing = false;
				}
			}
		});
		
		
		
		String tip;
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));
		this.add(panel);
		
		
		// Scaling selection
		tip = trans.get("lbl.scale.ttip");
		JLabel label = new JLabel(trans.get("lbl.scale"));
		label.setToolTipText(tip);
		panel.add(label, "span, split, gapright unrel");
		
		selectionOption = new JComboBox<>(options.toArray(new String[0]));
		selectionOption.setEditable(false);
		selectionOption.setToolTipText(tip);
		panel.add(selectionOption, "growx, wrap para*2");

		// Select the 'scale component / scale selection and all subcomponents' if a component is selected
		if (options.size() > 1 && selection != null && selection.size() > 0) {
			boolean entireRocket = false;	// Flag to scale entire rocket
			for (RocketComponent component : selection) {
				if (component instanceof Rocket || (component instanceof AxialStage && !(component instanceof ParallelStage))) {
					entireRocket = true;
					break;
				}
			}
			if (!entireRocket) {
				selectionOption.setSelectedIndex(1);
			}
		}

		// Change the offset checkbox to false when 'Scale selection' is selection and only one component is selected,
		// since this is a common action.
		ItemListener listener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (scaleOffsets == null) return;

				scaleOffsets.setSelected(!SCALE_SELECTION.equals(selectionOption.getSelectedItem()));
			}
		};
		selectionOption.addItemListener(listener);
		
		
		// Scale multiplier
		tip = trans.get("lbl.scaling.ttip");
		label = new JLabel(trans.get("lbl.scaling"));
		label.setToolTipText(tip);
		panel.add(label, "gapright unrel");
		
		
		JSpinner spin = new JSpinner(multiplier.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "wmin 40lp, growx 1000");
		
		multiplierUnit = new UnitSelector(multiplier);
		multiplierUnit.setToolTipText(tip);
		panel.add(multiplierUnit, "w 30lp");
		BasicSlider slider = new BasicSlider(multiplier.getSliderModel(0.25, 1.0, 4.0));
		slider.setToolTipText(tip);
		panel.add(slider, "w 100lp, growx, wrap para");
		
		
		// Scale from ... to ...
		tip = trans.get("lbl.scaleFromTo.ttip");
		label = new JLabel(trans.get("lbl.scaleFrom"));
		label.setToolTipText(tip);
		panel.add(label, "gapright unrel, right");
		
		spin = new JSpinner(fromField.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "span, split, wmin 40lp, growx 1000");

		UnitSelector unit = new UnitSelector(fromField);
		unit.setToolTipText(tip);
		panel.add(unit, "w 30lp");
		
		label = new JLabel(trans.get("lbl.scaleTo"));
		label.setToolTipText(tip);
		panel.add(label, "gap unrel");
		
		spin = new JSpinner(toField.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "wmin 40lp, growx 1000");
		
		unit = new UnitSelector(toField);
		unit.setToolTipText(tip);
		panel.add(unit, "w 30lp, wrap para*2");
		
		
		// Scale override
		scaleMassValues = new JCheckBox(trans.get("checkbox.scaleMass"));
		scaleMassValues.setToolTipText(trans.get("checkbox.scaleMass.ttip"));
		scaleMassValues.setSelected(true);
		boolean overridden = false;
		for (RocketComponent c : document.getRocket()) {
			if (c instanceof MassComponent || c.isMassOverridden()) {
				overridden = true;
				break;
			}
		}
		scaleMassValues.setEnabled(overridden);
		panel.add(scaleMassValues, "span, wrap");

		// Scale offsets
		scaleOffsets = new JCheckBox(trans.get("checkbox.scaleOffsets"));
		scaleOffsets.setToolTipText(trans.get("checkbox.scaleOffsets.ttip"));
		listener.itemStateChanged(null);		// Triggers the selection state of scaleOffsets
		panel.add(scaleOffsets, "span, wrap para*3");
		
		
		// Scale / Accept Buttons
		JButton scale = new JButton(trans.get("button.scale"));
		scale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				final Rocket rocket = document.getRocket();
				rocket.enableEvents(false);
				doScale();
				rocket.enableEvents(true);

				ScaleDialog.this.document.getRocket().fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);

				ScaleDialog.this.setVisible(false);
			}
		});

		panel.add(scale, "span, split, right, gap para");

		// Cancel Button
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScaleDialog.this.setVisible(false);
			}
		});
		panel.add(cancel, "right, gap para");
		

		
		GUIUtil.setDisposableDialogOptions(this, scale);
	}
	
	
	
	private void doScale() {
		double mul = multiplier.getValue();
		if (!(SCALE_MIN <= mul && mul <= SCALE_MAX)) {
			if (multiplierUnit == null) {
				Application.getExceptionHandler().handleErrorCondition("Illegal multiplier value, mul=" + mul);
			} else {
				Application.getExceptionHandler().handleErrorCondition("Illegal multiplier value, mul=" + multiplierUnit.getSelectedUnit().toStringUnit(mul));
			}
			return;
		}
		
		if (MathUtil.equals(mul, 1.0)) {
			// Nothing to do
			log.info(Markers.USER_MARKER, "Scaling by value 1.0 - nothing to do");
			return;
		}
		
		boolean scaleMass = scaleMassValues.isSelected();

		// Apply the selected scaling mode
		Iterable<RocketComponent> scaleComponents = selection;
		Object item = selectionOption.getSelectedItem();
		log.info(Markers.USER_MARKER, "Scaling design by factor " + mul + ", option=" + item);
		if (SCALE_ROCKET.equals(item)) {
			document.startUndo(trans.get("undo.scaleRocket"));

			// Scale the entire rocket design
			scaleComponents = document.getRocket();
		} else if (SCALE_SUBSELECTION.equals(item)) {
			document.startUndo(trans.get("undo.scaleComponents"));
			for (RocketComponent component : new ArrayList<>(selection)) {
				addChildrenToSelection(component);
			}
		} else if (SCALE_SELECTION.equals(item)) {
			document.startUndo(trans.get("undo.scaleComponent"));
		} else {
			throw new BugException("Unknown item selected, item=" + item);
		}

		// Perform the scaling
		try {
			// Scale the offsets
			if (scaleOffsets.isSelected()) {
				for (RocketComponent component : scaleComponents) {
					scaleOffset(component, mul, scaleMass);
				}
			}
			// Scale the components
			for (RocketComponent component : scaleComponents) {
				scale(component, mul, scaleMass);
			}
		} finally {
			document.stopUndo();
		}
	}


	/**
	 * Perform scaling on a single component.
	 * @param component component to be scaled
	 * @param mul scaling factor
	 * @param scaleMass flag to check if the mass should be scaled as well
	 */
	private void scale(RocketComponent component, double mul, boolean scaleMass) {
		Class<?> clazz = component.getClass();
		List<Class<?>> classes = new ArrayList<>();
		while (clazz != null) {
			classes.add(clazz);
			clazz = clazz.getSuperclass();
		}
		Collections.reverse(classes);	// Always do the super component scales first (can cause problems otherwise in the scale order)
		for (Class<?> cl : classes) {
			// Don't use the super-class methods of transitions for nose cones
			if (cl == Transition.class && component instanceof NoseCone) {
				continue;
			}
			List<Scaler> list = SCALERS_NO_OFFSET.get(cl);
			if (list != null && list.size() > 0) {
				for (Scaler s : list) {
					s.scale(component, mul, scaleMass);
				}
			}
		}
	}

	/**
	 * Perform scaling of the axial/radial offsets a single component.
	 * @param component component to be scaled
	 * @param mul scaling factor
	 * @param scaleMass flag to check if the mass should be scaled as well
	 */
	private void scaleOffset(RocketComponent component, double mul, boolean scaleMass) {
		Class<?> clazz = component.getClass();
		List<Class<?>> classes = new ArrayList<>();
		while (clazz != null) {
			classes.add(clazz);
			clazz = clazz.getSuperclass();
		}
		Collections.reverse(classes);	// Always do the super component scales first (can cause problems otherwise in the scaleNoOffset order)
		for (Class<?> cl : classes) {
			List<Scaler> list = SCALERS_OFFSET.get(cl);
			if (list != null && list.size() > 0) {
				for (Scaler s : list) {
					s.scale(component, mul, scaleMass);
				}
			}
		}
	}

	/**
	 * Iteratively add the children of component to the component selection list.
	 * @param component component whose children need to be added
	 */
	private void addChildrenToSelection(RocketComponent component) {
		for (RocketComponent child : component.getChildren()) {
			if (!selection.contains(child)) {
				selection.add(child);
			}
			addChildrenToSelection(child);
		}
	}
	
	
	private void updateToField() {
		double mul = multiplier.getValue();
		double from = fromField.getValue();
		double to = from * mul;
		toField.setValue(to);
	}
	
	private void updateMultiplier() {
		double from = fromField.getValue();
		double to = toField.getValue();
		double mul = to / from;
		
		if (!MathUtil.equals(from, 0)) {
			mul = MathUtil.clamp(mul, SCALE_MIN, SCALE_MAX);
			multiplier.setValue(mul);
		}
		updateToField();
	}
	
	
	
	/**
	 * Interface for scaling a specific component/value.
	 */
	private interface Scaler {
		void scale(RocketComponent c, double multiplier, boolean scaleMass);
	}
	
	/**
	 * General scaler implementation that uses reflection to get/set a specific value.
	 */
	private static class GeneralScaler implements Scaler {
		
		private final Method getter;
		private final Method setter;
		private final Method autoMethod;
		private final Object[] arguments;
		
		public GeneralScaler(Class<? extends RocketComponent> componentClass, String methodName, String autoMethodName,
							 Object... arguments) {

			this.getter = Reflection.findMethod(componentClass, "get" + methodName);
			if (arguments == null || arguments.length == 0) {
				this.setter = Reflection.findMethod(componentClass, "set" + methodName, double.class);
			} else {
				Class<?>[] argumentClasses = new Class<?>[arguments.length + 1];
				argumentClasses[0] = double.class;
				for (int i = 0; i < arguments.length; i++) {
					argumentClasses[i+1] = ClassUtils.wrapperToPrimitive(arguments[i].getClass());
				}
				this.setter = Reflection.findMethod(componentClass, "set" + methodName, argumentClasses);
			}
			this.arguments = arguments;
			if (autoMethodName != null) {
				this.autoMethod = Reflection.findMethod(componentClass, autoMethodName);
			} else {
				this.autoMethod = null;
			}
			
		}
		
		@Override
		public void scale(RocketComponent c, double multiplier, boolean scaleMass) {
			// Do not scale if set to automatic
			if (this.autoMethod != null) {
				boolean auto = (Boolean) this.autoMethod.invoke(c);
				if (auto) {
					return;
				}
			}
			
			// Scale value
			double value = (Double) this.getter.invoke(c);
			value = value * multiplier;
			if (this.arguments == null || this.arguments.length == 0) {
				this.setter.invoke(c, value);
			} else {
				Object[] parameters = new Object[this.arguments.length + 1];
				parameters[0] = value;
				System.arraycopy(this.arguments, 0, parameters, 1, this.arguments.length);
				this.setter.invoke(c, parameters);
			}
		}
		
	}
	
	
	private static class OverrideScaler implements Scaler {
		
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			
			if (component.isCGOverridden()) {
				double cgx = component.getOverrideCGX();
				cgx = cgx * multiplier;
				component.setOverrideCGX(cgx);
			}
			
			if (scaleMass && component.isMassOverridden()) {
				double mass = component.getOverrideMass();
				mass = mass * MathUtil.pow3(multiplier);
				component.setOverrideMass(mass);
			}

			//TODO: Fix overridden pressure!
		}
		
	}

	private static class TransitionScaler implements Scaler {

		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();

			// If the multiplier is larger than 1, the fore/aft radius is scaled first
			// to prevent the fore/aft shoulder radius from becoming larger than the fore/aft radius
			if (multiplier >= 1) {
				addScaler(Transition.class, "ForeRadius", "isForeRadiusAutomatic", scalers);
				addScaler(Transition.class, "AftRadius", "isForeRadiusAutomatic", scalers);
				addScaler(Transition.class, "ForeShoulderRadius", scalers, false);
				addScaler(Transition.class, "AftShoulderRadius", scalers, false);
			}
			// If the multiplier is smaller than 1, the fore/aft shoulder radius is scaled first
			// to prevent the fore/aft radius from becoming larger than the fore/aft shoulder radius
			else  {
				addScaler(Transition.class, "ForeShoulderRadius", scalers, false);
				addScaler(Transition.class, "AftShoulderRadius", scalers, false);
				addScaler(Transition.class, "ForeRadius", "isForeRadiusAutomatic", scalers);
				addScaler(Transition.class, "AftRadius", "isForeRadiusAutomatic", scalers);
			}

			performIterativeScaling(scalers, component, multiplier, scaleMass);
		}
	}

	private static class NoseConeScaler implements Scaler {

		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();

			// If the multiplier is larger than 1, the base radius is scaled first
			// to prevent the shoulder radius from becoming larger than the base radius
			if (multiplier >= 1) {
				addScaler(NoseCone.class, "BaseRadius", "isBaseRadiusAutomatic", scalers);
				addScaler(NoseCone.class, "ShoulderRadius", scalers);
			}
			// If the multiplier is smaller than 1, the shoulder radius is scaled first
			// to prevent the base radius from becoming larger than the shoulder radius
			else  {
				addScaler(NoseCone.class, "ShoulderRadius", scalers);
				addScaler(NoseCone.class, "BaseRadius", "isBaseRadiusAutomatic", scalers);
			}

			performIterativeScaling(scalers, component, multiplier, scaleMass);
		}
	}
	
	private static class MassComponentScaler implements Scaler {
		
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			if (scaleMass) {
				MassComponent c = (MassComponent) component;
				double mass = c.getComponentMass();
				mass = mass * MathUtil.pow3(multiplier);
				c.setComponentMass(mass);
			}
		}
		
	}

	private static class FinSetScaler implements Scaler {
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();
			FinSet finset = (FinSet) component;
			AxialMethod originalTabOffsetMethod = finset.getTabOffsetMethod();
			finset.setTabOffsetMethod(AxialMethod.ABSOLUTE);

			double tabOffset = finset.getTabOffset();
			tabOffset = tabOffset * multiplier;


			addScaler(FinSet.class, "Thickness", scalers);
			addScaler(FinSet.class, "TabHeight", scalers);
			addScaler(FinSet.class, "TabLength", scalers);

			performIterativeScaling(scalers, component, multiplier, scaleMass);

			finset.setTabOffset(tabOffset);
			finset.setTabOffsetMethod(originalTabOffsetMethod);
		}
	}
	
	private static class FreeformFinSetScaler implements Scaler {
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			FreeformFinSet finset = (FreeformFinSet) component;
			Coordinate[] points = finset.getFinPoints();
			for (int i = 0; i < points.length; i++) {
				points[i] = points[i].multiply(multiplier);
			}
			
			finset.setPoints(points, false);
		}
	}

	private static class RadiusRingComponentScaler implements Scaler {

		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();
			// We need to specify this particular order, otherwise scale the inner/outer radius may clip the dimensions of the other outer/inner radius
			if (multiplier >= 1) {			// Scale up
				addScaler(RadiusRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic", scalers);
				addScaler(RadiusRingComponent.class, "InnerRadius", "isInnerRadiusAutomatic", scalers);
			} else {						// Scale down
				addScaler(RadiusRingComponent.class, "InnerRadius", "isInnerRadiusAutomatic", scalers);
				addScaler(RadiusRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic", scalers);
			}

			performIterativeScaling(scalers, component, multiplier, scaleMass);
		}

	}

	private static class ThicknessRingComponentScaler implements Scaler {
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();
			// We need to specify this particular order, otherwise scale the inner/outer radius may clip the dimensions of the other outer/inner radius
			if (multiplier >= 1) {			// Scale up
				addScaler(ThicknessRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic", scalers);
				addScaler(ThicknessRingComponent.class, "Thickness", scalers);
			} else {						// Scale down
				addScaler(ThicknessRingComponent.class, "Thickness", scalers);
				addScaler(ThicknessRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic", scalers);
			}

			performIterativeScaling(scalers, component, multiplier, scaleMass);
		}
	}

	private static class RailButtonScaler implements Scaler {
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			final Map<Class<? extends RocketComponent>, List<Scaler>> scalers = new HashMap<>();
			// We need to specify this particular order, otherwise scale the inner/outer radius may clip the dimensions of the other outer/inner radius
			if (multiplier >= 1) {			// Scale up
				addScaler(RailButton.class, "OuterDiameter", scalers);
				addScaler(RailButton.class, "InnerDiameter", scalers);
				addScaler(RailButton.class, "TotalHeight", scalers);
				addScaler(RailButton.class, "BaseHeight", scalers);
				addScaler(RailButton.class, "FlangeHeight", scalers);
			} else {						// Scale down
				addScaler(RailButton.class, "InnerDiameter", scalers);
				addScaler(RailButton.class, "OuterDiameter", scalers);
				addScaler(RailButton.class, "BaseHeight", scalers);
				addScaler(RailButton.class, "FlangeHeight", scalers);
				addScaler(RailButton.class, "TotalHeight", scalers);
			}

			performIterativeScaling(scalers, component, multiplier, scaleMass);
		}
	}

	private static void performIterativeScaling(Map<Class<? extends RocketComponent>, List<Scaler>> scalers,
												RocketComponent component, double multiplier, boolean scaleMass) {
		for (List<Scaler> foo : scalers.values()) {
			for (Scaler s : foo) {
				s.scale(component, multiplier, scaleMass);
			}
		}
	}
	
}
