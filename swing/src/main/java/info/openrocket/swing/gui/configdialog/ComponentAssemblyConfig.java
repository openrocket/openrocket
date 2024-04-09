package info.openrocket.swing.gui.configdialog;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import info.openrocket.core.logging.Markers;
import info.openrocket.swing.gui.widgets.SelectColorButton;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


@SuppressWarnings("serial")
public class ComponentAssemblyConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(ComponentAssemblyConfig.class);
	private final RocketComponent component;

	private JButton split = null;

	public ComponentAssemblyConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
		super(document, component, parent);
		this.component = component;
	
		// only stages which are actually off-centerline will get the dialog here:
		if( ParallelStage.class.isAssignableFrom( component.getClass()) || PodSet.class.isAssignableFrom( component.getClass())){
			tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Assembly"), null, parallelTab( (ComponentAssembly)component ),
					trans.get("RocketCompCfg.tab.AssemblyComment"), 0);
			tabbedPane.setSelectedIndex(0);

			addSplitButton();
		}
	}

	
	private JPanel parallelTab( final ComponentAssembly boosters ){
		JPanel motherPanel = new JPanel( new MigLayout("fillx"));
		
		// radial distance method
		JLabel radiusMethodLabel = new JLabel(trans.get("RocketComponent.Position.Method.Radius.Label"));
        motherPanel.add( radiusMethodLabel, "align left");
		final EnumModel<RadiusMethod> radiusMethodModel = new EnumModel<>( boosters, "RadiusMethod", RadiusMethod.choices());
		register(radiusMethodModel);
		final JComboBox<RadiusMethod> radiusMethodCombo = new JComboBox<>( radiusMethodModel );
		motherPanel.add( radiusMethodCombo, "spanx 3, wrap");
		order.add(radiusMethodCombo);
		
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.radius"));
		motherPanel.add( radiusLabel , "align left");
		//radiusMethodModel.addEnableComponent(radiusLabel, false);
		DoubleModel radiusModel = new DoubleModel( boosters, "RadiusOffset", UnitGroup.UNITS_LENGTH, 0);
		register(radiusModel);

		JSpinner radiusSpinner = new JSpinner(radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner));
		motherPanel.add(radiusSpinner , "wmin 65lp, growx 1, align right");
		order.add(((SpinnerEditor) radiusSpinner.getEditor()).getTextField());
//		autoRadOffsModel.addEnableComponent(radiusSpinner, false);
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector);
		motherPanel.add(new BasicSlider(radiusModel.getSliderModel(0, new DoubleModel(component.getParent(), "OuterRadius", 4.0, UnitGroup.UNITS_LENGTH))),
				"gapleft para, growx 2, wrap");

		radiusMethodCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				radiusModel.stateChanged(new EventObject(e));
			}
		});

		// set angle
		JLabel angleLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( boosters, "AngleOffset", 1.0, UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI);
		register(angleModel);

		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "wmin 65lp, growx 1");
		order.add(((SpinnerEditor) angleSpinner.getEditor()).getTextField());
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector);
		motherPanel.add(new BasicSlider(angleModel.getSliderModel(-Math.PI, Math.PI)), "gapleft para, growx 2, wrap");

		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( boosters, "InstanceCount", 1);
		register(countModel);
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "wmin 65lp, growx 1, wrap 30lp");
		order.add(((SpinnerEditor) countSpinner.getEditor()).getTextField());
		
		// Position relative to
		PlacementPanel pp = new PlacementPanel(component, order);
		register(pp);
		motherPanel.add(pp, "span, grow, wrap");
		
		return motherPanel;
	}

	@Override
	public void updateFields() {
		super.updateFields();
		if (split != null) {
			split.setEnabled(component.getInstanceCount() > 1);
		}
	}

	private void addSplitButton() {
		//// Split fins
		final String btnText;
		final String btnTextTtip;
		final boolean freezeRocket;
		if (PodSet.class.isAssignableFrom(component.getClass())) {
			btnText = trans.get("ComponentAssemblyConfig.but.splitPods");
			btnTextTtip = trans.get("ComponentAssemblyConfig.but.splitPods.ttip");
			freezeRocket = true;
		} else if (ParallelStage.class.isAssignableFrom(component.getClass())) {
			btnText = trans.get("ComponentAssemblyConfig.but.splitBoosters");
			btnTextTtip = trans.get("ComponentAssemblyConfig.but.splitBoosters.ttip");
			freezeRocket = false;
		} else {
			return;
		}
		split = new SelectColorButton(btnText);
		split.setToolTipText(btnTextTtip);
		split.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Splitting " + component.getComponentName() + " into separate assemblies, instance count=" +
						component.getInstanceCount());

				// This is a bit awkward, we need to store the listeners before closing the dialog, because closing it
				// will remove them. We then add them back before the split and remove them afterwards.
				List<RocketComponent> listeners = new ArrayList<>(component.getConfigListeners());


				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						disposeDialog();

						document.startUndo("Split assembly");
						for (RocketComponent listener : listeners) {
							component.addConfigListener(listener);
						}
						component.splitInstances(freezeRocket);
						component.clearConfigListeners();
						document.stopUndo();
					}
				});
			}
		});
		split.setEnabled(component.getInstanceCount() > 1);

		addButtons(split);
		order.add(split);
	}
}
