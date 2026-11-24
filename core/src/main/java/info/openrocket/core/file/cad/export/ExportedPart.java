package info.openrocket.core.file.cad.export;

/**
 * A part as written to the file by {@link CADPartsExporter}, with the bounding box it was placed in, in mm.
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
