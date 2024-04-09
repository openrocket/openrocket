package info.openrocket.swing.gui.theme;

import com.formdev.flatlaf.ui.FlatScrollPaneBorder;

import javax.swing.UIManager;
import java.awt.Color;

public class ORScrollPaneBorder extends FlatScrollPaneBorder {
	public ORScrollPaneBorder() {
		Color borderColor = UIManager.getColor("OR.ScrollPane.borderColor");
		if (borderColor == null) {
			borderColor = UIManager.getColor("Component.borderColor");
		}
		this.borderColor = borderColor;
	}
}
