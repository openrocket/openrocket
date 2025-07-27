package info.openrocket.core.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.openrocket.core.document.Attachment;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.FileUtils;

public abstract class AttachmentUtils {

	public static void exportAttachment(Attachment a, File outFile) throws IOException, DecalNotFoundException {
		InputStream is = a.getBytes();
		OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));

		FileUtils.copy(is, os);

		is.close();
		os.close();

	}
}
