package info.openrocket.swing.gui.simulation;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.dialogs.DetailDialog;

public class SimulationWarningDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	public static void showWarningDialog(Component parent, Simulation simulation) {
		
		if (simulation.getSimulatedWarnings() != null && simulation.getSimulatedWarnings().size() > 0) {
			ArrayList<String> messages = new ArrayList<String>();
			messages.add(trans.get("SimuRunDlg.msg.errorOccurred"));
			for (Warning m : simulation.getSimulatedWarnings()) {
				messages.add(m.toString());
			}
			DetailDialog.showDetailedMessageDialog(parent,
					messages.toArray(),
					null, simulation.getName(), JOptionPane.ERROR_MESSAGE);
		}
		
	}
}
