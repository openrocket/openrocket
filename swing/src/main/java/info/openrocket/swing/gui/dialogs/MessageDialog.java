package info.openrocket.swing.gui.dialogs;

import java.awt.Component;
import java.util.function.BooleanSupplier;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Utility class for showing simple single-message dialogs consistently.
 * <p>
 * Use {@link WarningDialog} or {@link ErrorWarningDialog} when displaying
 * structured {@link info.openrocket.core.logging.WarningSet} /
 * {@link info.openrocket.core.logging.ErrorSet} content.
 */
public abstract class MessageDialog {

	public static void showInfo(Component parent, String message, String title) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showWarning(Component parent, String message, String title) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
	}

	public static void showError(Component parent, String message, String title) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}

	// --- Confirmation dialogs ---

	/** YES/NO buttons, question icon. Returns true if user clicked Yes. */
	public static boolean confirmYesNo(Component parent, Object message, String title) {
		return JOptionPane.showConfirmDialog(parent, message, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	/** YES/NO buttons, warning icon. Returns true if user clicked Yes. Use for destructive actions. */
	public static boolean confirmYesNoWarning(Component parent, Object message, String title) {
		return JOptionPane.showConfirmDialog(parent, message, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}

	/** OK/Cancel buttons, question icon. Returns true if user clicked OK. */
	public static boolean confirmOkCancel(Component parent, Object message, String title) {
		return JOptionPane.showConfirmDialog(parent, message, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}

	/** OK/Cancel buttons, warning icon. Returns true if user clicked OK. Use for destructive actions. */
	public static boolean confirmOkCancelWarning(Component parent, Object message, String title) {
		return JOptionPane.showConfirmDialog(parent, message, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
	}

	// --- Confirmation dialogs with "Don't ask again" checkbox ---

	/**
	 * YES/NO warning dialog with an inline "Don't ask me again" checkbox.
	 * {@code onDontAsk} is invoked only when the user both checks the box AND clicks Yes,
	 * so cancelling never permanently suppresses future prompts.
	 * Returns true if the user clicked Yes.
	 */
	public static boolean confirmYesNoWarningWithDontAsk(Component parent, String message, String title,
			String checkboxLabel, Runnable onDontAsk) {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel(message), "left, wrap");
		JCheckBox check = new JCheckBox(checkboxLabel);
		panel.add(check, "left, gaptop para");
		boolean confirmed = JOptionPane.showConfirmDialog(parent, panel, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
		if (confirmed && check.isSelected()) {
			onDontAsk.run();
		}
		return confirmed;
	}

	/**
	 * YES/NO warning dialog with an inline "Don't ask me again" checkbox, guarded by a preference.
	 * If {@code shouldAsk} returns false the dialog is skipped and the action proceeds.
	 * When the checkbox is selected, {@code onDontAsk} is invoked (disabling future prompts).
	 * Returns true if the action should proceed.
	 */
	public static boolean confirmYesNoWarningWithDontAsk(Component parent, String message, String title,
			String checkboxLabel, BooleanSupplier shouldAsk, Runnable onDontAsk) {
		if (!shouldAsk.getAsBoolean()) {
			return true;
		}
		return confirmYesNoWarningWithDontAsk(parent, message, title, checkboxLabel, onDontAsk);
	}
}
