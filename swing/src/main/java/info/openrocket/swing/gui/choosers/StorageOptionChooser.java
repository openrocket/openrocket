package info.openrocket.swing.gui.choosers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.startup.Application;

@SuppressWarnings("serial")
public class StorageOptionChooser extends JPanel implements OptionChooser {
	
	public static final double DEFAULT_SAVE_TIME_SKIP = 0.20;

	private final OpenRocketDocument document;
	
	private JRadioButton allButton;
	private JRadioButton someButton;
	private JRadioButton noneButton;
	
	private JSpinner timeSpinner;

	private JLabel infoLabel;
	
	
	private boolean artificialEvent = false;
	private static final Translator trans = Application.getTranslator();
	
	public StorageOptionChooser(OpenRocketDocument doc, StorageOptions opts) {
		super(new MigLayout());
		
		this.document = doc;
		

		ActionListener actionUpdater = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateInfoLabel();
			}
		};
		

		ButtonGroup buttonGroup = new ButtonGroup();
		String tip;
		
		//// Simulated data to store:
		this.add(new JLabel(trans.get("StorageOptChooser.lbl.Simdatatostore")), "spanx, wrap unrel");

		//// All simulated data
		allButton = new JRadioButton(trans.get("StorageOptChooser.rdbut.Allsimdata"));
		//// <html>Store all simulated data.<br>
		//// This can result in very large files!
		allButton.setToolTipText(trans.get("StorageOptChooser.lbl.longA1") +
				trans.get("StorageOptChooser.lbl.longA2"));
		buttonGroup.add(allButton);
		allButton.addActionListener(actionUpdater);
		this.add(allButton, "spanx, wrap rel");
				
		//// Only summary data
		noneButton = new JRadioButton(trans.get("StorageOptChooser.rdbut.Onlysummarydata"));
		//// <html>Store only the values shown in the summary table.<br>
		//// This results in the smallest files.
		noneButton.setToolTipText(trans.get("StorageOptChooser.lbl.longC1") +
				trans.get("StorageOptChooser.lbl.longC2"));
		buttonGroup.add(noneButton);
		noneButton.addActionListener(actionUpdater);
		this.add(noneButton, "spanx, wrap 20lp");
		
		// File size info label
		infoLabel = new JLabel("");
		infoLabel.setToolTipText(trans.get("StorageOptChooser.lbl.longD1"));
		this.add(infoLabel, "spanx");
		
		
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 10, 0, 0),
				//// Save options
				BorderFactory.createTitledBorder(trans.get("StorageOptChooser.ttip.Saveopt"))));
		
		loadOptions(opts);
	}
	
	
	public void loadOptions(StorageOptions opts) {
		
		// Data storage radio button
		if (opts.getSaveSimulationData()) {
			allButton.setSelected(true);
		} else {
			noneButton.setSelected(true);
		}
		
		updateInfoLabel();
	}
	
	
	public void storeOptions(StorageOptions opts) {
		opts.setSaveSimulationData(allButton.isSelected());
		opts.setExplicitlySet(true);
	}

	private void updateInfoLabel() {
		if (allButton.isSelected()) {
			infoLabel.setText(trans.get("StorageOptChooser.lbl.info1"));
		} else if (noneButton.isSelected()) {
			infoLabel.setText(trans.get("StorageOptChooser.lbl.info3"));
		} else {
			infoLabel.setText(trans.get("StorageOptChooser.lbl.info2"));
		}
	}
	
	/**
	 * Asks the user the storage options using a modal dialog window if the document
	 * contains simulated data and the user has not explicitly set how to store the data.
	 * 
	 * @param document	the document to check.
	 * @param parent	the parent frame for the dialog.
	 * @return			<code>true</code> to continue, <code>false</code> if the user cancelled.
	 */
	public static boolean verifyStorageOptions(OpenRocketDocument document, JFrame parent) {
		StorageOptions options = document.getDefaultStorageOptions();
		
		if (options.isExplicitlySet()) {
			// User has explicitly set the values, save as is
			return true;
		}
		
		
		boolean hasData = false;
		
		simulationLoop:
			for (Simulation s: document.getSimulations()) {
				if (s.getStatus() == Simulation.Status.NOT_SIMULATED ||
						s.getStatus() == Simulation.Status.EXTERNAL)
					continue;
				
				FlightData data = s.getSimulatedData();
				if (data == null)
					continue;
				
				for (int i=0; i < data.getBranchCount(); i++) {
					FlightDataBranch branch = data.getBranch(i);
					if (branch == null)
						continue;
					if (branch.getLength() > 0) {
						hasData = true;
						break simulationLoop;
					}
				}
			}
		

		if (!hasData) {
			// No data to store, do not ask only about compression
			return true;
		}
		
		
		StorageOptionChooser chooser = new StorageOptionChooser(document, options);

		//// Save options
		if (JOptionPane.showConfirmDialog(parent, chooser, trans.get("StorageOptChooser.lbl.Saveopt"), 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) !=
					JOptionPane.OK_OPTION) {
			// User cancelled
			return false;
		}
		
		chooser.storeOptions(options);
		return true;
	}

	@Override
	public void storeOptions(OpenRocketDocument document, ApplicationPreferences preferences) {
		this.storeOptions(document.getDefaultStorageOptions());
	}
}
