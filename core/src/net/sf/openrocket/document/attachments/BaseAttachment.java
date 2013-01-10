package net.sf.openrocket.document.attachments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.document.Attachment;

public abstract class BaseAttachment implements Attachment {
	
	private final String name;
	
	public BaseAttachment(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public final String getName() {
		return name;
	}
	
	@Override
	public abstract InputStream getBytes() throws FileNotFoundException, IOException;
	
	@Override
	public int compareTo(Attachment o) {
		if (!(o instanceof BaseAttachment)) {
			return -1;
		}
		return this.name.compareTo(((BaseAttachment) o).name);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	
}
