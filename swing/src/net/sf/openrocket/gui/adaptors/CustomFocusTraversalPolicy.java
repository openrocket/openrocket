package net.sf.openrocket.gui.adaptors;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.LinkedList;
import java.util.List;

/**
 * Custom adapter class for focus traversal, based on a given order of GUI components
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
    private final List<Component> order;

    /**
     * @param order the order of components to traverse
     */
    public CustomFocusTraversalPolicy(List<Component> order) {
        this.order = new LinkedList<>(order);
    }

    public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
        // Get the next component
        int idx = (order.indexOf(aComponent) + 1) % order.size();

        // If the next component is disabled, loop for the next enabled one
        int count = 0;
        while (!order.get(idx).isEnabled() || !order.get(idx).isShowing() || !order.get(idx).isVisible()) {
            idx = (idx + 1) % order.size();
            count++;
            // No active component found, fall back to the original component
            if (count == order.size())
                return aComponent;
        }
        return order.get(idx);
    }

    public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
        int idx = order.indexOf(aComponent) - 1;
        if (idx < 0) {
            idx = order.size() - 1;
        }
        int count = 0;
        while (!order.get(idx).isEnabled() || !order.get(idx).isShowing() || !order.get(idx).isVisible()) {
            idx = (idx - 1) % order.size();
            count++;
            if (count == order.size())
                return aComponent;
        }
        return order.get(idx);
    }

    public Component getDefaultComponent(Container focusCycleRoot) {
        return getFirstComponent(focusCycleRoot);
    }

    public Component getLastComponent(Container focusCycleRoot) {
        int idx = order.size() - 1;
        if ((order.get(idx).isEnabled() && order.get(idx).isShowing() && order.get(idx).isVisible())) {
            return order.get(idx);
        }
        return getComponentBefore(focusCycleRoot, order.get(idx));
    }

    public Component getFirstComponent(Container focusCycleRoot) {
        int idx = 0;
        if ((order.get(idx).isEnabled() && order.get(idx).isShowing() && order.get(idx).isVisible())) {
            return order.get(idx);
        }
        return getComponentAfter(focusCycleRoot, order.get(idx));
    }
}
