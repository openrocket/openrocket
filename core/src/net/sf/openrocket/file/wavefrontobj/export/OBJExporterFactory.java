package net.sf.openrocket.file.wavefrontobj.export;

import de.javagl.obj.ObjWriter;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.components.BodyTubeExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.FinSetExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.LaunchLugExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.MassObjectExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.MotorExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RailButtonExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RocketComponentExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.RingComponentExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.TransitionExporter;
import net.sf.openrocket.file.wavefrontobj.export.components.TubeFinSetExporter;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
    private final FlightConfiguration configuration;
    private final OBJExportOptions options;
    private final String filePath;

    // The different exporters for each component
    private static final Map<Class<? extends RocketComponent>, ExporterFactory<?>> EXPORTER_MAP = Map.of(
            BodyTube.class, (ExporterFactory<BodyTube>) BodyTubeExporter::new,
            Transition.class, (ExporterFactory<Transition>) TransitionExporter::new,
            LaunchLug.class, (ExporterFactory<LaunchLug>) LaunchLugExporter::new,
            TubeFinSet.class, (ExporterFactory<TubeFinSet>) TubeFinSetExporter::new,
            FinSet.class, (ExporterFactory<FinSet>) FinSetExporter::new,
            RingComponent.class, (ExporterFactory<RingComponent>) RingComponentExporter::new,
            MassObject.class, (ExporterFactory<MassObject>) MassObjectExporter::new,
            RailButton.class, (ExporterFactory<RailButton>) RailButtonExporter::new
    );

    /**
     * Exports a list of rocket components to a Wavefront OBJ file.
     * <b>NOTE: </b> you must call {@link #doExport()} to actually perform the export.
     * @param components List of components to export
     * @param configuration Flight configuration to use for the export
     * @param options Options to use for the export
     * @param filePath Path to the file to export to
     */
    public OBJExporterFactory(List<RocketComponent> components, FlightConfiguration configuration, String filePath,
                              OBJExportOptions options) {
        this.components = components;
        this.configuration = configuration;
        this.filePath = filePath;
        this.options = options;
    }

    /**
     * Performs the actual exporting.
     */
    public void doExport() {
        DefaultObj obj = new DefaultObj();
        boolean exportAsSeparateFiles = this.options.isExportAsSeparateFiles();

        // Get all the components to export
        Set<RocketComponent> componentsToExport = new HashSet<>(this.components);
        if (this.options.isExportChildren()) {
            for (RocketComponent component : this.components) {
                componentsToExport.addAll(component.getAllChildren());
            }
        }

        int idx = 1;
        for (RocketComponent component : componentsToExport) {
            if (component instanceof ComponentAssembly) {
                continue;
            }

            // Don't export inactive components
            if (!this.configuration.isComponentActive(component)) {
                continue;
            }

            // Get the instance transforms
            InstanceMap map = configuration.getActiveInstances();
            ArrayList<InstanceContext> contexts = map.get(component);
            contexts.get(0).transform.getXrotation();

            // If separate export, create a new OBJ for each component
            if (exportAsSeparateFiles) {
                obj = new DefaultObj();
            }

            // Component exporting
            String groupName = idx + "_" + component.getName();
            handleComponent(obj, this.configuration, this.options.getTransformer(), component, groupName, this.options.getLOD());

            // If separate export, already need to write the OBJ here
            if (exportAsSeparateFiles) {
                String path = FileUtils.removeExtension(this.filePath) + "_" + groupName + ".obj";
                writeObj(obj, path);
            }

            idx++;
        }

        if (this.options.isTriangulate()) {
            obj = de.javagl.obj.ObjUtils.triangulate(obj, new DefaultObj());
        }

        if (this.options.isRemoveOffset()) {
            // Because of some rotation and translation operations when creating the meshes, the bounds can be inaccurate.
            // Therefore, we will recalculate them to be sure.
            // Is a bit computationally expensive, but it's the only way to be sure...
            obj.recalculateAllVertexBounds();

            ObjUtils.removeVertexOffset(obj, this.options.getTransformer());
        }

        if (!exportAsSeparateFiles) {
            writeObj(obj, this.filePath);
        }
    }

    private static void writeObj(DefaultObj obj, String filePath) {
        try (OutputStream objOutputStream = new FileOutputStream(filePath)) {
            ObjWriter.write(obj, objOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked") // This is safe because of the structure we set up.
    private <T extends RocketComponent> void handleComponent(DefaultObj obj, FlightConfiguration config, CoordTransform transformer,
                                                             T component, String groupName, ObjUtils.LevelOfDetail LOD) {
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

        // Export component
        final RocketComponentExporter<T> exporter = factory.create(obj, config, transformer, component, groupName, LOD);
        exporter.addToObj();

        // Export motor
        if (component instanceof MotorMount) {
            MotorExporter motorExporter = new MotorExporter(obj, config, transformer, component, groupName, LOD);
            motorExporter.addToObj();
        }
    }

    interface ExporterFactory<T extends RocketComponent> {
        RocketComponentExporter<T> create(DefaultObj obj, FlightConfiguration config, CoordTransform transformer,
                                          T component, String groupName, ObjUtils.LevelOfDetail LOD);
    }
}
