package info.openrocket.core.file.threemf.export;

import java.util.HashMap;
import java.util.Map;

final class PrintableMeshValidator {
    private static final double MIN_TRIANGLE_AREA_SQUARED = 1.0e-16;
    private static final double MIN_VOLUME = 1.0e-6;

    private PrintableMeshValidator() {
    }

    static String validate(PrintablePart part) {
        if (part.getVertices().isEmpty() || part.getTriangles().isEmpty()) {
            return "contains no mesh geometry";
        }

        for (double[] vertex : part.getVertices()) {
            if (vertex.length < 3 || !Double.isFinite(vertex[0])
                    || !Double.isFinite(vertex[1]) || !Double.isFinite(vertex[2])) {
                return "contains an invalid vertex";
            }
        }

        Map<Edge, Integer> edgeCounts = new HashMap<>();
        double signedVolume = 0;
        for (int[] triangle : part.getTriangles()) {
            if (triangle.length != 3) {
                return "contains a non-triangular face";
            }
            for (int index : triangle) {
                if (index < 0 || index >= part.getVertices().size()) {
                    return "contains an out-of-range triangle index";
                }
            }
            if (triangle[0] == triangle[1] || triangle[1] == triangle[2] || triangle[2] == triangle[0]) {
                return "contains a degenerate triangle";
            }

            double[] a = part.getVertices().get(triangle[0]);
            double[] b = part.getVertices().get(triangle[1]);
            double[] c = part.getVertices().get(triangle[2]);
            double abx = b[0] - a[0];
            double aby = b[1] - a[1];
            double abz = b[2] - a[2];
            double acx = c[0] - a[0];
            double acy = c[1] - a[1];
            double acz = c[2] - a[2];
            double crossX = aby * acz - abz * acy;
            double crossY = abz * acx - abx * acz;
            double crossZ = abx * acy - aby * acx;
            if (crossX * crossX + crossY * crossY + crossZ * crossZ < MIN_TRIANGLE_AREA_SQUARED) {
                return "contains a zero-area triangle";
            }

            signedVolume += (a[0] * (b[1] * c[2] - b[2] * c[1])
                    - a[1] * (b[0] * c[2] - b[2] * c[0])
                    + a[2] * (b[0] * c[1] - b[1] * c[0])) / 6.0;
            increment(edgeCounts, triangle[0], triangle[1]);
            increment(edgeCounts, triangle[1], triangle[2]);
            increment(edgeCounts, triangle[2], triangle[0]);
        }

        for (int count : edgeCounts.values()) {
            if (count != 2) {
                return "is not a closed two-manifold mesh";
            }
        }
        if (Math.abs(signedVolume) < MIN_VOLUME) {
            return "has zero thickness or volume";
        }
        return null;
    }

    private static void increment(Map<Edge, Integer> edgeCounts, int first, int second) {
        Edge edge = new Edge(Math.min(first, second), Math.max(first, second));
        edgeCounts.merge(edge, 1, Integer::sum);
    }

    private record Edge(int first, int second) {
    }
}
