package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.util.StateChangeListener;

import java.util.EventObject;

/**
 * This is a marker interface which, if applied to a component, will mark that component as having the possibility to
 * have a different inside and outside color. This will cause the appearance editor of that component to have a separate
 * section for the inside and outside color and will consequently also render the inside and outside of that component
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
