package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.util.StateChangeListener;

import java.util.EventObject;

/**
 * This component handles the necessary functionalities of an InsideColorComponent.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class InsideColorComponentHandler {
    private final RocketComponent component;
    private Appearance insideAppearance = null;
    private boolean insideSameAsOutside = true;
    private boolean edgesSameAsInside = true;

    public InsideColorComponentHandler(RocketComponent component) {
        this.component = component;
    }

    /**
     * Get the realistic inside appearance of this component.
     *  <code>null</code> = use the default for this material
     *
     * @return The component's realistic inner appearance, or <code>null</code>
     */
    public Appearance getInsideAppearance() {
        return this.insideAppearance;
    }

    /**
     * Set the realistic inside appearance of this component.
     * Use <code>null</code> for default.
     *
     * @param appearance the inner appearance to be set
     */
    public void setInsideAppearance(Appearance appearance) {
        this.insideAppearance = appearance;
        if (this.insideAppearance != null) {
            Decal d = this.insideAppearance.getTexture();
            if (d != null) {
                d.getImage().addChangeListener(new StateChangeListener() {

                    @Override
                    public void stateChanged(EventObject e) {
                        component.fireComponentChangeEvent(ComponentChangeEvent.TEXTURE_CHANGE);
                    }

                });
            }
        }
        component.fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
    }

    /**
     * Checks whether the component uses for the edges the same appearance as the inside (return true) or as the
     * outside (return false)
     *
     * @return  true if edges should use the same appearance as the inside,
     *          false if edges should use the same appearance as the outside
     */
    public boolean isEdgesSameAsInside() {
        return this.edgesSameAsInside;
    }

    /**
     * Sets the new state for edgesUseInsideAppearance to newState
     *
     * @param newState new edgesUseInsideAppearance value
     */
    public void setEdgesSameAsInside(boolean newState) {
        this.edgesSameAsInside = newState;
    }

    /**
     * Checks whether the component should use the same appearance for the inside as the outside (return true) or as the
     * outside (return false)
     *
     * @return  true if edges should use the same appearance as the inside,
     *          false if edges should use the same appearance as the outside
     */
    public boolean isInsideSameAsOutside() {
        return this.insideSameAsOutside;
    }

    /**
     * Sets the new state for insideSameAsOutside to newState
     *
     * @param newState new edgesUseInsideAppearance value
     */
    public void setInsideSameAsOutside(boolean newState) {
        this.insideSameAsOutside = newState;
    }
}
