package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes flat parts as a 3MF file (3D Manufacturing Format), the native format of most slicers. Each part becomes
 * a separate named object, so slicers can arrange the parts on the build plate. Units are millimeters.
 */
final class ThreeMFPartsWriter {
	static final String MODEL_PATH = "3D/3dmodel.model";

	private ThreeMFPartsWriter() {
	}

	static void write(List<ExportedPart> parts, String title, OutputStream out) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(out);
		Writer writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);

		zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
				" <Default Extension=\"rels\" " +
				"ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
				" <Default Extension=\"model\" " +
				"ContentType=\"application/vnd.ms-package.3dmanufacturing-3dmodel+xml\"/>\n" +
				"</Types>\n");
		writer.flush();
		zip.closeEntry();

		zip.putNextEntry(new ZipEntry("_rels/.rels"));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
				" <Relationship Target=\"/" + MODEL_PATH + "\" Id=\"rel0\" " +
				"Type=\"http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel\"/>\n" +
				"</Relationships>\n");
		writer.flush();
		zip.closeEntry();

		zip.putNextEntry(new ZipEntry(MODEL_PATH));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<model unit=\"millimeter\" xml:lang=\"en-US\" " +
				"xmlns=\"http://schemas.microsoft.com/3dmanufacturing/core/2015/02\">\n");
		writer.write(" <metadata name=\"Title\">" + escape(title == null ? "" : title) + "</metadata>\n");
		writer.write(" <metadata name=\"Application\">OpenRocket</metadata>\n");
		writer.write(" <resources>\n");
		for (int i = 0; i < parts.size(); i++) {
			writeObject(writer, i + 1, parts.get(i));
		}
		writer.write(" </resources>\n <build>\n");
		for (int i = 0; i < parts.size(); i++) {
			ExportedPart part = parts.get(i);
			// The mesh of each object starts at its own origin; the build item places it on the plate
			writer.write("  <item objectid=\"" + (i + 1) + "\" transform=\"1 0 0 0 1 0 0 0 1 " +
					number(part.minXmm()) + " " + number(part.minYmm()) + " 0\"/>\n");
		}
		writer.write(" </build>\n</model>\n");
		writer.flush();
		zip.closeEntry();
		zip.finish();
	}

	private static void writeObject(Writer writer, int id, ExportedPart part) throws IOException {
		// Vertices are shared by their written coordinates, so neighboring triangles use the same vertex
		Map<String, Integer> indices = new HashMap<>();
		StringBuilder vertices = new StringBuilder();
		StringBuilder triangles = new StringBuilder();
		for (double[] t : PartMesher.triangulate(part.part())) {
			int[] v = new int[3];
			for (int k = 0; k < 3; k++) {
				String xyz = "x=\"" + number(t[3 * k] - part.minXmm()) + "\" y=\"" +
						number(t[3 * k + 1] - part.minYmm()) + "\" z=\"" + number(t[3 * k + 2]) + "\"";
				Integer index = indices.get(xyz);
				if (index == null) {
					index = indices.size();
					indices.put(xyz, index);
					vertices.append("    <vertex ").append(xyz).append("/>\n");
				}
				v[k] = index;
			}
			if (v[0] != v[1] && v[1] != v[2] && v[0] != v[2]) {
				triangles.append("    <triangle v1=\"").append(v[0]).append("\" v2=\"").append(v[1])
						.append("\" v3=\"").append(v[2]).append("\"/>\n");
			}
		}

		writer.write("  <object id=\"" + id + "\" type=\"model\" name=\"" + escape(part.name()) + "\">\n");
		writer.write("   <mesh>\n   <vertices>\n");
		writer.write(vertices.toString());
		writer.write("   </vertices>\n   <triangles>\n");
		writer.write(triangles.toString());
		writer.write("   </triangles>\n   </mesh>\n  </object>\n");
	}

	private static String number(double value) {
		String s = String.format(Locale.ROOT, "%.6f", value);
		return "-0.000000".equals(s) ? "0.000000" : s;
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
