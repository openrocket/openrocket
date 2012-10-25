package net.sf.openrocket.simulation;

import java.util.PriorityQueue;

import net.sf.openrocket.util.Monitorable;

/**
 * A sorted queue of FlightEvent objects.  This queue maintains the events in time order
 * and also keeps a modification count for the queue.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class EventQueue extends PriorityQueue<FlightEvent> implements Monitorable {

	private int modID = 0;
	
	public EventQueue() {
		super();
	}

	public EventQueue(PriorityQueue<? extends FlightEvent> c) {
		super(c);
	}

	@Override
	public boolean add(FlightEvent e) {
		modID++;
		return super.add(e);
	}

	@Override
	public void clear() {
		modID++;
		super.clear();
	}

	@Override
	public boolean offer(FlightEvent e) {
		modID++;
		return super.offer(e);
	}

	@Override
	public FlightEvent poll() {
		modID++;
		return super.poll();
	}

	@Override
	public boolean remove(Object o) {
		modID++;
		return super.remove(o);
	}

	@Override
	public int getModID() {
		return modID;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
}
