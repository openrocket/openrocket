package info.openrocket.swing.gui.simulation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.utils.CoreServicesModule;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

public class MultiLevelWindEditDialog extends JDialog {
	private final MultiLevelWindTable windTable;
	private final WindLevelVisualizationDialog.WindLevelVisualization visualization;
	private static final Translator trans = Application.getTranslator();

	public MultiLevelWindEditDialog(Window owner, MultiLevelPinkNoiseWindModel model) {
		super(owner, trans.get("WindProfileEditorDlg.title"), ModalityType.APPLICATION_MODAL);

		// Create main panel with split layout
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true); // Smoother resizing

		// Create left panel with table
		windTable = new MultiLevelWindTable(model);
		
		// Create and configure the scroll pane
		JScrollPane tableScrollPane = new JScrollPane(windTable.getRowsPanel()) {
			// Override scrollPane preferred size to ensure consistent width
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				// Keep preferred width from getting smaller than header panel
				int minWidth = windTable.getHeaderPanel().getPreferredSize().width;
				size.width = Math.max(size.width, minWidth);
				return size;
			}
		};
		
		tableScrollPane.setColumnHeaderView(windTable.getHeaderPanel());
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
		tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Create visualization panel
		visualization = new WindLevelVisualizationDialog.WindLevelVisualization(model, 
				UnitGroup.UNITS_DISTANCE.getSIUnit(), 
				UnitGroup.UNITS_WINDSPEED.getSIUnit());
		visualization.setPreferredSize(new Dimension(300, 400));

		// Set up synchronization between table and visualization
		windTable.addChangeListener(visualization);
		windTable.addSelectionListener(visualization::setSelectedLevel);

		// Add visualization controls
		JPanel visPanel = new JPanel(new BorderLayout());
		visPanel.add(visualization, BorderLayout.CENTER);

		JCheckBox showDirectionsCheckBox = new JCheckBox(trans.get("WindProfileEditorDlg.checkbox.ShowWindDirections"));
		showDirectionsCheckBox.setSelected(true);
		showDirectionsCheckBox.addActionListener(e -> {
			visualization.setShowDirections(showDirectionsCheckBox.isSelected());
			visualization.repaint();
		});

		JPanel visControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		visControlPanel.add(showDirectionsCheckBox);
		visPanel.add(visControlPanel, BorderLayout.SOUTH);

		// Add table controls (add button, etc.)
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);

		JButton addRowButton = new JButton(trans.get("WindProfileEditorDlg.button.AddNewLevel"));
		addRowButton.setIcon(Icons.FILE_NEW);
		addRowButton.addActionListener(e -> windTable.addRow());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		buttonPanel.add(addRowButton);

		tablePanel.add(buttonPanel, BorderLayout.SOUTH);

		// Add panels to split pane
		splitPane.setLeftComponent(tablePanel);
		splitPane.setRightComponent(visPanel);
		splitPane.setResizeWeight(0.6); // 60% to table initially

		// Dialog buttons
		JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(trans.get("button.close"));
		closeButton.addActionListener(e -> dispose());
		dialogButtonPanel.add(closeButton);

		// Main layout
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);
		contentPane.add(dialogButtonPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		setSize(900, 500);
		setLocationRelativeTo(owner);
		
		// Set minimum size to ensure UI doesn't get too cramped
		setMinimumSize(new Dimension(700, 400));
	}

	public static void main(String[] args) {
		com.google.inject.Module applicationModule = new CoreServicesModule();
		com.google.inject.Module pluginModule = new PluginModule();

		Injector injector = Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);

		MultiLevelPinkNoiseWindModel model = new MultiLevelPinkNoiseWindModel();
		model.addWindLevel(0, 0, 0);
		model.addWindLevel(1000, 5, 5);
		model.addWindLevel(2000, 10, 10);

		MultiLevelWindEditDialog dialog = new MultiLevelWindEditDialog(null, model);
		dialog.setVisible(true);
	}
}
