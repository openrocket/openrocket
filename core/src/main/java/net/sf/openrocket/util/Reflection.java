package net.sf.openrocket.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import net.sf.openrocket.rocketcomponent.RocketComponent;


public class Reflection {
	
	/**
	 * Simple wrapper class that converts the Method.invoke() exceptions into suitable
	 * RuntimeExceptions.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public static class Method {
		private final java.lang.reflect.Method method;
		
		public Method(java.lang.reflect.Method m) {
			if (m == null) {
				throw new IllegalArgumentException("method is null");
			}
			method = m;
		}
		
		/**
		 * Same as Method.invoke(), but the possible exceptions are wrapped into 
		 * RuntimeExceptions.
		 */
		public Object invoke(Object obj, Object... args) {
			try {
				return method.invoke(obj, args);
			} catch (IllegalArgumentException e) {
				throw new BugException("Error while invoking method '" + method + "'. " +
						"Please report this as a bug.", e);
			} catch (IllegalAccessException e) {
				throw new BugException("Error while invoking method '" + method + "'. " +
						"Please report this as a bug.", e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
		}
		
		/**
		 * Invoke static method.  Equivalent to invoke(null, args...).
		 */
		public Object invokeStatic(Object... args) {
			return invoke(null, args);
		}
		
		/**
		 * Same as Method.toString().
		 */
		@Override
		public String toString() {
			return method.toString();
		}
	}
	
	
	/**
	 * Handles an InvocationTargetException gracefully.  If the cause is an unchecked
	 * exception it is thrown, otherwise it is encapsulated in a BugException.
	 * <p>
	 * This method has a return type of Error in order to allow writing code like:
	 * <pre>throw Reflection.handleInvocationTargetException(e)</pre>
	 * This allows the compiler verifying that the call will never succeed correctly
	 * and ending that branch of execution.
	 * 
	 * @param e		the InvocationTargetException that occurred (not null).
	 * @return		never returns normally.
	 */
	public static Error handleWrappedException(Exception e) {
		Throwable cause = e.getCause();
		if (cause == null) {
			throw new BugException("wrapped exception without cause", e);
		}
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		if (cause instanceof Error) {
			throw (Error) cause;
		}
		throw new BugException("wrapped exception occurred", cause);
	}
	
	
	
	/**
	 * Find a method from a class.
	 * Throws an exception if method not found.
	 */
	public static Reflection.Method findMethod(Class<?> c, String method, Class<?>... params) {
		
		java.lang.reflect.Method m;
		try {
			m = c.getMethod(method, params);
			return new Reflection.Method(m);
		} catch (NoSuchMethodException e) {
			throw new BugException("Could not find method " + method + "(" + Arrays.toString(params) + ") from class " + c);
		}
	}
	
	
	
	public static Reflection.Method findMethod(String pack, RocketComponent component,
			String method, Class<?>... params) {
		return findMethod(pack, component.getClass(), "", method, params);
	}
	
	
	public static Reflection.Method findMethod(String pack, RocketComponent component,
			String suffix, String method, Class<?>... params) {
		return findMethod(pack, component.getClass(), suffix, method, params);
	}
	
	
	public static Reflection.Method findMethod(String pack,
			Class<? extends RocketComponent> componentClass,
			String suffix, String method, Class<?>... params) {
		Class<?> currentclass;
		String name;
		
		currentclass = componentClass;
		while ((currentclass != null) && (currentclass != Object.class)) {
			name = currentclass.getCanonicalName();
			if (name.lastIndexOf('.') >= 0)
				name = name.substring(name.lastIndexOf(".") + 1);
			name = pack + "." + name + suffix;
			
			try {
				Class<?> c = Class.forName(name);
				java.lang.reflect.Method m = c.getMethod(method, params);
				return new Reflection.Method(m);
			} catch (ClassNotFoundException ignore) {
			} catch (NoSuchMethodException ignore) {
			}
			
			currentclass = currentclass.getSuperclass();
		}
		return null;
	}
	
	
	public static Object construct(String pack, RocketComponent component, String suffix,
			Object... params) {
		
		Class<?> currentclass;
		String name;
		
		currentclass = component.getClass();
		while ((currentclass != null) && (currentclass != Object.class)) {
			name = currentclass.getCanonicalName();
			if (name.lastIndexOf('.') >= 0)
				name = name.substring(name.lastIndexOf(".") + 1);
			name = pack + "." + name + suffix;
			
			try {
				Class<?> c = Class.forName(name);
				Class<?>[] paramClasses = new Class<?>[params.length];
				for (int i = 0; i < params.length; i++) {
					paramClasses[i] = params[i].getClass();
				}
				
				// Constructors must be searched manually.  Why?!
				main: for (Constructor<?> constructor : c.getConstructors()) {
					Class<?>[] parameterTypes = constructor.getParameterTypes();
					if (params.length != parameterTypes.length)
						continue;
					for (int i = 0; i < params.length; i++) {
						if (!parameterTypes[i].isInstance(params[i]))
							continue main;
					}
					// Matching constructor found
					return constructor.newInstance(params);
				}
			} catch (ClassNotFoundException ignore) {
			} catch (IllegalArgumentException e) {
				throw new BugException("Construction of " + name + " failed", e);
			} catch (InstantiationException e) {
				throw new BugException("Construction of " + name + " failed", e);
			} catch (IllegalAccessException e) {
				throw new BugException("Construction of " + name + " failed", e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
			
			currentclass = currentclass.getSuperclass();
		}
		throw new BugException("Suitable constructor for component " + component +
				" not found");
	}
}
