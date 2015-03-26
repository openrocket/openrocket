package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.models.atmosphere.ExtendedISAModel;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;

public class LaunchPreferencesPanel extends PreferencesPanel {

	public LaunchPreferencesPanel(JDialog parent, LayoutManager layout) {
		super(parent, layout);
		// TODO Auto-generated constructor stub
	}

	public LaunchPreferencesPanel() {
		super(new MigLayout("fillx, ins 30lp n n n"));

		JPanel sub;
		String tip;
		UnitSelector unit;
		BasicSlider slider;
		DoubleModel m;
		JSpinner spin;

		// Wind settings: Average wind speed, turbulence intensity, std.
		// deviation, and direction
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		// Wind
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.lbl.Wind")));
		this.add(sub, "growx, split 2, aligny 0, flowy, gapright para");

		// Wind average
		// Average windspeed:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Averwindspeed"));
		// The average windspeed relative to the ground.
		tip = trans.get("simedtdlg.lbl.ttip.Averwindspeed");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "WindSpeedAverage",
				UnitGroup.UNITS_WINDSPEED, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 10.0));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Wind std. deviation
		// // Standard deviation:
		label = new JLabel(trans.get("simedtdlg.lbl.Stddeviation"));
		// // <html>The standard deviation of the windspeed.<br>
		// // The windspeed is within twice the standard deviation from the
		// average for 95% of the time.
		tip = trans.get("simedtdlg.lbl.ttip.Stddeviation");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "WindSpeedDeviation",
				UnitGroup.UNITS_WINDSPEED, 0);
		DoubleModel m2 = new DoubleModel(preferences, "WindSpeedAverage", 0.25,
				UnitGroup.UNITS_COEFFICIENT, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(new DoubleModel(0), m2));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");


		// Wind turbulence intensity
		// // Turbulence intensity:
		label = new JLabel(trans.get("simedtdlg.lbl.Turbulenceintensity"));
		// // <html>The turbulence intensity is the standard deviation divided
		// by the average windspeed.<br>
		// // Typical values range from
		// // to
		tip = trans.get("simedtdlg.lbl.ttip.Turbulenceintensity1")
				+ trans.get("simedtdlg.lbl.ttip.Turbulenceintensity2") + " "
				+ UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.05)
				+ " " + trans.get("simedtdlg.lbl.ttip.Turbulenceintensity3")
				+ " "
				+ UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.20)
				+ ".";
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "WindTurbulenceIntensity",
				UnitGroup.UNITS_RELATIVE, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");

		final JLabel intensityLabel = new JLabel(
				getIntensityDescription(preferences
						.getWindTurbulenceIntensity()));
		intensityLabel.setToolTipText(tip);
		sub.add(intensityLabel, "w 75lp, wrap");
		m.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(getIntensityDescription(preferences
						.getWindTurbulenceIntensity()));
			}
		});

		// Wind Direction:
		label = new JLabel(trans.get("simedtdlg.lbl.Winddirection"));
		// // Direction of the wind. 0 is north
		tip = trans.get("simedtdlg.lbl.ttip.Winddirection");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "WindDirection", 1.0,
				UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 2 * Math.PI));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// // Temperature and pressure
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		// // Atmospheric preferences
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.border.Atmoscond")));
		this.add(sub, "growx, aligny 0, gapright para");

		BooleanModel isa = new BooleanModel(preferences, "ISAAtmosphere");
		JCheckBox check = new JCheckBox(isa);
		// // Use International Standard Atmosphere
		check.setText(trans.get("simedtdlg.checkbox.InterStdAtmosphere"));
		// // <html>Select to use the International Standard Atmosphere model.
		// // <br>This model has a temperature of
		// // and a pressure of
		// // at sea level.
		check.setToolTipText(trans
				.get("simedtdlg.checkbox.ttip.InterStdAtmosphere1")
				+ " "
				+ UnitGroup.UNITS_TEMPERATURE
						.toStringUnit(ExtendedISAModel.STANDARD_TEMPERATURE)
				+ " "
				+ trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere2")
				+ " "
				+ UnitGroup.UNITS_PRESSURE
						.toStringUnit(ExtendedISAModel.STANDARD_PRESSURE)
				+ " "
				+ trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere3"));
		sub.add(check, "spanx, wrap unrel");

		// Temperature:
		label = new JLabel(trans.get("simedtdlg.lbl.Temperature"));
		// // The temperature at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Temperature");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchTemperature",
				UnitGroup.UNITS_TEMPERATURE, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(253.15, 308.15)); // -20 ...
																	// 35
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");

		// Pressure:
		label = new JLabel(trans.get("simedtdlg.lbl.Pressure"));
		// // The atmospheric pressure at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Pressure");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchPressure",
				UnitGroup.UNITS_PRESSURE, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0.950e5, 1.050e5));
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");

		// // Launch site preferences
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		// // Launch site
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.lbl.Launchsite")));
		this.add(sub, "growx, split 2, aligny 0, flowy");

		// Latitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Latitude"));
		// // <html>The launch site latitude affects the gravitational pull of
		// Earth.<br>
		// // Positive values are on the Northern hemisphere, negative values on
		// the Southern hemisphere.
		tip = trans.get("simedtdlg.lbl.ttip.Latitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchLatitude",
				UnitGroup.UNITS_NONE, -90, 90);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		label = new JLabel(Chars.DEGREE + " N");
		label.setToolTipText(tip);
		sub.add(label, "growx");
		slider = new BasicSlider(m.getSliderModel(-90, 90));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Longitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Longitude"));
		tip = trans.get("simedtdlg.lbl.ttip.Longitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchLongitude",
				UnitGroup.UNITS_NONE, -180, 180);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		label = new JLabel(Chars.DEGREE + " E");
		label.setToolTipText(tip);
		sub.add(label, "growx");
		slider = new BasicSlider(m.getSliderModel(-180, 180));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Altitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Altitude"));
		// // <html>The launch altitude above mean sea level.<br>
		// // This affects the position of the rocket in the atmospheric model.
		tip = trans.get("simedtdlg.lbl.ttip.Altitude");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchAltitude",
				UnitGroup.UNITS_DISTANCE, 0);

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

		// // Launch rod
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		// // Launch rod
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.border.Launchrod")));
		this.add(sub, "growx, aligny 0, wrap");

		// Length:
		label = new JLabel(trans.get("simedtdlg.lbl.Length"));
		// // The length of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Length");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchRodLength",
				UnitGroup.UNITS_LENGTH, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 1, 5));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Keep launch rod parallel to the wind.

		BooleanModel intoWind = new BooleanModel(preferences, "LaunchIntoWind");
		JCheckBox checkWind = new JCheckBox(intoWind);
		// // Use International Standard Atmosphere
		checkWind.setText(trans.get("simedtdlg.checkbox.Intowind"));
		checkWind.setToolTipText(trans.get("simedtdlg.checkbox.ttip.Intowind1")
				+ trans.get("simedtdlg.checkbox.ttip.Intowind2")
				+ trans.get("simedtdlg.checkbox.ttip.Intowind3")
				+ trans.get("simedtdlg.checkbox.ttip.Intowind4"));
		sub.add(checkWind, "spanx, wrap unrel");

		// Angle:
		label = new JLabel(trans.get("simedtdlg.lbl.Angle"));
		// // The angle of the launch rod from vertical.
		tip = trans.get("simedtdlg.lbl.ttip.Angle");
		label.setToolTipText(tip);
		sub.add(label);

		m = new DoubleModel(preferences, "LaunchRodAngle",
				UnitGroup.UNITS_ANGLE, -SimulationOptions.MAX_LAUNCH_ROD_ANGLE,
				SimulationOptions.MAX_LAUNCH_ROD_ANGLE);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(
				-SimulationOptions.MAX_LAUNCH_ROD_ANGLE, 0,
				SimulationOptions.MAX_LAUNCH_ROD_ANGLE));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");

		// Direction:
		JLabel directionLabel = new JLabel(trans.get("simedtdlg.lbl.Direction"));
		// // <html>Direction of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Direction1")
				+ UnitGroup.UNITS_ANGLE.toStringUnit(0) + " "
				+ trans.get("simedtdlg.lbl.ttip.Direction2") + " "
				+ UnitGroup.UNITS_ANGLE.toStringUnit(2 * Math.PI) + " "
				+ trans.get("simedtdlg.lbl.ttip.Direction3");
		directionLabel.setToolTipText(tip);
		sub.add(directionLabel);

		m = new DoubleModel(preferences, "LaunchRodDirection", 1.0,
				UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);

		JSpinner directionSpin = new JSpinner(m.getSpinnerModel());
		directionSpin.setEditor(new SpinnerEditor(directionSpin));
		directionSpin.setToolTipText(tip);
		sub.add(directionSpin, "w 65lp!");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		BasicSlider directionSlider = new BasicSlider(m.getSliderModel(0,
				2 * Math.PI));
		directionSlider.setToolTipText(tip);
		sub.add(directionSlider, "w 75lp, wrap");
		intoWind.addEnableComponent(directionLabel, false);
		intoWind.addEnableComponent(directionSpin, false);
		intoWind.addEnableComponent(unit, false);
		intoWind.addEnableComponent(directionSlider, false);

	}

	private String getIntensityDescription(double i) {
		if (i < 0.001)
			// // None
			return trans.get("simedtdlg.IntensityDesc.None");
		if (i < 0.05)
			// // Very low
			return trans.get("simedtdlg.IntensityDesc.Verylow");
		if (i < 0.10)
			// // Low
			return trans.get("simedtdlg.IntensityDesc.Low");
		if (i < 0.15)
			// // Medium
			return trans.get("simedtdlg.IntensityDesc.Medium");
		if (i < 0.20)
			// // High
			return trans.get("simedtdlg.IntensityDesc.High");
		if (i < 0.25)
			// // Very high
			return trans.get("simedtdlg.IntensityDesc.Veryhigh");
		// // Extreme
		return trans.get("simedtdlg.IntensityDesc.Extreme");
	}

}
