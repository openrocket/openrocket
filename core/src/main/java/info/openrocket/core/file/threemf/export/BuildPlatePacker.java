package info.openrocket.core.file.threemf.export;

import info.openrocket.core.logging.WarningSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class BuildPlatePacker {
    private BuildPlatePacker() {
    }

    static List<PrintablePart> pack(List<PrintablePart> input, ThreeMFExportOptions options, WarningSet warnings) {
        List<PrintablePart> parts = new ArrayList<>(input);
        parts.sort(Comparator
                .comparingDouble((PrintablePart part) -> Math.max(part.getWidth(), part.getDepth())).reversed()
                .thenComparing(Comparator.comparingDouble(
                        (PrintablePart part) -> part.getWidth() * part.getDepth()).reversed())
                .thenComparing(PrintablePart::getName));

        double spacing = options.getPartSpacing();
        List<Shelf> shelves = new ArrayList<>();
        double nextShelfY = spacing;
        double overflowX = options.getBuildWidth() + spacing;
        boolean layoutOverflow = false;

        for (PrintablePart part : parts) {
            if (part.getHeight() > options.getBuildHeight()) {
                warnings.add(part.getName() + " exceeds the configured build height ("
                        + format(part.getHeight()) + " mm > " + format(options.getBuildHeight()) + " mm).");
            }

            boolean placed = false;
            for (Shelf shelf : shelves) {
                if (tryPlaceOnShelf(part, shelf, options, spacing)) {
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                orientForNewShelf(part, options, spacing);
                boolean fitsPlate = fitsX(part, spacing, options, spacing)
                        && nextShelfY + part.getDepth() + spacing <= options.getBuildDepth();
                if (fitsPlate) {
                    part.setPlacement(spacing, nextShelfY);
                    Shelf shelf = new Shelf(nextShelfY, part.getDepth(),
                            spacing + part.getWidth() + spacing);
                    shelves.add(shelf);
                    nextShelfY += shelf.depth + spacing;
                    placed = true;
                }
            }

            if (!placed) {
                part.setPlacement(overflowX, spacing);
                overflowX += part.getWidth() + spacing;
                layoutOverflow = true;
            }
        }

        if (layoutOverflow) {
            warnings.add("Some parts do not fit on the configured build plate. They were exported without overlap "
                    + "outside the plate boundary.");
        }
        return parts;
    }

    private static boolean tryPlaceOnShelf(PrintablePart part, Shelf shelf,
                                           ThreeMFExportOptions options, double spacing) {
        boolean fits = fitsX(part, shelf.cursorX, options, spacing)
                && part.getDepth() <= shelf.depth;
        boolean fitsRotated = fitsRotatedX(part, shelf.cursorX, options, spacing)
                && part.getWidth() <= shelf.depth;
        if (!fits && !fitsRotated) {
            return false;
        }
        if (!fits || fitsRotated && part.getDepth() > part.getWidth()) {
            part.rotateZ90();
        }
        part.setPlacement(shelf.cursorX, shelf.y);
        shelf.cursorX += part.getWidth() + spacing;
        return true;
    }

    private static void orientForNewShelf(PrintablePart part, ThreeMFExportOptions options, double spacing) {
        boolean fits = fitsX(part, spacing, options, spacing);
        boolean fitsRotated = fitsRotatedX(part, spacing, options, spacing);
        if (!fits && fitsRotated || fits && fitsRotated && part.getDepth() > part.getWidth()) {
            part.rotateZ90();
        }
    }

    private static boolean fitsX(PrintablePart part, double x, ThreeMFExportOptions options, double spacing) {
        return x + part.getWidth() + spacing <= options.getBuildWidth();
    }

    private static boolean fitsRotatedX(PrintablePart part, double x, ThreeMFExportOptions options, double spacing) {
        return x + part.getDepth() + spacing <= options.getBuildWidth();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static final class Shelf {
        private final double y;
        private final double depth;
        private double cursorX;

        private Shelf(double y, double depth, double cursorX) {
            this.y = y;
            this.depth = depth;
            this.cursorX = cursorX;
        }
    }
}
