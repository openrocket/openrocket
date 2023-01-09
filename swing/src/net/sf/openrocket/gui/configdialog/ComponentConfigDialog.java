package net.sf.openrocket.gui.configdialog;


import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JDialog;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.util.WindowLocationUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Reflection;

/**
 * A dialog that contains the configuration elements of one component.
 * The contents of the dialog are instantiated from CONFIGDIALOGPACKAGE according
 * to the current component.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentConfigDialog extends JDialog implements ComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final String CONFIGDIALOGPACKAGE = "net.sf.openrocket.gui.configdialog";
	private static final String CONFIGDIALOGPOSTFIX = "Config";

	// Static Value -- This is a singleton value, and we should only have zero or one active at any time
	private static ComponentConfigDialog dialog = null;

	private OpenRocketDocument document = null;
	protected RocketComponent component = null;
	private RocketComponentConfig configurator = null;
	private boolean isModified = false;
	private final boolean isNewComponent;
	public static boolean clearConfigListeners = true;
	private static String previousSelectedTab = null;	// Name of the previous selected tab


	private final Window parent;
	private static final Translator trans = Application.getTranslator();

	private ComponentConfigDialog(Window parent, OpenRocketDocument document, RocketComponent component, boolean isNewComponent) {
		super(parent);
		this.parent = parent;
		this.isNewComponent = isNewComponent;

		setComponent(document, component);

		GUIUtil.setDisposableDialogOptions(this, null);
		GUIUtil.rememberWindowPosition(this);

		// overrides common defaults in 'GUIUTIL.setDisposableDialogOptions', above
		addWindowListener(new WindowAdapter() {
			/**
			 *  Triggered by the 'Close' Button on the ConfigDialogs.  AND Esc. AND the [x] button (on Windows)
			 *  In fact, it should trigger for any method of closing the dialog.
			 */
			public void windowClosed(WindowEvent e){
				configurator.clearConfigListeners();
				configurator.invalidate();
				document.getRocket().removeComponentChangeListener(ComponentConfigDialog.this);
				ComponentConfigDialog.this.dispose();
				if (clearConfigListeners) {
					component.clearConfigListeners();
				}
			}

			@Override
			public void windowOpened(WindowEvent e) {
				super.windowOpened(e);
				clearConfigListeners = true;
			}
		});
	}


	/**
	 * Set the component being configured.  The listening connections of the old configurator
	 * will be removed and the new ones created.
	 *
	 * @param component  Component to configure.
	 */
	private void setComponent(OpenRocketDocument document, RocketComponent component) {
		if (configurator != null) {
			// Remove listeners by setting all applicable models to null
			GUIUtil.setNullModels(configurator); // null-safe
		}

		this.document = document;
		this.component = component;
		this.document.getRocket().addComponentChangeListener(this);
		this.isModified = false;

		configurator = getDialogContents();
		configurator.setNewComponent(isNewComponent);
		this.setContentPane(configurator);
		configurator.updateFields();

		List<RocketComponent> listeners = component.getConfigListeners();

		// Set the default tab to 'Appearance' for a different-type multi-comp dialog (this is the most prominent use case)
		if (listeners != null && listeners.size() > 0 && !component.checkAllClassesEqual(listeners)) {
			configurator.setSelectedTabIndex(1);
		} else {
			configurator.setSelectedTab(previousSelectedTab);
		}

		//// configuration
		if (component.checkAllClassesEqual(listeners)) {
			if (listeners != null && listeners.size() > 0) {
				setTitle("(" + trans.get("ComponentCfgDlg.MultiComponent") + ") " +
						component.getComponentName() + " " + trans.get("ComponentCfgDlg.configuration"));
			} else {
				setTitle(component.getComponentName() + " " + trans.get("ComponentCfgDlg.configuration"));
			}
		} else {
			setTitle(trans.get("ComponentCfgDlg.MultiComponentConfig"));
		}

		this.pack();
	}

	public RocketComponent getComponent() {
		return component;
	}

	public static ComponentConfigDialog getDialog() {
		return dialog;
	}

	/**
	/**
	 * Return the configurator panel of the current component.
	 */
	private RocketComponentConfig getDialogContents() {
		List<RocketComponent> listeners = component.getConfigListeners();
		boolean isSameClass = component.checkAllClassesEqual(listeners);
		if (!isSameClass) {
			return new RocketComponentConfig(document, component, this);
		}

		Constructor<? extends RocketComponentConfig> constructor =
				findDialogContentsConstructor(component);
		if (constructor != null) {
			try {
				return constructor.newInstance(document, component, this);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new BugException("BUG in constructor reflection", e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
		}

		// Should never be reached, since RocketComponentConfig should catch all
		// components without their own configurator.
		throw new BugException("Unable to find any configurator for " + component);
	}

	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isTreeChange() || e.isUndoChange()) {
			// Hide dialog in case of tree or undo change
			disposeDialog();
		} else {
			/*
			 * TODO: HIGH:  The line below has caused a NullPointerException (without null check)
			 * How is this possible?  The null check was added to avoid this, but the
			 * root cause should be analyzed.
			 * [Openrocket-bugs] 2009-12-12 19:23:22 Automatic bug report for OpenRocket 0.9.5
			 */
			if (configurator != null) {
				configurator.updateFields();
			}
			if (!this.isModified) {
				setTitle("*" + getTitle());
				this.isModified = true;
			}
		}
	}


	/**
	 * Finds the Constructor of the given component's config dialog panel in
	 * CONFIGDIALOGPACKAGE.
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<? extends RocketComponentConfig> findDialogContentsConstructor(RocketComponent component) {
		Class<?> currentclass;
		String currentclassname;
		String configclassname;

		Class<?> configclass;
		Constructor<? extends RocketComponentConfig> c;

		currentclass = component.getClass();
		while ((currentclass != null) && (currentclass != Object.class)) {
			currentclassname = currentclass.getCanonicalName();
			int index = currentclassname.lastIndexOf('.');
			if (index >= 0)
				currentclassname = currentclassname.substring(index + 1);
			configclassname = CONFIGDIALOGPACKAGE + "." + currentclassname +
					CONFIGDIALOGPOSTFIX;

			try {
				configclass = Class.forName(configclassname);
				c = (Constructor<? extends RocketComponentConfig>)
						configclass.getConstructor(OpenRocketDocument.class, RocketComponent.class, JDialog.class);
				return c;
			} catch (Exception ignore) {
			}

			currentclass = currentclass.getSuperclass();
		}
		return null;
	}




	//////////  Static dialog  /////////

	/**
	 * A singleton configuration dialog.  Will create and show a new dialog if one has not
	 * previously been used, or update the dialog and show it if a previous one exists.
	 *
	 * @param document		the document to configure.
	 * @param component		the component to configure.
	 * @param rememberPreviousTab if true, the previous tab will be remembered and used for the new dialog
	 * @param isNewComponent	whether the component is just created, or has already been created and is being edited
	 */
	public static void showDialog(Window parent, OpenRocketDocument document, RocketComponent component, boolean rememberPreviousTab, boolean isNewComponent) {
		if (dialog != null) {
			// Don't remember the previous tab for rockets or stages, because this will leave you in the override tab for
			// the next component, which is generally not what you want.
			if (dialog.getComponent() instanceof Rocket ||
				(dialog.getComponent() instanceof AxialStage && !(component instanceof AxialStage))) {
				previousSelectedTab = null;
			} else {
				previousSelectedTab = dialog.getSelectedTabName();
			}
			// If the component is the same as the ComponentConfigDialog component, and the dialog is still visible,
			// that means that the user did a ctr/cmd click on a new component => don't remove the config listeners of component
			if (component == dialog.getComponent()) {
				ComponentConfigDialog.clearConfigListeners = false;
			}
			dialog.dispose();
		}

		final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
		if (preferences.isAlwaysOpenLeftmostTab() || !rememberPreviousTab) {
			previousSelectedTab = null;
		}

		dialog = new ComponentConfigDialog(parent, document, component, isNewComponent);
		dialog.setVisible(true);
		if (parent instanceof BasicFrame && BasicFrame.getStartupFrame() == parent) {
			WindowLocationUtil.moveIfOutsideOfParentMonitor(dialog, parent);
		}

		// Only add a modify undo action if the component is not a new component (because a "Create new component" undo action is already added)
		if (!isNewComponent) {
			if (component.getConfigListeners().size() == 0) {
				document.addUndoPosition(trans.get("ComponentCfgDlg.Modify") + " " + component.getComponentName());
			} else {
				document.addUndoPosition(trans.get("ComponentCfgDlg.ModifyComponents"));
			}
		}

		// Open preset dialog if set in preferences
		if (isNewComponent && component.getPresetType() != null &&
				preferences.getBoolean(component.getComponentName() + "AlwaysOpenPreset", true) &&
				component.getConfigListeners().size() == 0) {
			dialog.configurator.selectPreset();
		}
	}

	/**
	 * A singleton configuration dialog.  Will create and show a new dialog if one has not
	 * previously been used, or update the dialog and show it if a previous one exists.
	 *
	 * @param document		the document to configure.
	 * @param component		the component to configure.
	 * @param rememberPreviousTab if true, the previous tab will be remembered and used for the new dialog
	 */
	public static void showDialog(Window parent, OpenRocketDocument document, RocketComponent component, boolean rememberPreviousTab) {
		ComponentConfigDialog.showDialog(parent, document, component, rememberPreviousTab, false);
	}

	/**
	 * A singleton configuration dialog.  Will create and show a new dialog if one has not
	 * previously been used, or update the dialog and show it if a previous one exists.
	 * By default, the previous tab is remembered.
	 *
	 * @param document		the document to configure.
	 * @param component		the component to configure.
	 */
	public static void showDialog(Window parent, OpenRocketDocument document, RocketComponent component) {
		ComponentConfigDialog.showDialog(parent, document, component, true);
	}

	static void showDialog(RocketComponent component, boolean rememberPreviousTab) {
		showDialog(dialog.parent, dialog.document, component, rememberPreviousTab);
	}


	/* package */
	static void showDialog(RocketComponent component) {
		showDialog(dialog.parent, dialog.document, component, true);
	}

	/**
	 * Disposes the configuration dialog.  May be used even if not currently visible.
	 */
	public static void disposeDialog() {
		if (dialog != null) {
			dialog.dispose();
		}
	}


	/**
	 * Returns whether the singleton configuration dialog is currently visible or not.
	 */
	public static boolean isDialogVisible() {
		return (dialog != null) && (dialog.isVisible());
	}

	/**
	 * Returns true if the current component has been modified or not.
	 */
	public boolean isModified() {
		return isModified;
	}

	public int getSelectedTabIndex() {
		return configurator.getSelectedTabIndex();
	}

	public String getSelectedTabName() {
		if (configurator != null) {
			return configurator.getSelectedTabName();
		} else {
			return null;
		}
	}
	
}
