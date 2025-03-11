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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An enhanced version of the rotation control component for the RocketPanel.
 * This implementation adds a text field for direct value input and a lock button
 * for disabling/enabling the click-drag rotation.
 */
public class ViewRotationControl extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	private final DoubleModel rotationModel;
	private final BasicSlider rotationSlider;
	private final JToggleButton lockButton;
	private boolean dragRotationLocked = false;

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
		JSpinner spinner = new JSpinner(rotationModel.getSpinnerModel());
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
		UnitSelector unitSelector = new UnitSelector(rotationModel);
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

		// Add components to this panel
		add(controlsPanel, "growx, wrap");
		add(rotationSlider, "ax 50%, growy, pushy, wrap");
		add(lockButton, "ax 50%");
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

	/**
	 * Sets whether drag rotation is locked
	 */
	public void setDragRotationLocked(boolean locked) {
		this.dragRotationLocked = locked;
		this.lockButton.setSelected(locked);
	}
}