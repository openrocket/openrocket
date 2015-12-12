package net.sf.openrocket.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.startup.Application;

public class StorageOptionChooser extends JPanel {
	
	public static final double DEFAULT_SAVE_TIME_SKIP = 0.20;

	private final OpenRocketDocument document;
	
	private JRadioButton allButton;
	private JRadioButton someButton;
	private JRadioButton noneButton;
	
	private JSpinner timeSpinner;
	
	private JLabel estimateLabel;
	
	
	private boolean artificialEvent = false;
	private static final Translator trans = Application.getTranslator();
	
	public StorageOptionChooser(OpenRocketDocument doc, StorageOptions opts) {
		super(new MigLayout());
		
		this.document = doc;
		
		
		ChangeListener changeUpdater = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateEstimate();
			}
		};
		ActionListener actionUpdater = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateEstimate();
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
		
		//// Every
		someButton = new JRadioButton(trans.get("StorageOptChooser.rdbut.Every"));
		//// <html>Store plottable values approximately this far apart.<br>"
		//// Larger values result in smaller files.
		tip = trans.get("StorageOptChooser.lbl.longB1") +
		trans.get("StorageOptChooser.lbl.longB2");
		someButton.setToolTipText(tip);
		buttonGroup.add(someButton);
		someButton.addActionListener(actionUpdater);
		this.add(someButton, "");
		
		timeSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.1));
		timeSpinner.setToolTipText(tip);
		timeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (artificialEvent)
					return;
				someButton.setSelected(true);
			}
		});
		this.add(timeSpinner, "wmin 55lp");
		timeSpinner.addChangeListener(changeUpdater);
		
		//// seconds
		JLabel label = new JLabel(trans.get("StorageOptChooser.lbl.seconds"));
		label.setToolTipText(tip);
		this.add(label, "wrap rel");
		
		//// Only primary figures
		noneButton = new JRadioButton(trans.get("StorageOptChooser.rdbut.Onlyprimfig"));
		//// <html>Store only the values shown in the summary table.<br>
		//// This results in the smallest files.
		noneButton.setToolTipText(trans.get("StorageOptChooser.lbl.longC1") +
				trans.get("StorageOptChooser.lbl.longC2"));
		buttonGroup.add(noneButton);
		noneButton.addActionListener(actionUpdater);
		this.add(noneButton, "spanx, wrap 20lp");
		
		// Estimate is updated in loadOptions(opts)
		estimateLabel = new JLabel("");
		//// An estimate on how large the resulting file would
		//// be with the present options.
		estimateLabel.setToolTipText(trans.get("StorageOptChooser.lbl.longD1"));
		this.add(estimateLabel, "spanx");
		
		
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 10, 0, 0),
				//// Save options
				BorderFactory.createTitledBorder(trans.get("StorageOptChooser.ttip.Saveopt"))));
		
		loadOptions(opts);
	}
	
	
	public void loadOptions(StorageOptions opts) {
		double t;
		
		// Data storage radio button
		t = opts.getSimulationTimeSkip();
		if (t == StorageOptions.SIMULATION_DATA_ALL) {
			allButton.setSelected(true);
			t = DEFAULT_SAVE_TIME_SKIP;
		} else if (t == StorageOptions.SIMULATION_DATA_NONE) {
			noneButton.setSelected(true);
			t = DEFAULT_SAVE_TIME_SKIP;
		} else {
			someButton.setSelected(true);
		}
		
		// Time skip spinner
		artificialEvent = true;
		timeSpinner.setValue(t);
		artificialEvent = false;
		
		updateEstimate();
	}
	
	
	public void storeOptions(StorageOptions opts) {
		double t;
		
		if (allButton.isSelected()) {
			t = StorageOptions.SIMULATION_DATA_ALL;
		} else if (noneButton.isSelected()) {
			t = StorageOptions.SIMULATION_DATA_NONE;
		} else {
			t = (Double)timeSpinner.getValue();
		}
		
		opts.setSimulationTimeSkip(t);
		
		opts.setExplicitlySet(true);
	}
	
	
	
	// TODO: MEDIUM: The estimation method always uses OpenRocketSaver!
	private static final RocketSaver ROCKET_SAVER = new OpenRocketSaver();
	
	private void updateEstimate() {
		StorageOptions opts = new StorageOptions();
		
		storeOptions(opts);
		long size = ROCKET_SAVER.estimateFileSize(document, opts);
		size = Math.max((size+512)/1024, 1);

		String formatted;
		
		if (size >= 10000) {
			formatted = (size/1000) + " MB";
		} else if (size >= 1000){
			formatted = (size/1000) + "." + ((size/100)%10) + " MB";
		} else if (size >= 100) {
			formatted = ((size/10)*10) + " kB";
		} else {
			formatted = size + " kB";
		}

		//// Estimated file size:
		estimateLabel.setText(trans.get("StorageOptChooser.lbl.Estfilesize") + " " + formatted);
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
	
}
