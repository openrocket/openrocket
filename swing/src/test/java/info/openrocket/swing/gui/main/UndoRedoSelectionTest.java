package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;
import info.openrocket.swing.util.BaseTestCase;

public class UndoRedoSelectionTest extends BaseTestCase {

	@Test
	public void undoAddedComponentSelectsParent() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();

		JTree tree = new JTree();
		tree.setModel(new ComponentTreeModel(document.getRocket(), tree));

		DocumentSelectionModel selectionModel = new DocumentSelectionModel(document);
		tree.setSelectionModel(new DefaultTreeSelectionModel());
		selectionModel.attachComponentTreeSelectionModel(tree.getSelectionModel());

		AxialStage sustainer = (AxialStage) document.getRocket().getChild(0);
		UUID sustainerId = sustainer.getID();

		document.addUndoPosition("Add body tube");
		BodyTube bodyTube = new BodyTube();
		sustainer.addChild(bodyTube);

		selectionModel.setSelectedComponent(bodyTube);

		document.undo();

		assertNotNull(selectionModel.getSelectedComponent());
		assertEquals(sustainerId, selectionModel.getSelectedComponent().getID());
	}

	@Test
	public void undoAddedChildComponentSelectsParent() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();

		JTree tree = new JTree();
		tree.setModel(new ComponentTreeModel(document.getRocket(), tree));

		DocumentSelectionModel selectionModel = new DocumentSelectionModel(document);
		tree.setSelectionModel(new DefaultTreeSelectionModel());
		selectionModel.attachComponentTreeSelectionModel(tree.getSelectionModel());

		AxialStage sustainer = (AxialStage) document.getRocket().getChild(0);

		document.addUndoPosition("Add body tube");
		BodyTube bodyTube = new BodyTube();
		sustainer.addChild(bodyTube);
		UUID bodyTubeId = bodyTube.getID();

		document.addUndoPosition("Add inner tube");
		InnerTube innerTube = new InnerTube();
		bodyTube.addChild(innerTube);

		selectionModel.setSelectedComponent(innerTube);

		document.undo();

		assertNotNull(selectionModel.getSelectedComponent());
		assertEquals(bodyTubeId, selectionModel.getSelectedComponent().getID());
	}
}
