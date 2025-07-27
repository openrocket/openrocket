/*
 * StreamerHandler.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;

import org.xml.sax.SAXException;

/**
 * A SAX handler for Streamer components.
 */
class StreamerHandler extends RecoveryDeviceHandler<Streamer> {

	/**
	 * The OpenRocket Streamer.
	 */
	private final Streamer streamer;

	/**
	 * Constructor.
	 *
	 * @param c        the parent component
	 * @param warnings the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public StreamerHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings)
			throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent of a streamer may not be null.");
		}
		streamer = new Streamer();
		if (isCompatible(c, Streamer.class, warnings)) {
			c.addChild(streamer);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);

		try {
			if (RockSimCommonConstants.WIDTH.equals(element)) {
				streamer.setStripWidth(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.LEN.equals(element)) {
				streamer.setStripLength(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.DRAG_COEFFICIENT.equals(element)) {
				streamer.setCD(Double.parseDouble(content));
			}
			if (RockSimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Streamer getComponent() {
		return streamer;
	}

}