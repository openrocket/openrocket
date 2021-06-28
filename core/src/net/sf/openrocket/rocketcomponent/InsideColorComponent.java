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
     * Get the realistic inside appearance of this component.
     *  <code>null</code> = use the default for this material
     *
     * @return The component's realistic inner appearance, or <code>null</code>
     */
    Appearance getInsideAppearance();

    /**
     * Set the realistic inside appearance of this component.
     * Use <code>null</code> for default.
     *
     * @param appearance the inner appearance to be set
     */
     void setInsideAppearance(Appearance appearance);

    /**
     * Checks whether the component uses for the edges the same appearance as the inside (return true) or as the
     * outside (return false)
     *
     * @return  true if edges should use the same appearance as the inside,
     *          false if edges should use the same appearance as the outside
     */
     boolean isEdgesSameAsInside();

    /**
     * Sets the new state for edgesUseInsideAppearance to newState
     *
     * @param newState new edgesUseInsideAppearance value
     */
    void setEdgesSameAsInside(boolean newState);

    /**
     * Checks whether the component should use the same appearance for the inside as the outside (return true) or as the
     * outside (return false)
     *
     * @return  true if edges should use the same appearance as the inside,
     *          false if edges should use the same appearance as the outside
     */
    boolean isInsideSameAsOutside();

    /**
     * Sets the new state for insideSameAsOutside to newState
     *
     * @param newState new edgesUseInsideAppearance value
     */
    void setInsideSameAsOutside(boolean newState);
}
