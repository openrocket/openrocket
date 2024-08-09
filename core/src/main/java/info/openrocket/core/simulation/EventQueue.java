package info.openrocket.core.simulation;

import java.util.PriorityQueue;

import info.openrocket.core.util.ModID;
import info.openrocket.core.util.Monitorable;

/**
 * A sorted queue of FlightEvent objects. This queue maintains the events in
 * time order
 * and also keeps a modification count for the queue.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class EventQueue extends PriorityQueue<FlightEvent> implements Monitorable {

	private ModID modID = ModID.INVALID;

	public EventQueue() {
		super();
	}

	public EventQueue(PriorityQueue<? extends FlightEvent> c) {
		super(c);
	}

	@Override
	public boolean add(FlightEvent e) {
		modID = new ModID();
		return super.add(e);
	}

	@Override
	public void clear() {
		modID = new ModID();
		super.clear();
	}

	@Override
	public boolean offer(FlightEvent e) {
		modID = new ModID();
		return super.offer(e);
	}

	@Override
	public FlightEvent poll() {
		modID = new ModID();
		return super.poll();
	}

	@Override
	public boolean remove(Object o) {
		modID = new ModID();
		return super.remove(o);
	}

	@Override
	public ModID getModID() {
		return modID;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
