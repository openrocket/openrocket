package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Behavioural tests for the component editing actions in {@link RocketActions}:
 * move up/down, cut, copy, paste and duplicate. Covers single selection,
 * multi-component selection, and discontinuous selection where only a subset
 * of a component's children is selected (GitHub issue #1692).
 */

public class RocketActionsEditTest extends BaseTestCase {
    /*
     * Test rocket, built in setUp():
     *
     *   Rocket
     *   └── Sustainer (stage)
     *       ├── Nose cone            [index 0]
     *       └── Body tube            [index 1]
     *           ├── Fin set          [index 0]
     *           └── Inner tube       [index 1]
     */

    private OpenRocketDocument document;
    private Rocket rocket;
    private AxialStage stage;
    private NoseCone nose;
    private BodyTube tube;
    private TrapezoidFinSet fins;
    private InnerTube inner;

    private DocumentSelectionModel selection;
    private RocketActions actions;

    @BeforeEach
    public void setUpRocket() {
        document = OpenRocketDocumentFactory.createNewRocket();
        rocket = document.getRocket();
        stage = (AxialStage) rocket.getChild(0);

        nose = new NoseCone();
        nose.setName("Nose");
        tube = new BodyTube();
        tube.setName("Tube");
        fins = new TrapezoidFinSet();
        fins.setName("Fins");
        inner = new InnerTube();
        inner.setName("Inner");

        stage.addChild(nose);
        stage.addChild(tube);
        tube.addChild(fins);
        tube.addChild(inner);

        JTree tree = new JTree();
        tree.setModel(new ComponentTreeModel(rocket, tree));
        tree.setSelectionModel(new DefaultTreeSelectionModel());
        selection = new DocumentSelectionModel(document);
        selection.attachComponentTreeSelectionModel(tree.getSelectionModel());

        BasicFrame frame = mock(BasicFrame.class);
        SimulationPanel simulationPanel = mock(SimulationPanel.class);
        when(simulationPanel.getCutSimulationAction()).thenReturn(disabledSimulationAction());
        when(simulationPanel.getCopySimulationAction()).thenReturn(disabledSimulationAction());
        when(simulationPanel.getPasteSimulationAction()).thenReturn(disabledSimulationAction());
        when(simulationPanel.getDuplicateSimulationAction()).thenReturn(disabledSimulationAction());
        when(simulationPanel.getEditSimulationAction()).thenReturn(disabledSimulationAction());
        when(simulationPanel.getDeleteSimulationAction()).thenReturn(disabledSimulationAction());

        // The clipboard is a static singleton: start every test from an empty one.
        OpenRocketClipboard.setClipboard(new ArrayList<>());

        actions = new RocketActions(document, selection, frame, simulationPanel);
    }

    private static SimulationPanel.SimulationAction disabledSimulationAction() {
        SimulationPanel.SimulationAction action = new SimulationPanel.SimulationAction() {
            @Override
            public void updateEnabledState() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        action.setEnabled(false);
        return action;
    }

    private void select(RocketComponent... components) {
        selection.setSelectedComponents(List.of(components));
    }

    private static List<String> childNames(RocketComponent parent) {
        List<String> names = new ArrayList<>();
        for (RocketComponent c : parent.getChildren()) {
            names.add(c.getName());
        }
        return names;
    }

    private static RocketComponent childNamed(RocketComponent parent, String name) {
        for (RocketComponent c : parent.getChildren()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        throw new AssertionError("No child named " + name + " in " + parent + ", children: " + childNames(parent));
    }

    // ---------------------------------------------------------------- copy

    @Test
    public void copySingleComponentDeepCopiesSubtreeWithNewIds() {
        select(tube);
        actions.getCopyAction().actionPerformed(null);

        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(1, clipboard.size());
        RocketComponent copiedTube = clipboard.get(0);
        assertNotSame(tube, copiedTube);
        assertNotEquals(tube.getID(), copiedTube.getID());
        assertEquals(List.of("Fins", "Inner"), childNames(copiedTube));
        assertNotEquals(fins.getID(), childNamed(copiedTube, "Fins").getID());

        // Copy must not modify the design
        assertEquals(List.of("Nose", "Tube"), childNames(stage));
        assertEquals(List.of("Fins", "Inner"), childNames(tube));
    }
}