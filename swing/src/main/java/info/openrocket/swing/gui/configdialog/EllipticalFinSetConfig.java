package info.openrocket.swing.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;

@SuppressWarnings("serial")
public class EllipticalFinSetConfig extends FinSetConfig {
	private static final Translator trans = Application.getTranslator();
	
	public EllipticalFinSetConfig(OpenRocketDocument d, final RocketComponent component, JDialog parent) {
		super(d, component, parent);
		
		DoubleModel m;
		JSpinner spin;
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		// Left side
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
		
		////  Number of fins
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Nbroffins")));
		
		IntegerModel im = new IntegerModel(component, "FinCount", 1, 8);
		register(im);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		////  Fin cant
		JLabel label = new JLabel(trans.get("EllipticalFinSetCfg.Fincant"));
		//// "The angle that the fins are canted with respect to the rocket
		label.setToolTipText(trans.get("EllipticalFinSetCfg.ttip.Fincant"));
		panel.add(label);
		
		m = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE,
				-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS)),
				"w 100lp, wrap");
		
		
		
		////  Root chord
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Rootchord")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		////  Height
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Height")));
		
		m = new DoubleModel(component, "Height", UnitGroup.UNITS_LENGTH, 0);
		register(m);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap 30lp");

		////  Cross section
		{//// Fin cross section:
			panel.add(new JLabel(trans.get("EllipticalFinSetCfg.FincrossSection")), "span, split");
			EnumModel<FinSet.CrossSection> em = new EnumModel<>(component, "CrossSection");
			register(em);
			JComboBox<FinSet.CrossSection> sectionCombo = new JComboBox<>(em);
			panel.add(sectionCombo, "growx, wrap unrel");
			order.add(sectionCombo);
		}

		{////  Thickness:
			panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Thickness")));

			m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
			register(m);

			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");
		}

		mainPanel.add(panel, "aligny 0, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));

		{// ------ Placement ------
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow");

			{////  Fin rotation
				label = new JLabel(trans.get("FinSetCfg.lbl.FinRotation"));
				label.setToolTipText(trans.get("FinSetCfg.lbl.FinRotation.ttip"));
				placementPanel.add(label, "newline");

				m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);
				register(m);

				spin = new JSpinner(m.getSpinnerModel());
				spin.setEditor(new SpinnerEditor(spin));
				placementPanel.add(spin, "growx");
				order.add(((SpinnerEditor) spin.getEditor()).getTextField());

				placementPanel.add(new UnitSelector(m), "growx");
				placementPanel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
			}
		}

		{//// Material
			MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
			register(materialPanel);
			panel.add(materialPanel, "span, grow, wrap");
		}

		{//// Root fillets
			panel.add(filletMaterialPanel(), "span, grow, wrap");
		}
		
		mainPanel.add(panel, "aligny 0");
		
		addFinSetButtons();
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("EllipticalFinSetCfg.General"), null, mainPanel,
				trans.get("EllipticalFinSetCfg.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
}
