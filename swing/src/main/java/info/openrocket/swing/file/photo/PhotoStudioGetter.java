package info.openrocket.swing.file.photo;

import info.openrocket.swing.gui.figure3d.photo.PhotoSettings;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Lake;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Meadow;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Miramar;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Mountains;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Orbit;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Storm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.DoubleConsumer;

/**
 * This class takes in the PhotoSetting map from the core module and converts it
 * to a PhotoSettings object that can be used withing the swing module.
 * (this cumbersome solution is done because of dependency reasons
 * between files of the core and swing module; trying to just use PhotoSettings objects in the
 * core module would have caused circular dependencies)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PhotoStudioGetter {
    private PhotoSettings p = null;
    private Map<String, String> parameters = null;
    private static final Logger log = LoggerFactory.getLogger(PhotoStudioGetter.class);
    private boolean backgroundTypeExplicitlySet = false;

    public PhotoStudioGetter(Map<String, String> par) {
        this.parameters = par;
        p = new PhotoSettings();
    }

    public PhotoSettings getPhotoSettings() {
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                processElement(entry.getKey(), entry.getValue());
            }
        }
        // Backward compat: files saved before backgroundType was introduced have only <sky>.
        // Infer the background type from whether a sky texture was present.
        if (!backgroundTypeExplicitlySet) {
            p.setBackgroundType(p.getSky() != null
                    ? PhotoSettings.BackgroundType.TEXTURE
                    : PhotoSettings.BackgroundType.SOLID_COLOR);
        }
        return p;
    }

    private void processElement(String element, String content) {
        if ("roll".equals(element)) {
            setDouble(element, content, p::setRoll);
            return;
        }
        if ("yaw".equals(element)) {
            setDouble(element, content, p::setYaw);
            return;
        }
        if ("pitch".equals(element)) {
            setDouble(element, content, p::setPitch);
            return;
        }
        if ("advance".equals(element)) {
            setDouble(element, content, p::setAdvance);
            return;
        }

        if ("viewAlt".equals(element)) {
            setDouble(element, content, p::setViewAlt);
            return;
        }
        if ("viewAz".equals(element)) {
            setDouble(element, content, p::setViewAz);
            return;
        }
        if ("viewDistance".equals(element)) {
            setDouble(element, content, p::setViewDistance);
            return;
        }
        if ("fov".equals(element)) {
            setDouble(element, content, p::setFov);
            return;
        }

        if ("lightAlt".equals(element)) {
            setDouble(element, content, p::setLightAlt);
            return;
        }
        if ("lightAz".equals(element)) {
            setDouble(element, content, p::setLightAz);
            return;
        }
        if ("sunlight".equals(element)) {
            ORColor sunlight = getColor(element, content);
            if (sunlight != null) p.setSunlight(sunlight);
            return;
        }
        if ("lightStrength".equals(element)) {
            setDouble(element, content, p::setLightStrength);
            return;
        }
        if ("ambiance".equals(element)) {
            setDouble(element, content, p::setAmbiance);
            return;
        }

        if ("skyColor".equals(element)) {
            ORColor skyColor = getColor(element, content);
            if (skyColor != null) p.setSkyColor(skyColor);
            return;
        }
        if ("backgroundType".equals(element)) {
            try {
                p.setBackgroundType(PhotoSettings.BackgroundType.valueOf(content));
                backgroundTypeExplicitlySet = true;
            } catch (IllegalArgumentException e) {
                log.warn("Unknown backgroundType '{}', using default.", content);
            }
            return;
        }
        if ("gradientTopColor".equals(element)) {
            ORColor color = getColor(element, content);
            if (color != null) p.setGradientTopColor(color);
            return;
        }
        if ("gradientBottomColor".equals(element)) {
            ORColor color = getColor(element, content);
            if (color != null) p.setGradientBottomColor(color);
            return;
        }

        if ("motionBlurred".equals(element)) {
            boolean motionBlurred = Boolean.parseBoolean(content);
            p.setMotionBlurred(motionBlurred);
            return;
        }
        if ("motionBlurAmount".equals(element)) {
            setDouble(element, content, p::setMotionBlurAmount);
            return;
        }
        if ("flame".equals(element)) {
            boolean flame = Boolean.parseBoolean(content);
            p.setFlame(flame);
            return;
        }
        if ("flameColor".equals(element)) {
            ORColor flameColor = getColor(element, content);
            if (flameColor != null) p.setFlameColor(flameColor);
            return;
        }
        if ("smoke".equals(element)) {
            boolean smoke = Boolean.parseBoolean(content);
            p.setSmoke(smoke);
            return;
        }
        if ("smokeColor".equals(element)) {
            ORColor smokeColor = getColor(element, content);
            if (smokeColor != null) p.setSmokeColor(smokeColor);
            return;
        }
        if ("sparks".equals(element)) {
            boolean sparks = Boolean.parseBoolean(content);
            p.setSparks(sparks);
            return;
        }
        if ("exhaustScale".equals(element)) {
            setDouble(element, content, p::setExhaustScale);
            return;
        }
        if ("flameAspectRatio".equals(element)) {
            setDouble(element, content, p::setFlameAspectRatio);
            return;
        }

        if ("sparkConcentration".equals(element)) {
            setDouble(element, content, p::setSparkConcentration);
            return;
        }
        if ("sparkWeight".equals(element)) {
            setDouble(element, content, p::setSparkWeight);
            return;
        }

        if ("sky".equals(element)) {
            if (content.isEmpty()) {     // Case where sky is null
                p.setSky(null);
                return;
            }
            p.setSky(resolveSky(content));
        }
    }

    private Sky resolveSky(String content) {
        String simpleName = content.substring(content.lastIndexOf('.') + 1);
        return switch (simpleName) {
            case "Mountains" -> Mountains.instance;
            case "Lake" -> Lake.instance;
            case "Meadow" -> Meadow.instance;
            case "Miramar" -> Miramar.instance;
            case "Orbit" -> Orbit.instance;
            case "Storm" -> Storm.instance;
            default -> {
                log.info("Could not load sky class '{}'.", content);
                yield null;
            }
        };
    }

    private ORColor getColor(String element, String content) {
        try {
            String[] values = content.trim().split("\\s+");
            if (values.length != 4) {
                throw new IllegalArgumentException("expected four color channels");
            }

            int red = Integer.parseInt(values[0]);
            int green = Integer.parseInt(values[1]);
            int blue = Integer.parseInt(values[2]);
            int alpha = Integer.parseInt(values[3]);
            if (red < 0 || red > 255 || green < 0 || green > 255
                    || blue < 0 || blue > 255 || alpha < 0 || alpha > 255) {
                throw new IllegalArgumentException("color channel outside 0..255");
            }
            return new ORColor(red, green, blue, alpha);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid Photo Studio value for '{}': '{}'; using the default.", element, content);
            return null;
        }
    }

    private void setDouble(String element, String content, DoubleConsumer setter) {
        Double value = parseDouble(element, content);
        if (value != null) setter.accept(value);
    }

    private Double parseDouble(String element, String content) {
        try {
            double value = Double.parseDouble(content);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("non-finite value");
            }
            return value;
        } catch (NumberFormatException | NullPointerException e) {
            log.warn("Invalid Photo Studio value for '{}': '{}'; using the default.", element, content);
            return null;
        }
    }
}
