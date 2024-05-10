package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.openrocket.core.document.Simulation;

public abstract class Util {
	private static final Color[] PLOT_COLORS = {
			new Color(0,114,189),
			new Color(217,83,25),
			new Color(237,177,32),
			new Color(126,49,142),
			new Color(119,172,48),
			new Color(77,190,238),
			new Color(162,20,47),
			new Color(197, 106, 122),
			new Color(255, 127, 80),
			new Color(85, 107, 47),
	};

	public static List<String> generateSeriesLabels( Simulation simulation ) {
		int size = simulation.getSimulatedData().getBranchCount();
		ArrayList<String> stages = new ArrayList<String>(size);
		// we need to generate unique strings for each of the branches.  Since the branch names are based
		// on the stage name there is no guarantee they are unique.  In order to address this, we first assume
		// all the names are unique, then go through them looking for duplicates.
		for (int i = 0; i < simulation.getSimulatedData().getBranchCount(); i++) {
			stages.add(simulation.getSimulatedData().getBranch(i).getName());
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

	public static Color getPlotColor(int index) {
		return PLOT_COLORS[index % PLOT_COLORS.length];
	}
}
