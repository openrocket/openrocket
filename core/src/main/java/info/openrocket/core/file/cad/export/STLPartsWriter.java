package info.openrocket.core.file.cad.export;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Writes flat parts as one binary STL mesh (see {@link PartMesher}). Units are millimeters, which is what slicers
 * assume for STL files.
 * <p>
 * A binary STL file has no units, names or structure beyond a list of triangles. All numbers are little-endian:
 * <pre>
 * UINT8[80]    header, free text
 * UINT32       number of triangles
 * per triangle, 50 bytes:
 *     REAL32[3]    normal vector
 *     REAL32[3]    vertex 1
 *     REAL32[3]    vertex 2
 *     REAL32[3]    vertex 3
 *     UINT16       attribute byte count, 0
 * </pre>
 * The vertices are listed counter-clockwise seen from outside the part, and the normal points outwards.
 * See <a href="https://en.wikipedia.org/wiki/STL_(file_format)#Binary">STL (file format)</a> and the original
 * 3D Systems specification, <i>StereoLithography Interface Specification</i> (1989).
 */
final class STLPartsWriter {
	private static final int HEADER_BYTES = 80;
	private static final int TRIANGLE_BYTES = 50;

	private STLPartsWriter() {
	}

	/**
	 * Writes the parts as one binary STL file.
	 *
	 * @param parts  the parts to write, at their positions in the layout
	 * @param title  the text of the header, e.g. the rocket name; may be null
	 * @param out    the stream to write to; it is flushed but not closed
	 */
	static void write(List<ExportedPart> parts, String title, OutputStream out) throws IOException {
		List<double[]> triangles = new ArrayList<>();
		for (ExportedPart placed : parts) {
			triangles.addAll(PartMesher.triangulate(placed.part()));
		}

		BufferedOutputStream stream = new BufferedOutputStream(out);
		writeHeader(stream, title);
		writeTriangleCount(stream, triangles.size());
		ByteBuffer buffer = ByteBuffer.allocate(TRIANGLE_BYTES).order(ByteOrder.LITTLE_ENDIAN);
		for (double[] triangle : triangles) {
			writeTriangle(stream, buffer, triangle);
		}
		stream.flush();
	}

	/**
	 * Writes the 80-byte header: the title in ASCII, cut at 80 bytes and padded with zero bytes.
	 *
	 * @param out    the stream to write to
	 * @param title  the title; may be null
	 */
	private static void writeHeader(OutputStream out, String title) throws IOException {
		byte[] header = new byte[HEADER_BYTES];
		byte[] headerText = headerText(title).getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(headerText, 0, header, 0, Math.min(headerText.length, HEADER_BYTES));
		out.write(header);
	}

	/**
	 * Returns the text of the header. Readers treat a file whose first bytes are "solid" as an ASCII STL file, so a
	 * title that starts with "solid" gets an "OpenRocket: " prefix.
	 *
	 * @param title  the title; may be null
	 * @return the title, the prefixed title, or "OpenRocket" when the title is empty
	 */
	static String headerText(String title) {
		if (title == null || title.isBlank()) {
			return "OpenRocket";
		}
		if (title.strip().toLowerCase(Locale.ROOT).startsWith("solid")) {
			return "OpenRocket: " + title;
		}
		return title;
	}

	/**
	 * Writes the number of triangles as an unsigned 32-bit integer.
	 *
	 * @param out    the stream to write to
	 * @param count  the number of triangles that follow
	 */
	private static void writeTriangleCount(OutputStream out, int count) throws IOException {
		out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(count).array());
	}

	/**
	 * Writes one triangle: its normal, its three vertices and a zero attribute byte count, 50 bytes in all.
	 *
	 * @param out       the stream to write to
	 * @param buffer    a little-endian buffer of 50 bytes, reused between triangles
	 * @param triangle  the vertices as x1,y1,z1, x2,y2,z2, x3,y3,z3 in mm, counter-clockwise seen from outside
	 */
	private static void writeTriangle(OutputStream out, ByteBuffer buffer, double[] triangle) throws IOException {
		buffer.clear();
		double[] normal = normal(triangle);
		buffer.putFloat((float) normal[0]).putFloat((float) normal[1]).putFloat((float) normal[2]);
		for (double coordinate : triangle) {
			buffer.putFloat((float) coordinate);
		}
		buffer.putShort((short) 0);
		out.write(buffer.array(), 0, TRIANGLE_BYTES);
	}

	/**
	 * Returns the unit normal of a triangle, the cross product (v2 - v1) x (v3 - v1). By the right-hand rule it points
	 * to the side from which the vertices are counter-clockwise, i.e. out of the part. Readers mostly recompute the
	 * normal from the vertex order, so the vertex order is what matters.
	 *
	 * @param triangle  the vertices as x1,y1,z1, x2,y2,z2, x3,y3,z3
	 * @return the unit normal, or 0,0,0 for a degenerate triangle
	 */
	private static double[] normal(double[] triangle) {
		double ux = triangle[3] - triangle[0], uy = triangle[4] - triangle[1], uz = triangle[5] - triangle[2];
		double vx = triangle[6] - triangle[0], vy = triangle[7] - triangle[1], vz = triangle[8] - triangle[2];
		double nx = uy * vz - uz * vy;
		double ny = uz * vx - ux * vz;
		double nz = ux * vy - uy * vx;
		double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
		return length == 0 ? new double[]{0, 0, 0} : new double[]{nx / length, ny / length, nz / length};
	}
}
