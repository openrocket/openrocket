package net.sf.openrocket.document;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.startup.Application;

public class OpenRocketDocumentFactory {
	
	private static final Translator trans = Application.getTranslator();
	
	public static OpenRocketDocument createNewRocket() {
		Rocket rocket = new Rocket();
		Stage stage = new Stage();
		//// Sustainer
		stage.setName(trans.get("BasicFrame.StageName.Sustainer"));
		rocket.addChild(stage);
		OpenRocketDocument doc = new OpenRocketDocument(rocket);
		doc.setSaved(true);
		return doc;
	}
	
	public static OpenRocketDocument createDocumentFromRocket(Rocket r) {
		OpenRocketDocument doc = new OpenRocketDocument(r);
		return doc;
	}
	
	public static OpenRocketDocument createEmptyRocket() {
		Rocket rocket = new Rocket();
		OpenRocketDocument doc = new OpenRocketDocument(rocket);
		return doc;
	}
	
}
