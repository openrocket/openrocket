package info.openrocket.swing.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.desktop.AppForegroundEvent;
import java.awt.desktop.ScreenSleepEvent;
import java.awt.desktop.SystemSleepEvent;
import java.awt.desktop.UserSessionEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

public class OSXWindowRepaintHandlerTest {

	@Test
	public void testRegisterAddsOnlySupportedDesktopListenersAndWindowEventListener() {
		Desktop desktop = mock(Desktop.class);
		Toolkit toolkit = mock(Toolkit.class);
		when(desktop.isSupported(Desktop.Action.APP_EVENT_SYSTEM_SLEEP)).thenReturn(true);
		when(desktop.isSupported(Desktop.Action.APP_EVENT_SCREEN_SLEEP)).thenReturn(false);
		when(desktop.isSupported(Desktop.Action.APP_EVENT_USER_SESSION)).thenReturn(true);
		when(desktop.isSupported(Desktop.Action.APP_EVENT_HIDDEN)).thenReturn(false);
		when(desktop.isSupported(Desktop.Action.APP_EVENT_FOREGROUND)).thenReturn(true);

		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler();
		try {
			handler.register(desktop, toolkit);

			verify(desktop).addAppEventListener(handler.getSystemSleepListener());
			verify(desktop, never()).addAppEventListener(handler.getScreenSleepListener());
			verify(desktop).addAppEventListener(handler.getUserSessionListener());
			verify(desktop, never()).addAppEventListener(handler.getAppHiddenListener());
			verify(desktop).addAppEventListener(handler.getAppForegroundListener());
			verify(toolkit).addAWTEventListener(any(AWTEventListener.class), eq(AWTEvent.WINDOW_EVENT_MASK));
		} finally {
			handler.stopTimers();
		}
	}

	@Test
	public void testSystemWakeRepaintsShowingWindowsImmediatelyAndAfterFollowUpDelay() throws Exception {
		JRootPane showingRootPane = mock(JRootPane.class);
		Window showingWindow = mockRootPaneWindow(true, true, showingRootPane);
		JRootPane hiddenRootPane = mock(JRootPane.class);
		Window hiddenWindow = mockRootPaneWindow(true, false, hiddenRootPane);
		RepaintManager repaintManager = mock(RepaintManager.class);

		CountDownLatch followUpDone = new CountDownLatch(2);
		AtomicInteger dirtyCount = new AtomicInteger();
		org.mockito.Mockito.doAnswer(invocation -> {
			dirtyCount.incrementAndGet();
			followUpDone.countDown();
			return null;
		}).when(repaintManager).markCompletelyDirty(showingRootPane);

		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler(
				() -> new Window[] { showingWindow, hiddenWindow },
				c -> repaintManager,
				40);
		try {
			handler.getSystemSleepListener().systemAwoke(mock(SystemSleepEvent.class));

			assertEquals(true, followUpDone.await(2, TimeUnit.SECONDS),
					"Expected both immediate and delayed wake repaints to run");
			SwingUtilities.invokeAndWait(() -> { });

			assertEquals(2, dirtyCount.get());
			verify(repaintManager, times(2)).setDoubleBufferMaximumSize(null);
			verify(showingRootPane, times(2)).repaint();
			verify(showingWindow, times(2)).repaint();

			verify(repaintManager, never()).markCompletelyDirty(hiddenRootPane);
			verify(hiddenRootPane, never()).repaint();
			verify(hiddenWindow, never()).repaint();
		} finally {
			handler.stopTimers();
		}
	}

	@Test
	public void testRapidWakeEventsCoalesceFollowUpRepaint() throws Exception {
		JRootPane showingRootPane = mock(JRootPane.class);
		Window showingWindow = mockRootPaneWindow(true, true, showingRootPane);
		RepaintManager repaintManager = mock(RepaintManager.class);

		CountDownLatch followUpLatch = new CountDownLatch(4);
		org.mockito.Mockito.doAnswer(invocation -> {
			followUpLatch.countDown();
			return null;
		}).when(repaintManager).markCompletelyDirty(showingRootPane);

		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler(
				() -> new Window[] { showingWindow },
				c -> repaintManager,
				120);
		try {
			SwingUtilities.invokeAndWait(() -> {
				handler.getSystemSleepListener().systemAwoke(mock(SystemSleepEvent.class));
				handler.getScreenSleepListener().screenAwoke(mock(ScreenSleepEvent.class));
				handler.getUserSessionListener().userSessionActivated(mock(UserSessionEvent.class));
			});

			// 3 immediate calls + 1 coalesced follow-up timer firing = 4 total calls
			assertEquals(true, followUpLatch.await(2, TimeUnit.SECONDS),
					"Expected coalesced follow-up repaint after rapid wake events");
			Thread.sleep(200);
			SwingUtilities.invokeAndWait(() -> { });

			verify(repaintManager, times(4)).markCompletelyDirty(showingRootPane);
		} finally {
			handler.stopTimers();
		}
	}

	@Test
	public void testForegroundRepaintsWithoutFlushingBackBuffers() throws Exception {
		JRootPane showingRootPane = mock(JRootPane.class);
		Window showingWindow = mockRootPaneWindow(true, true, showingRootPane);
		RepaintManager repaintManager = mock(RepaintManager.class);

		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler(
				() -> new Window[] { showingWindow },
				c -> repaintManager,
				50);
		try {
			handler.getAppForegroundListener().appRaisedToForeground(mock(AppForegroundEvent.class));
			SwingUtilities.invokeAndWait(() -> { });

			verify(repaintManager, never()).setDoubleBufferMaximumSize(null);
			verify(repaintManager, times(1)).markCompletelyDirty(showingRootPane);
			verify(showingRootPane, times(1)).repaint();
			verify(showingWindow, times(1)).repaint();
		} finally {
			handler.stopTimers();
		}
	}

	@Test
	public void testWindowActivatedAndDeiconifiedRepaintTargetWindow() {
		JRootPane rootPane = mock(JRootPane.class);
		Window window = mockRootPaneWindow(true, true, rootPane);
		RepaintManager repaintManager = mock(RepaintManager.class);

		OSXWindowRepaintHandler handler = new OSXWindowRepaintHandler(
				() -> new Window[] { window },
				c -> repaintManager,
				50);
		try {
			handler.handleAwtWindowEvent(new WindowEvent(window, WindowEvent.WINDOW_DEACTIVATED));
			verify(repaintManager, never()).markCompletelyDirty(any());

			handler.handleAwtWindowEvent(new WindowEvent(window, WindowEvent.WINDOW_ACTIVATED));
			verify(repaintManager, times(1)).markCompletelyDirty(rootPane);
			verify(rootPane, times(1)).repaint();
			verify(window, never()).repaint();

			handler.handleAwtWindowEvent(new WindowEvent(window, WindowEvent.WINDOW_DEICONIFIED));
			verify(repaintManager, times(2)).markCompletelyDirty(rootPane);
			verify(rootPane, times(2)).repaint();
			verify(window, times(1)).repaint();
		} finally {
			handler.stopTimers();
		}
	}

	private static Window mockRootPaneWindow(boolean displayable, boolean showing, JRootPane rootPane) {
		Window window = mock(Window.class, withSettings().extraInterfaces(RootPaneContainer.class));
		when(window.isDisplayable()).thenReturn(displayable);
		when(window.isShowing()).thenReturn(showing);
		when(((RootPaneContainer) window).getRootPane()).thenReturn(rootPane);
		return window;
	}
}
