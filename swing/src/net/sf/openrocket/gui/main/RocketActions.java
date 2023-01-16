package net.sf.openrocket.gui.main;


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.dialogs.ScaleDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class that holds Actions for common rocket and simulation operations such as
 * cut/copy/paste/delete etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketActions {

	public static final KeyStroke CUT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_X,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke COPY_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_C,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke PASTE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_V,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke DUPLICATE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_D,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke EDIT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_E,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	
	private final OpenRocketDocument document;
	private final Rocket rocket;
	private final BasicFrame parentFrame;
	private final DocumentSelectionModel selectionModel;
	private final SimulationPanel simulationPanel;


	private final RocketAction deleteComponentAction;
	private final RocketAction deleteSimulationAction;
	private final RocketAction deleteAction;
	private final RocketAction cutAction;
	private final RocketAction copyAction;
	private final RocketAction pasteAction;
	private final RocketAction duplicateAction;
	private final RocketAction editAction;
	private final RocketAction scaleAction;
	private final RocketAction moveUpAction;
	private final RocketAction moveDownAction;
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(RocketActions.class);


	public RocketActions(OpenRocketDocument document, DocumentSelectionModel selectionModel,
			BasicFrame parentFrame, SimulationPanel simulationPanel) {
		this.document = document;
		this.rocket = document.getRocket();
		this.selectionModel = selectionModel;
		this.parentFrame = parentFrame;
		this.simulationPanel = simulationPanel;

		// Add action also to updateActions()
		this.deleteAction = new DeleteAction();
		this.deleteComponentAction = new DeleteComponentAction();
		this.deleteSimulationAction = new DeleteSimulationAction();
		this.cutAction = new CutAction();
		this.copyAction = new CopyAction();
		this.pasteAction = new PasteAction();
		this.duplicateAction = new DuplicateAction();
		this.editAction = new EditAction();
		this.scaleAction = new ScaleAction();
		this.moveUpAction = new MoveUpAction();
		this.moveDownAction = new MoveDownAction();

		OpenRocketClipboard.addClipboardListener(this.pasteAction);
		updateActions();

		// Update all actions when tree selection or rocket changes

		selectionModel.addDocumentSelectionListener(new DocumentSelectionListener() {
			@Override
			public void valueChanged(int changeType) {
				updateActions();
			}
		});
		document.getRocket().addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				updateActions();
			}
		});
	}

	/**
	 * Update the state of all of the actions.
	 */
	private void updateActions() {
		deleteAction.clipboardChanged();
		cutAction.clipboardChanged();
		copyAction.clipboardChanged();
		pasteAction.clipboardChanged();
		duplicateAction.clipboardChanged();
		editAction.clipboardChanged();
		scaleAction.clipboardChanged();
		moveUpAction.clipboardChanged();
		moveDownAction.clipboardChanged();
	}
	
	
	

	public Action getDeleteComponentAction() {
		return deleteAction;
	}

	public Action getDeleteSimulationAction() {
		return deleteAction;
	}

	public Action getDeleteAction() {
		return deleteAction;
	}

	public Action getCutAction() {
		return cutAction;
	}
	
	public Action getCopyAction() {
		return copyAction;
	}
	
	public Action getPasteAction() {
		return pasteAction;
	}

	public Action getDuplicateAction() {
		return duplicateAction;
	}
	
	public Action getEditAction() {
		return editAction;
	}

	public Action getScaleAction() {
		return scaleAction;
	}
	
	public Action getMoveUpAction() {
		return moveUpAction;
	}
	
	public Action getMoveDownAction() {
		return moveDownAction;
	}

	/**
	 * Tie an action to a JButton, without using the icon or text of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action and then set the
	 * icon to null and set the button text: this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 * @param text text to display on the button
	 */
	public static void tieActionToButtonNoIcon(JButton button, Action action, String text) {
		button.setAction(action);
		button.setIcon(null);
		button.setText(text);
	}

	/**
	 * Tie an action to a JButton, without using the icon of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action and then set the
	 * icon to null: this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 */
	public static void tieActionToButtonNoIcon(JButton button, Action action) {
		button.setAction(action);
		button.setIcon(null);
	}

	/**
	 * Tie an action to a JButton, without using the text of the action for the button.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action:
	 * this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 * @param text text to display on the button
	 */
	public static void tieActionToButton(JButton button, Action action, String text) {
		button.setAction(action);
		button.setText(text);
	}

	/**
	 * Tie an action to a JButton.
	 *
	 * For any smartass that wants to know why you don't just initialize the JButton with the action:
	 * this causes a bug where the text of the icon becomes much smaller than is intended.
	 *
	 * @param button button to tie the action to
	 * @param action action to tie to the button
	 */
	public static void tieActionToButton(JButton button, Action action) {
		button.setAction(action);
	}
	
	
	////////  Helper methods for the actions

	private boolean isDeletable(RocketComponent c) {
		// Sanity check
		if (c == null || c.getParent() == null)
			return false;

		// Cannot remove Rocket
		if (c instanceof Rocket)
			return false;

		// Cannot remove last stage, except from Boosters
		if ((c instanceof AxialStage) && !(c instanceof ParallelStage) && (c.getParent().getChildCount() == 1)) {
			return false;
		}

		return true;
	}

	private boolean isDeletable(List<RocketComponent> components) {
		if (components == null || components.size() == 0) return false;
		for (RocketComponent component : components) {
			if (!isDeletable(component)) return false;
		}
		return true;
	}

	private void delete(RocketComponent c) {
		if (!isDeletable(c)) {
			throw new IllegalArgumentException("Report bug!  Component " + c + 
					" not deletable.");
		}

		RocketComponent parent = c.getParent();
		parent.removeChild(c);
	}

	private void delete(List<RocketComponent> components) {
		if (!isDeletable(components)) {
			throw new IllegalArgumentException("Report bug!  Components not deletable.");
		}

		for (RocketComponent component : components) {
			delete(component);
		}
	}

	private boolean isCopyable(RocketComponent c) {
		if (c==null)
			return false;
		if (c instanceof Rocket)
			return false;
		return true;
	}

	private boolean isCopyable(List<RocketComponent> components) {
		if (components == null || components.size() == 0) return false;
		for (RocketComponent component : components) {
			if (!isCopyable(component)) return false;
		}
		return true;
	}

	/**
	 * Copies components, but with maintaining parent-relations with the newly copied components
	 * @param components components to copy
	 * @return copied components
	 */
	public static List<RocketComponent> copyComponentsMaintainParent(List<RocketComponent> components) {
		if (components == null) return null;
		List<RocketComponent> result = new LinkedList<>();
		for (RocketComponent component : components) {
			result.add(component.copy());
		}

		for (int i = 0; i < components.size(); i++) {
			if (components.contains(components.get(i).getParent())) {
				RocketComponent originalParent = components.get(i).getParent();
				int originalParentIdx = components.indexOf(originalParent);

				result.get(originalParentIdx).addChild(result.get(i));
			} else if (RocketComponent.listContainsParent(components, components.get(i))){
				RocketComponent originalParent = components.get(i);
				while (originalParent != components.get(i)) {
					if (components.contains(originalParent.getParent())) {
						originalParent = originalParent.getParent();
					}
				}
				int originalParentIdx = components.indexOf(originalParent);
				result.get(originalParentIdx).addChild(result.get(i));
			}
		}

		return result;
	}

	/**
	 * If the children of a parent are not selected, add them to the selection. Do this recursively for the children
	 * of the children as well.
	 * @param selections list of currently selected components
	 * @param parent parent component to parse the children of
	 */
	private void selectAllUnselectedInParent(List<RocketComponent> selections, RocketComponent parent) {
		if (parent.getChildCount() == 0) return;

		boolean noChildrenSelected = true;
		for (RocketComponent child : parent.getChildren()) {
			if (selections.contains(child)) {
				noChildrenSelected = false;
				break;
			}
		}

		// Add children to selection if none of them were selected
		if (noChildrenSelected) {
			selections.addAll(parent.getChildren());
		}

		// Recursively select all unselected children. Unselected children will not undergo this recursive updating.
		for (RocketComponent child : parent.getChildren()) {
			if (!noChildrenSelected && selections.contains(child)) {
				selectAllUnselectedInParent(selections, child);
			}
		}
	}

	/**
	 * This method fills in some selections in selections. If there is a parent where none of its children are selected,
	 * add all the children to the selection. If there is a component whose parent is not selected, but it does have
	 * a super-parent in the selection, add the parent to the selection.
	 * @param selections component selections to be filled up
	 */
	private void fillInMissingSelections(List<RocketComponent> selections) {
		List<RocketComponent> initSelections = new LinkedList<>(selections);
		for (RocketComponent component : initSelections) {
			selectAllUnselectedInParent(selections, component);

			// If there is a component in the selection, but its parent (or the parent of the parent) is still
			// not selected, add it to the selection
			RocketComponent temp = component;
			if (RocketComponent.listContainsParent(selections, temp) && !selections.contains(temp.getParent())) {
				while (!selections.contains(temp.getParent())) {
					selections.add(temp.getParent());
					temp = temp.getParent();
				}
			}
		}
	}

	
	private boolean isSimulationSelected() {
		Simulation[] selection = selectionModel.getSelectedSimulations();
		return (selection != null  &&  selection.length > 0);
	}
	
	
	
	private boolean verifyDeleteSimulation() {
		boolean verify = Application.getPreferences().getBoolean(Preferences.CONFIRM_DELETE_SIMULATION, true);
		if (verify) {
			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox dontAsk = new JCheckBox(trans.get("RocketActions.checkbox.Donotaskmeagain"));
			panel.add(dontAsk,"wrap");
			//// You can change the default operation in the preferences. 
			panel.add(new StyledLabel(trans.get("RocketActions.lbl.Youcanchangedefop"),-2));

			int ret = JOptionPane.showConfirmDialog(
					parentFrame,
					new Object[] {
					//// Delete the selected simulations?		
					trans.get("RocketActions.showConfirmDialog.lbl1"),
					//// <html><i>This operation cannot be undone.</i>
					trans.get("RocketActions.showConfirmDialog.lbl2"),
					"",
					panel },
					//// Delete simulations
					trans.get("RocketActions.showConfirmDialog.title"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return false;

			if (dontAsk.isSelected()) {
				Application.getPreferences().putBoolean(Preferences.CONFIRM_DELETE_SIMULATION, false);
			}
		}

		return true;
	}


	/**
	 * Return the component and position to which the current clipboard
	 * should be pasted.  Returns null if the clipboard is empty or if the
	 * clipboard cannot be pasted to the current selection.
	 * 
	 * @param   srcComponent	the component to be copy-pasted.
	 * @param	destComponent	the component where srcComponent should be pasted to.
	 * @return  a Pair with both components defined, or null.
	 */
	private Pair<RocketComponent, Integer> getPastePosition(RocketComponent srcComponent, RocketComponent destComponent) {
		if (destComponent == null)
			return new Pair<>(null, null);

		if (srcComponent == null)
			return new Pair<>(null, null);

		if (destComponent.isCompatible(srcComponent))
			return new Pair<>(destComponent, destComponent.getChildCount());

		RocketComponent parent = destComponent.getParent();
		return getPastePositionFromParent(srcComponent, destComponent, parent);
	}

	private Pair<RocketComponent, Integer> getPastePositionFromParent(RocketComponent srcComponent, RocketComponent destComponent,
																	  RocketComponent parent) {
		if (parent != null && parent.isCompatible(srcComponent)) {
			int index = parent.getChildPosition(destComponent) + 1;
			return new Pair<>(parent, index);
		}

		return new Pair<>(null, null);
	}

	/**
	 * Return the component and position to which the current clipboard
	 * should be pasted.  Returns null if the clipboard is empty or if the
	 * clipboard cannot be pasted to the current selection.
	 *
	 * @param   clipboard	the component on the clipboard.
	 * @return  a Pair with both components defined, or null.
	 */
	private List<Pair<RocketComponent, Integer>> getPastePositions(List<RocketComponent> clipboard) {
		List<Pair<RocketComponent, Integer>> result = new LinkedList<>();
		RocketComponent selected = selectionModel.getSelectedComponent();
		for (RocketComponent component : clipboard) {
			Pair<RocketComponent, Integer> position = getPastePosition(component, selected);
			if (position != null) {
				result.add(position);
			}
		}
		return result;
	}

	
	

	///////  Action classes

	private abstract class RocketAction extends AbstractAction implements ClipboardListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public abstract void clipboardChanged();
	}


	/**
	 * Action that deletes the selected component.
	 */
	private class DeleteComponentAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DeleteComponentAction() {
			//// Delete
			this.putValue(NAME, trans.get("RocketActions.DelCompAct.Delete"));
			//// Delete the selected component.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DelCompAct.ttip.Delete"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
//			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = new ArrayList<>(selectionModel.getSelectedComponents());
			if (components.size() == 0) return;
			components.sort(Comparator.comparing(c -> c.getParent().getChildPosition(c)));

			if (components.size() == 1) {
				document.addUndoPosition("Delete " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Delete components");
			}

			for (RocketComponent component : components) {
				deleteComponent(component);
			}
		}

		private void deleteComponent(RocketComponent component) {
			if (isDeletable(component)) {
				ComponentConfigDialog.disposeDialog();

				try {
					component.getRocket().removeComponentChangeListener(ComponentConfigDialog.getDialog());

					delete(component);
				} catch (IllegalStateException ignored) { }
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isDeletable(selectionModel.getSelectedComponent()));
		}
	}


	
	/**
	 * Action that deletes the selected component.
	 */
	private class DeleteSimulationAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DeleteSimulationAction() {
			//// Delete
			this.putValue(NAME, trans.get("RocketActions.DelSimuAct.Delete"));
			//// Delete the selected simulation.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DelSimuAct.ttip.Delete"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
//			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Simulation[] sims = selectionModel.getSelectedSimulations();
			if (sims.length > 0) {
				if (verifyDeleteSimulation()) {
					for (Simulation s: sims) {
						document.removeSimulation(s);
					}
				}
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isSimulationSelected());
		}
	}


	
	/**
	 * Action that deletes the selected component.
	 */
	private class DeleteAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DeleteAction() {
			//// Delete
			this.putValue(NAME, trans.get("RocketActions.DelAct.Delete"));
			//// Delete the selected component or simulation.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DelAct.ttip.Delete"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSimulationSelected()) {
				deleteSimulationAction.actionPerformed(e);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			} else {
				deleteComponentAction.actionPerformed(e);
				parentFrame.selectTab(BasicFrame.DESIGN_TAB);
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isDeletable(selectionModel.getSelectedComponent()) ||
					isSimulationSelected());
		}
	}


	
	/**
	 * Action the cuts the selected component (copies to clipboard and deletes).
	 */
	private class CutAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public CutAction() {
			//// Cut
			this.putValue(NAME, trans.get("RocketActions.CutAction.Cut"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_T);
			this.putValue(ACCELERATOR_KEY, CUT_KEY_STROKE);
			//// Cut this component or simulation to the clipboard and remove from this design
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.CutAction.ttip.Cut"));
			this.putValue(SMALL_ICON, Icons.EDIT_CUT);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (components.size() > 0) {
				components = new ArrayList<>(components);
				fillInMissingSelections(components);

				components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));
			}
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (isDeletable(components) && isCopyable(components)) {
				ComponentConfigDialog.disposeDialog();

				if (components.size() == 1) {
					document.addUndoPosition("Cut " + components.get(0).getComponentName());
				} else {
					document.addUndoPosition("Cut components");
				}

				List<RocketComponent> copiedComponents = new LinkedList<>(copyComponentsMaintainParent(components));
				copiedComponents.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

				OpenRocketClipboard.setClipboard(copiedComponents);
				delete(components);
				parentFrame.selectTab(BasicFrame.DESIGN_TAB);
			} else if (isSimulationSelected()) {

				Simulation[] simsCopy = new Simulation[sims.length];
				for (int i=0; i < sims.length; i++) {
					simsCopy[i] = sims[i].copy();
				}
				OpenRocketClipboard.setClipboard(simsCopy);

				for (Simulation s: sims) {
					document.removeSimulation(s);
				}
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			}
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			this.setEnabled((isDeletable(components) && isCopyable(components)) || isSimulationSelected());
		}
	}



	/**
	 * Action that copies the selected component to the clipboard.
	 */
	private class CopyAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public CopyAction() {
			//// Copy
			this.putValue(NAME, trans.get("RocketActions.CopyAct.Copy"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			this.putValue(ACCELERATOR_KEY, COPY_KEY_STROKE);
			//// Copy this component (and subcomponents) to the clipboard.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.CopyAct.ttip.Copy"));
			this.putValue(SMALL_ICON, Icons.EDIT_COPY);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			if (components.size() > 0) {
				components = new ArrayList<>(components);
				fillInMissingSelections(components);

				components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));
			}
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (isCopyable(components)) {
				List<RocketComponent> copiedComponents = new LinkedList<>(copyComponentsMaintainParent(components));
				copiedComponents.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

				OpenRocketClipboard.setClipboard(copiedComponents);
				parentFrame.selectTab(BasicFrame.DESIGN_TAB);
			} else if (sims != null && sims.length > 0) {
				Simulation[] simsCopy = new Simulation[sims.length];
				for (int i=0; i < sims.length; i++) {
					simsCopy[i] = sims[i].copy();
				}

				OpenRocketClipboard.setClipboard(simsCopy);
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isCopyable(selectionModel.getSelectedComponent()) ||
					isSimulationSelected());
		}
		
	}



	/**
	 * Action that pastes the current clipboard to the selected position.
	 * It first tries to paste the component to the end of the selected component
	 * as a child, and after that as a sibling after the selected component. 
	 */
	private class PasteAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public PasteAction() {
			//// Paste
			this.putValue(NAME, trans.get("RocketActions.PasteAct.Paste"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_P);
			this.putValue(ACCELERATOR_KEY, PASTE_KEY_STROKE);
			//// Paste the component or simulation on the clipboard to the design.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.PasteAct.ttip.Paste"));
			this.putValue(SMALL_ICON, Icons.EDIT_PASTE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = new LinkedList<>(OpenRocketClipboard.getClipboardComponents());
			Simulation[] sims = OpenRocketClipboard.getClipboardSimulations();

			if (components.size() > 0) {
				ComponentConfigDialog.disposeDialog();
				
				List<RocketComponent> pasted = new LinkedList<>();
				for (RocketComponent component : components) {
					pasted.add(component.copy());
				}

				List<Pair<RocketComponent, Integer>> positions = getPastePositions(pasted);

				if (pasted.size() == 1) {
					document.addUndoPosition("Paste " + pasted.get(0).getComponentName());
				} else {
					document.addUndoPosition("Paste components");
				}

				List<RocketComponent> successfullyPasted = new LinkedList<>();
				for (int i = 0; i < pasted.size(); i++) {
					if (positions.get(i) == null) {
						JOptionPane.showMessageDialog(null,
								String.format(trans.get("RocketActions.PasteAct.invalidPosition.msg"),
										pasted.get(i).getComponentName()),
								trans.get("RocketActions.PasteAct.invalidPosition.title"), JOptionPane.WARNING_MESSAGE);
					} else {
						RocketComponent parent = positions.get(i).getU();
						RocketComponent child = pasted.get(i);
						if (parent != null && parent.isCompatible(child)) {
							parent.addChild(child, positions.get(i).getV());
							successfullyPasted.add(pasted.get(i));
						} else {
							log.warn("Pasted component {} is not compatible with {}", child, parent);
						}
					}
				}

				selectionModel.setSelectedComponents(successfullyPasted);
				
				parentFrame.selectTab(BasicFrame.DESIGN_TAB);
				
			} else if (sims != null) {
				
				ArrayList<Simulation> copySims = new ArrayList<Simulation>();

				for (Simulation s: sims) {
					Simulation copy = s.duplicateSimulation(rocket);
					String name = copy.getName();
					if (name.matches(OpenRocketDocument.SIMULATION_NAME_PREFIX + "[0-9]+ *")) {
						copy.setName(document.getNextSimulationName());
					}
					document.addSimulation(copy);
					copySims.add(copy);
				}
				selectionModel.setSelectedSimulations(copySims.toArray(new Simulation[0]));
				
				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(
					(getPastePositions(OpenRocketClipboard.getClipboardComponents()).size() > 0) ||
					(OpenRocketClipboard.getClipboardSimulations() != null));
		}
	}


	/**
	 * Action that duplicates the selected component.
	 */
	private class DuplicateAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public DuplicateAction() {
			//// Copy
			this.putValue(NAME, trans.get("RocketActions.DuplicateAct.Duplicate"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			this.putValue(ACCELERATOR_KEY, DUPLICATE_KEY_STROKE);
			//// Copy this component (and subcomponents) to the clipboard.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.DuplicateAct.ttip.Duplicate"));
			this.putValue(SMALL_ICON, Icons.EDIT_DUPLICATE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			List<RocketComponent> topComponents = new LinkedList<>();		// Components without a parent component in <components>
			if (components.size() > 0) {
				components.sort(Comparator.comparing(c -> c.getParent() != null ? c.getParent().getChildPosition(c) : 0));
				components = new ArrayList<>(components);
				fillInMissingSelections(components);
			} else {
				return;
			}
			for (RocketComponent c: components) {
				if (!RocketComponent.listContainsParent(components, c)) {
					topComponents.add(c);
				}
			}
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (isCopyable(components)) {
				if (ComponentConfigDialog.isDialogVisible())
					ComponentConfigDialog.disposeDialog();

				List<RocketComponent> copiedComponents = copyComponentsMaintainParent(components);
				OpenRocketClipboard.setClipboard(copiedComponents);
				copiedComponents = new LinkedList<>(OpenRocketClipboard.getClipboardComponents());

				List<RocketComponent> duplicateComponents = new LinkedList<>();
				for (RocketComponent component : copiedComponents) {
					duplicateComponents.add(component.copy());
				}

				List<Pair<RocketComponent, Integer>> positions = new LinkedList<>();
				for (RocketComponent component : duplicateComponents) {
					Pair<RocketComponent, Integer> pos;
					if (RocketComponent.listContainsParent(duplicateComponents, component)) {
						pos = getPastePosition(component, component.getParent());
					} else {
						int compIdx = duplicateComponents.indexOf(component);
						RocketComponent pasteParent = topComponents.get(compIdx).getParent();
						pos = getPastePosition(component, pasteParent);
					}
					positions.add(pos);
				}

				if (duplicateComponents.size() == 1) {
					document.addUndoPosition("Duplicate " + duplicateComponents.get(0).getComponentName());
				} else {
					document.addUndoPosition("Duplicate components");
				}

				Collections.reverse(duplicateComponents);
				Collections.reverse(positions);

				for (int i = 0; i < duplicateComponents.size(); i++) {
					positions.get(i).getU().addChild(duplicateComponents.get(i), positions.get(i).getV());
				}

				selectionModel.setSelectedComponents(duplicateComponents);

				parentFrame.selectTab(BasicFrame.DESIGN_TAB);
			} else if (sims != null && sims.length > 0) {
				ArrayList<Simulation> copySims = new ArrayList<Simulation>();

				// TODO: the undoing doesn't do anything...
				if (sims.length == 1) {
					document.addUndoPosition("Duplicate " + sims[0].getName());
				} else {
					document.addUndoPosition("Duplicate simulations");
				}

				for (Simulation s: sims) {
					Simulation copy = s.duplicateSimulation(rocket);
					String name = copy.getName();
					if (name.matches(OpenRocketDocument.SIMULATION_NAME_PREFIX + "[0-9]+ *")) {
						copy.setName(document.getNextSimulationName());
					}
					document.addSimulation(copy);
					copySims.add(copy);
				}

				selectionModel.setSelectedSimulations(copySims.toArray(new Simulation[0]));

				parentFrame.selectTab(BasicFrame.SIMULATION_TAB);
			}
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(isCopyable(selectionModel.getSelectedComponent()) ||
					isSimulationSelected());
		}

	}
	

	
	/**
	 * Action to edit the currently selected component.
	 */
	private class EditAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public EditAction() {
			//// Edit
			this.putValue(NAME, trans.get("RocketActions.EditAct.Edit"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
			this.putValue(ACCELERATOR_KEY, EDIT_KEY_STROKE);
			//// Edit the selected component.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.EditAct.ttip.Edit"));
			this.putValue(SMALL_ICON, Icons.EDIT_EDIT);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components = selectionModel.getSelectedComponents();
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (components.size() > 0) {
				if (ComponentConfigDialog.isDialogVisible())
					ComponentConfigDialog.disposeDialog();

				RocketComponent component = components.get(0);
				component.clearConfigListeners();
				if (components.size() > 1) {
					for (int i = 1; i < components.size(); i++) {
						RocketComponent listener = components.get(i);
						listener.clearConfigListeners();	// Make sure all the listeners are cleared (should not be possible, but just in case)
						component.addConfigListener(listener);
					}
				}
				ComponentConfigDialog.showDialog(parentFrame, document, component);
			} else if (sims != null && sims.length > 0 && (simulationPanel != null)) {
				simulationPanel.editSimulation();
			}
		}

		@Override
		public void clipboardChanged() {
			List<RocketComponent> components = selectionModel.getSelectedComponents();

			this.setEnabled(components.size() > 0 || isSimulationSelected());
		}
	}

	/**
	 * Action to scale the currently selected component.
	 */
	private class ScaleAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public ScaleAction() {
			//// Scale
			this.putValue(NAME, trans.get("RocketActions.ScaleAct.Scale"));
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.ScaleAct.ttip.Scale"));
			this.putValue(SMALL_ICON, Icons.EDIT_SCALE);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			log.info(Markers.USER_MARKER, "Scale... selected");
			ScaleDialog dialog = new ScaleDialog(document, selectionModel.getSelectedComponents(), null);
			dialog.setVisible(true);
			dialog.dispose();
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(true);
		}
	}
	


	
	/**
	 * Action to move the selected component upwards in the parent's child list.
	 */
	private class MoveUpAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public MoveUpAction() {
			//// Move up
			this.putValue(NAME, trans.get("RocketActions.MoveUpAct.Moveup"));
			this.putValue(SMALL_ICON, Icons.UP);
			//// Move this component upwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveUpAct.ttip.Moveup"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components =  selectionModel.getSelectedComponents();
			if (components.size() == 0) return;
			components = new ArrayList<>(components);
			components.sort(Comparator.comparing(c -> c.getParent() != null ? c.getParent().getChildPosition(c) : 0));

			if (components.size() == 1) {
				document.addUndoPosition("Move " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Move components");
			}

			for (RocketComponent component : components) {
				// Only move top components, don't move its children
				if (!RocketComponent.listContainsParent(components, component)) {
					moveUp(component);
				}
			}
			rocket.fireComponentChangeEvent(ComponentChangeEvent.TREE_CHANGE);
			selectionModel.setSelectedComponents(components);
		}

		private void moveUp(RocketComponent component) {
			if (!canMove(component))
				return;

			ComponentConfigDialog.disposeDialog();

			RocketComponent parent = component.getParent();
			parent.moveChild(component, parent.getChildPosition(component) - 1);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponents()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) > 0)
				return true;
			return false;
		}

		private boolean canMove(List<RocketComponent> components) {
			if (components == null || components.size() == 0)
				return false;

			for (RocketComponent component : components) {
				if (!RocketComponent.listContainsParent(components, component) && !canMove(component))
					return false;
			}
			return true;
		}
	}



	/**
	 * Action to move the selected component down in the parent's child list.
	 */
	private class MoveDownAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public MoveDownAction() {
			//// Move down
			this.putValue(NAME, trans.get("RocketActions.MoveDownAct.Movedown"));
			this.putValue(SMALL_ICON, Icons.DOWN);
			//// Move this component downwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveDownAct.ttip.Movedown"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<RocketComponent> components =  selectionModel.getSelectedComponents();
			if (components.size() == 0) return;
			components = new ArrayList<>(components);
			components.sort(Comparator.comparing(c -> c.getParent() != null ? -c.getParent().getChildPosition(c) : 0));

			if (components.size() == 1) {
				document.addUndoPosition("Move " + components.get(0).getComponentName());
			} else {
				document.addUndoPosition("Move components");
			}

			for (RocketComponent component : components) {
				// Only move top components, don't move its children
				if (!RocketComponent.listContainsParent(components, component)) {
					moveDown(component);
				}
			}
			rocket.fireComponentChangeEvent(ComponentChangeEvent.TREE_CHANGE);
			selectionModel.setSelectedComponents(components);
		}

		private void moveDown(RocketComponent component) {
			if (!canMove(component))
				return;

			ComponentConfigDialog.disposeDialog();

			RocketComponent parent = component.getParent();
			parent.moveChild(component, parent.getChildPosition(component) + 1);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponents()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) < parent.getChildCount()-1)
				return true;
			return false;
		}

		private boolean canMove(List<RocketComponent> components) {
			if (components == null || components.size() == 0)
				return false;

			for (RocketComponent component : components) {
				if (!RocketComponent.listContainsParent(components, component) && !canMove(component))
					return false;
			}
			return true;
		}
	}



}
