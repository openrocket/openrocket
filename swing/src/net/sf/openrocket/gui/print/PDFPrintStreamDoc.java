/*
 * PDFPrintStreamDoc.java
 */
package net.sf.openrocket.gui.print;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.DocAttributeSet;
import java.io.*;

/**
 * This class implements a javax Doc specifically for PDF printing. All reports in OpenRocket are PDF (iText) based.
 */
public class PDFPrintStreamDoc implements Doc {

    /** The source stream of the PDF document. */
    private InputStream stream;

    /** The document's attributes. */
    private DocAttributeSet attributeSet;

    /**
     * Constructor.
     *
     * @param ostream  an output stream representing the pdf doc
     * @param attributes the attributes of the document
     */
    public PDFPrintStreamDoc (ByteArrayOutputStream ostream, DocAttributeSet attributes) {
        stream = new ByteArrayInputStream(ostream.toByteArray());
        if (attributes != null) {
            attributeSet = AttributeSetUtilities.unmodifiableView(attributes);
        }
    }

    /**
     * Flavor is PDF.
     *
     * @return PDF flavor
     */
    @Override
    public DocFlavor getDocFlavor () {
        return DocFlavor.INPUT_STREAM.PDF;
    }

    @Override
    public DocAttributeSet getAttributes () {
        return attributeSet;
    }

    /* Since the data is to be supplied as an InputStream delegate to
     * getStreamForBytes().
     */
    @Override
    public Object getPrintData () throws IOException {
        return getStreamForBytes();
    }

    /**
     * Intentionally null since the flavor is PDF.
     *
     * @return null
     */
    @Override
    public Reader getReaderForText () {
        return null;
    }

    /* Return the print data as an InputStream.
     * Always return the same instance.
     */
    @Override
    public InputStream getStreamForBytes () throws IOException {
        return stream;
    }
}
