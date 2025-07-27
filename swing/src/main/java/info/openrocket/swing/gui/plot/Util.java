package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StringUtils;

public abstract class Util {
	private static final Translator trans = Application.getTranslator();

	public enum PlotAxisSelection {
		AUTO(-1, trans.get("simplotpanel.AUTO_NAME")),			// Automatically decide to plot on the left or right y-axis
		LEFT(0, trans.get("simplotpanel.LEFT_NAME")),			// Plot on the left y-axis
		RIGHT(1, trans.get("simplotpanel.RIGHT_NAME"));			// Plot on the right y-axis

		private final int value;
		private final String name;

		PlotAxisSelection(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

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

	public static <T extends DataType, B extends DataBranch<T>> List<String> generateSeriesLabels(List<B> branches) {
		List<String> series = new ArrayList<>(branches.size());
		// We need to generate unique strings for each of the branches.  Since the branch names are based
		// on the stage name there is no guarantee they are unique.  In order to address this, we first assume
		// all the names are unique, then go through them looking for duplicates.
		for (B branch : branches) {
			series.add(formatHTMLString(branch.getName()));
		}
		// check for duplicates:
		for (int i = 0; i < series.size(); i++) {
			String stagename = series.get(i);
			int numberDuplicates = Collections.frequency(series, stagename);
			if (numberDuplicates > 1) {
				int index = i;
				int count = 1;
				while (count <= numberDuplicates) {
					series.set(index, stagename + "(" + count + ")");
					count ++;
					for (index++; index < series.size() && !stagename.equals(series.get(index)); index++);
				}
			}
		}
		return series;
	}

	public static String formatHTMLString(String input) {
		// TODO: Use AttributeString to format the string
		// Remove the HTML-like tags from the final string
		return StringUtils.removeHTMLTags(input);
	}

	public static Color getPlotColor(int index) {
		return PLOT_COLORS[index % PLOT_COLORS.length];
	}
}
