package net.sf.openrocket.gui.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.Configuration;


public class StageSelector extends JPanel implements ChangeListener {

	private final Configuration configuration;
	
	private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
	
	public StageSelector(Configuration configuration) {
		super(new MigLayout("gap 0!"));
		this.configuration = configuration;
		
		JToggleButton button = new JToggleButton(new StageAction(0));
		this.add(button);
		buttons.add(button);
		
		updateButtons();
		configuration.addChangeListener(this);
	}
	
	private void updateButtons() {
		int stages = configuration.getStageCount();
		if (buttons.size() == stages)
			return;
		
		while (buttons.size() > stages) {
			JToggleButton button = buttons.remove(buttons.size()-1);
			this.remove(button);
		}
		
		while (buttons.size() < stages) {
			JToggleButton button = new JToggleButton(new StageAction(buttons.size()));
			this.add(button);
			buttons.add(button);
		}
		
		this.revalidate();
	}
	



	@Override
	public void stateChanged(ChangeEvent e) {
		updateButtons();
	}
	
	
	private class StageAction extends AbstractAction implements ChangeListener {
		private final int stage;

		public StageAction(final int stage) {
			this.stage = stage;
			configuration.addChangeListener(this);
			stateChanged(null);
		}
		
		@Override
		public Object getValue(String key) {
			if (key.equals(NAME)) {
				return "Stage "+(stage+1);
			}
			return super.getValue(key);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			configuration.setToStage(stage);
			
//			boolean state = (Boolean)getValue(SELECTED_KEY);
//			if (state == true) {
//				// Was disabled, now enabled
//				configuration.setToStage(stage);
//			} else {
//				// Was enabled, check what to do
//				if (configuration.isStageActive(stage + 1)) {
//					configuration.setToStage(stage);
//				} else {
//					if (stage == 0)
//						configuration.setAllStages();
//					else 
//						configuration.setToStage(stage-1);
//				}
//			}
//			stateChanged(null);
		}
		

		@Override
		public void stateChanged(ChangeEvent e) {
			this.putValue(SELECTED_KEY, configuration.isStageActive(stage));
		}
	}
}
