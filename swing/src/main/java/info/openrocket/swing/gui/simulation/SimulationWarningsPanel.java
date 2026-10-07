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
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;

import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class SimulationWarningsPanel extends JPanel {
	private static final int PREFERRED_SCROLL_HEIGHT = 200;
	private static final Translator trans = Application.getTranslator();

	private static Border border;
	private static Color dimTextColor;
	private static Color darkErrorColor;
	private static Color warningColor;
	private static Color informationColor;

	static {
		initColors();
	}

	public SimulationWarningsPanel(final Simulation simulation) {
		this(simulation, true);
	}

	/**
	 * Creates a warning settings and results panel.
	 *
	 * @param simulation simulation whose warning settings are edited
	 * @param showSimulationWarnings whether to show results from the previous run
	 */
	public SimulationWarningsPanel(final Simulation simulation, boolean showSimulationWarnings) {
		super(new MigLayout("fill", "[grow]", "[grow]"));
		setMinimumSize(new Dimension(0, 0));

		SimulationOptions options = simulation.getOptions();
		JTabbedPane tabs = new JTabbedPane();
		tabs.setMinimumSize(new Dimension(0, 0));
		tabs.addTab(trans.get("SimulationWarningsPanel.tab.Messages"),
				createTabScrollPane(createMessagesPanel(simulation, showSimulationWarnings)));
		tabs.addTab(trans.get("simpanel.col.Configuration"),
				createTabScrollPane(createConfigurationPanel(options)));
		if (!showSimulationWarnings) {
			// Multi-simulation editing has no single set of run messages to display.
			tabs.setSelectedIndex(1);
		}
		this.add(tabs, "grow, wmin 0, hmin 0");
	}

	/**
	 * Keeps the nested tab header fixed while its potentially tall content scrolls.
	 */
	private JScrollPane createTabScrollPane(JPanel panel) {
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(null);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setMinimumSize(new Dimension(0, 0));
		Dimension preferredSize = scrollPane.getPreferredSize();
		scrollPane.setPreferredSize(new Dimension(preferredSize.width, PREFERRED_SCROLL_HEIGHT));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}

	private JPanel createMessagesPanel(Simulation simulation, boolean showSimulationWarnings) {
		JPanel panel = new JPanel(new MigLayout("fillx", "[grow]"));

		if (showSimulationWarnings) {
			addSimulationWarnings(panel, simulation);
		} else {
			StyledLabel unavailable = new StyledLabel(
					trans.get("SimulationConfigDialog.tab.warnDis.ttip"), StyledLabel.Style.ITALIC);
			panel.add(unavailable, "alignx center, wrap 20lp");
		}

		JPanel filler = new JPanel();
		panel.add(filler, "grow, spanx, pushy, growy 0.5");
		return panel;
	}

	private JPanel createConfigurationPanel(SimulationOptions options) {
		JPanel panel = new ConfigurationPanel();
		panel.add(new RecoveryWarningsPanel(options), "growx, wrap rel");
		panel.add(createDefaultsButtons(options), "wrap 20lp");
		panel.add(new JPanel(), "grow, spanx, pushy, growy 0.5");
		return panel;
	}

	/** Tracks the viewport width until the threshold controls need horizontal scrolling. */
	private static class ConfigurationPanel extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;

		ConfigurationPanel() {
			super(new MigLayout("fillx", "[grow]"));
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension size = super.getPreferredSize();
			if (getParent() instanceof JViewport viewport) {
				size.width = Math.max(getMinimumSize().width, viewport.getExtentSize().width);
			}
			return size;
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return super.getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return Math.max(16, (orientation == SwingConstants.VERTICAL
					? visibleRect.height : visibleRect.width) - 16);
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return getParent() instanceof JViewport viewport && viewport.getWidth() >= getMinimumSize().width;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return getParent() instanceof JViewport viewport && viewport.getHeight() >= getPreferredSize().height;
		}
	}

	private JPanel createDefaultsButtons(SimulationOptions options) {
		JPanel panel = new JPanel(new MigLayout("insets 0"));
		JButton resetButton = new JButton(trans.get("simedtdlg.but.resettodefault"));
		resetButton.addActionListener(event -> resetWarningThresholds(options));
		panel.add(resetButton, "split 2");

		JButton saveButton = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveButton.addActionListener(event -> saveWarningThresholds(options));
		panel.add(saveButton);
		return panel;
	}

	private void resetWarningThresholds(SimulationOptions options) {
		ApplicationPreferences preferences = Application.getPreferences();
		options.setRecoverySpeedWarning(preferences.getRecoverySpeedWarning());
		options.setRecoveryDrogueMainHighSpeedWarning(preferences.getRecoveryDrogueMainHighSpeedWarning());
		options.setRecoveryDrogueMainLowSpeedWarning(preferences.getRecoveryDrogueMainLowSpeedWarning());
	}

	private void saveWarningThresholds(SimulationOptions options) {
		ApplicationPreferences preferences = Application.getPreferences();
		preferences.setRecoverySpeedWarning(options.getRecoverySpeedWarning());
		preferences.setRecoveryDrogueMainHighSpeedWarning(options.getRecoveryDrogueMainHighSpeedWarning());
		preferences.setRecoveryDrogueMainLowSpeedWarning(options.getRecoveryDrogueMainLowSpeedWarning());
	}

	private void addSimulationWarnings(JPanel target, Simulation simulation) {
		WarningSet warnings = simulation.getSimulatedWarnings();
		List<Warning> criticalWarnings = warnings == null ? null : warnings.getCriticalWarnings();
		List<Warning> normalWarnings = warnings == null ? null : warnings.getNormalWarnings();
		List<Warning> informationalWarnings = warnings == null ? null : warnings.getInformationalWarnings();

		boolean hasCriticalWarnings = criticalWarnings != null && !criticalWarnings.isEmpty();
		boolean hasNormalWarnings = normalWarnings != null && !normalWarnings.isEmpty();
		boolean hasInformationalWarnings = informationalWarnings != null && !informationalWarnings.isEmpty();

		// No warnings
		if (!hasCriticalWarnings && !hasNormalWarnings && !hasInformationalWarnings) {
			StyledLabel noWarnings = new StyledLabel(trans.get("SimulationWarningsPanel.lbl.NoWarnings"), 1.1f,
					StyledLabel.Style.ITALIC);
			noWarnings.setToolTipText(trans.get("SimulationWarningsPanel.lbl.NoWarnings.ttip"));
			target.add(noWarnings, "spanx, alignx center, wrap 20lp");
		} else {
			// Critical warnings
			if (hasCriticalWarnings) {
				JPanel criticalPanel = createWarningsPanel(criticalWarnings, Icons.WARNING_HIGH,
						trans.get("SimulationWarningsPanel.lbl.CriticalWarnings"),
						trans.get("SimulationWarningsPanel.lbl.CriticalWarnings.desc"), darkErrorColor);
				String wrap = hasNormalWarnings || hasInformationalWarnings ? "wrap 20lp" : "wrap";
				target.add(criticalPanel, "spanx, grow, " + wrap);
			}

			// Normal warnings
			if (hasNormalWarnings) {
				JPanel normalPanel = createWarningsPanel(normalWarnings, Icons.WARNING_NORMAL,
						trans.get("SimulationWarningsPanel.lbl.NormalWarnings"),
						trans.get("SimulationWarningsPanel.lbl.NormalWarnings.desc"), warningColor);
				String wrap = hasInformationalWarnings ? "wrap 20lp" : "wrap";
				target.add(normalPanel, "spanx, grow, " + wrap);
			}

			// Informational warnings
			if (hasInformationalWarnings) {
				JPanel infoPanel = createWarningsPanel(informationalWarnings, Icons.WARNING_LOW,
						trans.get("SimulationWarningsPanel.lbl.InformationalWarnings"),
						trans.get("SimulationWarningsPanel.lbl.InformationalWarnings.desc"), informationColor);
				target.add(infoPanel, "spanx, grow, wrap");
			}
		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationWarningsPanel::updateColors);
	}

	public static void updateColors() {
		border = GUIUtil.getUITheme().getBorder();
		dimTextColor = UITheme.getColor(UITheme.Keys.TEXT_DIM);
		darkErrorColor = UITheme.getColor(UITheme.Keys.DARK_ERROR);
		warningColor = UITheme.getColor(UITheme.Keys.WARNING);
		informationColor = UITheme.getColor(UITheme.Keys.INFO);
	}

	private static JPanel createWarningsPanel(final List<Warning> warnings, final Icon icon,
											  final String titleText, final String descriptionText, Color textColor) {
		JPanel panel = new JPanel(new MigLayout("fillx, insets 1"));

		// Title
		float size = 2f;
		int nrOfWarnings = warnings == null ? 0 : warnings.size();
		StyledLabel title = new StyledLabel(titleText, size, StyledLabel.Style.BOLD);
		title.setFontColor(textColor);
		panel.add(title);

		if (nrOfWarnings == 0) {
			return panel;
		}

		// Description
		StyledLabel description = new StyledLabel("\u2014 " + descriptionText, 0f, StyledLabel.Style.ITALIC);
		description.setFontColor(dimTextColor);
		panel.add(description, "gapleft 10lp, left, wrap, spanx, pushx");

		// Warning list
		Warning[] w = warnings.toArray(new Warning[0]);
		final JList<Warning> warningList = new JList<>(w);
		warningList.setCellRenderer(new BetterListCellRenderer(icon));
		warningList.setBorder(border);
		panel.add(warningList, "wrap, spanx, growx"); // Reduced wrap gap

		warningList.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				warningList.clearSelection();
			}
		});

		return panel;
	}
}
