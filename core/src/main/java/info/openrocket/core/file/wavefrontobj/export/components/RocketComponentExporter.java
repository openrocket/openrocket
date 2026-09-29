package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.RocketComponent;

import java.util.List;

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
    protected final boolean exportAllInstances;
    protected final WarningSet warnings;
    private InstanceContext instanceContextOverride;

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
                                   T component, String groupName, ObjUtils.LevelOfDetail LOD, boolean exportAllInstances,
                                   WarningSet warnings) {
        this.obj = obj;
        this.config = config;
        this.component = component;
        this.groupName = groupName;
        this.LOD = LOD;
        this.transformer = transformer;
        this.exportAllInstances = exportAllInstances;
        this.warnings = warnings;
    }

    public abstract void addToObj();

    /**
     * Add only the specified physical component instance to the target mesh.
     * This is used by exporters that need one independently printable object per
     * instance while retaining the existing component mesh implementations.
     *
     * @param context instance transform to export
     */
    public final void addInstanceToObj(InstanceContext context) {
        this.instanceContextOverride = context;
        try {
            addToObj();
        } finally {
            this.instanceContextOverride = null;
        }
    }

    protected List<InstanceContext> getInstanceContexts() {
        if (instanceContextOverride != null) {
            return List.of(instanceContextOverride);
        }
        List<InstanceContext> contexts = config.getActiveInstances().getInstanceContexts(component);
        return (exportAllInstances || contexts.isEmpty()) ? contexts : contexts.subList(0, 1);
    }
}
