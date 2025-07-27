package info.openrocket.swing.gui.configdialog;


import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;

@SuppressWarnings("serial")
public class LaunchLugConfig extends RocketComponentConfig {
	
	private static final Translator trans = Application.getTranslator();
	
	public LaunchLugConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		JPanel primary = new JPanel(new MigLayout());
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));
		
		////  Body tube length
		//// Length:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Length")));
		
		DoubleModel m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		focusElement = spin;
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)), "w 100lp, wrap 20lp");
		
		
		//// Body tube diameter
		//// Outer diameter:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Outerdiam")));
		
		DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		register(od);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap rel");
		
		
		////  Inner diameter:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Innerdiam")));
		
		// Diameter = 2*Radius
		m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap rel");
		
		
		////  Wall thickness
		//// Thickness:
		panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Thickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");

		// -------- Instances ------
		InstancesPanel ip = new InstancesPanel(component, order);
		register(ip);
		panel.add(ip, "span, grow, wrap para");
		
		primary.add(panel, "grow, gapright 20lp");

		// Right panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		{//// Placement
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow, wrap");

			////  Rotation:
			placementPanel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Angle")), "newline");

			m = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI);
			register(m);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			placementPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			placementPanel.add(new UnitSelector(m), "growx");
			placementPanel.add(new BasicSlider(m.getSliderModel()), "w 100lp, wrap");
		}

		//// Material
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
		register(materialPanel);
		panel.add(materialPanel, "span, grow, wrap");

		primary.add(panel, "grow");
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("LaunchLugCfg.tab.General"), null, primary,
				trans.get("LaunchLugCfg.tab.Generalprop"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
