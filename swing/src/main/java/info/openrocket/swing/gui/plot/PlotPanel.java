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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlotPanel<T extends DataType & Groupable<G>,
		B extends DataBranch<T>,
		G extends Group,
		C extends PlotConfiguration<T, B>,
		S extends PlotTypeSelector<T, G>> extends JPanel {
	protected static final Translator trans = Application.getTranslator();

	//// Custom
	protected static final String CUSTOM = trans.get("simplotpanel.CUSTOM");
	/** The "Custom" configuration - not to be used for anything other than the title. */
	private final C customConfiguration;

	/** The array of presets for the combo box. */
	private final C[] presetArray;

	private C defaultConfiguration;
	private final List<PlotConfigurationListener<C>> configurationListeners = new ArrayList<>();

	// Data types for the x and y axis + plot configuration
	protected final T[] typesX;
	protected final T[] typesY;
	protected C configuration;

	protected final JComboBox<C> configurationSelector;
	protected JComboBox<T> domainTypeSelector;
	private UnitSelector domainUnitSelector;
	private final JPanel typeSelectorPanel;

	protected int modifying = 0;

	public PlotPanel(T[] typesX, T[] typesY, C customConfiguration, C[] presets,
					 C defaultConfiguration, Component[] extraWidgetsX, Component[] extraWidgetsY) {
		super(new MigLayout("fill"));

		this.customConfiguration = customConfiguration;
		this.presetArray = presets;
		this.defaultConfiguration = defaultConfiguration;
		this.typesX = typesX;
		this.typesY = typesY;

		setConfiguration(defaultConfiguration);

		////  Configuration selector

		// Setup the combo box
		configurationSelector = new JComboBox<>(presetArray);
		for (C config : presetArray) {
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
				C conf = (C) configurationSelector.getSelectedItem();
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
		addXAxisSelector(typesX, extraWidgetsX);

		//// Y axis selector panel
		typeSelectorPanel = addYAxisSelector(typesY, extraWidgetsY);
	}

	protected void addXAxisSelector(T[] typesX, Component[] extraWidgetsX) {
		//// X axis type:
		this.add(new JLabel(trans.get("simplotpanel.lbl.Xaxistype")), "spanx, split");
		domainTypeSelector = new GroupableAndSearchableComboBox<>(Arrays.asList(typesX), trans.get("FlightDataComboBox.placeholder"));
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
	}

	protected JPanel addYAxisSelector(T[] typesY, Component[] extraWidgetsY) {
		final JPanel typeSelectorPanel;
		//// Y axis types:
		JPanel yPanel = new JPanel(new MigLayout("fill, ins 0"));
		yPanel.add(new JLabel(trans.get("simplotpanel.lbl.Yaxistypes")), "wrap rel");

		typeSelectorPanel = new JPanel(new MigLayout("gapy rel"));
		JScrollPane scroll = new JScrollPane(typeSelectorPanel);
		yPanel.add(scroll, "pushy, grow 100");
		if (extraWidgetsY != null) {
			this.add(yPanel, "pushy, wmin 400lp, grow 100, gapright para");
		} else {
			this.add(yPanel, "pushy, spanx, wmin 400lp, grow 100, wrap");
		}

		// Extra Y widgets
		if (extraWidgetsY != null) {
			JPanel extraYPanel = new JPanel(new MigLayout("fill, ins 0"));
			for (Component widgetsY : extraWidgetsY) {
				extraYPanel.add(widgetsY, "growx, wrap rel");
			}
			extraYPanel.add(new JPanel(), "pushy, grow 100"); 	// Fill up the rest of the vertical space
			this.add(extraYPanel, "pushy, grow 100, wrap");
		}


		//// New Y axis plot type
		JButton newYAxisBtn = new JButton(trans.get("simplotpanel.but.NewYaxisplottype"));
		newYAxisBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configuration.getDataCount() >= 15) {
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
				for (T t : typesY) {
					boolean used = false;
					if (configuration.getDomainAxisType().equals(t)) {
						used = true;
					} else {
						for (int i = 0; i < configuration.getDataCount(); i++) {
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
					type = typesY[0];
				}

				// Add new type
				configuration.addPlotDataType(type);
				setToCustom();
				updatePlots();
			}
		});
		this.add(newYAxisBtn, "spanx, pushx, left");
		return typeSelectorPanel;
	}

	public C getConfiguration() {
		return configuration;
	}

	protected void setConfiguration(C conf) {
		boolean modified = false;

		configuration = (C) conf.clone();
		if (!Utils.contains(typesX, configuration.getDomainAxisType())) {
			configuration.setDomainAxisType(typesX[0]);
			modified = true;
		}

		for (int i = 0; i < configuration.getDataCount(); i++) {
			if (!Utils.contains(typesY, configuration.getType(i))) {
				configuration.removePlotDataType(i);
				i--;
				modified = true;
			}
		}

		if (modified) {
			configuration.setName(CUSTOM);
		}

		for (PlotConfigurationListener<C> listener : configurationListeners) {
			listener.onPlotConfigurationChanged(configuration);
		}
	}

	protected void setDefaultConfiguration(C newConfiguration) {
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
		if (domainTypeSelector != null) {
			domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
			domainUnitSelector.setUnitGroup(configuration.getDomainAxisType().getUnitGroup());
			domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
		}

		typeSelectorPanel.removeAll();
		for (int i = 0; i < configuration.getDataCount(); i++) {
			T type = configuration.getType(i);
			Unit unit = configuration.getUnit(i);
			int axis = configuration.getAxis(i);

			S selector = createSelector(i, type, unit, axis);
			addSelectionListeners(selector, i);

			typeSelectorPanel.add(selector, "wrap");
		}

		// In order to consistently update the UI, we need to validate before repaint.
		typeSelectorPanel.validate();
		typeSelectorPanel.repaint();
	}

	protected S createSelector(int i, T type, Unit unit, int axis) {
		return (S) new PlotTypeSelector<>(i, type, unit, axis, Arrays.asList(typesY));
	}

	protected void addSelectionListeners(S selector, final int idx) {
		// Type
		selector.addTypeSelectionListener(e -> {
			if (modifying > 0) return;
			T selectedType = selector.getSelectedType();
			configuration.setPlotDataType(idx, selectedType);
			selector.setUnitGroup(selectedType.getUnitGroup());
			configuration.setPlotDataUnit(idx, selector.getSelectedUnit());
			setToCustom();
		});

		// Unit
		selector.addUnitSelectionListener(e -> {
			if (modifying > 0) return;
			configuration.setPlotDataUnit(idx, selector.getSelectedUnit());
		});

		// Axis
		selector.addAxisSelectionListener(e -> {
			if (modifying > 0) return;
			configuration.setPlotDataAxis(idx, selector.getSelectedAxis());
		});

		// Remove button
		selector.addRemoveButtonListener(e -> {
			configuration.removePlotDataType(idx);
			setToCustom();
			updatePlots();
		});
	}

	public void addPlotConfigurationListener(PlotConfigurationListener<C> listener) {
		this.configurationListeners.add(listener);
	}

	public interface PlotConfigurationListener<C extends PlotConfiguration<?, ?>> {
		void onPlotConfigurationChanged(C newConfiguration);
	}
}
