package net.sf.openrocket.gui.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.StateChangeListener;


@SuppressWarnings("serial")
public class StageSelector extends JPanel implements StateChangeListener {

	private final Rocket rocket;
	
	private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
	
	public StageSelector(Rocket _rkt) {
		super(new MigLayout("gap 0!"));
		this.rocket = _rkt;
		
		updateButtons( this.rocket.getSelectedConfiguration() );
	}
	
	private void updateButtons( final FlightConfiguration configuration ) {
		buttons.clear();
		this.removeAll();
		for(AxialStage stage : configuration.getRocket().getStageList()){
			JToggleButton button = new JToggleButton(new StageAction(stage));
			button.setSelected(configuration.isStageActive(stage.getStageNumber()));
			this.add(button);
			buttons.add(button);
		}
		
		this.revalidate();
	}
	
	@Override
	public void stateChanged(EventObject eo) {
		Object source = eo.getSource();
		if ((source instanceof Rocket) || (source instanceof AxialStage)) {
			Rocket rkt = (Rocket) ((RocketComponent) source).getRoot();
			updateButtons( rkt.getSelectedConfiguration() );
		}
	}
	
	private class StageAction extends AbstractAction {
		private final AxialStage stage;
		
		public StageAction(final AxialStage stage) {
			this.stage = stage;
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
			rocket.getSelectedConfiguration().toggleStage(stage.getStageNumber());
			rocket.fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE | ComponentChangeEvent.MOTOR_CHANGE );
		}
		
	}
}
