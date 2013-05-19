/*
 * BaseRocksimTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.lang.reflect.Field;
import java.util.List;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Assert;

/**
 * A base class for the Rocksim tests.  Includes code from the junitx.addons project.
 */
public abstract class RocksimTestBase extends BaseTestCase {
	
	public void assertContains(RocketComponent child, List<RocketComponent> components) {
		Assert.assertTrue("Components did not contain child", components.contains(child));
	}
	
	/**
	 * Returns the value of the field on the specified object.  The name
	 * parameter is a <code>String</code> specifying the simple name of the
	 * desired field.<p>
	 *
	 * The object is first searched for any matching field.  If no matching
	 * field is found, the superclasses are recursively searched.
	 *
	 * @exception NoSuchFieldException if a field with the specified name is
	 * not found.
	 */
	public static Object getField(Object object, String name) throws NoSuchFieldException {
		if (object == null) {
			throw new IllegalArgumentException("Invalid null object argument");
		}
		for (Class<?> cls = object.getClass(); cls != null; cls = cls.getSuperclass()) {
			try {
				Field field = cls.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(object);
			} catch (Exception ex) {
				/* in case of an exception, we will throw a new
				 * NoSuchFieldException object */
				;
			}
		}
		throw new NoSuchFieldException("Could get value for field " +
				object.getClass().getName() + "." + name);
	}
	
	/**
	 * Returns the value of the field on the specified class.  The name
	 * parameter is a <code>String</code> specifying the simple name of the
	 * desired field.<p>
	 *
	 * The class is first searched for any matching field.  If no matching
	 * field is found, the superclasses are recursively searched.
	 *
	 * @exception NoSuchFieldException if a field with the specified name is
	 * not found.
	 */
	public static Object getField(Class<?> cls, String name) throws NoSuchFieldException {
		if (cls == null) {
			throw new IllegalArgumentException("Invalid null cls argument");
		}
		Class<?> base = cls;
		while (base != null) {
			try {
				Field field = base.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(base);
			} catch (Exception ex) {
				/* in case of an exception, we will throw a new
				 * NoSuchFieldException object */
				;
			}
			base = base.getSuperclass();
		}
		throw new NoSuchFieldException("Could get value for static field " +
				cls.getName() + "." + name);
	}
	
	
}
