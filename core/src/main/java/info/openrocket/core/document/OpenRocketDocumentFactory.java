package info.openrocket.core.document;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.startup.Application;

public class OpenRocketDocumentFactory {

	private static final Translator trans = Application.getTranslator();

	public static OpenRocketDocument createNewRocket() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		//// Sustainer
		stage.setName(trans.get("BasicFrame.StageName.Sustainer"));
		rocket.addChild(stage);
		rocket.getSelectedConfiguration().setAllStages();
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
