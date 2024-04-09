package info.openrocket.core.file.wavefrontobj.export;

import de.javagl.obj.ObjWriter;
import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultMtl;
import info.openrocket.core.file.wavefrontobj.DefaultMtlWriter;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.TriangulationHelper;
import info.openrocket.core.file.wavefrontobj.export.components.BodyTubeExporter;
import info.openrocket.core.file.wavefrontobj.export.components.FinSetExporter;
import info.openrocket.core.file.wavefrontobj.export.components.LaunchLugExporter;
import info.openrocket.core.file.wavefrontobj.export.components.MassObjectExporter;
import info.openrocket.core.file.wavefrontobj.export.components.MotorExporter;
import info.openrocket.core.file.wavefrontobj.export.components.RailButtonExporter;
import info.openrocket.core.file.wavefrontobj.export.components.RocketComponentExporter;
import info.openrocket.core.file.wavefrontobj.export.components.RingComponentExporter;
import info.openrocket.core.file.wavefrontobj.export.components.TransitionExporter;
import info.openrocket.core.file.wavefrontobj.export.components.TubeFinSetExporter;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final File file;
    private final WarningSet warnings;

    private static final Logger log = LoggerFactory.getLogger(OBJExporterFactory.class);

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
     * @param file The file to export the OBJ to
     */
    public OBJExporterFactory(List<RocketComponent> components, FlightConfiguration configuration, File file,
                              OBJExportOptions options, WarningSet warnings) {
        this.components = components;
        this.configuration = configuration;
        this.file = file;
        this.options = options;
        this.warnings = warnings;
    }

    /**
     * Performs the actual exporting.
     */
    public void doExport() {
        DefaultObj obj = new DefaultObj();
        Map<String, DefaultObj> objFileMap;
        Map<DefaultObj, List<DefaultMtl>> materials = new HashMap<>();
        materials.put(obj, new ArrayList<>());
        boolean exportAsSeparateFiles = this.options.isExportAsSeparateFiles();

        if (exportAsSeparateFiles) {
            objFileMap = new HashMap<>();
        } else {
            objFileMap = Map.of(this.file.getAbsolutePath(), obj);
        }

        // Get all the components to export
        Set<RocketComponent> componentsToExport = new HashSet<>(this.components);
        if (this.options.isExportChildren()) {
            for (RocketComponent component : this.components) {
                componentsToExport.addAll(component.getAllChildren());
            }
        }

        // Sort the components according to how they are ordered in the rocket (component tree)
        Set<RocketComponent> sortedComponents = sortComponents(componentsToExport);

        int idx = 1;
        for (RocketComponent component : sortedComponents) {
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
                materials.put(obj, new ArrayList<>());
            }

            // Component exporting
            String groupName = idx + "_" + component.getName();
            groupName = sanitizeGroupName(groupName);
            handleComponent(obj, this.configuration, this.options.getTransformer(), component, groupName,
                    materials.get(obj), this.options.getLOD(), options, warnings);

            // If separate export, add this object to the map of objects to export
            if (exportAsSeparateFiles) {
                String path = FileUtils.removeExtension(this.file.getAbsolutePath()) + "_" + groupName + ".obj";
                objFileMap.put(path, obj);
            }

            idx++;
        }

        // Apply export options and write the OBJ files
        for (Map.Entry<String, DefaultObj> entry : objFileMap.entrySet()) {
            String filePath = entry.getKey();
            obj = entry.getValue();

            // Triangulate mesh
            if (this.options.isTriangulate()) {
                ObjUtils.TriangulationMethod triangulationMethod = this.options.getTriangulationMethod();
                if (triangulationMethod == ObjUtils.TriangulationMethod.DELAUNAY) {
                    obj = TriangulationHelper.constrainedDelaunayTriangulate(obj);
                } else if (triangulationMethod == ObjUtils.TriangulationMethod.SIMPLE) {
                    obj = TriangulationHelper.simpleTriangulate(obj);
                } else {
                    throw new IllegalArgumentException("Unsupported triangulation method: " + triangulationMethod);
                }
            }

            // Remove position offset
            if (this.options.isRemoveOffset()) {
                // Because of some rotation and translation operations when creating the meshes, the bounds can be inaccurate.
                // Therefore, we will recalculate them to be sure.
                // Is a bit computationally expensive, but it's the only way to be sure...
                obj.recalculateAllVertexBounds();

                ObjUtils.removeVertexOffset(obj, this.options.getTransformer());
            }

            // Perform scaling
            if (Float.compare(options.getScaling(), 1) != 0) {
                ObjUtils.scaleVertices(obj, options.getScaling());
            }

            // Export materials
            if (options.isExportAppearance()) {
                String mtlFilePath = FileUtils.removeExtension(filePath) + ".mtl";
                List<DefaultMtl> mtls = materials.get(obj);
                if (mtls != null) {
                    try (OutputStream mtlOutputStream = new FileOutputStream(mtlFilePath, false)) {
                        DefaultMtlWriter.write(mtls, mtlOutputStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    log.debug("No materials to export for {}", filePath);
                }
                obj.setMtlFileNames(List.of(mtlFilePath));
            }

            // Write the OBJ file
            writeObj(obj, filePath);

            // Reset the material
            if (options.isExportAppearance()) {
                obj.resetToBlankMaterial();
            }
        }
    }

    private static void writeObj(DefaultObj obj, String filePath) {
        try (OutputStream objOutputStream = new FileOutputStream(filePath, false)) {
            ObjWriter.write(obj, objOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked") // This is safe because of the structure we set up.
    private <T extends RocketComponent> void handleComponent(DefaultObj obj, FlightConfiguration config, CoordTransform transformer,
                                                             T component, String groupName, List<DefaultMtl> materials,
                                                             ObjUtils.LevelOfDetail LOD, OBJExportOptions options,
                                                             WarningSet warnings) {
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

        // Export material
        if (options.isExportAppearance()) {
            String materialName = "mat_" + groupName;

            // Get the component appearance
            Appearance appearance = component.getAppearance();
            if (appearance == null) {
                appearance = DefaultAppearance.getDefaultAppearance(component);
            }

            AppearanceExporter appearanceExporter = new AppearanceExporter(obj, appearance, file, options, materialName, materials);
            appearanceExporter.doExport();
        }

        // Export component
        final RocketComponentExporter<T> exporter = factory.create(obj, config, transformer, component, groupName, LOD, warnings);
        exporter.addToObj();

        // Export motor
        if (component instanceof MotorMount && options.isExportMotors()) {
            // Get the motor
            MotorConfiguration motoConfig = ((MotorMount) component).getMotorConfig(config.getId());
            Motor motor = motoConfig.getMotor();

            // Export the motor appearance
            if (options.isExportAppearance() && motor != null) {
                String materialName = "mat_" + groupName + "_" + motor.getMotorName();
                Appearance appearance = DefaultAppearance.getDefaultAppearance(motor);
                AppearanceExporter appearanceExporter = new AppearanceExporter(obj, appearance, file, options, materialName, materials);
                appearanceExporter.doExport();
            }

            // Export the motor geometry
            MotorExporter motorExporter = new MotorExporter(obj, config, transformer, component, groupName, LOD, warnings);
            motorExporter.addToObj();
        }
    }

    /**
     * Sort a set of components according to how they are ordered in the rocket (component tree).
     * @param components components to sort
     * @return sorted components
     */
    private Set<RocketComponent> sortComponents(Set<RocketComponent> components) {
        Set<RocketComponent> sortedComponents = new LinkedHashSet<>();
        addChildComponentToList(this.configuration.getRocket(), components, sortedComponents);

        return sortedComponents;
    }

    /**
     * Sanitize the group name by replacing illegal characters with underscores.
     * @param groupName the group name to sanitize
     * @return the sanitized group name
     */
    private static String sanitizeGroupName(String groupName) {
        Character c = FileUtils.getIllegalFilenameChar(groupName);
        while (c != null) {
            groupName = groupName.replace(c, '_');
            c = FileUtils.getIllegalFilenameChar(groupName);
        }
        return groupName;
    }

    private void addChildComponentToList(RocketComponent parent, Set<RocketComponent> components, Set<RocketComponent> sortedComponents) {
        for (RocketComponent child : parent.getChildren()) {
            if (components.contains(child)) {
                sortedComponents.add(child);
            }
            addChildComponentToList(child, components, sortedComponents);
        }
    }

    interface ExporterFactory<T extends RocketComponent> {
        RocketComponentExporter<T> create(DefaultObj obj, FlightConfiguration config, CoordTransform transformer,
                                          T component, String groupName, ObjUtils.LevelOfDetail LOD, WarningSet warnings);
    }
}
