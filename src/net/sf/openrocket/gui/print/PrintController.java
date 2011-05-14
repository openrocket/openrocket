/*
 * PrintController.java
 *
 */
package net.sf.openrocket.gui.print;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.print.visitor.FinSetVisitorStrategy;
import net.sf.openrocket.gui.print.visitor.PartsDetailVisitorStrategy;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfWriter;

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
	 * @param msn         the paper size
	 */
	public void print(OpenRocketDocument doc, Iterator<PrintableContext> toBePrinted, OutputStream outputFile,
						PrintSettings settings) {
		
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
			} catch (InterruptedException e) {
			}
			while (toBePrinted.hasNext()) {
				PrintableContext printableContext = toBePrinted.next();
				
				Set<Integer> stages = printableContext.getStageNumber();
				
				switch (printableContext.getPrintable()) {
				case DESIGN_REPORT:
					DesignReport dp = new DesignReport(doc, idoc);
					dp.writeToDocument(writer);
					idoc.newPage();
					break;
				case FIN_TEMPLATE:
					final FinSetVisitorStrategy finWriter = new FinSetVisitorStrategy(idoc,
																							writer,
																							stages);
					finWriter.writeToDocument(doc.getRocket());
					break;
				case PARTS_DETAIL:
					final PartsDetailVisitorStrategy detailVisitor = new PartsDetailVisitorStrategy(idoc,
																										writer,
																										stages);
					detailVisitor.writeToDocument(doc.getRocket());
					detailVisitor.close();
					idoc.newPage();
					break;
				}
			}
			//Stupid iText throws a really nasty exception if there is no data when close is called.
			if (writer.getCurrentDocumentSize() <= 140) {
				writer.setPageEmpty(false);
			}
			writer.close();
			idoc.close();
		} catch (DocumentException e) {
		} catch (ExceptionConverter ec) {
		} finally {
			if (outputFile != null) {
				try {
					outputFile.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Get the correct paper size from the print settings.
	 * 
	 * @param settings	the print settings
	 * @return			the paper size
	 */
	private Rectangle getSize(PrintSettings settings) {
		PaperSize size = settings.getPaperSize();
		PaperOrientation orientation = settings.getPaperOrientation();
		return orientation.orient(size.getSize());
	}
	
}
