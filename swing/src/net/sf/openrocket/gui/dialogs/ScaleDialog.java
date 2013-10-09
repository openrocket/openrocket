package net.sf.openrocket.gui.dialogs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.Reflection.Method;

/**
 * Dialog that allows scaling the rocket design.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ScaleDialog extends JDialog {
	
	private static final Logger log = LoggerFactory.getLogger(ScaleDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	
	/*
	 * Scaler implementations
	 * 
	 * Each scaled value (except override cg/mass) is defined using a Scaler instance.
	 */
	private static final Map<Class<? extends RocketComponent>, List<Scaler>> SCALERS =
			new HashMap<Class<? extends RocketComponent>, List<Scaler>>();
	static {
		List<Scaler> list;
		
		// RocketComponent
		addScaler(RocketComponent.class, "PositionValue");
		SCALERS.get(RocketComponent.class).add(new OverrideScaler());
		
		// BodyComponent
		addScaler(BodyComponent.class, "Length");
		
		// SymmetricComponent
		addScaler(SymmetricComponent.class, "Thickness", "isFilled");
		
		// Transition + Nose cone
		addScaler(Transition.class, "ForeRadius", "isForeRadiusAutomatic");
		addScaler(Transition.class, "AftRadius", "isAftRadiusAutomatic");
		addScaler(Transition.class, "ForeShoulderRadius");
		addScaler(Transition.class, "ForeShoulderThickness");
		addScaler(Transition.class, "ForeShoulderLength");
		addScaler(Transition.class, "AftShoulderRadius");
		addScaler(Transition.class, "AftShoulderThickness");
		addScaler(Transition.class, "AftShoulderLength");
		
		// Body tube
		addScaler(BodyTube.class, "OuterRadius", "isOuterRadiusAutomatic");
		addScaler(BodyTube.class, "MotorOverhang");
		
		// Launch lug
		addScaler(LaunchLug.class, "OuterRadius");
		addScaler(LaunchLug.class, "Thickness");
		addScaler(LaunchLug.class, "Length");
		
		// FinSet
		addScaler(FinSet.class, "Thickness");
		addScaler(FinSet.class, "TabHeight");
		addScaler(FinSet.class, "TabLength");
		addScaler(FinSet.class, "TabShift");
		
		// TrapezoidFinSet
		addScaler(TrapezoidFinSet.class, "Sweep");
		addScaler(TrapezoidFinSet.class, "RootChord");
		addScaler(TrapezoidFinSet.class, "TipChord");
		addScaler(TrapezoidFinSet.class, "Height");
		
		// EllipticalFinSet
		addScaler(EllipticalFinSet.class, "Length");
		addScaler(EllipticalFinSet.class, "Height");
		
		// FreeformFinSet
		list = new ArrayList<ScaleDialog.Scaler>(1);
		list.add(new FreeformFinSetScaler());
		SCALERS.put(FreeformFinSet.class, list);
		
		// MassObject
		addScaler(MassObject.class, "Length");
		addScaler(MassObject.class, "Radius");
		addScaler(MassObject.class, "RadialPosition");
		
		// MassComponent
		list = new ArrayList<ScaleDialog.Scaler>(1);
		list.add(new MassComponentScaler());
		SCALERS.put(MassComponent.class, list);
		
		// Parachute
		addScaler(Parachute.class, "Diameter");
		addScaler(Parachute.class, "LineLength");
		
		// Streamer
		addScaler(Streamer.class, "StripLength");
		addScaler(Streamer.class, "StripWidth");
		
		// ShockCord
		addScaler(ShockCord.class, "CordLength");
		
		// RingComponent
		addScaler(RingComponent.class, "Length");
		addScaler(RingComponent.class, "RadialPosition");
		
		// ThicknessRingComponent
		addScaler(ThicknessRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic");
		addScaler(ThicknessRingComponent.class, "Thickness");
		
		// InnerTube
		addScaler(InnerTube.class, "MotorOverhang");
		
		// RadiusRingComponent
		addScaler(RadiusRingComponent.class, "OuterRadius", "isOuterRadiusAutomatic");
		addScaler(RadiusRingComponent.class, "InnerRadius", "isInnerRadiusAutomatic");
	}
	
	private static void addScaler(Class<? extends RocketComponent> componentClass, String methodName) {
		addScaler(componentClass, methodName, null);
	}
	
	private static void addScaler(Class<? extends RocketComponent> componentClass, String methodName, String autoMethodName) {
		List<Scaler> list = SCALERS.get(componentClass);
		if (list == null) {
			list = new ArrayList<ScaleDialog.Scaler>();
			SCALERS.put(componentClass, list);
		}
		list.add(new GeneralScaler(componentClass, methodName, autoMethodName));
	}
	
	
	
	
	
	private static final double DEFAULT_INITIAL_SIZE = 0.1; // meters
	private static final double SCALE_MIN = 0.01;
	private static final double SCALE_MAX = 100.0;
	
	private static final String SCALE_ROCKET = trans.get("lbl.scaleRocket");
	private static final String SCALE_SUBSELECTION = trans.get("lbl.scaleSubselection");
	private static final String SCALE_SELECTION = trans.get("lbl.scaleSelection");
	
	
	
	
	private final DoubleModel multiplier = new DoubleModel(1.0, UnitGroup.UNITS_RELATIVE, SCALE_MIN, SCALE_MAX);
	private final DoubleModel fromField = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
	private final DoubleModel toField = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
	
	private final OpenRocketDocument document;
	private final RocketComponent selection;
	private final boolean onlySelection;
	
	private JComboBox selectionOption;
	private JCheckBox scaleMassValues;
	
	private boolean changing = false;
	
	/**
	 * Sole constructor.
	 * 
	 * @param document		the document to modify.
	 * @param selection		the currently selected component (or <code>null</code> if none selected).
	 * @param parent		the parent window.
	 */
	public ScaleDialog(OpenRocketDocument document, RocketComponent selection, Window parent) {
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
	public ScaleDialog(OpenRocketDocument document, RocketComponent selection, Window parent, Boolean onlySelection) {
		super(parent, trans.get("title"), ModalityType.APPLICATION_MODAL);
		
		this.document = document;
		this.selection = selection;
		this.onlySelection = onlySelection;
		
		init();
	}
	
	private void init() {
		// Generate options for scaling
		List<String> options = new ArrayList<String>();
		if (!onlySelection)
			options.add(SCALE_ROCKET);
		if (selection != null && selection.getChildCount() > 0) {
			options.add(SCALE_SUBSELECTION);
		}
		if (selection != null) {
			options.add(SCALE_SELECTION);
		}
		
		
		/*
		 * Select initial size for "from" field.
		 * 
		 * If a component is selected, either its diameter (for SymmetricComponents) or length is selected.
		 * Otherwise the maximum body diameter is selected.  As a fallback DEFAULT_INITIAL_SIZE is used.
		 */
		// 
		double initialSize = 0;
		if (selection != null) {
			if (selection instanceof SymmetricComponent) {
				SymmetricComponent s = (SymmetricComponent) selection;
				initialSize = s.getForeRadius() * 2;
				initialSize = MathUtil.max(initialSize, s.getAftRadius() * 2);
			} else {
				initialSize = selection.getLength();
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
		
		selectionOption = new JComboBox(options.toArray());
		selectionOption.setEditable(false);
		selectionOption.setToolTipText(tip);
		panel.add(selectionOption, "growx, wrap para*2");
		
		
		// Scale multiplier
		tip = trans.get("lbl.scaling.ttip");
		label = new JLabel(trans.get("lbl.scaling"));
		label.setToolTipText(tip);
		panel.add(label, "gapright unrel");
		
		
		JSpinner spin = new JSpinner(multiplier.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "w :30lp:65lp");
		
		UnitSelector unit = new UnitSelector(multiplier);
		unit.setToolTipText(tip);
		panel.add(unit, "w 30lp");
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
		panel.add(spin, "span, split, w :30lp:65lp");
		
		unit = new UnitSelector(fromField);
		unit.setToolTipText(tip);
		panel.add(unit, "w 30lp");
		
		label = new JLabel(trans.get("lbl.scaleTo"));
		label.setToolTipText(tip);
		panel.add(label, "gap unrel");
		
		spin = new JSpinner(toField.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		panel.add(spin, "w :30lp:65lp");
		
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
		panel.add(scaleMassValues, "span, wrap para*3");
		
		
		// Buttons
		
		JButton scale = new JButton(trans.get("button.scale"));
		scale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doScale();
				ScaleDialog.this.setVisible(false);
			}
		});
		panel.add(scale, "span, split, right, gap para");
		
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
			Application.getExceptionHandler().handleErrorCondition("Illegal multiplier value, mul=" + mul);
			return;
		}
		
		if (MathUtil.equals(mul, 1.0)) {
			// Nothing to do
			log.info(Markers.USER_MARKER, "Scaling by value 1.0 - nothing to do");
			return;
		}
		
		boolean scaleMass = scaleMassValues.isSelected();
		
		Object item = selectionOption.getSelectedItem();
		log.info(Markers.USER_MARKER, "Scaling design by factor " + mul + ", option=" + item);
		if (SCALE_ROCKET.equals(item)) {
			
			// Scale the entire rocket design
			try {
				document.startUndo(trans.get("undo.scaleRocket"));
				for (RocketComponent c : document.getRocket()) {
					scale(c, mul, scaleMass);
				}
			} finally {
				document.stopUndo();
			}
			
		} else if (SCALE_SUBSELECTION.equals(item)) {
			
			// Scale component and subcomponents
			try {
				document.startUndo(trans.get("undo.scaleComponents"));
				for (RocketComponent c : selection) {
					scale(c, mul, scaleMass);
				}
			} finally {
				document.stopUndo();
			}
			
		} else if (SCALE_SELECTION.equals(item)) {
			
			// Scale only the selected component
			try {
				document.startUndo(trans.get("undo.scaleComponent"));
				scale(selection, mul, scaleMass);
			} finally {
				document.stopUndo();
			}
			
		} else {
			throw new BugException("Unknown item selected, item=" + item);
		}
	}
	
	
	/**
	 * Perform scaling on a single component.
	 */
	private void scale(RocketComponent component, double mul, boolean scaleMass) {
		
		Class<?> clazz = component.getClass();
		while (clazz != null) {
			List<Scaler> list = SCALERS.get(clazz);
			if (list != null) {
				for (Scaler s : list) {
					s.scale(component, mul, scaleMass);
				}
			}
			
			clazz = clazz.getSuperclass();
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
		public void scale(RocketComponent c, double multiplier, boolean scaleMass);
	}
	
	/**
	 * General scaler implementation that uses reflection to get/set a specific value.
	 */
	private static class GeneralScaler implements Scaler {
		
		private final Method getter;
		private final Method setter;
		private final Method autoMethod;
		
		public GeneralScaler(Class<? extends RocketComponent> componentClass, String methodName, String autoMethodName) {
			
			getter = Reflection.findMethod(componentClass, "get" + methodName);
			setter = Reflection.findMethod(componentClass, "set" + methodName, double.class);
			if (autoMethodName != null) {
				autoMethod = Reflection.findMethod(componentClass, autoMethodName);
			} else {
				autoMethod = null;
			}
			
		}
		
		@Override
		public void scale(RocketComponent c, double multiplier, boolean scaleMass) {
			
			// Do not scale if set to automatic
			if (autoMethod != null) {
				boolean auto = (Boolean) autoMethod.invoke(c);
				if (auto) {
					return;
				}
			}
			
			// Scale value
			double value = (Double) getter.invoke(c);
			value = value * multiplier;
			setter.invoke(c, value);
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
	
	private static class FreeformFinSetScaler implements Scaler {
		
		@Override
		public void scale(RocketComponent component, double multiplier, boolean scaleMass) {
			FreeformFinSet finset = (FreeformFinSet) component;
			Coordinate[] points = finset.getFinPoints();
			for (int i = 0; i < points.length; i++) {
				points[i] = points[i].multiply(multiplier);
			}
			try {
				finset.setPoints(points);
			} catch (IllegalFinPointException e) {
				throw new BugException("Failed to set points after scaling, original=" + Arrays.toString(finset.getFinPoints()) + " scaled=" + Arrays.toString(points), e);
			}
		}
		
	}
	
}
