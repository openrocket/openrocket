package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.HtmlLabel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class ParachuteConfig extends RecoveryDeviceConfig {
	
	private static final long serialVersionUID = 6108892447949958115L;
	private static final Translator trans = Application.getTranslator();
	
	public ParachuteConfig(OpenRocketDocument d, final RocketComponent component) {
		super(d, component);
		Parachute parachute = (Parachute) component;
		
		JPanel primary = new JPanel(new MigLayout());
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));
		
		
		//// Canopy
		panel.add(new StyledLabel(trans.get("ParachuteCfg.lbl.Canopy"), Style.BOLD), "wrap unrel");
		
		//// Diameter:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Diameter")));
		
		DoubleModel m = new DoubleModel(component, "Diameter", UnitGroup.UNITS_LENGTH, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)), "w 100lp, wrap");
		
		// CD
		JLabel label = new HtmlLabel(trans.get("ParachuteCfg.lbl.longA1"));
		String tip = trans.get("ParachuteCfg.lbl.longB1") +
				trans.get("ParachuteCfg.lbl.longB2") + "  " +
				trans.get("ParachuteCfg.lbl.longB3");
		label.setToolTipText(tip);
		panel.add(label);
		
		m = new DoubleModel(component, "CD", UnitGroup.UNITS_COEFFICIENT, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setToolTipText(tip);
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		//// Reset button
		JButton button = new JButton(trans.get("ParachuteCfg.but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parachute p = (Parachute) component;
				p.setCD(Parachute.DEFAULT_CD);
			}
		});
		panel.add(button, "spanx, wrap para");
		
		//// Material:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Material")));
		
		JComboBox<Material> surfaceMaterialCombo = new JComboBox<Material>(new MaterialModel(panel, component,
				Material.Type.SURFACE));
		surfaceMaterialCombo.setToolTipText(trans.get("ParachuteCfg.combo.MaterialModel"));
		panel.add( surfaceMaterialCombo, "spanx 3, growx, wrap 30lp");
		
		
		
		
		////  Shroud lines
		panel.add(new StyledLabel(trans.get("ParachuteCfg.lbl.Shroudlines"), Style.BOLD), "wrap unrel");
		
		//// Number of lines:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Numberoflines")));
		IntegerModel im = new IntegerModel(component, "LineCount", 0);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		//// Line length:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Linelength")));
		
		m = new DoubleModel(component, "LineLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)), "w 100lp, wrap");
		
		//// Material:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Material")));
		
		JComboBox<Material> shroudMaterialCombo = new JComboBox<Material>(new MaterialModel(panel, component, Material.Type.LINE,
				"LineMaterial"));
		panel.add( shroudMaterialCombo, "spanx 3, growx, wrap");
		
		
		
		primary.add(panel, "grow, gapright 20lp");
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));
		
		
		
		
		//// Position
		//// Position relative to:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Posrelativeto")));
		
		final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
        JComboBox<AxialMethod> positionCombo = new JComboBox<AxialMethod>( methodModel );
		panel.add( positionCombo, "spanx, growx, wrap");
		
		//// plus
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.plus")), "right");
		
		m = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");
		
		
		////  Spatial length
		//// Packed length:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Packedlength")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");
		
		
		//// Tube diameter
		//// Packed diameter:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Packeddiam")));
		
		DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 30lp");
		
		
		//// Deployment
		//// Deploys at:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Deploysat") + " " + CommonStrings.dagger), "");
		
		DeploymentConfiguration deploymentConfig = parachute.getDeploymentConfigurations().getDefault();
		// this issues a warning because EnumModel ipmlements ComboBoxModel without a parameter...
		ComboBoxModel<DeploymentConfiguration.DeployEvent> deployOptionsModel = new EnumModel<DeploymentConfiguration.DeployEvent>(deploymentConfig, "DeployEvent");
		JComboBox<DeploymentConfiguration.DeployEvent> eventCombo = new JComboBox<DeploymentConfiguration.DeployEvent>( deployOptionsModel );
		if( (component.getStageNumber() + 1 ) == d.getRocket().getStageCount() ){
			//	This is the bottom stage:  Restrict deployment options.
			eventCombo.removeItem( DeployEvent.LOWER_STAGE_SEPARATION );
		}
		panel.add(eventCombo, "spanx 3, growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.plusdelay")), "right");
		
		m = new DoubleModel(deploymentConfig, "DeployDelay", 0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "spanx, split");
		
		//// seconds
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.seconds")), "wrap paragraph");
		
		// Altitude:
		label = new JLabel(trans.get("ParachuteCfg.lbl.Altitude") + CommonStrings.dagger);
		altitudeComponents.add(label);
		panel.add(label);
		
		m = new DoubleModel(deploymentConfig, "DeployAltitude", UnitGroup.UNITS_DISTANCE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		altitudeComponents.add(spin);
		panel.add(spin, "growx");
		UnitSelector unit = new UnitSelector(m);
		altitudeComponents.add(unit);
		panel.add(unit, "growx");
		BasicSlider slider = new BasicSlider(m.getSliderModel(100, 1000));
		altitudeComponents.add(slider);
		panel.add(slider, "w 100lp, wrap");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap para");
		
		primary.add(panel, "grow");
		
		updateFields();
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("ParachuteCfg.tab.General"), null, primary, trans.get("ParachuteCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("ParachuteCfg.tab.Radialpos"), null, positionTab(),
				trans.get("ParachuteCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	
	
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		////  Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Radialdistance")));
		
		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");
		
		
		//// Radial direction:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Radialdirection")));
		
		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		
		
		//// Reset button
		JButton button = new JButton(trans.get("ParachuteCfg.but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((Parachute) component).setRadialDirection(0.0);
				((Parachute) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right");
		
		return panel;
	}
}
