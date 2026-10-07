package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.rocketcomponent.DesignType;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

public class OpenRocketDocumentUndoRedoTest extends BaseTestCase {

	/**
	 * The design info stored on the Rocket itself (designer, revision, design type, kit name) has to survive an
	 * undo/redo round trip just like the component fields do.
	 */
	@Test
	public void undoRedoDesignInfo_restoresTheRocketsOwnFields() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);

		rocket.setName("My Rocket");
		rocket.setDesigner("Original designer");
		rocket.setRevision("Revision 1");
		rocket.setDesignType(DesignType.CLONE_KIT);
		rocket.setKitName("Original kit");
		document.clearUndo();

		document.addUndoPosition("Modify Rocket");
		rocket.setName("Renamed");
		rocket.setDesigner("Someone else");
		rocket.setRevision("Revision 2");
		rocket.setDesignType(DesignType.COMMERCIAL_KIT);
		rocket.setKitName("Other kit");

		document.undo();

		assertEquals("My Rocket", rocket.getName());
		assertEquals("Original designer", rocket.getDesigner());
		assertEquals("Revision 1", rocket.getRevision());
		assertEquals(DesignType.CLONE_KIT, rocket.getDesignType());
		assertEquals("Original kit", rocket.getKitName());

		document.redo();

		assertEquals("Renamed", rocket.getName());
		assertEquals("Someone else", rocket.getDesigner());
		assertEquals("Revision 2", rocket.getRevision());
		assertEquals(DesignType.COMMERCIAL_KIT, rocket.getDesignType());
		assertEquals("Other kit", rocket.getKitName());
	}
}
