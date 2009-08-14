package net.sf.openrocket.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

public class GUIUtil {

	private static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private static final String CLOSE_ACTION_KEY =  "escape:WINDOW_CLOSING"; 
	
    private static final List<Image> images = new ArrayList<Image>();
    static {
    	loadImage("pix/icon/icon-256.png");
    	loadImage("pix/icon/icon-064.png");
    	loadImage("pix/icon/icon-048.png");
    	loadImage("pix/icon/icon-032.png");
    	loadImage("pix/icon/icon-016.png");
    }
    
    private static void loadImage(String file) {
    	InputStream is;
 
    	is = ClassLoader.getSystemResourceAsStream(file);
    	if (is == null)
    		return;
    	
    	try {
    		Image image = ImageIO.read(is);
    		images.add(image);
    	} catch (IOException ignore) {
    		ignore.printStackTrace();
    	}
    }
    

	
	
	/**
	 * Add the correct action to close a JDialog when the ESC key is pressed.
	 * The dialog is closed by sending is a WINDOW_CLOSING event.
	 * 
	 * @param dialog	the dialog for which to install the action.
	 */
	public static void installEscapeCloseOperation(final JDialog dialog) { 
	    Action dispatchClosing = new AbstractAction() { 
	        public void actionPerformed(ActionEvent event) { 
	            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)); 
	        } 
	    }; 
	    JRootPane root = dialog.getRootPane(); 
	    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE, CLOSE_ACTION_KEY); 
	    root.getActionMap().put(CLOSE_ACTION_KEY, dispatchClosing); 
	}
	
	
	/**
	 * Set the given button as the default button of the frame/dialog it is in.  The button
	 * must be first attached to the window component hierarchy.
	 * 
	 * @param button	the button to set as the default button.
	 */
	public static void setDefaultButton(JButton button) {
		Window w = SwingUtilities.windowForComponent(button);
		if (w == null) {
			throw new IllegalArgumentException("Attach button to a window first.");
		}
		if (!(w instanceof RootPaneContainer)) {
			throw new IllegalArgumentException("Button not attached to RootPaneContainer, w="+w);
		}
		((RootPaneContainer)w).getRootPane().setDefaultButton(button);
	}

	
	
	/**
	 * Change the behavior of a component so that TAB and Shift-TAB cycles the focus of
	 * the components.  This is necessary for e.g. <code>JTextArea</code>.
	 * 
	 * @param c		the component to modify
	 */
    public static void setTabToFocusing(Component c) {
        Set<KeyStroke> strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
        strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
    }

    
    public static void setWindowIcons(Window window) {
    	window.setIconImages(images);
    }
	
}
