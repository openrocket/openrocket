package net.sf.openrocket.gui.main;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.Icons;
import net.sf.openrocket.util.Pair;



/**
 * A class that holds Actions for common rocket operations such as
 * cut/copy/paste/delete etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketActions {

	private static RocketComponent clipboard = null;
	private static List<RocketAction> clipboardListeners = new ArrayList<RocketAction>();

	private final OpenRocketDocument document;
	private final Rocket rocket;
	private final JFrame parentFrame;
	private final TreeSelectionModel selectionModel;


	private final RocketAction deleteAction;
	private final RocketAction cutAction;
	private final RocketAction copyAction;
	private final RocketAction pasteAction;
	private final RocketAction editAction;
	private final RocketAction newStageAction;
	private final RocketAction moveUpAction;
	private final RocketAction moveDownAction;
	

	public RocketActions(OpenRocketDocument document, TreeSelectionModel selectionModel,
			JFrame parentFrame) {
		this.document = document;
		this.rocket = document.getRocket();
		this.selectionModel = selectionModel;
		this.parentFrame = parentFrame;

		// Add action also to updateActions()
		this.deleteAction = new DeleteAction();
		this.cutAction = new CutAction();
		this.copyAction = new CopyAction();
		this.pasteAction = new PasteAction();
		this.editAction = new EditAction();
		this.newStageAction = new NewStageAction();
		this.moveUpAction = new MoveUpAction();
		this.moveDownAction = new MoveDownAction();

		updateActions();

		// Update all actions when tree selection or rocket changes

		selectionModel.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
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
		deleteAction.update();
		cutAction.update();
		copyAction.update();
		pasteAction.update();
		editAction.update();
		newStageAction.update();
		moveUpAction.update();
		moveDownAction.update();
	}
	
	
	/**
	 * Update the state of all actions that depend on the clipboard.
	 */
	private void updateClipboardActions() {
		RocketAction[] array = clipboardListeners.toArray(new RocketAction[0]);
		for (RocketAction a: array) {
			a.update();
		}
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

	/**
	 * Return the currently selected rocket component, or null if none selected.
	 * 
	 * @return	the currently selected component.
	 */
	private RocketComponent getSelectedComponent() {
		RocketComponent c = null;
		TreePath p = selectionModel.getSelectionPath();
		if (p != null)
			c = (RocketComponent) p.getLastPathComponent();

		if (c != null && c.getRocket() != rocket) {
			throw new IllegalStateException("Selection not same as document rocket, "
					+ "report bug!");
		}
		return c;
	}
	
	private void setSelectedComponent(RocketComponent component) {
		TreePath path = ComponentTreeModel.makeTreePath(component);
		selectionModel.setSelectionPath(path);
	}


	private boolean isDeletable(RocketComponent c) {
		// Sanity check
		if (c == null || c.getParent() == null)
			return false;

		// Cannot remove Rocket
		if (c instanceof Rocket)
			return false;

		// Cannot remove last stage
		if ((c instanceof Stage) && (c.getParent().getChildCount() == 1)) {
			return false;
		}

		return true;
	}

	private void delete(RocketComponent c) {
		if (!isDeletable(c)) {
			throw new IllegalArgumentException("Report bug!  Component " + c + " not deletable.");
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

	


	/**
	 * Return the component and position to which the current clipboard
	 * should be pasted.  Returns null if the clipboard is empty or if the
	 * clipboard cannot be pasted to the current selection.
	 * 
	 * @return  a Pair with both components defined, or null.
	 */
	private Pair<RocketComponent, Integer> getPastePosition() {
		RocketComponent selected = getSelectedComponent();
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

	private abstract class RocketAction extends AbstractAction {
		public abstract void update();
	}


	/**
	 * Action that deletes the selected component.
	 */
	private class DeleteAction extends RocketAction {
		public DeleteAction() {
			this.putValue(NAME, "Delete");
			this.putValue(SHORT_DESCRIPTION, "Delete the selected component and subcomponents.");
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = getSelectedComponent();
			if (!isDeletable(c))
				return;

			ComponentConfigDialog.hideDialog();

			document.addUndoPosition("Delete " + c.getComponentName());
			delete(c);
		}

		@Override
		public void update() {
			this.setEnabled(isDeletable(getSelectedComponent()));
		}
	}


	
	/**
	 * Action the cuts the selected component (copies to clipboard and deletes).
	 */
	private class CutAction extends RocketAction {
		public CutAction() {
			this.putValue(NAME, "Cut");
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_T);
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
					ActionEvent.CTRL_MASK));
			this.putValue(SHORT_DESCRIPTION, "Cut this component (and subcomponents) to "
					+ "the clipboard and remove from this design");
			this.putValue(SMALL_ICON, Icons.EDIT_CUT);
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = getSelectedComponent();
			if (!isDeletable(c) || !isCopyable(c))
				return;

			ComponentConfigDialog.hideDialog();

			document.addUndoPosition("Cut " + c.getComponentName());
			clipboard = c.copy();
			delete(c);
			updateClipboardActions();
		}

		@Override
		public void update() {
			RocketComponent c = getSelectedComponent();
			this.setEnabled(isDeletable(c) && isCopyable(c));
		}
	}



	/**
	 * Action that copies the selected component to the clipboard.
	 */
	private class CopyAction extends RocketAction {
		public CopyAction() {
			this.putValue(NAME, "Copy");
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
					ActionEvent.CTRL_MASK));
			this.putValue(SHORT_DESCRIPTION, "Copy this component (and subcomponents) to "
					+ "the clipboard.");
			this.putValue(SMALL_ICON, Icons.EDIT_COPY);
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = getSelectedComponent();
			if (!isCopyable(c))
				return;

			clipboard = c.copy();
			updateClipboardActions();
		}

		@Override
		public void update() {
			this.setEnabled(isCopyable(getSelectedComponent()));
		}
		
	}



	/**
	 * Action that pastes the current clipboard to the selected position.
	 * It first tries to paste the component to the end of the selected component
	 * as a child, and after that as a sibling after the selected component. 
	 */
	private class PasteAction extends RocketAction {
		public PasteAction() {
			this.putValue(NAME, "Paste");
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_P);
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
					ActionEvent.CTRL_MASK));
			this.putValue(SHORT_DESCRIPTION, "Paste the component (and subcomponents) on "
					+ "the clipboard to the design.");
			this.putValue(SMALL_ICON, Icons.EDIT_PASTE);
			update();
			
			// Listen to when the clipboard changes
			clipboardListeners.add(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Pair<RocketComponent, Integer> position = getPastePosition();
			if (position == null)
				return;

			ComponentConfigDialog.hideDialog();

			RocketComponent pasted = clipboard.copy();
			document.addUndoPosition("Paste " + pasted.getComponentName());
			position.getU().addChild(pasted, position.getV());
			setSelectedComponent(pasted);
		}

		@Override
		public void update() {
			this.setEnabled(getPastePosition() != null);
		}
	}
	
	
	
	

	
	/**
	 * Action to edit the currently selected component.
	 */
	private class EditAction extends RocketAction {
		public EditAction() {
			this.putValue(NAME, "Edit");
			this.putValue(SHORT_DESCRIPTION, "Edit the selected component.");
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent c = getSelectedComponent();
			if (c == null)
				return;
			
			ComponentConfigDialog.showDialog(parentFrame, document, c);
		}

		@Override
		public void update() {
			this.setEnabled(getSelectedComponent() != null);
		}
	}


	
	
	
	
	
	/**
	 * Action to add a new stage to the rocket.
	 */
	private class NewStageAction extends RocketAction {
		public NewStageAction() {
			this.putValue(NAME, "New stage");
			this.putValue(SHORT_DESCRIPTION, "Add a new stage to the rocket design.");
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			ComponentConfigDialog.hideDialog();

			RocketComponent stage = new Stage();
			stage.setName("Booster stage");
			document.addUndoPosition("Add stage");
			rocket.addChild(stage);
			rocket.getDefaultConfiguration().setAllStages();
			setSelectedComponent(stage);
			ComponentConfigDialog.showDialog(parentFrame, document, stage);
			
		}

		@Override
		public void update() {
			this.setEnabled(true);
		}
	}



	
	/**
	 * Action to move the selected component upwards in the parent's child list.
	 */
	private class MoveUpAction extends RocketAction {
		public MoveUpAction() {
			this.putValue(NAME, "Move up");
			this.putValue(SHORT_DESCRIPTION, "Move this component upwards.");
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent selected = getSelectedComponent();
			if (!canMove(selected))
				return;
			
			ComponentConfigDialog.hideDialog();

			RocketComponent parent = selected.getParent();
			document.addUndoPosition("Move "+selected.getComponentName());
			parent.moveChild(selected, parent.getChildPosition(selected)-1);
			setSelectedComponent(selected);
		}

		@Override
		public void update() {
			this.setEnabled(canMove(getSelectedComponent()));
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
		public MoveDownAction() {
			this.putValue(NAME, "Move down");
			this.putValue(SHORT_DESCRIPTION, "Move this component downwards.");
			update();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RocketComponent selected = getSelectedComponent();
			if (!canMove(selected))
				return;
			
			ComponentConfigDialog.hideDialog();

			RocketComponent parent = selected.getParent();
			document.addUndoPosition("Move "+selected.getComponentName());
			parent.moveChild(selected, parent.getChildPosition(selected)+1);
			setSelectedComponent(selected);
		}

		@Override
		public void update() {
			this.setEnabled(canMove(getSelectedComponent()));
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
