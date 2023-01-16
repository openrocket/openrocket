package net.sf.openrocket.gui.configdialog;


import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class TubeFinSetConfig extends RocketComponentConfig {
	private static final long serialVersionUID = 508482875624928676L;
	private static final Translator trans = Application.getTranslator();
	
	public TubeFinSetConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		JPanel primary = new JPanel(new MigLayout());
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));
		
		////  Number of fins
		panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Nbroffins")));
		
		IntegerModel im = new IntegerModel(component, "FinCount", 1, 8);
		
		JSpinner spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		//// Length:
		panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Length")));
		
		DoubleModel m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		focusElement = spin;
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)), "w 100lp, wrap para");
		
		
		//// Outer diameter:
		panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Outerdiam")));
		
		DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap rel");
		
		JCheckBox check = new JCheckBox(od.getAutomaticAction());
		//// Automatic
		check.setText(trans.get("TubeFinSetCfg.checkbox.Automatic"));
		panel.add(check, "skip, span 2, wrap");
		order.add(check);

		////  Inner diameter:
		panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Innerdiam")));

		m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap rel");
		
		
		//// Thickness:
		panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Thickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 20lp");
		
		primary.add(panel, "grow, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		{ //// Placement
			//// Position relative to:
			JPanel placementPanel = new PlacementPanel(component, order);
			panel.add(placementPanel, "span, grow, wrap");

			//// Fin rotation:
			JLabel label = new JLabel(trans.get("TubeFinSetCfg.lbl.Finrotation"));
			//// The angle of the first fin in the fin set.
			label.setToolTipText(trans.get("TubeFinSetCfg.lbl.ttip.Finrotation"));
			placementPanel.add(label, "newline");

			m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			placementPanel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			placementPanel.add(new UnitSelector(m), "growx");
			placementPanel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		}

		{//// Material
			MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
			panel.add(materialPanel, "span, grow, wrap");
		}

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
