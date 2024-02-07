package net.sf.openrocket.file.wavefrontobj;

public abstract class TriangulationHelper {
	public static DefaultObj simpleTriangulate(DefaultObj obj) {
		return de.javagl.obj.ObjUtils.triangulate(obj, new DefaultObj());
	}
}
