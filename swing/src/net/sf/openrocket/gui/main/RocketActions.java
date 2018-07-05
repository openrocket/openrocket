package net.sf.openrocket.gui.main;


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.Pair;



/**
 * A class that holds Actions for common rocket and simulation operations such as
 * cut/copy/paste/delete etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketActions {

	public static final KeyStroke CUT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_X,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	public static final KeyStroke COPY_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_C,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	public static final KeyStroke PASTE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_V,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	private final OpenRocketDocument document;
	private final Rocket rocket;
	private final BasicFrame parentFrame;
	private final DocumentSelectionModel selectionModel;


	private final RocketAction deleteComponentAction;
	private final RocketAction deleteSimulationAction;
	private final RocketAction deleteAction;
	private final RocketAction cutAction;
	private final RocketAction copyAction;
	private final RocketAction pasteAction;
	private final RocketAction editAction;
	private final RocketAction newStageAction;
	private final RocketAction moveUpAction;
	private final RocketAction moveDownAction;
	private static final Translator trans = Application.getTranslator();


	public RocketActions(OpenRocketDocument document, DocumentSelectionModel selectionModel,
			BasicFrame parentFrame) {
		this.document = document;
		this.rocket = document.getRocket();
		this.selectionModel = selectionModel;
		this.parentFrame = parentFrame;

		// Add action also to updateActions()
		this.deleteAction = new DeleteAction();
		this.deleteComponentAction = new DeleteComponentAction();
		this.deleteSimulationAction = new DeleteSimulationAction();
		this.cutAction = new CutAction();
		this.copyAction = new CopyAction();
		this.pasteAction = new PasteAction();
		this.editAction = new EditAction();
		this.newStageAction = new NewStageAction();
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
		editAction.clipboardChanged();
		newStageAction.clipboardChanged();
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
	
	public Action getEditAction() {
		return editAction;
	}
	
	public Action getNewStageAction() {
		return newStageAction;
	}
	
	public Action getMoveUpAction() {
		return moveUpAction;
	}
	
	public Action getMoveDownAction() {
		return moveDownAction;
	}

	
	
	////////  Helper methods for the actions

	private boolean isDeletable(RocketComponent c) {
		// Sanity check
		if (c == null || c.getParent() == null)
			return false;

		// Cannot remove Rocket
		if (c instanceof Rocket)
			return false;

		// Cannot remove last stage
		if ((c instanceof AxialStage) && (c.getParent().getChildCount() == 1)) {
			return false;
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

	private boolean isCopyable(RocketComponent c) {
		if (c==null)
			return false;
		if (c instanceof Rocket)
			return false;
		return true;
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
	 * @param   clipboard	the component on the clipboard.
	 * @return  a Pair with both components defined, or null.
	 */
	private Pair<RocketComponent, Integer> getPastePosition(RocketComponent clipboard) {
		RocketComponent selected = selectionModel.getSelectedComponent();
		if (selected == null)
			return null;

		if (clipboard == null)
			return null;

		if (selected.isCompatible(clipboard))
			return new Pair<RocketComponent, Integer>(selected, selected.getChildCount());

		RocketComponent parent = selected.getParent();
		if (parent != null && parent.isCompatible(clipboard)) {
			int index = parent.getChildPosition(selected) + 1;
			return new Pair<RocketComponent, Integer>(parent, index);
		}

		return null;
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
			RocketComponent c = selectionModel.getSelectedComponent();

			if (isDeletable(c)) {
				ComponentConfigDialog.hideDialog();

				document.addUndoPosition("Delete " + c.getComponentName());
				delete(c);
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
				parentFrame.selectTab(BasicFrame.COMPONENT_TAB);
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
			RocketComponent c = selectionModel.getSelectedComponent();
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (isDeletable(c) && isCopyable(c)) {
				ComponentConfigDialog.hideDialog();
				
				document.addUndoPosition("Cut " + c.getComponentName());
				OpenRocketClipboard.setClipboard(c.copy());
				delete(c);
				parentFrame.selectTab(BasicFrame.COMPONENT_TAB);
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
			RocketComponent c = selectionModel.getSelectedComponent();
			this.setEnabled((isDeletable(c) && isCopyable(c)) || isSimulationSelected());
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
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.CopyAct.Copy"));
			this.putValue(SMALL_ICON, Icons.EDIT_COPY);
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = selectionModel.getSelectedComponent();
			Simulation[] sims = selectionModel.getSelectedSimulations();

			if (isCopyable(c)) {
				OpenRocketClipboard.setClipboard(c.copy());
				parentFrame.selectTab(BasicFrame.COMPONENT_TAB);
			} else if (sims.length >= 0) {

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
			RocketComponent clipboard = OpenRocketClipboard.getClipboardComponent();
			Simulation[] sims = OpenRocketClipboard.getClipboardSimulations();
			
			Pair<RocketComponent, Integer> position = getPastePosition(clipboard);
			if (position != null) {
				ComponentConfigDialog.hideDialog();
				
				RocketComponent pasted = clipboard.copy();
				document.addUndoPosition("Paste " + pasted.getComponentName());
				position.getU().addChild(pasted, position.getV());
				selectionModel.setSelectedComponent(pasted);
				
				parentFrame.selectTab(BasicFrame.COMPONENT_TAB);
				
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
					(getPastePosition(OpenRocketClipboard.getClipboardComponent()) != null) ||
					(OpenRocketClipboard.getClipboardSimulations() != null));
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
			//// Edit the selected component.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.EditAct.ttip.Edit"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = selectionModel.getSelectedComponent();
			if (c == null)
				return;
			
			ComponentConfigDialog.showDialog(parentFrame, document, c);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(selectionModel.getSelectedComponent() != null);
		}
	}


	
	
	
	
	
	/**
	 * Action to add a new stage to the rocket.
	 */
	private class NewStageAction extends RocketAction {
		private static final long serialVersionUID = 1L;

		public NewStageAction() {
			//// New stage
			this.putValue(NAME, trans.get("RocketActions.NewStageAct.Newstage"));
			//// Add a new stage to the rocket design.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.NewStageAct.Newstage"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			ComponentConfigDialog.hideDialog();

			RocketComponent stage = new AxialStage();

			//// Add stage
			document.addUndoPosition("Add stage");
			rocket.addChild(stage);
			rocket.getSelectedConfiguration().setAllStages();
			selectionModel.setSelectedComponent(stage);
			ComponentConfigDialog.showDialog(parentFrame, document, stage);
			
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
			//// Move this component upwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveUpAct.ttip.Moveup"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent selected = selectionModel.getSelectedComponent();
			if (!canMove(selected))
				return;
			
			ComponentConfigDialog.hideDialog();

			RocketComponent parent = selected.getParent();
			document.addUndoPosition("Move "+selected.getComponentName());
			parent.moveChild(selected, parent.getChildPosition(selected)-1);
			rocket.fireComponentChangeEvent( ComponentChangeEvent.TREE_CHANGE );
			selectionModel.setSelectedComponent(selected);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponent()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) > 0)
				return true;
			return false;
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
			//// Move this component downwards.
			this.putValue(SHORT_DESCRIPTION, trans.get("RocketActions.MoveDownAct.ttip.Movedown"));
			clipboardChanged();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent selected = selectionModel.getSelectedComponent();
			if (!canMove(selected))
				return;
			
			ComponentConfigDialog.hideDialog();

			RocketComponent parent = selected.getParent();
			document.addUndoPosition("Move "+selected.getComponentName());
			parent.moveChild(selected, parent.getChildPosition(selected)+1);
			rocket.fireComponentChangeEvent( ComponentChangeEvent.TREE_CHANGE );
			selectionModel.setSelectedComponent(selected);
		}

		@Override
		public void clipboardChanged() {
			this.setEnabled(canMove(selectionModel.getSelectedComponent()));
		}
		
		private boolean canMove(RocketComponent c) {
			if (c == null || c.getParent() == null)
				return false;
			RocketComponent parent = c.getParent();
			if (parent.getChildPosition(c) < parent.getChildCount()-1)
				return true;
			return false;
		}
	}



}
