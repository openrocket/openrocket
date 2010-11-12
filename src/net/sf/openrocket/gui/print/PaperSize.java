/*
 * PaperSize.java
 */
package net.sf.openrocket.gui.print;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;

import javax.print.attribute.standard.MediaSizeName;
import java.util.HashMap;
import java.util.Map;

/**
 * Various mappings of paper sizes.
 */
public class PaperSize {
    
    private static Map<String, MediaSizeName> paperNames = new HashMap<String, MediaSizeName>();
    private static Map<MediaSizeName, Rectangle> paperItext = new HashMap<MediaSizeName, Rectangle>();
    
    static {
        populateNameMap();
        populateITextSizeMap();
    }
    private PaperSize() {}
    
    public static MediaSizeName convert(String name) {
        return paperNames.get(name);
    }

    public static Rectangle convert(MediaSizeName name) {
        return paperItext.get(name);
    }
    
    private static void populateNameMap() {
        paperNames.put("iso-a0", MediaSizeName.ISO_A0);
        paperNames.put("iso-a1", MediaSizeName.ISO_A1);
        paperNames.put("iso-a2", MediaSizeName.ISO_A2);
        paperNames.put("iso-a3", MediaSizeName.ISO_A3);
        paperNames.put("iso-a4", MediaSizeName.ISO_A4);
        paperNames.put("iso-a5", MediaSizeName.ISO_A5);
        paperNames.put("iso-a6", MediaSizeName.ISO_A6);
        paperNames.put("iso-a7", MediaSizeName.ISO_A7);
        paperNames.put("iso-a8", MediaSizeName.ISO_A8);
        paperNames.put("iso-a9", MediaSizeName.ISO_A9);
        paperNames.put("iso-a10", MediaSizeName.ISO_A10);
        paperNames.put("iso-b0", MediaSizeName.ISO_B0);
        paperNames.put("iso-b1", MediaSizeName.ISO_B1);
        paperNames.put("iso-b2", MediaSizeName.ISO_B2);
        paperNames.put("iso-b3", MediaSizeName.ISO_B3);
        paperNames.put("iso-b4", MediaSizeName.ISO_B4);
        paperNames.put("iso-b5", MediaSizeName.ISO_B5);
        paperNames.put("iso-b6", MediaSizeName.ISO_B6);
        paperNames.put("iso-b7", MediaSizeName.ISO_B7);
        paperNames.put("iso-b8", MediaSizeName.ISO_B8);
        paperNames.put("iso-b9", MediaSizeName.ISO_B9);
        paperNames.put("iso-b10", MediaSizeName.ISO_B10);
        paperNames.put("na-letter", MediaSizeName.NA_LETTER);
        paperNames.put("na-legal", MediaSizeName.NA_LEGAL);
        paperNames.put("na-8x10", MediaSizeName.NA_8X10);
        paperNames.put("na-5x7", MediaSizeName.NA_5X7);
        paperNames.put("executive", MediaSizeName.EXECUTIVE);
        paperNames.put("folio", MediaSizeName.FOLIO);
        paperNames.put("invoice", MediaSizeName.INVOICE);
        paperNames.put("tabloid", MediaSizeName.TABLOID);
        paperNames.put("ledger", MediaSizeName.LEDGER);
        paperNames.put("quarto", MediaSizeName.QUARTO);
        paperNames.put("iso-c0", MediaSizeName.ISO_C0);
        paperNames.put("iso-c1", MediaSizeName.ISO_C1);
        paperNames.put("iso-c2", MediaSizeName.ISO_C2);
        paperNames.put("iso-c3", MediaSizeName.ISO_C3);
        paperNames.put("iso-c4", MediaSizeName.ISO_C4);
        paperNames.put("iso-c5", MediaSizeName.ISO_C5);
        paperNames.put("iso-c6", MediaSizeName.ISO_C6);
        paperNames.put("iso-designated-long", MediaSizeName.ISO_DESIGNATED_LONG);
        paperNames.put("jis-b0", MediaSizeName.JIS_B0);
        paperNames.put("jis-b1", MediaSizeName.JIS_B1);
        paperNames.put("jis-b2", MediaSizeName.JIS_B2);
        paperNames.put("jis-b3", MediaSizeName.JIS_B3);
        paperNames.put("jis-b4", MediaSizeName.JIS_B4);
        paperNames.put("jis-b5", MediaSizeName.JIS_B5);
        paperNames.put("jis-b6", MediaSizeName.JIS_B6);
        paperNames.put("jis-b7", MediaSizeName.JIS_B7);
        paperNames.put("jis-b8", MediaSizeName.JIS_B8);
        paperNames.put("jis-b9", MediaSizeName.JIS_B9);
        paperNames.put("jis-b10", MediaSizeName.JIS_B10);
        paperNames.put("a", MediaSizeName.A);
        paperNames.put("b", MediaSizeName.B);
        paperNames.put("c", MediaSizeName.C);
        paperNames.put("d", MediaSizeName.D);
        paperNames.put("e", MediaSizeName.E);
    }

    private static void populateITextSizeMap() {
        paperItext.put(MediaSizeName.ISO_A0, PageSize.A0);
        paperItext.put(MediaSizeName.ISO_A1, PageSize.A1);
        paperItext.put(MediaSizeName.ISO_A2, PageSize.A2);
        paperItext.put(MediaSizeName.ISO_A3, PageSize.A3);
        paperItext.put(MediaSizeName.ISO_A4, PageSize.A4);
        paperItext.put(MediaSizeName.ISO_A5, PageSize.A5);
        paperItext.put(MediaSizeName.ISO_A6, PageSize.A6);
        paperItext.put(MediaSizeName.ISO_A7, PageSize.A7);
        paperItext.put(MediaSizeName.ISO_A8, PageSize.A8);
        paperItext.put(MediaSizeName.ISO_A9, PageSize.A9);
        paperItext.put(MediaSizeName.ISO_A10, PageSize.A10);
        paperItext.put(MediaSizeName.ISO_B0, PageSize.B0);
        paperItext.put(MediaSizeName.ISO_B1, PageSize.B1);
        paperItext.put(MediaSizeName.ISO_B2, PageSize.B2);
        paperItext.put(MediaSizeName.ISO_B3, PageSize.B3);
        paperItext.put(MediaSizeName.ISO_B4, PageSize.B4);
        paperItext.put(MediaSizeName.ISO_B5, PageSize.B5);
        paperItext.put(MediaSizeName.ISO_B6, PageSize.B6);
        paperItext.put(MediaSizeName.ISO_B7, PageSize.B7);
        paperItext.put(MediaSizeName.ISO_B8, PageSize.B8);
        paperItext.put(MediaSizeName.ISO_B9, PageSize.B9);
        paperItext.put(MediaSizeName.ISO_B10, PageSize.B10);
        paperItext.put(MediaSizeName.NA_LETTER, PageSize.LETTER);
        paperItext.put(MediaSizeName.NA_LEGAL, PageSize.LEGAL);
        paperItext.put(MediaSizeName.EXECUTIVE, PageSize.EXECUTIVE);
        paperItext.put(MediaSizeName.A, PageSize.LETTER);
        paperItext.put(MediaSizeName.B, PageSize._11X17);
        paperItext.put(MediaSizeName.C, new RectangleReadOnly(PrintUnit.INCHES.toPoints(17), PrintUnit.INCHES.toPoints(22)));
        paperItext.put(MediaSizeName.D, new RectangleReadOnly(PrintUnit.INCHES.toPoints(22), PrintUnit.INCHES.toPoints(34)));
        paperItext.put(MediaSizeName.E, new RectangleReadOnly(PrintUnit.INCHES.toPoints(34), PrintUnit.INCHES.toPoints(44)));
    }
    
}
