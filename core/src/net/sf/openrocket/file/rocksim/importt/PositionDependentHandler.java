/*
 * PositionDependentHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimLocationMode;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;

import org.xml.sax.SAXException;

/**
 * An abstract base class that handles position dependencies for all lower level components that
 * are position aware.
 *
 * @param <C>   the specific position dependent RocketComponent subtype for which the concrete handler can create
 */
public abstract class PositionDependentHandler<C extends RocketComponent> extends BaseHandler<C> {
	
	/** Temporary position value. */
	private Double positionValue = 0d;
	
	/** Temporary position. */
	private AxialMethod position = AxialMethod.TOP;
	
	public PositionDependentHandler(DocumentLoadingContext context) {
		super(context);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		if (RocksimCommonConstants.XB.equals(element)) {
			positionValue = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
		}
		if (RocksimCommonConstants.LOCATION_MODE.equals(element)) {
			position = RocksimLocationMode.fromCode(Integer.parseInt(
					content)).asOpenRocket();
		}
	}
	
	/**
	 * This method sets the position information onto the component.  Rocksim splits the location/position
	 * information into two disparate data elements.  Both pieces of data are necessary to map into OpenRocket's
	 * position model.
	 *
	 * @param element     the element name
	 * @param attributes  the attributes
	 * @param content     the content of the element
	 * @param warnings        the warning set to store warnings in.
	 * @throws org.xml.sax.SAXException  not thrown
	 */
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		super.endHandler(element, attributes, content, warnings);
		setAxialMethod(position);
		setLocation(getComponent(), position, positionValue);
	}
	
	/**
	 * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not
	 * public in all components.
	 *
	 * @param position  the OpenRocket position
	 */
	protected abstract void setAxialMethod(AxialMethod position);
	
	/**
	 * Set the position of a component.
	 *
	 * @param component  the component
	 * @param position   the relative position
	 * @param location   the actual position value
	 */
	public static void setLocation(RocketComponent component, AxialMethod position, double location) {
		if (position.equals(AxialMethod.BOTTOM)) {
			component.setAxialOffset(-1d * location);
		}
		else {
			component.setAxialOffset(location);
		}
	}
	
}
