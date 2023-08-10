package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.OBJExporterFactory;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * Base class for a rocket component Wavefront OBJ exporter.
 * This class generates OBJ data for a rocket component and adds it to the given OBJ.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class RocketComponentExporter<T extends RocketComponent> {
    protected final DefaultObj obj;
    protected final T component;
    protected final String groupName;
    protected final ObjUtils.LevelOfDetail LOD;
    protected final CoordTransform transformer;

    /**
     * Wavefront OBJ exporter for a rocket component.
     * @param obj The OBJ to export to
     * @param component The component to export
     * @param groupName The name of the group to export to
     * @param LOD Level of detail to use for the export (e.g. '80')
     * @param transformer Coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     */
    public RocketComponentExporter(DefaultObj obj, T component, String groupName,
                                   ObjUtils.LevelOfDetail LOD, CoordTransform transformer) {
        this.obj = obj;
        this.component = component;
        this.groupName = groupName;
        this.LOD = LOD;
        this.transformer = transformer;
    }

    public abstract void addToObj();
}
