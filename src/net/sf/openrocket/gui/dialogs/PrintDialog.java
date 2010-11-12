/*
 * PrintDialog.java
 */
package net.sf.openrocket.gui.dialogs;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.print.PDFPrintStreamDoc;
import net.sf.openrocket.gui.print.PrintUtilities;
import sun.print.ServiceDialog;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Fidelity;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;

/**
 * This class is not a dialog by inheritance, but is by delegation.  It front-ends a java print dialog by
 * augmenting it with application specific (rocket) settings.
 */
public class PrintDialog {

    /**
     * The service UI dialog.
     */
    private ServiceDialog dialog;

    /**
     * A javax doc flavor specific for printing PDF documents.
     */
    private static final DocFlavor.INPUT_STREAM PDF = DocFlavor.INPUT_STREAM.PDF;

    /**
     * Construct a print dialog using an Open Rocket document - which contains the rocket data to ultimately be
     * printed.
     *
     * @param orDocument the rocket container
     */
    public PrintDialog (OpenRocketDocument orDocument) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(PrintUtilities.getDefaultMedia().getMediaSizeName());

        final PrintPanel panel = new PrintPanel(orDocument, this);
        PrintService ps = printDialog(null, 100, 100, services, svc, PDF, attrs, panel);
        if (ps != null) {
            DocPrintJob dpj = ps.createPrintJob();
            try {
                System.err.println(attrs.size());
                ByteArrayOutputStream baos = panel.generateReport();
                dpj.print(new PDFPrintStreamDoc(baos, null), attrs);
            }
            catch (PrintException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * Get the set of attributes from the service ui print dialog.
     *
     * @return a set of print attributes
     */
    PrintRequestAttributeSet getAttributes () {
        return dialog.getAttributes();
    }

    /**
     * Get the service ui dialog.  This is the actual dialog that gets displayed - we co-opt it for adding a rocket
     * specific tab.
     *
     * @return the Java service ui print dialog
     */
    JDialog getDialog () {
        return dialog;
    }

    /**
     * Mimics the ServiceUI.printDialog method, but with enhancements for our own print settings tab.
     *
     * @param gc             used to select screen. null means primary or default screen.
     * @param x              location of dialog including border in screen coordinates
     * @param y              location of dialog including border in screen coordinates
     * @param services       to be browsable, must be non-null.
     * @param defaultService - initial PrintService to display.
     * @param flavor         - the flavor to be printed, or null.
     * @param attributes     on input is the initial application supplied preferences. This cannot be null but may be
     *                       empty. On output the attributes reflect changes made by the user.
     * @param addnl          a panel to be added, as a tab, to the internal tabbed pane of the resulting print dialog
     *
     * @return print service selected by the user, or null if the user cancelled the dialog.
     *
     * @throws HeadlessException        if GraphicsEnvironment.isHeadless() returns true.
     * @throws IllegalArgumentException if services is null or empty, or attributes is null, or the initial PrintService
     *                                  is not in the list of browsable services.
     */
    private PrintService printDialog (GraphicsConfiguration gc,
                                      int x, int y,
                                      PrintService[] services,
                                      PrintService defaultService,
                                      DocFlavor flavor,
                                      PrintRequestAttributeSet attributes,
                                      PrintPanel addnl)
            throws HeadlessException {
        int defaultIndex = -1;

        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        else if ((services == null) || (services.length == 0)) {
            throw new IllegalArgumentException("services must be non-null " +
                                               "and non-empty");
        }
        else if (attributes == null) {
            throw new IllegalArgumentException("attributes must be non-null");
        }

        if (defaultService != null) {
            for (int i = 0; i < services.length; i++) {
                if (services[i].equals(defaultService)) {
                    defaultIndex = i;
                    break;
                }
            }

            if (defaultIndex < 0) {
                throw new IllegalArgumentException("services must contain " +
                                                   "defaultService");
            }
        }
        else {
            defaultIndex = 0;
        }

        Rectangle gcBounds = (gc == null) ? GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration().getBounds() : gc.getBounds();

        dialog = new ServiceDialog(gc,
                                   x + gcBounds.x,
                                   y + gcBounds.y,
                                   services, defaultIndex,
                                   flavor, attributes,
                                   (Dialog) null);
        Rectangle dlgBounds = dialog.getBounds();

        // get union of all GC bounds
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice g : gs) {
            gcBounds = gcBounds.union(g.getDefaultConfiguration().getBounds());
        }

        // if portion of dialog is not within the gc boundary
        if (!gcBounds.contains(dlgBounds)) {
            // put in the center relative to parent frame/dialog
            dialog.setLocationRelativeTo(null);
        }
        if (addnl != null && addnl.getTitle() != null) {
            JTabbedPane tp = (JTabbedPane) getDescendantOfClass(JTabbedPane.class, dialog);
            tp.add(addnl, addnl.getTitle(), 0);
            tp.setSelectedIndex(0);
        }

        dialog.setVisible(true);

        if (dialog.getStatus() == ServiceDialog.APPROVE) {
            PrintRequestAttributeSet newas = dialog.getAttributes();
            Class dstCategory = Destination.class;
            Class fdCategory = Fidelity.class;

            if (attributes.containsKey(dstCategory) &&
                !newas.containsKey(dstCategory)) {
                attributes.remove(dstCategory);
            }

            attributes.addAll(newas);

            Fidelity fd = (Fidelity) attributes.get(fdCategory);
            if (fd != null) {
                if (fd == Fidelity.FIDELITY_TRUE) {
                    removeUnsupportedAttributes(dialog.getPrintService(),
                                                flavor, attributes);
                }
            }
            return dialog.getPrintService();
        }
        else {
            return null;
        }
    }

    private Component getDescendantOfClass (Class c, Container cont) {
        if (c == null || cont == null) {
            return null;
        }
        Component[] children = (cont instanceof JMenu)
                               ? ((JMenu) cont).getMenuComponents()
                               : cont.getComponents();
        for (int i = 0, n = children.length; i < n; i++) {
            Component comp = children[i];
            if (c.isInstance(comp)) {
                return comp;
            }
            comp = getDescendantOfClass(c, (Container) comp);
            if (comp != null) {
                return comp;
            }
        }
        return null;
    }

    /**
     * Removes any attributes from the given AttributeSet that are unsupported by the given PrintService/DocFlavor
     * combination.
     * 
     * @param ps      the print service for which unsupported attributes will be determined
     * @param flavor  the document flavor; PDF in our case
     * @param aset    the set of attributes requested
     */
    private static void removeUnsupportedAttributes (PrintService ps,
                                                     DocFlavor flavor,
                                                     AttributeSet aset) {
        AttributeSet asUnsupported = ps.getUnsupportedAttributes(flavor,
                                                                 aset);

        if (asUnsupported != null) {
            Attribute[] usAttrs = asUnsupported.toArray();

            for (Attribute usAttr : usAttrs) {
                Class<? extends Attribute> category = usAttr.getCategory();

                if (ps.isAttributeCategorySupported(category)) {
                    Attribute attr =
                            (Attribute) ps.getDefaultAttributeValue(category);

                    if (attr != null) {
                        aset.add(attr);
                    }
                    else {
                        aset.remove(category);
                    }
                }
                else {
                    aset.remove(category);
                }
            }
        }
    }

}
