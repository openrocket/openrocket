package net.sf.openrocket.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.ChangeSource;

public abstract class Attachment extends AbstractChangeSource implements Comparable<Attachment>, ChangeSource {
	
	private final String name;
	
	public Attachment(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract InputStream getBytes() throws FileNotFoundException, IOException;
	
	@Override
	public int compareTo(Attachment o) {
		return this.name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public void fireChangeEvent() {
		super.fireChangeEvent();
	}
	
	
}