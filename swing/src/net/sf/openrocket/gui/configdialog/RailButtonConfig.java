package net.sf.openrocket.gui.configdialog;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
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
		order.add(closeButton);		// Make sure the close button is the last component
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	private JPanel buttonTab( final RailButton rbc ){
		
		JPanel primary = new JPanel(new MigLayout("fill")); 
		
		JPanel panel = new JPanel( new MigLayout());
		
			
		{ //// Outer Diameter
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.OuterDiam")));
			DoubleModel ODModel = new DoubleModel(component, "OuterDiameter", UnitGroup.UNITS_LENGTH, 0);
			JSpinner ODSpinner = new JSpinner( ODModel.getSpinnerModel());
			ODSpinner.setEditor(new SpinnerEditor(ODSpinner));
			panel.add(ODSpinner, "growx");
			order.add(((SpinnerEditor) ODSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(ODModel), "growx");
			panel.add(new BasicSlider(ODModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap");
		}
		{ //// Inner Diameter
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.InnerDiam")));
			DoubleModel IDModel = new DoubleModel(component, "InnerDiameter", UnitGroup.UNITS_LENGTH, 0);
			JSpinner IDSpinner = new JSpinner(IDModel.getSpinnerModel());
			IDSpinner.setEditor(new SpinnerEditor(IDSpinner));
			panel.add(IDSpinner, "growx");
			order.add(((SpinnerEditor) IDSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(IDModel), "growx");
			panel.add(new BasicSlider(IDModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap para");
		}
		{ //// Base Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.BaseHeight")));
			DoubleModel heightModel = new DoubleModel(component, "BaseHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap");
		}
		{ //// Flange Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.FlangeHeight")));
			DoubleModel heightModel = new DoubleModel(component, "FlangeHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap");
		}
		{ //// Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.TotalHeight")));
			DoubleModel heightModel = new DoubleModel(component, "TotalHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap para");
		}

		{ //// Angular Position:
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.Angle")));
			DoubleModel angleModel = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, +180);
			JSpinner angleSpinner = new JSpinner( angleModel.getSpinnerModel());
			angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
			panel.add(angleSpinner, "growx");
			order.add(((SpinnerEditor) angleSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector( angleModel), "growx");
			panel.add(new BasicSlider( angleModel.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		}

		primary.add(panel, "grow, gapright 201p");
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));

		{ //// Position relative to:
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.PosRelativeTo")));

			final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
			JComboBox<AxialMethod> relToCombo = new JComboBox<AxialMethod>( methodModel );
			panel.add( relToCombo, "spanx, growx, wrap");
			order.add(relToCombo);
		}

		{ //// plus
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.Plus")), "right");
			DoubleModel offsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
			JSpinner offsetSpinner = new JSpinner(offsetModel.getSpinnerModel());
			offsetSpinner.setEditor(new SpinnerEditor(offsetSpinner));
			focusElement = offsetSpinner;
			panel.add(offsetSpinner, "growx");
			order.add(((SpinnerEditor) offsetSpinner.getEditor()).getTextField());
			panel.add(new UnitSelector(offsetModel), "growx");
			panel.add(new BasicSlider(offsetModel.getSliderModel(
							new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
							new DoubleModel(component.getParent(), "Length"))),
					"w 100lp, wrap para");

		}
		//// Instance count
		panel.add(instanceablePanel(rbc), "span, wrap");

		//// Material
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
		panel.add(materialPanel,"span, wrap");

		primary.add(panel, "grow");

		return primary;
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
