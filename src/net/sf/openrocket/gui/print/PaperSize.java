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
 * Various mappings of paper sizes and their names.
 */
public class PaperSize {

    /** Map of name to MediaSizeName instance. */
    private static Map<String, MediaSizeName> paperNames = new HashMap<String, MediaSizeName>();
    /** Map of identifying name to displayable name. */
    private static Map<String, String> displayableNames = new HashMap<String, String>();
    /** Map of MediaSizeName to rectangle, which defines the paper size. */
    private static Map<MediaSizeName, Rectangle> paperItext = new HashMap<MediaSizeName, Rectangle>();

    /**
     * Init.
     */
    static {
        populateNameMap();
        populateITextSizeMap();
        populateDisplayableNameMap();
    }

    /** Disallow construction. */
    private PaperSize() {}

    /**
     * Map an identifying paper name to it's corresponding MediaSizeName.
     *
     * @param name  a paper name
     *
     * @return  the associated MediaSizeName (or null if not found).
     */
    public static MediaSizeName convert(String name) {
        return paperNames.get(name);
    }

    /**
     * Map a MediaSizeName to it's size Rectangle.
     *
     * @param name  a paper name
     *
     * @return a Rectangle or null
     */
    public static Rectangle convert(MediaSizeName name) {
        return paperItext.get(name);
    }

    /**
     * Map an identifying paper name to a displayable name (usually it's common name).
     *
     * @param name  a paper name
     *
     * @return a displayable name
     */
    public static String toDisplayable(String name) {
        return displayableNames.get(name);
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

    /**
     * Create a name map from standard to displayable names
     */
    private static void populateDisplayableNameMap() {
        displayableNames.put("iso-a0", "A0");
        displayableNames.put("iso-a1", "A1");
        displayableNames.put("iso-a2", "A2");
        displayableNames.put("iso-a3", "A3");
        displayableNames.put("iso-a4", "A4");
        displayableNames.put("iso-a5", "A5");
        displayableNames.put("iso-a6", "A6");
        displayableNames.put("iso-a7", "A7");
        displayableNames.put("iso-a8", "A8");
        displayableNames.put("iso-a9", "A9");
        displayableNames.put("iso-a10", "A10");
        displayableNames.put("iso-b0", "B0");
        displayableNames.put("iso-b1", "B1");
        displayableNames.put("iso-b2", "B2");
        displayableNames.put("iso-b3", "B3");
        displayableNames.put("iso-b4", "B4");
        displayableNames.put("iso-b5", "B5");
        displayableNames.put("iso-b6", "B6");
        displayableNames.put("iso-b7", "B7");
        displayableNames.put("iso-b8", "B8");
        displayableNames.put("iso-b9", "B9");
        displayableNames.put("iso-b10", "B10");
        displayableNames.put("na-letter", "US Letter");
        displayableNames.put("na-legal", "US Legal");
        displayableNames.put("na-8x10", "US 8x10 inch");
        displayableNames.put("na-5x7", "US 5x7 inch");
        displayableNames.put("executive", "Executive");
        displayableNames.put("folio", "Folio");
        displayableNames.put("invoice", "Invoice");
        displayableNames.put("tabloid", "Tabloid");
        displayableNames.put("ledger", "Ledger");
        displayableNames.put("quarto", "Quarto");
        displayableNames.put("iso-c0", "C0");
        displayableNames.put("iso-c1", "C1");
        displayableNames.put("iso-c2", "C2");
        displayableNames.put("iso-c3", "C3");
        displayableNames.put("iso-c4", "C4");
        displayableNames.put("iso-c5", "C5");
        displayableNames.put("iso-c6", "C6");
        displayableNames.put("iso-designated-long", "ISO Designated Long Size");
        displayableNames.put("jis-b0", "Japanese B0");
        displayableNames.put("jis-b1", "Japanese B1");
        displayableNames.put("jis-b2", "Japanese B2");
        displayableNames.put("jis-b3", "Japanese B3");
        displayableNames.put("jis-b4", "Japanese B4");
        displayableNames.put("jis-b5", "Japanese B5");
        displayableNames.put("jis-b6", "Japanese B6");
        displayableNames.put("jis-b7", "Japanese B7");
        displayableNames.put("jis-b8", "Japanese B8");
        displayableNames.put("jis-b9", "Japanese B9");
        displayableNames.put("jis-b10", "Japanese B10");
        displayableNames.put("a", "US Letter");
        displayableNames.put("b", "Engineering ANSI B");
        displayableNames.put("c", "Engineering ANSI C");
        displayableNames.put("d", "Engineering ANSI D");
        displayableNames.put("e", "Engineering ANSI E");
        displayableNames.put("arch-a", "Architectural A");
        displayableNames.put("arch-b", "Architectural B");
        displayableNames.put("arch-c", "Architectural C");
        displayableNames.put("arch-d", "Architectural D");
        displayableNames.put("arch-e", "Architectural E");
        displayableNames.put("japanese-postcard", "Japanese Postcard");
        displayableNames.put("oufuko-postcard", "Oufuko Postcard");
        displayableNames.put("italian-envelope", "Italian Envelope");
        displayableNames.put("personal-envelope", "Personal Envelope");
        displayableNames.put("na-number-11-envelope", "#11 Envelope");
        displayableNames.put("na-number-12-envelope", "#12 Envelope");
        displayableNames.put("na-number-14-envelope", "#14 Envelope");
        displayableNames.put("na-10x13-envelope", "10\"x13\" Envelope");
        displayableNames.put("na-9x12-envelope", "9\"x12\" Envelope");
        displayableNames.put("na-number-10-envelope", "#10 Envelope");
        displayableNames.put("na-7x9-envelope", "7\"x9\" Envelope");
        displayableNames.put("na-9x11-envelope", "9\"x11\" Envelope");
        displayableNames.put("na-10x14-envelope", "10\"x14\" Envelope");
        displayableNames.put("na-number-9-envelope", "#9 Envelope");
        displayableNames.put("na-6x9-envelope", "6\"x9\" Envelope");
        displayableNames.put("na-10x15-envelope", "10\"x15\" Envelope");
        displayableNames.put("monarch-envelope", "Monarch Envelope");
    }

}
