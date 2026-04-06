package info.openrocket.swing.file.photo;

import info.openrocket.core.file.openrocket.importt.OpenRocketHandler;
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
    private static final Logger log = LoggerFactory.getLogger(OpenRocketHandler.class);
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
            double roll = Double.parseDouble(content);
            p.setRoll(roll);
            return;
        }
        if ("yaw".equals(element)) {
            double yaw = Double.parseDouble(content);
            p.setYaw(yaw);
            return;
        }
        if ("pitch".equals(element)) {
            double pitch = Double.parseDouble(content);
            p.setPitch(pitch);
            return;
        }
        if ("advance".equals(element)) {
            double advance = Double.parseDouble(content);
            p.setAdvance(advance);
            return;
        }

        if ("viewAlt".equals(element)) {
            double viewAlt = Double.parseDouble(content);
            p.setViewAlt(viewAlt);
            return;
        }
        if ("viewAz".equals(element)) {
            double viewAz = Double.parseDouble(content);
            p.setViewAz(viewAz);
            return;
        }
        if ("viewDistance".equals(element)) {
            double viewDistance = Double.parseDouble(content);
            p.setViewDistance(viewDistance);
            return;
        }
        if ("fov".equals(element)) {
            double fov = Double.parseDouble(content);
            p.setFov(fov);
            return;
        }

        if ("lightAlt".equals(element)) {
            double lightAlt = Double.parseDouble(content);
            p.setLightAlt(lightAlt);
            return;
        }
        if ("lightAz".equals(element)) {
            double lightAz = Double.parseDouble(content);
            p.setLightAz(lightAz);
            return;
        }
        if ("sunlight".equals(element)) {
            ORColor sunlight = getColor(content);
            p.setSunlight(sunlight);
            return;
        }
        if ("ambiance".equals(element)) {
            double ambiance = Double.parseDouble(content);
            p.setAmbiance(ambiance);
            return;
        }

        if ("skyColor".equals(element)) {
            ORColor skyColor = getColor(content);
            p.setSkyColor(skyColor);
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
            ORColor color = getColor(content);
            if (color != null) p.setGradientTopColor(color);
            return;
        }
        if ("gradientBottomColor".equals(element)) {
            ORColor color = getColor(content);
            if (color != null) p.setGradientBottomColor(color);
            return;
        }

        if ("motionBlurred".equals(element)) {
            boolean motionBlurred = Boolean.parseBoolean(content);
            p.setMotionBlurred(motionBlurred);
            return;
        }
        if ("flame".equals(element)) {
            boolean flame = Boolean.parseBoolean(content);
            p.setFlame(flame);
            return;
        }
        if ("flameColor".equals(element)) {
            ORColor flameColor = getColor(content);
            p.setFlameColor(flameColor);
            return;
        }
        if ("smoke".equals(element)) {
            boolean smoke = Boolean.parseBoolean(content);
            p.setSmoke(smoke);
            return;
        }
        if ("smokeColor".equals(element)) {
            ORColor smokeColor = getColor(content);
            p.setSmokeColor(smokeColor);
            return;
        }
        if ("sparks".equals(element)) {
            boolean sparks = Boolean.parseBoolean(content);
            p.setSparks(sparks);
            return;
        }
        if ("exhaustScale".equals(element)) {
            double exhaustScale = Double.parseDouble(content);
            p.setExhaustScale(exhaustScale);
            return;
        }
        if ("flameAspectRatio".equals(element)) {
            double flameAspectRatio = Double.parseDouble(content);
            p.setFlameAspectRatio(flameAspectRatio);
            return;
        }

        if ("sparkConcentration".equals(element)) {
            double sparkConcentration = Double.parseDouble(content);
            p.setSparkConcentration(sparkConcentration);
            return;
        }
        if ("sparkWeight".equals(element)) {
            double sparkWeight = Double.parseDouble(content);
            p.setSparkWeight(sparkWeight);
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

    private ORColor getColor(String content) {
        String[] values = content.split(" ");
        if (values.length < 4) return null;

        int red = Integer.parseInt(values[0]);
        int green = Integer.parseInt(values[1]);
        int blue = Integer.parseInt(values[2]);
        int alpha = Integer.parseInt(values[3]);
        return new ORColor(red, green, blue, alpha);
    }
}
