package info.openrocket.core.file.threemf.export;

import info.openrocket.core.logging.WarningSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildPlatePackerTest {
    @Test
    void layoutIsDeterministicAndNonOverlapping() {
        ThreeMFExportOptions options = new ThreeMFExportOptions();
        options.setBuildWidth(100);
        options.setBuildDepth(100);
        options.setPartSpacing(5);

        List<PrintablePart> first = BuildPlatePacker.pack(
                List.of(box("a", 20, 30, 10), box("b", 40, 10, 10), box("c", 15, 15, 10)),
                options, new WarningSet());
        List<PrintablePart> second = BuildPlatePacker.pack(
                List.of(box("a", 20, 30, 10), box("b", 40, 10, 10), box("c", 15, 15, 10)),
                options, new WarningSet());

        for (int index = 0; index < first.size(); index++) {
            assertEquals(first.get(index).getName(), second.get(index).getName());
            assertEquals(first.get(index).getPlacementX(), second.get(index).getPlacementX());
            assertEquals(first.get(index).getPlacementY(), second.get(index).getPlacementY());
        }
        for (int firstIndex = 0; firstIndex < first.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < first.size(); secondIndex++) {
                assertTrue(doNotOverlap(first.get(firstIndex), first.get(secondIndex)));
            }
        }
    }

    @Test
    void oversizedPartsRemainInPackageAndProduceWarnings() {
        ThreeMFExportOptions options = new ThreeMFExportOptions();
        options.setBuildWidth(20);
        options.setBuildDepth(20);
        options.setBuildHeight(20);
        WarningSet warnings = new WarningSet();
        List<PrintablePart> packed = BuildPlatePacker.pack(
                List.of(box("oversized", 30, 25, 30), box("small", 5, 5, 5)), options, warnings);

        assertEquals(2, packed.size());
        assertTrue(warnings.size() >= 2);
        assertTrue(packed.stream().anyMatch(part -> part.getPlacementX() > options.getBuildWidth()));
    }

    @Test
    void usesFirstAvailableShelfInsteadOfOnlyTheNewestShelf() {
        ThreeMFExportOptions options = new ThreeMFExportOptions();
        options.setBuildWidth(100);
        options.setBuildDepth(100);
        options.setPartSpacing(5);

        List<PrintablePart> packed = BuildPlatePacker.pack(
                List.of(box("a", 50, 30, 10), box("b", 50, 25, 10), box("c", 25, 20, 10)),
                options, new WarningSet());

        PrintablePart first = packed.stream().filter(part -> part.getName().equals("a")).findFirst().orElseThrow();
        PrintablePart third = packed.stream().filter(part -> part.getName().equals("c")).findFirst().orElseThrow();
        assertEquals(first.getPlacementY(), third.getPlacementY());
        assertTrue(doNotOverlap(first, third));
    }

    private static PrintablePart box(String name, double width, double depth, double height) {
        List<double[]> vertices = new ArrayList<>();
        vertices.add(new double[] {0, 0, 0});
        vertices.add(new double[] {width, 0, 0});
        vertices.add(new double[] {width, depth, 0});
        vertices.add(new double[] {0, depth, 0});
        vertices.add(new double[] {0, 0, height});
        vertices.add(new double[] {width, 0, height});
        vertices.add(new double[] {width, depth, height});
        vertices.add(new double[] {0, depth, height});
        return new PrintablePart(name, null, vertices, List.of());
    }

    private static boolean doNotOverlap(PrintablePart first, PrintablePart second) {
        return first.getPlacementX() + first.getWidth() <= second.getPlacementX()
                || second.getPlacementX() + second.getWidth() <= first.getPlacementX()
                || first.getPlacementY() + first.getDepth() <= second.getPlacementY()
                || second.getPlacementY() + second.getDepth() <= first.getPlacementY();
    }
}
