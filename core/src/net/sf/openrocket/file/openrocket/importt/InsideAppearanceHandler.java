package net.sf.openrocket.file.openrocket.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.InsideColorComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class InsideAppearanceHandler extends AppearanceHandler {
    public InsideAppearanceHandler(RocketComponent component, DocumentLoadingContext context) {
        super(component, context);
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        // TODO: delete 'edgesSameAsInside' when backward compatibility with 22.02.beta.01-22.02.beta.05 is not needed anymore
        if ("edgessameasinside".equals(element) || "edgesSameAsInside".equals(element)) {
            boolean edgesSameAsInside = Boolean.parseBoolean(content);
            if (component instanceof InsideColorComponent)
                ((InsideColorComponent)component).getInsideColorComponentHandler().setEdgesSameAsInside(edgesSameAsInside);
            return;
        }
        // TODO: delete 'insideSameAsOutside' when backward compatibility with 22.02.beta.01-22.02.beta.05 is not needed anymore
        if ("insidesameasoutside".equals(element) || "insideSameAsOutside".equals(element)) {
            boolean insideSameAsOutside = Boolean.parseBoolean(content);
            if (component instanceof InsideColorComponent)
                ((InsideColorComponent)component).getInsideColorComponentHandler().setSeparateInsideOutside(insideSameAsOutside);
            return;
        }

        super.closeElement(element, attributes, content, warnings);
    }

    @Override
    protected void setAppearance() {
        if ((component instanceof InsideColorComponent))
            ((InsideColorComponent)component).getInsideColorComponentHandler().setInsideAppearance(builder.getAppearance());
    }
}
