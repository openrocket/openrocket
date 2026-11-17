package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckListEditorTest {
	private CheckList<String> checkList;
	private JList<String> list;

	@BeforeEach
	public void setUp() {
		checkList = new CheckList.Builder().build();
		checkList.setData(List.of("AeroTech", "Cesaroni", "Estes"));
		checkList.setUncheckedItems(List.of("AeroTech", "Cesaroni", "Estes"));
		list = checkList.getList();
		list.setSize(200, 200);
		list.doLayout();
	}

	private void mouse(int id, int x, int y, int clickCount) {
		list.dispatchEvent(new MouseEvent(list, id, System.currentTimeMillis(),
				id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0, x, y, clickCount, false,
				MouseEvent.BUTTON1));
	}

	private void click(int index, int pressDx, int releaseDx, int clickCount) {
		Rectangle cell = list.getCellBounds(index, index);
		int y = cell.y + cell.height / 2;
		mouse(MouseEvent.MOUSE_PRESSED, cell.x + 10 + pressDx, y, clickCount);
		mouse(MouseEvent.MOUSE_RELEASED, cell.x + 10 + releaseDx, y, clickCount);
		// Like AWT, only report a click when the mouse did not move between press and release
		if (pressDx == releaseDx) {
			mouse(MouseEvent.MOUSE_CLICKED, cell.x + 10 + releaseDx, y, clickCount);
		}
	}

	private boolean checked(int index) {
		return checkList.getModel().isCheckedIndex(index);
	}

	/** Moving the mouse slightly between press and release used to drop the toggle. */
	@Test
	public void testSmallMovementStillToggles() {
		click(1, 0, 3, 1);
		assertEquals(List.of(false, true, false), List.of(checked(0), checked(1), checked(2)));
	}

	/** A quick second click is a double click to Swing; it must toggle back, not be ignored. */
	@Test
	public void testQuickSecondClickTogglesAgain() {
		click(0, 0, 0, 1);
		click(0, 0, 0, 2);
		assertEquals(false, checked(0));
	}

	/** Dragging off the item before releasing cancels the toggle, like a normal checkbox. */
	@Test
	public void testReleasingOnAnotherItemDoesNotToggle() {
		Rectangle first = list.getCellBounds(0, 0);
		Rectangle second = list.getCellBounds(1, 1);
		mouse(MouseEvent.MOUSE_PRESSED, first.x + 10, first.y + first.height / 2, 1);
		mouse(MouseEvent.MOUSE_RELEASED, second.x + 10, second.y + second.height / 2, 1);
		assertEquals(List.of(false, false), List.of(checked(0), checked(1)));
	}
}
