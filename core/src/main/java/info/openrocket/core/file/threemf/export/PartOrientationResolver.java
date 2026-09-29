package info.openrocket.core.file.threemf.export;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeFinSet;

final class PartOrientationResolver {
    private PartOrientationResolver() {
    }

    static void orient(PrintablePart part, boolean autoOrient, WarningSet warnings) {
        if (!autoOrient) {
            part.normalizeToOrigin();
            return;
        }

        RocketComponent component = part.getComponent();
        if (component instanceof FinSet) {
            part.rotateVectorToZ(findSmallestPrincipalAxis(part));
        } else if (component instanceof RailButton) {
            double[] centroid = part.getCentroid();
            double[] radialAxis = new double[] {centroid[0], centroid[1], 0};
            if (Math.hypot(radialAxis[0], radialAxis[1]) < 1.0e-9) {
                radialAxis = findSmallestPrincipalAxis(part);
            }
            part.rotateVectorToZ(radialAxis);
        } else if (!(component instanceof BodyTube || component instanceof Transition
                || component instanceof LaunchLug || component instanceof TubeFinSet
                || component instanceof RingComponent || component instanceof MassObject)) {
            warnings.add("No automatic print orientation is defined for " + component.getName()
                    + "; its canonical orientation was preserved.");
        }

        part.normalizeToOrigin();
    }

    private static double[] findSmallestPrincipalAxis(PrintablePart part) {
        double[] centroid = part.getCentroid();
        double[][] covariance = new double[3][3];
        for (double[] vertex : part.getVertices()) {
            double[] delta = new double[] {
                    vertex[0] - centroid[0], vertex[1] - centroid[1], vertex[2] - centroid[2]
            };
            for (int row = 0; row < 3; row++) {
                for (int column = row; column < 3; column++) {
                    covariance[row][column] += delta[row] * delta[column];
                    covariance[column][row] = covariance[row][column];
                }
            }
        }

        double[][] eigenvectors = new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        for (int iteration = 0; iteration < 24; iteration++) {
            int p = 0;
            int q = 1;
            if (Math.abs(covariance[0][2]) > Math.abs(covariance[p][q])) {
                q = 2;
            }
            if (Math.abs(covariance[1][2]) > Math.abs(covariance[p][q])) {
                p = 1;
                q = 2;
            }
            if (Math.abs(covariance[p][q]) < 1.0e-12) {
                break;
            }

            double angle = 0.5 * Math.atan2(2 * covariance[p][q], covariance[q][q] - covariance[p][p]);
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            rotateSymmetric(covariance, p, q, cosine, sine);
            for (int row = 0; row < 3; row++) {
                double vp = eigenvectors[row][p];
                double vq = eigenvectors[row][q];
                eigenvectors[row][p] = cosine * vp - sine * vq;
                eigenvectors[row][q] = sine * vp + cosine * vq;
            }
        }

        int smallest = 0;
        if (covariance[1][1] < covariance[smallest][smallest]) {
            smallest = 1;
        }
        if (covariance[2][2] < covariance[smallest][smallest]) {
            smallest = 2;
        }
        return new double[] {
                eigenvectors[0][smallest], eigenvectors[1][smallest], eigenvectors[2][smallest]
        };
    }

    private static void rotateSymmetric(double[][] matrix, int p, int q, double cosine, double sine) {
        double app = matrix[p][p];
        double aqq = matrix[q][q];
        double apq = matrix[p][q];
        matrix[p][p] = cosine * cosine * app - 2 * sine * cosine * apq + sine * sine * aqq;
        matrix[q][q] = sine * sine * app + 2 * sine * cosine * apq + cosine * cosine * aqq;
        matrix[p][q] = 0;
        matrix[q][p] = 0;
        for (int index = 0; index < 3; index++) {
            if (index == p || index == q) {
                continue;
            }
            double aip = matrix[index][p];
            double aiq = matrix[index][q];
            matrix[index][p] = cosine * aip - sine * aiq;
            matrix[p][index] = matrix[index][p];
            matrix[index][q] = sine * aip + cosine * aiq;
            matrix[q][index] = matrix[index][q];
        }
    }
}
