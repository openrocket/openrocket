package info.openrocket.swing.gui.simulation;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.DefaultSimulationOptionFactory;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.SimulationOptionsInterface;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.util.Icons;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;

public class SimulationConditionsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();


	SimulationConditionsPanel(final Simulation simulation) {
		super(new MigLayout("fill"));

		SimulationOptions simulationOptions = simulation.getOptions();

		// Simulation conditions settings
		addSimulationConditionsPanel(this, simulationOptions);

		// Add buttons for restoring and saving defaults
		addDefaultButtons(simulationOptions);
	}

	/**
	 * Adds the simulation conditions panel to the parent panel.
	 * @param parent The parent panel.
	 * @param target The object containing the simulation conditions setters/getters.
	 * @param addAllWindModels if false, only the average wind model will be added.
	 */
	public static void addSimulationConditionsPanel(JPanel parent, SimulationOptionsInterface target,
													boolean addAllWindModels) {
		JPanel sub;
		DoubleModel pressureModel;
		DoubleModel m;
		JSpinner spin;
		DoubleModel temperatureModel;
		String tip;
		BasicSlider slider;
		UnitSelector unit;

		//// Wind settings:  Average wind speed, turbulence intensity, std. deviation, and direction
		sub = new JPanel(new MigLayout("fill, ins 20 20 0 20", "[grow]", ""));
		//// Wind
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Wind")));
		parent.add(sub, "growx, split 2, aligny 0, flowy, gapright para");

		// Add wind model selection and configuration panel
		if (addAllWindModels) {
			addWindModelPanel(sub, target);
		} else {
			addAverageWindSettings(sub, target);
		}

		//// Temperature and pressure
		sub = new JPanel(new MigLayout("gap rel unrel",
				"[][85lp!][35lp!][75lp!]", ""));
		//// Atmospheric conditions
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Atmoscond")));
		parent.add(sub, "growx, aligny 0, gapright para");


		BooleanModel isa = new BooleanModel(target, "ISAAtmosphere");
		JCheckBox check = new JCheckBox(isa);
		//// Use International Standard Atmosphere
		check.setText(trans.get("simedtdlg.checkbox.InterStdAtmosphere"));
		//// <html>Select to use the International Standard Atmosphere model.
		//// <br>This model has a temperature of
		//// and a pressure of
		//// at sea level.
		check.setToolTipText(trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere1") + " " +
				UnitGroup.UNITS_TEMPERATURE.toStringUnit(ExtendedISAModel.STANDARD_TEMPERATURE) +
				" " + trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere2") + " " +
				UnitGroup.UNITS_PRESSURE.toStringUnit(ExtendedISAModel.STANDARD_PRESSURE) +
				" " + trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere3"));
		sub.add(check, "spanx, wrap unrel");

		// Temperature:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Temperature"));
		//// The temperature at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Temperature");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label, "gapright 50lp");

		temperatureModel = new DoubleModel(target, "LaunchTemperature", UnitGroup.UNITS_TEMPERATURE, 0);

		spin = new JSpinner(temperatureModel.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "growx");

		unit = new UnitSelector(temperatureModel);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(temperatureModel.getSliderModel(253.15, 308.15)); // -20 ... 35
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");


		// Pressure:
		label = new JLabel(trans.get("simedtdlg.lbl.Pressure"));
		//// The atmospheric pressure at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Pressure");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);

		pressureModel = new DoubleModel(target, "LaunchPressure", UnitGroup.UNITS_PRESSURE, 0);

		spin = new JSpinner(pressureModel.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "growx");

		unit = new UnitSelector(pressureModel);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(pressureModel.getSliderModel(0.950e5, 1.050e5));
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");


		isa.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				temperatureModel.stateChanged(e);
				pressureModel.stateChanged(e);
			}
		});


		//// Launch site conditions
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][90lp!][30lp!][75lp!]", ""));
		//// Launch site
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Launchsite")));
		parent.add(sub, "growx, split 2, aligny 0, flowy");


		// Latitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Latitude"));
		//// <html>The launch site latitude affects the gravitational pull of Earth.<br>
		//// Positive values are on the Northern hemisphere, negative values on the Southern hemisphere.
		tip = trans.get("simedtdlg.lbl.ttip.Latitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "LaunchLatitude", UnitGroup.UNITS_LATITUDE, -90, 90);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel());
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		// Longitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Longitude"));
		tip = trans.get("simedtdlg.lbl.ttip.Longitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "LaunchLongitude", UnitGroup.UNITS_LONGITUDE, -180, 180);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel());
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		// Altitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Altitude"));
		//// <html>The launch altitude above mean sea level.<br>
		//// This affects the position of the rocket in the atmospheric model.
		tip = trans.get("simedtdlg.lbl.ttip.Altitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0, ExtendedISAModel.getMaximumAllowedAltitude());

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 250, 1000));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		//// Launch rod
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][75lp!][30lp!][75lp!]", ""));
		//// Launch rod
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Launchrod")));
		parent.add(sub, "growx, aligny 0, wrap");


		// Length:
		label = new JLabel(trans.get("simedtdlg.lbl.Length"));
		//// The length of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Length");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "LaunchRodLength", UnitGroup.UNITS_LENGTH, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 1, 5));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Keep launch rod parallel to the wind.

		BooleanModel intoWind = new BooleanModel(target, "LaunchIntoWind");
		JCheckBox checkWind = new JCheckBox(intoWind);
		//// Use International Standard Atmosphere
		checkWind.setText(trans.get("simedtdlg.checkbox.Intowind"));
		checkWind.setToolTipText(
				trans.get("simedtdlg.checkbox.ttip.Intowind1") +
				trans.get("simedtdlg.checkbox.ttip.Intowind2") +
				trans.get("simedtdlg.checkbox.ttip.Intowind3") +
				trans.get("simedtdlg.checkbox.ttip.Intowind4"));
		sub.add(checkWind, "spanx, wrap unrel");


		// Angle:
		label = new JLabel(trans.get("simedtdlg.lbl.Angle"));
		//// The angle of the launch rod from vertical.
		tip = trans.get("simedtdlg.lbl.ttip.Angle");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "LaunchRodAngle", UnitGroup.UNITS_ANGLE,
				-SimulationOptions.MAX_LAUNCH_ROD_ANGLE, SimulationOptions.MAX_LAUNCH_ROD_ANGLE);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(-SimulationOptions.MAX_LAUNCH_ROD_ANGLE, 0,
				SimulationOptions.MAX_LAUNCH_ROD_ANGLE));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		// Direction:
		JLabel directionLabel = new JLabel(trans.get("simedtdlg.lbl.Direction"));
		//// <html>Direction of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Direction1") +
				UnitGroup.UNITS_ANGLE.toStringUnit(0) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction2") + " " +
				UnitGroup.UNITS_ANGLE.toStringUnit(2*Math.PI) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction3");
		directionLabel.setToolTipText(tip);
		sub.add(directionLabel);

		m = new DoubleModel(target, "LaunchRodDirection", 1.0, UnitGroup.UNITS_ANGLE,
				0, 2*Math.PI);

		JSpinner directionSpin = new JSpinner(m.getSpinnerModel());
		directionSpin.setEditor(new SpinnerEditor(directionSpin));
		directionSpin.setToolTipText(tip);
		sub.add(directionSpin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		BasicSlider directionSlider = new BasicSlider(m.getSliderModel(0, 2*Math.PI));
		directionSlider.setToolTipText(tip);
		sub.add(directionSlider, "w 75lp, wrap");
		intoWind.addEnableComponent(directionLabel, false);
		intoWind.addEnableComponent(directionSpin, false);
		intoWind.addEnableComponent(unit, false);
		intoWind.addEnableComponent(directionSlider, false);
	}

	public static void addSimulationConditionsPanel(JPanel parent, SimulationOptionsInterface target) {
		addSimulationConditionsPanel(parent, target, true);
	}

	private static void addWindModelPanel(JPanel panel, SimulationOptionsInterface target) {
		ButtonGroup windModelGroup = new ButtonGroup();

		// Wind model to use
		panel.add(new JLabel(trans.get("simedtdlg.lbl.WindModelSelection")), "spanx, split 3, gapright para");

		//// Average
		JRadioButton averageButton = new JRadioButton(trans.get("simedtdlg.radio.AverageWind"));
		averageButton.setToolTipText(trans.get("simedtdlg.radio.AverageWind.ttip"));
		averageButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

		//// Multi-level
		JRadioButton multiLevelButton = new JRadioButton(trans.get("simedtdlg.radio.MultiLevelWind"));
		multiLevelButton.setToolTipText(trans.get("simedtdlg.radio.MultiLevelWind.ttip"));

		windModelGroup.add(averageButton);
		windModelGroup.add(multiLevelButton);

		panel.add(averageButton);
		panel.add(multiLevelButton, "wrap");

		panel.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap");

		JPanel windSettingsPanel = new JPanel(new CardLayout());

		JPanel averagePanel = new JPanel(new MigLayout("fill, ins 0", "[grow][75lp!][30lp!][75lp!]", ""));
		JPanel multiLevelPanel = new JPanel(new MigLayout("fill, ins 0"));

		addAverageWindSettings(averagePanel, target);
		addMultiLevelSettings(multiLevelPanel, target);

		windSettingsPanel.add(averagePanel, "Average");
		windSettingsPanel.add(multiLevelPanel, "MultiLevel");

		panel.add(windSettingsPanel, "grow, wrap");

		averageButton.addActionListener(e -> {
			((CardLayout) windSettingsPanel.getLayout()).show(windSettingsPanel, "Average");
			if (target instanceof SimulationOptions) {
				((SimulationOptions) target).setWindModelType(WindModelType.AVERAGE);
			}
		});

		multiLevelButton.addActionListener(e -> {
			((CardLayout) windSettingsPanel.getLayout()).show(windSettingsPanel, "MultiLevel");
			if (target instanceof SimulationOptions) {
				((SimulationOptions) target).setWindModelType(WindModelType.MULTI_LEVEL);
			}
		});

		// Set initial selection based on current wind model
		if (target instanceof SimulationOptions) {
			SimulationOptions options = (SimulationOptions) target;
			if (options.getWindModelType() == WindModelType.AVERAGE) {
				averageButton.setSelected(true);
				((CardLayout) windSettingsPanel.getLayout()).show(windSettingsPanel, "Average");
			} else {
				multiLevelButton.setSelected(true);
				((CardLayout) windSettingsPanel.getLayout()).show(windSettingsPanel, "MultiLevel");
			}
		}
	}

	private static void addAverageWindSettings(JPanel panel, SimulationOptionsInterface target) {
		PinkNoiseWindModel model = target.getAverageWindModel();

		// Wind average
		final DoubleModel windSpeedAverage = addDoubleModel(panel, "Averwindspeed", trans.get("simedtdlg.lbl.ttip.Averwindspeed"), model, "Average",
				UnitGroup.UNITS_WINDSPEED, 0, 10.0);

		// Wind standard deviation
		final DoubleModel windSpeedDeviation = addDoubleModel(panel, "Stddeviation", trans.get("simedtdlg.lbl.ttip.Stddeviation"),
				model, "StandardDeviation", UnitGroup.UNITS_WINDSPEED, 0, windSpeedAverage);

		windSpeedAverage.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				windSpeedDeviation.stateChanged(e);
			}
		});

		// Turbulence intensity
		String tip = trans.get("simedtdlg.lbl.ttip.Turbulenceintensity1") +
				trans.get("simedtdlg.lbl.ttip.Turbulenceintensity2") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.05) +
				" " + trans.get("simedtdlg.lbl.ttip.Turbulenceintensity3") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.20) + ".";
		final DoubleModel windTurbulenceIntensity = addDoubleModel(panel, "Turbulenceintensity", tip, model,
				"TurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0, 1.0, true);

		final JLabel intensityLabel = new JLabel(target.getAverageWindModel().getIntensityDescription());
		intensityLabel.setToolTipText(tip);
		panel.add(intensityLabel, "w 75lp, skip 1, wrap");
		windTurbulenceIntensity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(target.getAverageWindModel().getIntensityDescription());
				windSpeedDeviation.stateChanged(e);
			}
		});
		windSpeedDeviation.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				windTurbulenceIntensity.stateChanged(e);
			}
		});

		// Wind direction
		addDoubleModel(panel, "Winddirection", trans.get("simedtdlg.lbl.ttip.Winddirection"), model, "Direction",
				UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
	}

	private static void addMultiLevelSettings(JPanel panel, SimulationOptionsInterface target) {
		if (!(target instanceof SimulationOptions options)) {
			return;
		}
		MultiLevelPinkNoiseWindModel model = options.getMultiLevelWindModel();
		
		// Create a summary panel to show wind level information
		JPanel summaryPanel = new JPanel(new MigLayout("fill, ins 0"));
		JLabel summaryLabel = new JLabel();
		updateWindLevelSummary(summaryLabel, model);
		summaryPanel.add(summaryLabel, "grow, wrap");
		
		// Add edit button
		JButton editButton = new JButton(trans.get("simedtdlg.but.editWindLevels"));
		editButton.setIcon(Icons.EDIT_EDIT);
		editButton.addActionListener(e -> {
			Window owner = SwingUtilities.getWindowAncestor(panel);
			MultiLevelWindEditDialog dialog = new MultiLevelWindEditDialog(owner, model);
			dialog.setVisible(true);
			
			// Update summary after dialog is closed
			updateWindLevelSummary(summaryLabel, model);
		});
		
		panel.add(summaryPanel, "grow, wrap");
		panel.add(editButton, "spanx, growx, wrap");
	}
	
	private static void updateWindLevelSummary(JLabel label, MultiLevelPinkNoiseWindModel model) {
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		
		if (levels.isEmpty()) {		// This shouldn't really be possible, but oh well
			label.setText(trans.get("simedtdlg.lbl.noWindLevels"));
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append(String.format(trans.get("simedtdlg.lbl.windLevelCount"), levels.size())).append("<br>");
			
			// Show altitude range
			if (levels.size() > 1) {
				double minAlt = Double.MAX_VALUE;
				double maxAlt = Double.MIN_VALUE;
				
				for (MultiLevelPinkNoiseWindModel.LevelWindModel level : levels) {
					minAlt = Math.min(minAlt, level.getAltitude());
					maxAlt = Math.max(maxAlt, level.getAltitude());
				}
				
				sb.append(String.format(trans.get("simedtdlg.lbl.altitudeRange"),
						UnitGroup.UNITS_DISTANCE.toStringUnit(minAlt), UnitGroup.UNITS_DISTANCE.toStringUnit(maxAlt)));
				sb.append("<br>");
			} else {
				sb.append(String.format(trans.get("simedtdlg.lbl.altitude"),
						UnitGroup.UNITS_DISTANCE.toStringUnit(levels.get(0).getAltitude())));
				sb.append("<br>");
			}
			
			// Show speed range
			if (levels.size() > 1) {
				double minSpeed = Double.MAX_VALUE;
				double maxSpeed = Double.MIN_VALUE;
				
				for (MultiLevelPinkNoiseWindModel.LevelWindModel level : levels) {
					minSpeed = Math.min(minSpeed, level.getSpeed());
					maxSpeed = Math.max(maxSpeed, level.getSpeed());
				}
				
				sb.append(String.format(trans.get("simedtdlg.lbl.speedRange"),
						UnitGroup.UNITS_VELOCITY.toStringUnit(minSpeed), UnitGroup.UNITS_VELOCITY.toStringUnit(maxSpeed)));
			} else {
				sb.append(String.format(trans.get("simedtdlg.lbl.speed"),
						UnitGroup.UNITS_VELOCITY.toStringUnit(levels.get(0).getSpeed())));
			}
			
			sb.append("</html>");
			label.setText(sb.toString());
		}
	}

	private static DoubleModel addDoubleModel(JPanel panel, String labelKey, String tooltipText, Object source, String sourceKey,
									   UnitGroup unit, double min, Object max, boolean easterEgg) {
		JLabel label = new JLabel(trans.get("simedtdlg.lbl." + labelKey));
		panel.add(label);

		DoubleModel model;
		if (max instanceof Double) {
			model = new DoubleModel(source, sourceKey, unit, min, (Double) max);
		} else if (max instanceof DoubleModel) {
			model = new DoubleModel(source, sourceKey, unit, min, (DoubleModel) max);
		} else {
			throw new IllegalArgumentException("Invalid max value");
		}

		JSpinner spin = new JSpinner(model.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		if (tooltipText != null) {
			spin.setToolTipText(tooltipText);
		}
		panel.add(spin, "growx");

		if (easterEgg) {
			addEasterEgg(spin, panel);
		}

		UnitSelector unitSelector = new UnitSelector(model);
		panel.add(unitSelector, "growx");

		BasicSlider slider = new BasicSlider(model.getSliderModel());
		panel.add(slider, "w 75lp, wrap");

		return model;
	}

	private static DoubleModel addDoubleModel(JPanel panel, String labelKey, String tooltipText, Object source, String sourceKey,
											  UnitGroup unit, double min, Object max) {
		return addDoubleModel(panel, labelKey, tooltipText, source, sourceKey, unit, min, max, false);
	}


	private void addDefaultButtons(SimulationOptions options) {
		// Reset to default
		JButton restoreDefaults = new JButton(trans.get("simedtdlg.but.resettodefault"));
		restoreDefaults.addActionListener(e -> {
			DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
			SimulationOptions defaults = f.getDefault();
			options.copyConditionsFrom(defaults);
		});
		this.add(restoreDefaults, "span, split 3, skip, gapright para, right");

		// Save as default
		JButton saveDefaults = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveDefaults.addActionListener(e -> {
			DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
			f.saveDefault(options);
		});
		this.add(saveDefaults, "gapright para, right");
	}

	/**
	 * Shh, don't tell anyone about this easter-egg. (displays a fun quote when the text of the spinner equals 42)
	 * @param spinner the magic spinner!
	 */
	private static void addEasterEgg(JSpinner spinner, Component parent) {
		JTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String text = textField.getText() + e.getKeyChar();
				if (text.equals("42")) {
					JOptionPane.showMessageDialog(parent,
							"The answer to the ultimate question of life, the universe, and everything.",
							"42", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}

	public void cleanup() {
		// Remove all components from the panel
		removeAll();
	}

	@Override
	public void addNotify() {
		super.addNotify();

		// Now that the panel is added to a container, we can safely get the parent window
		Window parent = SwingUtilities.getWindowAncestor(this);
		if (parent != null) {
			parent.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					cleanup();
				}
			});
		}
	}

}
