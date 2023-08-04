package net.sf.openrocket.file.wavefrontobj.export;

import de.javagl.obj.ObjWriter;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.components.BodyTubeExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.FinSetExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.LaunchLugExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.MassObjectExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RadiusRingComponentExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RailButtonExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RocketComponentExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.ThicknessRingComponentExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.TransitionExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.TubeFinSetExporter;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeFinSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OBJExporterFactory {

    private final List<RocketComponent> components;
    private final boolean exportChildren;
    private final boolean triangulate;
    private final boolean removeOffset;
    private final ObjUtils.LevelOfDetail LOD;          // Level of detailed used for the export
    private final String filePath;

    /**
     * Exports a list of rocket components to a Wavefront OBJ file.
     * <b>NOTE: </b> you must call {@link #doExport()} to actually perform the export.
     * @param components List of components to export
     * @param exportChildren If true, export all children of the components as well
     * @param triangulate If true, triangulate all faces
     * @param removeOffset If true, remove the offset of the object so it is centered at the origin (but the bottom of the object is at y=0)
     * @param LOD Level of detail to use for the export (e.g. '80')
     * @param filePath Path to the file to export to
     */
    public OBJExporterFactory(List<RocketComponent> components, boolean exportChildren, boolean triangulate,
                              boolean removeOffset, ObjUtils.LevelOfDetail LOD, String filePath) {
        this.components = components;
        this.exportChildren = exportChildren;
        this.triangulate = triangulate;
        this.removeOffset = removeOffset;
        this.LOD = LOD;
        this.filePath = filePath;
    }

    /**
     * Wavefront OBJ exporter.
     * @param components List of components to export
     * @param exportChildren If true, export all children of the components as well
     * @param triangulate If true, triangulate all faces
     * @param removeOffset If true, remove the offset of the object so it is centered at the origin (but the bottom of the object is at y=0)
     * @param filePath Path to the file to export to
     */
    public OBJExporterFactory(List<RocketComponent> components, boolean exportChildren, boolean triangulate,
                              boolean removeOffset, String filePath) {
        this(components, exportChildren, triangulate, removeOffset, ObjUtils.LevelOfDetail.NORMAL, filePath);
    }

    /**
     * Performs the actual exporting.
     */
    public void doExport() {
        DefaultObj obj = new DefaultObj();

        Set<RocketComponent> componentsToExport = new HashSet<>(this.components);
        if (this.exportChildren) {
            for (RocketComponent component : this.components) {
                componentsToExport.addAll(component.getAllChildren());
            }
        }

        int idx = 1;
        for (RocketComponent component : componentsToExport) {
            final RocketComponentExporter exporter;

            String groupName = component.getName() + "_" + idx;       // Add index to make the name unique

            if (component instanceof BodyTube) {
                exporter = new BodyTubeExporter(obj, (BodyTube) component, groupName, this.LOD);
            } else if (component instanceof Transition) {
                exporter = new TransitionExporter(obj, (Transition) component, groupName, this.LOD);
            }else if (component instanceof LaunchLug) {
                exporter = new LaunchLugExporter(obj, (LaunchLug) component, groupName, this.LOD);
            } else if (component instanceof TubeFinSet) {
                exporter = new TubeFinSetExporter(obj, (TubeFinSet) component, groupName, this.LOD);
            } else if (component instanceof FinSet) {
                exporter = new FinSetExporter(obj, (FinSet) component, groupName, this.LOD);
            } else if (component instanceof ThicknessRingComponent) {
                exporter = new ThicknessRingComponentExporter(obj, (ThicknessRingComponent) component, groupName, this.LOD);
            } else if (component instanceof RadiusRingComponent) {
                exporter = new RadiusRingComponentExporter(obj, (RadiusRingComponent) component, groupName, this.LOD);
            } else if (component instanceof MassObject) {
                exporter = new MassObjectExporter(obj, (MassObject) component, groupName, this.LOD);
            } else if (component instanceof RailButton) {
                exporter = new RailButtonExporter(obj, (RailButton) component, groupName, this.LOD);
            } else if (component instanceof ComponentAssembly) {
                // Do nothing, component assembly instances are handled by the individual rocket component exporters
                // by using getComponentLocations()
                continue;
            } else {
                throw new IllegalArgumentException("Unknown component type");
            }

            exporter.addToObj();
            idx++;
        }

        if (this.triangulate) {
            obj = de.javagl.obj.ObjUtils.triangulate(obj, new DefaultObj());
        }

        if (this.removeOffset) {
            // Because of some rotation and translation operations when creating the meshes, the bounds can be inaccurate.
            // Therefore, we will recalculate them to be sure.
            // Is a bit computationally expensive, but it's the only way to be sure...
            obj.recalculateAllVertexBounds();

            ObjUtils.removeVertexOffset(obj);
        }

        try (OutputStream objOutputStream = new FileOutputStream(this.filePath)) {
            ObjWriter.write(obj, objOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
