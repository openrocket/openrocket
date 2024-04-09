package info.openrocket.swing.gui.simulation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.simulation.DefaultSimulationOptionFactory;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.SimulationOptionsInterface;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Chars;
import info.openrocket.core.util.StateChangeListener;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class SimulationConditionsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	
	
	SimulationConditionsPanel(final Simulation simulation) {
		super(new MigLayout("fill"));
		
		final SimulationOptions conditions = simulation.getOptions();

		// Simulation conditions settings
		addSimulationConditionsPanel(this, conditions);


		JButton restoreDefaults = new SelectColorButton(trans.get("simedtdlg.but.resettodefault"));
		restoreDefaults.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				SimulationOptions defaults = f.getDefault();
				conditions.copyConditionsFrom(defaults);

			}

		});
		this.add(restoreDefaults, "span, split 3, skip, gapbottom para, gapright para, right");

		JButton saveDefaults = new SelectColorButton(trans.get("simedtdlg.but.savedefault"));
		saveDefaults.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				f.saveDefault(conditions);

			}

		});

		this.add(saveDefaults, "gapbottom para, gapright para, right");

	}

	/**
	 * Adds the simulation conditions panel to the parent panel.
	 * @param parent The parent panel.
	 * @param target The object containing the simulation conditions setters/getters.
	 */
	public static void addSimulationConditionsPanel(JPanel parent, SimulationOptionsInterface target) {
		JPanel sub;
		DoubleModel pressureModel;
		DoubleModel m;
		JSpinner spin;
		DoubleModel temperatureModel;
		String tip;
		BasicSlider slider;
		UnitSelector unit;

		//// Wind settings:  Average wind speed, turbulence intensity, std. deviation, and direction
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][75lp!][30lp!][75lp!]", ""));
		//// Wind
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Wind")));
		parent.add(sub, "growx, split 2, aligny 0, flowy, gapright para");


		// Wind average
		//// Average windspeed:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Averwindspeed"));
		//// The average windspeed relative to the ground.
		tip = trans.get("simedtdlg.lbl.ttip.Averwindspeed");
		label.setToolTipText(tip);
		sub.add(label);

		DoubleModel windSpeedAverage = new DoubleModel(target, "WindSpeedAverage", UnitGroup.UNITS_WINDSPEED, 0);

		spin = new JSpinner(windSpeedAverage.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(windSpeedAverage);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(windSpeedAverage.getSliderModel(0, 10.0));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		// Wind std. deviation
		//// Standard deviation:
		label = new JLabel(trans.get("simedtdlg.lbl.Stddeviation"));
		//// <html>The standard deviation of the windspeed.<br>
		//// The windspeed is within twice the standard deviation from the average for 95% of the time.
		tip = trans.get("simedtdlg.lbl.ttip.Stddeviation");
		label.setToolTipText(tip);
		sub.add(label);

		DoubleModel windSpeedDeviation = new DoubleModel(target, "WindSpeedDeviation", UnitGroup.UNITS_WINDSPEED, 0);
		DoubleModel m2 = new DoubleModel(target, "WindSpeedAverage", 0.25, UnitGroup.UNITS_COEFFICIENT, 0);

		spin = new JSpinner(windSpeedDeviation.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		addEasterEgg(spin, parent);
		sub.add(spin, "growx");

		unit = new UnitSelector(windSpeedDeviation);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(windSpeedDeviation.getSliderModel(new DoubleModel(0), m2));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		windSpeedAverage.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				windSpeedDeviation.stateChanged(e);
			}
		});


		// Wind turbulence intensity
		//// Turbulence intensity:
		label = new JLabel(trans.get("simedtdlg.lbl.Turbulenceintensity"));
		//// <html>The turbulence intensity is the standard deviation divided by the average windspeed.<br>
		//// Typical values range from
		//// to
		tip = trans.get("simedtdlg.lbl.ttip.Turbulenceintensity1") +
				trans.get("simedtdlg.lbl.ttip.Turbulenceintensity2") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.05) +
				" " + trans.get("simedtdlg.lbl.ttip.Turbulenceintensity3") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.20) + ".";
		label.setToolTipText(tip);
		sub.add(label);

		DoubleModel windTurbulenceIntensity = new DoubleModel(target, "WindTurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0);

		spin = new JSpinner(windTurbulenceIntensity.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(windTurbulenceIntensity);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");

		final JLabel intensityLabel = new JLabel(
				getIntensityDescription(target.getWindTurbulenceIntensity()));
		intensityLabel.setToolTipText(tip);
		sub.add(intensityLabel, "w 75lp, wrap");
		windTurbulenceIntensity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(
						getIntensityDescription(target.getWindTurbulenceIntensity()));
				windSpeedDeviation.stateChanged(e);
			}
		});
		windSpeedDeviation.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				windTurbulenceIntensity.stateChanged(e);
			}
		});

		// Wind Direction:
		label = new JLabel(trans.get("simedtdlg.lbl.Winddirection"));
		//// Direction of the wind. 0 is north
		tip = trans.get("simedtdlg.lbl.ttip.Winddirection");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(target, "WindDirection", 1.0, UnitGroup.UNITS_ANGLE,
				0, 2*Math.PI);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "growx");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 2*Math.PI));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		//// Temperature and pressure
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][85lp!][35lp!][75lp!]", ""));
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
		label = new JLabel(trans.get("simedtdlg.lbl.Temperature"));
		//// The temperature at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Temperature");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);

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
				"[grow][65lp!][30lp!][75lp!]", ""));
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
		sub.add(spin, "w 65lp!");

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
		sub.add(spin, "w 65lp!");

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

		m = new DoubleModel(target, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

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

	private static String getIntensityDescription(double i) {
		if (i < 0.001)
			//// None
			return trans.get("simedtdlg.IntensityDesc.None");
		if (i < 0.05)
			//// Very low
			return trans.get("simedtdlg.IntensityDesc.Verylow");
		if (i < 0.10)
			//// Low
			return trans.get("simedtdlg.IntensityDesc.Low");
		if (i < 0.15)
			//// Medium
			return trans.get("simedtdlg.IntensityDesc.Medium");
		if (i < 0.20)
			//// High
			return trans.get("simedtdlg.IntensityDesc.High");
		if (i < 0.25)
			//// Very high
			return trans.get("simedtdlg.IntensityDesc.Veryhigh");
		//// Extreme
		return trans.get("simedtdlg.IntensityDesc.Extreme");
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
}
