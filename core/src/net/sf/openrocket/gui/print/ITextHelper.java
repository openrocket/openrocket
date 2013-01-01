/*
 * ITextHelper.java
 */
package net.sf.openrocket.gui.print;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A bunch of helper methods for creating iText components.
 */
public final class ITextHelper {

    public static BaseFont getBaseFont() {
        try {
            return BaseFont.createFont("/dejavu-font/DejaVuSerif.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a cell for an iText table.
     *
     * @return a cell with bottom border
     */
    public static PdfPCell createCell() {
        return createCell(Rectangle.BOTTOM);
    }

    /**
     * Create a cell for an iText table with the given border location.
     *
     * @param border the border location
     *
     * @return a cell with given border
     */
    public static PdfPCell createCell(int border) {
        PdfPCell result = new PdfPCell();
        result.setBorder(border);

        return result;
    }

    /**
     * Create a cell whose contents are a table.  No border.
     *
     * @param table the table to insert into the cell
     *
     * @return the cell containing a table
     */
    public static PdfPCell createCell(PdfPTable table) {
        PdfPCell result = new PdfPCell();
        result.setBorder(PdfPCell.NO_BORDER);
        result.addElement(table);

        return result;
    }

    /**
     * Create a cell whose contents are the given string. No border.  Standard PrintUtilities.NORMAL font.
     *
     * @param v the text of the cell.
     *
     * @return the cell containing the text
     */
    public static PdfPCell createCell(String v) {
        return createCell(v, Rectangle.NO_BORDER, PrintUtilities.NORMAL);
    }

    /**
     * Create a cell whose contents are the given string , rendered with the given font.  No border.
     *
     * @param v    the text of the cell
     * @param font the font
     *
     * @return the cell containing the text
     */
    public static PdfPCell createCell(String v, Font font) {
        return createCell(v, Rectangle.NO_BORDER, font);
    }

    /**
     * Create a cell whose contents are the given string with specified left and right padding (spacing).
     *
     * @param v        the text of the cell
     * @param leftPad  the number of points to precede the text
     * @param rightPad the number of points to follow the text
     *
     * @return the cell containing the text
     */
    public static PdfPCell createCell(String v, int leftPad, int rightPad) {
        PdfPCell c = createCell(v, Rectangle.NO_BORDER, PrintUtilities.NORMAL);
        c.setPaddingLeft(leftPad);
        c.setPaddingRight(rightPad);
        return c;
    }

    /**
     * Create a cell whose contents are the given string with the given border.  Uses NORMAL font.
     *
     * @param v      the text of the cell
     * @param border the border type
     *
     * @return the cell containing the text
     */
    public static PdfPCell createCell(String v, int border) {
        return createCell(v, border, PrintUtilities.NORMAL);
    }

    /**
     * Complete create cell - fully qualified.  Create a cell whose contents are the given string with the given border and font.
     *
     * @param v      the text of the cell
     * @param border the border type
     * @param font   the font
     *
     * @return the cell containing the text
     */
    public static PdfPCell createCell(String v, int border, Font font) {
        PdfPCell result = new PdfPCell();
        result.setBorder(border);
        Chunk c = new Chunk();
        c.setFont(font);
        c.append(v);
        result.addElement(c);
        return result;
    }

    /**
     * Create a phrase with the given text and font.
     *
     * @param text the text
     * @param font the font
     *
     * @return an iText phrase
     */
    public static Phrase createPhrase(String text, Font font) {
        Phrase p = new Phrase();
        final Chunk chunk = new Chunk(text);
        chunk.setFont(font);
        p.add(chunk);
        return p;
    }

    /**
     * Create a phrase with the given text.
     *
     * @param text the text
     *
     * @return an iText phrase
     */
    public static Phrase createPhrase(String text) {
        return createPhrase(text, PrintUtilities.NORMAL);
    }

    /**
     * Create a paragraph with the given text and font.
     *
     * @param text the text
     * @param font the font
     *
     * @return an iText paragraph
     */
    public static Paragraph createParagraph(String text, Font font) {
        Paragraph p = new Paragraph();
        final Chunk chunk = new Chunk(text);
        chunk.setFont(font);
        p.add(chunk);
        return p;
    }

    /**
     * Create a paragraph with the given text and using NORMAL font.
     *
     * @param text the text
     *
     * @return an iText paragraph
     */
    public static Paragraph createParagraph(String text) {
        return createParagraph(text, PrintUtilities.NORMAL);
    }

    /**
     * Break a large image up into page-size pieces and output each page in order to an iText document.  The image is overlayed with an matrix of pages
     * running from left to right until the right side of the image is reached. Then the next 'row' of pages is output from left to right, and so on.
     *
     * @param pageSize a rectangle that defines the bounds of the page size
     * @param doc      the iText document
     * @param writer   the underlying content writer
     * @param image    the source image
     *
     * @throws DocumentException thrown if the document could not be written
     */
    public static void renderImageAcrossPages(Rectangle pageSize, Document doc, PdfWriter writer, java.awt.Image image)
            throws DocumentException {
        final int margin = (int) Math.min(doc.topMargin(), PrintUnit.POINTS_PER_INCH * 0.3f);
        float wPage = pageSize.getWidth() - 2 * margin;
        float hPage = pageSize.getHeight() - 2 * margin;

        float wImage = image.getWidth(null);
        float hImage = image.getHeight(null);
        java.awt.Rectangle crop = new java.awt.Rectangle(0, 0, (int) Math.min(wPage, wImage), (int) Math.min(hPage,
                                                                                                             hImage));
        PdfContentByte content = writer.getDirectContent();

        int ymargin = margin;

        while (true) {
            BufferedImage subImage = ((BufferedImage) image).getSubimage((int) crop.getX(), (int) crop.getY(),
                                                                         (int) crop.getWidth(), (int) crop.getHeight());

            Graphics2D g2 = content.createGraphics(pageSize.getWidth(), pageSize.getHeight());
            g2.drawImage(subImage, margin, ymargin, null);
            g2.dispose();

            final int newImageX = (int) (crop.getWidth() + crop.getX());

            if (newImageX < wImage) {
                //Spans multiple pages horizontally
                double subImageWidth = Math.min(wImage - newImageX, wPage);
                crop = new java.awt.Rectangle(newImageX, (int) crop.getY(), (int) subImageWidth,
                                              (int) crop.getHeight());
            }
            else {
                //Spans multiple pages vertically
                final int newImageY = (int) (crop.getHeight() + crop.getY());

                if (newImageY < hImage) {
                    double subImageHeight = Math.min(hImage - newImageY, hPage);
                    crop = new java.awt.Rectangle(0, newImageY, (int) Math.min(wPage, wImage), (int) subImageHeight);
                }
                else {
                    break;
                }
            }
            doc.newPage();
        }
    }

}