package info.openrocket.core.util;

import info.openrocket.core.rocketcomponent.MassObject;

public class RocketComponentUtils {
    /**
     * Returns the radius of a mass object at a given z coordinate to be used for 3D
     * rendering.
     * This has no real physical meaning.
     * 
     * @param o the mass object
     * @param z the z coordinate
     * @return the radius of the mass object at the given z coordinate
     */
    public static double getMassObjectRadius(MassObject o, double z) {
        double arc = getMassObjectArcHeight(o);
        double r = o.getRadius();
        if (z == 0 || z == o.getLength())
            return 0;
        if (z < arc) {
            double zz = z - arc;
            return (r - arc) + Math.sqrt(arc * arc - zz * zz);
        }
        if (z > o.getLength() - arc) {
            double zz = (z - o.getLength() + arc);
            return (r - arc) + Math.sqrt(arc * arc - zz * zz);
        }
        return o.getRadius();
    }

    public static double getMassObjectArcHeight(MassObject o) {
        return Math.min(o.getLength(), 2 * o.getRadius()) * 0.35f;
    }
}
