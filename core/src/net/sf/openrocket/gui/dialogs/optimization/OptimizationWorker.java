package net.sf.openrocket.gui.dialogs.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.FunctionOptimizer;
import net.sf.openrocket.optimization.general.OptimizationController;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.ParallelExecutorCache;
import net.sf.openrocket.optimization.general.ParallelFunctionCache;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.general.multidim.MultidirectionalSearchOptimizer;
import net.sf.openrocket.optimization.general.onedim.GoldenSectionSearchOptimizer;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.OptimizationGoal;
import net.sf.openrocket.optimization.rocketoptimization.RocketOptimizationFunction;
import net.sf.openrocket.optimization.rocketoptimization.RocketOptimizationListener;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.BugException;

/**
 * A background worker that runs the optimization in the background.  It supports providing
 * evaluation and step counter information via listeners that are executed on the EDT.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class OptimizationWorker extends Thread implements OptimizationController, RocketOptimizationListener {
	
	/*
	 * Note:  This is implemented as a separate Thread object instead of a SwingWorker because
	 * the SwingWorker cannot be interrupted in any way except by canceling the task, which
	 * makes it impossible to wait for its exiting (SwingWorker.get() throws a CancellationException
	 * if cancel() has been called).
	 * 
	 * SwingWorker also seems to miss some chunks that have been provided to process() when the
	 * thread ends.
	 * 
	 * Nothing of this is documented, of course...
	 */

	private static final Logger log = LoggerFactory.getLogger(OptimizationWorker.class);
	
	/** Notify listeners every this many milliseconds */
	private static final long PURGE_TIMEOUT = 500;
	/** End optimization when step size is below this threshold */
	private static final double STEP_SIZE_LIMIT = 0.005;
	
	private final FunctionOptimizer optimizer;
	private final RocketOptimizationFunction function;
	
	private final Simulation simulation;
	private final SimulationModifier[] modifiers;
	
	private final ParallelFunctionCache cache;
	

	private final LinkedBlockingQueue<FunctionEvaluationData> evaluationQueue =
			new LinkedBlockingQueue<FunctionEvaluationData>();
	private final LinkedBlockingQueue<OptimizationStepData> stepQueue =
			new LinkedBlockingQueue<OptimizationStepData>();
	private volatile long lastPurge = 0;
	
	private OptimizationException optimizationException = null;
	
	
	/**
	 * Sole constructor
	 * @param simulation	the simulation
	 * @param parameter			the optimization parameter
	 * @param goal				the optimization goal
	 * @param domain			the optimization domain
	 * @param modifiers			the simulation modifiers
	 */
	public OptimizationWorker(Simulation simulation, OptimizableParameter parameter,
			OptimizationGoal goal, SimulationDomain domain, SimulationModifier... modifiers) {
		
		this.simulation = simulation;
		this.modifiers = modifiers.clone();
		
		function = new RocketOptimizationFunction(simulation, parameter, goal, domain, modifiers);
		function.addRocketOptimizationListener(this);
		
		cache = new ParallelExecutorCache(1);
		cache.setFunction(function);
		
		if (modifiers.length == 1) {
			optimizer = new GoldenSectionSearchOptimizer(cache);
		} else {
			optimizer = new MultidirectionalSearchOptimizer(cache);
		}
	}
	
	
	@Override
	public void run() {
		try {
			
			double[] current = new double[modifiers.length];
			for (int i = 0; i < modifiers.length; i++) {
				current[i] = modifiers[i].getCurrentScaledValue(simulation);
			}
			Point initial = new Point(current);
			
			optimizer.optimize(initial, this);
			
		} catch (OptimizationException e) {
			this.optimizationException = e;
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					lastPurge = System.currentTimeMillis() + 24L * 3600L * 1000L;
					processQueue();
					done(optimizationException);
				}
			});
		}
	}
	
	/**
	 * This method is called after the optimization has ended, either normally, when interrupted
	 * or by throwing an exception.  This method is called on the EDT, like the done() method of SwingWorker.
	 * <p>
	 * All data chunks to the listeners will be guaranteed to have been processed before calling done().
	 * 
	 * @param exception		a possible optimization exception that occurred, or <code>null</code> for normal exit.
	 */
	protected abstract void done(OptimizationException exception);
	
	
	/**
	 * This method is called for each function evaluation that has taken place.
	 * This method is called on the EDT.
	 * 
	 * @param data	the data accumulated since the last call
	 */
	protected abstract void functionEvaluated(List<FunctionEvaluationData> data);
	
	/**
	 * This method is called after each step taken by the optimization algorithm.
	 * This method is called on the EDT.
	 * 
	 * @param data	the data accumulated since the last call
	 */
	protected abstract void optimizationStepTaken(List<OptimizationStepData> data);
	
	
	/**
	 * Publishes data to the listeners.  The queue is purged every PURGE_TIMEOUT milliseconds.
	 * 
	 * @param data	the data to publish to the listeners
	 */
	private synchronized void publish(FunctionEvaluationData evaluation, OptimizationStepData step) {
		
		if (evaluation != null) {
			evaluationQueue.add(evaluation);
		}
		if (step != null) {
			stepQueue.add(step);
		}
		
		// Add a method to the EDT to process the queue data
		long now = System.currentTimeMillis();
		if (lastPurge + PURGE_TIMEOUT <= now) {
			lastPurge = now;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					processQueue();
				}
			});
		}
		
	}
	
	
	/**
	 * Process the queue and call the listeners.  This method must always be called from the EDT.
	 */
	private void processQueue() {
		
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new BugException("processQueue called from non-EDT");
		}
		

		List<FunctionEvaluationData> evaluations = new ArrayList<FunctionEvaluationData>();
		evaluationQueue.drainTo(evaluations);
		if (!evaluations.isEmpty()) {
			functionEvaluated(evaluations);
		}
		

		List<OptimizationStepData> steps = new ArrayList<OptimizationStepData>();
		stepQueue.drainTo(steps);
		if (!steps.isEmpty()) {
			optimizationStepTaken(steps);
		}
	}
	
	


	/*
	 * NOTE:  The stepTaken and evaluated methods may be called from other
	 * threads than the EDT or the SwingWorker thread!
	 */

	@Override
	public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
		publish(null, new OptimizationStepData(oldPoint, oldValue, newPoint, newValue, stepSize));
		
		if (stepSize < STEP_SIZE_LIMIT) {
			log.info("stepSize=" + stepSize + " is below limit, ending optimization");
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public void evaluated(Point point, Value[] state, Value domainReference, Value parameterValue, double goalValue) {
		publish(new FunctionEvaluationData(point, state, domainReference, parameterValue, goalValue), null);
	}
	
}
