/*
 * PrintServiceDialog.java
 *
 */
package net.sf.openrocket.gui.print;

import net.miginfocom.swing.MigLayout;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PrintServiceDialog extends JDialog implements ActionListener {

    public static final int APPROVE = 1;
    private JButton btnCancel, btnPrint;
    private boolean pdfFlavorSupported = true;
    private PrintService services[];
    private int defaultServiceIndex, status;
    private PrintRequestAttributeSet asOriginal;
    private HashPrintRequestAttributeSet asCurrent;
    private PrintService psCurrent;
    private DocFlavor docFlavor;
    private GeneralPanel pnlGeneral;
    private static final String GENERAL_TAB_TITLE = "General";
    private static final String PRINT_BUTTON_LABEL = "Print";
    private static final String CANCEL_BUTTON_LABEL = "Cancel";

    private class MediaPanel extends JPanel
            implements ItemListener {

        private static final String strTitle = "Media";
        private static final String SOURCE = "Source:";
        private static final String SIZE = "Size:";

        private JLabel lblSize, lblSource;
        private JComboBox cbSize, cbSource;
        private ArrayList<MediaSizeName> sizes;
        private ArrayList sources;

        private String getMediaName (String s) {
            String s1 = s.replace(' ', '-');
            s1 = s1.replace('#', 'n');
            return PaperSize.toDisplayable(s1);
        }

        public void itemStateChanged (ItemEvent itemevent) {
            Object obj = itemevent.getSource();
            if (itemevent.getStateChange() == 1) {
                if (obj == cbSize) {
                    int i = cbSize.getSelectedIndex();
                    if (i >= 0 && i < sizes.size()) {
                        if (cbSource.getItemCount() > 1 && cbSource.getSelectedIndex() >= 1) {
                            int k = cbSource.getSelectedIndex() - 1;
                            MediaTray mediatray = (MediaTray) sources.get(k);
                            asCurrent.add(new MediaWrapper(mediatray));
                        }
                        asCurrent.add(sizes.get(i));
                    }
                }
                else if (obj == cbSource) {
                    int j = cbSource.getSelectedIndex();
                    if (j >= 1 && j < sources.size() + 1) {
                        asCurrent.remove(MediaWrapper.class);
                        asCurrent.add((MediaTray) sources.get(j - 1));
                    }
                    else if (j == 0) {
                        asCurrent.remove(MediaWrapper.class);
                        if (cbSize.getItemCount() > 0) {
                            int l = cbSize.getSelectedIndex();
                            asCurrent.add(sizes.get(l));
                        }
                    }
                }
            }
        }


        public void updateInfo () {
            boolean flag = false;
            cbSize.removeItemListener(this);
            cbSize.removeAllItems();
            cbSource.removeItemListener(this);
            cbSource.removeAllItems();
            cbSource.addItem(getMediaName("auto-select"));
            sizes.clear();
            sources.clear();
            if (psCurrent.isAttributeCategorySupported(Media.class)) {
                flag = true;
                Object obj = null;
                try {
                    obj = psCurrent.getSupportedAttributeValues(Media.class, docFlavor, asCurrent);
                }
                catch (IllegalArgumentException iae) {
                    pdfFlavorSupported = false;
                                             //dgp
                }
                if (obj instanceof Media[]) {
                    Media amedia[] = (Media[]) obj;
                    Set<SortableMediaSizeName> sizeSet = new TreeSet<SortableMediaSizeName>();

                    for (int i = 0; i < amedia.length; i++) {
                        Media media = amedia[i];
                        if (media instanceof MediaSizeName) {
                            sizeSet.add(new SortableMediaSizeName((MediaSizeName) media));
                        }
                        else if (media instanceof MediaTray) {
                            sources.add(media);
                            cbSource.addItem(getMediaName(media.toString()));
                        }
                    }

                    //The set eliminates duplicates.
                    for (Iterator<SortableMediaSizeName> mediaSizeNameIterator = sizeSet.iterator(); mediaSizeNameIterator
                            .hasNext();) {
                        SortableMediaSizeName media = mediaSizeNameIterator.next();

                        sizes.add(media.getMediaSizeName());
                        cbSize.addItem(media.toString());
                    }

                }
            }
            boolean flag1 = flag && sizes.size() > 0;
            lblSize.setEnabled(flag1);
            cbSize.setEnabled(flag1);
            cbSource.setEnabled(false);
            lblSource.setEnabled(false);
            if (flag) {
                Media media = (Media) asCurrent.get(Media.class);
                boolean attributeValueSupported = false;
                try {
                    attributeValueSupported = psCurrent.isAttributeValueSupported(media, docFlavor, asCurrent);
                }
                catch (IllegalArgumentException iae) {
                    pdfFlavorSupported = false;
                    //dgp
                }
                if (media == null || !attributeValueSupported) {
                    media = (Media) psCurrent.getDefaultAttributeValue(Media.class);
                    if (media == null && sizes.size() > 0) {
                        media = sizes.get(0);
                    }
                    if (media != null) {
                        asCurrent.add(media);
                    }
                }
                if (media != null) {
                    if (media instanceof MediaSizeName) {
                        MediaSizeName mediasizename = (MediaSizeName) media;
                        cbSize.setSelectedIndex(sizes.indexOf(mediasizename));
                    }
                    else if (media instanceof MediaTray) {
                        MediaTray mediatray = (MediaTray) media;
                        cbSource.setSelectedIndex(sources.indexOf(mediatray) + 1);
                    }
                }
                else {
                    cbSize.setSelectedIndex(sizes.size() <= 0 ? -1 : 0);
                    cbSource.setSelectedIndex(0);
                }
                int j = cbSize.getSelectedIndex();
                if (j >= 0 && j < sizes.size()) {
                    asCurrent.add(sizes.get(j));
                }
                j = cbSource.getSelectedIndex();
                if (j >= 1 && j < sources.size() + 1) {
                    asCurrent.add((MediaTray) sources.get(j - 1));
                }
            }
            cbSize.addItemListener(this);
            cbSource.addItemListener(this);
        }

        public MediaPanel () {
            super(new MigLayout("fill, gap rel unrel"));
            sizes = new ArrayList<MediaSizeName>();
            sources = new ArrayList();
            setBorder(BorderFactory.createTitledBorder(strTitle));
            cbSize = new JComboBox();
            cbSource = new JComboBox();
            lblSize = new JLabel(SIZE, 11);
            lblSize.setDisplayedMnemonic(PrintServiceDialog.getMnemonic(SIZE));
            lblSize.setLabelFor(cbSize);
            add(lblSize);
            add(cbSize, "wrap");
            lblSource = new JLabel(SOURCE, 11);
            lblSource.setDisplayedMnemonic(PrintServiceDialog.getMnemonic(SOURCE));
            lblSource.setLabelFor(cbSource);
            add(lblSource);
            add(cbSource);
        }

        class SortableMediaSizeName implements Comparable {
            MediaSizeName delegate;

            String displayableName;

            SortableMediaSizeName(MediaSizeName msn) {
                delegate = msn;
                displayableName = getMediaName(delegate.toString());
                if (displayableName == null) {
                    displayableName = delegate.toString();
                }
            }

            /**
             * Returns a string value corresponding to this enumeration value.
             */
            @Override
            public String toString () {
                return displayableName;
            }

            @Override
            public int compareTo (final Object o) {
                String name = displayableName;
                if (name != null) {
                    return name.compareTo(o.toString());
                }
                return 1;
            }

            @Override
            public boolean equals (final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                final SortableMediaSizeName that = (SortableMediaSizeName) o;

                return displayableName.equals(that.displayableName);
            }

            @Override
            public int hashCode () {
                return displayableName.hashCode();
            }

            MediaSizeName getMediaSizeName() {
                return delegate;
            }
        }
    }

    private class PrintServicePanel extends JPanel
            implements ActionListener, ItemListener, PopupMenuListener {

        private final String strTitle = "Print Service";
        private JButton btnProperties;
        private JComboBox cbName;
        private JLabel lblType, lblStatus, lblInfo;
        private JLabel vType, vStatus, vInfo;
        private ServiceUIFactory uiFactory;
        private boolean changedService;

        public PrintServicePanel () {
            super(new MigLayout("fill, gap rel unrel"));
            changedService = false;
            uiFactory = psCurrent.getServiceUIFactory();
            setBorder(BorderFactory.createTitledBorder(strTitle));
            String as[] = new String[services.length];
            for (int i = 0; i < as.length; i++) {
                as[i] = services[i].getName();
            }

            cbName = new JComboBox(as);
            cbName.setSelectedIndex(defaultServiceIndex);
            cbName.addItemListener(this);
            cbName.addPopupMenuListener(this);
            JLabel jlabel = new JLabel(("Name:"), 11);
            jlabel.setDisplayedMnemonic(PrintServiceDialog.getMnemonic("Name"));
            jlabel.setLabelFor(cbName);
            add(jlabel);
            add(cbName);
            btnProperties = PrintServiceDialog.createButton("Properties...", this);
            add(btnProperties, "wrap");
            lblStatus = new JLabel("Status:", 11);
            add(lblStatus);
            vStatus = new JLabel();
            add(vStatus, "wrap");
            lblType = new JLabel("Type:", 11);
            vType = new JLabel();
            add(lblType);
            add(vType, "wrap");
            lblInfo = new JLabel("Info:", 11);
            vInfo = new JLabel();
            add(lblInfo);
            add(vInfo, "wrap");
        }

        public void actionPerformed (ActionEvent actionevent) {
            Object obj = actionevent.getSource();
            if (obj == btnProperties && uiFactory != null) {
                JDialog jdialog = (JDialog) uiFactory.getUI(3, "javax.swing.JDialog");
                if (jdialog != null) {
                    jdialog.show();
                }
                else {
                    btnProperties.setEnabled(false);
                }
            }
        }

        public void itemStateChanged (ItemEvent itemevent) {
            if (itemevent.getStateChange() == 1) {
                int i = cbName.getSelectedIndex();
                if (i >= 0 && i < services.length && !services[i].equals(psCurrent)) {
                    psCurrent = services[i];
                    uiFactory = psCurrent.getServiceUIFactory();
                    changedService = true;
                    Destination destination = (Destination) asOriginal.get(
                            Destination.class);
                    if ((destination != null) && psCurrent.isAttributeCategorySupported(
                            Destination.class)) {
                        asCurrent.add(destination);
                    }
                    else {
                        asCurrent.remove(Destination.class);
                    }
                }
            }
        }

        public void popupMenuWillBecomeVisible (PopupMenuEvent popupmenuevent) {
            changedService = false;
        }

        public void popupMenuWillBecomeInvisible (PopupMenuEvent popupmenuevent) {
            if (changedService) {
                changedService = false;
                updatePanels();
            }
        }

        public void popupMenuCanceled (PopupMenuEvent popupmenuevent) {
        }

        public void updateInfo () {
            PrintServiceAttribute psa = psCurrent.getAttribute(PrinterMakeAndModel.class);
            if (psa != null) {
                vType.setText(psa.toString());
            }
            psa = psCurrent.getAttribute(PrinterIsAcceptingJobs.class);
            if (psa != null) {
                vStatus.setText((psa.toString()));
            }
            psa = psCurrent.getAttribute(PrinterInfo.class);
            if (psa != null) {
                vInfo.setText(psa.toString());
            }
            btnProperties.setEnabled(uiFactory != null);
        }

    }

    private class GeneralPanel extends JPanel {

        public void updateInfo () {
            pnlPrintService.updateInfo();
            pnlMedia.updateInfo();
        }

        private PrintServicePanel pnlPrintService;
        private MediaPanel pnlMedia;

        public GeneralPanel () {
            super(new MigLayout("fill, gap rel unrel"));
            pnlPrintService = new PrintServicePanel();
            add(pnlPrintService, "wrap");
            pnlMedia = new MediaPanel();
            add(pnlMedia, "wrap");
        }
    }


    public PrintServiceDialog (GraphicsConfiguration graphicsconfiguration, int i, int j, PrintService aprintservice[],
                               int k, DocFlavor docflavor, PrintRequestAttributeSet printrequestattributeset,
                               Dialog dialog, JPanel... additional) {
        super(dialog, PRINT_BUTTON_LABEL, true, graphicsconfiguration);
        initPrintDialog(i, j, aprintservice, k, docflavor, printrequestattributeset, additional);
    }

    void initPrintDialog (int i, int j, PrintService aprintservice[], int k, DocFlavor docflavor,
                          PrintRequestAttributeSet printrequestattributeset,
                          JPanel... additional) {
        services = aprintservice;
        defaultServiceIndex = k;
        asOriginal = printrequestattributeset;
        asCurrent = new HashPrintRequestAttributeSet(printrequestattributeset);
        psCurrent = aprintservice[k];
        docFlavor = docflavor;
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        final JTabbedPane tpTabs = new JTabbedPane();
        tpTabs.setBorder(new EmptyBorder(5, 5, 5, 5));

        if (additional != null) {
            for (JPanel anAdditional : additional) {
                tpTabs.add(anAdditional, anAdditional.getName(), 0);
            }
        }
        pnlGeneral = new GeneralPanel();
        tpTabs.add(GENERAL_TAB_TITLE, pnlGeneral);

        container.add(tpTabs, "Center");
        updatePanels();
        JPanel jpanel = new JPanel(new MigLayout());
        btnPrint = createExitButton(PRINT_BUTTON_LABEL, this);
        jpanel.add(btnPrint, "x 300");
        getRootPane().setDefaultButton(btnPrint);
        btnPrint.setEnabled(pdfFlavorSupported);

        btnCancel = createExitButton(CANCEL_BUTTON_LABEL, this);
        handleEscKey(btnCancel);
        jpanel.add(btnCancel, "x 380");
        container.add(jpanel, "South");
        addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent windowevent) {
                dispose(2);
            }
        }
        );
        setResizable(false);
        setLocation(i, j);
        pack();
    }

    private void handleEscKey (JButton jbutton) {
        AbstractAction abstractaction = new AbstractAction() {

            public void actionPerformed (ActionEvent actionevent) {
                dispose(2);
            }

        };
        KeyStroke keystroke = KeyStroke.getKeyStroke('\033', false);
        InputMap inputmap = jbutton.getInputMap(2);
        ActionMap actionmap = jbutton.getActionMap();
        if (inputmap != null && actionmap != null) {
            inputmap.put(keystroke, "cancel");
            actionmap.put("cancel", abstractaction);
        }
    }

    public int getStatus () {
        return status;
    }

    public PrintRequestAttributeSet getAttributes () {
        if (status == 1) {
            return asCurrent;
        }
        else {
            return asOriginal;
        }
    }

    public PrintService getPrintService () {
        if (status == 1) {
            return psCurrent;
        }
        else {
            return null;
        }
    }

    public void dispose (int i) {
        status = i;
        super.dispose();
    }

    public void actionPerformed (ActionEvent actionevent) {
        Object obj = actionevent.getSource();
        boolean flag = false;
        if (obj == btnPrint) {
            flag = true;
            if (pnlGeneral != null) {
                asCurrent.remove(Destination.class);
            }
        }
        dispose(flag ? 1 : 2);
    }


    private void updatePanels () {
        pnlGeneral.updateInfo();
    }

    private static char getMnemonic (String s) {
        if (s != null && s.length() > 0) {
            return s.charAt(0);
        }
        else {
            return '\0';
        }
    }

    private static JButton createButton (String s, ActionListener actionlistener) {
        JButton jbutton = new JButton(s);
        jbutton.setMnemonic(getMnemonic(s));
        jbutton.addActionListener(actionlistener);
        return jbutton;
    }

    private static JButton createExitButton (String s, ActionListener actionlistener) {
        JButton jbutton = new JButton(s);
        jbutton.addActionListener(actionlistener);
        jbutton.getAccessibleContext().setAccessibleDescription(s);
        return jbutton;
    }

    static class MediaWrapper
            implements PrintRequestAttribute {

        private Media media;

        MediaWrapper (Media theMedia) {
            media = theMedia;
        }

        Media getMedia () {
            return media;
        }

        public final Class getCategory () {
            return this.getClass();
        }

        public final String getName () {
            return "mw";
        }

        public String toString () {
            return media.toString();
        }

        public int hashCode () {
            return media.hashCode();
        }

    }

}
