package net.sf.openrocket.gui.simulation;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.DetailDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

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
