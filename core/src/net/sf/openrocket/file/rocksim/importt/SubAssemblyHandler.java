
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * This class handles Rocksim 'SubAssembly' elements.  They are similar to 'AttachedParts' (which is why this class is subclassed from
 * AttachedPartsHandler) with some key differences.  In Rocksim, AttachedParts elements can contain SubAssembly elements, which can in turn
 * contain AttachedParts elements.  To represent them in OR, SubAssembly elements are treated as children of the stage - much like a nose cone or
 * external body tube.
 */
public class SubAssemblyHandler extends AttachedPartsHandler {

    public SubAssemblyHandler(final DocumentLoadingContext context, final RocketComponent c)
            throws IllegalArgumentException {
        //A bit of a risk here, but assign the subassembly to the stage, not to the component.  This is because typically the
        //first component within the subassembly will be an external component.
        super(context, c.getStage());
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
        // We're already part of a subassembly, and then there are attached parts! Can't use an attached parts handler in this situation because
        // the AttachedPartsHandler assumes that all body tubes are inner body tubes (Rocksim makes no distinction).  OR does not allow things
        // like fins to be attached to inner body tubes - which is often what these Rocksim subassemblies contain.  So just return this instance
        // which treats body tubes as external body tubes.
        if (RocksimCommonConstants.ATTACHED_PARTS.equals(element)) {
            return this;
        }
        // The key override of this class - treat body tubes as external body tubes.
        else if (RocksimCommonConstants.BODY_TUBE.equals(element)) {
            return new BodyTubeHandler(getContext(), getComponent(), warnings);
        }
        return super.openElement(element, attributes, warnings);
    }

}
