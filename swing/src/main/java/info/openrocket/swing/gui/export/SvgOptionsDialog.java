package info.openrocket.swing.gui.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.svg.export.SVGExportOptions;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.components.SVGOptionPanel;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;
import info.openrocket.swing.gui.main.componenttree.SelectableComponentTree;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

import info.openrocket.swing.gui.util.ColorConversion;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal dialog wrapping the SVGOptionPanel with component selection tree.
 */
public class SvgOptionsDialog extends JDialog {
	public static final int COMPONENTS_TAB = 0;

	private static final Translator trans = Application.getTranslator();
	private final SVGOptionPanel optionsPanel;
	private final SelectableComponentTree componentTree;
	private final List<RocketComponent> exportableComponents;
	private JTabbedPane tabbedPane;
	private OpenRocketDocument document;
	private boolean confirmed = false;

	public SvgOptionsDialog(Frame owner, OpenRocketDocument document, List<RocketComponent> initiallySelectedComponents) {
		super(owner, "SVG Export Options", true);
		this.document = document;
		optionsPanel = new SVGOptionPanel(true);
		
		// Get all exportable components
		exportableComponents = SVGRocketPartsExporter.collectExportableComponents(document);

		// Filter initially selected components to only include exportable ones
		List<RocketComponent> initialSelection = new ArrayList<>();
		if (initiallySelectedComponents != null && !initiallySelectedComponents.isEmpty()) {
			for (RocketComponent component : initiallySelectedComponents) {
				if (exportableComponents.contains(component)) {
					initialSelection.add(component);
				}
			}
		}
		
		componentTree = new SelectableComponentTree(document, exportableComponents, initialSelection);
		
		initialize();
	}

	private void initialize() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		// Create panel for component tree with selection buttons
		JPanel componentsTabPanel = new JPanel(new BorderLayout());
		JScrollPane treeScrollPane = new JScrollPane(componentTree);
		Border originalBorder = treeScrollPane.getBorder();
		treeScrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 10), originalBorder));
		componentsTabPanel.add(treeScrollPane, BorderLayout.CENTER);
		
		// Add Select All/None buttons below the tree
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton selectAllButton = new JButton(trans.get("SVGOptionPanel.btn.selectAll"));
		JButton selectNoneButton = new JButton(trans.get("SVGOptionPanel.btn.selectNone"));
		selectAllButton.addActionListener(e -> selectAllComponents());
		selectNoneButton.addActionListener(e -> selectNoneComponents());
		buttonPanel.add(selectAllButton);
		buttonPanel.add(selectNoneButton);
		componentsTabPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		// Create tabbed pane with Components and Fin Guides tabs
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(trans.get("SVGOptionPanel.tab.components"), componentsTabPanel);
		// TODO: add other tabs here (e.g. Fin Guides)
		
		// Create split pane with options on left and tabbed pane on right
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionsPanel, tabbedPane);
		splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.4);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancel = new JButton("Cancel");
		JButton ok = new JButton("OK");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed = false;
				dispose();
			}
		});
				ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Validate based on selected tab
				int selectedTab = tabbedPane.getSelectedIndex();
				if (selectedTab == COMPONENTS_TAB) {
					// Components tab - check if at least one component is selected
					if (getSelectedComponents().isEmpty()) {
						JOptionPane.showMessageDialog(
								SvgOptionsDialog.this,
								trans.get("SVGOptionPanel.noSelection.message"),
								trans.get("SVGOptionPanel.noSelection.title"),
								JOptionPane.WARNING_MESSAGE
						);
						return; // Don't close the dialog
					}
				}
				// TODO: other tabs here (e.g. fin guides)
				
				// Store preferences before closing
				optionsPanel.storePreferences();
				confirmed = true;
				dispose();
			}
		});
		buttons.add(cancel);
		buttons.add(ok);
		getContentPane().add(buttons, BorderLayout.SOUTH);

		pack();
		// Set a wider initial size for the dialog
		if (getWidth() < 600) {
			setSize(Math.max(600, getWidth()), getHeight());
		}
		setLocationRelativeTo(getOwner());
	}

	public boolean showDialog(JComponent relativeTo) {
		setLocationRelativeTo(relativeTo);
		setVisible(true);
		return confirmed;
	}

	public boolean showDialog() {
		setVisible(true);
		return confirmed;
	}

	public Color getStrokeColor() { return optionsPanel.getStrokeColor(); }
	public double getStrokeWidth() { return optionsPanel.getStrokeWidth(); }
	public boolean isDrawCrosshair() { return optionsPanel.isDrawCrosshair(); }
	public Color getCrosshairColor() { return optionsPanel.getCrosshairColor(); }
	public double getCrosshairSize() { return optionsPanel.getCrosshairSizeMm(); }

	public SVGExportOptions getExportOptions() {
		double partSpacing = optionsPanel.getPartSpacing();
		
		return new SVGExportOptions(
			ColorConversion.fromAwtColor(getStrokeColor()),
			getStrokeWidth(),
			isDrawCrosshair(),
			ColorConversion.fromAwtColor(getCrosshairColor()),
			getCrosshairSize(),
			isShowLabels(),
			ColorConversion.fromAwtColor(getLabelColor()),
			partSpacing
		);
	}

	public boolean isShowLabels() {
		return optionsPanel.isShowLabels();
	}

	public void setShowLabels(boolean showLabels) {
		optionsPanel.setShowLabels(showLabels);
	}

	public Color getLabelColor() {
		return optionsPanel.getLabelColor();
	}

	public void setLabelColor(Color color) {
		optionsPanel.setLabelColor(color);
	}

	public void setFromPreferences(ApplicationPreferences prefs) {
		optionsPanel.setStrokeColor(ColorConversion.toAwtColor(prefs.getSVGStrokeColor()));
		optionsPanel.setStrokeWidth(prefs.getSVGStrokeWidth());
		optionsPanel.setDrawCrosshair(prefs.isSVGDrawCrosshair());
		optionsPanel.setCrosshairColor(ColorConversion.toAwtColor(prefs.getSVGCrosshairColor()));
		optionsPanel.setShowLabels(prefs.isSVGShowLabels());
		optionsPanel.setLabelColor(ColorConversion.toAwtColor(prefs.getSVGLabelColor()));
		// Crosshair size is loaded in SVGOptionPanel constructor
	}

	public SVGOptionPanel getOptionsPanel() {
		return optionsPanel;
	}

	/**
	 * Get the selected components from the component tree.
	 * @return List of selected components, or empty list if none selected
	 */
	public List<RocketComponent> getSelectedComponents() {
		List<RocketComponent> selected = new ArrayList<>();
		TreePath[] selectedPaths = componentTree.getSelectionPaths();
		if (selectedPaths != null) {
			for (TreePath path : selectedPaths) {
				Object component = path.getLastPathComponent();
				if (component instanceof RocketComponent) {
					selected.add((RocketComponent) component);
				}
			}
		}
		return selected;
	}

	/**
	 * Select all exportable components in the tree.
	 */
	private void selectAllComponents() {
		List<TreePath> paths = ComponentTreeModel.makeTreePaths(exportableComponents);
		componentTree.getSelectionModel().setSelectionPaths(paths.toArray(new TreePath[0]));
	}

	/**
	 * Deselect all components in the tree.
	 */
	private void selectNoneComponents() {
		componentTree.getSelectionModel().clearSelection();
	}

	/**
	 * Get the currently selected tab index.
	 * @return The selected tab index (COMPONENTS_TAB or FIN_GUIDES_TAB)
	 */
	public int getSelectedTab() {
		return tabbedPane.getSelectedIndex();
	}
}

