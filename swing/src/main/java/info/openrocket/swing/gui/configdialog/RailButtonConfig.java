package info.openrocket.swing.gui.configdialog;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;


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
			DoubleModel odModel = new DoubleModel(component, "OuterDiameter", UnitGroup.UNITS_LENGTH, 0);
			register(odModel);
			JSpinner odSpinner = new JSpinner( odModel.getSpinnerModel());
			odSpinner.setEditor(new SpinnerEditor(odSpinner));
			panel.add(odSpinner, "growx");
			order.add(((SpinnerEditor) odSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(odModel), "growx");
			panel.add(new BasicSlider(odModel.getSliderModel(0, 0.02)), "w 100lp, wrap");
		}
		{ //// Inner Diameter
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.InnerDiam")));
			DoubleModel idModel = new DoubleModel(component, "InnerDiameter", UnitGroup.UNITS_LENGTH, 0);
			register(idModel);
			JSpinner idSpinner = new JSpinner(idModel.getSpinnerModel());
			idSpinner.setEditor(new SpinnerEditor(idSpinner));
			panel.add(idSpinner, "growx");
			order.add(((SpinnerEditor) idSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(idModel), "growx");
			panel.add(new BasicSlider(idModel.getSliderModel(0, 0.02)), "w 100lp, wrap 20lp");
		}
		{ //// Base Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.BaseHeight")));
			DoubleModel heightModel = new DoubleModel(component, "BaseHeight", UnitGroup.UNITS_LENGTH, 0);
			register(heightModel);
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
			register(heightModel);
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
			register(heightModel);
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
			register(heightModel);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.02)), "w 100lp, wrap 30lp");
		}
  
		// -------- Instances ------
		InstancesPanel ip = new InstancesPanel(component, order);
		register(ip);
		panel.add(ip, "span, grow, wrap para");


		primary.add(panel, "grow, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

		{// -------- Placement ------
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow, wrap");

			{ //// Rotation:
				placementPanel.add(new JLabel(trans.get("RailBtnCfg.lbl.Angle")), "newline");
				DoubleModel angleModel = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, +180);
				register(angleModel);
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
		register(materialPanel);
		panel.add(materialPanel,"span, grow, wrap");

		primary.add(panel, "grow");

		return primary;
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
