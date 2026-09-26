package info.openrocket.swing.gui.configdialog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

/**
 * Verifies the undo behaviour of the "Save Design Info" dialog's Cancel button.
 * <p>
 * The dialog is opened from {@code BasicFrame.showSaveRocketInfoDialog()}, which pushes an undo
 * position just before the dialog becomes visible; these tests simulate that sequence.
 */
class SaveDesignInfoPanelTest extends BaseTestCase {

	/** Set up a document whose undo history is at a clean, known state. */
	private static OpenRocketDocument documentAt(Rocket rocket, String name) {
		rocket.setName(name);
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		document.clearUndo();
		return document;
	}

	@Test
	void cancelRevertsTheEditsMadeInTheDialog() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = documentAt(rocket, "My Rocket");
		rocket.setDesigner("Original designer");

		// Open the dialog
		document.addUndoPosition("Modify Rocket");
		ModID modIDAtOpen = rocket.getModID();

		// Edit the design info, then cancel
		rocket.setName("Renamed in the dialog");
		rocket.setDesigner("Someone else");
		rocket.setRevision("Revision 2");
		SaveDesignInfoPanel.undoChangesSince(document, modIDAtOpen);

		assertEquals("My Rocket", document.getRocket().getName());
		assertEquals("Original designer", document.getRocket().getDesigner());
		assertEquals("", document.getRocket().getRevision());
	}

	@Test
	void cancelWithoutEditsKeepsTheWorkDoneBeforeTheDialogWasOpened() {
		// Regression test for #2680: cancelling an untouched dialog used to undo the user's
		// previous action instead of doing nothing.
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = documentAt(rocket, "My Rocket");

		// Work done before the dialog was opened, and not yet saved
		document.addUndoPosition("Rename rocket");
		rocket.setName("Work in progress");

		// Open the dialog, change nothing, cancel
		document.addUndoPosition("Modify Rocket");
		ModID modIDAtOpen = rocket.getModID();
		SaveDesignInfoPanel.undoChangesSince(document, modIDAtOpen);

		assertEquals("Work in progress", document.getRocket().getName());
	}

	@Test
	void cancelRevertsOnlyTheDialogsEditsWhenEarlierChangesArePending() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = documentAt(rocket, "My Rocket");

		// Work done before the dialog was opened, and not yet saved
		document.addUndoPosition("Rename rocket");
		rocket.setName("Work in progress");

		// Open the dialog, edit the design info, cancel
		document.addUndoPosition("Modify Rocket");
		ModID modIDAtOpen = rocket.getModID();
		rocket.setName("Renamed in the dialog");
		SaveDesignInfoPanel.undoChangesSince(document, modIDAtOpen);

		assertEquals("Work in progress", document.getRocket().getName());
	}
}
