package net.sf.openrocket.gui.simulation;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.startup.Application;

public class SimulationPlotExportDialog extends JDialog {
	
	private final Window parentWindow;
	private final Simulation simulation;
	private final OpenRocketDocument document;
	private final SimulationOptions conditions;
	private final Configuration configuration;
	private static final Translator trans = Application.getTranslator();
	
	public SimulationPlotExportDialog(Window parent, OpenRocketDocument document, Simulation s) {
		//// Plot/Export simulation
		super(parent, trans.get("simedtdlg.title.Editsim"), JDialog.ModalityType.DOCUMENT_MODAL);
		this.document = document;
		this.parentWindow = parent;
		this.simulation = s;
		this.conditions = simulation.getOptions();
		configuration = simulation.getConfiguration();
		
		JPanel mainPanel = new JPanel(new MigLayout("fill", "[grow]"));
		
		//// Simulation name:
		mainPanel.add(new JLabel(trans.get("simedtdlg.lbl.Simname") + " "), "span, split 2, shrink");
		final JTextField field = new JTextField(simulation.getName());
		field.setEditable(false);
		mainPanel.add(field, "shrinky, growx, wrap");
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		//// Plot data
		final SimulationPlotPanel plotTab = plotTab();
		tabbedPane.addTab(trans.get("simedtdlg.tab.Plotdata"), plotTab);
		//// Export data
		final SimulationExportPanel exportTab = exportTab();
		tabbedPane.addTab(trans.get("simedtdlg.tab.Exportdata"), exportTab);
		
		mainPanel.add(tabbedPane, "grow, wrap");
		
		JButton ok = new JButton(trans.get("dlg.but.ok"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (tabbedPane.getSelectedIndex() == 0) {
					JDialog plot = plotTab.doPlot();
					if (plot != null) {
						SimulationPlotExportDialog.this.dispose();
						plot.setVisible(true);
					}
				} else {
					if (exportTab.doExport()) {
						SimulationPlotExportDialog.this.dispose();
					}
				}
			}
		});
		mainPanel.add(ok, "tag ok, split 2");
		
		//// Close button 
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationPlotExportDialog.this.dispose();
			}
		});
		mainPanel.add(close, "tag cancel");
		
		
		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, close);
		
	}
	
	
	/**
	 * A panel for plotting the previously calculated data.
	 */
	private SimulationPlotPanel plotTab() {
		
		// Check that data exists
		// FIXME -
		/*
		if (simulation.getSimulatedData() == null ||
				simulation.getSimulatedData().getBranchCount() == 0) {
			return noDataPanel();
		}
		*/
		return new SimulationPlotPanel(simulation);
	}
	
	/**
	 * A panel for exporting the data.
	 */
	private SimulationExportPanel exportTab() {
		FlightData data = simulation.getSimulatedData();
		
		// Check that data exists
		// FIXME - 
		/*
		if (data == null || data.getBranchCount() == 0 ||
				data.getBranch(0).getTypes().length == 0) {
			return noDataPanel();
		}
		*/
		return new SimulationExportPanel(simulation);
	}
	
	
	
}
