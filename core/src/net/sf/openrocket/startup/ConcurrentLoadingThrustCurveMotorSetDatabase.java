package net.sf.openrocket.startup;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.openrocket.database.ThrustCurveMotorSet;
import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.MotorLoaderHelper;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Pair;

/**
 * Load motors in parallel using a three stage pipeline.
 * 
 * Stage 1: single thread managed by the ThrustCurveMotorSetDatabase.  This thread generates
 *          one object for each thrust curve motor file and puts it in the second stage.
 *          
 * Stage 2: multiple threads which process individual files.  Each process takes
 *          a single motor file and parses out the list of motors it contains.
 *          The list of motors is queued up for the third stage to process.
 *          
 * Stage 3: single thread which processes the list of motors generated in stage 2.
 *          This thread puts all the motors from the list in the motor set database.
 *          
 * It is important that stage 3 be done with a single thread because ThrustCurveMotorSetDatabase
 * is not thread safe.  Even if synchronization were to be done, it is unlikely that parallelizing
 * this process would improve anything.
 * 
 *
 */
public class ConcurrentLoadingThrustCurveMotorSetDatabase extends ThrustCurveMotorSetDatabase {

	private static final LogHelper log = Application.getLogger();
	private final String thrustCurveDirectory;

	/** Block motor loading for this many milliseconds */
	// Block motor loading for 1.5 seconds to allow window painting to be faster
	private static AtomicInteger blockLoading = new AtomicInteger(1500);

	public ConcurrentLoadingThrustCurveMotorSetDatabase(String thrustCurveDirectory) {
		// configure ThrustCurveMotorSetDatabase as true so we get our own thread in
		// loadMotors.
		super(true);
		this.thrustCurveDirectory = thrustCurveDirectory;
	}

	@Override
	protected void loadMotors() {

		// Block loading until timeout occurs or database is taken into use
		log.info("Blocking motor loading while starting up");
		/*
		while (!inUse && blockLoading.addAndGet(-100) > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		 */
		log.info("Blocking ended, inUse=" + inUse + " blockLoading=" + blockLoading.get());

		BookKeeping keeper = new BookKeeping();
		keeper.start();

		try {
			keeper.waitForFinish();
		}
		catch ( InterruptedException iex ) {
			throw new BugException(iex);
		}

		keeper = null;

	}

	private void addAll( List<Motor> motors ) {
		for (Motor m : motors) {
			addMotor( (ThrustCurveMotor) m);
		}
	}

	/**
	 * A class which holds all the threading data.
	 * Implemented as an inner class so we can easily jettison the references when
	 * the processing is terminated.
	 *
	 */
	private class BookKeeping {

		/*
		 * Executor for Stage 3.
		 */
		private final ExecutorService writerThread;

		/*
		 * Executor for Stage 2.
		 */
		private final ExecutorService loaderPool;

		/*
		 * Runnable used for Stage 1.
		 */
		private final WorkGenerator workGenerator;

		private long startTime;

		/*
		 * Number of thrust curves loaded
		 */
		private int thrustCurveCount = 0;

		/*
		 * Number of files processed.
		 */
		private int fileCount = 0;

		/*
		 * We have to hold on to the zip file iterator which is used to load
		 * the system motor files until all processing is done.  This is because
		 * closing the iterator prematurely causes all the InputStreams opened
		 * with it to close. 
		 */
		private FileIterator iterator;

		private BookKeeping() {

			writerThread = new ThreadPoolExecutor(1,1,200, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r,"MotorWriterThread");
					return t;
				}
			});

			loaderPool = new ThreadPoolExecutor(10,10, 2, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
				int threadCount = 0;
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r,"MotorLoaderPool-" + threadCount++);
					return t;
				}
			});

			workGenerator = new WorkGenerator();

		}

		private void start() {

			startTime = System.currentTimeMillis();

			log.info("Starting motor loading from " + thrustCurveDirectory + " in background thread.");

			// Run the work generator - in this thread.
			workGenerator.run();

		}

		private void waitForFinish() throws InterruptedException {
			try {
				loaderPool.shutdown();
				loaderPool.awaitTermination(90, TimeUnit.SECONDS);
				writerThread.shutdown();
				writerThread.awaitTermination(90, TimeUnit.SECONDS);
			}
			finally {
				iterator.close();
			}

			long endTime = System.currentTimeMillis();

			int distinctMotorCount = 0;
			int distinctThrustCurveCount = 0;
			distinctMotorCount = motorSets.size();
			for (ThrustCurveMotorSet set : motorSets) {
				distinctThrustCurveCount += set.getMotorCount();
			}

			log.info("Motor loading done, took " + (endTime - startTime) + " ms to load " 
					+ fileCount + " files/directories containing " 
					+ thrustCurveCount + " thrust curves which contained "
					+ distinctMotorCount + " distinct motors with "
					+ distinctThrustCurveCount + " distinct thrust curves.");

		}


		private class WorkGenerator implements Runnable {

			@Override
			public void run() {
				// Start loading
				log.info("Loading motors from " + thrustCurveDirectory);

				iterator = DirectoryIterator.findDirectory(thrustCurveDirectory, new SimpleFileFilter("",false,"ser"));

				// Load the packaged thrust curves
				if (iterator == null) {
					throw new IllegalStateException("Thrust curve directory " + thrustCurveDirectory +
							"not found, distribution built wrong");
				}

				while( iterator.hasNext() ) {
					Pair<String,InputStream> f = iterator.next();
					try {
						ObjectInputStream ois = new ObjectInputStream(f.getV());
						List<Motor> m = (List<Motor>) ois.readObject();
						writerThread.submit( new MotorInserter(m));
					}
					catch ( Exception ex ) {
						throw new BugException(ex);
					}
				}

				// Load the user-defined thrust curves
				for (File file : ((SwingPreferences) Application.getPreferences()).getUserThrustCurveFiles()) {
					if ( file == null ) {
						continue;
					}
					log.info("Loading motors from " + file);
					MotorLoader loader = new MotorLoader( file );
					loaderPool.execute(loader);
					fileCount++;
				}
			}
		}

		private class MotorLoader implements Runnable {

			private final InputStream is;
			private final String fileName;

			private final File file;

			public MotorLoader( File file ) {
				super();
				this.file = file;
				this.is = null;
				this.fileName = null;
			}

			public MotorLoader(InputStream is, String fileName) {
				super();
				this.file = null;
				this.is = is;
				this.fileName = fileName;
			}

			@Override
			public void run() {
				if ( fileName != null ) {
					log.debug("Loading motor from " + fileName);
				}

				try {
					List<Motor> motors;
					if ( file == null ) {
						motors = MotorLoaderHelper.load(is, fileName);
					} else {
						motors = MotorLoaderHelper.load(file);
					}
					writerThread.submit( new MotorInserter(motors));
				}
				finally {
					if ( is != null ) {
						try {
							is.close();
						} catch ( IOException iex ) {
						}
					}
				}
			}
		}

		private class MotorInserter implements Runnable {

			private final List<Motor> motors;

			MotorInserter( Motor motor ) {
				this.motors = Collections.singletonList(motor);
			}
			MotorInserter( List<Motor> motors ) {
				this.motors = motors;
			}

			@Override
			public void run() {
				thrustCurveCount += motors.size();
				ConcurrentLoadingThrustCurveMotorSetDatabase.this.addAll(motors);
			}

		}
	}

}
