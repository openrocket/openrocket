package net.sf.openrocket.appearance.defaults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.util.StateChangeListener;


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
	public void exportImage(File file) throws IOException {
	}
		
	@Override
	public void fireChangeEvent(Object source) {
	}
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		//Unimplemented, this can not change
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		//Unimplemented, this can not change
	}
	
	@Override
	public int compareTo(DecalImage o) {
		return getName().compareTo(o.getName());
	}
	
}
