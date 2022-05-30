package net.sf.openrocket.gui.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.gui.widgets.SelectColorButton;


@SuppressWarnings("serial")
public class MassComponentConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public MassComponentConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);

		//// Left side
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		//// Attributes
		panel.add(new StyledLabel(trans.get("MassComponentCfg.lbl.Attributes"), StyledLabel.Style.BOLD), "wrap unrel");

		//// Mass component type
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.type")));
		
		final JComboBox<?> typecombo = new JComboBox<MassComponent.MassComponentType>(
				new EnumModel<MassComponent.MassComponentType>(component, "MassComponentType",
						new MassComponent.MassComponentType[] {
								MassComponent.MassComponentType.MASSCOMPONENT,
								MassComponent.MassComponentType.ALTIMETER,
								MassComponent.MassComponentType.FLIGHTCOMPUTER,
								MassComponent.MassComponentType.DEPLOYMENTCHARGE,
								MassComponent.MassComponentType.TRACKER,
								MassComponent.MassComponentType.PAYLOAD,
								MassComponent.MassComponentType.RECOVERYHARDWARE,
								MassComponent.MassComponentType.BATTERY}));
		
		panel.add(typecombo, "spanx, wrap");
		
		////  Mass
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Mass")));
		
		DoubleModel m = new DoubleModel(component, "ComponentMass", UnitGroup.UNITS_MASS, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.5)), "w 100lp, wrap");
		
		
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Density")));
		
		m = new DoubleModel(component, "Density", UnitGroup.UNITS_DENSITY_BULK, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(500, 2000, 10000)), "w 100lp, wrap");
		
		
		
		////  Mass length
		//// Length
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Length")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");
		
		
		//// Tube diameter
		//// Diameter:
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Diameter")));
		
		DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");

		////// Automatic
		JCheckBox checkAutoPackedRadius = new JCheckBox(od.getAutomaticAction());
		checkAutoPackedRadius.setText(trans.get("TransitionCfg.checkbox.Automatic"));
		panel.add(checkAutoPackedRadius, "skip, span 2, wrap");


		//// Right side
		JPanel panel2 = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");

		//// Placement
		panel2.add(new StyledLabel(trans.get("MassComponentCfg.lbl.Placement"), StyledLabel.Style.BOLD), "wrap unrel");

		//// Position relative to:
		panel2.add(new JLabel(trans.get("MassComponentCfg.lbl.PosRelativeto")));
		
        final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
        final JComboBox<?> methodCombo = new JComboBox<AxialMethod>( methodModel );
		panel2.add(methodCombo, "spanx, growx, wrap");

		//// plus
		panel2.add(new JLabel(trans.get("MassComponentCfg.lbl.plus")), "right");
		
		m = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin, "growx");
		
		panel2.add(new UnitSelector(m), "growx");
		panel2.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");
		
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("MassComponentCfg.tab.General"), null, panel,
				trans.get("MassComponentCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("MassComponentCfg.tab.Radialpos"), null, positionTab(),
				trans.get("MassComponentCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		////  Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Radialdistance")));
		
		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");
		
		
		//// Radial direction:
		panel.add(new JLabel(trans.get("MassComponentCfg.lbl.Radialdirection")));
		
		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		
		
		//// Reset button
		JButton button = new SelectColorButton(trans.get("MassComponentCfg.but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((MassComponent) component).setRadialDirection(0.0);
				((MassComponent) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right");
		
		return panel;
	}
}
