package info.openrocket.swing.gui.plot;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.Group;
import info.openrocket.core.util.Groupable;
import info.openrocket.core.util.Utils;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.widgets.GroupableAndSearchableComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

public class PlotPanel<T extends DataType & Groupable<G>, B extends DataBranch<T>, G extends Group> extends JPanel {
	private static final Translator trans = Application.getTranslator();

	//// Custom
	protected static final String CUSTOM = trans.get("simplotpanel.CUSTOM");
	/** The "Custom" configuration - not to be used for anything other than the title. */
	private final PlotConfiguration<T, B> customConfiguration;

	/** The array of presets for the combo box. */
	private final PlotConfiguration<T, B>[] presetArray;

	private PlotConfiguration<T, B> defaultConfiguration;

	private final T[] types;
	protected PlotConfiguration<T, B> configuration;

	private JComboBox<PlotConfiguration<T, B>> configurationSelector;

	protected GroupableAndSearchableComboBox<G, T> domainTypeSelector;
	private UnitSelector domainUnitSelector;

	private JPanel typeSelectorPanel;

	protected int modifying = 0;

	public PlotPanel(T[] types, PlotConfiguration<T, B> customConfiguration, PlotConfiguration<T, B>[] presets,
					 PlotConfiguration<T, B> defaultConfiguration,
					 Component[] extraWidgetsX, Component[] extraWidgetsY) {
		super(new MigLayout("fill"));

		this.customConfiguration = customConfiguration;
		this.presetArray = presets;
		this.defaultConfiguration = defaultConfiguration;
		this.types = types;

		setConfiguration(defaultConfiguration);

		////  Configuration selector

		// Setup the combo box
		configurationSelector = new JComboBox<>(presetArray);
		for (PlotConfiguration<T, B> config : presetArray) {
			if (config.getName().equals(configuration.getName())) {
				configurationSelector.setSelectedItem(config);
			}
		}

		configurationSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// We are only concerned with ItemEvent.SELECTED to update
				// the UI when the selected item changes.
				// TODO - this should probably be implemented as an ActionListener instead
				// of ItemStateListener.
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				if (modifying > 0)
					return;
				PlotConfiguration<T, B> conf = (PlotConfiguration<T, B>) configurationSelector.getSelectedItem();
				if (conf == null || conf == customConfiguration)
					return;
				modifying++;
				setConfiguration(conf.clone().resetUnits());
				updatePlots();
				modifying--;
			}
		});
		//// Preset plot configurations:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Presetplotconf")), "spanx, split");
		this.add(configurationSelector, "growx, wrap 20lp");


		this.add(new JSeparator(JSeparator.HORIZONTAL), "spanx, growx, wrap");


		//// X axis

		//// X axis type:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Xaxistype")), "spanx, split");
		domainTypeSelector = new GroupableAndSearchableComboBox<>(Arrays.asList(types), trans.get("FlightDataComboBox.placeholder"));
		domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
		domainTypeSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				T type = (T) domainTypeSelector.getSelectedItem();
				if (type == null) {
					return;
				}
				configuration.setDomainAxisType(type);
				domainUnitSelector.setUnitGroup(type.getUnitGroup());
				domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
				setToCustom();
			}
		});
		this.add(domainTypeSelector, "gapright para");

		//// Unit:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Unit")));
		domainUnitSelector = new UnitSelector(configuration.getDomainAxisType().getUnitGroup());
		domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
		domainUnitSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				configuration.setDomainAxisUnit(domainUnitSelector.getSelectedUnit());
			}
		});
		this.add(domainUnitSelector, "width 40lp, gapright para");

		// Extra X widgets
		if (extraWidgetsX != null) {
			for (int i = 0; i < extraWidgetsX.length; i++) {
				if (i == extraWidgetsX.length - 1) {
					this.add(extraWidgetsX[i], "growx, wrap");
				} else {
					this.add(extraWidgetsX[i], "growx, wrap unrel");
				}
			}
		} else {
			this.add(new JLabel(), "wrap unrel");
		}


		//// Y axis selector panel
		//// Y axis types:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Yaxistypes")));
		//// Flight events:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Flightevents")), "wrap rel");

		typeSelectorPanel = new JPanel(new MigLayout("gapy rel"));
		JScrollPane scroll = new JScrollPane(typeSelectorPanel);
		int spanY = extraWidgetsY == null ? 1 : extraWidgetsY.length + 1;
		this.add(scroll, "spany " + spanY + ", pushy, wmin 400lp, grow 100, gapright para");

		// Extra Y widgets
		if (extraWidgetsY != null) {
			for (Component widgetsY : extraWidgetsY) {
				this.add(widgetsY, "growx, wrap");
			}
			this.add(new JPanel(), "pushy, wrap");		// Fill up the rest of the vertical space
		}


		//// New Y axis plot type
		JButton newYAxisBtn = new JButton(trans.get("simplotpanel.but.NewYaxisplottype"));
		newYAxisBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configuration.getTypeCount() >= 15) {
					JOptionPane.showMessageDialog(PlotPanel.this,
							//// A maximum of 15 plots is allowed.
							//// Cannot add plot
							trans.get("simplotpanel.OptionPane.lbl1"),
							trans.get("simplotpanel.OptionPane.lbl2"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Select new type smartly
				T type = null;
				for (T t : types) {

					boolean used = false;
					if (configuration.getDomainAxisType().equals(t)) {
						used = true;
					} else {
						for (int i = 0; i < configuration.getTypeCount(); i++) {
							if (configuration.getType(i).equals(t)) {
								used = true;
								break;
							}
						}
					}

					if (!used) {
						type = t;
						break;
					}
				}
				if (type == null) {
					type = types[0];
				}

				// Add new type
				configuration.addPlotDataType(type);
				setToCustom();
				updatePlots();
			}
		});
		this.add(newYAxisBtn, "spanx, pushx, left");
	}

	protected PlotConfiguration<T, B> getConfiguration() {
		return configuration;
	}

	protected void setConfiguration(PlotConfiguration<T, B> conf) {
		boolean modified = false;

		configuration = conf.clone();
		if (!Utils.contains(types, configuration.getDomainAxisType())) {
			configuration.setDomainAxisType(types[0]);
			modified = true;
		}

		for (int i = 0; i < configuration.getTypeCount(); i++) {
			if (!Utils.contains(types, configuration.getType(i))) {
				configuration.removePlotDataType(i);
				i--;
				modified = true;
			}
		}

		if (modified) {
			configuration.setName(CUSTOM);
		}
	}

	protected void setDefaultConfiguration(PlotConfiguration<T, B> newConfiguration) {
		defaultConfiguration = newConfiguration;
	}


	protected void setToCustom() {
		modifying++;
		configuration.setName(CUSTOM);
		configurationSelector.setSelectedItem(customConfiguration);
		modifying--;
	}

	public JDialog doPlot(Window parent) {
		throw new RuntimeException("Not implemented");
	}


	protected void updatePlots() {
		domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
		domainUnitSelector.setUnitGroup(configuration.getDomainAxisType().getUnitGroup());
		domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());

		typeSelectorPanel.removeAll();
		for (int i = 0; i < configuration.getTypeCount(); i++) {
			T type = configuration.getType(i);
			Unit unit = configuration.getUnit(i);
			int axis = configuration.getAxis(i);

			PlotTypeSelector<G, T> selector = new PlotTypeSelector<>(i, type, unit, axis, Arrays.asList(types));
			int finalI = i;
			selector.addTypeSelectionListener(e -> {
				if (modifying > 0) return;
				T selectedType = selector.getSelectedType();
				configuration.setPlotDataType(finalI, selectedType);
				selector.setUnitGroup(selectedType.getUnitGroup());
				configuration.setPlotDataUnit(finalI, selector.getSelectedUnit());
				setToCustom();
			});
			selector.addUnitSelectionListener(e -> {
				if (modifying > 0) return;
				configuration.setPlotDataUnit(finalI, selector.getSelectedUnit());
			});
			selector.addAxisSelectionListener(e -> {
				if (modifying > 0) return;
				configuration.setPlotDataAxis(finalI, selector.getSelectedAxis());
			});
			selector.addRemoveButtonListener(e -> {
				configuration.removePlotDataType(finalI);
				setToCustom();
				updatePlots();
			});
			typeSelectorPanel.add(selector, "wrap");
		}

		// In order to consistently update the UI, we need to validate before repaint.
		typeSelectorPanel.validate();
		typeSelectorPanel.repaint();
	}
}
