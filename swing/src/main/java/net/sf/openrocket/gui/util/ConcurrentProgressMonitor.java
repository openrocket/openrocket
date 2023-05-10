package net.sf.openrocket.gui.util;

import java.awt.Component;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;


/**
 * A thread-safe <code>ProgressMonitor</code>.  This class may be instantiated
 * and the method {@link #setProgress(int)} called safely from any thread.
 * <p>
 * Why the FSCK&!#&% isn't the default API version thread-safe?!?!
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ConcurrentProgressMonitor extends ProgressMonitor {

	public ConcurrentProgressMonitor(Component parentComponent, Object message,
			String note, int min, int max) {
		super(parentComponent, message, note, min, max);
	}

	@Override
	public void setProgress(final int nv) {
		
		if (SwingUtilities.isEventDispatchThread()) {
			super.setProgress(nv);
		} else {
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ConcurrentProgressMonitor.super.setProgress(nv);
				}
				
			});
		}
	}
	
	
	@Override
	public void close() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.close();
		} else {
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ConcurrentProgressMonitor.super.close();
				}
				
			});
		}
	}


}
