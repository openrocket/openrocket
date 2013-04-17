/*
 * MassObjectHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimDensityType;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.Coaxial;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;

import org.xml.sax.SAXException;

/**
 * A SAX handler for Rocksim's MassObject XML type.
 */
class MassObjectHandler extends PositionDependentHandler<MassObject> {
	
	/**
	 * The Rocksim Mass length fudge factor.  Rocksim completely exaggerates the length of a mass object to the point
	 * that it looks ridiculous in OpenRocket.  This fudge factor is here merely to get the typical mass object to
	 * render in the OpenRocket UI with it's bounds mostly inside it's parent.  The odd thing about it is that Rocksim
	 * does not expose the length of a mass object in the UI and actually treats mass objects as point objects - not 3
	 * or even 2 dimensional.
	 */
	public static final int MASS_LEN_FUDGE_FACTOR = 100;
	
	/**
	 * The OpenRocket MassComponent - counterpart to the RS MassObject.
	 */
	private final MassComponent mass;
	
	/**
	 * Reference to answer for getComponent().
	 */
	private MassObject current;
	
	/**
	 * Parent.
	 */
	private RocketComponent parent;
	
	/**
	 * 0 == General, 1 == Shock Cord
	 */
	private int typeCode = 0;
	
	/**
	 * Constructor. l
	 *
	 * @param c        the parent component
	 * @param warnings the warning set
	 *
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public MassObjectHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a mass component may not be null.");
		}
		mass = new MassComponent();
		current = mass;
		parent = c;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		try {
			if (RocksimCommonConstants.LEN.equals(element)) {
				mass.setLength(Double.parseDouble(content) / (RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RocksimCommonConstants.KNOWN_MASS.equals(element)) {
				mass.setComponentMass(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
			if (RocksimCommonConstants.KNOWN_CG.equals(element)) {
				//Setting the CG of the Mass Object to 0 is important because of the different ways that Rocksim and
				//OpenRocket treat mass objects.  Rocksim treats them as points (even though the data file contains a
				//length) and because Rocksim sets the CG of the mass object to really be relative to the front of
				//the parent.  But that value is already assumed in the position and position value for the component.
				//Thus it needs to be set to 0 to say that the mass object's CG is at the point of the mass object.
				super.setCG(0);
			}
			if (RocksimCommonConstants.TYPE_CODE.equals(element)) {
				typeCode = Integer.parseInt(content);
			}
			if (RocksimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws
			SAXException {
		if (inferAsShockCord(typeCode, warnings)) { //Shock Cord
			mapMassObjectAsShockCord(element, attributes, content, warnings);
		}
		else { // typeCode == 0   General Mass Object
			if (isCompatible(parent, MassComponent.class, warnings)) {
				parent.addChild(mass);
			}
			super.endHandler(element, attributes, content, warnings);
		}
	}
	
	/**
	 * Rocksim does not have a separate entity for Shock Cords.  It has to be inferred.  Sometimes the typeCode
	 * indicates it's a shock cord, but most times it does not.  This is due to bugs in the Rocksim Component and
	 * Material databases.  Try to infer a shock cord based on it's length and it's material type.  It's somewhat
	 * arbitrary, but if the mass object's length is more than twice the length of it's parent component and it's a LINE
	 * material, then assume a shock cord.
	 *
	 * @param theTypeCode the code from the RKT XML file
	 *
	 * @return true if we think it's a shock cord
	 */
	private boolean inferAsShockCord(int theTypeCode, WarningSet warnings) {
		return (theTypeCode == 1 || (mass.getLength() >= 2 * parent.getLength() && RocksimDensityType.ROCKSIM_LINE
				.equals(getDensityType()))) && isCompatible(parent, ShockCord.class, warnings, true);
	}
	
	/**
	 * If it appears that the mass object is a shock cord, then create an OR shock cord instance.
	 *
	 * @param element    the element name
	 * @param attributes the attributes
	 * @param content    the content of the element
	 * @param warnings   the warning set to store warnings in.
	 *
	 * @throws org.xml.sax.SAXException not thrown
	 */
	private void mapMassObjectAsShockCord(final String element, final HashMap<String, String> attributes,
			final String content, final WarningSet warnings) throws SAXException {
		ShockCord cord = new ShockCord();
		current = cord;
		if (isCompatible(parent, ShockCord.class, warnings)) {
			parent.addChild(cord);
		}
		super.endHandler(element, attributes, content, warnings);
		cord.setName(mass.getName());
		
		setOverride(cord, mass.isMassOverridden(), mass.getOverrideMass(), mass.getOverrideCGX());
		
		cord.setRadialDirection(mass.getRadialDirection());
		cord.setRadialPosition(mass.getRadialPosition());
		cord.setRadius(mass.getRadius());
		
		//Rocksim does not distinguish between total length of the cord and the packed length.  Fudge the
		//packed length and set the real length.
		cord.setCordLength(mass.getLength());
		cord.setLength(cord.getCordLength() / MASS_LEN_FUDGE_FACTOR);
		if (parent instanceof Coaxial) {
			Coaxial parentCoaxial = (Coaxial) parent;
			cord.setRadius(parentCoaxial.getInnerRadius());
		}
	}
	
	/**
	 * Get the component this handler is working upon.  This changes depending upon the type of mass object.
	 *
	 * @return a component
	 */
	@Override
	public MassObject getComponent() {
		return current;
	}
	
	/**
	 * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not
	 * public in all components.
	 *
	 * @param position the OpenRocket position
	 */
	public void setRelativePosition(RocketComponent.Position position) {
		current.setRelativePosition(position);
	}
	
	/**
	 * Get the required type of material for this component.  Does not apply to MassComponents, but does apply to Shock
	 * Cords.
	 *
	 * @return LINE
	 */
	@Override
	public Material.Type getMaterialType() {
		return Material.Type.LINE;
	}
	
}
