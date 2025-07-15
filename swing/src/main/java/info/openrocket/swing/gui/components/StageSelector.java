package info.openrocket.swing.gui.components;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StateChangeListener;


@SuppressWarnings("serial")
public class StageSelector extends JPanel implements StateChangeListener {

	private static final Translator trans = Application.getTranslator();
	private final Rocket rocket;
	
	private List<JToggleButton> buttons = new ArrayList<>();
	
	public StageSelector(Rocket _rkt) {
		super(new MigLayout("gap rel!, insets 0"));
		this.rocket = _rkt;
		
		updateButtons( this.rocket.getSelectedConfiguration() );
	}

	private void updateButtons(final FlightConfiguration configuration ) {
		buttons.clear();
		this.removeAll();
		List<ComponentAssembly> assemblies = configuration.getRocket().getAllChildAssemblies();

		// First, count the number of stages that are currently active and can be toggled
		int activeStageCount = 0;
		for (RocketComponent stage : assemblies) {
			if (stage instanceof AxialStage && configuration.isStageActive(stage.getStageNumber()) && ((AxialStage) stage).getChildCount() > 0) {
				activeStageCount++;
			}
		}

		for (RocketComponent component : assemblies) {
			if (!(component instanceof AxialStage stage)) continue;

			JToggleButton button = new JToggleButton(new StageAction(stage));
			boolean isActive = configuration.isStageActive(stage.getStageNumber());
			button.setSelected(isActive);

			button.setHorizontalTextPosition(SwingConstants.RIGHT);

			// If the stage is the only active one, disable its button to prevent deactivation.
			// The button might already be disabled by the StageAction if the stage has no children.
			if (button.isEnabled() && isActive && activeStageCount <= 1) {
				button.setEnabled(false);
				button.setToolTipText(trans.get("RocketPanel.btn.Stages.LastActive.ttip"));
			}

			// Set text to italic for any disabled button
			if (!button.isEnabled()) {
				button.setFont(button.getFont().deriveFont(Font.ITALIC));
				button.setIcon(null);
			} else {
				if (!button.isSelected()) {
					button.setIcon(GUIUtil.getUITheme().getDisabledIcon());
				} else {
					button.setIcon(null);
				}
			}

			this.add(button, "sgy stagebuttons");
			buttons.add(button);
		}

		this.revalidate();
		this.repaint();
	}
	
	@Override
	public void stateChanged(EventObject eo) {
		Object source = eo.getSource();
		if ((source instanceof Rocket) || (source instanceof AxialStage) || (source instanceof BodyTube)) {
			Rocket rkt = (Rocket) ((RocketComponent) source).getRoot();
			updateButtons( rkt.getSelectedConfiguration() );
		}
	}
	
	private class StageAction extends AbstractAction {
		private final AxialStage stage;

		public StageAction(final AxialStage stage) {
			this.stage = stage;
			if (this.stage.getChildCount() == 0) {
				putValue(SHORT_DESCRIPTION, trans.get("RocketPanel.btn.Stages.NoChildren.ttip"));
				setEnabled(false);
			} else {
				putValue(SHORT_DESCRIPTION, trans.get("RocketPanel.btn.Stages.Toggle.ttip"));
			}
			updateUI();
		}
		
		@Override
		public Object getValue(String key) {
			if (key.equals(NAME)) {
				// Stage
				return stage.getName();
			}
			return super.getValue(key);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FlightConfiguration config = rocket.getSelectedConfiguration();
			config.toggleStage(stage.getStageNumber());

			rocket.fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE | ComponentChangeEvent.MOTOR_CHANGE,
					config.getFlightConfigurationID());
		}
		
	}
}
