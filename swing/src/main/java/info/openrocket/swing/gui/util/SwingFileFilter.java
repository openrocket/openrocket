package info.openrocket.swing.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import info.openrocket.core.gui.util.SimpleFileFilter;

/**
 * Swing {@link javax.swing.filechooser.FileFilter} adapter around the headless
 * core {@link SimpleFileFilter}, so the same extension-matching logic can be used
 * both in {@link javax.swing.JFileChooser} and as a {@link java.io.FileFilter}.
 * <p>
 * The matching logic lives in core; this class only supplies the Swing
 * ({@code java.desktop}) file-chooser type.
 */
public class SwingFileFilter extends FileFilter implements java.io.FileFilter {

	private final SimpleFileFilter delegate;

	public SwingFileFilter(String description, String... extensions) {
		this.delegate = new SimpleFileFilter(description, extensions);
	}

	public SwingFileFilter(String description, boolean acceptDir, String... extensions) {
		this.delegate = new SimpleFileFilter(description, acceptDir, extensions);
	}

	public SwingFileFilter(SimpleFileFilter delegate) {
		this.delegate = delegate;
	}

	public String[] getExtensions() {
		return delegate.getExtensions();
	}

	@Override
	public boolean accept(File file) {
		return delegate.accept(file);
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}
}
