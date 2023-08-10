package net.sf.openrocket.file.wavefrontobj.export;

import de.javagl.obj.ObjWriter;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
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
import java.util.Map;
import java.util.Set;

/**
 * Exporter for rocket components to a Wavefront OBJ file.
 * <b>NOTE: </b> The coordinate system of the Wavefront OBJ file and OpenRocket is different.
 * An OBJ file has the y-axis pointing up, the z-axis pointing towards the viewer, and the x-axis pointing to the right (right-handed system).
 * OpenRocket uses a left-handed system with the y-axis pointing up, the z-axis pointing away from the viewer, and the
 * x-axis pointing to the right (in the side view). Its origin is also at the tip of the rocket, whereas for the OBJ it
 * would be the bottom of the rocket.
 *      => the following transformation applies from OBJ coordinate system to OpenRocket coordinate system:
 *              x = y
 *              y = rocketLength - x
 *              z = -z
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class OBJExporterFactory {
    private final List<RocketComponent> components;
    private final boolean exportChildren;
    private final boolean triangulate;
    private final boolean removeOffset;
    private final ObjUtils.LevelOfDetail LOD;          // Level of detailed used for the export
    private final String filePath;
    private final CoordTransform transformer;

    // The different exporters for each component
    private static final Map<Class<? extends RocketComponent>, ExporterFactory<?>> EXPORTER_MAP = Map.of(
            BodyTube.class, (ExporterFactory<BodyTube>) BodyTubeExporter::new,
            Transition.class, (ExporterFactory<Transition>) TransitionExporter::new,
            LaunchLug.class, (ExporterFactory<LaunchLug>) LaunchLugExporter::new,
            TubeFinSet.class, (ExporterFactory<TubeFinSet>) TubeFinSetExporter::new,
            FinSet.class, (ExporterFactory<FinSet>) FinSetExporter::new,
            ThicknessRingComponent.class, (ExporterFactory<ThicknessRingComponent>) ThicknessRingComponentExporter::new,
            RadiusRingComponent.class, (ExporterFactory<RadiusRingComponent>) RadiusRingComponentExporter::new,
            MassObject.class, (ExporterFactory<MassObject>) MassObjectExporter::new,
            RailButton.class, (ExporterFactory<RailButton>) RailButtonExporter::new
    );

    /**
     * Exports a list of rocket components to a Wavefront OBJ file.
     * <b>NOTE: </b> you must call {@link #doExport()} to actually perform the export.
     * @param components List of components to export
     * @param exportChildren If true, export all children of the components as well
     * @param triangulate If true, triangulate all faces
     * @param removeOffset If true, remove the offset of the object so it is centered at the origin (but the bottom of the object is at y=0)
     * @param LOD Level of detail to use for the export (e.g. '80')
     * @param transformer Coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param filePath Path to the file to export to
     */
    public OBJExporterFactory(List<RocketComponent> components, boolean exportChildren, boolean triangulate,
                              boolean removeOffset, ObjUtils.LevelOfDetail LOD, CoordTransform transformer, String filePath) {
        this.components = components;
        this.exportChildren = exportChildren;
        this.triangulate = triangulate;
        this.removeOffset = removeOffset;
        this.LOD = LOD;
        this.transformer = transformer;
        this.filePath = filePath;
    }

    /**
     * Wavefront OBJ exporter.
     * @param components List of components to export
     * @param exportChildren If true, export all children of the components as well
     * @param triangulate If true, triangulate all faces
     * @param removeOffset If true, remove the offset of the object so it is centered at the origin (but the bottom of the object is at y=0)
     * @param transformer Coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param filePath Path to the file to export to
     */
    public OBJExporterFactory(List<RocketComponent> components, boolean exportChildren, boolean triangulate,
                              boolean removeOffset, CoordTransform transformer, String filePath) {
        this(components, exportChildren, triangulate, removeOffset, ObjUtils.LevelOfDetail.NORMAL, transformer, filePath);
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
            if (component instanceof ComponentAssembly) {
                continue;
            }

            String groupName = component.getName() + "_" + idx;
            handleComponent(obj, component, groupName, this.LOD, this.transformer);

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

    @SuppressWarnings("unchecked") // This is safe because of the structure we set up.
    private <T extends RocketComponent> void handleComponent(DefaultObj obj, T component, String groupName,
            ObjUtils.LevelOfDetail LOD, CoordTransform transformer) {
        ExporterFactory<T> factory = null;
        Class<?> currentClass = component.getClass();

        // Need to iterate over superclasses to find the correct exporter (otherwise e.g. a NoseCone would not work for the TransitionExporter)
        while (RocketComponent.class.isAssignableFrom(currentClass) && factory == null) {
            factory = (ExporterFactory<T>) EXPORTER_MAP.get(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        if (factory == null) {
            throw new IllegalArgumentException("Unsupported component type: " + component.getClass().getName());
        }

        final RocketComponentExporter<T> exporter = factory.create(obj, component, groupName, LOD, transformer);
        exporter.addToObj();
    }

    interface ExporterFactory<T extends RocketComponent> {
        RocketComponentExporter<T> create(DefaultObj obj, T component, String groupName,
                                          ObjUtils.LevelOfDetail LOD, CoordTransform transformer);
    }
}
