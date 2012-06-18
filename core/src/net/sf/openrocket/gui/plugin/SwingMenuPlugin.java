package net.sf.openrocket.gui.plugin;

import javax.swing.Action;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.main.BasicFrame;

/**
 * A plugin that provides a menu item to the Swing GUI menus.
 * This may open a dialog window or perform some other action on
 * the current document.
 * <p>
 * During plugin discovery, the BasicFrame and OpenRocketDocument
 * objects are passed to the plugin.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SwingMenuPlugin {
	
	/**
	 * Return the menu position where the action is placed.
	 * The first string in the array indicates the menu to place
	 * the item in, the second is the sub-menu, the third is the
	 * sub-sub-menu etc.
	 * <p>
	 * The strings are translated menu names.
	 * 
	 * @return	the menu position for the action
	 */
	public String[] getMenuPosition();
	
	/**
	 * Return the Action that the menu item performs.  This contains
	 * the menu item text and may contain an icon.
	 * 
	 * @return	the action to perform on the menu item.
	 */
	public Action getAction(BasicFrame frame, OpenRocketDocument document);
	
}
