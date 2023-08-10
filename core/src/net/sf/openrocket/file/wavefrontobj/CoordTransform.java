package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;

/**
 * Interface for classes that can convert coordinates from the OpenRocket coordinate system to a custom OBJ coordinate system.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public interface CoordTransform {
    FloatTuple convertToOBJCoord(double x, double y, double z);
}
