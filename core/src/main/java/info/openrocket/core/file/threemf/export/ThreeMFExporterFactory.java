package info.openrocket.core.file.threemf.export;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ObjFace;
import info.openrocket.core.file.wavefrontobj.Axis;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.TriangulationHelper;
import info.openrocket.core.file.wavefrontobj.export.OBJExporterFactory;
import info.openrocket.core.file.wavefrontobj.export.components.RocketComponentExporter;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.RocketComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Exports active component instances as independently printable objects in a generic 3MF package.
 */
public class ThreeMFExporterFactory {
    private static final double MILLIMETERS_PER_METER = 1000.0;

    private final List<RocketComponent> components;
    private final FlightConfiguration configuration;
    private final File file;
    private final ThreeMFExportOptions options;
    private final WarningSet warnings;

    public ThreeMFExporterFactory(List<RocketComponent> components, FlightConfiguration configuration, File file,
                                  ThreeMFExportOptions options, WarningSet warnings) {
        this.components = List.copyOf(components);
        this.configuration = configuration;
        this.file = file;
        this.options = new ThreeMFExportOptions(options);
        this.warnings = warnings;
    }

    /**
     * Perform the export using a temporary sibling file and replace the target only after success.
     *
     * @return true when at least one valid part was written
     * @throws IOException if the package cannot be written or installed at the target path
     */
    public boolean doExport() throws IOException {
        if (!options.isValid()) {
            throw new IllegalArgumentException("Invalid 3MF export options");
        }

        List<PrintablePart> parts = createPrintableParts();
        if (parts.isEmpty()) {
            warnings.add("No valid printable component meshes were found; no 3MF file was created.");
            return false;
        }

        List<PrintablePart> packedParts = BuildPlatePacker.pack(parts, options, warnings);
        Path target = file.toPath().toAbsolutePath();
        Path directory = target.getParent();
        if (directory == null) {
            directory = Path.of(".").toAbsolutePath();
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, ".openrocket-3mf-", ".tmp");
        try {
            ThreeMFPackageWriter.write(temporary, packedParts);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return true;
    }

    private List<PrintablePart> createPrintableParts() {
        List<PrintablePart> parts = new ArrayList<>();
        for (RocketComponent component : getSortedComponents()) {
            if (component instanceof ComponentAssembly || !configuration.isComponentActive(component)) {
                continue;
            }

            List<InstanceContext> contexts = configuration.getActiveInstances().getInstanceContexts(component);
            for (int index = 0; index < contexts.size(); index++) {
                String name = contexts.size() == 1 ? component.getName()
                        : component.getName() + " (" + (index + 1) + "/" + contexts.size() + ")";
                try {
                    PrintablePart part = createPrintablePart(component, contexts.get(index), name);
                    String invalidReason = PrintableMeshValidator.validate(part);
                    if (invalidReason != null) {
                        warnings.add("Skipped " + name + " because it " + invalidReason + ".");
                        continue;
                    }
                    PartOrientationResolver.orient(part, options.isAutoOrient(), warnings);
                    parts.add(part);
                } catch (IllegalArgumentException exception) {
                    warnings.add("Skipped " + name + ": " + exception.getMessage() + ".");
                } catch (RuntimeException exception) {
                    warnings.add("Skipped " + name + " because its printable mesh could not be generated: "
                            + exception.getMessage());
                }
            }
        }
        return parts;
    }

    private PrintablePart createPrintablePart(RocketComponent component, InstanceContext context, String name) {
        DefaultObj obj = new DefaultObj();
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(
                Axis.Z, Axis.Y, 0, 0, 0);
        RocketComponentExporter<RocketComponent> exporter = OBJExporterFactory.createComponentExporter(
                obj, configuration, transformer, component, name, ObjUtils.LevelOfDetail.HIGH_QUALITY,
                true, warnings);
        exporter.addInstanceToObj(context);
        DefaultObj triangulated = TriangulationHelper.constrainedDelaunayTriangulate(obj);

        List<double[]> vertices = new ArrayList<>(triangulated.getNumVertices());
        for (int index = 0; index < triangulated.getNumVertices(); index++) {
            FloatTuple vertex = triangulated.getVertex(index);
            vertices.add(new double[] {
                    vertex.getX() * MILLIMETERS_PER_METER,
                    vertex.getY() * MILLIMETERS_PER_METER,
                    vertex.getZ() * MILLIMETERS_PER_METER
            });
        }

        List<int[]> triangles = new ArrayList<>(triangulated.getNumFaces());
        for (ObjFace face : triangulated.getFaces()) {
            if (face.getNumVertices() != 3) {
                throw new IllegalArgumentException("triangulation produced a non-triangular face");
            }
            triangles.add(new int[] {
                    face.getVertexIndex(0), face.getVertexIndex(1), face.getVertexIndex(2)
            });
        }
        return new PrintablePart(name, component, vertices, triangles);
    }

    private Set<RocketComponent> getSortedComponents() {
        Set<RocketComponent> requested = new HashSet<>(components);
        if (options.isExportChildren()) {
            for (RocketComponent component : components) {
                requested.addAll(component.getAllChildren());
            }
        }
        Set<RocketComponent> sorted = new LinkedHashSet<>();
        addChildren(configuration.getRocket(), requested, sorted);
        return sorted;
    }

    private void addChildren(RocketComponent parent, Set<RocketComponent> requested,
                             Set<RocketComponent> sorted) {
        for (RocketComponent child : parent.getChildren()) {
            if (requested.contains(child)) {
                sorted.add(child);
            }
            addChildren(child, requested, sorted);
        }
    }
}
