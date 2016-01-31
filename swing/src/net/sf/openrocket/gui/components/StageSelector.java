package net.sf.openrocket.gui.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StateChangeListener;


@SuppressWarnings("serial")
public class StageSelector extends JPanel implements StateChangeListener {

	private static final Translator trans = Application.getTranslator();
	
	private final Rocket rocket;
	
	private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
	
	public StageSelector(Rocket _rkt) {
		super(new MigLayout("gap 0!"));
		this.rocket = _rkt;
		
		updateButtons( this.rocket.getSelectedConfiguration() );
	}
	
	private void updateButtons( final FlightConfiguration configuration ) {
		int stages = configuration.getStageCount();
		if (buttons.size() == stages)
			return;
		
		buttons.clear();
		this.removeAll();
		for(AxialStage stage : configuration.getRocket().getStageList()){
			int stageNum = stage.getStageNumber(); 
			JToggleButton button = new JToggleButton(new StageAction(stageNum));
			button.setSelected(true);
			this.add(button);
			buttons.add(button);
		}
		
		this.revalidate();
	}
	
	@Override
	public void stateChanged(EventObject eo) {
		Object source = eo.getSource();
		if( source instanceof Rocket ){
			Rocket rkt = (Rocket) eo.getSource();
			updateButtons( rkt.getSelectedConfiguration() );
		}
	}
	
	private class StageAction extends AbstractAction {
		private final int stageNumber;
		
		public StageAction(final int stage) {
			this.stageNumber = stage;
		}
		
		@Override
		public Object getValue(String key) {
			if (key.equals(NAME)) {
				// Stage
				return trans.get("StageAction.Stage") + " " + (stageNumber );
			}
			return super.getValue(key);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			rocket.getSelectedConfiguration().toggleStage(stageNumber);
			rocket.fireComponentChangeEvent(ComponentChangeEvent.GRAPHIC_CHANGE);
		}
		
	}
}
