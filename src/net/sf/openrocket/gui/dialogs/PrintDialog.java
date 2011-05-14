/*
 * PrintPanel.java
 */
package net.sf.openrocket.gui.dialogs;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

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
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.gui.print.PrintController;
import net.sf.openrocket.gui.print.PrintSettings;
import net.sf.openrocket.gui.print.PrintableContext;
import net.sf.openrocket.gui.print.TemplateProperties;
import net.sf.openrocket.gui.print.components.CheckTreeManager;
import net.sf.openrocket.gui.print.components.RocketPrintTree;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

/**
 * This class isolates the Swing components used to create a panel that is added to a standard Java print dialog.
 */
public class PrintDialog extends JDialog implements TreeSelectionListener {
	
	private static final LogHelper log = Application.getLogger();
	
	private static final String SETTINGS_BUTTON_TEXT = "Settings";
	private static final String PREVIEW_BUTTON_TEXT = "Preview & Print";
	private static final String SAVE_AS_PDF_BUTTON_TEXT = "Save as PDF";
	private static final String SHOW_BY_STAGE = "Show By Stage";
	
	private final RocketPrintTree stagedTree;
	private final RocketPrintTree noStagedTree;
	private OpenRocketDocument document;
	private RocketPrintTree currentTree;
	private Desktop desktop = null;
	
	private JButton previewButton;
	private JButton saveAsPDF;
	private JButton cancel;
	
	/**
	 * Constructor.
	 *
	 * @param orDocument the OR rocket container
	 */
	public PrintDialog(Window parent, OpenRocketDocument orDocument) {
		super(parent, "Print or export", ModalityType.APPLICATION_MODAL);
		

		JPanel panel = new JPanel(new MigLayout("fill, gap rel unrel"));
		this.add(panel);
		

		// before any Desktop APIs are used, first check whether the API is
		// supported by this particular VM on this particular host
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}
		
		document = orDocument;
		Rocket rocket = orDocument.getRocket();
		
		noStagedTree = RocketPrintTree.create(rocket.getName());
		noStagedTree.setShowsRootHandles(false);
		CheckTreeManager ctm = new net.sf.openrocket.gui.print.components.CheckTreeManager(noStagedTree);
		ctm.addTreeSelectionListener(this);
		
		final int stages = rocket.getStageCount();
		

		JLabel label = new JLabel("Select elements to include:");
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
		final JCheckBox sortByStage = new JCheckBox(SHOW_BY_STAGE);
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
		panel.add(sortByStage, "aligny top, split");
		

		panel.add(new JPanel(), "growx");
		

		JButton settingsButton = new JButton(SETTINGS_BUTTON_TEXT);
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintSettings settings = Prefs.getPrintSettings();
				log.debug("settings=" + settings);
				PrintSettingsDialog settingsDialog = new PrintSettingsDialog(PrintDialog.this, settings);
				settingsDialog.setVisible(true);
				Prefs.setPrintSettings(settings);
			}
		});
		panel.add(settingsButton, "wrap para");
		

		previewButton = new JButton(PREVIEW_BUTTON_TEXT);
		previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPreview();
				PrintDialog.this.setVisible(false);
			}
		});
		panel.add(previewButton, "split, right, gap para");
		

		saveAsPDF = new JButton(SAVE_AS_PDF_BUTTON_TEXT);
		saveAsPDF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onSavePDF()) {
					PrintDialog.this.setVisible(false);
				}
			}
		});
		panel.add(saveAsPDF, "right, gap para");
		

		cancel = new JButton("Cancel");
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
		theTree.addSelectionPath(parent);
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
	 * @param paper the name of the paper size
	 *
	 * @return a file, populated with the "printed" output (the rocket info)
	 *
	 * @throws IOException thrown if the file could not be generated
	 */
	private File generateReport(PrintSettings settings) throws IOException {
		final File f = File.createTempFile("openrocket-", ".pdf");
		f.deleteOnExit();
		return generateReport(f, settings);
	}
	
	/**
	 * Generate a report to a specified file.
	 *
	 * @param f     the file to which rocket data will be written
	 * @param paper the name of the paper size
	 *
	 * @return a file, populated with the "printed" output (the rocket info)
	 *
	 * @throws IOException thrown if the file could not be generated
	 */
	private File generateReport(File f, PrintSettings settings) throws IOException {
		Iterator<PrintableContext> toBePrinted = currentTree.getToBePrinted();
		new PrintController().print(document, toBePrinted, new FileOutputStream(f), settings);
		return f;
	}
	
	
	/**
	 * Handler for when the Preview button is clicked.
	 */
	private void onPreview() {
		if (desktop != null) {
			try {
				PrintSettings settings = Prefs.getPrintSettings();
				// TODO: HIGH: Remove UIManager, and pass settings to the actual printing methods
				TemplateProperties.setColors(settings);
				File f = generateReport(settings);
				desktop.open(f);
			} catch (IOException e) {
				log.error("Could not create temporary file for previewing.", e);
				JOptionPane.showMessageDialog(this, "Could not create a temporary file for previewing.",
												"Error creating file", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this,
											"Your environment does not support automatically opening the default PDF viewer.",
											"Error creating file", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Handler for when the "Save as PDF" button is clicked.
	 *
	 * @return	true if the PDF was saved
	 */
	private boolean onSavePDF() {
		
		JFileChooser chooser = new JFileChooser();
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		FileFilter filter = new FileFilter() {
			
			//Accept all directories and all pdf files.
			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".pdf");
			}
			
			//The description of this filter
			@Override
			public String getDescription() {
				return "PDF files";
			}
		};
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			try {
				String fname = chooser.getSelectedFile().getCanonicalPath();
				if (!getExtension(fname).equals("pdf")) {
					fname = fname + ".pdf";
				}
				File f = new File(fname);
				PrintSettings settings = Prefs.getPrintSettings();
				// TODO: HIGH: Remove UIManager, and pass settings to the actual printing methods
				TemplateProperties.setColors(settings);
				generateReport(f, settings);
			} catch (IOException e) {
				ExceptionHandler.handleErrorCondition(e);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the extension of a file.
	 */
	private static String getExtension(String s) {
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext != null ? ext : "";
	}
}
