package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sf.openrocket.util.BugException;

public class ComponentCompare {
	
	private static final Pattern GETTER_PATTERN = Pattern.compile("^(is|get)[A-Z].*+");
	
	private static final String[] IGNORED_METHODS = {
			"getClass", "getChildCount", "getChildren", "getNextComponent", "getID",
			"getPreviousComponent", "getParent", "getRocket", "getRoot", "getStage",
			"getStageNumber", "getComponentName",
			"getStageSeparationConfiguration",
			"getMotorConfiguration",
			"getIgnitionConfiguration",
			// Rocket specific methods:
			"getModID", "getMassModID", "getAerodynamicModID", "getTreeModID", "getFunctionalModID",
			"getFlightConfigurationIDs", "getDefaultConfiguration", "getMotorMounts"
	};
	
	
	/**
	 * Check whether the two components are <em>equal</em>.  Two components are considered
	 * equal if they are of the same type and all of their getXXX() and isXXX() methods
	 * return equal values.
	 * 
	 * @param c1	the first component to compare.
	 * @param c2	the second component to compare.
	 */
	public static void assertEquality(RocketComponent c1, RocketComponent c2) {
		assertEquals(c1.getClass(), c2.getClass());
		
		// Same class + similar  ==  equal
		assertSimilarity(c1, c2);
	}
	
	
	
	public static void assertDeepEquality(RocketComponent c1, RocketComponent c2) {
		assertEquality(c1, c2);
		
		Iterator<RocketComponent> i1 = c1.getChildren().iterator();
		Iterator<RocketComponent> i2 = c2.getChildren().iterator();
		while (i1.hasNext()) {
			assertTrue("iterator continues", i2.hasNext());
			RocketComponent comp1 = i1.next();
			RocketComponent comp2 = i2.next();
			assertDeepEquality(comp1, comp2);
		}
		assertFalse("iterator end", i2.hasNext());
	}
	
	
	
	public static void assertDeepSimilarity(RocketComponent c1, RocketComponent c2,
			boolean allowNameDifference) {
		assertSimilarity(c1, c2, allowNameDifference);
		
		Iterator<RocketComponent> i1 = c1.getChildren().iterator();
		Iterator<RocketComponent> i2 = c2.getChildren().iterator();
		while (i1.hasNext()) {
			assertTrue("iterator continues", i2.hasNext());
			RocketComponent comp1 = i1.next();
			RocketComponent comp2 = i2.next();
			assertDeepSimilarity(comp1, comp2, allowNameDifference);
		}
		assertFalse("iterator end", i2.hasNext());
	}
	
	
	
	/**
	 * Check whether the two components are <em>similar</em>.  Two components are similar
	 * if each of the getXXX and isXXX methods that both object types have return
	 * equal values.  This does not check whether the two components are of the same type.
	 * 
	 * @param c1	the first component.
	 * @param c2	the second component.
	 */
	public static void assertSimilarity(RocketComponent c1, RocketComponent c2) {
		assertSimilarity(c1, c2, false);
	}
	
	/**
	 * Check whether the two components are <em>similar</em>, allowing a name difference.
	 * 
	 * @param c1	the first component.
	 * @param c2	the second component.
	 * @param allowNameDifference	whether to allow the components to have different names.
	 */
	public static void assertSimilarity(RocketComponent c1, RocketComponent c2,
			boolean allowNameDifference) {
		Class<? extends RocketComponent> class1 = c1.getClass();
		Class<? extends RocketComponent> class2 = c2.getClass();
		
		mainloop: for (Method m1 : class1.getMethods()) {
			// Check for getter method
			String name = m1.getName();
			if (!GETTER_PATTERN.matcher(name).matches())
				continue;
			
			// Ignore methods that take parameters
			if (m1.getParameterTypes().length != 0)
				continue;
			
			// Ignore specific getters
			for (String ignore : IGNORED_METHODS) {
				if (name.equals(ignore))
					continue mainloop;
			}
			if (allowNameDifference && name.equals("getName"))
				continue;
			
			
			// Check for method in other class
			Method m2;
			try {
				m2 = class2.getMethod(name);
			} catch (NoSuchMethodException e) {
				continue;
			}
			
			//			System.out.println("Testing results of method " + name);
			
			// Run the methods
			Object result1, result2;
			try {
				result1 = m1.invoke(c1);
				result2 = m2.invoke(c2);
			} catch (Exception e) {
				throw new BugException("Error executing method " + name, e);
			}
			
			if (result1 != null && result2 != null &&
					result1.getClass().isArray() && result2.getClass().isArray()) {
				assertArrayEquals("Comparing result of method " + name,
						(Object[]) result1, (Object[]) result2);
			} else {
				assertEquals("Comparing result of method " + name, result1, result2);
			}
		}
	}
	
}
