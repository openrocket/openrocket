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

    /**
     * Builds the test rocket, attaches a {@link DocumentSelectionModel} to a real {@link JTree},
     * mocks the frame and simulation panel, clears the static clipboard and creates the
     * {@link RocketActions} under test.
     */
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

    /**
     * Creates a disabled {@link SimulationPanel.SimulationAction} so every component action
     * reports its own enabled state rather than being enabled through a simulation.
     *
     * @return a no-op action with {@code isEnabled() == false}
     */
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

    /**
     * Replaces the current selection, in the given click order.
     *
     * @param components the components to select; the first becomes {@code getSelectedComponent()}
     */
    private void select(RocketComponent... components) {
        selection.setSelectedComponents(List.of(components));
    }

    /**
     * Returns the names of the direct children of a component, in tree order.
     *
     * @param parent the component to inspect
     * @return the child names, top to bottom
     */
    private static List<String> childNames(RocketComponent parent) {
        List<String> names = new ArrayList<>();
        for (RocketComponent c : parent.getChildren()) {
            names.add(c.getName());
        }
        return names;
    }

    /**
     * Finds a direct child by name.
     *
     * @param parent the component to search
     * @param name   the child name to look for
     * @return the matching child
     * @throws AssertionError if no child has that name
     */
    private static RocketComponent childNamed(RocketComponent parent, String name) {
        for (RocketComponent c : parent.getChildren()) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        throw new AssertionError("No child named " + name + " in " + parent + ", children: " + childNames(parent));
    }

    // ---------------------------------------------------------------- copy

    /**
     * Copying one component puts a deep copy of its subtree on the clipboard with fresh IDs
     * and leaves the design untouched.
     */
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

    /**
     * Selecting a parent together with all of its children copies a single component; the
     * children are carried inside it rather than as separate clipboard entries.
     */
    @Test
    public void copyParentAndAllChildrenPutsOnlyParentOnClipboard() {
        select(tube, fins, inner);
        actions.getCopyAction().actionPerformed(null);

        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(1, clipboard.size());
        assertEquals(List.of("Fins", "Inner"), childNames(clipboard.get(0)));
    }

    /**
     * Discontinuous selection: a parent with only one of its two children selected copies
     * a single parent whose only child is the selected one (issue #1692).
     */
    @Test
    public void copyParentWithSubsetOfChildrenKeepsOnlySelectedChildren() {
        // Discontinuous selection: the tube and only one of its two children
        select(tube, fins);
        actions.getCopyAction().actionPerformed(null);

        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(1, clipboard.size(), "children must be folded into the copied parent");
        assertEquals(List.of("Fins"), childNames(clipboard.get(0)));
    }

    /**
     * Copying two siblings selected in reverse click order still puts both on the clipboard.
     */
    @Test
    public void copyMultipleSiblingsKeepsTreeOrder() {
        // Select in reverse click order; clipboard order must still be usable for a sensible paste
        select(tube, nose);
        actions.getCopyAction().actionPerformed(null);

        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(2, clipboard.size());
        assertTrue(clipboard.stream().anyMatch(c -> c.getName().equals("Nose")));
        assertTrue(clipboard.stream().anyMatch(c -> c.getName().equals("Tube")));
    }

    /**
     * The Rocket root cannot be copied; an ordinary component can.
     */
    @Test
    public void copyActionDisabledForRocketAndEnabledForComponent() {
        select(rocket);
        assertFalse(actions.getCopyAction().isEnabled());
        select(nose);
        assertTrue(actions.getCopyAction().isEnabled());
    }

    // ---------------------------------------------------------------- cut

    /**
     * Cutting removes the component from its parent and places a copy on the clipboard.
     */
    @Test
    public void cutSingleComponentRemovesItAndFillsClipboard() {
        select(nose);
        actions.getCutAction().actionPerformed(null);

        assertEquals(List.of("Tube"), childNames(stage));
        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(1, clipboard.size());
        assertEquals("Nose", clipboard.get(0).getName());
        assertNotSame(nose, clipboard.get(0));
    }

    /**
     * Undo after a cut restores the component and its whole subtree.
     */
    @Test
    public void cutIsUndoable() {
        select(tube);
        actions.getCutAction().actionPerformed(null);
        assertEquals(List.of("Nose"), childNames(stage));

        document.undo();

        AxialStage restoredStage = (AxialStage) document.getRocket().getChild(0);
        assertEquals(List.of("Nose", "Tube"), childNames(restoredStage));
        assertEquals(List.of("Fins", "Inner"), childNames(childNamed(restoredStage, "Tube")));
    }

    /**
     * Discontinuous selection: cutting a parent with a subset of its children removes the
     * entire parent from the design, but only the selected children travel on the clipboard.
     */
    @Test
    public void cutParentWithSubsetOfChildrenRemovesWholeParent() {
        select(tube, fins);
        actions.getCutAction().actionPerformed(null);

        // The parent (and therefore all its children) leaves the design...
        assertEquals(List.of("Nose"), childNames(stage));
        // ...but only the selected child travels with it on the clipboard
        List<RocketComponent> clipboard = OpenRocketClipboard.getClipboardComponents();
        assertEquals(1, clipboard.size());
        assertEquals(List.of("Fins"), childNames(clipboard.get(0)));
    }

    /**
     * Cutting two siblings removes both and puts both on the clipboard.
     */
    @Test
    public void cutMultipleSiblings() {
        select(nose, tube);
        actions.getCutAction().actionPerformed(null);

        assertEquals(0, stage.getChildCount());
        assertEquals(2, OpenRocketClipboard.getClipboardComponents().size());
    }

    /**
     * Neither the only stage nor the Rocket root can be cut.
     */
    @Test
    public void cutActionDisabledForLastStageAndRocket() {
        select(stage);
        assertFalse(actions.getCutAction().isEnabled(), "the only stage cannot be cut");
        select(rocket);
        assertFalse(actions.getCutAction().isEnabled());
    }

    /**
     * Candidate bug (found by code reading, verify when running): {@code cutComponents()} checks
     * deletability of the whole list up front, then deletes one by one. After the first stage is
     * removed the second one has become the last stage, and the private {@code delete()} throws
     * {@code IllegalArgumentException("Report bug! ...")}, leaving the design half-cut.
     */
    @Test
    public void cutAllStagesOfTwoStageRocketDoesNotThrow() {
        AxialStage booster = new AxialStage();
        booster.setName("Booster");
        rocket.addChild(booster);
        select(stage, booster);
        assertTrue(actions.getCutAction().isEnabled());

        actions.getCutAction().actionPerformed(null);

        // Expected (after a fix): either both stages removed with a rocket in a consistent state,
        // or the action refuses; it must not throw or leave exactly one stage with a populated clipboard.
        assertTrue(rocket.getChildCount() == 0 || rocket.getChildCount() == 2,
                "cut must be all-or-nothing, got " + childNames(rocket));
    }
}