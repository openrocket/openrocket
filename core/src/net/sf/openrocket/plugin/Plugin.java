package net.sf.openrocket.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that defines an interface to be a plugin interface and
 * classes as plugin implementations.
 * <p>
 * Plugin interfaces are automatically discovered from the classpath and
 * registered as plugins in Guice.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Plugin {
	
}
