package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FinSet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Exports flat parts (fins and centering rings) to CAD files at 1:1 scale in millimeters.
 * <p>
 * The parts are laid out side by side in the XY plane. 3D formats extrude each part from z = 0 to its
 * material thickness. The returned {@link ExportedPart}s describe where each part was placed, so the written
 * file can be checked with {@link CADDimensionVerifier}.
 */
public class CADPartsExporter {
	private static final double PART_SPACING_MM = 10;
	private static final double MIN_ROW_WIDTH_MM = 350;
	/** Space below each part that is reserved for its label in 2D formats. */
	static final double LABEL_HEIGHT_MM = 3;
	static final double LABEL_GAP_MM = 2;

	/**
	 * The file formats that parts can be exported to.
	 */
	public enum Format {
		/** 2D drawing for laser cutters, CNC routers and 2D CAD */
		DXF("dxf"),
		/** 3D solid model (ISO 10303-21, AP214) for CAD programs */
		STEP("step");

		private final String extension;

		Format(String extension) {
			this.extension = extension;
		}

		/**
		 * @return the file extension of the format, without the dot
		 */
		public String getExtension() {
			return extension;
		}
	}

	/**
	 * A part as written to the file.
	 *
	 * @param part the part, moved to its location in the file
	 */
	public record ExportedPart(FlatPart part, double minXmm, double minYmm, double maxXmm, double maxYmm) {
		/**
		 * @return the name of the part
		 */
		public String name() {
			return part.getName();
		}
	}

	private final List<FlatPart> parts = new ArrayList<>();

	/**
	 * Adds a single fin of a fin set, including its tab.
	 */
	public void addFinSet(FinSet finSet) {
		parts.add(FlatPart.fromFinSet(finSet));
	}

	/**
	 * Adds a centering ring.
	 *
	 * @param holes additional holes, e.g. for motor mounts (see {@link RingSvgExporter#holesFromMotorMounts})
	 */
	public void addCenteringRing(CenteringRing ring, Collection<RingSvgExporter.Hole> holes) {
		parts.add(FlatPart.fromCenteringRing(ring, holes));
	}

	/**
	 * Lays out all added parts and writes them in the given format.
	 *
	 * @return the written parts with their location in the file
	 * @throws IllegalStateException if a 3D format is requested for a part without thickness
	 */
	public List<ExportedPart> write(Format format, String title, OutputStream out) throws IOException {
		if (format != Format.DXF) {
			for (FlatPart part : parts) {
				if (!(part.getThicknessMm() > 0)) {
					throw new IllegalStateException("Part '" + part.getName() + "' has no thickness");
				}
				part.validateSolid();
			}
		}

		List<ExportedPart> placed = layout();
		switch (format) {
			case DXF -> DXFPartsWriter.write(placed, out);
			case STEP -> STEPPartsWriter.write(placed, title, out);
		}
		return placed;
	}

	/**
	 * Places the parts left to right in rows, with rows going downwards and room for a label below each part.
	 * The parts end up at positive coordinates, starting at the origin.
	 */
	private List<ExportedPart> layout() {
		double rowWidth = MIN_ROW_WIDTH_MM;
		for (FlatPart part : parts) {
			double[] b = part.getBounds();
			rowWidth = Math.max(rowWidth, b[2] - b[0]);
		}

		List<ExportedPart> result = new ArrayList<>();
		double cursorX = 0;
		double rowTop = 0;
		double rowHeight = 0;
		for (FlatPart part : parts) {
			double[] b = part.getBounds();
			double width = b[2] - b[0];
			double height = b[3] - b[1];
			if (cursorX > 0 && cursorX + width > rowWidth) {
				cursorX = 0;
				rowTop -= rowHeight + PART_SPACING_MM;
				rowHeight = 0;
			}

			double dx = cursorX - b[0];
			double dy = rowTop - b[3];
			result.add(new ExportedPart(part.translated(dx, dy), cursorX, rowTop - height, cursorX + width, rowTop));

			cursorX += width + PART_SPACING_MM;
			rowHeight = Math.max(rowHeight, height + LABEL_GAP_MM + LABEL_HEIGHT_MM);
		}

		// Move the rows up so the lowest part starts at y = 0
		double minY = 0;
		for (ExportedPart part : result) {
			minY = Math.min(minY, part.minYmm());
		}
		List<ExportedPart> shifted = new ArrayList<>();
		for (ExportedPart part : result) {
			shifted.add(new ExportedPart(part.part().translated(0, -minY), part.minXmm(), part.minYmm() - minY,
					part.maxXmm(), part.maxYmm() - minY));
		}
		return shifted;
	}
}
