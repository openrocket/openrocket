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
    private boolean separateInsideOutside = false;  // Flag for separate inside and outside appearance
    private boolean edgesSameAsInside = false;      // Flag for setting the edge appearance to the inside (true) or outside (false) appearance

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
        for (RocketComponent listener : component.configListeners) {
            if (listener instanceof InsideColorComponent) {
                ((InsideColorComponent) listener).getInsideColorComponentHandler().setInsideAppearance(appearance);
            }
        }

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
        for (RocketComponent listener : component.configListeners) {
            if (listener instanceof InsideColorComponent) {
                ((InsideColorComponent) listener).getInsideColorComponentHandler().setEdgesSameAsInside(newState);
            }
        }

        this.edgesSameAsInside = newState;
    }

    /**
     * Checks whether the component should use a separate appearance for the inside and outside.
     *
     * @return  true if a separate inside and outside appearance should be used,
     *          false if the inside should have the outside appearance
     */
    public boolean isSeparateInsideOutside() {
        return this.separateInsideOutside;
    }

    /**
     * Sets the new state for separateInsideOutside to newState
     *
     * @param newState new separateInsideOutside value
     */
    public void setSeparateInsideOutside(boolean newState) {
        for (RocketComponent listener : component.configListeners) {
            if (listener instanceof InsideColorComponent) {
                ((InsideColorComponent) listener).getInsideColorComponentHandler().setSeparateInsideOutside(newState);
            }
        }

        this.separateInsideOutside = newState;
    }

    public void copyFrom(InsideColorComponentHandler src) {
        this.insideAppearance = src.insideAppearance;
        this.separateInsideOutside = src.separateInsideOutside;
        this.edgesSameAsInside = src.edgesSameAsInside;
    }
}
