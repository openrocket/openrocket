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
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


public class TrapezoidFinSetConfig extends FinSetConfig {
	private static final Translator trans = Application.getTranslator();
	
	public TrapezoidFinSetConfig(OpenRocketDocument d, final RocketComponent component) {
		super(d, component);
		
		DoubleModel m;
		JSpinner spin;
		JComboBox combo;
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		////  Number of fins:
		JLabel label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Nbroffins"));
		//// The number of fins in the fin set.
		label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(label);
		
		IntegerModel im = new IntegerModel(component, "FinCount", 1, 8);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// The number of fins in the fin set.
		spin.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(spin, "growx, wrap");
		
		
		////  Base rotation
		//// Fin rotation:
		label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Finrotation"));
		//// The angle of the first fin in the fin set.
		label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Finrotation"));
		panel.add(label);
		
		m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		
		
		////  Fin cant:
		label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Fincant"));
		//// The angle that the fins are canted with respect to the rocket 
		label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Fincant"));
		panel.add(label);
		
		m = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE,
				-FinSet.MAX_CANT, FinSet.MAX_CANT);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-FinSet.MAX_CANT, FinSet.MAX_CANT)),
				"w 100lp, wrap");
		
		
		////  Root chord:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Rootchord")));
		
		m = new DoubleModel(component, "RootChord", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		
		////  Tip chord:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Tipchord")));
		
		m = new DoubleModel(component, "TipChord", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		////  Height:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Height")));
		
		m = new DoubleModel(component, "Height", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		
		
		
		////  Sweep length:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweeplength")));
		
		m = new DoubleModel(component, "Sweep", UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		
		// sweep slider from -1.1*TipChord to 1.1*RootChord
		DoubleModel tc = new DoubleModel(component, "TipChord", -1.1, UnitGroup.UNITS_LENGTH);
		DoubleModel rc = new DoubleModel(component, "RootChord", 1.1, UnitGroup.UNITS_LENGTH);
		panel.add(new BasicSlider(m.getSliderModel(tc, rc)), "w 100lp, wrap");
		
		
		////  Sweep angle:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweepangle")));
		
		m = new DoubleModel(component, "SweepAngle", UnitGroup.UNITS_ANGLE,
				-TrapezoidFinSet.MAX_SWEEP_ANGLE, TrapezoidFinSet.MAX_SWEEP_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI / 4, Math.PI / 4)),
				"w 100lp, wrap paragraph");
		
		
		////  Position
		//// Position relative to:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Posrelativeto")));
		
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
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.plus")), "right");
		
		m = new DoubleModel(component, "PositionValue", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap para");
		

		
		
		
		mainPanel.add(panel, "aligny 20%");
		
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy");
		
		
		
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		
		
		////  Fin cross section:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.FincrossSection")));
		combo = new JComboBox(
				new EnumModel<FinSet.CrossSection>(component, "CrossSection"));
		panel.add(combo, "span, growx, wrap");
		
		
		////  Thickness:
		panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Thickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap para");
		
		
		
		//// Material
		panel.add(materialPanel(Material.Type.BULK), "span, wrap");
		
		
		
		mainPanel.add(panel, "aligny 20%");
		
		panel.add(filletMaterialPanel(), "span, wrap");
		//// General and General properties
		tabbedPane.insertTab(trans.get("TrapezoidFinSetCfg.tab.General"), null, mainPanel,
				trans.get("TrapezoidFinSetCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);
		
		addFinSetButtons();
		
	}
}
