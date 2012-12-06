package net.sf.openrocket.startup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import net.sf.openrocket.file.motor.GeneralMotorLoader;
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
	
	private static final int MAX_LOADING_THREADS = 10;
	
	private static final LogHelper log = Application.getLogger();
	private final String thrustCurveDirectory;
	
	/** Block motor loading for this many milliseconds */
	// Block motor loading for 1.5 seconds to allow window painting to be faster
	private static AtomicInteger blockLoading = new AtomicInteger(1500);
	
	private int motorCount = 0;
	
	public ConcurrentLoadingThrustCurveMotorSetDatabase(String thrustCurveDirectory) {
		super(true);
		this.thrustCurveDirectory = thrustCurveDirectory;
	}
	
	@Override
	protected void loadMotors() {
		
		// Block loading until timeout occurs or database is taken into use
		log.info("Blocking motor loading while starting up");
		
		while (!inUse && blockLoading.addAndGet(-100) > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		log.info("Blocking ended, inUse=" + inUse + " blockLoading=" + blockLoading.get());
		
		if (true) {
			GeneralMotorLoader loader = new GeneralMotorLoader();
			SimpleFileFilter fileFilter = new SimpleFileFilter("", loader.getSupportedExtensions());
			
			log.info("Starting reading serialized motor database");
			FileIterator iterator = DirectoryIterator.findDirectory(thrustCurveDirectory, new SimpleFileFilter("", false, "ser"));
			while (iterator.hasNext()) {
				Pair<String, InputStream> f = iterator.next();
				loadSerialized(f);
			}
			log.info("Ending reading serialized motor database, motorCount=" + motorCount);
			
			
			log.info("Starting reading user-defined motors");
			for (File file : ((SwingPreferences) Application.getPreferences()).getUserThrustCurveFiles()) {
				if (file.isFile()) {
					loadFile(loader, file);
				} else if (file.isDirectory()) {
					loadDirectory(loader, fileFilter, file);
				} else {
					log.warn("User-defined motor file " + file + " is neither file nor directory");
				}
			}
			log.info("Ending reading user-defined motors, motorCount=" + motorCount);
			
		} else {
			BookKeeping keeper = new BookKeeping();
			keeper.start();
			
			try {
				keeper.waitForFinish();
			} catch (InterruptedException iex) {
				throw new BugException(iex);
			}
			
			keeper = null;
		}
	}
	
	
	private void loadSerialized(Pair<String, InputStream> f) {
		try {
			log.debug("Reading motors from file " + f.getU());
			ObjectInputStream ois = new ObjectInputStream(f.getV());
			List<Motor> motors = (List<Motor>) ois.readObject();
			addMotors(motors);
		} catch (Exception ex) {
			throw new BugException(ex);
		}
	}
	
	
	private void loadFile(GeneralMotorLoader loader, File file) {
		BufferedInputStream bis = null;
		try {
			log.debug("Loading motors from file " + file);
			bis = new BufferedInputStream(new FileInputStream(file));
			List<Motor> motors = loader.load(bis, file.getName());
			addMotors(motors);
			bis.close();
		} catch (IOException e) {
			log.warn("IOException while reading " + file + ": " + e, e);
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e1) {
					
				}
			}
		}
	}
	
	private void loadDirectory(GeneralMotorLoader loader, SimpleFileFilter fileFilter, File file) {
		FileIterator iterator;
		try {
			iterator = new DirectoryIterator(file, fileFilter, true);
		} catch (IOException e) {
			log.warn("Unable to read directory " + file + ": " + e, e);
			return;
		}
		while (iterator.hasNext()) {
			Pair<String, InputStream> f = iterator.next();
			try {
				List<Motor> motors = loader.load(f.getV(), f.getU());
				addMotors(motors);
				f.getV().close();
			} catch (IOException e) {
				log.warn("IOException while loading file " + f.getU() + ": " + e, e);
				try {
					f.getV().close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	private synchronized void addMotors(List<Motor> motors) {
		for (Motor m : motors) {
			motorCount++;
			addMotor((ThrustCurveMotor) m);
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
			
			writerThread = new ThreadPoolExecutor(1, 1, 200, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
						@Override
						public Thread newThread(Runnable r) {
							Thread t = new Thread(r, "MotorWriterThread");
							return t;
						}
					});
			
			loaderPool = new ThreadPoolExecutor(10, 10, 2, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
						int threadCount = 0;
						
						@Override
						public Thread newThread(Runnable r) {
							Thread t = new Thread(r, "MotorLoaderPool-" + threadCount++);
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
			} finally {
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
				
				iterator = DirectoryIterator.findDirectory(thrustCurveDirectory, new SimpleFileFilter("", false, "ser"));
				
				// Load the packaged thrust curves
				if (iterator == null) {
					throw new IllegalStateException("Thrust curve directory " + thrustCurveDirectory +
							"not found, distribution built wrong");
				}
				
				while (iterator.hasNext()) {
					Pair<String, InputStream> f = iterator.next();
					try {
						log.debug("Start reading motors from file " + f.getU());
						ObjectInputStream ois = new ObjectInputStream(f.getV());
						List<Motor> m = (List<Motor>) ois.readObject();
						writerThread.submit(new MotorInserter(m));
						log.debug("Stop reading motors from file " + f.getU());
					} catch (Exception ex) {
						throw new BugException(ex);
					}
				}
				
				// Load the user-defined thrust curves
				for (File file : ((SwingPreferences) Application.getPreferences()).getUserThrustCurveFiles()) {
					if (file == null) {
						continue;
					}
					log.info("Loading motors from " + file);
					BackgroundMotorLoader loader = new BackgroundMotorLoader(file);
					loaderPool.execute(loader);
					fileCount++;
				}
			}
		}
		
		private class BackgroundMotorLoader implements Runnable {
			
			private final InputStream is;
			private final String fileName;
			
			private final File file;
			
			public BackgroundMotorLoader(File file) {
				super();
				this.file = file;
				this.is = null;
				this.fileName = null;
			}
			
			public BackgroundMotorLoader(InputStream is, String fileName) {
				super();
				this.file = null;
				this.is = is;
				this.fileName = fileName;
			}
			
			@Override
			public void run() {
				if (fileName != null) {
					log.debug("Loading motor from " + fileName);
				}
				
				try {
					List<Motor> motors;
					if (file == null) {
						motors = MotorLoaderHelper.load(is, fileName);
					} else {
						motors = MotorLoaderHelper.load(file);
					}
					writerThread.submit(new MotorInserter(motors));
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException iex) {
						}
					}
				}
			}
		}
		
		private class MotorInserter implements Runnable {
			
			private final List<Motor> motors;
			
			MotorInserter(Motor motor) {
				this.motors = Collections.singletonList(motor);
			}
			
			MotorInserter(List<Motor> motors) {
				this.motors = motors;
			}
			
			@Override
			public void run() {
				thrustCurveCount += motors.size();
				ConcurrentLoadingThrustCurveMotorSetDatabase.this.addMotors(motors);
			}
			
		}
	}
	
	
	private class BackgroundMotorLoader implements Runnable {
		
		private final InputStream is;
		private final String fileName;
		
		private final File file;
		
		public BackgroundMotorLoader(File file) {
			super();
			this.file = file;
			this.is = null;
			this.fileName = null;
		}
		
		public BackgroundMotorLoader(InputStream is, String fileName) {
			super();
			this.file = null;
			this.is = is;
			this.fileName = fileName;
		}
		
		@Override
		public void run() {
			if (fileName != null) {
				log.debug("Loading motor from " + fileName);
			}
			
			try {
				List<Motor> motors;
				if (file == null) {
					motors = MotorLoaderHelper.load(is, fileName);
				} else {
					motors = MotorLoaderHelper.load(file);
				}
				addMotors(motors);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException iex) {
					}
				}
			}
		}
	}
	
}
