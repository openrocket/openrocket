/*
 * PDFPrintStreamDoc.java
 */
package net.sf.openrocket.gui.print;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.DocAttributeSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 */
public class PDFPrintStreamDoc implements Doc {

    private InputStream stream;
    private DocAttributeSet attributeSet;

    public PDFPrintStreamDoc (ByteArrayOutputStream ostream, DocAttributeSet attributes) {
        stream = new ByteArrayInputStream(ostream.toByteArray());
        if (attributes != null) {
            attributeSet = AttributeSetUtilities.unmodifiableView(attributes);
        }
    }

    public DocFlavor getDocFlavor () {
        return DocFlavor.INPUT_STREAM.PDF;
    }

    public DocAttributeSet getAttributes () {
        return attributeSet;
    }

    /* Since the data is to be supplied as an InputStream delegate to
     * getStreamForBytes().
     */

    public Object getPrintData () throws IOException {
        return getStreamForBytes();
    }

    public Reader getReaderForText () {
        return null;
    }

    /* Return the print data as an InputStream.
     * Always return the same instance.
     */

    public InputStream getStreamForBytes () throws IOException {
        return stream;
    }
}
