package info.openrocket.swing.gui.main;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;

import info.openrocket.core.preferences.ApplicationPreferences;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Pair;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;

/**
 * A component that contains addition buttons to add different types of rocket components
 * to a rocket.  It enables and disables buttons according to the current selection of a 
 * TreeSelectionModel. 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentAddButtons extends JPanel implements Scrollable {
	private static final long serialVersionUID = 4315680855765544950L;
	
	private static final Translator trans = Application.getTranslator();
	
	private static final int ROWS = 4;

	private final ComponentButton[][] buttons;
	
	private final OpenRocketDocument document;
	private final TreeSelectionModel selectionModel;
	private final JViewport viewport;
	private final MigLayout layout;

	private static final int BUTTON_GAP = 9;  // Space between buttons
	private static final int BUTTON_PADDING = 6;  // Internal padding within buttons

	
	public ComponentAddButtons(OpenRocketDocument document, TreeSelectionModel model,
			JViewport viewport) {
		
		super();

		// With this more flexible configuration:
		layout = new MigLayout("wrap", // Layout constraints - enable wrapping
				"[]", // Column constraints - don't grow
				"[]"); // Row constraints
		setLayout(layout);
		this.document = document;
		this.selectionModel = model;
		this.viewport = viewport;

		buttons = new ComponentButton[ROWS][];
		for (int rowCur = 0; rowCur < ROWS; rowCur++) {
			buttons[rowCur] = null;
		}
		int row = 0;

		////////////////////////////////////////////
		add(new JLabel(trans.get("compaddbuttons.ComponentAssembly")), "span, gaptop 0, wrap");

		//// Component Assembly Components:
		addButtonGroup(row,
				new StageButton(AxialStage.class, trans.get("compaddbuttons.AxialStage")),
				new ComponentButton(document, selectionModel, ParallelStage.class, trans.get("compaddbuttons.ParallelStage")),
				new ComponentButton(document, selectionModel, PodSet.class, trans.get("compaddbuttons.Pods")));
		row++;

		////////////////////////////////////////////
		
		//// Body components and fin sets
		add(new JLabel(trans.get("compaddbuttons.Bodycompandfinsets")), "span, gaptop unrel, wrap");
		addButtonGroup(row, 
				//// Nose cone
				new BodyComponentButton(NoseCone.class, trans.get("compaddbuttons.Nosecone")),
				//// Body tube
				new BodyComponentButton(BodyTube.class, trans.get("compaddbuttons.Bodytube")),
				//// Transition
				new BodyComponentButton(Transition.class, trans.get("compaddbuttons.Transition")),
				//// Trapezoidal
				new ComponentButton(document, selectionModel, TrapezoidFinSet.class, trans.get("compaddbuttons.Trapezoidal")), // TODO: MEDIUM: freer fin placing
				//// Elliptical
				new ComponentButton(document, selectionModel, EllipticalFinSet.class, trans.get("compaddbuttons.Elliptical")),
				//// Freeform
				new ComponentButton(document, selectionModel, FreeformFinSet.class, trans.get("compaddbuttons.Freeform")),
				//// Freeform
				new ComponentButton(document, selectionModel, TubeFinSet.class, trans.get("compaddbuttons.Tubefin")),
				//// Rail Button
				new ComponentButton(document, selectionModel, RailButton.class, trans.get("compaddbuttons.RailButton")),
				//// Launch lug
				new ComponentButton(document, selectionModel, LaunchLug.class, trans.get("compaddbuttons.Launchlug")));
		row++;
		
		/////////////////////////////////////////////
		
		//// Inner component
		add(new JLabel(trans.get("compaddbuttons.InnerComponent")), "span, gaptop unrel, wrap");
		addButtonGroup(row, 
				//// Inner tube
				new ComponentButton(document, selectionModel, InnerTube.class, trans.get("compaddbuttons.Innertube")),
				//// Coupler
				new ComponentButton(document, selectionModel, TubeCoupler.class, trans.get("compaddbuttons.Coupler")),
				//// Centering\nring
				new ComponentButton(document, selectionModel, CenteringRing.class, trans.get("compaddbuttons.Centeringring")),
				//// Bulkhead
				new ComponentButton(document, selectionModel, Bulkhead.class, trans.get("compaddbuttons.Bulkhead")),
				//// Engine\nblock
				new ComponentButton(document, selectionModel, EngineBlock.class, trans.get("compaddbuttons.Engineblock")));
		
		row++;

		////////////////////////////////////////////
		add(new JLabel(trans.get("compaddbuttons.MassComponents")), "span, gaptop unrel, wrap");

		//// Mass objects
		// NOTE: These are on the same line as the assemblies above
		addButtonGroup(row, 
				//// Parachute
				new ComponentButton(document, selectionModel, Parachute.class, trans.get("compaddbuttons.Parachute")),
				//// Streamer
				new ComponentButton(document, selectionModel, Streamer.class, trans.get("compaddbuttons.Streamer")),
				//// Shock cord
				new ComponentButton(document, selectionModel, ShockCord.class, trans.get("compaddbuttons.Shockcord")),
				//				new ComponentButton("Motor clip"),
				//				new ComponentButton("Payload"),
				//// Mass component
				new ComponentButton(document, selectionModel, MassComponent.class, trans.get("compaddbuttons.MassComponent")));

		// Align the buttons
		if (viewport != null) {
			viewport.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					flowButtons();
				}
			});
		}

		flowButtons();

		add(new JPanel(), "grow");

		//SwingUtilities.invokeLater(this::flowButtons);
	}
	
	
	/**
	 * Adds a buttons to the panel in a row.  Assumes.
	 *
	 * @param row    Row number
	 * @param b      List of ComponentButtons to place on the row
	 */
	private void addButtonGroup(int row, ComponentButton... b) {
		// Create a panel for this group of buttons with specific gaps
		JPanel groupPanel = new JPanel(new MigLayout("ins 0",
				"[]" + BUTTON_GAP + "[]",
				"[]"));

		int oldLen = 0;
		if (null == buttons[row]) {
			buttons[row] = new ComponentButton[b.length];
		} else {
			ComponentButton[] oldArr = buttons[row];
			oldLen = oldArr.length;
			ComponentButton[] newArr = new ComponentButton[oldLen + b.length];
			System.arraycopy(oldArr, 0, newArr, 0, oldLen);
			buttons[row] = newArr;
		}

		int dstCol = oldLen;
		int srcCol = 0;
		while (srcCol < b.length) {
			ComponentButton button = b[srcCol];
			buttons[row][dstCol] = button;

			// Set button padding
			button.setMargin(new Insets(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING));

			groupPanel.add(button, "top, gapright " + BUTTON_GAP);
			dstCol++;
			srcCol++;
		}

		// Add the group panel to the main panel
		add(groupPanel, "wrap");
	}

	/**
	 * Calculate and apply uniform size to all buttons
	 */
	private void uniformButtonSizes() {
		int maxWidth = 0;
		int maxHeight = 0;

		// First pass: find maximum dimensions and ensure buttons have rendered
		for (ComponentButton[] buttonRow : buttons) {
			if (buttonRow == null) continue;
			for (ComponentButton button : buttonRow) {
				if (button == null) continue;
				// Force the button to calculate its preferred size
				button.setSize(button.getPreferredSize());
				Dimension prefSize = button.getPreferredSize();
				maxWidth = Math.max(maxWidth, prefSize.width);
				maxHeight = Math.max(maxHeight, prefSize.height);
			}
		}

		// Second pass: apply uniform size
		Dimension uniformSize = new Dimension(maxWidth, maxHeight);
		for (ComponentButton[] buttonRow : buttons) {
			if (buttonRow == null) continue;
			for (ComponentButton button : buttonRow) {
				if (button == null) continue;
				button.setPreferredSize(uniformSize);
				button.setMinimumSize(uniformSize);
				button.setMaximumSize(uniformSize);
			}
		}
	}
	
	
	/**
	 * Flows the buttons in all rows of the panel.
	 */
	private void flowButtons() {
		if (viewport == null)
			return;

		uniformButtonSizes();

		int availableWidth = viewport.getWidth() - 35;	// Add extra margin
		boolean anyLayoutChanged = false;

		// For each group panel
		for (Component comp : getComponents()) {
			if (comp instanceof JPanel) {
				JPanel groupPanel = (JPanel) comp;
				Component[] groupButtons = groupPanel.getComponents();

				if (groupButtons.length > 0) {
					int totalButtonWidth = groupButtons[0].getPreferredSize().width + BUTTON_GAP;
					int buttonsPerRow = Math.max(1, (availableWidth - BUTTON_GAP) / totalButtonWidth);

					MigLayout layout = (MigLayout)groupPanel.getLayout();
					String newConstraints = "ins 0, wrap " + buttonsPerRow;

					// Only update if constraints actually changed
					if (!newConstraints.equals(layout.getLayoutConstraints())) {
						layout.setLayoutConstraints(newConstraints);
						anyLayoutChanged = true;

						// Force the group panel to re-layout
						groupPanel.invalidate();
					}
				}
			}
		}

		if (anyLayoutChanged) {
			// Force complete re-layout of the entire component hierarchy
			Container parent = getParent();
			while (parent != null) {
				parent.invalidate();
				parent = parent.getParent();
			}

			// Request validation of the entire hierarchy
			SwingUtilities.invokeLater(() -> {
				Container top = getTopLevelAncestor();
				if (top != null) {
					top.validate();
					top.repaint();
				}
			});
		}
	}

	/**
	 * A class suitable for BodyComponents.  Addition is allowed ...  
	 */
	private class BodyComponentButton extends ComponentButton {
		private static final long serialVersionUID = 1574998068156786363L;

		public BodyComponentButton(Class<? extends RocketComponent> c, String text) {
			super(document, selectionModel, c, text);
		}
		
		public BodyComponentButton(String text, Icon enabled, Icon disabled) {
			super(document, selectionModel, text, enabled, disabled);
		}
		
		public BodyComponentButton(String text) {
			super(document, selectionModel, text);
		}

		@Override
		public boolean isAddable(RocketComponent selectedComponent) {
			if (super.isAddable(selectedComponent)) {
				return true;
			}else if (selectedComponent instanceof BodyComponent) {
	            // Handled separately:
				return true;
		    }else if (selectedComponent == null) {
			    return false;
		    }else if( selectedComponent instanceof Rocket) {
			    return false;
			}
			return false;
		}
		
		@Override
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			if (super.isAddable(c)) // Handled automatically
				return super.getAdditionPosition(c);
			

			if (c == null || c instanceof Rocket) {
				// Add as last body component of the last stage
				Rocket rocket = document.getRocket();
				return new Pair<>(rocket.getChild(rocket.getStageCount() - 1),
						null);
			}
			
			if (!(c instanceof BodyComponent))
				return null;
			RocketComponent parent = c.getParent();
			if (parent == null) {
				throw new BugException("Component " + c.getComponentName() + " is the root component, " +
						"componentClass=" + componentClass);
			}
			
			// Check whether to insert between or at the end.
			// 0 = ask, 1 = in between, 2 = at the end
			int pos = Application.getPreferences().getChoice(ApplicationPreferences.BODY_COMPONENT_INSERT_POSITION_KEY, 2, 0);
			if (pos == 0) {
				if (parent.getChildPosition(c) == parent.getChildCount() - 1)
					pos = 2; // Selected component is the last component
				else
					pos = askPosition();
			}

			return switch (pos) {
				case 0 ->
					// Cancel
						null;
				case 1 ->
					// Insert after current position
						new Pair<>(parent, parent.getChildPosition(c) + 1);
				case 2 ->
					// Insert at the end of the parent
						new Pair<>(parent, null);
				default -> {
					Application.getExceptionHandler().handleErrorCondition("ERROR:  Bad position type: " + pos);
					yield null;
				}
			};
		}
		
		private int askPosition() {
			//// Insert here 
			//// Add to the end
			//// Cancel
			Object[] options = { trans.get("compaddbuttons.askPosition.Inserthere"), 
					trans.get("compaddbuttons.askPosition.Addtotheend"), 
					trans.get("compaddbuttons.askPosition.Cancel") };
			
			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox check = new JCheckBox(trans.get("compaddbuttons.Donotaskmeagain"));
			panel.add(check, "wrap");
			//// You can change the default operation in the preferences.
			panel.add(new StyledLabel(trans.get("compaddbuttons.lbl.Youcanchange"), -2));
			
			int sel = JOptionPane.showOptionDialog(null, // parent component 
					//// Insert the component after the current component or as the last component?
					new Object[] {
					trans.get("compaddbuttons.lbl.insertcomp"),
							panel },
							//// Select component position
							trans.get("compaddbuttons.Selectcomppos"), // title
					JOptionPane.DEFAULT_OPTION, // default selections
					JOptionPane.QUESTION_MESSAGE, // dialog type
					null, // icon
					options, // options
					options[0]); // initial value
			
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
				Application.getExceptionHandler().handleErrorCondition("ERROR:  JOptionPane returned " + sel);
				return 0;
			}
			
			if (check.isSelected()) {
				// Save the preference
				Application.getPreferences().putInt(ApplicationPreferences.BODY_COMPONENT_INSERT_POSITION_KEY, sel);
			}
			return sel;
		}
		
	}

	/**
	 * A class suitable for the Stage component.
	 * If a stage component or any of its subcomponents is selected and there is already a stage after the
	 * selected stage or parent stage, then a popup window will ask whether the user wants to insert the stage
	 * at the end or between the two stages.
	 * In any other case, the new stage will be added to the end of the component tree
	 */
	private class StageButton extends ComponentButton {

		public StageButton(String text) {
			super(document, selectionModel, text);
		}

		public StageButton(String text, Icon enabled, Icon disabled) {
			super(document, selectionModel, text, enabled, disabled);
		}

		public StageButton(Class<? extends RocketComponent> c, String text) {
			super(document, selectionModel, c, text);
		}

		@Override
		public boolean isAddable(RocketComponent c) {
			return true;
		}

		@Override
		public Pair<RocketComponent, Integer> getAdditionPosition(RocketComponent c) {
			if (c == null || c instanceof Rocket) {
				// Add to the end
				return new Pair<>(document.getRocket(), null);
			}

			RocketComponent parentStage = null;
			if (c instanceof AxialStage)
				parentStage = c;
			else {
				parentStage = c.getStage();
			}
			if (parentStage == null) {
				throw new BugException("Component " + c.getComponentName() + " has no parent stage");
			}

			// Check whether to insert between or at the end.
			// 0 = ask, 1 = in between, 2 = at the end
			int pos = Application.getPreferences().getChoice(ApplicationPreferences.STAGE_INSERT_POSITION_KEY, 2, 0);
			if (pos == 0) {
				if (document.getRocket().getChildPosition(parentStage) == document.getRocket().getChildCount() - 1)
					pos = 2; // Selected component is the last component
				else
					pos = askPosition();
			}

			return switch (pos) {
				case 0 ->
					// Cancel
						null;
				case 1 ->
					// Insert after current stage
						new Pair<>(document.getRocket(), document.getRocket().getChildPosition(parentStage) + 1);
				case 2 ->
					// Insert at the end
						new Pair<>(document.getRocket(), null);
				default -> {
					Application.getExceptionHandler().handleErrorCondition("ERROR:  Bad position type: " + pos);
					yield null;
				}
			};
		}

		private int askPosition() {
			//// Insert here
			//// Add to the end
			//// Cancel
				Object[] options = { trans.get("compaddbuttons.askPosition.Inserthere"),
					trans.get("compaddbuttons.askPosition.Addtotheend"),
					trans.get("compaddbuttons.askPosition.Cancel") };

			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox check = new JCheckBox(trans.get("compaddbuttons.Donotaskmeagain"));
			panel.add(check, "wrap");
			//// You can change the default operation in the preferences.
			panel.add(new StyledLabel(trans.get("compaddbuttons.lbl.Youcanchange"), -2));

			int sel = JOptionPane.showOptionDialog(null, // parent component
					//// Insert the component after the current component or as the last component?
					new Object[] {
							trans.get("compaddbuttons.lbl.insertstage"),
							panel },
					//// Select component position
					trans.get("compaddbuttons.Selectstagepos"), // title
					JOptionPane.DEFAULT_OPTION, // default selections
					JOptionPane.QUESTION_MESSAGE, // dialog type
					null, // icon
					options, // options
					options[0]); // initial value

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
					Application.getExceptionHandler().handleErrorCondition("ERROR:  JOptionPane returned " + sel);
					return 0;
			}

			if (check.isSelected()) {
				// Save the preference
				Application.getPreferences().putInt(ApplicationPreferences.STAGE_INSERT_POSITION_KEY, sel);
			}
			return sel;
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
