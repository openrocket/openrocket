package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.LookAndFeel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatLightLaf;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.swing.gui.components.CollapsiblePanel;
import info.openrocket.swing.util.BaseTestCase;

/** Tests placement and binding of recovery deployment warning controls. */
public class RecoveryWarningsPanelTest extends BaseTestCase {

	@Test
	public void testThresholdEditorsUpdateSimulationOptions() throws Exception {
		SimulationOptions options = new SimulationOptions();

		SwingUtilities.invokeAndWait(() -> {
			RecoveryWarningsPanel panel = new RecoveryWarningsPanel(options);
			assertTrue(findComponents(panel, JTabbedPane.class).isEmpty(),
					"Recovery configuration should use collapsible sections instead of tabs");

			List<CollapsiblePanel> sections = findComponents(panel, CollapsiblePanel.class);
			assertEquals(2, sections.size());
			assertTrue(sections.get(0).isExpanded());
			assertFalse(sections.get(1).isExpanded());
			List<JToggleButton> sectionButtons = findComponents(panel, JToggleButton.class);
			assertEquals(2, sectionButtons.size());
			assertTrue(sectionButtons.get(0).isRolloverEnabled());
			assertTrue(sectionButtons.get(0).getIcon() != null);
			assertTrue(sectionButtons.get(0).getSelectedIcon() != null);

			List<JSpinner> thresholdSpinners = findComponents(panel, JSpinner.class);
			assertEquals(1, thresholdSpinners.size(),
					"Collapsed content should be removed from the component hierarchy");
			thresholdSpinners.get(0).setValue("37");
			assertEquals(37.0, options.getRecoverySpeedWarning(), 0.0001);

			sectionButtons.get(1).doClick();
			assertTrue(sections.get(1).isExpanded());
			thresholdSpinners = findComponents(panel, JSpinner.class);
			assertEquals(3, thresholdSpinners.size());
			JPanel dualDeploymentPanel = (JPanel) thresholdSpinners.get(1).getParent();
			dualDeploymentPanel.setSize(1000, dualDeploymentPanel.getPreferredSize().height);
			dualDeploymentPanel.doLayout();
			assertEquals(thresholdSpinners.get(1).getY(), thresholdSpinners.get(2).getY(),
					"Dual-deployment thresholds should share one compact row");

			sectionButtons.get(1).doClick();
			assertFalse(sections.get(1).isExpanded());
			assertEquals(1, findComponents(panel, JSpinner.class).size(),
					"Collapsing should remove the dual deployment controls from layout");
		});
	}

	@Test
	public void testSettingsMovedFromOptionsToWarningsPanel() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());

		SwingUtilities.invokeAndWait(() -> {
			SimulationOptionsPanel optionsPanel = new SimulationOptionsPanel(document, simulation);
			SimulationWarningsPanel warningsPanel = new SimulationWarningsPanel(simulation, false);

			assertTrue(findComponents(optionsPanel, RecoveryWarningsPanel.class).isEmpty());
			assertEquals(1, findComponents(warningsPanel, RecoveryWarningsPanel.class).size());
			List<JTabbedPane> warningTabs = findComponents(warningsPanel, JTabbedPane.class);
			assertEquals(1, warningTabs.size());
			assertEquals(2, warningTabs.get(0).getTabCount());
			assertTrue(warningTabs.get(0).getComponentAt(0) instanceof JScrollPane,
					"Messages should have its own scroll pane below the tab header");
			assertTrue(warningTabs.get(0).getComponentAt(1) instanceof JScrollPane,
					"Configuration should have its own scroll pane below the tab header");
			assertEquals(1, warningTabs.get(0).getSelectedIndex(),
					"Multi-simulation editing should open configuration because messages are unavailable");
			assertTrue(findComponents(warningsPanel, JList.class).isEmpty(),
					"Multi-simulation editing should hide run-specific results, not warning settings");
			SimulationWarningsPanel singleSimulationPanel = new SimulationWarningsPanel(simulation, true);
			JTabbedPane singleSimulationTabs = findComponents(singleSimulationPanel, JTabbedPane.class).get(0);
			assertEquals(0, singleSimulationTabs.getSelectedIndex(),
					"Actual warning messages should be shown first for a single simulation");
		});
	}

	@Test
	public void testConfigurationResizesAndUpdatesScrollRanges() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());

		SwingUtilities.invokeAndWait(() -> {
			SimulationWarningsPanel warningsPanel = new SimulationWarningsPanel(simulation, false);
			findComponents(warningsPanel, CollapsiblePanel.class).get(1).setExpanded(true);
			JTabbedPane nestedTabs = findComponents(warningsPanel, JTabbedPane.class).get(0);
			JScrollPane configurationScrollPane = (JScrollPane) nestedTabs.getComponentAt(1);
			Component configurationContent = configurationScrollPane.getViewport().getView();
			int preferredContentWidth = configurationContent.getMinimumSize().width;

			// Measure the tab and scroll-pane chrome, then make the viewport narrower
			// than the controls can fit. Only then should horizontal scrolling be needed.
			warningsPanel.setSize(preferredContentWidth + 200, 400);
			layoutRecursively(warningsPanel);
			int horizontalChrome = warningsPanel.getWidth()
					- configurationScrollPane.getViewport().getExtentSize().width;
			int narrowViewportWidth = Math.max(100, preferredContentWidth - 100);
			warningsPanel.setSize(narrowViewportWidth + horizontalChrome, 400);
			layoutRecursively(warningsPanel);
			layoutRecursively(warningsPanel);
			assertTrue(nestedTabs.getX() + nestedTabs.getWidth() <= warningsPanel.getWidth(),
					"The nested tabs should shrink instead of clipping their scroll pane");
			assertTrue(configurationScrollPane.getX() + configurationScrollPane.getWidth() <= nestedTabs.getWidth(),
					"The configuration scroll pane should remain within the nested tab bounds");
			assertTrue(configurationScrollPane.getHorizontalScrollBar().isVisible(),
					"Configuration should scroll before the dual-threshold row is clipped");
			assertTrue(configurationContent.getWidth() >= preferredContentWidth,
					"Narrow resizing must preserve enough width for the threshold controls");
			List<JSpinner> configuredSpinners = findComponents(configurationContent, JSpinner.class);
			JPanel dualThresholdPanel = (JPanel) configuredSpinners.get(1).getParent();
			int secondSpinnerRightEdge = configuredSpinners.get(2).getX() + configuredSpinners.get(2).getWidth();
			assertTrue(secondSpinnerRightEdge <= dualThresholdPanel.getWidth(),
					"The second threshold spinner should remain inside the scrollable content: spinner edge "
							+ secondSpinnerRightEdge + ", panel width " + dualThresholdPanel.getWidth());

			int preferredContentHeight = configurationContent.getPreferredSize().height;
			int wideWidth = preferredContentWidth + 200;
			int tallHeight = preferredContentHeight + 200;
			int narrowWidth = narrowViewportWidth + horizontalChrome;

			// Exercise each axis independently and together, then return to ample space.
			for (int[] size : new int[][] {{wideWidth, tallHeight}, {wideWidth, 180},
					{narrowWidth, 180}, {narrowWidth, tallHeight}, {wideWidth, tallHeight}}) {
				warningsPanel.setSize(size[0], size[1]);
				layoutRecursively(warningsPanel);
				layoutRecursively(warningsPanel);
				assertEquals(size[0] == narrowWidth, configurationScrollPane.getHorizontalScrollBar().isVisible());
				assertEquals(size[1] == 180, configurationScrollPane.getVerticalScrollBar().isVisible());
				assertTrue(nestedTabs.getY() + nestedTabs.getHeight() <= warningsPanel.getHeight(),
						"Vertical resizing should keep the nested tabs inside the panel");
				assertTrue(configurationScrollPane.getY() + configurationScrollPane.getHeight() <= nestedTabs.getHeight(),
						"Vertical resizing should keep the scroll pane inside the tabs");

				// At the end of each scrollbar, the trailing content must be reachable.
				configurationScrollPane.getHorizontalScrollBar().setValue(Integer.MAX_VALUE);
				configurationScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
				assertEquals(configurationContent.getWidth(),
						configurationScrollPane.getViewport().getViewRect().x
								+ configurationScrollPane.getViewport().getExtentSize().width);
				assertEquals(configurationContent.getHeight(),
						configurationScrollPane.getViewport().getViewRect().y
								+ configurationScrollPane.getViewport().getExtentSize().height);
			}

			// Toggle after layout to catch stale MigLayout preferred-size caches.
			List<CollapsiblePanel> sections = findComponents(warningsPanel, CollapsiblePanel.class);
			sections.forEach(section -> section.setExpanded(false));
			int collapsedHeight = configurationContent.getPreferredSize().height;
			int verticalChrome = warningsPanel.getHeight() - configurationScrollPane.getViewport().getExtentSize().height;
			warningsPanel.setSize(wideWidth, verticalChrome + (collapsedHeight + preferredContentHeight) / 2);
			for (boolean expanded : new boolean[] {false, true, false, true}) {
				sections.forEach(section -> section.setExpanded(expanded));
				layoutRecursively(warningsPanel);
				layoutRecursively(warningsPanel);
				assertEquals(expanded, configurationScrollPane.getVerticalScrollBar().isVisible(),
						"Expanding and collapsing must update the vertical scroll range");
				assertTrue(configurationContent.getPreferredSize().height <= preferredContentHeight);
			}
		});
	}

	@Test
	public void testDescriptionsWrapToAvailableWidth() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());
		SwingUtilities.invokeAndWait(() -> {
			SimulationWarningsPanel panel = new SimulationWarningsPanel(simulation, false);
			findComponents(panel, CollapsiblePanel.class).get(1).setExpanded(true);
			JScrollPane scrollPane = (JScrollPane) findComponents(panel, JTabbedPane.class).get(0).getSelectedComponent();
			Component content = scrollPane.getViewport().getView();
			List<JLabel> descriptions = findComponents(content, JLabel.class).stream()
					.filter(label -> label.getClientProperty(BasicHTML.propertyKey) != null).toList();
			assertEquals(3, descriptions.size());
			// Use real prose instead of the test translator's unbreakable resource keys.
			descriptions.forEach(label -> label.setText("<html><i>"
					+ "Warn when a recovery device deploys at an unsafe speed. Warnings apply independently by stage. ".repeat(4)
					+ "</i><br><b>Check the recovery configuration.</b></html>"));
			int minimumWidth = content.getMinimumSize().width;
			int narrowHeight = 0;
			for (int extraWidth : new int[] {100, 700, 100}) {
				panel.setSize(minimumWidth + extraWidth, 800);
				for (int pass = 0; pass < 4; pass++) {
					layoutRecursively(panel);
				}
				assertFalse(scrollPane.getHorizontalScrollBar().isVisible(),
						"Descriptions should wrap while controls fit in the viewport");
				assertEquals(scrollPane.getViewport().getExtentSize().width, content.getWidth());
				int textHeight = descriptions.get(0).getHeight();
				if (narrowHeight == 0) {
					narrowHeight = textHeight;
				} else if (extraWidth == 700) {
					assertTrue(textHeight < narrowHeight, "Widening the panel must reduce the wrapped text height");
				} else {
					assertEquals(narrowHeight, textHeight, "Narrowing again must restore all wrapped lines");
				}
				for (JLabel description : descriptions) {
					View view = (View) description.getClientProperty(BasicHTML.propertyKey);
					assertTrue(description.getHeight() >= Math.ceil(view.getPreferredSpan(View.Y_AXIS)),
							"All wrapped lines must remain visible");
					Component child = description;
					while (child != content) {
						Container parent = child.getParent();
						assertTrue(child.getY() + child.getHeight() <= parent.getHeight(),
								"Sections must grow to contain the reflowed text");
						child = parent;
					}
				}
			}
		});
	}

	@Test
	public void testFirstExpansionWrapsWithoutResizing() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required for Swing validation");
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());
		JDialog[] dialog = new JDialog[1];
		LookAndFeel originalLookAndFeel = UIManager.getLookAndFeel();
		List<JLabel> descriptions = new ArrayList<>();
		List<CollapsiblePanel> sections = new ArrayList<>();
		try {
			SwingUtilities.invokeAndWait(() -> {
				FlatLightLaf.setup();
				SimulationWarningsPanel panel = new SimulationWarningsPanel(simulation, false);
				sections.addAll(findComponents(panel, CollapsiblePanel.class));
				sections.get(1).setExpanded(true);
				descriptions.addAll(findComponents(sections.get(1), JLabel.class).stream()
						.filter(label -> label.getClientProperty(BasicHTML.propertyKey) != null).toList());
				descriptions.forEach(label -> label.setText("<html>"
						+ "Warn when a recovery device deploys at an unsafe speed. ".repeat(12) + "</html>"));
				sections.get(1).setExpanded(false);
				dialog[0] = new JDialog();
				dialog[0].setContentPane(panel);
				dialog[0].setSize(1200, 700);
				dialog[0].setVisible(true);
			});
			flushSwingValidation();
			SwingUtilities.invokeAndWait(() -> sections.get(1).getHeaderButton().doClick());
			flushSwingValidation();
			SwingUtilities.invokeAndWait(() -> {
				assertEquals(1200, dialog[0].getWidth(), "The dialog must not need a resize to wrap correctly");
				for (JLabel description : descriptions) {
					View view = (View) description.getClientProperty(BasicHTML.propertyKey);
					assertEquals(description.getWidth(), (int) view.getPreferredSpan(View.X_AXIS),
							"First expansion must wrap at the allocated width before measuring again");
					assertEquals(description.getPreferredSize().height, description.getHeight(),
							"First expansion must allocate the height for the actual text width");
					Component child = description;
					while (child != sections.get(1)) {
						Container parent = child.getParent();
						assertTrue(child.getY() + child.getHeight() <= parent.getHeight(),
								"The expanded section must contain every wrapped line");
						child = parent;
					}
				}
			});
		} finally {
			SwingUtilities.invokeAndWait(() -> {
				if (dialog[0] != null) {
					dialog[0].dispose();
				}
				try {
					UIManager.setLookAndFeel(originalLookAndFeel);
				} catch (UnsupportedLookAndFeelException e) {
					throw new AssertionError(e);
				}
			});
		}
	}

	/** Drains queued revalidation without manually laying out or resizing components. */
	private static void flushSwingValidation() throws Exception {
		for (int pass = 0; pass < 4; pass++) {
			SwingUtilities.invokeAndWait(() -> RepaintManager.currentManager(null).validateInvalidComponents());
		}
	}

	@Test
	public void testWarningsResizeInsideSimulationDialog() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required to construct the dialog");
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket());

		SwingUtilities.invokeAndWait(() -> {
			SimulationConfigDialog dialog = new SimulationConfigDialog(null, document, false, simulation);
			try {
				dialog.switchToWarningsTab();
				SimulationWarningsPanel warnings = findComponents(dialog, SimulationWarningsPanel.class).get(0);
				JTabbedPane tabs = findComponents(warnings, JTabbedPane.class).get(0);
				tabs.setSelectedIndex(1);
				findComponents(warnings, CollapsiblePanel.class).get(1).setExpanded(true);
				JScrollPane scrollPane = (JScrollPane) tabs.getSelectedComponent();
				for (int[] size : new int[][] {{1000, 800}, {600, 800}, {1000, 350}, {600, 350}, {1000, 800}}) {
					dialog.setSize(size[0], size[1]);
					dialog.validate();
					assertTrue(scrollPane.getViewport().getExtentSize().width > 0);
					assertTrue(scrollPane.getViewport().getExtentSize().height > 0);
					// Every ancestor must fit: testing only the inner panel misses outer-tab clipping.
					Component child = scrollPane;
					while (child != dialog.getContentPane()) {
						Container parent = child.getParent();
						assertTrue(child.getX() >= 0 && child.getX() + child.getWidth() <= parent.getWidth(),
								"Warnings must fit horizontally: " + child.getClass().getSimpleName() + child.getBounds()
										+ " in " + parent.getClass().getSimpleName() + parent.getBounds());
						assertTrue(child.getY() >= 0 && child.getY() + child.getHeight() <= parent.getHeight(),
								"Warnings must fit vertically in " + parent.getClass().getSimpleName());
						child = parent;
					}
				}
			} finally {
				dialog.dispose();
			}
		});
	}

	private static void layoutRecursively(Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container childContainer) {
				layoutRecursively(childContainer);
			}
		}
	}

	private static <T extends Component> List<T> findComponents(Component root, Class<T> type) {
		List<T> matches = new ArrayList<>();
		if (type.isInstance(root)) {
			matches.add(type.cast(root));
		}
		if (root instanceof Container container) {
			for (Component child : container.getComponents()) {
				matches.addAll(findComponents(child, type));
			}
		}
		return matches;
	}
}
