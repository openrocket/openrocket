package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.util.BaseTestCase;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The bundled Inter UI font measures narrower through {@link FontMetrics}, which is what sizes a
 * label, than the look and feel paints it. A label laid out at exactly its preferred width fails
 * the fit test by a fraction of a pixel, and Swing does not trim a fraction: it drops whole
 * characters until the text plus an ellipsis fits, costing three or four of them. Every label in
 * this panel came out as "Burno..." or "Color pins by sta..." before the layout gave them slack.
 */
public class SimulationFlightPathExportPanelTest extends BaseTestCase {

	private static final String[] FONT_KEYS = {
			"Label.font", "CheckBox.font", "ComboBox.font", "Panel.font",
			"Spinner.font", "TitledBorder.font",
	};

	private static Graphics2D graphics;
	private static Injector previousInjector;
	private static final Map<String, Object> previousFonts = new HashMap<>();

	@BeforeAll
	public static void installBundledFontAndRealStrings() {
		graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();

		Font inter = GUIUtil.createUIFont("Regular", 12f, 0f);
		for (String key : FONT_KEYS) {
			previousFonts.put(key, UIManager.get(key));
			UIManager.put(key, inter);
		}

		// The test services bind a translator that echoes keys back, which would measure nothing
		// like the real strings. Bind the shipped bundle instead.
		previousInjector = Application.getInjector();
		Module realStrings = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(
						new ResourceBundleTranslator("l10n.messages", Locale.ROOT));
			}
		};
		Application.setInjector(Guice.createInjector(
				Modules.override(new ServicesForTesting()).with(realStrings), new PluginModule()));
	}

	@AfterAll
	public static void restore() {
		for (Map.Entry<String, Object> e : previousFonts.entrySet()) {
			UIManager.put(e.getKey(), e.getValue());
		}
		if (previousInjector != null) {
			Application.setInjector(previousInjector);
		}
	}

	@Test
	public void noLabelIsTruncatedAtTheLayoutsOwnWidth() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());

		SimulationFlightPathExportPanel panel = new SimulationFlightPathExportPanel(simulation);
		panel.setSize(panel.getPreferredSize());
		layoutDeep(panel);

		List<String> truncated = new ArrayList<>();
		collectTruncated(panel, truncated);

		assertEquals(List.of(), truncated,
				"every label must be laid out wide enough to draw in full");
	}

	private static void layoutDeep(Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				layoutDeep((Container) child);
			}
		}
	}

	private static void collectTruncated(Container container, List<String> truncated) {
		for (Component child : container.getComponents()) {
			if (child instanceof JLabel) {
				JLabel label = (JLabel) child;
				check(label, label.getText(), label.getIcon(), truncated);
			} else if (child instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) child;
				check(button, button.getText(), checkBoxIcon(button), truncated);
			}
			if (child instanceof Container) {
				collectTruncated((Container) child, truncated);
			}
		}
	}

	/** Ask Swing's own routine what it would actually draw in the width the layout handed out. */
	private static void check(JLabel c, String text, Icon icon, List<String> truncated) {
		if (text == null || text.isEmpty()) {
			return;
		}
		String shown = SwingUtilities.layoutCompoundLabel(c, metrics(c), text, icon,
				c.getVerticalAlignment(), c.getHorizontalAlignment(),
				c.getVerticalTextPosition(), c.getHorizontalTextPosition(),
				bounds(c), new Rectangle(), new Rectangle(), c.getIconTextGap());
		if (!text.equals(shown)) {
			truncated.add(text + " -> " + shown);
		}
	}

	private static void check(AbstractButton c, String text, Icon icon, List<String> truncated) {
		if (text == null || text.isEmpty()) {
			return;
		}
		String shown = SwingUtilities.layoutCompoundLabel(c, metrics(c), text, icon,
				c.getVerticalAlignment(), c.getHorizontalAlignment(),
				c.getVerticalTextPosition(), c.getHorizontalTextPosition(),
				bounds(c), new Rectangle(), new Rectangle(), c.getIconTextGap());
		if (!text.equals(shown)) {
			truncated.add(text + " -> " + shown);
		}
	}

	/**
	 * The box a checkbox draws beside its text. It has to be included or the whole width looks
	 * available to the text and nothing ever appears clipped. {@code UIManager.getIcon} returns
	 * null under the test look and feel, so ask the button's own UI delegate first.
	 */
	private static Icon checkBoxIcon(AbstractButton button) {
		if (button.getUI() instanceof BasicRadioButtonUI) {
			Icon icon = ((BasicRadioButtonUI) button.getUI()).getDefaultIcon();
			if (icon != null) {
				return icon;
			}
		}
		return UIManager.getIcon("CheckBox.icon");
	}

	private static FontMetrics metrics(Component c) {
		return graphics.getFontMetrics(c.getFont());
	}

	private static Rectangle bounds(Component c) {
		Dimension size = c.getSize();
		return new Rectangle(0, 0, size.width, size.height);
	}
}
