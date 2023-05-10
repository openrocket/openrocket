package net.sf.openrocket.gui.print.visitor;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.gui.print.AbstractPrintable;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Common logic for printing strategies.
 */
public abstract class AbstractPrintStrategy<V> {
	/**
	 * The logger.
	 */
	protected static final Logger log = LoggerFactory.getLogger(AbstractPrintStrategy.class);
	/**
	 * The iText document.
	 */
	protected Document document;
	/**
	 * The direct iText writer.
	 */
	protected PdfWriter writer;
	/**
	 * The stages selected.
	 */
	protected Set<Integer> stages;
	/**
	 * Strategy for fitting multiple components onto a page.
	 */
	protected PageFitPrintStrategy pageFitPrint;
	
	/**
	 * Constructor.
	 *
	 * @param doc       the document
	 * @param pageFit   the page fitting strategy
	 * @param theWriter the pdf writer
	 * @param theStages the set of stages in the rocket
	 */
	public AbstractPrintStrategy(Document doc, PageFitPrintStrategy pageFit, PdfWriter theWriter,
			Set<Integer> theStages) {
		document = doc;
		pageFitPrint = pageFit;
		writer = theWriter;
		stages = theStages;
	}
	
	/**
	 * Recurse through the given rocket component.
	 *
	 * @param root the root component; all children will be printed recursively
	 */
	public V writeToDocument(final RocketComponent root) {
		return goDeep(root.getChildren());
	}
	
	/**
	 * Recurse through the given rocket component.
	 *
	 * @param theRc an array of rocket components; all children will be printed recursively
	 */
	protected abstract V goDeep(List<RocketComponent> theRc);
	
	/**
	 * Determine if the image will fit on the given page.
	 *
	 * @param pageSize the page size
	 * @param wImage   the width of the thing to be printed
	 * @param hImage   the height of the thing to be printed
	 *
	 * @return true if the thing to be printed will fit on a single page
	 */
	protected boolean fitsOnOnePage(Dimension pageSize, double wImage, double hImage) {
		double wPage = pageSize.getWidth() - PageFitPrintStrategy.MARGIN * 2;
		double hPage = pageSize.getHeight() - PageFitPrintStrategy.MARGIN * 2;
		
		int wRatio = (int) Math.ceil(wImage / wPage);
		int hRatio = (int) Math.ceil(hImage / hPage);
		
		return wRatio <= 1.0d && hRatio <= 1.0d;
	}
	
	/**
	 * Get the dimensions of the paper page.
	 *
	 * @return an internal Dimension
	 */
	protected Dimension getPageSize() {
		return new Dimension(document.getPageSize().getWidth(),
				document.getPageSize().getHeight());
	}
	
	/**
	 * Determine if the strategy's set of stage numbers (to print) contains the specified stage.
	 *
	 * @param stageNumber a stage number
	 *
	 * @return true if the strategy contains the stage number provided
	 */
	public boolean shouldPrintStage(int stageNumber) {
		if (stages == null || stages.isEmpty()) {
			return false;
		}
		
		for (final Integer stage : stages) {
			if (stage == stageNumber) {
				return true;
			}
		}
		
		return false;
	}
	
	void render(final AbstractPrintable thePrintable) throws DocumentException {
		java.awt.Dimension size = thePrintable.getSize();
		final Dimension pageSize = getPageSize();
		if (fitsOnOnePage(pageSize, size.getWidth(), size.getHeight())) {
			pageFitPrint.addComponent(thePrintable);
		}
		else {
			BufferedImage image = (BufferedImage) thePrintable.createImage();
			ITextHelper.renderImageAcrossPages(new Rectangle(pageSize.getWidth(), pageSize.getHeight()),
					document, writer, image);
			document.newPage();
		}
	}
}
