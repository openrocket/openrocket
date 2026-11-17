package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatLightLaf;

public class CheckListRendererTest {
	private static Insets insets(JList<String> list, boolean selected, boolean focused) {
		Component c = list.getCellRenderer().getListCellRendererComponent(list, "AeroTech", 0, selected, focused);
		return ((JComponent) c).getInsets();
	}

	/** The focused row used a wider border than the other rows, shifting its checkbox and text. */
	@Test
	public void testFocusedRowKeepsTheSameInsets() throws Exception {
		LookAndFeel previous = UIManager.getLookAndFeel();
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			CheckList<String> checkList = new CheckList.Builder().build();
			checkList.setData(List.of("AeroTech", "Cesaroni"));
			JList<String> list = checkList.getList();

			Insets unfocused = insets(list, false, false);
			assertEquals(unfocused, insets(list, true, true));
			assertEquals(unfocused, insets(list, false, true));
			assertEquals(unfocused, insets(list, true, false));
		} finally {
			UIManager.setLookAndFeel(previous);
		}
	}
}
