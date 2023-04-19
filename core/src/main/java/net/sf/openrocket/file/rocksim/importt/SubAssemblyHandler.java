
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * This class handles RockSim 'SubAssembly' elements.  They are similar to 'AttachedParts' (which is why this class is subclassed from
 * AttachedPartsHandler) with some key differences.  In RockSim, AttachedParts elements can contain SubAssembly elements, which can in turn
 * contain AttachedParts elements.  To represent them in OR, SubAssembly elements are treated as children of the stage - much like a nose cone or
 * external body tube.
 */
public class SubAssemblyHandler extends AttachedPartsHandler {

    /**
     * Constructor
     * @param c the parent component
     * @throws IllegalArgumentException
     */
    public SubAssemblyHandler(final DocumentLoadingContext context, final RocketComponent c)
            throws IllegalArgumentException {
        super(context, c);
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        // We're already part of a subassembly, and then there are attached parts! Can't use an attached parts handler in this situation because
        // the AttachedPartsHandler assumes that all body tubes are inner body tubes (Rocksim makes no distinction).  OR does not allow things
        // like fins to be attached to inner body tubes - which is often what these Rocksim subassemblies contain.  So just return this instance
        // which treats body tubes as external body tubes.
        if (RockSimCommonConstants.ATTACHED_PARTS.equals(element)) {
            return this;
        }
        // The key override of this class - treat body tubes, transitions, and nose cones as external components.
        // note: this will only work if the parent component is a stage.
        else if (RockSimCommonConstants.BODY_TUBE.equals(element)) {
            return new BodyTubeHandler(getContext(), getComponent(), warnings);
        } else if (RockSimCommonConstants.TRANSITION.equals(element)) {
            return new TransitionHandler(getContext(), getComponent(), warnings);
        } else if (RockSimCommonConstants.NOSE_CONE.equals(element)) {
            return new NoseConeHandler(getContext(), getComponent(), warnings);
        }
        return super.openElement(element, attributes, warnings);
    }

}
