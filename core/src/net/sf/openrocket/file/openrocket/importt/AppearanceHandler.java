package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

import org.xml.sax.SAXException;

class AppearanceHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent component;
	
	private final AppearanceBuilder builder = new AppearanceBuilder();
	private boolean isInDecal = false;
	
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
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		if ("paint".equals(element)) {
			int red = Integer.parseInt(attributes.get("red"));
			int green = Integer.parseInt(attributes.get("green"));
			int blue = Integer.parseInt(attributes.get("blue"));
			builder.setPaint(new Color(red, green, blue));
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
		component.setAppearance(builder.getAppearance());
		super.endHandler(element, attributes, content, warnings);
	}
	
}