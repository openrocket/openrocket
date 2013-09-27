/*
 * PrintController.java
 *
 */
package net.sf.openrocket.gui.print;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.print.components.Rule;
import net.sf.openrocket.gui.print.visitor.CenteringRingStrategy;
import net.sf.openrocket.gui.print.visitor.FinMarkingGuideStrategy;
import net.sf.openrocket.gui.print.visitor.FinSetPrintStrategy;
import net.sf.openrocket.gui.print.visitor.PageFitPrintStrategy;
import net.sf.openrocket.gui.print.visitor.PartsDetailVisitorStrategy;
import net.sf.openrocket.gui.print.visitor.TransitionStrategy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * This is the main active object for printing.  It performs all actions necessary to create and populate the print
 * file.
 */
public class PrintController {

    /**
     * Print the selected components to a PDF document.
     *
     * @param doc         the OR document
     * @param toBePrinted the user chosen items to print
     * @param outputFile  the file being written to
     * @param settings    the print settings
     * @param rotation    the angle the rocket figure is rotated
     */
    public void print(OpenRocketDocument doc, Iterator<PrintableContext> toBePrinted, OutputStream outputFile,
                      PrintSettings settings, double rotation) {

        Document idoc = new Document(getSize(settings));
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(idoc, outputFile);
            writer.setStrictImageSequence(true);

            writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);
            writer.addViewerPreference(PdfName.PICKTRAYBYPDFSIZE, PdfBoolean.PDFTRUE);
            try {
                idoc.open();
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
            }

            // Used to combine multiple components onto fewer sheets of paper
            PageFitPrintStrategy pageFitPrint = new PageFitPrintStrategy(idoc, writer);

            boolean addRule = false;

            while (toBePrinted.hasNext()) {
                PrintableContext printableContext = toBePrinted.next();

                Set<Integer> stages = printableContext.getStageNumber();

                switch (printableContext.getPrintable()) {
                    case DESIGN_REPORT:
                        DesignReport dp = new DesignReport(doc, idoc, rotation);
                        dp.writeToDocument(writer);
                        idoc.newPage();
                        break;

                    case FIN_TEMPLATE:
                        final FinSetPrintStrategy finWriter = new FinSetPrintStrategy(idoc, writer, stages, pageFitPrint);
                        finWriter.writeToDocument(doc.getRocket());
                        addRule = true;
                        break;

                    case PARTS_DETAIL:
                        final PartsDetailVisitorStrategy detailVisitor = new PartsDetailVisitorStrategy(idoc, writer, stages);
                        detailVisitor.writeToDocument(doc.getRocket());
                        detailVisitor.close();
                        idoc.newPage();
                        break;

                    case TRANSITION_TEMPLATE:
                        final TransitionStrategy tranWriter = new TransitionStrategy(idoc, writer, stages, pageFitPrint, false);
                        if (tranWriter.writeToDocument(doc.getRocket())) {
                            addRule = true;
                        }
                        break;

                    case NOSE_CONE_TEMPLATE:
                        final TransitionStrategy coneWriter = new TransitionStrategy(idoc, writer, stages, pageFitPrint, true);
                        if (coneWriter.writeToDocument(doc.getRocket())) {
                            addRule = true;
                        }
                        break;

                    case CENTERING_RING_TEMPLATE:
                        final CenteringRingStrategy crWriter = new CenteringRingStrategy(idoc, writer, stages,
                                pageFitPrint);
                        crWriter.writeToDocument(doc.getRocket());
                        addRule = true;
                        break;

                    case FIN_MARKING_GUIDE:
                        final FinMarkingGuideStrategy fmg = new FinMarkingGuideStrategy(idoc, writer);
                        fmg.writeToDocument(doc.getRocket());
                        idoc.newPage();
                        addRule = true;
                        break;
                }
            }

            if (addRule) {
                //Add a ruler to the output.
                pageFitPrint.addComponent(new Rule(Rule.Orientation.BOTTOM));
            }

            // Write out parts that we are going to combine onto single sheets of paper
            pageFitPrint.writeToDocument();
            idoc.newPage();

            //Stupid iText throws a really nasty exception if there is no data when close is called.
            if (writer.getCurrentDocumentSize() <= 140) {
                writer.setPageEmpty(false);
            }
            writer.close();
            idoc.close();
        }
        catch (DocumentException e) {
        }
        catch (ExceptionConverter ec) {
        }
        finally {
            if (outputFile != null) {
                try {
                    outputFile.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    /**
     * Get the correct paper size from the print settings.
     *
     * @param settings the print settings
     *
     * @return the paper size
     */
    private Rectangle getSize(PrintSettings settings) {
        PaperSize size = settings.getPaperSize();
        PaperOrientation orientation = settings.getPaperOrientation();
        return orientation.orient(size.getSize());
    }

}
