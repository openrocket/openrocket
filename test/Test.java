import java.util.ArrayList;

import net.sf.openrocket.simulation.SimulationListener;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


public class Test {

	public static int COUNT = 1000000;

	public static void main(String[] args) throws Exception {

		System.out.println("COUNT="+COUNT);
		
		for (int i=1; ; i++) {
			long t1 = System.currentTimeMillis();
			run();
			long t2 = System.currentTimeMillis();
			System.out.printf("Run %2d took %d ms, %.1f ms / 1000 rounds\n",
					i, (t2-t1), ((t2-t1)*1000.0/COUNT));
		}
		
	}
	
	
	static volatile ArrayList<SimulationListener> listeners = new ArrayList<SimulationListener>();
	static {
		for (int i=0; i<5; i++) {
			listeners.add(new AbstractSimulationListener());
		}
	}
	private static void run() throws SimulationException {

		
		for (int i=0; i<COUNT; i++) {
			SimulationListener[] array = listeners.toArray(new SimulationListener[0]);
			for (SimulationListener l: array) {
				l.forceCalculation(null, null, null);
			}
		}
		
		return;
	}

}
