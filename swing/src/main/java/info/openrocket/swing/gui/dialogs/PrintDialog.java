/*
 * PrintPanel.java
 */
package info.openrocket.swing.gui.dialogs;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import info.openrocket.swing.gui.widgets.SaveFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.print.PrintController;
import info.openrocket.swing.gui.print.PrintSettings;
import info.openrocket.swing.gui.print.PrintableContext;
import info.openrocket.swing.gui.print.MonteCarloReportData;
import info.openrocket.swing.gui.print.MonteCarloReportData.Entry;
import info.openrocket.swing.gui.print.OpenRocketPrintable;
import info.openrocket.swing.gui.print.TemplateProperties;
import info.openrocket.swing.gui.print.components.CheckTreeManager;
import info.openrocket.swing.gui.print.components.RocketPrintTree;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.simulation.LandingDispersionAnalysisCache;

/**
 * This class isolates the Swing components used to create a panel that is added to a standard Java print dialog.
 */
public class PrintDialog extends JDialog implements TreeSelectionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(PrintDialog.class);
	private static final Translator trans = Application.getTranslator();
	
	private final Desktop desktop;
	
	private final RocketPrintTree stagedTree;
	private final RocketPrintTree noStagedTree;
	private OpenRocketDocument document;
	private RocketPrintTree currentTree;
	
	private JButton previewButton;
	private JButton saveAsPDF;
	private JButton cancel;

    private double rotation = 0.0d;
    
    private boolean updateSimulations = true;
	
	private final static SwingPreferences prefs = (SwingPreferences) Application.getPreferences();
	
	/**
	 * Constructor.
	 *
     * @param parent     the parent awt component
	 * @param orDocument the OR rocket container
     * @param theRotation the angle of rocket figure rotation
	 */
	public PrintDialog(Window parent, OpenRocketDocument orDocument, double theRotation) {
		super(parent, trans.get("title"), ModalityType.APPLICATION_MODAL);
		

		JPanel panel = new JPanel(new MigLayout("fill, gap rel unrel"));
		this.add(panel);
		rotation = theRotation;

		// before any Desktop APIs are used, first check whether the API is
		// supported by this particular VM on this particular host
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		} else {
			desktop = null;
		}
		
		document = orDocument;
		Rocket rocket = orDocument.getRocket();
		
		noStagedTree = RocketPrintTree.create(rocket.getName());
		noStagedTree.setShowsRootHandles(false);
		CheckTreeManager ctm = new info.openrocket.swing.gui.print.components.CheckTreeManager(noStagedTree);
		ctm.addTreeSelectionListener(this);
		
		final int stages = rocket.getStageCount();
		

		JLabel label = new JLabel(trans.get("lbl.selectElements"));
		panel.add(label, "wrap unrel");
		
		// Create the tree
		if (stages > 1) {
			stagedTree = RocketPrintTree.create(rocket.getName(), rocket.getChildren());
			ctm = new CheckTreeManager(stagedTree);
			stagedTree.setShowsRootHandles(false);
			ctm.addTreeSelectionListener(this);
		} else {
			stagedTree = noStagedTree;
		}
		currentTree = stagedTree;
		
		// Add the tree to the UI
		final JScrollPane scrollPane = new JScrollPane(stagedTree);
		panel.add(scrollPane, "width 400lp, height 200lp, grow, wrap para");

		// Checkboxes and buttons
		final JPanel optionsPanel = new JPanel(new MigLayout());
		
		final JCheckBox updateSimulationsCheckbox = new JCheckBox(trans.get("checkbox.updateSimulations"));
		updateSimulationsCheckbox.setEnabled(true);
		updateSimulationsCheckbox.setSelected(this.updateSimulations);
		updateSimulationsCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSimulations = updateSimulationsCheckbox.isSelected();
			}
		});
		optionsPanel.add(updateSimulationsCheckbox, "pad 0, grow, wrap");
		
		final JCheckBox sortByStage = new JCheckBox(trans.get("checkbox.showByStage"));
		sortByStage.setEnabled(stages > 1);
		sortByStage.setSelected(stages > 1);
		sortByStage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sortByStage.isEnabled()) {
					if (((JCheckBox) e.getSource()).isSelected()) {
						scrollPane.setViewportView(stagedTree);
						stagedTree.setExpandsSelectedPaths(true);
						currentTree = stagedTree;
					}
					else {
						scrollPane.setViewportView(noStagedTree);
						noStagedTree.setExpandsSelectedPaths(true);
						currentTree = noStagedTree;
					}
				}
			}
		});
		optionsPanel.add(sortByStage);
		panel.add(optionsPanel, "pad 0, aligny top, split");
		
		panel.add(new JPanel(), "pad 0, aligny top, growx");
		

		JButton settingsButton = new JButton(trans.get("printdlg.but.settings"));
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintSettings settings = getPrintSettings();
				log.debug("settings=" + settings);
				PrintSettingsDialog settingsDialog = new PrintSettingsDialog(PrintDialog.this, settings);
				settingsDialog.setVisible(true);
				setPrintSettings(settings);
			}
		});
		panel.add(settingsButton, "aligny top, wrap para");
				

		previewButton = new JButton(trans.get("but.previewAndPrint"));
		previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onPreview()) {
					PrintDialog.this.setVisible(false);
				}
			}
		});
		panel.add(previewButton, "split, right, gap para");
		

		saveAsPDF = new JButton(trans.get("printdlg.but.saveaspdf"));
		saveAsPDF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onSavePDF()) {
					PrintDialog.this.setVisible(false);
				}
			}
		});
		panel.add(saveAsPDF, "right, gap para");
		

		cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintDialog.this.setVisible(false);
			}
		});
		panel.add(cancel, "right, gap para");
		

		expandAll(currentTree, true);
		if (currentTree != noStagedTree) {
			expandAll(noStagedTree, true);
		}
		

		GUIUtil.setDisposableDialogOptions(this, previewButton);
		
	}
	
	
	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		final TreePath path = e.getNewLeadSelectionPath();
		if (path != null) {
			previewButton.setEnabled(true);
			saveAsPDF.setEnabled(true);
		} else {
			previewButton.setEnabled(false);
			saveAsPDF.setEnabled(false);
		}
	}

	/**
	 * If expand is true, expands all nodes in the tree. Otherwise, collapses all nodes in the theTree.
	 *
	 * @param theTree   the tree to expand/contract
	 * @param expand expand if true, contract if not
	 */
	public void expandAll(RocketPrintTree theTree, boolean expand) {
		TreeNode root = (TreeNode) theTree.getModel().getRoot();
		// Traverse theTree from root
		expandAll(theTree, new TreePath(root), expand);
	}
	
	/**
	 * Recursively walk a tree, and if expand is true, expands all nodes in the tree. Otherwise, collapses all nodes in
	 * the theTree.
	 *
	 * @param theTree   the tree to expand/contract
	 * @param parent the node to iterate/recurse over
	 * @param expand expand if true, contract if not
	 */
	private void expandAll(RocketPrintTree theTree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(theTree, path, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			theTree.expandPath(parent);
		} else {
			theTree.collapsePath(parent);
		}
	}
	
	

	/**
	 * Generate a report using a temporary file.  The file will be deleted upon JVM exit.
	 *
     * @param settings  the container of different print settings
	 *
	 * @return a file, populated with the "printed" output (the rocket info)
	 *
	 * @throws IOException thrown if the file could not be generated
	 */
	private File generateReport(PrintSettings settings, PreparedReport report) throws IOException {
		final File f = File.createTempFile("openrocket-", ".pdf");
		f.deleteOnExit();
		return generateReport(f, settings, report);
	}
	
	/**
	 * Generate a report to a specified file.
	 *
	 * @param f     the file to which rocket data will be written
	 * @param settings  the container of different print settings
	 *
	 * @return a file, populated with the "printed" output (the rocket info)
	 *
	 * @throws IOException thrown if the file could not be generated
	 */
	private File generateReport(File f, PrintSettings settings, PreparedReport report) throws IOException {
		PrintController controller = new PrintController();
		controller.setWindow(this.getOwner());
		controller.setMonteCarloReportData(report.monteCarloData());
		controller.print(document, report.printables().iterator(), new FileOutputStream(f),
		                 settings, rotation, updateSimulations);
		return f;
	}

	private PreparedReport prepareReport() {
		List<PrintableContext> toBePrinted = new ArrayList<>();
		currentTree.getToBePrinted().forEachRemaining(toBePrinted::add);
		MonteCarloReportData monteCarloData = prepareMonteCarloReport(toBePrinted);
		if (monteCarloData == null) {
			return null;
		}
		return new PreparedReport(toBePrinted, monteCarloData);
	}

	private MonteCarloReportData prepareMonteCarloReport(List<PrintableContext> printables) {
		boolean reportSelected = printables.stream()
				.anyMatch(context -> context.getPrintable() == OpenRocketPrintable.MONTE_CARLO_REPORT);
		if (!reportSelected) {
			return MonteCarloReportData.EMPTY;
		}

		MonteCarloReportOptionsPanel optionsPanel = new MonteCarloReportOptionsPanel(getOwner(), document);
		if (!optionsPanel.showDialog(this)) {
			return null;
		}
		List<Simulation> selected = optionsPanel.getSelectedSimulations();
		if (selected.isEmpty()) {
			JOptionPane.showMessageDialog(this, trans.get("printdlg.monteCarlo.noneSelected"),
					trans.get("printdlg.monteCarlo.noneSelected.title"), JOptionPane.WARNING_MESSAGE);
			return null;
		}

		List<Simulation> missing = selected.stream()
				.filter(simulation -> LandingDispersionAnalysisCache.get(simulation,
						simulation.getLandingDispersionSettings()) == null)
				.toList();
		boolean skipMissing = false;
		if (!missing.isEmpty()) {
			int decision = showMissingResultsPrompt(missing);
			if (decision == 2 || decision == JOptionPane.CLOSED_OPTION) {
				return null;
			}
			skipMissing = decision == 1;
			if (!skipMissing && !MonteCarloReportRunDialog.run(this, missing)) {
				return null;
			}
		}

		List<Entry> entries = new ArrayList<>();
		List<String> omitted = new ArrayList<>();
		for (Simulation simulation : selected) {
			var result = LandingDispersionAnalysisCache.get(simulation,
					simulation.getLandingDispersionSettings());
			boolean wasMissing = missing.stream().anyMatch(candidate -> candidate == simulation);
			if (result == null || skipMissing && wasMissing) {
				omitted.add(simulation.getName());
			} else {
				entries.add(new Entry(simulation, result));
			}
		}
		return new MonteCarloReportData(entries, omitted);
	}

	private int showMissingResultsPrompt(List<Simulation> missing) {
		int runCount = missing.stream()
				.map(Simulation::getLandingDispersionSettings)
				.mapToInt(settings -> settings.getRunCount())
				.sum();
		String names = missing.stream().map(Simulation::getName)
				.collect(java.util.stream.Collectors.joining("\n• ", "• ", ""));
		String message = String.format(trans.get("printdlg.monteCarlo.missing"), names, runCount);
		Object[] options = {
				trans.get("printdlg.monteCarlo.missing.run"),
				trans.get("printdlg.monteCarlo.missing.skip"),
				trans.get("button.cancel")
		};
		return JOptionPane.showOptionDialog(this, message,
				trans.get("printdlg.monteCarlo.missing.title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}
	
	
	/**
	 * Handler for when the Preview button is clicked.
	 */
	private boolean onPreview() {
		if (desktop != null) {
			try {
				PreparedReport report = prepareReport();
				if (report == null) {
					return false;
				}
				PrintSettings settings = getPrintSettings();
				// TODO: HIGH: Remove UIManager, and pass settings to the actual printing methods
				TemplateProperties.setColors(settings);
				File f = generateReport(settings, report);
				openPreviewHelper(f);
				return true;
			} catch (IOException e) {
				log.error("Could not open preview.", e);
				JOptionPane.showMessageDialog(this, new String[] {
						trans.get("error.preview.desc1"),
						trans.get("error.preview.desc2") },
						trans.get("error.preview.title"),
						JOptionPane.ERROR_MESSAGE);
			return false;
			}
		} else {
			JOptionPane.showMessageDialog(this, new String[] {
					trans.get("error.preview.desc1"),
					trans.get("error.preview.desc2") },
					trans.get("error.preview.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
	}
	
	private void openPreviewHelper(final File f) throws IOException {
		if (SystemInfo.getPlatform() == Platform.UNIX && SystemInfo.isConfined()) {
			/* When installed via a snap package on Linux, the default option
			 * to open PDF options using java.awt.Desktop.open() doesn't work
			 * due to using . Instead, use the xdg-open command
			 * which will work for URLs.
			 */
			String command = "xdg-open " + f.getAbsolutePath();
			Runtime.getRuntime().exec(command);
		} else {
			desktop.open(f);
		}
	}
	
	/**
	 * Handler for when the "Save as PDF" button is clicked.
	 *
	 * @return	true if the PDF was saved
	 */
	private boolean onSavePDF() {
		PreparedReport report = prepareReport();
		if (report == null) {
			return false;
		}
		
		JFileChooser chooser = new SaveFileChooser();
		chooser.setFileFilter(FileHelper.PDF_FILTER);
		
		// Select initial directory
		File dir = document.getFile();
		if (dir != null) {
			dir = dir.getParentFile();
		}
		if (dir == null) {
			dir = prefs.getDefaultDirectory();
		}
		chooser.setCurrentDirectory(dir);
		
		int returnVal = chooser.showSaveDialog(this);
		File file = chooser.getSelectedFile();
		if (returnVal == JFileChooser.APPROVE_OPTION && file != null) {
			
			file = FileHelper.forceExtension(file, "pdf");
			if (!FileHelper.confirmWrite(file, this)) {
				return false;
			}
			
			try {
				
				PrintSettings settings = getPrintSettings();
				// TODO: HIGH: Remove UIManager, and pass settings to the actual printing methods
				TemplateProperties.setColors(settings);
				generateReport(file, settings, report);
				
			} catch (IOException e) {
				FileHelper.errorWriting(e, this);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	private record PreparedReport(List<PrintableContext> printables, MonteCarloReportData monteCarloData) {
	}
	
	public PrintSettings getPrintSettings() {
		PrintSettings settings = new PrintSettings();
		Color c;
		
		c = prefs.getColor("print.template.fillColor", (java.awt.Color) null);
		if (c != null) {
			settings.setTemplateFillColor(c);
		}
		
		c = prefs.getColor("print.template.borderColor", (java.awt.Color) null);
		if (c != null) {
			settings.setTemplateBorderColor(c);
		}
		
		settings.setPaperSize(prefs.getEnum("print.paper.size", settings.getPaperSize()));
		settings.setPaperOrientation(prefs.getEnum("print.paper.orientation", settings.getPaperOrientation()));
		
		return settings;
	}
	
	public void setPrintSettings(PrintSettings settings) {
		prefs.putColor("print.template.fillColor", settings.getTemplateFillColor() );
		prefs.putColor("print.template.borderColor", settings.getTemplateBorderColor() );
		prefs.putEnum("print.paper.size", settings.getPaperSize());
		prefs.putEnum("print.paper.orientation", settings.getPaperOrientation());
	}

}
