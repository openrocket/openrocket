package net.sf.openrocket.android.simservice;

import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.customexpression.CustomExpressionSimulationListener;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class SimulationService extends IntentService {

	// We use an id (from a dummy string) as the notificationID because it is unique.
	private final static int notificationID = R.string.SimulationServiceNotificationID;
	
	private Notification notification;
	
	public static void executeSimulationTask( Context c, SimulationTask t ) {
		AndroidLogWrapper.d(SimulationService.class, "Submitting simulation " + t.simulationId );

		CurrentRocketHolder.getCurrentRocket().lockSimulation( c, t.simulationId );
		
		Intent intent = new Intent( c, SimulationService.class );
		intent.putExtra("net.sf.openrocket.simulationtask", t);
		c.startService(intent);
	}
	
	public SimulationService() {
		super("OpenRocket Simulation Execution Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SimulationTask t = (SimulationTask) intent.getSerializableExtra("net.sf.openrocket.simulationtask");
		try {
			Simulation sim = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getSimulation(t.simulationId);

			List<CustomExpression> exprs = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getCustomExpressions();
			SimulationListener exprListener = new CustomExpressionSimulationListener(exprs);

			AndroidLogWrapper.d(SimulationService.class, "simulating " + t.simulationId );
			sim.simulate(exprListener);
			CurrentRocketHolder.getCurrentRocket().unlockSimulation(this, t.simulationId);
		}
		catch (SimulationException simex) {
			Toast.makeText(this, "Error in simulation:" + simex.getMessage(), Toast.LENGTH_LONG ).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		this.notification = buildNotification();
		startForeground(notificationID, notification);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}

	private Notification buildNotification( ) {
		String message = "OpenRocket Simulation Execution";
		Notification notification = new Notification(R.drawable.or_launcher, message, System.currentTimeMillis());
		
		notification.flags = Notification.FLAG_NO_CLEAR;
		PendingIntent contentIntent = PendingIntent.getActivity( this, 0 , new Intent( ), PendingIntent.FLAG_UPDATE_CURRENT );
		notification.setLatestEventInfo(this, "OpenRocket", message, contentIntent);
		return notification;
	}
}
