package info.openrocket.swing.gui.adaptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.junit.jupiter.api.Test;

class CustomFocusTraversalPolicyTest {

    private static class StubComponent extends JComponent {
        private boolean showing = true;

        StubComponent(String name) {
            setName(name);
        }

        @Override
        public boolean isShowing() {
            return showing;
        }

        void setShowing(boolean showing) {
            this.showing = showing;
        }
    }

    private static CustomFocusTraversalPolicy policyWith(Component... components) {
        List<Component> order = Arrays.asList(components);
        return new CustomFocusTraversalPolicy(order);
    }

    @Test
    void getComponentAfterSkipsDisabledEntries() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        StubComponent third = new StubComponent("third");
        second.setEnabled(false);

        CustomFocusTraversalPolicy policy = policyWith(first, second, third);

        Component next = policy.getComponentAfter((Container) null, first);

        assertEquals(third, next);
    }

    @Test
    void getComponentBeforeSkipsInvisibleEntries() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        StubComponent third = new StubComponent("third");
        second.setVisible(false);

        CustomFocusTraversalPolicy policy = policyWith(first, second, third);

        Component previous = policy.getComponentBefore(null, third);

        assertEquals(first, previous);
    }

    @Test
    void firstComponentFallsBackToNextVisibleEntry() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        first.setShowing(false);

        CustomFocusTraversalPolicy policy = policyWith(first, second);

        Component component = policy.getFirstComponent(null);

        assertEquals(second, component);
    }

    @Test
    void lastComponentFallsBackToPreviousVisibleEntry() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        second.setEnabled(false);

        CustomFocusTraversalPolicy policy = policyWith(first, second);

        Component component = policy.getLastComponent(null);

        assertEquals(first, component);
    }

    @Test
    void getComponentAfterReturnsOriginalWhenNoActiveComponents() {
        StubComponent only = new StubComponent("only");
        only.setEnabled(false);
        only.setShowing(false);
        only.setVisible(false);

        CustomFocusTraversalPolicy policy = policyWith(only);

        Component result = policy.getComponentAfter(null, only);

        assertEquals(only, result);
    }

    @Test
    void getComponentBeforeWrapsFromFirstToLast() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        CustomFocusTraversalPolicy policy = policyWith(first, second);

        Component previous = policy.getComponentBefore(null, first);

        assertEquals(second, previous);
    }

    @Test
    void getComponentBeforeWrapsPastInactiveFirstEntry() {
        StubComponent first = new StubComponent("first");
        StubComponent second = new StubComponent("second");
        StubComponent third = new StubComponent("third");
        first.setEnabled(false);
        CustomFocusTraversalPolicy policy = policyWith(first, second, third);

        Component previous = policy.getComponentBefore(null, second);

        assertEquals(third, previous);
    }

    @Test
    void emptyOrderHasSafeFallbacks() {
        StubComponent component = new StubComponent("component");
        CustomFocusTraversalPolicy policy = policyWith();

        assertSame(component, policy.getComponentAfter(null, component));
        assertSame(component, policy.getComponentBefore(null, component));
        assertNull(policy.getFirstComponent(null));
        assertNull(policy.getLastComponent(null));
        assertNull(policy.getDefaultComponent(null));
    }

    @Test
    void duplicateEntriesDoNotBreakTraversal() {
        StubComponent first = new StubComponent("first");
        StubComponent duplicate = new StubComponent("duplicate");
        CustomFocusTraversalPolicy policy = policyWith(first, duplicate, duplicate);

        Component after = policy.getComponentAfter(null, duplicate);

        assertEquals(duplicate, after);
    }
}
