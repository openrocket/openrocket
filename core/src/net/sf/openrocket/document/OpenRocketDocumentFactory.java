package net.sf.openrocket.document;

import java.io.File;
import java.net.URL;

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
		OpenRocketDocument doc = new OpenRocketDocument(rocket, (File) null, false);
		doc.setSaved(true);
		return doc;
	}
	
	public static OpenRocketDocument createDocumentFromRocket(Rocket r) {
		OpenRocketDocument doc = new OpenRocketDocument(r, (File) null, false);
		return doc;
	}
	
	public static OpenRocketDocument createDocumentForFile(File filename, boolean isContainer) {
		Rocket rocket = new Rocket();
		OpenRocketDocument doc = new OpenRocketDocument(rocket, filename, isContainer);
		return doc;
	}
	
	public static OpenRocketDocument createDocumentForUrl(URL filename, boolean isContainer) {
		Rocket rocket = new Rocket();
		OpenRocketDocument doc = new OpenRocketDocument(rocket, filename, isContainer);
		return doc;
	}
	
}
