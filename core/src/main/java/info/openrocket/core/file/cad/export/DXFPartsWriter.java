package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Loop;
import info.openrocket.core.file.cad.export.FlatPart.Polygon;
import info.openrocket.core.file.cad.export.FlatPart.Region;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes flat parts as a 2D DXF drawing. Outlines to cut are placed on the {@link #LAYER_CUT} layer, center marks
 * on {@link #LAYER_MARK} and part names on {@link #LAYER_LABEL}, so that CAM / laser software can treat them
 * separately.
 */
final class DXFPartsWriter {
	static final String LAYER_CUT = "CUT";
	static final String LAYER_MARK = "MARK";
	static final String LAYER_LABEL = "LABEL";

	private static final double MAX_MARK_HALF_LENGTH_MM = 1;

	private DXFPartsWriter() {
	}

	static void write(List<ExportedPart> parts, OutputStream out) throws IOException {
		DXFWriter writer = new DXFWriter();
		writer.addLayer(LAYER_CUT, DXFWriter.COLOR_WHITE);
		writer.addLayer(LAYER_MARK, DXFWriter.COLOR_RED);
		writer.addLayer(LAYER_LABEL, DXFWriter.COLOR_BLUE);

		for (ExportedPart placed : parts) {
			// Concentric circles (e.g. a ring and its bore) share one center mark
			List<double[]> marks = new ArrayList<>();
			for (Region region : placed.part().getRegions()) {
				addLoop(writer, region.outer(), marks);
				for (Loop hole : region.holes()) {
					addLoop(writer, hole, marks);
				}
			}
			String name = placed.name();
			if (!name.isBlank()) {
				writer.addCenteredText(LAYER_LABEL, (placed.minXmm() + placed.maxXmm()) / 2,
						placed.minYmm() - CADPartsExporter.LABEL_GAP_MM - CADPartsExporter.LABEL_HEIGHT_MM,
						CADPartsExporter.LABEL_HEIGHT_MM, name);
			}
		}

		writer.write(out);
	}

	private static void addLoop(DXFWriter writer, Loop loop, List<double[]> marks) {
		if (loop instanceof Circle c) {
			writer.addCircle(LAYER_CUT, c.cx(), c.cy(), c.r());
			addCenterMark(writer, c.cx(), c.cy(), c.r(), marks);
		} else if (loop instanceof ArcLoop arcs) {
			for (Arc arc : arcs.arcs()) {
				// DXF arcs run counter-clockwise, so the clockwise arc is written from its end to its start
				writer.addArc(LAYER_CUT, arc.cx(), arc.cy(), arc.r(),
						Math.toDegrees(arc.endAngle()), Math.toDegrees(arc.startAngle()));
				addCenterMark(writer, arc.cx(), arc.cy(), arc.r(), marks);
			}
		} else {
			Polygon p = (Polygon) loop;
			writer.addClosedPolyline(LAYER_CUT, p.xs(), p.ys());
		}
	}

	private static void addCenterMark(DXFWriter writer, double x, double y, double r, List<double[]> marks) {
		for (double[] mark : marks) {
			if (Math.hypot(mark[0] - x, mark[1] - y) < 1e-6) {
				return;
			}
		}
		marks.add(new double[]{x, y});
		double half = Math.min(MAX_MARK_HALF_LENGTH_MM, r / 2);
		writer.addLine(LAYER_MARK, x - half, y, x + half, y);
		writer.addLine(LAYER_MARK, x, y - half, x, y + half);
	}
}
