package info.openrocket.swing.gui.choosers;

import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.OBJExportOptions;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OBJOptionChooserTest extends BaseTestCase {
    @Test
    void blenderPresetUsesBlenderCompatibleOptions() {
        OBJExportOptions options = new OBJExportOptions(new Rocket());
        options.setExportMotors(false);
        options.setExportAppearance(false);
        options.setUseSRGB(false);
        options.setScaling(20);
        options.setTriangulate(true);
        options.setLOD(ObjUtils.LevelOfDetail.LOW_QUALITY);

        OBJOptionChooser.applyBlenderPreset(options);

        assertTrue(options.isExportMotors());
        assertTrue(options.isExportAppearance());
        assertTrue(options.isUseSRGB());
        assertEquals(1, options.getScaling());
        assertFalse(options.isTriangulate());
        assertEquals(ObjUtils.LevelOfDetail.NORMAL_QUALITY, options.getLOD());
        assertTrue(OBJOptionChooser.isOptimizedForBlender(options));
    }

    @Test
    void blenderPresetMatcherRejectsChangedColorSpace() {
        OBJExportOptions options = new OBJExportOptions(new Rocket());
        OBJOptionChooser.applyBlenderPreset(options);
        options.setUseSRGB(false);

        assertFalse(OBJOptionChooser.isOptimizedForBlender(options));
    }
}
