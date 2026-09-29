package info.openrocket.core.file.threemf.export;

import info.openrocket.core.rocketcomponent.RocketComponent;

import java.util.List;

final class PrintablePart {
    private final String name;
    private final RocketComponent component;
    private final List<double[]> vertices;
    private final List<int[]> triangles;
    private double placementX;
    private double placementY;

    PrintablePart(String name, RocketComponent component, List<double[]> vertices, List<int[]> triangles) {
        this.name = name;
        this.component = component;
        this.vertices = vertices;
        this.triangles = triangles;
    }

    String getName() {
        return name;
    }

    RocketComponent getComponent() {
        return component;
    }

    List<double[]> getVertices() {
        return vertices;
    }

    List<int[]> getTriangles() {
        return triangles;
    }

    double getPlacementX() {
        return placementX;
    }

    double getPlacementY() {
        return placementY;
    }

    void setPlacement(double x, double y) {
        this.placementX = x;
        this.placementY = y;
    }

    double getWidth() {
        return getMax(0) - getMin(0);
    }

    double getDepth() {
        return getMax(1) - getMin(1);
    }

    double getHeight() {
        return getMax(2) - getMin(2);
    }

    double getMinZ() {
        return getMin(2);
    }

    void normalizeToOrigin() {
        double minX = getMin(0);
        double minY = getMin(1);
        double minZ = getMin(2);
        for (double[] vertex : vertices) {
            vertex[0] -= minX;
            vertex[1] -= minY;
            vertex[2] -= minZ;
        }
    }

    void rotateZ90() {
        for (double[] vertex : vertices) {
            double x = vertex[0];
            vertex[0] = -vertex[1];
            vertex[1] = x;
        }
        normalizeToOrigin();
    }

    void rotateVectorToZ(double[] source) {
        double length = Math.sqrt(source[0] * source[0] + source[1] * source[1] + source[2] * source[2]);
        if (length < 1.0e-12) {
            return;
        }
        double ax = source[0] / length;
        double ay = source[1] / length;
        double az = source[2] / length;
        double dot = az;
        if (dot > 1.0 - 1.0e-12) {
            return;
        }
        if (dot < -1.0 + 1.0e-12) {
            for (double[] vertex : vertices) {
                vertex[1] = -vertex[1];
                vertex[2] = -vertex[2];
            }
            return;
        }

        double vx = ay;
        double vy = -ax;
        double vz = 0;
        double factor = 1.0 / (1.0 + dot);
        for (double[] vertex : vertices) {
            double x = vertex[0];
            double y = vertex[1];
            double z = vertex[2];
            double crossX = vy * z - vz * y;
            double crossY = vz * x - vx * z;
            double crossZ = vx * y - vy * x;
            double secondCrossX = vy * crossZ - vz * crossY;
            double secondCrossY = vz * crossX - vx * crossZ;
            double secondCrossZ = vx * crossY - vy * crossX;
            vertex[0] = x + crossX + secondCrossX * factor;
            vertex[1] = y + crossY + secondCrossY * factor;
            vertex[2] = z + crossZ + secondCrossZ * factor;
        }
    }

    double[] getCentroid() {
        double[] centroid = new double[3];
        for (double[] vertex : vertices) {
            centroid[0] += vertex[0];
            centroid[1] += vertex[1];
            centroid[2] += vertex[2];
        }
        if (!vertices.isEmpty()) {
            centroid[0] /= vertices.size();
            centroid[1] /= vertices.size();
            centroid[2] /= vertices.size();
        }
        return centroid;
    }

    private double getMin(int axis) {
        double value = Double.POSITIVE_INFINITY;
        for (double[] vertex : vertices) {
            value = Math.min(value, vertex[axis]);
        }
        return value;
    }

    private double getMax(int axis) {
        double value = Double.NEGATIVE_INFINITY;
        for (double[] vertex : vertices) {
            value = Math.max(value, vertex[axis]);
        }
        return value;
    }
}
