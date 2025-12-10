package info.openrocket.swing.gui.figure3d.ui;

/**
 * Interface for components that need to be notified when the HUD should be updated.
 *
 * <p>This interface allows the HUDPanel to notify GL scene panels of needed updates
 * without being tightly coupled to a specific panel implementation.</p>
 */
public interface HUDUpdateListener {

    /**
     * Called when the HUD content has changed and needs to be re-rendered.
     */
    void markHudForUpdate();
}
