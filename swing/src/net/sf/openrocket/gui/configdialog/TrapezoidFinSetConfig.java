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
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


public class TrapezoidFinSetConfig extends FinSetConfig {
	private static final long serialVersionUID = -4870745241749769842L;
	private static final Translator trans = Application.getTranslator();
	
	public TrapezoidFinSetConfig(OpenRocketDocument d, final RocketComponent component) {
		super(d, component);

		JPanel mainPanel = new JPanel(new MigLayout());


		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		////  Number of fins:
		JLabel label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Nbroffins"));
		//// The number of fins in the fin set.
		label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(label);

		final IntegerModel finCountModel = new IntegerModel(component, "FinCount", 1, 8);

		final JSpinner finCountSpinner = new JSpinner(finCountModel.getSpinnerModel());
		finCountSpinner.setEditor(new SpinnerEditor(finCountSpinner));
		//// The number of fins in the fin set.
		finCountSpinner.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(finCountSpinner, "growx, wrap");


		{ ///  Base rotation
			label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Finrotation"));
			//// The angle of the first fin in the fin set.
			label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Finrotation"));
			panel.add(label);

			final DoubleModel baseRotationModel = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);

			final JSpinner baseRotationSpinner = new JSpinner(baseRotationModel.getSpinnerModel());
			baseRotationSpinner.setEditor(new SpinnerEditor(baseRotationSpinner));
			panel.add(baseRotationSpinner, "growx");

			panel.add(new UnitSelector(baseRotationModel), "growx");
			panel.add(new BasicSlider(baseRotationModel.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		}

		{////  Fin cant:
			label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Fincant"));
			//// The angle that the fins are canted with respect to the rocket
			label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Fincant"));
			panel.add(label);

			final DoubleModel cantModel = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE, -FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS);

			final JSpinner cantSpinner = new JSpinner(cantModel.getSpinnerModel());
			cantSpinner.setEditor(new SpinnerEditor(cantSpinner));
			panel.add(cantSpinner, "growx");

			panel.add(new UnitSelector(cantModel), "growx");
			panel.add(new BasicSlider(cantModel.getSliderModel(-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS)),
					"w 100lp, wrap");
		}

		{////  Root chord:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Rootchord")));

			final DoubleModel rootChordModel = new DoubleModel(component, "RootChord", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner rootChordSpinner = new JSpinner(rootChordModel.getSpinnerModel());
			rootChordSpinner.setEditor(new SpinnerEditor(rootChordSpinner));
			panel.add(rootChordSpinner, "growx");

			panel.add(new UnitSelector(rootChordModel), "growx");
			panel.add(new BasicSlider(rootChordModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Tip chord:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Tipchord")));

			final DoubleModel tipChordModel = new DoubleModel(component, "TipChord", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner tipChordSpinner = new JSpinner(tipChordModel.getSpinnerModel());
			tipChordSpinner.setEditor(new SpinnerEditor(tipChordSpinner));
			panel.add(tipChordSpinner, "growx");

			panel.add(new UnitSelector(tipChordModel), "growx");
			panel.add(new BasicSlider(tipChordModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Height:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Height")));

			final DoubleModel heightModel = new DoubleModel(component, "Height", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");

			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Sweep length:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweeplength")));

			final DoubleModel sweepDistanceModel = new DoubleModel(component, "Sweep", UnitGroup.UNITS_LENGTH);
			component.addChangeListener(sweepDistanceModel);
			final JSpinner sweepDistanceSpinner = new JSpinner(sweepDistanceModel.getSpinnerModel());
			sweepDistanceSpinner.setEditor(new SpinnerEditor(sweepDistanceSpinner));
			panel.add(sweepDistanceSpinner, "growx");

			panel.add(new UnitSelector(sweepDistanceModel), "growx");

			// sweep slider from -1.1*TipChord to 1.1*RootChord
			DoubleModel tc = new DoubleModel(component, "TipChord", -1.1, UnitGroup.UNITS_LENGTH);
			DoubleModel rc = new DoubleModel(component, "RootChord", 1.1, UnitGroup.UNITS_LENGTH);
			panel.add(new BasicSlider(sweepDistanceModel.getSliderModel(tc, rc)), "w 100lp, wrap");
		}

		{////  Sweep angle:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweepangle")));

			final DoubleModel sweepAngleModel = new DoubleModel(component, "SweepAngle", UnitGroup.UNITS_ANGLE,
					-TrapezoidFinSet.MAX_SWEEP_ANGLE, TrapezoidFinSet.MAX_SWEEP_ANGLE);
			component.addChangeListener(sweepAngleModel);

			final JSpinner sweepAngleSpinner = new JSpinner(sweepAngleModel.getSpinnerModel());
			sweepAngleSpinner.setEditor(new SpinnerEditor(sweepAngleSpinner));
			panel.add(sweepAngleSpinner, "growx");

			panel.add(new UnitSelector(sweepAngleModel), "growx");
			panel.add(new BasicSlider(sweepAngleModel.getSliderModel(-Math.PI / 4, Math.PI / 4)),
					"w 100lp, wrap paragraph");
		}

		{//// Position relative to:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Posrelativeto")));

			final EnumModel<AxialMethod> methodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods);
			final JComboBox<AxialMethod> positionCombo = new JComboBox<AxialMethod>(methodModel);

			panel.add(positionCombo, "spanx, growx, wrap");
			//// plus
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.plus")), "right");

			final DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
			final JSpinner axialOffsetSpinner = new JSpinner(axialOffsetModel.getSpinnerModel());
			axialOffsetSpinner.setEditor(new SpinnerEditor(axialOffsetSpinner));
			panel.add(axialOffsetSpinner, "growx");

			panel.add(new UnitSelector(axialOffsetModel), "growx");
			panel.add(new BasicSlider(axialOffsetModel.getSliderModel(
					new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
					new DoubleModel(component.getParent(), "Length"))),
					"w 100lp, wrap para");


			mainPanel.add(panel, "aligny 20%");

			mainPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy");
		}
		
		
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));


		{////  Fin cross section:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.FincrossSection")));
			JComboBox<FinSet.CrossSection> sectionCombo = new JComboBox<FinSet.CrossSection>(
					new EnumModel<FinSet.CrossSection>(component, "CrossSection"));
			panel.add(sectionCombo, "span, growx, wrap");


			////  Thickness:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Thickness")));

			final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner thicknessSpinner = new JSpinner(thicknessModel.getSpinnerModel());
			thicknessSpinner.setEditor(new SpinnerEditor(thicknessSpinner));
			panel.add(thicknessSpinner, "growx");

			panel.add(new UnitSelector(thicknessModel), "growx");
			panel.add(new BasicSlider(thicknessModel.getSliderModel(0, 0.01)), "w 100lp, wrap para");
		}
		
		
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
