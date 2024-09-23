package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.main.componenttree.SelectableComponentTree;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ComponentSelectionDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();

	private final SelectableComponentTree componentTree;
	private List<RocketComponent> selectedComponents;

	public ComponentSelectionDialog(Window owner, OpenRocketDocument document, List<RocketComponent> enabledComponents,
									List<RocketComponent> initialSelection) {
		super(owner, "Select Components", ModalityType.APPLICATION_MODAL);

		// Component tree
		componentTree = new SelectableComponentTree(document, enabledComponents, initialSelection);
		JScrollPane scrollPane = new JScrollPane(componentTree);

		// Confirm button
		JButton confirmButton = new JButton(trans.get("ComponentSelectionDialog.btn.ConfirmSelection"));
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSelectedComponents();
				dispose();
			}
		});

		// Cancel button
		JButton cancelButton = new JButton(trans.get("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedComponents = null;
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(confirmButton);
		buttonPanel.add(cancelButton);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setSize(400, 500);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void updateSelectedComponents() {
		TreePath[] selectedPaths = componentTree.getSelectionPaths();
		selectedComponents = new ArrayList<>();
		if (selectedPaths != null) {
			for (TreePath path : selectedPaths) {
				Object component = path.getLastPathComponent();
				if (component instanceof RocketComponent) {
					selectedComponents.add((RocketComponent) component);
				}
			}
		}
	}

	public List<RocketComponent> showDialog() {
		setVisible(true);
		return selectedComponents;
	}
}
