package info.openrocket.swing.gui.choosers;

import info.openrocket.core.file.threemf.export.ThreeMFExportOptions;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeMFOptionChooserTest extends BaseTestCase {
    @Test
    void validatesDimensionsAndSpacingAtFieldLevel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ThreeMFOptionChooser chooser = new ThreeMFOptionChooser(new ThreeMFExportOptions());
            assertTrue(chooser.validateOptions());

            chooser.getBuildWidthField().setText("0");
            assertFalse(chooser.validateOptions());
            assertTrue(chooser.getBuildWidthField().getToolTipText().length() > 0);

            chooser.getBuildWidthField().setText("210");
            chooser.getPartSpacingField().setText("-1");
            assertFalse(chooser.validateOptions());

            chooser.getPartSpacingField().setText("4.5");
            assertTrue(chooser.validateOptions());
            assertEquals(210, chooser.getOptions().getBuildWidth());
            assertEquals(4.5, chooser.getOptions().getPartSpacing());
        });
    }
}
