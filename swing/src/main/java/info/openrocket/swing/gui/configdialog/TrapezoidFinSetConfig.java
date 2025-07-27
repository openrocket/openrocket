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
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;


public class TrapezoidFinSetConfig extends FinSetConfig {
	private static final long serialVersionUID = -4870745241749769842L;
	private static final Translator trans = Application.getTranslator();
	
	public TrapezoidFinSetConfig(OpenRocketDocument d, final RocketComponent component, JDialog parent) {
		super(d, component, parent);

		JPanel mainPanel = new JPanel(new MigLayout());

		// Left side
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));

		////  Number of fins:
		JLabel label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Nbroffins"));
		//// The number of fins in the fin set.
		label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(label);

		final IntegerModel finCountModel = new IntegerModel(component, "FinCount", 1, 8);
		register(finCountModel);

		final JSpinner finCountSpinner = new JSpinner(finCountModel.getSpinnerModel());
		finCountSpinner.setEditor(new SpinnerEditor(finCountSpinner));
		//// The number of fins in the fin set.
		finCountSpinner.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Nbroffins"));
		panel.add(finCountSpinner, "growx, wrap");
		order.add(((SpinnerEditor) finCountSpinner.getEditor()).getTextField());

		{////  Fin cant:
			label = new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Fincant"));
			//// The angle that the fins are canted with respect to the rocket
			label.setToolTipText(trans.get("TrapezoidFinSetCfg.lbl.ttip.Fincant"));
			panel.add(label);

			final DoubleModel cantModel = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE, -FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS);
			register(cantModel);

			final JSpinner cantSpinner = new JSpinner(cantModel.getSpinnerModel());
			cantSpinner.setEditor(new SpinnerEditor(cantSpinner));
			panel.add(cantSpinner, "growx");
			order.add(((SpinnerEditor) cantSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(cantModel), "growx");
			panel.add(new BasicSlider(cantModel.getSliderModel(-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS)),
					"w 100lp, wrap");
		}

		{////  Root chord:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Rootchord")));

			final DoubleModel rootChordModel = new DoubleModel(component, "RootChord", UnitGroup.UNITS_LENGTH, 0);
			register(rootChordModel);

			final JSpinner rootChordSpinner = new JSpinner(rootChordModel.getSpinnerModel());
			rootChordSpinner.setEditor(new SpinnerEditor(rootChordSpinner));
			panel.add(rootChordSpinner, "growx");
			order.add(((SpinnerEditor) rootChordSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(rootChordModel), "growx");
			panel.add(new BasicSlider(rootChordModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Tip chord:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Tipchord")));

			final DoubleModel tipChordModel = new DoubleModel(component, "TipChord", UnitGroup.UNITS_LENGTH, 0);
			register(tipChordModel);

			final JSpinner tipChordSpinner = new JSpinner(tipChordModel.getSpinnerModel());
			tipChordSpinner.setEditor(new SpinnerEditor(tipChordSpinner));
			panel.add(tipChordSpinner, "growx");
			order.add(((SpinnerEditor) tipChordSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(tipChordModel), "growx");
			panel.add(new BasicSlider(tipChordModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Height:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Height")));

			final DoubleModel heightModel = new DoubleModel(component, "Height", UnitGroup.UNITS_LENGTH, 0);
			register(heightModel);

			final JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
			heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
			panel.add(heightSpinner, "growx");
			order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(heightModel), "growx");
			panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.05, 0.2)), "w 100lp, wrap");
		}

		{////  Sweep length:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweeplength")));

			final DoubleModel sweepDistanceModel = new DoubleModel(component, "Sweep", UnitGroup.UNITS_LENGTH);
			register(sweepDistanceModel);
			component.addChangeListener(sweepDistanceModel);
			final JSpinner sweepDistanceSpinner = new JSpinner(sweepDistanceModel.getSpinnerModel());
			sweepDistanceSpinner.setEditor(new SpinnerEditor(sweepDistanceSpinner));
			panel.add(sweepDistanceSpinner, "growx");
			order.add(((SpinnerEditor) sweepDistanceSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(sweepDistanceModel), "growx");

			// sweep slider from -1.1*TipChord to 1.1*RootChord
			DoubleModel tc = new DoubleModel(component, "TipChord", -1.1, UnitGroup.UNITS_LENGTH);
			DoubleModel rc = new DoubleModel(component, "RootChord", 1.1, UnitGroup.UNITS_LENGTH);
			register(tc);
			register(rc);
			panel.add(new BasicSlider(sweepDistanceModel.getSliderModel(tc, rc)), "w 100lp, wrap");
		}

		{////  Sweep angle:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Sweepangle")));

			final DoubleModel sweepAngleModel = new DoubleModel(component, "SweepAngle", UnitGroup.UNITS_ANGLE,
					-TrapezoidFinSet.MAX_SWEEP_ANGLE, TrapezoidFinSet.MAX_SWEEP_ANGLE);
			register(sweepAngleModel);
			component.addChangeListener(sweepAngleModel);

			final JSpinner sweepAngleSpinner = new JSpinner(sweepAngleModel.getSpinnerModel());
			sweepAngleSpinner.setEditor(new SpinnerEditor(sweepAngleSpinner));
			panel.add(sweepAngleSpinner, "growx");
			order.add(((SpinnerEditor) sweepAngleSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(sweepAngleModel), "growx");
			panel.add(new BasicSlider(sweepAngleModel.getSliderModel(-Math.PI / 4, Math.PI / 4)),
					"w 100lp, wrap 30lp");
		}

		{////  Fin cross section:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.FincrossSection")));
			EnumModel<FinSet.CrossSection> em = new EnumModel<>(component, "CrossSection");
			register(em);
			JComboBox<FinSet.CrossSection> sectionCombo = new JComboBox<>(em);
			panel.add(sectionCombo, "span, growx, wrap");
			order.add(sectionCombo);
		}

		{ ////  Thickness:
			panel.add(new JLabel(trans.get("TrapezoidFinSetCfg.lbl.Thickness")));

			final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
			register(thicknessModel);

			final JSpinner thicknessSpinner = new JSpinner(thicknessModel.getSpinnerModel());
			thicknessSpinner.setEditor(new SpinnerEditor(thicknessSpinner));
			panel.add(thicknessSpinner, "growx");
			order.add(((SpinnerEditor) thicknessSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(thicknessModel), "growx");
			panel.add(new BasicSlider(thicknessModel.getSliderModel(0, 0.01)), "w 100lp, wrap para");
		}

		mainPanel.add(panel, "aligny 0, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));

		{//// -------- Placement -------
			// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow");

			{ ///  Fin rotation
				label = new JLabel(trans.get("FinSetCfg.lbl.FinRotation"));
				label.setToolTipText(trans.get("FinSetCfg.lbl.FinRotation.ttip"));
				placementPanel.add(label, "newline");

				final DoubleModel baseRotationModel = new DoubleModel(component, "BaseRotation",
						UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI);
				register(baseRotationModel);

				final JSpinner baseRotationSpinner = new JSpinner(baseRotationModel.getSpinnerModel());
				baseRotationSpinner.setEditor(new SpinnerEditor(baseRotationSpinner));
				placementPanel.add(baseRotationSpinner, "growx");
				order.add(((SpinnerEditor) baseRotationSpinner.getEditor()).getTextField());

				placementPanel.add(new UnitSelector(baseRotationModel), "growx");
				placementPanel.add(new BasicSlider(baseRotationModel.getSliderModel()), "w 100lp, wrap");
			}
		}

		{//// Material
			MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
			register(materialPanel);
			panel.add(materialPanel, "span, grow, wrap");
		}

		{// Root fillets
			panel.add(filletMaterialPanel(), "span, grow, wrap");
		}

		mainPanel.add(panel, "aligny 0");

		//// General and General properties
		tabbedPane.insertTab(trans.get("TrapezoidFinSetCfg.tab.General"), null, mainPanel,
				trans.get("TrapezoidFinSetCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);

		addFinSetButtons();

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
}
