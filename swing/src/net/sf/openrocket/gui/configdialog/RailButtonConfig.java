package net.sf.openrocket.gui.configdialog;

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
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

@SuppressWarnings("serial")
public class RailButtonConfig extends RocketComponentConfig {
	
	private static final Translator trans = Application.getTranslator();
	
	public RailButtonConfig( OpenRocketDocument document, RocketComponent component, JDialog parent) {
		super(document, component, parent);

		//// General and General properties
		tabbedPane.insertTab( trans.get("RailBtnCfg.tab.General"), null, buttonTab( (RailButton)component ), trans.get("RailBtnCfg.tab.GeneralProp"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this panel
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	private JPanel buttonTab( final RailButton rbc ){
		
		JPanel primary = new JPanel(new MigLayout());
		
		JPanel panel = new JPanel( new MigLayout("gap rel unrel, ins 0"));
		
			
		{ //// Outer Diameter
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.OuterDiam")));
			DoubleModel ODModel = new DoubleModel(component, "OuterDiameter", UnitGroup.UNITS_LENGTH, 0);
			JSpinner ODSpinner = new JSpinner( ODModel.getSpinnerModel());
			ODSpinner.setEditor(new SpinnerEditor(ODSpinner));
			panel.add(ODSpinner, "growx");
			order.add(((SpinnerEditor) ODSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(ODModel), "growx");
			panel.add(new BasicSlider(ODModel.getSliderModel(0, 0.02)), "w 100lp, wrap");
		}
		{ //// Inner Diameter
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.InnerDiam")));
			DoubleModel IDModel = new DoubleModel(component, "InnerDiameter", UnitGroup.UNITS_LENGTH, 0);
			JSpinner IDSpinner = new JSpinner(IDModel.getSpinnerModel());
			IDSpinner.setEditor(new SpinnerEditor(IDSpinner));
			panel.add(IDSpinner, "growx");
			order.add(((SpinnerEditor) IDSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(IDModel), "growx");
			panel.add(new BasicSlider(IDModel.getSliderModel(0, 0.02)), "w 100lp, wrap 20lp");
		}
		{ //// Base Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.BaseHeight")));
			DoubleModel heightModel = new DoubleModel(component, "BaseHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, new DoubleModel(component, "MaxBaseHeight", UnitGroup.UNITS_LENGTH))),
					"w 100lp, wrap");
		}
		{ //// Flange Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.FlangeHeight")));
			DoubleModel heightModel = new DoubleModel(component, "FlangeHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, new DoubleModel(component, "MaxFlangeHeight", UnitGroup.UNITS_LENGTH))),
					"w 100lp, wrap");
		}
		{ //// Total Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.TotalHeight")));
			DoubleModel heightModel = new DoubleModel(component, "TotalHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(new DoubleModel(component, "MinTotalHeight", UnitGroup.UNITS_LENGTH), 0.02)),
					"w 100lp, wrap 20lp");
		}
		{ //// Screw height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.ScrewHeight")));
			DoubleModel heightModel = new DoubleModel(component, "ScrewHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.02)), "w 100lp, wrap 30lp");
		}

		{ //// Instance Count
			panel.add(new JLabel(trans.get("RocketCompCfg.lbl.InstanceCount")));
			IntegerModel countModel = new IntegerModel(component, "InstanceCount", 1);
			JSpinner countSpinner = new JSpinner( countModel.getSpinnerModel());
			countSpinner.setEditor(new SpinnerEditor(countSpinner));
			panel.add(countSpinner, "growx, wrap rel");
			order.add(((SpinnerEditor) countSpinner.getEditor()).getTextField());
		}

		{ //// Instance separation
			panel.add(new JLabel(trans.get("RocketCompCfg.lbl.InstanceSeparation")));
			DoubleModel separationModel = new DoubleModel(component, "InstanceSeparation", UnitGroup.UNITS_LENGTH);
			JSpinner separationSpinner = new JSpinner( separationModel.getSpinnerModel());
			separationSpinner.setEditor(new SpinnerEditor(separationSpinner));
			panel.add(separationSpinner, "growx");
			order.add(((SpinnerEditor) separationSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(separationModel), "growx");
			double maxSeparationDistance = 0.1;
			if (component.getParent() != null && component.getParent().getLength() > 0) {
				maxSeparationDistance = component.getParent().getLength();
			}
			panel.add(new BasicSlider(separationModel.getSliderModel(0, 0.001, maxSeparationDistance)), "w 100lp, wrap para");
		}


		primary.add(panel, "grow, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		{// -------- Placement ------
			//// Position relative to:
			JPanel placementPanel = new PlacementPanel(component, order);
			panel.add(placementPanel, "span, grow, wrap");

			{ //// Rotation:
				placementPanel.add(new JLabel(trans.get("RailBtnCfg.lbl.Angle")), "newline");
				DoubleModel angleModel = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, +180);
				JSpinner angleSpinner = new JSpinner( angleModel.getSpinnerModel());
				angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
				placementPanel.add(angleSpinner, "growx");
				order.add(((SpinnerEditor) angleSpinner.getEditor()).getTextField());
				placementPanel.add(new UnitSelector(angleModel), "growx");
				placementPanel.add(new BasicSlider(angleModel.getSliderModel(-Math.PI, Math.PI)), "w 100lp");
			}
		}

		//// Material
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
		panel.add(materialPanel,"span, grow, wrap");

		primary.add(panel, "grow");

		return primary;
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
