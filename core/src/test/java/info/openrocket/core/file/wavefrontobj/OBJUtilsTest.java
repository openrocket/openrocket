package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OBJUtilsTest extends BaseTestCase {
    public static final float EPSILON = 0.0001f;

    @Test
    public void testTranslateSingleVertex() {
        final DefaultObj obj = new DefaultObj();

        int startIdx = obj.getNumVertices();
        obj.addVertex(0.0f, 1.0f, 2.3f);
        int endIdx = obj.getNumVertices() - 1;

        ObjUtils.translateVertices(obj, startIdx, endIdx, 1.0f, 1.0f, 1.0f);

        FloatTuple vertex = obj.getVertex(startIdx);
        assertEquals(1.0f, vertex.getX(), EPSILON);
        assertEquals(2.0f, vertex.getY(), EPSILON);
        assertEquals(3.3f, vertex.getZ(), EPSILON);
    }

    @Test
    public void testTranslateMultipleVertices() {
        final DefaultObj obj = new DefaultObj();

        int startIdx = obj.getNumVertices();
        obj.addVertex(0.0f, 1.0f, 2.3f);
        obj.addVertex(2.0f, 2.0f, 4.6f);
        int endIdx = obj.getNumVertices() - 1;

        ObjUtils.translateVertices(obj, startIdx, endIdx, 0.0f, -2.0f, 0.0f);

        FloatTuple vertex0 = obj.getVertex(startIdx);
        assertEquals(0.0f, vertex0.getX(), EPSILON);
        assertEquals(-1.0f, vertex0.getY(), EPSILON);
        assertEquals(2.3f, vertex0.getZ(), EPSILON);

        FloatTuple vertex1 = obj.getVertex(endIdx);
        assertEquals(2.0f, vertex1.getX(), EPSILON);
        assertEquals(0.0f, vertex1.getY(), EPSILON);
        assertEquals(4.6f, vertex1.getZ(), EPSILON);
    }

    @Test
    public void testTranslateWithNoTranslation() {
        final DefaultObj obj = new DefaultObj();

        int startIdx = obj.getNumVertices();
        obj.addVertex(0.0f, 1.0f, 2.3f);
        int endIdx = obj.getNumVertices() - 1;

        ObjUtils.translateVertices(obj, startIdx, endIdx, 0.0f, 0.0f, 0.0f);

        FloatTuple vertex = obj.getVertex(startIdx);
        assertEquals(0.0f, vertex.getX(), EPSILON);
        assertEquals(1.0f, vertex.getY(), EPSILON);
        assertEquals(2.3f, vertex.getZ(), EPSILON);
    }

    @Test
    public void testTranslateIndexBoundaries() {
        final DefaultObj obj = new DefaultObj();

        obj.addVertex(0.0f, 1.0f, 2.3f);
        obj.addVertex(2.0f, 2.0f, 4.6f);
        obj.addVertex(4.0f, 3.0f, 6.9f);

        // Translate only the first vertex
        ObjUtils.translateVertices(obj, 0, 0, 1.0f, 1.0f, 1.0f);

        FloatTuple vertex0 = obj.getVertex(0);
        assertEquals(1.0f, vertex0.getX(), EPSILON);
        assertEquals(2.0f, vertex0.getY(), EPSILON);
        assertEquals(3.3f, vertex0.getZ(), EPSILON);

        FloatTuple vertex1 = obj.getVertex(1);
        assertEquals(2.0f, vertex1.getX(), EPSILON);
        assertEquals(2.0f, vertex1.getY(), EPSILON);
        assertEquals(4.6f, vertex1.getZ(), EPSILON);

        FloatTuple vertex2 = obj.getVertex(2);
        assertEquals(4.0f, vertex2.getX(), EPSILON);
        assertEquals(3.0f, vertex2.getY(), EPSILON);
        assertEquals(6.9f, vertex2.getZ(), EPSILON);

        // Reset and translate only the last vertex
        obj.setVertex(0, new DefaultFloatTuple(0.0f, 1.0f, 2.3f));
        ObjUtils.translateVertices(obj, 2, 2, 1.0f, 1.0f, 1.0f);

        vertex0 = obj.getVertex(0);
        assertEquals(0.0f, vertex0.getX(), EPSILON);
        assertEquals(1.0f, vertex0.getY(), EPSILON);
        assertEquals(2.3f, vertex0.getZ(), EPSILON);

        vertex1 = obj.getVertex(1);
        assertEquals(2.0f, vertex1.getX(), EPSILON);
        assertEquals(2.0f, vertex1.getY(), EPSILON);
        assertEquals(4.6f, vertex1.getZ(), EPSILON);

        vertex2 = obj.getVertex(2);
        assertEquals(5.0f, vertex2.getX(), EPSILON);
        assertEquals(4.0f, vertex2.getY(), EPSILON);
        assertEquals(7.9f, vertex2.getZ(), EPSILON);
    }

    @Test
    public void testTranslateWithNegativeValues() {
        final DefaultObj obj = new DefaultObj();

        int startIdx = obj.getNumVertices();
        obj.addVertex(0.0f, 1.0f, 2.3f);
        int endIdx = obj.getNumVertices() - 1;

        ObjUtils.translateVertices(obj, startIdx, endIdx, -1.0f, -1.0f, -1.0f);

        FloatTuple vertex = obj.getVertex(startIdx);
        assertEquals(-1.0f, vertex.getX(), EPSILON);
        assertEquals(0.0f, vertex.getY(), EPSILON);
        assertEquals(1.3f, vertex.getZ(), EPSILON);
    }



    @Test
    public void testRotateWithNoChange() {
        final DefaultObj obj = new DefaultObj();
        int verticesStartIdx = obj.getNumVertices();
        obj.addVertex(1.0f, 1.0f, 1.0f);
        int normalsStartIdx = obj.getNumNormals();
        obj.addNormal(1.0f, 0.0f, 0.0f);

        ObjUtils.rotateVertices(obj, verticesStartIdx, verticesStartIdx, normalsStartIdx, normalsStartIdx,
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        FloatTuple vertex = obj.getVertex(verticesStartIdx);
        assertEquals(1.0f, vertex.getX(), EPSILON);
        assertEquals(1.0f, vertex.getY(), EPSILON);
        assertEquals(1.0f, vertex.getZ(), EPSILON);

        FloatTuple normal = obj.getNormal(normalsStartIdx);
        assertEquals(1.0f, normal.getX(), EPSILON);
        assertEquals(0.0f, normal.getY(), EPSILON);
        assertEquals(0.0f, normal.getZ(), EPSILON);
    }

    @Test
    public void testRotationAroundXAxis() {
        final DefaultObj obj = new DefaultObj();
        obj.addVertex(1.0f, 1.0f, 1.0f);
        obj.addNormal(1.0f, 0.0f, 0.0f);

        ObjUtils.rotateVertices(obj, 0, 0, 0, 0,
                (float) Math.PI/2, 0, 0, 0.0f, 0.0f, 0.0f);

        FloatTuple vertex = obj.getVertex(0);
        assertEquals(1.0f, vertex.getX(), EPSILON);
        assertEquals(-1.0f, vertex.getY(), EPSILON);
        assertEquals(1.0f, vertex.getZ(), EPSILON);

        FloatTuple normal = obj.getNormal(0);
        assertEquals(1.0f, normal.getX(), EPSILON);
        assertEquals(0.0f, normal.getY(), EPSILON);
        assertEquals(0.0f, normal.getZ(), EPSILON);
    }

    @Test
    public void testRotationAroundYAxis() {
        final DefaultObj obj = new DefaultObj();
        int verticesStartIdx = obj.getNumVertices();
        obj.addVertex(1.0f, 1.0f, 1.0f);
        int normalsStartIdx = obj.getNumNormals();
        obj.addNormal(1.0f, 0.0f, 0.0f);

        ObjUtils.rotateVertices(obj, verticesStartIdx, verticesStartIdx, normalsStartIdx, normalsStartIdx,
                0.0f, (float) Math.PI/2, 0.0f, 0.0f, 0.0f, 0.0f);

        FloatTuple vertex = obj.getVertex(verticesStartIdx);
        assertEquals(1.0f, vertex.getX(), EPSILON);
        assertEquals(1.0f, vertex.getY(), EPSILON);
        assertEquals(-1.0f, vertex.getZ(), EPSILON);

        FloatTuple normal = obj.getNormal(normalsStartIdx);
        assertEquals(0.0f, normal.getX(), EPSILON);
        assertEquals(0.0f, normal.getY(), EPSILON);
        assertEquals(-1.0f, normal.getZ(), EPSILON);
    }
}
