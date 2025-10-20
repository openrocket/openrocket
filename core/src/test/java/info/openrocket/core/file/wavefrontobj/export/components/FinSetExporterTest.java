package info.openrocket.core.file.wavefrontobj.export.components;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.file.wavefrontobj.Axis;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ImmutableCoordinate;
import info.openrocket.core.util.Transformation;
import de.javagl.obj.FloatTuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinSetExporterTest {

    private static final float FLOAT_EPSILON = 1.0e-6f;

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void exportsZeroThicknessFinAsSinglePlane() {
        TestFinSet finSet = createFin(0.0,
                new ImmutableCoordinate(0.0, 0.0, 0.0),
                new ImmutableCoordinate(0.02, 0.03, 0.0),
                new ImmutableCoordinate(0.05, 0.03, 0.0),
                new ImmutableCoordinate(0.05, 0.0, 0.0));

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(finSet);

        newExporter(obj, config, finSet, warnings).addToObj();

        assertEquals(4, obj.getNumVertices(), "Unexpected vertex count for zero-thickness fin");
        assertEquals(2, obj.getNumNormals(), "Zero-thickness fin should only create front/back normals");
        assertEquals(2, obj.getNumFaces(), "Zero-thickness fin should only create two faces");
        assertEquals(1, warnings.size(), "Zero-thickness fin should emit a warning");

        for (int i = 0; i < obj.getNumVertices(); i++) {
            FloatTuple vertex = obj.getVertex(i);
            assertEquals(0.0f, vertex.getZ(), FLOAT_EPSILON, "Zero-thickness fin must lie on a plane");
        }
    }

    @Test
    void skipsDegeneratePolygonWithLessThanThreePoints() {
        TestFinSet finSet = createFin(0.0,
                new ImmutableCoordinate(0.0, 0.0, 0.0),
                new ImmutableCoordinate(0.03, 0.0, 0.0));

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(finSet);

        newExporter(obj, config, finSet, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate fin geometry should be ignored");
        assertEquals(0, obj.getNumNormals(), "Degenerate fin geometry should not create normals");
        assertEquals(0, obj.getNumFaces(), "Degenerate fin geometry should not create faces");
        assertEquals(1, warnings.size(), "Zero-thickness degenerate fin should still register warning");
    }

    @Test
    void skipsDegeneratePolygonWithLessThanThreePointsWhenThick() {
        TestFinSet finSet = createFin(0.004,
                new ImmutableCoordinate(0.0, 0.0, 0.0),
                new ImmutableCoordinate(0.03, 0.0, 0.0));

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(finSet);

        newExporter(obj, config, finSet, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate fin geometry should be ignored");
        assertEquals(0, obj.getNumNormals(), "Degenerate fin geometry should not create normals");
        assertEquals(0, obj.getNumFaces(), "Degenerate fin geometry should not create faces");
        assertEquals(0, warnings.size(), "No warning expected for finite thickness degenerate fin");
    }

    @Test
    void extrudesConcaveFiniteThicknessFin() {
        TestFinSet finSet = createFin(0.004,
                new ImmutableCoordinate(0.0, 0.0, 0.0),
                new ImmutableCoordinate(-0.01, 0.01, 0.0),
                new ImmutableCoordinate(-0.005, 0.025, 0.0),
                new ImmutableCoordinate(0.015, 0.03, 0.0),
                new ImmutableCoordinate(0.03, 0.015, 0.0));

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(finSet);

        newExporter(obj, config, finSet, warnings).addToObj();

        int expectedVertices = finSet.getFinPoints().length * 2;
        int expectedFaces = finSet.getFinPoints().length + 2; // front/back + one quad per edge
        int expectedNormals = finSet.getFinPoints().length + 2;

        assertEquals(expectedVertices, obj.getNumVertices(), "Unexpected vertex count for extruded fin");
        assertEquals(expectedFaces, obj.getNumFaces(), "Unexpected face count for extruded fin");
        assertEquals(expectedNormals, obj.getNumNormals(), "Unexpected normal count for extruded fin");
        assertEquals(0, warnings.size(), "Finite thickness fin should not emit zero-thickness warning");
    }

    private static FinSetExporter newExporter(DefaultObj obj, FlightConfiguration config, TestFinSet finSet, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new FinSetExporter(obj, config, transformer, finSet, "testGroup", ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(TestFinSet finSet) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(finSet, 0, Transformation.IDENTITY);
        return config;
    }

    private static TestFinSet createFin(double thickness, Coordinate... points) {
        TestFinSet finSet = new TestFinSet(points);
        finSet.setThickness(thickness);
        return finSet;
    }

    private static final class TestFinSet extends FinSet {
        private Coordinate[] finPoints;

        private TestFinSet(Coordinate[] points) {
            setFinPoints(points);
        }

        private void setFinPoints(Coordinate[] points) {
            this.finPoints = Arrays.copyOf(points, points.length);
            if (this.finPoints.length > 0) {
                this.length = this.finPoints[this.finPoints.length - 1].getX();
            } else {
                this.length = 0;
            }
        }

        @Override
        public Coordinate[] getFinPoints() {
            return Arrays.copyOf(finPoints, finPoints.length);
        }

        @Override
        public Coordinate[] getFinPointsWithRoot() {
            return getFinPoints();
        }

        @Override
        public Coordinate[] getTabPointsWithRoot() {
            return new Coordinate[0];
        }

        @Override
        public double getSpan() {
            if (finPoints.length == 0) {
                return 0;
            }
            double minY = finPoints[0].getY();
            double maxY = finPoints[0].getY();
            for (Coordinate point : finPoints) {
                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());
            }
            return maxY - minY;
        }

        @Override
        public String getComponentName() {
            return "TestFinSet";
        }
    }
}
