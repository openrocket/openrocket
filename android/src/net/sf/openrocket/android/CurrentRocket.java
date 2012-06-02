package net.sf.openrocket.android;

import java.io.File;
import java.io.IOException;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.net.Uri;

public class CurrentRocket {

	private Uri fileUri;

	private OpenRocketDocument rocketDocument;
	private WarningSet warnings;

	private RocketChangedEventHandler handler;
	
	public void setHandler( RocketChangedEventHandler handler ) {
		this.handler = handler;
	}
	
	/**
	 * @return the rocketDocument
	 */
	public OpenRocketDocument getRocketDocument() {
		return rocketDocument;
	}

	public void addNewSimulation() {
		Rocket rocket = rocketDocument.getRocket();
		Simulation newSim = new Simulation(rocket);
		newSim.setName(rocketDocument.getNextSimulationName());
		rocketDocument.addSimulation(newSim);
		if ( handler != null ) {
			handler.simsChangedMessage();
		}
	}
	
	public void deleteSimulation( int simulationPos ) {
		rocketDocument.removeSimulation( simulationPos );
		if ( handler != null ) {
			handler.simsChangedMessage();
		}
	}
	
	public String addNewMotorConfig() {
		String configId = rocketDocument.getRocket().newMotorConfigurationID();
		if ( handler != null ) {
			handler.configsChangedMessage();
		}
		return configId;
	}
	/**
	 * @param rocketDocument the rocketDocument to set
	 */
	public void setRocketDocument(OpenRocketDocument rocketDocument) {
		this.rocketDocument = rocketDocument;
	}

	public WarningSet getWarnings() {
		return warnings;
	}

	public void setWarnings(WarningSet warnings) {
		this.warnings = warnings;
	}

	public Uri getFileUri() {
		return fileUri;
	}

	public void setFileUri(Uri fileUri) {
		this.fileUri = fileUri;
	}

	public void saveOpenRocketDocument() throws IOException {
		OpenRocketSaver saver = new OpenRocketSaver();
		saver.save(new File(fileUri.getPath()),rocketDocument);

	}


}
