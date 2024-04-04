package info.openrocket.swing.gui.simulation;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

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
