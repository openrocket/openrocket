/*
 * PrintUnit.java
 */
package net.sf.openrocket.gui.print;

/**
 * Utilities for print units.
 */
public enum PrintUnit {
    INCHES {
        public double toInches(double d) { return d; }
        public double toMillis(double d) { return d/INCHES_PER_MM; }
        public double toCentis(double d) { return d/(INCHES_PER_MM*TEN); }
        public double toMeters(double d) { return d/(INCHES_PER_MM*TEN*TEN*TEN); }
        public long   toPoints(double d) { return (long)(d * POINTS_PER_INCH); }
        public double convert(double d, PrintUnit u) { return u.toInches(d); }
    },
    MILLIMETERS {
        public double toInches(double d) { return d * INCHES_PER_MM; }
        public double toMillis(double d) { return d; }
        public double toCentis(double d) { return d/TEN; }
        public double toMeters(double d) { return d/(TEN*TEN*TEN); }
        public long   toPoints(double d) { return INCHES.toPoints(toInches(d)); }
        public double convert(double d, PrintUnit u) { return u.toMillis(d); }
    },
    CENTIMETERS {
        public double toInches(double d) { return d * INCHES_PER_MM * TEN; }
        public double toMillis(double d) { return d * TEN; }
        public double toCentis(double d) { return d; }
        public double toMeters(double d) { return d/(TEN*TEN); }
        public long   toPoints(double d) { return INCHES.toPoints(toInches(d)); }
        public double convert(double d, PrintUnit u) { return u.toCentis(d); }
    },
    METERS {
        public double toInches(double d) { return d * INCHES_PER_MM * TEN * TEN * TEN; }
        public double toMillis(double d) { return d * TEN * TEN * TEN; }
        public double toCentis(double d) { return d * TEN * TEN; }
        public double toMeters(double d) { return d; }
        public long   toPoints(double d) { return INCHES.toPoints(toInches(d)); }
        public double convert(double d, PrintUnit u) { return u.toMeters(d); }
    },
    POINTS {
        public double toInches(double d) { return d/POINTS_PER_INCH; }
        public double toMillis(double d) { return d/(POINTS_PER_INCH * INCHES_PER_MM); }
        public double toCentis(double d) { return toMillis(d)/TEN; }
        public double toMeters(double d) { return toMillis(d)/(TEN*TEN*TEN); }
        public long   toPoints(double d) { return (long)d; }
        public double convert(double d, PrintUnit u) { return u.toPoints(d); }
    };

    // Handy constants for conversion methods
    public static final double INCHES_PER_MM = 0.0393700787d;
    public static final double MM_PER_INCH = 1.0d/INCHES_PER_MM;
    public static final long TEN = 10;
    /**
     * PPI is Postscript Point and is a standard of 72.  Java2D also uses this internally as a pixel-per-inch, so pixels
     * and points are for the most part interchangeable (unless the defaults are changed), which makes translating
     * between the screen and a print job easier.
     * 
     * Not to be confused with Dots-Per-Inch, which is printer and print mode dependent.
     */
    public static final int POINTS_PER_INCH = 72;

    // To maintain full signature compatibility with 1.5, and to improve the
    // clarity of the generated javadoc (see 6287639: Abstract methods in
    // enum classes should not be listed as abstract), method convert
    // etc. are not declared abstract but otherwise act as abstract methods.

    /**
     * Convert the given length in the given unit to this
     * unit.  Conversions from finer to coarser granularities
     * truncate, so may lose precision.
     *
     * <p>For example, to convert 10 inches to point, use:
     * <tt>PrintUnit.POINTS.convert(10L, PrintUnit.INCHES)</tt>
     *
     * @param sourceLength the length in the given <tt>sourceUnit</tt>
     * @param sourceUnit the unit of the <tt>sourceDuration</tt> argument
     *
     * @return the converted length in this unit,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     */
    public double convert(double sourceLength, PrintUnit sourceUnit) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to <tt>INCHES.convert(length, this)</tt>.
     *
     * @param length  the length
     *
     * @return the converted length,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     * @see #convert
     */
    public double toInches(double length) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to <tt>MILLIMETERS.convert(length, this)</tt>.
     *
     * @param length  the length
     *
     * @return the converted length,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     * @see #convert
     */
    public double toMillis(double length) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to <tt>CENTIMETERS.convert(length, this)</tt>.
     *
     * @param length  the length
     *
     * @return the converted length,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     * @see #convert
     */
    public double toCentis(double length) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to <tt>METERS.convert(length, this)</tt>.
     *
     * @param length  the length
     *
     * @return the converted length,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     * @see #convert
     */
    public double toMeters(double length) {
        throw new AbstractMethodError();
    }

    /**
     * Equivalent to <tt>POINTS.convert(length, this)</tt>.
     *
     * @param length  the length
     *
     * @return the converted length,
     * or <tt>Long.MIN_VALUE</tt> if conversion would negatively
     * overflow, or <tt>Long.MAX_VALUE</tt> if it would positively overflow.
     * @see #convert
     */
    public long toPoints(double length) {
        throw new AbstractMethodError();
    }

}