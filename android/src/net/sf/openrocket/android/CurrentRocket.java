package net.sf.openrocket.android;

import static net.sf.openrocket.android.events.Events.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

public class CurrentRocket {

	private Uri fileUri;

	private OpenRocketDocument rocketDocument;
	private WarningSet warnings;

	private boolean isModified = false;
	private Set<Integer> runningSims = new HashSet<Integer>();

	/**
	 * @return the rocketDocument
	 */
	public OpenRocketDocument getRocketDocument() {
		return rocketDocument;
	}

	private void notifySimsChanged( Context context ) {
		Intent msg = new Intent(MESSAGE_ACTION);
		msg.putExtra(TYPE, SIMS_CHANGED);

		LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
	}

	private void notifySimComplete( Context context ) {
		Intent msg = new Intent(MESSAGE_ACTION);
		msg.putExtra(TYPE, SIM_COMPLETE);

		LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
	}

	private void notifyMotorConfigChanged( Context context ) {
		Intent msg = new Intent(MESSAGE_ACTION);
		msg.putExtra(TYPE, CONFIGS_CHANGED);

		LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
	}

	public synchronized void lockSimulation( Context context, int simulationId ) {
		runningSims.add(simulationId);
		// TODO - someday we might want to know about this:
		// notifySimsChanged( context );
	}

	public synchronized void unlockSimulation( Context context, int simulationId ) {
		this.isModified = true;
		runningSims.remove(simulationId);
		notifySimComplete(context);
	}

	public synchronized Set<Integer> lockedSimulations() {
		return new HashSet<Integer>(runningSims);
	}

	public synchronized void addNewSimulation( Context context ) {
		isModified = true;
		Rocket rocket = rocketDocument.getRocket();
		Simulation newSim = new Simulation(rocket);
		newSim.setName(rocketDocument.getNextSimulationName());
		rocketDocument.addSimulation(newSim);
		notifySimsChanged(context);
	}

	public synchronized void deleteSimulation( Context context, int simulationPos ) {
		isModified = true;
		rocketDocument.removeSimulation( simulationPos );
		notifySimsChanged(context);
	}

	public synchronized String addNewMotorConfig( Context context ) {
		isModified = true;
		String configId = rocketDocument.getRocket().newMotorConfigurationID();
		notifyMotorConfigChanged(context);
		return configId;
	}
	
	public synchronized void deleteMotorConfig( Context context, String config ) {
		rocketDocument.getRocket().removeMotorConfigurationID(config);
		notifyMotorConfigChanged(context);
	}
	
	/**
	 * @param rocketDocument the rocketDocument to set
	 */
	public void setRocketDocument(OpenRocketDocument rocketDocument) {
		this.rocketDocument = rocketDocument;
		synchronized ( this ) {
			isModified = false;
		}
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

	public boolean isModified() {
		return this.isModified;
	}

	public boolean canSave() {
		return this.isModified && this.runningSims.isEmpty();
	}

	public void saveOpenRocketDocument() throws IOException {

		// Translate the fileUri if it happens to be a .rkt file.

		String filename = fileUri.getPath();

		if ( ! filename.endsWith(".ork") ) {
			filename = filename.concat(".ork");
		}

		OpenRocketSaver saver = new OpenRocketSaver();
		StorageOptions options = new StorageOptions();
		options.setCompressionEnabled(true);
		options.setSimulationTimeSkip(StorageOptions.SIMULATION_DATA_ALL);
		saver.save(new File(filename),rocketDocument,options);
		isModified = false;
	}

}
