package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;

/**
 * Handles the content of the <openrocket> tag.
 */
class OpenRocketContentHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;

	private boolean rocketDefined = false;
	private boolean simulationsDefined = false;
	private boolean datatypesDefined = false;

	public OpenRocketContentHandler(DocumentLoadingContext context) {
		this.context = context;
	}

	public OpenRocketDocument getDocument() {
		if (!rocketDefined)
			return null;
		return context.getOpenRocketDocument();
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("rocket")) {
			if (rocketDefined) {
				warnings.add(Warning
						.fromString("Multiple rocket designs within one document, "
								+ "ignoring later ones."));
				return null;
			}
			rocketDefined = true;
			return new ComponentParameterHandler(getDocument().getRocket(), context);
		}

		if (element.equals("datatypes")) {
			if (datatypesDefined) {
				warnings.add(Warning.fromString("Multiple datatype blocks. Ignoring later ones."));
				return null;
			}
			datatypesDefined = true;
			return new DatatypeHandler(this, context);
		}

		if (element.equals("simulations")) {
			if (simulationsDefined) {
				warnings.add(Warning
						.fromString("Multiple simulation definitions within one document, "
								+ "ignoring later ones."));
				return null;
			}
			simulationsDefined = true;
			return new SimulationsHandler(getDocument(), context);
		}

		if (element.equals("photostudio")) {
			return new PhotoStudioHandler(context.getOpenRocketDocument().getPhotoSettings());
		}

		warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));

		return null;
	}
}