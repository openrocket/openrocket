package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADataTypeGroup;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.plot.PlotPanel;

import javax.swing.JDialog;
import java.awt.Component;
import java.awt.Window;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CAPlotPanel extends PlotPanel<CADataType, CADataBranch, CADataTypeGroup,
		CAPlotConfiguration, CAPlotTypeSelector> {
	/** The "Custom" configuration - not to be used for anything other than the title. */
	private static final CAPlotConfiguration CUSTOM_CONFIGURATION;

	/** The array of presets for the combo box. */
	private static final CAPlotConfiguration[] PRESET_ARRAY;


	/** The current default configuration, set each time a plot is made. */
	private static CAPlotConfiguration DEFAULT_CONFIGURATION =
			CAPlotConfiguration.DEFAULT_CONFIGURATIONS[0].resetUnits();

	private final ComponentAnalysisPlotExportPanel parent;

	static {
		CUSTOM_CONFIGURATION = new CAPlotConfiguration(CUSTOM);

		PRESET_ARRAY = Arrays.copyOf(CAPlotConfiguration.DEFAULT_CONFIGURATIONS,
				CAPlotConfiguration.DEFAULT_CONFIGURATIONS.length + 1);
		PRESET_ARRAY[PRESET_ARRAY.length - 1] = CUSTOM_CONFIGURATION;
	}

	private CAPlotPanel(ComponentAnalysisPlotExportPanel parent, CADomainDataType[] typesX, CADataType[] typesY) {
		super(typesX, typesY, CUSTOM_CONFIGURATION, PRESET_ARRAY, DEFAULT_CONFIGURATION, null, null);

		this.parent = parent;
		updatePlots();
	}

	public static CAPlotPanel create(ComponentAnalysisPlotExportPanel parent, CADataType[] typesY) {
		CADomainDataType[] typesX = new CADomainDataType[] { parent.getSelectedParameter() };

		return new CAPlotPanel(parent, typesX, typesY);
	}

	@Override
	protected void addXAxisSelector(CADataType[] typesX, Component[] extraWidgetsX) {
		// Don't add the X axis selector (this is added in the ComponentAnalysisPlotExportPanel)
	}

	@Override
	protected void addSelectionListeners(CAPlotTypeSelector selector, final int idx) {
		super.addSelectionListeners(selector, idx);

		selector.addComponentSelectionListener(e -> {
			if (modifying > 0) return;
			configuration.setPlotDataComponents(idx, selector.getSelectedComponents());
		});
	}

	@Override
	protected CAPlotTypeSelector createSelector(int i, CADataType type, Unit unit, int axis) {
		return new CAPlotTypeSelector(parent, parent.getDocument(), i, type, unit, axis, List.of(typesY),
				parent.getComponentsForType(type), configuration, configuration.getComponents(i));
	}

	public void setXAxis(CADomainDataType type) {
		if (modifying > 0 || type == null)
			return;
		configuration.setDomainAxisType(type);
		setToCustom();
	}

	@Override
	protected void setDefaultConfiguration(CAPlotConfiguration newConfiguration) {
		super.setDefaultConfiguration(newConfiguration);
		DEFAULT_CONFIGURATION = newConfiguration;
	}

	@Override
	public JDialog doPlot(Window parentWindow) {
		CADataBranch branch = this.parent.runParameterSweep();
		CAPlotConfiguration config = this.getConfiguration();
		return CAPlotDialog.create(parent.getParentWindow(), trans.get("CAPlotPanel.lbl.PlotTitle"), config,
				Collections.singletonList(branch));
	}
}
