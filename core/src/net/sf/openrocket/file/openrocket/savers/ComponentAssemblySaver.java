package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.Instanceable;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RingInstanceable;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class ComponentAssemblySaver extends RocketComponentSaver {
	
	
	private static final ComponentAssemblySaver instance = new ComponentAssemblySaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		if (!c.isAfter()) {
			if (c instanceof PodSet) {
				list.add("<podset>");
				instance.addParams(c, list);
				list.add("</podset>");
			}
//			else if (c instanceof ParallelStage) {
//				list.add("<boosterset>");
//				instance.addParams(c, list);
//				list.add("</boosterset>");
//			}
		}
		
		return list;
	}
	
	@Override
	protected void addParams(RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		ComponentAssembly ca = (ComponentAssembly) c;
		
		if (!ca.isAfter()) {
			elements.addAll(this.addAssemblyInstanceParams(ca));
		}
		
	}
	
	protected Collection<? extends String> addAssemblyInstanceParams(final ComponentAssembly currentStage) {
		List<String> elementsToReturn = new ArrayList<String>();
		final String instCt_tag = "instancecount";
		final String radoffs_tag = "radialoffset";
		final String startangle_tag = "angleoffset";
		
		if ( currentStage instanceof Instanceable) {
			int instanceCount = currentStage.getInstanceCount();
			elementsToReturn.add("<" + instCt_tag + ">" + instanceCount + "</" + instCt_tag + ">");
			if( currentStage instanceof RingInstanceable ){
				RingInstanceable ring = (RingInstanceable) currentStage;
				if(( currentStage instanceof ParallelStage )&&( ((ParallelStage)currentStage).getAutoRadialOffset() )){
					elementsToReturn.add("<" + radoffs_tag + ">auto</" + radoffs_tag + ">");
				}else{
					double radialOffset = ring.getRadialOffset();
					elementsToReturn.add("<" + radoffs_tag + ">" + radialOffset + "</" + radoffs_tag + ">");
				}
				double angularOffset = ring.getAngularOffset();
				elementsToReturn.add("<" + startangle_tag + ">" + angularOffset + "</" + startangle_tag + ">");
				
				
			}
		}
		
		return elementsToReturn;
	}
	
}