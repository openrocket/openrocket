package net.sf.openrocket.gui.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.document.Simulation;

public abstract class Util {

	public static List<String> generateSeriesLabels( Simulation simulation ) {
		int size = simulation.getSimulatedData().getBranchCount();
		ArrayList<String> stages = new ArrayList<String>(size);
		// we need to generate unique strings for each of the branches.  Since the branch names are based
		// on the stage name there is no guarantee they are unique.  In order to address this, we first assume
		// all the names are unique, then go through them looking for duplicates.
		for (int i = 0; i < simulation.getSimulatedData().getBranchCount(); i++) {
			stages.add(simulation.getSimulatedData().getBranch(i).getBranchName());
		}
		// check for duplicates:
		for( int i = 0; i< stages.size(); i++ ) {
			String stagename = stages.get(i);
			int numberDuplicates = Collections.frequency(stages, stagename);
			if ( numberDuplicates > 1 ) {
				int index = i;
				int count = 1;
				while( count <= numberDuplicates ) {
					stages.set(index, stagename + "(" + count + ")" );
					count ++;
					for( index++; index < stages.size() && !stagename.equals(stages.get(index)); index++ );
				}
			}
		}
		return stages;
	}
}
