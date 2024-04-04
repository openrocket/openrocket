package info.openrocket.swing.gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.formdev.flatlaf.FlatLightLaf;
import info.openrocket.swing.gui.Resettable;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Invalidatable;
import info.openrocket.core.util.MemoryManagement;

import info.openrocket.swing.gui.theme.UITheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUIUtil {
	private static final Logger log = LoggerFactory.getLogger(GUIUtil.class);
	
	private static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private static final String CLOSE_ACTION_KEY = "escape:WINDOW_CLOSING";
	
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
	 * Return the DPI setting of the monitor.  This is either the setting provided
	 * by the system or a user-specified DPI setting.
	 * 
	 * @return    the DPI setting to use.
	 */
	public static double getDPI() {
		int dpi = Application.getPreferences().getInt("DPI", 0); // Tenths of a dpi
		
		if (dpi < 10) {
			dpi = Toolkit.getDefaultToolkit().getScreenResolution() * 10;
		}
		if (dpi < 10)
			dpi = 960;
		
		return (dpi) / 10.0;
	}
	
	
	
	
	/**
	 * Set suitable options for a single-use disposable dialog.  This includes
	 * setting ESC to close the dialog, adding the appropriate window icons and
	 * setting the location based on the platform.  If defaultButton is provided, 
	 * it is set to the default button action.
	 * <p>
	 * The default button must be already attached to the dialog.
	 * 
	 * @param dialog		the dialog.
	 * @param defaultButton	the default button of the dialog, or <code>null</code>.
	 */
	public static void setDisposableDialogOptions(JDialog dialog, JButton defaultButton) {
		installEscapeCloseOperation(dialog);
		setWindowIcons(dialog);
		addModelNullingListener(dialog);
		dialog.setLocationRelativeTo(dialog.getOwner());
		dialog.setLocationByPlatform(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.pack();
		if (defaultButton != null) {
			setDefaultButton(defaultButton);
		}
	}
	
	
	
	/**
	 * Add the correct action to close a JDialog when the ESC key is pressed.
	 * The dialog is closed by sending is a WINDOW_CLOSING event.
	 * 
	 * @param dialog	the dialog for which to install the action.
	 */
	public static void installEscapeCloseOperation(final JDialog dialog) {
		installEscapeCloseOperation(dialog, null);
	}

	public static void installEscapeCloseButtonOperation(final JDialog dialog, final JButton buttonToClick) {
		Action triggerButtonAndClose = new AbstractAction() {
			private static final long serialVersionUID = 9196153713666242274L;

			@Override
			public void actionPerformed(ActionEvent event) {
				log.info(Markers.USER_MARKER, "Closing dialog " + dialog);

				if (buttonToClick != null) {
					buttonToClick.doClick();  // Programmatically "press" the button
				}

				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		};

		installEscapeCloseOperation(dialog, triggerButtonAndClose);
	}

	/**
	 * Add the correct action to close a JDialog when the ESC key is pressed.
	 * The dialog is closed by sending it a WINDOW_CLOSING event.
	 *
	 * An additional action can be passed which will be executed upon the close action key.
	 *
	 * @param dialog	the dialog for which to install the action.
	 * @param action	action to execute upon the close action
	 */
	public static void installEscapeCloseOperation(final JDialog dialog, Action action) {
		Action dispatchClosing = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 9196153713666242274L;

			@Override
			public void actionPerformed(ActionEvent event) {
				log.info(Markers.USER_MARKER, "Closing dialog " + dialog);
				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		};
		JRootPane root = dialog.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE, CLOSE_ACTION_KEY);
		root.getActionMap().put(CLOSE_ACTION_KEY, dispatchClosing);
		if (action != null) {
			root.getActionMap().put(CLOSE_ACTION_KEY, action);
		}
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
			throw new IllegalArgumentException("Button not attached to RootPaneContainer, w=" + w);
		}
		((RootPaneContainer) w).getRootPane().setDefaultButton(button);
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
	
	
	
	/**
	 * Set the OpenRocket icons to the window icons.
	 * 
	 * @param window	the window to set.
	 */
	public static void setWindowIcons(Window window) {
		window.setIconImages(images);
	}
	
	/**
	 * Add a listener to the provided window that will call {@link #setNullModels(Component)}
	 * on the window once it is closed.  This method may only be used on single-use
	 * windows and dialogs, that will never be shown again once closed!
	 * 
	 * @param window	the window to add the listener to.
	 */
	public static void addModelNullingListener(final Window window) {
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				log.debug("Clearing all models of window " + window);
				setNullModels(window);
				MemoryManagement.collectable(window);
			}
		});
	}

	/**
	 * Get the current theme used for the UI.
	 * @return the current theme
	 */
	public static UITheme.Theme getUITheme() {
		Preferences prefs = Application.getPreferences();
		Object theme = prefs.getUITheme();
		if (theme instanceof UITheme.Theme) {
			return (UITheme.Theme) theme;
		}
		return UITheme.Themes.LIGHT;
	}

	public static void applyLAF() {
		UITheme.Theme theme = getUITheme();
		theme.applyTheme();
	}
	
	
	/**
	 * Changes the size of the font of the specified component by the given amount.
	 * 
	 * @param component		the component for which to change the font
	 * @param size			the change in the font size
	 */
	public static void changeFontSize(JComponent component, float size) {
		Font font = component.getFont();
		font = font.deriveFont(font.getSize2D() + size);
		component.setFont(font);
	}
	
	
	
	/**
	 * Automatically remember the size of a window.  This stores the window size in the user
	 * preferences when resizing/maximizing the window and sets the state on the first call.
	 */
	public static void rememberWindowSize(final Window window) {
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension previousWindowSize = ((SwingPreferences)Application.getPreferences()).getWindowSize(window.getClass());
				if( ! window.getSize().equals(previousWindowSize)) {
					log.debug("Storing size of " + window.getClass().getName() + ": " + window.getSize());
					((SwingPreferences) Application.getPreferences()).setWindowSize(window.getClass(), window.getSize());
				}
				if (window instanceof JFrame) {
					if ((((JFrame) window).getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
						log.debug("Storing maximized state of " + window.getClass().getName());
						((SwingPreferences) Application.getPreferences()).setWindowMaximized(window.getClass());
					}
				}
			}
		});
		
		if (((SwingPreferences) Application.getPreferences()).isWindowMaximized(window.getClass())) {
			if (window instanceof JFrame) {
				((JFrame) window).setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
		} else {
			Dimension dim = ((SwingPreferences) Application.getPreferences()).getWindowSize(window.getClass());
			if (dim != null) {
				window.setSize(dim);
			}
		}
	}

	public static int getOptimalColumnWidth(JTable table, int columnIndex) {
		if (columnIndex >= table.getColumnModel().getColumnCount()) {
			return -1;
		}

		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		Component headerRenderer = table.getTableHeader().getDefaultRenderer()
				.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, columnIndex);

		int maxWidth = headerRenderer.getPreferredSize().width;

		for (int row = 0; row < table.getRowCount(); row++) {
			Component renderer = table.getCellRenderer(row, columnIndex)
					.getTableCellRendererComponent(table, table.getValueAt(row, columnIndex), false, false, row, columnIndex);
			maxWidth = Math.max(maxWidth, renderer.getPreferredSize().width);
		}

		// Optional: Add some padding
		int padding = 5;  // adjust this value as needed
		return maxWidth + padding;
	}
	
	
	/**
	 * Automatically remember the position of a window.  The position is stored in the user preferences
	 * every time the window is moved and set from there when first calling this method.
	 */
	public static void rememberWindowPosition(final Window window) {
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				((SwingPreferences) Application.getPreferences()).setWindowPosition(window.getClass(), window.getLocation());
			}
		});
		
		// Set window position according to preferences, and set prefs when moving
		Point position = ((SwingPreferences) Application.getPreferences()).getWindowPosition(window.getClass());
		if (position != null) {
			window.setLocationByPlatform(false);
			window.setLocation(position);
		}
	}

	/**
	 * Computes the optimal column widths for the specified table, based on the content in all rows for that column,
	 * and optionally also the column header content.
	 * @param table the table
	 * @param max the maximum width for a column
	 * @param includeHeaderWidth whether to include the width of the column header
	 * @return an array of column widths
	 */
	public static int[] computeOptimalColumnWidths(JTable table, int max, boolean includeHeaderWidth) {
		int columns = table.getColumnCount();
		int[] widths = new int[columns];
		Arrays.fill(widths, 1);

		// Consider the width required by the header text if includeHeaderWidth is true
		if (includeHeaderWidth) {
			TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
			for (int col = 0; col < columns; col++) {
				TableColumn column = table.getColumnModel().getColumn(col);
				Component headerComp = headerRenderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, col);
				widths[col] = headerComp.getPreferredSize().width;
			}
		}

		// Compare header width to the width required by the cell data
		for (int row = 0; row < table.getRowCount(); row++) {
			for (int col = 0; col < columns; col++) {
				Object value = table.getValueAt(row, col);
				TableCellRenderer cellRenderer = table.getCellRenderer(row, col);
				Component cellComp = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, col);
				int cellWidth = cellComp.getPreferredSize().width;
				widths[col] = Math.max(widths[col], cellWidth);
			}
		}

		for (int col = 0; col < columns; col++) {
			widths[col] = Math.min(widths[col], max * 100);  // Adjusting for your max value and scaling
		}

		return widths;
	}

	public static Window getWindowAncestor(Component c) {
		while (c != null) {
			if (c instanceof Window) {
				return (Window) c;
			}
			c = c.getParent();
		}
		return null;
	}

	/**
	 * Changes the style of the font of the specified border.
	 * 
	 * @param border		the component for which to change the font
	 * @param style			the change in the font style
	 */
	public static void changeFontStyle(TitledBorder border, int style) {
		/*
		 * The fix of JRE bug #4129681 causes a TitledBorder occasionally to
		 * return a null font.  We try to work around the issue by detecting it
		 * and reverting to the font of a JLabel instead.
		 */
		Font font = border.getTitleFont();
		if (font == null) {
			log.warn("JRE bug workaround : Border font is null, reverting to JLabel font");
			font = new JLabel().getFont();
			if (font == null) {
				log.warn("JRE bug workaround : JLabel font is null, not modifying font");
				return;
			}
		}
		font = font.deriveFont(style);
		if (font == null) {
			throw new BugException("Derived font is null");
		}
		border.setTitleFont(font);
	}
	
	
	/**
	 * Changes the style of the font of the specified label.
	 * 
	 * @param label			the component for which to change the font
	 * @param style			the change in the font style
	 */
	public static void changeFontStyle(JLabel label, int style) {
		Font font = label.getFont();
		font = font.deriveFont(style);
		label.setFont(font);
	}
	
	
	
	/**
	 * Traverses recursively the component tree, and sets all applicable component 
	 * models to null, so as to remove the listener connections.  After calling this
	 * method the component hierarchy should no longed be used.
	 * <p>
	 * All components that use custom models should be added to this method, as
	 * there exists no standard way of removing the model from a component.
	 * 
	 * @param c		the component (<code>null</code> is ok)
	 */
	public static void setNullModels(Component c) {
		if (c == null)
			return;
		
		// Remove various listeners
		for (ComponentListener l : c.getComponentListeners()) {
			c.removeComponentListener(l);
		}
		for (FocusListener l : c.getFocusListeners()) {
			c.removeFocusListener(l);
		}
		for (MouseListener l : c.getMouseListeners()) {
			c.removeMouseListener(l);
		}
		for (PropertyChangeListener l : c.getPropertyChangeListeners()) {
			c.removePropertyChangeListener(l);
		}
		for (PropertyChangeListener l : c.getPropertyChangeListeners("model")) {
			c.removePropertyChangeListener("model", l);
		}
		for (PropertyChangeListener l : c.getPropertyChangeListeners("action")) {
			c.removePropertyChangeListener("action", l);
		}
		
		// Remove models for known components
		//  Why the FSCK must this be so hard?!?!?
		
		if (c instanceof JSpinner) {
			
			JSpinner spinner = (JSpinner) c;
			for (ChangeListener l : spinner.getChangeListeners()) {
				spinner.removeChangeListener(l);
			}
			SpinnerModel model = spinner.getModel();
			spinner.setModel(new SpinnerNumberModel());
			if (model instanceof Invalidatable) {
				((Invalidatable) model).invalidateMe();
			}
			
		} else if (c instanceof JSlider) {
			
			JSlider slider = (JSlider) c;
			for (ChangeListener l : slider.getChangeListeners()) {
				slider.removeChangeListener(l);
			}
			BoundedRangeModel model = slider.getModel();
			slider.setModel(new DefaultBoundedRangeModel());
			if (model instanceof Invalidatable) {
				((Invalidatable) model).invalidateMe();
			}
			
		} else if (c instanceof JComboBox) {
			@SuppressWarnings("unchecked")
			JComboBox<Object> combo = (JComboBox<Object>) c;
			for (ActionListener l : combo.getActionListeners()) {
				combo.removeActionListener(l);
			}
			ComboBoxModel<?> model = combo.getModel();
			combo.setModel(new DefaultComboBoxModel<Object>());
			if (model instanceof Invalidatable) {
				((Invalidatable) model).invalidateMe();
			}
			
		} else if (c instanceof AbstractButton) {
			
			AbstractButton button = (AbstractButton) c;
			for (ActionListener l : button.getActionListeners()) {
				button.removeActionListener(l);
			}
			Action model = button.getAction();
			button.setAction(new AbstractAction() {
				private static final long serialVersionUID = 3499667830135101535L;

				@Override
				public void actionPerformed(ActionEvent e) {
				}
			});
			if (model instanceof Invalidatable) {
				((Invalidatable) model).invalidateMe();
			}
			
		} else if (c instanceof JTable) {
			
			JTable table = (JTable) c;
			TableModel model1 = table.getModel();
			table.setModel(new DefaultTableModel());
			if (model1 instanceof Invalidatable) {
				((Invalidatable) model1).invalidateMe();
			}
			
			TableColumnModel model2 = table.getColumnModel();
			table.setColumnModel(new DefaultTableColumnModel());
			if (model2 instanceof Invalidatable) {
				((Invalidatable) model2).invalidateMe();
			}
			
			ListSelectionModel model3 = table.getSelectionModel();
			table.setSelectionModel(new DefaultListSelectionModel());
			if (model3 instanceof Invalidatable) {
				((Invalidatable) model3).invalidateMe();
			}
			
		} else if (c instanceof JTree) {
			
			JTree tree = (JTree) c;
			TreeModel model1 = tree.getModel();
			tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
			if (model1 instanceof Invalidatable) {
				((Invalidatable) model1).invalidateMe();
			}
			
			TreeSelectionModel model2 = tree.getSelectionModel();
			tree.setSelectionModel(new DefaultTreeSelectionModel());
			if (model2 instanceof Invalidatable) {
				((Invalidatable) model2).invalidateMe();
			}
			
		} else if (c instanceof Resettable) {
			
			((Resettable) c).resetModel();
			
		}
		
		// Recurse the component
		if (c instanceof Container) {
			Component[] cs = ((Container) c).getComponents();
			for (Component sub : cs)
				setNullModels(sub);
		}
		
	}
	
	
	
	/**
	 * A mouse listener that toggles the state of a boolean value in a table model
	 * when clicked on another column of the table.
	 * <p>
	 * NOTE:  If the table model does not extend AbstractTableModel, the model must
	 * fire a change event (which in normal table usage is not necessary).
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public static class BooleanTableClickListener extends MouseAdapter {
		
		private final JTable table;
		// these are different because the MouseEvent and the model use different indexing (0- vs 1-)
		private final int clickColumn;
		private final int booleanColumn;
		
		
		public BooleanTableClickListener(JTable table) {
			this(table, 1, 0);
		}
		
		
		public BooleanTableClickListener(JTable table, int clickColumn, int booleanColumn) {
			this.table = table;
			this.clickColumn = clickColumn;
			this.booleanColumn = booleanColumn;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1)
				return;
			
			final Point p = e.getPoint();
			final int tableColumn = table.columnAtPoint(p);
			if (tableColumn < 0)
				return;

			final int modelColumn= table.convertColumnIndexToModel(tableColumn);
			if (modelColumn != clickColumn)
				return;

			final int tableRow = table.rowAtPoint(p);
			if (tableRow < 0)
				return;

			final int modelRow = table.convertRowIndexToModel(tableRow);
			if ( modelRow < 0)
				return;

			TableModel model = table.getModel();
			final Object value = model.getValueAt(modelRow, booleanColumn);
	        if (!(value instanceof Boolean)) {
				throw new IllegalStateException("Table value at row=" + modelRow + " col=" +
						booleanColumn + " is not a Boolean, value=" + value);
			}
			
			final Boolean oldValue = (Boolean) value;
			final Boolean newValue = !oldValue;
			model.setValueAt(newValue, tableRow, booleanColumn);
			if (model instanceof AbstractTableModel) {
				((AbstractTableModel) model).fireTableCellUpdated(tableRow, booleanColumn);
			}
		}
		
	}

	/**
	 * Executes the given code after a specified delay.
	 *
	 * @param delayMillis the delay in milliseconds.
	 * @param runnable the code to be executed after the delay.
	 */
	public static void executeAfterDelay(int delayMillis, Runnable runnable) {
		Timer timer = new Timer(delayMillis, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				runnable.run();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

}
