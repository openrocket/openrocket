package net.sf.openrocket.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * Base class for a rocket component Wavefront OBJ exporter.
 * This class generates OBJ data for a rocket component and adds it to the given OBJ.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class RocketComponentExporter<T extends RocketComponent> {
    protected final DefaultObj obj;
    protected final FlightConfiguration config;
    protected final T component;
    protected final String groupName;
    protected final ObjUtils.LevelOfDetail LOD;
    protected final CoordTransform transformer;
    protected final WarningSet warnings;

    /**
     * Wavefront OBJ exporter for a rocket component.
     * @param obj The OBJ to export to
     * @param config The flight configuration to use for the export
     * @param transformer Coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param component The component to export
     * @param groupName The name of the group to export to
     * @param LOD Level of detail to use for the export (e.g. '80')
     */
    public RocketComponentExporter(@NotNull DefaultObj obj, @NotNull FlightConfiguration config, @NotNull CoordTransform transformer,
                                   T component, String groupName, ObjUtils.LevelOfDetail LOD,
                                   WarningSet warnings) {
        this.obj = obj;
        this.config = config;
        this.component = component;
        this.groupName = groupName;
        this.LOD = LOD;
        this.transformer = transformer;
        this.warnings = warnings;
    }

    public abstract void addToObj();
}
