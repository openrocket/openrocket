package info.openrocket.swing.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;

import info.openrocket.swing.gui.widgets.SelectColorButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ShockCordConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public ShockCordConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
		super(d, component, parent);

		JPanel primary = new JPanel(new MigLayout());

		//////  Left side
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
		primary.add(panel, "grow");
		JLabel label;
		DoubleModel m;
		JSpinner spin;

		//	Attributes

		//// Shock cord length
		label = new JLabel(trans.get("ShockCordCfg.lbl.Shockcordlength"));
		panel.add(label);
		
		m = new DoubleModel(component, "CordLength", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 1, 10)), "w 100lp, wrap");

		// Material
		//// Shock cord material:
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.LINE,
				trans.get("ShockCordCfg.lbl.Shockcordmaterial"), null, "Material", order);
		register(materialPanel);
		panel.add(materialPanel, "spanx 4, wrap, gapright 40lp");

		/////  Right side
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
		primary.add(panel, "aligny 0%, grow, spany");

		{ // ----------- Placement ----------
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow, wrap");

			{//// Packed length:
				placementPanel.add(new JLabel(trans.get("ShockCordCfg.lbl.Packedlength")), "newline");

				m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
				register(m);

				spin = new JSpinner(m.getSpinnerModel());
				spin.setEditor(new SpinnerEditor(spin));
				placementPanel.add(spin, "growx");
				order.add(((SpinnerEditor) spin.getEditor()).getTextField());

				placementPanel.add(new UnitSelector(m), "growx");
				placementPanel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");
			}


			{//// Packed diameter:
				placementPanel.add(new JLabel(trans.get("ShockCordCfg.lbl.Packeddiam")));

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
				placementPanel.add(checkAutoPackedRadius, "skip, spanx 2, wrap");
				order.add(checkAutoPackedRadius);
			}
		}


		//// General and General properties
		tabbedPane.insertTab(trans.get("ShockCordCfg.tab.General"), null, primary,
				trans.get("ShockCordCfg.tab.ttip.General"), 0);
		//// Radial position and Radial position configuration
		tabbedPane.insertTab(trans.get("ShockCordCfg.tab.Radialpos"), null, positionTab(),
				trans.get("ShockCordCfg.tab.ttip.Radialpos"), 1);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}

	// TODO: LOW: there is a lot of duplicate code here with other mass components... (e.g. in MassComponentConfig or ParachuteConfig)
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		////  Radial position
		//// Radial distance:
		panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdistance")));

		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		register(m);

		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 150lp, wrap");


		//// Radial direction:
		panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdirection")));

		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		register(m);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 150lp, wrap");


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
		order.add(button);

		return panel;
	}
}
