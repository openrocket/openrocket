package net.sf.openrocket.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.JarUtil;
import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;

/**
 * An AnnotationFinder that uses annotation-detector library to scan
 * the class path.  Compatible with the JIJ loader.
 */
public class AnnotationFinderImpl implements AnnotationFinder {
	
	@Override
	public List<Class<?>> findAnnotatedTypes(Class<?> annotation) {
		final List<Class<?>> classes = new ArrayList<Class<?>>();
		
		TypeReporter reporter = new ListReporter(classes);
		final AnnotationDetector cf = new AnnotationDetector(reporter);
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			if (loader instanceof URLClassLoader) {
				
				/*
				 * In case of URLClassLoader (which may be our own instantiation)
				 * use the URLs from there, as java.class.path may not be up-to-date.
				 */
				
				URLClassLoader urlClassLoader = (URLClassLoader) loader;
				URL[] urls = urlClassLoader.getURLs();
				
				List<File> files = new ArrayList<File>();
				for (URL url : urls) {
					if (url.getProtocol().equals("file")) {
						files.add(JarUtil.urlToFile(url));
					}
				}
				
				cf.detect(files.toArray(new File[0]));
			} else {
				
				/*
				 * If not using a URLClassLoader, just do the default.
				 */
				cf.detect();
			}
			
		} catch (IOException e) {
			throw new BugException("Unable to search class path", e);
		}
		
		return classes;
	}
	
	
	private static class ListReporter implements TypeReporter {
		private final List<Class<?>> classes;
		private final Set<String> names = new HashSet<String>();
		
		public ListReporter(List<Class<?>> classes) {
			this.classes = classes;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Annotation>[] annotations() {
			return new Class[] { Plugin.class };
		}
		
		@Override
		public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
			if (names.add(className)) {
				try {
					classes.add(this.getClass().getClassLoader().loadClass(className));
				} catch (ClassNotFoundException e) {
					// Ignore
				}
			}
		}
	}
}
