package info.openrocket.swing.gui.simulation;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.document.Simulation;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.BetterListCellRenderer;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.startup.Application;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.util.List;

public class SimulationWarningsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();

	private static Border border;
	private static Color darkErrorColor;
	private static Color warningColor;
	private static Color informationColor;

	static {
		initColors();
	}

	public SimulationWarningsPanel(final Simulation simulation) {
		super(new MigLayout("fill"));

		WarningSet warnings = simulation.getSimulatedWarnings();
		List<Warning> criticalWarnings = warnings == null ? null : warnings.getCriticalWarnings();
		List<Warning> normalWarnings = warnings == null ? null : warnings.getNormalWarnings();
		List<Warning> informativeWarnings = warnings == null ? null : warnings.getInformativeWarnings();

		// Critical warnings
		JPanel criticalPanel = createWarningsPanel(criticalWarnings, Icons.WARNING_HIGH, trans.get("SimulationWarningsPanel.lbl.CriticalWarnings"), darkErrorColor);
		this.add(criticalPanel, "spanx, grow, wrap 3lp");

		// Normal warnings
		JPanel normalPanel = createWarningsPanel(normalWarnings, Icons.WARNING_NORMAL, trans.get("SimulationWarningsPanel.lbl.NormalWarnings"), warningColor);
		this.add(normalPanel, "spanx, grow, wrap 5lp");

		// Informative warnings
		//JPanel infoPanel = createWarningsPanel(informativeWarnings, Icons.WARNING_LOW, trans.get("SimulationWarningsPanel.lbl.InformativeWarnings"), informationColor);
		//this.add(infoPanel, "spanx, grow, wrap 5lp");

		JPanel filler = new JPanel();
		this.add(filler, "grow, spanx, pushy");
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationWarningsPanel::updateColors);
	}

	private static void updateColors() {
		border = GUIUtil.getUITheme().getBorder();
		darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
		warningColor = GUIUtil.getUITheme().getWarningColor();
		informationColor = GUIUtil.getUITheme().getInformationColor();
	}

	private static JPanel createWarningsPanel(final List<Warning> warnings, final Icon icon, final String titleText, Color textColor) {
		JPanel panel = new JPanel(new MigLayout("fillx"));

		// Title
		float size = 1.1f;
		int nrOfWarnings = warnings == null ? 0 : warnings.size();
		StyledLabel title = new StyledLabel(nrOfWarnings + " " + titleText, size, StyledLabel.Style.BOLD);
		title.setFontColor(textColor);
		panel.add(title, "wrap, spanx");

		if (nrOfWarnings == 0) {
			return panel;
		}

		// Warning list
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> warningList = new JList<>(w);
		warningList.setSelectionModel(new NoSelectionModel());		// Disable selection
		warningList.setCellRenderer(new BetterListCellRenderer(icon));
		JScrollPane warningPane = new JScrollPane(warningList);
		warningList.setBorder(border);
		panel.add(warningPane, "wrap, spanx, growx");

		return panel;
	}

	private static class NoSelectionModel extends DefaultListSelectionModel {

		@Override
		public void setAnchorSelectionIndex(final int anchorIndex) {}

		@Override
		public void setLeadAnchorNotificationEnabled(final boolean flag) {}

		@Override
		public void setLeadSelectionIndex(final int leadIndex) {}

		@Override
		public void setSelectionInterval(final int index0, final int index1) { }
	}
}
