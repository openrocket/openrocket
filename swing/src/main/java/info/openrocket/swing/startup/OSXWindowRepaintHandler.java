package info.openrocket.swing.startup;

import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.desktop.AppForegroundEvent;
import java.awt.desktop.AppForegroundListener;
import java.awt.desktop.AppHiddenEvent;
import java.awt.desktop.AppHiddenListener;
import java.awt.desktop.ScreenSleepEvent;
import java.awt.desktop.ScreenSleepListener;
import java.awt.desktop.SystemSleepEvent;
import java.awt.desktop.SystemSleepListener;
import java.awt.desktop.UserSessionEvent;
import java.awt.desktop.UserSessionListener;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recovers Swing windows on macOS that lose their native backing layer contents (turning black
 * until individual components are hovered) after system sleep, display sleep, screen unlock,
 * or display reconfiguration.
 *
 * <p>On macOS, {@code LWWindowPeer.displayChanged()} replaces the native surface with a blank
 * layer and issues a repaint that can be dropped while displays or the window server are still
 * waking up. Because Swing's {@link RepaintManager} does not consider the root pane dirty once
 * the wake transition finishes, only individual hovered components repaint. Listening for
 * macOS wake/session/foreground events and window activations forces a clean full-window
 * repaint on the EDT.</p>
 */
final class OSXWindowRepaintHandler {
	private static final Logger log = LoggerFactory.getLogger(OSXWindowRepaintHandler.class);

	/**
	 * Coalesced follow-up delay after sleep/wake or unlock events so external displays that
	 * finish reconfiguring slightly after the OS wake notification also receive a full repaint.
	 */
	static final int WAKE_FOLLOW_UP_DELAY_MS = 500;

	private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

	private final Supplier<Window[]> windowSupplier;
	private final Function<JComponent, RepaintManager> repaintManagerLookup;
	private final Timer wakeFollowUpTimer;

	private final SystemSleepListener systemSleepListener = new SystemSleepListener() {
		@Override
		public void systemAboutToSleep(SystemSleepEvent e) {
			// Nothing to do before sleep
		}

		@Override
		public void systemAwoke(SystemSleepEvent e) {
			log.debug("macOS system awoke; scheduling Swing window repaint");
			scheduleWakeRepaint();
		}
	};

	private final ScreenSleepListener screenSleepListener = new ScreenSleepListener() {
		@Override
		public void screenAboutToSleep(ScreenSleepEvent e) {
			// Nothing to do before display sleep
		}

		@Override
		public void screenAwoke(ScreenSleepEvent e) {
			log.debug("macOS screen awoke; scheduling Swing window repaint");
			scheduleWakeRepaint();
		}
	};

	private final UserSessionListener userSessionListener = new UserSessionListener() {
		@Override
		public void userSessionDeactivated(UserSessionEvent e) {
			// Nothing to do on lock/session switch away
		}

		@Override
		public void userSessionActivated(UserSessionEvent e) {
			log.debug("macOS user session activated; scheduling Swing window repaint");
			scheduleWakeRepaint();
		}
	};

	private final AppHiddenListener appHiddenListener = new AppHiddenListener() {
		@Override
		public void appHidden(AppHiddenEvent e) {
			// Nothing to do when hidden
		}

		@Override
		public void appUnhidden(AppHiddenEvent e) {
			log.debug("macOS app unhidden; scheduling Swing window repaint");
			scheduleWakeRepaint();
		}
	};

	private final AppForegroundListener appForegroundListener = new AppForegroundListener() {
		@Override
		public void appRaisedToForeground(AppForegroundEvent e) {
			scheduleForegroundRepaint();
		}

		@Override
		public void appMovedToBackground(AppForegroundEvent e) {
			// Nothing to do when moving to background
		}
	};

	private final AWTEventListener windowEventListener = this::handleAwtWindowEvent;

	OSXWindowRepaintHandler() {
		this(Window::getWindows, RepaintManager::currentManager, WAKE_FOLLOW_UP_DELAY_MS);
	}

	OSXWindowRepaintHandler(Supplier<Window[]> windowSupplier,
			Function<JComponent, RepaintManager> repaintManagerLookup,
			int wakeFollowUpDelayMs) {
		this.windowSupplier = windowSupplier;
		this.repaintManagerLookup = repaintManagerLookup;
		this.wakeFollowUpTimer = new Timer(wakeFollowUpDelayMs, e -> repaintAllShowingWindows(true));
		this.wakeFollowUpTimer.setRepeats(false);
	}

	/**
	 * Installs the macOS wake and window-activation repaint handler once per JVM.
	 *
	 * @param desktop the macOS {@link Desktop} instance
	 */
	static void install(Desktop desktop) {
		if (!INSTALLED.compareAndSet(false, true)) {
			return;
		}
		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler();
		handler.register(desktop, Toolkit.getDefaultToolkit());
	}

	void register(Desktop desktop, Toolkit toolkit) {
		if (desktop != null) {
			if (desktop.isSupported(Desktop.Action.APP_EVENT_SYSTEM_SLEEP)) {
				desktop.addAppEventListener(systemSleepListener);
			}
			if (desktop.isSupported(Desktop.Action.APP_EVENT_SCREEN_SLEEP)) {
				desktop.addAppEventListener(screenSleepListener);
			}
			if (desktop.isSupported(Desktop.Action.APP_EVENT_USER_SESSION)) {
				desktop.addAppEventListener(userSessionListener);
			}
			if (desktop.isSupported(Desktop.Action.APP_EVENT_HIDDEN)) {
				desktop.addAppEventListener(appHiddenListener);
			}
			if (desktop.isSupported(Desktop.Action.APP_EVENT_FOREGROUND)) {
				desktop.addAppEventListener(appForegroundListener);
			}
		}
		if (toolkit != null) {
			toolkit.addAWTEventListener(windowEventListener, AWTEvent.WINDOW_EVENT_MASK);
		}
	}

	void scheduleWakeRepaint() {
		runOnEdt(() -> {
			repaintAllShowingWindows(true);
			wakeFollowUpTimer.restart();
		});
	}

	void scheduleForegroundRepaint() {
		runOnEdt(() -> repaintAllShowingWindows(false));
	}

	void handleAwtWindowEvent(AWTEvent event) {
		if (!(event instanceof WindowEvent windowEvent)) {
			return;
		}
		int id = windowEvent.getID();
		if (id == WindowEvent.WINDOW_ACTIVATED) {
			repaintWindow(windowEvent.getWindow(), false);
		} else if (id == WindowEvent.WINDOW_DEICONIFIED) {
			repaintWindow(windowEvent.getWindow(), true);
		}
	}

	void repaintAllShowingWindows(boolean flushBackBuffers) {
		Window[] windows = windowSupplier.get();
		if (windows == null || windows.length == 0) {
			return;
		}

		boolean backBuffersFlushed = false;
		for (Window window : windows) {
			if (window == null || !window.isDisplayable() || !window.isShowing()) {
				continue;
			}
			if (flushBackBuffers && !backBuffersFlushed && window instanceof RootPaneContainer container) {
				JRootPane rootPane = container.getRootPane();
				if (rootPane != null) {
					RepaintManager rm = repaintManagerLookup.apply(rootPane);
					if (rm != null) {
						// Matches RepaintManager.displayChanged(): clears cached VolatileImage back buffers
						// that may have been invalidated during macOS display sleep/reconfiguration.
						rm.setDoubleBufferMaximumSize(null);
						backBuffersFlushed = true;
					}
				}
			}
			repaintWindow(window, true);
		}
	}

	void repaintWindow(Window window, boolean repaintWindowPeer) {
		if (window == null || !window.isDisplayable() || !window.isShowing()) {
			return;
		}
		if (window instanceof RootPaneContainer container) {
			JRootPane rootPane = container.getRootPane();
			if (rootPane != null) {
				RepaintManager rm = repaintManagerLookup.apply(rootPane);
				if (rm != null) {
					rm.markCompletelyDirty(rootPane);
				}
				rootPane.repaint();
			}
			if (repaintWindowPeer) {
				window.repaint();
			}
		} else {
			window.repaint();
		}
	}

	SystemSleepListener getSystemSleepListener() {
		return systemSleepListener;
	}

	ScreenSleepListener getScreenSleepListener() {
		return screenSleepListener;
	}

	UserSessionListener getUserSessionListener() {
		return userSessionListener;
	}

	AppHiddenListener getAppHiddenListener() {
		return appHiddenListener;
	}

	AppForegroundListener getAppForegroundListener() {
		return appForegroundListener;
	}

	void stopTimers() {
		wakeFollowUpTimer.stop();
	}

	private static void runOnEdt(Runnable action) {
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		} else {
			SwingUtilities.invokeLater(action);
		}
	}
}
