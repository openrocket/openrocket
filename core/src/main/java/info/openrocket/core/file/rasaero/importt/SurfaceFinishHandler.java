package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

/**
 * Applies the RASAero surface finish to all components.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class SurfaceFinishHandler {
    public static void setSurfaceFinishes(Rocket rocket, String finish, WarningSet warnings) {
        ExternalComponent.Finish surfaceFinish = RASAeroCommonConstants.RASAERO_TO_OPENROCKET_SURFACE(finish, warnings);
        for (RocketComponent component : rocket) {
            if (component instanceof ExternalComponent) {
                ((ExternalComponent) component).setFinish(surfaceFinish);
            }
        }
    }
}
