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
 * An abstract base class that handles axialMethod dependencies for all lower level components that
 * are axialMethod aware.
 *
 * @param <C>   the specific axialMethod dependent RocketComponent subtype for which the concrete handler can create
 */
public abstract class PositionDependentHandler<C extends RocketComponent> extends BaseHandler<C> {
	
	/** Temporary axialMethod value. */
	private Double positionValue = 0d;
	
	/** Temporary axialMethod. */
	private AxialMethod axialMethod = AxialMethod.TOP;
	
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
			axialMethod = RocksimLocationMode.fromCode(Integer.parseInt(
					content)).asOpenRocket();
		}
	}
	
	/**
	 * This method sets the axialMethod information onto the component.  Rocksim splits the location/axialMethod
	 * information into two disparate data elements.  Both pieces of data are necessary to map into OpenRocket's
	 * axialMethod model.
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
		setLocation();
	}

	/**
	 * Set the axialMethod of a component.
	 */
	protected void setLocation() {
		getComponent().setAxialMethod(axialMethod);
		if (axialMethod.equals(AxialMethod.BOTTOM)) {
			getComponent().setAxialOffset(-1d * positionValue);
		} else {
			getComponent().setAxialOffset(positionValue);
		}
	}
	
}
