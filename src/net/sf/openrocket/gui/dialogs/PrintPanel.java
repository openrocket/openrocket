/*
 * PrintPanel.java
 */
package net.sf.openrocket.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.components.ColorChooser;
import net.sf.openrocket.gui.print.PrintController;
import net.sf.openrocket.gui.print.PrintUtilities;
import net.sf.openrocket.gui.print.PrintableContext;
import net.sf.openrocket.gui.print.TemplateProperties;
import net.sf.openrocket.gui.print.components.CheckTreeManager;
import net.sf.openrocket.gui.print.components.RocketPrintTree;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class isolates the Swing components used to create a panel that is added to the standard Java print dialog.
 */
public class PrintPanel extends JPanel implements TreeSelectionListener {

    private static final LogHelper log = Application.getLogger();

    private final RocketPrintTree stagedTree;
    private final RocketPrintTree noStagedTree;
    private OpenRocketDocument rocDoc;
    private RocketPrintTree currentTree;
    private boolean bDesktopSupported = false;
    private Desktop desktop;
    private PrintDialog printDialog;

    JButton previewButton;
    JButton saveAsPDF;
    
    /**
     * Constructor.
     *
     * @param orDocument the OR rocket container
     * @param theParent  the OR parent print dialog
     */
    public PrintPanel (OpenRocketDocument orDocument, PrintDialog theParent) {

        super(new MigLayout("fill, gap rel unrel"));

        // before any Desktop APIs are used, first check whether the API is
        // supported by this particular VM on this particular host
        if (Desktop.isDesktopSupported()) {
            bDesktopSupported = true;
            desktop = Desktop.getDesktop();
        }

        printDialog = theParent;
        rocDoc = orDocument;
        Rocket rocket = orDocument.getRocket();

        noStagedTree = RocketPrintTree.create(rocket.getName());
        noStagedTree.setShowsRootHandles(false);
        CheckTreeManager ctm = new net.sf.openrocket.gui.print.components.CheckTreeManager(noStagedTree);
        ctm.addTreeSelectionListener(this);

        final int stages = rocket.getStageCount();

        if (stages > 1) {
            stagedTree = RocketPrintTree.create(rocket.getName(), rocket.getChildren());
            ctm = new CheckTreeManager(stagedTree);
            stagedTree.setShowsRootHandles(false);
            ctm.addTreeSelectionListener(this);
        }
        else {
            stagedTree = noStagedTree;
        }
        currentTree = stagedTree;

        final JScrollPane scrollPane = new JScrollPane(stagedTree);
        add(scrollPane, "width 475!, wrap");

        final JCheckBox sortByStage = new JCheckBox("Show By Stage");
        sortByStage.setEnabled(stages > 1);
        sortByStage.setSelected(stages > 1);
        sortByStage.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
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
        add(sortByStage, "wrap");

        saveAsPDF = new JButton("Save as PDF");
        saveAsPDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                onSavePDF(PrintPanel.this);
            }
        });
        add(saveAsPDF, "span 2, tag save");

        previewButton = new JButton("Preview");
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                onPreview();
            }
        });
        add(previewButton, "x 150");

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                PrintSettingsDialog settingsDialog = new PrintSettingsDialog(printDialog.getDialog());
                settingsDialog.setVisible(true);
            }
        });
        add(settingsButton, "x 400");

        expandAll(currentTree, true);
        if (currentTree != noStagedTree) {
            expandAll(noStagedTree, true);
        }
        setVisible(true);
    }

    /**
     * The title of the tab that gets displayed for this panel, when placed in the print dialog.
     *
     * @return a title
     */
    public String getTitle () {
        return "Rocket";
    }

    @Override 
    public void valueChanged (final TreeSelectionEvent e) {
        final TreePath path = e.getNewLeadSelectionPath();
        if (path != null){
            previewButton.setEnabled(true);
            saveAsPDF.setEnabled(true);
        }
        else {
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
    public void expandAll (RocketPrintTree theTree, boolean expand) {
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
    private void expandAll (RocketPrintTree theTree, TreePath parent, boolean expand) {
        theTree.addSelectionPath(parent);
        // Traverse children 
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(theTree, path, expand);
            }
        }
        // Expansion or collapse must be done bottom-up 
        if (expand) {
            theTree.expandPath(parent);
        }
        else {
            theTree.collapsePath(parent);
        }
    }

    /**
     * Get a media size name (the name of a paper size).  If no page size is selected, it will default to the locale
     * specific page size (LETTER in North America, A4 elsewhere).
     *
     * @return the name of a page size
     */
    private MediaSizeName getMediaSize () {
        MediaSizeName paperSize = getMediaSize(printDialog.getAttributes());
        if (paperSize == null) {
            paperSize = PrintUtilities.getDefaultMedia().getMediaSizeName();
        }
        return paperSize;
    }

    /**
     * Get the media size name (the name of a paper size) as selected by the user.
     *
     * @param atts the set of selected printer attributes
     *
     * @return a media size name, may be null
     */
    private MediaSizeName getMediaSize (PrintRequestAttributeSet atts) {
        return (MediaSizeName) atts.get(javax.print.attribute.standard.Media.class);
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
    private File generateReport (MediaSizeName paper) throws IOException {
        final File f = File.createTempFile("oro", ".pdf");
        f.deleteOnExit();
        return generateReport(f, paper);
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
    private File generateReport (File f, MediaSizeName paper) throws IOException {
        Iterator<PrintableContext> toBePrinted = currentTree.getToBePrinted();
        new PrintController().print(rocDoc, toBePrinted, new FileOutputStream(f), paper);
        return f;
    }

    /**
     * Generate a report to a byte array output stream.
     *
     * @return a stream populated with the "printed" output (the rocket info)
     */
    public ByteArrayOutputStream generateReport() {
        Iterator<PrintableContext> toBePrinted = currentTree.getToBePrinted();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new PrintController().print(rocDoc, toBePrinted, baos, getMediaSize ());
        return baos;
    }

    /**
     * Handler for when the Preview button is clicked.
     */
    private void onPreview () {
        if (bDesktopSupported) {
            try {
                MediaSizeName paperSize = getMediaSize();
                File f = generateReport(paperSize);
                desktop.open(f);
            }
            catch (IOException e) {
                log.error("Could not create temporary file for previewing.", e);
                JOptionPane.showMessageDialog(this, "Could not create a temporary file for previewing.",
                                              "Error creating file", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(this,
                                          "Your environment does not support automatically opening the default PDF viewer.",
                                          "Error creating file", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Handler for when the "Save as PDF" button is clicked.
     *
     * @param p the component to parent the save dialog to
     */
    private void onSavePDF (JComponent p) {

        JFileChooser chooser = new JFileChooser();
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
        FileFilter filter = new FileFilter() {

            //Accept all directories and all pdf files.
            public boolean accept (File f) {
                return true;
            }

            //The description of this filter
            public String getDescription () {
                return "pdf";
            }
        };
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(p);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {
                String fname = chooser.getSelectedFile().getCanonicalPath();
                if (!getExtension(fname).equals("pdf")) {
                    fname = fname + ".pdf";
                }
                File f = new File(fname);
                generateReport(f, getMediaSize());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the extension of a file.
     */
    private static String getExtension (String s) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext != null ? ext : "";
    }
}

/**
 * This class is a dialog for displaying advanced settings for printing rocket related info.
 */
class PrintSettingsDialog extends JDialog {

    /**
     * The fill color chooser.
     */
    private ColorChooser fill;

    /**
     * The line color chooser.
     */
    private ColorChooser line;

    /**
     * Construct a dialog for setting the advanced rocket print settings.
     *
     * @param parent the owning dialog
     */
    public PrintSettingsDialog (JDialog parent) {
        super(parent, "Advanced Settings", true);
        setLayout(new MigLayout("fill"));

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new MigLayout("gap rel"));

        fill = addColorChooser(settingsPanel, "Template Fill", TemplateProperties.getFillColor());
        line = addColorChooser(settingsPanel, "Template Line", TemplateProperties.getLineColor());

        settingsPanel.add(fill);
        settingsPanel.add(line);

        add(settingsPanel, "wrap");

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                UIManager.put(TemplateProperties.TEMPLATE_FILL_COLOR_PROPERTY, fill.getCurrentColor());
                UIManager.put(TemplateProperties.TEMPLATE_LINE_COLOR_PROPERTY, line.getCurrentColor());
                dispose();
            }
        });
        add(closeButton, "right, gapright para");

        setSize(400, 200);
    }

    /**
     * Add a color chooser to a panel.
     *
     * @param panel        the parent panel to add the color chooser.
     * @param label        the label that indicates which color property is being changed
     * @param initialColor the initial, or current, color to display
     *
     * @return a swing component containing a label, a colorized field, and a button that when clicked opens a color
     *         chooser dialog
     */
    private ColorChooser addColorChooser (JPanel panel, String label, Color initialColor) {
        final JColorChooser colorChooser = new JColorChooser(initialColor);
        return new ColorChooser(panel, colorChooser, label);
    }

}
