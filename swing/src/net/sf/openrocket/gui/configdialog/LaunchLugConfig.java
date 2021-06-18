package net.sf.openrocket.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class LaunchLugConfig extends RocketComponentConfig {
	
	private static final Translator trans = Application.getTranslator();
	
	public LaunchLugConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		JPanel primary = new JPanel(new MigLayout("fill"));
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));
		
		////  Body tube length
		//// Length:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Length")));
		
		DoubleModel m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)), "w 100lp, wrap para");
		
		
		//// Body tube diameter
		//// Outer diameter:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Outerdiam")));
		
		DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap rel");
		
		
		////  Inner diameter:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Innerdiam")));
		
		// Diameter = 2*Radius
		m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap rel");
		
		
		////  Wall thickness
		//// Thickness:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Thickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 20lp");
		
		
		////  Radial position:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Angle")));
		
		m = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, 180);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI) ), "w 100lp, wrap");
		
		// finish up the left column
		primary.add(panel, "grow, gapright 20lp");
		
		// create a new panel for the right column
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));

		//// Position relative to:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto")));		
		EnumModel<AxialMethod> positionModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
		JComboBox<AxialMethod> positionCombo = new JComboBox<AxialMethod>( positionModel );
		panel.add(positionCombo, "spanx, growx, wrap");
		
		//// plus
		final DoubleModel mAxOff = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.plus")), "right");
		spin = new JSpinner(mAxOff.getSpinnerModel());				// Plus quantity input
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(mAxOff), "growx");		// Unity selection
		panel.add(new BasicSlider(mAxOff.getSliderModel(			// Plus quantity slider
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap para");

		// Add an action listener to update the plus quantity, based on the selected reference point
		positionCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mAxOff.stateChanged(e);
			}
		});
		
		//// Material
		panel.add(materialPanel( Material.Type.BULK), "span, wrap");
		
		
		primary.add(panel, "grow");
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("LaunchLugCfg.tab.General"), null, primary,
				trans.get("LaunchLugCfg.tab.Generalprop"), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
