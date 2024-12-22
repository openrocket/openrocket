package info.openrocket.swing.gui.main;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Pair;
import info.openrocket.core.util.Reflection;
import info.openrocket.swing.gui.configdialog.ComponentConfigDialog;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import info.openrocket.swing.gui.widgets.IconTextButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A button class for adding rocket components to the rocket design.
 * Extends IconTextButton to provide a consistent look with icon above text.
 */
public class ComponentButton extends IconTextButton implements TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ComponentButton.class);

	protected final Class<? extends RocketComponent> componentClass;
	private final Constructor<? extends RocketComponent> constructor;
	private final OpenRocketDocument document;
	private final TreeSelectionModel selectionModel;


	/**
	 * Create a button with only text, no component class association.
	 *
	 * @param text the button text
	 */
	public ComponentButton(OpenRocketDocument document, TreeSelectionModel selectionModel, String text) {
		this(document, selectionModel, text, null, null);
	}

	/**
	 * Create a button with text and icons but no component class association.
	 *
	 * @param text the button text
	 * @param enabled the icon for enabled state
	 * @param disabled the icon for disabled state
	 */
	public ComponentButton(OpenRocketDocument document, TreeSelectionModel selectionModel,
						   String text, Icon enabled, Icon disabled) {
		super(text, enabled, disabled);
		this.document = document;
		this.selectionModel = selectionModel;
		this.componentClass = null;
		this.constructor = null;

		initializeButton();
	}

	/**
	 * Main constructor that should be used. Creates a button for adding a specific component type.
	 *
	 * @param document the rocket document
	 * @param selectionModel the tree selection model
	 * @param componentClass the class of component this button will create
	 * @param text the button text
	 */
	public ComponentButton(OpenRocketDocument document, TreeSelectionModel selectionModel,
						   Class<? extends RocketComponent> componentClass, String text) {
		super(text, ComponentIcons.getLargeIcon(componentClass),
				ComponentIcons.getLargeDisabledIcon(componentClass));

		this.document = document;
		this.selectionModel = selectionModel;
		this.componentClass = componentClass;

		// Get the constructor for the component
		Constructor<? extends RocketComponent> tempConstructor = null;
		if (componentClass != null) {
			try {
				tempConstructor = componentClass.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Unable to get default " +
						"constructor for class " + componentClass, e);
			}
		}
		this.constructor = tempConstructor;

		initializeButton();
	}

	/**
	 * Initialize the button's listeners and enabled state
	 */
	private void initializeButton() {
		// Initialize enabled status
		updateEnabled();

		// Add selection listener if model exists
		if (selectionModel != null) {
			selectionModel.addTreeSelectionListener(this);
		}
	}

	/**
	 * Return whether the current component is addable when the component c is selected.
	 *
	 * @param c the currently selected component (null if nothing selected)
	 * @return true if this component can be added to the selected component
	 */
	public boolean isAddable(RocketComponent c) {
		if (c == null || componentClass == null) {
			return false;
		}
		return c.isCompatible(componentClass);
	}

	/**
	 * Get the position where the new component should be added.
	 *
	 * @param c the currently selected component
	 * @return Pair containing the parent component and position, or null to cancel
	 */
	public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
		return new Pair<>(c, null);
	}

	/**
	 * Handle tree selection changes by updating enabled state
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		updateEnabled();
	}

	/**
	 * Update the enabled status of the button based on current selection
	 */
	private void updateEnabled() {
		RocketComponent selected = null;
		TreePath path = selectionModel.getSelectionPath();
		if (path != null) {
			selected = (RocketComponent) path.getLastPathComponent();
		}
		setEnabled(isAddable(selected));
	}

	/**
	 * Handle button click by creating and adding the new component
	 */
	@Override
	protected void fireActionPerformed(ActionEvent event) {
		super.fireActionPerformed(event);

		if (componentClass == null || constructor == null) {
			Application.getExceptionHandler().handleErrorCondition("ERROR: Component class or constructor is null");
			return;
		}

		log.info("Adding component of type " + componentClass.getSimpleName());

		// Get selected component
		RocketComponent selected = null;
		TreePath path = selectionModel.getSelectionPath();
		if (path != null) {
			selected = (RocketComponent) path.getLastPathComponent();
		}

		// Get addition position
		Pair<RocketComponent, Integer> position = getAdditionPosition(selected);
		if (position == null || position.getU() == null) {
			log.info("No position to add component");
			return;
		}

		RocketComponent parent = position.getU();
		Integer index = position.getV();

		// Create new component
		RocketComponent component;
		try {
			component = constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new BugException("Could not construct new instance of class " + constructor, e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}

		// Add to document with undo position
		document.addUndoPosition("Add " + component.getComponentName());

		log.info("Adding component " + component.getComponentName() +
				" to component " + parent.getComponentName() + " position=" + index);

		if (index == null) {
			parent.addChild(component);
		} else {
			parent.addChild(component, index);
		}

		// Select new component and open config dialog
		selectionModel.setSelectionPath(ComponentTreeModel.makeTreePath(component));

		// Find parent frame
		JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		ComponentConfigDialog.showDialog(parentFrame, document, component, false, true);
	}
}
