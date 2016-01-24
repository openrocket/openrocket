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
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StateChangeListener;


public class StageSelector extends JPanel implements StateChangeListener {
	private static final long serialVersionUID = -2898763402479628711L;

	private static final Translator trans = Application.getTranslator();
	
	private final FlightConfiguration configuration;
	
	private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
	
	public StageSelector(FlightConfiguration configuration) {
		super(new MigLayout("gap 0!"));
		this.configuration = configuration;
		
		updateButtons();
		
	}
	
	private void updateButtons() {
		int stages = configuration.getStageCount();
		if (buttons.size() == stages)
			return;
		
		buttons.clear();
		this.removeAll();
		for(AxialStage stage : configuration.getRocket().getStageList()){
			int stageNum = stage.getStageNumber(); 
			JToggleButton button = new JToggleButton(new StageAction(stageNum));
			this.add(button);
			buttons.add(button);
		}
		
		this.revalidate();
	}
	
	
	@Override
	public void stateChanged(EventObject e) {
		updateButtons();
	}
	
	
	private class StageAction extends AbstractAction implements StateChangeListener {
		private static final long serialVersionUID = 7433006728984943763L;
		private final int stageNumber;
		
		public StageAction(final int stage) {
			this.stageNumber = stage;
			stateChanged(null);
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
			configuration.toggleStage(stageNumber);
		}
		
		@Override
		public void stateChanged(EventObject e) {
			this.putValue(SELECTED_KEY, configuration.isStageActive(stageNumber));
		}
	}
}
