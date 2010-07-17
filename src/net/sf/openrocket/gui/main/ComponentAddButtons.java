package net.sf.openrocket.gui.main;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.Prefs;
import net.sf.openrocket.util.Reflection;

/**
 * A component that contains addition buttons to add different types of rocket components
 * to a rocket.  It enables and disables buttons according to the current selection of a 
 * TreeSelectionModel. 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentAddButtons extends JPanel implements Scrollable {

	private static final int ROWS = 3;
	private static final int MAXCOLS = 6;
	private static final String BUTTONPARAM = "grow, sizegroup buttons";

	private static final int GAP = 5;
	private static final int EXTRASPACE = 0;
	
	private final ComponentButton[][] buttons;
	
	private final OpenRocketDocument document;
	private final TreeSelectionModel selectionModel;
	private final JViewport viewport;
	private final MigLayout layout;
	
	private final int width, height;
	
	
	public ComponentAddButtons(OpenRocketDocument document, TreeSelectionModel model, 
			JViewport viewport) {
		
		super();
		String constaint = "[min!]";
		for (int i=1; i<MAXCOLS; i++)
			constaint = constaint + GAP + "[min!]";
		
		layout = new MigLayout("fill",constaint);
		setLayout(layout);
		this.document = document;
		this.selectionModel = model;
		this.viewport = viewport;
		
		buttons = new ComponentButton[ROWS][];
		int row = 0;
		
		////////////////////////////////////////////
		
		
		addButtonRow("Body components and fin sets",row,
				new BodyComponentButton(NoseCone.class,"Nose cone"),
				new BodyComponentButton(BodyTube.class,"Body tube"),
				new BodyComponentButton(Transition.class,"Transition"),
				new FinButton(TrapezoidFinSet.class,"Trapezoidal"),  // TODO: MEDIUM: freer fin placing
				new FinButton(EllipticalFinSet.class,"Elliptical"),
				new FinButton(FreeformFinSet.class,"Freeform"),
				new FinButton(LaunchLug.class,"Launch lug")
		);
		
		row++;
		
		
		/////////////////////////////////////////////
		
		addButtonRow("Inner component",row,
				new ComponentButton(InnerTube.class, "Inner tube"),
				new ComponentButton(TubeCoupler.class, "Coupler"),
				new ComponentButton(CenteringRing.class, "Centering\nring"),
				new ComponentButton(Bulkhead.class, "Bulkhead"),
				new ComponentButton(EngineBlock.class, "Engine\nblock"));

		row++;
		
		////////////////////////////////////////////
		
		addButtonRow("Mass objects",row,
				new ComponentButton(Parachute.class, "Parachute"),
				new ComponentButton(Streamer.class, "Streamer"),
				new ComponentButton(ShockCord.class, "Shock cord"),
//				new ComponentButton("Motor clip"),
//				new ComponentButton("Payload"),
				new ComponentButton(MassComponent.class,"Mass\ncomponent")
		);
		
		
		// Get maximum button size
		int w=0, h=0;
		
		for (row=0; row < buttons.length; row++) {
			for (int col=0; col < buttons[row].length; col++) {
				Dimension d = buttons[row][col].getPreferredSize();
				if (d.width > w)
					w = d.width;
				if (d.height > h)
					h = d.height;
			}
		}
		
		// Set all buttons to maximum size
		System.out.println("Setting w="+w+" h="+h);
		width=w;
		height=h;
		Dimension d = new Dimension(width,height);
		for (row=0; row < buttons.length; row++) {
			for (int col=0; col < buttons[row].length; col++) {
				buttons[row][col].setMinimumSize(d);
				buttons[row][col].setPreferredSize(d);
				buttons[row][col].getComponent(0).validate();
			}
		}
		
		// Add viewport listener if viewport provided
		if (viewport != null) {
			viewport.addChangeListener(new ChangeListener() {
				private int oldWidth = -1;
				public void stateChanged(ChangeEvent e) {
					Dimension d = ComponentAddButtons.this.viewport.getExtentSize();
					if (d.width != oldWidth) {
						oldWidth = d.width;
						flowButtons();
					}
				}
			});
		}
		
		add(new JPanel(),"grow");
	}
	
	
	/**
	 * Adds a row of buttons to the panel.
	 * @param label  Label placed before the row
	 * @param row    Row number
	 * @param b      List of ComponentButtons to place on the row
	 */
	private void addButtonRow(String label, int row, ComponentButton ... b) {
		if (row>0)
			add(new JLabel(label),"span, gaptop unrel, wrap");
		else 
			add(new JLabel(label),"span, gaptop 0, wrap");
		
		int col=0;
		buttons[row] = new ComponentButton[b.length];

		for (int i=0; i<b.length; i++) {
			buttons[row][col] = b[i];
			if (i < b.length-1)
				add(b[i],BUTTONPARAM);
			else
				add(b[i],BUTTONPARAM+", wrap");
			col++;
		}
	}
	

	/**
	 * Flows the buttons in all rows of the panel.  If a button would come too close
	 * to the right edge of the viewport, "newline" is added to its constraints flowing 
	 * it to the next line.
	 */
	private void flowButtons() {
		if (viewport==null)
			return;
		
		int w;
		
		Dimension d = viewport.getExtentSize();

		for (int row=0; row < buttons.length; row++) {
			w=0;
			for (int col=0; col < buttons[row].length; col++) {
				w += GAP+width;
				String param = BUTTONPARAM+",width "+width+"!,height "+height+"!";

				if (w+EXTRASPACE > d.width) {
					param = param + ",newline";
					w = GAP+width;
				}
				if (col == buttons[row].length-1)
					param = param + ",wrap";
				layout.setComponentConstraints(buttons[row][col], param);
			}
		}
		revalidate();
	}
	
	
	
	/**
	 * Class for a component button.
	 */
	private class ComponentButton extends JButton implements TreeSelectionListener {
		protected Class<? extends RocketComponent> componentClass = null;
		private Constructor<? extends RocketComponent> constructor = null;
		
		/** Only label, no icon. */
		public ComponentButton(String text) {
			this(text,null,null);
		}
		
		/**
		 * Constructor with icon and label.  The icon and label are placed into the button.
		 * The label may contain "\n" as a newline.
		 */
		public ComponentButton(String text, Icon enabled, Icon disabled) {
			super();
			setLayout(new MigLayout("fill, flowy, insets 0, gap 0","",""));
			
			add(new JLabel(),"push, sizegroup spacing");
			
			// Add Icon
			if (enabled != null) {
				JLabel label = new JLabel(enabled);
				if (disabled != null)
					label.setDisabledIcon(disabled);
				add(label,"growx");
			}
				
			// Add labels
			String[] l = text.split("\n");
			for (int i=0; i<l.length; i++) {
				add(new StyledLabel(l[i],SwingConstants.CENTER,-3.0f),"growx");
			}
			
			add(new JLabel(),"push, sizegroup spacing");
			
			valueChanged(null);  // Update enabled status
			selectionModel.addTreeSelectionListener(this);
		}

		
		/**
		 * Main constructor that should be used.  The generated component type is specified
		 * and the text.  The icons are fetched based on the component type.
		 */
		public ComponentButton(Class<? extends RocketComponent> c, String text) {
			this(text,ComponentIcons.getLargeIcon(c),ComponentIcons.getLargeDisabledIcon(c));
			
			if (c==null)
				return;
			
			componentClass = c; 

			try {
				constructor = c.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Unable to get default "+
						"constructor for class "+c,e);
			}
		}
		
		
		/**
		 * Return whether the current component is addable when the component c is selected.
		 * c is null if there is no selection.  The default is to use c.isCompatible(class).
		 */
		public boolean isAddable(RocketComponent c) {
			if (c==null)
				return false;
			if (componentClass==null)
				return false;
			return c.isCompatible(componentClass);
		}
		
		/**
		 * Return the position to add the component if component c is selected currently.
		 * The first element of the returned array is the RocketComponent to add the component
		 * to, and the second (if non-null) an Integer telling the position of the component.
		 * A return value of null means that the user cancelled addition of the component.
		 * If the Integer is null, the component is added at the end of the sibling 
		 * list.  By default returns the end of the currently selected component.
		 * 
		 * @param c  The component currently selected
		 * @return   The position to add the new component to, or null if should not add.
		 */
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			return new Pair<RocketComponent, Integer>(c, null);
		}
		
		/**
		 * Updates the enabled status of the button.
		 * TODO: LOW: What about updates to the rocket tree?
		 */
		public void valueChanged(TreeSelectionEvent e) {
			updateEnabled();
		}
		
		/**
		 * Sets the enabled status of the button and all subcomponents.
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			Component[] c = getComponents();
			for (int i=0; i<c.length; i++)
				c[i].setEnabled(enabled);
		}
		

		/**
		 * Update the enabled status of the button.
		 */
		private void updateEnabled() {
			RocketComponent c=null;
			TreePath p = selectionModel.getSelectionPath();
			if (p!=null)
				c = (RocketComponent)p.getLastPathComponent();
			setEnabled(isAddable(c));
		}

		
		@Override
		protected void fireActionPerformed(ActionEvent event) {
			super.fireActionPerformed(event);
			RocketComponent c = null;
			Integer position = null;
			
			TreePath p = selectionModel.getSelectionPath();
			if (p!= null)
				c = (RocketComponent)p.getLastPathComponent();

			Pair<RocketComponent, Integer> pos = getAdditionPosition(c);
			if (pos==null) {
				// Cancel addition
				return;
			}
			c = pos.getU();
			position = pos.getV();

			
			if (c == null) {
				// Should not occur
				ExceptionHandler.handleErrorCondition("ERROR:  Could not place new component.");
				updateEnabled();
				return;
			}
			
			if (constructor == null) {
				ExceptionHandler.handleErrorCondition("ERROR:  Construction of type not supported yet.");
				return;
			}
			
			RocketComponent component;
			try {
				component = (RocketComponent)constructor.newInstance();
			} catch (InstantiationException e) {
				throw new BugException("Could not construct new instance of class "+
						constructor,e);
			} catch (IllegalAccessException e) {
				throw new BugException("Could not construct new instance of class "+
						constructor,e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
			
			// Next undo position is set by opening the configuration dialog
			document.addUndoPosition("Add " + component.getComponentName());
			
			
			if (position == null)
				c.addChild(component);
			else
				c.addChild(component, position);
			
			// Select new component and open config dialog
			selectionModel.setSelectionPath(ComponentTreeModel.makeTreePath(component));
			
			JFrame parent = null;
			for (Component comp = ComponentAddButtons.this; comp != null; 
				 comp = comp.getParent()) {
				if (comp instanceof JFrame) {
					parent = (JFrame) comp;
					break;
				}
			}
				
			ComponentConfigDialog.showDialog(parent, document, component);
		}
	}
	
	/**
	 * A class suitable for BodyComponents.  Addition is allowed ...  
	 */
	private class BodyComponentButton extends ComponentButton {
		
		public BodyComponentButton(Class<? extends RocketComponent> c, String text) {
			super(c, text);
		}

		public BodyComponentButton(String text, Icon enabled, Icon disabled) {
			super(text, enabled, disabled);
		}

		public BodyComponentButton(String text) {
			super(text);
		}

		@Override
		public boolean isAddable(RocketComponent c) {
			if (super.isAddable(c))
				return true;
			// Handled separately:
			if (c instanceof BodyComponent)
				return true;
			if (c == null || c instanceof Rocket)
				return true;
			return false;
		}
		
		@Override
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			if (super.isAddable(c))     // Handled automatically
				return super.getAdditionPosition(c);
			
			
			if (c == null || c instanceof Rocket) {
				// Add as last body component of the last stage
				Rocket rocket = document.getRocket();
				return new Pair<RocketComponent,Integer>(rocket.getChild(rocket.getStageCount()-1),
						null);
			}
			
			if (!(c instanceof BodyComponent))
				return null;
			RocketComponent parent = c.getParent();
			assert(parent != null);
			
			// Check whether to insert between or at the end.
			// 0 = ask, 1 = in between, 2 = at the end
			int pos = Prefs.getChoise(Prefs.BODY_COMPONENT_INSERT_POSITION_KEY, 2, 0);
			if (pos==0) {
				if (parent.getChildPosition(c) == parent.getChildCount()-1)
					pos = 2;  // Selected component is the last component
				else
					pos = askPosition();
			}
			
			switch (pos) {
			case 0:
				// Cancel
				return null;
			case 1:
				// Insert after current position
				return new Pair<RocketComponent,Integer>(parent, parent.getChildPosition(c)+1);
			case 2:
				// Insert at the end of the parent
				return new Pair<RocketComponent,Integer>(parent, null);
			default:
				ExceptionHandler.handleErrorCondition("ERROR:  Bad position type: "+pos);
				return null;
			}
		}
		
		private int askPosition() {
			Object[] options = { "Insert here", "Add to the end", "Cancel" };
			
			JPanel panel = new JPanel(new MigLayout());
			JCheckBox check = new JCheckBox("Do not ask me again");
			panel.add(check,"wrap");
			panel.add(new StyledLabel("You can change the default operation in the " +
					"preferences.",-2));
			
			int sel = JOptionPane.showOptionDialog(null,  // parent component 
					new Object[] {
					"Insert the component after the current component or as the last " +
					"component?",
					panel },
					"Select component position",   // title
					JOptionPane.DEFAULT_OPTION,    // default selections
					JOptionPane.QUESTION_MESSAGE,  // dialog type
					null,         // icon
					options,      // options
					options[0]);  // initial value

			switch (sel) {
			case JOptionPane.CLOSED_OPTION:
			case 2:
				// Cancel
				return 0;
			case 0:
				// Insert
				sel = 1;
				break;
			case 1:
				// Add
				sel = 2;
				break;
			default:
				ExceptionHandler.handleErrorCondition("ERROR:  JOptionPane returned "+sel);
				return 0;
			}
			
			if (check.isSelected()) {
				// Save the preference
				Prefs.NODE.putInt(Prefs.BODY_COMPONENT_INSERT_POSITION_KEY, sel);
			}
			return sel;
		}
		
	}
	


	/**
	 * Class for fin sets, that attach only to BodyTubes.
	 */
	private class FinButton extends ComponentButton {
		public FinButton(Class<? extends RocketComponent> c, String text) {
			super(c, text);
		}

		public FinButton(String text, Icon enabled, Icon disabled) {
			super(text, enabled, disabled);
		}

		public FinButton(String text) {
			super(text);
		}

		@Override
		public boolean isAddable(RocketComponent c) {
			if (c==null)
				return false;
			return (c.getClass().equals(BodyTube.class));
		}
	}


	
	/////////  Scrolling functionality

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL)
			return visibleRect.height * 8 / 10;
		return 10;
	}


	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}
	
}

