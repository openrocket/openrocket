package net.sf.openrocket.appearance;

import java.io.File;
import java.io.IOException;

import net.sf.openrocket.document.Attachment;

public interface DecalImage extends Attachment {

	public void exportImage( File file, boolean watchForChanges ) throws IOException;
	
}
