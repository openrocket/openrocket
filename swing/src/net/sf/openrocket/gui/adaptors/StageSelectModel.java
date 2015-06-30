package net.sf.openrocket.gui.adaptors;

import java.util.EventObject;
import java.util.Iterator;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;

public class StageSelectModel extends AbstractListModel<Stage> implements ComboBoxModel<Stage>, StateChangeListener {
	private static final long serialVersionUID = 1311302134934033684L;
	private static final Logger log = LoggerFactory.getLogger(StageSelectModel.class);

	protected final String nullText;//?
	
	protected Stage sourceStage = null;
	protected ArrayList<Stage> displayValues = new ArrayList<Stage>();
	protected Stage selectedStage = null;
	protected int selectedStageIndex=-1; // index of stage in rocket, as returned by stage.getStageNumber();

	//@SuppressWarnings("unchecked")
	public StageSelectModel( final Stage _stage, String nullText) {
		this.sourceStage = _stage;
		this.nullText = nullText;
		
		populateDisplayValues();
		
		stateChanged(null);  // Update current value
		this.sourceStage.addChangeListener(this);
	}

	public StageSelectModel( final Stage _stage ){
		this( _stage, "(no stage selected)");
	}
	
	private void populateDisplayValues(){
		Rocket rocket = this.sourceStage.getRocket();
		
		this.displayValues.clear();
		Iterator<RocketComponent> stageIter = rocket.getChildren().iterator();
		while( stageIter.hasNext() ){
			RocketComponent curComp = stageIter.next();
			if( curComp instanceof Stage ){
				Stage curStage = (Stage)curComp;
				if( curStage.equals( this.sourceStage )){
					continue;
				}else{
					displayValues.add( curStage );
				}
			}else{
				throw new IllegalStateException("Rocket has a child which is something other than a Stage: "+curComp.getClass().getCanonicalName()+"(called: "+curComp.getName()+")");
			}
					
		}
		
	}

	@Override
	public int getSize() {
		return this.displayValues.size();
	}

	@Override
	public Stage getElementAt(int index) {
		return this.displayValues.get(index);
	}

	@Override
	public void setSelectedItem(Object newItem) {
		if (newItem == null) {
			// Clear selection - huh?
			return;
		}
		
		if (newItem instanceof String) {
			log.error("setStage to string?  huh? (unexpected value type");
			return;
		}
		
		if( newItem instanceof Stage ){
			Stage nextStage = (Stage) newItem;
			int nextStageIndex = nextStage.getStageNumber();

			if (nextStage.equals(this.selectedStage)){
				return; // i.e. no change
			}

			this.selectedStage = nextStage;
			this.selectedStageIndex = nextStageIndex;
			this.sourceStage.setRelativeToStage(nextStageIndex);
			
			// DEVEL
			int nextDisplayIndex = this.displayValues.indexOf(newItem);
			log.error("DEVEL success. set stage number to: "+nextDisplayIndex+" @"+nextStageIndex);
			log.error("DEVEL success. set stage number to: "+nextStage.getName()+" ="+nextStage.toString()); 
			return;
		}
		
	}

	@Override
	public Stage getSelectedItem() {
		return this.selectedStage;
		//return "StageSelectModel["+this.selectedIndex+": "+this.displayValues.get(this.selectedIndex).getName()+"]";
	}

	@Override
	public void stateChanged(EventObject eo) {
		if(( null == this.sourceStage)||(null==this.selectedStage)){
			return;
		}
		int sourceRelToIndex = this.sourceStage.getRelativeToStage();
		int selectedRelIndex = this.selectedStage.getStageNumber();
		if ( selectedRelIndex != sourceRelToIndex){
			this.selectedStage = (Stage)sourceStage.getRocket().getChild(sourceRelToIndex);
			
			// I don't think this is required -- we're not changing the list, just the selected item.
			//this.fireContentsChanged(this, 0, values.length);
		}
	
	}
	
	@Override
	public String toString() {
		return "StageSelectModel["+this.selectedStage.getName()+" @"+this.selectedStageIndex+"]";
	}



}
