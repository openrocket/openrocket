package net.sf.openrocket.file.photo;

import net.sf.openrocket.file.openrocket.importt.OpenRocketHandler;
import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.PhotoSettings;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.*;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Color;
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

    public PhotoStudioGetter(Map<String, String> par) {
        this.parameters = par;
        p = new PhotoSettings();
    }

    public PhotoSettings getPhotoSettings() {
        if (parameters != null) {
            for (String element : parameters.keySet()) {
                processElement(element, parameters.get(element));
            }
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
            Color sunlight = getColor(content);
            p.setSunlight(sunlight);
            return;
        }
        if ("ambiance".equals(element)) {
            double ambiance = Double.parseDouble(content);
            p.setAmbiance(ambiance);
            return;
        }

        if ("skyColor".equals(element)) {
            Color skyColor = getColor(content);
            p.setSkyColor(skyColor);
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
            Color flameColor = getColor(content);
            p.setFlameColor(flameColor);
            return;
        }
        if ("smoke".equals(element)) {
            boolean smoke = Boolean.parseBoolean(content);
            p.setSmoke(smoke);
            return;
        }
        if ("smokeColor".equals(element)) {
            Color smokeColor = getColor(content);
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
            if (content.equals("")) {     // Case where sky is null
                p.setSky(null);
                return;
            }
            Sky s = null;
            try {
                Class<?> cl = Class.forName(content);
                if (Mountains.class.isAssignableFrom(cl))
                    s = Mountains.instance;
                else if (Lake.class.isAssignableFrom(cl))
                    s = Lake.instance;
                else if (Meadow.class.isAssignableFrom(cl))
                    s = Meadow.instance;
                else if (Miramar.class.isAssignableFrom(cl))
                    s = Miramar.instance;
                else if (Orbit.class.isAssignableFrom(cl))
                    s = Orbit.instance;
                else if (Storm.class.isAssignableFrom(cl))
                    s = Storm.instance;
            }
            catch (ClassNotFoundException e) {
                log.info("Could not load sky class '" + content + "'.");
            }
            p.setSky(s);
        }
    }

    private Color getColor(String content) {
        String[] values = content.split(" ");
        if (values.length < 4) return null;

        int red = Integer.parseInt(values[0]);
        int green = Integer.parseInt(values[1]);
        int blue = Integer.parseInt(values[2]);
        int alpha = Integer.parseInt(values[3]);
        return new Color(red, green, blue, alpha);
    }
}
