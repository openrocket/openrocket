package net.sf.openrocket.gui.simulation;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.io.Serial;

public class SimulationSettingsPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = -2114129713185477998L;

	private static final Translator trans = Application.getTranslator();

	public SimulationSettingsPanel(final OpenRocketDocument document, final Simulation simulation) {
		super(new MigLayout("fill"));

		JTabbedPane tabbedPane = new JTabbedPane();

		//// Launch conditions
		tabbedPane.addTab(trans.get("simedtdlg.tab.Launchcond"), new SimulationConditionsPanel(simulation));

		//// Simulation options
		tabbedPane.addTab(trans.get("simedtdlg.tab.Simopt"), new SimulationOptionsPanel(document, simulation));

		tabbedPane.setSelectedIndex(0);

		this.add(tabbedPane, "spanx, grow, wrap");
	}


}
