package info.openrocket.swing.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.*;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.HtmlLabel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class ParachuteConfig extends RecoveryDeviceConfig {
	
	private static final long serialVersionUID = 6108892447949958115L;
	private static final Translator trans = Application.getTranslator();
	
	public ParachuteConfig(OpenRocketDocument d, final RocketComponent component, JDialog parent) {
		super(d, component, parent);
		Parachute parachute = (Parachute) component;

		// Left Side
		JPanel primary = new JPanel(new MigLayout());
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		// ---------------------------- Canopy ----------------------------
		JPanel canopyPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]"));
		canopyPanel.setBorder(BorderFactory.createTitledBorder(trans.get("ParachuteCfg.lbl.Canopy")));

		//// Diameter:
		canopyPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Diameter")));
		
		DoubleModel m = new DoubleModel(component, "Diameter", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		canopyPanel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		canopyPanel.add(new UnitSelector(m), "growx");
		canopyPanel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)), "w 150lp, wrap");

		// TODO COMPLETE Spill hole development
/*		pacanopyPanelnel.add(new JLabel(trans.get("ParachuteCfg.lbl.SpillDia") + CommonStrings.daggerDouble));

		m = new DoubleModel(component, "SpillDia", UnitGroup.UNITS_LENGTH, 0, 0.08);
		register(m);
			// The "max" value does not affect the slider maximum, and manual entry above that value is possible.

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		canopyPanel.add(spin, "growx");
		canopyPanel.add(new UnitSelector(m), "growx");
		canopyPanel.add(new BasicSlider(m.getSliderModel(0, 0.01, .1)), "w 150lp, wrap");
			// The slider maximum value is "max", however, manual entry above that value is possible.
*/
		// TODO END Spill hole development

		//// Material:
		canopyPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Material")), "wrap rel");

		MaterialModel mm = new MaterialModel(canopyPanel, component, Material.Type.SURFACE);
		register(mm);
		JComboBox<Material> surfaceMaterialCombo = new JComboBox<>(mm);
		surfaceMaterialCombo.setToolTipText(trans.get("ParachuteCfg.combo.MaterialModel"));
		canopyPanel.add(surfaceMaterialCombo, "spanx, growx, wrap 15lp");
		order.add(surfaceMaterialCombo);

		// Drag Coefficient:
		// CD
		JLabel label = new HtmlLabel(trans.get("ParachuteCfg.lbl.longA1"));
		String tip = trans.get("ParachuteCfg.lbl.longB1") +
				trans.get("ParachuteCfg.lbl.longB2") + "  " +
				trans.get("ParachuteCfg.lbl.longB3");
		label.setToolTipText(tip);
		canopyPanel.add(label);
		
		m = new DoubleModel(component, "CD", UnitGroup.UNITS_COEFFICIENT, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setToolTipText(tip);
		spin.setEditor(new SpinnerEditor(spin));
		canopyPanel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		//// Reset button
		JButton button = new SelectColorButton(trans.get("ParachuteCfg.but.Reset"));
		button.setToolTipText(String.format(trans.get("ParachuteCfg.but.ResetCd.ttip"), Parachute.DEFAULT_CD));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parachute p = (Parachute) component;
				p.setCD(Parachute.DEFAULT_CD);
			}
		});
		canopyPanel.add(button, "spanx");
		order.add(button);

		panel.add(canopyPanel, "spanx, growx, wrap 10lp");


		//  ---------------------------- Shroud lines ----------------------------
		JPanel shroudPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]"));
		shroudPanel.setBorder(BorderFactory.createTitledBorder(trans.get("ParachuteCfg.lbl.Shroudlines")));

		//// Number of lines:
		shroudPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Numberoflines")));
		IntegerModel im = new IntegerModel(component, "LineCount", 0);
		register(im);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		shroudPanel.add(spin, "growx, wrap");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		//// Line length:
		shroudPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Linelength")));
		
		m = new DoubleModel(component, "LineLength", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		shroudPanel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		shroudPanel.add(new UnitSelector(m), "growx");
		shroudPanel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)), "w 150lp, wrap");
		
		//// Material:
		shroudPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Material")), "spanx, wrap rel");

		mm = new MaterialModel(shroudPanel, component, Material.Type.LINE, "LineMaterial");
		register(mm);
		JComboBox<Material> shroudMaterialCombo = new JComboBox<>(mm);
		shroudPanel.add(shroudMaterialCombo, "spanx, growx");
		order.add(shroudMaterialCombo);

		panel.add(shroudPanel, "spanx, wrap");
		primary.add(panel, "grow, gapright 20lp");

		// Right side
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		{// ---------------------------- Placement ----------------------------
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow, wrap");

			//// Packed length:
			placementPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Packedlength")), "newline");

			m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
			register(m);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			placementPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			placementPanel.add(new UnitSelector(m), "growx");
			placementPanel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");


			//// Packed diameter:
			placementPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Packeddiam")));

			final DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
			register(od);

			spin = new JSpinner(od.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			placementPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			placementPanel.add(new UnitSelector(od), "growx");
			placementPanel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");

			////// Automatic
			JCheckBox checkAutoPackedRadius = new JCheckBox(od.getAutomaticAction());
			checkAutoPackedRadius.setText(trans.get("ParachuteCfg.checkbox.AutomaticPacked"));
			checkAutoPackedRadius.setToolTipText(trans.get("ParachuteCfg.checkbox.AutomaticPacked.ttip"));
			placementPanel.add(checkAutoPackedRadius, "skip, spanx 2");
			order.add(checkAutoPackedRadius);
		}

		{// ---------------------------- Deployment ----------------------------
			JPanel deploymentPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]"));
			deploymentPanel.setBorder(BorderFactory.createTitledBorder(trans.get("ParachuteCfg.lbl.Deployment")));

			//// Deploys at:
			deploymentPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.Deploysat") + " " + CommonStrings.dagger), "");

			DeploymentConfiguration deploymentConfig = parachute.getDeploymentConfigurations().getDefault();
			// this issues a warning because EnumModel implements ComboBoxModel without a parameter...
			EnumModel<DeploymentConfiguration.DeployEvent> deployOptionsModel = new EnumModel<>(deploymentConfig, "DeployEvent");
			register(deployOptionsModel);
			JComboBox<DeploymentConfiguration.DeployEvent> eventCombo = new JComboBox<>(deployOptionsModel);
			if ((component.getStageNumber() + 1) == d.getRocket().getStageCount()) {
				//	This is the bottom stage:  Restrict deployment options.
				eventCombo.removeItem(DeployEvent.LOWER_STAGE_SEPARATION);
			}
			eventCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateFields();
				}
			});
			deploymentPanel.add(eventCombo, "spanx 3, growx, wrap");
			order.add(eventCombo);

			// ... and delay
			//// plus
			deploymentPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.plusdelay")), "right");

			m = new DoubleModel(deploymentConfig, "DeployDelay", 0);
			register(m);
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin, 3));
			deploymentPanel.add(spin, "spanx, split");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			//// seconds
			deploymentPanel.add(new JLabel(trans.get("ParachuteCfg.lbl.seconds")), "wrap paragraph");

			//// Altitude:
			label = new JLabel(trans.get("ParachuteCfg.lbl.Altitude") + CommonStrings.dagger);
			altitudeComponents.add(label);
			deploymentPanel.add(label);

			m = new DoubleModel(deploymentConfig, "DeployAltitude", UnitGroup.UNITS_DISTANCE, 0);
			register(m);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			altitudeComponents.add(spin);
			deploymentPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
			UnitSelector unit = new UnitSelector(m);
			altitudeComponents.add(unit);
			deploymentPanel.add(unit, "growx");
			BasicSlider slider = new BasicSlider(m.getSliderModel(100, 1000));
			altitudeComponents.add(slider);
			deploymentPanel.add(slider, "w 100lp, wrap");

			deploymentPanel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap");

			panel.add(deploymentPanel, "spanx, growx, wrap para");
		}

		primary.add(panel, "grow");
		
		updateFields();
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("ParachuteCfg.tab.General"), null, primary, trans.get("ParachuteCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("ParachuteCfg.tab.Radialpos"), null, positionTab(),
				trans.get("ParachuteCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}


	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 25lp", "[][65lp::][30lp::]", ""));
		
		////  Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Radialdistance")), "gapright para");
		
		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 150lp, wrap");
		
		
		//// Radial direction:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Radialdirection")), "gapright para");
		
		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 150lp, wrap");
		
		
		//// Reset button
		JButton button = new SelectColorButton(trans.get("ParachuteCfg.but.Reset"));
		button.setToolTipText("ParachuteCfg.but.ResetRadial.ttip");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((Parachute) component).setRadialDirection(0.0);
				((Parachute) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right");
		order.add(button);

		return panel;
	}
}
