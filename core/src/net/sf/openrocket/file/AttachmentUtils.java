package net.sf.openrocket.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.util.FileUtils;

public abstract class AttachmentUtils {
	
	public static void exportAttachment(Attachment a, File outFile) throws IOException {
		InputStream is = a.getBytes();
		OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
		
		FileUtils.copy(is, os);
		
		is.close();
		os.close();
		
	}
}
