package net.sf.openrocket.file;

import net.sf.openrocket.rocketcomponent.FinSet;

/**
 */
public final class TipShapeCode {

    /**
     * Convert a Rocksim tip shape to an OpenRocket CrossSection.
     *
     * @param tipShape the tip shape code from Rocksim
     *
     * @return a CrossSection instance
     */
    public static FinSet.CrossSection convertTipShapeCode (int tipShape) {
        switch (tipShape) {
            case 0:
                return FinSet.CrossSection.SQUARE;
            case 1:
                return FinSet.CrossSection.ROUNDED;
            case 2:
                return FinSet.CrossSection.AIRFOIL;
            default:
                return FinSet.CrossSection.SQUARE;
        }
    }

    public static int convertTipShapeCode (FinSet.CrossSection cs) {
        if (FinSet.CrossSection.ROUNDED.equals(cs)) {
            return 1;
        }
        if (FinSet.CrossSection.AIRFOIL.equals(cs)) {
            return 2;
        }
        return 0;
    }
}
