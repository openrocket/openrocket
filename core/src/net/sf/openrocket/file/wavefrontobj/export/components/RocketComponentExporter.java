package net.sf.openrocket.file.wavefrontobj.export.components;

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
public abstract class RocketComponentExporter {
    protected final DefaultObj obj;
    protected final RocketComponent component;
    protected final String groupName;
    protected final ObjUtils.LevelOfDetail LOD;

    /**
     * Wavefront OBJ exporter for a rocket component.
     * @param obj The OBJ to export to
     * @param component The component to export
     * @param groupName The name of the group to export to
     * @param LOD Level of detail to use for the export (e.g. '80')
     */
    public RocketComponentExporter(DefaultObj obj, RocketComponent component, String groupName, ObjUtils.LevelOfDetail LOD) {
        this.obj = obj;
        this.component = component;
        this.groupName = groupName;
        this.LOD = LOD;
    }

    public abstract void addToObj();
}
