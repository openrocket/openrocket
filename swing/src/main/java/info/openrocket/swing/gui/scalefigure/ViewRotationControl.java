package info.openrocket.swing.gui.scalefigure;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * An enhanced version of the rotation control component for the RocketPanel.
 * This implementation adds a text field for direct value input and a lock button
 * for disabling/enabling the click-drag rotation.
 */
public class ViewRotationControl extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	private final DoubleModel rotationModel;
	private final JSpinner spinner;
	private final UnitSelector unitSelector;
	private final BasicSlider rotationSlider;
	private final JToggleButton lockButton;
	private boolean dragRotationLocked = false;
	private boolean shiftKeyPressed = false;

	/**
	 * Creates a new enhanced rotation control panel
	 *
	 * @param figure the rocket figure to control
	 */
	public ViewRotationControl(RocketFigure figure) {
		super(new MigLayout("fill, insets 0, gap 0"));

		dragRotationLocked = prefs.isClickDragRotationLocked();

		// Create rotation model
		rotationModel = new DoubleModel(figure, "Rotation", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
		figure.addChangeListener(rotationModel);

		// Create spinner
		this.spinner = new JSpinner(rotationModel.getSpinnerModel());
		spinner.setToolTipText(trans.get("RocketPanel.ttip.Rotation"));
		spinner.setEditor(new SpinnerEditor(spinner));
		// Remove the spinner buttons
		spinner.setUI(new BasicSpinnerUI() {
			@Override
			protected Component createNextButton() {
				return null;
			}

			@Override
			protected Component createPreviousButton() {
				return null;
			}
		});

		// Create unit selector
		this.unitSelector = new UnitSelector(rotationModel);
		unitSelector.setHorizontalAlignment(JLabel.CENTER);
		unitSelector.setToolTipText(trans.get("RocketPanel.ttip.Rotation"));

		// Create a panel for the rotation controls
		JPanel controlsPanel = new JPanel(new MigLayout("fill, insets 0, gap 0"));

		// Create lock button
		lockButton = new JToggleButton(Icons.UNLOCKED);
		lockButton.setSelectedIcon(Icons.LOCKED);
		lockButton.setToolTipText(trans.get("RocketPanel.ttip.lockDragRotation"));
		lockButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dragRotationLocked = lockButton.isSelected();
				prefs.setClickDragRotationLocked(dragRotationLocked);
			}
		});
		Dimension lockSize = new Dimension(24, 24);
		lockButton.setPreferredSize(lockSize);
		lockButton.setMaximumSize(lockSize);
		lockButton.setMinimumSize(lockSize);
		lockButton.setSelected(dragRotationLocked);

		// Add components to the control panel
		controlsPanel.add(spinner, "width 50!");
		controlsPanel.add(unitSelector, "growx, wrap");

		rotationSlider = new BasicSlider(rotationModel.getSliderModel(0, 2 * Math.PI), JSlider.VERTICAL, true);
		rotationSlider.setToolTipText(trans.get("RocketPanel.ttip.Rotation"));
		
		// Track Shift key state via mouse events
		rotationSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				shiftKeyPressed = e.isShiftDown();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				shiftKeyPressed = false;
			}
		});
		
		// Track Shift key state during mouse motion (for when shift is pressed/released during drag)
		rotationSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				shiftKeyPressed = e.isShiftDown();
			}
		});
		
		// Add change listener to apply snapping when Shift is pressed
		rotationSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (rotationSlider.getValueIsAdjusting() && shiftKeyPressed) {
					double currentRotation = rotationModel.getValue();
					double snappedRotation = snapRotation(currentRotation);
					// Only update if the snapped value is different to avoid loops
					if (Math.abs(currentRotation - snappedRotation) > 0.001) {
						rotationModel.setValue(snappedRotation);
					}
				}
			}
		});

		// Add components to this panel
		add(controlsPanel, "growx, wrap");
		add(rotationSlider, "ax 50%, growy, pushy, wrap");
		add(lockButton, "ax 50%");
	}
	
	/**
	 * Snaps an angle (in radians) to the nearest multiple of 30 or 45 degrees.
	 * Returns whichever snap point (30° or 45° multiple) is closer to the input angle.
	 * 
	 * @param angle the angle in radians to snap
	 * @return the snapped angle in radians
	 */
	public static double snapRotation(double angle) {
		// Convert to degrees for easier calculation
		double angleDeg = Math.toDegrees(angle);
		
		// Calculate nearest multiples
		double nearest30 = Math.round(angleDeg / 30.0) * 30.0;
		double nearest45 = Math.round(angleDeg / 45.0) * 45.0;
		
		// Normalize to [0, 360)
		nearest30 = ((nearest30 % 360) + 360) % 360;
		nearest45 = ((nearest45 % 360) + 360) % 360;
		
		// Find which is closer
		double dist30 = Math.min(Math.abs(angleDeg - nearest30), 
		                         Math.min(Math.abs(angleDeg - (nearest30 - 360)), 
		                                 Math.abs(angleDeg - (nearest30 + 360))));
		double dist45 = Math.min(Math.abs(angleDeg - nearest45),
		                         Math.min(Math.abs(angleDeg - (nearest45 - 360)),
		                                 Math.abs(angleDeg - (nearest45 + 360))));
		
		// Return whichever is closer, converted back to radians
		if (dist30 <= dist45) {
			return Math.toRadians(nearest30);
		} else {
			return Math.toRadians(nearest45);
		}
	}

	/**
	 * Gets the rotation slider component
	 */
	public BasicSlider getRotationSlider() {
		return rotationSlider;
	}

	/**
	 * Checks if drag rotation is currently locked
	 */
	public boolean isDragRotationLocked() {
		return dragRotationLocked;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		rotationSlider.setEnabled(enabled);
		spinner.setEnabled(enabled);
		unitSelector.setEnabled(enabled);
		lockButton.setEnabled(enabled);
	}

	/**
	 * Sets whether drag rotation is locked
	 */
	public void setDragRotationLocked(boolean locked) {
		this.dragRotationLocked = locked;
		this.lockButton.setSelected(locked);
	}
}