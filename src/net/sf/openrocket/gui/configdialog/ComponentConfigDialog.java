package net.sf.openrocket.gui.configdialog;


import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

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
	
	
	private static ComponentConfigDialog dialog = null;

	
	private OpenRocketDocument document = null;
	private RocketComponent component = null;
	private RocketComponentConfig configurator = null;
	
	private final Window parent;
	
	private ComponentConfigDialog(Window parent, OpenRocketDocument document, 
			RocketComponent component) {
		super(parent);
		this.parent = parent;
		
		setComponent(document, component);
		
		// Set window position according to preferences, and set prefs when moving
		Point position = Prefs.getWindowPosition(this.getClass());
		if (position == null)
			this.setLocationByPlatform(true);
		else
			this.setLocation(position);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				Prefs.setWindowPosition(ComponentConfigDialog.this.getClass(), 
						ComponentConfigDialog.this.getLocation());
			}
		});
		
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	

	/**
	 * Set the component being configured.  The listening connections of the old configurator
	 * will be removed and the new ones created.
	 * 
	 * @param component  Component to configure.
	 */
	private void setComponent(OpenRocketDocument document, RocketComponent component) {
		if (this.document != null) {
			this.document.getRocket().removeComponentChangeListener(this);
		}

		if (configurator != null) {
			// Remove listeners by setting all applicable models to null
			GUIUtil.setNullModels(configurator);  // null-safe
		}
		
		this.document = document;
		this.component = component;
		this.document.getRocket().addComponentChangeListener(this);
		
		configurator = getDialogContents();
		this.setContentPane(configurator);
		configurator.updateFields();
		
		setTitle(component.getComponentName()+" configuration");

//		Dimension pref = getPreferredSize();
//		Dimension real = getSize();
//		if (pref.width > real.width || pref.height > real.height)
		this.pack();
	}
	
	/**
	 * Return the configurator panel of the current component.
	 */
	private RocketComponentConfig getDialogContents() {
		Constructor<? extends RocketComponentConfig> c = 
			findDialogContentsConstructor(component);
		if (c != null) {
			try {
				return (RocketComponentConfig) c.newInstance(component);
			} catch (InstantiationException e) {
				throw new RuntimeException("BUG in constructor reflection",e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("BUG in constructor reflection",e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("BUG in constructor reflection",e);
			}
		}
		
		// Should never be reached, since RocketComponentConfig should catch all
		// components without their own configurator.
		throw new RuntimeException("Unable to find any configurator for "+component);
	}

	/**
	 * Finds the Constructor of the given component's config dialog panel in 
	 * CONFIGDIALOGPACKAGE.
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<? extends RocketComponentConfig> 
			findDialogContentsConstructor(RocketComponent component) {
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
					configclass.getConstructor(RocketComponent.class);
				return c;
			} catch (Exception ignore) { }

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
	 */
	public static void showDialog(Window parent, OpenRocketDocument document, 
			RocketComponent component) {
		if (dialog != null)
			dialog.dispose();
		
		dialog = new ComponentConfigDialog(parent, document, component);
		dialog.setVisible(true);
		
		document.addUndoPosition("Modify "+component.getComponentName());
	}
	
	
	/* package */ 
	static void showDialog(RocketComponent component) {
		showDialog(dialog.parent, dialog.document, component);
	}
	
	/**
	 * Hides the configuration dialog.  May be used even if not currently visible.
	 */
	public static void hideDialog() {
		if (dialog != null)
			dialog.setVisible(false);
	}

	
	/**
	 * Add an undo position for the current document.  This is intended for use only
	 * by the currently open dialog.
	 * 
	 * @param description  Description of the undoable action
	 */
	/*package*/ static void addUndoPosition(String description) {
		if (dialog == null) {
			throw new IllegalStateException("Dialog not open, report bug!");
		}
		dialog.document.addUndoPosition(description);
	}
	
	/*package*/
	static String getUndoDescription() {
		if (dialog == null) {
			throw new IllegalStateException("Dialog not open, report bug!");
		}
		return dialog.document.getUndoDescription();
	}
	
	/**
	 * Returns whether the singleton configuration dialog is currently visible or not.
	 */
	public static boolean isDialogVisible() {
		return (dialog!=null) && (dialog.isVisible());
	}


	public void componentChanged(ComponentChangeEvent e) {
		if (e.isTreeChange() || e.isUndoChange()) {
			
			// Hide dialog in case of tree or undo change
			dialog.setVisible(false);

		} else {
			configurator.updateFields();
		}
	}
	
}
