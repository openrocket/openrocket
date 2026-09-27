package info.openrocket.core.plugin;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * An AnnotationFinder that uses annotation-detector library to scan
 * the class path. Compatible with the JIJ loader.
 */
public class AnnotationFinderImpl implements AnnotationFinder {

	@Override
	public List<Class<?>> findAnnotatedTypes(Class<? extends Annotation> annotation) {
		List<Class<?>> classes;
		// Only class and annotation info is needed; field and method info make the startup scan about twice as slow
		try (ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
			classes = new ArrayList<>(scanResult.getClassesWithAnnotation(annotation.getName()).loadClasses());
		}
		return classes;
	}
}
