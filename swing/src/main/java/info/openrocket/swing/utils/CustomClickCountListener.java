package info.openrocket.swing.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is a custom implementation of the mouse click count listener, where you can choose the maximum
 * interval between two clicks for them to still be registered as a double click.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CustomClickCountListener {
    private final int clickInterval; // Maximum interval between two clicks for them to still be registered as a double click (in ms)
    private int clickCnt = 0;
    private final Timer timer;

    public CustomClickCountListener() {
        this(600);
    }

    public CustomClickCountListener(int clickInterval) {
        this(clickInterval, new Timer("doubleClickTimer", false));
    }

    /**
     * Creates a click listener using the supplied timer.
     *
     * @param clickInterval maximum interval between grouped clicks, in milliseconds
     * @param timer timer used to schedule the click-count reset
     */
    CustomClickCountListener(int clickInterval, Timer timer) {
        this.clickInterval = clickInterval;
        this.timer = timer;
    }

    /**
     * Call this method when the mouseClicked event is activated.
     */
    public synchronized void click() {
        clickCnt++;
        if (clickCnt == 1) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (CustomClickCountListener.this) {
                        clickCnt = 0;
                    }
                }
            }, clickInterval);
        }
    }

    /**
     * Return the current click count.
     * @return the current click count
     */
    public synchronized int getClickCount() {
        return clickCnt;
    }
}
