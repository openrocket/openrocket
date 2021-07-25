package net.sf.openrocket.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ShockCordConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public ShockCordConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		JLabel label;
		DoubleModel m;
		JSpinner spin;	

		//////  Left side
		
		// Cord length
		//// Shock cord length
		label = new JLabel(trans.get("ShockCordCfg.lbl.Shockcordlength"));
		panel.add(label);
		
		m = new DoubleModel(component, "CordLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 1, 10)), "w 100lp, wrap");
		

		// Material
		//// Shock cord material:
		panel.add(materialPanel(Material.Type.LINE, trans.get("ShockCordCfg.lbl.Shockcordmaterial"), null, "Material"), "span, wrap");
		


		/////  Right side
		JPanel panel2 = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		

		////  Position
		//// Position relative to:
		panel2.add(new JLabel(trans.get("ShockCordCfg.lbl.Posrelativeto")));
		
		final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
        final JComboBox<AxialMethod> combo = new JComboBox<AxialMethod>( methodModel );
		panel2.add(combo, "spanx, growx, wrap");
		
		//// plus
		panel2.add(new JLabel(trans.get("ShockCordCfg.lbl.plus")), "right");
		
		m = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin, "growx");
		
		panel2.add(new UnitSelector(m), "growx");
		panel2.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");
		

		////  Spatial length
		//// Packed length:
		panel2.add(new JLabel(trans.get("ShockCordCfg.lbl.Packedlength")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin, "growx");
		
		panel2.add(new UnitSelector(m), "growx");
		panel2.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");
		

		//// Tube diameter
		//// Packed diameter:
		panel2.add(new JLabel(trans.get("ShockCordCfg.lbl.Packeddiam")));
		
		DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin, "growx");
		
		panel2.add(new UnitSelector(od), "growx");
		panel2.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");
		


		//// General and General properties
		tabbedPane.insertTab(trans.get("ShockCordCfg.tab.General"), null, panel,
				trans.get("ShockCordCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("ShockCordCfg.tab.Radialpos"), null, positionTab(),
				trans.get("ShockCordCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);
	}

	// TODO: LOW: there is a lot of duplicate code here with other mass components... (e.g. in MassComponentConfig or ParachuteConfig)
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		////  Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdistance")));

		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);

		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");


		//// Radial direction:
		panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdirection")));

		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");


		//// Reset button
		JButton button = new SelectColorButton(trans.get("ShockCordCfg.but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ShockCord) component).setRadialDirection(0.0);
				((ShockCord) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right");

		return panel;
	}
}
