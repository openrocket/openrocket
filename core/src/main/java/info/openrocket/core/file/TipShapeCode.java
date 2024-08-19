package info.openrocket.core.file;

import info.openrocket.core.rocketcomponent.FinSet;

/**
 */
public final class TipShapeCode {

    /**
     * Convert a RockSim tip shape to an OpenRocket CrossSection.
     *
     * @param tipShape the tip shape code from RockSim
     *
     * @return a CrossSection instance
     */
    public static FinSet.CrossSection convertTipShapeCode(int tipShape) {
		return switch (tipShape) {
			case 0 -> FinSet.CrossSection.SQUARE;
			case 1 -> FinSet.CrossSection.ROUNDED;
			case 2 -> FinSet.CrossSection.AIRFOIL;
			default -> FinSet.CrossSection.SQUARE;
		};
    }

    public static int convertTipShapeCode(FinSet.CrossSection cs) {
        if (FinSet.CrossSection.ROUNDED.equals(cs)) {
            return 1;
        }
        if (FinSet.CrossSection.AIRFOIL.equals(cs)) {
            return 2;
        }
        return 0;
    }
}
