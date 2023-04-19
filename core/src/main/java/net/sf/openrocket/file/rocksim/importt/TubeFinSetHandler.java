package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.file.rocksim.RockSimFinishCode;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;

import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Rocksim import SAX handler for Tube Fin Sets.
 */
public class TubeFinSetHandler extends PositionDependentHandler<TubeFinSet> {

    /**
     * The OpenRocket TubeFinSet instance.
     */
    private final TubeFinSet tubeFin;

    /**
     * Constructor.
     *
     * @param c the parent
     * @param warnings  the warning set
     *
     * @throws IllegalArgumentException thrown if <code>c</code> is null
     */
    public TubeFinSetHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
    	super(context);
    	if (c == null) {
    		throw new IllegalArgumentException("The parent component of a tube fin may not be null.");
    	}
    	tubeFin = new TubeFinSet();
    	if (isCompatible(c, TubeFinSet.class, warnings)) {
    		c.addChild(tubeFin);
    	}
    }

    /**
     * Get the OR instance after the XML parsing is done.
     *
     * @return a TubeFinSet instance
     */
    @Override
    protected TubeFinSet getComponent() {
        return tubeFin;
    }

    /**
     * Get the type of material the tube fins are constructed from.
     *
     * @return Material.Type
     */
    @Override
    protected Material.Type getMaterialType() {
        return Material.Type.BULK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementHandler openElement(final String element, final HashMap<String, String> attributes, final WarningSet warnings) throws SAXException {
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
   			if (RockSimCommonConstants.OD.equals(element)) {
   				tubeFin.setOuterRadius(Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
   			}
   			if (RockSimCommonConstants.ID.equals(element)) {
                tubeFin.setInnerRadius(Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
   			}
   			if (RockSimCommonConstants.LEN.equals(element)) {
                tubeFin.setLength(Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
   			}
   			if (RockSimCommonConstants.MATERIAL.equals(element)) {
   				setMaterialName(content);
   			}
            if (RockSimCommonConstants.RADIAL_ANGLE.equals(element)) {
                tubeFin.setBaseRotation(Double.parseDouble(content));
            }
   			if (RockSimCommonConstants.TUBE_COUNT.equals(element)) {
                tubeFin.setFinCount(Integer.parseInt(content));
   			}
   			if (RockSimCommonConstants.FINISH_CODE.equals(element)) {
                tubeFin.setFinish(RockSimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
   			}
   		} catch (NumberFormatException nfe) {
   			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
   		}
   	}

}
