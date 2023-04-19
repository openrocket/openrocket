package net.sf.openrocket.utils;

import java.text.DecimalFormat;

/**
 * This is a helper class for generating more elaborate DecimalFormats.
 * @author SiboVanGool <sibo.vangool@hotmail.com>
 */
public abstract class DecimalFormatter {
    /**
     * Return a decimal formatter for displaying {value} with {decimals} decimals. If {value} is smaller than the second
     * to last least significant decimal, then value will be formatted to show the 2 most significant decimals of {value}.
     * E.g. value = 0.00345, decimals = 2 => smallest value = 0.01; value < 0.1 => format the 2 most significant decimals
     * of value => value will be displayed as 0.0035.
     * @param value number to be formatted
     * @param decimals number of decimals to round off to
     * @param display_zeros flag to check whether to display trailing zeros or not
     *      value true: display e.g. 8.00, value false, display e.g. 8
     * @return decimal format
     */
    public static DecimalFormat df (double value, int decimals, boolean display_zeros) {
        if (decimals <= 0) {
            return new DecimalFormat("#");
        }
        value = Math.abs(value);
        String pattern = "#";
        if (display_zeros) {
            pattern = "0";
        }
        if (value < Math.pow(0.1, decimals-1) && value > 0) {
            int decimals_value = (int) Math.floor(Math.log10(1/value));
            decimals = decimals_value + decimals;
        }
        return new DecimalFormat("#." + new String(new char[decimals]).replace("\0", pattern));
    }
}
