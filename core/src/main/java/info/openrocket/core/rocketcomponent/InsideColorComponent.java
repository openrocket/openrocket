package info.openrocket.core.rocketcomponent;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.StateChangeListener;

import java.util.EventObject;

/**
 * This is a marker interface which, if applied to a component, will mark that
 * component as having the possibility to
 * have a different inside and outside color. This will cause the appearance
 * editor of that component to have a separate
 * section for the inside and outside color and will consequently also render
 * the inside and outside of that component
 * (in a 3D figure) differently.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public interface InsideColorComponent {
    /**
     * @return the InsideColorComponentHandler
     */
    InsideColorComponentHandler getInsideColorComponentHandler();

    void setInsideColorComponentHandler(InsideColorComponentHandler handler);
}
