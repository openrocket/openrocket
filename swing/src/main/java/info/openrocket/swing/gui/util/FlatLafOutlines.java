package info.openrocket.swing.gui.util;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.StateChangeListener;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static info.openrocket.core.util.StringUtils.escapeHtml;

/**
 * Helpers for applying FlatLaf component outlines (e.g. "warning", "error") via
 * the {@code JComponent.outline} client property, with optional validation messages.
 */
public final class FlatLafOutlines {
	/**
	 * FlatLaf client property key used by many Swing components to render validation outlines.
	 */
	public static final String OUTLINE_PROPERTY = "JComponent.outline";
	/**
	 * Client property key holding the original tooltip while a validation tooltip is active.
	 */
	private static final String BASE_TOOLTIP_PROPERTY = FlatLafOutlines.class.getName() + ".baseToolTip";
	/**
	 * Sentinel value meaning "original tooltip was null".
	 */
	private static final Object NULL_TOOLTIP = new Object();

	public enum Outline {
		/** Removes the outline. */
		NONE(null),
		/** Draws a warning outline (yellow/orange depending on theme). */
		WARNING("warning"),
		/** Draws an error outline (red depending on theme). */
		ERROR("error");

		private final String flatLafValue;

		Outline(String flatLafValue) {
			this.flatLafValue = flatLafValue;
		}

		String getFlatLafValue() {
			return flatLafValue;
		}
	}

	private FlatLafOutlines() {
	}

	/**
	 * Apply an operation to the component and any related editor subcomponents (e.g. spinner editor/textfield).
	 *
	 * @param component base component
	 * @param consumer operation to apply to each related component
	 */
	private static void forEachRelatedComponent(JComponent component, Consumer<JComponent> consumer) {
		consumer.accept(component);
		if (component instanceof JSpinner spinner) {
			JComponent editor = spinner.getEditor();
			if (editor != null) {
				consumer.accept(editor);
				if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
					JFormattedTextField textField = defaultEditor.getTextField();
					if (textField != null) {
						consumer.accept(textField);
					}
				}
			}
		}
	}

	/**
	 * Set a FlatLaf outline on a component. For spinners, also applies to the editor and editor text field.
	 *
	 * @param component component to style
	 * @param outline outline to apply (null clears)
	 */
	public static void setOutline(JComponent component, Outline outline) {
		Objects.requireNonNull(component, "component");
		final String value = outline == null ? null : outline.getFlatLafValue();

		forEachRelatedComponent(component, c -> c.putClientProperty(OUTLINE_PROPERTY, value));
	}

	/**
	 * Read the current FlatLaf outline from the component.
	 *
	 * @param component component to query
	 * @return {@link Outline#NONE} if unset/unknown.
	 */
	public static Outline getOutline(JComponent component) {
		Objects.requireNonNull(component, "component");
		Object value = component.getClientProperty(OUTLINE_PROPERTY);
		if (!(value instanceof String outlineValue) || outlineValue.isEmpty()) {
			return Outline.NONE;
		}
		if (Outline.ERROR.getFlatLafValue().equals(outlineValue)) {
			return Outline.ERROR;
		}
		if (Outline.WARNING.getFlatLafValue().equals(outlineValue)) {
			return Outline.WARNING;
		}
		return Outline.NONE;
	}

	/**
	 * Replace the component tooltip with a validation message (when non-null), and restore the original tooltip when cleared.
	 *
	 * @param component component whose tooltip should change
	 * @param message validation message, or null to restore the original tooltip
	 */
	private static void setValidationTooltip(JComponent component, String message) {
		String tooltipMessage = message != null ? message.trim() : null;
		if (tooltipMessage != null && tooltipMessage.isEmpty()) {
			tooltipMessage = null;
		}

		final String finalTooltipMessage = tooltipMessage;
		forEachRelatedComponent(component, c -> setValidationTooltipOnSingleComponent(c, finalTooltipMessage));
	}

	/**
	 * Set/restore the tooltip for a single component, preserving the original tooltip in a client property.
	 *
	 * @param component component whose tooltip should change
	 * @param message validation message, or null to restore the original tooltip
	 */
	private static void setValidationTooltipOnSingleComponent(JComponent component, String message) {
		if (message == null) {
			Object base = component.getClientProperty(BASE_TOOLTIP_PROPERTY);
			if (base == null) {
				return;
			}
			component.setToolTipText(base == NULL_TOOLTIP ? null : (String) base);
			component.putClientProperty(BASE_TOOLTIP_PROPERTY, null);
			return;
		}

		Object base = component.getClientProperty(BASE_TOOLTIP_PROPERTY);
		if (base == null) {
			String existing = component.getToolTipText();
			component.putClientProperty(BASE_TOOLTIP_PROPERTY, existing == null ? NULL_TOOLTIP : existing);
		}

		component.setToolTipText(formatTooltipText(message));
	}

	/**
	 * Normalize tooltip text: keep HTML tooltips as-is; otherwise escape and wrap in {@code <html>}.
	 *
	 * @param tooltip raw tooltip text (plain text or HTML)
	 * @return tooltip text suitable for {@link JComponent#setToolTipText(String)}, or null to clear
	 */
	private static String formatTooltipText(String tooltip) {
		if (tooltip == null) {
			return null;
		}
		String trimmed = tooltip.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		if (isHtml(trimmed)) {
			return trimmed;
		}
		return "<html>" + escapeHtml(trimmed)  + "</html>";
	}

	/**
	 * @param tooltip tooltip string to inspect
	 * @return true if the tooltip already looks like an HTML tooltip.
	 */
	private static boolean isHtml(String tooltip) {
		return tooltip.toLowerCase(Locale.ROOT).startsWith("<html");
	}

	/**
	 * Binds outline + optional message to a component based on one or more conditions.
	 * Call {@link #listenTo(ChangeSource...)} to keep it up to date.
	 */
	public static final class Validator implements AutoCloseable {
		private record Condition(Outline outline, BooleanSupplier predicate, Supplier<String> message) {
		}

		private final JComponent component;
		private final List<Condition> conditions = new ArrayList<>();
		private final Set<ChangeSource> sources = new HashSet<>();
		private final StateChangeListener listener = e -> update();
		private final HierarchyListener autoCloseListener;
		private int transientMessageDurationMs = 0;
		private Outline lastOutline = Outline.NONE;
		private String lastMessage = null;
		private Popup transientPopup;
		private Timer transientTimer;
		private boolean closed = false;

		private Validator(JComponent component) {
			this.component = Objects.requireNonNull(component, "component");
			this.autoCloseListener = e -> {
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !component.isDisplayable()) {
					close();
				}
			};
			this.component.addHierarchyListener(autoCloseListener);
		}

		/**
		 * Add a warning condition without a message.
		 *
		 * @param predicate returns true when the warning is active
		 * @return this
		 */
		public Validator warnIf(BooleanSupplier predicate) {
			return warnIf(predicate, (Supplier<String>) null);
		}

		/**
		 * Add a warning condition with a fixed message (shown in tooltip/popup).
		 *
		 * @param predicate returns true when the warning is active
		 * @param message message to show (tooltip/popup), may be null/blank
		 * @return this
		 */
		public Validator warnIf(BooleanSupplier predicate, String message) {
			return warnIf(predicate, message == null ? null : () -> message);
		}

		/**
		 * Add a warning condition with a dynamic message supplier.
		 *
		 * @param predicate returns true when the warning is active
		 * @param message message supplier (tooltip/popup), may be null
		 * @return this
		 */
		public Validator warnIf(BooleanSupplier predicate, Supplier<String> message) {
			conditions.add(new Condition(Outline.WARNING, Objects.requireNonNull(predicate, "predicate"), message));
			return this;
		}

		/**
		 * Add an error condition without a message.
		 *
		 * @param predicate returns true when the error is active
		 * @return this
		 */
		public Validator errorIf(BooleanSupplier predicate) {
			return errorIf(predicate, (Supplier<String>) null);
		}

		/**
		 * Add an error condition with a fixed message (shown in tooltip/popup).
		 *
		 * @param predicate returns true when the error is active
		 * @param message message to show (tooltip/popup), may be null/blank
		 * @return this
		 */
		public Validator errorIf(BooleanSupplier predicate, String message) {
			return errorIf(predicate, message == null ? null : () -> message);
		}

		/**
		 * Add an error condition with a dynamic message supplier.
		 *
		 * @param predicate returns true when the error is active
		 * @param message message supplier (tooltip/popup), may be null
		 * @return this
		 */
		public Validator errorIf(BooleanSupplier predicate, Supplier<String> message) {
			conditions.add(new Condition(Outline.ERROR, Objects.requireNonNull(predicate, "predicate"), message));
			return this;
		}

		/**
		 * When enabled, shows the validation message as a tooltip-like popup near the component
		 * for a short period when the invalid state first appears or when the message changes.
		 *
		 * @param durationMs popup duration in milliseconds (0 disables)
		 * @return this
		 */
		public Validator showMessagePopup(int durationMs) {
			this.transientMessageDurationMs = Math.max(0, durationMs);
			return this;
		}

		/**
		 * Register one or more {@link ChangeSource}s that will trigger validation updates.
		 *
		 * @param changeSources sources that fire when inputs change
		 * @return this
		 */
		public Validator listenTo(ChangeSource... changeSources) {
			if (changeSources == null) {
				return this;
			}
			for (ChangeSource source : changeSources) {
				if (source == null || !sources.add(source)) {
					continue;
				}
				source.addChangeListener(listener);
			}
			update();
			return this;
		}

		/**
		 * Recompute outline/message and apply them to the component (runs on the EDT).
		 */
		public void update() {
			if (closed) {
				return;
			}
			if (SwingUtilities.isEventDispatchThread()) {
				updateNow();
			} else {
				SwingUtilities.invokeLater(this::updateNow);
			}
		}

		private void updateNow() {
			if (closed) {
				return;
			}
			Outline outline = Outline.NONE;
			List<String> messages = new ArrayList<>();
			for (Condition condition : conditions) {
				if (!condition.predicate().getAsBoolean()) {
					continue;
				}
				Outline conditionOutline = condition.outline();
				if (conditionOutline.ordinal() > outline.ordinal()) {
					outline = conditionOutline;
					messages.clear();
				}
				if (conditionOutline == outline && condition.message() != null) {
					String message = condition.message().get();
					if (message != null && !message.trim().isEmpty()) {
						messages.add(message.trim());
					}
				}
			}
			FlatLafOutlines.setOutline(component, outline);
			String message = messages.isEmpty() ? null : String.join("\n", messages);
			setValidationTooltip(component, message);

			if (outline == Outline.NONE || message == null) {
				hideTransientMessage();
			} else if (transientMessageDurationMs > 0 && (lastOutline == Outline.NONE || !Objects.equals(lastMessage, message))) {
				showTransientMessage(message, transientMessageDurationMs);
			}

			lastOutline = outline;
			lastMessage = message;
		}

		/**
		 * Show a tooltip-like popup near the component.
		 *
		 * @param message message to show
		 * @param durationMs popup duration in milliseconds
		 */
		private void showTransientMessage(String message, int durationMs) {
			hideTransientMessage();
			if (!component.isShowing()) {
				return;
			}
			Point location;
			try {
				location = component.getLocationOnScreen();
			} catch (IllegalComponentStateException ignore) {
				return;
			}

			JToolTip toolTip = component.createToolTip();
			String tipText = formatTooltipText(message);
			if (tipText == null) {
				return;
			}
			toolTip.setTipText(tipText);
			toolTip.setComponent(component);

			int x = location.x;
			int y = location.y + component.getHeight() + 2;
			transientPopup = PopupFactory.getSharedInstance().getPopup(component, toolTip, x, y);
			transientPopup.show();

			transientTimer = new Timer(durationMs, e -> hideTransientMessage());
			transientTimer.setRepeats(false);
			transientTimer.start();
		}

		/**
		 * Hide any active transient popup.
		 */
		private void hideTransientMessage() {
			if (transientTimer != null) {
				transientTimer.stop();
				transientTimer = null;
			}
			if (transientPopup != null) {
				transientPopup.hide();
				transientPopup = null;
			}
		}

		/**
		 * Detach listeners, hide any popup, and restore the original tooltip.
		 */
		@Override
		public void close() {
			if (closed) {
				return;
			}
			closed = true;
			component.removeHierarchyListener(autoCloseListener);
			hideTransientMessage();
			setValidationTooltip(component, null);
			for (ChangeSource source : sources) {
				source.removeChangeListener(listener);
			}
			sources.clear();
		}
	}

	/**
	 * Create a new validator for a component.
	 *
	 * @param component component to style/validate
	 * @return new validator instance
	 */
	public static Validator validator(JComponent component) {
		return new Validator(component);
	}

	/**
	 * Return the more severe outline, based on enum ordering.
	 *
	 * @param a first outline (may be null)
	 * @param b second outline (may be null)
	 * @return the more severe outline (never null)
	 */
	private static Outline max(Outline a, Outline b) {
		if (a == null) {
			return b == null ? Outline.NONE : b;
		}
		if (b == null) {
			return a;
		}
		return a.ordinal() >= b.ordinal() ? a : b;
	}
}
