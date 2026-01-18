package info.openrocket.swing.gui.figure3d.core.geometry.components;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CPCaretGenerator {

    private static final float RADIUS = 0.06f;

    public static Mesh create(GraphicsQualitySettings.RenderQuality renderQuality) {
        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        final int segments = switch (renderQuality) {
            case LOW -> RenderingConstants.LOW_SEGMENT_COUNT;
            case MEDIUM -> RenderingConstants.MEDIUM_SEGMENT_COUNT;
            case HIGH -> RenderingConstants.HIGH_SEGMENT_COUNT;
        };

        // Generate the outer ring
        generateRing(vertices, indices, RADIUS, RADIUS * 0.9f, segments);
        // Generate the filled inner circle
        generateFilledCircle(vertices, indices, RADIUS * 0.75f, segments);

        return new Mesh(vertices, indices);
    }

    private static void generateRing(List<Vertex> vertices, List<Integer> indices, float outerRadius, float innerRadius, int segments) {
        int baseIndex = vertices.size();
        Vector3f normal = new Vector3f(0, 0, 1);
        Vector2f texCoords = new Vector2f(0, 0);

        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / segments * 2 * (float) Math.PI;

            float xOuter = (float) Math.cos(angle) * outerRadius;
            float yOuter = (float) Math.sin(angle) * outerRadius;
            vertices.add(new Vertex(new Vector3f(xOuter, yOuter, 0), normal, texCoords, 0));

            float xInner = (float) Math.cos(angle) * innerRadius;
            float yInner = (float) Math.sin(angle) * innerRadius;
            vertices.add(new Vertex(new Vector3f(xInner, yInner, 0), normal, texCoords, 0));
        }

        for (int i = 0; i < segments; i++) {
            int v0 = baseIndex + i * 2;
            int v1 = baseIndex + i * 2 + 1;
            int v2 = baseIndex + i * 2 + 2;
            int v3 = baseIndex + i * 2 + 3;

            indices.add(v0);
            indices.add(v2);
            indices.add(v1);

            indices.add(v1);
            indices.add(v2);
            indices.add(v3);
        }
    }

    private static void generateFilledCircle(List<Vertex> vertices, List<Integer> indices, float radius, int segments) {
        int baseIndex = vertices.size();
        Vector3f normal = new Vector3f(0, 0, 1);
        Vector2f texCoords = new Vector2f(0, 0);

        // Center vertex
        vertices.add(new Vertex(new Vector3f(0, 0, 0), normal, texCoords, 0));

        // Edge vertices
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / segments * 2 * (float) Math.PI;
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            vertices.add(new Vertex(new Vector3f(x, y, 0), normal, texCoords, 0));
        }

        // Indices for the triangles
        for (int i = 0; i < segments; i++) {
            indices.add(baseIndex); // Center vertex
            indices.add(baseIndex + i + 1);
            indices.add(baseIndex + i + 2);
        }
    }
}
