package net.sf.openrocket.appearance.defaults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;

import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.Attachment;


class ResourceDecalImage implements DecalImage {
	
	final String resource;
	
	ResourceDecalImage(final String resource) {
		this.resource = resource;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getName() {
		return resource;
	}
	
	@Override
	public InputStream getBytes() throws FileNotFoundException, IOException {
		return this.getClass().getResourceAsStream(resource);
	}
	
	@Override
	public void exportImage(File file, boolean watchForChanges) throws IOException {
		
	}
	
	@Override
	public int compareTo(Attachment a) {
		return this.hashCode() - a.hashCode();
	}
	
	@Override
	public void addChangeListener(EventListener listener) {
		//Unimplemented, this can not change
	}
	
	@Override
	public void removeChangeListener(EventListener listener) {
		//Unimplemented, this can not change
	}
	
}
