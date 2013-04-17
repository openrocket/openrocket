/*
 * FinSetHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import junit.framework.TestCase;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.Coordinate;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * FinSetHandler Tester.
 *
 */
public class FinSetHandlerTest extends TestCase {

    /**
     * Method: asOpenRocket(WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testAsOpenRocket() throws Exception {

        FinSetHandler dto = new FinSetHandler(null, new BodyTube());

        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        dto.closeElement("Name", attributes, "The name", warnings);
        dto.closeElement("ShapeCode", attributes, "0", warnings);
        dto.closeElement("Xb", attributes, "2", warnings);
        dto.closeElement("FinCount", attributes, "4", warnings);
        dto.closeElement("RootChord", attributes, "10", warnings);
        dto.closeElement("TipChord", attributes, "11", warnings);
        dto.closeElement("SemiSpan", attributes, "12", warnings);
        dto.closeElement("MidChordLen", attributes, "13", warnings);
        dto.closeElement("SweepDistance", attributes, "14", warnings);
        dto.closeElement("Thickness", attributes, "200", warnings);
        dto.closeElement("TipShapeCode", attributes, "1", warnings);
        dto.closeElement("TabLength", attributes, "400", warnings);
        dto.closeElement("TabDepth", attributes, "500", warnings);
        dto.closeElement("TabOffset", attributes, "30", warnings);
        dto.closeElement("RadialAngle", attributes, ".123", warnings);
        dto.closeElement("PointList", attributes, "20,0|2,2|0,0", warnings);
        dto.closeElement("LocationMode", attributes, "0", warnings);

        WarningSet set = new WarningSet();
        FinSet fins = dto.asOpenRocket(set);
        assertNotNull(fins);
        assertEquals(0, set.size());

        assertEquals("The name", fins.getName());
        assertTrue(fins instanceof TrapezoidFinSet);
        assertEquals(4, fins.getFinCount());

        assertEquals(0.012d, ((TrapezoidFinSet) fins).getHeight());
        assertEquals(0.012d, fins.getSpan());
        
        assertEquals(0.2d, fins.getThickness());
        assertEquals(0.4d, fins.getTabLength());
        assertEquals(0.5d, fins.getTabHeight());
        assertEquals(0.03d, fins.getTabShift());
        assertEquals(.123d, fins.getBaseRotation());

        dto.closeElement("ShapeCode", attributes, "1", warnings);
        fins = dto.asOpenRocket(set);
        assertNotNull(fins);
        assertEquals(0, set.size());

        assertEquals("The name", fins.getName());
        assertTrue(fins instanceof EllipticalFinSet);
        assertEquals(4, fins.getFinCount());

        assertEquals(0.2d, fins.getThickness());
        assertEquals(0.4d, fins.getTabLength());
        assertEquals(0.5d, fins.getTabHeight());
        assertEquals(0.03d, fins.getTabShift());
        assertEquals(.123d, fins.getBaseRotation());
    }


    /**
     * Method: toCoordinates(String pointList)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testToCoordinates() throws Exception {
        FinSetHandler holder = new FinSetHandler(null, new BodyTube());
        Method method = FinSetHandler.class.getDeclaredMethod("toCoordinates", String.class, WarningSet.class);
        method.setAccessible(true);

        WarningSet warnings = new WarningSet();
        //Null finlist
        String finlist = null;
        Coordinate[] result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(0 == result.length);

        //Empty string finlist
        finlist = "";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(0 == result.length);
        
        //Invalid finlist (only x coordinate)
        finlist = "10.0";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(0 == result.length);
        assertEquals(1, warnings.size());
        warnings.clear();

        //Invalid finlist (non-numeric character)
        finlist = "10.0,asdf";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(0 == result.length);
        assertEquals(1, warnings.size());
        warnings.clear();

        //Invalid finlist (all delimiters)
        finlist = "||||||";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(0 == result.length);
        assertEquals(0, warnings.size());
        warnings.clear();

        //One point finlist - from a parsing view it's valid; from a practical view it may not be, but that's outside
        //the scope of this test case
        finlist = "10.0,5.0";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(1 == result.length);
        assertEquals(0, warnings.size());
        warnings.clear();
        
        //Two point finlist - from a parsing view it's valid; from a practical view it may not be, but that's outside
        //the scope of this test case
        finlist = "10.0,5.0|3.3,4.4";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(2 == result.length);
        assertEquals(0, warnings.size());
        warnings.clear();
        
        //Normal four point finlist.
        finlist = "518.16,0|517.494,37.2145|1.31261,6.77283|0,0|";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(4 == result.length);
        assertEquals(new Coordinate(0,0), result[0]);
        assertEquals(0, warnings.size());
        warnings.clear();
        
        //Normal four point finlist with spaces.
        finlist = "518.16 , 0 | 517.494 , 37.2145 | 1.31261,6.77283|0,0|";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(4 == result.length);
        assertEquals(new Coordinate(0,0), result[0]);
        assertEquals(new Coordinate(.51816,0), result[3]);
        assertEquals(0, warnings.size());
        warnings.clear();
        
        //Reversed Normal four point finlist.
        finlist = "0,0|1.31261,6.77283|517.494,37.2145|518.16,0|";
        result = (Coordinate[])method.invoke(holder, finlist, warnings);
        assertNotNull(result);
        assertTrue(4 == result.length);
        assertEquals(new Coordinate(0,0), result[0]);
        assertEquals(new Coordinate(.51816,0), result[3]);
        assertEquals(0, warnings.size());
        warnings.clear();
        
    }
}
