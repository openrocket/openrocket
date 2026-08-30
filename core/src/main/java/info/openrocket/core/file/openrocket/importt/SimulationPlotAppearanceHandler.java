package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;
import java.util.Map;

import info.openrocket.core.document.PlotAppearance;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.util.LineStyle;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.StringUtils;

class SimulationPlotAppearanceHandler extends AbstractElementHandler {
	private final Map<String, PlotAppearance> plotAppearances = new HashMap<>();

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (element.equals("series")) {
			return PlainTextHandler.INSTANCE;
		}
		warnings.add("Unknown element '" + element + "', ignoring.");
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) {
		if (!element.equals("series")) {
			return;
		}

		String symbol = attributes.get("symbol");
		if (StringUtils.isEmpty(symbol)) {
			warnings.add("Plot appearance series missing symbol, ignoring.");
			return;
		}

		LineStyle lineStyle = (LineStyle) DocumentConfig.findEnum(attributes.get("linestyle"), LineStyle.class);
		ORColor color = ORColor.fromXMLAttributes(attributes);

		PlotAppearance appearance = new PlotAppearance(color, lineStyle);
		if (!appearance.isEmpty()) {
			plotAppearances.put(symbol, appearance);
		}
	}

	public Map<String, PlotAppearance> getPlotAppearances() {
		return plotAppearances;
	}
}
