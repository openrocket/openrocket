package info.openrocket.core.plugin;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Interface for finding annotated classes from the class path.
 */
public interface AnnotationFinder {

	/**
	 * Return a list of all types (classes and interfaces) that are annotated
	 * with the provided annotation.
	 */
	public List<Class<?>> findAnnotatedTypes(Class<? extends Annotation> annotation);

}
