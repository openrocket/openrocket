package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.appearance.AppearanceBuilder;
import info.openrocket.core.appearance.Decal.EdgeMode;
import info.openrocket.core.document.Attachment;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;

import org.xml.sax.SAXException;

class AppearanceHandler extends AbstractElementHandler {
	protected final DocumentLoadingContext context;
	protected final RocketComponent component;

	protected final AppearanceBuilder builder = new AppearanceBuilder();
	protected boolean isInDecal = false;
	
	public AppearanceHandler(RocketComponent component, DocumentLoadingContext context) {
		this.context = context;
		this.component = component;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		if ("decal".equals(element)) {
			String name = attributes.remove("name");
			Attachment a = context.getAttachmentFactory().getAttachment(name);
			builder.setImage(context.getOpenRocketDocument().getDecalImage(a));
			double rotation = Double.parseDouble(attributes.remove("rotation"));
			builder.setRotation(rotation);
			String edgeModeName = attributes.remove("edgemode");
			EdgeMode edgeMode = EdgeMode.valueOf(edgeModeName);
			builder.setEdgeMode(edgeMode);
			isInDecal = true;
			return this;
		}
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		if ("paint".equals(element)) {
			int red = Integer.parseInt(attributes.get("red"));
			int green = Integer.parseInt(attributes.get("green"));
			int blue = Integer.parseInt(attributes.get("blue"));
			int alpha = 255;// set default
			// add a test if "alpha" was added to the XML / backwards compatibility
			String a = attributes.get("alpha");
			if (a != null) {
				// "alpha" string was present so load the value
				alpha = Integer.parseInt(a);
			}
			builder.setPaint(new ORColor(red, green, blue, alpha));
			return;
		}
		if ("shine".equals(element)) {
			double shine = Double.parseDouble(content);
			builder.setShine(shine);
			return;
		}
		if (isInDecal && "center".equals(element)) {
			double x = Double.parseDouble(attributes.get("x"));
			double y = Double.parseDouble(attributes.get("y"));
			builder.setCenter(x, y);
			return;
		}
		if (isInDecal && "offset".equals(element)) {
			double x = Double.parseDouble(attributes.get("x"));
			double y = Double.parseDouble(attributes.get("y"));
			builder.setOffset(x, y);
			return;
		}
		if (isInDecal && "scale".equals(element)) {
			double x = Double.parseDouble(attributes.get("x"));
			double y = Double.parseDouble(attributes.get("y"));
			builder.setScaleUV(x, y);
			return;
		}
		if (isInDecal && "decal".equals(element)) {
			isInDecal = false;
			return;
		}
		
		super.closeElement(element, attributes, content, warnings);
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		if ("decal".equals(element)) {
			isInDecal = false;
			return;
		}
		setAppearance();
		super.endHandler(element, attributes, content, warnings);
	}

	protected void setAppearance() {
		component.setAppearance(builder.getAppearance());
	}
	
}
