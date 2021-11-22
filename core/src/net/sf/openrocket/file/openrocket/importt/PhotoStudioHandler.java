package net.sf.openrocket.file.openrocket.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.util.*;

/**
 * This class will parse the photostudio xml parameters from the OpenRocketDocument and
 * save it as a parameter map. This map can then later be used in the swing module for getting
 * or setting the PhotoSetting. (this cumbersome solution is done because of dependency reasons
 * between files of the core and swing module; trying to just use PhotoSettings objects in the
 * core module would have caused circular dependencies)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PhotoStudioHandler extends AbstractElementHandler {
    // The Photo Settings will be saved in the core module as a map of key values with corresponding content
    private final Map<String, String> p;
    private static final Logger log = LoggerFactory.getLogger(OpenRocketHandler.class);

    public PhotoStudioHandler(Map<String, String> p) {
        this.p = p;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        String[] params = new String[] {"roll", "yaw", "pitch", "advance", "viewAlt", "viewAz", "viewDistance", "fov",
            "lightAlt", "lightAz", "ambiance", "ambiance", "motionBlurred", "flame", "smoke", "smokeOpacity", "sparks",
            "exhaustScale", "flameAspectRatio", "sparkConcentration", "sparkWeight", "sky"};
        if (Arrays.asList(params).contains(element)) {
            p.put(element, content);
            return;
        }
        String[] colors = new String[] {"sunlight", "skyColor", "flameColor", "smokeColor"};
        if (Arrays.asList(colors).contains(element)) {
            p.put(element, getColor(attributes));
            return;
        }

        super.closeElement(element, attributes, content, warnings);
    }

    private String getColor(HashMap<String, String> attributes) {
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
        return red + " " + green + " " + blue + " " + alpha;
    }
}
