package info.openrocket.swing.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.MaterialModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.HtmlLabel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.widgets.SelectColorButton;


public class StreamerConfig extends RecoveryDeviceConfig {
	private static final long serialVersionUID = -4445736703470494588L;
	private static final Translator trans = Application.getTranslator();
	
	public StreamerConfig(OpenRocketDocument d, final RocketComponent component, JDialog parent) {
		super(d, component, parent);
		Streamer streamer = (Streamer) component;

		JPanel primary = new JPanel(new MigLayout());

		//	Left side
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]"));
		
		//// ---------------------------- Attributes ----------------------------

		//// Strip length:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Striplength")));
		
		DoubleModel m = new DoubleModel(component, "StripLength", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.6, 1.5)), "w 150lp, wrap");
		
		//// Strip width:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Stripwidth")));
		
		m = new DoubleModel(component, "StripWidth", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.2)), "w 150lp, wrap 10lp");

		//// Strip area:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Striparea")));
		
		m = new DoubleModel(component, "Area", UnitGroup.UNITS_AREA, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.25)), "w 150lp, wrap");
		
		//// Aspect ratio:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Aspectratio")));
		
		m = new DoubleModel(component, "AspectRatio", UnitGroup.UNITS_NONE, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		//		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(2, 15)), "skip, w 150lp, wrap 10lp");

		//// Material:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Material")));

		MaterialModel mm = new MaterialModel(panel, component, Material.Type.SURFACE);
		register(mm);
		JComboBox<Material> streamerMaterialCombo = new JComboBox<>(mm);
		//// The component material affects the weight of the component.
		streamerMaterialCombo.setToolTipText(trans.get("StreamerCfg.combo.ttip.MaterialModel"));
		panel.add(streamerMaterialCombo, "spanx 3, growx, wrap 15lp");
		order.add(streamerMaterialCombo);

		// CD
		//// <html>Drag coefficient C<sub>D</sub>:
		JLabel label = new HtmlLabel(trans.get("StreamerCfg.lbl.longA1"));
		//// <html>The drag coefficient relative to the total area of the streamer.<br>
		String tip = trans.get("StreamerCfg.lbl.longB1") +
				//// "A larger drag coefficient yields a slowed descent rate.
				trans.get("StreamerCfg.lbl.longB2");
		label.setToolTipText(tip);
		panel.add(label);
		
		m = new DoubleModel(component, "CD", UnitGroup.UNITS_COEFFICIENT, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setToolTipText(tip);
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		JCheckBox check = new JCheckBox(m.getAutomaticAction());
		//// Automatic
		check.setText(trans.get("StreamerCfg.lbl.AutomaticCd"));
		check.setToolTipText(trans.get("StreamerCfg.lbl.AutomaticCd.ttip"));
		panel.add(check, "skip, span, wrap");
		order.add(check);
		
		//// The drag coefficient is relative to the area of the streamer.
		panel.add(new StyledLabel(trans.get("StreamerCfg.lbl.longC1"),
				-1, StyledLabel.Style.ITALIC), "gapleft para, span, wrap");

		primary.add(panel, "grow, gapright 20lp");

		//	Right side
		panel = new JPanel(new MigLayout("ins 0"));


		{//// ---------------------------- Placement ----------------------------
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);

			////  Packed length:
			placementPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Packedlength")), "newline");

			m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
			register(m);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			placementPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			placementPanel.add(new UnitSelector(m), "growx");
			placementPanel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");

			//// Packed diameter:
			placementPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Packeddiam")));

			DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
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

			panel.add(placementPanel, "growx, wrap");
		}
		
		//// ---------------------------- Deployment ----------------------------
		JPanel deploymentPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]"));
		deploymentPanel.setBorder(BorderFactory.createTitledBorder(trans.get("StreamerCfg.lbl.Deployment")));

		//// Deploys at:
		deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Deploysat") + " " + CommonStrings.dagger), "");
		
		DeploymentConfiguration deploymentConfig = streamer.getDeploymentConfigurations().getDefault();
		EnumModel<DeploymentConfiguration.DeployEvent> em = new EnumModel<>(deploymentConfig, "DeployEvent");
		register(em);
		JComboBox<DeploymentConfiguration.DeployEvent> eventCombo = new JComboBox<>(em);
		if ((component.getStageNumber() + 1) == d.getRocket().getStageCount()) {
			//	This is the bottom stage.  restrict deployment options.
			eventCombo.removeItem( DeployEvent.LOWER_STAGE_SEPARATION );
		}
		eventCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateFields();
			}
		});
		deploymentPanel.add( eventCombo, "spanx 3, growx, wrap");
		order.add(eventCombo);
		
		// ... and delay
		//// plus
		deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.plusdelay")), "right");
		
		m = new DoubleModel(deploymentConfig, "DeployDelay", 0);
		register(m);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		deploymentPanel.add(spin, "spanx, split");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		//// seconds
		deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.seconds")), "wrap paragraph");
		
		// Altitude:
		label = new JLabel(trans.get("StreamerCfg.lbl.Altitude") + CommonStrings.dagger);
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

		panel.add(deploymentPanel, "growx");
		primary.add(panel, "grow");
		
		updateFields();
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("StreamerCfg.tab.General"), null, primary,
				trans.get("StreamerCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("StreamerCfg.tab.Radialpos"), null, positionTab(),
				trans.get("StreamerCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	
	
	
	
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		//// Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Radialdistance")));
		
		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 150lp, wrap");

		//// Radial direction:
		panel.add(new JLabel(trans.get("StreamerCfg.lbl.Radialdirection")));
		
		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 150lp, wrap");
		
		
		//// Reset button
		JButton button = new SelectColorButton(trans.get("StreamerCfg.but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((Streamer) component).setRadialDirection(0.0);
				((Streamer) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right");
		order.add(button);

		return panel;
	}
}
