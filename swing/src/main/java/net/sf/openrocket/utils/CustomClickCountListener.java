package net.sf.openrocket.utils;

import java.util.TimerTask;

/**
 * This class is a custom implementation of the mouse click count listener, where you can choose the maximum
 * interval between two clicks for them to still be registered as a double click.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CustomClickCountListener {
    private final int CLICK_INTERVAL; // Maximum interval between two clicks for them to still be registered as a double click (in ms)
    private int clickCnt = 0;
    private final java.util.Timer timer = new java.util.Timer("doubleClickTimer", false);

    public CustomClickCountListener() {
        this.CLICK_INTERVAL = 600;  // ms
    }

    public CustomClickCountListener(int clickInterval) {
        this.CLICK_INTERVAL = clickInterval;
    }

    /**
     * Call this method when the mouseClicked event is activated.
     */
    public void click() {
        clickCnt++;
        if (clickCnt == 1) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    clickCnt = 0;
                }
            }, CLICK_INTERVAL);
        }
    }

    /**
     * Return the current click count.
     * @return the current click count
     */
    public int getClickCount() {
        return clickCnt;
    }
}
