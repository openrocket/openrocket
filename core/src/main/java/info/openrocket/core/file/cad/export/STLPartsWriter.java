package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes flat parts as one binary STL mesh (see {@link PartMesher}). Units are millimeters, which is what slicers
 * assume for STL files.
 */
final class STLPartsWriter {
	private STLPartsWriter() {
	}

	static void write(List<ExportedPart> parts, String title, OutputStream out) throws IOException {
		List<double[]> triangles = new ArrayList<>();
		for (ExportedPart placed : parts) {
			triangles.addAll(PartMesher.triangulate(placed.part()));
		}

		BufferedOutputStream stream = new BufferedOutputStream(out);
		byte[] header = new byte[80];
		// Binary STL headers must not start with "solid", which marks ASCII STL files
		byte[] text = ("OpenRocket parts: " + (title == null ? "" : title)).getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(text, 0, header, 0, Math.min(text.length, header.length));
		stream.write(header);

		ByteBuffer buffer = ByteBuffer.allocate(50).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(triangles.size());
		stream.write(buffer.array(), 0, 4);
		for (double[] t : triangles) {
			buffer.clear();
			double[] n = normal(t);
			buffer.putFloat((float) n[0]).putFloat((float) n[1]).putFloat((float) n[2]);
			for (double v : t) {
				buffer.putFloat((float) v);
			}
			buffer.putShort((short) 0);
			stream.write(buffer.array(), 0, 50);
		}
		stream.flush();
	}

	private static double[] normal(double[] t) {
		double ux = t[3] - t[0], uy = t[4] - t[1], uz = t[5] - t[2];
		double vx = t[6] - t[0], vy = t[7] - t[1], vz = t[8] - t[2];
		double nx = uy * vz - uz * vy;
		double ny = uz * vx - ux * vz;
		double nz = ux * vy - uy * vx;
		double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
		return length == 0 ? new double[]{0, 0, 0} : new double[]{nx / length, ny / length, nz / length};
	}
}
