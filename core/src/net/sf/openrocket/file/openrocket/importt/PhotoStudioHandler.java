package net.sf.openrocket.file.openrocket.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.PhotoSettings;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky;
import net.sf.openrocket.gui.figure3d.photo.sky.SkyBox;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.*;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * File import handler for PhotoSettings
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PhotoStudioHandler extends AbstractElementHandler {
    private PhotoSettings p;
    private static final Logger log = LoggerFactory.getLogger(OpenRocketHandler.class);

    public PhotoStudioHandler(PhotoSettings p) {
        this.p = p;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
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
            Color sunlight = getColor(attributes);
            p.setSunlight(sunlight);
            return;
        }
        if ("ambiance".equals(element)) {
            double ambiance = Double.parseDouble(content);
            p.setAmbiance(ambiance);
            return;
        }

        if ("skyColor".equals(element)) {
            Color skyColor = getColor(attributes);
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
            Color flameColor = getColor(attributes);
            p.setFlameColor(flameColor);
            return;
        }
        if ("smoke".equals(element)) {
            boolean smoke = Boolean.parseBoolean(content);
            p.setSmoke(smoke);
            return;
        }
        if ("smokeColor".equals(element)) {
            Color smokeColor = getColor(attributes);
            p.setSmokeColor(smokeColor);
            return;
        }
        if ("smokeOpacity".equals(element)) {
            double smokeOpacity = Double.parseDouble(content);
            p.setSmokeOpacity(smokeOpacity);
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
                else {
                    // Case where sky is a dummy sky, displaying <none>
                    s = new Sky() {
                        @Override
                        public void draw(com.jogamp.opengl.GL2 gl, TextureCache cache) { }

                        @Override
                        public String toString() {
                            return Application.getTranslator().get("DecalModel.lbl.select");
                        }
                    };
                }
            }
            catch (ClassNotFoundException e) {
                log.info("Could not load sky class '" + content + "'.");
            }
            p.setSky(s);
            return;
        }

        super.closeElement(element, attributes, content, warnings);
    }

    private Color getColor(HashMap<String, String> attributes) {
        int red = Integer.parseInt(attributes.get("red"));
        int green = Integer.parseInt(attributes.get("green"));
        int blue = Integer.parseInt(attributes.get("blue"));
        int alpha = 255;//set default
        // add a test if "alpha" was added to the XML / backwards compatibility
        String a = attributes.get("alpha");
        if (a != null){
            // "alpha" string was present so load the value
            alpha = Integer.parseInt(a);
        }
        return new Color(red, green, blue, alpha);
    }
}
