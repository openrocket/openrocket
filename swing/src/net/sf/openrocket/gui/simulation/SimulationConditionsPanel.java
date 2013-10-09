package net.sf.openrocket.gui.simulation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.models.atmosphere.ExtendedISAModel;
import net.sf.openrocket.simulation.DefaultSimulationOptionFactory;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;

public class SimulationConditionsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	
	
	SimulationConditionsPanel(final Simulation simulation) {
		super(new MigLayout("fill"));
		
		final SimulationOptions conditions = simulation.getOptions();
		
		JPanel sub;
		String tip;
		UnitSelector unit;
		BasicSlider slider;
		DoubleModel m;
		JSpinner spin;
		
		//// Wind settings:  Average wind speed, turbulence intensity, std. deviation
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Wind
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Wind")));
		this.add(sub, "growx, split 2, aligny 0, flowy, gapright para");
		
		
		// Wind average
		//// Average windspeed:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Averwindspeed"));
		//// The average windspeed relative to the ground.
		tip = trans.get("simedtdlg.lbl.ttip.Averwindspeed");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "WindSpeedAverage", UnitGroup.UNITS_WINDSPEED, 0);
		
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
		//// Standard deviation:
		label = new JLabel(trans.get("simedtdlg.lbl.Stddeviation"));
		//// <html>The standard deviation of the windspeed.<br>
		//// The windspeed is within twice the standard deviation from the average for 95% of the time.
		tip = trans.get("simedtdlg.lbl.ttip.Stddeviation");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "WindSpeedDeviation", UnitGroup.UNITS_WINDSPEED, 0);
		DoubleModel m2 = new DoubleModel(conditions, "WindSpeedAverage", 0.25,
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
		
		m = new DoubleModel(conditions, "WindTurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		
		final JLabel intensityLabel = new JLabel(
				getIntensityDescription(conditions.getWindTurbulenceIntensity()));
		intensityLabel.setToolTipText(tip);
		sub.add(intensityLabel, "w 75lp, wrap");
		m.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(
						getIntensityDescription(conditions.getWindTurbulenceIntensity()));
			}
		});
		
		
		
		
		
		//// Temperature and pressure
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Atmospheric conditions
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Atmoscond")));
		this.add(sub, "growx, aligny 0, gapright para");
		
		
		BooleanModel isa = new BooleanModel(conditions, "ISAAtmosphere");
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
		
		m = new DoubleModel(conditions, "LaunchTemperature", UnitGroup.UNITS_TEMPERATURE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(253.15, 308.15)); // -20 ... 35
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
		
		m = new DoubleModel(conditions, "LaunchPressure", UnitGroup.UNITS_PRESSURE, 0);
		
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
		
		
		
		
		
		//// Launch site conditions
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Launch site
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Launchsite")));
		this.add(sub, "growx, split 2, aligny 0, flowy");
		
		
		// Latitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Latitude"));
		//// <html>The launch site latitude affects the gravitational pull of Earth.<br>
		//// Positive values are on the Northern hemisphere, negative values on the Southern hemisphere.
		tip = trans.get("simedtdlg.lbl.ttip.Latitude");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchLatitude", UnitGroup.UNITS_NONE, -90, 90);
		
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
		
		m = new DoubleModel(conditions, "LaunchLongitude", UnitGroup.UNITS_NONE, -180, 180);
		
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
		//// <html>The launch altitude above mean sea level.<br> 
		//// This affects the position of the rocket in the atmospheric model.
		tip = trans.get("simedtdlg.lbl.ttip.Altitude");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0);
		
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
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Launch rod
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Launchrod")));
		this.add(sub, "growx, aligny 0, wrap");
		
		
		// Length:
		label = new JLabel(trans.get("simedtdlg.lbl.Length"));
		//// The length of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Length");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodLength", UnitGroup.UNITS_LENGTH, 0);
		
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
		
		
		
		// Angle:
		label = new JLabel(trans.get("simedtdlg.lbl.Angle"));
		//// The angle of the launch rod from vertical.
		tip = trans.get("simedtdlg.lbl.ttip.Angle");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodAngle", UnitGroup.UNITS_ANGLE,
				0, SimulationOptions.MAX_LAUNCH_ROD_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, Math.PI / 9,
				SimulationOptions.MAX_LAUNCH_ROD_ANGLE));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		// Direction:
		label = new JLabel(trans.get("simedtdlg.lbl.Direction"));
		//// <html>Direction of the launch rod relative to the wind.<br>
		////  = towards the wind, 
		////  = downwind.
		tip = trans.get("simedtdlg.lbl.ttip.Direction1") +
				UnitGroup.UNITS_ANGLE.toStringUnit(0) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction2") + " " +
				UnitGroup.UNITS_ANGLE.toStringUnit(Math.PI) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction3");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodDirection", UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(-Math.PI, Math.PI));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		JButton restoreDefaults = new JButton(trans.get("simedtdlg.but.resettodefault"));
		restoreDefaults.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				SimulationOptions defaults = f.getDefault();
				conditions.copyConditionsFrom(defaults);
				
			}
			
		});
		this.add(restoreDefaults, "span, split 3, skip, gapbottom para, gapright para, right");
		
		JButton saveDefaults = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveDefaults.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				f.saveDefault(conditions);
				
			}
			
		});
		
		this.add(saveDefaults, "gapbottom para, gapright para, right");
		
	}
	
	private String getIntensityDescription(double i) {
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
	
}
