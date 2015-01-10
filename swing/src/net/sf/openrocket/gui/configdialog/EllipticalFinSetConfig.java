package net.sf.openrocket.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class EllipticalFinSetConfig extends FinSetConfig {
	private static final Translator trans = Application.getTranslator();
	
	public EllipticalFinSetConfig(OpenRocketDocument d, final RocketComponent component) {
		super(d, component);
		
		DoubleModel m;
		JSpinner spin;
		JComboBox combo;
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		////  Number of fins
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Nbroffins")));
		
		IntegerModel im = new IntegerModel(component, "FinCount", 1, 8);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx, wrap");
		
		
		////  Base rotation
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Rotation")));
		
		m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		
		
		////  Fin cant
		JLabel label = new JLabel(trans.get("EllipticalFinSetCfg.Fincant"));
		//// "The angle that the fins are canted with respect to the rocket
		label.setToolTipText(trans.get("EllipticalFinSetCfg.ttip.Fincant"));
		panel.add(label);
		
		m = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE,
				-FinSet.MAX_CANT, FinSet.MAX_CANT);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-FinSet.MAX_CANT, FinSet.MAX_CANT)),
				"w 100lp, wrap");
		
		
		
		////  Root chord
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Rootchord")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		////  Height
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Height")));
		
		m = new DoubleModel(component, "Height", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		////  Position
		//// Position relative to:
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Positionrelativeto")));
		
		combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
								RocketComponent.Position.TOP,
								RocketComponent.Position.MIDDLE,
								RocketComponent.Position.BOTTOM,
								RocketComponent.Position.ABSOLUTE
						}));
		panel.add(combo, "spanx, growx, wrap");
		
		//// plus
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.plus")), "right");
		
		m = new DoubleModel(component, "PositionValue", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");
		
		
		
		//// Right portion
		mainPanel.add(panel, "aligny 20%");
		
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy");
		
		
		
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		
		////  Cross section
		//// Fin cross section:
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.FincrossSection")), "span, split");
		combo = new JComboBox(
				new EnumModel<FinSet.CrossSection>(component, "CrossSection"));
		panel.add(combo, "growx, wrap unrel");
		
		
		////  Thickness:
		panel.add(new JLabel(trans.get("EllipticalFinSetCfg.Thickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");
		
		
		
		//// Material
		panel.add(materialPanel(Material.Type.BULK), "span, wrap");
		
		panel.add(filletMaterialPanel(), "span, wrap");
	
		
		
		
		mainPanel.add(panel, "aligny 20%");
		
		addFinSetButtons();
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("EllipticalFinSetCfg.General"), null, mainPanel,
				trans.get("EllipticalFinSetCfg.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}
