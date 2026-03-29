package info.openrocket.swing.gui.dialogs.optimization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.junit.jupiter.api.Test;

import info.openrocket.core.optimization.rocketoptimization.SimulationModifier;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Regression test for a NullPointerException in {@link SimulationModifierTree.ComponentModifierTreeRenderer}
 * when {@link GUIUtil#setNullModels} replaces the tree model with a new {@link DefaultTreeModel} whose
 * root node has a null user object, causing the renderer to call {@code object.getClass()} on null.
 */
public class SimulationModifierTreeTest extends BaseTestCase {

	/**
	 * Reproduces the NPE in {@code ComponentModifierTreeRenderer.getTreeCellRendererComponent}:
	 * when the dialog is closed, {@code GUIUtil.setNullModels} replaces the tree model with
	 * {@code new DefaultTreeModel(new DefaultMutableTreeNode())} — a root node with a null user
	 * object. The renderer then calls {@code object.getClass()} on null, throwing NPE.
	 */
	@Test
	public void testSetNullModelsDoesNotThrowNPEOnNullUserObject() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			Rocket rocket = new Rocket();
			Map<Object, List<SimulationModifier>> modifiers = Collections.emptyMap();

			SimulationModifierTree tree = new SimulationModifierTree(rocket, modifiers, Collections.emptyList());

			// Simulate what GUIUtil.setNullModels does when the optimization dialog is closed:
			// it replaces the model with a new DefaultTreeModel whose root has a null user object.
			// The renderer must not throw NullPointerException when rendering this node.
			assertDoesNotThrow(
					() -> tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode())),
					"Setting a null-user-object model should not throw NullPointerException in the renderer"
			);
		});
	}
}
