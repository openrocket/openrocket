/*
 * FinSetHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * FinSetHandler Tester.
 *
 */
public class FinSetHandlerTest {

	final static double EPSILON = MathUtil.EPSILON;
	
	@BeforeClass
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		
		Module pluginModule = new PluginModule();
		
		Module debugTranslator = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};
				
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator), pluginModule);
		
		Application.setInjector(injector);
		
		File tmpDir = new File("./tmp");
		if (!tmpDir.exists()) {
			boolean success = tmpDir.mkdirs();
			assertTrue("Unable to create core/tmp dir needed for tests.", success);
		}
		
	}
	
	
    /**
     * Method: asOpenRocket(WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testAsOpenRocket() throws Exception {

        FinSetHandler dto = new FinSetHandler(null, new BodyTube());

        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        dto.closeElement("Name", attributes, "The name", warnings);
        dto.closeElement("ShapeCode", attributes, "0", warnings);
        dto.closeElement("Xb", attributes, "2", warnings);
        dto.closeElement("FinCount", attributes, "4", warnings);
        dto.closeElement("RootChord", attributes, "100", warnings);
        dto.closeElement("TipChord", attributes, "50", warnings);
        dto.closeElement("SemiSpan", attributes, "12", warnings);
        dto.closeElement("MidChordLen", attributes, "13", warnings);
        dto.closeElement("SweepDistance", attributes, "14", warnings);
        dto.closeElement("Thickness", attributes, "200", warnings);
        dto.closeElement("TipShapeCode", attributes, "1", warnings);
        dto.closeElement("TabLength", attributes, "40", warnings);
        dto.closeElement("TabDepth", attributes, "50", warnings);
        dto.closeElement("TabOffset", attributes, "30", warnings);
        dto.closeElement("RadialAngle", attributes, ".123", warnings);
        dto.closeElement("PointList", attributes, "20,0|2,2|0,0", warnings);
        dto.closeElement("LocationMode", attributes, "0", warnings);

        WarningSet set = new WarningSet();
        FinSet fins = dto.asOpenRocket(set);
        assertNotNull(fins);
        assertEquals(0, set.size());
        
	    String debugInfo = fins.toDebugDetail().toString();

        assertEquals("The name", fins.getName());
        assertTrue(fins instanceof TrapezoidFinSet);
        assertEquals("imported fin count does not match.", 4, fins.getFinCount());

        assertEquals("imported fin height does not match.", 0.012d, ((TrapezoidFinSet) fins).getHeight(), EPSILON);
        assertEquals("imported fin span does not match.", 0.012d, fins.getSpan(), EPSILON);
        
        assertEquals("imported fin thickness does not match.", 0.2d, fins.getThickness(), EPSILON);
        assertEquals("imported fin tab length does not match: "+debugInfo, 0.04d, fins.getTabLength(), EPSILON);
        assertEquals("imported fin tab height does not match: "+debugInfo, 0.05d, fins.getTabHeight(), EPSILON);
        assertEquals("imported fin shift does not match.", 0.03d, fins.getTabOffset(), EPSILON);
        assertEquals("imported fin rotation does not match.", .123d, fins.getBaseRotation(), EPSILON);

        dto.closeElement("ShapeCode", attributes, "1", warnings);
        
        fins = dto.asOpenRocket(set);
        assertNotNull(fins);
        assertEquals(0, set.size());

        assertEquals("The name", fins.getName());
        assertTrue(fins instanceof EllipticalFinSet);
        assertEquals("imported fin count does not match.", 4, fins.getFinCount());

        assertEquals("imported fin thickness does not match.", 0.2d, fins.getThickness(), EPSILON);
        assertEquals("imported fin tab length does not match.", 0.04d, fins.getTabLength(), EPSILON);
        assertEquals("imported fin tab height does not match.", 0.05d, fins.getTabHeight(), EPSILON);
        assertEquals("imported fin tab shift does not match.", 0.03d, fins.getTabOffset(), EPSILON);
        assertEquals("imported fin rotation does not match.", .123d, fins.getBaseRotation(), EPSILON);
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
