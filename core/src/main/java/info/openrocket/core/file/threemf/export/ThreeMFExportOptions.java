package info.openrocket.core.file.threemf.export;

/**
 * Options for exporting independently printable component instances in a 3MF package.
 */
public class ThreeMFExportOptions {
    public static final double DEFAULT_BUILD_WIDTH = 220.0;
    public static final double DEFAULT_BUILD_DEPTH = 220.0;
    public static final double DEFAULT_BUILD_HEIGHT = 250.0;
    public static final double DEFAULT_PART_SPACING = 5.0;

    private boolean exportChildren = true;
    private boolean autoOrient = true;
    private double buildWidth = DEFAULT_BUILD_WIDTH;
    private double buildDepth = DEFAULT_BUILD_DEPTH;
    private double buildHeight = DEFAULT_BUILD_HEIGHT;
    private double partSpacing = DEFAULT_PART_SPACING;

    public ThreeMFExportOptions() {
    }

    public ThreeMFExportOptions(ThreeMFExportOptions source) {
        this.exportChildren = source.exportChildren;
        this.autoOrient = source.autoOrient;
        this.buildWidth = source.buildWidth;
        this.buildDepth = source.buildDepth;
        this.buildHeight = source.buildHeight;
        this.partSpacing = source.partSpacing;
    }

    public boolean isExportChildren() {
        return exportChildren;
    }

    public void setExportChildren(boolean exportChildren) {
        this.exportChildren = exportChildren;
    }

    public boolean isAutoOrient() {
        return autoOrient;
    }

    public void setAutoOrient(boolean autoOrient) {
        this.autoOrient = autoOrient;
    }

    public double getBuildWidth() {
        return buildWidth;
    }

    public void setBuildWidth(double buildWidth) {
        this.buildWidth = buildWidth;
    }

    public double getBuildDepth() {
        return buildDepth;
    }

    public void setBuildDepth(double buildDepth) {
        this.buildDepth = buildDepth;
    }

    public double getBuildHeight() {
        return buildHeight;
    }

    public void setBuildHeight(double buildHeight) {
        this.buildHeight = buildHeight;
    }

    public double getPartSpacing() {
        return partSpacing;
    }

    public void setPartSpacing(double partSpacing) {
        this.partSpacing = partSpacing;
    }

    public boolean isValid() {
        return Double.isFinite(buildWidth) && buildWidth > 0
                && Double.isFinite(buildDepth) && buildDepth > 0
                && Double.isFinite(buildHeight) && buildHeight > 0
                && Double.isFinite(partSpacing) && partSpacing >= 0;
    }
}
