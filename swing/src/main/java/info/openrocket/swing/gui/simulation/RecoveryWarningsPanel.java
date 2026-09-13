package info.openrocket.swing.gui.simulation;

import static info.openrocket.core.util.StringUtils.escapeHtml;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.CollapsiblePanel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.components.UnitSelector;
import net.miginfocom.swing.MigLayout;

/**
 * Editor for the speed thresholds that generate recovery deployment warnings.
 *
 * <p>The source must expose the recovery warning properties used by
 * {@link DoubleModel}. Both simulation options and application preferences
 * provide those properties, allowing this editor to be reused in both places.</p>
 */
public class RecoveryWarningsPanel extends JPanel {
	private static final long serialVersionUID = -6464176770429595114L;
	private static final int PREFERRED_INFO_WIDTH = 420;
	private static final Translator trans = Application.getTranslator();

	/**
	 * Creates a recovery warning threshold editor bound to the supplied source.
	 *
	 * @param warningSource source with recovery warning threshold properties
	 */
	public RecoveryWarningsPanel(Object warningSource) {
		super(new MigLayout("fillx, insets 0", "[grow]"));

		add(new CollapsiblePanel(trans.get("simedtdlg.border.SingleDeployment"),
				createSingleDeploymentPanel(warningSource), true), "growx, wrap rel");
		add(new CollapsiblePanel(trans.get("simedtdlg.border.DualDeployment"),
				createDualDeploymentPanel(warningSource), false), "growx");
	}

	private JPanel createSingleDeploymentPanel(Object warningSource) {
		JPanel panel = createDeploymentPanel();
		panel.add(createWrappingInfoText(trans.get("simedtdlg.lbl.RecoveryWarnings.desc"), Style.ITALIC),
				"spanx, growx, wmin 0, wrap para");
		addThresholdEditor(panel, warningSource, "RecoverySpeedWarning",
				"simedtdlg.lbl.HighSpeedWarning", "simedtdlg.lbl.ttip.HighSpeedWarning", true);
		return panel;
	}

	private JPanel createDualDeploymentPanel(Object warningSource) {
		JPanel panel = createDeploymentPanel();
		panel.add(createWrappingInfoText(trans.get("simedtdlg.lbl.RecoveryWarnings.desc"), Style.ITALIC),
				"spanx, growx, wmin 0, wrap para");
		panel.add(createWrappingInfoText(trans.get("simedtdlg.lbl.DualDeployment.HowTo"), Style.PLAIN),
				"spanx, growx, wmin 0, wrap rel");
		addThresholdEditor(panel, warningSource, "RecoveryDrogueMainLowSpeedWarning",
				"simedtdlg.lbl.LowSpeedWarning", "simedtdlg.lbl.ttip.LowSpeedWarning", false);
		addThresholdEditor(panel, warningSource, "RecoveryDrogueMainHighSpeedWarning",
				"simedtdlg.lbl.HighSpeedWarning", "simedtdlg.lbl.ttip.HighSpeedWarning", true);
		return panel;
	}

	private JPanel createDeploymentPanel() {
		return new JPanel(new MigLayout("insets n, fillx", "[][pref!][pref!]unrel[][pref!][pref!][grow]"));
	}

	private void addThresholdEditor(JPanel panel, Object warningSource, String propertyName,
			String labelKey, String tooltipKey, boolean wrap) {
		String tooltip = trans.get(tooltipKey);
		JLabel label = new JLabel(trans.get(labelKey));
		label.setToolTipText(tooltip);
		panel.add(label, "gapright para");

		DoubleModel model = new DoubleModel(warningSource, propertyName, UnitGroup.UNITS_VELOCITY, 0);
		JSpinner spinner = new JSpinner(model.getSpinnerModel());
		spinner.setEditor(new SpinnerEditor(spinner));
		spinner.setToolTipText(tooltip);
		panel.add(spinner);
		panel.add(new UnitSelector(model), wrap ? "wrap" : "");
	}

	/**
	 * Lets MigLayout measure the description height at its allocated width.
	 * The HTML document is retained during resizing, preserving translated markup.
	 */
	private JLabel createWrappingInfoText(String text, Style style) {
		String normalizedText = text == null ? "" : text;
		String htmlContent = getHtmlContent(normalizedText);
		JLabel label = new JLabel("<html><body style='margin:0;"
				+ getStyleCss(style) + "'>" + htmlContent + "</body></html>") {
			private static final long serialVersionUID = 1L;
			private boolean revalidationPending;

			@Override
			public void setBounds(int x, int y, int width, int height) {
				boolean widthChanged = width != getWidth();
				super.setBounds(x, y, width, height);
				if (widthChanged) {
					View view = (View) getClientProperty(BasicHTML.propertyKey);
					if (view != null) {
						view.setSize(width, 0);
					}
					invalidateParentLayouts();
					if (!revalidationPending) {
						revalidationPending = true;
						// Validation can mark ancestors valid again after setBounds returns.
						// Recheck their heights after that pass, including on first expansion.
						SwingUtilities.invokeLater(() -> {
							revalidationPending = false;
							invalidateParentLayouts();
							revalidate();
							repaint();
						});
					}
				}
			}

			private void invalidateParentLayouts() {
				// Reflow changes every enclosing section's height and the scroll range.
				for (Container parent = getParent(); parent != null; parent = parent.getParent()) {
					parent.invalidate();
					if (parent instanceof JComponent component && component.isValidateRoot()) {
						break;
					}
				}
			}

			@Override
			public Dimension getPreferredSize() {
				View view = (View) getClientProperty(BasicHTML.propertyKey);
				if (view == null) {
					return super.getPreferredSize();
				}
				// A starting width only influences packing; laid-out text uses all available space.
				int width = getWidth() > 0 ? getWidth() : PREFERRED_INFO_WIDTH;
				view.setSize(width, 0);
				return new Dimension(PREFERRED_INFO_WIDTH, (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)));
			}
		};
		label.setMinimumSize(new Dimension(0, 0));
		label.putClientProperty("migLayout.dynamicAspectRatio", true);
		label.setVerticalAlignment(SwingConstants.TOP);
		return label;
	}

	/**
	 * Preserves trusted formatting from translated HTML while allowing plain
	 * translations to be embedded safely in the wrapping label.
	 */
	private String getHtmlContent(String text) {
		String trimmedText = text.trim();
		String lowerCaseText = trimmedText.toLowerCase(Locale.ROOT);
		if (!lowerCaseText.startsWith("<html")) {
			return escapeHtml(trimmedText);
		}

		int openingTagEnd = trimmedText.indexOf('>');
		int closingTagStart = lowerCaseText.lastIndexOf("</html>");
		if (openingTagEnd >= 0 && closingTagStart > openingTagEnd) {
			return trimmedText.substring(openingTagEnd + 1, closingTagStart);
		}
		return trimmedText;
	}

	private String getStyleCss(Style style) {
		return switch (style) {
			case ITALIC -> "font-style:italic;";
			case BOLD -> "font-weight:bold;";
			case BOLD_ITALIC -> "font-weight:bold;font-style:italic;";
			case PLAIN -> "";
		};
	}
}
