package net.sf.openrocket.gui.configdialog;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
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
	
	public RailButtonConfig( OpenRocketDocument document, RocketComponent component) {
		super(document, component);
	
		// For DEBUG purposes
//		if( component instanceof AxialStage ){
//			System.err.println(" Dumping AxialStage tree info for devel / debugging.");
//			System.err.println(component.toDebugTree());
//		}
				

		//// General and General properties
		tabbedPane.insertTab( trans.get("RailBtnCfg.tab.General"), null, buttonTab( (RailButton)component ), trans.get("RailBtnCfg.tab.GeneralProp"), 0);
		tabbedPane.setSelectedIndex(0);
		
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
			panel.add(new UnitSelector(ODModel), "growx");
			panel.add(new BasicSlider(ODModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap");
		}
		{ //// Height
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.TotalHeight")));
			DoubleModel heightModel = new DoubleModel(component, "TotalHeight", UnitGroup.UNITS_LENGTH, 0);
			JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap");
		}

		{ //// Angular Position:
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.Angle")));
			DoubleModel angleModel = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, +180);
			JSpinner angleSpinner = new JSpinner( angleModel.getSpinnerModel());
			angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
			panel.add(angleSpinner, "growx");
			panel.add(new UnitSelector( angleModel), "growx");
			panel.add(new BasicSlider( angleModel.getSliderModel(-180, 180)), "w 100lp, wrap");
		}

		{ //// Position relative to:
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.PosRelativeTo")));
			
			final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
			JComboBox<AxialMethod> relToCombo = new JComboBox<AxialMethod>( methodModel );
			panel.add( relToCombo, "spanx, growx, wrap");
		}
			
		{ //// plus
			panel.add(new JLabel(trans.get("RailBtnCfg.lbl.Plus")), "right");
			DoubleModel offsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
			JSpinner offsetSpinner = new JSpinner(offsetModel.getSpinnerModel());
			offsetSpinner.setEditor(new SpinnerEditor(offsetSpinner));
			panel.add(offsetSpinner, "growx");
			panel.add(new UnitSelector(offsetModel), "growx");
			panel.add(new BasicSlider(offsetModel.getSliderModel(
					new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
					new DoubleModel(component.getParent(), "Length"))),
					"w 100lp, wrap para");
			
		}

		primary.add(panel, "grow, gapright 201p");
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));
		
		//// Instance count
		panel.add( instanceablePanel(rbc), "span, wrap");

		//// Material
		panel.add(materialPanel(Material.Type.BULK),"span, wrap");

		primary.add(panel, "grow");
		
		return primary;
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
	}
	
}
