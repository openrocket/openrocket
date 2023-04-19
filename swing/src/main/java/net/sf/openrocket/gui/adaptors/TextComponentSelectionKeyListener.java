package net.sf.openrocket.gui.adaptors;

import javax.swing.text.JTextComponent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * This key listener fixes a default behavior by Java Swing text components, where if you select a text, pressing the
 * left or right arrow key would not bring the text cursor to the beginning or the end of the selection
 * (@Java, please fix...).
 * <p>
 * This listener's behavior:
 * If some text of the editor is selected, set the caret position to:
 *      - the end of the selection if the user presses the right arrow key
 *      - the beginning of the selection if the user presses the left arrow key
 */
public class TextComponentSelectionKeyListener extends KeyAdapter {
    private final JTextComponent textField;

    public TextComponentSelectionKeyListener(JTextComponent textField) {
        this.textField = textField;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isShiftDown()) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
            int start = textField.getSelectionStart();
            int end = textField.getSelectionEnd();
            if (end > start) {
                textField.setCaretPosition(start + 1);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT) {
            int start = textField.getSelectionStart();
            int end = textField.getSelectionEnd();
            if (end > start) {
                textField.setCaretPosition(end - 1);
            }
        }
    }
}
