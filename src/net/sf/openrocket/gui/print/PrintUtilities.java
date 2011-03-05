/*
 * PrintUtilities.java
 */
package net.sf.openrocket.gui.print;


import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Locale;

/**
 * Utilities methods and fonts used for printing.
 */
public class PrintUtilities implements Printable {

    /**
     * The logger.
     */
    private static final LogHelper log = Application.getLogger();

    public static final int NORMAL_FONT_SIZE = Font.DEFAULTSIZE - 3;
    public static final int SMALL_FONT_SIZE = NORMAL_FONT_SIZE - 3;

    public static final Font BOLD = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE, Font.BOLD);
    public static final Font BIG_BOLD = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE + 3, Font.BOLD);
    public static final Font BOLD_UNDERLINED = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE,
                                                        Font.BOLD | Font.UNDERLINE);
    public static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, NORMAL_FONT_SIZE);
    public static final Font SMALL = new Font(Font.FontFamily.HELVETICA, SMALL_FONT_SIZE);


    private Component componentToBePrinted;

    public static void printComponent (Component c) {
        new PrintUtilities(c).print();
    }

    public PrintUtilities (Component componentToBePrinted) {
        this.componentToBePrinted = componentToBePrinted;
    }

    public void print () {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            }
            catch (PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
        }
    }

    public int print (Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return (NO_SUCH_PAGE);
        }
        else {
            Graphics2D g2d = (Graphics2D) g;
            translateToJavaOrigin(g2d, pageFormat);
            disableDoubleBuffering(componentToBePrinted);
            componentToBePrinted.paint(g2d);
            enableDoubleBuffering(componentToBePrinted);
            return (PAGE_EXISTS);
        }
    }

    public static void disableDoubleBuffering (Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
    }

    public static void enableDoubleBuffering (Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }

    public static PrintService askUserForPDFPrintService () {
        return askUserForPrintService(DocFlavor.INPUT_STREAM.PDF);
    }

    public static PrintService askUserForPrintService (DocFlavor flavor) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(getDefaultMedia().getMediaSizeName());

        return ServiceUI.printDialog(null, 100, 100, services, svc, flavor, attrs);
    }

    /**
     * Sets the paper size for pages using these attributes to the default size for the default locale. The default size
     * for locales in the United States and Canada is MediaType.NA_LETTER. The default size for all other locales is
     * MediaType.ISO_A4.
     */
    public static MediaSize getDefaultMedia () {
        String defaultCountry = Locale.getDefault().getCountry();
        if (defaultCountry != null &&
            (defaultCountry.equals(Locale.US.getCountry()) ||
             defaultCountry.equals(Locale.CANADA.getCountry()))) {
            return MediaSize.NA.LETTER;
        }
        else {
            return MediaSize.ISO.A4;
        }
    }

    /**
     * Translate the page format coordinates onto the graphics object using Java's origin (top left).
     *
     * @param g2d        the graphics object
     * @param pageFormat the print page format
     */
    public static void translateToJavaOrigin (Graphics2D g2d, PageFormat pageFormat) {
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    }

    /**
     * Add text as a new paragraph in a given font to the document.
     *
     * @param document  the document
     * @param font      the font
     * @param title     the title
     */
    public static void addText (Document document, com.itextpdf.text.Font font, String title) {
        Chunk sectionHeader = new Chunk(title);
        sectionHeader.setFont(font);
        try {
            Paragraph p = new Paragraph();
            p.add(sectionHeader);
            document.add(p);
        }
        catch (DocumentException e) {
            log.error("Could not add paragraph.", e);
        }
    }
}
